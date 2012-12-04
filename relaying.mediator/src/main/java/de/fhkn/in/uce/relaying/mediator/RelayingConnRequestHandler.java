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
package de.fhkn.in.uce.relaying.mediator;

import java.io.OutputStream;
import java.net.Inet6Address;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.mediator.peerregistry.Endpoint;
import de.fhkn.in.uce.mediator.peerregistry.UserData;
import de.fhkn.in.uce.mediator.peerregistry.UserList;
import de.fhkn.in.uce.mediator.util.MediatorUtil;
import de.fhkn.in.uce.plugininterface.mediator.HandleMessage;
import de.fhkn.in.uce.plugininterface.message.NATTraversalTechniqueAttribute;
import de.fhkn.in.uce.relaying.message.RelayingAttribute;
import de.fhkn.in.uce.stun.attribute.EndpointClass;
import de.fhkn.in.uce.stun.attribute.EndpointClass.EndpointCategory;
import de.fhkn.in.uce.stun.attribute.ErrorCode.STUNErrorCode;
import de.fhkn.in.uce.stun.attribute.Username;
import de.fhkn.in.uce.stun.attribute.XorMappedAddress;
import de.fhkn.in.uce.stun.message.Message;

/**
 * Plugin for handling relaying connection requests. The handler sends a
 * response to the client with the {@link XorMappedAddress} of the endpoint at
 * the relay server. This endpoint is created by the registered user.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class RelayingConnRequestHandler implements HandleMessage {
    private static final Logger logger = LoggerFactory.getLogger(RelayingConnRequestHandler.class);
    private final MediatorUtil mediatorUtil = MediatorUtil.INSTANCE;
    private final UserList userList = UserList.INSTANCE;

    @Override
    public void handleMessage(Message message, Socket controlConnection) throws Exception {
        this.mediatorUtil.checkForAttribute(message, Username.class);
        final Username username = message.getAttribute(Username.class);
        final UserData user = this.userList.getUserDataByUserId(username.getUsernameAsString());
        if (user == null) {
            final String errorMessage = "User " + username.getUsernameAsString() + " not exists"; //$NON-NLS-1$ //$NON-NLS-2$
            this.sendFailureResponse(message, errorMessage, STUNErrorCode.BAD_REQUEST,
                    controlConnection.getOutputStream());
        } else {
            this.sendRelayEndpointToSource(message, user, controlConnection);
        }
    }

    private void sendRelayEndpointToSource(final Message requestMessage, final UserData user,
            final Socket controlConnection) throws Exception {
        final List<Endpoint> endpointsAtRelay = user.getEndpointsForCategory(EndpointCategory.RELAY);
        // TODO what if there are several endpoints? is this possible?
        if (!endpointsAtRelay.isEmpty()) {
            final Message responseMessage = requestMessage.buildSuccessResponse();
            final Endpoint epAtRelay = endpointsAtRelay.get(0);
            if (epAtRelay.getEndpointAddress().getAddress() instanceof Inet6Address) {
                responseMessage.addAttribute(new XorMappedAddress(epAtRelay.getEndpointAddress(), ByteBuffer.wrap(
                        responseMessage.getHeader().getTransactionId()).getInt()));
            } else {
                responseMessage.addAttribute(new XorMappedAddress(epAtRelay.getEndpointAddress()));
            }
            responseMessage.addAttribute(new EndpointClass(EndpointCategory.RELAY));
            responseMessage.writeTo(controlConnection.getOutputStream());
        } else {
            final String errorMessage = "No relay endpoint registered for user " + user.getUserId(); //$NON-NLS-1$
            this.sendFailureResponse(requestMessage, errorMessage, STUNErrorCode.BAD_REQUEST,
                    controlConnection.getOutputStream());
        }
    }

    private void sendFailureResponse(final Message message, final String errorReaon, final STUNErrorCode errorCode,
            final OutputStream out) throws Exception {
        logger.debug(errorReaon);
        final Message failureResponse = message.buildFailureResponse(errorCode, errorReaon);
        failureResponse.writeTo(out);
    }

    @Override
    public NATTraversalTechniqueAttribute getAttributeForTraversalTechnique() {
        return new RelayingAttribute();
    }
}
