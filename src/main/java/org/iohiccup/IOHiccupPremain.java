/**
 * Written by Fedor Burdun of Azul Systems, and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Fedor Burdun
 */
package org.iohiccup;

import com.sun.tools.attach.VirtualMachine;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

public class IOHiccupPremain {
    
    
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
            instrumentation.appendToBootstrapClassLoaderSearch(
                    new JarFile(IOHiccupPremain.class.getProtectionDomain().
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
        
//        IOHiccup.main(args);
        boolean hasPid = false;
        String pid = null;
        StringBuilder sb = new StringBuilder();
        
        for (String s : args) {
            if (s.startsWith("-pid")) {
                String[] p = s.split("=");
                if (p.length==2) {
                    pid = p[1];
                    hasPid = true;
                }
            } else {
                if (sb.length()>0){ 
                    sb.append(",");
                }
                sb.append(s);
            }
        }
        
        if (!hasPid) {
            System.err.println("please, rerun with -pid=%pid parameter");
            System.exit(1);
        }
        
        try {
            
            VirtualMachine vm = VirtualMachine.attach(pid);
            
            vm.loadAgent(IOHiccupPremain.class.getProtectionDomain().
                    
                            getCodeSource().getLocation().getPath(), sb.toString());
            vm.detach();
            System.exit(0);
        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
