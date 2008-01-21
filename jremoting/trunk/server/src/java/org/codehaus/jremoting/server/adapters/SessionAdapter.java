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
package org.codehaus.jremoting.server.adapters;

import org.codehaus.jremoting.server.Publisher;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.Session;

import java.util.HashMap;

public class SessionAdapter extends PublicationAdapter {

    private Long lastSessionID = (long) 0;
    private Session lastSession;
    private static long sessionId = 0;
    private final HashMap<Long, Session> sessions = new HashMap<Long, Session>();
    private final ServerMonitor serverMonitor;

    public SessionAdapter(Publisher delegate, ServerMonitor serverMonitor) {
        super(delegate);
        this.serverMonitor = serverMonitor;
    }

    protected Session getSession(long session) {
        return sessions.get(session);
    }

    private Long getNewSessionNum() {
        // approve everything and setContext session identifier.
        return new Long((++sessionId << 16) + ((long) (Math.random() * 65536)));
    }

    protected Long newSession(Object connectionDetails) {
        long sessionNum = getNewSessionNum();
        Session session = new Session(sessionNum);
        session.setConnectionDetails(connectionDetails);
        sessions.put(sessionNum, session);
        serverMonitor.newSession(session, sessions.size(), connectionDetails);
        return sessionNum;
    }

    protected boolean sessionExists(long session) {
        return sessions.containsKey(session);
    }

    protected void removeSession(long session) {
        Session sess = sessions.remove(session);
        serverMonitor.removeSession(sess, sessions.size());
    }

    public void pruneSessionsStaleForLongerThan(long millis) {
        Object[] sessionArray = sessions.values().toArray();
        for (Object sessionObj : sessionArray) {
            Session s = (Session) sessionObj;
            long now = System.currentTimeMillis();
            if (s.getLastTouched() + millis < now) {
                Session sess = sessions.remove(s.getSession());
                serverMonitor.staleSession(sess, sessions.size());
            }
        }
    }

    /**
     * Does a session exist
     *
     * @param session The session
     * @return true if it exists
     */
    protected boolean doesSessionExistAndRefreshItIfItDoes(long session) {

        if (lastSessionID.equals(session)) {

            lastSession.refresh();
            // buffer last session for performance.
            return true;
        } else {
            if (sessions.containsKey(session)) {
                lastSessionID = session;
                lastSession = sessions.get(session);
                lastSession.refresh();
                return true;
            }
        }

        return false;
    }



}
