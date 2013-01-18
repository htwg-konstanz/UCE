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
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import de.fhkn.in.uce.stun.util.MessageFormatException;

/**
 * The {@link ChangeRequest} is the implementation of {@link Attribute} for the
 * CHANGE-REQUEST STUN attribute which is defined in RFC 5780.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class ChangeRequest implements Attribute {
    public static final int CHANGE_IP = 0x4;
    public static final int CHANGE_PORT = 0x2;
    public static final int CHANGE_IP_AND_PORT = 0x6;
    public static final int FLAGS_NOT_SET = 0x0;

    private final int length;
    private final int flag;

    /**
     * Creates a new {@link ChangeRequest} with the given flag. For detailed
     * information see RFC 5780.
     * 
     * @param flag
     *            the flag of the {@link ChangeRequest}
     */
    public ChangeRequest(final int flag) {
        checkFlagValidity(flag);
        this.flag = flag;
        this.length = 4;
    }

    /**
     * Creates a {@link ChangeRequest} with not set flags.
     */
    public ChangeRequest() {
        this(FLAGS_NOT_SET);
    }

    private static void checkFlagValidity(final int toCheck) {
        if (toCheck != FLAGS_NOT_SET && toCheck != CHANGE_IP && toCheck != CHANGE_PORT && toCheck != CHANGE_IP_AND_PORT) {
            throw new IllegalArgumentException("Invalid flag: " + toCheck); //$NON-NLS-1$
        }
    }

    /**
     * Returns the flag of the {@link ChangeRequest}.
     * 
     * @return the value of the flag.
     */
    public int getFlag() {
        return this.flag;
    }

    /**
     * Indicates whether the change IP flag is set or not.
     * 
     * @return true if the change IP flag is set, false else
     */
    public boolean isChangeIp() {
        return this.flag == CHANGE_IP || this.flag == CHANGE_IP_AND_PORT;
    }

    /**
     * Indicates whether the change port flag is set.
     * 
     * @return true if the change port flag is set, false else
     */
    public boolean isChangePort() {
        return this.flag == CHANGE_PORT || this.flag == CHANGE_IP_AND_PORT;
    }

    @Override
    public AttributeType getType() {
        return STUNAttributeType.CHANGE_REQUEST;
    }

    @Override
    public int getLength() {
        return this.length;
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final DataOutputStream dout = new DataOutputStream(bout);

        // flag
        dout.writeInt(this.flag);

        out.write(bout.toByteArray());
        out.flush();
    }

    /**
     * Creates a {@link ChangeRequest} from the given encoded attribute and
     * header.
     * 
     * @param encoded
     *            the encoded {@link ChangeRequest} attribute
     * @param header
     *            the attribute header
     * @return the {@link ChangeRequest} of the given encoding
     * @throws IOException
     *             if an I/O exception occurs
     * @throws MessageFormatException
     *             if the encoded {@link ChangeRequest} is malformed
     */
    public static ChangeRequest fromBytes(final byte[] encoded, final AttributeHeader header) throws IOException,
            MessageFormatException {
        final ByteArrayInputStream bin = new ByteArrayInputStream(encoded);
        final DataInputStream din = new DataInputStream(bin);

        final int flagBits = din.readInt();
        checkFlagValidity(flagBits);

        return new ChangeRequest(flagBits);
    }
}
