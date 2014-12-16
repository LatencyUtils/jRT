/*
 * Written by Fedor Burdun of Azul Systems, and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 * 

 */
package org.iohiccup.socket.regular;

import java.util.Collections;
import org.iohiccup.impl.IOHiccup;
import org.iohiccup.socket.api.CodeWriter;

/**
 *
 * @author fijiol
 */
public class JavaNetSocketCodeWrapper implements CodeWriter {
    String time_provider_class = "org.iohiccup.socket.api.TimeProvider";
    String get_current_time = time_provider_class + ".getCurrentTime()";
    String io_hiccup_static = "org.iohiccup.impl.IOHiccup";
    public IOHiccup ioHiccup = null;
    
    
    public String _getCurrentTime() {
        return get_current_time;
    }
    
    public String _str(String arg) {
        return "\"" + arg + "\"";
    }
    
    public String _sout(String arg) {
        return "System.out.println(" + arg + ");";
    }
    
    public String _serr(String arg) {
        return "System.err.println(" + arg + ");";
    }
    
    public String _plus() {
        return "+";
    }
    
    public String _block(String arg) {
        return "{" + arg + "}";
    }
    
    public String _if(String condition, String thenBlock, String elseBlock) {
        return "if (" + condition + ") { " + thenBlock + " } else { " + elseBlock + " } ";
    }
    
    public String _if(String condition, String thenBlock) {
        return _if(condition, thenBlock, "");
    }
    
    public String _rethrowWithPrint(String arg) {
        return 
                "try {" + 
                    arg + 
                "} catch (Exception e) " + 
                "{ "
                    + "System.err.println(" + _str("some exception was thrown during _trace: ") + " + e ); " + 
                    "e.printStackTrace(); "
                    + "throw e; " + 
                "} ";
    }
    
    public String _debugWraps(String arg) {
        if (ioHiccup.configuration.printExceptions) {
            return _rethrowWithPrint(arg);
        } else { 
            return arg;
        }
    }
    
    public String _uniqVar(String name) {
        return ioHiccup.configuration.uuid.replaceAll("", "_") + name;
    }
    
    public String _ioHiccup() {
        return "((org.iohiccup.impl.IOHiccup)" + io_hiccup_static + ".ioHiccupWorkers.get(" + _str(ioHiccup.configuration.uuid) + "))";
    }
    
    public String _ioHic() {
        return _ioHiccup() + ".sockHiccups.get(impl)";
    }
    
    public String _saveIOHic(String ioHicValue) {
        return _block( ioHicValue );
    }
    

    @Override
    public boolean needInstrument(String className) {
        return className != null && (
                className.equals("java/net/SocketInputStream") | 
                className.equals("java/net/SocketOutputStream"));
    }

    @Override
    public String preCode(String methodName) {
        if (methodName == null) {
            return null;
        }

        if (methodName.equals("java.net.SocketInputStream.read(byte[],int,int,int)")) {
            return _debugWraps(
                    Accumulator._readBefore(_ioHiccup(), _ioHic())
            );
        }

        if (methodName.equals("java.net.SocketOutputStream.write(byte[],int,int)")) {
            return _debugWraps(
                    Accumulator._writeBefore(_ioHiccup(), _ioHic())
            );
        }

        
        return null;
    }
    
    @Override
    public String postCode(String methodName) {
        if (methodName == null) {
            return null;
        }
        
        if (methodName.equals("java.net.SocketInputStream(java.net.AbstractPlainSocketImpl)") || 
            methodName.equals("java.net.SocketOutputStream(java.net.AbstractPlainSocketImpl)")) {
            
            return _debugWraps(
                    _saveIOHic(
                            Accumulator._filter(_ioHiccup(), "impl", "impl.getInetAddress()", "impl.getPort()", "impl.getLocalPort()")
                    )
            );
        }
        
       if (methodName.equals("java.net.SocketInputStream.read(byte[],int,int,int)")) {
            return _debugWraps(
                    Accumulator._readAfter(_ioHiccup(), _ioHic())
            );
        }

        if (methodName.equals("java.net.SocketOutputStream.write(byte[],int,int)")) {
            return _debugWraps(
                    Accumulator._writeAfter(_ioHiccup(), _ioHic())
            );
        }

        return null;
    }

    @Override
    public Iterable<String> classNewFields(String className) {
        return Collections.emptyList();
    }

    @Override
    public void init(IOHiccup ioHiccup) {
        this.ioHiccup = ioHiccup;
    }

}
