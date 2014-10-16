/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iohiccup;

import java.net.SocketImpl;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 *
 * @author fijiol
 */

/*
 * An naive implementation.
 *
 * Implementation of latency counter in sampling manner (save only current one)
 * Consumer thread read only last one, and all latency values between reading 
 * will lost.
 */
public class IOHiccupAccumulator {
    /*
     * Should be a zero in case last put timestamp was when write?
     */
    static Map<IOHiccupSocketPresentation, IOHicHolder> i2o = new HashMap<IOHiccupSocketPresentation, IOHicHolder>();
    
    /*
    // place holder to call from SocketImpl.constructor
    public static void initializeIOHicHolder(SocketImpl sock) {
        
    }
    */
    
    public static void initializeIOHicHolderIfNeed(IOHiccupSocketPresentation sockImpl) {
        if (!Collections.synchronizedMap(i2o).containsKey(sockImpl)) {
            long time = System.nanoTime();
            IOHicHolder preHic = new IOHicHolder();
            preHic.hic = preHic.new IOHic();
            preHic.hic.endTime = time;
            preHic.hic.latency = 0;
            preHic.lastLatencyStartTimestamp = time;
            Collections.synchronizedMap(i2o).put(sockImpl, preHic);
        }
    }
    
    public static void putTimestampReadAfter(String addr, int port) {
        IOHiccupSocketPresentation sockImpl = new IOHiccupSocketPresentation(addr, port);
        initializeIOHicHolderIfNeed(sockImpl);
        Collections.synchronizedMap(i2o).get(sockImpl).lastLatencyStartTimestamp = System.nanoTime();
    }
    
    public static void putTimestampWriteBefore(String addr, int port) {
        IOHiccupSocketPresentation sockImpl = new IOHiccupSocketPresentation(addr, port);
        initializeIOHicHolderIfNeed(sockImpl);
        long time = System.nanoTime();
        Collections.synchronizedMap(i2o).get(sockImpl).hic.latency = time - Collections.synchronizedMap(i2o).get(sockImpl).lastLatencyStartTimestamp;
        Collections.synchronizedMap(i2o).get(sockImpl).hic.endTime = time;
    }
    
    public static void putTimestampWriteAfter(String addr, int port) {
        System.out.println("** READ o2i case: <isn't implemented yet>");
        System.out.println("   you can remove -o2i option for now. It will be coming soon :)");
    }
    
    public static void putTimestampReadBefore(String addr, int port) {
        System.out.println("** WRITE o2i case: <isn't implemented yet>");
        System.out.println("   you can remove -o2i option for now. It will be coming soon :)");
    }
    
    public static Map<IOHiccupSocketPresentation, IOHicHolder> getI2O() {
        return i2o;
    }
    
    public static String dumpIOHiccups() {
        StringBuilder sb = new StringBuilder();
        for (IOHiccupSocketPresentation sock : Collections.synchronizedMap(i2o).keySet()) {
            sb.append("\t" + sock);
            sb.append(" ");
            IOHicHolder val = Collections.synchronizedMap(i2o).get(sock);
            sb.append(val.hic);
            sb.append("\n");
        }
        return sb.toString();
    }
    
}
