/**
 * Written by Fedor Burdun of Azul Systems, and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Fedor Burdun
 */
package org.iohiccup;

import org.iohiccup.socket.api.attachable.IOHiccupAttachable;
import org.iohiccup.impl.IOHiccup;
import java.lang.instrument.Instrumentation;

import java.util.jar.JarFile;

public class Agentmain {
    
    
    public static void premain(String agentArgument, Instrumentation instrumentation) {
        commonmain(agentArgument, instrumentation);

        try {
            IOHiccup.premain0(agentArgument, instrumentation);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        //instrumentation.addTransformer(new IOHiccupTransformer(IOHiccup.premain0(agentArgument, instrumentation)));
    }

    private static void commonmain(String arguments, Instrumentation instrumentation) {
        // Exclude CLI option Xbootclasspath
        try {
            instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(Agentmain.class.getProtectionDomain().
                            getCodeSource().getLocation().getPath()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public static void agentmain(String agentArgument, Instrumentation instrumentation) {
        
        commonmain(agentArgument, instrumentation);
        
        try {
            IOHiccupAttachable.premain0(agentArgument, instrumentation);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        System.err.println("There is no main() method.");
        System.exit(1);
    }
}
