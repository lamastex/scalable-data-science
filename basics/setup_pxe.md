# Setup Ubuntu Server master for network boot (BIOS/Legacy PXE)

to be safe disable EFI-boot (otherwise it might install as one and try to boot as other)

## Install and setup DHCP (isc-dhcp-server)

listen only locally (so as not to screw up rest of network)

Install

Add static (?) 

## Install and setup TFTP (tftpd-hdf via xinetd)

## Install HTTPd to serve files during installation (Apache)

## Setting boot flags

give http URL to ks file

give http URL to ubuntu install files (modifying cdrom:// prefix no longer required)

(optional) give proxy

reduce timeout

## Automatic Ubuntu Server installation via Kickstart-file

Note kickstart by Red Hat works also for a number of other distros

Include basic template

install openssh-server

install python (required by lots of stuff e.g. ansible)

## (Optional) Configure automatic download of public key for passwordless access

Note first-startup script because OS not in final state during %post

also 
       User squid
       StrictHostKeyChecking no
       UserKnownHostsFile=/dev/null
       IdentityFile /home/squid/.ssh/squid_key

## (Optional) Setting Ubuntu Server client hostname via DHCP

## (Appendix) Alternative approaches

Ubuntu-specific preseed file

Cobbler, not maintained for Ubuntu, alternatively run in Docker

MaaS, somewhat fragile and overkill, and does not work very well without intel AMT or similar (not available in newer NUCs)

CoreOS or CentOS might have been a better fit than Ubuntu (which seems to rely too much on Canonical in-house stuff?)
