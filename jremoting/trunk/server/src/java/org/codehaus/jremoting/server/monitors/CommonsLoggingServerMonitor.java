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
package org.codehaus.jremoting.server.monitors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.Session;

import java.io.IOException;

/**
 * Class CommonsLoggingServerMonitor
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class CommonsLoggingServerMonitor implements ServerMonitor {

    private ServerMonitor delegate;

    public CommonsLoggingServerMonitor(ServerMonitor delegate) {
        this.delegate = delegate;
    }

    public CommonsLoggingServerMonitor() {
        delegate = new NullServerMonitor();
    }


    public void closeError(Class clazz, String s, IOException e) {
        Log log = LogFactory.getLog(clazz);
        if (log.isDebugEnabled()) {
            log.debug("<closeError>" + s, e);
        }
        delegate.closeError(clazz, s, e);

    }

    public void classNotFound(Class clazz, ClassNotFoundException e) {
        Log log = LogFactory.getLog(clazz);
        if (log.isDebugEnabled()) {
            log.debug("<classNotFound>", e);
        }
        delegate.classNotFound(clazz, e);

    }

    public void unexpectedException(Class clazz, String s, Exception e) {
        Log log = LogFactory.getLog(clazz);
        if (log.isDebugEnabled()) {
            log.debug("<unexpectedException>" + s, e);
        }
        delegate.unexpectedException(clazz, s, e);
    }

    public void stopServerError(Class clazz, String s, Exception e) {
        Log log = LogFactory.getLog(clazz);
        if (log.isErrorEnabled()) {
            log.error("<stopServerError>" + s, e);
        }
        delegate.stopServerError(clazz, s, e);
    }

    public void newSession(Session session, int newSize, String connectionDetails) {
        Log log = LogFactory.getLog(this.getClass());
        if (log.isErrorEnabled()) {
            log.error("<newSession>" + session.getSession());
        }
        delegate.newSession(session, newSize, connectionDetails);
    }

    public void removeSession(Session session, int newSize) {
        Log log = LogFactory.getLog(this.getClass());
        if (log.isErrorEnabled()) {
            log.error("<removeSession>" + session.getSession());
        }
        delegate.removeSession(session, newSize);
    }

    public void staleSession(Session session, int newSize) {
        Log log = LogFactory.getLog(this.getClass());
        if (log.isErrorEnabled()) {
            log.error("<staleSession>" + session.getSession());
        }
        delegate.staleSession(session, newSize);
    }
}
