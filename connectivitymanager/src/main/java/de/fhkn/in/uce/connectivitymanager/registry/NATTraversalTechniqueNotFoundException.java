/*
    Copyright (c) 2012 Alexander Diener, 

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fhkn.in.uce.connectivitymanager.registry;

/**
 * A exception of type {@code NATTraversalTechniqueNotFoundException} indicates
 * that a NAT Traversal Technique could not be found.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class NATTraversalTechniqueNotFoundException extends Exception {

    /**
     * The serial of this class.
     */
    private static final long serialVersionUID = 2958396841714163405L;
    private final String natTraversalTechniqueName;

    /**
     * Public constructor to create objects of type
     * {@code NATTraversalTechniqueNotFoundException}.
     * 
     * @param natTraversalTechniqueName
     *            The name of the NAT Traversal Technique which could not be
     *            found.
     */
    public NATTraversalTechniqueNotFoundException(final String natTraversalTechniqueName) {
        this.natTraversalTechniqueName = natTraversalTechniqueName;
    }

    /**
     * Getter for the name of the NAT Traversal Technique which could not be
     * found.
     * 
     * @return the name of the NAT Traversal Technique.
     */
    public String getNatTraversalTechniqueName() {
        return this.natTraversalTechniqueName;
    }

    @Override
    public String getMessage() {
        return this.toString();
    }

    @Override
    public String toString() {
        return "The NAT Traversal Technique named " + this.natTraversalTechniqueName + " could not be found."; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
