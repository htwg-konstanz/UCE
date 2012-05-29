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
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 * {@link UceAttribute} that holds a list of strings.
 * 
 * @author Daniel Maier
 * 
 */
public final class StringList implements UceAttribute {

    private static final String STRING_ENCODING = "UTF-8";
    private final List<String> strings;
    private final byte[] stringBytes;

    /**
     * Creates a new {@link StringList}.
     * 
     * @param strings
     *            list of strings that should be hold by this {@link StringList}
     * @throws UnsupportedEncodingException
     *             if the charset (UTF-8) to encode the strings is not supported
     */
    public StringList(List<String> strings) throws UnsupportedEncodingException {
        this(strings, encodeStrings(strings));
    }

    /**
     * Creates a new {@link StringList}.
     * 
     * @param strings
     *            list of strings that should be hold by this {@link StringList}
     * @param stringBytes
     *            the list of strings encoded as UTF-8
     * @throws NullPointerException
     *             if one of the arguments is null
     */
    private StringList(List<String> strings, byte[] stringBytes) {
        if (strings == null || stringBytes == null) {
            throw new NullPointerException();
        }
        this.strings = strings;
        this.stringBytes = stringBytes;
    }

    /**
     * Encodes the given strings in UTF-8 as a byte array.
     * 
     * @param strings
     *            the strings to be encoded
     * @return the encoded strings
     * @throws UnsupportedEncodingException
     *             if the charset (UTF-8) to encode the strings is not supported
     */
    private static byte[] encodeStrings(List<String> strings) throws UnsupportedEncodingException {
        // format: 8 bits for length, then UTF8 encoded String
        int maxStringLength = 255;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);
        for (String s : strings) {
            byte[] stringBytes = s.getBytes(STRING_ENCODING);
            int length = (stringBytes.length > maxStringLength) ? maxStringLength
                    : stringBytes.length;
            try {
                dout.writeByte(length);
                dout.write(stringBytes, 0, length);
            } catch (IOException e) {
                // can't happen because underlying stream is
                // ByteArrayOutputStream
                throw new AssertionError();
            }
        }
        return bout.toByteArray();
    }

    public UceAttributeType getType() {
        return CommonUceAttributeType.STRING_LIST;
    }

    public int getLength() {
        return stringBytes.length;
    }

    public void writeTo(OutputStream out) throws IOException {
        out.write(stringBytes);
    }

    /**
     * Returns the list of strings that are hold by this {@link StringList}.
     * 
     * @return the list of strings of this {@link StringList}
     */
    public List<String> getStrings() {
        return strings;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(stringBytes);
        result = prime * result + ((strings == null) ? 0 : strings.hashCode());
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
        if (!(obj instanceof StringList)) {
            return false;
        }
        StringList other = (StringList) obj;
        if (!Arrays.equals(stringBytes, other.stringBytes)) {
            return false;
        }
        if (strings == null) {
            if (other.strings != null) {
                return false;
            }
        } else if (!strings.equals(other.strings)) {
            return false;
        }
        return true;
    }

    /**
     * Decodes a list of strings.
     * 
     * @param encoded
     *            the encoded {@link StringList} (without header)
     * @param header
     *            the header of this attribute
     * @return the decoded attribute
     * @throws IOException
     *             if an I/O error occurs
     */
    static UceAttribute fromBytes(byte[] encoded, UceAttributeHeader header) throws IOException {
        List<String> strings = new Vector<String>();
        ByteArrayInputStream bin = new ByteArrayInputStream(encoded);
        DataInputStream din = new DataInputStream(bin);
        while (bin.available() > 0) {
            int length = din.readUnsignedByte();
            byte[] stringBytes = new byte[length];
            din.readFully(stringBytes);
            strings.add(new String(stringBytes, STRING_ENCODING));
        }
        return new StringList(strings, encoded);
    }

}
