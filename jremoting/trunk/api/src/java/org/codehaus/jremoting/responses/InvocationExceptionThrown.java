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
package org.codehaus.jremoting.responses;

import org.codehaus.jremoting.responses.Response;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Class InvocationExceptionThrown
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class InvocationExceptionThrown extends Response {

//    static final long serialVersionUID = TODO;

    private String message;

    public InvocationExceptionThrown(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(message);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        message = (String) in.readObject();
    }

}
