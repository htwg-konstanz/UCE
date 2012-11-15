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
package de.fhkn.in.uce.stun.attribute;

/**
 * {@link AttributeTypeDecoder} to decode {@link AttributeType} that are
 * declared in the enum {@link STUNAttributeType}.
 * 
 * @author Daniel Maier, Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
final class STUNAttributeTypeDecoder implements AttributeTypeDecoder {

    @Override
    public AttributeType decode(final int encoded) {
        return STUNAttributeType.fromEncoded(encoded);
    }

}
