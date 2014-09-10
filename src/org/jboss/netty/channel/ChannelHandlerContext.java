package org.jboss.netty.channel;

public interface ChannelHandlerContext {

	// �õ����pipeline������Channel���ȼ���getPipeline().getChannel()
	Channel getChannel();

	// Handler������pipeline
	ChannelPipeline getPipeline();

	// Handler���ж�Ӧ������
	String getName();

	// �������Contextά����Handler
	ChannelHandler getHandler();

	// ��Ӧ��Handler���ͣ����Ƿ���ChannelUpstreamHandler��ChannelDownstreamHandlerʵ��
	boolean canHandleUpstream();

	boolean canHandleDownstream();

	// �����¼��������Handler
	void sendUpstream(ChannelEvent e);

	void sendDownstream(ChannelEvent e);

	Object getAttachment();

	void setAttachment(Object attachment);
}