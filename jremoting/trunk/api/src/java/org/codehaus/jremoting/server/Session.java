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

    private Long session;

    private HashMap<Long, Object> beansInUse = new HashMap<Long, Object>();
    private long lastTouched;

    public Session(Long session) {
        this.session = session;
    }

    public Long getSession() {
        return session;
    }

    public void addBeanInUse(Long referenceID, Object bean) {
        beansInUse.put(referenceID, bean);
    }

    public void removeBeanInUse(Long referenceID) {
        beansInUse.remove(referenceID);
    }

    public void refresh() {
        lastTouched = System.currentTimeMillis();
    }

    public long getLastTouched() {
        return lastTouched;
    }
}
