# Setup networking for cluster (mostly Ubuntu Server)

make sure to purge network manager

## VLANs

Use one VLAN for WAN, one for cluster and one for switch admin (conceptually
simpler), three special ports, all others untagged on Cluster VLAN, special
ports are
* One untagged WAN for internet access
* One tagged WAN/Cluster for gateway/master machine (could run one VLAN untagged, but this is conceptually simpler)
* One untagged for admin (just for switch access)

## Setup VLANs on Ubuntu Server (without Network Manager)

Now that we now how to be explicit about vlan id include it in config

## Setup Firewall for completely open inside cluster and very restricted out of cluster, with ufw

enable IP forwarding

setup firewall rules (remember to suggest allowing all forwarding internally if
you are to fiddle with docker-type bridging and other weird things)

setup NAT rules

## (Optional) Setup master as gateway for Cluster vlan

Remember to setup ufw firewall

## (Optional) Setup caching DNS (bind9)

Maybe required by kubernetes/docker

## (Optional) Setup caching proxy for apt (squid-deb-proxy)

some bug forces you to restart every time you change IP (or something along
these lines), set a dhcp hook to restart

## (Optional) Setup automatically updated hostname for dynamic IP with Duckdns and isc-dhcp-server

## (Optional) Setup a NTP server to keep clocks in sync

TODO

## (Optional) Very unsophisticated Ansible
