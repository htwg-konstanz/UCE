/*
    Copyright (c) 2012 Alexander Diener,

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
package de.fhkn.in.uce.stun.header;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.fhkn.in.uce.stun.util.MessageFormatException;

/**
 * Class to decode byte encoded message headers.
 *
 * @author Daniel Maier, Alexander Diener (aldiener@htwg-konstanz.de)
 *
 */
public final class MessageHeaderDecoder {
    // tzn: this should go into MessageHeader, it is header info anyway
    private static final int HEADER_LENGTH = 20;
    private static final int LEADING_ZEROS_MASK = 0xC0000000;
    private static final int LEADING_ZEROS_SHIFT = 0x1E;
    private static final int MESSAGE_TYPE_MASK = 0x3FFF0000;
    private static final int MESSAGE_TYPE_SHIFT = 0x10;
    private static final int MESSAGE_CLASS_MASK = 0x0110;
    private static final int MESSAGE_METHOD_MASK = 0x3EEF;
    private static final int MESSAGE_LENGTH_MASK = 0x0000FFFF;
    private static final int TRANSACTION_ID_LENGTH = 12;

    private final MessageMethodDecoder commonMessageMethodDecoder;
    private final List<MessageMethodDecoder> customMessageMethodDecoders;
    private final MessageClassDecoder commonMethodClassDecoder;

    /**
     * Creates a new {@link MessageHeaderDecoder}.
     */
    public MessageHeaderDecoder() {
        this(new ArrayList<MessageMethodDecoder>());
    }

    /**
     * Creates a new {@link MessageHeaderDecoder} that uses additionally to the
     * {@link STUNMessageMethodDecoder} the given
     * <code>customMethodDecoders</code> to decode the method of the message.
     *
     * @param customMethodDecoders
     *            a list with decoders that gets used to decode custom methods
     * @throws NullPointerException
     *             if the given <code>customMethodDecoder</code> is null
     */
    public MessageHeaderDecoder(final List<MessageMethodDecoder> customMethodDecoders) {
        if (customMethodDecoders == null) {
            throw new NullPointerException();
        }
        this.commonMessageMethodDecoder = new STUNMessageMethodDecoder();
        this.customMessageMethodDecoders = customMethodDecoders;
        this.commonMethodClassDecoder = new STUNMessageClassDecoder();
    }

    /**
     * Decodes the given byte encoded message header.
     *
     * @param encoded
     *            the byte encoded message header
     * @return the decoded message header
     * @throws IOException
     *             if an I/O error occurs
     * @throws MessageFormatException
     *             if the message header is malformed
     * @throws NullPointerException
     *             if the parameter <code>encoded</code> is null
     */
    public MessageHeader decodeSTUNMessageHeader(final byte[] encoded) throws IOException, MessageFormatException {
        this.checkHeaderLength(encoded);
        final ByteArrayInputStream bin = new ByteArrayInputStream(encoded);
        final DataInputStream din = new DataInputStream(bin);
        final int leading32Bits = din.readInt();
        this.decodeLeadingZeroBits(leading32Bits);
        final int messageTypeBits = (leading32Bits & MESSAGE_TYPE_MASK) >> MESSAGE_TYPE_SHIFT;
        final MessageClass messageClass = this.decodeMessageClass(messageTypeBits);
        final MessageMethod messageMethod = this.decodeMessageMethod(messageTypeBits);
        final int length = leading32Bits & MESSAGE_LENGTH_MASK;
        final int magicCookieBits = din.readInt();
        this.checkMagicCookie(magicCookieBits);
        final byte[] transactionIdBytes = new byte[TRANSACTION_ID_LENGTH];
        din.readFully(transactionIdBytes);
        return new MessageHeaderImpl(messageClass, messageMethod, length, transactionIdBytes);
    }

    private void checkHeaderLength(final byte[] encodedHeader) throws MessageFormatException {
        if (encodedHeader == null) {
            throw new NullPointerException();
        } else if (encodedHeader.length != HEADER_LENGTH) {
            throw new MessageFormatException("Header has not the expected length."); //$NON-NLS-1$
        }
    }

    private void decodeLeadingZeroBits(final int leading32Bits) throws MessageFormatException {
        final int leadingZeroBits = (leading32Bits & LEADING_ZEROS_MASK) >> LEADING_ZEROS_SHIFT;
        if (leadingZeroBits != 0) {
            throw new MessageFormatException("The most significant two bits must be zero but were " + leadingZeroBits); //$NON-NLS-1$
        }
    }

    private MessageClass decodeMessageClass(final int messageTypeBits) {
        final int messageClassBits = messageTypeBits & MESSAGE_CLASS_MASK;
        return this.commonMethodClassDecoder.decode(messageClassBits);
    }

