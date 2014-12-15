/**
 * Written by Fedor Burdun of Azul Systems, and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Fedor Burdun
 */
package org.iohiccup.socket.regular;

import org.iohiccup.impl.Configuration;
import org.iohiccup.impl.IOHiccup;
import org.iohiccup.socket.api.IOHic;
import java.net.InetAddress;

public class Accumulator {

    private static String this_package = "org.iohiccup.socket.regular";
    private static String this_class = this_package + ".Accumulator";
    
    private static boolean match(String a, String filter) {
        //return a.matches(".*" + filter + ".*");
        if (null == filter) {
            return false;
        }
        return a.equals(filter);
    }
    
    private static boolean match(IOHiccup ioHiccup, InetAddress remoteAddress, int remotePort, int localPort) {
        
        for (Configuration.IOFilterEntry entry : ioHiccup.configuration.filterEntries) {
            if (null != entry.remoteaddr  &&
                    !match(remoteAddress.getHostAddress(), entry.remoteaddr) &&
                    !match(remoteAddress.getHostName(), entry.remoteaddr)  ) {
                return false;
            }
            if (null != entry.remoteport && 
                    !match(String.valueOf(remotePort), entry.remoteport)) {
                return false;
            }
            if (null != entry.localport && 
                    !match(String.valueOf(localPort), entry.localport)) {
                return false;
            }
        }
        
        //System.out.println("Calculate hiccups between " + remoteAddress + ":" + remotePort + " <-> " + "127.0.0.1:" + localPort); //Print on debug level?
        
        return true;
    }    
    
    public static IOHiccup getIOHiccup(String uuid) {
        return IOHiccup.ioHiccupWorkers.get(uuid);
    }
    
    public static String _filter(String ioHiccup, String sock, String remoteInetAddress, String remotePort, String localPort) {
        return this_class + "initializeIOHic(" + ioHiccup + ", " + sock + ", " + remoteInetAddress + ", " + remotePort + ", " + localPort + ");";
    }
    
    public static IOHic initializeIOHic(IOHiccup ioHiccup, Object sock, InetAddress remoteAddress, int remotePort, int localPort) {
        //System.out.println("initializeIOHic " + sock + "," + remoteAddress + "," + remotePort + "," + localPort);;
        
        IOHic iohic = null;
        
        if (ioHiccup == null) {
            System.err.println("ioHiccup non initialized!");
            return null;
        }
        
        if (ioHiccup.sockHiccups.containsKey(sock)) {
            return ioHiccup.sockHiccups.get(sock);
        } else {
            iohic = new IOHic();
            ioHiccup.sockHiccups.put(sock, iohic);
        }
        
        //Decide to filter or not?
        if (!match(ioHiccup, remoteAddress, remotePort, localPort)) { 
            //sockHiccups.put(sock, null); //??!
            ioHiccup.sockHiccups.remove(sock);
            
            return null;
        }
        
        ++ioHiccup.ioStat.processedSocket;
        
        return iohic;
    }

    
    
    

    private static String _timestampStub(String methodName, String ioHiccup, String ioHic) {
        return this_class + "." + methodName + "(" + "(org.iohiccup.impl.IOHiccup)" + ioHiccup + ", " +  "(org.iohiccup.socket.api.IOHic)" +  ioHic + ");";
    }
    
    public static String _readAfter(String ioHiccup, String ioHic) {
        return _timestampStub("putTimestampReadAfter", ioHiccup, ioHic);
    }
    
    public static String _readBefore(String ioHiccup, String ioHic) {
        return _timestampStub("putTimestampReadBefore", ioHiccup, ioHic);
    }
    
    public static String _writeAfter(String ioHiccup, String ioHic) {
        return _timestampStub("putTimestampWriteAfter", ioHiccup, ioHic);
    }
    
    public static String _writeBefore(String ioHiccup, String ioHic) {
        return _timestampStub("putTimestampWriteBefore", ioHiccup, ioHic);
    }
    
    
    
    
    public static void putTimestampReadAfter(IOHiccup ioHiccup, IOHic hic) {
        System.out.println("here: " + ioHiccup + ", " + hic);
        if (null == ioHiccup || hic == null) {
            return;
        }
        
        hic.i2oReadTime = System.nanoTime();
        hic.i2oLastRead = true;
    }
    
    public static void putTimestampWriteBefore(IOHiccup ioHiccup, IOHic hic) {
        if (null == ioHiccup || hic == null) {
            return;
        }
        
        hic.i2oWriteTime = System.nanoTime();
        if (hic.i2oLastRead && (hic.i2oLatency = hic.i2oWriteTime - hic.i2oReadTime) > 0) {
            ioHiccup.i2oLS.recordLatency(hic.i2oLatency);
        }
        hic.i2oLastRead = false;
    }
    
    public static void putTimestampWriteAfter(IOHiccup ioHiccup, IOHic hic) {
        if (null == ioHiccup || hic == null) {
            return;
        }
        
        hic.o2iReadTime = System.nanoTime();
        hic.o2iLastWrite = true;
    }
    
    public static void putTimestampReadBefore(IOHiccup ioHiccup, IOHic hic) {
        if (null == ioHiccup || hic == null) {
            return;
        }
        
        hic.o2iWriteTime = System.nanoTime();
        if (hic.o2iLastWrite && (hic.o2iLatency = hic.o2iWriteTime - hic.o2iReadTime) > 0) {
            ioHiccup.o2iLS.recordLatency(hic.o2iLatency);
        }
        hic.o2iLastWrite = false;
    }
    
}
