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
package de.fhkn.in.uce.reversal.mediator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.messages.CommonUceMethod;
import de.fhkn.in.uce.messages.MessageFormatException;
import de.fhkn.in.uce.messages.SemanticLevel;
import de.fhkn.in.uce.messages.SocketEndpoint;
import de.fhkn.in.uce.messages.SocketEndpoint.EndpointClass;
import de.fhkn.in.uce.messages.UceMessage;
import de.fhkn.in.uce.messages.UceMessageReader;
import de.fhkn.in.uce.messages.UceMessageStaticFactory;
import de.fhkn.in.uce.messages.UniqueUserName;

/**
 * Handle UCE messages
 * 
 * @author thomas zink, stefan lohr
 */
public class HandleMessage implements Runnable {
	private DatagramSocket datagramSocket;
	private UceMessage uceRequestMessage;
	private InetAddress sourceAddress;
	private int sourcePort;
	private byte[] data;
	private static final Logger logger = LoggerFactory.getLogger(HandleMessage.class);
	
	/**
	 * @param datagramPacket message datagram
	 * @param datagramSocket response socket
	 */
	public HandleMessage(DatagramPacket datagramPacket, DatagramSocket datagramSocket) {
		sourceAddress = datagramPacket.getAddress();
		sourcePort = datagramPacket.getPort();
		data = datagramPacket.getData();
		this.datagramSocket = datagramSocket;
	}
	
	/**
	 * 
	 */
	public void run() {
		UceMessageReader uceMessageReader = new UceMessageReader();
		
		try {
			uceRequestMessage = uceMessageReader.readUceMessage(data);
			logger.info("message received ({})", uceRequestMessage.getTransactionId());
			
			if (uceRequestMessage.isMethod(CommonUceMethod.CONNECTION_REQUEST)) connectionRequest();
			else if (uceRequestMessage.isMethod(CommonUceMethod.REGISTER)) register();
			else if (uceRequestMessage.isMethod(CommonUceMethod.KEEP_ALIVE)) keepAlive();
			else if (uceRequestMessage.isMethod(CommonUceMethod.DEREGISTER)) deregister();
			else if (uceRequestMessage.isMethod(CommonUceMethod.LIST)) list();
			else {
				logger.info("unknown message ({})", uceRequestMessage.getMethod().toString());
				sendErrorResponse(uceRequestMessage, 0, "unknown message");
			}
		}
		catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
	
	/**
	 * deregister user and send response.
	 * 
	 * @throws IOException 
	 */
	private void deregister() throws IOException {		
		UniqueUserName uniqueUserName;
		String userName;
		
		if (!uceRequestMessage.hasAttribute(UniqueUserName.class)) {
			sendErrorResponse(uceRequestMessage, 0, "UniqueUserName attribute Expected");
			throw new MessageFormatException("UniqueUserName attribute Expected");
		}
		
		uniqueUserName = uceRequestMessage.getAttribute(UniqueUserName.class);
		userName = uniqueUserName.getUniqueUserName();
		
		logger.info("handle deregister message ({})", userName);
		UserList.getInstance().removeUser(userName);
		
		logger.info("send deregister success responde ({})", userName);
		sendSuccessResponse(uceRequestMessage);
	}
	
	/**
	 * list all registered users.
	 * 
	 * @throws IOException
	 */
	private void list() throws IOException {
		/*
		 * TODO:
		 * What if packet > 65536 (512) bytes?
		 * Possible results:
		 * - multiple messages with 1..9 users and fragmentation flag / seq numbers
		 * - send over tcp
		 * 
		 * 512 bytes allow max 9 users per message
		 * #########################################################
		 *  20 Bytes header
		 *  52 Bytes UniqueUserName (UserName[48]+Header[4])
		 * --------------------------------------...
		 *  72 Bytes per user
		 * ---------------------------------
		 * 488 Bytes with 9 user
		 * ==============================
		 * 512 - 488 = 24 Bytes for flags / seq nums.
		 * remark: would also require out-of-order handling ...
		 */
		
		UUID uuid = uceRequestMessage.getTransactionId();
		
		UceMessage uceResponseMessage = UceMessageStaticFactory.newUceMessageInstance(
				CommonUceMethod.LIST, SemanticLevel.SUCCESS_RESPONSE, uuid);
		
		logger.info("create list of users");
		for (String userName : UserList.getInstance().getUserNames()) {
			UniqueUserName uniqueUserName = new UniqueUserName(userName);
			uceResponseMessage.addAttribute(uniqueUserName);
		}
		
		byte[] buf = uceResponseMessage.toByteArray();
		DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, sourceAddress, sourcePort);
		
		logger.info("send list of users");
		datagramSocket.send(datagramPacket);
	}
	
