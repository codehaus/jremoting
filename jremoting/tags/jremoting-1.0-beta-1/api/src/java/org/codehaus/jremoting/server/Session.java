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

import java.util.HashMap;

/**
 * Class Session
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class Session {

    private long session;

    private HashMap<Long, Object> instancesInUse = new HashMap<Long, Object>();
    private long lastTouched;

    public Session(long session) {
        this.session = session;
    }

    public Long getSession() {
        return session;
    }

    public void addInstanceInUse(Long reference, Object instance) {
        instancesInUse.put(reference, instance);
    }

    public void removeInstanceInUse(Long reference) {
        instancesInUse.remove(reference);
    }

    public void refresh() {
        lastTouched = System.currentTimeMillis();
    }

    public long getLastTouched() {
        return lastTouched;
    }
}
