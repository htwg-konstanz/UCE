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
 * Implementation of {@link Attribute} to represent the SOFTWARE attribute which
 * is defined in RFC 5389. This is a comprehenson-optional attribute.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class Software implements Attribute {
    private static final String STRING_ENCODING = "UTF-8"; //$NON-NLS-1$
    private static final int MAX_DESCRIPTION_BYTES = 763;
    private final byte[] description;

    /**
     * Creates a new {@link Software} object.
     * 
     * @param description
     *            the description of the software, should include manufacturer
     *            and version
     * @throws UnsupportedEncodingException
     *             if the {@code description} could not be encoded
     */
    public Software(final String description) throws UnsupportedEncodingException {
        this.description = this.encodeDescription(description);
        if (this.description.length > MAX_DESCRIPTION_BYTES) {
            throw new IllegalArgumentException(
                    "Description encoded with " + STRING_ENCODING + "must not be larger than " //$NON-NLS-1$ //$NON-NLS-2$
                            + MAX_DESCRIPTION_BYTES + ", but was " + this.description.length); //$NON-NLS-1$
        }
    }

    private byte[] encodeDescription(final String toEncode) throws UnsupportedEncodingException {
        return toEncode.getBytes(STRING_ENCODING);
    }

    @Override
    public AttributeType getType() {
        return STUNAttributeType.SOFTWARE;
    }

    @Override
    public int getLength() {
        return this.description.length;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final DataOutputStream dout = new DataOutputStream(bout);

        final int paddingSize = this.calculatePaddingBytes(this.description);
        if (paddingSize != 0) {
            final byte[] paddingBits = new byte[paddingSize];
            dout.write(paddingBits);
        }

        dout.write(this.description);

        out.write(bout.toByteArray());
        out.flush();
    }

    private int calculatePaddingBytes(final byte[] desc) {
        int result = 0;
        final int modulo = desc.length % 4;
        if (modulo != 0) {
            result = 4 - modulo;
        }

        return result;
    }

    /**
     * Creates a {@link Software} from the given encoded attribute and header.
     * 
     * @param encoded
     *            the encoded {@link Software} attribute
     * @param header
     *            the attribute header
     * @return the {@link Software} of the given encoding
     * @throws IOException
     *             if an I/O exception occurs
     * @throws MessageFormatException
     *             if the encoded {@link Software} is malformed
     */
    public static Software fromBytes(final byte[] encoded, final AttributeHeader header) throws IOException {
        return new Software(new String(encoded, STRING_ENCODING));
    }
}
