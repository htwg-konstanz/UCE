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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * {@link UceAttribute} that may be used to give additional information in a
 * {@link UceMessage} with {@link SemanticLevel} of
 * {@link SemanticLevel#ERROR_RESPONSE}.
 * 
 * @author Daniel Maier
 * 
 */
public final class ErrorCode implements UceAttribute {

    private static final String REASON_PHRASE_ENCODING = "UTF-8";
    private final int errorNumber;
    private final String reasonPhrase;
    private final byte[] reasonPhraseBytes;

    /**
     * Creates a new {@link ErrorCode}.
     * 
     * @param errorNumber
     *            the error number
     * @param reasonPhrase
     *            the reason phrase
     * @throws UnsupportedEncodingException
     *             if the charset (UTF-8) to encode the reason phrase is not
     *             supported
     */
    public ErrorCode(int errorNumber, String reasonPhrase) throws UnsupportedEncodingException {
        if (reasonPhrase == null) {
            throw new NullPointerException();
        }
        this.errorNumber = errorNumber;
        this.reasonPhrase = reasonPhrase;
        this.reasonPhraseBytes = reasonPhrase.getBytes(REASON_PHRASE_ENCODING);
    }

    public UceAttributeType getType() {
        return CommonUceAttributeType.ERROR_CODE;
    }

    public int getLength() {
        // error number bytes + reason phrase bytes length
        return 2 + reasonPhraseBytes.length;
    }

    public void writeTo(OutputStream out) throws IOException {
        DataOutputStream dout = new DataOutputStream(out);
        dout.writeShort(errorNumber);
        dout.write(reasonPhraseBytes);
    }

    /**
     * Returns the reason phrase of this {@link ErrorCode}.
     * 
     * @return the reason phrase
     */
    public String getReasonPhrase() {
        return reasonPhrase;
    }

    /**
     * Returns the error number of this reason phrase.
     * 
     * @return the error number
     */
    public int getErrorNumber() {
        return errorNumber;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + errorNumber;
        result = prime * result + ((reasonPhrase == null) ? 0 : reasonPhrase.hashCode());
        result = prime * result + Arrays.hashCode(reasonPhraseBytes);
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
        if (!(obj instanceof ErrorCode)) {
            return false;
        }
        ErrorCode other = (ErrorCode) obj;
        if (errorNumber != other.errorNumber) {
            return false;
        }
        if (reasonPhrase == null) {
            if (other.reasonPhrase != null) {
                return false;
            }
        } else if (!reasonPhrase.equals(other.reasonPhrase)) {
            return false;
        }
        if (!Arrays.equals(reasonPhraseBytes, other.reasonPhraseBytes)) {
            return false;
        }
        return true;
    }

    /**
     * Class to represent some general constants for error numbers.
     * 
     * @author Daniel Maier
     * 
     */
    public static final class ErrorCodes {
        /**
         * The request could not be processed because the request was invalid.
         */
        public static final int BAD_REQUEST = 400;
        /**
         * The request could not be processed because the recipient has
         * insufficient capacity.
         */
        public static final int INSUFFICIENT_CAPACITY = 508;
    }

    /**
     * Decodes a byte encoded {@link ErrorCode} from a byte array.
     * 
     * @param encoded
     *            the encoded {@link ErrorCode} (without header)
     * @param header
     *            the header of the attribute
     * @return the decoded {@link ErrorCode}
     * @throws IOException
     *             if an I/O error occurs
     */
    static UceAttribute fromBytes(byte[] encoded, UceAttributeHeader header) throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(encoded);
        DataInputStream din = new DataInputStream(bin);
        int errorNumber = din.readUnsignedShort();
        byte[] reasonPhraseBytes = new byte[header.getLength() - 2];
        din.readFully(reasonPhraseBytes);
        String reasonPhrase = new String(reasonPhraseBytes, REASON_PHRASE_ENCODING);
        return new ErrorCode(errorNumber, reasonPhrase);
    }

}
