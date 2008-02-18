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
                protocolEncoderOutput.write(aBytes.length);
                protocolEncoderOutput.write(aBytes);
                protocolEncoderOutput.flush();
            }
            public void dispose(IoSession ioSession) throws Exception {
            }
        };
    }

    public ProtocolDecoder getDecoder(IoSession ioSession) throws Exception {
        return new ProtocolDecoder() {
            public void decode(IoSession ioSession, IoBuffer ioBuffer, ProtocolDecoderOutput protocolDecoderOutput) throws Exception {
                int size = ioBuffer.getInt();
                byte[] req = new byte[size];
                ioBuffer.get(req);
                Object foo = SerializationHelper.getInstanceFromBytes(req, this.getClass().getClassLoader());
                protocolDecoderOutput.write(foo);
            }
            public void finishDecode(IoSession ioSession, ProtocolDecoderOutput protocolDecoderOutput) throws Exception {
            }
            public void dispose(IoSession ioSession) throws Exception {
            }
        };
    }
}
