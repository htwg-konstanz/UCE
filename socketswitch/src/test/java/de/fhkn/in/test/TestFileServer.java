/**
 * 
 */
package de.fhkn.in.test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;

import de.fhkn.in.net.SwitchableSocket;

/**
 * This class represents the file server, which the SwitchableSocketTest class is sending a file to.
 * After recieving a file it compares it with the original file, which has to be to be there before the test is running.
 * 
 * @author Steven B�ckle
 * 
 */
public class TestFileServer implements Runnable {

	static private SwitchableSocket switchableSocket;

	// only needed if messages should be sent back!
	private static OutputStream out;
	private static ServerSocket serverSocket;

	static private InputStream in;
	static private FileOutputStream writer;
	static private long totalBytesRecieved;
	private static String pathOriginalFile;
	private static String pathSaveFile;

	private static Date c;

	public TestFileServer(Socket socket) {
		try {
			switchableSocket = new SwitchableSocket(socket);
			out = switchableSocket.getOutputStream();
			in = switchableSocket.getInputStream();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
			try {
				// creates new File on pathSaveFile location,
				// overrides already existing files, e.g. the files from last tests
				writer = new FileOutputStream(pathSaveFile);
				
				byte[] buffer = new byte[Constants.BYTE_ARRAY_SIZE];
				int recvMsgSize;
				
			//Each loop stands for a different read() Method of the SwitchableInputStream.
			//To test each read() Method each loop has to be traversed  in a separate test run
				
				//this one should be tested with small files only, because it is tried to read 
				//and write every single byte seperately!
				// uses/tests the read() method.
//				while ((recvMsgSize = in.read()) != -1) {
//					writer.write(recvMsgSize);
//					writer.flush();
//					totalBytesRecieved ++;
//					buffer = new byte[Constants.BYTE_ARRAY_SIZE];
//				}
				// uses/tests the read(byte[],int,int) method.
				// -8 because it should not use the full array, 
				//so that the read(byte[],int,int) can be testet probably
				while ((recvMsgSize = in.read(buffer,0,buffer.length -8)) != -1) {
					writer.write(buffer,0,recvMsgSize);
					writer.flush();
					totalBytesRecieved += recvMsgSize;
					buffer = new byte[Constants.BYTE_ARRAY_SIZE];
				}
				// uses/tests the read(byte[]) method.
//				while ((recvMsgSize = in.read(buffer)) != -1) {
//					writer.write(buffer,0,recvMsgSize);
//					writer.flush();
//					totalBytesRecieved += recvMsgSize;
//					buffer = new byte[Constants.BYTE_ARRAY_SIZE];
//				}
				
				System.out.println("total Bytes Recieved: " + totalBytesRecieved);
				c = new Date();
				System.out.println("Received EOF -> compare Files -> server closing");
				
				System.out.println("Comparing the converted File with the recieved one, which takes a few seconds. pls wait...");
				int result = testIfFilesAreEqual(pathSaveFile, pathOriginalFile);
				String code = Integer.toString(result);
				System.out
				.println("sending the Result of the comparison between the received "
						+ "and the original File to  the Client: " + code);
				out.write(code.getBytes(), 0, code.getBytes().length);
				out.flush();
				System.out.println("closing old and creating new listening FileServer...");
				switchableSocket.close();
				serverSocket.close();
				System.exit(0);
				
			}catch (IOException e) {
				e.printStackTrace();
			}

	}

	/**
	 * 
	 * @param args
	 *            args[0] Path where recieved File should be stored args[1] Path
	 *            to File to which recieved one should be compared
	 * @throws InterruptedException
	 */
	public static void main(String[] args) {
			if (args.length < 2) {
				System.err
						.println("need 2 Arguments: first -> Path where recieved File should be stored \n "
								+ "second ->Path to File to which recieved one should be compared");
				System.exit(-1);
			}
			System.out.println(args[0]);
			pathSaveFile = args[0];
			pathOriginalFile = args[1];
			startNewFileServer();
	}

	/**
	 * Starts a new FileServer which is listening for new connections.
	 * As soon as a Connection is available, the Server Socket switches the already standing
	 * connection to the new one.
	 */
	private static void startNewFileServer() {
		try{
		totalBytesRecieved = 0;
		serverSocket = new ServerSocket(Constants.SERVER_PORT);
		System.out.println("TCPServer waiting for file..");
		Socket newSocket;
		TestFileServer server = new TestFileServer(serverSocket.accept());
		Thread thread = new Thread(server);
		thread.start();
		while (true) {
			newSocket = serverSocket.accept();	
			// Server is switching now his Socket with the one from the New Connection
			switchableSocket.switchSocket(newSocket);
			System.out.println("receiving from new connection...");
		}
	
		}catch (SocketException e) {
		} catch (IOException e) {
		e.printStackTrace();
		}
		
	}

	/**
	 * Tests if original File and recieved File are the same
	 */
	private int testIfFilesAreEqual(String path1, String path2) {
		//
		try {
			int cBB = FileChecker.CompareFilesbyByte(path1, path2);
			if (cBB == -2) {
				System.err.println("The two files have different lengths!");
				return cBB;
			} else if (cBB == -1) {
				System.err.println("Difference using CompareFilesByBytes!");
				return cBB;
			} else
				System.out
						.println("The Files are the same accordding to CompareFilesByBytes :) ");

			if (FileChecker.MD5HashFile(path1).equals(
					FileChecker.MD5HashFile(path2))) {
				System.out.println("No Differnce using MD5HashFile Method :) ");
				return 0;
			} else {
				System.err.println("Difference using MD5HashFile!");
				return -3;
			}

		} catch (IOException e1) {
			e1.printStackTrace();
			return -5;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -5;
		}

	}
}
