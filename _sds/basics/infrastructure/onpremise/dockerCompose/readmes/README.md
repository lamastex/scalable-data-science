<!-- Copyright 2019 Dan Lilja

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. -->

# hs dockerimages and docker-compose configuration #

This is a guide to using the `lamastex/hs{base,zeppelin,jupyter,nifi,kafka}`
docker images and the `docker-compose.yml` configuration to start all the
services.

First each image is explained on its own. Then the `docker-compose` config is
explained with some minimal examples included. Lastly some important notes to be
aware of.

## Docker images ##

The docker images are called `lamastex/hsbase`, `lamastex/hszeppelin`,
`lamastex/hsjupyter` and `lamastex/hskafka`.

### hsbase ###

The image `lamastex/hsbase` contains Spark and Hadoop ready to start in
pseudo-distributed mode.

To start a container, use `docker run -p 50070:50070 -p 7070:7070 -p 8088:8088
-d lamastex/hsbase`. This will start hdfs, Yarn and fork the container to the
background. Note that this only works if you have mounted an already functioning
hdfs file system. If you're not mounting an already existing hdfs file system,
use `docker run -p 50070:50070 -p 7070:7070 -p 8088:8088 -d lamastex/hsbase
--format` which will also format the namenode and create a functioning, empty
hdfs.

The hdfs webUi will be available at `localhost:50070`, the Yarn webUI will be
available at `localhost:8088`.

### hszeppelin ###

The image `lamastex/hszeppelin` contains Zeppelin, Spark and Hadoop ready to
start in pseudo-distributed mode.

To start only Zeppelin, use `docker run -p 8080:8080 lamastex/hszeppelin`.

To start `hdfs` and Zeppelin, use `docker run -it -p 50070:50070 -p 7070:7070 -p
8088:8088 -p 8080:8080 lamastex/hszeppelin --hadoop`. Furthermore if you also
want to format the namenode, for example if starting fresh, also add `--format`.

In either case Zeppelin will be available at `localhost:8080` in your web
browser of choice.

### hsjupyter ###

The image `lamastex/hsjupyter` contains Jupyter with Spark kernels, Spark and
Hadoop ready to start in pseudo-distributed mode.

To start only Jupyter, use `docker run -p 8888:8888 lamastex/hsjupyter`.

To start `hdfs` and Jupyter, use `docker run -it -p 50070:50070 -p 7070:7070 -p
8088:8088 -p 8080:8080 lamastex/hsjupyter --hadoop`. Furthermore if you also
want to format the namenode, for example if starting fresh, also add `--format`.

In either case Jupyter will be available at `localhost:8888` in your web browser
of choice.

**Note**: There is a little trick to using `PySpark` in `hsjupyter` since there
is no `PySpark` kernel for Jupyter (as far as I can tell). To do this you must
start a `python3` notebook and run the following commands:

```
import findspark
findspark.init()
import pyspark
sc = pyspark.SparkContext()
```

Now you can use `PySpark` in the notebook and there is a Spark context available
as `sc`.

### hskafka ###

The image `lamastex/hskafka` is built on `lamastex/hsbase` and in addition
contains Kafka.

To start a Kafka server with default standalone configuration, use `docker run
-p 2181:2181 -p 9092:9092 -d lamastex/hskafka`.

To start a container with a different command, e.g. `bash`, use `docker run -p
2181:2181 -p 9092:9092 --entrypoint <command> lamastex/hskafka` where
`<command>` is the command you want to run. If you want to gain access to a
command line in the container use the command `bash` and add the options `-it`
before `lamastex/hskafka`.

If you want to get a terminal inside a running container, e.g. if you want to
run Kafka commands such as creating topics and console producer and consumer,
use `docker exec <container> bash ` where `<container>` is the ID of the running
container.

### Running on squidmaster ###

To run the docker containers on `squidmaster` you also need to forward the
corresponding ports when you `ssh` to `squidmaster`. The following command will
forward the ports for all the various webUIs used by the docker containers:

```
ssh -L 50070:localhost:50070 -L 8080:localhost:8080 -L 8088:localhost:8088 -L 7070:localhost:7070 -L 8888:localhost:8888 -L 2181:localhost:2181 -L 9092:localhost:9092 squidmaster
```

## docker-compose ##

The required file `docker-compose.yml` is located in the folder
`.../_sds/basics/infrastructure/onpremise/dockerCompose`. You can either `cd` to
this directory or run every command with `-f
.../_sds/basics/infrastructure/onpremise/dockerCompose/docker-compose.yml`. For
simplicity I will assume that your working directory is the above folder and
I'll refer to it as the *root folder*.

### Folder structure ###

There are currently 5 folders in the root folder.

- **data**: This is a folder for any data you want mounted into the containers
  started by `docker-compose`. It will automatically be mounted as read-only
  into the `/root` folder (which is also the working directory, the one that you
  automatically start in when opening a terminal) of any started container.
- **hadoopConfig**: Contains configuration files for hadoop. Is automatically
  mounted as read-only into each container so they all can communicate with
  hdfs.
- **sshConfig**: Contains ssh configuration. Is built in to each image. Allows
  the containers to speak to each other without requiring you to accept the host
  key manually each time.
- **readmes**: Contains this readme and some others.
- **zimport**: Contains the `zimport.py` Python script for automatically
  importing the output of Pinot into Zeppelin. See `dbToZp.md` for instructions
  on how to use it.

### Start and stop commands ###

To start everything use `docker-compose up`. This will print the output from all
the containers to your shell and attach to them. Using Ctrl-C will, like normal,
send the signal to stop the containers. If you do not want your shell to be
filled with output you can use `docker-compose up -d` to fork it to background.
With this command you can no longer use Ctrl-C to stop the containers. Ports
will automatically be published as defined in the `docker-compose.yml` file.

