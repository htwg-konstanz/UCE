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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.Key;
import java.util.Arrays;

import javax.crypto.Mac;

/**
 * The {@link HmacInputStream} reads from an {@link InputStream} and updates the
 * HMAC calculation. As hashing algorithm SHA1 is used. It is important to know
 * that first all data is read from the {@link InputStream}. This is necessary
 * to compare the transmitted HMAC with the calculated HMAC. The first 20 bytes
 * of the {@link InputStream} are interpreted as the transmitted HMAC.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class HmacInputStream extends FilterInputStream {
    private static final String HMAC_ALGORITHM = "HmacSHA1"; //$NON-NLS-1$
    private static final int HMAC_LENGTH = 20;
    private final Mac mac;
    private final ByteArrayInputStream buffer;

    /**
     * Creates a {@link HmacInputStream} object. The used hash algorithm is
     * SHA1. The HMAC is calculated with the given key.
     * 
     * @param in
     * @param key
     * @throws Exception
     */
    public HmacInputStream(final InputStream in, final Key key) throws Exception {
        super(in);
        this.mac = Mac.getInstance(HMAC_ALGORITHM);
        this.mac.init(key);
        this.buffer = this.processInputStream(in);
    }

    private ByteArrayInputStream processInputStream(final InputStream in) throws Exception {
        final byte[] receivedHmac = this.readHmac(in);
        final ByteArrayOutputStream outBuffer = this.readPayload(in);
        this.checkHmac(receivedHmac, this.mac.doFinal());
        return new ByteArrayInputStream(outBuffer.toByteArray());
    }

    private byte[] readHmac(final InputStream in) throws Exception {
        byte[] result = new byte[HMAC_LENGTH];
        final int hmacRead = in.read(result);
        this.checkHmacLength(hmacRead, result.length);
        return result;
    }

    private void checkHmacLength(final int readHmac, final int calculatedHmac) {
        if (HMAC_LENGTH != readHmac || HMAC_LENGTH != calculatedHmac) {
            throw new RuntimeException("The received hmac has wrong length"); //$NON-NLS-1$
        }
    }

    private ByteArrayOutputStream readPayload(final InputStream in) throws Exception {
        final ByteArrayOutputStream result = new ByteArrayOutputStream();
        int content;
        while ((content = in.read()) != -1) {
            this.mac.update((byte) content);
            result.write(content);
        }
        return result;
    }

    private void checkHmac(final byte[] expectedHmac, final byte[] actualHmac) {
        if (!Arrays.equals(actualHmac, expectedHmac)) {
            throw new RuntimeException("The HMACs are not equals: " + ByteBuffer.wrap(expectedHmac).getInt() + " != " //$NON-NLS-1$ //$NON-NLS-2$
                    + ByteBuffer.wrap(actualHmac).getInt());
        }
    }

    @Override
    public int read() throws IOException {
        return this.buffer.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return this.buffer.read(b, off, len);
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.buffer.read(b);
    }
}
