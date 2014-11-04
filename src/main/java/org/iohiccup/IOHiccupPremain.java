/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iohiccup;

import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

/**
 *
 * @author fijiol
 */
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
