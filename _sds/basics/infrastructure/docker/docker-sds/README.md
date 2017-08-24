# How to use docker images

To launch spark and zeppelin do:
```%sh
docker run -d -p 8080:8080 -p 7077:7077 -p 4040:4040 raazesh/sds
```
Then open a browser at `http://localhost:8080/`

---
---

This image was built as follows:
```%sh
make build
docker images
docker tag sds-spark:latest raazesh/sds:latest
docker images
docker push raazesh/sds:latest
```

# Docker images built by raazesh

Ideally this can be used to cook-up docker images from scratch for the sds course.


## sds-spark:zeppelin

To launch spark and zeppelin do:
```%sh
$ docker run -d -p 8080:8080 -p 7077:7077 -p 4040:4040 raazesh/sds-spark:zeppelin
```



## sds-spark
This is heavy-weight image over 2G in size

### for spark-shell

```%sh
$ docker run -it raazesh/sds-spark /spark/bin/spark-shell
Using Spark's default log4j profile: org/apache/spark/log4j-defaults.properties
Setting default log level to "WARN".
To adjust logging level use sc.setLogLevel(newLevel). For SparkR, use setLogLevel(newLevel).
17/08/24 16:28:59 WARN NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
17/08/24 16:29:04 WARN ObjectStore: Version information not found in metastore. hive.metastore.schema.verification is not enabled so recording the schema version 1.2.0
17/08/24 16:29:04 WARN ObjectStore: Failed to get database default, returning NoSuchObjectException
17/08/24 16:29:05 WARN ObjectStore: Failed to get database global_temp, returning NoSuchObjectException
Spark context Web UI available at http://172.17.0.2:4040
Spark context available as 'sc' (master = local[*], app id = local-1503592140184).
Spark session available as 'spark'.
Welcome to
      ____              __
     / __/__  ___ _____/ /__
    _\ \/ _ \/ _ `/ __/  '_/
   /___/ .__/\_,_/_/ /_/\_\   version 2.2.0
      /_/
         
Using Scala version 2.11.8 (Java HotSpot(TM) 64-Bit Server VM, Java 1.8.0_144)
Type in expressions to have them evaluated.
Type :help for more information.

scala> val output = sc.parallelize(Seq(1 to 5)).collect()
output: Array[scala.collection.immutable.Range.Inclusive] = Array(Range(1, 2, 3, 4, 5))

scala> :quit
```

### for pyspark 
```%sh
$ docker run -it raazesh/sds-spark /spark/bin/pyspark
Python 2.7.12 (default, Nov 19 2016, 06:48:10) 
[GCC 5.4.0 20160609] on linux2
Type "help", "copyright", "credits" or "license" for more information.
Using Spark's default log4j profile: org/apache/spark/log4j-defaults.properties
Setting default log level to "WARN".
To adjust logging level use sc.setLogLevel(newLevel). For SparkR, use setLogLevel(newLevel).
17/08/24 16:26:47 WARN NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
17/08/24 16:26:53 WARN ObjectStore: Version information not found in metastore. hive.metastore.schema.verification is not enabled so recording the schema version 1.2.0
17/08/24 16:26:53 WARN ObjectStore: Failed to get database default, returning NoSuchObjectException
17/08/24 16:26:54 WARN ObjectStore: Failed to get database global_temp, returning NoSuchObjectException
Welcome to
      ____              __
     / __/__  ___ _____/ /__
    _\ \/ _ \/ _ `/ __/  '_/
   /__ / .__/\_,_/_/ /_/\_\   version 2.2.0
      /_/

Using Python version 2.7.12 (default, Nov 19 2016 06:48:10)
SparkSession available as 'spark'.

>>> output = sc.parallelize([1,2,3,4,5]).collect()

>>> output
[1, 2, 3, 4, 5]

>>> exit()
```

