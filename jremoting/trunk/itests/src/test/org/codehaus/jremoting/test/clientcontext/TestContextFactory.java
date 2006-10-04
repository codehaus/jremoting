package org.codehaus.jremoting.test.clientcontext;

import org.codehaus.jremoting.client.Context;
import org.codehaus.jremoting.server.ServerSideContextFactory;

/**
 * @author Paul Hammant and Rune Johanessen (pairing for part)
 * @version $Revision: 1.1 $
 */

public class TestContextFactory implements ServerSideContextFactory {
    public TestContextFactory() {
    }

    public Context get() {
        return new TestContext();
    }

    //return "TestCCF:" + System.identityHashCode(Thread.currentThread());

    public void set(Long session, Context context) {
    }

    public boolean isSet() {
        return false;
    }
}
