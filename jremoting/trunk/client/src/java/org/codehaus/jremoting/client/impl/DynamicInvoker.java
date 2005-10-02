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
package org.codehaus.jremoting.client.impl;

import org.codehaus.jremoting.client.ClientInvocationHandler;
import org.codehaus.jremoting.client.HostContext;
import org.codehaus.jremoting.client.factories.DynamicClassFactory;
import org.codehaus.jremoting.client.factories.DynamicStub;
import org.codehaus.jremoting.commands.ListMethodsResponse;
import org.codehaus.jremoting.commands.ListMethodsRequest;
import org.codehaus.jremoting.commands.Response;
import org.codehaus.jremoting.api.ConnectionException;

import java.util.HashMap;
import java.util.Map;

/**
 * DynamicInvoker enables  complete dynamic
 * invocation of the remote methods ;Its a stubless execution working
 * directly with the JRemoting Remoting defined request and response messages.
 * .
 * One creates a DynamicInvoker as follows:
 * <code>
 * //create custom HostContext for the specific transport
 * HostContext arhc;
 * arhc = new SocketCustomStreamHostContext("127.0.0.1", 1235);
 * <p/>
 * //Initialize the dynamic invoker
 * DynamicInvoker invoker = new DynamicInvoker(arhc);
 * <p/>
 * </code>
 * Subsequently client can auto-magically query as well as execute the methods
 * on the server without having to bother about the stubs.
 * <code>
 * //Get the list quotes listed on the stock exchange
 * String[] quotes=(String[])invoker.invoke("StockExchange","getQuotes()",null,null);
 * </code>
 *
 * @author <a href="mailto:vinayc@apache.org">Vinay Chandran</a>
 */
public class DynamicInvoker {
    //------Variables------------//

    /**
     * Factory
     */
    private DynamicClassFactory factory;
    /**
     * ClientInvocationHandler
     */
    private ClientInvocationHandler clientInvocationHandler;
    /**
     * Cache of  stubs lookeup by the client
     */
    private Map stubs = new HashMap();

    //-------Constructor---------//
    /**
     * Constructor
     *
     * @param hostContext
     * @throws Exception
     */
    public DynamicInvoker(HostContext hostContext) throws Exception {

        factory = new DynamicClassFactory(hostContext, false);
        //cache the invocationhandler
        clientInvocationHandler = hostContext.getInvocationHandler();
    }

    //--------Methods---------------//

    /**
     * Close the underlying transport
     */
    public void close() {
        factory.close();
    }

    /**
     * Retrieve the list of publishedObjects on the server.
     * Re-uses the impl within AbstractFactory.
     */
    public String[] list() {
        return factory.list();
    }

    /**
     * Retrieve the list of remote methods of the given published object
     *
     * @param publishedName name
     * @return String[] list of remote methods
     */

    public String[] listOfMethods(String publishedName) {
        Response ar = clientInvocationHandler.handleInvocation(new ListMethodsRequest(publishedName));
        return ((ListMethodsResponse) ar).getListOfMethods();
    }

    /**
     * Invoke the  remote method.
     * <p/>
     * Todo: Enable invocation given the ONLY the method name(now entire signature is needed)
     * Todo: Remove the need to pass Class[] during invocation.
     * Todo: Enable invocations with String array being passed as arguments.
     * This would be resolved to their proper type on the server.(using BeanUtils.ConverterUtils)
     *
     * @param publishedName published objects name
     * @param methodName    signature of the method (this is quite clumsy)
     * @param args          arguments to the function
     * @param argClasses    corresponding classes of the arguments.
     * @return Object the return value of the remote call
     * @throws ConnectionException If a problem
     */
    public Object invoke(String publishedName, String methodName, Object[] args, Class[] argClasses) throws ConnectionException {
        //check the stub cache
        DynamicStub stub = (DynamicStub) stubs.get(publishedName);
        if (stub == null) {
            stub = (DynamicStub) factory.lookup(publishedName);
            stubs.put(publishedName, stub);
        }
        if (args == null) {
            args = new Object[0];
        }
        if (argClasses == null) {
            argClasses = new Class[0];
        }
        // Regenerate the methodSignature so that its conformant with whats expected
        // on the server side.
        //Right now the methodSignature that is passed to the server needs to
        // be formatted with arguments spaced by commas and a space.
        // e,g, hello4(float, double)
        methodName = methodName.trim();
        // if there are multiple arguments,the space them properly
        StringBuffer buf = new StringBuffer(methodName);
        for (int i = 0; i < buf.length(); i++) {
            char c = buf.charAt(i);
            if (c == ' ') {
                buf.deleteCharAt(i);
                i--; // account for the deletion
            }
            if (c == ',') {
                buf.insert(i + 1, ' ');
                i++;
            }

        }
        methodName = buf.toString();

        //invoke the operation on the stub
        Object ret = stub.invoke(methodName, args, argClasses);

        return ret;
    }

}
