---
title: Starting Notes: Quick start for docker-compose
permalink: /sds/basics/infrastructure/onpremise/dockerCompose/readmes/startingNotes/
sidebar:
  nav: "lMenu-SDS-2.x"
author: "Raazesh Sainudiin"
author_profile: true
---

# Quick Start

This is the `dockerCompose/readmes/startingNotes.md` file.

**PRE-REQUISITE:** You already need to have docker installed on your local system (laptop or provisioned VM). If not follow these instructions:

- [TASK 2 of /sds/basics/instructions/prep/](/sds/basics/instructions/prep/)

These starting notes are meant to quickly get you going with the following:

1. Download dockerCompose
  - download and set-up for using `docker-compose` on your local system (laptop or provisioned VM).
1. Cache the docker images from dockerhub 
1. Running `spark-shell`, `sbt`, etc. via `docker-compose`
  - start and stop a `docker-compose`d hadoop service (either the SKINNY way or the FAT way)
  - initially we only need the SKINNY `docker-compose` variant as this will require far less resources of your local system
  - attach to the service to run `spark-shell` from command-line.
1. `spark-submit` in `cluster` `deploy-mode`
  - attach to the service to run `spark-submit` from command-line.
1. Bring down the composition
1. (optionally) inject zeppelin notes (notebooks) into the zeppelin notebook server
  - skip this step if you went the SKINNY way for now.

Details of the `docker-compose` and the services it comes with including `hadoop` (hdfs, yarn, spark, sbt), extended by `zeppelin`, `jupyter`, `kafka` and `nifi` are in:

- [dockerCompose/readmes/README.md](/sds/basics/infrastructure/onpremise/dockerCompose/readmes/README/).

## 1. Download `dockerCompose` 

