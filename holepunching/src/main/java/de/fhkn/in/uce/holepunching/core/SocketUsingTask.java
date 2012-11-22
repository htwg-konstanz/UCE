/**
 * Copyright (C) 2011 Daniel Maier
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.fhkn.in.uce.holepunching.core;

import java.io.IOException;
import java.net.Socket;

/**
 * Abstract super class for classes that should be {@link CancelableTask} and
 * might block by a blocking method of {@link Socket}.
 * 
 * @author Daniel Maier
 * 
 */
public abstract class SocketUsingTask implements CancelableTask {

    protected Socket socket;
    private boolean canceled = false;

    /**
     * Cancels this task by closing the socket set via
     * {@link SocketUsingTask#setSocket(Socket)}. It also sets the canceled flag
     * returned by {@link SocketUsingTask#isCanceled()} to true.
     */
    @Override
    public synchronized void cancel() {
        this.canceled = true;
        try {
            if (this.socket != null) {
                this.socket.close();
            }
        } catch (final IOException ignored) {
        }
    }

    /**
     * Indicates whether this task is canceled or not.
     * 
     * @return true if this task is canceled; false otherwise
     */
    public synchronized boolean isCanceled() {
        return this.canceled;
    }

    /**
     * Sets the socket by that this task might block.
     * 
     * @param s
     *            the socket by that this task might block
     */
    protected synchronized void setSocket(final Socket s) {
        this.socket = s;
    }

}
