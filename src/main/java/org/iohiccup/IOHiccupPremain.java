/**
 * Written by Fedor Burdun of Azul Systems, and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Fedor Burdun
 */
package org.iohiccup;

import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

public class IOHiccupPremain {
    public static void premain(String agentArgument, Instrumentation instrumentation) {
        // Exclude CLI option Xbootclasspath
        try {
            instrumentation.appendToBootstrapClassLoaderSearch(
                    new JarFile(IOHiccupPremain.class.getProtectionDomain().
                            getCodeSource().getLocation().getPath()));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        IOHiccup.premain(agentArgument, instrumentation);
    }
    
    public static void main(String[] args) {
        IOHiccup.main(args);
    }
}