### for sparkR
```%sh
$ docker run -it raazesh/sds-spark /spark/bin/sparkR

R version 3.4.1 (2017-06-30) -- "Single Candle"
Copyright (C) 2017 The R Foundation for Statistical Computing
Platform: x86_64-pc-linux-gnu (64-bit)

R is free software and comes with ABSOLUTELY NO WARRANTY.
You are welcome to redistribute it under certain conditions.
Type 'license()' or 'licence()' for distribution details.

R is a collaborative project with many contributors.
Type 'contributors()' for more information and
'citation()' on how to cite R or R packages in publications.

Type 'demo()' for some demos, 'help()' for on-line help, or
'help.start()' for an HTML browser interface to help.
Type 'q()' to quit R.

Launching java with spark-submit command /spark/bin/spark-submit   "sparkr-shell" /tmp/RtmpyE900x/backend_port17553d55a 
Using Spark's default log4j profile: org/apache/spark/log4j-defaults.properties
Setting default log level to "WARN".
To adjust logging level use sc.setLogLevel(newLevel). For SparkR, use setLogLevel(newLevel).
17/08/24 16:41:30 WARN NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
17/08/24 16:41:36 WARN ObjectStore: Version information not found in metastore. hive.metastore.schema.verification is not enabled so recording the schema version 1.2.0
17/08/24 16:41:36 WARN ObjectStore: Failed to get database default, returning NoSuchObjectException
17/08/24 16:41:36 WARN ObjectStore: Failed to get database global_temp, returning NoSuchObjectException

 Welcome to
    ____              __ 
   / __/__  ___ _____/ /__ 
  _\ \/ _ \/ _ `/ __/  '_/ 
 /___/ .__/\_,_/_/ /_/\_\   version  2.2.0 
    /_/ 


 SparkSession available as 'spark'.
> df <- as.DataFrame(faithful)
> head(df)
  eruptions waiting
1     3.600      79
2     1.800      54
3     3.333      74
4     2.283      62
5     4.533      85
6     2.883      55
> quit()
Save workspace image? [y/n/c]: n
```

### using spark UI
To be able to use spark UI, add " -p 4040:4040 " argument:
```%sh
docker run -ti -p 4040:4040 raazesh/sds-spark /spark/bin/spark-shell
```

### running a python script
To run a python script do:
```%sh
echo "import pyspark\nprint(pyspark.SparkContext().parallelize(range(0, 5)).count())" > count.py
docker run -it -p 4040:4040 -v $(pwd)/count.py:/count.py raazesh/sds-spark /spark/bin/spark-submit /count.py
```

### Hadoop
With this image you can connect to Hadoop cluster from spark by specifying `HADOOP_CONF_DIR` and passing  the directory with hadoop configs as volume

```%sh
docker run -v $(pwd)/hadoop:/etc/hadoop/conf -e "HADOOP_CONF_DIR=/etc/hadoop/conf" --net=host  -it raazesh/sds-spark /spark/bin/spark-shell --master yarn-client
```

## sds-spark:lightweight

For spark-shell light-weight version do:
```%sh
$ docker run -it raazesh/sds-spark:lightweight

