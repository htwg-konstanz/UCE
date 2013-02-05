/**
 * 
 */


package de.fhkn.in.net;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A blocking Server that can only handle a single connection.
 * 
 * @author tzink
 */
public final class BlockingServer implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(BlockingServer.class);
	
	public static final int DEFAULT_PORT = 10101;
	private ServerSocket server;
	
	public ServerSocket getServer() {
		return server;
	}

	public BlockingServer() {
		this(DEFAULT_PORT);
	}
	
	public BlockingServer(int port) {
		try {
			server = new ServerSocket(port);
			logger.info("Serving on " + server.toString());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	public void run() {
		Socket client;
		while (true){
			try {
				client = server.accept();
				logger.info("[INFO] client connected " + client.toString());
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
	}
	
	public static void main(String[] args) {
		int port = BlockingServer.DEFAULT_PORT;
		if (args.length == 1) {
			port = Integer.parseInt(args[0]);
		}
		BlockingServer server = new BlockingServer(port);
		server.run();
	}
}
