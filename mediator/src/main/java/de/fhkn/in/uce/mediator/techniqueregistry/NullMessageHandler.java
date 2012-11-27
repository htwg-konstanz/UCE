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

import java.net.Socket;

import de.fhkn.in.uce.plugininterface.mediator.HandleMessage;
import de.fhkn.in.uce.plugininterface.message.NATTraversalTechniqueAttribute;
import de.fhkn.in.uce.stun.message.Message;

/**
 * Singleton implementation of {@link HandleMessage} which does nothing. It can
 * be used to avoid null.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class NullMessageHandler implements HandleMessage {
    private static final NullMessageHandler INSTANCE = new NullMessageHandler();

    @Override
    public void handleMessage(final Message message, final Socket controlConnection) throws Exception {
        // do nothing
    }

    @Override
    public NATTraversalTechniqueAttribute getAttributeForTraversalTechnique() {
        // return attribute with non-existing encoding
        return new NATTraversalTechniqueAttribute(Integer.MAX_VALUE);
    }

    private NullMessageHandler() {
        // private constructor
    }

    /**
     * Returns the sole instance of {@link NullMessageHandler}.
     * 
     * @return the sole instance of {@link NullMessageHandler}
     */
    public static NullMessageHandler getInstance() {
        return INSTANCE;
    }
}
