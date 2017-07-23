# Installing Spark-Hadoop-Yarn-Hive-Zeppelin without Root Access

By [Dan Strangberg](https://www.linkedin.com/in/dan-str%C3%A4ngberg-a2ab8096/) with assistance from [Tilo Wiklund](https://www.linkedin.com/in/tilo-wiklund-682aa496/) 

This guide will help you set up an Apache Spark cluster in standalone mode without requiring root access. It assumes a basic familiarity with Spark, OpenSSH, and Bash (the use of which will be assumed throughout this guide). This guide assumes the following setup:

* A computer which you use to connect to other machines on the network. Could be your own computer, a workstation, or something similar.
* A number of networked machines which you can connect to. These will be used as master and workers for the Spark cluster.
* The same username for the master and all the workers.

The master will be referred to as separate from the workers but the same machine that is running the master node could also run a worker process. 

## Video of the Meetup

[![Uppsala Big Data Meetup Video of the Event](https://img.youtube.com/vi/Z1vijINoV3k/0.jpg)](https://www.youtube.com/watch?v=Z1vijINoV3k)

## Requirements

* OpenSSH (or alternative) installed on each machine with execution privileges,
* SSH login for each machine and access to each machine from your computer and the chosen master,
* read and execute permissions on your `/home/user/` folder on each machine.

## Preparations

Before beginning, decide which machine to use as master and which machines to use as workers. Make sure you can SSH to each worker from the master and vice versa. The hostnames or IP addresses of each machine will be needed. Using Bash, run the command 
`ifconfig` or `ip addr show` to find the IP address of the current machine.

## Setting up passwordless SSH logins (optional but highly recommended)

Since we will be commnicating with the machines using SSH and Spark also communicate via SSH we will set up a 
passwordless login to each machine and from the master to each worker.

### Step 0: Generate a public/private keypair on your computer

If you do not have a public/private keypair on your computer the first step will be to generate one. 

Make sure that you have a directory called `.ssh` in your home directory by running `ls -a ~`. If it does not exist, run the command `mkdir ~/.ssh`. 

To create the keypair run the command `ssh-keygen` from the `.ssh` directory in your home folder and select a filename for your private key when prompted. The corresponding public key will be created as `filename.pub`. If you want or require a specific type of keypair run the command `ssh-keygen -t [type]` where `[type]` is your desired keypair type, for example `ed25519`.

### Step 1: Generate a public/private keypair for your master

Generate a new public/private keypair to be used solely for connecting the master to the workers. Since you will need to 
upload the private key to the master you do NOT want to use your own key to set up passwordless login from the master to 
the workers.

Make sure that you have a directory called `.ssh` in your home directory by running `ls -a ~`. If it does not exist, run the command `mkdir ~/.ssh`. 

To create the keypair run the command `ssh-keygen` from the `.ssh` directory in your home folder and select a filename for your private key when prompted. The corresponding public key will be created as `filename.pub`. If you want or require a specific type of keypair run the command `ssh-keygen -t [type]` where `[type]` is your desired keypair type, for example `ed25519`.

### Step 2: Setting up passwordless logins

With the necessary keypairs created we are ready to set up the passwordless SSH login. We will need to set up passwordless logins from your computer to the master and the workers using the computer's keypair and passwordless logins from the master to each worker using the dedicated keypair. Make sure that each machine has a directory called `.ssh` in your home directory by running `ls -a ~`. If it is missing on any machine run the command `mkdir ~/.ssh`.

First copy the public/private keypair for your master with the command `scp ~/.ssh/[keyfile] ~/.ssh/[keyfile].pub [username]@[master]:.ssh` where `[keyfile]` is the filename given to the private key, `[username]` is your username on the master, and `[master]` is the hostname or IP address of the master. Do NOT use your personal private/public keypair for this.

Next, add your computer's public key to the `authorized_keys` file on the master by running the command `ssh-copy-id -i ~/.ssh/[keyfile].pub [username]@[master]'` on your computer, where `[keyfile].pub` is your public key, `[username]` is your username on the master, and `[master]` is the hostname or IP address of the master. At this point you might want to test that the passwordless login is working by running `ssh [username]@[master]` from your computer.

From the master, run the command `ssh-copy-id -i ~/.ssh/[keyfile].pub [username]@[worker]`, where `[keyfile].pub` is the public key generated for the master node, `[username]` is your username on a worker, and `[worker]` is the hostname or IP address of a worker. Do this for each worker. Test that it is working by running `ssh [username]@[worker]` from the master.

## Downloading binaries

Download pre-built Spark binaries: [http://spark.apache.org/downloads.html](http://spark.apache.org/downloads.html)

Download Java JRE binaries: [http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html#close](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html#close)

Extract the archives to a folder of your choice. The rest of this guide will assume that they have been extracted to your home folder. If they are not in your home folder, change the paths accordingly.

**Note**: The Spark root folder will be referred to as `[spark]` and the Java JRE root folder will be referred to as `[jre]`.

## Configuring Spark

Before we can begin using Spark we sill have to edit the configuration files.

Begin by copying the file `~/[spark]/conf/spark-env.sh.template` using the command `cp ~/[spark]/conf/spark-env.sh.template ~/[spark]/conf/spark-env.sh`. This will copy its contents to the new file `spark-env.sh` in the `~/[spark]/conf/` folder.

Open the newly created file `spark-env.sh` and add the following lines:

* `export JAVA_HOME="/home/[username]/[jre]"`
* `SPARK_MASTER_HOST="[master]"`

where `[username]` is your username for the master and the workers and `[master]` is the hostname or IP address of the master. You can also make other changes as appropriate. All Spark configuration options are described in the comments of the file `spark-env.sh.template` or the file `spark-env.sh` you just created.

Next create the file `slaves` in the `~/[spark]/conf/` by copying the template using the command `cp ~/[spark]/conf/slaves.template ~/[spark]/conf/slaves`. 

Open the newly created file `slaves` and add, for each worker, the line `[worker]` where `[worker]` is the hostname or IP address of that worker. You may also want to remove the line `localhost` so that a worker will not be started on your own computer.

## Copying to machines

With everything configured properly we need to copy all the files to the master and each worker. Do this by first running the commands `scp -r ~/[spark] ~/[jre] [username]@[master]:` where `[username]` is your username and `[master]` is the hostname or IP address of the master. Similarly, for each worker, run `scp -r ~/[spark] ~/[jre] [username]@[worker]:` where `[username]` is your username and `[worker]` is the hostname or IP address of the worker.

**Note**: If you're asked for your SSH login password during this then passwordless SSH login is not conigured properly.

## Starting and testing

After everything is copied it's time to make sure that everything works. To start everything at once run the script `start-all.sh` found in the `~/[spark]/sbin/` folder. If everything goes well, open a browser and go to `[master]:8080`. You should be able to see the Spark webUI with all workers connected. To launch a Spark shell against this cluster run `~/[spark]/bin/spark-shell --master spark://[master]:7077` where `[master]` is the hostname or IP address of the master. Once started you should be able to see the application in the Spark web-ui. You can also use `spark-submit` with appropriate options to submit jobs to the master. For example, `spark-submit --class org.apache.spark.examples.SparkPi --master spark://[master]:7077 --deploy-mode cluster examples/jars/spark-examples_2.11-2.1.1.jar 10` to run an example that computes an approximation of Pi.

## Other suggestions
