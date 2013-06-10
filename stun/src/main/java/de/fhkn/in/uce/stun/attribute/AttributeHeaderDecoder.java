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
import java.util.ArrayList;
import java.util.List;

import de.fhkn.in.uce.stun.util.MessageFormatException;

/**
 * Class to decode byte encoded attribute headers.
 *
 * @author Daniel Maier, Alexander Diener (aldiener@htwg-konstanz.de)
 *
 */
public final class AttributeHeaderDecoder {
    private final AttributeTypeDecoder commonAttributeTypeDecoder;
    private final List<AttributeTypeDecoder> customAttributeTypeDecoders;

    /**
     * Creates a new {@link AttributeHeaderDecoder}.
     */
    AttributeHeaderDecoder() {
        this(new ArrayList<AttributeTypeDecoder>());
    }

    /**
     * Creates a new {@link AttributeHeaderDecoder} with the specified
     * {@link AttributeTypeDecoder} to decode custom attribute types.
     *
     * @param customAttributeTypeDecoder
     *            a list of {@link AttributeTypeDecoder} to decode custom
     *            attribute types
     * @throws NullPointerException
     *             if the specified {@link AttributeTypeDecoder} is null
     */
    AttributeHeaderDecoder(final List<AttributeTypeDecoder> customAttributeTypeDecoders) {
        if (customAttributeTypeDecoders == null) {
            throw new NullPointerException();
        }
        this.commonAttributeTypeDecoder = new STUNAttributeTypeDecoder();
        this.customAttributeTypeDecoders = customAttributeTypeDecoders;
    }

    /**
     * Decodes the specified byte array to a {@link AttributeHeader} instance.
     *
     * @param encoded
     *            the byte encoded attribute header
     * @return the decoded attribute header
     * @throws IOException
     *             if an I/O error occurs if the encoded attribute header is
     *             malformed
     * @throws MessageFormatException
     * @throws NullPointerException
     *             if the specified byte array is null
     */
    public AttributeHeader decodeSTUNAttributeHeader(final byte[] encoded) throws IOException, MessageFormatException {
        this.checkHeaderLength(encoded);
        final ByteArrayInputStream bin = new ByteArrayInputStream(encoded);
        final DataInputStream din = new DataInputStream(bin);
        final int typeBits = din.readUnsignedShort();
        final AttributeType type = this.decodeAttributeType(typeBits);
        final int length = din.readUnsignedShort();
        return new AttributeHeaderImpl(type, length);
    }

    private void checkHeaderLength(final byte[] encodedHeader) throws MessageFormatException {
        if (encodedHeader == null) {
            throw new NullPointerException();
        } else if (encodedHeader.length != AttributeHeader.HEADER_LENGTH) {
            throw new MessageFormatException("Header has not the expected length"); //$NON-NLS-1$
        }
    }

    private AttributeType decodeAttributeType(final int attributeTypeBits) throws MessageFormatException {
        AttributeType result = this.decodeAttributeTypeWithCommonDecoder(attributeTypeBits);
        if (result == null) {
            result = this.decodeAttributeTypeWithCustomDecoders(attributeTypeBits);
            if (result == null) {
                // TODO this could be a comprehension-optional attribute, check
                // it and do something useful, if comprehension-required throw
                // exception
                result = STUNAttributeType.UNKNOWN_ATTRIBUTES;
            }
        }
        return result;
    }

    private AttributeType decodeAttributeTypeWithCommonDecoder(final int attributeTypeBits) {
        AttributeType result = null;
        result = this.commonAttributeTypeDecoder.decode(attributeTypeBits);
        return result;
    }

    private AttributeType decodeAttributeTypeWithCustomDecoders(final int attributeTypeBits) {
        AttributeType result = null;
        for (final AttributeTypeDecoder customDecoder : this.customAttributeTypeDecoders) {
            result = customDecoder.decode(attributeTypeBits);
            if (result != null) {
                break;
            }
        }
        return result;
    }

    @SuppressWarnings("unused")
    private boolean isComprehensionOptional(final int typeBits) {
        boolean result = false;
        if ((typeBits >= 0x8000) && (typeBits <= 0xFFFF)) {
            result = true;
        }
        return result;
    }

    /**
     * Implementation of a {@link AttributeHeader}.
     *
     * @author Daniel Maier, Alexander Diener (aldiener@htwg-konstanz.de)
     *
     */
    static final class AttributeHeaderImpl implements AttributeHeader {

        private final AttributeType type;
        private final int length;

        /**
         * Creates a new {@link AttributeHeaderImpl} with the specified
         * {@link AttributeType} and length.
         *
         * @param type
         *            the attribute type of this header
         * @param length
         *            the length of the attribute
         */
        AttributeHeaderImpl(final AttributeType type, final int length) {
            this.type = type;
            this.length = length;
        }

        @Override
        public AttributeType getType() {
            return this.type;
        }

        @Override
        public int getLength() {
            return this.length;
        }

        @Override
        public void writeTo(final OutputStream out) throws IOException {
            final ByteArrayOutputStream bout = new ByteArrayOutputStream();
            final DataOutputStream dout = new DataOutputStream(bout);
            dout.writeInt(this.type.encode());
            dout.writeInt(this.length);
            out.write(bout.toByteArray());
            out.flush();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + this.length;
            result = (prime * result) + ((this.type == null) ? 0 : this.type.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            final AttributeHeaderImpl other = (AttributeHeaderImpl) obj;
            if (this.length != other.length) {
                return false;
            }
            if (this.type == null) {
                if (other.type != null) {
                    return false;
                }
            } else if (!this.type.equals(other.type)) {
                return false;
            }
            return true;
        }
    }
}
