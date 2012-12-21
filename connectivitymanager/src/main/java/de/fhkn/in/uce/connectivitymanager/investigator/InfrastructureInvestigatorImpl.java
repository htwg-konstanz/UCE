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
package de.fhkn.in.uce.connectivitymanager.investigator;

import java.net.InetSocketAddress;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.plugininterface.NATBehavior;
import de.fhkn.in.uce.plugininterface.NATFeatureRealization;

public final class InfrastructureInvestigatorImpl implements InfrastructreInvestigator {
    private static final String PROPERTY_STUN_SERVER = "de.fhkn.in.uce.connectivitymanager.investigator.stunserver"; //$NON-NLS-1$ 
    private static final String PROPERTY_NAME_PRIMARY_IP = "stunserver.primary.address"; //$NON-NLS-1$
    private static final String PROPERTY_NAME_PRIMARY_PORT = "stunserver.primary.port"; //$NON-NLS-1$ 
    private final Logger logger = LoggerFactory.getLogger(InfrastructureInvestigatorImpl.class);
    private final ResourceBundle bundle;

    public InfrastructureInvestigatorImpl() {
        this.bundle = ResourceBundle.getBundle(PROPERTY_STUN_SERVER);
    }

    @Override
    public NATBehavior investigateOwnNat(final int sourcePort) {
        final NATFeatureRealization mapping = this.investigateMappingBehavior(sourcePort);
        final NATFeatureRealization filtering = this.invetigateFilteringBehavior(sourcePort);
        final NATBehavior result = new NATBehavior(mapping, filtering);
        logger.debug("Current nat behavior: {}", result.toString()); //$NON-NLS-1$
        return result;
    }

    private NATFeatureRealization investigateMappingBehavior(final int sourcePort) {
        final DeterminingNATFeatureRealization determineMapping = new DeterminingTcpNatMapping(sourcePort,
                this.getPrimaryStunServerAddress());
        return determineMapping.executeTest();
    }

    private NATFeatureRealization invetigateFilteringBehavior(final int sourcePort) {
        final DeterminingNATFeatureRealization determineFiltering = new DeterminingTcpNatFiltering(sourcePort,
                this.getPrimaryStunServerAddress());
        return determineFiltering.executeTest();
    }

    private InetSocketAddress getPrimaryStunServerAddress() {
        final String stunServerIp = this.bundle.getString(PROPERTY_NAME_PRIMARY_IP);
        final int stunServerPort = Integer.valueOf(this.bundle.getString(PROPERTY_NAME_PRIMARY_PORT));
        return new InetSocketAddress(stunServerIp, stunServerPort);
    }
}
