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
import org.HdrHistogram.AtomicHistogram;

/**
 *
 * @author fijiol
 */

/*
 * 
 */
public class IOHiccupAccumulator {

    static WeakHashMap<SocketImpl, IOHic> sockHiccups = new WeakHashMap<SocketImpl, IOHic>();
    
    private static IOHic getSockHic(SocketImpl sock) {
        IOHic hic = Collections.synchronizedMap(sockHiccups).get(sock);
        if (null == hic) {
            hic = new IOHic();
            Collections.synchronizedMap(sockHiccups).put(sock, hic);
        }
        return hic;
    }
    
    public static void putTimestampReadAfter(SocketImpl sock) {
        IOHic hic = getSockHic(sock);
        hic.readTime = System.nanoTime();
        hic.lastRead = true;
    }
    
    public static void putTimestampWriteBefore(SocketImpl sock) {
        IOHic hic = getSockHic(sock);
        hic.writeTime = System.nanoTime();
        if (hic.lastRead && (hic.latency = hic.writeTime - hic.readTime) > 0) {
//            i2oHistogram.recordValue(hic.latency);
            IOHiccup.ls.recordLatency(hic.latency);
        }
        hic.lastRead = false;
    }
    
    public static void putTimestampWriteAfter(SocketImpl sock) {
        System.out.println("** READ o2i case: <isn't implemented yet>");
        System.out.println("   you can remove -o2i option for now. It will be coming soon :)");
    }
    
    public static void putTimestampReadBefore(SocketImpl sock) {
        System.out.println("** WRITE o2i case: <isn't implemented yet>");
        System.out.println("   you can remove -o2i option for now. It will be coming soon :)");
    }
    
    public static String dumpIOHiccups() {
        StringBuilder sb = new StringBuilder();

        return sb.toString();
    }
    
}
