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
    private IOHiccup ioHiccup = null;
    
    
    String _getCurrentTime() {
        return get_current_time;
    }
    
    String _str(String arg) {
        return "\"" + arg + "\"";
    }
    
    String _sout(String arg) {
        return "System.out.println(" + arg + ");";
    }
    
    String _serr(String arg) {
        return "System.err.println(" + arg + ");";
    }
    
    String _plus() {
        return "+";
    }
    
    String _block(String arg) {
        return "{" + arg + "}";
    }
    
    String _if(String condition, String thenBlock, String elseBlock) {
        return "if (" + condition + ") { " + thenBlock + " } else { " + elseBlock + " } ";
    }
    
    String _if(String condition, String thenBlock) {
        return _if(condition, thenBlock, "");
    }
    
    String _rethrowWithPrint(String arg) {
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
    
    String _debugWraps(String arg) {
        if (ioHiccup.configuration.printExceptions) {
            return _rethrowWithPrint(arg);
        } else { 
            return arg;
        }
    }
    
    String _uniqVar(String name) {
        return ioHiccup.configuration.uuid.replaceAll("", "_") + name;
    }
    
    String _ioHiccup() {
//        if (true) return "null";
              
        return io_hiccup_static + ".ioHiccupWorkers.get(" + _str(ioHiccup.configuration.uuid) + ")";
    }
    
    String _ioHic() {
//        if (true) return "null";
        
//        try {org.iohiccup.socket.regular.Accumulator.putTimestampWriteBefore(org.iohiccup.impl.IOHiccup.ioHiccupWorkers.get("1"), 
//                org.iohiccup.impl.IOHiccup.ioHiccupWorkers.get("1").sockHiccups.get(impl));} 
//        catch (Exception e) { System.err.println("some exception was thrown during _trace: " + e ); e.printStackTrace(); throw e; } 
//        
//        IOHiccup.ioHiccupWorkers.get(null).sockHiccups.get(null);
        return "((org.iohiccup.impl.IOHiccup)" + _ioHiccup() + ") " + ".sockHiccups.get(impl)";
    }
    
    String _saveIOHic(String ioHicValue) {
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
        
        if (methodName.equals("SocketOutputStream(java.net.AbstractPlainSocketImpl)")) {
            return _debugWraps(
                    _saveIOHic(
                            Accumulator._filter(methodName, methodName, methodName, methodName, methodName)
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
