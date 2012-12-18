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
package de.fhkn.in.uce.mediator.util;

import de.fhkn.in.uce.stun.attribute.Attribute;
import de.fhkn.in.uce.stun.attribute.AttributeType;
import de.fhkn.in.uce.stun.message.Message;

/**
 * Utility for common mediator functionality. It is implemented as singleton.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public enum MediatorUtil {
    INSTANCE;

    /**
     * Checks if a message has the requested attribute. The class of the
     * required attribute has to be the same. Subclasses will not be detected.
     * 
     * @param message
     *            the message to check
     * @param attributeClass
     *            the attribute which should be included in the message
     * @throws Exception
     *             if the requested attribute is not included in the message
     */
    public <T extends Attribute> void checkForAttribute(final Message message, final Class<T> attributeClass)
            throws Exception {
        if (!message.hasAttribute(attributeClass)) {
            final String errorMessage = attributeClass.getSimpleName() + " attribute expected"; //$NON-NLS-1 
            throw new Exception(errorMessage);
        }
    }

    /**
     * Checks if a message has an attribute with the given {@link AttributeType}
     * . This method can be used to check if a message includes a subclass of an
     * attribute with the given type.
     * 
     * @param message
     *            the message to check
     * @param requiredAttributeType
     *            the required {@link AttributeType}
     * @throws Exception
     *             if the message has no attribute with the given type
     */
    public void checkForAttributeType(final Message message, final AttributeType requiredAttributeType)
            throws Exception {
        boolean hasAttribute = false;
        for (final Attribute a : message.getAttributes()) {
            if (a.getType().equals(requiredAttributeType)) {
                hasAttribute = true;
                break;
            }
        }
        if (!hasAttribute) {
            throw new Exception(requiredAttributeType.toString() + " attribute expected"); //$NON-NLS-1
        }
    }
}
