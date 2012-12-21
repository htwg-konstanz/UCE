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
package de.fhkn.in.uce.connectivitymanager.security.diffiehellman;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Implementation of the Diffie-Hellman Key Exchange according to RFC 2631. The
 * creation of {@link KeyExchange} does not start the key exchange. To do that
 * the corresponding method has to be called.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class KeyExchange {
    private final ParameterGenerator paramGenerator;
    private BigInteger sharedKey = BigInteger.ZERO;

    /**
     * Creates a {@link KeyExchange} object.
     */
    public KeyExchange() {
        this.paramGenerator = ParameterGeneratorImpl.getInstance();
    }

    /**
     * Starts the key exchange in client role. That means the required
     * parameters are created and sended to the server.
     * 
     * @param in
     *            the {@link InputStream} from the server
     * @param out
     *            the {@link OutputStream} to to server
     * @throws Exception
     *             if the key exchange failed
     */
    public void exchangeKeyClientRole(final InputStream in, final OutputStream out) throws Exception {
        BigInteger[] params = paramGenerator.generateDiffieHellmanParameters();
        BigInteger p = params[0];
        BigInteger g = params[1];
        this.writeBigInteger(p, out);
        this.writeBigInteger(g, out);
        this.calculatedSharedKey(in, out, g, p);
    }

    /**
     * Starts the key exchange in server role. No parameters are generated but
     * delivered from the client.
     * 
     * @param in
     *            the {@link InputStream} from the client
     * @param out
     *            the {@link OutputStream} to the client
     * @throws Exception
     *             if the key exchange failed
     */
    public void exchangeKeyServerRole(final InputStream in, final OutputStream out) throws Exception {
        BigInteger p = this.readBigInteger(in);
        BigInteger g = this.readBigInteger(in);
        this.calculatedSharedKey(in, out, g, p);
    }

    private void calculatedSharedKey(final InputStream in, final OutputStream out, final BigInteger g,
            final BigInteger p) throws Exception {
        BigInteger privateKey = BigInteger.valueOf((new SecureRandom().nextInt(Integer.MAX_VALUE)));
        BigInteger privateKeyModPow = g.modPow(privateKey, p);
        this.writeBigInteger(privateKeyModPow, out);
        BigInteger privateKeyModPowOther = this.readBigInteger(in);
        this.sharedKey = privateKeyModPowOther.modPow(privateKey, p);
    }

    private void writeBigInteger(final BigInteger toWrite, final OutputStream out) throws Exception {
        ObjectOutputStream objectOutput = new ObjectOutputStream(out);
        byte[] bigIntBytes = toWrite.toByteArray();
        objectOutput.writeInt(bigIntBytes.length);
        objectOutput.write(bigIntBytes);
        objectOutput.flush();
    }

    private BigInteger readBigInteger(final InputStream in) throws Exception {
        ObjectInputStream objectInput = new ObjectInputStream(in);
        int lengthOfBigInt = objectInput.readInt();
        byte[] bigIntBytes = new byte[lengthOfBigInt];
        objectInput.readFully(bigIntBytes);
        return new BigInteger(bigIntBytes);
    }

    /**
     * Returns the symmetric key as a {@link SecretKey}.
     * 
     * @param algorithm
     *            the name of the secret-key algorithm to be associated with the
     *            given key material e.g. AES
     * @return the {@link SecretKey} which is a symmetric key
     */
    public SecretKey getSharedKey(final String algorithm) {
        return new SecretKeySpec(this.sharedKey.toByteArray(), algorithm);
    }
}
