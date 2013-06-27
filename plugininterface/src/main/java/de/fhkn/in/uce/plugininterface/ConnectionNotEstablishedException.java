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

/**
 * This exception occurs if a connection via a NAT traversal technique could not
 * be established.
 *
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 *
 */
public final class ConnectionNotEstablishedException extends Exception {

    /**
     * The serial of this class.
     */
    private static final long serialVersionUID = 7704960463539566176L;
    private final String natTraversalTechniqueName;
    private final Throwable cause;
    private final String message;

    /**
     * Creates a {@link ConnectionNotEstablishedException}.
     *
     * @param natTraversalTechniqueName
     *            the name of the traversal technique
     * @param message
     *            the reason why the connection could not be established
     * @param cause
     *            the cause of the exception
     */
    public ConnectionNotEstablishedException(final String natTraversalTechniqueName, final String message,
            final Throwable cause) {
        super(message, cause);
        this.cause = cause;
        this.natTraversalTechniqueName = natTraversalTechniqueName;
        this.message = message;
    }

    /**
     * Returns the name of the traversal technique.
     *
     * @return the name of the traversal technique
     */
    public String getNatTraversalTechniqueName() {
        return this.natTraversalTechniqueName;
    }

    @Override
    public Throwable getCause() {
        return this.cause;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder("Connection with "); //$NON-NLS-1$
        builder.append(this.natTraversalTechniqueName);
        builder.append(" could not be established: "); //$NON-NLS-1$
        builder.append(this.message);
        builder.append("\n"); //$NON-NLS-1$
        builder.append("Cause: "); //$NON-NLS-1$
        builder.append(super.toString());
        return builder.toString();
    }
}