	/**
	 * Process connection requests.
	 * 
	 * Address information for the connection is contained within the message.
	 * The port can be extracted from the ListenerSocket, the IP is extracted
	 * from the sent message, since it is not available in advance.
	 * 
	 * The address is sent to the user identified in the message.
	 * 
	 * If a requesting target has different network interfaces for UDP and TCP
	 * this method does not work due to different ports and addresses.
	 * 
	 * @throws IOException 
	 */
	private void connectionRequest() throws IOException {
		if (!uceRequestMessage.hasAttribute(SocketEndpoint.class)) {
			sendErrorResponse(uceRequestMessage, 0, "SocketEndpoint attribute Expected");
			throw new MessageFormatException("SocketEndpoint attribute Expected");
		}
		
		if (!uceRequestMessage.hasAttribute(UniqueUserName.class)) {
			sendErrorResponse(uceRequestMessage, 0, "UniqueUserName attribute Expected");
			throw new MessageFormatException("UniqueUserName attribute Expected");
		}
		
		SocketEndpoint socketEndpoint = uceRequestMessage.getAttribute(SocketEndpoint.class);
		UniqueUserName uniqueUserName = uceRequestMessage.getAttribute(UniqueUserName.class);
		String userName = uniqueUserName.getUniqueUserName();
		
		logger.info("handle connectionRequest ({})", userName);
		
		UserData userData = UserList.getInstance().getUser(userName);
		
		if (userData == null) {
			sendErrorResponse(uceRequestMessage, 0, "unknown user");
			throw new MessageFormatException("unknown user");
		}
		
		InetSocketAddress targetAddress = userData.getInetSocketAddress();
		UUID uuid = uceRequestMessage.getTransactionId();
		
		UceMessage uceResponseMessage = UceMessageStaticFactory.newUceMessageInstance(
				CommonUceMethod.CONNECTION_REQUEST, SemanticLevel.REQUEST, uuid);
		
		int tcpSourcePort = socketEndpoint.getEndpoint().getPort();
		EndpointClass endpointClass = SocketEndpoint.EndpointClass.CONNECTION_REVERSAL;
		InetSocketAddress newEndpoint = new InetSocketAddress(sourceAddress, tcpSourcePort);
		SocketEndpoint newSocketEndpoint = new SocketEndpoint(newEndpoint, endpointClass);
		
		uceResponseMessage.addAttribute(newSocketEndpoint);
		
		byte[] buf = uceResponseMessage.toByteArray();
		
		DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
		datagramPacket.setAddress(targetAddress.getAddress());
		datagramPacket.setPort(targetAddress.getPort());
		
		logger.info("send successResponse and tcp address {}:{} to target", sourceAddress, tcpSourcePort);
		datagramSocket.send(datagramPacket);
	}
	
