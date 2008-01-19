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
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.responses.BadServerSideEvent;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.server.Publication;
import org.codehaus.jremoting.server.PublicationException;
import org.codehaus.jremoting.server.Server;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.adapters.DefaultInvocationHandler;
import org.codehaus.jremoting.JRemotingException;

import java.util.concurrent.ScheduledExecutorService;

public abstract class StatefulServer implements Server {

    protected DefaultInvocationHandler invocationHandler;
    private String state = UNSTARTED;
    protected final ServerMonitor serverMonitor;
    protected final ScheduledExecutorService executorService;

    public StatefulServer(ServerMonitor serverMonitor, DefaultInvocationHandler invocationHandler,
                          ScheduledExecutorService executorService) {
        this.invocationHandler = invocationHandler;
        this.executorService = executorService;
        this.serverMonitor = serverMonitor;
    }

    public Response invoke(Request request, Object connectionDetails) {
        if (getState().equals(STARTED) || request instanceof CollectGarbage) {
            return invocationHandler.invoke(request, connectionDetails);
        } else {
            return new BadServerSideEvent("Service is not started");
        }
    }

    public void suspend() {
        invocationHandler.suspend();
    }

    public void resume() {
        invocationHandler.resume();
    }

    public final void start() {
        this.state = STARTING;
        starting();
        this.state = STARTED;
        started();
    }

    public void starting() {}
    public void started() {}

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


    public void publish(Object impl, String service, Class primaryFacade) throws PublicationException {
        invocationHandler.publish(impl, service, primaryFacade);
    }

    public void publish(Object impl, String service, Publication publicationDescription) throws PublicationException {
        invocationHandler.publish(impl, service, publicationDescription);
    }

    public void unPublish(Object impl, String service) throws PublicationException {
        invocationHandler.unPublish(impl, service);
    }

    public void replacePublished(Object oldImpl, String service, Object withImpl) throws PublicationException {
        invocationHandler.replacePublished(oldImpl, service, withImpl);
    }

    public boolean isPublished(String service) {
        return invocationHandler.isPublished(service);
    }

    public String getState() {
        return state;
    }
}
