/* ====================================================================
 * Copyright 2005 JRemoting Committers
 * Portions copyright 2001 - 2004 Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.codehaus.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.codehaus.jremoting.client.factories;

import org.codehaus.jremoting.client.Proxy;

/**
 * Implementation of Proxy(client stub) for dynamic invocation.
 *
 * @author <a href="mailto:vinayc@apache.org">Vinay Chandran</a>
 */
public class DynamicStub implements Proxy {
    //-------Variables-----------//
    /**
     * Name of the published obj for which this is a stub
     */
    private String publishedName;
    /**
     * ObjectName of the stub(facades have diff obj names)
     */
    private String objectName;
    /**
     * Proxy Helper
     */
    private DefaultProxyHelper proxyHelper;

    //-------Constructor--------------//
    /**
     * Constructor
     */
    public DynamicStub(String publishedName, String objectName, DefaultProxyHelper proxyHelper) {
        this.publishedName = publishedName;
        this.objectName = objectName;
        this.proxyHelper = proxyHelper;
    }

    //------Proxy override---------------------//

    /**
     * @see org.codehaus.jremoting.client.Proxy#codehausRemotingGetReferenceID(Object)
     */

    public Long codehausRemotingGetReferenceID(Object factoryThatIsAsking) {
        return proxyHelper.getReferenceID(factoryThatIsAsking);
    }

    //--------Methods----------------//
    /**
     * Invoke the method with the given arguments
     * (todo: passing the methodSingature is quite clumsy.
     * Replace with method name).
     * (todo: Remove the Class[] argument).
     * (todo: More).
     *
     * @param methodSignature the method to invoke
     * @param args            Argument array
     * @param argClasses      Classes of the respec parameters
     * @return the result of the invocation.
     */
    public Object invoke(String methodSignature, Object[] args, Class[] argClasses) {
        try {
            Object retVal = proxyHelper.processObjectRequest(methodSignature, args, argClasses);
            return retVal;
        } catch (Throwable t) {
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                t.printStackTrace();
                throw new org.codehaus.jremoting.client.InvocationException("Should never get here: " + t.getMessage());
            }
        }
    }


}
