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
package de.fhkn.in.uce.connectivitymanager.manager.target;

import java.net.Socket;
import java.util.concurrent.Callable;

final class ServerSocketTask implements Callable<Socket> {

    // private final String targetId;
    // private final NATTraversalTechnique natTraversalTechnique;
    // private final InetSocketAddress mediatorAddress;
    // private final Logger logger =
    // LoggerFactory.getLogger(ServerSocketTask.class);
    //
    // public ServerSocketTask(final String targetId, final
    // NATTraversalTechnique natTraversalTechnique,
    // final InetSocketAddress mediatorAddress) {
    // this.natTraversalTechnique = natTraversalTechnique;
    // this.targetId = targetId;
    // this.mediatorAddress = mediatorAddress;
    // }
    //
    @Override
    public Socket call() throws Exception {
        // this.registerAtMediator();
        // final Socket connectedSocket = this.createTarget();
        //        this.logger.info("Connection with {} created via {}.", this.targetId, //$NON-NLS-1$
        // this.natTraversalTechnique.getMetaData().getTraversalTechniqueName());
        // return connectedSocket;
        return null;
    }
    //
    // private void registerAtMediator() throws
    // ConnectionNotEstablishedException {
    // this.logger
    //                .debug("Registering {} via {}.", this.targetId, this.natTraversalTechnique.getMetaData().getTraversalTechniqueName()); //$NON-NLS-1$
    // this.natTraversalTechnique.registerTargetAtMediator(this.targetId,
    // this.mediatorAddress);
    // }
    //
    // private Socket createTarget() throws ConnectionNotEstablishedException {
    //        this.logger.debug("Creating target for {} via {} and waiting for incoming connections.", this.targetId, //$NON-NLS-1$
    // this.natTraversalTechnique.getMetaData().getTraversalTechniqueName());
    // return
    // this.natTraversalTechnique.createTargetSideConnection(this.targetId,
    // this.mediatorAddress);
    // }
}
