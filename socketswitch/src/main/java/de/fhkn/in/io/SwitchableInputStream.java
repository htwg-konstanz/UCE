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
package de.fhkn.in.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

/**
 * Wrapper of {@link java.io.InputStream}. The {@link SwitchableInputStream}
 * allows switching to a new InputStream 
 * 
 * @author Steven Boeckle (sbo), Thomas Zink (tzn)
 * 
 */
public class SwitchableInputStream extends InputStream {

	/** Lock, monitor pattern **/
	//private static Object monitor = new Object();
	
	/** The wrapped InputStream **/
	private InputStream inputStream;
	
	// got rid of queues to unify with OutputStream switch interface
	// according to thesis, could lead to deadlocks with fast consecutive
	// switching
	// UNTESTED
	/** Queue of input streams to switch to **/
	//private BlockingQueue<InputStream> newInputStreams;
	
	/** Number of bytes left to read before switching **/
	private int numberOfBytesToRead;

	private boolean isSwitchException = false;
	private boolean isReading = false;
	private boolean isSwitching = false;

	private int numberOfBytesReceived;

	/**
	 * Constructor. Wraps the given InputStream in a new SwitchableInputStream
	 * 
	 * @param inputStream the InputStream to wrap
	 */
	public SwitchableInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
		//this.newInputStreams = new LinkedBlockingQueue<InputStream>();
		this.numberOfBytesToRead = 0;
		this.numberOfBytesReceived = 0;
	}
	
	/**
	 * @return number of bytes received on the active stream
	 */
	public synchronized int getNumberOfBytesReceived() {
		return numberOfBytesReceived;
	}

	/**
	 * @return number of bytes left to read
	 */
	public int getNumberOfBytesToRead() {
		return numberOfBytesToRead;
	}

	/**
	 * @return true if stream is reading, false else
	 */
	public boolean isReading() {
		return isReading;
	}

	/**
	 * Adds the given InputStream to newInputStreams queue
	 * 
	 * @param newSocket
	 * @throws IOException
	 */
	/*public void addInputStream(InputStream inputStream) {
		this.newInputStreams.add(inputStream);
	}*/

	/**
	 * Sets the numberOfBytesToRead from the existing connection before it can
	 * be closed.
	 * 
	 * @param numberOfBytesToRead the numberOfBytesToRead to set
	 */
	public void setNumberOfBytesToRead(int numberOfBytesToRead) {
		this.numberOfBytesToRead = numberOfBytesToRead;
	}

	// delegate work to internal input stream

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#available()
	 */
	@Override
	public int available() throws IOException {
		return inputStream.available();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#close()
	 */
	@Override
	public void close() throws IOException {
		inputStream.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#mark(int)
	 */
	@Override
	public synchronized void mark(int readlimit) {
		inputStream.mark(readlimit);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#markSupported()
	 */
	@Override
	public synchronized boolean markSupported() {
		return inputStream.markSupported();
	}

	//TODO: cleanup the read ... read ... read mess and unify.
	// only read() has to be overridden since the InputStream implementations
	// all call read internally
	// UNTESTED
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	/*@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int data = 0;
		try {
			if (numberOfBytesToRead != 0) {
				if (numberOfBytesToRead > numberOfBytesReceived) {
					int bytesToReadLeft = numberOfBytesToRead
							- numberOfBytesReceived;
					// are there more bytes left than the array can handle?
					if (bytesToReadLeft > b.length) {
						data = this.inputStream.read(b, 0, b.length);
						numberOfBytesReceived += data;
						return data;
					} else {
						data = this.inputStream.read(b, 0, bytesToReadLeft);
						// only switch the stream if all the bytes which are
						// left has been read
						if (data == bytesToReadLeft) {
							// then switch it
							switchStream();
							return data;
						} else {
							// Still some bytes left to read
							numberOfBytesReceived += data;
							return data;
						}
					}
				}
			} else {
				this.isReading = true;
				data = inputStream.read(b, off, len);
			}
		} catch (SocketException e) {
			if (isSwitchException) {
				switchStream();
				return this.read(b, off, len);
			} else {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (data != -1) {
			numberOfBytesReceived += data;
		}else if (isSwitching) {
			switchStream();
			return this.read(b,off,len);
		}
		this.isReading = false;
		return data;
	}*/

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read(byte[])
	 */
	/*@Override
	public int read(byte[] b) {
		int data = 0;
		try {
			if (numberOfBytesToRead != 0) {
				if (numberOfBytesToRead > numberOfBytesReceived) {
					int bytesToReadLeft = numberOfBytesToRead
							- numberOfBytesReceived;
					// are there more bytes left than the array can handle?
					// if so don't switch the stream.
					if (bytesToReadLeft > b.length) {
						data = this.inputStream.read(b, 0, b.length);
						numberOfBytesReceived += data;
						return data;
					} else {
						data = this.inputStream.read(b, 0, bytesToReadLeft);
						// has there been an end of file? if so, its because of
						// socket closing on the other side
						if (data == -1) {
							System.out.println("bin drin");
							switchStream();
							return this.read(b);
						}
						// only switch the stream
						// if all the bytes which are left has been read
						if (data == bytesToReadLeft) {
							// then switch it
							switchStream();
							return data;
						} else {
							numberOfBytesReceived += data;
							return data;
						}
					}
				}

			} else {
				this.isReading = true;
				data = inputStream.read(b);
			}
		} catch (SocketException e) {
			if (isSwitchException) {
				switchStream();
				return this.read(b);
			} else {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (data != -1) {
			numberOfBytesReceived += data;
		}

		this.isReading = false;
		return data;
	}*/

	
	// seriously, I have no idea why switching is done in the read method.
	// also, the implementations of the reads do not seem correct and are
	// equal apart of some minor differences.
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() {
		int data = 0;
		try {
			if (numberOfBytesToRead != 0) {
				if (numberOfBytesToRead > numberOfBytesReceived) {
					//int bytesToReadLeft = numberOfBytesToRead
						//	- numberOfBytesReceived;
					data = this.inputStream.read();
					if (data == -1) {
						switchStream();
						return this.read();
					} else {
						numberOfBytesReceived++;
						return data;
					}
				} else {
					switchStream();
					return this.read();
				}
			}
			this.isReading = true;
			data = inputStream.read();
			// System.out.println("DATA recieved: " + data);
		} catch (SocketException e) {
			if (isSwitchException) {
					switchStream();
					setSwitchException(false);
					return this.read();
			} else {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (data != -1) {
			numberOfBytesReceived += data;
		} else {
			if (isSwitching) {
				switchStream();
				return this.read();
			} 
		}
		this.isReading = false;

		return data;

	}

	/**
	 * Actually switches the InputStream to the next one in the queue
	 * newInputStreams
	 * 
	 * @throws InterruptedException
	 */
	public synchronized void switchStream() {
		//synchronized (monitor) {
			/*try {
				inputStream = newInputStreams.take();
				this.numberOfBytesReceived = 0;
				this.numberOfBytesToRead = 0;
				this.isSwitching = false;
				//monitor.notify();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
		//}
	}
	
	public synchronized void switchStream(InputStream newStream) {
		this.inputStream = newStream;
		this.numberOfBytesReceived = 0;
		this.numberOfBytesToRead = 0;
		this.isSwitching = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#reset()
	 */
	@Override
	public synchronized void reset() throws IOException {
		inputStream.reset();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#skip(long)
	 */
	@Override
	public synchronized long skip(long n) throws IOException {
		return inputStream.skip(n);
	}

	public synchronized void setSwitching(boolean b) {
		this.isSwitching = b;
	}

	public synchronized void setSwitchException(boolean isSwitchException) {
		this.isSwitchException = isSwitchException;
	}

	public synchronized boolean isSwitchException() {
		return isSwitchException;
	}
}
