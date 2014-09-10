package org.jboss.netty.channel.socket.oio;

import static org.jboss.netty.channel.Channels.*;

import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.Socket;

import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelSink;

//ע�����Ȩ����package
class OioClientSocketChannel extends OioSocketChannel {

    volatile PushbackInputStream in;
    volatile OutputStream out;

    OioClientSocketChannel(
            ChannelFactory factory,
            ChannelPipeline pipeline,
            ChannelSink sink) {

        super(null, factory, pipeline, sink, new Socket());

        //����"channelOpen"�¼�����ͨ������ˮ���е�һ��ChannelUpstreamHandler
    	//������Channel��parent����ôһ��"childChannelOpen"�¼�Ҳ�ᷢ��
        fireChannelOpen(this);//-->Channels
    }

    @Override
    PushbackInputStream getInputStream() {
        return in;
    }

    @Override
    OutputStream getOutputStream() {
        return out;
    }
}
