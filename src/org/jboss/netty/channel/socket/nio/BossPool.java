package org.jboss.netty.channel.socket.nio;

/**
 * ����ء�
 * A Pool that holds {@link Boss} instances
 */
public interface BossPool<E extends Boss> extends NioSelectorPool {
    /**
     * Return the next {@link Boss} to use
     *
     */
    E nextBoss();
}
