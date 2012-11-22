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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Attribute which carries a 128 bit token.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class Token implements Attribute {
    private static final int TOKEN_LENGTH_IN_BYTE = 16;
    private final UUID token;

    /**
     * Creates a {@link Token}.
     * 
     * @param token
     *            the UUID which is used for the token
     */
    public Token(final UUID token) {
        this.token = token;
    }

    /**
     * The method returns the token as {@link UUID}.
     * 
     * @return the token as {@link UUID}
     */
    public UUID getToken() {
        return this.token;
    }

    @Override
    public AttributeType getType() {
        return STUNAttributeType.TOKEN;
    }

    @Override
    public int getLength() {
        return TOKEN_LENGTH_IN_BYTE;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeLong(this.token.getLeastSignificantBits());
        dos.writeLong(this.token.getMostSignificantBits());
        dos.flush();
    }

    /**
     * Decodes a {@link Token} attribute.
     * 
     * @param encoded
     *            the encoded token attribute
     * @return the decoded {@link Token} attribute
     * @throws IOException
     *             if an I/O error occurs
     */
    static Attribute fromBytes(byte[] encoded) throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(encoded);
        DataInputStream din = new DataInputStream(bin);
        final long leastSignificantBits = din.readLong();
        final long mostSignificantBits = din.readLong();
        final UUID decodedToken = new UUID(mostSignificantBits, leastSignificantBits);
        return new Token(decodedToken);
    }
}
