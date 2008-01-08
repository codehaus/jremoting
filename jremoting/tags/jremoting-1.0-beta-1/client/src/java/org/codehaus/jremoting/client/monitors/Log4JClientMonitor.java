package org.codehaus.jremoting.client.monitors;

import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.InvocationException;
import org.codehaus.jremoting.client.ConnectionClosedException;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.JRemotingException;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

import java.io.IOException;

/**
 * Class Log4JClientMonitor
 *
 * @author Paul Hammant
 * @version * $Revision: 1.2 $
 */
public class Log4JClientMonitor implements ClientMonitor {

    private Logger logger;
    private final ClientMonitor delegate;

    public Log4JClientMonitor(ClientMonitor delegate) {
        this.delegate = delegate;
    }

    public Log4JClientMonitor() {
        this(new NullClientMonitor());
    }

    public Log4JClientMonitor(ClientMonitor delegate, String logName) {
        this.delegate = delegate;
        logger = LogManager.getLogger(logName);
    }

    public Log4JClientMonitor(String logName) {
        this(new NullClientMonitor(), logName);
    }

    public void methodCalled(Class clazz, final String methodSignature, final long duration, String annotation) {
        delegate.methodCalled(clazz, methodSignature, duration, annotation);
    }

    public boolean methodLogging() {
        return false;
    }

    public void serviceSuspended(Class clazz, final Request request, final int attempt, final int suggestedWaitMillis) {
        getLog(clazz).debug("Log4JClientMonitor: serviceSuspended:" + maybeWriteClass(clazz) + "' attempt: '" + attempt + "' waiting: '" + suggestedWaitMillis + "'");
        delegate.serviceSuspended(clazz, request, attempt, suggestedWaitMillis);
    }

    private Logger getLog(Class clazz) {
        Logger l;
        if (logger == null) {
            l = LogManager.getLogger(clazz);
        } else {
            l = logger;
        }
        return l;
    }

    public void serviceAbend(Class clazz, int attempt, IOException cause) {
        getLog(clazz).debug("Log4JClientMonitor: serviceAbend:" + maybeWriteClass(clazz) + "' attempt: '" + attempt + "' IOException: '" + cause.getMessage() + "'", cause );
        delegate.serviceAbend(clazz, attempt, cause);
    }

    public void invocationFailure(Class clazz, String publishedServiceName, String objectName, String methodSignature, InvocationException ie) {
        getLog(clazz).debug("Log4JClientMonitor: invocationFailure:" + maybeWriteClass(clazz) + "' publishedServiceName: '" + publishedServiceName +
                "' objectName: '" + objectName +
                "' methodSignature: '" + methodSignature +
                "' InvocationException: '" + ie.getMessage() + "'", ie );
        delegate.invocationFailure(clazz, publishedServiceName, objectName, methodSignature, ie);
    }

    public void unexpectedConnectionClosed(Class clazz, String name, ConnectionClosedException cce) {
        getLog(clazz).debug("Log4JClientMonitor: unexpectedClosedConnection:" + maybeWriteClass(clazz) + "' name: '" + name + "' ConnectionClosedException: '" + cce.getMessage() + "'", cce);
        delegate.unexpectedConnectionClosed(clazz, name, cce);
    }

    public void unexpectedInterruption(Class clazz, String name, InterruptedException ie) {
        getLog(clazz).debug("Log4JClientMonitor: unexpectedInterruption:" + maybeWriteClass(clazz) + "' name: '" + name + "'", ie);
        delegate.unexpectedInterruption(clazz, name, ie);
    }

    public void classNotFound(Class clazz, String msg, ClassNotFoundException cnfe) {
        getLog(clazz).debug("Log4JClientMonitor: classNotFound:" + maybeWriteClass(clazz) + "' msg: '" + msg + "' ClassNotFoundException: '" + cnfe.getMessage() + "'" , cnfe);
        cnfe.printStackTrace();
        delegate.classNotFound(clazz, msg, cnfe);
    }

    public void unexpectedIOException(Class clazz, String msg, IOException ioe) {
        getLog(clazz).debug("Log4JClientMonitor: unexpectedIOException:" + maybeWriteClass(clazz) + "' msg: '" + msg + "' IOException: '" + ioe.getMessage() + "'" , ioe);
        delegate.unexpectedIOException(clazz, msg, ioe);
    }

    public void pingFailure(Class clazz, JRemotingException jre) {
        getLog(clazz).debug("Log4JClientMonitor: pingFailure:" + maybeWriteClass(clazz) + "' JRemotingException: '" + jre.getMessage() + "'");
    }

    protected String maybeWriteClass(Class clazz) {
        return logger == null ? "" : " for class'" + clazz.getName();
    }

}
