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

import java.io.IOException;
import java.io.OutputStream;

import de.fhkn.in.uce.stun.util.MessageFormatException;

/**
 * The {@link Unknown} attribute is an implementation of {@link Attribute} and
 * is used if an attribute can not be decoded. This is required if an attribute
 * is unknown, the error response contains this attribute.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class Unknown implements Attribute {
    private final int length;

    public Unknown() {
        this.length = 0;
    }

    @Override
    public AttributeType getType() {
        return STUNAttributeType.UNKNOWN_ATTRIBUTES;
    }

    @Override
    public int getLength() {
        return this.length;
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        // TODO implement
        throw new RuntimeException("Not yet implemented for this attribute.");
        // final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        // final DataOutputStream dout = new DataOutputStream(bout);
        //
        // final int paddingSize =
        // this.calculatePaddingBytes(this.unknownAttributeTypes);
        // if (paddingSize != 0) {
        // final byte[] paddingBits = new byte[paddingSize];
        // dout.write(paddingBits);
        // }
        //
        // dout.write(this.username);
        //
        // out.write(bout.toByteArray());
        // out.flush();
    }

    /**
     * Creates a {@link Unknown} from the given encoded attribute and header.
     * 
     * @param encoded
     *            the encoded {@link Unknown} attribute
     * @param header
     *            the attribute header
     * @return the {@link Unknown} of the given encoding
     * @throws IOException
     *             if an I/O exception occurs
     * @throws MessageFormatException
     *             if the encoded {@link Unknown} is malformed
     */
    public static Unknown fromBytes(final byte[] encoded, final AttributeHeader header) throws IOException {
        return new Unknown();
    }
}
