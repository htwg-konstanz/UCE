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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main-Class which starts the Mediator functionality
 * 
 * Mediator is an public accessible Server where clients (targets) can be registered.
 * If another client (source) wants to access to a registered client (target)
 * it sends a request to Mediator, which will help to connect these clients.
 * 
 * For connecting the clients it is necessary that one of those clients (target) runs
 * as ConnectionReversalTarget and the other client (source) runs as ConnectionReversalSource.
 * 
 * The target client can be behind a NAT-Router, the source client must be accessible for the public
 * 
 * @author thomas zink, stefan lohr
 */
public class Mediator {
	private static final Logger logger = LoggerFactory.getLogger("Mediator");
	
	/**
	 * @param args arg0: listenerPort;  arg1: iterationTimeInSeconds; arg2: maxLifeTimeInSeconds
	 */
	public static void main(String[] args) {
		
		int listenerPort;
		int iterationTimeInSeconds;
		int maxLifeTimeInSeconds;
		
		if (args.length != 3) {
			logger.error("Illegal count of arguments, exact three arguments expected");
			logger.error("listenerPort, iterationTimeInSeconds, maxLifeTimeInSeconds");
			System.exit(1);
		}
		
		try {
			listenerPort = Integer.parseInt(args[0]);
			iterationTimeInSeconds = Integer.parseInt(args[1]);
			maxLifeTimeInSeconds = Integer.parseInt(args[2]);
		}
		catch (Exception e) {
			logger.error("Illegal arguments, numbers expected");
			System.exit(2);
			return;
		}
		
		ListenerThread listenerThread = new ListenerThread(listenerPort);
		UserCleaner userCleaner = new UserCleaner(iterationTimeInSeconds, maxLifeTimeInSeconds);
		
		listenerThread.start();
		userCleaner.run();
	}
}
