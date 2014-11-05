/**
 * Written by Fedor Burdun of Azul Systems, and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Fedor Burdun
 */
package org.iohiccup;

import java.util.ArrayList;

public class IOHiccupConfiguration {
    
    public String uuid = String.valueOf(++IOHiccup.hiccupInstances);
    
    public boolean i2oEnabled   = true;
    public boolean o2iEnabled   = true;
    public long logWriterInterval = 1000;
    public String logPrefix = "hiccup." + uuid;
    public boolean printExceptions = true;

    public static class IOFilterEntry {
        public String remoteaddr = null;
        public String localport = null;
        public String remoteport = null;

        public IOFilterEntry(String localport, String remoteaddr, String remoteport) {
            this.localport = localport;
            this.remoteaddr = remoteaddr;
            this.remoteport = remoteport;
        }
        
    }
    
    public ArrayList<IOFilterEntry> filterEntries  = new ArrayList<IOFilterEntry>();
    
    public long startDelaying = 0;             //miliseconds
    public long workingTime = Long.MAX_VALUE;  //infinity
}