To stop and remove all containers use `docker-compose down`. To only stop but
not remove the containers, e.g. if you want to continue working later, use
`docker-compose stop`.

To start all stopped containers use `docker-compose start`.

If you want to attach to a running container you can use `docker-compose exec
<service> <command>`. This will attach to the service `<service>` and run the
command `<command>`. For example, if you want to access a shell inside a running
`jupyter` service you can use `docker-compose exec jupyter bash`. The defined
services are `hadoop`, `zeppelin`, `jupyter`, `nifi` and `kafka`.

Lastly, if you want to start a subset of the services you cna open the
`docker-compose.yml` file and comment out the services you don't need. When you
then run `docker-compose up` it won't start those services.

### Communication between containers ###

When using `docker-compose up` a network is automatically set up to communicate
between services. Furthermore DNS resolution is set up so that the service names
point to the correct container, for example you could use `ping zeppelin` if you
wanted to ping the container running the `zeppelin` service.

Note that firewalls can interfere with this communication. If you're getting
timeouts or the network is otherwise not working as it should, try turning off
all firewalls.

### SOU minimal example ###

This is a minimal example for doing a word count on the SOU data loaded into
HDFS from zeppelin on the `docker-compose` config.

1. Run `docker-compose up -d` to start services. You can comment out `jupyter`,
   `nifi` and `kafka` services for this example.
2. Run `docker-compose exec hadoop bash` to start a terminal in the `hadoop`
   container.
3. In the `hadoop` container run `hdfs dfs -ls /`. You should get back an empty
   directory.
4. In the `hadoop` container run `hdfs dfs -put data /`. This will boot the
   `data` folder which contains the SOU data in the root folder of HDFS.
5. In the `hadoop` container run `hdfs dfs -ls /data/sou`. You should see all
   the SOU text files.
6. In your browser go to `localhost:8080` to open the Zeppelin webUI and create
   a new notebook.
7. In the new notebook run the following:
   ```
   val sou = sc.textFile("hdfs:///data/sou/*")
   sou.take(5)
   ```
   to load all the SOU files into the rdd `sou` and then print the first five lines.
8. From here you can continue doing whatever analysis you like of the SOU data
   from Zeppelin.

### Spark Streaming with Kafka in spark-shell minimal example ###

The following is a "minimal" example of Spark Streaming using Kafka

1. Run `docker-compose up -d` to start services. You can comment out `zeppelin`,
   `jupyter` and `nifi` for this example.
2. Run `docker-compose exec kafka bash` to start a terminal in the `kafka`
   container.
3. Create a simple text file using `echo -e "hello\nworld" > test.txt`. Skip
   this step if you want to use a different text file.
4. Run `kafka-topic --create --zookeeper kafka:2181 --replication-factor 1
   --partitions 1 --topic test` to create a Kafka topic called `test`.
5. Run `kafka-console-producer --broker-list kafka:9092 --topic test < test.txt`
   to populate the topic with some data. Instead of `test.txt` you can use any
   other text file you can find in the container.
6. Exit the `kafka` container or open a new terminal and run `docker-compose
   exec hadoop bash` to open a terminal in the `hadoop` service.
7. In the `hadoop` service run `spark-shell` to start the Spark shell.
8. In the Spark shell run the following code to set up for streaming:
   ```
   import org.apache.spark.streaming._
   import org.apache.spark.streaming.kafka010._
   import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
   import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
   import org.apache.kafka.clients.consumer.ConsumerRecord
   import org.apache.kafka.common.serialization.StringDeserializer
   
   val ssc = new StreamingContext(sc, Seconds(5))
   
   val kafkaParams = Map[String, Object]("bootstrap.servers" -> "kafka:9092", "key.deserializer" -> classOf[StringDeserializer], "value.deserializer" -> classOf[StringDeserializer], "group.id" -> "stream_group_id", "auto.offset.reset" -> "earliest", "enable.auto.commit" -> (false: java.lang.Boolean))
   
  val topics = Array("test")
  val stream = KafkaUtils.createDirectStream[String, String](ssc, PreferConsistent, Subscribe[String, String](topics, kafkaParams))
  stream.map(record => (record.key, record.value)).print()
   ```
9. In the Spark shell run `ssc.start()` to start streaming data. You should
   first see the data you populated the Kafka topic with. If you add more data
   to the Kafka topic while streaming it will show up in the Spark shell.
   
The same setup could of course work in Zeppelin as well but the printing out to
console won't work so you would have to do something different, like saving to
files.

## Notes ##

Some important notes for each application included are:

- **Spark**: The Spark webUI is published to port 7070 to not collide with
  Zeppelin. The Spark worker webUI is not published by default.
- **hdfs**: By default the namenode will always be formatted when using
  `docker-compose up`. To change this go into `docker-compose.yml` and remove
  the line `command: --format` from the `hadoop` service.
- **Yarn**: By default only the port for the central resource manager webUI is
  published.
- **Zeppelin**: Nothing I can think of.
- **Jupyter**: The published port is 8889 instead of the standard 8888 but only
  because there's currently a running sagemath container using 8888. Since
  finding the token in the torrent of text being output to the shell when using
  `docker-compose up` I have set up the `docker-compose.yml` file so that it
  will define the password `lamastex` for the Jupyter notebook server. To change
  this password change the word `lamastex` in the line `command: --password
  lamastex` under the `jupyter` service. If you want the token instead of a
  password simply remove the entire line.
- **Nifi**: The port for the webUI is published to port 9090 to not collide with
  Zeppelin.
- **Kafka**: The default port for the built-in Zookeeper is 2181 and the default
  port for Kafka clients is 9092.
