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
package de.fhkn.in.uce.reversal.message;

import de.fhkn.in.uce.plugininterface.message.NATTraversalTechniqueAttribute;

/**
 * Subclass of {@link NATTraversalTechniqueAttribute} for Connection Reversal.
 * This class holds the unique encoding for identifying the nat traversal
 * technique.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class ReversalAttribute extends NATTraversalTechniqueAttribute {
    private static final int ENCODED = 0x1;

    /**
     * Creates a {@link ReversalAttribute} with the unique encoding.
     */
    public ReversalAttribute() {
        this(ENCODED);
    }

    private ReversalAttribute(int encoded) {
        super(encoded);
    }

}
