/*
 * Copyright (c) 2012 Alexander Diener,
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.fhkn.in.uce.stun.attribute.rfc3489;

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

import de.fhkn.in.uce.stun.attribute.Attribute;
import de.fhkn.in.uce.stun.attribute.AttributeHeader;
import de.fhkn.in.uce.stun.attribute.AttributeType;
import de.fhkn.in.uce.stun.util.MessageFormatException;

/**
 * The CHANGED-ADDRESS attribute indicates the IP address and port where
 * responses would have been sent from if the "change IP" and "change port"
 * flags had been set in the CHANGE-REQUEST attribute of the Binding Request.
 * The attribute is always present in a Binding Response, independent of the
 * value of the flags. Its syntax is identical to MAPPED-ADDRESS.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class ChangedAddress implements Attribute {
    private static final int IPV4_FAMILY = 0x01;
    private static final int IPV6_FAMILY = 0x02;
    private static final int IPV4_MESSAGE_LENGTH = 8;
    private static final int IPV6_MESSAGE_LENGTH = 20;

    private final InetSocketAddress endpoint;
    private final int length;
    private final int ipFamily;

    /**
     * Creates a {@link ChangedAddress} attribute for an endpoint.
     * 
     * @param endpoint
     *            the endpoint for the attribute
     */
    public ChangedAddress(final InetSocketAddress endpoint) {
        this.length = this.determineLengthOfAttribute(endpoint);
        this.endpoint = endpoint;
        this.ipFamily = this.determineIpFamily(endpoint);
    }

    private int determineLengthOfAttribute(final InetSocketAddress endpoint) {
        int result = 0;
        if (endpoint.getAddress() instanceof Inet4Address) {
            result = IPV4_MESSAGE_LENGTH;
        } else if (endpoint.getAddress() instanceof Inet6Address) {
            result = IPV6_MESSAGE_LENGTH;
        } else {
            throw new IllegalArgumentException("Unknown address familiy " + endpoint.getAddress().getClass()); //$NON-NLS-1$
        }

        return result;
    }

    private int determineIpFamily(final InetSocketAddress endpoint) {
        int result;
        if (endpoint.getAddress() instanceof Inet4Address) {
            result = IPV4_FAMILY;
        } else if (endpoint.getAddress() instanceof Inet6Address) {
            result = IPV6_FAMILY;
        } else {
            throw new IllegalArgumentException("Unknown address familiy " + endpoint.getAddress().getClass()); //$NON-NLS-1$
        }

        return result;
    }

    /**
     * Returns the endpoint.
     * 
     * @return the endpoint
     */
    public InetSocketAddress getEndpoint() {
        return this.endpoint;
    }

    @Override
    public AttributeType getType() {
        return Rfc3489AttributeType.CHANGED_ADDRESS;
    }

    @Override
    public int getLength() {
        return this.length;
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final DataOutputStream dout = new DataOutputStream(bout);

        // leading zeros
        dout.writeByte(0x00);
        dout.writeByte(this.ipFamily);
        dout.writeShort(this.endpoint.getPort());
        dout.write(this.endpoint.getAddress().getAddress());

        out.write(bout.toByteArray());
        out.flush();
    }

    /**
     * Creates a {@link ChangedAddress} from the given encoded attribute and
     * header.
     * 
     * @param encoded
     *            the encoded {@link ChangedAddress} attribute
     * @param header
     *            the attribute header
     * @return the {@link ChangedAddress} of the given encoding
     * @throws IOException
     *             if an I/O exception occurs
     * @throws MessageFormatException
     *             if the encoded {@link ChangedAddress} is malformed
     */
    public static ChangedAddress fromBytes(final byte[] encoded, final AttributeHeader header) throws IOException,
            MessageFormatException {
        final ByteArrayInputStream bin = new ByteArrayInputStream(encoded);
        final DataInputStream din = new DataInputStream(bin);
        final int leadingZeroBits = din.readUnsignedByte();
        checkLeadingZeros(leadingZeroBits);
        final int ipFamilyBits = din.readUnsignedByte();
        final byte[] ipAsBytes = getByteArrayForIp(ipFamilyBits);
        final int port = din.readUnsignedShort();
        din.readFully(ipAsBytes);
        final InetAddress address = InetAddress.getByAddress(ipAsBytes);

        return new ChangedAddress(new InetSocketAddress(address, port));
    }

    private static void checkLeadingZeros(final int leadingZeroBits) throws MessageFormatException {
        if (leadingZeroBits != 0) {
            throw new MessageFormatException("Wrong message format, the leading zeros were " + leadingZeroBits); //$NON-NLS-1$
        }
    }

    private static byte[] getByteArrayForIp(final int ipFamilyBits) throws MessageFormatException {
        byte[] bytesForIp;
        if (ipFamilyBits == IPV4_FAMILY) {
            bytesForIp = new byte[4];
        } else if (ipFamilyBits == IPV6_FAMILY) {
            bytesForIp = new byte[16];
        } else {
            throw new MessageFormatException("Unknown address family " + ipFamilyBits); //$NON-NLS-1$
        }
        return bytesForIp;
    }
}
