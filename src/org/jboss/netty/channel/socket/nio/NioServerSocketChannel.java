package org.jboss.netty.channel.socket.nio;

import static org.jboss.netty.channel.Channels.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

import org.jboss.netty.channel.AbstractServerChannel;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelSink;
import org.jboss.netty.channel.socket.DefaultServerSocketChannelConfig;
import org.jboss.netty.channel.socket.ServerSocketChannelConfig;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;

class NioServerSocketChannel extends AbstractServerChannel
                             implements org.jboss.netty.channel.socket.ServerSocketChannel {

    private static final InternalLogger logger =
        InternalLoggerFactory.getInstance(NioServerSocketChannel.class);

    final ServerSocketChannel socket;  // ���ķ�����
    final Boss boss;
    final WorkerPool<NioWorker> workerPool;  //�̳߳�

    private final ServerSocketChannelConfig config;

    NioServerSocketChannel(
            ChannelFactory factory,
            ChannelPipeline pipeline,
            ChannelSink sink, Boss boss, WorkerPool<NioWorker> workerPool) {

        super(factory, pipeline, sink);
        this.boss = boss;
        this.workerPool = workerPool;
        try {
            socket = ServerSocketChannel.open();
        } catch (IOException e) {
            throw new ChannelException("Failed to open a server socket.", e);
        }

        try {
        	// ������
            socket.configureBlocking(false);
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException e2) {
                if (logger.isWarnEnabled()) {
                    logger.warn( "Failed to close a partially initialized socket.", e2);
                }
            }

            throw new ChannelException("Failed to enter non-blocking mode.", e);
        }

        config = new DefaultServerSocketChannelConfig(socket.socket());

        //�ܹؼ����ڴ�����ͨ����ִ��ʵ�ʵ��׽���open������ͻἤ��Open Event�����������Binder�ͻ��յ�������ʵ�ʴ���
        fireChannelOpen(this);
    }

    public ServerSocketChannelConfig getConfig() {
        return config;
    }

    public InetSocketAddress getLocalAddress() {
        return (InetSocketAddress) socket.socket().getLocalSocketAddress();
    }

    public InetSocketAddress getRemoteAddress() {
        return null;
    }

    public boolean isBound() {
        return isOpen() && socket.socket().isBound();
    }

    @Override
    protected boolean setClosed() {
        return super.setClosed();
    }
}
