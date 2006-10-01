package org.codehaus.jremoting.responses;

import java.io.ObjectOutput;
import java.io.IOException;
import java.io.ObjectInput;

public class AuthenticationFailed extends Response {

    public AuthenticationFailed() {
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
    }
}
