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
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;

/**
 * Used to export remote objects using a {@link RMIRelaySocketFactory} both as
 * {@link RMISocketFactory} and {@link RMIServerSocketFactory}
 * 
 * @author thomas zink, daniel maier
 * 
 */
public final class RelayRemoteObject extends UnicastRemoteObject {

    private static final long serialVersionUID = -3925843416954100102L;

    /*
     * Dummy constructor to call super constructor with single custom RMI
     * factory (same for client and server)
     */
    private RelayRemoteObject(int port, RMIRelaySocketFactory fac) throws RemoteException {
        super(port, fac, fac);
    }

    /**
     * Creates and exports a new {@link RelayRemoteObject} object using an
     * anonymous port.
     * 
     * @param relayServerEndpoint
     *            endpoint of the relay server on that it is waiting for new
     *            control connections
     * @throws RemoteException
     *             if failed to export object
     */
    protected RelayRemoteObject(InetSocketAddress relayServerEndpoint) throws RemoteException {
        this(0, new RMIRelaySocketFactory(relayServerEndpoint));
    }

    /**
     * Creates and exports a new {@link RelayRemoteObject} object. The supplied
     * port is the local port of the control connection to the relay server.
     * 
     * @param port
     *            the local port of the control connection to the relay server
     * @param relayServerEndpoint
     *            endpoint of the relay server on that it is waiting for new
     *            control connections
     * @throws RemoteException
     *             if failed to export object
     */
    protected RelayRemoteObject(int port, InetSocketAddress relayServerEndpoint)
            throws RemoteException {
        this(port, new RMIRelaySocketFactory(relayServerEndpoint));
    }

    /**
     * Exports the remote object to make it ready to establish connections via
     * connection reversal.
     * 
     * @param obj
     *            the remote object to be exported
     * @param port
     *            the local port of the control connection to the relay server
     * @param relayServerEndpoint
     *            endpoint of the relay server on that it is waiting for new
     *            control connections
     * @return remote object stub
     * @throws RemoteException
     *             if failed to export object
     */
    public static Remote exportObject(Remote obj, int port, InetSocketAddress relayServerEndpoint)
            throws RemoteException {
        RMIRelaySocketFactory fac = new RMIRelaySocketFactory(relayServerEndpoint);
        return UnicastRemoteObject.exportObject(obj, port, fac, fac);
    }

}
