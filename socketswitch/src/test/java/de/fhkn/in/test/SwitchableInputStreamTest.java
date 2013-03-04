package de.fhkn.in.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fhkn.in.io.SwitchableInputStream;
import de.fhkn.in.net.SwitchableSocket;

public class SwitchableInputStreamTest {
	private static TestServer testServer;
	private static Thread serverThread;
	private static SwitchableInputStream switchableInputStream;
	private static Socket s;
	
	@BeforeClass 
	public static void setUpBeforeClass(){
		testServer = new TestServer();
		serverThread = new Thread(testServer);
		serverThread.start();
	}
	
	@AfterClass 
	public static void tearDownAfterClass(){
		serverThread.interrupt();
	}
	

	@Before
	public void setUp(){
		try {
			s = new Socket("localhost",Constants.SERVER_PORT);
			SwitchableSocket switchableSocket = new SwitchableSocket(s);
			switchableInputStream = switchableSocket.getInputStream();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testPutNewInputStream() throws UnknownHostException, IOException {
		BlockingQueue<InputStream> newInputStreams = (BlockingQueue<InputStream>) PrivateAccessor.getPrivateField(switchableInputStream, "newInputStreams");
		// before switch queue has to be empty
		assertTrue(newInputStreams.isEmpty());		
		// switch the underlying input stream
		Socket socket = new Socket("localhost", Constants.SERVER_PORT);
		InputStream newInputStream = socket.getInputStream();		
		switchableInputStream.putNewInputStream(newInputStream);
		// after switch queue has to have elements
		assertFalse(newInputStreams.isEmpty());
		System.out.println("PutNewInputStreamTest ended");
		socket.close();
	}

	@Test
	public void testInternStreamSwitch() throws UnknownHostException, IOException {
		BlockingQueue<InputStream> newInputStreams = (BlockingQueue<InputStream>) PrivateAccessor.getPrivateField(switchableInputStream, "newInputStreams");
		InputStream oldIn = (InputStream) PrivateAccessor.getPrivateField(switchableInputStream,"inputStream");
		Socket socket = new Socket("localhost", Constants.SERVER_PORT);
		InputStream newInputStream = socket.getInputStream();		
		switchableInputStream.putNewInputStream(newInputStream);		
		switchableInputStream.internStreamSwitch();
		InputStream newIn = (InputStream) PrivateAccessor.getPrivateField(switchableInputStream,"inputStream");
		assertFalse(oldIn.equals(newIn));
		System.out.println("InternStreamSwitchTest ended");
		socket.close();
	}

}
