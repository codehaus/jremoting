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
import org.codehaus.jremoting.requests.AbstractRequest;

import java.io.IOException;

/**
 * Interface SimpleRetryingClientMonitor
 *
 * @author Paul Hammant
 * @version * $Revision: 1.2 $
 */
public class SimpleRetryingClientMonitor implements ClientMonitor {

    private final ClientMonitor delegate;
    private final int maxReconnectAttempts;

    /**
     * Creates a new SimpleRetryingClientMonitor.
     */
    public SimpleRetryingClientMonitor() {
        this(3);  // default to 3 reconnect attempts.
    }

    /**
     * Creates a new SimpleRetryingClientMonitor.
     *
     * @param maxReconnectAttempts Specifies the maximum number of times that
     *                             the client will attempt to reconnect to
     *                             the server if the connection is lost.  A
     *                             value of 0 implies that no reconnect
     *                             attempts should be made.
     */
    public SimpleRetryingClientMonitor(int maxReconnectAttempts) {
        this(new NullClientMonitor(), maxReconnectAttempts);
    }

    public SimpleRetryingClientMonitor(ClientMonitor clientMonitor) {
        this(clientMonitor, 3);
    }


    public SimpleRetryingClientMonitor(ClientMonitor clientMonitor, int maxReconnectAttempts) {
        this.delegate = clientMonitor;
        this.maxReconnectAttempts = maxReconnectAttempts;
    }


    /**
     * Method methodCalled
     *
     * @param methodSignature
     * @param duration
     */
    public void methodCalled(Class clazz, final String methodSignature, final long duration, String annotation) {

        delegate.methodCalled(clazz, methodSignature, duration, annotation);
    }

    public boolean methodLogging() {
        return false;
    }

    public void serviceSuspended(Class clazz, final AbstractRequest request, final int attempt, final int suggestedWaitMillis) {

        // Lets say that ten retries is too many.
        if (attempt == 10) {
            throw new InvocationException("Too many retries on suspended service");
        }

        printMessage("JRemoting service suspended, Trying to reconnect (attempt " + attempt + ", waiting for " + suggestedWaitMillis / 1000 + " seconds)");

        // We are quite happy with the recommended wait time.
        try {
            Thread.sleep(suggestedWaitMillis);
        } catch (InterruptedException ie) {
            unexpectedInterruption(this.getClass(), this.getClass().getName(), ie);
        }
    }

    public void serviceAbend(Class clazz, int attempt, IOException cause) {

        // Lets say that ten retries is too many.
        if (attempt >= maxReconnectAttempts) {
            String msg;
            if (maxReconnectAttempts <= 0) {
                msg = "Reconnect to abended service disabled.";
            } else {
                msg = "Too many retries on abended service. ";
                if (cause != null) {
                    msg = msg + "Possible cause of abend (exception=" + cause.getClass().getName() + "). ";
                    if (cause.getMessage() != null) {
                        msg = msg + "Message= '" + cause.getMessage() + "'";
                    } else {
                        msg = msg + "No Message in exception.";
                    }
                } else {
                    msg = msg + "Unknown cause of abend.";
                }
            }
            //TODO replace with call to delegate ?
            throw new InvocationException(msg);
        }

        printMessage("JRemoting service abnormally ended, Trying to reconnect (attempt " + attempt + ")");

        // Increasing wait time.
        try {
            Thread.sleep((2 ^ attempt) * 500);
        } catch (InterruptedException ie) {
            unexpectedInterruption(this.getClass(), this.getClass().getName(), ie);
        }
    }

    public void invocationFailure(Class clazz, String name, InvocationException ie) {
        delegate.invocationFailure(clazz, name, ie);
    }

    public void unexpectedClosedConnection(Class clazz, String name, ConnectionClosedException cce) {
        delegate.unexpectedClosedConnection(clazz, name, cce);
    }

    public void unexpectedInterruption(Class clazz, String name, InterruptedException ie) {
        delegate.unexpectedInterruption(clazz, name, ie);
    }

    public void classNotFound(Class clazz, String msg, ClassNotFoundException cnfe) {
        delegate.classNotFound(clazz, msg, cnfe);
    }

    void printMessage(String message) {
    }

}
