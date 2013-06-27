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
package de.fhkn.in.uce.plugininterface.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import de.fhkn.in.uce.stun.attribute.Attribute;
import de.fhkn.in.uce.stun.attribute.AttributeHeader;
import de.fhkn.in.uce.stun.attribute.AttributeType;
import de.fhkn.in.uce.stun.util.MessageFormatException;

public class NATTraversalTechniqueAttribute implements Attribute {
    private static final int LENGTH = 4;
    private final int encoded;

    public NATTraversalTechniqueAttribute(final int encoded) {
        this.encoded = encoded;
    }

    public int getEncoded() {
        return this.encoded;
    }

    @Override
    public AttributeType getType() {
        return NATSTUNAttributeType.NAT_TRAVERSAL_TECHNIQUE;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final DataOutputStream dout = new DataOutputStream(bout);
        dout.writeInt(this.encoded);
        out.write(bout.toByteArray());
        out.flush();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + encoded;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (null == obj) {
            return false;
        }
        if ((this instanceof NATTraversalTechniqueAttribute) && (obj instanceof NATTraversalTechniqueAttribute)) {
            NATTraversalTechniqueAttribute other = (NATTraversalTechniqueAttribute) obj;
            if (encoded != other.encoded) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    /**
     * Creates a {@link NATTraversalTechniqueAttribute} from the given encoding.
     *
     * @param encoded
     *            the encoded NAT traversal technique
     * @param header
     *            the attribute header
     * @return the {@link NATTraversalTechniqueAttribute} for the corresponding
     *         encoding
     * @throws IOException
     *             if the attribute can not be read
     * @throws MessageFormatException
     *             if the message is malformed
     */
    public static NATTraversalTechniqueAttribute fromBytes(final byte[] encoded, final AttributeHeader header)
            throws IOException, MessageFormatException {
        final ByteArrayInputStream bin = new ByteArrayInputStream(encoded);
        final DataInputStream din = new DataInputStream(bin);
        final int travTech = din.readInt();
        return new NATTraversalTechniqueAttribute(travTech);
    }
}
