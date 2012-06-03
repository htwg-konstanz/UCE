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

import java.util.UUID;

/**
 * Helper class to encode a UUID as byte array and decode a byte array
 * as a UUID.
 * @author Daniel Maier
 *
 */
final class UUIDCoder {

    /**
     * Converts a UUID to a byte array.
     * @param uuid the UUID to be converted.
     * @return the UUID as byte array.
     */
	static final byte[] asByteArray(UUID uuid) {
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        byte[] buffer = new byte[16];

        for (int i = 0; i < 8; i++) {
            buffer[i] = (byte) (msb >>> 8 * (7 - i));
        }
        for (int i = 8; i < 16; i++) {
            buffer[i] = (byte) (lsb >>> 8 * (7 - i));
        }

        return buffer;

    }

    /**
     * Converts a byte array to a UUID.
     * @param byteArray the byte array to be converted.
     * @return the UUID.
     */
    static final UUID toUUID(byte[] byteArray) {
        long msb = 0;
        long lsb = 0;
        for (int i = 0; i < 8; i++)
            msb = (msb << 8) | (byteArray[i] & 0xff);
        for (int i = 8; i < 16; i++)
            lsb = (lsb << 8) | (byteArray[i] & 0xff);
        UUID result = new UUID(msb, lsb);

        return result;
    }
}
