package org.jboss.netty.util.internal;

import java.util.concurrent.Executor;

/**
 * һ���������ڲ��࣬���Է�������ֻ��һ����̬��start������
 * ������ִ����ȥִ����Ӧ������ע���Ǹ�ThreadLocal�����ʹ�á�
 */
public final class DeadLockProofWorker {

    /**
     * An <em>internal use only</em> thread-local variable that tells the
     * {@link Executor} that this worker acquired a worker thread from.
     */
    public static final ThreadLocal<Executor> PARENT = new ThreadLocal<Executor>();

    public static void start(final Executor parent, final Runnable runnable) {
        if (parent == null) {
            throw new NullPointerException("parent");
        }
        if (runnable == null) {
            throw new NullPointerException("runnable");
        }

        parent.execute(new Runnable() {
            public void run() {
            	//ÿ���߳���ִ�е�ʱ�������Լ���ThreadLocal����Ϊ��������ִ������
            	// ִ����ɺ��Ƴ��������ھ����Runnable�оͿ��Ի�ö�Ӧ��Excutor��
                PARENT.set(parent);
                try {
                    runnable.run();
                } finally {
                    PARENT.remove();
                }
            }
        });
    }

    /**
     * ˽�еĹ�������
     */
    private DeadLockProofWorker() {
    }
}
