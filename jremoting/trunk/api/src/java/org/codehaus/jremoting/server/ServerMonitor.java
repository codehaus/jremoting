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
package org.codehaus.jremoting.server;

import java.io.IOException;

/**
 * Class ServerMonitor is used in many server pieces to indicate select events.
 * Some indicate trouble, some are informational.  
 *
 * @author Paul Hammant
 */
public interface ServerMonitor {

    /**
     * Unexpected error (IOException) during close of connection
     *
     * @param clazz the class where the exception occurred
     * @param desc some accompanying text
     * @param e the exception in question.
     */
    void closeError(Class clazz, String desc, IOException e);

    /**
     * A class was needed that is available in the classpath. Most likely, during
     * deserialization of the request that arrived over a connection to a client.
     *
     * @param clazz the class where the exception occurred
     * @param e the exception in question.
     */
    void classNotFound(Class clazz, ClassNotFoundException e);

    /**
     * An unexpected exception occurred.
     * @param clazz the class where the exception occurred
     * @param desc some accompanying text
     * @param e the exception in question.
     */
    void unexpectedException(Class clazz, String desc, Throwable e);

    /**
     * An unexpected exception occurred during the stopping of a server.
     * @param clazz the class where the exception occurred
     * @param desc some accompanying text
     * @param e the exception in question.
     */
    void stopServerError(Class clazz, String desc, Exception e);

    /**
     * New Session created
     * @param session the session being created
     * @param numberOfSessions the number of sessions over all
     * @param connectionDetails
     */
    void newSession(Session session, int numberOfSessions, String connectionDetails);

    /**
     * Existing Session being removed
     * @param session the session to be removed
     * @param numberOfSessions the number of sessions over all
     */
    void removeSession(Session session, int numberOfSessions);

    /**
     * A session has gone stale through non-use
     * @param session the session that has gone stale
     * @param numberOfSessions the number of sessions over all
     */
    void staleSession(Session session, int numberOfSessions);
}
