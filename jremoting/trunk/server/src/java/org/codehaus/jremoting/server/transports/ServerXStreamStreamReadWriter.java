package org.codehaus.jremoting.server.transports;

import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.api.ThreadPool;
import org.codehaus.jremoting.api.ConnectionException;
import org.codehaus.jremoting.commands.Request;
import org.codehaus.jremoting.commands.Response;

import java.io.*;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Class ServerCustomStreamReadWriter
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class ServerXStreamStreamReadWriter extends AbstractServerStreamReadWriter {

    private LineNumberReader lineNumberReader;
    private PrintWriter printWriter;
    private XStream xStream;


    public ServerXStreamStreamReadWriter(ServerMonitor serverMonitor, ThreadPool threadPool) {
        super(serverMonitor, threadPool);
        xStream = new XStream(new DomDriver());
    }

    /**
     * Initialize
     *
     * @throws java.io.IOException if an IO Excpetion
     */
    protected void initialize() throws IOException {
        lineNumberReader = new LineNumberReader(new InputStreamReader(getInputStream()));
        printWriter = new PrintWriter(new BufferedOutputStream(getOutputStream()));
    }

    /**
     * Write a response, and wait for a request
     *
     * @param response The response to send
     * @return The new request
     * @throws java.io.IOException            In an IO Exception
     * @throws org.codehaus.jremoting.api.ConnectionException    In an IO Exception
     * @throws ClassNotFoundException If a class not found during deserialization.
     */
    protected synchronized Request writeReplyAndGetRequest(Response response) throws IOException, ClassNotFoundException, ConnectionException {

        if (response != null) {
            writeReply(response);
        }

        return readRequest();
    }

    private void writeReply(Response response) throws IOException {

        String xml = xStream.toXML(response);

        printWriter.write(xml + "\n");
        printWriter.flush();
    }

    protected void close() {
        try {
            lineNumberReader.close();
        } catch (IOException e) {
        }
        printWriter.close();
        super.close();
    }

    private Request readRequest() throws IOException, ClassNotFoundException, ConnectionException {
        StringBuffer req = new StringBuffer();

        String line = lineNumberReader.readLine();
        req.append(line);
        line = lineNumberReader.readLine();
        while (line != null) {
            req.append("\n").append(line);
            if (!Character.isWhitespace(line.charAt(0))) {
                line = null;
            } else {
                line = lineNumberReader.readLine();
            }
        }

        // todo ClassLoader magic ?  or use Reader with XStream direct ?
        String r = req.toString() + "\n";

        try {
            return (Request) xStream.fromXML(r);
        } catch (ConversionException e) {
            Throwable cause = e.getCause();
            if (cause != null && cause instanceof ClassCastException) {
                throw (ClassCastException) cause;
            } else {
                throw e;
            }
        }
    }
}
