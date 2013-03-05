package de.fhkn.in.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.fhkn.in.io.SwitchableInputStream;
import de.fhkn.in.io.SwitchableOutputStream;
import de.fhkn.in.net.SwitchableSocket;

/**
 * This class tests the switchSocket Methods of the SwitchableSocket class. For
 * this Test to be working there must be the
 * {@link de.htwg_konstanz.in.switchable.tests.TestFileServer} class running on
 * the Server to which this test should send the file. The hostname and Port of
 * the Server has to be defined in the
 * {@link de.htwg_konstanz.in.switchable.tests.Constants} class
 * 
 * There is one Test Method For each write Operation, of the underlying
 * SwitchableOuputStream.
 * 
 * To Test the different read methods of the underlying SwitchableInputStream
 * view the {@link de.htwg_konstanz.in.switchable.tests.TestFileServer} class
 * 
 * @author Steven B�ckle
 * 
 */
public class SwitchableSocketTest {
	public static SwitchableSocket switchableSocket;
	public static SwitchableInputStream switchableInputStream;
	public static SwitchableOutputStream switchableOutputStream;
	public static Socket s;
	public static Thread controlThread;
	public static ControlInstance control;
	public static String path;
	public final int WRITE_PART_ARRAY = 3;
	public final int WRITE_FULL_ARRAY = 2;
	public final int WRITE_STANDARD = 1;

	public long alreadyTransferred = 0;
	public long fileSize;
	public byte[] buffer = new byte[Constants.BYTE_ARRAY_SIZE];
	public File fileToSend;
	public FileInputStream fIn;


	@Before
	public void setUp() {
		try {
			/* TODO: testing should be automatic, not interactive.
			better provide a test file and start everything automatically */
			path = (String) JOptionPane
			.showInputDialog("Path to File for sending:");
			/* TODO: also, this input doesn't do anything, need to start
			server manually. */
			JOptionPane.showInputDialog("Please start the TestFileServer:");
			s = new Socket(Constants.SERVER_ADDRESS, Constants.SERVER_PORT);
			switchableSocket = new SwitchableSocket(s);
			switchableInputStream = switchableSocket.getInputStream();
			switchableOutputStream = switchableSocket.getOutputStream();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@After
	public void tearDown() {
		try {
			s.close();
		} catch (IOException e) {
			System.err.println("Test Method ended -> Socket closed...");
			// e.printStackTrace();
		}
	}

	/**
	 * Testing File Transfer with the write(byte[],int,int) Method of the
	 * SwitchableOutputStream
	 * 
	 * @throws IOException
	 */
	@Test
	public void testSwitchSocket() throws IOException {
		startControllInstance();
		boolean done = false;
		while (!done) {
			done = sendFile(path);
		}
		shutDownControllInstance();
		int code = EvaluateAnswerFromServer();
		assertTrue(code == 0);
	}

	public void startControllInstance() {
		control = new ControlInstance(switchableSocket);
		controlThread = new Thread(control);
		controlThread.start();
	}

	public void shutDownControllInstance() {
		control.finished = true;
		controlThread.interrupt();
	}

	private int EvaluateAnswerFromServer() {
		byte[] answer = new byte[64];
		System.out
				.println("Waiting for Answercode from sever, which could depending "
						+ "on the Filesize take a few seconds/minutes");
		switchableInputStream.read(answer);
		String answerString = new String(answer).trim();
		int answerCode = Integer.parseInt(answerString);
		switch (answerCode) {
		case -2:
			System.err.println("files have different lengths");
			break;
		case -1:
			System.err
					.println("files have difference using CompareFilesByBytes");
			break;
		case -3:
			System.err.println("files have difference using MD5HashFile");
			break;
		case -5:
			System.err.println("Exception occured");
			break;
		case 0:
			System.out.println("Files are identical");
			break;
		}
		return answerCode;
	}

	/**
	 * 
	 * Sends a File to the Server to which the SwitchableSocket is connected
	 * 
	 * @param path
	 *            The Path to the File
	 * @return true if all bytes has been transferred otherwise false
	 */
	public boolean sendFile(String path) {
		try {
			if (fileToSend == null) {
				fileToSend = new File(path);
			}
			if (fIn == null) {
				fIn = new FileInputStream(fileToSend);
			}
			// First check the filelength, but only on the first time
			if (fileSize == 0) {
				fileSize = fileToSend.length();
				System.out.println("fileSize: " + fileSize);
			}
			int res;
			// skip the already transferred Bytes
			if (alreadyTransferred != 0) {
				long skipped = fIn.skip(alreadyTransferred);
				if (alreadyTransferred != skipped) {
					System.err.println("bytes already transferred "
							+ "!= bytes skipped!!!");
				}
			}

			while ((res = fIn.read(buffer)) != -1) {
				switchableOutputStream.write(buffer, 0, res);
				switchableOutputStream.flush();
				alreadyTransferred += res;
			}
			if (fileSize == alreadyTransferred) {
				System.out.println("All bytes from File has been transferred: "
						+ fileSize);
				return true;
			} else
				return false;
		} catch (FileNotFoundException e) {

			e.printStackTrace();
			return false;
		}

		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
}
