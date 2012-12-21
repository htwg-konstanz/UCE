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

import de.fhkn.in.uce.mediator.techniqueregistry.MessageHandlerRegistry;
import de.fhkn.in.uce.mediator.techniqueregistry.MessageHandlerRegistryImpl;
import de.fhkn.in.uce.mediator.util.MediatorUtil;
import de.fhkn.in.uce.plugininterface.mediator.HandleMessage;
import de.fhkn.in.uce.plugininterface.message.NATSTUNAttributeType;
import de.fhkn.in.uce.plugininterface.message.NATTraversalTechniqueAttribute;
import de.fhkn.in.uce.stun.attribute.Attribute;
import de.fhkn.in.uce.stun.header.STUNMessageMethod;
import de.fhkn.in.uce.stun.message.Message;

/**
 * This {@link HandleMessage} for connection requests chooses the concrete
 * {@link HandleMessage} implementation by examining the
 * {@link NATTraversalTechniqueAttribute} which is provided by the connection
 * request message. The {@link MessageHandlerRegistry} delivers the used
 * implementation for the concrete {@link HandleMessage}.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class ConnectionRequestHandling implements HandleMessage {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionRequestHandling.class);
    private final MessageHandlerRegistry messageHandlerRegistry;
    private final MediatorUtil mediatorUtil;
    private final ConnectionRequestList connectionRequests;

    /**
     * Creates a {@link ConnectionRequestHandling} object.
     */
    public ConnectionRequestHandling() {
        this.messageHandlerRegistry = MessageHandlerRegistryImpl.getInstance();
        this.mediatorUtil = MediatorUtil.INSTANCE;
        this.connectionRequests = ConnectionRequestList.INSTANCE;
    }

    @Override
    public void handleMessage(final Message connectionRequestMessage, final Socket controlConnection) throws Exception {
        this.checkForRequiredTravTechAttribute(connectionRequestMessage);
        final NATTraversalTechniqueAttribute usedTravTech = connectionRequestMessage
                .getAttribute(NATTraversalTechniqueAttribute.class);
        final HandleMessage connectionRequestHandler = this.messageHandlerRegistry
                .getConnectionRequestHandlerByEncoding(usedTravTech.getEncoded());
        connectionRequestHandler.handleMessage(connectionRequestMessage, controlConnection);
        if (connectionRequestMessage.isMethod(STUNMessageMethod.CONNECTION_REQUEST)
                && connectionRequestMessage.isSuccessResponse()) {
            // remove only connection requests from list where a response was
            // seen
            this.connectionRequests.removeConnectionRequest(new String(connectionRequestMessage.getHeader()
                    .getTransactionId()));
            logger.debug(
                    "Connection request with transactionId={} removed from list", String.valueOf(connectionRequestMessage.getHeader().getTransactionId())); //$NON-NLS-1$
        }
    }

    private void checkForRequiredTravTechAttribute(final Message message) throws Exception {
        for (final Attribute a : message.getAttributes()) {
            logger.debug("connection request message has attribute {}", a.getType().toString()); //$NON-NLS-1$
        }
        try {
            this.mediatorUtil.checkForAttributeType(message, NATSTUNAttributeType.NAT_TRAVERSAL_TECHNIQUE);
        } catch (final Exception e) {
            final String errorMessage = "Required NATTraversalTechniqueAttribute is not provided, can not decide which handler to used"; //$NON-NLS-1$
            logger.error(errorMessage);
            throw new Exception(errorMessage, e);
        }
    }

    @Override
    public NATTraversalTechniqueAttribute getAttributeForTraversalTechnique() {
        return new NATTraversalTechniqueAttribute(Integer.MAX_VALUE);
    }
}
