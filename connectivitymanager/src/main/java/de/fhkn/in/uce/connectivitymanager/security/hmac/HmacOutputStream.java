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
package de.fhkn.in.uce.connectivitymanager.security.hmac;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Key;

import javax.crypto.Mac;

/**
 * The {@link HmacOutputStream} updates the HMAC calculation with the bits going
 * through the stream. When calling {@code flush} first the fixed length HMAC is
 * written to the stream before the data is transmitted. As hash algorithm SHA1
 * is used which produces with the given key a 160 bit long checksum.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class HmacOutputStream extends FilterOutputStream {
    private static final String HMAC_ALGORITHM = "HmacSHA1"; //$NON-NLS-1$
    private final Mac mac;
    private final ByteArrayOutputStream buffer;

    /**
     * Creates a {@link HmacOutputStream} object. The used hash algorithm is
     * SHA1. The HMAC is calculated with the given key.
     * 
     * @param out
     *            the {@link OutputStream} which is wrapped
     * @param key
     *            the {@link Key} for calculating the HMAC
     * @throws Exception
     *             if there is a problem with initializing the HMAC computation
     */
    public HmacOutputStream(final OutputStream out, final Key key) throws Exception {
        super(out);
        this.mac = Mac.getInstance(HMAC_ALGORITHM);
        this.mac.init(key);
        this.buffer = new ByteArrayOutputStream();
    }

    @Override
    public void close() throws IOException {
        this.out.close();
    }

    /**
     * Writes first the calculated HMAC with its fixed length to the wrapped
     * {@link OutputStream}. After that the data is written to the
     * {@link OutputStream}.
     */
    public void flush() throws IOException {
        // write calculated hmac with fixed length
        this.out.write(this.mac.doFinal());
        // write data from buffer
        this.buffer.writeTo(this.out);

    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.mac.update(b, off, len);
        this.buffer.write(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.mac.update(b);
        this.buffer.write(b);
    }

    @Override
    public void write(int b) throws IOException {
        this.mac.update((byte) b);
        this.buffer.write(b);
    }
}
