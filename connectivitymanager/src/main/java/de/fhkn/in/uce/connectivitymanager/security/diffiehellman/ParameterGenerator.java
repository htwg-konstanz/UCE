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
package de.fhkn.in.uce.connectivitymanager.security.diffiehellman;

import java.math.BigInteger;

/**
 * The {@link ParameterGenerator} provides utility methods to generate
 * parameters for cryptographic uses.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public interface ParameterGenerator {
    /**
     * The name of the Diffie-Hellman algorithm.
     */
    //    public static final String DIFFIE_HELLMAN_ALGORITHM_NAME = "DIFFIEHELLMAN"; //$NON-NLS-1$
    public static final String DIFFIE_HELLMAN_ALGORITHM_NAME = "DH"; //$NON-NLS-1$

    /**
     * Method to generate parameters for the Diffie-Hellman Key Exchange.
     * 
     * @return a field with the generated parameters, the field with index 0
     *         contains p, the field with the index 1 contains g
     * @throws Exception
     *             if the parameters could not be generated
     */
    BigInteger[] generateDiffieHellmanParameters() throws Exception;
}
