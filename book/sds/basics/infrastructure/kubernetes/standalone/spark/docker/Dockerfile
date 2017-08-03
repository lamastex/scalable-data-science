FROM java:openjdk-8-jdk

ENV spark_ver 2.1.1
ENV hadoop_ver 2.7
# Get Spark from US Apache mirror.
RUN mkdir -p /opt && \
    cd /opt && \
    curl http://www.us.apache.org/dist/spark/spark-${spark_ver}/spark-${spark_ver}-bin-hadoop${hadoop_ver}.tgz | \
        tar -zx && \
    ln -s spark-${spark_ver}-bin-hadoop${hadoop_ver} spark && \
    echo Spark ${spark_ver} installed in /opt


ADD start-common.sh start-worker.sh start-master.sh /
RUN chmod +x /start-common.sh /start-master.sh /start-worker.sh
ENV PATH $PATH:/opt/spark/bin

