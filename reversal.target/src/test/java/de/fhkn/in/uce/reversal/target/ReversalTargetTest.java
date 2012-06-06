/**
 * Copyright (C) 2011 Stefan Lohr
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fhkn.in.uce.reversal.target;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.fhkn.in.test.ThreadedExceptionRunner;
import de.fhkn.in.uce.messages.CommonUceMethod;
import de.fhkn.in.uce.messages.MessageFormatException;
import de.fhkn.in.uce.messages.SemanticLevel;
import de.fhkn.in.uce.messages.SocketEndpoint;
import de.fhkn.in.uce.messages.SocketEndpoint.EndpointClass;
import de.fhkn.in.uce.messages.UceMessage;
import de.fhkn.in.uce.messages.UceMessageReader;
import de.fhkn.in.uce.messages.UceMessageStaticFactory;
import de.fhkn.in.uce.messages.UniqueUserName;

@RunWith (value = ThreadedExceptionRunner.class)
public class ReversalTargetTest {
	
	static String mediatorIP;
	static String uniqueUserName;
	static int mediatorPort;
	static int iterationTime;
	static ConnectionReversalTarget connectionReversalTarget;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		mediatorIP = "127.0.0.1";
		uniqueUserName = "target";
		mediatorPort = 11111;
		iterationTime = 20;
	}
	
	@Before
	public void setUpBeforeMethod() throws Exception {
		connectionReversalTarget = new ConnectionReversalTarget(uniqueUserName, iterationTime, mediatorIP, mediatorPort);
	}
	
	@Test
	public void registerUserWithMediator() throws Exception {
		MediatorStub mediatorStub = new MediatorStub();
		mediatorStub.start();
		connectionReversalTarget.register();
		mediatorStub.interrupt();
	}
	
	@Test (expected=IOException.class)
	public void registerUserNoMediator() throws Exception {
		connectionReversalTarget.register();
	}
	
	@Test
	public void deregisterUnregistered() throws Exception {
		
		MediatorStub mediatorStub = new MediatorStub();
		mediatorStub.start();
		
		try {
			connectionReversalTarget.deregister();
			Assert.assertTrue(false);
		}
		catch (IllegalStateException e) {
			mediatorStub.interrupt();
			Assert.assertTrue(true);
		}
	}
	
	@Test
	public void deregisterRegistered() throws Exception {
		// register
		MediatorStub mediatorStub = new MediatorStub();
		mediatorStub.start();
		connectionReversalTarget.register();
		mediatorStub.interrupt();
		
		//deregister
		mediatorStub = new MediatorStub();
		mediatorStub.start();
		connectionReversalTarget.deregister();
		mediatorStub.interrupt();
	}
	
	@Test
	public void connetUserSuccess() throws Exception {
		
		MediatorStub mediatorStub = new MediatorStub();
		mediatorStub.start();
		connectionReversalTarget.register();
		
		int port = connectionReversalTarget.getListenerPort();
		ServerSocket serverSocket = new ServerSocket(12345);
		
		UceMessage uceConnectionRequestMessage = UceMessageStaticFactory.newUceMessageInstance(
				CommonUceMethod.CONNECTION_REQUEST, SemanticLevel.REQUEST, UUID.randomUUID());
		
		InetSocketAddress endpoint = new InetSocketAddress("127.0.0.1", 12345);
		EndpointClass endpointClass = SocketEndpoint.EndpointClass.CONNECTION_REVERSAL;
		SocketEndpoint socketEndpoint = new SocketEndpoint(endpoint, endpointClass);
		
		uceConnectionRequestMessage.addAttribute(new UniqueUserName(uniqueUserName));
		uceConnectionRequestMessage.addAttribute(socketEndpoint);
		
		byte[] buf = uceConnectionRequestMessage.toByteArray();
		DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, endpoint.getAddress(), port);
		DatagramSocket datagramSocket = new DatagramSocket();
		datagramSocket.send(datagramPacket);
		
		serverSocket.setSoTimeout(3000);
		Socket socketSource = null;
		try {
			socketSource = serverSocket.accept();
		}
		catch (SocketTimeoutException e) {
			serverSocket.setSoTimeout(0);
			throw new SocketTimeoutException();
		}
		finally {
			serverSocket.close();
		}
		
		mediatorStub = new MediatorStub();
		mediatorStub.start();
		
		Socket socketTarget = connectionReversalTarget.accept();
		
		OutputStream outputStream = socketSource.getOutputStream();
		outputStream.write("string from source\r\n".getBytes());
		outputStream.flush();
		
		InputStream inputStream = socketTarget.getInputStream();
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		
		Assert.assertEquals("string from source", bufferedReader.readLine());
		
		mediatorStub.interrupt();
	}
	
	@Test (expected=IllegalStateException.class)
	public void connetUserNotRegistered() throws Exception {
		MediatorStub mediatorStub = new MediatorStub();
		mediatorStub.start();
		connectionReversalTarget.accept();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		connectionReversalTarget = null;
	}
	
	class MediatorStub extends Thread {
		
		private InetAddress sourceAddress;
		private int sourcePort;
		private UceMessage uceRequestMessage;
		private DatagramSocket datagramSocket;
		private DatagramPacket datagramPacket;
		
		public MediatorStub() throws SocketException {
			datagramSocket = new DatagramSocket(11111);
			datagramPacket = new DatagramPacket(new byte[512], 512);
		}
		
		public MediatorStub(String registeredUserName) throws SocketException {
			datagramSocket = new DatagramSocket(11111);
			datagramPacket = new DatagramPacket(new byte[512], 512);
		}
		
		public void run() {
			try {
				datagramSocket.receive(datagramPacket);
				sourceAddress = datagramPacket.getAddress();
				sourcePort = datagramPacket.getPort();
				byte[] data = datagramPacket.getData();
				
				UceMessageReader uceMessageReader = new UceMessageReader();
				uceRequestMessage = uceMessageReader.readUceMessage(data);
				
				if (uceRequestMessage.isMethod(CommonUceMethod.REGISTER)) register();
				else if (uceRequestMessage.isMethod(CommonUceMethod.DEREGISTER)) deregister();
			}
			catch (IOException e) {
				// e.printStackTrace();
			}
			finally {
				datagramSocket.close();
			}
		}
		
		private void register() throws IOException {
			if (!uceRequestMessage.hasAttribute(UniqueUserName.class)) {
				sendErrorResponse(uceRequestMessage, 0, "UniqueUserName attribute Expected");
				throw new MessageFormatException("UniqueUserName attribute Expected");
			}
			
			sendSuccessResponse(uceRequestMessage);
		}
		
		private void deregister() throws IOException {
			if (!uceRequestMessage.hasAttribute(UniqueUserName.class)) {
				sendErrorResponse(uceRequestMessage, 0, "UniqueUserName attribute Expected");
				throw new MessageFormatException("UniqueUserName attribute Expected");
			}
			sendSuccessResponse(uceRequestMessage);
		}
		
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

		public void interrupt() {
			try {
				datagramSocket.close();
			}
			catch (Exception e) {
				
			}
			super.interrupt();
		}
	}
}
