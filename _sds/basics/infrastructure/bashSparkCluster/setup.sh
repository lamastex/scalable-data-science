#/bin/bash
source Variables.sh

tar -xvzf packages/$SPARKTGZ
tar -xvzf packages/$JRETGZ
tar -xvzf packages/$HADOOPTGZ
 
rm -rf /tmp/hadoop*

#Spark config for JRE and master hostname
cp ~/spark-2.2.1-bin-hadoop2.7/conf/spark-env.sh.template ~/spark-2.2.1-bin-hadoop2.7/conf/spark-env.sh
echo 'export JAVA_HOME="'$HOME/$JRE'"' >> ~/$SPARK/conf/spark-env.sh
echo 'SPARK_MASTER_HOST="'$MASTER'"' >> ~/$SPARK/conf/spark-env.sh

#Setting up slaves for spark, note that we keep localhost, i.e. the driver has a worker as well
cp ~/$SPARK/conf/slaves.template ~/$SPARK/conf/slaves
echo -e '\n'$SLAVE >> ~/$SPARK/conf/slaves

#Setting up Hadoop by fixing the JAVA_HOME
cp ~/$HADOOP/etc/hadoop/hadoop-env.sh ~/$HADOOP/etc/hadoop/hadoop-env.sh.template
sed 's/${JAVA_HOME}/\/home\/xadmin\/'$JRE'/g' ~/$HADOOP/etc/hadoop/hadoop-env.sh.template > ~/$HADOOP/etc/hadoop/hadoop-env.sh 
#Setting the tmpdirs and HDFS settings
echo '<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<!--
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License. See accompanying LICENSE file.
-->

<!-- Put site-specific property overrides in this file. -->

<configuration>
<property>
  <name>hadoop.tmp.dir</name>
  <value>/tmp/hadoop_tmp</value>
</property>

<property>
  <name>fs.defaultFS</name>
  <value>hdfs://'$MASTER':9000</value>
</property>
</configuration>' > ~/$HADOOP/etc/hadoop/core-site.xml
mkdir /tmp/hadoop_tmp
echo '<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<!--
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License. See accompanying LICENSE file.
-->

<!-- Put site-specific property overrides in this file. -->

<configuration>
<property>
  <name>dfs.replication</name>
  <value>'$NUMRED'</value>
</property>

<property>
  <name>dfs.namenode.rpc-bind-host</name>
  <value>0.0.0.0</value>
</property>

<property>
  <name>dfs.namenode.servicerpc-bind-host</name>
  <value>0.0.0.0</value>
</property>
</configuration>' > ~/$HADOOP/etc/hadoop/hdfs-site.xml
echo -e 'localhost\n'$SLAVE > ~/$HADOOP/etc/hadoop/slaves

#Set up Yarn to play along with the HDFS setup above

echo '<?xml version="1.0"?>
<!--
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License. See accompanying LICENSE file.
-->
<configuration>

<!-- Site specific YARN configuration properties -->
<property>
  <name>yarn.nodemanager.local-dirs</name>
  <value>/tmp/hadoop_tmp</value>
</property>

<property>
  <name>yarn.resourcemanager.hostname</name>
  <value>10.10.200.2</value>
</property>

<property>
  <name>yarn.resourcemanager.bind-host</name>
  <value>0.0.0.0</value>
</property>

<property>
  <name>yarn.nodemanager.host</name>
  <value>0.0.0.0</value>
</property>

<property>
  <name>yarn.resourcemanager.bind-host</name>
  <value>0.0.0.0</value>
</property>

<property>
  <name>yarn.nodemanager.local-dirs</name>
  <value>/tmp/hadoop_tmp</value>
</property>
<property>
  <name>yarn.nodemanager.vmem-pmem-ratio</name>
  <value>4</value>
  <description>Ratio between virtual memory to physical memory when setting memory limits for containers</description>
</property>
</configuration>' > ~/$HADOOP/etc/hadoop/yarn-site.xml
cp ~/$HADOOP/etc/hadoop/yarn-env.sh ~/$HADOOP/etc/hadoop/yarn-env.sh.template
echo 'export JAVA_HOME="'$HOME/$JRE'"' >> ~/$HADOOP/etc/hadoop/yarn-env.sh