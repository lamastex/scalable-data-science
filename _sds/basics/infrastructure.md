---
title: Infrastructure
permalink: /sds/basics/infrastructure/
sidebar:
  nav: "lMenu-SDS-2.2"
---

## Distributed Computing Requirements

We need infrastructure for processing big data. 

Many real-world datasets cannot be analysed on your laptop or a single desktop computer.

However, we can learn how to use Apache Spark on a laptop and even analyse big data sets *from* our laptop by running the jobs on a public or private *cloud*, a large cluster of computers that is housed elsewhere but accessible remotely. 

One of the easiest way of doing is through a fully managed Spark cluster databricks cluster:

* [datbricks community edition - Sign Up! It is FREE for all!](https://community.cloud.databricks.com/login.html)
To work on the databricks community edition all you need is a laptop computer with a browser and internet connection.
 
Thus, to work with big data we need to first:

* Learn [how to work with Spark on or from a local laptop or desktop computer](local/).

We will see some more **advanced** topics on infrastructure in the sequel and these can be skipped by most readers.

The following are **advanced** topics in self-managed cloud computing (it is optional for the SDS-2.2 course). One typically uses a powerful laptop to develop and deploy such insfrastructures in one of three environments: 

* [On-premise Cluster of Computers](onpremise/)
* In public commercially available clouds: 
  * AWS
  * Google
  * Azure
* Hybrid on-premise and public clouds.
