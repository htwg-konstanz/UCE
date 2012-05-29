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

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Uce attribute to represent a unique string username.
 * 
 * @author Daniel Maier
 * 
 */
public final class UniqueUserName implements UceAttribute {

    private static final String STRING_ENCODING = "UTF-8";
    private static final int MAX_USERNAME_BYTES = 48;
    private final String uniqueUserName;
    private final byte[] uniqueUserNameBytes;

    /**
     * Creates a new {@link UniqueUserName}.
     * 
     * @param uniqueUserName
     *            the string representation of the unique username
     * @throws UnsupportedEncodingException
     *             if the charset (UTF-8) for the string encoding is not
     *             supported
     * @throws IllegalArgumentException
     *             if the username is too long (maximum 48 bytes as encoded
     *             UTF-8 string)
     */
    public UniqueUserName(String uniqueUserName) throws UnsupportedEncodingException {
        this.uniqueUserName = uniqueUserName;
        uniqueUserNameBytes = encodeUniqueUsername(uniqueUserName);
        if (uniqueUserName.length() > MAX_USERNAME_BYTES) {
            throw new IllegalArgumentException("Username encoded with " + STRING_ENCODING
                    + "must not be larger than " + MAX_USERNAME_BYTES + ", but was "
                    + uniqueUserName.length());
        }
    }

    private static byte[] encodeUniqueUsername(String uniqueUserName)
            throws UnsupportedEncodingException {
        return uniqueUserName.getBytes(STRING_ENCODING);
    }

    public UceAttributeType getType() {
        return CommonUceAttributeType.UNIQUE_USER_NAME;
    }

    public int getLength() {
        return uniqueUserNameBytes.length;
    }

    public String getUniqueUserName() {
        return uniqueUserName;
    }

    public void writeTo(OutputStream out) throws IOException {
        out.write(uniqueUserNameBytes);
    }

    /**
     * Decodes a unique username
     * 
     * @param encoded
     *            the encoded username
     * @param header
     *            the header of the attribute
     * @return the decoded unique username
     * @throws IOException
     *             if an I/O error occurs
     */
    static UniqueUserName fromBytes(byte[] encoded, UceAttributeHeader header) throws IOException {
        return new UniqueUserName(new String(encoded, STRING_ENCODING));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((uniqueUserName == null) ? 0 : uniqueUserName.hashCode());
        result = prime * result + Arrays.hashCode(uniqueUserNameBytes);
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
        if (!(obj instanceof UniqueUserName)) {
            return false;
        }
        UniqueUserName other = (UniqueUserName) obj;
        if (uniqueUserName == null) {
            if (other.uniqueUserName != null) {
                return false;
            }
        } else if (!uniqueUserName.equals(other.uniqueUserName)) {
            return false;
        }
        if (!Arrays.equals(uniqueUserNameBytes, other.uniqueUserNameBytes)) {
            return false;
        }
        return true;
    }
}
