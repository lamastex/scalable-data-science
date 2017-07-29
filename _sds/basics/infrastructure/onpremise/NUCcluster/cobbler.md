---
title: NUC Cluster
permalink: /sds/basics/infrastructure/onpremise/NUCcluster/cobbler/
sidebar:
  nav: "lMenu-SDS-2.2"
author: "Alexey Siretskiy"
author_profile: true
---

## Automating private bare-metal NUC cluster setup with Cobbler

By [Alexey Siretskiy](https://www.linkedin.com/in/alexey-siretskiy-254992a7/)


## Five Main Steps

[Building a NUC cluster](/sds/basics/infrastructure/onpremise/NUCcluster/) has the following five steps:

* Step 1. [Connecting Macbook, switch and  NUC](/sds/basics/infrastructure/onpremise/NUCcluster/01_configuring_switch/)
* Step 2. [Configuring NUC gateway](/sds/basics/infrastructure/onpremise/NUCcluster/02_Configuring_NUC_gateway/)
* Step 3. [Installing and configuring Cobbler on the NUC gateway](/sds/basics/infrastructure/onpremise/NUCcluster/03_installing_cobbler/)
* Step 4. [Provisioning NUC-worker  hosts](/sds/basics/infrastructure/onpremise/NUCcluster/04_provisioning_nuc/)
* Step 5. [Postconfigure -- hiding switch behind the firewall](/sds/basics/infrastructure/onpremise/NUCcluster/05_hiding_switch/)

{% include toc %}

# How-to's

One can not use "Desktop" version of the distribution, since

 >  "The reason is that Ubuntu does not support PXE installing from the
desktop ISO (netboot installer is not included). So just adding a
signature wont do any good..
Just use the server ISO and install a desktop from there."

https://github.com/cobbler/cobbler/issues/1331


>  replace in /usr/share/cobbler/web/cobbler.wsgi:
import django.core.handlers.wsgi
_application = django.core.handlers.wsgi.WSGIHandler()

> by:
from django.core.wsgi import get_wsgi_application
_application = get_wsgi_application()

https://github.com/cobbler/cobbler/issues/1488\
https://www.unixmen.com/setup-pxe-boot-environment-using-cobbler-centos-6-5/



links and errors

https://www.centos.org/forums/viewtopic.php?t=47775 \
https://bugs.launchpad.net/tuxboot/+bug/1190256

    ubuntu@pxeserver:~$ sudo cp /usr/lib/syslinux/modules/bios/libcom32.c32 /var/lib/tftpboot/
    ubuntu@pxeserver:~$ sudo cp /usr/lib/syslinux/modules/bios/libutil.c32 /var/lib/tftpboot/
    ubuntu@pxeserver:~$ sudo cp /usr/lib/syslinux/modules/bios/menu.c32 /var/lib/tftpboot





ON CENTOS
=========
`"/etc/httpd/conf.d/cobbler_web.conf" 46L, 1240C`

    # This configuration file enables the cobbler web interface (django version)

    <Directory "/usr/share/cobbler/web/">
    #        <IfModule mod_ssl.c>
    #            SSLRequireSSL
    #        </IfModule>
    #        <IfModule mod_nss.c>
    #            NSSRequireSSL
    #        </IfModule>
            SetEnv VIRTUALENV
            Options Indexes MultiViews
            AllowOverride All
            Order allow,deny
            Allow from all
    </Directory>

    <Directory "/var/www/cobbler_webui_content/">
            <IfModule mod_ssl.c>
                SSLRequireSSL
            </IfModule>
            <IfModule mod_nss.c>
                NSSRequireSSL
            </IfModule>
            Options +Indexes +FollowSymLinks
            AllowOverride All
            Order allow,deny
            Allow from all
    </Directory>

    # Use separate process group for wsgi
    WSGISocketPrefix /var/run/wsgi
    WSGIScriptAlias /cobbler_web /usr/share/cobbler/web/cobbler.wsgi
    WSGIScriptAlias /cobbler_webui_content /var/www/cobbler_webui_content
    WSGIDaemonProcess cobbler_web display-name=%{GROUP}
    WSGIProcessGroup cobbler_web
    WSGIPassAuthorization On

    <IfVersion >= 2.4>
        <Location /cobbler_web>
            Require all granted
        </Location>
        <Location /cobbler_webui_content>
            Require all granted
ss

    [vagrant@pxeserver ~]$ sudo yum -y install epel-release
    [vagrant@pxeserver ~]$ sudo yum install cobbler
    [vagrant@pxeserver ~]$ sudo yum install cobbler cobbler-web

https://www.centos.org/docs/5/html/5.1/Deployment_Guide/sec-sel-enable-disable.html

    [vagrant@pxeserver ~]$ sudo yum install dhcp

    # To disable tunneled clear text passwords, change to no here!
    PasswordAuthentication yes
    in "/etc/ssh/sshd_config" 154L, 4368C written

http://centoshowtos.org/installation/kickstart-and-cobbler/

    [root@pxeserver vagrant]# yum install  xinetd


remote tftp server for dnsmasq
------------------------------
https://ubuntuforums.org/showthread.php?t=1605715


    xadmin@cn102:~$ apt-cache  policy
    Package files:
     100 /var/lib/dpkg/status
         release a=now
     500 http://10.200.1.101/cblr/links/xenial-x86_64 xenial/main amd64 Packages
         release v=16.04,o=Ubuntu,a=xenial,n=xenial,l=Ubuntu,c=main,b=amd64
         origin 10.200.1.101
    Pinned packages:


 f

    xadmin@c2gw:~$ cat /etc/dnsmasq.conf
    interface=vlan2
    domain=c2.combient.com,10.200.1.0/24,local
    dhcp-range=10.200.1.100,10.200.1.199
    dhcp-sequential-ip
    dhcp-script=/var/lib/misc/dnsmasq-assign.sh
    dhcp-hostsfile=/var/lib/misc/dnsmasq.hosts
    ## pxe booting
    dhcp-boot=pxelinux.0,boothost,10.200.1.101
    #enable-tftp
    #tftp-root=/var/lib/tftpboot
    xadmin@c2gw:~$


 https://bugs.launchpad.net/ubuntu/+source/debmirror/+bug/1565751 \
[root@cn101 xadmin]# vi /usr/bin/debmirror

     2397     #next if $filename !~ /bz2$/;
     2398     next if  $filename !~  /xz$/;
