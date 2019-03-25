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

This is a guide to using the lamastex/hs{base,zeppelin,jupyter,nifi,kafka}
docker images and the `docker-compose.yml` configuration to start all the
services.

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
simplicity I will assume that your working directory is the above folder.

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

If you only want to start a single container you can use `docker-compose run
<service>` where `<service>` is the name of one of the services defined in the
`docker-compose.yml` file you're using. For now the defined services are
`hadoop` for hdfs and Yarn, `zeppelin` for Zeppelin, `jupyter` for Jupyter,
`nifi` for Nifi and `kafka` for Kafka. It works like the normal `docker run`
command in that you have to use `-p` to publish ports except that you don't need
to use `-it` to access the shell since it will automatically attach input and
output. Instead you have to use `-T` if you **don't** want it to attach input and
output.

Lastly if you want to attach to a running container you can use `docker-compose
exec <service> <command>`. This will attach to the service `<service>` and run
the command `<command>`. For example, if you want to access a shell inside a
running `jupyter` service you can use `docker-compose exec jupyter bash`.

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
