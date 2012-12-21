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
package de.fhkn.in.uce.directconnection.mediator;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.directconnection.message.DirectconnectionAttribute;
import de.fhkn.in.uce.mediator.connectionhandling.ConnectionRequest;
import de.fhkn.in.uce.mediator.connectionhandling.ConnectionRequestList;
import de.fhkn.in.uce.mediator.peerregistry.UserData;
import de.fhkn.in.uce.mediator.peerregistry.UserList;
import de.fhkn.in.uce.mediator.util.MediatorUtil;
import de.fhkn.in.uce.plugininterface.mediator.HandleMessage;
import de.fhkn.in.uce.plugininterface.message.NATTraversalTechniqueAttribute;
import de.fhkn.in.uce.stun.attribute.EndpointClass;
import de.fhkn.in.uce.stun.attribute.EndpointClass.EndpointCategory;
import de.fhkn.in.uce.stun.attribute.ErrorCode.STUNErrorCode;
import de.fhkn.in.uce.stun.attribute.Username;
import de.fhkn.in.uce.stun.attribute.XorMappedAddress;
import de.fhkn.in.uce.stun.header.STUNMessageClass;
import de.fhkn.in.uce.stun.header.STUNMessageMethod;
import de.fhkn.in.uce.stun.message.Message;
import de.fhkn.in.uce.stun.message.MessageStaticFactory;

/**
 * Handles custom connection request messages for direct connection. For every
 * connection request the public endpoint of the requested user is returned to
 * the source. Furthermore the target is asked to start the target-side
 * behavior.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
@ThreadSafe
public final class DirectconnConnRequestHandler implements HandleMessage {
    private static final Logger logger = LoggerFactory.getLogger(DirectconnConnRequestHandler.class);
    private final ConnectionRequestList connectionRequests = ConnectionRequestList.INSTANCE;
    private final MediatorUtil mediatorUtil = MediatorUtil.INSTANCE;
    private final UserList userList = UserList.INSTANCE;

    @Override
    public void handleMessage(final Message message, final Socket controlConnection) throws Exception {
        if (message.getMessageClass() == STUNMessageClass.REQUEST) {
            logger.debug("Handling connection request from {}", controlConnection.toString()); //$NON-NLS-1$
            this.handleConnectionRequest(message, controlConnection);
        } else if (message.getMessageClass() == STUNMessageClass.SUCCESS_RESPONSE) {
            logger.debug("Handling connection request response from {}", controlConnection.toString()); //$NON-NLS-1$
            this.handleConnectionRequestResponse(message, controlConnection);
        }
    }

    private void handleConnectionRequest(final Message message, final Socket controlConnection) throws Exception {
        this.mediatorUtil.checkForAttribute(message, Username.class);
        this.connectionRequests.putConnectionRequest(new ConnectionRequest(controlConnection, message));
        final String username = message.getAttribute(Username.class).getUsernameAsString();
        final UserData userData = this.userList.getUserDataByUserId(username);
        if (userData == null) {
            final String errorMessage = "User " + username + " not exists"; //$NON-NLS-1$ //$NON-NLS-2$
            this.sendFailureResponse(message, errorMessage, STUNErrorCode.BAD_REQUEST,
                    controlConnection.getOutputStream());
        } else {
            this.callTarget(userData, message);
        }
    }

    private void handleConnectionRequestResponse(final Message message, final Socket controlConnection)
            throws Exception {
        logger.debug("Handling connection request response"); //$NON-NLS-1$
        final ConnectionRequest connReq = this.connectionRequests.getConnectionRequest(new String(message.getHeader()
                .getTransactionId()));
        if (null == connReq) {
            logger.debug("Connection request is NOT in list"); //$NON-NLS-1$
        }
        logger.debug(
                "Got connection request from list with transactionId={}", String.valueOf(connReq.getConnectionRequestMessage().getHeader().getTransactionId())); //$NON-NLS-1$
        final Message successResponse = connReq.getConnectionRequestMessage().buildSuccessResponse();
        final InetSocketAddress targetEndpoint = new InetSocketAddress(controlConnection.getInetAddress(),
                controlConnection.getPort());
        logger.debug("Sending connection request response with {} to source", targetEndpoint.toString()); //$NON-NLS-1$
        successResponse.addAttribute(new XorMappedAddress(targetEndpoint));
        successResponse.addAttribute(new EndpointClass(EndpointCategory.PUBLIC));
        successResponse.writeTo(connReq.getControlConnection().getOutputStream());
    }

    private void callTarget(final UserData target, final Message connectionRequestFromSource) throws Exception {
        logger.debug("Calling target {}", target.getUserId()); //$NON-NLS-1$
        final Socket toTarget = target.getSocketToUser();
        final Message connectionRequest = MessageStaticFactory.newSTUNMessageInstance(STUNMessageClass.REQUEST,
                STUNMessageMethod.CONNECTION_REQUEST, connectionRequestFromSource.getHeader().getTransactionId());
        connectionRequest.addAttribute(new DirectconnectionAttribute());
        logger.debug("Forwarding connection request to target"); //$NON-NLS-1$
        connectionRequest.writeTo(toTarget.getOutputStream());
    }

    private void sendFailureResponse(final Message message, final String errorReaon, final STUNErrorCode errorCode,
            final OutputStream out) throws Exception {
        logger.debug(errorReaon);
        final Message failureResponse = message.buildFailureResponse(errorCode, errorReaon);
        failureResponse.writeTo(out);
    }

    @Override
    public NATTraversalTechniqueAttribute getAttributeForTraversalTechnique() {
        return new DirectconnectionAttribute();
    }
}
