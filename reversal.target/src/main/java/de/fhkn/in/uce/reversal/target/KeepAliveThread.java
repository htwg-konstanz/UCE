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
import java.net.SocketAddress;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.messages.CommonUceMethod;
import de.fhkn.in.uce.messages.SemanticLevel;
import de.fhkn.in.uce.messages.UceMessage;
import de.fhkn.in.uce.messages.UceMessageStaticFactory;
import de.fhkn.in.uce.messages.UniqueUserName;

/**
 * This class is part of the ConnectionReversalTarget class.
 * It sends every iterationTime a keep alive message to mediator. 
 * 
 * @author thomas zink, stefan lohr
 */
public class KeepAliveThread extends Thread {
	private static final Logger logger = LoggerFactory.getLogger(KeepAliveThread.class);
	private String userName;
	private int iterationTime;
	private SocketAddress socketAddress;
	private DatagramSocket datagramSocket;
	
	public KeepAliveThread(String userName, DatagramSocket datagramSocket, SocketAddress socketAddress, int iterationTimeInSeconds) {
		this.userName = userName;
		this.datagramSocket = datagramSocket;
		this.socketAddress = socketAddress;
		this.iterationTime = iterationTimeInSeconds * 1000;
	}
	
	public void run() {
		
		DatagramPacket datagrammPacket;
		
		UceMessage uceMessage = UceMessageStaticFactory.newUceMessageInstance(
				CommonUceMethod.KEEP_ALIVE, SemanticLevel.REQUEST, UUID.randomUUID());
		
		try {
			uceMessage.addAttribute(new UniqueUserName(userName));
			byte[] buf = uceMessage.toByteArray();
			datagrammPacket = new DatagramPacket(buf, buf.length, socketAddress);
			
			while (!isInterrupted()) {
				try {
					Thread.sleep(iterationTime);
					datagramSocket.send(datagrammPacket);
				}
				catch (InterruptedException e) { Thread.currentThread().interrupt(); }
			}
		}
		catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
}
