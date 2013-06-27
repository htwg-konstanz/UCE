package de.fhkn.in.uce.reversal;

import java.util.Collections;
import java.util.Set;

import net.jcip.annotations.Immutable;
import de.fhkn.in.uce.plugininterface.NATSituation;
import de.fhkn.in.uce.plugininterface.NATTraversalTechniqueMetaData;
import de.fhkn.in.uce.plugininterface.message.NATTraversalTechniqueAttribute;
import de.fhkn.in.uce.plugininterface.util.NATTraversalTechniqueUtil;
import de.fhkn.in.uce.reversal.message.ReversalAttribute;

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
/**
 * Implementation of {@link NATTraversalTechniqueMetaData} for {@link Reversal}.
 *
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 *
 */
@Immutable
public final class ReversalMetaData implements NATTraversalTechniqueMetaData {
    private static final String RESOURCE_TRAVERSALED_SITUATIONS = "de/fhkn/in/uce/reversal/traversaledNATSituations"; //$NON-NLS-1$
    private final String name = "Reversal"; //$NON-NLS-1$
    private final String version = "1.0"; //$NON-NLS-1$
    private final int connectionSetupTime = 3;
    private final int timeoutInSeconds = 15;
    private final Set<NATSituation> traversaledNATSituations;
    private final NATTraversalTechniqueUtil util = NATTraversalTechniqueUtil.getInstance();

    /**
     * Creates a {@link ReversalMetaData} object.
     *
     * @throws Exception
     */
    public ReversalMetaData() throws Exception {
        this.traversaledNATSituations = Collections.unmodifiableSet(this.util
                .parseNATSituations(RESOURCE_TRAVERSALED_SITUATIONS));
    }

    /**
     * Copy constructor for {@link ReversalMetaData}.
     *
     * @param toCopy
     *            the {@link ReversalMetaData} to copy
     */
    public ReversalMetaData(final ReversalMetaData toCopy) {
        this.traversaledNATSituations = toCopy.traversaledNATSituations;
    }

    @Override
    public String getTraversalTechniqueName() {
        return this.name;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public int getMaxConnectionSetupTime() {
        return this.connectionSetupTime;
    }

    @Override
    public boolean providesDirectConnection() {
        return true;
    }

    @Override
    public Set<NATSituation> getTraversaledNATSituations() {
        return Collections.unmodifiableSet(this.traversaledNATSituations);
    }

    @Override
    public long getTimeout() {
        return this.timeoutInSeconds * 1000L;
    }

    @Override
    public NATTraversalTechniqueAttribute getAttribute() {
        return new ReversalAttribute();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + connectionSetupTime;
        result = (prime * result) + ((name == null) ? 0 : name.hashCode());
        result = (prime * result) + timeoutInSeconds;
        result = (prime * result) + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ReversalMetaData other = (ReversalMetaData) obj;
        if (connectionSetupTime != other.connectionSetupTime) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (timeoutInSeconds != other.timeoutInSeconds) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }
}
