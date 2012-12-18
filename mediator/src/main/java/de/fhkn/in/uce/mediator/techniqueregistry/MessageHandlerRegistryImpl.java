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

import java.util.Iterator;

import de.fhkn.in.uce.plugininterface.mediator.HandleMessage;

/**
 * Singleton implementation of {@link MessageHandlerRegistry}.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class MessageHandlerRegistryImpl implements MessageHandlerRegistry {
    private static final MessageHandlerRegistry INSTANCE = new MessageHandlerRegistryImpl();
    private final PluginLoader pluginLoader = PluginLoaderImpl.getInstance();

    @Override
    public HandleMessage getConnectionRequestHandlerByEncoding(final int encoding) {
        HandleMessage result = null;
        final Iterator<HandleMessage> handlerIterator = this.pluginLoader.getPluginIterator();
        while (handlerIterator.hasNext()) {
            final HandleMessage handler = handlerIterator.next();
            if (encoding == handler.getAttributeForTraversalTechnique().getEncoded()) {
                result = handler;
                break;
            }
        }
        return result;
    }

    private MessageHandlerRegistryImpl() {
        // private constructor;
    }

    /**
     * Delivers the sole instance of the implementation of
     * {@code NATTraversalRegistryImpl}.
     * 
     * @return the sole instance of the implementation of
     *         {@code NATTraversalRegistryImpl}.
     */
    public static MessageHandlerRegistry getInstance() {
        return INSTANCE;
    }
}
