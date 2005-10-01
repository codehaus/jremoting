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
package org.codehaus.jremoting.client.transports;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;

import org.codehaus.jremoting.client.ClientStreamReadWriter;
import org.codehaus.jremoting.client.InvocationException;
import org.codehaus.jremoting.commands.Response;
import org.codehaus.jremoting.commands.Request;
import org.codehaus.jremoting.client.NoSuchReferenceException;
import org.codehaus.jremoting.client.NotPublishedException;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.commands.NotPublishedResponse;
import org.codehaus.jremoting.commands.PublishedNameRequest;
import org.codehaus.jremoting.commands.TryLaterResponse;
import org.codehaus.jremoting.commands.RequestConstants;
import org.codehaus.jremoting.commands.ReplyConstants;
import org.codehaus.jremoting.commands.NoSuchSessionResponse;
import org.codehaus.jremoting.api.ThreadPool;
import org.codehaus.jremoting.client.NoSuchSessionException;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.*;
import org.codehaus.jremoting.commands.ClientInvocationAbendResponse;
import org.codehaus.jremoting.commands.NoSuchReferenceResponse;
import org.codehaus.jremoting.commands.*;

import javax.swing.*;

/**
 * Class AbstractStreamClientInvocationHandler
 *
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public abstract class AbstractStreamClientInvocationHandler extends AbstractClientInvocationHandler
{

    private ClientStreamReadWriter objectReadWriter;
    private boolean methodLogging = false;
    private long lastRealRequest = System.currentTimeMillis();
    protected final ClassLoader interfacesClassLoader;

    /**
     * Constructor AbstractStreamClientInvocationHandler
     *
     * @param threadPool
     * @param clientMonitor
     * @param connectionPinger
     * @param interfacesClassLoader
     */
    public AbstractStreamClientInvocationHandler(ThreadPool threadPool, ClientMonitor clientMonitor, ConnectionPinger connectionPinger, ClassLoader interfacesClassLoader)
    {
        super(threadPool, clientMonitor, connectionPinger);
        this.interfacesClassLoader = interfacesClassLoader;
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

    protected void setObjectReadWriter( ClientStreamReadWriter objectReadWriter )
    {
        this.objectReadWriter = objectReadWriter;
    }

    protected void requestWritten()
    {
    }

    /**
     * Method handleInvocation
     *
     *
     * @param request
     *
     * @return
     *
     */
    public synchronized Response handleInvocation( Request request )
    {
        if( request.getRequestCode() != RequestConstants.PINGREQUEST )
        {
            lastRealRequest = System.currentTimeMillis();
        }

        try
        {
            while( true )
            {
                boolean again = true;
                Response response = null;
                int tries = 0;
                long start = 0;

                if( methodLogging )
                {
                    start = System.currentTimeMillis();
                }

                while( again )
                {
                    tries++;

                    again = false;

                    try
                    {
                        long t1 = System.currentTimeMillis();

                        response = (Response)objectReadWriter.postRequest( request );

                        long t2 = System.currentTimeMillis();

                        if( response.getReplyCode() >= 100 )
                        {
                            // special case for callabcks.
                            if (response.getReplyCode() == ReplyConstants.CLIENTABEND)
                            {
                                ClientInvocationAbendResponse abendReply = (ClientInvocationAbendResponse) response;
                                throw abendReply.getIOException();
                            }

                            if( response instanceof TryLaterResponse )
                            {
                                int millis = ( (TryLaterResponse)response ).getSuggestedDelayMillis();

                                clientMonitor.serviceSuspended(this.getClass(), request, tries,
                                                                            millis );

                                again = true;
                            }
                            else if( response instanceof NoSuchReferenceResponse )
                            {
                                throw new NoSuchReferenceException( ( (NoSuchReferenceResponse)response )
                                                                    .getReferenceID() );
                            }
                            else if( response instanceof NoSuchSessionResponse )
                            {
                                throw new NoSuchSessionException( ( (NoSuchSessionResponse)response )
                                                                    .getSessionID() );
                            }
                            else if( response instanceof NotPublishedResponse )
                            {
                                PublishedNameRequest pnr = (PublishedNameRequest)request;

                                throw new NotPublishedException( pnr.getPublishedServiceName(),
                                                                 pnr.getObjectName() );
                            }
                        }
                    }
                    catch( IOException ioe )
                    {
                        if(isSafeEnd(ioe))
                        {
                            int retryConnectTries = 0;

                            again = true;

                            while( !tryReconnect() )
                            {
                                clientMonitor.serviceAbend(this.getClass(), retryConnectTries, ioe);

                                retryConnectTries++;
                            }
                        }
                        else
                        {
                            ioe.printStackTrace();

                            throw new InvocationException(
                                "IO Exception during invocation to server :" + ioe.getMessage() );
                        }
                    }
                }
                if( methodLogging )
                {
                    if( request instanceof MethodRequest )
                    {
                        clientMonitor.methodCalled(
                                this.getClass(), ( (MethodRequest)request ).getMethodSignature(),
                            System.currentTimeMillis() - start, "" );
                    }
                }

                return response;
            }
        }
        catch( ClassNotFoundException e )
        {
            throw new InvocationException( "Class definition missing on Deserialization: "
                                                 + e.getMessage() );
        }
    }

    private boolean isSafeEnd(IOException ioe) {
        if (ioe instanceof SocketException | ioe instanceof EOFException
                            | ioe instanceof InterruptedIOException) {
                                return true;
                            }
        if (ioe.getMessage() != null) {
            String msg = ioe.getMessage();
            if (msg.equals("Read end dead") | msg.equals("Pipe closed")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method getLastRealRequest
     *
     *
     * @return
     *
     */
    public long getLastRealRequest()
    {
        return lastRealRequest;
    }
}
