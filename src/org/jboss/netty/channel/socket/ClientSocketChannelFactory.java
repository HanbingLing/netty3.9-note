package org.jboss.netty.channel.socket;

import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;

/**
 * �����ͻ���SocketChannel��ChannelFactory
 */
public interface ClientSocketChannelFactory extends ChannelFactory {
    SocketChannel newChannel(ChannelPipeline pipeline);
}
