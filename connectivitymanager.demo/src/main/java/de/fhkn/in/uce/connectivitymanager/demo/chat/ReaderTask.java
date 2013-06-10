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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Scanner;

/**
 * Task for reading messages from the command line and sending it to the
 * communication partner.
 *
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 *
 */
public final class ReaderTask implements Runnable {
    private final ObjectOutputStream outStreamToPartner;
    private final Scanner scanner;

    /**
     * Creates a reader task which reads tasks from the command line and sends
     * it via the given output stream.
     *
     * @param outStreamToPartner
     *            the output stream to the communication partner
     */
    public ReaderTask(final OutputStream outStreamToPartner) {
        try {
            this.outStreamToPartner = new ObjectOutputStream(outStreamToPartner);
            this.scanner = new Scanner(System.in);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        String input;
        while (true) {
            input = this.scanner.nextLine();
            try {
                this.outStreamToPartner.writeObject(input);
                this.outStreamToPartner.flush();
            } catch (final IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
