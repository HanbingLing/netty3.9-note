package org.jboss.netty.channel;

import java.util.List;
import java.util.Map;


/**
 * ChannelPipeline�����þ�����֯һϵ�е�ChannelHandlers Ϊĳһ��Channel���񣬴�������¼���
 * ʵ�������ع������ĸ߼���ʽ��an advanced form of the Intercepting Filter pattern����
 * ������Ч������δ���һ���¼��Լ�ChannelHandlers֮����ν�����
 */
public interface ChannelPipeline {

    /**
     * ��ͷ���������ChannelHandler��
     * ���ͬ����Handler�Ѿ����ڣ�IllegalArgumentException
     * ���ĳ������Ϊnull��NullPointerException
     */
    void addFirst(String name, ChannelHandler handler);

    /**
     * β������
     */
    void addLast(String name, ChannelHandler handler);

    /**
     * �����ָ����baseName Handlerǰ�����
     * baseNameָ����Handler���ڣ�NoSuchElementException��
     * ʣ�µ������쳣ͬ���棻
     */
    void addBefore(String baseName, String name, ChannelHandler handler);

    /**
     * �����ָ����baseName Handler�������
     */
    void addAfter(String baseName, String name, ChannelHandler handler);

    /**
     * Removes the specified ChannelHandler from this pipeline.
     */
    void remove(ChannelHandler handler);

    /**
     * �Ƴ���Ϊname��Handler��������
     */
    ChannelHandler remove(String name);

    /**
     * �Ƴ�ĳ�����͵�Handler�����ͷ���
     */
    <T extends ChannelHandler> T remove(Class<T> handlerType);

    /**
     * Removes the first/last ChannelHandler in this pipeline.
     *���pipelineΪ�յĻ���NoSuchElementException��
     */
    ChannelHandler removeFirst();
    ChannelHandler removeLast();

    /**
     * Handler���滻
     */
    void replace(ChannelHandler oldHandler, String newName, ChannelHandler newHandler);

    /**
     * Handler���滻��
     * ֻ��������ɵ�Handler������ָ����
     */
    ChannelHandler replace(String oldName, String newName, ChannelHandler newHandler);

    /**
     * �滻ĳ�����͵�Handler��
     */
    <T extends ChannelHandler> T replace(Class<T> oldHandlerType, String newName, ChannelHandler newHandler);

    /**
     * ���ֻ�Handler�ķ�������һ��
     */
    ChannelHandler getFirst();

    /**
     * ���һ��
     */
    ChannelHandler getLast();

    /**
     * ��Handler֮��
     */
    ChannelHandler get(String name);

    /**
     * ��HandlerClass������
     */
    <T extends ChannelHandler> T get(Class<T> handlerType);

    /**
     * �õ����Handler��context
     */
    ChannelHandlerContext getContext(ChannelHandler handler);

    /**
     * �����õ����Handler��context
     */
    ChannelHandlerContext getContext(String name);

    /**
     * �������͵õ����Handler��context
     */
    ChannelHandlerContext getContext(Class<? extends ChannelHandler> handlerType);

    /**
     * ����һ���¼�������ˮ�ߵ�һ��ChannelUpstreamHandler��
     * ��Ϊ���pipeline���͵�
     */
    void sendUpstream(ChannelEvent e);

    /**
     *  ����һ���¼�������ˮ�ߵ�һ��ChannelDownstreamHandler��
     * ��Ϊ���pipeline���͵�
     */
    void sendDownstream(ChannelEvent e);

    /**
     * ����һ������Runnable���󣩸����pipeline��IO�߳�ȥִ�У�
     */
    ChannelFuture execute(Runnable task);

    /**
     * ������Channel��
     */
    Channel getChannel();

    /**
     * ������ChannelSink��
     */
    ChannelSink getSink();

    /**
     * ����Pipeline���õ��ض���ͨ����ChannelSink��
     * һ�����ã��������õġ�
     * Attaches this pipeline to the specified Channel and
     * ChannelSink.  Once a pipeline is attached, it can't be detached
     * nor attached again.
     *
     * �׳�IllegalStateException ������pipeline�Ѿ���ӵ�ĳ��channelSink��
     */
    void attach(Channel channel, ChannelSink sink);

    /**
     * �ж����Pipeline�Ƿ��Ѽ���ĳ��Channel
     */
    boolean isAttached();

    /**
     * �������Handler������
     */
    List<String> getNames();

    /**
     * �������Handler����Ϣ�����֣�Handler ӳ����������TreeMap;
     */
    Map<String, ChannelHandler> toMap();
}
