#/bin/bash
source Variables.sh

$HADOOP/sbin/start-dfs.sh
$HADOOP/sbin/start-yarn.sh