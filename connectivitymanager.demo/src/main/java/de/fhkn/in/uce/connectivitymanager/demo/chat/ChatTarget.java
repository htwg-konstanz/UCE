/*
 * Copyright (c) 2012 Alexander Diener,
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.fhkn.in.uce.connectivitymanager.demo.chat;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.fhkn.in.uce.connectivitymanager.connection.UCESocket;
import de.fhkn.in.uce.connectivitymanager.connection.UCEUnsecureSocketFactory;

/**
 * Creates a chat target and waits for sources to connect. THe target has to be
 * started first.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class ChatTarget {
    private final String targetId;

    /**
     * Creates a chat target with the given id.
     * 
     * @param targetId
     *            the id of the target
     */
    public ChatTarget(final String targetId) {
        this.targetId = targetId;
    }

    /**
     * Creates a UCE target socket and waits for a source.
     * 
     * @throws Exception
     */
    public void startChatTarget() throws Exception {
        System.out.println("Starting target " + this.targetId + " and waiting for source ...");
        final UCESocket socketTpPartner = UCEUnsecureSocketFactory.getInstance().createTargetSocket(this.targetId);
        socketTpPartner.connect();
        System.out.println("Connection established");
        System.out.println("Starting threads for processing ...");
        final Executor executor = Executors.newCachedThreadPool();
        executor.execute(new ReaderTask(socketTpPartner.getOutputStream()));
        executor.execute(new PrinterTask(socketTpPartner.getInputStream()));
        System.out.println("Ready to chat ...");
    }

    /**
     * Starts the chat target. Args: targetId
     * 
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("args: targetId");
        }
        final ChatTarget target = new ChatTarget(args[0]);
        target.startChatTarget();
    }
}
