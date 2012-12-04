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
/**
 * 
 */
package de.fhkn.in.uce.core.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author thomas zink, daniel maier
 */
public final class MockitoSocket {
	private final Socket socketMock;
	private final ByteArrayOutputStream os;

	public MockitoSocket() throws IOException {
		socketMock = mock(Socket.class);
		os = new ByteArrayOutputStream();
		when(socketMock.getOutputStream())
			.thenReturn(os);
	}

	public MockitoSocket setClosed(boolean closed) {
		when(socketMock.isClosed())
			.thenReturn(closed);
		return this;
	}

	public MockitoSocket setConnected(boolean connected) {
		when(socketMock.isConnected())
			.thenReturn(connected);
		return this;
	}

	public MockitoSocket setRemoteSocketAddress(
			InetSocketAddress remoteSocketAddress) {
		when(socketMock.getRemoteSocketAddress())
			.thenReturn(remoteSocketAddress);
		when(socketMock.getInetAddress())
			.thenReturn(remoteSocketAddress.getAddress());
		when(socketMock.getPort())
			.thenReturn(remoteSocketAddress.getPort());
		return this;
	}

	public MockitoSocket setLocalSocketAddress(
			InetSocketAddress localSocketAddress) {
		when(socketMock.getLocalSocketAddress())
			.thenReturn(localSocketAddress);
		when(socketMock.getLocalAddress())
			.thenReturn(localSocketAddress.getAddress());
		when(socketMock.getLocalPort())
			.thenReturn(localSocketAddress.getPort());
		return this;
	}

	public MockitoSocket setInputStreamData(byte[] data) throws IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(data);
		when(socketMock.getInputStream())
			.thenReturn(is);
		return this;
	}
	
	public byte[] getOutputStreamData() {
		return os.toByteArray();
	}
	
	public InputStream getOutputStreamDataAsInputStream() {
		return new ByteArrayInputStream(os.toByteArray());
	}

	public Socket getSocket() {
		return socketMock;
	}
}
