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
public class IOHiccupHistogramWriter extends Thread {

    Boolean isAlive = true;
    Boolean isFinished = false;
    
    @Override
    public void run() {
        isFinished = false;
        while (isAlive) {
            System.out.println("hi, there");
            System.out.println(IOHiccupAccumulator.dumpIOHiccups());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                isAlive = false;
            }
        }
        isFinished = true;
    }
    
}
