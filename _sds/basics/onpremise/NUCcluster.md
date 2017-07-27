---
layout: single
title: Automating private bare-metal cluster setup with Cobbler
permalink: /sds/basics/onpremise/NUCcluster/
---

By [Alexey Siretskiy](https://www.linkedin.com/in/alexey-siretskiy-254992a7/)

### Abstract

Aside from the supercomputing centers, a small private cluster up to about 10-50 cores provides sufficient amount of resources for the majority of tasks for startup-sized enterprises.
A 50-core cluster can occupy about 0.5 square meters of the desk space, consuming less than 500 watts under the load. This is comparable to the resources needed by a single  desktop-sized workstation.

As a buiding block we will use Intel [NUC](http://www.intel.com/content/www/us/en/products/boards-kits/nuc.html), a 10x10x3-5 cm box equipped with 4 CPU cores, fast SSD drive up to 2TB, and up to 32 GB RAM. See [Next Unit of Computing](https://en.wikipedia.org/wiki/Next_Unit_of_Computing) for more information.

[Cobbler](http://cobbler.github.io) is a piece of software which will be used to push *life* into NUC's *bare-bones*!

This material is intended to serve as guidelines for deployment, including network configuration, but omitting some software installation details. For the latter we will refer the reader to appropriate online tutorials.

-----------

### Materials and equipment

The main equipment for setup is the following:

 1. NUCs, [nuc7i3bnk](http://www.intel.com/content/www/us/en/products/boards-kits/nuc/kits/nuc7i3bnk.html)
 2. Netgear switch, [GS108Tv2](https://www.netgear.com/support/product/GS108Tv2)
 3. MacBook with Internet enabled to make the initial kick-off (or equivalent)

Apart from the above hardware equipment, we also need some software, connectors, Ethernet wires, and most importantly patience and persistence.

Cobbler helps to   automate  OS installation (PXE-install) on either  bare-metal or virtual host, providing possibility to configure each host according to it's role in the future cluster.
Cobbler is a mature product used by supercomputer centers (i.e. [UPPMAX](uppmax.uu.se)) and currently  has the best support for CentOS.

Its key features are:

  1. integration with [DNSmasq](http://www.thekelleys.org.uk/dnsmasq/doc.html), which is easy to configure and targeted for small networks. DNSmasq includes [TFTP](https://en.wikipedia.org/wiki/Trivial_File_Transfer_Protocol)-server for delivering files during [PXE](https://en.wikipedia.org/wiki/Preboot_Execution_Environment) booting, [DHCP](https://en.wikipedia.org/wiki/Dynamic_Host_Configuration_Protocol) server for leasing IP addresses to the nodes, DNS-caching server for resolving names.

1.  *mirror* a full Linux repo from the Internet repository and serve the packages with package manager (`yum`, `apt`), not only speeding up the deployment and installation process, but also making things work in Internet-restricted places, like banks.

1. More fine-grained provisioning to tune up the host to it's role could be achieved by integrating with  [Ansible](https://www.ansible.com), pulling and applying playbooks (`ansible-pull`)  from the GitHub, as exampled [here](https://www.stavros.io/posts/automated-large-scale-deployments-ansibles-pull-mo/)

The full list of capabilities could be found at the official page:
* [http://cobbler.github.io/manuals/quickstart/](http://cobbler.github.io/manuals/quickstart/)



--------


### Steps to go

The whole process could be divided into roughly  the following steps:


 1. [Connecting Macbook, switch and  NUC](configuring_switch.md)
 2. [Configuring NUC gateway](Configuring_NUC_gateway.md)
 3. [Installing and configuring Cobbler on the NUC gateway](installing_cobbler.md)
 4. [Provisioning NUC-worker  hosts](provisioning_nuc.md)
 5. [Postconfigure -- hiding switch behind the firewall](hiding_switch.md)







