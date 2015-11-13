    Copyright (c) 2012 HTWG Konstanz, 

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

# [UCE - Universal Connection Establishment](http://ice.in.htwg-konstanz.de/)

**THIS PROJECT IS NO LONGER MAINTAINED.**

Universal Connection Establishment (UCE) is a framework for firewall and NAT traversal
techniques that is designed to provide universal IP connectivity with minimal administrative
and configuration overhead.

Current build status:

[![Build Status](https://travis-ci.org/htwg/UCE.png?branch=master)](https://travis-ci.org/htwg/UCE)

## NAT introduction

Network Address (and Port) Translation (NAT / NAPT) allows address sharing of public IP
addresses and hiding private networks from the public. The downside is that NAT breaks
the end-to-end principle and prevents incoming pakets that are not related to outgoing
traffic from traversing the NAT device. As a result, hosts behind NAT are not reachable
from the public internet. For an explanation of NAT and it's terminology see
[RFC2663](http://tools.ietf.org/html/rfc2663). This is especially problematic for P2P
applications that require direct connections between participating peers.

One can distinguish between NAT mapping, that is how a NAT maps public endpoints (IP, Port)
to private endpoints, and NAT filtering, that is how the NAT deals with incoming pakets
and connection requests. Four strategies are common 
(see [RFC5128](http://tools.ietf.org/html/rfc5128) for more explanation):

* Endpoint-Independent (EI)
* Address-Dependent (AD)
* Adress- and Port-Dependent (APD)
* Connection-Dependent (CD)

The mapping and filtering behavior on a NAT device is usually called the _NAT behavior_.
There can be multiple NAT devices between two peers, the sum of all NAT behaviors on 
the path is called _NAT situation_. The NAT situation is decisive for the success of
NAT traversal strategies.

## NAT Traversal Components

NAT traversal describes the process of traversing NAT devices from the outside world
with the intend of enabling incoming pakets that are unrelated to outoing traffic.

The following NAT traversal techniques have been proposed 
in literature. See [P2PNat](http://www.brynosaurus.com/pub/net/p2pnat/),
[RFC5128](http://tools.ietf.org/html/rfc5128).

*	Direct connect

    If the target peer is publicly available or has a persistent mapping on the 
    NAT or a user defined mapping the source can directly connect to the public
    address.

*	Relaying

    Instead of directly communicating with each other, the peers connect to
    a public relay server. This always works but puts pressure on the relays
    and might increase delay and latency which is undesirable in certain applications.

*	Connection Reversal

    If only the target (serving) peer is behind NAT, or the source (requesting)
    peer's NAT is configured for persistent port forwarding or otherwise
    has a persistent non-filtering mapping, the connection can be reversed.
    In this case the target peer connects to the source peer. This requires
    the target to maintain a connection to a rendezvous or mediator that 
    signals the connection request and tells the target to reverse the connection.

*	Hole Punching

    Both peers open connections to the outside, thus creating a mapping in the
    NAT, effectively punching a hole. This mapping can then be used to connect
    to. Requires some sort of rendezvous server to exchange public and private
    endpoints.

* 	UPnP

    If the NAT box supports UPnP it can be directed to automatically create
    persistent mappings. The peer behind this NAT box is then effectively
    visible from the outside.

There are other solutions like NAT-PMP or SOCKS which are not discussed here.
A good overview can be found on [Wikipedia](http://en.wikipedia.org/wiki/NAT_traversal).

## So what's UCE and what's different

UCE tries to unify a multitude of NAT traversal techniques in one library
(and application) that is able to guarantee connectivity in basically any
network environment. It tries to collect information about the NAT situation
and then uses some logic to find the best suitable NAT traversal method.

In that regard it is quite similar to ICE or ANTS. ICE, however, is only defined
for UDP-based media streams whereas UCE focuses entirely on TCP. ANTS promises
similar behavior for TCP. Unfortunately, there is no implementation publicly
available. You can test their [NAT-Analyzer](http://nattest.net.in.tum.de/) 
(you can also download the code there if you look closely)

An essential part of UCE is exposure to Java RMI. That allows building RMI
applications over NAT and firewall boundaries.

## Contents

All modules and bundles are located in a flat hierarchy parallel to
parent. Modules are components of UCE. Bundles are different from modules
in the way that they extend and bundle modules to new releases. E.g.
'UCE Messages' is a module that implements all the core messages, while
'UCE Core' is a bundle that bundles all UCE core functionality to
one release.


* 	README.md

    This readme file
    
* 	LICENSE

    The license file. GPLv3.

* 	parent/

    The parent project with all global definitions and dependencies.

* 	uce/

    The UCE build project. Defines and builds all modules and bundles.
    Here run 
        mvn compile
    or
        mvn package

* 	core/

    Core functionality that is used by multiple other modules.

* 	plugininterface/

    UCE is built to be extendible using plugins and the java Service Loader.
    The plugininterface includes all common interfaces for NAT behavior, situation
    traversal techniques, method handlers and so on. All NAT traversal techniques
    for instance must implement the interface NATTraversalTechnique.

* 	stun/

    Another core technology in UCE this is an implementation of the
    [RFC5389](https://tools.ietf.org/html/rfc5389) STUN message standard. Only
    the messages are implemented here. Custom message handlers as well as custom 
    NAT traversal STUN messages are implemented alongside specific NAT traversal
    techniques.
     
*	stun.server/

    An implementation of a STUN server. This is _not_ compliant to RFC 5389
    since it uses TCP exclusivly and also opens TCP connections to the STUN
    client in order to investigate NAT filtering behavior.

* 	connectivitymanager/

    The connectivity manager implements all the logic of collecting NAT information,
    identifying the NAT situation and then choosing a suitable NAT traversal
    techniques. For that it always has a control connection open to a publicly
    available mediator (see below). The connectivity manager requires the implementation
    of at least one NAT traversal techniques as plugin in it's classpath or
    plugin path. For easy use in your application, use the
    connectivitymanager, to manage you connections transparently.
    
*	connectivitymanager.demo/
    
    A demo chat application that uses the connectivity manager to establish a
    peer-to-peer connection behind NATs and provides a simple CLI chat between
    the peers. You need to manually copy the NAT traversal jar files into
    the plugin directory for testing.

*   connectivitymanger.demo.complete/

    This is a bundle of the connectivitymanager demo and all related plugins for the demo.

*	mediator/

    The mediator is a publicly available rendezvous server that handles peers,
    peer requests, and exchanges endpoints. As the connectivity manager it requires the
    presense of NAT traversal plugins for the messages it should handle.

*	All-In-One-Mediator/

    The All-In-One-Mediator contains a mediator and all provided plugins from UCE.

*   master.server/

    The master server is a bundle of the stun, relay and mediator server with all plugins for the mediator.

*	directconnection/

    Implementation of the direct connection NAT traversal (not really a traversal
    method though).

*	directconnection.mediator/

    Direct connect request handler for the mediator.

* 	directconnection.message/

    STUN messages for direct connection.

* 	holepunching/

    Implementation of parallel TCP holepunching NAT traversal.
    
*	holepunching.mediator/

    Holepunching request handler
        
*	holepunching.message/

    Holepunching STUN messages.

*	relaying/

    Implementation of a TURN like relaying NAT traversal.

*	relaying.mediator/

    Relaying request handler.
        
* 	relaying.message/

    Relaying STUN messages.
    
*	relaying.server/

    Implementation of a TURN like Relay server. Not compliant to the standard.
    
*	reversal/

    Implementation of connection reversal NAT traversal.

*	reversal.mediator/

    Connection reversal request handler.
    
*	reversal.message/

    Connection reversal STUN messages.
    
*	socketswitch/

    A proof-of-concept implementation of TCP socket switching. Is able to switch
    connections from one socket to another. This should enable to change sockets
    on-the-fly while maintaining an active connection from an application point
    of view. Not very well tested, code is a mess right now and it is not yet
    integrated into the UCE framework.

## State of UCE

UCE originates from a BMBF funded research project. Most components are in a
proof-of-concept or prototype state. See the [project page](http://ice.in.htwg-konstanz.de/)
for detailed information, references, publications and presentations.

Currently implemented are the NAT traversal techniques mentioned above as well
as the connectivity manager, a modular mediator, STUN and relay server. There
are no plans from our side to extend this for support of further techniques.

## Clone and build UCE

- Clone the repo
    
        git clone git@github.com:htwg/UCE.git

- Compile and install

        cd UCE/uce
        mvn install

## Test the connectivity manager demo

- Copy the connectivitymanager.demo.complete-1.0-bin.[tar.gz, zip] archive from connectivitymanager.demo.complete/target to a target and source machine.
  Both or one of them can be behind a NAT device.

- Unpack both.

- Cd into connectivitymanager.demo.complete-1.0

- On the _target_ machine run:

        java -cp connectivitymanager.demo-1.0-jar-with-dependencies.jar de.fhkn.in.uce.connectivitymanager.demo.chat.ChatTarget <targetID>
        
  Where targetID is any arbitrary string you want as identifier for the target
 
- On the _source_ machine run:

        java -cp connectivitymanager.demo-1.0-jar-with-dependencies.jar de.fhkn.in.uce.connectivitymanager.demo.chat.ChatSource <targetID>
        
  Where targetID is the same identifier you used before.

You should now have a running CLI-based chat between the (NATed) peers.

*BEWARE* The demo application uses a built-in mediator and STUN server IP. These servers
are hosted by us and might not always be available.

To see how to integrate UCE into your own application see the connectivitymanager.demo
implementation. It boils down to do sth like the following:

        import de.fhkn.in.uce.connectivitymanager.connection.UCESocket;
        import de.fhkn.in.uce.connectivitymanager.connection.UCEUnsecureSocketFactory;
        
        UCESocket socketTpPartner = UCEUnsecureSocketFactory.getInstance().createTargetSocket(targetId);
        socketTpPartner.connect();

## Build your own bundle

- Copy the connectivitymanager.demo.complete directory and change the folder name

- Cd into this directory, head to the pom.xml and change the following:

        <artifactId>: Id of your bundle
        <name>: Name of your bundle

- Edit the source path in the `<file>`-tag in config/assembly-bin.xml.

- Add your new module in the module section of uce/pom.xml.

- Build UCE as above.

## Run your own mediator

- Build UCE as above.

- cd All-In-One-Mediator/target

- Unpack the UCE-All-In-One-Mediator-1.0-bin.[tar.gz, zip] archive in your destination folder.

- cd to UCE-All-In-One-Mediator-1.0 and execute

        java -jar mediator-1.0.jar <port> <user clean interval> <max lifetime>
eg.

        java -jar mediator-1.0.jar 10140 300 600
    
The mediator is now working on your own machine. To use it from the connectivity manager, you hava to change the mediator.properties. Unfortunately it does not currently work to change the system properties from the command line. To change the mediator.properties goto the directory:

    connectivitymanager/src/main/resources/de/fhkn/in/uce/connectivitymanager/mediatorconnection/

Edit the file `mediator.properties` like this:

    mediator.ip=<your mediator ip>
    mediator.port=<your mediator port>
    mediator.keepalive=600
    
Afterwards you have to rebuild the connectivity manager and the demo.

Similarly, you can also change the plugin directory location of the connectivity manager and the mediator. Just look into the appropriate resources directories, find the registry / techniqueregistry dirs and change the file `nattraversalregistry.properties`.

## Run the master server

- Build UCE as above.

- Unpack the master.server-1.0-bin.[tar.gz, zip] archive in to your destination folder.

- Cd into master.server-1.0.

- Now you have four posibilities to run the master server:

    1. Add your values into the provided properties file (config/master.server.properties).
    
    2. Set the values with system properties.

        -DARGUMENT=VALUE

    eg.
        
        -DSTUNFIRSTIP=127.0.0.1

    3. Provide the values as command line arguments:
        
        [-?]ARGUMENT=VALUE

    eg.
        
        -StunFirstIP=127.0.0.1  or  StunFirstIP=127.0.0.1

    4. A mixture of these three.

- Example of a correct start of the master server using command line arguments:

        java -jar master.server-1.0.jar StunFirstIP=127.0.0.2 StunSecondIP=127.0.0.3 MediatorPort=14001 RelayPort=14000 MediatorLifeTime=1 MediatorIteration=1

**NOTE**

The properties will be read and overwritten in the following ascending order:

    properties file < system properties < command line arguments

To start the master server correctly the following arguments need to be set:
    
    StunFirstIP
    StunSecondIP
    RelayPort			(optional)
    MediatorPort
    MediatorIteration
    MediatorLifeTime

## Plugin development

If you want to use your own plugins, please provide them in the corresponding plugins/ folder in the mediator and connectivitymanager.demo.
For the mediator it should look like this:

    ./core-1.0.jar
    ./jcip-annotations-1.0.jar
    ./log4j-1.2.17.jar
    ./mediator-1.0.jar
    ./plugininterface-1.0.jar
    ./slf4j-api-1.6.1.jar
    ./slf4j-log4j12-1.6.6.jar
    ./stun-1.0.jar
    ./plugins/
        directconnection.mediator-1.0.jar
        directconnection.message-1.0.jar
        holepunching.mediator-1.0.jar
        holepunching.message-1.0.jar
        relaying.mediator-1.0.jar
        relaying.message-1.0.jar
        reversal.mediator-1.0.jar
        reversal.message-1.0.jar

And for the demo it should look like this:

    ./connectivitymanager.demo-1.0-jar-with-dependencies.jar
    ./plugins/
        directconnection.message-1.0.jar
        directconnection-1.0.jar
        holepunching.message-1.0.jar
        holepunching-1.0.jar
        relaying.message-1.0.jar
        relaying-1.0.jar
        reversal.message-1.0.jar
        reversal-1.0.jar

## How it works

A target behind NAT that wants to be publicly available registers with a
publicly available mediator. In addition a public UCE STUN server can be used
to investigate the NAT behavior. A client that wants to connect needs to know
the mediator and the registered target ID. The cnnectivity manager on the source
side decides in which sequence NAT traversal techniques should be tried and using the 
mediator issues connection requests and starts connecting. As a last resort,
relaying is tried.

## What's left to do and how to contribute

Although UCE work has proven to work quite well in our tests there are some open
issues.

- UPnP support would be nice, although most UPnP implementations have severe
  security issues and should not really be activated. It is important to note here
  that all NAT traversal techniques implement the equals method correctly.

- Although UCE is intended for TCP, it would be nice to have a UDP HP implementation
  and be able to use UDP tunnels for TCP.

- UCE is both targetted towards RMI and P2P. But both is not yet really well
  supported. Although RMI has in all development stages been present and well
  tested (it works), the refactored implementation lacks the required RMI
  Factory classes.
 
- P2P support would require a mechanism to decentralize the mediators. This in
  turn would require a better user management as now (none at all). Sth like
  uuid@MediatorIP:MediatorPort would uniquely identify a user on a specific
  mediator (like SIP or Diaspora does). Then of course mediator-to-mediator
  communication must be implemented, that is, forwarding requests and responses
  between mediators. Same would be nice for relay servers too. Stun servers, however,
  do not require such a behavior.

If you like to contribute and think that you could deal with one of the issues
above, or you have your own ideas / improvements, just fork the repo, do your
stuff and send us a pull request. Also feel
free to contact the project maintainer at any time.

## Credits

*   Maintainer:
    
    none, project concluded in 2013

*	Lead:

    Thomas Zink, tzink {at-sign} htwg-konstanz {a dot} de

*	Contributors:

    - Daniel Maier: former lead and all initial code (see UCE_deprecated)
    - Stefan Lohr: connection reversal, web hole punching, some demos
    - Alexander Diener: connectivity manager, STUN, refactoring to plugins
    - Ellen Wieland / Steven Boeckle: socket switching
    - Robert Danczak: Configuration of bundles, CI with Travis and Jenkins, Master Server