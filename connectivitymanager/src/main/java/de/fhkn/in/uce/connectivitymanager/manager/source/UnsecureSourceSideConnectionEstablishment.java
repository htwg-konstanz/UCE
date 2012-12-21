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
package de.fhkn.in.uce.connectivitymanager.manager.source;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.connectivitymanager.connection.configuration.ConnectionConfiguration;
import de.fhkn.in.uce.connectivitymanager.investigator.InfrastructreInvestigator;
import de.fhkn.in.uce.connectivitymanager.investigator.InfrastructureInvestigatorImpl;
import de.fhkn.in.uce.connectivitymanager.manager.ConnectionEstablishment;
import de.fhkn.in.uce.connectivitymanager.manager.ManagerUtil;
import de.fhkn.in.uce.connectivitymanager.manager.ManagerUtilImpl;
import de.fhkn.in.uce.connectivitymanager.mediatorconnection.MediatorConnection;
import de.fhkn.in.uce.connectivitymanager.registry.NATTraversalRegistry;
import de.fhkn.in.uce.connectivitymanager.registry.NATTraversalRegistryImpl;
import de.fhkn.in.uce.connectivitymanager.selector.NATTraversalSelection;
import de.fhkn.in.uce.connectivitymanager.selector.strategy.ConnectionSetupTimeSelection;
import de.fhkn.in.uce.plugininterface.NATBehavior;
import de.fhkn.in.uce.plugininterface.NATSituation;
import de.fhkn.in.uce.plugininterface.NATTraversalTechnique;
import de.fhkn.in.uce.plugininterface.message.NATAttributeTypeDecoder;
import de.fhkn.in.uce.plugininterface.message.NATTraversalTechniqueAttribute;
import de.fhkn.in.uce.stun.attribute.Username;
import de.fhkn.in.uce.stun.header.STUNMessageClass;
import de.fhkn.in.uce.stun.header.STUNMessageMethod;
import de.fhkn.in.uce.stun.message.Message;
import de.fhkn.in.uce.stun.message.MessageReader;
import de.fhkn.in.uce.stun.message.MessageStaticFactory;

public final class UnsecureSourceSideConnectionEstablishment implements ConnectionEstablishment {
    private final Logger logger = LoggerFactory.getLogger(UnsecureSourceSideConnectionEstablishment.class);
    // private final MediatorUtil mediatorUtil;
    private final NATTraversalSelection selection;
    private final NATTraversalRegistry registry;
    private final InfrastructreInvestigator investigator;
    private final ManagerUtil managerUtil;
    private final MediatorConnection mediatorConnection;

    public UnsecureSourceSideConnectionEstablishment() throws Exception {
        this.registry = NATTraversalRegistryImpl.getInstance();
        this.selection = new ConnectionSetupTimeSelection(this.registry);
        this.investigator = new InfrastructureInvestigatorImpl();
        this.managerUtil = ManagerUtilImpl.getInstance();
        this.mediatorConnection = new MediatorConnection();
    }

    @Override
    public Socket establishConnection(final String targetId, final ConnectionConfiguration config) {
        // final NATBehavior sourceNat =
        // this.investigator.investigateOwnNat(this.mediatorConnection
        // .getControlConnection().getLocalPort());
        final NATBehavior sourceNat = new NATBehavior();
        NATBehavior targetNat;
        try {
            logger.debug("Requesting nat of {}", targetId); //$NON-NLS-1$
            targetNat = this.requestTargetNatBehavior(targetId);
        } catch (final Exception e) {
            logger.error(e.getMessage());
            targetNat = new NATBehavior();
        }
        final NATSituation natSituation = new NATSituation(sourceNat, targetNat);
        logger.debug("Current nat situation: {}", natSituation.toString()); //$NON-NLS-1$ 
        List<NATTraversalTechniqueAttribute> supportedTravTechsByTarget = new ArrayList<NATTraversalTechniqueAttribute>();
        try {
            logger.debug("Requesting supported nat traversal techniques of {}", targetId); // $NON-NLS-1$
            supportedTravTechsByTarget = this.requestSupportedTraversalTechniquesOfTarget(targetId);
        } catch (final Exception e1) {
            logger.error("Could not request supported nat traversal techniques of target", e1); //$NON-NLS-1$
        }
        logger.debug("Creating source-side connection"); // $NON-NLS-1$
        final Socket connectedSocket = this.createSourceSideConnection(targetId, natSituation,
                supportedTravTechsByTarget);
        try {
            this.managerUtil.setTrafficClass(connectedSocket, config);
        } catch (final Exception e) {
            this.logger
                    .error("Could not set service class {} for socket {}", config.getServiceClass(), connectedSocket); //$NON-NLS-1$
        }
        logger.debug("Connection established to {}", connectedSocket.toString()); // $NON-NLS-1$
        this.mediatorConnection.close();
        return connectedSocket;
    }

