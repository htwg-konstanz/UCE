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

import java.net.Socket;
import java.util.Collections;
import java.util.Set;

import de.fhkn.in.uce.plugininterface.ConnectionNotEstablishedException;
import de.fhkn.in.uce.plugininterface.NATSituation;
import de.fhkn.in.uce.plugininterface.NATTraversalTechnique;
import de.fhkn.in.uce.plugininterface.NATTraversalTechniqueMetaData;
import de.fhkn.in.uce.plugininterface.message.NATTraversalTechniqueAttribute;
import de.fhkn.in.uce.stun.message.Message;

public class NATTraversalTechniqueMock implements NATTraversalTechnique {
    private final NATTraversalTechniqueMetaData metaData;

    public NATTraversalTechniqueMock(final String name, final int maxConnectionSetupTime,
            final boolean providesDirectConnection) {
        this.metaData = new NATTraversalTechniqueMockMetaData(name, providesDirectConnection, maxConnectionSetupTime);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NATTraversalTechnique) {
            NATTraversalTechnique other = (NATTraversalTechnique) obj;
            return this.metaData.getTraversalTechniqueName().equals(other.getMetaData().getTraversalTechniqueName());
        }
        return false;
    }

    @Override
    public Socket createSourceSideConnection(final String targetId, final Socket controlConnection) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Socket createTargetSideConnection(final String targetId, final Socket controlConnection,
            final Message request) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void registerTargetAtMediator(final String targetId, final Socket controlConnection)
            throws ConnectionNotEstablishedException {
        // TODO Auto-generated method stub
    }

    @Override
    public void deregisterTargetAtMediator(final String targetId, final Socket controlConnection)
            throws ConnectionNotEstablishedException {
        // TODO Auto-generated method stub
    }

    @Override
    public NATTraversalTechniqueMetaData getMetaData() {
        return this.metaData;
    }

    public final class NATTraversalTechniqueMockMetaData implements NATTraversalTechniqueMetaData {
        private final String name;
        private final int maxConnectionSetupTime;
        private final boolean providesDirectConnection;

        public NATTraversalTechniqueMockMetaData(final String name, final boolean providesDirectConnection,
                final int maxConnectionSetupTime) {
            this.name = name;
            this.maxConnectionSetupTime = maxConnectionSetupTime;
            this.providesDirectConnection = providesDirectConnection;
        }

        @Override
        public String getTraversalTechniqueName() {
            return this.name;
        }

        @Override
        public int getMaxConnectionSetupTime() {
            return this.maxConnectionSetupTime;
        }

        @Override
        public Set<NATSituation> getTraversaledNATSituations() {
            return Collections.emptySet();
        }

        @Override
        public String getVersion() {
            return "0.1";
        }

        @Override
        public long getTimeout() {
            return 30 * 1000;
        }

        @Override
        public NATTraversalTechniqueAttribute getAttribute() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean providesDirectConnection() {
            return this.providesDirectConnection;
        }
    }

    @Override
    public NATTraversalTechnique copy() {
        return new NATTraversalTechniqueMock(this.metaData.getTraversalTechniqueName(),
                this.metaData.getMaxConnectionSetupTime(), this.metaData.providesDirectConnection());
    }
}
