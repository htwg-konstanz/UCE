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
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * Task for printing incoming messages to System.out.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class PrinterTask implements Runnable {
    private final ObjectInputStream reader;

    /**
     * Creates a printer task which prints messages of the given input stream.
     * 
     * @param inStreamToPartner
     */
    public PrinterTask(final InputStream inStreamToPartner) {
        try {
            this.reader = new ObjectInputStream(inStreamToPartner);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                final String line = (String) this.reader.readObject();
                System.out.println("Other: " + line);
                System.out.flush();
            }
        } catch (final Exception e) {
            System.err.println(e.getMessage());
            return;
        }
    }
}
