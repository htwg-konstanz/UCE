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
package de.fhkn.in.uce.reversal.mediator;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.mediator.connectionhandling.ConnectionRequest;
import de.fhkn.in.uce.mediator.connectionhandling.ConnectionRequestList;
import de.fhkn.in.uce.mediator.peerregistry.UserData;
import de.fhkn.in.uce.mediator.peerregistry.UserList;
import de.fhkn.in.uce.mediator.util.MediatorUtil;
import de.fhkn.in.uce.plugininterface.mediator.HandleMessage;
import de.fhkn.in.uce.plugininterface.message.NATTraversalTechniqueAttribute;
import de.fhkn.in.uce.reversal.message.ReversalAttribute;
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
 * This mediator plugin handles connection request for reversal. The source
 * requests a connection to a user, the {@link ReversalConnRequestHandler}
 * forwards this connection request with the public endpoint of the client to
 * the target. If the requested target does not exists a failure response is
 * sent.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class ReversalConnRequestHandler implements HandleMessage {
    private static final Logger logger = LoggerFactory.getLogger(ReversalConnRequestHandler.class);
    private final ConnectionRequestList connectionRequests = ConnectionRequestList.INSTANCE;
    private final MediatorUtil mediatorUtil = MediatorUtil.INSTANCE;
    private final UserList userList = UserList.INSTANCE;

    @Override
    public void handleMessage(final Message message, final Socket controlConnection) throws Exception {
        if (message.getMessageClass() == STUNMessageClass.REQUEST) {
            logger.debug("Handling connection request from {}", controlConnection.toString());
            this.handleConnectionRequest(message, controlConnection);
        } else if (message.getMessageClass() == STUNMessageClass.SUCCESS_RESPONSE) {
            logger.debug("Handling connection request response from {}", controlConnection.toString());
            this.handleConnectionRequestResponse(message, controlConnection);
        }
    }

    private void handleConnectionRequest(final Message message, final Socket controlConnection) throws Exception {
        this.mediatorUtil.checkForAttribute(message, Username.class);
        this.connectionRequests.putConnectionRequest(new ConnectionRequest(controlConnection, message));
        final Username username = message.getAttribute(Username.class);
        final UserData user = this.userList.getUserDataByUserId(username.getUsernameAsString());
        if (user == null) {
            final String errorMessage = "User " + username.getUsernameAsString() + " not exists"; //$NON-NLS-1$ //$NON-NLS-2$
            this.sendFailureResponse(message, errorMessage, STUNErrorCode.BAD_REQUEST,
                    controlConnection.getOutputStream());
        } else {
            this.forwardReversalRequestToTarget(controlConnection, user, message);
        }
    }

    private void handleConnectionRequestResponse(final Message message, final Socket controlConnection)
            throws Exception {
        final ConnectionRequest connReq = this.connectionRequests.getConnectionRequest(new String(message.getHeader()
                .getTransactionId()));
        final Message successResponse = connReq.getConnectionRequestMessage().buildSuccessResponse();
        successResponse.writeTo(connReq.getControlConnection().getOutputStream());
    }

    private void forwardReversalRequestToTarget(final Socket toSource, final UserData user,
            final Message connectionRequestFromSource) throws IOException {
        final Message connectionRequest = MessageStaticFactory.newSTUNMessageInstance(STUNMessageClass.REQUEST,
                STUNMessageMethod.CONNECTION_REQUEST, connectionRequestFromSource.getHeader().getTransactionId());
        XorMappedAddress clientAddress;
        if (toSource.getInetAddress() instanceof Inet6Address) {
            clientAddress = new XorMappedAddress(new InetSocketAddress(toSource.getInetAddress(), toSource.getPort()),
                    ByteBuffer.wrap(connectionRequest.getHeader().getTransactionId()).getInt());
        } else {
            clientAddress = new XorMappedAddress(new InetSocketAddress(toSource.getInetAddress(), toSource.getPort()));
        }
        connectionRequest.addAttribute(clientAddress);
        connectionRequest.addAttribute(new EndpointClass(EndpointCategory.PUBLIC));
        connectionRequest.addAttribute(new ReversalAttribute());
        final Socket toTarget = user.getSocketToUser();
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
        return new ReversalAttribute();
    }
}
