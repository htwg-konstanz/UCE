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
 * Defines methods for loading and handling plugins. The plugins which are
 * loaded must implement the interface {@link HandleMessage}.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
interface PluginLoader {
    /**
     * Loads and initializes the plugins of type {@link HandleMessage}.
     * 
     * @throws Exception
     */
    void loadPlugins() throws Exception;

    /**
     * Returns an {@link Iterator} to access the {@link HandleMessage}
     * implementations. The implementations are delivered by the plugins.
     * 
     * @return
     */
    Iterator<HandleMessage> getPluginIterator();
}
