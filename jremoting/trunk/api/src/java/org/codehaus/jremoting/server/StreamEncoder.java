package org.codehaus.jremoting.server;

import java.io.IOException;

import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.responses.Response;

public interface StreamEncoder {

    /**
     * Write a Response, then Get a new Request over the stream.
     *
     * @param response The response to pass back to the client
     * @return The Request that is new and incoming
     * @throws IOException            if a problem during write & read.
     * @throws ConnectionException    if a problem during write & read.
     * @throws ClassNotFoundException If a Class is not found during serialization.
     */
    Request writeResponseAndGetRequest(Response response) throws IOException, ConnectionException, ClassNotFoundException;

    Object getConnectionDetails();

    void close();

    void initialize() throws IOException;
}
