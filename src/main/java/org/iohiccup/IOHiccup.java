/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iohiccup;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import org.LatencyUtils.LatencyStats;

/**
 *
 * @author fijiol
 */
public class IOHiccup {

    public static long startTime;
    public static LatencyStats i2oLS;
    public static LatencyStats o2iLS;
    public static boolean isAlive = true;

    public static IOHiccupConfiguration configuration;
    public static IOStatistic ioStat;

    public static void main(String[] args) throws UnsupportedEncodingException, IOException, InterruptedException {
        System.out.println("ioHiccup.jar doesn't have now functional main method. Please rerun your application as:\n\t"
                + "java -javaagent:ioHiccup.jar -Xbootclasspath/a:ioHiccup.jar -jar yourapp.jar");
    }

    public static void printHelp() {
        System.out.println("Some help message here");
    }
    
    private static String fixupRegex(String str) {
        if (true) {
            return str;
        }
        
        try {
        "".matches(str);
        } catch (Exception e) {
            System.err.println("WARN: regex '" + str + "' is not understandable");
            System.exit(1); //tempory
            return null;
        }
        return str;
    }
    
    public static void premain(String agentArgument, Instrumentation instrumentation) {
        configuration = new IOHiccupConfiguration();
        ioStat = new IOStatistic();

        startTime = System.currentTimeMillis();

        if (null != agentArgument) {
            for (String v : agentArgument.split(",")) {
                String[] vArr = v.split("=");
                if (vArr.length > 2) {
                    System.out.println("Wrong format ioHiccup arguments. Please use next 'arg1,arg2=val2,...'");
                    System.exit(1);
                }
                if (Arrays.asList(new String[]{"-h", "--help", "help", "h"}).contains(vArr[0])) {
                    System.out.println("todo: print here options :P, now only default values...");
                }
                //TODO add port/address filtering
//                if (something) {
//                    configuration.something = something
//                }
                if (vArr[0].equals("remoteaddr")) {
                    configuration.remoteaddr = fixupRegex(vArr[1]);
                }
                if (vArr[0].equals("localport")) {
                    configuration.localport = fixupRegex(vArr[1]);
                }
                if (vArr[0].equals("remoteport")) {
                    configuration.remoteport = fixupRegex(vArr[1]);
                }
                if (vArr[0].equals("log-interval")) {
                    configuration.logWriterInterval = Long.valueOf(vArr[1]);
                }
                //delay start time
                //how long to work
            }
        }

        instrumentation.addTransformer(new IOHiccupTransformer(configuration));

        //Some temporary place to print collected statistic.
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                System.out.println(" \\n");
                System.out.println("***************************************************************");
                System.out.println("ioHiccupStatistic: ");
                System.out.println("***************************************************************");
                System.out.println(" " + IOHiccup.ioStat.wrappedSocket + " sockets was wrapped");
            }

        });

        i2oLS = new LatencyStats();
        o2iLS = new LatencyStats();

        IOHiccupLogWriter ioHiccupLogWriter = new IOHiccupLogWriter();
        ioHiccupLogWriter.start();
    }

}
