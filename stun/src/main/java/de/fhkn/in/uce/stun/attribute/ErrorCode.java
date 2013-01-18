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
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import de.fhkn.in.uce.stun.util.MessageFormatException;

/**
 * Implementation of {@link Attribute} according to the ERROR-CODE described in
 * RFC 5389.
 * 
 * <pre>
 *   0                   1                   2                   3
 *   0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *  |           Reserved, should be 0         |Class|     Number    |
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *  |      Reason Phrase (variable)                                ..
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * </pre>
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class ErrorCode implements Attribute {
    private static final String STRING_ENCODING = "UTF-8"; //$NON-NLS-1$
    private static final int MAX_REASON_PHRASE_BYTES = 763;
    private static final int LEADING_ZEROS_SHIFT = 0xB;
    private static final int ERROR_CLASS_SHIFT = 0x8;
    private static final int LEADING_ZEROS_MASK = 0xFFFFF800;
    private static final int ERROR_CLASS_MASK = 0x000007FF;
    private static final int ERROR_CODE_MASK = 0x000000FF;
    private final STUNErrorCode errorNumber;
    private final int errorClass;
    private final String reasonPhrase;
    private final byte[] reasonPhraseBytes;

    /**
     * Creates an {@link ErrorCode} attribute.
     * 
     * @param errorCode
     *            the {@link STUNErrorCode} which are defined in RFC 5389
     * @param reasonPhrase
     *            a textual description of the reason for that error which can
     *            be as long as 763 bytes
     * @throws UnsupportedEncodingException
     *             if the {@code reasonPhrase} could not be encoded
     */
    public ErrorCode(final STUNErrorCode errorCode, final String reasonPhrase) throws UnsupportedEncodingException {
        this.errorNumber = errorCode;
        this.errorClass = this.getClassForErrorCode(errorCode.getErrorCode());
        this.reasonPhrase = reasonPhrase;
        this.reasonPhraseBytes = this.encodeString(reasonPhrase);
        if (this.reasonPhraseBytes.length > MAX_REASON_PHRASE_BYTES) {
            throw new IllegalArgumentException(
                    "Reason phrase encoded with " + STRING_ENCODING + "must not be larger than " //$NON-NLS-1$ //$NON-NLS-2$
                            + MAX_REASON_PHRASE_BYTES + ", but was " + this.reasonPhraseBytes.length); //$NON-NLS-1$
        }
    }

    /**
     * Returns the {@link STUNErrorCode} according to RFC 5389.
     * 
     * @return the {@link STUNErrorCode}
     */
    public STUNErrorCode getErrorNumber() {
        return this.errorNumber;
    }

    /**
     * Returns the error class which is described in the RFC.
     * 
     * @return the encoded error class
     */
    public int getErrorClass() {
        return this.errorClass;
    }

    /**
     * Returns the textual description for that error.
     * 
     * @return the textual description for that error
     */
    public String getReasonPhrase() {
        return this.reasonPhrase;
    }

    @Override
    public AttributeType getType() {
        return STUNAttributeType.ERROR_CODE;
    }

    @Override
    public int getLength() {
        return this.reasonPhraseBytes.length;
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final DataOutputStream dout = new DataOutputStream(bout);

        // leading 32 bits
        final int leading32bits = (0x0 << LEADING_ZEROS_SHIFT) | (this.errorClass << ERROR_CLASS_SHIFT)
                | (this.errorNumber.getErrorCode());
        dout.writeInt(leading32bits);
        // padding
        final int paddingSize = this.calculatePaddingBytes(this.reasonPhraseBytes);
        if (paddingSize != 0) {
            final byte[] paddingBits = new byte[paddingSize];
            dout.write(paddingBits);
        }
        // error phrase
        dout.write(this.reasonPhraseBytes);

        out.write(bout.toByteArray());
        out.flush();

    }

    private byte[] encodeString(final String text) throws UnsupportedEncodingException {
        return text.getBytes(STRING_ENCODING);
    }

    private int calculatePaddingBytes(final byte[] username) {
        int result = 0;
        final int modulo = username.length % 4;
        if (modulo != 0) {
            result = 4 - modulo;
        }

        return result;
    }

    private int getClassForErrorCode(final int errorCode) {
        final int result = errorCode / 100;
        if (result < 3 || result > 6) {
            throw new IllegalArgumentException("A invalid error class was calculated: " + result); //$NON-NLS-1$
        }
        return result;
    }

    /**
     * Creates a {@link ErrorCode} from the given encoded attribute and header.
     * 
     * @param encoded
     *            the encoded {@link ErrorCode} attribute
     * @param header
     *            the attribute header
     * @return the {@link ErrorCode} of the given encoding
     * @throws IOException
     *             if an I/O exception occurs
     * @throws MessageFormatException
     *             if the encoded {@link ErrorCode} is malformed
     */
    public static ErrorCode fromBytes(final byte[] encoded, final AttributeHeader header) throws IOException,
            MessageFormatException {
        final ByteArrayInputStream bin = new ByteArrayInputStream(encoded);
        final DataInputStream din = new DataInputStream(bin);

        final int leading32Bits = din.readInt();
        // leading zeros
        final int leadingZeroBits = leading32Bits & LEADING_ZEROS_MASK;
        if (leadingZeroBits != 0) {
            throw new MessageFormatException("Wrong message format, the leading zeros were " + leadingZeroBits); //$NON-NLS-1$
        }
        // error class
        @SuppressWarnings("unused")
        final int errorClassBits = (leading32Bits & ERROR_CLASS_MASK) >> ERROR_CLASS_SHIFT;
        // error code
        final int errorCodeBITS = leading32Bits & ERROR_CODE_MASK;
        final STUNErrorCode errorCode = STUNErrorCode.fromErrorCode(errorCodeBITS);
        // error phrase
        final byte[] reasonPhraseBytes = new byte[header.getLength() - 4];
        din.readFully(reasonPhraseBytes);
        final String reasonPhrase = new String(reasonPhraseBytes, STRING_ENCODING);

        return new ErrorCode(errorCode, reasonPhrase);
    }

    /**
     * Enumeration to represent the error code. The error codes are defined in
     * RFC 5389.
     * 
     * @author Alexander Diener (aldiener@htwg-konstanz.de)
     * 
     */
    public enum STUNErrorCode {
        TRY_ALTERNATE(300),

        BAD_REQUEST(400),

        UNAUTHORIZED(401),

        UNKNOWN_ATTRIBUTE(420),

        STALE_NONCE(438),

        SERVER_ERROR(500),

        INSUFFICIENT_CAPACITY(600);

        private static final Map<Integer, STUNErrorCode> intToEnum = new HashMap<Integer, STUNErrorCode>();

        static {
            for (final STUNErrorCode l : values()) {
                intToEnum.put(l.errorCode, l);
            }
        }

        private final int errorCode;

        private STUNErrorCode(final int errorCode) {
            this.errorCode = errorCode;
        }

        /**
         * Returns the encoded error code.
         * 
         * @return the encoded error code
         */
        public int getErrorCode() {
            return this.errorCode;
        }

        /**
         * Returns for the given encoded {@code errorCode} the corresponding
         * {@link STUNErrorCode}.
         * 
         * @param errorCode
         *            the encoded error code
         * @return the corresponding {@link STUNErrorCode}
         */
        public static STUNErrorCode fromErrorCode(final int errorCode) {
            return intToEnum.get(errorCode);
        }
    }
}
