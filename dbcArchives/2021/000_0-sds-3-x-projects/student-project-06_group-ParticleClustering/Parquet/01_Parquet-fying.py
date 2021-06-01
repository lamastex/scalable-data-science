# Databricks notebook source
# MAGIC %md
# MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

# COMMAND ----------

# MAGIC %md We start by seeing if the files necessary to run our notebooks are already in the distributed file system

# COMMAND ----------

# MAGIC %scala
# MAGIC dbutils.fs.ls("dbfs:///FileStore/06_LHC")

# COMMAND ----------

# MAGIC %md import the files to the local driver. This takes a minute.

# COMMAND ----------

# MAGIC %scala
# MAGIC dbutils.fs.cp("dbfs:///FileStore/06_LHC", "file:////databricks/driver/06_LHC", recurse=true)

# COMMAND ----------

# MAGIC %sh
# MAGIC ls 06_LHC/h5 

# COMMAND ----------

# MAGIC %md Parquet-fying of the h5 dataset

# COMMAND ----------

# MAGIC %python
# MAGIC import h5py
# MAGIC import pandas as pd
# MAGIC ## BOOKMARK FROM HERE IMPORTANT
# MAGIC import pyarrow as pa
# MAGIC import pyarrow.parquet as pq
# MAGIC 
# MAGIC def get_unique(xs):
# MAGIC   x0 = next(xs)
# MAGIC   assert(x == x0 for x in xs)
# MAGIC   return x0
# MAGIC 
# MAGIC f = h5py.File('06_LHC/h5/train_multi_20v_100P.h5', 'r')
# MAGIC data_keys =list(f.keys())
# MAGIC row_count = get_unique(f[k].shape[0] for k in data_keys)
# MAGIC #print(len(data_keys))
# MAGIC 
# MAGIC data = { data_keys[0] : list(f[data_keys[0]][:].reshape(row_count,-1)),
# MAGIC          data_keys[1] : list(f[data_keys[1]][:].reshape(row_count,-1)),
# MAGIC          data_keys[2] : list(f[data_keys[2]][:].reshape(row_count,-1)),
# MAGIC          data_keys[3] : list(f[data_keys[3]][:]),
# MAGIC          data_keys[4] : list(f[data_keys[4]][:])}
# MAGIC data_table = pa.Table.from_pandas(pd.DataFrame(data))
# MAGIC writer = None
# MAGIC if writer is None:
# MAGIC   writer = pq.ParquetWriter('train.parquet', data_table.schema)
# MAGIC   
# MAGIC writer.write_table(data_table)
# MAGIC   
# MAGIC writer.close()
# MAGIC 
# MAGIC f = h5py.File('06_LHC/h5/test_multi_20v_100P.h5', 'r')
# MAGIC data_keys =list(f.keys())
# MAGIC row_count = get_unique(f[k].shape[0] for k in data_keys)
# MAGIC #print(len(data_keys))
# MAGIC 
# MAGIC data = { data_keys[0] : list(f[data_keys[0]][:].reshape(row_count,-1)),
# MAGIC          data_keys[1] : list(f[data_keys[1]][:].reshape(row_count,-1)),
# MAGIC          data_keys[2] : list(f[data_keys[2]][:].reshape(row_count,-1)),
# MAGIC          data_keys[3] : list(f[data_keys[3]][:]),
# MAGIC          data_keys[4] : list(f[data_keys[4]][:])}
# MAGIC data_table = pa.Table.from_pandas(pd.DataFrame(data))
# MAGIC writer = None
# MAGIC if writer is None:
# MAGIC   writer = pq.ParquetWriter('test.parquet', data_table.schema)
# MAGIC   
# MAGIC writer.write_table(data_table)
# MAGIC   
# MAGIC writer.close()

# COMMAND ----------

# MAGIC %sh
# MAGIC ls -lh

# COMMAND ----------

dbutils.fs.cp("file:////databricks/driver/train.parquet", "dbfs:///datasets/physics/LHC/train/", recurse=true)
dbutils.fs.cp("file:////databricks/driver/test.parquet", "dbfs:///datasets/physics/LHC/test/", recurse=true)

# COMMAND ----------

dbutils.fs.mv("dbfs:///datasets/physics/LHC/train", "dbfs:///datasets/physics/LHC/train.parquet")
dbutils.fs.mv("dbfs:///datasets/physics/LHC/test", "dbfs:///datasets/physics/LHC/test.parquet")

# COMMAND ----------

pq.read_table('test.parquet')['pid'].

# COMMAND ----------

t = pq.read_table('test.parquet', rows=[3])
t_pd = t.to_pandas()
t_pd

# COMMAND ----------



# COMMAND ----------

