First copy the contents of the [dockerCompose](https://github.com/lamastex/scalable-data-science/tree/master/_sds/basics/infrastructure/onpremise/dockerCompose) directory (best to unzip from the latest as it becomess ready!):

Say you have a directory called `sds` inside your home directory called `user` and have copied `dockerCompose` into it.

Then run these commands or their equivalents to make sure docker is pulling down all the images as needed and test our Spark-shel,, etc.

$ pwd
/.../home/username/sds/dockerCompose
:dockerCompose $ ls
data				hadoopConfig			zimport
docker-compose-hszeppelin.yml	readmes
docker-compose.yml		sshConfig


:dockerCompose $ docker-compose up -d
Creating network "dockercompose_default" with the default driver
Creating dockercompose_jupyter_1  ... done
Creating dockercompose_hadoop_1   ... done
Creating dockercompose_kafka_1    ... done
Creating dockercompose_nifi_1     ... done
Creating dockercompose_zeppelin_1 ... done

Andreas-MacBook-Pro:dockerCompose $ docker-compose exec hadoop bash
root@c004f9cc093d:~# ls
data  hadoop-2.9.2  spark-2.3.0-bin-hadoop2.7  start.sh


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

scala> sc.setLogLevel("ERROR")

scala> val myrdd = sc.parallelize(Seq(1,2,3))
myrdd: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[0] at parallelize at <console>:24

scala> myrdd.co
coalesce   collect   collectAsync   compute   context   copy   count   countApprox   countApproxDistinct   countAsync   countByValue   countByValueApprox

scala> myrdd.collect
collect   collectAsync

scala> myrdd.collect()
res1: Array[Int] = Array(1, 2, 3)

scala> val sourdd = sc.textFile("file:///root/data/sou/*")
sourdd: org.apache.spark.rdd.RDD[String] = file:///root/data/sou/* MapPartitionsRDD[4] at textFile at <console>:24

scala> sourdd.count()
res3: Long = 21881  

scala> :quit

# 3. spark-submit in cluster deploy-mode

## run the job from jar using spark-submit
root@30265c1156ef:~# spark-submit --class org.apache.spark.examples.SparkPi --master yarn --deploy-mode cluster spark-2.3.0-bin-hadoop2.7/examples/jars/spark-examples_2.11-2.3.0.jar

## Here are the spark master and two worker nodes assigned by yarn for the spark-submit job:
 
root@30265c1156ef:~# ls hadoop-2.9.2/logs/userlogs/application_1553892810556_0002/container* 
container_1553892810556_0002_01_000001/ container_1553892810556_0002_01_000002/ container_1553892810556_0002_01_000003/

root@30265c1156ef:~# ls hadoop-2.9.2/logs/userlogs/application_1553892810556_0002/container_1553892810556_0002_01_000001/
prelaunch.err  prelaunch.out  stderr         stdout 

root@30265c1156ef:~# cat hadoop-2.9.2/logs/userlogs/application_1553892810556_0002/container_1553892810556_0002_01_000001/stdout | grep "Pi is "
Pi is roughly 3.1368956844784224

Ctrl-D to exit the hadoop service.

$ docker-compose down


# zeppelin notebook postsings

1. docker-compose up -d
2. When zeppelin is running do:
3.1
:zimport $ pwd
/Users//all/git/scalable-data-science/_sds/basics/infrastructure/onpremise/dockerCompose/zimport
3.2
:zimport $ python3 zimport.py --host localhost --port 8080 test/
3.3
If you want to make changes from zeppelin then test/ should be mounted as a volume in yaml and the notebook exported back.
