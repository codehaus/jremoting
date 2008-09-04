package org.codehaus.jremoting.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Closeable;

import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.responses.Response;

/**
 *
 */
public abstract class StreamConnection {

    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final ServerMonitor serverMonitor;
    private final ClassLoader facadesClassLoader;
    private final String connectionDetails;

    public StreamConnection(ServerMonitor serverMonitor,
                                      InputStream inputStream, OutputStream outputStream,
                                      ClassLoader facadesClassLoader, String connectionDetails) {
        this.serverMonitor = serverMonitor;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.facadesClassLoader = facadesClassLoader;
        this.connectionDetails = connectionDetails;
    }

    public final synchronized Request writeResponseAndGetRequest(Response response) throws IOException, ClassNotFoundException {
        if (response != null) {
            writeResponse(response);
        }
        return readRequest();
    }

    protected abstract Request readRequest() throws IOException, ClassNotFoundException;

    protected abstract void writeResponse(Response response) throws IOException;

    public String getConnectionDetails() {
        return connectionDetails;
    }

    public void closeConnection() {
        closeCloseable(inputStream, "input stream");
        closeCloseable(outputStream, "output stream");
    }

    protected void closeCloseable(Closeable closeable, String msg) {
        try {
            closeable.close();
        } catch (IOException e) {
            serverMonitor.closeError(this.getClass(), "StreamConnection.closeConnection(): Failed closing an JRemoting connection "+ msg +": ", e);
        }
    }

    protected InputStream getInputStream() {
        return inputStream;
    }

    protected OutputStream getOutputStream() {
        return outputStream;
    }

    public ClassLoader getFacadesClassLoader() {
        return facadesClassLoader;
    }

    public void initialize() throws IOException {
    }
}
