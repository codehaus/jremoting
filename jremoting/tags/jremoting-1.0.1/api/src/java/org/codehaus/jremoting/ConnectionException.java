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
package org.codehaus.jremoting;

import java.io.IOException;

/**
 * Class ConnectionException
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class ConnectionException extends IOException {

    private Throwable throwableCause;
    private static final long serialVersionUID = -2917930346241466338L;

    public ConnectionException(String msg) {
        super(msg);
    }

    public ConnectionException(String message, Throwable cause) {
        super(message);
        throwableCause = cause;
    }

    public Throwable getCause() {
        return throwableCause;
    }

    public String getMessage() {
        return super.getMessage() + (throwableCause != null ? " : " + throwableCause.getMessage() : "");
    }
}
