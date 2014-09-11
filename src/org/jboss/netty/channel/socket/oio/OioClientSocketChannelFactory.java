package org.jboss.netty.channel.socket.oio;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.channel.socket.SocketChannel;
import org.jboss.netty.util.ThreadNameDeterminer;
import org.jboss.netty.util.internal.ExecutorUtil;

/**��һ��ClientSocketChannelFactory ��ʵ�֣�����SocketChannel��������ʽ�Ŀͻ���ͨ����
 * ʹ�õ��Ǵ�ͳ������IO API���ص����ܵõ��õ��������͵��ӳ٣�����Ҫ������������ٵ�ʱ�򡣣���ϵNIO��OIO������
 * 
 * ��OioClientSocketChannelFactory��ֻ��һ���߳����ͣ�worker threads��ÿ�����ӵ�ͨ����һ��ר�õ�worker��
 * ���Ǵ�ͳ��IOģ�͡�
 * 
 * ��OioClientSocketChannelFactory�ڹ����ʱ����Ĳ����̳߳ض�����Executors.newCachedThreadPool()��
 * ����worker threads����Դ������Ҫ��ָ֤����Executor ���ṩ�㹻�������̡߳�
 * 
 * worker thread���ӳٻ�õģ�acquired lazily��������û������Ҫ����ʱ�ͻ��ͷ�,���и�thread��ص���Դ�����ͷţ�
 * ����Ҫ���ŵĹرշ����ȹر����е�Channel������ ChannelGroup.close()��
 * �������releaseExternalResources()�ͷ��ⲿ��Դ����
 * 
 *  Ҫע�ⲻҪ�����е�ͨ���ر�ǰȥ�ر�executor ������ᷢ��RejectedExecutionException ��
 *  ������Щ�����Դ���������ͷš�
 *  
 * ���ƣ�ͨ��OioClientSocketChannelFactory������SocketChannel ��֧���첽������
 * �κΣɣϲ��������ӣ�д�ȶ���������ʽִ�С�
 */
public class OioClientSocketChannelFactory implements ClientSocketChannelFactory {

    private final Executor workerExecutor;
    final OioClientSocketPipelineSink sink;///����
    private boolean shutdownExecutor;

    /**
     * ����һ��ʵ��
     * Ĭ�ϻ�ʹ��Executors.newCachedThreadPool()������worker thread
     */
    public OioClientSocketChannelFactory() {
        this(Executors.newCachedThreadPool());
        shutdownExecutor = true;
    }

    /**
     * Creates a new instance.
     * ָ��Executor��ִ�� I/O worker threads
     */
    public OioClientSocketChannelFactory(Executor workerExecutor) {
        this(workerExecutor, null);
    }

    /**
     * ����ThreadNameDeterminer������thread ����
     */
    public OioClientSocketChannelFactory(Executor workerExecutor,
                                         ThreadNameDeterminer determiner) {
        if (workerExecutor == null) {
            throw new NullPointerException("workerExecutor");
        }
        this.workerExecutor = workerExecutor;
        sink = new OioClientSocketPipelineSink(workerExecutor, determiner);
    }

    /**
     * ���ķ��������������ͨ�� OioClientSocketChannel
     */
    public SocketChannel newChannel(ChannelPipeline pipeline) {
        return new OioClientSocketChannel(this, pipeline, sink);
    }

    public void shutdown() {
        if (shutdownExecutor) {
            ExecutorUtil.shutdownNow(workerExecutor);
        }
    }

    /**
     * ����ExcutorService��shutdownNow����
     */
    public void releaseExternalResources() {
        ExecutorUtil.shutdownNow(workerExecutor);
    }
}
