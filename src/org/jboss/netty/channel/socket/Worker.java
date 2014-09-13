package org.jboss.netty.channel.socket;

/**
 * ��Ȼ�̳�RunnableΪ�β���Task��
 * ˵������߳�����ר�ŷַ�IO�������������Լ�ִ�С�
 * A Worker is responsible to dispatch IO operations
 */
public interface Worker extends Runnable {

    /**
     * �ڣɣ��߳���ִ�и�����Runnable;
     * 
     * Execute the given Runnable in the IO-Thread. This may be now or
     * later once the IO-Thread do some other work.
     */
    void executeInIoThread(Runnable task);

}
