package org.jboss.netty.channel.socket.nio;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.util.ThreadNameDeterminer;
import org.jboss.netty.util.ThreadRenamingRunnable;
import org.jboss.netty.util.internal.DeadLockProofWorker;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ConcurrentModificationException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

abstract class AbstractNioSelector implements NioSelector {

    private static final AtomicInteger nextId = new AtomicInteger();

    //ԭ�Ӳ���
    private final int id = nextId.incrementAndGet();

    /**
     * Internal Netty logger.
     */
    protected static final InternalLogger logger = InternalLoggerFactory
            .getInstance(AbstractNioSelector.class);

    private static final int CLEANUP_INTERVAL = 256; 
    // XXX Hard-coded value, but won't need customization.

    /**
     * ����������ִ��������ִ������Runnables������ͨ��ע������
     */
    private final Executor executor;

    /**
     * ���worker��������ô�ñ�����ָ��������Thread����Ϊ��Щ��������ݸñ��������ж���
     * If this worker has been started thread will be a reference to the thread
     * used when starting. i.e. the current thread when the run method is executed.
     */
    protected volatile Thread thread;

    /**
     * ������ֱ��IO�߳����������ҽ�����thread��Ϊ��Чֵ��
     * Count down to 0 when the I/O thread starts and {@link #thread} is set to non-null.
     */
    final CountDownLatch startupLatch = new CountDownLatch(1);

    /**
     * NIO���е�ѡ����Selector
     */
    protected volatile Selector selector;

    /**
     * ���� Selector.select ����������״̬������ԭ�ӱ�����
     * Boolean that controls determines if a blocked Selector.select should
     * break out of its selection process. In our case we use a timeone for
     * the select method and the select method will block for that time unless
     * waken up.
     */
    protected final AtomicBoolean wakenUp = new AtomicBoolean();

    /**
     * ���ǵĹ������С�
     */
    private final Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<Runnable>();

    private volatile int cancelledKeys; 
    // should use AtomicInteger but we just need approximation

    
    /**
     * �������ȵ��̹߳رա�
     */
    private final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private volatile boolean shutdown;

    AbstractNioSelector(Executor executor) {
        this(executor, null);
    }

    AbstractNioSelector(Executor executor, ThreadNameDeterminer determiner) {
        this.executor = executor;
        // ������� AbstractNioSelector��
        openSelector(determiner);
    }

    public void register(Channel channel, ChannelFuture future) {
    	//����һ��ע�����񣨾������ϲ�ʵ�֣���������빤�����С�
        Runnable task = createRegisterTask(channel, future);
        registerTask(task);
    }

    protected final void registerTask(Runnable task) {
        taskQueue.add(task);

        Selector selector = this.selector;

        if (selector != null) {
        	//Causes the first selection operation that has not yet returned to return immediately
            if (wakenUp.compareAndSet(false, true)) {
                selector.wakeup();
            }
        } else {
            if (taskQueue.remove(task)) {
                // the selector was null this means the Worker has already been shutdown.
                throw new RejectedExecutionException("Worker has already been shutdown");
            }
        }
    }

    protected final boolean isIoThread() {
        return Thread.currentThread() == thread;
    }

    public void rebuildSelector() {
    	// ������������߳��е���rebuildSelector����ô�ͽ�����Ϊһ��������뵽�������С�
        if (!isIoThread()) {
            taskQueue.add(new Runnable() {
                public void run() {
                    rebuildSelector();
                }
            });
            return;
        }

        final Selector oldSelector = selector;
        final Selector newSelector;

        if (oldSelector == null) {
            return;
        }

        try {
        	// �޷��ٴδ�һ��Selector��
            newSelector = SelectorUtil.open();
        } catch (Exception e) {
            logger.warn("Failed to create a new Selector.", e);
            return;
        }

        //Ȼ������е�ChannelǨ�Ƶ�����µ�Selector�ϡ�
        // Register all channels to the new Selector.
        int nChannels = 0;
        for (;;) {
            try {
                for (SelectionKey key: oldSelector.keys()) {
                    try {
                        if (key.channel().keyFor(newSelector) != null) {
                            continue;
                        }

                        int interestOps = key.interestOps();
                        key.cancel();
                        key.channel().register(newSelector, interestOps, key.attachment());
                        nChannels ++;
                    } catch (Exception e) {
                        logger.warn("Failed to re-register a Channel to the new Selector,", e);
                        close(key);
                    }
                }
            } catch (ConcurrentModificationException e) {
                // Probably due to concurrent modification of the key set.
                continue;
            }

            break;
        }

        // ���³ɹ���
        selector = newSelector;

        try {
            // time to close the old selector as everything else is registered to the new one
            oldSelector.close();
        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to close the old Selector.", t);
            }
        }

