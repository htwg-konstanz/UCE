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
import java.security.AlgorithmParameterGenerator;

import javax.crypto.spec.DHParameterSpec;

import net.jcip.annotations.Immutable;

/**
 * Singleton implementation of {@link ParameterGenerator}.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
@Immutable
public final class ParameterGeneratorImpl {

    private enum Implementation implements ParameterGenerator {
        INSTANCE;

        @Override
        public BigInteger[] generateDiffieHellmanParameters() throws Exception {
            DHParameterSpec params = AlgorithmParameterGenerator
                    .getInstance(ParameterGenerator.DIFFIE_HELLMAN_ALGORITHM_NAME).generateParameters()
                    .getParameterSpec(DHParameterSpec.class);
            BigInteger[] result = new BigInteger[2];
            result[0] = params.getP();
            result[1] = params.getG();
            return result;
        }
    }

    private ParameterGeneratorImpl() {
        throw new AssertionError();
    }

    /**
     * Returns the sole instance of {@link ParameterGeneratorImpl}.
     * 
     * @return the sole instance of {@link ParameterGeneratorImpl}
     */
    public static ParameterGenerator getInstance() {
        return Implementation.INSTANCE;
    }
}
