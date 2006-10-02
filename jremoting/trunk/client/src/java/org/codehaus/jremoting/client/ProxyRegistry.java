package org.codehaus.jremoting.client;

import org.codehaus.jremoting.client.factories.DefaultProxyHelper;
import org.codehaus.jremoting.ConnectionException;

public interface ProxyRegistry {
    void registerReferenceObject(Object implBean, Long referenceID);

    Object getImplObj(Long referenceID);

    Object getInstance(String publishedServiceName, String objectName, ProxyHelper proxyHelper) throws ConnectionException;

    void marshallCorrection(String remoteObjectName, String methodSignature, Object[] args, Class[] argClasses);        
}
