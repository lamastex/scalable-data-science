# Quick Start

This is the `dockerCompose/readmes/startingNotes.md` file.

It is meant to quickly get you going. Details are in `readmes/README.md` file.

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

Now run `docker-compose` like this in a open network so all the images are downloaded from dockerhub:

```
:dockerCompose $ docker-compose up -d
Creating network "dockercompose_default" with the default driver
Creating dockercompose_jupyter_1  ... done
Creating dockercompose_hadoop_1   ... done
Creating dockercompose_kafka_1    ... done
Creating dockercompose_nifi_1     ... done
Creating dockercompose_zeppelin_1 ... done
```

## 3. Running `spark-shell`

Next, test if `spark-shell` works. We will first `exec`ute `bash` inside the core docker container called `hadoop` (it has hdfs, yarn and spark), our core workhorse. 

```
:dockerCompose $ docker-compose exec hadoop bash
root@c004f9cc093d:~# ls
data  hadoop-2.9.2  spark-2.3.0-bin-hadoop2.7  start.sh
```

Now you are root inside the `hadoop` container.

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

## 4. spark-submit in cluster deploy-mode

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

## 6. zeppelin notebook postings

1. docker-compose up -d
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
