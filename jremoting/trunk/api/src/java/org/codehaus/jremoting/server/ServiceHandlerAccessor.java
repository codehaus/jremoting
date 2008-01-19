package org.codehaus.jremoting.server;

import org.codehaus.jremoting.server.ServiceHandler;
import org.codehaus.jremoting.server.Publisher;
import org.codehaus.jremoting.requests.InvokeMethod;

public interface ServiceHandlerAccessor extends Publisher {

    /**
     * Get the ServiceHandler for a published lookup name. Used in special adapters.
     *
     * @param service the published lookup name.
     * @return a suitable ServiceHandler
     */
    ServiceHandler getServiceHandler(String service);
      

}
