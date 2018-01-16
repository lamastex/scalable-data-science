This is to generate version 9 netflow logs on your laptop for tesing purposes.

## Step 1 - start softflowd — Traffic flow monitoring

Install `softflowd`

```
$ sudo apt-get install softflowd
```

Edit file
```
$ sudo vim /etc/default/softflowd
```

to look like this:
```
$ cat /etc/default/softflowd
#
# configuration for softflowd
#
# note: softflowd will not start without an interface configured.

# The interface softflowd listens on. You may also use "any" to listen
# on all interfaces.
INTERFACE="any"

# Further options for softflowd, see "man softflowd" for details.
# You should at least define a host and a port where the accounting
# datagrams should be sent to, e.g.
# OPTIONS="-n 127.0.0.1:9995"
OPTIONS="-n 127.0.0.1:9995"
```

To monitor your wireless interface named `wls8` based on seeing the ouput of `$ ifconfig` do:
```
$ sudo softflowd -D -i wls8 -v 9 -t maxlife=1 -n 127.0.0.1:9995
```

You should see output like so:
```
$ sudo softflowd -D -i wls8 -v 9 -t maxlife=1 -n 127.0.0.1:9995
[sudo] password for raazesh: 
Using wls8 (idx: 0)
softflowd v0.9.9 starting data collection
Exporting flows to [127.0.0.1]:9995
ADD FLOW seq:1 [fe80::10db:3b53:4a58:e7b6]:5353 <> [ff02::fb]:5353 proto:17
ADD FLOW seq:2 [10.10.3.118]:5353 <> [224.0.0.251]:5353 proto:17
ADD FLOW seq:3 [10.10.3.149]:33566 <> [64.233.161.189]:443 proto:6
ADD FLOW seq:4 [10.10.3.149]:34548 <> [35.186.224.62]:443 proto:6
ADD FLOW seq:5 [10.10.3.149]:51974 <> [172.217.21.162]:443 proto:6
ADD FLOW seq:6 [10.10.3.149]:51972 <> [172.217.21.162]:443 proto:6
```

OPTIONAL: To check if there is traffic on `lo` at port 9995 corresponding to `127.0.0.1:9995` from `softflowd` do:
```
sudo tcpdump -n -v -i lo port 9995
```
You should see somethinglike this:
```
tcpdump: listening on lo, link-type EN10MB (Ethernet), capture size 262144 bytes
13:00:01.131866 IP (tos 0x0, ttl 64, id 43951, offset 0, flags [DF], proto UDP (17), length 484)
    127.0.0.1.37240 > 127.0.0.1.9995: UDP, length 456
13:00:01.132333 IP (tos 0x0, ttl 64, id 43952, offset 0, flags [DF], proto UDP (17), length 472)
```

## Step 2 - Capture the netflow into files with nfcapd from nfdump

```
$ sudo apt-get install nfdump
$ sudo nfcapd -z -w  -T all -l /var/cache/nfdump -I any -S 2 -P /var/run/nfcapd.allflows.pid
Add extension: 2 byte input/output interface index
Add extension: 4 byte input/output interface index
Add extension: 2 byte src/dst AS number
Add extension: 4 byte src/dst AS number
Add extension: dst tos, direction, src/dst mask
Add extension: IPv4 next hop
Add extension: IPv6 next hop
Add extension: IPv4 BGP next IP
Add extension: IPv6 BGP next IP
Add extension: src/dst vlan id
Add extension: 4 byte output packets
Add extension: 8 byte output packets
Add extension: 4 byte output bytes
Add extension: 8 byte output bytes
Add extension: 4 byte aggregated flows
Add extension: 8 byte aggregated flows
Add extension: in src/out dst mac address
Add extension: in dst/out src mac address
Add extension: MPLS Labels
Add extension: IPv4 router IP addr
Add extension: IPv6 router IP addr
Add extension: router ID
Add extension: BGP adjacent prev/next AS
Add extension: time packet received
Add extension: NSEL Common block
Add extension: NSEL xlate ports
Add extension: NSEL xlate IPv4 addr
Add extension: NSEL xlate IPv6 addr
Add extension: NSEL ACL ingress/egress acl ID
Add extension: NSEL username
Add extension: NSEL max username
Add extension: nprobe latency
Add extension: NEL Common block
Add extension: Compat NEL IPv4
Add extension: NAT Port Block Allocation
Bound to IPv4 host/IP: any, Port: 9995
Startup.
```

Check we have files:
```
$ ls -al /var/cache/nfdump/
total 12
drwxr-xr-x  2 root root 4096 Jan 16 13:10 .
drwxr-xr-x 19 root root 4096 Jan 16 10:57 ..
-rw-r--r--  1 root root  276 Jan 16 13:10 nfcapd.current.15920
```
After a few minutes you should have rotations of the older files organized by `YYYY/mm/dd/hh/nfcapd.*`:
```
$ $ ls -al /var/cache/nfdump/
total 16
drwxr-xr-x  3 root root 4096 Jan 16 13:15 .
drwxr-xr-x 19 root root 4096 Jan 16 10:57 ..
drwxr-xr-x  3 root root 4096 Jan 16 13:15 2018
-rw-r--r--  1 root root  276 Jan 16 13:15 nfcapd.current.15920
```

See the first few lines:
```
$ nfdump -R /var/cache/nfdump/2018/01/16/13/nfcapd.201801161310 | head -5
Date first seen          Duration Proto      Src IP Addr:Port          Dst IP Addr:Port   Packets    Bytes Flows
2018-01-16 13:10:31.848     2.148 UDP        10.10.3.114:5353  ->      224.0.0.251:5353         3     1091     1
2018-01-16 13:10:31.745     0.039 TCP        10.10.3.149:48274 ->      130.238.3.9:443          1       52     1
2018-01-16 13:10:31.745     0.039 TCP        130.238.3.9:443   ->      10.10.3.149:48274        1      185     1
2018-01-16 13:10:31.848     1.024 UDP   fe80::1..9a:bc53.5353  ->         ff02::fb.5353         2      951     1
```




