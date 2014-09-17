package org.jboss.netty.channel.socket.nio;

import java.nio.channels.Selector;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.socket.ServerSocketChannel;
import org.jboss.netty.channel.socket.ServerSocketChannelFactory;
import org.jboss.netty.util.ExternalResourceReleasable;

/**
 * ����ʵ����ServerSocketChannelFactory �����������˵Ļ���NIO��ServerSocketChannel��
 * ���Ը�Ч�Ĵ����������ӡ��������������͵��̣߳�Boss threads �� Worker threads�����Զ�Ӧ���������̳߳ط���
 * ��ÿ���󶨵�ServerSocketChannel ���� �Լ���boss thread������˵��������ͬ�˿ڵ�server
 * �ͻ��Ӧ����boss threads��Boss thread�������ǽ��ܵ������������ֱ������˿ڽ�󶨡�
 * һ�����ӽ��ܳɹ�������accept�ɹ����أ������ boss thread �ͻὫ������ܵ������׽���ͨ��
 * ���ݸ�NioServerSocketChannelFactory �������ĳ��worker �߳�������һ��worker thread
 * ��ְ�����Ϊһ������ͨ��ִ�з������Ķ�д����
 */
public class NioServerSocketChannelFactory implements ServerSocketChannelFactory {

    private final WorkerPool<NioWorker> workerPool;
    private final NioServerSocketPipelineSink sink;
    private final BossPool<NioServerBoss> bossPool;
    private boolean releasePools;

    /**
     * ���ֹ�����������boss �� worker�̳߳ص����ͺʹ�С��
     */
    public NioServerSocketChannelFactory() {
        this(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
        releasePools = true;
    }

    public NioServerSocketChannelFactory(
            Executor bossExecutor, Executor workerExecutor) {
        this(bossExecutor, workerExecutor, getMaxThreads(workerExecutor));
    }

  
    public NioServerSocketChannelFactory(
            Executor bossExecutor, Executor workerExecutor,
            int workerCount) {
        this(bossExecutor, 1, workerExecutor, workerCount);
    }

    public NioServerSocketChannelFactory(
            Executor bossExecutor, int bossCount, Executor workerExecutor,
            int workerCount) {
        this(bossExecutor, bossCount, new NioWorkerPool(workerExecutor, workerCount));
    }

    public NioServerSocketChannelFactory(
            Executor bossExecutor, WorkerPool<NioWorker> workerPool) {
        this(bossExecutor, 1 , workerPool);
    }

    public NioServerSocketChannelFactory(
            Executor bossExecutor, int bossCount, WorkerPool<NioWorker> workerPool) {
        this(new NioServerBossPool(bossExecutor, bossCount, null), workerPool);
    }

    /**
     * �����Ĺ��������������������������
     * Ҳ����˵��Excutor��ܵ���Щ�̳߳صĻ�����������һ���װ��ʹ������������ְ�������
     * ������BossPool����WorkerPool��ʵ������϶�����Excutor��Ա���ǲ�����������ִ�еĵط���
     * ͬʱ���̳߳ػ��������ͣ���ô������ȻҲҪ�������֣���������� NioServerBoss �� NioWorker��
     * BossPool which will be used to obtain the NioServerBoss that execute
     *        the I/O for accept new connections
     * WorkerPool which will be used to obtain the NioWorker that execute
     *        the I/O worker threads
     */
    public NioServerSocketChannelFactory(BossPool<NioServerBoss> bossPool, WorkerPool<NioWorker> workerPool) {
        if (bossPool == null) {
            throw new NullPointerException("bossExecutor");
        }
        if (workerPool == null) {
            throw new NullPointerException("workerPool");
        }
        this.bossPool = bossPool;
        this.workerPool = workerPool;
        sink = new NioServerSocketPipelineSink();
    }
    
    /**
     * ���ķ�����Ҳ�����Factory��ְ�𣬴���һ���µ�NioServerSocketChannel��
     */
    public ServerSocketChannel newChannel(ChannelPipeline pipeline) {
        return new NioServerSocketChannel(this, pipeline, sink, bossPool.nextBoss(), workerPool);
    }

    public void shutdown() {
        bossPool.shutdown();
        workerPool.shutdown();
        if (releasePools) {
            releasePools();
        }
    }

    public void releaseExternalResources() {
        bossPool.shutdown();
        workerPool.shutdown();
        releasePools();
    }

    private void releasePools() {
        if (bossPool instanceof ExternalResourceReleasable) {
            ((ExternalResourceReleasable) bossPool).releaseExternalResources();
        }
        if (workerPool instanceof ExternalResourceReleasable) {
            ((ExternalResourceReleasable) workerPool).releaseExternalResources();
        }
    }

    /**
     * Returns number of max threads for the {@link NioWorkerPool} to use. If
     * the * {@link Executor} is a {@link ThreadPoolExecutor}, check its
     * maximum * pool size and return either it's maximum or
     * {@link SelectorUtil#DEFAULT_IO_THREADS}, whichever is lower. Note that
     * {@link SelectorUtil#DEFAULT_IO_THREADS} is 2 * the number of available
     * processors in the machine.  The number of available processors is
     * obtained by {@link Runtime#availableProcessors()}.
     *
     * @param executor
     *        the {@link Executor} which will execute the I/O worker threads
     * @return
     *        number of maximum threads the NioWorkerPool should use
     */
    private static int getMaxThreads(Executor executor) {
        if (executor instanceof ThreadPoolExecutor) {
            final int maxThreads = ((ThreadPoolExecutor) executor).getMaximumPoolSize();
            return Math.min(maxThreads, SelectorUtil.DEFAULT_IO_THREADS);
        }
        return SelectorUtil.DEFAULT_IO_THREADS;
    }
}
