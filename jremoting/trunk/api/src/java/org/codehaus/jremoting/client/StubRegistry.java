package org.codehaus.jremoting.client;

import org.codehaus.jremoting.ConnectionException;

public interface StubRegistry {
    void registerReferenceObject(Object implBean, Long referenceID);

    Object getImplObj(Long referenceID);

    Object getInstance(String publishedServiceName, String objectName, StubHelper stubHelper) throws ConnectionException;

    void marshallCorrection(String remoteObjectName, String methodSignature, Object[] args, Class[] argClasses);        
}
