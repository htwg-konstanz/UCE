/**
 * Copyright (C) 2011 Daniel Maier
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.fhkn.in.uce.messages;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;

/**
 * The decoded representation of a uce message. A uce message consists of a
 * {@link UceMessageHeader} and zero or more {@link UceAttribute}.
 * 
 * @author Daniel Maier
 * 
 */
public interface UceMessage {

    /**
     * Returns the {@link UceMethod} of this uce message.
     * 
     * @return the method of the message
     */
    UceMethod getMethod();

    /**
     * Returns the {@link SemanticLevel} of this uce message.
     * 
     * @return the semantic level of the message
     */
    SemanticLevel getSemanticLevel();

    /**
     * Returns the length of this uce message (without the message header).
     * 
     * @return the length of the message
     */
    int getLength();

    /**
     * Returns the transaction id of this uce message.
     * 
     * @return the transaction id of the message
     */
    UUID getTransactionId();

    /**
     * Return the {@link UceMessageHeader} of this uce message.
     * 
     * @return the header of the message
     */
    UceMessageHeader getHeader();

    /**
     * Appends the given {@link UceAttribute} to this message.
     * 
     * @param attribute
     *            the {@link UceAttribute} to be appended
     * 
     * @return the current object of {@link UceMessage} with the appended
     *         attribute (for method chaining)
     */
    UceMessage addAttribute(UceAttribute attribute);

    /**
     * Returns all {@link UceAttribute} of this uce message.
     * 
     * @return all attributes of this message as a list
     */
    List<UceAttribute> getAttributes();

    /**
     * Returns all {@link UceAttribute} its type is of the given parameter.
     * 
     * @param <T>
     *            the type of the desired attributes
     * @param attributeClass
     *            the class of the desired attributes
     * @return the desired attributes as a list
     */
    <T extends UceAttribute> List<T> getAttributes(Class<T> attributeClass);

    /**
     * Returns a single {@link UceAttribute} of the desired type. If the uce
     * message has more than one {@link UceAttribute} of this type, only the
     * first one is returned.
     * 
     * @param <T>
     *            the type of the desired attribute
     * @param attributeClass
     *            the class of the desired attribute
     * @return the attribute of the desired type
     */
    <T extends UceAttribute> T getAttribute(Class<T> attributeClass);

    /**
     * Checks whether this {@link UceMessage} has an attribute of the specified
     * type.
     * 
     * @param <T>
     *            the type of the attribute to be checked
     * @param attributeClass
     *            the class oft he attribute to be checked
     * @return true if this {@link UceMessage} has an attribute of the specified
     *         type, otherwise false
     */
    <T extends UceAttribute> boolean hasAttribute(Class<T> attributeClass);

    /**
     * Encodes and writes this {@link UceMessage} to the given output stream.
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

    /**
     * Creates and returns a corresponding error response to this
     * {@link UceMessage}. The {@link UceMethod} and transaction id of this
     * {@link UceMessage} are copied to the error response. In addition semantic
     * level {@link SemanticLevel#ERROR_RESPONSE} is set to the resulting error
     * response.
     * 
     * @return a corresponding error response to this message
     */
    UceMessage buildErrorResponse();

    /**
     * Does the same as {@link #buildErrorResponse()}. In addition it adds an
     * {@link ErrorCode} attribute with the specified error number and reason
     * phrase to the resulting error response.
     * 
     * @param errorNumber
     *            the error number
     * @param reasonPhrase
     *            the reason phrase
     * @return a corresponding error response to this message
     * @throws UnsupportedEncodingException
     *             if the charset (UTF-8) to encode the reason phrase is not
     *             supported
     */
    UceMessage buildErrorResponse(int errorNumber, String reasonPhrase)
            throws UnsupportedEncodingException;

    /**
     * Creates and returns a corresponding success response to this
     * {@link UceMessage}. The {@link UceMethod} and transaction id of this
     * {@link UceMessage} are copied to the success response. In addition
     * semantic level {@link SemanticLevel#SUCCESS_RESPONSE} is set to the
     * resulting success. response.
     * 
     * @return a corresponding success response to this message
     */
    UceMessage buildSuccessResponse();

    /**
     * Indicates whether this {@link UceMessage} has semantic level
     * {@link SemanticLevel#REQUEST}.
     * 
     * @return true if this message has semantic level request, otherwise false
     */
    boolean isRequest();

    /**
     * Indicates whether this {@link UceMessage} has semantic level
     * {@link SemanticLevel#INDICATION}.
     * 
     * @return true if this message has semantic level indication, otherwise
     *         false
     */
    boolean isIndication();

    /**
     * Indicates whether this {@link UceMessage} has semantic level
     * {@link SemanticLevel#SUCCESS_RESPONSE}.
     * 
     * @return true if this message has semantic level success response,
     *         otherwise false
     */
    boolean isSuccessResponse();

    /**
     * Indicates whether this {@link UceMessage} has semantic level
     * {@link SemanticLevel#ERROR_RESPONSE}.
     * 
     * @return true if this message has semantic level error response, otherwise
     *         false
     */
    boolean isErrorResponse();

    /**
     * Indicates whether this {@link UceMessage} is of the specified
     * {@link UceMethod}.
     * 
     * @param method
     *            the {@link UceMethod} to be checked against
     * @return true if this message is of the specified method, otherwise false
     */
    boolean isMethod(UceMethod method);
}
