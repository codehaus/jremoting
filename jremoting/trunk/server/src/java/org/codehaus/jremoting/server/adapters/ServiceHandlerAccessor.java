package org.codehaus.jremoting.server.adapters;

import org.codehaus.jremoting.server.ServiceHandler;
import org.codehaus.jremoting.server.Publisher;
import org.codehaus.jremoting.requests.InvokeMethod;

public interface ServiceHandlerAccessor extends Publisher {

    /**
     * Get the ServiceHandler for this transport.  Used in special adapters.
     *
     * @param invokeMethod used as a hint for getting the right handler.
     * @param objectName    the object name relating to the method request.
     * @return a suitable ServiceHandler
     */
    ServiceHandler getServiceHandler(InvokeMethod invokeMethod, String objectName);


    /**
     * Get the ServiceHandler for a published lookup name. Used in special adapters.
     *
     * @param service the published lookup name.
     * @return a suitable ServiceHandler
     */
    ServiceHandler getServiceHandler(String service);
      

}
