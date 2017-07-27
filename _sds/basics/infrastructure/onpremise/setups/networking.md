---
title: Network Setup
permalink: /sds/basics/infrastructure/onpremise/setups/networking/
sidebar:
  nav: "lMenu-SDS-2.2"
---

**Advanced**

## Setup Networking for Cluster

By [Tilo Wiklund](https://www.linkedin.com/in/tilo-wiklund-682aa496/) 

By following these instructions you will turn a switch and a computer
(henceforth the gateway) with a single network interface (network card) into
something similar to a home router.

When done the ports on the switch will be allocated as follows. One port will be
configured for WAN (internet) access, one port will be used by the gateway, and
any remaining ports will be define an internal network. On this internal network
any connected machines can freely communicate with each other and the gateway
but can access the outside (WAN/internet) only through the gateway.

The guide assumes the computer to be running Ubuntu 16.04 Server, though many of
the instructions will apply to other operating systems. If the computer is
running a desktop oriented version of Ubuntu you will probably have to purge, or
at least disable, network manager before proceeding (see the optional section
below).

## VLANs

In order to isolate the internal network and set up the gateway using a single
network interface we will use VLANs (Virtual LAN). Conceptually one may think of
a collection of VLANs as a collection of LANs. existing on the same, or at least
on potentially overlapping, hardware.

In this setup we will have up to three different VLANs:
* one connecting the WAN/internet interface and the gateway machine (VLAN 5),
* one connecting the gateway machine and all machines on the internal network (VLAN 4),
* one (more optional) exposing the switch's administrative interface (VLAN 1). 

Each VLAN is identified by a number, here 5, 4, and 1 for reasons specific to
the router. Except for peculiarities of the hardware used these numbers are more
or less arbitrary as long as they are distinct.

The way this setup is achieved in practice is by assigning physical ports on the
switch to different VLANs. In the simplest case, so called *untagged*, a port is
simply declared part of one VLAN and ports assigned to different VLANs cannot
communicate with each other. Using only untagged ports one can use one physical
switch as, at least conceptually, two or more switches with fewer ports.

As each physical port can, at least in general, be set as untagged for at most
one VLAN a slightly more complicated setup is required for ports that are to be
used for access to more than one VLAN. In our setup this the case for the
gateway, which needs to communicate both with the WAN (on VLAN 5) and the
internal network (on VLAN 4).

The way this is achieved is by setting up a port as *tagged* for the VLANs that
are to be accessible by that port. For the switch to send along traffic from a
port tagged for a VLAN, that traffic has to carry along an extra tag indicating
for which VLAN it is destined.

The ports on the switch will thus be allocated as follows:
* one WAN/internet port (untagged for VLAN 5),
* one gateway port (tagged for VLAN 4 and VLAN 5),
* one for switch administration (untagged for VLAN 1),
* all remaining ports for the internal network (untagged for VLAN 4).

The switch administration port is just meant to be used by the computers of
administrators. One may also want to allow (tagged) access to switch
administration from the gateway and/or the internal network, though probably
*not* from the WAN port. We keep it sealed off for a bit of extra security.

Note that it would be possible to have the gateway port untagged for *either*
VLAN 4 or VLAN 5. We have opted to keep both tagged as this makes configuration
somewhat more consistent. 

## Setup VLANs on Switch

How to configure VLANs on the switch depends on the manufacturer, model, and
version of the switch. What follows is an example of how the setup described
above can be configured on a NETGEAR ProSAFE GS108T.

TODO: Add an example based on our switch.

Q: Do we need the pvid thing?

## Setup VLANs on Ubuntu Server

Recall that the gateway, running Ubuntu 16.04, is connected to a (single) port
set as tagged for VLAN 4 (the internal network) and VLAN 5 (the WAN/internet
connection). In order to make the gateway able to communicate on both VLANs
Ubuntu therefore needs to be instructed to mark any network packages it
generates for the correct VLAN.

The way this is achieved is by creating what are essentially virtual network
interfaces, one for each VLAN. Both virtual interfaces actually communicate
using the same physical interface, connected to the switch. They differ from
each other, and the physical interface, in that they tag any packages they
generate for their respective VLAN. At least on the surface level any software
on the gateway can then treat these virtual interfaces as if they were two
distinct physical interfaces connected to different networks.

TODO: Are the VLAN packages required or not?

Before proceeding we need to identify the name of the physical interface
connected to the switch.

TODO: Describe how to identify NICs in Ubuntu

Throughout we will assume the interface to be named *eno1*. We will now
configure Ubuntu not to attempt to assign any address to eno1 itself but declare
two VLAN interfaces on top of it. 

The relevant file on Ubuntu 16.04 is `/etc/network/interfaces`. Search the file
for something similar to
```
auto eno1
iface eno1 inet dhcp
```
or
```
auto eno1
iface eno1 inet static
address ***
...
```
and modify it to
```
auto eno1
iface eno1 inet manual

```
TODO: I think we can just comment it all out, check what the consequences of that vs `manual` are!

The physical interface is now configured not to have any address assigned to it.
Next we set up access to VLAN 5 (WAN/internet). The general pattern is
```
auto vlan5
iface vlan5 inet ***
  vlan-raw-device eno1
  vlan_id 5
  ***
```
where the values at the asterisks depends on whether the address is to be
assigned statically or via DHCP. If the ip is assigned via DHCP (for example if
you are assigned a dynamic IP by your ISP) write
```
auto vlan5
iface vlan5 inet dhcp
  vlan-raw-device eno1
  vlan_id 5
```
or for a static IP write
```
auto vlan5
iface vlan5 inet static
  vlan-raw-device eno1
  vlan_id 5
  address X.X.X.X
  netmask X.X.X.X
  network X.X.X.X
  broadcast X.X.X.X
  gateway X.X.X.X
  dns-nameserver X.X.X.X
```
where `X.X.X.X` are replaced by whatever values are appropriate for your
external network.

The name `vlan5` is arbitrary and one is free to use something more descriptive
like `vlanwan`, as long as `vlan_id` is specified.

On the internal network we will use the subnet 10.0.10.0/24 and assign the
gateway the static address 10.0.10.100. Later we will reserve
10.0.10.1--10.0.10.99 for auxiliary things connected to the network and
10.0.10.101-10.0.10.254 for statically allocated addresses of computing nodes.

The virtual interface for VLAN 4 is thus configured as follows
```
auto vlan4
iface vlan4 inet static
  vlan-raw-device eno1
  vlan_id 4
  address 10.0.10.100
  netmask 255.255.255.0
  network 10.0.10.0
  broadcast 10.0.10.255
```
note that we do not specify a gateway (or any DNS servers).

Both virtual interfaces will now be brought up on boot. They can be activated
now by restarting the systemd service `networking`. Note that you may wish to
configure the firewall before bringing up the WAN interface. To restart the
service and bring up the interfaces run, with super user privileges, `systemctl
restart networking`.

## Setup Firewall for completely open inside cluster and very restricted out of cluster, with ufw

We will now set up the firewall using the `ufw` tool. The firewall will be
configured to allow all traffic except *incoming* traffic on the WAN interface
(vlan5) not going to SSH (usually 22). Note that this is a somewhat insecure,
but convenient, setup. The more security concerned user may wish to instead
allow arbitrary traffic only on explicitly selected interfaces, rather than on
all *except* explicitly selected ones. Our reason for the less secure setup here
is that we will be working with many virtual interfaces created by
Kubernetes/Docker/etc.

TODO: I think this disabled ICMP responses (Ping) on the WAN interface, which
could be problematic.

To configure the firewall run the following with root privileges. Begin by
making sure the firewall is disabled
```
ufw disable
```
next set default rules to allow all incoming, outgoing 
```
ufw default allow incoming
ufw default allow outgoing
ufw default allow routed
```
add a rule to explicitly accept port 22 (or whatever port you wish to use for SSH)
```
ufw allow 22
```
and a rule to deny any (other) incoming traffic on the wan interface
```
ufw deny incoming on vlan5
```
TODO: double check syntax

## (Optional) Setup master as gateway for Cluster vlan

While we now have a machine (the gateway) connected both to WAN (VLAN 5) and the
internal network (VLAN 4) it cannot yet act as a gateway for machines on the
internal network.

TODO: enable IP forwarding

TODO: setup NAT rules

TODO: Remember to setup ufw firewall

## (Optional) Setup caching DNS (bind9)

TODO: Maybe required by kubernetes/docker

## (Optional) Setup caching proxy for apt (squid-deb-proxy)

TODO: some bug forces you to restart every time you change IP (or something
along these lines), set a dhcp hook to restart

## (Optional) Setup automatically updated hostname for dynamic IP with Duckdns and isc-dhcp-server

TODO

## (Optional) Setup a NTP server to keep clocks in sync

TODO

## (Optional) Very unsophisticated Ansible

TODO

## (Optional) Wifi on gateway

TODO: WPA without network manager

## (Optional) Purging network manager

TODO: 
* Check if network manager is installed/active
* Disable network manager
* Purge network manager
* Mention problem with network manager and dhcp hooks

# Old raw notes

* Hardware
** disable legacy boot
** enable unlimited network attempts
** disable intel trusted crap?
** Set non-masters to always netboot (how?) (only if using MAAS!)
** Fix wake-on-lan? (MAAS 2.x does not support WOL though) Seems to work without config
* network vlan-config?
* Ubuntu server on master
** (IF WE WANT) FORWARDING do
   https://help.ubuntu.com/lts/serverguide/firewall.html#ip-masquerading
   and ensure
   net.ipv4.ip_forward=1
** (IF WE WANT) DNS (forward/cache) server 
   https://www.digitalocean.com/community/tutorials/how-to-configure-bind-as-a-caching-or-forwarding-dns-server-on-ubuntu-14-04
** (TODO) configure an ntp-server (??)
** Configure firewall (unless we're paranoid I'd suggest allowing traffic except incoming on the wan interface)
   ufw default allow outgoing
   ufw default allow incoming
   ufw default allow routed
   ufw allow ssh
   ufw deny in on WAN-INTERFACE to any
   ufw enable
** install vlan packages
** isc-dhcp-serverrver
