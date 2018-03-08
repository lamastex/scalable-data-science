---
title: Local Computer
permalink: /sds/basics/infrastructure/local/
sidebar:
  nav: "lMenu-SDS-2.2"
---

## How to work with Apache Spark on or from your local laptop or desktop computer?

### Cloud-full

We will mostly be using Apace Spark via the free *databricks community edition* from [https://community.cloud.databricks.com/](https://community.cloud.databricks.com). Just Sign Up if you have not done so already.
 
To work on the databricks community edition all you need is a laptop computer with a browser and internet connection.

If you do not own a laptop, which is highly recommened for the course, then you may use a desktop computer that you can access (perhaps in a computer lab).

### Cloud-free

It is also important to be able to run Spark locally on your laptop. 
So you can ignore this section if you do not own a laptop.

Towards this we will be using [docker](https://www.docker.com/) to set up the *local cloud-free* environment for the course:

* Setting up the local cloud-free environment for the course

1. Step 1: Download and Install Docker by following instructions below:
  * Linux OS
  * Mac OS
  * Windows OS
2. Step 2: Install JDK
  * Linux OS
  * Mac OS
  * Windows OS
3. Step 3: Run Docker from pre-built images for scalable data sciences
  * Follow instructions at [docker-sds](https://github.com/lamastex/scalable-data-science/tree/master/_sds/basics/infrastructure/docker/docker-sds) to jump-start from pre-built docker images in order to work on your laptop (recommend 2-4 GB of memory). You may need 8-16 GB of memory for more sophisticated docker compositions (but this is only needed for developers and data engineering scientists).
    * [How were these Docker images made from "scratch"?](https://github.com/lamastex/scalable-data-science/tree/master/_sds/basics/infrastructure/docker/)

**Optionally** after some downloads and setups, you can also:

* [work with Scala Spark from command-line using SBT](https://github.com/lamastex/scalable-data-science/tree/master/dev/commandline/sbt)
* [work with IntelliJ-IDEA](https://github.com/lamastex/scalable-data-science/tree/master/dev/ide/with-IntelliJIDEA).
* [work with Scala-IDE](/sds/basics/infrastructure/local/sparkScala/).

