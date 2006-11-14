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

import org.codehaus.jremoting.server.Session;
import org.codehaus.jremoting.server.Publisher;

import java.util.HashMap;

public class SessionAdapter extends PublicationAdapter {

    private Long lastSessionID = (long) 0;
    private Session lastSession;
    private static long sessionId = 0;
    private final HashMap<Long, Session> sessions = new HashMap<Long, Session>();

    public SessionAdapter(Publisher delegate) {
        super(delegate);
    }

    protected Session getSession(Long sessionID) {
        return sessions.get(sessionID);
    }

    private Long getNewSession() {
        // approve everything and setContext session identifier.
        return new Long((++sessionId << 16) + ((long) (Math.random() * 65536)));
    }


    protected Long newSession() {
        Long session = getNewSession();
        sessions.put(session, new Session(session));
        return session;
    }

    protected boolean sessionExists(long session) {
        return sessions.containsKey(session);
    }

    protected void removeSession(Long sessionID) {
        sessions.remove(sessionID);
    }

    public void pruneSessionsStaleForLongerThan(long millis) {
        Object[] sessionArray = sessions.values().toArray();
        for (Object sessionObj : sessionArray) {
            Session s = (Session) sessionObj;
            long now = System.currentTimeMillis();
            if (s.getLastTouched() + millis < now) {
                sessions.remove(s.getSession());
            }
        }
    }

    /**
     * Does a session exist
     *
     * @param session The session
     * @return true if it exists
     */
    protected boolean doesSessionExistAndRefreshItIfItDoes(Long session) {

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
