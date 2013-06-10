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
package de.fhkn.in.uce.plugininterface;

import net.jcip.annotations.Immutable;

/**
 * A {@link NATSituation} represents a constellation of two NAT devices, the NAT
 * device on the client-side and the NAT device on the server-side. Multilevel
 * NAT devices are also represented by a NAT situation because for a public
 * reachable peer multilevel NATs behave like a single NAT.
 *
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 *
 */
@Immutable
public final class NATSituation {
    private final NATBehavior clientNat;
    private final NATBehavior serviceNat;

    /**
     * Creates a unknown {@link NATSituation}.
     */
    public NATSituation() {
        this.clientNat = new NATBehavior();
        this.serviceNat = new NATBehavior();
    }

    /**
     * Creates a {@link NATSituation} with the given client and server
     * {@link NATBehavior}.
     *
     * @param clientNat
     *            the {@link NATBehavior} of the client
     * @param serviceNat
     *            the {@link NATBehavior} of the server
     */
    public NATSituation(final NATBehavior clientNat, final NATBehavior serviceNat) {
        this.clientNat = clientNat;
        this.serviceNat = serviceNat;
    }

    /**
     * Creates a {@link NATSituation} with the given client and server
     * {@link NATFeatureRealization}s.
     *
     * @param clientMapping
     *            the {@link NATFeatureRealization} for the client NAT mapping
     *
     * @param clientFiltering
     *            the {@link NATFeatureRealization} for the client NAT filtering
     * @param serviceMapping
     *            the {@link NATFeatureRealization} for the server NAT mapping
     * @param serviceFiltering
     *            the {@link NATFeatureRealization} for the server NAT filtering
     */
    public NATSituation(final NATFeatureRealization clientMapping, final NATFeatureRealization clientFiltering,
            final NATFeatureRealization serviceMapping, final NATFeatureRealization serviceFiltering) {
        this(new NATBehavior(clientMapping, clientFiltering), new NATBehavior(serviceMapping, serviceFiltering));
    }

    /**
     * Returns the {@link NATBehavior} of the client NAT.
     *
     * @return the {@link NATBehavior} of the client NAT
     */
    public NATBehavior getClientNATBehavior() {
        return this.clientNat;
    }

    /**
     * Returns the {@link NATBehavior} of the server NAT.
     *
     * @return the {@link NATBehavior} of the server NAT
     */
    public NATBehavior getServiceNATBehavior() {
        return this.serviceNat;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((this.clientNat == null) ? 0 : this.clientNat.hashCode());
        result = (prime * result) + ((this.serviceNat == null) ? 0 : this.serviceNat.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (null == obj) {
            return false;
        }
        if (!(obj instanceof NATSituation)) {
            return false;
        }
        final NATSituation other = (NATSituation) obj;

        final NATFeatureRealization thisClientMapping = this.clientNat.getFeatureRealization(NATFeature.MAPPING);
        final NATFeatureRealization thisClientFiltering = this.clientNat.getFeatureRealization(NATFeature.FILTERING);
        final NATFeatureRealization thisServiceMapping = this.serviceNat.getFeatureRealization(NATFeature.MAPPING);
        final NATFeatureRealization thisServiceFiltering = this.serviceNat.getFeatureRealization(NATFeature.FILTERING);

        final NATFeatureRealization otherClientMapping = other.getClientNATBehavior().getFeatureRealization(
                NATFeature.MAPPING);
        final NATFeatureRealization otherClientFiltering = other.getClientNATBehavior().getFeatureRealization(
                NATFeature.FILTERING);
        final NATFeatureRealization otherServiceMapping = other.getServiceNATBehavior().getFeatureRealization(
                NATFeature.MAPPING);
        final NATFeatureRealization otherServiceFiltering = other.getServiceNATBehavior().getFeatureRealization(
                NATFeature.FILTERING);

        if (thisClientMapping.equals(otherClientMapping) && thisClientFiltering.equals(otherClientFiltering)
                && thisServiceMapping.equals(otherServiceMapping) && thisServiceFiltering.equals(otherServiceFiltering)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("NATSituation=");
        sb.append("client");
        sb.append(this.clientNat.toString());
        sb.append("service");
        sb.append(this.serviceNat.toString());
        return sb.toString();
    }
}
