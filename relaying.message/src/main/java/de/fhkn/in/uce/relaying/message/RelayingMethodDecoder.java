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
package de.fhkn.in.uce.relaying.message;

import de.fhkn.in.uce.stun.header.MessageMethod;
import de.fhkn.in.uce.stun.header.MessageMethodDecoder;

/**
 * A {@link UceMethodDecoder} to decode specific relay uce methods.
 *
 * @author Daniel Maier, Alexander Diener (aldiener@htwg-konstanz.de)
 *
 */
public final class RelayingMethodDecoder implements MessageMethodDecoder {

    @Override
    public MessageMethod decode(int encoded) {
        return RelayingMethod.fromEncoded(encoded);
    }

}
