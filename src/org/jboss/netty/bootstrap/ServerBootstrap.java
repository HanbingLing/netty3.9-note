package org.jboss.netty.bootstrap;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelConfig;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ChildChannelStateEvent;
import org.jboss.netty.channel.DefaultChannelFuture;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.ServerChannelFactory;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.jboss.netty.channel.Channels.*;

/**
 * ��ǰ��ClientBootstrap��Ӧ�ģ������Ƿ������˵���������ServerBootsrapֻ������������ӵĴ��䣬
 * ��TCP��local transport���������UDP�����ӵĴ���Ӧ����ConnectionlessBootstrap��
 * ��Ϊ����UDP���ԶԵ������������ֱ�ӽ�����Ϣ�������Ǵ���һ����ͨ��������ÿ���ͻ���
 * �ڷ������ˣ�һ����ͨ��ָ���Ǽ����׽��ֶ�Ӧ��ͨ����������������parent channel ��ͨ��
 * ��bootstrap��ChannelFactory ����bind�����ɵġ�һ���ɹ��󶨣�
 * ���parent Channel�Ϳ��Խ������󣬶�Ӧ�ľʹ�����ͨ����
 */
public class ServerBootstrap extends Bootstrap {

    private volatile ChannelHandler parentHandler;

    /**
     * Creates a new instance with no ChannelFactory  set.
     *  setFactory(ChannelFactory)  must be called before any I/O operation is requested.
     */
    public ServerBootstrap() {
    }

    /**
     * Creates a new instance with the specified initial ChannelFactory.
     */
    public ServerBootstrap(ChannelFactory channelFactory) {
        super(channelFactory);
    }

    /**
     * ����server�˵�ServerChannelFactory������ִ��IO������
     * �÷���ֻ�ܱ�����һ�Σ�������캯��ָ���ˣ��Ͳ����ٴ��趨��
     */
    @Override
    public void setFactory(ChannelFactory factory) {
        if (factory == null) {
            throw new NullPointerException("factory");
        }
        if (!(factory instanceof ServerChannelFactory)) {
            throw new IllegalArgumentException(
                    "factory must be a " +
                    ServerChannelFactory.class.getSimpleName() + ": " +
                    factory.getClass());
        }
        super.setFactory(factory);
    }

    /**
     * Returns an optional {@link ChannelHandler} which intercepts an event
     * of a newly bound server-side channel which accepts incoming connections.
     *
     * @return the parent channel handler.
     *         {@code null} if no parent channel handler is set.
     */
    public ChannelHandler getParentHandler() {
        return parentHandler;
    }

    /**
     * Sets an optional {@link ChannelHandler} which intercepts an event of
     * a newly bound server-side channel which accepts incoming connections.
     *
     * @param parentHandler
     *        the parent channel handler.
     *        {@code null} to unset the current parent channel handler.
     */
    public void setParentHandler(ChannelHandler parentHandler) {
        this.parentHandler = parentHandler;
    }

    /**
     * ����һ��Channel����󶨵�ѡ��"localAddress"ָ�����׽��ֵ�ַ��
     */
    public Channel bind() {
        SocketAddress localAddress = (SocketAddress) getOption("localAddress");
        if (localAddress == null) {
            throw new IllegalStateException("localAddress option is not set.");
        }
        return bind(localAddress);
    }

    /**
     * ͬ�ϣ�ֻ�ǰ󶨵�ʱ��ָ����ַ��
     */
    public Channel bind(final SocketAddress localAddress) {
        ChannelFuture future = bindAsync(localAddress);

        // Wait for the future.
        future.awaitUninterruptibly();// �ȴ�ChannelFuture��ɡ�
        if (!future.isSuccess()) {
            future.getChannel().close().awaitUninterruptibly();
            throw new ChannelException("Failed to bind to: " + localAddress, future.getCause());
        }

        return future.getChannel();
    }

    /**
     * Bind a channel asynchronous to the local address
     * specified in the current {@code "localAddress"} option.  This method is
     * similar to the following code:
     *
     * <pre>
     * {@link ServerBootstrap} b = ...;
     * b.bindAsync(b.getOption("localAddress"));
     * </pre>
     *
     *
     * @return a new {@link ChannelFuture} which will be notified once the Channel is
     * bound and accepts incoming connections
     *
     * @throws IllegalStateException
     *         if {@code "localAddress"} option was not set
     * @throws ClassCastException
     *         if {@code "localAddress"} option's value is
     *         neither a {@link SocketAddress} nor {@code null}
     * @throws ChannelException
     *         if failed to create a new channel and
     *                      bind it to the local address
     */
    public ChannelFuture bindAsync() {
        SocketAddress localAddress = (SocketAddress) getOption("localAddress");
        if (localAddress == null) {
            throw new IllegalStateException("localAddress option is not set.");
        }
        return bindAsync(localAddress);
    }

