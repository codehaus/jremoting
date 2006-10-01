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
package org.codehaus.jremoting.requests;

import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.requests.RequestConstants;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Class GroupedMethodRequest
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class GroupedMethodRequest extends Request {

    static final long serialVersionUID = 2433454402872395509L;

    private String methodSignature;
    private Object[] args;

    public GroupedMethodRequest() {
    }

    public GroupedMethodRequest(String methodSignature, Object[] args) {
        this.methodSignature = methodSignature;
        this.args = args;
    }

    public String getMethodSignature() {
        return methodSignature;
    }

    public Object[] getArgs() {
        return args;
    }

    public int getRequestCode() {
        return RequestConstants.METHODASYNCREQUEST;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(methodSignature);
        out.writeObject(args);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        methodSignature = (String) in.readObject();
        args = (Object[]) in.readObject();
    }

}
