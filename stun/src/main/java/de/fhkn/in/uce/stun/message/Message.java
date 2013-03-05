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
package de.fhkn.in.uce.stun.message;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import de.fhkn.in.uce.stun.attribute.Attribute;
import de.fhkn.in.uce.stun.attribute.ErrorCode.STUNErrorCode;
import de.fhkn.in.uce.stun.header.MessageClass;
import de.fhkn.in.uce.stun.header.MessageHeader;
import de.fhkn.in.uce.stun.header.MessageMethod;

/*
 * TODO 
 * this interface duplicates methods from MessageHeader interface.
 * It could / should extend MessageHeader
 */

/**
 * The decoded representation of a message. A message consists of a
 * {@link MessageHeader} and zero or more {@link Attribute}.
 * 
 * @author daniel maier, alexander diener, thomas zink
 * 
 */
public interface Message {
    /**
     * Returns the {@link MessageMethod} of this message.
     * 
     * @return the method of the message
     */
    MessageMethod getMessageMethod();

    /**
     * Returns the {@link MessageClass} of this message.
     * 
     * @return the class of the message
     */
    MessageClass getMessageClass();

    /**
     * Returns the length of this message (without the message header).
     * 
     * @return the length of the message
     */
    int getLength();

    /**
     * Returns the transaction id of this message.
     * 
     * @return the transaction id of the message
     */
    byte[] getTransactionId();

    /**
     * Return the {@link MessageHeader} of this message.
     * 
     * @return the header of the message
     */
    MessageHeader getHeader();

    /**
     * Appends the given {@link Attribute} to this message.
     * 
     * @param attribute
     *            the {@link Attribute} to be appended
     * 
     * @return the current object of {@link Message} with the appended attribute
     *         (for method chaining)
     */
    Message addAttribute(Attribute attribute);

    /**
     * Returns all {@link Attribute} of this message.
     * 
     * @return all attributes of this message as a list
     */
    List<Attribute> getAttributes();

    /**
     * Returns all {@link Attribute} its type is of the given parameter.
     * 
     * @param <T>
     *            the type of the desired attributes
     * @param attributeClass
     *            the class of the desired attributes
     * @return the desired attributes as a list
     */
    <T extends Attribute> List<T> getAttributes(Class<T> attributeClass);

    /**
     * Returns a single {@link Attribute} of the desired type. If the message
     * has more than one {@link Attribute} of this type, only the first one is
     * returned.
     * 
     * @param <T>
     *            the type of the desired attribute
     * @param attributeClass
     *            the class of the desired attribute
     * @return the attribute of the desired type
     */
    <T extends Attribute> T getAttribute(Class<T> attributeClass);

    /**
     * Checks whether this {@link Message} has an attribute of the specified
     * type.
     * 
     * @param <T>
     *            the type of the attribute to be checked
     * @param attributeClass
     *            the class oft he attribute to be checked
     * @return true if this {@link Message} has an attribute of the specified
     *         type, otherwise false
     */
    <T extends Attribute> boolean hasAttribute(Class<T> attributeClass);

    /**
     * Encodes and writes this {@link Message} to the given output stream.
     * 
     * @param out
     *            the stream to that this message gets written to
     * @throws IOException
     *             if an I/O error occurs
     */
    void writeTo(OutputStream out) throws IOException;

    /**
     * Returns the encoded message as a byte array.
     * 
     * @return the encoded message as a byte array
     */
    byte[] toByteArray();

    Message buildFailureResponse(STUNErrorCode errorNumber, String reasonPhrase) throws UnsupportedEncodingException;

    Message buildSuccessResponse();

    boolean isRequest();

    boolean isIndication();

    boolean isSuccessResponse();

    boolean isFailureResponse();

    /**
     * Indicates whether this {@link Message} is of the specified
     * {@link MessageMethod}.
     * 
     * @param method
     *            the {@link MessageMethod} to be checked against
     * @return true if this message is of the specified method, otherwise false
     */
    boolean isMethod(MessageMethod method);
}
