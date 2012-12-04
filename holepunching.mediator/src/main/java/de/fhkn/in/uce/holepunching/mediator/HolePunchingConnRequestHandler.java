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
package de.fhkn.in.uce.holepunching.mediator;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.holepunching.message.HolePunchingAttribute;
import de.fhkn.in.uce.holepunching.message.HolePunchingMethod;
import de.fhkn.in.uce.mediator.peerregistry.UserData;
import de.fhkn.in.uce.mediator.peerregistry.UserList;
import de.fhkn.in.uce.mediator.util.MediatorUtil;
import de.fhkn.in.uce.plugininterface.mediator.HandleMessage;
import de.fhkn.in.uce.plugininterface.message.NATTraversalTechniqueAttribute;
import de.fhkn.in.uce.stun.attribute.EndpointClass.EndpointCategory;
import de.fhkn.in.uce.stun.attribute.ErrorCode.STUNErrorCode;
import de.fhkn.in.uce.stun.attribute.Username;
import de.fhkn.in.uce.stun.attribute.XorMappedAddress;
import de.fhkn.in.uce.stun.header.STUNMessageClass;
import de.fhkn.in.uce.stun.message.Message;
import de.fhkn.in.uce.stun.message.MessageStaticFactory;

/**
 * This custom handler handles connection request messages for hole punching. It
 * sends the private and public endpoints to the source and the target.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class HolePunchingConnRequestHandler implements HandleMessage {
    private static final Logger logger = LoggerFactory.getLogger(HolePunchingConnRequestHandler.class);
    private final MediatorUtil mediatorUtil = MediatorUtil.INSTANCE;
    private final UserList userList = UserList.INSTANCE;

    @Override
    public void handleMessage(final Message message, final Socket controlConnection) throws Exception {
        this.mediatorUtil.checkForAttribute(message, Username.class);
        final Username username = message.getAttribute(Username.class);
        final UserData user = this.userList.getUserDataByUserId(username.getUsernameAsString());
        if (user == null) {
            final String errorMessage = "User " + username.getUsernameAsString() + " not exists"; //$NON-NLS-1$ //$NON-NLS-2$
            this.sendFailureResponse(message, errorMessage, STUNErrorCode.BAD_REQUEST,
                    controlConnection.getOutputStream());
        } else {
            this.sendHPMessagesToPeers(user, message, controlConnection);
        }
    }

    private void sendHPMessagesToPeers(final UserData user, final Message requestMessage, final Socket socketToSource)
            throws IOException {
        final List<InetSocketAddress> addressesOfSource = new ArrayList<InetSocketAddress>();
        addressesOfSource.add(requestMessage.getAttribute(XorMappedAddress.class).getEndpoint());
        addressesOfSource.add(new InetSocketAddress(socketToSource.getInetAddress(), socketToSource.getPort()));
        final Message toTarget = this.createHPMessageWithAddresses(addressesOfSource);
        final List<InetSocketAddress> addressesOfTarget = new ArrayList<InetSocketAddress>();
        addressesOfTarget.add(user.getEndpointsForCategory(EndpointCategory.PRIVATE).get(0).getEndpointAddress());
        addressesOfTarget.add(new InetSocketAddress(user.getSocketToUser().getInetAddress(), user.getSocketToUser()
                .getPort()));
        final Message toSource = this.createHPMessageWithAddresses(addressesOfTarget);
        this.sendMessage(toTarget, user.getSocketToUser());
        this.sendMessage(toSource, socketToSource);
    }

    private void sendMessage(final Message toSend, final Socket socket) throws IOException {
        synchronized (socket.getOutputStream()) {
            toSend.writeTo(socket.getOutputStream());
        }
    }

    private Message createHPMessageWithAddresses(final List<InetSocketAddress> addresses) {
        final Message result = MessageStaticFactory.newSTUNMessageInstance(STUNMessageClass.REQUEST,
                HolePunchingMethod.FORWARDED_ENDPOINTS);
        for (InetSocketAddress address : addresses) {
            if (address.getAddress() instanceof Inet6Address) {
                final XorMappedAddress xorAddressIpv6 = new XorMappedAddress(address, ByteBuffer.wrap(
                        result.getHeader().getTransactionId()).getInt());
                result.addAttribute(xorAddressIpv6);
            } else {
                final XorMappedAddress xorAddressIpv4 = new XorMappedAddress(address);
                result.addAttribute(xorAddressIpv4);
            }
        }
        return result;
    }

    private void sendFailureResponse(final Message message, final String errorReaon, final STUNErrorCode errorCode,
            final OutputStream out) throws Exception {
        logger.debug(errorReaon);
        final Message failureResponse = message.buildFailureResponse(errorCode, errorReaon);
        failureResponse.writeTo(out);
    }

    @Override
    public NATTraversalTechniqueAttribute getAttributeForTraversalTechnique() {
        return new HolePunchingAttribute();
    }
}
