---
title: Step 2 of Building NUC Cluster
permalink: /sds/basics/infrastructure/onpremise/NUCcluster/Configuring_NUC_gateway/
sidebar:
  nav: "lMenu-SDS-2.2"
---

Configuring VLANs on NUC and switch
===================================


By [Alexey Siretskiy](https://www.linkedin.com/in/alexey-siretskiy-254992a7/)


The NUC node, `192.168.2.3`, is assumed to have CentOS installed and, after being connected to to `192.168.2.0` network, should have  Internet access enabled.
The network structure we have already is depicted at the upper part of the figure. By the end of this part of tutorial  the  network setup should look like as shown at the lower part of the figure.

<img src="pics/network2.png" alt="Network setup" style="width:300px;"/>

The reason for using [VLANs](https://en.wikipedia.org/wiki/Virtual_LAN) is easy to grasp considering that the NUC gateway has just one network interface card (NIC), but has to participate in two networks: one being  the external and the another as internal LAN for the cluster.

## Creating VLANs on the GW NUC, setting the IPs and fixing network

The node has one NIC, named as *e.g.* `eno1`. On top of this *physical* card we have to create 2 *virtual* ones, one for each of the VLANs.

    > ssh xadmin@192.168.2.3
    [root@c2gw xadmin]# ip link add link eno1 name vlan1 type vlan  id 1
Here name `vlan1` could be anything, but the number after the `id` field must correspond to the VLAN1 on the switch. Show the created VLAN:

    [root@c2gw xadmin]# ip -d link show vlan1
    5: vlan1@eno1: <BROADCAST,MULTICAST> mtu 1500 qdisc noop state DOWN mode DEFAULT qlen 1000
        link/ether ec:a8:6b:fb:5d:f8 brd ff:ff:ff:ff:ff:ff promiscuity 0
        vlan protocol 802.1Q id 1 <REORDER_HDR> addrgenmode eui64
Bring up the interface and assign the IP address:

    [root@c2gw xadmin]# ip link set vlan1 up
    [root@c2gw xadmin]# ip addr add 192.168.2.3/24 brd 192.168.2.255 dev vlan1
    [root@c2gw xadmin]# ip addr show vlan1
    5: vlan2@eno1: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc noqueue state UP qlen 1000
      link/ether ec:a8:6b:fb:5d:f8 brd ff:ff:ff:ff:ff:ff
      inet 192.168.2.3/24 brd 192.168.2.255 scope global vlan1
      valid_lft forever preferred_lft forever
      inet6 fe80::eea8:6bff:fefb:5df8/64 scope link valid_lft forever preferred_lft forever


Similar procedure must be repeated on the NUC GW in order to create VLAN2.
The only difference except the VLAN `id`, will be the IP settings line:

    [root@c2gw xadmin]# ip addr add 10.200.1.1/24 brd 10.200.1.255 dev vlan2
We need 2 VLANs, since the gateway `10.200.1.1/192.168.2.3` (lower part of the Figure) will use one physical cable to participate in two networks.

Now lets remove the  `192.168.2.3`   from `eno1`:

    [root@c2gw xadmin]#  ip addr del 192.168.2.3 dev eno1


Test  the networking, it should *not* be working:

     [root@c2gw xadmin]# ping 192.168.2.1
     [root@c2gw xadmin]# ping google.com

The reason is the following: we have now 2 VLANs, but the port 1 on the switch `192.168.2.2` is not configured yet to support tagging *i.e.* switch forwards *untagged* frames, and OS does not know to which of the virtual NICs we created to send those packets to.


In order to resolve the issue, lets login to the switch and mark port 1 at tagged `T` for `vlan1`. And at the same time unmark ports 2-7. Apply changes.

<img src="pics/router_vlan1.png" alt='router setting for VLAN1' style="width:100px;"/>


Test  the networking, it should *still not* be fully working:

     [root@c2gw xadmin]# ping 192.168.2.1
     PING 192.168.2.1 (192.168.2.1) 56(84) bytes of data.
     64 bytes from 192.168.2.1: icmp_seq=1 ttl=64 time=0.550 ms
     64 bytes from 192.168.2.1: icmp_seq=2 ttl=64 time=0.494 ms
     [root@c2gw xadmin]# ping google.com
     connect: Network is unreachable
So we can access MacBook, but not the Internet.
Why? The reason is the following: nodes in each `vlan` is isolated from the othen VLANs and from the outer world (`vlan` is implemented on Level2 in OSI). In order to communicate between `vlan`s and to the outer world, the device of Level3 is needed.

**Alexey should read more about it**
The default route must be redefined as well to go via `192.168.2.1` on `vlan1`:

    [root@c2gw xadmin]#  ip route add default via 192.168.2.1


Test  the networking, it should be working as expected:

     [root@c2gw xadmin]# ping 192.168.2.1
     PING 192.168.2.1 (192.168.2.1) 56(84) bytes of data.
     64 bytes from 192.168.2.1: icmp_seq=1 ttl=64 time=0.550 ms
     64 bytes from 192.168.2.1: icmp_seq=2 ttl=64 time=0.494 ms
     [root@c2gw xadmin]# ping google.com
     PING google.com (216.58.209.142) 56(84) bytes of data.
     64 bytes from arn09s05-in-f14.1e100.net (216.58.209.142): icmp_seq=1 ttl=53 time=46.4 ms






## Changing router settings

We have already created 2 VLANs (1 and 2) on the NUC GW. Now lets change the switch settings accordingly.
Login to the switch (`192.168.2.2`) and make sure that the setting are as the following.\
For VLAN1 (the one with `id 1`):

 1. assign ports 1 and 8 to VLAN1, all the rest port-cells are empty/unselected
 1. mark the port 8 as untagged (**U**), and port 1 as tagged (**T**). The tagged port should be used for the gateway (`192.168.2.3`)

For VLAN2 (the one with `id 2`):

 1. assign ports 1-7 to VLAN2
 1. mark ports 2-7 as untagged
 1. mark port 1 as tagged


<img src="pics/router_vlan2.png" alt='router setting for VLAN2' style="width:100px;"/>

Also the `PVID` (the default VLAN for each port)  settings should be satisfied:

<img src="pics/pvid.png" style="width:100px;"/>






## Firewall settings

After setting up 2 VLANs on NUC GW and configuring the switch, we should take care of the firewall on the NUC GW. Indeed, in the final setup the MacBook is absent, and the GW NUC is connected to the public network.


Enabling the packets forwarding for the kernel:

    [root@c2gw xadmin]# sysctl -w net.ipv4.ip_forward=1

These settings are required in order to make the Internet accessible to the nodes with private IPs behind the firewall.

    [root@c2gw xadmin]# systemctl start firewalld
    [root@c2gw xadmin]# firewall-cmd --direct --passthrough ipv4 -t nat -I POSTROUTING -o vlan1 -j MASQUERADE
    [root@c2gw xadmin]# firewall-cmd --direct --passthrough ipv4 -I FORWARD -o vlan2 -i vlan1 -m state --state RELATED,ESTABLISHED -j ACCEPT
    [root@c2gw xadmin]# firewall-cmd --direct --passthrough ipv4 -I FORWARD -i vlan2 -o vlan1 -j ACCEPT
    [root@c2gw xadmin]# firewall-cmd --add-interface vlan1 vlan2


## Result

By the end of this part we should have the setup as shown on the lower part of the figure (**which one?**). On the next part we will go briefly through the Cobbler installation and configuration of the corresponding associated services on the NUC GW to enable PXE-booting and provisioning  worker NUCs.

