/* ====================================================================
 * Copyright 2005 JRemoting Committers
 * Portions copyright 2001 - 2004 Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.codehaus.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.codehaus.jremoting.client.transports.direct;

import java.io.IOException;
import org.codehaus.jremoting.api.ConnectionException;
import org.codehaus.jremoting.commands.Response;
import org.codehaus.jremoting.commands.Request;
import org.codehaus.jremoting.commands.RequestFailedResponse;
import org.codehaus.jremoting.api.SerializationHelper;
import org.codehaus.jremoting.api.ThreadPool;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.server.ServerMarshalledInvocationHandler;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.ConnectionPinger;

/**
 * Class DirectInvocationHandler
 *
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public final class DirectMarshalledInvocationHandler extends AbstractDirectInvocationHandler
{

    private ServerMarshalledInvocationHandler invocationHandler;
    private ClassLoader interfacesClassLoader;


    /**
     * Constructor DirectInvocationHandler
     *
     * @param threadPool
     * @param clientMonitor
     * @param connectionPinger
     * @param invocationHandler
     */
    public DirectMarshalledInvocationHandler(ThreadPool threadPool, ClientMonitor clientMonitor,
                                             ConnectionPinger connectionPinger,
                                             ServerMarshalledInvocationHandler invocationHandler,
                                             ClassLoader classLoader)
    {
        super(threadPool, clientMonitor, connectionPinger);
        this.invocationHandler = invocationHandler;
        interfacesClassLoader = classLoader;
    }

    protected Response performInvocation( Request request ) throws IOException
    {

        try
        {
            byte[] serRequest = SerializationHelper.getBytesFromInstance( request );
            byte[] serReply = invocationHandler.handleInvocation( serRequest, null );

            Object instanceFromBytes = SerializationHelper.getInstanceFromBytes( serReply,
                                                                                      interfacesClassLoader );
            return (Response)instanceFromBytes;
        }
        catch( ClassNotFoundException e )
        {
            e.printStackTrace();

            return new RequestFailedResponse( "Some Class not found Exception on server side : "
                                           + e.getMessage() );
        }
    }

    /**
     * Method getInterfacesClassLoader
     *
     *
     * @return
     *
     */
    public ClassLoader getInterfacesClassLoader()
    {
        return interfacesClassLoader;
    }

}
