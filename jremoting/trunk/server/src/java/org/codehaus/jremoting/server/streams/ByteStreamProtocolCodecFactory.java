package org.codehaus.jremoting.server.streams;

import org.apache.mina.filter.codec.*;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.IoBuffer;
import org.codehaus.jremoting.util.SerializationHelper;

public class ByteStreamProtocolCodecFactory implements ProtocolCodecFactory {
    public ProtocolEncoder getEncoder(IoSession ioSession) throws Exception {
        return new ProtocolEncoder() {
            public void encode(IoSession ioSession, Object o, ProtocolEncoderOutput protocolEncoderOutput) throws Exception {
                byte[] aBytes = SerializationHelper.getBytesFromInstance(o);
                IoBuffer iob = IoBuffer.allocate(aBytes.length+4);
                //protocolEncoderOutput.write(aBytes.length);
                iob.putInt(aBytes.length);
                //protocolEncoderOutput.write(aBytes);
                iob.put(aBytes);
                iob.flip();
                protocolEncoderOutput.write(iob);
                protocolEncoderOutput.flush();
            }
            public void dispose(IoSession ioSession) throws Exception {
            }
        };
    }

    public ProtocolDecoder getDecoder(IoSession ioSession) throws Exception {
        return new CumulativeProtocolDecoder() {
            protected boolean doDecode(IoSession ioSession, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
                // Ensure we have enough data to even start processing (this is the header)
                // payload size
                if (in.remaining() < 4) {
                    return false;
                }

                // Save our current position
                in.mark();

                // Get the payload size
                int payloadSize = in.getInt(); //4

                // Ensure we have the full payload
                if (in.remaining() < payloadSize) {
                    in.reset();
                    return false;
                }

                byte[] req = new byte[payloadSize];
                in.get(req);
                Object foo = SerializationHelper.getInstanceFromBytes(req, this.getClass().getClassLoader());
                out.write(foo);
                return true;
            }
        };
    }
}
