package org.jboss.netty.channel;

import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.handler.execution.ExecutionHandler;

public interface ChannelFuture {

    /**
     * �����future��Ӧ�����ĸ�Channel��IO�¼���
     * Returns a channel where the I/O operation associated with this
     * future takes place.
     */
    Channel getChannel();

    /**
     * future�Ƿ���complete״̬�����۳ɹ���ʧ�ܣ�ȡ����
     */
    boolean isDone();

    /**
     * �Ƿ�ȡ����
     * ���future��������cancel()������
     */
    boolean isCancelled();

    /**
     * IO�����ɹ���ɣ�
     */
    boolean isSuccess();

    /**
     * IO����ʧ�ܵ�ԭ��
     * ����ɹ�����future����ɣ��򷵻�null��
     */
    Throwable getCause();

    /**
     * ȡ�����future������IO���������ȡ���ɹ���֪ͨ���еĹ۲��ߡ�
     */
    boolean cancel();

    /**
     * ��־�ɹ�����֪ͨ���еĹ۲��ߡ�
     * Marks this future as a success and notifies all listeners.
     */
    boolean setSuccess();

    /**
     * ��־ʧ��ԭ�򣬲�֪ͨ�۲��ߡ�
     */
    boolean setFailure(Throwable cause);

    /**
     * Notifies the progress of the operation to the listeners that implements
     * {@link ChannelFutureProgressListener}. Please note that this method will
     * not do anything and return {@code false} if this future is complete
     * already.
     *
     * @return {@code true} if and only if notification was made.
     */
    boolean setProgress(long amount, long current, long total);

    /**
     * Ϊ���future���ӹ۲��ߣ���������ɺ�֪ͨ���ǡ�
     */
    void addListener(ChannelFutureListener listener);

    /**
     * Removes the specified listener from this future.
     * The specified listener is no longer notified when this
     * future is {@linkplain #isDone() done}.  If the specified
     * listener is not associated with this future, this method
     * does nothing and returns silently.
     */
    void removeListener(ChannelFutureListener listener);

    /**
     * @deprecated Use {@link #sync()} or {@link #syncUninterruptibly()} instead.
     */
    @Deprecated
    ChannelFuture rethrowIfFailed() throws Exception;

    /**
     * Waits for this future until it is done, and rethrows the cause of the failure if this future
     * failed.  If the cause of the failure is a checked exception, it is wrapped with a new
     * {@link ChannelException} before being thrown.
     */
    ChannelFuture sync() throws InterruptedException;

    /**
     * Waits for this future until it is done, and rethrows the cause of the failure if this future
     * failed.  If the cause of the failure is a checked exception, it is wrapped with a new
     * {@link ChannelException} before being thrown.
     */
    ChannelFuture syncUninterruptibly();

    /**
     * �����ȴ����future��ɡ�
     * �׳��쳣ʱ��Ϊ��ǰ�߳̿��ܻᱻ��ϡ�
     * Waits for this future to be completed.
     *
     * @throws InterruptedException
     *         if the current thread was interrupted
     */
    ChannelFuture await() throws InterruptedException;

    /**
     * ���жϵĵȴ�������ɣ���ĬĬ�Ķ����쳣InterruptedException��
     */
    ChannelFuture awaitUninterruptibly();

    /**
     * �����⼸����������ָ����ʱʱ��Ĺ��ܡ�
     */
    boolean await(long timeout, TimeUnit unit) throws InterruptedException;
    boolean await(long timeoutMillis) throws InterruptedException;
    boolean awaitUninterruptibly(long timeout, TimeUnit unit);
    boolean awaitUninterruptibly(long timeoutMillis);
}
