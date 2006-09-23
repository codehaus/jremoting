package org.codehaus.jremoting.server.transports;

import org.codehaus.jremoting.requests.AbstractRequest;
import org.codehaus.jremoting.responses.AbstractResponse;
import org.codehaus.jremoting.api.ConnectionException;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public interface ServerStreamDriver {

    /**
     * Initialize the Driver.
     *
     * @throws IOException if a problem during initialization.
     */
    void initialize() throws IOException;

    /**
     * Method setStreams
     *
     * @param inputStream  The input stream
     * @param outputStream the outpur stream
     */
    void setStreams(InputStream inputStream, OutputStream outputStream, Object connectionDetails);

    /**
     * Write a AbstractResponse, then Get a new AbstractRequest over the stream.
     *
     * @param response The response to pass back to the client
     * @return The AbstractRequest that is new and incoming
     * @throws IOException            if a problem during write & read.
     * @throws ConnectionException    if a problem during write & read.
     * @throws ClassNotFoundException If a Class is not found during serialization.
     */
    AbstractRequest writeResponseAndGetRequest(AbstractResponse response) throws IOException, ConnectionException, ClassNotFoundException;

    Object getConnectionDetails();

    void close();
}
