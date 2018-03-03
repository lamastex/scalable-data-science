# Setup of Spark-Yarn-HDFS cluster
I have prepared a set of scripts that can be used to setup a cluster given that we already have access to a cluster with Ubuntu running and the network is setup.

We begin by noting that the system this is created for is for user the same user on the master and all nodes, in our notes it is xadmin.

* Download, Spark with hadoop 2.7, hadoop-2.7.5, jre-1.8.0_u161 and zeppelin 0.73
* Verify the packages using gpg or MD5 or whatever, this should always be done to make sure we have uncorrupted master packages, or untampered by man-in-the-middle or just shifty network connections.
* Put these `tar gz` files inside the home folder on the master node inside a folder called packages.
* Edit Variables.sh to have the names of the targz files where applicable, and the name of the extraction folder. Fill in the hostnames of the slaves separated by `\n`.
* Run ssh-keygen on your laptop, followed by ssh-copy-id -i xadmin@master
* Upload all the .sh files in this repository to the master node by running,
	- scp *.sh xadmin@master:~
* On the master node run
	- chmod +x *.sh
* On the master node run setup_all.sh
* Check that everything is up and running by using you browser and surfing in to the master node at the following ports
	- Port: 8090 = Zeppelin
	- Port: 8088 = Yarn
	- Port: 50070 = HDFS
