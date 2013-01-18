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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.connectivitymanager.connection.configuration.ConnectionConfiguration;
import de.fhkn.in.uce.connectivitymanager.investigator.InfrastructreInvestigator;
import de.fhkn.in.uce.connectivitymanager.investigator.InfrastructureInvestigatorImpl;
import de.fhkn.in.uce.connectivitymanager.manager.ConnectionEstablishment;
import de.fhkn.in.uce.connectivitymanager.mediatorconnection.MediatorConnection;
import de.fhkn.in.uce.connectivitymanager.registry.NATTraversalRegistry;
import de.fhkn.in.uce.connectivitymanager.registry.NATTraversalRegistryImpl;
import de.fhkn.in.uce.plugininterface.NATBehavior;
import de.fhkn.in.uce.plugininterface.NATTraversalTechnique;
import de.fhkn.in.uce.plugininterface.message.NATAttributeTypeDecoder;
import de.fhkn.in.uce.plugininterface.message.NATTraversalTechniqueAttribute;
import de.fhkn.in.uce.stun.header.STUNMessageMethod;
import de.fhkn.in.uce.stun.message.Message;
import de.fhkn.in.uce.stun.message.MessageReader;
import de.fhkn.in.uce.stun.util.MessageFormatException;

public final class UnsecureTargetSideConnectionEstablishment implements ConnectionEstablishment {
    private final Logger logger = LoggerFactory.getLogger(UnsecureTargetSideConnectionEstablishment.class);
    private final NATTraversalRegistry registry;
    private final InfrastructreInvestigator investigator;
    private final MediatorConnection mediatorConnection;

    public UnsecureTargetSideConnectionEstablishment() throws Exception {
        this.registry = NATTraversalRegistryImpl.getInstance();
        this.investigator = new InfrastructureInvestigatorImpl();
        this.mediatorConnection = new MediatorConnection();
    }

