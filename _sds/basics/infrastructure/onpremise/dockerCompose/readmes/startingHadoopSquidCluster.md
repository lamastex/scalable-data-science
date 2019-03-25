# Running Hadoop clusters on squidcluster

First you need to forward the correct ports when you ssh into `squidmaster`. To
do this use the command

```
ssh -L 50070:localhost:50070 -L 8080:localhost:8080 -L 9999:localhost:9999  squidmaster
```

This will allow you to access the hdfs webUI on port `50070`, the Yarn webUI on
port `9999` and the Spark webUI on port `8080`.

Hadoop and Spark folders are located in `~/sparkie`.

To start hdfs run `start-dfs.sh` in `~/sparkie/hadoop-2.8.0/sbin`.

You may need to reformat the `dfs` before the namenode will start. If so run
`~/sparkie/hadoop-2.8.0/bin/hdfs namenode -format`.

To start Yarn run `start-yarn.sh` in `~/sparkie/hadoop-2.8.0/sbin`.

Now you can use `spark-submit` with `--master yarn` to submit Spark jobs to the
Yarn manager.

When you're finished run `stop-yarn.sh` and `stop-dfs.sh` to shut down the cluster. 

## Simple example

Here is a simple step-by-step example to submit a Spark job to Yarn starting
from your own computer.

```
ssh -L 50070:localhost:50070 -L 8080:localhost:8080 -L 9999:localhost:9999  squidmaster
~/sparkie/hadoop-2.8.0/bin/hdfs namenode -format
~/sparkie/hadoop-2.8.0/sbin/start-dfs.sh
~/sparkie/hadoop-2.8.0/sbin/start-yarn.sh
spark-2.2.0-bin-hadoop2.7/bin/spark-submit --master yarn --class org.apache.spark.examples.SparkPi --deploy-mode client spark-2.2.0-bin-hadoop2.7/examples/jars/spark-examples_2.11-2.2.0.jar 1000
hadoop-2.8.0/sbin/stop-yarn.sh
hadoop-2.8.0/sbin/stop-dfs.sh
```

This runs the driver in your shell, showing you all the output. You can also run
the job in cluster mode where some machine in the cluster will instead act as
the driver. To do this use `--deploy-mode cluster` instead of `--deploy-mode
client`. The output will then be saved to the the Hadoop logs under
`hadoop-2.8.0/logs/userlogs`. ```
