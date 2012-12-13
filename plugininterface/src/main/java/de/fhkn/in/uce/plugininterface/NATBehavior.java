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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.jcip.annotations.Immutable;
import de.fhkn.in.uce.plugininterface.message.NATSTUNAttributeType;
import de.fhkn.in.uce.stun.MessageFormatException;
import de.fhkn.in.uce.stun.attribute.Attribute;
import de.fhkn.in.uce.stun.attribute.AttributeHeader;
import de.fhkn.in.uce.stun.attribute.AttributeType;

/**
 * A NAT behavior represents a NAT device. The NAT devices is characterized by
 * the {@link NATFeatureRealization}s of {@link NATFeature}s.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
@Immutable
public final class NATBehavior implements Attribute {
    private final Map<NATFeature, NATFeatureRealization> behavior;
    private static final int LENGTH = 4;

    /**
     * Creates a {@link NATBehavior}. In this case all
     * {@link NATFeatureRealization}s are set to don't care.
     */
    public NATBehavior() {
        this(NATFeatureRealization.DONT_CARE, NATFeatureRealization.DONT_CARE);
    }

    /**
     * Creates {@link NATBehavior} with the given {@link NATFeatureRealization}
     * for mapping and filtering.
     * 
     * @param mapping
     *            the {@link NATFeatureRealization} of the mapping
     * @param filtering
     *            the {@link NATFeatureRealization} of the filtering
     */
    public NATBehavior(final NATFeatureRealization mapping, final NATFeatureRealization filtering) {
        final Map<NATFeature, NATFeatureRealization> modifiableMap = new HashMap<NATFeature, NATFeatureRealization>();

        modifiableMap.put(NATFeature.MAPPING, mapping);
        modifiableMap.put(NATFeature.FILTERING, filtering);

        this.behavior = Collections.unmodifiableMap(modifiableMap);
    }

    /**
     * Returns the {@link NATFeatureRealization} for the given
     * {@link NATFeature}.
     * 
     * @param natFeature
     *            the {@link NATFeature}
     * @return the {@link NATFeatureRealization} for the given
     *         {@link NATFeature}
     */
    public NATFeatureRealization getFeatureRealization(final NATFeature natFeature) {
        return this.behavior.get(natFeature);
    }

    /**
     * Returns the {@link NATFeature}s of this {@link NATBehavior}.
     * 
     * @return a set with {@link NATFeature}s
     */
    public Set<NATFeature> getNATFeatures() {
        return this.behavior.keySet();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.behavior == null) ? 0 : this.behavior.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (null == obj) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final NATBehavior other = (NATBehavior) obj;
        if (this.getFeatureRealization(NATFeature.MAPPING).equals(other.getFeatureRealization(NATFeature.MAPPING))
                && this.getFeatureRealization(NATFeature.FILTERING).equals(
                        other.getFeatureRealization(NATFeature.FILTERING))) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[mapping:"); //$NON-NLS-1$
        sb.append(this.getFeatureRealization(NATFeature.MAPPING));
        sb.append(","); //$NON-NLS-1$
        sb.append("filtering:"); //$NON-NLS-1$
        sb.append(this.getFeatureRealization(NATFeature.FILTERING));
        sb.append("]"); //$NON-NLS-1$
        return sb.toString();
    }

    /**
     * Creates a {@link NATBehavior} from a given encoding.
     * 
     * @param encoded
     *            the encoded {@link NATBehavior}
     * @param header
     *            the header of the attribute
     * @return the decoded {@link NATBehavior}
     * @throws IOException
     *             if the encoding could not be read
     * @throws MessageFormatException
     *             if the {@code encoded} is malformed
     */
    public static NATBehavior fromBytes(final byte[] encoded, final AttributeHeader header) throws IOException,
            MessageFormatException {

        final ByteArrayInputStream bin = new ByteArrayInputStream(encoded);
        final DataInputStream din = new DataInputStream(bin);

        // mapping feature
        final int mappingFeatureBits = din.readUnsignedByte();
        final NATFeature mappingFeature = NATFeature.fromEncoded(mappingFeatureBits);
        // mapping feature realization
        final int mappingRealizationBits = din.readUnsignedByte();
        final NATFeatureRealization mappingRealization = NATFeatureRealization.fromEncoded(mappingRealizationBits);
        // filtering feature
        final int filteringFeatureBits = din.readUnsignedByte();
        final NATFeature filteringFeature = NATFeature.fromEncoded(filteringFeatureBits);
        // filtering realization
        final int filteringRealizationBits = din.readUnsignedByte();
        final NATFeatureRealization filteringRealization = NATFeatureRealization.fromEncoded(filteringRealizationBits);

        if (null == mappingFeature || !mappingFeature.equals(NATFeature.MAPPING) || null == filteringFeature
                || !filteringFeature.equals(NATFeature.FILTERING)) {
            throw new MessageFormatException("Could not encode the NAT feature attributes."); //$NON-NLS-1$
        }

        if (null == mappingRealization || null == filteringRealization) {
            throw new MessageFormatException("Could not get the realization of a NAT feature"); //$NON-NLS-1$
        }

        return new NATBehavior(mappingRealization, filteringRealization);
    }

    @Override
    public AttributeType getType() {
        return NATSTUNAttributeType.NAT_BEHAVIOR;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final DataOutputStream dout = new DataOutputStream(bout);

        dout.writeByte(NATFeature.MAPPING.encode());
        dout.writeByte(this.getFeatureRealization(NATFeature.MAPPING).encode());
        dout.writeByte(NATFeature.FILTERING.encode());
        dout.writeByte(this.getFeatureRealization(NATFeature.FILTERING).encode());

        out.write(bout.toByteArray());
        out.flush();
    }
}
