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

import static org.junit.Assert.assertFalse;

import org.junit.Test;

public final class TestNATSituation {

    @Test
    public void testNotEquals() {
        NATSituation nat1 = new NATSituation();
        NATSituation nat2 = new NATSituation(NATFeatureRealization.ADDRESS_AND_PORT_DEPENDENT,
                NATFeatureRealization.ADDRESS_AND_PORT_DEPENDENT, NATFeatureRealization.ADDRESS_AND_PORT_DEPENDENT,
                NATFeatureRealization.ADDRESS_AND_PORT_DEPENDENT);

        assertFalse(nat1.equals(nat2));
    }

    @Test
    public void testNotEqualsWithNull() {
        NATSituation nat = new NATSituation();

        assertFalse(nat.equals(null));
    }

    @Test
    public void testNotEqualsWithNATBehavior() {
        NATSituation nat = new NATSituation();
        NATBehavior beh = new NATBehavior();

        assertFalse(nat.equals(beh));
    }
}
