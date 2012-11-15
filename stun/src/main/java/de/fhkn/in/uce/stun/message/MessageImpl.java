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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Vector;

import de.fhkn.in.uce.stun.attribute.Attribute;
import de.fhkn.in.uce.stun.attribute.AttributeHeader;
import de.fhkn.in.uce.stun.attribute.ErrorCode;
import de.fhkn.in.uce.stun.attribute.ErrorCode.STUNErrorCode;
import de.fhkn.in.uce.stun.header.MessageClass;
import de.fhkn.in.uce.stun.header.MessageHeader;
import de.fhkn.in.uce.stun.header.MessageHeaderDecoder.MessageHeaderImpl;
import de.fhkn.in.uce.stun.header.MessageMethod;
import de.fhkn.in.uce.stun.header.STUNMessageClass;

/**
 * An implementation of a STUN {@link Message}.
 * 
 * @author Daniel Maier, Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
final class MessageImpl implements Message {
    private final MessageMethod messageMethod;
    private final MessageClass messageClass;
    private final byte[] transactionID;
    private final List<Attribute> attributes;

    /**
     * Creates a implementation of {@link Message}.
     * 
     * @param header
     *            the corresponding {@link MessageHeader}
     */
    MessageImpl(final MessageHeader header) {
        this(header.getMessageClass(), header.getMethod(), header.getTransactionId());
    }

    /**
     * Creates a implementation of {@link Message}.
     * 
     * @param messageClass
     *            the class of the message
     * @param method
     *            the method of the message
     * @param transactionID
     *            the transaction id of the message
     */
    MessageImpl(final MessageClass messageClass, final MessageMethod method, final byte[] transactionID) {
        // TODO check if combination of message class and message method is
        // allowed
        if (messageClass == null || method == null || transactionID == null) {
            throw new NullPointerException();
        }
        this.messageMethod = method;
        this.messageClass = messageClass;
        this.transactionID = transactionID;
        this.attributes = new Vector<Attribute>();
    }

    @Override
    public MessageMethod getMessageMethod() {
        return this.messageMethod;
    }

    @Override
    public MessageClass getMessageClass() {
        return this.messageClass;
    }

    @Override
    public byte[] getTransactionId() {
        return this.transactionID;
    }

    @Override
    public MessageHeader getHeader() {
        return new MessageHeaderImpl(this.messageClass, this.messageMethod, this.getLength(), this.transactionID);
    }

    @Override
    public int getLength() {
        int length = 0;
        for (final Attribute a : this.attributes) {
            length += AttributeHeader.HEADER_LENGTH;
            length += a.getLength();
        }
        return length;
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        out.write(this.toByteArray());
        out.flush();
    }

    @Override
    public byte[] toByteArray() {
        // first write to local buffer to prevent half sent messages caused by
        // exceptions
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final DataOutputStream dout = new DataOutputStream(bout);
        // header
        try {
            this.getHeader().writeTo(bout);
            // write attributes
            for (final Attribute a : this.attributes) {
                // header
                dout.writeShort(a.getType().encode());
                dout.writeShort(a.getLength());
                // value
                a.writeTo(bout);
            }
            return bout.toByteArray();
        } catch (final IOException e) {
            // can't happen because we write on ByteArrayOutputStream
            throw new AssertionError();
        }
    }

    @Override
    public Message addAttribute(final Attribute attribute) {
        this.attributes.add(attribute);
        return this;
    }

    @Override
    public List<Attribute> getAttributes() {
        return this.attributes;
    }

    @Override
    public <T extends Attribute> List<T> getAttributes(final Class<T> attributeClass) {
        final List<T> reAttributes = new Vector<T>();
        for (final Attribute a : this.attributes) {
            if (a.getClass() == attributeClass) {
                // this is safe because we check for class equality before
                @SuppressWarnings("unchecked")
                final T toAdd = (T) a;
                reAttributes.add(toAdd);
            }
        }
        return reAttributes;
    }

    @Override
    public <T extends Attribute> T getAttribute(final Class<T> attributeClass) {
        for (final Attribute a : this.attributes) {
            if (a.getClass() == attributeClass) {
                // this is safe because we check for class equality before
                @SuppressWarnings("unchecked")
                final T toReturn = (T) a;
                return toReturn;
            }
        }
        return null;
    }

    @Override
    public <T extends Attribute> boolean hasAttribute(final Class<T> attributeClass) {
        for (final Attribute a : this.attributes) {
            if (a.getClass() == attributeClass) {
                return true;
            }
        }
        return false;
    }

    // @Override
    // public Message buildFailureResponse() {
    // return new MessageImpl(STUNMessageClass.FAILURE_RESPONSE,
    // this.messageMethod, this.transactionID);
    // }

    @Override
    public Message buildFailureResponse(final STUNErrorCode errorNumber, final String reasonPhrase)
            throws UnsupportedEncodingException {
        return new MessageImpl(STUNMessageClass.FAILURE_RESPONSE, this.messageMethod, this.transactionID)
                .addAttribute(new ErrorCode(errorNumber, reasonPhrase));
    }

    @Override
    public Message buildSuccessResponse() {
        return new MessageImpl(STUNMessageClass.SUCCESS_RESPONSE, this.messageMethod, this.transactionID);
    }

    @Override
    public boolean isRequest() {
        return this.messageClass == STUNMessageClass.REQUEST;
    }

    @Override
    public boolean isIndication() {
        return this.messageClass == STUNMessageClass.INDICATION;
    }

    @Override
    public boolean isSuccessResponse() {
        return this.messageClass == STUNMessageClass.SUCCESS_RESPONSE;
    }

    @Override
    public boolean isFailureResponse() {
        return this.messageClass == STUNMessageClass.FAILURE_RESPONSE;
    }

    @Override
    public boolean isMethod(final MessageMethod method) {
        return this.messageMethod.equals(method);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.attributes == null) ? 0 : this.attributes.hashCode());
        result = prime * result + ((this.messageMethod == null) ? 0 : this.messageMethod.hashCode());
        result = prime * result + ((this.messageClass == null) ? 0 : this.messageClass.hashCode());
        result = prime * result + ((this.transactionID == null) ? 0 : this.transactionID.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MessageImpl)) {
            return false;
        }
        final MessageImpl other = (MessageImpl) obj;
        if (this.attributes == null) {
            if (other.attributes != null) {
                return false;
            }
        } else if (!this.attributes.equals(other.attributes)) {
            return false;
        }
        if (this.messageMethod == null) {
            if (other.messageMethod != null) {
                return false;
            }
        } else if (!this.messageMethod.equals(other.messageMethod)) {
            return false;
        }
        if (this.messageClass != other.messageClass) {
            return false;
        }
        if (this.transactionID == null) {
            if (other.transactionID != null) {
                return false;
            }
        } else if (!this.transactionID.equals(other.transactionID)) {
            return false;
        }
        return true;
    }
}
