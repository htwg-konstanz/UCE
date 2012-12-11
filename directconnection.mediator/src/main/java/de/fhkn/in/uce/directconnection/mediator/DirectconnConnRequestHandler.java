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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.directconnection.message.DirectconnectionAttribute;
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
import de.fhkn.in.uce.stun.message.MessageReader;
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
public final class DirectconnConnRequestHandler implements HandleMessage {
    private static final Logger logger = LoggerFactory.getLogger(DirectconnConnRequestHandler.class);
    private final MediatorUtil mediatorUtil = MediatorUtil.INSTANCE;
    private final UserList userList = UserList.INSTANCE;

    @Override
    public void handleMessage(final Message message, final Socket controlConnection) throws Exception {
        this.mediatorUtil.checkForAttribute(message, Username.class);
        final String username = message.getAttribute(Username.class).getUsernameAsString();
        final UserData userData = this.userList.getUserDataByUserId(username);
        if (userData == null) {
            final String errorMessage = "User " + username + " not exists"; //$NON-NLS-1$ //$NON-NLS-2$
            this.sendFailureResponse(message, errorMessage, STUNErrorCode.BAD_REQUEST,
                    controlConnection.getOutputStream());
        } else {
            this.callTarget(userData);
            this.sendConnectionRequestSuccessReponse(userData, message, controlConnection);
        }
    }

    private void sendConnectionRequestSuccessReponse(final UserData userData, final Message requestMessage,
            final Socket controlConnection) throws Exception {
        final Message successResponse = requestMessage.buildSuccessResponse();
        final InetSocketAddress targetEndpoint = new InetSocketAddress(userData.getSocketToUser().getInetAddress(),
                userData.getSocketToUser().getPort());
        successResponse.addAttribute(new XorMappedAddress(targetEndpoint));
        successResponse.addAttribute(new EndpointClass(EndpointCategory.PUBLIC));
        successResponse.writeTo(controlConnection.getOutputStream());
    }

    private void callTarget(final UserData target) throws Exception {
        final Socket toTarget = target.getSocketToUser();
        final Message connectionRequest = MessageStaticFactory.newSTUNMessageInstance(STUNMessageClass.REQUEST,
                STUNMessageMethod.CONNECTION_REQUEST);
        connectionRequest.addAttribute(new DirectconnectionAttribute());
        connectionRequest.writeTo(toTarget.getOutputStream());
        this.waitForTarget(toTarget);
    }

    private void waitForTarget(final Socket toTarget) throws Exception {
        final Message responseFromTarget = MessageReader.createMessageReader().readSTUNMessage(
                toTarget.getInputStream());
        if (responseFromTarget.isFailureResponse()) {
            throw new Exception("Target could not be started"); //$NON-NLS-1$
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
        return new DirectconnectionAttribute();
    }
}
