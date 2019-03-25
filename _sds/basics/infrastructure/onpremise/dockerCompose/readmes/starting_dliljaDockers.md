# Starting hsDockers

## hsbase

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

## hszeppelin

The image `lamastex/hszeppelin` contains Zeppelin, Spark and Hadoop ready to
start in pseudo-distributed mode.

To start only Zeppelin, use `docker run -p 8080:8080 lamastex/hszeppelin`.

To start `hdfs` and Zeppelin, use `docker run -it -p 50070:50070 -p 7070:7070 -p
8088:8088 -p 8080:8080 lamastex/hszeppelin --hadoop`. Furthermore if you also
want to format the namenode, for example if starting fresh, also add `--format`.

In either case Zeppelin will be available at `localhost:8080` in your web
browser of choice.

## hsjupyter

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

## Running on squidmaster

To run the docker containers on `squidmaster` you also need to forward the
corresponding ports when you `ssh` to `squidmaster`. The following command will
forward the ports for all the various webUIs used by the docker containers:

```
ssh -L 50070:localhost:50070 -L 8080:localhost:8080 -L 8088:localhost:8088 -L 7070:localhost:7070 -L 8888:localhost:8888 squidmaster
```