        logger.info("Migrated " + nChannels + " channel(s) to the new Selector,");
    }

    public void run() {
        thread = Thread.currentThread();
        // �򿪱�����
        startupLatch.countDown();

        int selectReturnsImmediately = 0;
        Selector selector = this.selector;

        if (selector == null) {
            return;
        }
        // use 80% of the timeout for measure
        final long minSelectTimeout = SelectorUtil.SELECT_TIMEOUT_NANOS * 80 / 100;
        boolean wakenupFromLoop = false;
        for (;;) {
            wakenUp.set(false);

            try {
                long beforeSelect = System.nanoTime();
                // �����ж��ٸ�Channel׼������
                int selected = select(selector);
                if (SelectorUtil.EPOLL_BUG_WORKAROUND && selected == 0 && !wakenupFromLoop && !wakenUp.get()) {
                	//����select������ʱ�䣻
                    long timeBlocked = System.nanoTime() - beforeSelect;

                    //���С����С��ʱʱ�����ƣ�
                    if (timeBlocked < minSelectTimeout) {
                        boolean notConnected = false;
                        // loop over all keys as the selector may was unblocked because of a closed channel
                        for (SelectionKey key: selector.keys()) {
                            SelectableChannel ch = key.channel();
                            try {
                                if (ch instanceof DatagramChannel && !ch.isOpen() ||
                                        ch instanceof SocketChannel && !((SocketChannel) ch).isConnected()) {
                                    notConnected = true;
                                    // cancel the key just to be on the safe side
                                    key.cancel();
                                }
                            } catch (CancelledKeyException e) {
                                // ignore
                            }
                        }
                        if (notConnected) {
                            selectReturnsImmediately = 0;
                        } else {
                        	//�ڳ�ʱ����֮ǰ�ͷ��أ����ҷ��صĽ����0��������ǵ���jdk epoll bug��ԭ���ۻ���
                            // returned before the minSelectTimeout elapsed with nothing select.
                            // this may be the cause of the jdk epoll(..) bug, so increment the counter
                            // which we use later to see if its really the jdk bug.
                            selectReturnsImmediately ++;
                        }
                    } else {
                    	// �ǳ�ʱ��
                        selectReturnsImmediately = 0;
                    }
                    
                    // ����jdk epoll bug��������Ҫ�滻�����Selector������
                    //Ȼ��������һ�ֵ�select����
                    if (selectReturnsImmediately == 1024) {
                        // The selector returned immediately for 10 times in a row,
                        // so recreate one selector as it seems like we hit the
                        // famous epoll(..) jdk bug.
                        rebuildSelector();
                        selector = this.selector;
                        selectReturnsImmediately = 0;
                        wakenupFromLoop = false;
                        // try to select again
                        continue;
                    }
                } else {
                    // reset counter
                    selectReturnsImmediately = 0;
                }

                /**
                 * �ڵ���selector.wakeup()֮ǰ������ִ��wakenUp.compareAndSet(false, true)��
                 * ����Сwake-up�Ŀ�������ΪSelector.wakeup()ִ�еĴ��ۺܴ�
                 * Ȼ�����ַ�������һ�־�̬����������������� wakenUp ����Ϊtrue̫���ʱ��
                 * 1��Selecttor��'wakenUp.set(false)'��'selector.select(...)'֮��������BAD����
                 * 2����'selector.select(...)'��'if (wakenUp.get()) { ... }'����ʱOK�ġ�
                 * �ڵ�һ������£�'wakenUp'����Ϊ��true������û�ж��Ǹ�select��Ч�����������ý��������Ǹ�
                 * 'selector.select(...)'����������ֱ������һ��ѭ������'wakenUp' ���ٴ���ΪFALSE��ʱ��
                 * ��ô 'wakenUp.compareAndSet(false, true)'�ͻ�ʧ�ܣ��κ��뾪��Selector�ĳ��Զ���ʧ�ܣ�
                 * ���½�������'selector.select(...)'������ν��������
                 * 
                 * Ϊ�˽��������⣬����selector.select(...)֮���ж�wakenUp��true��ʱ����������һ��
                 * selector.wakeup()��
                 * �������������˵������selector�Ĳ������ǵ�Ч�ġ�
                 */
                // It is inefficient in that it wakes up the selector for both
                // the first case (BAD - wake-up required) and the second case
                // (OK - no wake-up required).

                if (wakenUp.get()) {
                    wakenupFromLoop = true;
                    selector.wakeup();
                } else {
                    wakenupFromLoop = false;
                }

                cancelledKeys = 0;
                processTaskQueue();// ��������
                selector = this.selector; // processTaskQueue() can call rebuildSelector()

                if (shutdown) {
                    this.selector = null;

                    // process one time again
                    processTaskQueue();

                    for (SelectionKey k: selector.keys()) {
                        close(k);
                    }

                    try {
                    	// Ҫ�ر�Selector��
                        selector.close();
                    } catch (IOException e) {
                        logger.warn(
                                "Failed to close a selector.", e);
                    }
                    // �����������
                    shutdownLatch.countDown();
                    break;
                } else {
                    process(selector);
                }
            } catch (Throwable t) {
                logger.warn(
                        "Unexpected exception in the selector loop.", t);

                // Prevent possible consecutive immediate failures that lead to
                // excessive CPU consumption.
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // Ignore.
                }
            }
        }
    }

    /**
     * �������AbstractNioWorker��������Ӧ��Selector������������ע��AbstractNioChannel��
     */
    private void openSelector(ThreadNameDeterminer determiner) {
        try {
        	// ����һ��Selector��
            selector = SelectorUtil.open();
        } catch (Throwable t) {
            throw new ChannelException("Failed to create a selector.", t);
        }

        // Start the worker thread with the new Selector.
        boolean success = false;
        try {
        	//ִ����������Ӻζ����ɾ������ʵ�֡�
            DeadLockProofWorker.start(executor, newThreadRenamingRunnable(id, determiner));
            success = true;
        } finally {
            if (!success) {
                // Release the Selector if the execution fails.
                try {
                	// ���ʧ�ܵĻ��͹ر�Selector��
                    selector.close();
                } catch (Throwable t) {
                    logger.warn("Failed to close a selector.", t);
                }
                selector = null;
                // The method will return to the caller at this point.
            }
        }
        assert selector != null && selector.isOpen();
    }

    private void processTaskQueue() {
        for (;;) {
            final Runnable task = taskQueue.poll();
            if (task == null) {
                break;
            }
            task.run();
            try {
                cleanUpCancelledKeys();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    protected final void increaseCancelledKeys() {
        cancelledKeys ++;
    }

    protected final boolean cleanUpCancelledKeys() throws IOException {
        if (cancelledKeys >= CLEANUP_INTERVAL) {
            cancelledKeys = 0;
            // ������ Select��
            selector.selectNow();
            return true;
        }
        return false;
    }

    public void shutdown() {
    	// ������IO�߳��е��� ���shutdown������
        if (isIoThread()) {
            throw new IllegalStateException("Must not be called from a I/O-Thread to prevent deadlocks!");
        }

        Selector selector = this.selector;
        shutdown = true;
        if (selector != null) {
            selector.wakeup();
        }
        try {
        	// �ȴ���������򿪡�
            shutdownLatch.await();
        } catch (InterruptedException e) {
            logger.error("Interrupted while wait for resources to be released #" + id);
            Thread.currentThread().interrupt();
        }
    }

    protected abstract void process(Selector selector) throws IOException;

    // ��selector.select()�����ķ�װ��
    protected int select(Selector selector) throws IOException {
        return SelectorUtil.select(selector);
    }

    protected abstract void close(SelectionKey k);

    protected abstract ThreadRenamingRunnable newThreadRenamingRunnable(int id, ThreadNameDeterminer determiner);

    protected abstract Runnable createRegisterTask(Channel channel, ChannelFuture future);
}
