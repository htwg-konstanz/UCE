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

import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.mediator.peerregistry.UserData;
import de.fhkn.in.uce.mediator.peerregistry.UserList;
import de.fhkn.in.uce.mediator.util.MediatorUtil;
import de.fhkn.in.uce.plugininterface.NATBehavior;
import de.fhkn.in.uce.plugininterface.mediator.HandleMessage;
import de.fhkn.in.uce.plugininterface.message.NATTraversalTechniqueAttribute;
import de.fhkn.in.uce.stun.attribute.Username;
import de.fhkn.in.uce.stun.message.Message;

public final class DefaultNatRequestHandling implements HandleMessage {
    private static final Logger logger = LoggerFactory.getLogger(DefaultNatRequestHandling.class);
    private final UserList userList;
    private final MediatorUtil mediatorUtil;

    public DefaultNatRequestHandling() {
        this.userList = UserList.INSTANCE;
        this.mediatorUtil = MediatorUtil.INSTANCE;
    }

    @Override
    public void handleMessage(final Message message, final Socket controlConnection) throws Exception {
        this.mediatorUtil.checkForAttribute(message, Username.class);
        final Username username = message.getAttribute(Username.class);
        final UserData user = this.userList.getUserDataByUserId(username.getUsernameAsString());
        final NATBehavior userNat = user.getUserNat();
        final Message response = message.buildSuccessResponse();
        response.addAttribute(userNat);
        response.writeTo(controlConnection.getOutputStream());
        logger.debug("response to {}:{} sent", message.getMessageClass().toString(), message.getMessageMethod()
                .toString());
    }

    @Override
    public NATTraversalTechniqueAttribute getAttributeForTraversalTechnique() {
        return new NATTraversalTechniqueAttribute(Integer.MAX_VALUE);
    }
}
