package org.jboss.netty.channel.socket.nio;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;

import java.net.ConnectException;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;

import static org.jboss.netty.channel.Channels.*;

class NioClientSocketPipelineSink extends AbstractNioChannelSink {

    static final InternalLogger logger =
        InternalLoggerFactory.getInstance(NioClientSocketPipelineSink.class);

    private final BossPool<NioClientBoss> bossPool;

    NioClientSocketPipelineSink(BossPool<NioClientBoss> bossPool) {
        this.bossPool = bossPool;
    }

    public void eventSunk(
            ChannelPipeline pipeline, ChannelEvent e) throws Exception {
        if (e instanceof ChannelStateEvent) {
            ChannelStateEvent event = (ChannelStateEvent) e;
            NioClientSocketChannel channel =
                (NioClientSocketChannel) event.getChannel();
            ChannelFuture future = event.getFuture();
            ChannelState state = event.getState();
            Object value = event.getValue();

            switch (state) {
            case OPEN:
                if (Boolean.FALSE.equals(value)) {
                    channel.worker.close(channel, future);
                }
                break;
            case BOUND:
                if (value != null) {
                    bind(channel, future, (SocketAddress) value);
                } else {
                    channel.worker.close(channel, future);
                }
                break;
            case CONNECTED:
                if (value != null) {
                    connect(channel, future, (SocketAddress) value);
                } else {
                    channel.worker.close(channel, future);
                }
                break;
            case INTEREST_OPS:
                channel.worker.setInterestOps(channel, future, ((Integer) value).intValue());
                break;
            }
        } else if (e instanceof MessageEvent) {
            MessageEvent event = (MessageEvent) e;
            NioSocketChannel channel = (NioSocketChannel) event.getChannel();
            boolean offered = channel.writeBufferQueue.offer(event);
            assert offered;
            channel.worker.writeFromUserCode(channel);
        }
    }

    private static void bind(
            NioClientSocketChannel channel, ChannelFuture future,
            SocketAddress localAddress) {
        try {
            channel.channel.socket().bind(localAddress);
            channel.boundManually = true;
            channel.setBound();
            future.setSuccess();
            fireChannelBound(channel, channel.getLocalAddress());
        } catch (Throwable t) {
            future.setFailure(t);
            fireExceptionCaught(channel, t);
        }
    }

    private void connect(
            final NioClientSocketChannel channel, final ChannelFuture cf,
            SocketAddress remoteAddress) {
        channel.requestedRemoteAddress = remoteAddress;
        try {
        	// ʹ�� NIO SocketChannel ���������ӶԶˡ�
            if (channel.channel.connect(remoteAddress)) {
            	// ����false˵���������������������ڽ����С�
                channel.worker.register(channel, cf);
            } else {
                channel.getCloseFuture().addListener(new ChannelFutureListener() {
                    public void operationComplete(ChannelFuture f)
                            throws Exception {
                        if (!cf.isDone()) {
                            cf.setFailure(new ClosedChannelException());
                        }
                    }
                });
                cf.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                channel.connectFuture = cf;
                nextBoss().register(channel, cf);
            }

        } catch (Throwable t) {
            if (t instanceof ConnectException) {
                Throwable newT = new ConnectException(t.getMessage() + ": " + remoteAddress);
                newT.setStackTrace(t.getStackTrace());
                t = newT;
            }
            cf.setFailure(t);
            fireExceptionCaught(channel, t);
            channel.worker.close(channel, succeededFuture(channel));
        }
    }

    private NioClientBoss nextBoss() {
        return bossPool.nextBoss();
    }

}
