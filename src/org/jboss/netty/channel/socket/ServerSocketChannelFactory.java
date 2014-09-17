package org.jboss.netty.channel.socket;

import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ServerChannelFactory;

/**
 * ����ӿںܼ򵥣����Ǹ����ض���ChannelPipeline����һ���µ�ServerSocketChannel��
 */
public interface ServerSocketChannelFactory extends ServerChannelFactory {
    ServerSocketChannel newChannel(ChannelPipeline pipeline);
}
