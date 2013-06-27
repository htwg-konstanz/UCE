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
package de.fhkn.in.uce.stun.attribute;

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
import java.nio.ByteBuffer;

import de.fhkn.in.uce.stun.header.MessageHeader;
import de.fhkn.in.uce.stun.util.MessageFormatException;

/**
 * Implementation of {@link Attribute} for the XOR-MAPPED-ADDRESS attribute
 * according to RFC 5389. The IPv4 endpoint is encoded with the magic cookie of
 * the STUN message header. The IPv6 endpoint is encoded with the magic cookie
 * and the transaction id of the STUN message. The attributes is defined as
 * follows:
 *
 * <pre>
 *  0                   1                   2                   3
 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |x x x x x x x x|    Family     |         X-Port                |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                X-Address (Variable)
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * </pre>
 *
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 *
 */
public final class XorMappedAddress implements Attribute {
    private static final int IPV4_FAMILY = 0x01;
    private static final int IPV6_FAMILY = 0x02;
    private static final int IPV4_MESSAGE_LENGTH = 8;
    private static final int IPV6_MESSAGE_LENGTH = 20;

    private final InetSocketAddress endpoint;
    private final int length;
    private final int ipFamily;
    private final int transactionId;

    /**
     * Creates a {@link XorMappedAddress} attribute for a IPv4 endpoint.
     *
     * @param endpoint
     *            the IPv4 endpoint
     */
    public XorMappedAddress(final InetSocketAddress endpoint) {
        this(endpoint, -1, IPV4_FAMILY, IPV4_MESSAGE_LENGTH);
    }

    /**
     * Creates a {@link XorMappedAddress} attribute for a IPv6 endpoint.
     *
     * @param endpoint
     *            the IPv6 endpoint
     * @param transactionId
     *            the transaction id of the corresponding message
     */
    public XorMappedAddress(final InetSocketAddress endpoint, final int transactionId) {
        this(endpoint, transactionId, IPV6_FAMILY, IPV6_MESSAGE_LENGTH);
    }

    private XorMappedAddress(final InetSocketAddress endpoint, final int transactionId, final int ipFamily,
            final int messageLength) {
        this.checkCorrectIpFamiy(endpoint, ipFamily);
        this.length = messageLength;
        this.endpoint = endpoint;
        this.ipFamily = ipFamily;
        this.transactionId = transactionId;
    }

    private void checkCorrectIpFamiy(final InetSocketAddress endpoint, final int expectedIpFamily) {
        int actualIpFamily;
        if (endpoint.getAddress() instanceof Inet4Address) {
            actualIpFamily = IPV4_FAMILY;
        } else if (endpoint.getAddress() instanceof Inet6Address) {
            actualIpFamily = IPV6_FAMILY;
        } else {
            throw new IllegalArgumentException("Unknown IP family: " + endpoint.getAddress().getClass()); //$NON-NLS-1$
        }
        if (actualIpFamily != expectedIpFamily) {
            throw new IllegalArgumentException("Actual address familiy meets not the expected address family."); //$NON-NLS-1$
        }
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
        return STUNAttributeType.XOR_MAPPED_ADDRESS;
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
        // ip family
        dout.writeByte(this.ipFamily);
        // port
        final int xport = xorPort(this.endpoint.getPort());
        dout.writeShort(xport);
        // ip address
        final byte[] xip = xorIp(this.endpoint.getAddress().getAddress(), this.ipFamily, this.transactionId);
        dout.write(xip);

        out.write(bout.toByteArray());
        out.flush();
    }

    /**
     * Creates a {@link XorMappedAddress} from the given encoded attribute and
     * header.
     *
     * @param encoded
     *            the encoded {@link XorMappedAddress} attribute
     * @param header
     *            the attribute header
     * @return the {@link XorMappedAddress} of the given encoding
     * @throws IOException
     *             if an I/O exception occurs
     * @throws MessageFormatException
     *             if the encoded {@link XorMappedAddress} is malformed
     */
    public static XorMappedAddress fromBytes(final byte[] encoded, final AttributeHeader header,
            final MessageHeader messageHeader) throws IOException, MessageFormatException {
        final ByteArrayInputStream bin = new ByteArrayInputStream(encoded);
        final DataInputStream din = new DataInputStream(bin);
        // leading zeros
        final int leadingZeroBits = din.readUnsignedByte();
        checkLeadingZeros(leadingZeroBits);
        // ip family
        final int ipFamilyBits = din.readUnsignedByte();
        final byte[] xIpAsBytes = getByteArrayForIp(ipFamilyBits);
        // port
        final int xport = din.readUnsignedShort();
        final int port = xorPort(xport);
        // ip address
        din.readFully(xIpAsBytes);
        final ByteBuffer txid = ByteBuffer.wrap(messageHeader.getTransactionId());
        final byte[] correctIp = xorIp(xIpAsBytes, ipFamilyBits, txid.getInt());
        final InetAddress address = InetAddress.getByAddress(correctIp);

        return new XorMappedAddress(new InetSocketAddress(address, port));
    }

    private static int xorPort(final int port) {
        final int xorKey = (MessageHeader.MAGIC_COOKIE & 0xFFFF0000) >> 0x10;
        return port ^ xorKey;
    }

    private static byte[] xorIp(final byte[] ipBytes, final int ipFamily, final int transactionId) {
        final ByteBuffer byteBuffer = ByteBuffer.wrap(ipBytes);
        int xorKey;
        int length;
        if (ipFamily == IPV4_FAMILY) {
            xorKey = MessageHeader.MAGIC_COOKIE;
            length = 4;
        } else if (ipFamily == IPV6_FAMILY) {
            xorKey = MessageHeader.MAGIC_COOKIE | transactionId;
            length = 16;
        } else {
            throw new IllegalArgumentException("Unknown ip family: " + ipFamily); //$NON-NLS-1$
        }
        final int xoredIp = (byteBuffer.getInt() ^ xorKey);
        return ByteBuffer.allocate(length).putInt(xoredIp).array();
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((endpoint == null) ? 0 : endpoint.hashCode());
        result = (prime * result) + ipFamily;
        result = (prime * result) + length;
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        XorMappedAddress other = (XorMappedAddress) obj;
        if (endpoint == null) {
            if (other.endpoint != null) {
                return false;
            }
        } else if (!endpoint.equals(other.endpoint)) {
            return false;
        }
        if (ipFamily != other.ipFamily) {
            return false;
        }
        if (length != other.length) {
            return false;
        }
        return true;
    }
}
