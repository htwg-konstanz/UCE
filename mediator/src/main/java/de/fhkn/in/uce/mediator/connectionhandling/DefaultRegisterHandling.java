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
package de.fhkn.in.uce.mediator.connectionhandling;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.mediator.peerregistry.Endpoint;
import de.fhkn.in.uce.mediator.peerregistry.UserData;
import de.fhkn.in.uce.mediator.peerregistry.UserList;
import de.fhkn.in.uce.mediator.util.MediatorUtil;
import de.fhkn.in.uce.plugininterface.NATBehavior;
import de.fhkn.in.uce.plugininterface.mediator.HandleMessage;
import de.fhkn.in.uce.plugininterface.message.NATSTUNAttributeType;
import de.fhkn.in.uce.plugininterface.message.NATTraversalTechniqueAttribute;
import de.fhkn.in.uce.stun.attribute.Attribute;
import de.fhkn.in.uce.stun.attribute.EndpointClass;
import de.fhkn.in.uce.stun.attribute.EndpointClass.EndpointCategory;
import de.fhkn.in.uce.stun.attribute.Username;
import de.fhkn.in.uce.stun.attribute.XorMappedAddress;
import de.fhkn.in.uce.stun.message.Message;

/**
 * Handles register messages and adds the public endpoint to the created user.
 * If the user already exists it will be updated. If the register message
 * contains an other endpoint it will be added too. After handling the message
 * no response will be sent.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class DefaultRegisterHandling implements HandleMessage {
    private static final Logger logger = LoggerFactory.getLogger(DefaultRegisterHandling.class);
    private final UserList userList;
    private final MediatorUtil mediatorUtil;

    public DefaultRegisterHandling() {
        this.userList = UserList.INSTANCE;
        this.mediatorUtil = MediatorUtil.INSTANCE;
    }

    @Override
    public void handleMessage(final Message registerMessage, final Socket controlConnection) throws Exception {
        this.checkForRequiredAttributes(registerMessage);
        final UserData newUser = this.createNewUserWithRequiredAttributes(registerMessage, controlConnection);
        newUser.addEndpoint(this.getPublicEndpointFromSocket(controlConnection));
        if (registerMessage.hasAttribute(XorMappedAddress.class)) {
            newUser.addEndpoint(this.createEndpointFromAttributes(registerMessage));
        }
        this.userList.addOrUpdateUser(newUser);
        logger.debug("User {} added or updated", newUser.getUserId()); //$NON-NLS-1$
        this.sendSuccessResponse(registerMessage, controlConnection);
    }

    private void sendSuccessResponse(final Message toRespond, final Socket controlConnection) throws Exception {
        final Message response = toRespond.buildSuccessResponse();
        response.writeTo(controlConnection.getOutputStream());
    }

    private void checkForRequiredAttributes(final Message registerMessage) throws Exception {
        this.mediatorUtil.checkForAttribute(registerMessage, Username.class);
        this.mediatorUtil.checkForAttribute(registerMessage, NATBehavior.class);
        this.mediatorUtil.checkForAttributeType(registerMessage, NATSTUNAttributeType.NAT_TRAVERSAL_TECHNIQUE);
    }

    private UserData createNewUserWithRequiredAttributes(final Message registerMessage, final Socket socketToUser)
            throws Exception {
        final Username username = registerMessage.getAttribute(Username.class);
        final NATBehavior userNat = registerMessage.getAttribute(NATBehavior.class);
        final List<NATTraversalTechniqueAttribute> supportedTravTechs = this
                .createListOfTechniquesFromMessage(registerMessage);
        return new UserData(username.getUsernameAsString(), userNat, socketToUser, supportedTravTechs);
    }

    private List<NATTraversalTechniqueAttribute> createListOfTechniquesFromMessage(final Message message) {
        final List<NATTraversalTechniqueAttribute> result = new ArrayList<NATTraversalTechniqueAttribute>();
        for (final Attribute a : message.getAttributes()) {
            if (a.getType() == NATSTUNAttributeType.NAT_TRAVERSAL_TECHNIQUE) {
                result.add((NATTraversalTechniqueAttribute) a);
            }
        }
        return result;
    }

    private Endpoint getPublicEndpointFromSocket(final Socket socket) {
        final InetSocketAddress publicAddress = new InetSocketAddress(socket.getInetAddress(), socket.getPort());
        return new Endpoint(publicAddress, EndpointCategory.PUBLIC);
    }

    private Endpoint createEndpointFromAttributes(final Message registerMessage) {
        final XorMappedAddress address = registerMessage.getAttribute(XorMappedAddress.class);
        EndpointCategory category = EndpointCategory.PRIVATE;
        if (registerMessage.hasAttribute(EndpointClass.class)) {
            category = registerMessage.getAttribute(EndpointClass.class).getEndpointCategory();
        }
        return new Endpoint(address.getEndpoint(), category);
    }

    @Override
    public NATTraversalTechniqueAttribute getAttributeForTraversalTechnique() {
        return new NATTraversalTechniqueAttribute(Integer.MAX_VALUE);
    }
}
