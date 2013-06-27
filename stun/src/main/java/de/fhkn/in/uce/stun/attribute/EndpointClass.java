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
import java.util.HashMap;
import java.util.Map;

import de.fhkn.in.uce.stun.util.MessageFormatException;

/**
 * The {@link EndpointClass} is an implementation of {@link Attribute} and
 * represents the class of a endpoint. The attribute is used in the Universal
 * Connection Establishment context to categorize endpoints.
 *
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 *
 */
public final class EndpointClass implements Attribute {
    private final int length;
    private final EndpointCategory endpointCategory;

    /**
     * Creates a new attribute of type {@link EndpointClass} with the given
     * {@link EndpointCategory}.
     *
     * @param edpointCategory
     */
    public EndpointClass(final EndpointCategory edpointCategory) {
        this.length = 4;
        this.endpointCategory = edpointCategory;
    }

    /**
     * Returns the {@link EndpointCategory} of the endpoint.
     *
     * @return
     */
    public EndpointCategory getEndpointCategory() {
        return this.endpointCategory;
    }

    @Override
    public AttributeType getType() {
        return STUNAttributeType.ENDPOINT_CLASS;
    }

    @Override
    public int getLength() {
        return this.length;
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final DataOutputStream dout = new DataOutputStream(bout);

        dout.writeInt(this.endpointCategory.encode());

        out.write(bout.toByteArray());
        out.flush();
    }

    /**
     * Creates a {@link EndpointCategory} from the given encoded attribute and
     * header.
     *
     * @param encoded
     *            the encoded {@link EndpointClass} attribute
     * @param header
     *            the attribute header
     * @return the {@link EndpointClass} of the given encoding
     * @throws IOException
     *             if an I/O exception occurs
     * @throws MessageFormatException
     *             if the encoded {@link EndpointClass} is malformed
     */
    public static EndpointClass fromBytes(final byte[] encoded, final AttributeHeader header) throws IOException,
            MessageFormatException {
        final ByteArrayInputStream bin = new ByteArrayInputStream(encoded);
        final DataInputStream din = new DataInputStream(bin);

        final int endpointCategoryBits = din.readInt();
        final EndpointCategory endpointCategory = EndpointCategory.fromEncoded(endpointCategoryBits);

        return new EndpointClass(endpointCategory);
    }

    /**
     * Enum to represent different types of endpoints.
     *
     * @author Daniel Maier
     *
     */
    public static enum EndpointCategory {
        /**
         * Type of endpoint is undefined.
         */
        UNDEFINED(0x0),
        /**
         * Endpoint is a private endpoint (behind NAT).
         */
        PRIVATE(0x1),
        /**
         * Endpoint is a public endpoint (visible from outside).
         */
        PUBLIC(0x2),
        /**
         * Endpoint is an endpoint on a server for relaying data between client
         * and peer.
         */
        RELAY(0x3),
        /**
         * Endpoint is an endpoint for connection reversal.
         */
        CONNECTION_REVERSAL(0x4);

        private static final Map<Integer, EndpointCategory> intToEnum = new HashMap<Integer, EndpointCategory>();

        static {
            for (final EndpointCategory l : values()) {
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
        private EndpointCategory(final int encoded) {
            this.encoded = encoded;
        }

        /**
         * Returns the byte encoded {@link EndpointClass}.
         *
         * @return
         */
        int encode() {
            return this.encoded;
        }

        /**
         * Decodes the {@link EndpointClass}.
         *
         * @param encoded
         *            the byte encoded {@link EndpointClass}
         * @return the decoded {@link EndpointClass}
         */
        private static EndpointCategory fromEncoded(final int encoded) {
            return intToEnum.get(encoded);
        }
    }
}
