

cd /root/tilowiklund/pinot
stack exec pinot -- --from databricks --to zeppelin /root/tmp/dbc/000_1-sds-3-x-spark.dbc -o /root/tmp/zp #get env variables for these two guys if we want
stack exec pinot -- --from databricks --to zeppelin /root/tmp/dbc/000_1-sds-3-x-sql.dbc -o /root/tmp/zp
stack exec pinot -- --from databricks --to zeppelin /root/tmp/dbc/000_2-sds-3-x-ml.dbc -o /root/tmp/zp




