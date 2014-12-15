/*
 * Written by Fedor Burdun of Azul Systems, and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 * 

 */
package org.iohiccup.socket.mockup;

import org.iohiccup.socket.api.*;
import java.util.Collections;
import org.iohiccup.impl.IOHiccup;

/**
 *
 * @author fijiol
 */
public class CodeWriterMockup implements CodeWriter {
    
    @Override
    public void init(IOHiccup iOHiccup) {
    }
    
    @Override
    public boolean needInstrument(String className) {
        return true;
    }

    @Override
    public Iterable<String> classNewFields(String className) {
        return Collections.emptyList();
    }

    @Override
    public String preCode(String methodName) {
        System.out.println("preCode: " + methodName);
        return null;
    }

    @Override
    public String postCode(String methodName) {
        System.out.println("preCode: " + methodName);
        return null;
    }

    
}
