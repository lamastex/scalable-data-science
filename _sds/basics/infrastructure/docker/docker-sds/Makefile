build:
	@docker build -t sds-spark:latest -f Dockerfile .

build-heavyweight:
	@docker build -t sds-spark:spark_2.1_hadoop_2.7 -f heavyweightSparkDockerfile .

build-lightweight:
	@docker build -t sds-spark:lightweight -f lightweightSparkDockerfile .

build-spark-zeppelin:
	@docker build -t sds-spark:zeppelin -f zeppelinSparkDockerfile .

build-spark-zeppelin-getty:
	@docker build -t sds-spark:zeppelin-getty -f zeppelinSparkFromGettyDockerfile .

# these need to be modified
run:
	@docker run \
	  -it \
	  --name sds-spark sds-spark /bin/bash

clean:
	@docker rm sds-spark > /dev/null || true

stop:
	@docker stop sds-spark

start:
	@docker start sds-spark
