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
package de.fhkn.in.uce.connectivitymanager.connection;

import de.fhkn.in.uce.connectivitymanager.connection.configuration.ConnectionConfiguration;
import de.fhkn.in.uce.connectivitymanager.manager.source.UnsecureSourceSideConnectionEstablishment;
import de.fhkn.in.uce.connectivitymanager.manager.target.UnsecureTargetSideConnectionEstablishment;

public final class UCEUnsecureSocketFactory extends UCESocketFactory {
    private static final UCEUnsecureSocketFactory INSTANCE = new UCEUnsecureSocketFactory();

    private UCEUnsecureSocketFactory() {
        // private constructor
    }

    public static UCEUnsecureSocketFactory getInstance() {
        return INSTANCE;
    }

    @Override
    protected UCESocket createNewSourceSocket(String targetId, ConnectionConfiguration configuration) {
        try {
            return new UCEUnsecureSocket(targetId, configuration, new UnsecureSourceSideConnectionEstablishment());
        } catch (final Exception e) {
            throw new RuntimeException("Could not create UCESocket:", e); //$NON-NLS-1$
        }
    }

    @Override
    protected UCESocket createNewTargetSocket(String targetId) {
        try {
            return new UCEUnsecureSocket(targetId, new UnsecureTargetSideConnectionEstablishment());
        } catch (Exception e) {
            throw new RuntimeException("Could not create UCESocket:", e); //$NON-NLS-1$
        }
    }
}
