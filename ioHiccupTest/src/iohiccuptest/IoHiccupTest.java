/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package iohiccuptest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fijiol
 */
class TestI {

    public final int count;
    public final long delay;

    public TestI(int count, long delay) {
        this.count = count;
        this.delay = delay;
    }

}

public class IoHiccupTest {

    private static final ArrayList<TestI> tests = new ArrayList<>();
    private static long finishTime;

    private static String getUrlSource(String url) throws IOException {
        URL yahoo = new URL(url);
        URLConnection yc = yahoo.openConnection();
        StringBuilder a;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                yc.getInputStream(), "UTF-8"))) {
            String inputLine;
            a = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                a.append(inputLine);
            }
        }
        return a.toString();
    }

    static String[] sites = {"http://ya.ru", "http://vk.com", "http://www.yahoo.com", "http://tut.by", "http://twitter.com"};

    static int workersQuantity = 0;
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        boolean printHelp = false;
        for (String param : args) {
            String[] paramVals = param.split(":");
            if (paramVals.length == 3 && paramVals[0].equals("-i")) {
                tests.add(new TestI(Integer.valueOf(paramVals[1]), Long.valueOf(paramVals[2])));
            } else 
            if (paramVals.length == 2 && paramVals[0].equals("-t")) {
                finishTime = System.currentTimeMillis() + Long.valueOf(paramVals[1]);
            } else {
                printHelp = true;
            }
        }
        if (printHelp) {
            System.out.println("Usage ioHiccupTest -t:<ms> -i:<count>:<delays> -i... -i...");
            System.exit(1);
        }

        for (TestI t : tests) {
            workersQuantity += t.count;
        }

        final CountDownLatch latch = new CountDownLatch(workersQuantity);
        for (TestI t : tests) {
            for (int i = 0; i < t.count; ++i) {
                doSite(latch, getRandomUrl(), t.delay);
            }
        }

        try {
            latch.await(100, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Logger.getLogger(IoHiccupTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            exec.shutdownNow();
        }
    }

    static Random rnd = new Random();

    public static String getRandomUrl() {
        return sites[Math.abs(rnd.nextInt()) % sites.length];
    }

    private static final ExecutorService exec = Executors.newCachedThreadPool();

    static int threadId = 0;
    
    public static void doSite(final CountDownLatch latch, final String url, final long delay) {
        
        exec.submit(new Runnable() {
            
            @Override
            public void run() {
                final int tid =  ++threadId;
                try {
                    int i =0;
                    while (System.currentTimeMillis() < finishTime) {
                        try {
                            final int length = getUrlSource(url).length();
                            
                            System.out.println(" Thread " + tid + " of " + workersQuantity + ": [ iteration " + (i + 1) 
                                    + " ]: getting context of site... " + url + " has been got " + length + " bytes");
                            
                            Thread.sleep(delay);
                        } catch (InterruptedException ex) {
                            return;
                        } catch (IOException ex) {
                            System.err.println("(" + url + ") ER: " + ex);
                            Logger.getLogger(IoHiccupTest.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        ++i;
                    }
                } finally {
                    latch.countDown();
                }
            }
        });
    }

}
