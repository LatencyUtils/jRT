/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iohiccup;

/**
 *
 * @author fijiol
 */
public class IOHicHolder {
    public class IOHic {
        public long latency;
        public long endTime;
        
        @Override
        public String toString() {
            return latency + " on " + endTime;
        }
    }
    
    public IOHic hic;
    public long lastLatencyStartTimestamp;

}
