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

import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.Session;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class JavaLoggingServerMonitor
 *
 * @author Paul Hammant
 *
 */
public class JavaLoggingServerMonitor implements ServerMonitor {

    private ServerMonitor delegate;

    public JavaLoggingServerMonitor(ServerMonitor delegate) {
        this.delegate = delegate;
    }

    public JavaLoggingServerMonitor() {
        delegate = new NullServerMonitor();
    }

    public void closeError(Class clazz, String s, IOException e) {
        Logger logger = Logger.getLogger(clazz.getName());
        logger.log(Level.INFO, "<closeError>" + s, e);
        delegate.closeError(clazz, s, e);
    }

    public void classNotFound(Class clazz, ClassNotFoundException e) {
        Logger logger = Logger.getLogger(clazz.getName());
        logger.log(Level.INFO, "<classNotFound>", e);
        delegate.classNotFound(clazz, e);
    }

    public void unexpectedException(Class clazz, String s, Throwable e) {
        Logger logger = Logger.getLogger(clazz.getName());
        logger.log(Level.INFO, "<unexpectedException>" + s, e);
        delegate.unexpectedException(clazz, s, e);
    }

    public void stopServerError(Class clazz, String s, Exception e) {
        Logger logger = Logger.getLogger(clazz.getName());
        logger.log(Level.INFO, "<stopServerError>" + s, e);
        delegate.stopServerError(clazz, s, e);
    }

    public void newSession(Session session, int numberOfSessions, String connectionDetails) {
        Logger logger = Logger.getLogger(this.getClass().getName());
        logger.log(Level.INFO, "<newSession>" + session.getSession());
        delegate.newSession(session, numberOfSessions, connectionDetails);
    }

    public void removeSession(Session session, int newSize) {
        Logger logger = Logger.getLogger(this.getClass().getName());
        logger.log(Level.INFO, "<removeSession>" + session.getSession());
        delegate.removeSession(session, newSize);
    }

    public void staleSession(Session session, int numberOfSessions) {
        Logger logger = Logger.getLogger(this.getClass().getName());
        logger.log(Level.INFO, "<staleSession>" + session.getSession());
        delegate.staleSession(session, numberOfSessions);
    }
}
