#!/bin/sh

. /start-common.sh

echo "$(hostname -i) spark-master" >> /etc/hosts

/opt/spark/sbin/start-master.sh --ip spark-master --port 7077
