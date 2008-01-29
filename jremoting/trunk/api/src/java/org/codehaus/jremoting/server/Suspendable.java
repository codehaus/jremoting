package org.codehaus.jremoting.server;

public interface Suspendable {


    /**
     * Suspend publishing
     */
    void suspend();

    /**
     * Resume publishing
     */
    void resume();

    /**
     * Is publishing suspended  ?
     * @return whether is or not
     */
    boolean isSuspended();

}
