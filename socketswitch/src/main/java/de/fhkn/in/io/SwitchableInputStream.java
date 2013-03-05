/*
    Copyright (c) 2012 Steven Boeckle, 

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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Steven Böckle
 * 
 */
public class SwitchableInputStream extends InputStream {

	private InputStream inputStream;
	private BlockingQueue<InputStream> newInputStreams;
	public static Object monitor = new Object();

	private int numberOfBytesReceived;

	public int getNumberOfBytesReceived() {
		return numberOfBytesReceived;
	}

	public int getNumberOfBytesToRead() {
		return numberOfBytesToRead;
	}

	private int numberOfBytesToRead;

	private boolean isSwitchException = false;
	private boolean isReading = false;
	private boolean isSwitching = false;

	public boolean isReading() {
		return isReading;
	}

	public SwitchableInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
		this.newInputStreams = new LinkedBlockingQueue<InputStream>();
		this.numberOfBytesToRead = 0;
		this.numberOfBytesReceived = 0;
	}

	/**
	 * Puts the new InputStream in the newInputStreams queue
	 * 
	 * @param newSocket
	 * @throws IOException
	 */
	public void putNewInputStream(InputStream inputStream) {
		this.newInputStreams.add(inputStream);
	}

	/**
	 * Sets the numberOfBytesToRead from the existing connection before it can
	 * be closed
	 * 
	 * @param numberOfBytesToRead
	 *            the numberOfBytesToRead to set
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
	public boolean markSupported() {
		return inputStream.markSupported();
	}

	// TODO: Fix the read(*) implementations
	/*
	 * checking needs to be done in read(*), however, maybe this can be done
	 * more efficiently. Also, would be nice to be able to actually replace
	 * the switching input stream with a non-switching one, in case we
	 * do not want to switch anymore. Where to do that? 
	 */
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	@Override
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
							internStreamSwitch();
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
				internStreamSwitch();
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
			internStreamSwitch();
			return this.read(b,off,len);
		}
		this.isReading = false;
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read(byte[])
	 */
	@Override
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
							internStreamSwitch();
							return this.read(b);
						}
						// only switch the stream
						// if all the bytes which are left has been read
						if (data == bytesToReadLeft) {
							// then switch it
							internStreamSwitch();
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
				internStreamSwitch();
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
	}

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
					int bytesToReadLeft = numberOfBytesToRead
							- numberOfBytesReceived;
					data = this.inputStream.read();
					if (data == -1) {
						internStreamSwitch();
						return this.read();
					} else {
						numberOfBytesReceived++;
						return data;
					}
				} else {
					internStreamSwitch();
					return this.read();
				}
			}
			this.isReading = true;
			data = inputStream.read();
			// System.out.println("DATA recieved: " + data);
		} catch (SocketException e) {
			if (isSwitchException) {
					internStreamSwitch();
					setSwitchException(false);
					return this.read();
			} else {
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (data != -1) {
			numberOfBytesReceived += data;
		} else {
			if (isSwitching) {
				internStreamSwitch();
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
	public void internStreamSwitch() {
		synchronized (monitor) {
			try {
				inputStream = newInputStreams.take();
				this.numberOfBytesReceived = 0;
				this.numberOfBytesToRead = 0;
				this.isSwitching = false;
				monitor.notify();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
	public long skip(long n) throws IOException {
		return inputStream.skip(n);
	}

	public void setSwitching(boolean b) {
		this.isSwitching = b;
	}

	public void setSwitchException(boolean isSwitchException) {
		this.isSwitchException = isSwitchException;
	}

	public boolean isSwitchException() {
		return isSwitchException;
	}
}
