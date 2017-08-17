---
title: Rootless Spark
permalink: /sds/basics/infrastructure/onpremise/rootless/
sidebar:
  nav: "lMenu-SDS-2.2"
author: "Dan Strängberg"
author_profile: true
---

## Installing Spark-Hadoop-Yarn-Hive-Zeppelin without Root Access

By [Dan Strängberg](https://www.linkedin.com/in/dan-str%C3%A4ngberg-a2ab8096/) with assistance from [Tilo Wiklund](https://www.linkedin.com/in/tilo-wiklund-682aa496/)

This guide will help you set up an Apache Spark cluster both in standalone mode and together with Apache Hadoop's HDFS and YARN along with Apache Hive and Apache Zeppelin, all without requiring root access. It assumes a basic familiarity with Spark, OpenSSH, and Bash (the use of which will be assumed throughout this guide). This guide assumes the following setup:

* A computer which you use to connect to other machines on the network. Could be your own computer, a workstation, or something similar.
* A number of networked machines which you can connect to. These will be used as master and workers for the Spark cluster.
* The same username for the master and all the workers.

The master will be referred to as separate from the workers but the same machine that is running the master node could also run a worker process. The guide also assumes using Hadoop version 2.8.0 and Spark veriosn 2.1.1 though it should apply to other versions with only minor changes.

## Video of the Meetup

[![Uppsala Big Data Meetup Video of the Event](https://img.youtube.com/vi/Z1vijINoV3k/0.jpg)](https://www.youtube.com/watch?v=Z1vijINoV3k)

The preconfigured files for the setup from the video are included in the `rootless-files` folder. Note that they require modification to be usable for your particular setup.

## Requirements

* OpenSSH (or alternative) installed on each machine with execution privileges,
* SSH login for each machine and access to each machine from your computer and the chosen master,
* read and execute permissions on your `/home/user/` folder on each machine.

## Preparations

Before beginning, decide which machine to use as master and which machines to use as workers. Make sure you can SSH to each
worker from the master and vice versa. The hostnames or IP addresses of each machine will be needed. Using Bash, run the
command `ifconfig` or `ip addr` to find the IP address of the current machine.

## Setting up passwordless SSH logins (optional but highly recommended)

Since we will be commnicating with the machines using SSH and Spark also communicate via SSH we will set up a
passwordless login to each machine and from the master to each worker.

### Generate a public/private keypair on your computer

If you do not have a public/private keypair on your computer the first step will be to generate one.

Make sure that you have a directory called `.ssh` in your home directory by running `ls -a ~`. If it does not exist, run the
command `mkdir ~/.ssh`.

To create the keypair run the command `ssh-keygen` from the `.ssh` directory in your home folder and select a filename for
your private key when prompted. The corresponding public key will be created as `filename.pub`. If you want or require a
specific type of keypair run the command `ssh-keygen -t [type]` where `[type]` is your desired keypair type, for example
`ed25519`.

### Generate a public/private keypair for your master

Generate a new public/private keypair to be used solely for connecting the master to the workers. Since you will need to
upload the private key to the master you do NOT want to use your own key to set up passwordless login from the master to
the workers.

Make sure that you have a directory called `.ssh` in your home directory by running `ls -a ~`. If it does not exist, run the
command `mkdir ~/.ssh`.

To create the keypair run the command `ssh-keygen` from the `.ssh` directory in your home folder and select a filename for
your private key when prompted. The corresponding public key will be created as `filename.pub`. If you want or require a
specific type of keypair run the command `ssh-keygen -t [type]` where `[type]` is your desired keypair type, for example
`ed25519`.

### Setting up passwordless logins

With the necessary keypairs created we are ready to set up the passwordless SSH login. We will need to set up passwordless
logins from your computer to the master and the workers using the computer's keypair and passwordless logins from the master
to each worker using the dedicated keypair. Make sure that each machine has a directory called `.ssh` in your home directory
by running `ls -a ~`. If it is missing on any machine run the command `mkdir ~/.ssh`.

First copy the public/private keypair for your master with the command `scp ~/.ssh/[keyfile] ~/.ssh/[keyfile].pub
[username]@[master]:.ssh` where `[keyfile]` is the filename given to the private key, `[username]` is your username on the
master, and `[master]` is the hostname or IP address of the master. Do NOT use your personal private/public keypair for
this.

Next, add your computer's public key to the `authorized_keys` file on the master by running the command `ssh-copy-id -i
~/.ssh/[keyfile].pub [username]@[master]'` on your computer, where `[keyfile].pub` is your public key, `[username]` is your
username on the master, and `[master]` is the hostname or IP address of the master. At this point you might want to test
that the passwordless login is working by running `ssh [username]@[master]` from your computer.

From the master, run the command `ssh-copy-id -i ~/.ssh/[keyfile].pub [username]@[worker]`, where `[keyfile].pub` is the
public key generated for the master node, `[username]` is your username on a worker, and `[worker]` is the hostname or IP
address of a worker. Do this for each worker. Test that it is working by running `ssh [username]@[worker]` from the master.

## Setting up Spark

### Downloading binaries

Download pre-built Spark binaries: [http://spark.apache.org/downloads.html](http://spark.apache.org/downloads.html)

Download Java JRE binaries: [http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html#]
(http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)

Extract the archives to a folder of your choice. The rest of this guide will assume that they have been extracted to your
home folder. If they are not in your home folder, change the paths accordingly.

**Note**: The Spark root folder will be referred to as `[spark]` and the Java JRE root folder will be referred to as
`[jre]`.

### Configuring Spark

Before we can begin using Spark we sill have to edit the configuration files.

Begin by copying the file `~/[spark]/conf/spark-env.sh.template` using the command `cp ~/[spark]/conf/spark-env.sh.template
~/[spark]/conf/spark-env.sh`. This will copy its contents to the new file `spark-env.sh` in the `~/[spark]/conf/` folder.

Open the newly created file `spark-env.sh` and add the following lines:

* `export JAVA_HOME="/home/[username]/[jre]"`
* `SPARK_MASTER_HOST="[master]"`

where `[username]` is your username for the master and the workers and `[master]` is the hostname or IP address of the
master. You can also make other changes as appropriate. All Spark configuration options are described in the comments of the
file `spark-env.sh.template` or the file `spark-env.sh` you just created.

Next create the file `slaves` in the `~/[spark]/conf/` by copying the template using the command `cp
~/[spark]/conf/slaves.template ~/[spark]/conf/slaves`.

Open the newly created file `slaves` and add, for each worker, the line `[worker]` where `[worker]` is the hostname or IP
address of that worker. You may also want to remove the line `localhost` so that a worker will not be started on your own
computer.

### Copying to machines

With everything configured properly we need to copy all the files to the master and each worker. Do this by first running
the commands `scp -r ~/[spark] ~/[jre] [username]@[master]:` where `[username]` is your username and `[master]` is the
hostname or IP address of the master. Similarly, for each worker, run `scp -r ~/[spark] ~/[jre] [username]@[worker]:` where
`[username]` is your username and `[worker]` is the hostname or IP address of the worker.

**Note**: If you're asked for your SSH login password during this then passwordless SSH login is not conigured properly.

### Starting and testing

After everything is copied it's time to make sure that everything works. To start everything at once run the script `start-all.sh` found in the `~/[spark]/sbin/` folder. If everything goes well, open a browser and go to `[master]:8080`. You should be able to see the Spark webUI with all workers connected. To launch a Spark shell against this cluster run `~/[spark]/bin/spark-shell --master spark://[master]:7077` where `[master]` is the hostname or IP address of the master. Once started you should be able to see the application in the Spark web-ui. You can also use `spark-submit` with appropriate options to submit jobs to the master. For example, `spark-submit --class org.apache.spark.examples.SparkPi --master spark://[master]:7077 --deploy-mode cluster examples/jars/spark-examples_2.11-2.1.1.jar 10` to run an example that computes an approximation of Pi.

## Setting up Hadoop

The next step is to use Spark as a part of Hadoop with YARN as the resource manager. This will, among other things, let Spark access the HDFS for data to analyze and several isntances to run simultaneously. For this we will first configure HDFS and YARN.

The binaries for Hadoop can be found here: [http://hadoop.apache.org/releases.html](http://hadoop.apache.org/releases.html)

This guide will assume that it has been extracted to the same folder as the Spark folder, i.e. your home folder. The Hadoop root folder will be referred to as `[hadoop]`. We will also assume that you want the same master and worker setup as before.

### Configuring HDFS

We will start with HDFS. Once HDFS is configured and running we will add YARN.

As with Spark, we first need to tell Hadoop where to look for Java. To do this, open the file `~/[hadoop]/etc/hadoop/hadoop-env.sh` and change the line `export JAVA_HOME=${JAVA_HOME}` to the `export JAVA_HOME="/home/[username]/[jre]"`.

Next, open the file `~/[hadoop]/etc/hadoop/core-site.xml`. Between the opening `<configuration>` tag and the closing `</configuration>` add the following:

```
<property>
  <name>hadoop.tmp.dir</name>
  <value>/tmp/hadoop_tmp</value>
</property>

<property>
  <name>fs.defaultFS</name>
  <value>hdfs://[master]:9000</value>
</property>
```

where `[master]` is the hostname or IP address of the master. The folder `/tmp/hadoop_tmp` can be changed to anything you like as long as it is somewhere you have write privileges and needs to exist on each machine you intend to use.

Next open the file `~/[hadoop]/etc/hadoop/hdfs-site.xml`. between the opening `<configuration>` tag and the closing `</configuration>` tag add the following:

```
<property>
  <name>dfs.replication</name>
  <value>k</value>
</property>

<property>
  <name>dfs.namenode.rpc-bind-host</name>
  <value>0.0.0.0</value>
</property>

<property>
  <name>dfs.namenode.servicerpc-bind-host</name>
  <value>0.0.0.0</value>
</property>
```

where `k` is the number of copies of a file to store on the DFS.

Lastly we need to let Hadoop know which slaves to use. To do this create a file called `slaves` in `[hadoop]/etc/hadoop/` and add the hostname or IP address of each slave to use.

### Starting HDFS

Before we can start HDFS we need to format the DFS. Do this by running `[hadoop]/bin/hdfs namenode -format`. After it has completed succesfully we can start the DFS by running `[hadoop]/sbin/start-dfs.sh` from the master. To check that it is working, start a browser and go to `[master]:50070` where `[master]` is the hostname or the IP address of the master. You should get the web interface of HDFS. From here you can check the status of datanodes, explor the filesystem, view logs, etc.

### Configuring YARN

Next we will configure YARN so that we can use it as the resource manager for Spark, among other things.

First add the following to the file `[hadoop]/etc/hadoop/yarn-site.xml`:

```
<property>
  <name>yarn.nodemanager.local-dirs</name>
  <value>/tmp/hadoop_tmp</value>
</property>

<property>
  <name>yarn.resourcemanager.hostname</name>
  <value>[master]</value>
</property>

<property>
  <name>yarn.resourcemanager.bind-host</name>
  <value>0.0.0.0</value>
</property>

<property>
  <name>yarn.nodemanager.host</name>
  <value>0.0.0.0</value>
</property>

<property>
  <name>yarn.resourcemanager.bind-host</name>
  <value>0.0.0.0</value>
</property>

<property>
  <name>yarn.nodemanager.local-dirs</name>
  <value>/tmp/hadoop_tmp</value>
</property>
```

Next add `export JAVA_HOME=[jre]` to the file `[hadoop]/etc/hadoop/yarn-env.sh` where `[jre]` is the JRE root folder.

### Starting YARN

Start YARN by running `[hadoop]/sbin/start-yarn.sh` from the master. To check that YARN is running try to connect to the web interface at `[master]:8088`.

### Configuring Spark to run with YARN

With YARN and HDFS running we want to configure Spark to use them.

Begin by creating a directory `/spark` in the DFS to hold the Spark .jar files. To do this, run `[hadoop]/bin/hdfs dfs -mkdir /spark`. To upload the .jar files run `[hadoop]/bin/hdfs dfs -put [spark]/jars /spark`. Check that the files were uploaded correctly by using the HDFS web interface or by running `[hadoop]/bin/hdfs dfs -ls /spark/jars`.

Next add the line `spark.yarn.archive hdfs:///spark/jars` to the file `[spark]/conf/spark-defaults.conf` to point Spark towards the uploaded files.

Lastly, and optionally, run a test program with `[spark]/bin/spark-submit --class org.apache.spark.examples.SparkPi --master yarn --deploy-mode cluster examples/jars/spark-examples_2.11-2.1.1.jar 10`. Note that if you are using a different version of Spark you will have to change `2.1.1` to your version.

### Downloading Hive

The Apache Hive binaries can be found at: [https://hive.apache.org/downloads.html](https://hive.apache.org/downloads.html)

This guide assumes version 2.1.1. Download and extract the archive to a folder of your choosing. We will assume it is extracted to the same folder that contains Spark and Hadoop. The Hive root folder will be referred to as `[hive]`.

### Configuring Hive

Make a copy of the file `[hive]/conf/hive-env.sh.template` and name it `[hive]/conf/hive-env.sh`. Open it and add the following lines:
```
export JAVA_HOME=[jre]
export HADOOP_HOME=[hadoop]
```
where as before `[jre]` is the JRE root folder and `[hadoop]` is the Hadoop root folder.

### Starting Hive

Before we can use Hive we need to create a couple of folders in the DFS for Hive. To do this run the following commands:
```
[hadoop]/bin/hdfs dfs -mkdir /tmp
[hadoop]/bin/hdfs dfs -mkdir /user/hive
[hadoop]/bin/hdfs dfs -mkdir /user/hive/warehouse
[hadoop]/bin/hdfs dfs -chmod g+w /tmp
[hadoop]/bin/hdfs dfs -chmod g+w /user/hive
[hadoop]/bin/hdfs dfs -chmod g+w /user/hive/warehouse
```
Next, initialize the database by running `[hive]/bin/schematool -dbType [type] -initSchema` where `[type]` is the type of database you want to initialize, e.g. `derby`.

We are now ready to start the Hive server and connect to it. To start the server run `[hive]/bin/hiveserver2`. After it has started run `[hive]/bin/beeline -u jdbc:hive2://[master]:10000` where `[master]` is the hostname or IP address of the machine running the Hive server. We will assume that the Hive server is running on the HDFS master.

If you get an error message about your user not being allowed to impersonate, add the following lines to the `[hadoop]/etc/hadoop/core-site.xml` file:
```
<property>
  <name>hadoop.proxyuser.dan.group</name>
  <value>*</value>
</property>

<property>
  <name>hadoop.proxyuser.dan.hosts</name>
  <value>*</value>
</property>
```

### Downloading Zeppelin

The Apache Zeppelin binaries can be downloaded at: [https://zeppelin.apache.org/download.html](https://zeppelin.apache.org/download.html)

We will assume version 0.7.1 for this guide. Download ad extract the archive to a folder of your choosing. We will assume it is extracted to the same folder that contains Spark and Hadoop. The Zeppelin root folder will be referred to as `[zeppelin]`.

### Configuring Hive

Copy the file `[zeppelin]/conf/zeppelin-env.sh.template` to `[zeppelin]/conf/zeppelin-env.sh` and add the following lines:
```
export JAVA_HOME=[jre]
export HADOOP_HOME=[hadoop]
export SPARK_HOME=[spark]
export HIVE_HOME=[hive]
```
where `[jre]` is the root folder of the JRE binaries, `[hadoop]` is the Hadoop root folder, `[spark]` is the Spark root folder and `[hive]` is the Hive root folder.

Next run the command `[zeppelin]/bin/zeppelin-daemon.sh start`, open a web browser and go to the address `[master]:8080`. In the top right there is a drop down menu. Select the option "Interpreter" and find the Spark section. Change the value of the property `master` to `yarn-client` to use the YARN resource manager. Next find the jdbc section. To use Hive, change `default.driver` to `org.apache.hive.jdbc.HiveDriver`, change `default.url` to `jdbc:hive2:[master]:10000`, and change `default.user` to your user name. Lastly add the following dependencies:
```
[hive]/jdbc/hive-jdbc-2.1.1-standalone.jar
[hadoop]/share/hadoop/common/hadoop-common-2.8.0.jar
```

### Testing Zeppelin

Test if Zeppelin is working by creating a notebook and running some code Spark and Hive commands. The jobs should also show up on the YARN web interface. If you are having trouble, try adding the following lines to the file `[hadoop]/etc/hadoop/yarn-site.xml` and restarting YARN:
```
<property>
  <name>yarn.nodemanager.pmem-check-enabled</name>
  <value>false</value>
</property>

<property>
  <name>yarn.nodemanager.vmem-check-enabled</name>
  <value>false</value>
</property>

<property>
  <name>yarn.timeline-service.hostname</name>
  <value>[master]</value>
</property>

<property>
  <name>yarn.timeline-service.bind-host</name>
  <value>0.0.0.0</value>
</property>
```
