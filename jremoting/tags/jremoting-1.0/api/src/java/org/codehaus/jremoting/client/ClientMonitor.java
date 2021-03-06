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
package org.codehaus.jremoting.client;

import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.JRemotingException;

import java.io.IOException;

/**
 * Interface ClientMonitor
 *
 * @author Paul Hammant
 * @version * $Revision: 1.2 $
 */
public interface ClientMonitor {

    void methodCalled(Class clazz, String methodSignature, long duration, String annotation);

    boolean methodLogging();

    void serviceSuspended(Class clazz, Request request, int attempt, int suggestedWaitMillis);

    void serviceAbend(Class clazz, int attempt, IOException cause);

    void invocationFailure(Class clazz, String publishedServiceName, String objectName, String methodSignature, InvocationException ie);

    void unexpectedConnectionClosed(Class clazz, String name, ConnectionClosedException cce);

    void unexpectedInterruption(Class clazz, String name, InterruptedException ie);

    void classNotFound(Class clazz, String msg, ClassNotFoundException cnfe);

    void unexpectedIOException(Class clazz, String msg, IOException ioe);

    void pingFailure(Class clazz, JRemotingException jre);
}
