---
title: Step 4 of Building NUC Cluster
permalink: /sds/basics/infrastructure/onpremise/NUCcluster/04_provisioning_nuc/
sidebar:
  nav: "lMenu-SDS-2.2"
author: "Alexey Siretskiy"
author_profile: true
---

Provisioning NUC node with OS, PXE booting
==========================================


By [Alexey Siretskiy](https://www.linkedin.com/in/alexey-siretskiy-254992a7/)


## Five Main Steps

[Building a NUC cluster](/sds/basics/infrastructure/onpremise/NUCcluster/) has the following five steps:

* Step 1. [Connecting Macbook, switch and  NUC](/sds/basics/infrastructure/onpremise/NUCcluster/01_configuring_switch/)
* Step 2. [Configuring NUC gateway](/sds/basics/infrastructure/onpremise/NUCcluster/02_Configuring_NUC_gateway/)
* Step 3. [Installing and configuring Cobbler on the NUC gateway](/sds/basics/infrastructure/onpremise/NUCcluster/03_installing_cobbler/)
* Step 4. [Provisioning NUC-worker  hosts](/sds/basics/infrastructure/onpremise/NUCcluster/04_provisioning_nuc/)
* Step 5. [Postconfigure -- hiding switch behind the firewall](/sds/basics/infrastructure/onpremise/NUCcluster/05_hiding_switch/)

{% include toc %}


In order to be able to  install OS via network, the node's network interface card (NIC) must support PXE booting. This is so for the most modern NICs.

The only setting might  be necessary to enable -- is the network booting in the BIOS settings.

Lets attach empty NUC to port 2, which belongs to `vlan2` and press the start button.


Most likely one will face a screen similar to:

<img src="/sds/basics/infrastructure/onpremise/NUCcluster/pics/no_dhcp.png" alt=" no dhcp offers were received" style="width:200px;"/>

Check `systmemctl status dnsmasq`. If the status is `alive` -- check the firewall:

    [root@c2gw xadmin]# firewall-cmd --list-all

 Observe the `services` line. If that does not contain  `dhcp`, it means that firewall in blocking port 67/53.

    [root@c2gw xadmin]# firewall-cmd --add-service dhcp


Reboot the worker NUC. Now one should see that DHCP works, and the worker was assigned the local IP *e.g.* `10.200.1.103`. The trouble, however, is with the `TFTP`:

<img src="/sds/basics/infrastructure/onpremise/NUCcluster/pics/no_tftp.png" alt=" no dhcp offers were received" style="width:200px;"/>


That is correct, the TFTP service on port 69 is not enabled on the firewall.

    [root@c2gw xadmin]# firewall-cmd --add-service tftp dns http

Let's add `dns` and `http` as well. Reboot the worker NUC. The screen should display Cobbler greeting menu with a set of offers. Choose one to fire-up the PXE-install!

<img src="/sds/basics/infrastructure/onpremise/NUCcluster/pics/cobbler_welcome.png" alt=" no dhcp offers were received" style="width:200px;"/>



##Result

The setup up to the this moment allows usto establish a cluster by attaching new nodes to the switch  and automatically provision them with the OS. Our final mark could be to hide switch behind the NUC GW firewall.
