/**
 * Written by Fedor Burdun of Azul Systems, and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Fedor Burdun
 */
package org.iohiccup;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.HistogramLogWriter;

public class LogWriter extends Thread {

    private final IOHiccup ioHiccup;
    
    public LogWriter(IOHiccup ioHiccup) {
        setDaemon(true);
        this.ioHiccup = ioHiccup;
    }

    @Override
    public void run() {
        HistogramLogWriter i2olog = null;
        HistogramLogWriter o2ilog = null;
        try {
            i2olog = new HistogramLogWriter(new File(ioHiccup.configuration.logPrefix + ".i2o.hlog"));
            o2ilog = new HistogramLogWriter(new File(ioHiccup.configuration.logPrefix + ".o2i.hlog"));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(IOHiccup.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        try {
            i2olog.outputLegend();
            o2ilog.outputLegend();
            i2olog.outputStartTime(ioHiccup.startTime);
            o2ilog.outputStartTime(ioHiccup.startTime);
            
            while ((System.currentTimeMillis() - ioHiccup.startTime < ioHiccup.configuration.startDelaying)) {
                ioHiccup.i2oLS.forceIntervalSample();
                Histogram intervalHistogram = ioHiccup.i2oLS.getIntervalHistogram();
                ioHiccup.o2iLS.forceIntervalSample();
                Histogram intervalHistogram2 = ioHiccup.o2iLS.getIntervalHistogram();
                
                Thread.sleep(ioHiccup.configuration.logWriterInterval);
            }
            
            while ((System.currentTimeMillis() - ioHiccup.startTime < ioHiccup.configuration.workingTime) && ioHiccup.isAlive && !Thread.interrupted()) {
                
                ioHiccup.i2oLS.forceIntervalSample();
                Histogram intervalHistogram = ioHiccup.i2oLS.getIntervalHistogram();
                intervalHistogram.setStartTimeStamp(intervalHistogram.getStartTimeStamp() - ioHiccup.startTime);
                intervalHistogram.setEndTimeStamp(intervalHistogram.getEndTimeStamp() - ioHiccup.startTime);
                i2olog.outputIntervalHistogram(intervalHistogram);
                
                ioHiccup.o2iLS.forceIntervalSample();
                Histogram intervalHistogram2 = ioHiccup.o2iLS.getIntervalHistogram();
                intervalHistogram2.setStartTimeStamp(intervalHistogram2.getStartTimeStamp() - ioHiccup.startTime);
                intervalHistogram2.setEndTimeStamp(intervalHistogram2.getEndTimeStamp() - ioHiccup.startTime);
                o2ilog.outputIntervalHistogram(intervalHistogram2);
                
                
                Thread.sleep(ioHiccup.configuration.logWriterInterval);
                
            }
        } catch (InterruptedException ex) {
            //Nothing to do?
        } finally {
            //Nothing to do?
            //Need to flush logs?
            ioHiccup.i2oLS.stop();
            ioHiccup.o2iLS.stop();
        }
    }
    
}
