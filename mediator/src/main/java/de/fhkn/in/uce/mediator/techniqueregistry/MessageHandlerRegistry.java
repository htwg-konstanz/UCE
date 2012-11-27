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
package de.fhkn.in.uce.mediator.techniqueregistry;

import de.fhkn.in.uce.plugininterface.mediator.HandleMessage;

/**
 * Registry to manage and access message handlers of type {@link HandleMessage}.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public interface MessageHandlerRegistry {

    /**
     * Returns for a given encoding the corresponding {@link HandleMessage} for
     * connection requests. The encoding indicates which nat traversal technique
     * is used. If no implementation exists, the {@link NullMessageHandler}
     * returned.
     * 
     * @param encoding
     *            the encoding of the nat traversal technique for connection
     *            request handling
     * @return the {@link HandleMessage} for the connection request handling
     */
    HandleMessage getConnectionRequestHandlerByEncoding(int encoding);
}
