#/bin/bash
source Variables.sh

~/$HADOOP/bin/hdfs namenode -format

$HADOOP/sbin/start-dfs.sh

~/$HADOOP/bin/hdfs dfs -mkdir /spark
~/$HADOOP/bin/hdfs dfs -put $SPARK/jars /spark

cp ~/$SPARK/conf/spark-defaults.conf.template ~/$SPARK/conf/spark-defaults.conf
echo 'spark.yarn.archive hdfs:///spark/jars' >> ~/$SPARK/conf/spark-defaults.conf