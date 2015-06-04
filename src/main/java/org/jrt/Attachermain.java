/**
 * Written by Fedor Burdun of Azul Systems, and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Fedor Burdun
 */
package org.jrt;

import org.jrt.impl.JRT;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import java.io.IOException;

public class Attachermain {
    
    public static void main(String[] args) {
        //TODO: Exclude CLI option Xbootclasspath/a=...../tools.jar
//        try {
//        Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
//        method.setAccessible(true);
//        method.invoke(ClassLoader.getSystemClassLoader(), new Object[]{new File("/usr/lib/jvm/java-1.7.0-openjdk-amd64/lib/tools.jar").toURI().toURL()});
//        } catch (Exception e) {
//        }
        
        
        boolean needHelp = false;
        String pid = null;
        String agentArguments = "";
        
        for (String s : args) {
            if (s.startsWith("-pid")) {
                String[] p = s.split("=");
                if (p.length==2) {
                    pid = p[1];
                } else {
                    needHelp = true;
                }
            } else if (s.startsWith("-agentargs")) {
                String[] p = s.split("=", 2);
                if (p.length==2) {
                    agentArguments = p[1];
                } else {
                    needHelp = true;
                }
            } else if (s.startsWith("-h") || s.startsWith("--help") || s.startsWith("-help")) {
                needHelp = true;
            } else {
                needHelp = true;
            }
        }
        
        //validate agent arguments
        //print help message and exit if something is wrong
        {
            (new JRT()).parseArguments(agentArguments);
        }
        
        if (needHelp || null == pid) {
            System.err.println("please, to attach jRT to already running application rerun it in next manner:\n\n"
                    + "\tjava -jar jRT.jar -pid=<PID of java VM> -agentargs='<args>' \n\n");
            JRT.printHelpParameters();
            System.exit(1);
        }
        
        try {
            
            VirtualMachine vm = VirtualMachine.attach(pid);
            
            vm.loadAgent(Agentmain.class.getProtectionDomain().
                            getCodeSource().getLocation().getPath(), agentArguments);
            vm.detach();
            System.exit(0);
        
        } catch (IOException e) {
            System.err.println("Seems like java process with pid="+pid+" doesn't exist or not permit to instrument. \nPlease ensure that pid is correct.");
        } catch (AgentInitializationException e) {
            System.err.println("Failed to initialize agent: " + e);
        } catch (AgentLoadException e) {
            System.err.println("Failed to load agent: " + e);
        } catch (AttachNotSupportedException e) {
            System.err.println("Seems like attach isn't supported: " + e);
        }
    }
}
