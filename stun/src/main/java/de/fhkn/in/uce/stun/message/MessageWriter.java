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
package de.fhkn.in.uce.stun.message;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Helper class to write {@link Message} messages synchronized to an output
 * stream.
 * 
 * @author Daniel Maier, Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class MessageWriter {
    private final OutputStream out;

    /**
     * Creates a new {@link MessageWriter}.
     * 
     * @param out
     *            the output stream to that messages should be written to
     */
    public MessageWriter(final OutputStream out) {
        this.out = out;
    }

    /**
     * Writes the given message synchronized on the intrinsic lock of this
     * {@link MessageWriter} object to the output stream.
     * 
     * @param message
     *            the {@link Message} to be written
     * @throws IOException
     *             if an I/O error occurs
     */
    public synchronized void writeMessage(final Message message) throws IOException {
        message.writeTo(this.out);
    }
}