First copy the contents of the [dockerCompose](https://github.com/lamastex/scalable-data-science/tree/master/_sds/basics/infrastructure/onpremise/dockerCompose) directory (start from the `dockerCompose.zip`, but we may evolve the `dockerCompose` dir and the `.zip` may lag behind, so later on you may have to download newer content into `dockerCompose` as it becomes necessary).

Say you have a directory called `sds` inside your home directory called `user` and have copied `dockerCompose` into it.

Then run these commands or their equivalents in an open network to make sure docker is pulling down all the images as needed and test that `spark-shell` works. There are more complex docker compositions you can check as well by looking into `readmes/README.md`. 

NOTE: you may have to set your firewall rules to allow the different containers to "talk" to each other via the specific ports, etc. Ask you system administrator for support on such matters in a closed network environment. 

So, you should see files and directories like this:

```
$ pwd
/.../home/username/sds/dockerCompose
:dockerCompose $ ls
data				hadoopConfig			zimport
docker-compose-hszeppelin.yml	readmes
docker-compose.yml		sshConfig
```

## 2. Cache the docker images from dockerhub


The images will get updated as needs evolve. 
Run the following commands to pull the latest docker images:

```
:dockerCompose $ docker pull lamastex/hsbase
:dockerCompose $ docker pull lamastex/hszeppelin
:dockerCompose $ docker pull lamastex/hsjupyter
:dockerCompose $ docker pull apache/nifi
:dockerCompose $ docker pull lamastex/hskafka
```

## 3. Running `spark-shell`, `sbt`, etc. via `docker-compose` 

There are two ways (FAT and SKINNY) to run the `spark-shell`, `sbt`, etc.
 
The SKINNY way is recommended for older laptops although one cannot run several simultaneous services in this case.

### 3.a SKINNY way, i.e. with hadoop service only (better for older laptops)

```
:dockerCompose $ docker-compose -f docker-compose-hadoop.yml up -d
Recreating dockercompose_hadoop_1 ... done
```

Next open a browser and check that the webUIs are active at the following URLs:

- [http://localhost:50070/](http://localhost:50070/) for hdfs namenode information
- [http://localhost:8088/cluster](http://localhost:8088/cluster) for yarn cluster manager
- [http://localhost:8042/node](http://localhost:8042/node) for yarn worker (or spark-master) for spark jobs
- [http://localhost:8042/logs/](http://localhost:8042/logs/userlogs/) see userlogs for result of a `spark-submit` applications

Now listing the running docker processes as follows will show `dockercompose_hadoop_1` container we just composed up:

```
$ docker ps
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                                                                                                                                          NAMES
565e8dc215d9        lamastex/hsbase     "/root/start.sh --fo…"   53 seconds ago      Up 52 seconds       0.0.0.0:4040->4040/tcp, 7070/tcp, 0.0.0.0:8042->8042/tcp, 0.0.0.0:8088->8088/tcp, 0.0.0.0:50070->50070/tcp, 8081/tcp, 0.0.0.0:7070->8080/tcp   dockercompose_hadoop_1
```
 
```
:dockerCompose $ n$ docker-compose exec hadoop bash
root@565e8dc215d9:~# 
```

Now you are root inside the `hadoop` container (in the SKINNY way).

```
root@565e8dc215d9:~# ls
data  hadoop-2.9.2  programs  spark-2.3.0-bin-hadoop2.7  start.sh
```

### 3.b FAT way, i.e. with hadoop, zeppelin, jupyter, nifi and kafka services

**This is the preferred way for development in a more representative hadoop ecosystem.**

First do `docker ps` to list all the running container. 
You can `docker stop CONTAINER_ID` to stop a running container with the specified `CONTAINER_ID` given by the `docker ps` command.
Stopping unneeded containers can free up resources.

Then you can do the following `docker-compose` command to start all the services in `docker-compose.yml` file.

```
:dockerCompose $ docker-compose up -d
Creating network "dockercompose_default" with the default driver
Creating dockercompose_jupyter_1  ... done
Creating dockercompose_hadoop_1   ... done
Creating dockercompose_kafka_1    ... done
Creating dockercompose_nifi_1     ... done
Creating dockercompose_zeppelin_1 ... done
```

#### SKINNY or FAT: run `spark-shell` in `hadoop` service
Next, test if `spark-shell` works. We will first `exec`ute `bash` inside the core docker container called `hadoop` (it has hdfs, yarn and spark), our core workhorse. 

```
:dockerCompose $ docker-compose exec hadoop bash
root@c004f9cc093d:~# ls
data  hadoop-2.9.2  programs  spark-2.3.0-bin-hadoop2.7  start.sh
```

Now you are root inside the `hadoop` container (in the FAT way).

Run `spark-shell` like this:

```
root@c004f9cc093d:~# spark-shell 
2019-03-29 20:12:31 WARN  NativeCodeLoader:62 - Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
Setting default log level to "WARN".
To adjust logging level use sc.setLogLevel(newLevel). For SparkR, use setLogLevel(newLevel).
Spark context Web UI available at http://c004f9cc093d:4040
Spark context available as 'sc' (master = local[*], app id = local-1553890361338).
Spark session available as 'spark'.
Welcome to
      ____              __
     / __/__  ___ _____/ /__
    _\ \/ _ \/ _ `/ __/  '_/
   /___/ .__/\_,_/_/ /_/\_\   version 2.3.0
      /_/
         
Using Scala version 2.11.8 (OpenJDK 64-Bit Server VM, Java 1.8.0_191)
Type in expressions to have them evaluated.
Type :help for more information.
```

Congratulations! You have started spark-shell, this is the interctive REPL that is really behind various web-based notebooks that data scientists and data engineering scientists use, including [Apache Zeppelin](https://zeppelin.apache.org/), which supports a large hadoop ecosystem of interacting components for data engineeering science pipelines and processes and [Jupyter](https://jupyter.org/), originally developed for Julia, Python and R with extensions for Spark (popular with data scientists who are more familiar with Python and R).

We will mostly use zeppelin as a local alternative to databricks (until databricks provides local notebook server support in some minimal form, as anticipated/envisioned by Ion Stoica, a core member of the BDAS or Berkely Data Analytic Stack that gave rise to Apache Spark, in a tech video interview linked in the learning content). However, Jupyter can also be run in this docker composition to comfort the *Jupyter-istas*.

We will start by using `spark-shell` that is under the hood of these web-based notebooks.

It's time to write your first set of `spark-shell` commands.

```
scala> sc.setLogLevel("ERROR")

scala> val myrdd = sc.parallelize(Seq(1,2,3))
myrdd: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[0] at parallelize at <console>:24

scala> myrdd.collect()
res1: Array[Int] = Array(1, 2, 3)

scala> val sourdd = sc.textFile("file:///root/data/sou/*")
sourdd: org.apache.spark.rdd.RDD[String] = file:///root/data/sou/* MapPartitionsRDD[4] at textFile at <console>:24

scala> sourdd.count()
res3: Long = 21881  

scala> :quit
```

You can put these commands in a file, say `aSparkShellScript.scala`, that contains these lines:

```
sc.setLogLevel("ERROR")

val myrdd = sc.parallelize(Seq(1,2,3))

myrdd.collect()

val sourdd = sc.textFile("file:///root/data/sou/*")

sourdd.count()

```

and load it into `spark-shell` by using the `:load  aSparkShellScript.scala` command.

```
scala> :load aSparkShellScript.scala
Loading aSparkShellScript.scala...
myrdd: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[0] at parallelize at <console>:24
res2: Array[Int] = Array(1, 2, 3)
sourdd: org.apache.spark.rdd.RDD[String] = file:///root/data/sou/* MapPartitionsRDD[2] at textFile at <console>:24
res3: Long = 21881             
```

This is a simple and powerful way of using your favourite text editor on `myFirstCommands.scala` and simply rerunning the `load` command. 

Of course notebooks are more handy when it comes to interactive visualisations, etc. So we will see that too.

## 4. `spark-submit` in `cluster` `deploy-mode`

Run the job from jar using `spark-submit`.

```
root@30265c1156ef:~# spark-submit --class org.apache.spark.examples.SparkPi --master yarn --deploy-mode cluster spark-2.3.0-bin-hadoop2.7/examples/jars/spark-examples_2.11-2.3.0.jar
```

Here are the spark master and two worker nodes assigned by yarn for the `spark-submit` job:

```
root@30265c1156ef:~# ls hadoop-2.9.2/logs/userlogs/application_1553892810556_0002/container* 
container_1553892810556_0002_01_000001/ container_1553892810556_0002_01_000002/ container_1553892810556_0002_01_000003/

root@30265c1156ef:~# ls hadoop-2.9.2/logs/userlogs/application_1553892810556_0002/container_1553892810556_0002_01_000001/
prelaunch.err  prelaunch.out  stderr         stdout 

root@30265c1156ef:~# cat hadoop-2.9.2/logs/userlogs/application_1553892810556_0002/container_1553892810556_0002_01_000001/stdout | grep "Pi is "
Pi is roughly 3.1368956844784224
```

`Ctrl-D` to exit the hadoop service.

## 5. Bring down the composition

```
$ docker-compose down
```

Note that you will typically only do the following instead of `docker-compose down`:

```
$ docker-compose stop
```

This way you are just stopping the containers in the composition. This will keep all the injected files into hdfs, etc.

This way you can simply do the following to get back into the service/containers:

```
docker-compose start
```

and `attach` as before to continue your work on the re`start`ed service/containers.
 
## 6. zeppelin notebook postings

This is optional and not required until the local environment is provisioned sufficiently.

1. first do `docker-compose up -d` if `docker ps` does not show the container/service you started (in the FAT way), otherwise simply `start` and `attach` 
2. When zeppelin is running do:

```
:zimport $ pwd
/Users//all/git/scalable-data-science/_sds/basics/infrastructure/onpremise/dockerCompose/zimport
```

and then

```
:zimport $ python3 zimport.py --host localhost --port 8080 test/
```

Finally, if you want to make changes from zeppelin then `test/` should be mounted as a volume in yaml and the notebook exported back.
