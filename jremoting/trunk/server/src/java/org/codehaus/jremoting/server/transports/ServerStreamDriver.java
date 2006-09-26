package org.codehaus.jremoting.server.transports;

import java.io.IOException;

import org.codehaus.jremoting.api.ConnectionException;
import org.codehaus.jremoting.requests.AbstractRequest;
import org.codehaus.jremoting.responses.AbstractResponse;

public interface ServerStreamDriver {

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

    void initialize() throws IOException;
}
