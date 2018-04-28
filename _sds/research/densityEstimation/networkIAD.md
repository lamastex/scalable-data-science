---
layout: single
title: Research - Integrative Anomaly Detection in Computer Networks
permalink: /sds/research/densityEstimation/networkIAD/
author_profile: true
header:
  overlay_color: "#5e616c"
  overlay_image: /sds/research/densityEstimation/sahde/notes/MDE_20161010_141708_01.jpg
  caption: 
excerpt: 'for Scalable Mathematical Statistical Experiments.<br /><br /><br />'
---

{% include toc %}

# Project IAD:<br /> Integrative Anomaly Detection in Computer Networks

Prepared by Raazesh Sainudiin partly for [Combient AB](https://combient.com/).

**These are merely field notes for *live* research...**

## Basic Background Viewing

* [PlayList](https://www.youtube.com/playlist?list=PL_I1mOIPmfpbkPn3IoPiY6oGbtBiJ4wLS)

## Basic Background Reading

* [https://en.wikipedia.org/wiki/Packet_analyzer](https://en.wikipedia.org/wiki/Packet_analyzer)
* [https://lifehacker.com/how-to-tap-your-network-and-see-everything-that-happens-1649292940](https://lifehacker.com/how-to-tap-your-network-and-see-everything-that-happens-1649292940)
* [https://en.wikiversity.org/wiki/Internet_Protocol_Analysis](https://en.wikiversity.org/wiki/Internet_Protocol_Analysis)
* [http://resources.infosecinstitute.com/](http://resources.infosecinstitute.com/)
  * [http://resources.infosecinstitute.com/detection-prevention-dns-anomalies/](http://resources.infosecinstitute.com/detection-prevention-dns-anomalies/)

## Open Source Network Monitoring and Analytics Tools

### Network Monitoring Systems
* [https://github.com/OpenNMS/opennms](https://github.com/OpenNMS/opennms)

### Automated Anomaly Detector
* [https://cuckoosandbox.org/](https://cuckoosandbox.org/)

### Github showcases security

* [https://github.com/showcases/security](https://github.com/showcases/security)
  * [https://github.com/mozilla/MozDef](https://github.com/mozilla/MozDef)

### Other Codes

* [https://www.mlsecproject.org/](https://www.mlsecproject.org/)

* Structured Streaming of Network Logs
  * [https://databricks.com/blog/2016/07/28/structured-streaming-in-apache-spark.html](https://databricks.com/blog/2016/07/28/structured-streaming-in-apache-spark.html)
  * [https://docs.databricks.com/spark/latest/structured-streaming/examples.html](https://docs.databricks.com/spark/latest/structured-streaming/examples.html)

* Writing logs to hdfs:
  * [https://www.balabit.com/documents/syslog-ng-ose-latest-guides/en/syslog-ng-ose-guide-admin/html/configuring-destinations-hdfs.html](https://www.balabit.com/documents/syslog-ng-ose-latest-guides/en/syslog-ng-ose-guide-admin/html/configuring-destinations-hdfs.html)

* [docker container for DNS logs](https://hub.docker.com/r/storytel/dnsmasq/)
Some instructions on getting the DNS logs collection going:

```%sh
#Pull this docker down: storytel/dnsmasq

# Then you will need these two lines to start logging queries to a file: 
log-facility=/var/log/dnsmasq.log
log-queries=extra

# Once that is in place, you need a syslog-ng to pick up the file and send it off to hdfs.
```

## Literature

* [https://en.wikipedia.org/wiki/Dorothy_E._Denning](https://en.wikipedia.org/wiki/Dorothy_E._Denning) and her ground-breaking work:
    * [https://dl.acm.org/citation.cfm?id=22862](An Intrusion Detection Model)