    private MessageMethod decodeMessageMethod(final int messageTypeBits) throws MessageFormatException {
        final int messageMethodBits = messageTypeBits & MESSAGE_METHOD_MASK;
        MessageMethod result = this.decodeMessageMethodWithCommonDecoder(messageMethodBits);
        if (result == null) {
            result = this.decodeMessageMethodWithCustomDecoders(messageMethodBits);
            if (result == null) {
                throw new MessageFormatException("Unknown message: " + messageMethodBits); //$NON-NLS-1$
            }
        }
        return result;
    }

    private MessageMethod decodeMessageMethodWithCommonDecoder(final int messageMethodBits) {
        return this.commonMessageMethodDecoder.decode(messageMethodBits);
    }

    private MessageMethod decodeMessageMethodWithCustomDecoders(final int messageMethodBits) {
        MessageMethod result = null;
        for (final MessageMethodDecoder customDecoder : this.customMessageMethodDecoders) {
            result = customDecoder.decode(messageMethodBits);
            if (result != null) {
                break;
            }
        }

        return result;
    }

    private void checkMagicCookie(final int toCheck) throws MessageFormatException {
        if (toCheck != MessageHeader.MAGIC_COOKIE) {
            throw new MessageFormatException("The magic cookie is wrong: " + toCheck); //$NON-NLS-1$
        }
    }

    /**
     * A implementation of a {@link MessageHeader}.
     *
     * @author Daniel Maier, Alexander Diener (aldiener@htwg-konstanz.de)
     *
     */
    public static final class MessageHeaderImpl implements MessageHeader {

        private final MessageClass messageClass;
        private final MessageMethod method;
        private final int length;
        private final byte[] transactionId;

        /**
         * Creates an object of {@link MessageHeaderImpl}.
         *
         * @param messageClass
         *            the {@link MessageClass}
         * @param method
         *            the {@link MessageMethod}
         * @param length
         *            the length of the message without header
         * @param transactionId
         *            the transaction id
         */
        public MessageHeaderImpl(final MessageClass messageClass, final MessageMethod method, final int length,
                final byte[] transactionId) {
            this.checkParameters(messageClass, method, length, transactionId);
            this.method = method;
            this.messageClass = messageClass;
            this.length = length;
            this.transactionId = transactionId;
        }

        private void checkParameters(final MessageClass messageClass, final MessageMethod method, final int length,
                final byte[] transactionId) {
            if ((method == null) || (messageClass == null) || (transactionId == null)) {
                throw new NullPointerException();
            }
            if (length < 0) {
                throw new IllegalArgumentException("Length needs to be possitive, but was: " + length); //$NON-NLS-1$
            }
            if (ByteBuffer.wrap(transactionId).getInt() == 0) {
                throw new IllegalArgumentException("The transaction id must not be 0."); //$NON-NLS-1$
            }
        }

        @Override
        public MessageMethod getMethod() {
            return this.method;
        }

        @Override
        public byte[] getTransactionId() {
            return this.transactionId;
        }

        @Override
        public int getLength() {
            return this.length;
        }

        @Override
        public MessageClass getMessageClass() {
            return this.messageClass;
        }

        @Override
        public void writeTo(final OutputStream out) throws IOException {
            final ByteArrayOutputStream bout = new ByteArrayOutputStream();
            final DataOutputStream dout = new DataOutputStream(bout);

            final int methodBits = this.method.encode();
            final int messageClassBits = this.messageClass.encode();
            // TODO is the bitwise addition of class and method correct?
            final int leading32bits = (0x0 << LEADING_ZEROS_SHIFT)
                    | ((messageClassBits + methodBits) << MESSAGE_TYPE_SHIFT) | ((short) this.length);
            dout.writeInt(leading32bits);
            dout.writeInt(MessageHeader.MAGIC_COOKIE);
            dout.write(this.transactionId);
            final byte[] headerAsBytes = bout.toByteArray();
            if (headerAsBytes.length != HEADER_LENGTH) {
                throw new MessageFormatException("Header has the wrong length: " + headerAsBytes.length); //$NON-NLS-1$
            }

            out.write(bout.toByteArray());
            out.flush();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + this.length;
            result = (prime * result) + ((this.messageClass == null) ? 0 : this.messageClass.hashCode());
            result = (prime * result) + ((this.method == null) ? 0 : this.method.hashCode());
            result = (prime * result) + Arrays.hashCode(this.transactionId);
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
            final MessageHeaderImpl other = (MessageHeaderImpl) obj;
            if (this.length != other.length) {
                return false;
            }
            if (this.messageClass == null) {
                if (other.messageClass != null) {
                    return false;
                }
            } else if (!this.messageClass.equals(other.messageClass)) {
                return false;
            }
            if (this.method == null) {
                if (other.method != null) {
                    return false;
                }
            } else if (!this.method.equals(other.method)) {
                return false;
            }
            if (!Arrays.equals(this.transactionId, other.transactionId)) {
                return false;
            }
            return true;
        }
    }
}