    @Override
    public Socket establishConnection(final String targetId, final ConnectionConfiguration config) {
        try {
            this.register(targetId);
            final Socket socketToSource = this.startTarget(targetId);
            logger.debug("Returning socket {} to cm", socketToSource.toString()); //$NON-NLS-1$
            this.mediatorConnection.close();
            return socketToSource;
        } catch (final Exception e) {
            final String errorMessage = "Exception while creating target-side connection"; //$NON-NLS-1$
            this.logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    private void register(final String targetId) throws Exception {
        final List<NATTraversalTechnique> supportedTraversalTechniques = this.registry
                .getAllSupportedNATTraversalTechniques();
        final List<NATTraversalTechniqueAttribute> travTechAttributes = new ArrayList<NATTraversalTechniqueAttribute>();
        for (final NATTraversalTechnique supportedTraversalTechnique : supportedTraversalTechniques) {
            travTechAttributes.add(supportedTraversalTechnique.getMetaData().getAttribute());
        }
        final NATBehavior ownNatBehavior = this.investigator.investigateOwnNat(this.mediatorConnection
                .getControlConnection().getLocalPort());
        this.mediatorConnection.registerTarget(targetId, ownNatBehavior, travTechAttributes);
    }

    private Socket startTarget(final String targetId) {
        Socket result = null;
        boolean checkEstTask = false;
        final ExecutorService waitingExecutor = Executors.newSingleThreadExecutor();
        final CompletionService<Message> complServiceConnReq = new ExecutorCompletionService<Message>(waitingExecutor);
        complServiceConnReq.submit(new ConnectionRequestWaiting(this.mediatorConnection.getControlConnection()));
        CompletionService<Socket> complServiceConnEst = null;
        while (true) {
            final Future<Message> waitingResult = complServiceConnReq.poll();
            if (null != waitingResult) {
                try {
                    final Message connRequest = waitingResult.get();
                    final NATTraversalTechnique usedNatTraversalTechnique = this
                            .getUsedTraversalTechniqueFromMessage(connRequest);
                    final ExecutorService estabExecutor = Executors.newSingleThreadExecutor();
                    complServiceConnEst = new ExecutorCompletionService<Socket>(estabExecutor);
                    logger.debug(
                            "Starting target-side with {}", usedNatTraversalTechnique.getMetaData().getTraversalTechniqueName()); //$NON-NLS-1$
                    complServiceConnEst.submit(new ConnectionEstablishmentTask(usedNatTraversalTechnique, targetId,
                            this.mediatorConnection.getControlConnection(), connRequest));
                    checkEstTask = true;
                } catch (final Exception e) {
                    logger.equals(e.getMessage());
                }
            }
            if (checkEstTask) {
                final Future<Socket> estResult = complServiceConnEst.poll();
                if (null != estResult) {
                    logger.debug("Connection establishing result is not null, returning socket"); //$NON-NLS-1$
                    try {
                        result = estResult.get();
                    } catch (final Exception e) {
                        logger.error(e.getMessage());
                    }
                    break;
                }
            }
        }
        waitingExecutor.shutdownNow();
        return result;
    }

    private NATTraversalTechnique getUsedTraversalTechniqueFromMessage(final Message message) throws Exception {
        NATTraversalTechnique result = null;
        if (message.hasAttribute(NATTraversalTechniqueAttribute.class)) {
            final NATTraversalTechniqueAttribute usedTechAttr = message
                    .getAttribute(NATTraversalTechniqueAttribute.class);
            result = this.registry.getNATTraversalTechniqueByEncoding(usedTechAttr.getEncoded());
        } else {
            throw new MessageFormatException("NATTraversalTechniqueAttriute not included in message"); //$NON-NLS-1$
        }
        return result;
    }

    /**
     * Tries to establish a connection via the given
     * {@link NATTraversalTechnique}.
     * 
     * @author Alexander Diener (aldiener@htwg-konstanz.de)
     * 
     */
    private final class ConnectionEstablishmentTask implements Callable<Socket> {
        final NATTraversalTechnique travTech;
        final String targetId;
        final Socket controlConnection;
        final Message connectionRequestMessage;

        /**
         * Creates a {@link ConnectionEstablishmentTask} and tries to establish
         * a connection.
         * 
         * @param travTechToUse
         *            the {@link NATTraversalTechnique} to use
         * @param targetId
         *            the id of the target
         * @param controlConnection
         *            the connection to the mediator
         * @param connectionRequestMessage
         *            the connection request message for that connection
         *            establishment
         */
        public ConnectionEstablishmentTask(final NATTraversalTechnique travTechToUse, final String targetId,
                final Socket controlConnection, final Message connectionRequestMessage) {
            this.travTech = travTechToUse;
            this.targetId = targetId;
            this.controlConnection = controlConnection;
            this.connectionRequestMessage = connectionRequestMessage;
        }

        @Override
        public Socket call() throws Exception {
            logger.debug(
                    "Trying to establish connection via {}", this.travTech.getMetaData().getTraversalTechniqueName()); //$NON-NLS-1$
            return this.travTech.createTargetSideConnection(this.targetId, this.controlConnection,
                    this.connectionRequestMessage);
        }

    }

    /**
     * Waits for connection request messages and returns the decoded
     * {@link Message}.
     * 
     * @author Alexander Diener (aldiener@htwg-konstanz.de)
     * 
     */
    private final class ConnectionRequestWaiting implements Callable<Message> {
        private final Socket controlConnection;

        /**
         * Creats a {@link ConnectionRequestWaiting} object which uses the given
         * control connection to read and return connection request messages.
         * 
         * @param controlConnection
         *            the control connection to the mediator
         */
        public ConnectionRequestWaiting(final Socket controlConnection) {
            this.controlConnection = controlConnection;
        }

        @Override
        public Message call() throws Exception {
            Message result = null;
            final MessageReader messageReader = MessageReader
                    .createMessageReaderWithCustomAttributeTypeDecoder(new NATAttributeTypeDecoder());
            UnsecureTargetSideConnectionEstablishment.this.logger.debug(
                    "waiting for connection request message from {}", this.controlConnection.toString()); //$NON-NLS-1$
            while (this.controlConnection.isConnected()) {
                final Message inMessage = messageReader.readSTUNMessage(this.controlConnection.getInputStream());
                if (inMessage.isMethod(STUNMessageMethod.CONNECTION_REQUEST)) {
                    UnsecureTargetSideConnectionEstablishment.this.logger.debug("Got connection request message"); //$NON-NLS-1$
                    result = inMessage;
                    break;
                }

            }
            return result;
        }
    }
}
