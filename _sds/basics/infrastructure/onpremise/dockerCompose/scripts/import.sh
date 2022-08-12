
#! bin/bash

#-it
docker run --rm -v $PWD/dbc/scalable-data-science:/root/tilowiklund/pinot/dbc -v $PWD/zp:/root/tilowiklund/pinot/zp lamastex/haskell-pinot /bin/bash pinot.sh

#docker exec -i pinot bash < pinot.sh
#docker kill pinot
docker run -d -it -u $(id -u) -p 8080:8080 -p 4040:4040 --rm -v $PWD/data:/datasets/sds -v $PWD/logs:/logs -v $PWD/notebook:/notebook  -e ZEPPELIN_LOG_DIR='/logs' -e ZEPPELIN_NOTEBOOK_DIR='/notebook' --name zeppelin lamastex/zeppelin-spark
#mkdir -r notebook
cd zp
python3 ../zimport.py 000_1-sds-3-x-spark
python3 ../zimport.py 000_1-sds-3-x-sql
python3 ../zimport.py 000_2-sds-3-x-ml


#docker run -d -it -u $(id -u) -p 8080:8080 -p 4040:4040 --rm -v $PWD/zeppelinConf:/opt/zeppelin/conf -v $PWD/data:/datasets/sds -v $PWD/logs:/logs -v $PWD/notebook:/notebook  -e ZEPPELIN_LOG_DIR='/logs' -e ZEPPELIN_NOTEBOOK_DIR='/notebook' --name zeppelin lamastex/zeppelin-spark


