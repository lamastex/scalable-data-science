There are two exercises here to get you started on building packaged jars for submission to a cluster via `spark-submit`.

They are:

- Exercise 1: SparkPi
- Exercise 2: souWordCount

# Exercise 1: SparkPi

Here are the steps taken to `compile` and `spark-submit` to the yarn-managed cluster with hdfs and spark running as the hadoop service in our docker-compose.

First make sure `docker-compose` launched hadoop service is still running:

```
$ docker ps
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                                                                                                                                          NAMES
4bf966fb974f        0aef5f549c7f        "/root/start.sh --fo…"   11 hours ago        Up 11 hours         0.0.0.0:4040->4040/tcp, 7070/tcp, 0.0.0.0:8042->8042/tcp, 0.0.0.0:8088->8088/tcp, 0.0.0.0:50070->50070/tcp, 8081/tcp, 0.0.0.0:7070->8080/tcp   dockercompose_hadoop_1
```

There is a hadoop service with name `dockercompose_hadoop_1` already running with container id `4bf966fb974f` (if you do not see a similar output, then you need to be in `dockerCompose` directory and type `docker-compose -f docker-compose-hadoop.yml up -d` as explained in `readmes/gettingStarted.md`).
 
Once hadoop service is availbale we just need to connect to it via `bash` as follows from a new terminal and changing directory to `dockerCompose`:

```
$ pwd
/path_to/dockerCompose
:dockerCompose $ docker-compose exec hadoop bash
root@4bf966fb974f:~# ls
data  hadoop-2.9.2  programs  spark-2.3.0-bin-hadoop2.7  start.sh
```

Once inside the container, `cd` to the right directory to `compile` and `package` using `sbt`. Note this can also be done outside the container if you have sbt installed on your system.

``` 
root@4bf966fb974f:~# cd programs/exercises/sparkSubmit
root@4bf966fb974f:~/programs/exercises/sparkSubmit# ls 
README.md  build.sbt  project  src  target
root@4bf966fb974f:~/programs/exercises/sparkSubmit# sbt
[info] Loading project definition from /root/programs/exercises/sparkSubmit/project
[info] Updating ProjectRef(uri("file:/root/programs/exercises/sparkSubmit/project/"), "spark-build")...
[info] Done updating.
[info] Loading settings for project spark from build.sbt ...
[info] Set current project to spark (in build file:/root/programs/exercises/sparkSubmit/)
[info] sbt server started at local:///root/.sbt/1.0/server/16c759866da28222faaf/sock
sbt:spark> clean
[success] Total time: 1 s, completed Apr 7, 2019 7:21:56 PM
sbt:spark> compile
[info] Updating ...
[info] downloading https://repo1.maven.org/maven2/org/apache/avro/avro/1.7.7/avro-1.7.7.jar ...
[info] 	[SUCCESSFUL ] org.apache.avro#avro;1.7.7!avro.jar (193ms)
[info] Done updating.
[warn] There may be incompatibilities among your library dependencies; run 'evicted' to see detailed eviction warnings.
[info] Compiling 2 Scala sources to /root/programs/exercises/sparkSubmit/target/scala-2.11/classes ...
[info] Done compiling.
[success] Total time: 18 s, completed Apr 7, 2019 7:22:18 PM
sbt:spark> package
[warn] Multiple main classes detected.  Run 'show discoveredMainClasses' to see the list
[info] Packaging /root/programs/exercises/sparkSubmit/target/scala-2.11/spark_2.11-0.1.0-SNAPSHOT.jar ...
[info] Done packaging.
[success] Total time: 1 s, completed Apr 7, 2019 7:22:48 PM
sbt:spark> 
```

Now we can submit the packaged jar to our cluster using `spark-submit` as follows:

```
root@4bf966fb974f:~/programs/exercises/sparkSubmit# spark-submit --class org.lamastex.exercises.SparkPi --master yarn --deploy-mode cluster target/scala-2.11/spark_2.11-0.1.0-SNAPSHOT.jar 100
```

To see the output of the submitted job look for the `logs/userlogs` in `/root/hadoop-2.9.2/` under the latest application number 'X' in `application_1554656571706_000X`, as `X` will increase with the applications submitted to the yarn-managed hdfs-spark cluster running as our hadoop service via docker-compose:

```
root@4bf966fb974f:~/programs/exercises/sparkSubmit# cat /root/hadoop-2.9.2/logs/userlogs/application_1554656571706_0001/container_1554656571706_0001_01_000001/stdout | grep Pi
Pi is roughly 3.1413746
```

You can also see the `logs/userlogs` for the desired application that was submitted  via yarn's userlogs webUI:

- http://localhost:8042/logs/userlogs/

# Exercise 2: souWordCount

Here you need to do a little more work. 

- 2.1. load data into hdfs, our distributed file system, from the local mounted file system at `dockerCompose/data/sou`
- 2.2. use `sbt` to compile and package to produce the `souWordCount` App located in the path below and `spark-submit` it to confirm that reading from hdfs works as expected
  - `root@4bf966fb974f:~/programs/exercises/sparkSubmit/target/scala-2.11/spark_2.11-0.1.0-SNAPSHOT.jar` 
- 2.3. by recalling the end of `006_WordCount` notebook of SOU addresses, add additional code to `souWordCount.scala` file at `root@4bf966fb974f:~/programs/exercises/sparkSubmit/src/main/scala/examples/souWordCount.scala` *in order to count the number of each word across all the `sou/*.txt` files and output the result as an Array of (word,count) tuples from the most frequent to the least frequent word.* 
- 2.4 finally, using sbt `compile`, `package` and `spark-submit` this as an application to the cluster.

