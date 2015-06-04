/**
 * Written by Fedor Burdun of Azul Systems, and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Fedor Burdun
 */
package org.jrt.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.HistogramLogWriter;

public class LogWriter extends Thread {

    private final JRT jRT;
    
    public LogWriter(JRT jRT) {
        setDaemon(true);
        this.jRT = jRT;
    }

    @Override
    public void run() {
        HistogramLogWriter i2olog = null;
        HistogramLogWriter o2ilog = null;
        try {
            if (null != jRT.i2oLS) i2olog = new HistogramLogWriter(new File(jRT.configuration.logPrefix + ".i2o.hlog"));
            if (null != jRT.o2iLS) o2ilog = new HistogramLogWriter(new File(jRT.configuration.logPrefix + ".o2i.hlog"));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JRT.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        try {
            if (null != jRT.i2oLS) {
                i2olog.outputLegend();
                i2olog.outputStartTime(jRT.startTime);
            }
            if (null != jRT.o2iLS) {
                o2ilog.outputLegend();
                o2ilog.outputStartTime(jRT.startTime);
            }
            
            while ((System.currentTimeMillis() - jRT.startTime < jRT.configuration.startDelaying)) {
                if (null != jRT.i2oLS) { Histogram intervalHistogram = jRT.i2oLS.getIntervalHistogram(); }
                if (null != jRT.o2iLS) { Histogram intervalHistogram2 = jRT.o2iLS.getIntervalHistogram(); }
                
                Thread.sleep(jRT.configuration.logWriterInterval);
            }
            
            while ((System.currentTimeMillis() - jRT.startTime < jRT.configuration.workingTime) && jRT.isAlive && !Thread.interrupted()) {
                if (null != jRT.i2oLS) {
                    Histogram intervalHistogram = jRT.i2oLS.getIntervalHistogram();
                    intervalHistogram.setStartTimeStamp(intervalHistogram.getStartTimeStamp() - jRT.startTime);
                    intervalHistogram.setEndTimeStamp(intervalHistogram.getEndTimeStamp() - jRT.startTime);
                    i2olog.outputIntervalHistogram(intervalHistogram);
                }
                
                if (null != jRT.o2iLS) {
                    Histogram intervalHistogram2 = jRT.o2iLS.getIntervalHistogram();
                    intervalHistogram2.setStartTimeStamp(intervalHistogram2.getStartTimeStamp() - jRT.startTime);
                    intervalHistogram2.setEndTimeStamp(intervalHistogram2.getEndTimeStamp() - jRT.startTime);
                    o2ilog.outputIntervalHistogram(intervalHistogram2);
                }
                
                Thread.sleep(jRT.configuration.logWriterInterval);
                
            }
        } catch (InterruptedException ex) {
            //Nothing to do?
        } finally {
            //Nothing to do?
            //Need to flush logs?
            if (null != jRT.i2oLS) jRT.i2oLS.stop();
            if (null != jRT.o2iLS) jRT.o2iLS.stop();
        }
    }
    
}
