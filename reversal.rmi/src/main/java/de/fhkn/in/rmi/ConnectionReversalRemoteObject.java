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

import java.net.InetSocketAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author zink
 *
 */
public final class ConnectionReversalRemoteObject extends UnicastRemoteObject {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = -3561774719802751322L;

	/*
     * Dummy constructor to call super constructor with single custom RMI
     * factory (same for client and server)
     */
	private ConnectionReversalRemoteObject(int port, ConnectionReversalSocketFactory fac)
            throws RemoteException {
        super(port, fac, fac);
    }

    /**
     * Creates and exports a new ConnectionReversalRemoteObject object using an
     * anonymous port.
     * 
     * @param mediatorInetSocketAddress
     *            Register Endpoint of the mediator
     * @throws RemoteException
     *             if failed to export object
     */
    protected ConnectionReversalRemoteObject(InetSocketAddress mediatorInetSocketAddress)
            throws RemoteException {
        this(0, new ConnectionReversalSocketFactory(mediatorInetSocketAddress));
    }

    /**
     * Creates and exports a new ConnectionReversalRemoteObject object. The supplied
     * port is ignored in current implementation. Instead an anonymous port is
     * chosen.
     * 
     * @param port
     *            ignored in current implementation
     * @param mediatorInetSocketAddress
     *            IP Socket Address of the mediator
     * @throws RemoteException
     *             if failed to export object
     */
    protected ConnectionReversalRemoteObject(int port, InetSocketAddress mediatorInetSocketAddress)
            throws RemoteException {
        this(port, new ConnectionReversalSocketFactory(mediatorInetSocketAddress));
    }

    /**
     * Exports the remote object to make it ready to establish connections via
     * connection reversal.
     * 
     * @param obj
     *            the remote object to be exported
     * @param port
     *            the port to export the given object on (ignored in current
     *            implementation)
     * @param mediatorInetSocketAddress
     *            Register Endpoint of the mediator
     * @return remote object stub
     * @throws RemoteException
     *             if the export fails
     */
    public static Remote exportObject(Remote obj, int port,
            InetSocketAddress mediatorInetSocketAddress) throws RemoteException {
    	ConnectionReversalSocketFactory fac = new ConnectionReversalSocketFactory(
                mediatorInetSocketAddress);
        return UnicastRemoteObject.exportObject(obj, port, fac, fac);
    }

}
