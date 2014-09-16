package org.jboss.netty.channel.socket.nio;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import java.nio.channels.Selector;


/**
 * �����NIO selector������ͼ���epoll���Ǹ�bug����������һ��rebuildSelector������
 */
public interface NioSelector extends Runnable {

    void register(Channel channel, ChannelFuture future);

    /**
     * Replaces the current {@link Selector} with a new {@link Selector} to work around the infamous epoll 100% CPU
     * bug.
     */
    void rebuildSelector();

    void shutdown();
}
