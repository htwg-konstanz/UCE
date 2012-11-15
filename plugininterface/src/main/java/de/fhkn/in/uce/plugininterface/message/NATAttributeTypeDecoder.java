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
package de.fhkn.in.uce.plugininterface.message;

import de.fhkn.in.uce.messages.UceAttributeType;
import de.fhkn.in.uce.messages.UceAttributeTypeDecoder;

/**
 * Implementation of {@link UceAttributeTypeDecoder} which decodes
 * {@link NATUCEAttributeType}s.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public class NATAttributeTypeDecoder implements UceAttributeTypeDecoder {

    @Override
    public UceAttributeType decode(int encoded) {
        return NATUCEAttributeType.fromEncoded(encoded);
    }

}
