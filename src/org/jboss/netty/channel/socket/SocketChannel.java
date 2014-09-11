package org.jboss.netty.channel.socket;

import java.net.InetSocketAddress;

import org.jboss.netty.channel.Channel;


/**
 * ����TCP/IP�׽���ͨ����������ServerSocketChannel���ն������������׽��֣�������ClientSocketChannelFactory����
 */
public interface SocketChannel extends Channel {
    SocketChannelConfig getConfig();
    InetSocketAddress getLocalAddress();
    InetSocketAddress getRemoteAddress();
}
