package org.codehaus.jremoting.server;

import org.codehaus.jremoting.api.ThreadPool;

public interface CallbackHelper {

    ServerMonitor createServerMonitor();

    ServerSideClientContextFactory createServerSideClientContextFactory();

    Authenticator createAuthenticator();

    ClassRetriever createClassRetriever(ClassLoader classLoader);

    Server createServer(ClassRetriever classRetriever, Authenticator authenticator, ServerMonitor serverMonitor, ThreadPool threadPool, ServerSideClientContextFactory serverSideClientContextFactory);
}
