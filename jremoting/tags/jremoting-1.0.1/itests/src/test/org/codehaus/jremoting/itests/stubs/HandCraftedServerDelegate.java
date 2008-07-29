package org.codehaus.jremoting.itests.stubs;

import org.codehaus.jremoting.server.adapters.DefaultServerDelegate;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.Publication;
import org.codehaus.jremoting.server.PublicationItem;
import org.codehaus.jremoting.server.servicehandlers.ServiceHandler;
import org.codehaus.jremoting.server.context.ThreadLocalServerContextFactory;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.stubretrievers.RefusingStubRetriever;

public class HandCraftedServerDelegate extends DefaultServerDelegate {
        public HandCraftedServerDelegate(ServerMonitor serverMonitor) {
            super(serverMonitor, new RefusingStubRetriever(), new NullAuthenticator(), new ThreadLocalServerContextFactory());
        }

        protected ServiceHandler makeServiceHandler(String thing, Publication publicationDescription, PublicationItem item) {
            return new HandCraftedServiceHandler(HandCraftedServerDelegate.this, thing, publicationDescription, item.getFacadeClass());
        }
    }
