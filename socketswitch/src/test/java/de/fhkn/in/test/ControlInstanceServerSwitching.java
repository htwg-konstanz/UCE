package de.fhkn.in.test;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import de.fhkn.in.net.SwitchableSocket;
/**
 * This class is in charge of initializing the switch. But with each switch the port is changing.
 * @author Steven B�ckle
 * 
 */
public class ControlInstanceServerSwitching implements Runnable {
	private SwitchableSocket client;
	public boolean finished = false;

	public ControlInstanceServerSwitching(SwitchableSocket client) {
		this.client = client;
	}

	@Override
	public void run() {
		int i = 0;
		while (true) {
			try {
				int randomTime = (int) (Math.random() * 3000) + 1500;
				System.out.println(randomTime + " milisecs till next switch");
				Thread.sleep(randomTime);
				if (!finished) {
					Socket newClient;
					if(i % 2 == 0){
						newClient = new Socket(Constants.SERVER_ADDRESS, Constants.SERVER_PORT);
					}else newClient = new Socket(Constants.SERVER_ADDRESS, Constants.SERVER_PORT2);
					client.switchSocket(newClient);
					i++;
				}

			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				System.out.println("going out now...");
				try {
					//Tell the Server, that the file was transferred completely,
					//for that shut down the Output Only, because the 
					//Input is listening for an answer from the server
					client.shutdownOutput();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				break;
			}

		}

	}

}
