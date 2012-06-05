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
package de.fhkn.in.uce.relay.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * ServerSocket implementation of a relayed server, that is actually the
 * {@link RelayClient}, which creates allocations on a remote relay server.
 * 
 * @author thomas zink, daniel maier
 */
public class RelayServerSocket extends ServerSocket {
	private final RelayClient relayClient;
	private InetSocketAddress peerAddress = null; 
	
	/**
	 * Creates a new {@link RelayServerSocket} using the specified
	 * Relay server.
	 * 
	 * @param	hostname  the relay server host name
     * @param	port  the relay server port number
	 */
	public RelayServerSocket(String hostname, int port) throws IOException {
		this(new InetSocketAddress(hostname, port));
	}
	
	/**
	 * Creates a new {@link RelayServerSocket} using the specified
	 * Relay server socket address.
	 * 
	 * @param relaySocketAddress
	 * @throws IOException
	 */
	public RelayServerSocket(InetSocketAddress relaySocketAddress) throws IOException {
		this(new RelayClient(relaySocketAddress));
	}
	
	/**
     * Creates a new {@link RelayServerSocket}.
     * 
     * @param relayClient
     *            the {@link RelayClient} that is used to communicate with the
     *            rely server
     * @throws IOException
     *             if an I/O error occurs
     */
    public RelayServerSocket(RelayClient relayClient) throws IOException {
        this.relayClient = relayClient;
        this.peerAddress = this.relayClient.createAllocation();
    }
    
    public InetSocketAddress getPeerAddress() {
    	return this.peerAddress;
    }
    /**
     * Creates a new allocation on the relay server by using the given
     * {@link #relayClient}.
     * 
     * @return the public endpoint of the allocation on the relay server
     * @throws IOException
     *             if an I/O error occurs
     */
    /*public InetSocketAddress createAllocation() throws IOException {
    	// TODO state check
        return relayClient.createAllocation();
    }*/

    /**
     * Returns sockets that are connected to the relay server to relay data
     * between the client and peers.
     */
    @Override
    public Socket accept() throws IOException {
        try {
            return relayClient.accept();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    /**
     * Discards the relay allocation on the relay server.
     */
    @Override
    public void close() throws IOException {
        this.relayClient.discardAllocation();
        super.close();
    }
}
