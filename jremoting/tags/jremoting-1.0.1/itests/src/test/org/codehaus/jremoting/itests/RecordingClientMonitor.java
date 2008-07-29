package org.codehaus.jremoting.itests;

import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.InvocationException;
import org.codehaus.jremoting.client.ConnectionClosedException;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.JRemotingException;

import java.io.IOException;

public class RecordingClientMonitor implements ClientMonitor {
    public StringBuilder sb = new StringBuilder();
    public void methodCalled(Class clazz, String methodSignature, long duration, String annotation) {
        sb.append("methodCalled:").append(methodSignature).append("\n");
    }

    public boolean methodLogging() {
        sb.append("methodLogging\n");
        return false;
    }

    public void serviceSuspended(Class clazz, Request request, int attempt, int suggestedWaitMillis) {
        sb.append("serviceSuspended\n");
    }

    public void serviceAbend(Class clazz, int attempt, IOException cause) {
        sb.append("serviceAbend\n");
    }

    public void invocationFailure(Class clazz, String publishedServiceName, String objectName, String methodSignature, InvocationException ie) {
        sb.append("invocationFailure\n");
    }

    public void unexpectedConnectionClosed(Class clazz, String name, ConnectionClosedException cce) {
        sb.append("unexpectedConnectionClosed\n");
    }

    public void unexpectedInterruption(Class clazz, String name, InterruptedException ie) {
        sb.append("unexpectedInterruption\n");
    }

    public void classNotFound(Class clazz, String msg, ClassNotFoundException cnfe) {
        sb.append("classNotFound\n");
    }

    public void unexpectedIOException(Class clazz, String msg, IOException ioe) {
        sb.append("unexpectedIOException\n");
    }

    public void pingFailure(Class clazz, JRemotingException jre) {
        sb.append("pingFailure\n");
    }
}
