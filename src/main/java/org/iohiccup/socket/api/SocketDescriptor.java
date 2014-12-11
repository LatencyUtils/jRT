/*
 * Written by Fedor Burdun of Azul Systems, and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 * 

 */
package org.iohiccup.socket.api;

import java.util.Objects;

/**
 *
 * @author fijiol
 */
public class SocketDescriptor {
    public String host;
    public int port;

    @Override
    public boolean equals(Object obj) {
        return obj != null && 
                obj instanceof SocketDescriptor &&
                Objects.equals(host, ((SocketDescriptor)obj).host) &&
                Objects.equals(port, ((SocketDescriptor)obj).port);
    }

    @Override
    public int hashCode() {
        return (Objects.hashCode(host) >>> 3)  ^ Objects.hashCode(port);
    }

}
