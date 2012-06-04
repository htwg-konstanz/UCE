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
package de.fhkn.in.rmi;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

import de.fhkn.in.uce.relay.client.RelayServerSocket;

/**
 * @author thomas zink
 *
 */
public class RMIRelaySocketFactory implements RMIClientSocketFactory,
		RMIServerSocketFactory, Serializable {
	
	private final InetSocketAddress relayServer;
	private RelayServerSocket clientSocket = null;
	private InetSocketAddress peerAddress = null;

	/**
	 * 
	 */
	private static final long serialVersionUID = -7707534555030453027L;

	/**
	 * Creates a {@link RelaySocketFactory} using the hostname and port of
	 * the relay server.
	 * 
	 * @param hostname  the relay server host name
	 * @param port  the relay server port
	 */
	public RMIRelaySocketFactory(String hostname, int port) {
		this(new InetSocketAddress(hostname, port));
	}
	
	/**
	 * Creates a {@link RelaySocketFactory} using a specified endpoint.
	 * 
	 * @param relayServerSocketAddress  socket address of the relay server
	 */
	public RMIRelaySocketFactory(InetSocketAddress relayServerSocketAddress) {
		this.relayServer = relayServerSocketAddress;
	}

	/* (non-Javadoc)
	 * @see java.rmi.server.RMIServerSocketFactory#createServerSocket(int)
	 */
	@Override
	public ServerSocket createServerSocket(int port) throws IOException {
		if (this.clientSocket == null) {
			this.clientSocket = new RelayServerSocket(relayServer);
			this.peerAddress = this.clientSocket.createAllocation();
		}
		return this.clientSocket;
	}

	/* (non-Javadoc)
	 * @see java.rmi.server.RMIClientSocketFactory#createSocket(java.lang.String, int)
	 */
	@Override
	public Socket createSocket(String host, int port) throws IOException { 
		return new Socket(peerAddress.getAddress(), peerAddress.getPort());
	}

}
