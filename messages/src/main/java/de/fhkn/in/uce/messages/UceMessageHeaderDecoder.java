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
import java.util.UUID;

/**
 * Class to decode byte encoded uce message headers.
 * 
 * @author Daniel Maier
 * 
 */
final class UceMessageHeaderDecoder {
    private static final int HEADER_LENGTH = 20;
    private static final int MAGIC_MASK = 0xF000;
    private static final int MAGIC = 0x5;
    private static final int MAGIC_SHIFT = 0xC;
    private static final int METHOD_MASK = 0x0FFC;
    private static final int METHOD_SHIFT = 0x2;
    private static final int SEMANTIC_LEVEL_MASK = 0x0003;
    private static final int TRANSACTION_ID_LENGTH = 16;
    private final UceMethodDecoder commonMethodDecoder;
    private final UceMethodDecoder customMethodDecoder;

    /**
     * Creates a new {@link UceMessageHeaderDecoder}.
     */
    UceMessageHeaderDecoder() {
        this(new UceMethodDecoder() {

            public UceMethod decode(int encoded) {
                return null;
            }
        });
    }

    /**
     * Creates a new {@link UceMessageHeaderDecoder} that uses additionally to
     * the {@link CommonUceMethodDecoder} the given
     * <code>customMethodDecoder</code> to decode the method of the message.
     * 
     * @param customMethodDecoder
     *            the decoder that gets used to decode custom uce methods
     * @throws NullPointerException
     *             if the given <code>customMethodDecoder</code> is null
     */
    UceMessageHeaderDecoder(UceMethodDecoder customMethodDecoder) {
        if (customMethodDecoder == null) {
            throw new NullPointerException();
        }
        this.commonMethodDecoder = new CommonUceMethodDecoder();
        this.customMethodDecoder = customMethodDecoder;
    }

    /**
     * Decodes the given byte encoded uce message header.
     * 
     * @param encoded
     *            the byte encoded uce message header
     * @return the decoded uce message header
     * @throws IOException
     *             if an I/O error occurs
     * @throws MessageFormatException
     *             if the uce message header is malformed
     * @throws NullPointerException
     *             if the parameter <code>encoded</code> is null
     */
    UceMessageHeader decodeUceMessageHeader(byte[] encoded) throws IOException,
            MessageFormatException {
        if (encoded == null) {
            throw new NullPointerException();
        } else if (encoded.length != HEADER_LENGTH) {
            throw new MessageFormatException("Header has not the expected length");
        }
        ByteArrayInputStream bin = new ByteArrayInputStream(encoded);
        DataInputStream din = new DataInputStream(bin);
        int leading16Bits = din.readUnsignedShort();
        // magic
        int magic = (leading16Bits & MAGIC_MASK) >> MAGIC_SHIFT;
        if (magic != MAGIC) {
            throw new MessageFormatException("Wrong magic: " + magic);
        }
        // method
        int methodBits = (leading16Bits & METHOD_MASK) >> METHOD_SHIFT;
        // is it common method?
        UceMethod method = commonMethodDecoder.decode(methodBits);
        if (method == null) {
            // no common method
            method = customMethodDecoder.decode(methodBits);
            // still not found -> method is unknown
            if (method == null) {
                throw new MessageFormatException("Unknown method: " + methodBits);
            }
        }
        // semantic level
        int semanticLevelBits = (leading16Bits & SEMANTIC_LEVEL_MASK);
        SemanticLevel semanticLevel = SemanticLevel.fromEncoded(semanticLevelBits);
        // length
        int length = din.readUnsignedShort();
        // transaction id
        byte[] transactionIdBytes = new byte[TRANSACTION_ID_LENGTH];
        din.readFully(transactionIdBytes);
        UUID transactionId = UUIDCoder.toUUID(transactionIdBytes);
        return new UceMessageHeaderImpl(method, semanticLevel, length, transactionId);
    }

    /**
     * A implementation of a {@link UceMessageHeader}.
     * 
     * @author Daniel Maier
     * 
     */
    static final class UceMessageHeaderImpl implements UceMessageHeader {

        private final UceMethod method;
        private final SemanticLevel semanticLevel;
        private final int length;
        private final UUID transactionId;

        /**
         * Creates a new {@link UceMessageHeaderImpl}.
         * 
         * @param method
         *            the method of the message
         * @param semanticLevel
         *            the semantic level of the message
         * @param length
         *            the length of the rest of the message (without message
         *            header)
         * @param transactionId
         *            the transaction id of the message
         * 
         * @throws NullPointerException
         *             if <code>method</code>, <code>semanticLevel</code> or
         *             <code>transactionId</code> is null, or if length is
         *             negative
         */
        UceMessageHeaderImpl(UceMethod method, SemanticLevel semanticLevel, int length,
                UUID transactionId) {
            if (method == null || semanticLevel == null || transactionId == null) {
                throw new NullPointerException();
            } else if (length < 0) {
                throw new IllegalArgumentException("Length needs to be possitive, but was: "
                        + length);
            }
            this.method = method;
            this.semanticLevel = semanticLevel;
            this.length = length;
            this.transactionId = transactionId;
        }

        /**
         * @return the method
         */
        public UceMethod getMethod() {
            return method;
        }

        /**
         * @return the semanticLevel
         */
        public SemanticLevel getSemanticLevel() {
            return semanticLevel;
        }

        /**
         * @return the transactionId
         */
        public UUID getTransactionId() {
            return transactionId;
        }

        public int getLength() {
            return length;
        }

        public void writeTo(OutputStream out) throws IOException {
            int methodBits = method.encode();
            int semanticLevelBits = semanticLevel.encode();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(bout);
            byte[] transactionIdBytes = UUIDCoder.asByteArray(transactionId);
            int leading16Bits = (MAGIC << MAGIC_SHIFT) | (methodBits << METHOD_SHIFT)
                    | semanticLevelBits;
            dout.writeShort(leading16Bits);
            dout.writeShort(length);
            dout.write(transactionIdBytes);
            out.write(bout.toByteArray());
            out.flush();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + length;
            result = prime * result + ((method == null) ? 0 : method.hashCode());
            result = prime * result + ((semanticLevel == null) ? 0 : semanticLevel.hashCode());
            result = prime * result + ((transactionId == null) ? 0 : transactionId.hashCode());
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
            if (!(obj instanceof UceMessageHeaderImpl)) {
                return false;
            }
            UceMessageHeaderImpl other = (UceMessageHeaderImpl) obj;
            if (length != other.length) {
                return false;
            }
            if (method == null) {
                if (other.method != null) {
                    return false;
                }
            } else if (!method.equals(other.method)) {
                return false;
            }
            if (semanticLevel != other.semanticLevel) {
                return false;
            }
            if (transactionId == null) {
                if (other.transactionId != null) {
                    return false;
                }
            } else if (!transactionId.equals(other.transactionId)) {
                return false;
            }
            return true;
        }
    }
}
