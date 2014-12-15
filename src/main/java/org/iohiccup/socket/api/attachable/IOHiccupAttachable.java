/**
 * Written by Fedor Burdun of Azul Systems, and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Fedor Burdun
 */
package org.iohiccup.socket.api.attachable;

import org.iohiccup.impl.IOHiccup;
import org.iohiccup.socket.api.IOHic;
//import org.iohiccup.socket.api.attachable.TransformerAttachable;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.net.SocketImpl;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class IOHiccupAttachable extends IOHiccup {

    public static ConcurrentHashMap<SocketImpl, IOHic> attachableHics = new ConcurrentHashMap<SocketImpl, IOHic>();
            
    /* 
    @Override
    public void retransformStreams(Instrumentation instrumentation) {
        try {
            Class[] allLoadedClasses = instrumentation.getAllLoadedClasses();
            ArrayList<Class> classesToTransform  = new ArrayList<Class>();
            for (Class c : allLoadedClasses) {
                if (c.getSimpleName().equals("SocketInputStream") || c.getSimpleName().equals("SocketOutputStream")) {
                    classesToTransform.add(c);
                    instrumentation.retransformClasses(c);
                }
            }
        } catch (UnmodifiableClassException ex) {
            ex.printStackTrace();
        }
    }
    */

    public void instrument(String agentArgument, Instrumentation instrumentation) {
    /*
        TransformerAttachable ioHiccupTransformer = new TransformerAttachable(this);
        ioHiccupTransformer.attachTo(instrumentation);
            */
    }
        
    public static IOHiccup premain0(String agentArgument, Instrumentation instrumentation) {
        
        //Check here another instances and exit if then!
        if (initialized) {
            System.out.println("WARNING: multiple instances of ioHiccup was ran. (It's not well tested yet)");
            //System.err.println("\nTrying to run multiple instances of ioHiccup simultaneously.\n"
            //        + "\nPlease run only one at the same time.\n\n");
            //finishByError = true;
            //System.exit(1);
        }
        
//        IOHiccup ioHiccup = new IOHiccupAttachable();
//        ioHiccup.premain(agentArgument, instrumentation);
        
        initialized = true;
        
        return null;
    }
    
    
}


