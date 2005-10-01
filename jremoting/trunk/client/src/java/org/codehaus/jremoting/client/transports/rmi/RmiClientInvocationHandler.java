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
package org.codehaus.jremoting.client.transports.rmi;

import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.ConnectIOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.codehaus.jremoting.api.ConnectionException;
import org.codehaus.jremoting.commands.Response;
import org.codehaus.jremoting.commands.Request;
import org.codehaus.jremoting.client.NotPublishedException;
import org.codehaus.jremoting.commands.PublishedNameRequest;
import org.codehaus.jremoting.commands.TryLaterResponse;
import org.codehaus.jremoting.commands.RequestConstants;
import org.codehaus.jremoting.api.BadConnectionException;
import org.codehaus.jremoting.api.RmiInvocationHandler;
import org.codehaus.jremoting.api.ThreadPool;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.*;
import org.codehaus.jremoting.client.transports.AbstractClientInvocationHandler;
import org.codehaus.jremoting.client.transports.AbstractClientInvocationHandler;
import org.codehaus.jremoting.commands.MethodRequest;
import org.codehaus.jremoting.commands.NotPublishedResponse;
import org.codehaus.jremoting.commands.*;

/**
 * Class RmiClientInvocationHandler
 *
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public final class RmiClientInvocationHandler extends AbstractClientInvocationHandler
{

    private RmiInvocationHandler m_rmiInvocationHandler;
    private String m_URL;
    private long m_lastRealRequest = System.currentTimeMillis();

    /**
     * Constructor RmiClientInvocationHandler
     *
     *
     * @param host
     * @param port
     *
     * @throws ConnectionException
     *
     */
    public RmiClientInvocationHandler( ThreadPool threadPool, ClientMonitor clientMonitor,
                                       ConnectionPinger connectionPinger,
                                       String host, int port ) throws ConnectionException
    {

        super(threadPool, clientMonitor, connectionPinger);

        m_URL = "rmi://" + host + ":" + port + "/" + RmiInvocationHandler.class.getName();

        try
        {
            m_rmiInvocationHandler = (RmiInvocationHandler)Naming.lookup( m_URL );
        }
        catch( NotBoundException nbe )
        {
            throw new ConnectionException(
                "Cannot bind to the remote RMI service.  Either an IP or RMI issue." );
        }
        catch( MalformedURLException mfue )
        {
            throw new ConnectionException( "Malformed URL, host/port (" + host + "/" + port
                                                 + ") must be wrong: " + mfue.getMessage() );
        }
        catch( ConnectIOException cioe )
        {
            throw new BadConnectionException( "Cannot connect to remote RMI server. "
                    + "It is possible that transport mismatch");
        }
        catch( RemoteException re )
        {
            throw new ConnectionException( "Unknown Remote Exception : " + re.getMessage() );
        }
    }

    /**
     * Method tryReconnect
     *
     * @return
     *
     */
    protected boolean tryReconnect()
    {

        try
        {
            m_rmiInvocationHandler = (RmiInvocationHandler)Naming.lookup( m_URL );

            return true;
        }
        catch( Exception e )
        {
            return false;
        }
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
            m_lastRealRequest = System.currentTimeMillis();
        }

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
                response = m_rmiInvocationHandler.handleInvocation( request );

                if( response.getReplyCode() >= 100 )
                {
                    if( response instanceof TryLaterResponse )
                    {
                        int millis = ( (TryLaterResponse)response ).getSuggestedDelayMillis();

                        clientMonitor.serviceSuspended(this.getClass(), request, tries, millis );

                        again = true;
                    }
                    else if( response instanceof NoSuchReferenceResponse )
                    {
                        throw new NoSuchReferenceException( ( (NoSuchReferenceResponse)response )
                                                            .getReferenceID() );
                    }
                    else if( response instanceof NotPublishedResponse )
                    {
                        PublishedNameRequest pnr = (PublishedNameRequest)request;

                        throw new NotPublishedException( pnr.getPublishedServiceName(),
                                                         pnr.getObjectName() );
                    }
                }
            }
            catch( RemoteException re )
            {
                if( re instanceof ConnectException | re instanceof ConnectIOException )
                {
                    int retryConnectTries = 0;

                    m_rmiInvocationHandler = null;

                    while( !tryReconnect() )
                    {
                        clientMonitor.serviceAbend(this.getClass(), retryConnectTries, re);

                        retryConnectTries++;
                    }
                }
                else
                {
                    throw new InvocationException( "Unknown RMI problem : "
                                                         + re.getMessage() );
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

    /**
     * Method getLastRealRequest
     *
     *
     * @return
     *
     */
    public long getLastRealRequest()
    {
        return m_lastRealRequest;
    }
}