	/**
	 * keep-alive registered user.
	 * 
	 * If source address changes it is updated in the DB alongside the timestamp. 
	 * Responses have same ID as requests.
	 * Response is needed to signal arrival as well as keep NAT mappings alive
	 * 
	 * @throws IOException 
	 */
	private void keepAlive() throws IOException {
		
		UniqueUserName uniqueUserName;
		String userName;
		
		if (!uceRequestMessage.hasAttribute(UniqueUserName.class)) {
			sendErrorResponse(uceRequestMessage, 0, "UniqueUserName attribute Expected");
			throw new MessageFormatException("UniqueUserName attribute Expected");
		}
		
		uniqueUserName = uceRequestMessage.getAttribute(UniqueUserName.class);
		userName = uniqueUserName.getUniqueUserName();
		
		UserData userData = UserList.getInstance().getUser(userName);
		InetSocketAddress inetSocketAddress = new InetSocketAddress(sourceAddress, sourcePort);
		
		if (userData == null) {
			logger.info("unknown UserData of keepAlive message for {}", userName);
			sendErrorResponse(uceRequestMessage, 0, "unknown user");
			throw new MessageFormatException("unknown user");
		}
		else if (!userData.getInetSocketAddress().equals(inetSocketAddress)) {
			userData = new UserData(userName, inetSocketAddress);
			UserList.getInstance().updateUser(userData);
			logger.info("handle keepAlive message ({}) [update]", userName);
		}
		else {
			UserList.getInstance().refreshUserTimeStamp(userName);
			logger.info("handle keepAlive message ({}) [refresh]", userName);
		}
		
		logger.info("send keepAlive success responde ({})", userName);
		sendSuccessResponse(uceRequestMessage);
	}
	
	/**
	 * Register a user at mediator and send response.
	 * 
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws MessageFormatException
	 */
	private void register() throws IOException {
		InetSocketAddress inetSocketAddress;
		UniqueUserName uniqueUserName;
		UserData userData;
		String userName;
		
		/*
		 * TODO: what if user already exists?
		 */
		
		if (!uceRequestMessage.hasAttribute(UniqueUserName.class)) {
			sendErrorResponse(uceRequestMessage, 0, "UniqueUserName attribute Expected");
			throw new MessageFormatException("UniqueUserName attribute Expected");
		}
		
		uniqueUserName = uceRequestMessage.getAttribute(UniqueUserName.class);
		userName = uniqueUserName.getUniqueUserName();
		
		inetSocketAddress = new InetSocketAddress(sourceAddress, sourcePort);
		userData = new UserData(userName, inetSocketAddress);
		
		logger.info("handle register message ({})", userName);
		UserList.getInstance().addUser(userData);
		
		logger.info("send register success responde ({})", userName);
		sendSuccessResponse(uceRequestMessage);
	}
	
	/**
	 * Send success responses
	 * 
	 * @param uceMessage Muss die Anfrage-Nachricht enthalten
	 * @throws IOException
	 */
	private void sendSuccessResponse(UceMessage uceMessage) throws IOException {
		byte[] buffer;
		UceMessage uceResponseMessage;
		DatagramPacket datagramPacket;
		
		uceResponseMessage = uceRequestMessage.buildSuccessResponse();
		buffer = uceResponseMessage.toByteArray();
		
		datagramPacket = new DatagramPacket(buffer, buffer.length);
		datagramPacket.setAddress(sourceAddress);
		datagramPacket.setPort(sourcePort);
		
		datagramSocket.send(datagramPacket);
	}
	
	/**
	 * Send ErrorResponse Message with ErrorCode and ErrorMessage to sender
	 * 
	 * @param uceMessage UceRequestMessage
	 * @param errorCode Number of Error-Code
	 * @param errorMessage Message of the error
	 * @throws IOException
	 */
	private void sendErrorResponse(UceMessage uceMessage, int errorCode, String errorMessage) throws IOException {
		
		byte[] buffer;
		UceMessage uceResponseMessage;
		DatagramPacket datagramPacket;
		
		uceResponseMessage = uceRequestMessage.buildErrorResponse(errorCode, errorMessage);
		
		buffer = uceResponseMessage.toByteArray();
		
		datagramPacket = new DatagramPacket(buffer, buffer.length);
		datagramPacket.setAddress(sourceAddress);
		datagramPacket.setPort(sourcePort);
		
		datagramSocket.send(datagramPacket);
	}
}
