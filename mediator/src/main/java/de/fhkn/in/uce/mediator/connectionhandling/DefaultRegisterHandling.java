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

import de.fhkn.in.uce.mediator.peerregistry.Endpoint;
import de.fhkn.in.uce.mediator.peerregistry.UserData;
import de.fhkn.in.uce.mediator.peerregistry.UserList;
import de.fhkn.in.uce.mediator.util.MediatorUtil;
import de.fhkn.in.uce.plugininterface.NATBehavior;
import de.fhkn.in.uce.plugininterface.mediator.HandleMessage;
import de.fhkn.in.uce.plugininterface.message.NATTraversalTechniqueAttribute;
import de.fhkn.in.uce.stun.attribute.EndpointClass;
import de.fhkn.in.uce.stun.attribute.EndpointClass.EndpointCategory;
import de.fhkn.in.uce.stun.attribute.MappedAddress;
import de.fhkn.in.uce.stun.attribute.Username;
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
    private final UserList userList = UserList.INSTANCE;
    private final MediatorUtil mediatorUtil = MediatorUtil.INSTANCE;

    @Override
    public void handleMessage(final Message registerMessage, final Socket controlConnection) throws Exception {
        this.checkForRequiredAttributes(registerMessage);
        final UserData newUser = this.createNewUserWithRequiredAttributes(registerMessage, controlConnection);
        newUser.addEndpoint(this.getPublicEndpointFromSocket(controlConnection));
        newUser.addEndpoint(this.createEndpointFromAttributes(registerMessage));
        this.userList.addOrUpdateUser(newUser);
    }

    private void checkForRequiredAttributes(final Message registerMessage) throws Exception {
        this.mediatorUtil.checkForAttribute(registerMessage, Username.class);
        this.mediatorUtil.checkForAttribute(registerMessage, NATBehavior.class);
    }

    private UserData createNewUserWithRequiredAttributes(final Message registerMessage, final Socket socketToUser)
            throws Exception {
        final Username username = registerMessage.getAttribute(Username.class);
        final NATBehavior userNat = registerMessage.getAttribute(NATBehavior.class);
        return new UserData(username.getUsernameAsString(), userNat, socketToUser);
    }

    private Endpoint getPublicEndpointFromSocket(final Socket socket) {
        final InetSocketAddress publicAddress = new InetSocketAddress(socket.getInetAddress(), socket.getPort());
        return new Endpoint(publicAddress, EndpointCategory.PUBLIC);
    }

    private Endpoint createEndpointFromAttributes(final Message registerMessage) {
        // TODO what if there are several endpoints?
        final MappedAddress address = registerMessage.getAttribute(MappedAddress.class);
        final EndpointClass category = registerMessage.getAttribute(EndpointClass.class);
        return new Endpoint(address.getEndpoint(), category.getEndpointCategory());
    }

    @Override
    public NATTraversalTechniqueAttribute getAttributeForTraversalTechnique() {
        return new NATTraversalTechniqueAttribute(Integer.MAX_VALUE);
    }
}
