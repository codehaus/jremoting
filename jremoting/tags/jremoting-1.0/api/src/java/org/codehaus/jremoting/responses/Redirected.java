/* ====================================================================
 * Copyright 2005-2006 JRemoting Committers
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

import java.io.ObjectOutput;
import java.io.IOException;
import java.io.ObjectInput;

public class Redirected extends Response {

    private String to;
    private static final long serialVersionUID = 1486445222268135950L;

    public Redirected(String to) {
        this.to = to;
    }


    /**
     * For Externalization
      */
    public Redirected() {
    }

    public String getRedirectedTo() {
        return to;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(to);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        to = (String) in.readObject();
    }


}