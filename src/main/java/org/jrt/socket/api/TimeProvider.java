/**
 * Written by Fedor Burdun of Azul Systems, and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Fedor Burdun
 */
package org.jrt.socket.api;

public class TimeProvider {
    public static long getCurrentTime() {
        return System.nanoTime();
    }
}
