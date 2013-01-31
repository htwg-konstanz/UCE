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
package de.fhkn.in.uce.stun.util;

import java.security.SecureRandom;

/**
 * Implementation of {@link MessageUtil}. The utility class is implemented as a
 * singleton.
 * 
 * @author alexander diener, thomas zink
 * 
 */
public final class MessageUtilImpl {

    private enum Implementation implements MessageUtil {
        INSTANCE;

        @Override
        public byte[] generateSecureTranactionId() {
            byte[] transactionId = new byte[12];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(transactionId);

            return transactionId;
        }
    }

    private MessageUtilImpl() {
        throw new AssertionError();
    }

    /**
     * Returns the sole instance.
     * 
     * @return the sole instance.
     */
    public static Implementation getInstance() {
        return Implementation.INSTANCE;
    }
}
