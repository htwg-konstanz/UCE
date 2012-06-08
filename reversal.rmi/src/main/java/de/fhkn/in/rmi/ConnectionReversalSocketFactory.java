/**
 * Copyright (C) 2011 Thomas Zink
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * 
 */
package de.fhkn.in.rmi;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.UUID;

import de.fhkn.in.uce.reversal.source.ConnectionReversalSource;
import de.fhkn.in.uce.reversal.target.ConnectionReversalServerSocket;

/**
 * @author zink
 *
 */
public final class ConnectionReversalSocketFactory implements
		RMIClientSocketFactory, RMIServerSocketFactory, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3870797984430688560L;
	
	private final UUID id;
    private final InetSocketAddress mediatorSocketAddress;

    public ConnectionReversalSocketFactory(InetSocketAddress mediatorSocketAddress) {
    	id = UUID.randomUUID();
    	this.mediatorSocketAddress = mediatorSocketAddress;
    }
    
    
	/* (non-Javadoc)
	 * @see java.rmi.server.RMIServerSocketFactory#createServerSocket(int)
	 */
	public ServerSocket createServerSocket(int arg0) throws IOException {
		ConnectionReversalServerSocket serverSocket = new ConnectionReversalServerSocket(id, mediatorSocketAddress);
		serverSocket.register();
		return serverSocket;
	}

	/* (non-Javadoc)
	 * @see java.rmi.server.RMIClientSocketFactory#createSocket(java.lang.String, int)
	 */
	public Socket createSocket(String arg0, int arg1) throws IOException {
		//ConnectionReversalSource source = new ConnectionReversalSource(id.toString(), mediatorSocketAddress.getHostName(), mediatorSocketAddress.getPort());
		ConnectionReversalSource source = new ConnectionReversalSource(mediatorSocketAddress.getHostName(), mediatorSocketAddress.getPort());
		Socket socket;
		try {
			socket = source.connect(id.toString());
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
		return socket;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime
				* result
				+ ((mediatorSocketAddress == null) ? 0 : mediatorSocketAddress
						.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ConnectionReversalSocketFactory)) {
			return false;
		}
		ConnectionReversalSocketFactory other = (ConnectionReversalSocketFactory) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (mediatorSocketAddress == null) {
			if (other.mediatorSocketAddress != null) {
				return false;
			}
		} else if (!mediatorSocketAddress.equals(other.mediatorSocketAddress)) {
			return false;
		}
		return true;
	}
}
