package de.fhkn.in.io;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fhkn.in.net.BlockingServer;

/**
 * Unit test of the SwitchableInputStream.
 * 
 * @author tzink, sboeckle
 *
 */
public class SwitchableInputStreamTest {
	private static int DEFAULT_PORT;
	
	private static BlockingServer server;
	private static Thread serverThread;

	@BeforeClass 
	public static void setUpBeforeClass(){
		server = new BlockingServer();
		serverThread = new Thread(server);
		serverThread.start();
	}
	
	@AfterClass 
	public static void tearDownAfterClass(){
		serverThread.interrupt();
	}
	
	@Before
	public void setUp(){
		try {
			Socket socket = new Socket("localhost", DEFAULT_PORT);
			SwitchableInputStream swinstream = new SwitchableInputStream(socket.getInputStream());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
		
	@Test
	public void test() {
		fail("Not yet implemented");
	}

}
