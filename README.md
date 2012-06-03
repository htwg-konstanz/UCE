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

## Contents

*	pom.xml

	The builder project, it builds all modules and bundles.

*	parent

	The parent project. All modules / bundles inherit this one.
	It has all settings and dependencies.

*	modules and bundles

	See below

## Modules and Bundles

All modules and bundles are located in a flat hierarchy parallel to
parent. Modules are components of UCE. Bundles are different from modules
in the way that they extend and bundle modules to new releases. E.g.
'UCE Messages' is a module that implements all the core messages, while
'UCE Core' is a bundle that bundles all UCE core functionality to
one release.

### Modules

*	messages

	UCE Messages. All core messages commonly used by UCE components.

*	relay.core

	Core components of the Relay modules

	
### Bundles

*	core

	all core components:
	- messages

* 	relay

	The UCE Relay project. Builds all relay modules

## Credits

*	Lead / Maintainer:

	Thomas Zink, tzink@htwg-konstanz.de

*	Contributors:

	- Daniel Maier
	- Stefan Lohr
