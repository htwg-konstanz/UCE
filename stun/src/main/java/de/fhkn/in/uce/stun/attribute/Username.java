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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import de.fhkn.in.uce.stun.util.MessageFormatException;

/**
 * Implementation of {@link Attribute} which represents a USERNAME attribute
 * according to RFC 5389.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class Username implements Attribute {
    private static final String STRING_ENCODING = "UTF-8"; //$NON-NLS-1$
    private static final int MAX_USERNAME_BYTES = 513;
    private final byte[] username;

    /**
     * Creates a {@link Username} attribute.
     * 
     * @param username
     *            the username
     * @throws UnsupportedEncodingException
     *             if the {@code username} could not be encoded
     */
    public Username(final String username) throws UnsupportedEncodingException {
        this.username = this.encodeUniqueUsername(username);
        if (this.username.length > MAX_USERNAME_BYTES) {
            throw new IllegalArgumentException("Username encoded with " + STRING_ENCODING + "must not be larger than " //$NON-NLS-1$ //$NON-NLS-2$
                    + MAX_USERNAME_BYTES + ", but was " + this.username.length); //$NON-NLS-1$
        }
    }

    @Override
    public AttributeType getType() {
        return STUNAttributeType.USERNAME;
    }

    @Override
    public int getLength() {
        return this.username.length + this.calculatePaddingBytes(username);
    }

    /**
     * Returns the username as byte array.
     * 
     * @return the username as byte array
     */
    public byte[] getUsername() {
        return this.username;
    }

    /**
     * Returns the username as string.
     * 
     * @return the username as string
     * @throws UnsupportedEncodingException
     *             if an encoding exception occurs
     */
    public String getUsernameAsString() throws UnsupportedEncodingException {
        return new String(this.username, STRING_ENCODING);
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final DataOutputStream dout = new DataOutputStream(bout);

        final int paddingSize = this.calculatePaddingBytes(this.username);
        if (paddingSize != 0) {
            final byte[] paddingBits = new byte[paddingSize];
            dout.write(paddingBits);
        }

        dout.write(this.username);

        out.write(bout.toByteArray());
        out.flush();
    }

    private int calculatePaddingBytes(final byte[] username) {
        int result = 0;
        final int modulo = username.length % 4;
        if (modulo != 0) {
            result = 4 - modulo;
        }

        return result;
    }

    private byte[] encodeUniqueUsername(final String uniqueUserName) throws UnsupportedEncodingException {
        return uniqueUserName.getBytes(STRING_ENCODING);
    }

    /**
     * Creates a {@link Username} from the given encoded attribute and header.
     * 
     * @param encoded
     *            the encoded {@link Username} attribute
     * @param header
     *            the attribute header
     * @return the {@link Username} of the given encoding
     * @throws IOException
     *             if an I/O exception occurs
     * @throws MessageFormatException
     *             if the encoded {@link Username} is malformed
     */
    public static Username fromBytes(final byte[] encoded, final AttributeHeader header) throws IOException {
        return new Username(new String(encoded, STRING_ENCODING));
    }
}
