package de.fhkn.in.test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class TestServer implements Runnable{
	
	private ServerSocket server;
	private Socket client;
	
	public TestServer(){
		try {
			server = new ServerSocket(Constants.SERVER_PORT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		
		while (true){
			try {
				client = server.accept();
				System.out.println("TestServer: client connected to Server");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
}
