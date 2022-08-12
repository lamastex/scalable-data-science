

cd tilowiklund/pinot
stack exec pinot -- --from databricks --to zeppelin dbc/000_1-sds-3-x-spark.dbc -o zp
stack exec pinot -- --from databricks --to zeppelin dbc/000_1-sds-3-x-sql.dbc -o zp
stack exec pinot -- --from databricks --to zeppelin dbc/000_2-sds-3-x-ml.dbc -o zp




