/*
 * Written by Fedor Burdun of Azul Systems, and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 * 

 */
package org.iohiccup.socket.api;

import org.iohiccup.impl.IOHiccup;

/**
 *
 * @author fijiol
 */
public interface CodeWriter {
    
    // place to do some preconditions here, like to put ioHiccup to some place
    // visible to instrumented code
    public void init(IOHiccup iOHiccup);
    
    public boolean needInstrument(String className);

    public Iterable<String> classNewFields(String className);

    public String preCode(String methodName);

    public String postCode(String methodName);
    
}
