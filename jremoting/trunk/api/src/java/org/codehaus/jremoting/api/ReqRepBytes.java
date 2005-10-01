/* ====================================================================
 * Copyright 2005 JRemoting Committers
 * Portions copyright 2001 - 2004 Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.codehaus.jremoting.api;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * ReqRepBytes
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class ReqRepBytes {
    int byteSize;
    byte[] bytes;
    Boolean isRequest;
    IOException ioe;

    public ReqRepBytes(int byteSize, byte[] bytes, Boolean isRequest, IOException ioe) {
        this.byteSize = byteSize;
        this.bytes = bytes;
        this.isRequest = isRequest;
        this.ioe = ioe;
    }

    public boolean ioeDuringReadInt() {
        return (ioe != null & byteSize == 0);
    }

    public boolean hadIOE() {
        return (ioe != null);
    }

    public int getByteSize() {
        return byteSize;
    }

    public byte[] getBytes() {
        return bytes;
    }

    // request or reply
    public boolean isRequest() {
        return isRequest.booleanValue();
    }

    public IOException getIOException() {
        return ioe;
    }

    public static ReqRepBytes getRequestReplyBytesFromDataStream(DataInputStream dis) {
        int byteArraySize = 0;
        Boolean isRequest = null;
        byte[] byteArray = null;
        IOException ioe = null;
        try {
            byteArraySize = dis.readInt();
            isRequest = dis.readBoolean() ? Boolean.TRUE : Boolean.FALSE;
            byteArray = new byte[byteArraySize];
            dis.read(byteArray);
        } catch (IOException e) {
            ioe = e;
        }
        return new ReqRepBytes(byteArraySize, byteArray, isRequest, ioe);
    }


}
