package org.jboss.netty.channel;

import static org.jboss.netty.channel.Channels.*;

/**
 * A skeletal ChannelSink implementation.
 */
public abstract class AbstractChannelSink implements ChannelSink {

    /**
     * Creates a new instance.
     */
    protected AbstractChannelSink() {
    }

    //Sends an ExceptionEvent upstream with the specified exception cause code
    public void exceptionCaught(ChannelPipeline pipeline,
            ChannelEvent event, ChannelPipelineException cause) throws Exception {
        Throwable actualCause = cause.getCause();
        if (actualCause == null) {
            actualCause = cause;
        }
        if (isFireExceptionCaughtLater(event, actualCause)) {
            fireExceptionCaughtLater(event.getChannel(), actualCause);
        } else {
        	//����һ�� "exceptionCaught"�¼�����Channel��ˮ�ߵĵ�һ�� ChannelUpstreamHandler
            fireExceptionCaught(event.getChannel(), actualCause);
        }
    }


    //�ڴ���Event�Ĺ����з������쳣���Ƿ���һ��IO�߳��д���һ��"exceptionCaught"�¼�
    protected boolean isFireExceptionCaughtLater(ChannelEvent event, Throwable actualCause) {
        return false;
    }

    /**
     * ����ֱ�ӵ��� Runnable.run()�����������������������и��õĴ���ʽ��
     * �����������
     */
    public ChannelFuture execute(ChannelPipeline pipeline, Runnable task) {
        try {
            task.run();
            return succeededFuture(pipeline.getChannel());
        } catch (Throwable t) {
            return failedFuture(pipeline.getChannel(), t);
        }
    }
}
