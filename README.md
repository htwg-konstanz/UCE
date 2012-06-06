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

# UCE - Universal Connection Establishment

Universal Connection Establishment (UCE) is a combination of firewall and NAT traversal
techniques that is designed to provide universal IP connectivity with minimal administrative
and configuration overhead.

## NAT Traversal Components

Communication through NATs can basically be achieved using the following
techniques.

*	Relaying

	A relay server relays messages between the communicating peers. Always
	works but requires a publicly available relay server.

*	Connection Reversal

	The source peer that wants to open a connection to the target peer is
	publicly visible. The target peer connects to the source peer, i.e. the
	connection is reversed. Requires some sort of rendezvous server.

*	Hole Punching

	Both peers open connections to the outside, thus creating a mapping in the
	NAT, effectively punching a hole. This mapping can then be used to connect
	to. Requires some sort of rendezvous server to exchange mapping info.

* 	UPnP

	If the NAT box supports UPnP it can be directed to automatically create
	persistent mappings. The peer behind this NAT box is then effectively
	visible from the outside.

*	Direct connect

	If the target peer is publicly available the source can directly connect
	to it.


UCE tries to unify a multitude of NAT traversal techniques in one library
(and application) that is able to guarantee connectivity in basically any
network environment, including mobile networks.

An essential part of UCE is exposure to Java RMI. That allows building RMI
applications over NAT and firewall boundaries.

## State

UCE originates from a BMBF funded research project. It is under active
development. Most components are in a proof-of-concept or prototype state and
we are in the process of gradually releasing them to the public. During this
transition phase, the version of all modules is fixed to 1.0.

See the [project page](http://example.net/) for detailed information, references
and publications. Some information is outdated and will be updated once the
project has been fully released and a new maven repository is up and running.

## UCE Modules and Bundles

All modules and bundles are located in a flat hierarchy parallel to
parent. Modules are components of UCE. Bundles are different from modules
in the way that they extend and bundle modules to new releases. E.g.
'UCE Messages' is a module that implements all the core messages, while
'UCE Core' is a bundle that bundles all UCE core functionality to
one release.

*	uce/

	The uce project, it builds all modules and bundles.

*	parent/

	The parent project. All modules / bundles inherit this one.
	It has all common settings and dependencies.

* 	relay/

	The relay project, it builds all the relay modules.
	
*	relay.parent

	Relay parent project, inherited by relay modules

*	relay.core

	Core components of the Relay modules.

*	relay.server

	An implementation of a relay server that relays messages between a client
	and peers.
	
*	relay.client

	A relay client. The client is the host that allocates and directs relay
	connections on the relay server.

*	relay.rmi

	RMI related socket factories and remote objects to expose relay
	functionality to RMI.

*	reversal

	Connection reversal project

*	reversal.parent

	Connection reversal parent project
	
*	reversal.source

	The source of a connection, i.e. the public client that wants to connect
	to a service behind NAT
	
* 	reversal.target

	The target service that is behind NAT. It reverses the connection back to
	the source.
	
*	reversal.mediator

	Rendezvous server that negotiates connection reversal parameters between
	source and target.

*	reversal.rmi

	RMI socket factories and remote objects for connection reversal.
	
## Credits

*	Lead / Maintainer:

	Thomas Zink, tzink {at-sign} htwg-konstanz {a dot} de

*	Contributors:

	- Daniel Maier, all initial code
	- Stefan Lohr, connection reversal, web hole punching, some demos
