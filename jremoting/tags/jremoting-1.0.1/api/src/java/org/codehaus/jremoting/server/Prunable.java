package org.codehaus.jremoting.server;

public interface Prunable {

    void pruneSessionsStaleForLongerThan(long millis);
}
