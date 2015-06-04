/*
 * Written by Fedor Burdun of Azul Systems, and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 * 

 */
package org.jrt.socket.mockup;

import org.jrt.socket.api.*;
import java.util.Collections;
import org.jrt.impl.JRT;

/**
 *
 * @author fijiol
 */
public class CodeWriterMockup implements CodeWriter {
    
    @Override
    public void init(JRT jrt) {
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
