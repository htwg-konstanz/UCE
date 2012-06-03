/*
    Copyright (c) 2012 Thomas Zink, 

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
package de.fhkn.in.uce.messages;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Uce attribute to represent a unique id (implemented as {@link UUID}).
 * 
 * @author Daniel Maier
 * 
 */
public final class UniqueId implements UceAttribute {

    private final UUID id;

    /**
     * Creates a new {@link UniqueId}.
     * 
     * @param id
     *            the id that is represented by this {@link UniqueId}
     */
    public UniqueId(UUID id) {
        this.id = id;
    }

    public UceAttributeType getType() {
        return CommonUceAttributeType.UNIQUE_ID;
    }

    public int getLength() {
        return UUIDCoder.asByteArray(id).length;
    }

    public void writeTo(OutputStream out) throws IOException {
        out.write(UUIDCoder.asByteArray(id));
    }

    /**
     * Returns the id that is represented by this {@link UniqueId}.
     * 
     * @return the id that is represented by this {@link UniqueId}
     */
    public UUID getId() {
        return id;
    }

    /**
     * Decodes an encoded {@link UniqueId}.
     * 
     * @param encoded
     *            the value of the {@link UniqueId} encoded as a byte array
     * @param header
     *            the header of the encoded {@link UniqueId}
     * @return the decoded {@link UniqueId}
     */
    static UceAttribute fromBytes(byte[] encoded, UceAttributeHeader header) {
        return new UniqueId(UUIDCoder.toUUID(encoded));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        if (!(obj instanceof UniqueId)) {
            return false;
        }
        UniqueId other = (UniqueId) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

}
