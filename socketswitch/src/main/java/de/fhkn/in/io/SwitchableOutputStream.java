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
import java.io.OutputStream;

/**
 * Wrapper of {@link java.io.OutputStream} that allows switching to new
 * output streams. {@link SwitchableOutputStream} keeps an internal counter
 * of the number of sent bytes which is used for synchronization.
 * 
 * @author Steven Boeckle (sbo), Thomas Zink (tzn)
 * 
 */
public class SwitchableOutputStream extends OutputStream {

	/** The wrapped OutputStream **/
	private OutputStream outputStream;

	/** Counter of the bytes written on the wrapped OutputStream **/
	private int numberOfBytesSent;
	//private volatile int numberOfBytesSent;// @sbo: why volatile?
	
	/* 
	 * Constructor. Creates a new {@link SwitchableOutputStream} wrapping the
	 * passed OutputStream.
	 * 
	 * @param outputStream The OutputStream to wrap as switchable
	 */
	public SwitchableOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
		this.numberOfBytesSent = 0;
	}
	/**
	 * Switches the wrapped OutputStream to a new one. First flushes the stream
	 * and then resets the number of sent bytes.
	 * 
	 * @param newStream the new OutputStream to wrap and switch to
	 * @return the number of bytes totally sent by this OutputStream
	 * @throws IOException
	 */
	public synchronized int switchOutputStream(OutputStream newStream) throws IOException {
		this.outputStream.flush();
		this.outputStream = newStream;
		int number = numberOfBytesSent;
		this.numberOfBytesSent = 0;
		return number;
	}

	/**
	 * @return outputStream the wrapped OutputStream 
	 */
	public synchronized OutputStream getOutputStream() {
		return outputStream;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#close()
	 */
	@Override
	public synchronized void close() throws IOException {
		outputStream.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#flush()
	 */
	@Override
	public synchronized void flush() throws IOException {
		outputStream.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	@Override
	public synchronized void write(byte[] b, int off, int len) throws IOException {
		numberOfBytesSent = numberOfBytesSent + len;
		outputStream.write(b, off, len);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#write(byte[])
	 */
	@Override
	public synchronized void write(byte[] b) throws IOException {
		numberOfBytesSent = numberOfBytesSent + b.length;
		outputStream.write(b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public synchronized void write(int b) throws IOException {
		numberOfBytesSent++;
		outputStream.write(b);
	}

}
