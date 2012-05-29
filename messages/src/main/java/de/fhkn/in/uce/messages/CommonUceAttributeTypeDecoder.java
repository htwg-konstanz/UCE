/**
 * Copyright (C) 2011 Daniel Maier
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.fhkn.in.uce.messages;

/**
 * {@link UceAttributeTypeDecoder} to decode {@link UceAttributeType} that are
 * declared in the enum {@link CommonUceAttributeType}.
 * 
 * @author Daniel Maier
 * 
 */
final class CommonUceAttributeTypeDecoder implements UceAttributeTypeDecoder {

    public UceAttributeType decode(int encoded) {
        return CommonUceAttributeType.fromEncoded(encoded);
    }

}
