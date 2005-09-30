package org.codehaus.jremoting.test.clientcontext;

import org.codehaus.jremoting.ClientContext;
import org.codehaus.jremoting.server.ServerSideClientContextFactory;

/**
 * @author Paul Hammant and Rune Johanessen (pairing for part)
 * @version $Revision: 1.1 $
 */

public class TestClientContextFactory implements ServerSideClientContextFactory {
    public TestClientContextFactory() {
    }

    public ClientContext get() {
        return new TestClientContext();
    }

    //return "TestCCF:" + System.identityHashCode(Thread.currentThread());

    public void set(Long session, ClientContext clientContext) {
    }

    public boolean isSet() {
        return false;
    }
}
