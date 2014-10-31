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
public class IOHiccupConfiguration {
    public boolean i2oEnabled   = true;
    public boolean o2iEnabled   = true;
    public String matchPort     = null; // null means do not filter them
    public String matchAddress  = null; // null means do not filter them
    public long logWriterInterval = 1000;
}
