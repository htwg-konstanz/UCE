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
package de.fhkn.in.uce.reversal.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.stun.attribute.ErrorCode.STUNErrorCode;
import de.fhkn.in.uce.stun.attribute.XorMappedAddress;
import de.fhkn.in.uce.stun.header.STUNMessageMethod;
import de.fhkn.in.uce.stun.message.Message;
import de.fhkn.in.uce.stun.message.MessageReader;

/**
 * Contains the Target-Side of the ConnetionReversal implementation
 * 
 * With the integrated Builder-Class there can be build a configuration. With
 * this configuration a ConnectionReversalTarget can be initialized by calling
 * the build-Method on the inner configuration class.
 * 
 * The class provides to register and deregister a target on the Mediator, which
 * port and address is defined by the inner configuration class. With the accept
 * Method, there can be accepted connection requests of the source-side in the
 * ConnetionReversal implementation.
 * 
 * @author thomas zink, stefan lohr, Alexander Diener
 *         (aldiener@htwg-konstanz.de)
 */
public final class ReversalTarget {
    private static final Logger logger = LoggerFactory.getLogger(ReversalTarget.class);

    // private BlockingQueue<Socket> blockingSocketQueue;
    // private ListenerThread listenerThread;
    // private boolean registered;

    public ReversalTarget() {
        // this.registered = false;
        // this.blockingSocketQueue = new LinkedBlockingQueue<Socket>();
    }

    // public void deregister(final String targetId, final Socket
    // controlConnection) throws Exception {
    // if (!this.registered) {
    //            logger.error("Target not yet registered"); //$NON-NLS-1$
    //            throw new IllegalStateException("Target not yet registered"); //$NON-NLS-1$
    // }
    // this.listenerThread.interrupt();
    // this.blockingSocketQueue.clear();
    // this.sendRequestMessageWithUsername(STUNMessageMethod.DEREGISTER,
    // targetId, controlConnection);
    // if (this.waitForSuccessResponse(STUNMessageMethod.DEREGISTER,
    // controlConnection)) {
    //            logger.info("Success message for deregistration {} received", targetId); //$NON-NLS-1$
    // } else {
    //            logger.error("No success message for deregistration {} received", targetId); //$NON-NLS-1$
    //            throw new Exception("Could not deregister target"); //$NON-NLS-1$
    // }
    // }

    // public void register(final String targetId, final Socket
    // controlConnection) throws Exception {
    // if (this.registered) {
    //            throw new IllegalStateException("Target is already registered"); //$NON-NLS-1$
    // }
    // this.sendRequestMessageWithUsername(STUNMessageMethod.REGISTER, targetId,
    // controlConnection);
    // if (this.waitForSuccessResponse(STUNMessageMethod.REGISTER,
    // controlConnection)) {
    //            logger.info("starting listenerThread"); //$NON-NLS-1$
    // this.listenerThread = new ListenerThread(controlConnection,
    // this.blockingSocketQueue);
    // this.listenerThread.start();
    // this.registered = true;
    // } else {
    //            logger.error("No success message received, could not register target {}", targetId); //$NON-NLS-1$
    //            throw new Exception("Could not register target"); //$NON-NLS-1$
    // }
    // }

    // private void sendRequestMessageWithUsername(final STUNMessageMethod
    // method, final String username,
    // final Socket controlConnection) throws Exception {
    // final Message requestMessage =
    // MessageStaticFactory.newSTUNMessageInstance(STUNMessageClass.REQUEST,
    // method);
    // requestMessage.addAttribute(new Username(username));
    // requestMessage.writeTo(controlConnection.getOutputStream());
    // }

    // /**
    // * Method for accepting incoming connection from the source-side
    // *
    // * @return Socket of the ConnectionReversal connection
    // * @throws InterruptedException
    // */
    // public Socket establishTargetSideConnection() throws InterruptedException
    // {
    // Socket result = null;
    // if (this.registered) {
    // result = this.blockingSocketQueue.take();
    // } else {
    //            final String errorMessage = "Target not yet registered"; //$NON-NLS-1$
    // logger.error(errorMessage);
    // throw new IllegalStateException(errorMessage);
    // }
    // return result;
    // }

    public Socket establishTargetSideConnection(final Socket controlConnection, final Message connectionRequestMessage)
            throws Exception {
        if (connectionRequestMessage.hasAttribute(XorMappedAddress.class)) {
            final String errorReason = "Source endpoint is not provided by the connection request"; //$NON-NLS-1$
            this.sendFailureResponse(controlConnection, connectionRequestMessage, errorReason);
            throw new Exception(errorReason);
        } else {
            this.sendSuccessResponse(controlConnection, connectionRequestMessage);
            final InetSocketAddress sourceAddress = connectionRequestMessage.getAttribute(XorMappedAddress.class)
                    .getEndpoint();
            final Socket toClient = new Socket();
            toClient.setReuseAddress(true);
            toClient.connect(sourceAddress);
            return toClient;
        }
    }

    private void sendSuccessResponse(final Socket controlConnection, final Message request) throws IOException {
        logger.debug("Sending success response"); //$NON-NLS-1$
        final Message response = request.buildSuccessResponse();
        response.writeTo(controlConnection.getOutputStream());
    }

    private void sendFailureResponse(final Socket controlConnection, final Message request, final String errorReason)
            throws IOException {
        logger.debug(errorReason);
        final Message response = request.buildFailureResponse(STUNErrorCode.BAD_REQUEST, errorReason);
        response.writeTo(controlConnection.getOutputStream());
    }

    /**
     * waits for the correct response message of the request messages from
     * parameter. After receiving message or a timeout this method returns true
     * or false.
     * 
     * @param method
     *            the {@link STUNMessageMethod} to check the response message
     * @return true if the response message matches the expected
     *         {@link STUNMessageMethod} and the response message is a success
     *         response, false else
     * @throws IOException
     */
    private boolean waitForSuccessResponse(final STUNMessageMethod method, final Socket controlConnection)
            throws IOException {
        boolean result = false;
        final MessageReader messageReader = MessageReader.createMessageReader();
        final Message responseMessage = messageReader.readSTUNMessage(controlConnection.getInputStream());
        if (responseMessage.isMethod(method) && responseMessage.isSuccessResponse()) {
            result = true;
        }
        return result;
    }
}
