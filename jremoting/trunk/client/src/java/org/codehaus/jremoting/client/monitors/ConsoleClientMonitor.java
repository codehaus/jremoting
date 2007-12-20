package org.codehaus.jremoting.client.monitors;

import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.InvocationException;
import org.codehaus.jremoting.client.ConnectionClosedException;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.JRemotingException;

import java.io.IOException;

/**
 * Class ConsoleClientMonitor
 *
 * @author Paul Hammant
 * @version * $Revision: 1.2 $
 */
public class ConsoleClientMonitor implements ClientMonitor {

    private final ClientMonitor delegate;

    public ConsoleClientMonitor(ClientMonitor delegate) {
        this.delegate = delegate;
    }

    public ConsoleClientMonitor() {
        this(new NullClientMonitor());
    }

    public void methodCalled(Class clazz, final String methodSignature, final long duration, String annotation) {
        delegate.methodCalled(clazz, methodSignature, duration, annotation);
    }

    public boolean methodLogging() {
        return false;
    }

    public void serviceSuspended(Class clazz, final Request request, final int attempt, final int suggestedWaitMillis) {
        System.out.println("ConsoleClientMonitor: serviceSuspended: for class'" + clazz.getName() + "' attempt: '" + attempt + "' waiting: '" + suggestedWaitMillis + "'" );
        delegate.serviceSuspended(clazz, request, attempt, suggestedWaitMillis);
    }

    public void serviceAbend(Class clazz, int attempt, IOException cause) {
        System.out.println("ConsoleClientMonitor: serviceAbend: for class'" + clazz.getName() + "' attempt: '" + attempt + "' IOException: '" + cause.getMessage() + "'" );
        cause.printStackTrace();
        delegate.serviceAbend(clazz, attempt, cause);
    }

    public void invocationFailure(Class clazz, String publishedServiceName, String objectName, String methodSignature, InvocationException ie) {
        System.out.println("ConsoleClientMonitor: invocationFailure: for class'" + clazz.getName() + "' publishedServiceName: '" + publishedServiceName +
                "' objectName: '" + objectName +
                "' methodSignature: '" + methodSignature +
                "' InvocationException: '" + ie.getMessage() + "'" );
        ie.printStackTrace();
        delegate.invocationFailure(clazz, publishedServiceName, objectName, methodSignature, ie);
    }

    public void unexpectedConnectionClosed(Class clazz, String name, ConnectionClosedException cce) {
        System.out.println("ConsoleClientMonitor: unexpectedClosedConnection: for class'" + clazz.getName() + "' name: '" + name + "' ConnectionClosedException: '" + cce.getMessage() + "'" );
        cce.printStackTrace();
        delegate.unexpectedConnectionClosed(clazz, name, cce);
    }

    public void unexpectedInterruption(Class clazz, String name, InterruptedException ie) {
        System.out.println("ConsoleClientMonitor: unexpectedInterruption: for class'" + clazz.getName() + "' name: '" + name + "'");
        ie.printStackTrace();
        delegate.unexpectedInterruption(clazz, name, ie);
    }

    public void classNotFound(Class clazz, String msg, ClassNotFoundException cnfe) {
        System.out.println("ConsoleClientMonitor: classNotFound: for class'" + clazz.getName() + "' msg: '" + msg + "' ClassNotFoundException: '" + cnfe.getMessage() + "'" );
        cnfe.printStackTrace();
        delegate.classNotFound(clazz, msg, cnfe);
    }

    public InvocationException unexpectedIOException(Class clazz, String msg, IOException ioe) {
        System.out.println("ConsoleClientMonitor: unexpectedIOException: for class'" + clazz.getName() + "' msg: '" + msg + "' IOException: '" + ioe.getMessage() + "'" );
        ioe.printStackTrace();
        return delegate.unexpectedIOException(clazz, msg, ioe);
    }

    public void pingFailure(Class clazz, JRemotingException jre) {
        System.out.println("ConsoleClientMonitor: pingFailure: for class'" + clazz.getName() + "' JRemotingException: '" + jre.getMessage() + "'" );
    }

}
