package org.jboss.netty.channel;


// ��ϸ���Ϳ����� http://blog.csdn.net/vonzhoufz/article/details/39181917
public interface ChannelEvent {

	//��������¼��Ĺ�����Channel
    Channel getChannel();

    //�����upstream event���÷������Ƿ���SucceededChannelFuture����Ϊ�Ѿ�������
    //���downstream event������IO���󣩣���ô���ص�Future�ͻ���IO����ʵ����ɺ�õ�֪ͨ
    ChannelFuture getFuture();
}
