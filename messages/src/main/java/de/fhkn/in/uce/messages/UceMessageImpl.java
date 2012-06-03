/*
    Copyright (c) 2012 Thomas Zink, 

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
package de.fhkn.in.uce.messages;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import de.fhkn.in.uce.messages.UceMessageHeaderDecoder.UceMessageHeaderImpl;

/**
 * An implementation of a {@link UceMessage}.
 * 
 * @author Daniel Maier
 * 
 */
final class UceMessageImpl implements UceMessage {

    private final UceMethod method;
    private final SemanticLevel semanticLevel;
    private final UUID transactionID;
    private final List<UceAttribute> attributes;

    /**
     * Creates a new {@link UceMessageImpl} with the given header.
     * 
     * @param header
     *            the header of the message
     */
    UceMessageImpl(UceMessageHeader header) {
        this(header.getMethod(), header.getSemanticLevel(), header.getTransactionId());
    }

    /**
     * Creates a new {@link UceMessageImpl}. The header is built of the given
     * <code>method</code>, <code>semanticLevel</code> and
     * <code>transactionID</code>.
     * 
     * @param method
     *            the method of this uce message
     * @param semanticLevel
     *            the semantic level of this uce message
     * @param transactionID
     *            the transaction id of this uce message
     * 
     * @throws NullPointerException
     *             if one of the parameters is null
     */
    UceMessageImpl(UceMethod method, SemanticLevel semanticLevel, UUID transactionID) {
        if (method == null || semanticLevel == null || transactionID == null) {
            throw new NullPointerException();
        }
        this.method = method;
        this.semanticLevel = semanticLevel;
        this.transactionID = transactionID;
        this.attributes = new Vector<UceAttribute>();
    }

    public UceMethod getMethod() {
        return method;
    }

    public SemanticLevel getSemanticLevel() {
        return semanticLevel;
    }

    public UUID getTransactionId() {
        return transactionID;
    }

    public UceMessageHeader getHeader() {
        return new UceMessageHeaderImpl(method, semanticLevel, getLength(), transactionID);
    }

    public int getLength() {
        int length = 0;
        for (UceAttribute a : attributes) {
            length += UceAttributeHeaderDecoder.HEADER_LENGTH;
            length += a.getLength();
        }
        return length;
    }

    public void writeTo(OutputStream out) throws IOException {
        out.write(toByteArray());
        out.flush();
    }

    public byte[] toByteArray() {
        // first write to local buffer to prevent half sent messages caused by
        // exceptions
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);
        // header
        try {
            getHeader().writeTo(bout);
            // write attributes
            for (UceAttribute a : attributes) {
                // header
                dout.writeShort(a.getType().encode());
                dout.writeShort(a.getLength());
                // value
                a.writeTo(bout);
            }
            return bout.toByteArray();
        } catch (IOException e) {
            // can't happen because we write on ByteArrayOutputStream
            throw new AssertionError();
        }
    }

    public UceMessage addAttribute(UceAttribute attribute) {
        attributes.add(attribute);
        return this;
    }

    public List<UceAttribute> getAttributes() {
        return attributes;
    }

    public <T extends UceAttribute> List<T> getAttributes(Class<T> attributeClass) {
        List<T> reAttributes = new Vector<T>();
        for (UceAttribute a : attributes) {
            if (a.getClass() == attributeClass) {
                // this is safe because we check for class equality before
                @SuppressWarnings("unchecked")
                T toAdd = (T) a;
                reAttributes.add(toAdd);
            }
        }
        return reAttributes;
    }

    public <T extends UceAttribute> T getAttribute(Class<T> attributeClass) {
        for (UceAttribute a : attributes) {
            if (a.getClass() == attributeClass) {
                // this is safe because we check for class equality before
                @SuppressWarnings("unchecked")
                T toReturn = (T) a;
                return toReturn;
            }
        }
        return null;
    }

    public <T extends UceAttribute> boolean hasAttribute(Class<T> attributeClass) {
        for (UceAttribute a : attributes) {
            if (a.getClass() == attributeClass) {
                return true;
            }
        }
        return false;
    }

    public UceMessage buildErrorResponse() {
        return new UceMessageImpl(method, SemanticLevel.ERROR_RESPONSE, transactionID);
    }

    public UceMessage buildErrorResponse(int errorNumber, String reasonPhrase)
            throws UnsupportedEncodingException {
        return new UceMessageImpl(method, SemanticLevel.ERROR_RESPONSE, transactionID)
                .addAttribute(new ErrorCode(errorNumber, reasonPhrase));
    }

    public UceMessage buildSuccessResponse() {
        return new UceMessageImpl(method, SemanticLevel.SUCCESS_RESPONSE, transactionID);
    }

    public boolean isRequest() {
        return semanticLevel == SemanticLevel.REQUEST;
    }

    public boolean isIndication() {
        return semanticLevel == SemanticLevel.INDICATION;
    }

    public boolean isSuccessResponse() {
        return semanticLevel == SemanticLevel.SUCCESS_RESPONSE;
    }

    public boolean isErrorResponse() {
        return semanticLevel == SemanticLevel.ERROR_RESPONSE;
    }

    public boolean isMethod(UceMethod method) {
        return this.method.equals(method);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
        result = prime * result + ((method == null) ? 0 : method.hashCode());
        result = prime * result + ((semanticLevel == null) ? 0 : semanticLevel.hashCode());
        result = prime * result + ((transactionID == null) ? 0 : transactionID.hashCode());
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
        if (!(obj instanceof UceMessageImpl)) {
            return false;
        }
        UceMessageImpl other = (UceMessageImpl) obj;
        if (attributes == null) {
            if (other.attributes != null) {
                return false;
            }
        } else if (!attributes.equals(other.attributes)) {
            return false;
        }
        if (method == null) {
            if (other.method != null) {
                return false;
            }
        } else if (!method.equals(other.method)) {
            return false;
        }
        if (semanticLevel != other.semanticLevel) {
            return false;
        }
        if (transactionID == null) {
            if (other.transactionID != null) {
                return false;
            }
        } else if (!transactionID.equals(other.transactionID)) {
            return false;
        }
        return true;
    }
}
