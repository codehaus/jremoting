package org.codehaus.jremoting.client.monitors;

import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.InvocationException;
import org.codehaus.jremoting.client.ConnectionClosedException;
import org.codehaus.jremoting.requests.AbstractRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * Class CommonsLoggingClientMonitor
 *
 * @author Paul Hammant
 * @version * $Revision: 1.2 $
 */
public class CommonsLoggingClientMonitor implements ClientMonitor {

    private Log log;
    private final ClientMonitor delegate;

    public CommonsLoggingClientMonitor(ClientMonitor delegate) {
        this.delegate = delegate;
    }

    public CommonsLoggingClientMonitor() {
        this(new NullClientMonitor());
    }

    public CommonsLoggingClientMonitor(ClientMonitor delegate, String logName) {
        this.delegate = delegate;
        log = LogFactory.getLog(logName);
    }

    public CommonsLoggingClientMonitor(String logName) {
        this(new NullClientMonitor(), logName);
    }

    public void methodCalled(Class clazz, final String methodSignature, final long duration, String annotation) {
        delegate.methodCalled(clazz, methodSignature, duration, annotation);
    }

    public boolean methodLogging() {
        return false;
    }

    public void serviceSuspended(Class clazz, final AbstractRequest request, final int attempt, final int suggestedWaitMillis) {
        getLog(clazz).debug("ConsoleClientMonitor: serviceSuspended:" + maybeWriteClass(clazz) + "' attempt: '" + attempt + "' waiting: '" + suggestedWaitMillis + "'");
        delegate.serviceSuspended(clazz, request, attempt, suggestedWaitMillis);
    }

    private Log getLog(Class clazz) {
        Log l;
        if (log == null) {
            l = LogFactory.getLog(clazz);
        } else {
            l = log;
        }
        return l;
    }

    public void serviceAbend(Class clazz, int attempt, IOException cause) {
        getLog(clazz).debug("ConsoleClientMonitor: serviceAbend:" + maybeWriteClass(clazz) + "' attempt: '" + attempt + "' IOException: '" + cause.getMessage() + "'", cause );
        delegate.serviceAbend(clazz, attempt, cause);
    }

    public void invocationFailure(Class clazz, String publishedServiceName, String objectName, String methodSignature, InvocationException ie) {
        getLog(clazz).debug("ConsoleClientMonitor: invocationFailure:" + maybeWriteClass(clazz) + "' publishedServiceName: '" + publishedServiceName +
                "' objectName: '" + objectName +
                "' methodSignature: '" + methodSignature +
                "' InvocationException: '" + ie.getMessage() + "'", ie );
        delegate.invocationFailure(clazz, publishedServiceName, objectName, methodSignature, ie);
    }

    public void unexpectedConnectionClosed(Class clazz, String name, ConnectionClosedException cce) {
        getLog(clazz).debug("ConsoleClientMonitor: unexpectedClosedConnection:" + maybeWriteClass(clazz) + "' name: '" + name + "' ConnectionClosedException: '" + cce.getMessage() + "'", cce);
        delegate.unexpectedConnectionClosed(clazz, name, cce);
    }

    public void unexpectedInterruption(Class clazz, String name, InterruptedException ie) {
        getLog(clazz).debug("ConsoleClientMonitor: unexpectedInterruption:" + maybeWriteClass(clazz) + "' name: '" + name + "'", ie);
        delegate.unexpectedInterruption(clazz, name, ie);
    }

    public void classNotFound(Class clazz, String msg, ClassNotFoundException cnfe) {
        getLog(clazz).debug("ConsoleClientMonitor: classNotFound:" + maybeWriteClass(clazz) + "' msg: '" + msg + "' ClassNotFoundException: '" + cnfe.getMessage() + "'" , cnfe);
        cnfe.printStackTrace();
        delegate.classNotFound(clazz, msg, cnfe);
    }

    public InvocationException unexpectedIOException(Class clazz, String msg, IOException ioe) {
        getLog(clazz).debug("ConsoleClientMonitor: unexpectedIOException:" + maybeWriteClass(clazz) + "' msg: '" + msg + "' IOException: '" + ioe.getMessage() + "'" , ioe);
        return delegate.unexpectedIOException(clazz, msg, ioe);
    }

    protected String maybeWriteClass(Class clazz) {
        return log == null ? "" : " for class'" + clazz.getName();
    }

}
