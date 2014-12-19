/*
 * Written by Fedor Burdun of Azul Systems, and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 * 

 */
package org.iohiccup.socket.nio;

import org.iohiccup.impl.IOHiccup;
import org.iohiccup.socket.regular.Accumulator;
import org.iohiccup.socket.regular.JavaNetSocketCodeWrapper;

/**
 *
 * @author fijiol
 */
public class NioSocketCodeWrapper extends JavaNetSocketCodeWrapper {

    @Override
    public void init(IOHiccup ioHiccup) {
        super.init(ioHiccup);
    }

    @Override
    public boolean needInstrument(String className) {
        return className != null && 
                (
                className.equals("sun/nio/ch/IOUtil") || 
                className.equals("sun/nio/ch/SocketChannelImpl") ||
                false
                );
    }

    @Override
    public String postCode(String methodName) {
        
        if (methodName.equals("sun.nio.ch.SocketChannelImpl(java.nio.channels.spi.SelectorProvider,java.io.FileDescriptor,java.net.InetSocketAddress)")) {
            return _block(
                    Accumulator._filter(_ioHiccup(), "fd", "remoteAddress.getAddress()", "remoteAddress.getPort()", "localAddress.getPort()")
            );
        }

        if (methodName.equals("sun.nio.ch.SocketChannelImpl.connect(java.net.SocketAddress)")) {
            return _block(
                    Accumulator._filter(_ioHiccup(), "fd", "((java.net.InetSocketAddress)sa).getAddress()", "((java.net.InetSocketAddress)sa).getPort()", "0")
            );
        }

        if (methodName.contains("sun.nio.ch.IOUtil.read(java.io.FileDescriptor,")) {
            return _debugWraps(
                    Accumulator._readAfter(_ioHiccup(), _ioHic())
            );
        }

        if (methodName.contains("sun.nio.ch.IOUtil.write(java.io.FileDescriptor,")) {
            return _debugWraps(
                    Accumulator._writeAfter(_ioHiccup(), _ioHic())
            );
        }
        
        return null;
    }

    @Override
    public String preCode(String methodName) {
       if (methodName == null) {
            return null;
        }
                              
        if (methodName.contains("sun.nio.ch.IOUtil.read(java.io.FileDescriptor,")) {
            
            return _debugWraps(
                    Accumulator._readBefore(_ioHiccup(), _ioHic())
            );
        }

        if (methodName.contains("sun.nio.ch.IOUtil.write(java.io.FileDescriptor,")) {
            return _debugWraps(
                    Accumulator._writeBefore(_ioHiccup(), _ioHic())
            );
        }

        return null;
    }
    
    
    @Override
    public String _ioHic() {
        return _ioHiccup() + ".sockHiccups.get(fd)";
    }
    
       
}

