package org.jboss.netty.channel;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;

import java.util.List;


/**
 * ���ڲ�ͬ���¼����Ͷ��ṩ�˷����������������ת����Щ���յ��� upstream event��
 * �õ�����������������ͣ���������ʵ��Ĵ���������Щ���������ֺ���ChannelEvent�ж�Ӧ���¼�������һ���ġ�
 */
public class SimpleChannelUpstreamHandler implements ChannelUpstreamHandler {

    private static final InternalLogger logger =
        InternalLoggerFactory.getInstance(SimpleChannelUpstreamHandler.class.getName());

    /**
     * ����ת�ͣ�����ִ�С�
     * Down-casts the received upstream event into more meaningful sub-type 
     * event and calls an appropriate handler method with the down-casted event.
     */
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {

        if (e instanceof MessageEvent) {
            messageReceived(ctx, (MessageEvent) e);
        } else if (e instanceof WriteCompletionEvent) {
            WriteCompletionEvent evt = (WriteCompletionEvent) e;
            writeComplete(ctx, evt);
        } else if (e instanceof ChildChannelStateEvent) {
            ChildChannelStateEvent evt = (ChildChannelStateEvent) e;
            if (evt.getChildChannel().isOpen()) {
                childChannelOpen(ctx, evt);
            } else {
                childChannelClosed(ctx, evt);
            }
        } else if (e instanceof ChannelStateEvent) {
            ChannelStateEvent evt = (ChannelStateEvent) e;
            switch (evt.getState()) {
            case OPEN:
                if (Boolean.TRUE.equals(evt.getValue())) {
                    channelOpen(ctx, evt);
                } else {
                    channelClosed(ctx, evt);
                }
                break;
            case BOUND:
                if (evt.getValue() != null) {
                    channelBound(ctx, evt);
                } else {
                    channelUnbound(ctx, evt);
                }
                break;
            case CONNECTED:
                if (evt.getValue() != null) {
                    channelConnected(ctx, evt);
                } else {
                    channelDisconnected(ctx, evt);
                }
                break;
            case INTEREST_OPS:
                channelInterestChanged(ctx, evt);
                break;
            default:
                ctx.sendUpstream(e);
            }
        } else if (e instanceof ExceptionEvent) {
            exceptionCaught(ctx, (ExceptionEvent) e);
        } else {
            ctx.sendUpstream(e);
        }
    }

    /**
     * ���Ӵ�Զ���յ�һ����Ϣʵ�壨��ChannelBuffer����ʱ��ִ�С�
     */
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        ctx.sendUpstream(e);
    }

    /**
     * ��IO�̻߳�ChannelHandler�����쳣ʱ��
     */
    public void exceptionCaught(
            ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        ChannelPipeline pipeline = ctx.getPipeline();

        ChannelHandler last = pipeline.getLast();
        if (!(last instanceof ChannelUpstreamHandler) && ctx instanceof DefaultChannelPipeline) {
            // The names comes in the order of which they are insert when using DefaultChannelPipeline
            List<String> names = ctx.getPipeline().getNames();
            for (int i = names.size() - 1; i >= 0; i--) {
                ChannelHandler handler = ctx.getPipeline().get(names.get(i));
                if (handler instanceof ChannelUpstreamHandler) {
                    // find the last handler
                    last = handler;
                    break;
                }
            }
        }
        if (this == last) {
            logger.warn(
                    "EXCEPTION, please implement " + getClass().getName() +
                    ".exceptionCaught() for proper handling.", e.getCause());
        }
        ctx.sendUpstream(e);
    }

    /**
     * ͨ����ʱ��û�а󶨻����ӡ�
     * ע����ǣ�����¼���IO�߳��ڲ������ģ������Binder�������ǲ���������ִ�и��Ӳ�����heavy operation��
     * ���������������worker�ַ���
     */
    public void channelOpen( ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        ctx.sendUpstream(e);
    }

    /**
     * �򿪰󶨺�ִ�С�
     * ע��ͬ�ϡ�
     */
    public void channelBound(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        ctx.sendUpstream(e);
    }

    /**
     * �򿪣��󶨣�����֮��ִ�С�
     * ע��ͬ�ϡ�
     */
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        ctx.sendUpstream(e);
    }

    /**
     * ��ͨ����interestOps�ı��ִ�С�
     */
    public void channelInterestChanged(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        ctx.sendUpstream(e);
    }

    /**
     * �Ͽ���Զ�˵�����֮ʱ��
     */
    public void channelDisconnected(  ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        ctx.sendUpstream(e);
    }

    /**
     * Invoked when a Channel was unbound from the current local address.
     */
    public void channelUnbound(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        ctx.sendUpstream(e);
    }

    /**
     * Invoked when a Channel was closed and all its related resources
     * were released.
     */
    public void channelClosed(
            ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        ctx.sendUpstream(e);
    }

    /**
     * Invoked when something was written into a Channel.
     */
    public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent e) throws Exception {
        ctx.sendUpstream(e);
    }

    /**
     * ����ͨ����ʱ������˵a server channel accepted a connection
     */
    public void childChannelOpen(ChannelHandlerContext ctx, ChildChannelStateEvent e) throws Exception {
        ctx.sendUpstream(e);
    }

    /**
     * ��ͨ���ر�ʱ�����������׽��ֹرա�
     */
    public void childChannelClosed(ChannelHandlerContext ctx, ChildChannelStateEvent e) throws Exception {
        ctx.sendUpstream(e);
    }
}
