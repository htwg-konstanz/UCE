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
import java.io.OutputStream;

/**
 * @author Steven Böckle
 * 
 */
public class SwitchableOutputStream extends OutputStream {

	private OutputStream outputStream;
	private volatile int numberOfBytesSent;
	
	public SwitchableOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
		this.numberOfBytesSent = 0;
	}
	/**
	 * 
	 * @param newStream
	 * @return the of bytes totally sent by this OutputStream
	 * @throws IOException
	 */
	public synchronized int switchOutputStream(OutputStream newStream) throws IOException {
		this.outputStream.flush();
		this.outputStream = newStream;
		int number = numberOfBytesSent;
		numberOfBytesSent = 0;
		return number;
	}

	public synchronized OutputStream getOutputStream() {
		return outputStream;
	}

	// delegate work to internal output stream

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
