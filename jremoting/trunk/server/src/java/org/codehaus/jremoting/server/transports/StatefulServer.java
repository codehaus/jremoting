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

import org.codehaus.jremoting.JRemotingException;
import org.codehaus.jremoting.requests.CollectGarbage;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.responses.BadServerSideEvent;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.server.ServerDelegate;
import org.codehaus.jremoting.server.Publication;
import org.codehaus.jremoting.server.PublicationException;
import org.codehaus.jremoting.server.Server;
import org.codehaus.jremoting.server.ServerMonitor;

import java.util.concurrent.ScheduledExecutorService;

public abstract class StatefulServer implements Server {

    protected ServerDelegate serverDelegate;
    private String state = UNSTARTED;
    protected final ServerMonitor serverMonitor;

    public StatefulServer(ServerMonitor serverMonitor, ServerDelegate serverDelegate) {
        this.serverDelegate = serverDelegate;
        this.serverMonitor = serverMonitor;
    }

    /**
     * {@inheritDoc}
     */
    public Response invoke(Request request, String connectionDetails) {
        if (getState().equals(STARTED) || request instanceof CollectGarbage) {
            return serverDelegate.invoke(request, connectionDetails);
        } else {
            return new BadServerSideEvent("Service is not started");
        }
    }

    public void suspend() {
        serverDelegate.suspend();
    }

    public void resume() {
        serverDelegate.resume();
    }

    public boolean isSuspended() {
        return serverDelegate.isSuspended();
    }

    /**
     * {@inheritDoc}
     */
    public final void start() {
        this.state = STARTING;
        starting();
        this.state = STARTED;
        started();
    }

    /**
     * {@inheritDoc}
     */
    public void starting() {}

    /**
     * {@inheritDoc}
     */
    public void started() {}

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
     */
    public void stopping() {}

    /**
     * {@inheritDoc}
     */
    public void stopped() {}

    /**
     * {@inheritDoc}
     */
    public void publish(Object impl, String service, Class primaryFacade) throws PublicationException {
        serverDelegate.publish(impl, service, primaryFacade);
    }

    /**
     * {@inheritDoc}
     */
    public void publish(Object impl, String service, Publication publicationDescription) throws PublicationException {
        serverDelegate.publish(impl, service, publicationDescription);
    }

    /**
     * {@inheritDoc}
     */
    public void publish(Object impl, String service, Class facadeClass, Class... additionalFacades) {
        serverDelegate.publish(impl, service, new Publication(facadeClass).addAdditionalFacades(additionalFacades));
    }

    /**
     * {@inheritDoc}
     */
    public void redirect(String serviceName, String to) {
        serverDelegate.redirect(serviceName, to);
    }

    /**
     * {@inheritDoc}
     */
    public Object getInstanceForReference(String objectName, Long reference) {
        return serverDelegate.getInstanceForReference(objectName, reference);
    }

    /**
     * {@inheritDoc}
     */
    public void unPublish(Object impl, String service) throws PublicationException {
        serverDelegate.unPublish(impl, service);
    }

    /**
     * {@inheritDoc}
     */
    public void replacePublished(Object oldImpl, String service, Object withImpl) throws PublicationException {
        serverDelegate.replacePublished(oldImpl, service, withImpl);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isPublished(String service) {
        return serverDelegate.isPublished(service);
    }

    /**
     * {@inheritDoc}
     */
    public String getState() {
        return state;
    }

}
