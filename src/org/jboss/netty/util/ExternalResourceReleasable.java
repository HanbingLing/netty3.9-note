package org.jboss.netty.util;

//��������ӿڱ����������������ⲿ����Դ����Ҫ��ʾ���ͷŻ�ر�
public interface ExternalResourceReleasable {

    /**
     * Releases the external resources that this object depends on.  You should
     * not call this method if the external resources (e.g. thread pool) are
     * in use by other objects.
     */
    void releaseExternalResources();
}
