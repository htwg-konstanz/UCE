/**
 * Copyright (C) 2011 Daniel Maier
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.fhkn.in.uce.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link UceAttribute} to represent a socket endpoint (IP address and port).
 * 
 * @author Daniel Maier
 * 
 */
public final class SocketEndpoint implements UceAttribute {

    private final InetSocketAddress endpoint;
    private final EndpointClass endpointClass;
    private final int length;

    /**
     * Creates a new {@link SocketEndpoint}.
     * 
     * @param endpoint
     *            the socket endpoint
     * @param endpointClass
     *            the type of endpoint
     */
    public SocketEndpoint(InetSocketAddress endpoint, EndpointClass endpointClass) {
        if (endpoint.getAddress() instanceof Inet4Address) {
            length = 8;
        } else if (endpoint.getAddress() instanceof Inet6Address) {
            length = 20;
        } else {
            throw new IllegalArgumentException("Unknown address familiy "
                    + endpoint.getAddress().getClass());
        }
        this.endpoint = endpoint;
        this.endpointClass = endpointClass;
    }

    /**
     * Returns the endpoint.
     * 
     * @return the endpoint
     */
    public InetSocketAddress getEndpoint() {
        return endpoint;
    }

    /**
     * Returns the type of the endpoint.
     * 
     * @return the type of the endpoint
     */
    public EndpointClass getEndpointClass() {
        return endpointClass;
    }

    public UceAttributeType getType() {
        return CommonUceAttributeType.SOCKET_ENDPOINT;
    }

    public int getLength() {
        return length;
    }

    /**
     * Decodes a {@link SocketEndpoint} from a byte array.
     * 
     * @param encoded
     *            the encoded {@link SocketEndpoint} as a abyte array
     * @param header
     *            the header of this attribute
     * @return the decoded {@link SocketEndpoint}
     * @throws IOException
     *             if an I/O error occurs
     * @throws MessageFormatException
     *             if the encoded {@link SocketEndpoint} is malformed
     */
    static SocketEndpoint fromBytes(byte[] encoded, UceAttributeHeader header) throws IOException,
            MessageFormatException {
        ByteArrayInputStream bin = new ByteArrayInputStream(encoded);
        DataInputStream din = new DataInputStream(bin);
        int endpointClassBits = din.readUnsignedByte();
        // endpoint class
        EndpointClass endpointClass = EndpointClass.fromEncoded(endpointClassBits);
        if (endpointClass == null) {
            throw new MessageFormatException("Unknown endpoint class " + endpointClassBits);
        }
        int familyBits = din.readUnsignedByte();
        // ip address
        byte[] ipBytes;
        if (familyBits == 4) {
            ipBytes = new byte[4];
        } else if (familyBits == 6) {
            ipBytes = new byte[16];
        } else {
            throw new MessageFormatException("Unknown address family " + familyBits);
        }
        // port
        int port = din.readUnsignedShort();
        // ip continue
        din.readFully(ipBytes);
        InetAddress addr = InetAddress.getByAddress(ipBytes);
        return new SocketEndpoint(new InetSocketAddress(addr, port), endpointClass);
    }

    public void writeTo(OutputStream out) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);
        dout.writeByte(endpointClass.encoded);
        if (endpoint.getAddress() instanceof Inet4Address) {
            dout.writeByte(4);
        } else if (endpoint.getAddress() instanceof Inet6Address) {
            dout.writeByte(6);
        } else {
            throw new AssertionError("Unknown address familiy " + endpoint.getAddress().getClass());
        }
        dout.writeShort(endpoint.getPort());
        dout.write(endpoint.getAddress().getAddress());

        out.write(bout.toByteArray());
        out.flush();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((endpoint == null) ? 0 : endpoint.hashCode());
        result = prime * result + ((endpointClass == null) ? 0 : endpointClass.hashCode());
        result = prime * result + length;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SocketEndpoint)) {
            return false;
        }
        SocketEndpoint other = (SocketEndpoint) obj;
        if (endpoint == null) {
            if (other.endpoint != null) {
                return false;
            }
        } else if (!endpoint.equals(other.endpoint)) {
            return false;
        }
        if (endpointClass != other.endpointClass) {
            return false;
        }
        if (length != other.length) {
            return false;
        }
        return true;
    }

    /**
     * Enum to represent different types of {@link SocketEndpoint}.
     * 
     * @author Daniel Maier
     * 
     */
    public static enum EndpointClass {
        /**
         * Type of endpoint is undefined.
         */
        UNDEFINED(0x0),
        /**
         * Endpoint is a private endpoint (behind NAT)
         */
        PRIVATE(0x1),
        /**
         * Endpoint is a public endpoint (visible from outside)
         */
        PUBLIC(0x2),
        /**
         * Endpoint is an endpoint on a server for relaying data between client
         * and peer.
         */
        RELAY(0x3),
        /**
         * Endpoint is an endpoint for connection reversal
         */
        CONNECTION_REVERSAL(0x4);

        private static final Map<Integer, EndpointClass> intToEnum = new HashMap<Integer, EndpointClass>();

        static {
            for (EndpointClass l : values()) {
                intToEnum.put(l.encoded, l);
            }
        }

        private final int encoded;

        /**
         * Creates a new {@link EndpointClass}.
         * 
         * @param encoded
         *            the encoded representation of the endpoint.
         */
        private EndpointClass(int encoded) {
            this.encoded = encoded;
        }

        /**
         * Returns the byte encoded {@link EndpointClass}.
         * 
         * @return
         */
        int encode() {
            return encoded;
        }

        /**
         * Decodes the {@link EndpointClass}.
         * 
         * @param encoded
         *            the byte encoded {@link EndpointClass}
         * @return the decoded {@link EndpointClass}
         */
        private static EndpointClass fromEncoded(int encoded) {
            return intToEnum.get(encoded);
        }
    }
}
