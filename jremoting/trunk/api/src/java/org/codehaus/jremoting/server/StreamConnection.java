package org.codehaus.jremoting.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Closeable;

import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.responses.Response;

/**
 * A server side connection that uses streams.
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

    /**
     * Return a response for the previous request and get another request.
     * @param response the response
     * @return the new request
     * @throws IOException if a problem
     * @throws ClassNotFoundException if a needed class can't be found.
     */
    public final synchronized Request writeResponseAndGetRequest(Response response) throws IOException, ClassNotFoundException {
        if (response != null) {
            writeResponse(response);
        }
        return readRequest();
    }

    /**
     * Read a request
     * @return
     * @throws IOException reading the request could cause an IOException
     * @throws ClassNotFoundException the request could reference a class not in the classpath.
     */
    protected abstract Request readRequest() throws IOException, ClassNotFoundException;

    /**
     * Write a response
     * @param response the response to write
     * @throws IOException writing the response could cause an IOException
     */
    protected abstract void writeResponse(Response response) throws IOException;

    /**
     * Get a representation of the connection type itself.
     * @return
     */
    public String getConnectionDetails() {
        return connectionDetails;
    }

    /**
     * Close the connection
     */
    public void closeConnection() {
        closeCloseable(inputStream, "input stream");
        closeCloseable(outputStream, "output stream");
    }

    /**
     * Close a Closeable thing.
     * @param closeable the thing to close
     * @param msg a message for use if the thing can't be closed.
     */
    protected void closeCloseable(Closeable closeable, String msg) {
        try {
            closeable.close();
        } catch (IOException e) {
            serverMonitor.closeError(this.getClass(), "StreamConnection.closeConnection(): Failed closing an JRemoting connection "+ msg +": ", e);
        }
    }

    /**
     * The input stream being used
     * @return
     */
    protected InputStream getInputStream() {
        return inputStream;
    }

    /**
     * The output stream being used
     * @return
     */
    protected OutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * The facades classloader
     * @return
     */
    protected ClassLoader getFacadesClassLoader() {
        return facadesClassLoader;
    }

    /**
     * Initialize the connection.
     * @throws IOException if a problem during initialization
     */
    public void initialize() throws IOException {
    }
}
