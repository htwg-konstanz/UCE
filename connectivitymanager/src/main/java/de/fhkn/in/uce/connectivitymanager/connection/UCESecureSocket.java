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
package de.fhkn.in.uce.connectivitymanager.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SocketChannel;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.connectivitymanager.connection.configuration.ConnectionConfiguration;
import de.fhkn.in.uce.connectivitymanager.connection.configuration.DefaultConnectionConfiguration;
import de.fhkn.in.uce.connectivitymanager.manager.ConnectionEstablishment;
import de.fhkn.in.uce.connectivitymanager.manager.ConnectivityManager;
import de.fhkn.in.uce.connectivitymanager.security.diffiehellman.KeyExchange;
import de.fhkn.in.uce.connectivitymanager.security.hmac.HmacInputStream;
import de.fhkn.in.uce.connectivitymanager.security.hmac.HmacOutputStream;

final class UCESecureSocket extends UCESocket {
    private final Logger logger = LoggerFactory.getLogger(UCESecureSocket.class);
    private static final String AES_ALGORITHM_NAME = "AES"; //$NON-NLS-1$
    private Cipher encrypt;
    private Cipher decrypt;
    private KeyExchange keyExchange;

    /**
     * schreiben ob connection bereits hergestellt wird oder nicht
     * 
     * @param targetId
     * @param config
     * @param connectionEstablishment
     */
    public UCESecureSocket(final String targetId, final ConnectionConfiguration config,
            final ConnectionEstablishment connectionEstablishment) {
        this.connectivityManager = new ConnectivityManager(targetId, connectionEstablishment);
        this.establishConnection();
        try {
            this.initializeAndStartKeyExhange();
            this.initializeEncryptCipher();
            this.initializeDecryptCipher();
        } catch (Exception e) {
            logger.error("Exception while creating a secure socket.", e); //$NON-NLS-1$
            throw new RuntimeException("Exception while creating a secure socket.", e); //$NON-NLS-1$
        }
    }

    public UCESecureSocket(final String targetId, final ConnectionEstablishment connectionEstablishment) {
        this(targetId, DefaultConnectionConfiguration.getInstance(), connectionEstablishment);
    }

    private void initializeAndStartKeyExhange() throws Exception {
        this.keyExchange = new KeyExchange();
        this.keyExchange.exchangeKeyClientRole(this.delegate.getInputStream(), this.delegate.getOutputStream());
    }

    private void initializeEncryptCipher() throws Exception {
        this.encrypt = Cipher.getInstance(AES_ALGORITHM_NAME);
        this.encrypt.init(Cipher.ENCRYPT_MODE, this.keyExchange.getSharedKey(AES_ALGORITHM_NAME));
    }

    private void initializeDecryptCipher() throws Exception {
        this.decrypt = Cipher.getInstance(AES_ALGORITHM_NAME);
        this.decrypt.init(Cipher.DECRYPT_MODE, this.keyExchange.getSharedKey(AES_ALGORITHM_NAME));
    }

    @Override
    public SocketChannel getChannel() {
        throw new UnsupportedOperationException("Not supported in secure UCE context"); //$NON-NLS-1$
    }

    @Override
    public InputStream getInputStream() throws IOException {
        final CipherInputStream cipherIn = new CipherInputStream(super.getInputStream(), this.decrypt);
        try {
            return new HmacInputStream(cipherIn, this.keyExchange.getSharedKey(AES_ALGORITHM_NAME));
        } catch (Exception e) {
            logger.error("Could not create hmac input stream.", e); //$NON-NLS-1$
            throw new IOException(e);
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        final CipherOutputStream cipherOut = new CipherOutputStream(super.getOutputStream(), this.encrypt);
        try {
            return new HmacOutputStream(cipherOut, this.keyExchange.getSharedKey(AES_ALGORITHM_NAME));
        } catch (Exception e) {
            logger.error("Could not create hmac output stream.", e); //$NON-NLS-1$
            throw new IOException(e);
        }
    }
}
