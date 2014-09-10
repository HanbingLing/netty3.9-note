package org.jboss.netty.channel;

/*
 * ChannelSink�ӿڴ����������pipeline��NIO socket�����ĵط������պʹ�������downstream�¼���
 * �����ܣ�transport provider���ṩ������ڲ��������������������д�����ʱ�򣬲����ע��Щ��
 */
public interface ChannelSink {

    /**
     * Invoked by ChannelPipeline when a downstream ChannelEvent
     * has reached its terminal (the head of the pipeline).
     * �����Downstream Channel Event�����������ʱ��ͻ�ִ��
     */
    void eventSunk(ChannelPipeline pipeline, ChannelEvent e) throws Exception;

    /**
     * Invoked by ChannelPipeline when an exception was raised while
     * one of its ChannelHandlers process a ChannelEvent.
     */
    void exceptionCaught(ChannelPipeline pipeline, ChannelEvent e, ChannelPipelineException cause) throws Exception;

    /**
     * Execute the given Runnable later in the io-thread.
     * Some implementation may not support this and just execute it directly.
     */
    ChannelFuture execute(ChannelPipeline pipeline, Runnable task);
}
