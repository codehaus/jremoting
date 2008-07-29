/* ====================================================================
 * Copyright 2005-2006 JRemoting Committers
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
package org.codehaus.jremoting.client.monitors;

import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionClosedException;
import org.codehaus.jremoting.client.InvocationException;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.JRemotingException;

import java.io.IOException;

/**
 * Class NullClientMonitor
 *
 * @author Paul Hammant
 * @version * $Revision: 1.2 $
 */
public class NullClientMonitor implements ClientMonitor {

    public void methodCalled(Class clazz, final String methodSignature, final long duration, String annotation) {
    }

    public boolean methodLogging() {
        return false;
    }

    public void serviceSuspended(Class clazz, final Request request, final int attempt, final int suggestedWaitMillis) {
        throw new InvocationException("Service suspended");
    }

    public void serviceAbend(Class clazz, int attempt, IOException cause) {
        throw new InvocationException("JRemoting Service has Abnormally ended.", cause);
    }

    public void invocationFailure(Class clazz, String publishedServiceName, String objectName, String methodSignature, InvocationException ie) {
    }

    public void unexpectedConnectionClosed(Class clazz, String name, ConnectionClosedException cce) {
    }

    public void unexpectedInterruption(Class clazz, String name, InterruptedException ie) {
    }

    public void classNotFound(Class clazz, String msg, ClassNotFoundException cnfe) {
    }

    public void unexpectedIOException(Class clazz, String msg, IOException ioe) {
    }

    public void pingFailure(Class clazz, JRemotingException jre) {
    }

}
