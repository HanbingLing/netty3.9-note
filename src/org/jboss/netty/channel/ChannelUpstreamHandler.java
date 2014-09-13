package org.jboss.netty.channel;

import java.util.concurrent.Executor;

import org.jboss.netty.handler.execution.ExecutionHandler;


/**
 * ChannelUpstreamHandler�������е�ͨ���¼�����������ˮ���д����¼���
 * ����ӿ���õĳ���������IO�����ֳ��������¼���������Ϣ����ִ����ص�ҵ���߼���
 * �ڴ󲿷�����£�������ʹ��SimpleChannelUpstreamHandler ��ʵ��һ��
 * �����upstream handler����Ϊ��Ϊÿ���¼������ṩ�˵����Ĵ�������
 * ����������ChannelUpstreamHandler �������η����¼�����Ȼ���������¼�Ҳ������ģ���������ݣ���
 */
public interface ChannelUpstreamHandler extends ChannelHandler {

    /**
     * �������ͨ���¼���
     */
    void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception;
}
