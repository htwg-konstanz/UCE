package de.fhkn.in.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fhkn.in.io.SwitchableOutputStream;
import de.fhkn.in.net.SwitchableSocket;

public class SwitchableOutputStreamTest {
	private static TestServer testServer;
	private static Thread serverThread;
	private static SwitchableOutputStream switchableOutputStream;
	private static Socket s;
	private static SwitchableSocket switchableSocket;
	
	@BeforeClass 
	public static void setUpBeforeClass(){
		testServer = new TestServer();
		serverThread = new Thread(testServer);
		serverThread.start();
	}
	
	@Before
	public void setUp(){
		try {
			s = new Socket("localhost",Constants.SERVER_PORT);
			switchableSocket = new SwitchableSocket(s);
			switchableOutputStream = switchableSocket.getOutputStream();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@AfterClass 
	public static void tearDownAfterClass(){
		serverThread.interrupt();
	}

	@Test
	public void testWriteInt() {
		try {
			switchableOutputStream.write(277);
			Integer bytesSent =  (Integer) PrivateAccessor.getPrivateField(switchableOutputStream, "numberOfBytesSent");
			int numberOfBytesSent = bytesSent.intValue();
			assertTrue(numberOfBytesSent == 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testWriteByteArray() {
		try {
			byte[] buffer = {13,35,67,2,11};
			switchableOutputStream.write(buffer);
			Integer bytesSent =  (Integer) PrivateAccessor.getPrivateField(switchableOutputStream, "numberOfBytesSent");
			int numberOfBytesSent = bytesSent.intValue();
			assertTrue(numberOfBytesSent == buffer.length);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testWriteByteArrayIntInt() {
		try {
			byte[] buffer = {1,25,87,112,127,98,12,34};
			int off = 2;
			int len = 5;
			switchableOutputStream.write(buffer,off,len);
			Integer bytesSent =  (Integer) PrivateAccessor.getPrivateField(switchableOutputStream, "numberOfBytesSent");
			int numberOfBytesSent = bytesSent.intValue();
			assertTrue(numberOfBytesSent == len);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testSwitchOutputStream() {
		OutputStream outOld = switchableOutputStream.getOutputStream();
		Socket newSocket;
		try {
			newSocket = new Socket("localhost", Constants.SERVER_PORT);
			switchableOutputStream.switchOutputStream(newSocket.getOutputStream());
			assertFalse(switchableOutputStream.getOutputStream().equals(outOld));
			Integer bytesSent =  (Integer) PrivateAccessor.getPrivateField(switchableOutputStream, "numberOfBytesSent");
			int numberOfBytesSent = bytesSent.intValue();
			assertTrue(numberOfBytesSent == 0);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
