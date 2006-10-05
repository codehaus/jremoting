package org.codehaus.jremoting.client.transports;

public class CustomStreamHeader {
    private final int payLoadLength;
    private final int requestCode;

    public CustomStreamHeader(int payLoadLength, int requestCode) {
        this.payLoadLength = payLoadLength;
        this.requestCode = requestCode;
    }
}
