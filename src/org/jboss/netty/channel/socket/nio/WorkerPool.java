package org.jboss.netty.channel.socket.nio;

import org.jboss.netty.channel.socket.Worker;

/**
 * Ϊ���ܵ��׽���ָ��worker���з���
 * The {@link WorkerPool} is responsible to hand of {@link Worker}'s on demand
 *
 */
public interface WorkerPool<E extends Worker> extends NioSelectorPool {

    /**
     * Return the next {@link Worker} to use
     *
     * @return worker
     */
    E nextWorker();
}