    private NATBehavior requestTargetNatBehavior(final String targetId) throws Exception {
        NATBehavior result = new NATBehavior();
        final Message requestMessage = MessageStaticFactory.newSTUNMessageInstance(STUNMessageClass.REQUEST,
                STUNMessageMethod.NAT_REQUEST);
        requestMessage.addAttribute(new Username(targetId));
        requestMessage.writeTo(this.mediatorConnection.getControlConnection().getOutputStream());
        final MessageReader messageReader = MessageReader
                .createMessageReaderWithCustomAttributeTypeDecoder(new NATAttributeTypeDecoder());
        final Message response = messageReader.readSTUNMessage(this.mediatorConnection.getControlConnection()
                .getInputStream());
        if (response.hasAttribute(NATBehavior.class)) {
            result = response.getAttribute(NATBehavior.class);
        }
        this.logger.debug("target {} is behind nat {}", targetId, result.toString()); //$NON-NLS-1$
        return result;
    }

    private List<NATTraversalTechniqueAttribute> requestSupportedTraversalTechniquesOfTarget(final String targetId)
            throws Exception {
        final List<NATTraversalTechniqueAttribute> result = new ArrayList<NATTraversalTechniqueAttribute>();
        final Message requestMessage = MessageStaticFactory.newSTUNMessageInstance(STUNMessageClass.REQUEST,
                STUNMessageMethod.SUPPORTED_TRAV_TECHS_REQUEST);
        requestMessage.addAttribute(new Username(targetId));
        requestMessage.writeTo(this.mediatorConnection.getControlConnection().getOutputStream());
        final MessageReader messageReader = MessageReader
                .createMessageReaderWithCustomAttributeTypeDecoder(new NATAttributeTypeDecoder());
        final Message response = messageReader.readSTUNMessage(this.mediatorConnection.getControlConnection()
                .getInputStream());
        if (response.hasAttribute(NATTraversalTechniqueAttribute.class)) {
            for (final NATTraversalTechniqueAttribute ntta : response
                    .getAttributes(NATTraversalTechniqueAttribute.class)) {
                result.add(ntta);
            }
        } else {
            logger.debug("No supported traversal techniques for {} returned.", targetId);
        }
        return Collections.unmodifiableList(result);
    }

    private Socket createSourceSideConnection(final String targetId, final NATSituation natSituation,
            final List<NATTraversalTechniqueAttribute> supportedTravTechsByTarget) {
        for (NATTraversalTechniqueAttribute supportedTravTechByTarget : supportedTravTechsByTarget) {
            logger.debug("target supports {}", supportedTravTechByTarget.getEncoded()); //$NON-NLS-1$
        }
        Socket resultSocket = null;
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final List<NATTraversalTechnique> traversalTechniques = this.selection
                .getNATTraversalTechniquesForNATSituation(natSituation);
        this.logger.debug("Current nat situation {}", natSituation.toString()); //$NON-NLS-1$
        this.logger.debug("Got the following traversal techniques in order"); //$NON-NLS-1$
        for (final NATTraversalTechnique natTraversalTechnique : traversalTechniques) {
            this.logger.debug(
                    "Appropriate technique: {}", natTraversalTechnique.getMetaData().getTraversalTechniqueName()); //$NON-NLS-1$
        }
        for (final NATTraversalTechnique natTraversalTechnique : traversalTechniques) {
            if (supportedTravTechsByTarget.contains(natTraversalTechnique.getMetaData().getAttribute())) {
                logger.debug("Trying to establish connection via {}", natTraversalTechnique.getMetaData()
                        .getTraversalTechniqueName());
                Future<Socket> future = null;
                try {
                    final Callable<Socket> socketTask = new SocketTask(targetId, natTraversalTechnique,
                            this.mediatorConnection.getControlConnection());
                    future = executor.submit(socketTask);
                    resultSocket = future.get(natTraversalTechnique.getMetaData().getTimeout(), TimeUnit.MILLISECONDS);
                } catch (final TimeoutException toe) {
                    this.logger.error("Timeout while creating source-side connection via {}.", natTraversalTechnique //$NON-NLS-1$
                            .getMetaData().getTraversalTechniqueName());
                    // do nothing, try next traversal technique
                } catch (final Exception e) {
                    this.logger.error(e.getMessage());
                } finally {
                    if (null != future) {
                        future.cancel(true);
                    }
                }
                if (resultSocket != null && resultSocket.isConnected()) {
                    break;
                }
            } else {
                logger.debug(
                        "{} is not supported by the target and not tried", natTraversalTechnique.getMetaData().getTraversalTechniqueName()); //$NON-NLS-1$
            }
        }
        executor.shutdownNow();
        return resultSocket;
    }
}
