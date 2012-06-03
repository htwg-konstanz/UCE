/*
    Copyright (c) 2012 Thomas Zink, 

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fhkn.in.uce.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Class to decode byte encoded uce attribute headers.
 * 
 * @author Daniel Maier
 * 
 */
final class UceAttributeHeaderDecoder {

    static final int HEADER_LENGTH = 4;
    private final UceAttributeTypeDecoder commonAttributeTypeDecoder;
    private final UceAttributeTypeDecoder customAttributeTypeDecoder;

    /**
     * Creates a new {@link UceAttributeHeaderDecoder}.
     */
    UceAttributeHeaderDecoder() {
        this(new UceAttributeTypeDecoder() {

            public UceAttributeType decode(int encoded) {
                return null;
            }
        });
    }

    /**
     * Creates a new {@link UceAttributeHeaderDecoder} with the specified
     * {@link UceAttributeTypeDecoder} to decode custom attribute types.
     * 
     * @param customAttributeTypeDecoder
     *            {@link UceAttributeTypeDecoder} to decode custom attribute
     *            types
     * @throws NullPointerException
     *             if the specified {@link UceAttributeTypeDecoder} is null
     */
    UceAttributeHeaderDecoder(UceAttributeTypeDecoder customAttributeTypeDecoder) {
        if (customAttributeTypeDecoder == null) {
            throw new NullPointerException();
        }
        this.commonAttributeTypeDecoder = new CommonUceAttributeTypeDecoder();
        this.customAttributeTypeDecoder = customAttributeTypeDecoder;
    }

    /**
     * Decodes the specified byte array to a {@link UceAttributeHeader}
     * instance.
     * 
     * @param encoded
     *            the byte encoded attribute header
     * @return the decoded attribute header
     * @throws IOException
     *             if an I/O error occurs if the encoded attribute header is
     *             malformed
     * @throws MessageFormatException
     * @throws NullPointerException
     *             if the specified byte aray is null
     */
    UceAttributeHeader decodeUceAttributeHeader(byte[] encoded) throws IOException,
            MessageFormatException {
        if (encoded == null) {
            throw new NullPointerException();
        } else if (encoded.length != HEADER_LENGTH) {
            throw new MessageFormatException("Header has not the expected length");
        }
        ByteArrayInputStream bin = new ByteArrayInputStream(encoded);
        DataInputStream din = new DataInputStream(bin);
        // type
        int typeBits = din.readUnsignedShort();
        // is it common type?
        UceAttributeType type = commonAttributeTypeDecoder.decode(typeBits);
        if (type == null) {
            // no common type
            type = customAttributeTypeDecoder.decode(typeBits);
            // still not found -> type is unknown
            if (type == null) {
                throw new MessageFormatException("Unknown type: " + typeBits);
            }
        }
        // length
        int length = din.readUnsignedShort();
        return new UceAttributeHeaderImpl(type, length);
    }

    /**
     * Implementation of a {@link UceAttributeHeader}.
     * 
     * @author Daniel Maier
     * 
     */
    static final class UceAttributeHeaderImpl implements UceAttributeHeader {

        private final UceAttributeType type;
        private final int length;

        /**
         * Creates a new {@link UceAttributeHeaderImpl} with the specified
         * {@link UceAttributeType} and length.
         * 
         * @param type
         *            the uce attribute type of this header
         * @param length
         *            the length of the attribute
         */
        UceAttributeHeaderImpl(UceAttributeType type, int length) {
            this.type = type;
            this.length = length;
        }

        public UceAttributeType getType() {
            return type;
        }

        public int getLength() {
            return length;
        }

        public void writeTo(OutputStream out) throws IOException {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(bout);
            dout.writeShort(type.encode());
            dout.write(length);
            out.write(bout.toByteArray());
            out.flush();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + length;
            result = prime * result + ((type == null) ? 0 : type.hashCode());
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
            if (!(obj instanceof UceAttributeHeaderImpl)) {
                return false;
            }
            UceAttributeHeaderImpl other = (UceAttributeHeaderImpl) obj;
            if (length != other.length) {
                return false;
            }
            if (type == null) {
                if (other.type != null) {
                    return false;
                }
            } else if (!type.equals(other.type)) {
                return false;
            }
            return true;
        }
    }
}
