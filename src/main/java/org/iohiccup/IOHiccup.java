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

/**
 *
 * @author fijiol
 */


public class IOHiccup {
    
    public static IOHiccupConfiguration configuration;

    public static void main(String[] args) throws UnsupportedEncodingException, IOException, InterruptedException {
        System.out.println("ioHiccup.jar doesn't have now functional main method. Please rerun your application as:\n\t"+
                "java -javaagent:ioHiccup.jar -jar yourapp.jar");
    }

    public static void premain(String agentArgument, Instrumentation instrumentation) {
        configuration = new IOHiccupConfiguration();
        
        if (null != agentArgument) {
            for (String v : agentArgument.split(",")) {
                String[] vArr = v.split("=");
                if (vArr.length > 2) {
                    System.out.println("Wrong format ioHiccup arguments. Please use next 'arg1,arg2=val2,...'");
                    System.exit(1);
                }
                if (Arrays.asList(new String[]{"-h","--help","help","h"}).contains(vArr[0])) {
                    System.out.println("todo: print here options :P, now only default values...");
                }
//                if (something) {
//                    configuration.something = something
//                }
            }
        }
        
        instrumentation.addTransformer(new IOHiccupTransformer(configuration));

        final IOHiccupHistogramWriter writer = new IOHiccupHistogramWriter();
        writer.start();
        
        //Some temporary place to print collected statistic.
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                writer.isAlive = false;
                System.out.println(" \\n");
                System.out.println("***************************************************************");
                System.out.println("ioHiccupStatistic: ");
                System.out.println("**************************************************************");
//                System.out.println(ioHiccupDatas.toString());
            }
            
        });
    }

}
