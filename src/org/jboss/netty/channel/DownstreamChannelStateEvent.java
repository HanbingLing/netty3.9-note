package org.jboss.netty.channel;

/**
 * The default downstream ChannelStateEvent implementation.
 */
public class DownstreamChannelStateEvent implements ChannelStateEvent {

    private final Channel channel;
    private final ChannelFuture future;
    private final ChannelState state;
    private final Object value;

    //和UpstreamChannelStateEvent不同的是，多了一个Future对象，需要合适的时候得到通知
    public DownstreamChannelStateEvent(
            Channel channel, ChannelFuture future,
            ChannelState state, Object value) {

        if (channel == null) {
            throw new NullPointerException("channel");
        }
        if (future == null) {
            throw new NullPointerException("future");
        }
        if (state == null) {
            throw new NullPointerException("state");
        }
        this.channel = channel;
        this.future = future;
        this.state = state;
        this.value = value;
    }

    public Channel getChannel() {
        return channel;
    }

    public ChannelFuture getFuture() {
        return future;
    }

    public ChannelState getState() {
        return state;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        String channelString = getChannel().toString();
        StringBuilder buf = new StringBuilder(channelString.length() + 64);
        buf.append(channelString);
        switch (getState()) {
        case OPEN:
            if (Boolean.TRUE.equals(getValue())) {
                buf.append(" OPEN");
            } else {
                buf.append(" CLOSE");
            }
            break;
        case BOUND:
            if (getValue() != null) {
                buf.append(" BIND: ");
                buf.append(getValue());
            } else {
                buf.append(" UNBIND");
            }
            break;
        case CONNECTED:
            if (getValue() != null) {
                buf.append(" CONNECT: ");
                buf.append(getValue());
            } else {
                buf.append(" DISCONNECT");
            }
            break;
        case INTEREST_OPS:
            buf.append(" CHANGE_INTEREST: ");
            buf.append(getValue());
            break;
        default:
            buf.append(' ');
            buf.append(getState().name());
            buf.append(": ");
            buf.append(getValue());
        }
        return buf.toString();
    }
}
