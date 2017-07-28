---
title: Infrastructure
permalink: /sds/basics/infrastructure/
sidebar:
  nav: "lMenu-SDS-2.2"
redirect_from:
  - /theme-setup/
---

## Distributed Computing Requirements

We need an infrastructure of computing machinery to store and process big data. 
Many real-world datasets cannot be analysed on your laptop or a single desktop computer. We will typically need a cluster of several computers to cope with big data. Such clusters are hosted in a public or private *cloud*, i.e. a cluster of computers that is housed elsewhere but accessible remotely. 

{% include toc %}

**Note:** We can learn how to use Apache Spark *on* or *from* our laptop and even analyse big data sets *from* our laptop by running the jobs in the cloud.
{: .notice--info}

## databricks - managed clusters in the cloud

One of the easiest way of doing is through a fully managed Spark cluster databricks cluster:

* [datbricks community edition - Sign Up! It is FREE for all!](https://community.cloud.databricks.com/login.html)
To work on the databricks community edition all you need is a laptop computer with a browser and internet connection.

## Getting your laptop ready
 
Thus, to work with big data we need to first:

* Learn [how to work with Spark on or from a local laptop or desktop computer](local/).


## Advanced Topics in self-managed clusters

We will see some more **advanced** topics on infrastructure in the sequel and these can be skipped by most readers.

The following are **advanced** topics in self-managed cloud computing (it is optional for the SDS-2.2 course). One typically uses a powerful laptop to develop and deploy such insfrastructures in one of three environments: 

* [On-premise Cluster of Computers](onpremise/)
* In public commercially available clouds: 
  * AWS
  * Google
  * Azure
* Hybrid on-premise and public clouds.
