/*
 * Copyright (c) 2012 Thomas Zink,
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
package de.fhkn.in.uce.reversal.target;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Enumeration;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.messages.CommonUceMethod;
import de.fhkn.in.uce.messages.SemanticLevel;
import de.fhkn.in.uce.messages.UceMessage;
import de.fhkn.in.uce.messages.UceMessageReader;
import de.fhkn.in.uce.messages.UceMessageStaticFactory;
import de.fhkn.in.uce.messages.UniqueUserName;

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
 * @author thomas zink, stefan lohr
 */
public class ConnectionReversalTarget {

    private DatagramSocket datagramSocket;
    private SocketAddress socketAddress;
    private KeepAliveThread keepAliveThread;
    private BlockingQueue<Socket> blockingSocketQueue;
    private ListenerThread listenerThread;
    private int iterationTime;
    private static final Logger logger = LoggerFactory.getLogger(ConnectionReversalTarget.class);
    private boolean registered;
    private String uniqueUserName;

    private Socket socketToMediator;
    private static final int TARGET_SIDE_LISTENER_PORT = 60927;

    /**
     * Private Constructor, can only be called by using inner
     * configuration-class.
     * 
     * @param configuration
     *            Builder-Configuration for initialization
     */
    public ConnectionReversalTarget(String uniqueUserName, int iterationTime, String mediatorIP, int mediatorPort) {
        this.registered = false;
        this.uniqueUserName = uniqueUserName;
        this.iterationTime = iterationTime;

        blockingSocketQueue = new LinkedBlockingQueue<Socket>();

        try {
            logger.debug("creating mediator connection");
            this.socketToMediator = new Socket();
            this.socketToMediator.setReuseAddress(true);
            this.socketToMediator.bind(new InetSocketAddress(getEth0Address(), TARGET_SIDE_LISTENER_PORT));
            this.socketToMediator.connect(new InetSocketAddress(mediatorIP, mediatorPort));
            logger.debug("connection to mediator established");

            // socketAddress = new InetSocketAddress(mediatorIP, mediatorPort);
            // datagramSocket = new DatagramSocket();
        } catch (Exception e) {
            logger.error("could not connect to mediator {}:{}", mediatorIP, mediatorPort);
            logger.error(e.getMessage());
        }
    }

    /**
     * Method for deregistration of the target on the mediator
     * 
     * @throws Exception
     */
    public void deregister() throws Exception {
        //
        // if (!registered) {
        // IllegalStateException e = new
        // IllegalStateException("not yet registered");
        // logger.error(e.getMessage());
        // throw e;
        // }
        //
        // listenerThread.interrupt();
        // keepAliveThread.interrupt();
        // blockingSocketQueue.clear();
        //
        // UceMessage uceDeregisterMessage =
        // UceMessageStaticFactory.newUceMessageInstance(
        // CommonUceMethod.DEREGISTER, SemanticLevel.REQUEST,
        // UUID.randomUUID());
        //
        // uceDeregisterMessage.addAttribute(new
        // UniqueUserName(uniqueUserName));
        //
        // byte[] buf = uceDeregisterMessage.toByteArray();
        //
        // DatagramPacket datagrammPacket = new DatagramPacket(buf, buf.length,
        // socketAddress);
        //
        // while (listenerThread.isReceiving) Thread.sleep(100);
        //
        // logger.info("send deregister message to mediator");
        // datagramSocket.send(datagrammPacket);
        //
        // if (waitForSuccessResponse(CommonUceMethod.DEREGISTER)) {
        // logger.info("success message for deregistration received");
        // }
        // else {
        // logger.error("no success message for deregistration received");
        // throw new Exception("Could not deregister");
        // }
    }

    /**
     * Method for registration of the target on the mediator
     * 
     * @throws Exception
     */
    public void register() throws Exception {

        if (registered)
            throw new IllegalStateException("already registered");

        UceMessage uceRegisterMessage = UceMessageStaticFactory.newUceMessageInstance(CommonUceMethod.REGISTER,
                SemanticLevel.REQUEST, UUID.randomUUID());

        uceRegisterMessage.addAttribute(new UniqueUserName(uniqueUserName));

        logger.info("send register message to mediator");
        uceRegisterMessage.writeTo(this.socketToMediator.getOutputStream());

        if (waitForSuccessResponse(CommonUceMethod.REGISTER)) {

            logger.info("success message for registration received");

            // keepAliveThread = new KeepAliveThread(uniqueUserName,
            // datagramSocket, socketAddress, iterationTime);
            keepAliveThread = new KeepAliveThread(uniqueUserName, socketToMediator, iterationTime);
            listenerThread = new ListenerThread(socketToMediator, blockingSocketQueue);

            logger.info("start listenerThread");
            listenerThread.start();

            logger.info("start keepAliveThread");
            keepAliveThread.start();

            registered = true;
        } else {
            logger.error("no success message received");
            throw new IOException("Could not register");
        }
    }

    /**
     * Method for accepting incoming connection from the source-side
     * 
     * @return Socket of the ConnectionReversal connection
     * @throws InterruptedException
     */
    public Socket accept() throws InterruptedException {
        /**
         * no successResponses from keepAliveThread -> empty socket? should be
         * checked
         */
        if (!registered) {
            logger.error("not yet registered");
            throw new IllegalStateException("not yet registered");
        }

        return blockingSocketQueue.take();
    }

    // /**
    // * Method to get the local listener port of the listenerThread which
    // listens
    // * for incoming connection requests of the source-side.
    // *
    // * @return int of the port number
    // */
    // public int getListenerPort() {
    // return datagramSocket.getLocalPort();
    // }

    /**
     * Private method for waiting for success response messages. This method
     * waits for the correct response message of the request messages from
     * parameter. After receiving message or a timeout this method returns true
     * or false.
     * 
     * @param commonUceMethod
     *            Message-Type
     * @return true or false, dependent on if the received message is correct
     * @throws IOException
     */
    private boolean waitForSuccessResponse(CommonUceMethod commonUceMethod) throws IOException {
        // DatagramPacket datagramPacket = new DatagramPacket(new byte[65536],
        // 65536);
        //
        // datagramSocket.setSoTimeout(10000);
        // try {
        // datagramSocket.receive(datagramPacket);
        // } catch (SocketTimeoutException ste) {
        // datagramSocket.setSoTimeout(0);
        // return false;
        // } finally {
        // datagramSocket.setSoTimeout(0);
        // }
        //
        // byte[] data = datagramPacket.getData();

        UceMessageReader uceMessageReader = new UceMessageReader();
        UceMessage uceResponseMessage = uceMessageReader.readUceMessage(this.socketToMediator.getInputStream());

        if (uceResponseMessage.isMethod(commonUceMethod) && uceResponseMessage.isSuccessResponse())
            return true;
        else
            return false;
    }

    private InetAddress getEth0Address() throws Exception {
        InetAddress result = null;
        final NetworkInterface eth0 = NetworkInterface.getByName("eth0"); //$NON-NLS-1$
        final Enumeration<InetAddress> eth0Addresses = eth0.getInetAddresses();
        while (eth0Addresses.hasMoreElements()) {
            final InetAddress eth0Address = eth0Addresses.nextElement();
            if (eth0Address instanceof Inet4Address) {
                result = eth0Address;
                break;
            }
        }
        logger.debug("eth0IPv4Adress=" + result.toString());
        return result;
    }
}
