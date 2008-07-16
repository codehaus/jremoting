package org.codehaus.jremoting.client.context;

import org.codehaus.jremoting.client.ContextFactory;
import org.codehaus.jremoting.client.Context;

/**
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */

public class NullContextFactory implements ContextFactory {

    public Context getClientContext() {
        return null;
    }

}
