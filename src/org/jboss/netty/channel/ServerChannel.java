package org.jboss.netty.channel;


//������Ϊ�����׽���ͨ��
//A Channel that accepts an incoming connection attempt and creates 
//its child Channels by accepting them. ServerSocketChannel is a good example.
public interface ServerChannel extends Channel {
    // This is a tag interface.
}
