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
    
    
    String _getCurrentTime() {
        return get_current_time;
    }
    
    String _str(String arg) {
        return "\"" + arg + "\"";
    }
    
    String _sout(String arg) {
        return "System.out.println(" + arg + ");";
    }
    
    String _plus() {
        return "+";
    }

    @Override
    public boolean needInstrument(String className) {
        return className != null && className.contains("SocketChannelImpl");
    }

    @Override
    public String preCode(String methodName) {
        if (methodName != null && methodName.contains("read")) {
            return _sout (_str("pre : ") + _plus() +  _getCurrentTime() );
        }
        
        return null;
    }
    
    @Override
    public String postCode(String methodName) {
        if (methodName != null && methodName.contains("read")) {
            return _sout (_str("post: ") + _plus() +  _getCurrentTime() );
        }
        
        return null;
    }

    @Override
    public Iterable<String> classNewFields(String className) {
        return Collections.emptyList();
    }

    @Override
    public void init(IOHiccup iOHiccup) {
    }
    
}
