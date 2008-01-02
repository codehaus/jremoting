/* ====================================================================
 * Copyright 2005-2006 JRemoting Committers
 * Portions copyright 2001 - 2004 Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.codehaus.jremoting.server.transports;

import org.codehaus.jremoting.requests.CollectGarbage;
import org.codehaus.jremoting.requests.InvokeMethod;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.responses.BadServerSideEvent;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.server.Publication;
import org.codehaus.jremoting.server.PublicationException;
import org.codehaus.jremoting.server.Server;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.ServiceHandler;
import org.codehaus.jremoting.server.adapters.InvokerDelegate;
import org.codehaus.jremoting.JRemotingException;

import java.util.concurrent.ScheduledExecutorService;

public abstract class StatefulServer implements Server {
    /**
     * The invocation handler
     */
    protected InvokerDelegate invokerDelegate;
    /**
     * The state of the system.
     */
    private String state = UNSTARTED;
    protected final ServerMonitor serverMonitor;
    protected final ScheduledExecutorService executorService;


    public StatefulServer(ServerMonitor serverMonitor, InvokerDelegate invokerDelegate,
                          ScheduledExecutorService executorService) {
        this.invokerDelegate = invokerDelegate;
        this.executorService = executorService;
        this.serverMonitor = serverMonitor;
    }

    /**
     * Handle an Invocation
     *
     * @param request The request of the invocation.
     * @return An suitable reply.
     */
    public Response invoke(Request request, Object connectionDetails) {
        if (getState().equals(STARTED) || request instanceof CollectGarbage) {
            return invokerDelegate.invoke(request, connectionDetails);
        } else {
            return new BadServerSideEvent("Service is not started");
        }
    }

    /**
     * Suspend the server with open connections.
     */
    public void suspend() {
        invokerDelegate.suspend();
    }

    /**
     * Resume a server with open connections.
     */
    public void resume() {
        invokerDelegate.resume();
    }

    /**
     * Method openConnection
     */
    public final void start() {
        this.state = STARTING;
        starting();
        this.state = STARTED;
        started();
    }

    public void starting() {}
    public void started() {}

    /**
     * Method stop
     */
    public final void stop() {
        if (!getState().equals(STARTED)) {
            throw new JRemotingException("Server Not Started at time of stop");
        }

        this.state = STOPPING;
        stopping();
        this.state = STOPPED;
        stopped();
    }

    public void stopping() {}
    public void stopped() {}


    /**
     * Publish an object via its interface
     *
     * @param impl              The implementation
     * @param service            as this name.
     * @param primaryFacade The interface to expose.
     * @throws org.codehaus.jremoting.server.PublicationException
     *          if an error during publication.
     */
    public void publish(Object impl, String service, Class primaryFacade) throws PublicationException {
        invokerDelegate.publish(impl, service, primaryFacade);
    }

    /**
     * Publish an object via its publication description
     *
     * @param impl                   The implementation
     * @param service                 as this name.
     * @param publicationDescription The publication description.
     * @throws org.codehaus.jremoting.server.PublicationException if an error during publication.
     */
    public void publish(Object impl, String service, Publication publicationDescription) throws PublicationException {
        invokerDelegate.publish(impl, service, publicationDescription);
    }

    /**
     * UnPublish an object.
     *
     * @param impl   The implementation
     * @param service as this name.
     * @throws org.codehaus.jremoting.server.PublicationException if an error during publication.
     */
    public void unPublish(Object impl, String service) throws PublicationException {
        invokerDelegate.unPublish(impl, service);
    }

    /**
     * Replace the server side instance of a service
     *
     * @param oldImpl       The previous implementation.
     * @param service The name it is published as.
     * @param withImpl      The impl to superceed.
     * @throws org.codehaus.jremoting.server.PublicationException if an error during publication.
     */
    public void replacePublished(Object oldImpl, String service, Object withImpl) throws PublicationException {
        invokerDelegate.replacePublished(oldImpl, service, withImpl);
    }

    public boolean isPublished(String service) {
        return invokerDelegate.isPublished(service);
    }

    /**
     * Get the Method Invocation Handler for a particular request.
     *
     * @param invokeMethod The method request
     * @param objectName   The object Name.
     * @return The Method invocation handler
     */
    public ServiceHandler getServiceHandler(InvokeMethod invokeMethod, String objectName) {
        return invokerDelegate.getServiceHandler(invokeMethod, objectName);
    }

    /**
     * Get the ServiceHandler for a particular published name.
     *
     * @param service The published name.
     * @return The Method invocation handler
     */
    public ServiceHandler getServiceHandler(String service) {
        return invokerDelegate.getServiceHandler(service);
    }

    /**
     * Get the InvokerDelegate.
     *
     * @return the invocation handler adapter.
     */
    public InvokerDelegate getInvokerDelegate() {
        return invokerDelegate;
    }

    /**
     * Get the state for teh server.
     *
     * @return the state.
     */
    public String getState() {
        return state;
    }
}
