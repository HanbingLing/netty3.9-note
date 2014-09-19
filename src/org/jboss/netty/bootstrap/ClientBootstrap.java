package org.jboss.netty.bootstrap;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelConfig;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineException;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * �ͻ���Channel������
 */
public class ClientBootstrap extends Bootstrap {

    /**
     * û������ChannelFactory����IO����֮ǰҪsetFactory(ChannelFactory)
     */
    public ClientBootstrap() {
    }

    /**
     * ��ָ���� ChannelFactory������ʵ��.
     */
    public ClientBootstrap(ChannelFactory channelFactory) {
        super(channelFactory);
    }

    /**
     * ͨ����"localAddress"��������connect��
     * ���"localAddress"û�� ���ã�����Զ�����
     * ���"remoteAddress"û�еĻ��ͻ��׳�IllegalStateException
     * �������ChannelPipelineʧ�ܣ���ChannelPipelineException
     */
    public ChannelFuture connect() {
        SocketAddress remoteAddress = (SocketAddress) getOption("remoteAddress");
        if (remoteAddress == null) {
            throw new IllegalStateException("remoteAddress option is not set.");
        }
        return connect(remoteAddress);
    }

    /**
     * �����ѡ���еõ���localAddress����SocketAddress���ͻ���null�ͻ��׳�ClassCastException
     * ������ͬ��
     */
    public ChannelFuture connect(SocketAddress remoteAddress) {
        if (remoteAddress == null) {
            throw new NullPointerException("remoteAddress");
        }
        SocketAddress localAddress = (SocketAddress) getOption("localAddress");
        return connect(remoteAddress, localAddress);
    }

    /**
     * ���ԺͶԶ����ӣ���� localAddress=null�������׽��ֵ�ַ���Զ����䣻
     * 
     * �����ӳ��Գɹ���ʧ�ܺ� ChannelFuture ����֪ͨ��
     * 
     *  �������ChannelPipelineʧ�ܣ���ChannelPipelineException
     */
    public ChannelFuture connect(final SocketAddress remoteAddress, final SocketAddress localAddress) {

        if (remoteAddress == null) {
            throw new NullPointerException("remoteAddress");
        }

        ChannelPipeline pipeline;
        try {
        	//����pipeline Factory�õ�һ����ˮ��
            pipeline = getPipelineFactory().getPipeline();
        } catch (Exception e) {
            throw new ChannelPipelineException("Failed to initialize a pipeline.", e);
        }

        //Set the options.
        //ChannelFactory����ָ����pipeline����һ��ͨ����������ʱ��ͻ�򿪣����翴 OioClientSocketChannel
        Channel ch = getFactory().newChannel(pipeline);
        boolean success = false;
        try {
        	// ����ͨ��
            ch.getConfig().setOptions(getOptions());
            success = true;
        } finally {
            if (!success) {
                ch.close();
            }
        }

        //����İ󶨣����Ӷ�����AbstractChannelʵ�ֵ�-ͨ��Channels�еľ�̬������
        // Bind.
        if (localAddress != null) {
            ch.bind(localAddress);
        }

        // Connect.�������ջ����Channels.connect��������
        return ch.connect(remoteAddress);
    }

    /**
     * �󶨲������ڰ󶨺�������Ҫ�ֿ���ʱ���õ���
     * �����ڳ�������֮ǰ������ͨ��Channel.setAttachment(Object)Ϊ���ͨ������һ��attachment
     * ����һ�����ӽ��飬�Ϳ��Է������attachment
     */
    public ChannelFuture bind(final SocketAddress localAddress) {

        if (localAddress == null) {
            throw new NullPointerException("localAddress");
        }

        ChannelPipeline pipeline;
        try {
            pipeline = getPipelineFactory().getPipeline();
        } catch (Exception e) {
            throw new ChannelPipelineException("Failed to initialize a pipeline.", e);
        }

        // Set the options.
        Channel ch = getFactory().newChannel(pipeline);
        boolean success = false;
        try {
            ch.getConfig().setOptions(getOptions());
            success = true;
        } finally {
            if (!success) {
                ch.close();
            }
        }
        // Bind.
        return ch.bind(localAddress);
    }
}
