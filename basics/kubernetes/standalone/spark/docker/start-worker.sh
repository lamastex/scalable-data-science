#!/bin/sh

. /start-common.sh

/opt/spark/sbin/start-slave.sh spark://spark-master:7077
