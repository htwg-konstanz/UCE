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
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

import de.fhkn.in.uce.connectivitymanager.manager.ConnectivityManager;

public class UCESocket extends Socket {
    protected ConnectivityManager connectivityManager;
    // TODO socket should only be available through connectivity manager to
    // support a switchable socket, socket methods must be delegated to the
    // switchable socket. Is that correct? Delegate in subclasses?
    protected Socket delegate;

    protected UCESocket() {
        super();
    }

    protected void establishConnection() {
        this.delegate = this.connectivityManager.establishConnection();
    }

    public void connect() {
        this.establishConnection();
    }

    @Override
    public void connect(SocketAddress arg0, int arg1) throws IOException {
        this.establishConnection();
    }

    @Override
    public void connect(SocketAddress arg0) throws IOException {
        this.establishConnection();
    }

    @Override
    public void bind(SocketAddress bindpoint) throws IOException {
        throw new UnsupportedOperationException("Not supported in UCE context"); //$NON-NLS-1$
    }

    @Override
    public synchronized void close() throws IOException {
        this.delegate.close();
    }

    @Override
    public SocketChannel getChannel() {
        return this.delegate.getChannel();
    }

    @Override
    public InetAddress getInetAddress() {
        return this.delegate.getInetAddress();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return this.delegate.getInputStream();
    }

    @Override
    public boolean getKeepAlive() throws SocketException {
        return this.delegate.getKeepAlive();
    }

    @Override
    public InetAddress getLocalAddress() {
        return this.delegate.getLocalAddress();
    }

    @Override
    public int getLocalPort() {
        return this.delegate.getLocalPort();
    }

    @Override
    public SocketAddress getLocalSocketAddress() {
        return this.delegate.getLocalSocketAddress();
    }

    @Override
    public boolean getOOBInline() throws SocketException {
        return this.delegate.getOOBInline();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return this.delegate.getOutputStream();
    }

    @Override
    public int getPort() {
        return this.delegate.getPort();
    }

    @Override
    public synchronized int getReceiveBufferSize() throws SocketException {
        return this.delegate.getReceiveBufferSize();
    }

    @Override
    public SocketAddress getRemoteSocketAddress() {
        return this.delegate.getRemoteSocketAddress();
    }

    @Override
    public boolean getReuseAddress() throws SocketException {
        return this.delegate.getReuseAddress();
    }

    @Override
    public synchronized int getSendBufferSize() throws SocketException {
        return this.delegate.getSendBufferSize();
    }

    @Override
    public int getSoLinger() throws SocketException {
        return this.delegate.getSoLinger();
    }

    @Override
    public synchronized int getSoTimeout() throws SocketException {
        return this.delegate.getSoTimeout();
    }

    @Override
    public boolean getTcpNoDelay() throws SocketException {
        return this.delegate.getTcpNoDelay();
    }

    @Override
    public int getTrafficClass() throws SocketException {
        return this.delegate.getTrafficClass();
    }

    @Override
    public boolean isBound() {
        return this.delegate.isBound();
    }

    @Override
    public boolean isClosed() {
        return this.delegate.isClosed();
    }

    @Override
    public boolean isConnected() {
        return this.delegate.isConnected();
    }

    @Override
    public boolean isInputShutdown() {
        return this.delegate.isInputShutdown();
    }

    @Override
    public boolean isOutputShutdown() {
        return this.delegate.isOutputShutdown();
    }

    @Override
    public void sendUrgentData(int data) throws IOException {
        this.delegate.sendUrgentData(data);
    }

    @Override
    public void setKeepAlive(boolean on) throws SocketException {
        this.delegate.setKeepAlive(on);
    }

    @Override
    public void setOOBInline(boolean on) throws SocketException {
        this.delegate.setOOBInline(on);
    }

    @Override
    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        this.delegate.setPerformancePreferences(connectionTime, latency, bandwidth);
    }

    @Override
    public synchronized void setReceiveBufferSize(int size) throws SocketException {
        this.delegate.setReceiveBufferSize(size);
    }

    @Override
    public void setReuseAddress(boolean on) throws SocketException {
        this.delegate.setReuseAddress(on);
    }

    @Override
    public synchronized void setSendBufferSize(int size) throws SocketException {
        this.delegate.setSendBufferSize(size);
    }

    @Override
    public void setSoLinger(boolean on, int linger) throws SocketException {
        this.delegate.setSoLinger(on, linger);
    }

    @Override
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        this.delegate.setSoTimeout(timeout);
    }

    @Override
    public void setTcpNoDelay(boolean on) throws SocketException {
        this.delegate.setTcpNoDelay(on);
    }

    @Override
    public void setTrafficClass(int tc) throws SocketException {
        this.delegate.setTrafficClass(tc);
    }

    @Override
    public void shutdownInput() throws IOException {
        this.delegate.shutdownInput();
    }

    @Override
    public void shutdownOutput() throws IOException {
        this.delegate.shutdownOutput();
    }

    @Override
    public String toString() {
        return this.delegate.toString();
    }
}
