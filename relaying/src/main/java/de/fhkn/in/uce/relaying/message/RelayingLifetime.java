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
package de.fhkn.in.uce.relaying.message;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import de.fhkn.in.uce.stun.attribute.Attribute;
import de.fhkn.in.uce.stun.attribute.AttributeType;

/**
 * Attribute for the lifetime of a binding. Is sent in refresh and allocation
 * requests.
 * 
 * @author Daniel Maier, Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class RelayingLifetime implements Attribute {
    private final int lifetime;

    /**
     * Creates a new {@link RelayingLifetime}.
     * 
     * @param lifetime
     *            the lifetime in seconds
     */
    public RelayingLifetime(int lifetime) {
        this.lifetime = lifetime;
    }

    @Override
    public AttributeType getType() {
        return RelayingAttributeType.LIFETIME;
    }

    @Override
    public int getLength() {
        return 4;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        DataOutputStream dout = new DataOutputStream(out);
        dout.writeInt(lifetime);
        dout.flush();
    }

    /**
     * Returns the lifetime.
     * 
     * @return the lifetime
     */
    public int getLifeTime() {
        return lifetime;
    }

    /**
     * Decodes a lifetime attribute.
     * 
     * @param encoded
     *            the encoded life time attribute
     * @return the decoded lifetime attribute
     * @throws IOException
     *             if an I/O error occurs
     */
    static Attribute fromBytes(byte[] encoded) throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(encoded);
        DataInputStream din = new DataInputStream(bin);
        int lifetime = din.readInt();
        return new RelayingLifetime(lifetime);
    }

}