Using Spark's default log4j profile: org/apache/spark/log4j-defaults.properties
Setting default log level to "WARN".
To adjust logging level use sc.setLogLevel(newLevel). For SparkR, use setLogLevel(newLevel).
17/08/24 15:17:49 WARN NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
17/08/24 15:17:53 WARN General: Plugin (Bundle) "org.datanucleus.api.jdo" is already registered. Ensure you dont have multiple JAR versions of the same plugin in the classpath. The URL "file:/spark-2.2.0-bin-hadoop2.7/jars/datanucleus-api-jdo-3.2.6.jar" is already registered, and you are trying to register an identical plugin located at URL "file:/spark/jars/datanucleus-api-jdo-3.2.6.jar."
17/08/24 15:17:53 WARN General: Plugin (Bundle) "org.datanucleus" is already registered. Ensure you dont have multiple JAR versions of the same plugin in the classpath. The URL "file:/spark-2.2.0-bin-hadoop2.7/jars/datanucleus-core-3.2.10.jar" is already registered, and you are trying to register an identical plugin located at URL "file:/spark/jars/datanucleus-core-3.2.10.jar."
17/08/24 15:17:53 WARN General: Plugin (Bundle) "org.datanucleus.store.rdbms" is already registered. Ensure you dont have multiple JAR versions of the same plugin in the classpath. The URL "file:/spark-2.2.0-bin-hadoop2.7/jars/datanucleus-rdbms-3.2.9.jar" is already registered, and you are trying to register an identical plugin located at URL "file:/spark/jars/datanucleus-rdbms-3.2.9.jar."
17/08/24 15:17:58 WARN ObjectStore: Version information not found in metastore. hive.metastore.schema.verification is not enabled so recording the schema version 1.2.0
17/08/24 15:17:58 WARN ObjectStore: Failed to get database default, returning NoSuchObjectException
17/08/24 15:17:59 WARN ObjectStore: Failed to get database global_temp, returning NoSuchObjectException
Spark context Web UI available at http://172.17.0.3:4040
Spark context available as 'sc' (master = local[*], app id = local-1503587870643).
Spark session available as 'spark'.
Welcome to
      ____              __
     / __/__  ___ _____/ /__
    _\ \/ _ \/ _ `/ __/  '_/
   /___/ .__/\_,_/_/ /_/\_\   version 2.2.0
      /_/
         
Using Scala version 2.11.8 (OpenJDK 64-Bit Server VM, Java 1.8.0_131)
Type in expressions to have them evaluated.
Type :help for more information.

scala> sc.parallelize(Seq(1 to 10)).collect
res0: Array[scala.collection.immutable.Range.Inclusive] = Array(Range(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))

scala> :quit
```

You can also launch bash and then call spark-shell. This can be useful if you want to do other things inside the container.
Remember to Press `Ctrl D` to logout of bash and exit docker container.
```%sh
$ docker run -it raazesh/sds-spark:lightweight /bin/bash
bash-4.3# 
bash-4.3# ./spark/bin/spark-shell 
...
...
...
Welcome to
      ____              __
     / __/__  ___ _____/ /__
    _\ \/ _ \/ _ `/ __/  '_/
   /___/ .__/\_,_/_/ /_/\_\   version 2.2.0
      /_/
         
Using Scala version 2.11.8 (OpenJDK 64-Bit Server VM, Java 1.8.0_131)
Type in expressions to have them evaluated.
Type :help for more information.

scala> :quit

bash-4.3#  
```

# How to build, tag and push
This is only for developers 

## How to build tag and push `raazesh/sds-spark:lightweight` to dockerhub
To build, tag and push the heavy-weight Apache Spark from git directly:
```%sh
$ make build-lightweight
$ docker tag sds-lightweight-spark:latest raazesh/sds-spark:lightweight
$ docker images
$ docker push raazesh/sds-spark:lightweight

```


## How to build tag and push `raazesh/sds-spark:latest` to dockerhub
To build, tag and push the heavy-weight Apache Spark by building from git:
```%sh
$ make build-heavyweight # but with spark version 2.2
$ docker tag sds-spark raazesh/sds-spark
$ docker push raazesh/sds-spark
```

## How to build tag and push `raazesh/sds-spark:spark_2.1_hadoop_2.7` to dockerhub
Zeppelin needs these versions. 
```%sh
$ make build-heavyweight
$ docker tag sds-spark:spark_2.1_hadoop_2.7 raazesh/sds-spark:spark_2.1_hadoop_2.7
$ docker push raazesh/sds-spark:spark_2.1_hadoop_2.7
```

## How to build tag and push `raazesh/sds-spark:zeppelin:` to dockerhub
```%sh
$ make build-spark-zeppelin
$ docker tag sds-spark:zeppelin raazesh/sds-spark:zeppelin
$ docker push raazesh/sds-spark:zeppelin
```
