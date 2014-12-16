/*
 * Written by Fedor Burdun of Azul Systems, and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 * 

 */
package org.iohiccup.socket.nio;

import org.iohiccup.impl.IOHiccup;
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
                (className.equals("sun/nio/ch/IOUtil") || 
                 className.equals("sun/nio/ch/net"));
    }

    @Override
    public String postCode(String methodName) {
        System.out.println("method: " + methodName);
        
        if (true) return _sout(_str(methodName) + " + java.util.Arrays.deepToString($args) ") + "(new Throwable()).printStackTrace();";
        return null;
    }

    @Override
    public String preCode(String methodName) {
        return null;
    }
    
    
}
