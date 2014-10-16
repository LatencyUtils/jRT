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
public class IOHiccupSocketPresentation {
    
    public final long port;
    public final String addr;
    
    IOHiccupSocketPresentation(String addr, int port) {
        this.port = port;
        this.addr = addr;
    }

    @Override
    public boolean equals(Object obj) {
        return (null != obj) && (obj instanceof IOHiccupSocketPresentation) && 
                (port == ((IOHiccupSocketPresentation)obj).port) &&
                (addr.equals(((IOHiccupSocketPresentation)obj).addr));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 31 * hash + (int) (this.port ^ (this.port >>> 32));
        hash = 31 * hash + (this.addr != null ? this.addr.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return addr + ":" + port;
    }
    
}
