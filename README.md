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

*	modules

	All available modules

*	bundles

	Bundles are different from modules in the way that they
	extend and bundle modules to new releases. They are also
	not essential for functionality of the library but include
	demonstrations and examples.

## Modules

*	Core

	The core of UCE, basically required by all or most other modules.

*	HP-TCP

	HolePunching TCP

*	ConnectionReversal

*	Relay


