/**
 * Written by Fedor Burdun of Azul Systems, and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Fedor Burdun
 */
package org.iohiccup.socket.mockup;

public class Socket {
    private int fd;
    private String host;
    private int port;
            
    public void Socket(int fd, String host, int port) {
        this.fd = fd;
        this.host = host;
        this.port = port;
    }
    
    public void open() {
    }
    
    public void close() {
    }
    
}
