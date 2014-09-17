package org.jboss.netty.channel.socket.nio;

import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.util.ExternalResourceReleasable;
import org.jboss.netty.util.internal.ExecutorUtil;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractNioBossPool<E extends Boss>
        implements BossPool<E>, ExternalResourceReleasable {

    /**
     * The boss pool raises an exception unless all boss threads start and run within this timeout (in seconds.)
     */
    private static final int INITIALIZATION_TIMEOUT = 10;

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractNioBossPool.class);

    private final Boss[] bosses;
    private final AtomicInteger bossIndex = new AtomicInteger();
    private final Executor bossExecutor;
    private volatile boolean initialized; // ״̬���Ʊ���

    /**
     * ��������ִ������boss�ĸ������Ƿ��Զ���ʼ����
     */
    AbstractNioBossPool(Executor bossExecutor, int bossCount) {
        this(bossExecutor, bossCount, true);
    }

    AbstractNioBossPool(Executor bossExecutor, int bossCount, boolean autoInit) {
        if (bossExecutor == null) {
            throw new NullPointerException("bossExecutor");
        }
        if (bossCount <= 0) {
            throw new IllegalArgumentException(
                    "bossCount (" + bossCount + ") " +
                            "must be a positive integer.");
        }
        // ����Boss�������ڴ档
        bosses = new Boss[bossCount];
        this.bossExecutor = bossExecutor;
        if (autoInit) {
            init();
        }
    }

    protected void init() {
        if (initialized) {
            throw new IllegalStateException("initialized already");
        }
        initialized = true;

        for (int i = 0; i < bosses.length; i++) {
        	// ������ЩBoss���о�����ʵ�֣�ʵ���Ͼ��� NioServerBoss ����
        	// ����NioServerBoss ���������openSelector�򿪶�Ӧ��Selector������Ҫ�г�ʱ���ơ�
            bosses[i] = newBoss(bossExecutor);
        }

        waitForBossThreads();
    }

    private void waitForBossThreads() {
    	// Java�������ʵ���� 
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(INITIALIZATION_TIMEOUT);
        boolean warn = false;
        for (Boss boss: bosses) { //NioServerBoss extends AbstractNioSelector
        	// ��������������
            if (!(boss instanceof AbstractNioSelector)) {
                continue;
            }

            AbstractNioSelector selector = (AbstractNioSelector) boss;
            long waitTime = deadline - System.nanoTime();
            try {
                if (waitTime <= 0) { // ��ʼ���׶γ�ʱ
                    if (selector.thread == null) {
                        warn = true;
                        break;
                    }
                } else if (!selector.startupLatch.await(waitTime, TimeUnit.NANOSECONDS)) {
                	// �ȴ����е��̶߳��ﵽ������� false if the waiting time elapsed before the count reached zero
                    warn = true;
                    break;
                }
            } catch (InterruptedException ignore) {
                // Stop waiting for the boss threads and let someone else take care of the interruption.
            	// �ָ��жϣ����ϲ������д���
                Thread.currentThread().interrupt();
                break;
            }
        }

        if (warn) {
            logger.warn(
                    "Failed to get all boss threads ready within " + INITIALIZATION_TIMEOUT + " second(s). " +
                    "Make sure to specify the executor which has more threads than the requested bossCount. " +
                    "If unsure, use Executors.newCachedThreadPool().");
        }
    }

    /**
     * ����һ���µ�Boss������Executor��ִ��IO������
     */
    protected abstract E newBoss(Executor executor);

    @SuppressWarnings("unchecked")
    public E nextBoss() {
        return (E) bosses[Math.abs(bossIndex.getAndIncrement() % bosses.length)];
    }

    public void rebuildSelectors() {
        for (Boss boss: bosses) {
            boss.rebuildSelector();
        }
    }

    public void releaseExternalResources() {
        shutdown();
        ExecutorUtil.shutdownNow(bossExecutor);
    }

    public void shutdown() {
        for (Boss boss: bosses) {
            boss.shutdown();
        }
    }
}
