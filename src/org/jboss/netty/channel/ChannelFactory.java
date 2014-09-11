package org.jboss.netty.channel;

import org.jboss.netty.util.ExternalResourceReleasable;


/**
 * ���Ǻʹ�������Ҫ�ӿڣ�����һ���;���ͨ��ʵ�壨�������׽��֣�������Channel��
 * ����˵NioServerSocketChannelFactory������ͨ������л���NIO��server socket���ײ��ͨ��ʵ�塣
 * һ��һ���µ�Channel�����ˣ���ôͨ������ָ�����Ǹ�ChannelPipeline �ͻῪʼ����ChannelEvents��
 */
public interface ChannelFactory extends ExternalResourceReleasable {

    /**
     *��������һ���µ�Channel��������һ��pipeline
     *���ʧ�ܻ��׳�ChannelException
     */
    Channel newChannel(ChannelPipeline pipeline);

    /**
     * �ر�ChannelFactory�����ڲ���������Դ
     */
    void shutdown();

    /**
     * �ͷ����factory�����������ⲿ��Դ�������������factory�Լ������ģ�
     * ����˵��ChannelFactory���췽��ʱָ����Executor���󣬾���Ҫ���������������ͷŹ���
     * ���������һ���򿪵�Channel��ȴ���ⲿ��Դ�ͷ��ˣ��ͻᷢ������Ľ��
     * ͬ����Ҫ�ȵ���shutdown()
     */
    void releaseExternalResources();
}
