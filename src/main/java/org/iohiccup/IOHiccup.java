/**
 * Written by Fedor Burdun of Azul Systems, and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Fedor Burdun
 */
package org.iohiccup;

import java.lang.instrument.Instrumentation;
import java.net.SocketImpl;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import org.LatencyUtils.LatencyStats;

public class IOHiccup {

    public static volatile boolean initialized = false;
    public static volatile boolean finishByError = false;
    public static long hiccupInstances = 0;
    private static final String title = "";
    
    public long startTime;
    public LatencyStats i2oLS;
    public LatencyStats o2iLS;
    public boolean isAlive = true;

    public IOHiccupConfiguration configuration;
    public IOStatistic ioStat;
    
    public Map<SocketImpl, IOHic> sockHiccups = new ConcurrentHashMap(new WeakHashMap<SocketImpl, IOHic>());
    
    public static void main(String[] args) {
        System.out.println("ioHiccup.jar doesn't have now functional main method. Please rerun your application as:\n\t"
                + "java -javaagent:ioHiccup.jar -jar yourapp.jar");
        System.exit(1);
    }

    private static String printKeys(String[] keys) {
        StringBuilder sb = new StringBuilder();
        for (String s : keys) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(s);
        }
        return sb.toString();
    }
    
    public static void printHelpAndExit() {
        System.out.println("Usage:");
        System.out.println("\tjava -jar ioHiccup.jar[=<args>]  -jar yourapp.jar\n");
        System.out.println("\t\twhere <args> is an comma separated list of arguments like arg1,arg2=val2 e.t.c\n");
        System.out.println("\t\tARGUMENTS:");
        System.out.println("\t\t  " + printKeys(help) + " \t\t to print help");
        System.out.println("\t\t  " + printKeys(remoteaddr) + " \t\t to add filter by remote address");
        System.out.println("\t\t  " + printKeys(remoteport) + " \t\t to add filter by remote port");
        System.out.println("\t\t  " + printKeys(localport) + " \t\t to add filter by local port");
        System.out.println("\t\t  " + printKeys(filterentry) + " \t\t to add filter by entry: <Local port>::<Remote address>:<Remote port> any part can be empty");
        System.out.println("\t\t  " + printKeys(loginterval) + " \t\t to set log sampling interval");
        System.out.println("\t\t  " + printKeys(startdelaying) + " \t\t to specify time delay to start ioHiccup");
        System.out.println("\t\t  " + printKeys(workingtime) + " \t\t to specify how long ioHiccup will work");
        System.out.println("\t\t  " + printKeys(logprefix) + " \t\t to specify ioHiccup log prefix");
        System.out.println("\t\t  " + printKeys(uuid) + " \t\t to specify ioHiccup inner ID (take <string>)");
        System.out.println("\t\t  " + printKeys(i2oenabling) + " \t\t to calculate latency (take <boolean>)");
        System.out.println("\t\t  " + printKeys(o2ienabling) + " \t\t to calculate latency (take <boolean>)");
                
        System.out.println("\n");
        System.out.println("Please rerun application with proper CLI options.\n");
        
        finishByError = true;
        System.exit(1);
    }
    
    private static String fixupRegex(String str) {
        if (true) {
            return str;
        }       
        try {
        "".matches(str);
        } catch (Exception e) {
            System.err.println("WARN: regex '" + str + "' is not understandable");
            System.exit(1); //??
            return null;
        }
        return str;
    }
    
    public static ConcurrentHashMap<String, IOHiccup> ioHiccupWorkers = new ConcurrentHashMap<String, IOHiccup>();
    
    public void premain(String agentArgument, Instrumentation instrumentation) {
        
        configuration = new IOHiccupConfiguration();
        ioStat = new IOStatistic();

        startTime = System.currentTimeMillis();

        if (null != agentArgument) {
            for (String v : agentArgument.split(",")) {
                String[] vArr = v.split("=");
                if (vArr.length > 2) {
                    System.out.println("Wrong format ioHiccup arguments.\n");
                    printHelpAndExit();
                }
                if (hasKey(help, vArr[0])) {
                    printHelpAndExit();
                }
                if (hasKey(remoteaddr, vArr[0])) {
                    configuration.filterEntries.add(
                            new IOHiccupConfiguration.IOFilterEntry(null, vArr[1], null));
                }
                if (hasKey(localport, vArr[0])) {
                    configuration.filterEntries.add(
                            new IOHiccupConfiguration.IOFilterEntry(vArr[1], null, null));
                }
                if (hasKey(remoteport, vArr[0])) {
                    configuration.filterEntries.add(
                            new IOHiccupConfiguration.IOFilterEntry(null, null, vArr[1]));
                }
                if (hasKey(filterentry, vArr[0]) && vArr.length == 2) {
                    boolean isCorrect = true;
                    
                    String localPort = null;
                    String remoteAddr = null;
                    String remotePort = null;
                    
                    String[] ports = vArr[1].split("::");
                    
                    if (ports.length == 2) {
                        if (ports[0].length() > 0) {
                            localPort = ports[0];
                        }
                        String[] remote = ports[1].split(":");
                        
                        if (remote.length >= 1) {
                            if (remote[0].length() > 0) {
                                remoteAddr = remote[0];
                            } 
                        }
                        if (remote.length >= 2) {
                            if (remote[1].length() > 0) {
                                remotePort = remote[1];
                            }
                        } 
                        if (remote.length > 2){
                            isCorrect = false;
                        }
                    } else {
                        isCorrect = false;
                    }

                    //System.out.println("'" + localPort + "'" + remoteAddr + "'" + remotePort + "'");
                    
                    if (!isCorrect) {
                        System.err.println("Wrong " + printKeys(filterentry) + " format\n\n");
                        printHelpAndExit();
                    }
                    
                    configuration.filterEntries.add(
                            new IOHiccupConfiguration.IOFilterEntry(localPort, remoteAddr, remotePort));
                }
                if (hasKey(loginterval, vArr[0])) {
                    configuration.logWriterInterval = Long.valueOf(vArr[1]);
                }
                if (hasKey(startdelaying, vArr[0])) {
                    configuration.startDelaying = Long.valueOf(vArr[1]);
                }
                if (hasKey(workingtime, vArr[0])) {
                    configuration.workingTime = Long.valueOf(vArr[1]);
                }
                if (hasKey(logprefix, vArr[0])) {
                    configuration.logPrefix = (vArr[1]);
                }
                if (hasKey(uuid, vArr[0])) {
                    configuration.uuid = (vArr[1]);
                }
                if (hasKey(i2oenabling, vArr[0])) {
                    configuration.i2oEnabled = Boolean.valueOf(vArr[1]);
                }
                if (hasKey(o2ienabling, vArr[0])) {
                    configuration.o2iEnabled = Boolean.valueOf(vArr[1]);
                }
            }
        }
        
        ioHiccupWorkers.put(configuration.uuid, this);
        
        instrumentation.addTransformer(new IOHiccupTransformer(this));
                
        //Some temporary place to print collected statistic.
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                synchronized (IOHiccup.title) {
                    if (finishByError) {
                        return;
                    }
                    System.out.println("");
                    System.out.println("***************************************************************");
                    System.out.println("ioHiccup configuration: ");
                    System.out.println("ioHiccup uid " + configuration.uuid);
                    System.out.println("---------------------------------------------------------------");
                    System.out.println("ioHiccupStatistic: ");
                    System.out.println(" " + ioStat.processedSocket + " sockets was processed");
                    System.out.println("***************************************************************");
                    System.out.flush();
                }
            }

        });

        i2oLS = new LatencyStats();
        o2iLS = new LatencyStats();

        IOHiccupLogWriter ioHiccupLogWriter = new IOHiccupLogWriter(this);
        ioHiccupLogWriter.start();    
    }
    
    public static void premain0(String agentArgument, Instrumentation instrumentation) {
        
        //Check here another instances and exit if then!
        if (initialized) {
            System.out.println("WARNING: multiple instances of ioHiccup was ran. (It's not well tested yet)");
            //System.err.println("\nTrying to run multiple instances of ioHiccup simultaneously.\n"
            //        + "\nPlease run only one at the same time.\n\n");
            //finishByError = true;
            //System.exit(1);
        }
        
        IOHiccup ioHiccup = new IOHiccup();
        ioHiccup.premain(agentArgument, instrumentation);
        
        initialized = true;
    }
    
    private static final String[] remoteaddr = {"-raddr", "remote-addr"};
    private static final String[] loginterval = {"-si", "sample-interval"};
    private static final String[] remoteport = {"-rport", "remote-port"};
    private static final String[] localport = {"-lport", "local-port"};
    private static final String[] filterentry = {"-f", "filter-entry"};
    private static final String[] help = {"-h", "--help", "help", "h"};
    private static final String[] startdelaying = {"-start", "start"};
    private static final String[] workingtime = {"-fin", "finish-after"};
    private static final String[] logprefix = {"-lp", "log-prefix"};
    private static final String[] uuid = {"-id", "uuid"};
    private static final String[] i2oenabling = {"-i2o"};
    private static final String[] o2ienabling = {"-o2i"};

    private static boolean hasKey(String[] list, String key) {
        for (String s : list) {
            if (s.equals(key)) {
                return true;
            }
        }
        return false;
    }
}