    /**
     * �첽����ִ��ͨ���󶨣�
     * Bind a channel asynchronous to the specified local address.
     *
     *���ص�ChannelFuture������ͨ���󶨳ɹ������Խ����������󣩺�õ�֪ͨ��
     *
     */
    public ChannelFuture bindAsync(final SocketAddress localAddress) {
        if (localAddress == null) {
            throw new NullPointerException("localAddress");
        }
        Binder binder = new Binder(localAddress);
        ChannelHandler parentHandler = getParentHandler();

        ChannelPipeline bossPipeline = pipeline(); //����һ��Ĭ�ϵ�Pipeline��
        // ע����������ͨ����ˮ��������һ��binder����
        bossPipeline.addLast("binder", binder);
        
        if (parentHandler != null) {
            bossPipeline.addLast("userHandler", parentHandler);
        }

        // ������ͨ���������׽���ͨ������������pipeline��bossPipeline��
        // ���� NioServerSocketChannel(this, pipeline, sink, bossPool.nextBoss(), workerPool);
        Channel channel = getFactory().newChannel(bossPipeline);
        // ����һ������ȡ���İ�Future
        final ChannelFuture bfuture = new DefaultChannelFuture(channel, false);
        
        // ��Binder�����е�Future����һ���۲��ߣ�����Ǹ�Future�ɹ��Ļ��������bfuture�ͻ�ɹ�
        // ���Ի��ǿ��Ǳߵ� ��ʱ�ɹ���
        binder.bindFuture.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    bfuture.setSuccess();
                } else {
                    // Call close on bind failure
                    bfuture.getChannel().close();
                    bfuture.setFailure(future.getCause());
                }
            }
        });
        return bfuture;
    }

    private final class Binder extends SimpleChannelUpstreamHandler {

        private final SocketAddress localAddress;
        private final Map<String, Object> childOptions =   new HashMap<String, Object>();
        private final DefaultChannelFuture bindFuture = new DefaultChannelFuture(null, false);
        
        Binder(SocketAddress localAddress) {
            this.localAddress = localAddress;
        }

        /**
         * �� handleUpstream �����л����
         */
        @Override
        public void channelOpen( ChannelHandlerContext ctx, ChannelStateEvent evt) {

            try {
            	// �ڲ�����Է����ⲿ��ķ��� ���� getPipelineFactory()��
                evt.getChannel().getConfig().setPipelineFactory(getPipelineFactory());

                // Split options into two categories: parent and child.
                Map<String, Object> allOptions = getOptions();
                Map<String, Object> parentOptions = new HashMap<String, Object>();
                for (Entry<String, Object> e: allOptions.entrySet()) {
                    if (e.getKey().startsWith("child.")) {
                        childOptions.put( e.getKey().substring(6), e.getValue());
                    } else if (!"pipelineFactory".equals(e.getKey())) {
                        parentOptions.put(e.getKey(), e.getValue());
                    }
                }

                // Apply parent options.
                evt.getChannel().getConfig().setOptions(parentOptions);
            } finally {
                ctx.sendUpstream(evt);
            }

            //============ �����İ��׽��� =======
            evt.getChannel().bind(localAddress).addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                    	// ���ﴥ���� bind�ĳɹ�
                        bindFuture.setSuccess();
                    } else {
                        bindFuture.setFailure(future.getCause());
                    }
                }
            });
        }

        @Override
        public void childChannelOpen(
                ChannelHandlerContext ctx,
                ChildChannelStateEvent e) throws Exception {
            // Apply child options.
            try {
                e.getChildChannel().getConfig().setOptions(childOptions);
            } catch (Throwable t) {
                fireExceptionCaught(e.getChildChannel(), t);
            }
            ctx.sendUpstream(e);
        }

        @Override
        public void exceptionCaught(
                ChannelHandlerContext ctx, ExceptionEvent e)
                throws Exception {
            bindFuture.setFailure(e.getCause());
            ctx.sendUpstream(e);
        }
    }
}
