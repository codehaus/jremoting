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

import java.util.concurrent.ExecutorService;

import org.codehaus.jremoting.requests.AbstractRequest;
import org.codehaus.jremoting.requests.InvokeMethod;
import org.codehaus.jremoting.responses.AbstractResponse;
import org.codehaus.jremoting.responses.InvocationExceptionThrown;
import org.codehaus.jremoting.server.MethodInvocationHandler;
import org.codehaus.jremoting.server.PublicationDescription;
import org.codehaus.jremoting.server.PublicationException;
import org.codehaus.jremoting.server.Server;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.adapters.InvocationHandlerAdapter;

public class StatefulServer implements Server {
    /**
     * The invocation handler
     */
    protected InvocationHandlerAdapter invocationHandlerAdapter;
    /**
     * The state of the system.
     */
    private String state = UNSTARTED;
    protected final ServerMonitor serverMonitor;
    protected final ExecutorService executorService;


    public StatefulServer(ServerMonitor serverMonitor, InvocationHandlerAdapter invocationHandlerAdapter,
                          ExecutorService executor) {
        this.invocationHandlerAdapter = invocationHandlerAdapter;
        this.executorService = executor;
        this.serverMonitor = serverMonitor;
    }


    public synchronized ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Handle an Invocation
     *
     * @param request The request of the invocation.
     * @return An suitable reply.
     */
    public AbstractResponse handleInvocation(AbstractRequest request, Object connectionDetails) {
        if (getState().equals(STARTED)) {
            return invocationHandlerAdapter.handleInvocation(request, connectionDetails);
        } else {
            return new InvocationExceptionThrown("Service is not started");
        }
    }

    /**
     * Suspend the server with open connections.
     */
    public void suspend() {
        invocationHandlerAdapter.suspend();
    }

    /**
     * Resume a server with open connections.
     */
    public void resume() {
        invocationHandlerAdapter.resume();
    }

    /**
     * Method start
     */
    public void start() {
        setState(STARTED);
    }

    /**
     * Method stop
     */
    public void stop() {
        setState(STOPPED);
    }

    /**
     * Publish an object via its interface
     *
     * @param impl              The implementation
     * @param service            as this name.
     * @param interfaceToExpose The interface to expose.
     * @throws org.codehaus.jremoting.server.PublicationException
     *          if an error during publication.
     */
    public void publish(Object impl, String service, Class interfaceToExpose) throws PublicationException {
        invocationHandlerAdapter.publish(impl, service, interfaceToExpose);
    }

    /**
     * Publish an object via its publication description
     *
     * @param impl                   The implementation
     * @param service                 as this name.
     * @param publicationDescription The publication description.
     * @throws org.codehaus.jremoting.server.PublicationException if an error during publication.
     */
    public void publish(Object impl, String service, PublicationDescription publicationDescription) throws PublicationException {
        invocationHandlerAdapter.publish(impl, service, publicationDescription);
    }

    /**
     * UnPublish an object.
     *
     * @param impl   The implementation
     * @param service as this name.
     * @throws org.codehaus.jremoting.server.PublicationException if an error during publication.
     */
    public void unPublish(Object impl, String service) throws PublicationException {
        invocationHandlerAdapter.unPublish(impl, service);
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
        invocationHandlerAdapter.replacePublished(oldImpl, service, withImpl);
    }

    /**
     * Get the Method Invocation Handler for a particular request.
     *
     * @param invokeMethod The method request
     * @param objectName   The object Name.
     * @return The Method invocation handler
     */
    public MethodInvocationHandler getMethodInvocationHandler(InvokeMethod invokeMethod, String objectName) {
        return invocationHandlerAdapter.getMethodInvocationHandler(invokeMethod, objectName);
    }

    /**
     * Get the MethodInvocationHandler for a particular published name.
     *
     * @param service The published name.
     * @return The Method invocation handler
     */
    public MethodInvocationHandler getMethodInvocationHandler(String service) {
        return invocationHandlerAdapter.getMethodInvocationHandler(service);
    }

    /**
     * Get the Invocation Handler Adapter.
     *
     * @return the invocation handler adapter.
     */
    public InvocationHandlerAdapter getInovcationHandlerAdapter() {
        return invocationHandlerAdapter;
    }

    /**
     * Set the state for the server
     *
     * @param state The state
     */
    protected void setState(String state) {
        this.state = state;
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
