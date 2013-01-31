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
package de.fhkn.in.uce.core.concurrent;

import java.util.concurrent.ThreadFactory;

/**
 * Implementation of {@link ThreadFactory} that creates daemon threads.
 * All created threads belong to the same thread group. The thread group used
 * is the one, the original creator thread belongs to.
 * 
 * @author thomas zink, daniel maier
 * 
 */
public class ThreadGroupThreadFactory implements ThreadFactory {

    private static final ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();

    public Thread newThread(Runnable r) {
        Thread thread = new Thread(threadGroup, r);
        thread.setDaemon(true);
        return thread;
    }
}