## 2.1. load data into `hdfs`

```
root@4bf966fb974f:~/programs/exercises/sparkSubmit/src/main/scala/examples# hdfs dfs -ls
Found 1 items
drwxr-xr-x   - root supergroup          0 2019-04-07 18:48 .sparkStaging
```

### Hadoop filesystem commands:

Here are the basic `hadoop` commands. We will only use a couple of them in the sequel.

```
Usage: hadoop fs [generic options]
	[-appendToFile <localsrc> ... <dst>]
	[-cat [-ignoreCrc] <src> ...]
	[-checksum <src> ...]
	[-chgrp [-R] GROUP PATH...]
	[-chmod [-R] <MODE[,MODE]... | OCTALMODE> PATH...]
	[-chown [-R] [OWNER][:[GROUP]] PATH...]
	[-copyFromLocal [-f] [-p] [-l] [-d] <localsrc> ... <dst>]
	[-copyToLocal [-f] [-p] [-ignoreCrc] [-crc] <src> ... <localdst>]
	[-count [-q] [-h] [-v] [-t [<storage type>]] [-u] [-x] <path> ...]
	[-cp [-f] [-p | -p[topax]] [-d] <src> ... <dst>]
	[-createSnapshot <snapshotDir> [<snapshotName>]]
	[-deleteSnapshot <snapshotDir> <snapshotName>]
	[-df [-h] [<path> ...]]
	[-du [-s] [-h] [-x] <path> ...]
	[-expunge]
	[-find <path> ... <expression> ...]
	[-get [-f] [-p] [-ignoreCrc] [-crc] <src> ... <localdst>]
	[-getfacl [-R] <path>]
	[-getfattr [-R] {-n name | -d} [-e en] <path>]
	[-getmerge [-nl] [-skip-empty-file] <src> <localdst>]
	[-help [cmd ...]]
	[-ls [-C] [-d] [-h] [-q] [-R] [-t] [-S] [-r] [-u] [<path> ...]]
	[-mkdir [-p] <path> ...]
	[-moveFromLocal <localsrc> ... <dst>]
	[-moveToLocal <src> <localdst>]
	[-mv <src> ... <dst>]
	[-put [-f] [-p] [-l] [-d] <localsrc> ... <dst>]
	[-renameSnapshot <snapshotDir> <oldName> <newName>]
	[-rm [-f] [-r|-R] [-skipTrash] [-safely] <src> ...]
	[-rmdir [--ignore-fail-on-non-empty] <dir> ...]
	[-setfacl [-R] [{-b|-k} {-m|-x <acl_spec>} <path>]|[--set <acl_spec> <path>]]
	[-setfattr {-n name [-v value] | -x name} <path>]
	[-setrep [-R] [-w] <rep> <path> ...]
	[-stat [format] <path> ...]
	[-tail [-f] <file>]
	[-test -[defsz] <path>]
	[-text [-ignoreCrc] <src> ...]
	[-touchz <path> ...]
	[-truncate [-w] <length> <path> ...]
	[-usage [cmd ...]]

Generic options supported are:
-conf <configuration file>        specify an application configuration file
-D <property=value>               define a value for a given property
-fs <file:///|hdfs://namenode:port> specify default filesystem URL to use, overrides 'fs.defaultFS' property from configurations.
-jt <local|resourcemanager:port>  specify a ResourceManager
-files <file1,...>                specify a comma-separated list of files to be copied to the map reduce cluster
-libjars <jar1,...>               specify a comma-separated list of jar files to be included in the classpath
-archives <archive1,...>          specify a comma-separated list of archives to be unarchived on the compute machines

The general command line syntax is:
command [genericOptions] [commandOptions]
```

### Make hdfs directory and copy sou files from local file system into hdfs.

```
root@4bf966fb974f:~/programs/exercises/sparkSubmit/src/main/scala/examples# hdfs dfs -mkdir -p /datasets/sou
root@4bf966fb974f:~/programs/exercises/sparkSubmit/src/main/scala/examples# hdfs dfs -ls /datasets          
Found 1 items
drwxr-xr-x   - root supergroup          0 2019-04-07 19:05 /datasets/sou

root@4bf966fb974f:~/programs/exercises/sparkSubmit/src/main/scala/examples# ls /root/data/sou/
17900108.txt  18071027.txt  18251206.txt  18431206.txt  18611203.txt  18791201.txt ... 
17901208.txt  18081108.txt  18261205.txt  18441203.txt  18621201.txt  18801206.txt ...
17911025.txt  18091129.txt  18271204.txt  18451202.txt  18631208.txt  18811206.txt ...
...
18051203.txt  18231202.txt  18411207.txt  18591219.txt  18771203.txt  18951207.txt ...
18061202.txt  18241207.txt  18421206.txt  18601203.txt  18781202.txt  18961204.txt ...

root@4bf966fb974f:~/programs/exercises/sparkSubmit/src/main/scala/examples# hdfs dfs -put /root/data/sou/* /datasets/sou/
root@4bf966fb974f:~/programs/exercises/sparkSubmit/src/main/scala/examples# hdfs dfs -ls /datasets/sou
Found 230 items
-rw-r--r--   1 root supergroup       6725 2019-04-07 19:07 /datasets/sou/17900108.txt
-rw-r--r--   1 root supergroup       8427 2019-04-07 19:07 /datasets/sou/17901208.txt
...
-rw-r--r--   1 root supergroup      38528 2019-04-07 19:07 /datasets/sou/20150120.txt
-rw-r--r--   1 root supergroup      31083 2019-04-07 19:07 /datasets/sou/20160112.txt
```

The remaining steps 2.2-2.4 should be similar to the first exercise.

