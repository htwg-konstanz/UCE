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
package de.fhkn.in.uce.connectivitymanager;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.fhkn.in.uce.connectivitymanager.connection.configuration.TestConnectionConfigurationImpl;
import de.fhkn.in.uce.connectivitymanager.connection.configuration.TestDefaultConnectionConfiguration;
import de.fhkn.in.uce.connectivitymanager.connection.configuration.TestServiceClass;
import de.fhkn.in.uce.connectivitymanager.manager.TestConnectivityManager;
import de.fhkn.in.uce.connectivitymanager.manager.TestUnsecureConnectionEstablishment;
import de.fhkn.in.uce.connectivitymanager.registry.TestNATTraversalRegistryImpl;
import de.fhkn.in.uce.connectivitymanager.registry.TestNATTraversalTechniqueNotFoundException;
import de.fhkn.in.uce.connectivitymanager.security.diffiehellman.TestKeyExchange;
import de.fhkn.in.uce.connectivitymanager.security.diffiehellman.TestParameterGenerator;
import de.fhkn.in.uce.connectivitymanager.security.hmac.TestHmacStreams;
import de.fhkn.in.uce.connectivitymanager.selector.TestNATTraversalSelection;
import de.fhkn.in.uce.connectivitymanager.selector.strategy.TestConnectionSetupTimeSelection;
import de.fhkn.in.uce.connectivitymanager.selector.weighting.TestConnectionSetupTimeComparator;

/**
 * The test suite requires the nat traversal techniques directconnection,
 * connetion reversal, hole punching and relaying which can be found in the
 * Universal Connection Establishing project of the HTWG Konstanz.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ TestConnectionSetupTimeComparator.class, TestNATTraversalSelection.class,
        TestNATTraversalRegistryImpl.class, TestNATTraversalTechniqueNotFoundException.class,
        TestConnectionSetupTimeSelection.class, TestServiceClass.class, TestDefaultConnectionConfiguration.class,
        TestConnectionConfigurationImpl.class, TestParameterGenerator.class, TestKeyExchange.class,
        TestHmacStreams.class, TestConnectivityManager.class, TestUnsecureConnectionEstablishment.class })
public final class ConnectivityManagerTestSuite {

    private ConnectivityManagerTestSuite() {
        throw new AssertionError();
    }
}
