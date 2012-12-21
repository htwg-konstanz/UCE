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
package de.fhkn.in.uce.connectivitymanager.mediatorconnection;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.core.KeepAliveTask;
import de.fhkn.in.uce.plugininterface.NATBehavior;
import de.fhkn.in.uce.plugininterface.message.NATTraversalTechniqueAttribute;
import de.fhkn.in.uce.stun.attribute.ErrorCode;
import de.fhkn.in.uce.stun.attribute.Username;
import de.fhkn.in.uce.stun.header.STUNMessageClass;
import de.fhkn.in.uce.stun.header.STUNMessageMethod;
import de.fhkn.in.uce.stun.message.Message;
import de.fhkn.in.uce.stun.message.MessageReader;
import de.fhkn.in.uce.stun.message.MessageStaticFactory;

/**
 * Connection to the mediator. Provides functionality which is used to
 * communicate with it.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class MediatorConnection {
    private static final String PROPERTY_NAME_MEDIATOR = "mediator"; //$NON-NLS-1$
    private static final String PROPERTY_NAME_IP = "ip"; //$NON-NLS-1$
    private static final String PROPERTY_NAME_PORT = "port"; //$NON-NLS-1$
    private static final String PROPERTY_NAME_KEEP_ALIVE = "keepalive"; //$NON-NLS-1$
    private static final String SEPARATOR = "."; //$NON-NLS-1$
    private final Logger logger = LoggerFactory.getLogger(MediatorConnection.class);
    private final ResourceBundle bundle = ResourceBundle
            .getBundle("de.fhkn.in.uce.connectivitymanager.mediatorconnection.mediator"); //$NON-NLS-1$
    private final Socket controlConnection;
    private final MessageReader messageReader;
    private final ScheduledExecutorService keepAliveExecutor;

    /**
     * Creates a mediator connection and connects to the mediator address in the
     * property file.
     * 
     * @param mediatorAddress
     * @throws Exception
     */
    public MediatorConnection() throws Exception {
        this.messageReader = MessageReader.createMessageReader();
        this.keepAliveExecutor = Executors.newScheduledThreadPool(1);
        final InetSocketAddress mediatorAddress = this.getMediatorAddress();
        this.controlConnection = new Socket();
        this.controlConnection.setReuseAddress(true);
        logger.debug("connecting to mediator {}", mediatorAddress.toString());
        this.controlConnection.connect(mediatorAddress);
    }

    /**
     * Sends a register message for the given target id and NAT situation of the
     * target. Also starts a keep alive thread for the registration.
     * 
     * @param targetId
     *            the unique name of the target
     * @param currentNatBehavior
     *            the {@link NATBehavior} of the target
     * @param delayInSecondsForKeepAlive
     *            the time in seconds which is used to send keep alive messages
     * @throws Exception
     */
    public void registerTarget(final String targetId, final NATBehavior currentNatBehavior,
            final List<NATTraversalTechniqueAttribute> supportedTraversalTechniques) throws Exception {
        this.sendRegisterMessage(targetId, currentNatBehavior, supportedTraversalTechniques);
        this.waitForDeRegisterResponseMessage();
        this.startKeepAlive(targetId, this.getKeepAliveInSeconds());
    }

    private void sendRegisterMessage(final String targetId, final NATBehavior natBehavior,
            final List<NATTraversalTechniqueAttribute> supportedTraversalTechniques) throws Exception {
        final Message registerMessage = MessageStaticFactory.newSTUNMessageInstance(STUNMessageClass.REQUEST,
                STUNMessageMethod.REGISTER);
        registerMessage.addAttribute(new Username(targetId));
        registerMessage.addAttribute(natBehavior);
        for (final NATTraversalTechniqueAttribute natTraversalTechniqueAttribute : supportedTraversalTechniques) {
            registerMessage.addAttribute(natTraversalTechniqueAttribute);
        }
        registerMessage.writeTo(this.controlConnection.getOutputStream());
    }

    private void waitForDeRegisterResponseMessage() throws Exception {
        final Message response = this.messageReader.readSTUNMessage(this.controlConnection.getInputStream());
        if (response.isSuccessResponse()) {
            this.logger.debug("Target successfully (de)registered"); //$NON-NLS-1$
        } else {
            final ErrorCode errorCode = response.getAttribute(ErrorCode.class);
            final String errorMessage = "Could not (de)register target: " + errorCode.getErrorNumber() + " " + errorCode.getReasonPhrase(); //$NON-NLS-1$ //$NON-NLS-2$
            throw new Exception(errorMessage);
        }
    }

    private void startKeepAlive(final String targetId, final int delayInSeconds) {
        final Runnable keepAliveTask = new KeepAliveTask(targetId, this.controlConnection);
        this.keepAliveExecutor.scheduleWithFixedDelay(keepAliveTask, delayInSeconds, delayInSeconds, TimeUnit.SECONDS);
    }

    /**
     * Send a deregister message and stops the task for sending keep alive
     * messages.
     * 
     * @param targetId
     *            the unique name of the target
     * @throws Exception
     */
    public void deregisterTarget(final String targetId) throws Exception {
        this.sendDeregisterMesage(targetId);
        this.waitForDeRegisterResponseMessage();
        this.keepAliveExecutor.shutdownNow();
    }

    private void sendDeregisterMesage(final String targetId) throws Exception {
        final Message deregisterMessage = MessageStaticFactory.newSTUNMessageInstance(STUNMessageClass.REQUEST,
                STUNMessageMethod.DEREGISTER);
        deregisterMessage.addAttribute(new Username(targetId));
        deregisterMessage.writeTo(this.controlConnection.getOutputStream());
    }

    /**
     * Returns the control connection to the mediator.
     * 
     * @return the control connection to the mediator
     */
    public Socket getControlConnection() {
        return this.controlConnection;
    }

    private InetSocketAddress getMediatorAddress() {
        final String mediatorIP = this.bundle.getString(PROPERTY_NAME_MEDIATOR + SEPARATOR + PROPERTY_NAME_IP);
        final int mediatorPort = Integer.valueOf(this.bundle.getString(PROPERTY_NAME_MEDIATOR + SEPARATOR
                + PROPERTY_NAME_PORT));
        return new InetSocketAddress(mediatorIP, mediatorPort);
    }

    private int getKeepAliveInSeconds() {
        return Integer.valueOf(this.bundle.getString(PROPERTY_NAME_MEDIATOR + SEPARATOR + PROPERTY_NAME_KEEP_ALIVE));
    }

    /**
     * Sends a request for getting the supported NAT traversal techniques of a
     * user to the mediator. The result is returned as a unmodifiable list of
     * {@link NATTraversalTechniqueAttribute}s.
     * 
     * @param targetId
     *            the id of the user
     * @return the supported NAT traversal techniques of the user as a list of
     *         {@link NATTraversalTechniqueAttribute}s
     * @throws Exception
     */
    public List<NATTraversalTechniqueAttribute> requestSupportedTraversalTechniques(final String targetId)
            throws Exception {
        this.logger.debug("Requesting supported nat traversal techniques of {}", targetId); //$NON-NLS-1$
        final List<NATTraversalTechniqueAttribute> result = new ArrayList<NATTraversalTechniqueAttribute>();
        final Message requestMessage = MessageStaticFactory.newSTUNMessageInstance(STUNMessageClass.REQUEST,
                STUNMessageMethod.SUPPORTED_TRAV_TECHS_REQUEST);
        requestMessage.addAttribute(new Username(targetId));
        requestMessage.writeTo(this.controlConnection.getOutputStream());
        final Message response = this.messageReader.readSTUNMessage(this.controlConnection.getInputStream());
        for (final NATTraversalTechniqueAttribute suppTravTech : response
                .getAttributes(NATTraversalTechniqueAttribute.class)) {
            result.add(suppTravTech);
            this.logger.debug("User {} supports traversal technique {}", targetId, suppTravTech.getEncoded()); //$NON-NLS-1$
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Closes the mediator connection and cancels the keep alive task. But this
     * method does not sends a deregister message to the mediator.
     */
    public void close() {
        try {
            this.keepAliveExecutor.shutdownNow();
            this.controlConnection.close();
        } catch (final Exception e) {
            logger.error(e.getMessage());
        }
    }
}
