/*
 * Written by Fedor Burdun of Azul Systems, and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 * 

 */
package org.jrt.socket.regular;

import java.util.Collections;
import org.jrt.impl.JRT;
import org.jrt.socket.api.CodeWriter;

/**
 *
 * @author fijiol
 */
public class JavaNetSocketCodeWrapper implements CodeWriter {
    String time_provider_class = "org.jrt.socket.api.TimeProvider";
    String get_current_time = time_provider_class + ".getCurrentTime()";
    String jrt_static = "org.jrt.impl.JRT";
    public JRT jRT = null;
    
    
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
        if (jRT.configuration.printExceptions) {
            return _rethrowWithPrint(arg);
        } else { 
            return arg;
        }
    }
    
    public String _uniqVar(String name) {
        return jRT.configuration.uuid.replaceAll("", "_") + name;
    }
    
    public String _jRT() {
        return "((org.jrt.impl.JRT)" + jrt_static + ".jRTWorkers.get(" + _str(jRT.configuration.uuid) + "))";
    }
    
    public String _ioHic() {
        return _jRT() + ".sockRTs.get(impl)";
    }
    
    public String _saveJRTHic(String ioHicValue) {
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
                    Accumulator._readBefore(_jRT(), _ioHic())
            );
        }

        if (methodName.equals("java.net.SocketOutputStream.write(byte[],int,int)")) {
            return _debugWraps(
                    Accumulator._writeBefore(_jRT(), _ioHic())
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
                    _saveJRTHic(
                            Accumulator._filter(_jRT(), "impl", "impl.getInetAddress()", "impl.getPort()", "impl.getLocalPort()")
                    )
            );
        }
        
       if (methodName.equals("java.net.SocketInputStream.read(byte[],int,int,int)")) {
            return _debugWraps(
                    Accumulator._readAfter(_jRT(), _ioHic())
            );
        }

        if (methodName.equals("java.net.SocketOutputStream.write(byte[],int,int)")) {
            return _debugWraps(
                    Accumulator._writeAfter(_jRT(), _ioHic())
            );
        }

        return null;
    }

    @Override
    public Iterable<String> classNewFields(String className) {
        return Collections.emptyList();
    }

    @Override
    public void init(JRT jRT) {
        this.jRT = jRT;
    }

}
