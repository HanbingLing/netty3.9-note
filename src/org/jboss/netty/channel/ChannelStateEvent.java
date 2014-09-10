package org.jboss.netty.channel;


public interface ChannelStateEvent extends ChannelEvent {

    //Returns the changed property of the Channel
    ChannelState getState();

     // Returns the value of the changed property of the Channel
    //��Щֵ����true��false��һ��������һ��״̬�����棬����󶨣����
    //�����ǶԶ˵��׽��ֵ�ַ
    Object getValue();
}
