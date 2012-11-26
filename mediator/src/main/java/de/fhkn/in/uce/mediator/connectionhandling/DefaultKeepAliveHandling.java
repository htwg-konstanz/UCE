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

import de.fhkn.in.uce.mediator.peerregistry.UserList;
import de.fhkn.in.uce.mediator.util.MediatorUtil;
import de.fhkn.in.uce.plugininterface.mediator.HandleMessage;
import de.fhkn.in.uce.plugininterface.message.NATTraversalTechniqueAttribute;
import de.fhkn.in.uce.stun.attribute.Username;
import de.fhkn.in.uce.stun.message.Message;

/**
 * Handles keep alive messages and refreshes the time stamp of the user. After
 * handling the message no response will be sent.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class DefaultKeepAliveHandling implements HandleMessage {
    private final UserList userList = UserList.INSTANCE;
    private final MediatorUtil mediatorUtil = MediatorUtil.INSTANCE;

    @Override
    public void handleMessage(final Message keepaliveMessage, final Socket controlConnection) throws Exception {
        this.mediatorUtil.checkForAttribute(keepaliveMessage, Username.class);
        final Username username = keepaliveMessage.getAttribute(Username.class);
        this.userList.refreshUserTimestamp(username.getUsernameAsString());
    }

    @Override
    public NATTraversalTechniqueAttribute getAttributeForTraversalTechnique() {
        return new NATTraversalTechniqueAttribute(Integer.MAX_VALUE);
    }
}
