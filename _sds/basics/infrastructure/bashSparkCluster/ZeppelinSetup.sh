#/bin/bash
source Variables.sh
#Converts hadoop-2.x.x... -> hadoop-common-2.x.x...
export HADOOP_COMMON=$(echo $HADOOP | sed 's/-/-common-/g')

tar -xvzf packages/$ZEPPELINTGZ

sed 's/8080/8090/g' $HOME/$ZEPPELIN'/conf/zeppelin-site.xml.template' > $HOME/$ZEPPELIN'/conf/zeppelin-site.xml'

echo 'export JAVA_HOME='$HOME/$JRE'
export HADOOP_HOME='$HOME/$HADOOP'
export SPARK_HOME='$HOME/$SPARK'
export MASTER="yarn-client"
export SPARK_SUBMIT_OPTIONS="--jars '$HOME/$HADOOP'/share/hadoop/common/'$HADOOP_COMMON'.jar"' >> $HOME/$ZEPPELIN'/conf/zeppelin-env.sh'