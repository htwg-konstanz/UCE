/*
    Copyright (c) 2012 Thomas Zink, 

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fhkn.in.uce.reversal.target;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.messages.CommonUceMethod;
import de.fhkn.in.uce.messages.SocketEndpoint;
import de.fhkn.in.uce.messages.UceMessage;
import de.fhkn.in.uce.messages.UceMessageReader;

/**
 * Class which contains the listener thread of the connection reversal target.
 * This class is used by ConnectionReversalTarget class as listener thread.
 * 
 * Listens for connection request and keep alive messages.
 * Incoming connection request sockets are added to blockingSocketQueue.
 * The content of blockingSocketQueue can be read by calling the accept-
 * Method of the ConnectionReversalTarget class.
 * 
 * @author thomas zink, stefan lohr
 */
public class ListenerThread extends Thread {
	
	private BlockingQueue<Socket> blockingSocketQueue;
	private DatagramSocket datagramSocket;
	private UceMessage uceRequestMessage;
	private static final Logger logger = LoggerFactory.getLogger(ListenerThread.class);
	public boolean isReceiving;
	
	/**
	 * Constructor of the ListenerThread which is used by ConnectionReversalTarget
	 * for receiving and handling incoming messages for the mediator.
	 * 
	 * @param datagramSocket Socket for receiving messages from mediator
	 * @param blockingSocketQueue Queue for adding new connection for source
	 */
	public ListenerThread(DatagramSocket datagramSocket, BlockingQueue<Socket> blockingSocketQueue) {
		isReceiving = false;
		this.datagramSocket = datagramSocket;
		this.blockingSocketQueue = blockingSocketQueue;
	}
	
	/**
	 * Run-Method of the listenerThread.
	 * It contains a while-loop for receiving messages.
	 */
	public void run() {
		try {
			boolean isSocketTimeoutException;
			datagramSocket.setSoTimeout(1000);
			
			while (!isInterrupted()) {
				isSocketTimeoutException = true;
				
				logger.info("listen on {}:{}", datagramSocket.getLocalPort(),
						datagramSocket.getLocalAddress().getHostAddress());
				
				DatagramPacket datagramPacket = new DatagramPacket(new byte[65536], 65536);
				
				while (!isInterrupted() && isSocketTimeoutException) {
					try {
						isReceiving = true;
						datagramSocket.receive(datagramPacket);
						isReceiving = false;
						isSocketTimeoutException = false;
					}
					catch (SocketTimeoutException e) {
						isSocketTimeoutException = true;
						isReceiving = false;
					}
				}
				
				if (!isInterrupted() && !isSocketTimeoutException) {
					byte[] data = datagramPacket.getData();
					UceMessageReader uceMessageReader = new UceMessageReader();
					uceRequestMessage = uceMessageReader.readUceMessage(data);
					
					if (uceRequestMessage.isMethod(CommonUceMethod.CONNECTION_REQUEST)) connectionRequest();
					else if (uceRequestMessage.isMethod(CommonUceMethod.KEEP_ALIVE)) keepAlive();
					else logger.error("unknown message");
				}
			}
		}
		catch (Exception e) {
			Thread.currentThread().interrupt();
			logger.error(e.getMessage());
		}
		finally {
			try {
				isReceiving = false;
				datagramSocket.setSoTimeout(0);
			}
			catch (SocketException e) {
				logger.error(e.getMessage());
			}
		}
	}
	
	/**
	 * Private method for handling keep alive messages
	 */
	private void keepAlive() {
		/**
		 * TODO: check if mediator still alive
		 */
		
		if (uceRequestMessage.isSuccessResponse()) logger.info("keepAlive response message received");
		else logger.error("keepAlive successResponse message expacted");
	}
	
	/**
	 * Private method for handling connection requests
	 */
	private void connectionRequest() {
		SocketEndpoint socketEndpoint = uceRequestMessage.getAttribute(SocketEndpoint.class);
		
		logger.info("ConnectionRequest message from {} with uuid {}",
				socketEndpoint.getEndpoint().toString(), uceRequestMessage.getTransactionId().toString());
		
		try {
			String hostName = socketEndpoint.getEndpoint().getHostName();
			int portNumber = socketEndpoint.getEndpoint().getPort();
			
			logger.info("create socket connection {}:{}", hostName, portNumber);
			Socket socket = new Socket(hostName, portNumber);
			blockingSocketQueue.add(socket);
		}
		catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
}
