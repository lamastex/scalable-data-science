// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %sh
// MAGIC wget https://zenodo.org/record/3602254/files/hls4ml_LHCjet_100p_train.tar.gz

// COMMAND ----------

// MAGIC %sh
// MAGIC pip install tables

// COMMAND ----------

// MAGIC %python
// MAGIC import h5py
// MAGIC import pandas as pd
// MAGIC ## BOOKMARK FROM HERE IMPORTANT
// MAGIC import pyarrow as pa
// MAGIC import pyarrow.parquet as pq
// MAGIC ## TO HERE
// MAGIC import glob
// MAGIC 
// MAGIC fps = glob.glob('train/*.h5')
// MAGIC all_key_sets = [h5py.File(fp, 'r').keys() for fp in fps]
// MAGIC all_keys = set().union(*all_key_sets)
// MAGIC 
// MAGIC print("Ignoring due to missing keys in:", [(fp, k) for fp,k in zip(fps, all_key_sets) if k != all_keys])
// MAGIC 
// MAGIC data_keys = ['jetImageHCAL', 'jetConstituentList', 'jetImage', 'jetImageECAL', 'jets']
// MAGIC name_keys = ['particleFeatureNames', 'jetFeatureNames']
// MAGIC 
// MAGIC assert(all_keys == set().union(data_keys, name_keys))
// MAGIC 
// MAGIC fps = [fp for fp,k in zip(fps, all_key_sets) if k == all_keys]
// MAGIC 
// MAGIC def get_unique(xs):
// MAGIC   x0 = next(xs)
// MAGIC   assert(x == x0 for x in xs)
// MAGIC   return x0
// MAGIC 
// MAGIC jetFeatureNames = [k.decode() for k in get_unique(h5py.File(fp, 'r')['jetFeatureNames'][:] for fp in fps)]
// MAGIC particleFeatureNames = [k.decode() for k in get_unique(h5py.File(fp, 'r')['particleFeatureNames'][:] for fp in fps)[:-1]]
// MAGIC 
// MAGIC ## Initialise writer based on first result
// MAGIC writer = None
// MAGIC 
// MAGIC for fp in fps:
// MAGIC   print("Processing: ", fp)
// MAGIC   
// MAGIC   ## for chunk in h5y.File(fp, 'r', chunksize) ... or similar
// MAGIC   
// MAGIC   f = h5py.File(fp, 'r')
// MAGIC   row_count = get_unique(f[k].shape[0] for k in data_keys)
// MAGIC   assert(row_count == 10000)
// MAGIC   
// MAGIC   ## 100x100 bitmap per row
// MAGIC   assert(f['jetImageHCAL'].shape == (row_count, 100, 100))
// MAGIC   
// MAGIC   ## 100x100 bitmap per row
// MAGIC   assert(f['jetImageECAL'].shape == (row_count, 100, 100))
// MAGIC   
// MAGIC   ## 100x100 bitmap per row
// MAGIC   assert(f['jetImage'].shape == (row_count, 100, 100))
// MAGIC   
// MAGIC   constituent_count = f['jetConstituentList'].shape[1]
// MAGIC   
// MAGIC   assert(f['jets'].shape == (row_count, len(jetFeatureNames)))
// MAGIC   assert(f['jetConstituentList'].shape == (row_count, constituent_count, len(particleFeatureNames)))
// MAGIC   
// MAGIC   ## Slow, but works...
// MAGIC   
// MAGIC   #jets = pa.Table.from_arrays([f['jets'][:,i] for i,k in enumerate(jetFeatureNames)], names=jetFeatureNames)
// MAGIC   #print(jets.schema)
// MAGIC   
// MAGIC   #particles = pa.Table.from_pydict({ k : [f['jetConstituentList'][:,j,i] for j in range(constituent_count)] for i,k in enumerate(particleFeatureNames) })
// MAGIC   #print(particles.schema)
// MAGIC   
// MAGIC   ## Messy and a bit slow, but couldn't coerce the API to do anything slicker
// MAGIC   data = { 'file' : [fp] * row_count, 'i' : range(row_count) }
// MAGIC   data.update({ k : list(f['jetConstituentList'][:,:,i]) for i,k in enumerate(particleFeatureNames) })
// MAGIC   data.update({ k : f['jets'][:,i] for i,k in enumerate(jetFeatureNames) })
// MAGIC   data.update({ 'jetImageHCAL' : list(f['jetImageHCAL'][:].reshape(row_count, -1)),
// MAGIC                 'jetImageECAL' : list(f['jetImageECAL'][:].reshape(row_count, -1)),
// MAGIC                 'jetImage'     : list(f['jetImage'][:].reshape(row_count, -1)) })
// MAGIC 
// MAGIC   data_table = pa.Table.from_pandas(pd.DataFrame(data))
// MAGIC   
// MAGIC   ## print(data_table['j1_px'])
// MAGIC   
// MAGIC   if writer is None:
// MAGIC     ## Initialise writer based on schema from first parsed data
// MAGIC     
// MAGIC     ## BOOKMARK FROM HERE IMPORTANT
// MAGIC     writer = pq.ParquetWriter('jetImage.parquet', data_table.schema)
// MAGIC     
// MAGIC   writer.write_table(data_table)
// MAGIC   
// MAGIC writer.close()
// MAGIC 
// MAGIC ## TO HERE

// COMMAND ----------

// MAGIC %sh
// MAGIC ls -lh jetImage.parquet
// MAGIC pwd

// COMMAND ----------

// MAGIC %sh
// MAGIC zcat hls4ml_LHCjet_100p_train.tar.gz | file -

// COMMAND ----------

// MAGIC %md
// MAGIC I am going to download locally and see what's up... In the mean time have a read through the links in notebook `03_...` in this directory.. Raaz

// COMMAND ----------

// MAGIC %fs
// MAGIC ls /datasets/physics/LHC/

// COMMAND ----------

dbutils.fs.cp("file:////databricks/driver/jetImage.parquet", "dbfs:///datasets/physics/LHC/hls4ml_LHCjet_100p/", recurse=true)

// COMMAND ----------

dbutils.fs.mv("dbfs:///datasets/physics/LHC/hls4ml_LHCjet_100p", "dbfs:///datasets/physics/LHC/hls4ml_LHCjet_100p.parquet")

// COMMAND ----------

sqlContext.read.parquet("dbfs:///datasets/physics/LHC/hls4ml_LHCjet_100p.parquet").groupBy("file").count().collect()

// COMMAND ----------

dbutils.fs.cp("file:////databricks/driver/hls4ml_LHCjet_100p_train.tar.gz","dbfs:///datasets/physics/LHC/")

// COMMAND ----------

// MAGIC %fs
// MAGIC ls /datasets/physics/LHC/

// COMMAND ----------

// MAGIC %md
// MAGIC At least the file is in dbfs

// COMMAND ----------

val possiblyMalformedInput = sc.textFile("dbfs:/datasets/physics/LHC/hls4ml_LHCjet_100p_train.tar.gz")

// COMMAND ----------

possiblyMalformedInput.count // so it reads the lines fine

// COMMAND ----------

possiblyMalformedInput.take(1) // this is not compressed text, but hdf format... so google search (perhapos in incognito in chrome or private mode in firefox) for 

// COMMAND ----------

// MAGIC %md
// MAGIC google search returned
// MAGIC 
// MAGIC - first try: https://forums.databricks.com/questions/22178/how-to-parse-hdf5-file-using-sparkreadformat.html
// MAGIC - deeped dive if needed: https://www.hdfgroup.org/2015/03/from-hdf5-datasets-to-apache-spark-rdds/
// MAGIC - etc

// COMMAND ----------

import org.hdfgroup.spark.hdf5._ 
import org.apache.spark.sql.SparkSession 
//val spark = SparkSession.builder().appName("Spark SQL HDF5").getOrCreate()

val df=spark.read.option("extension", "he5").option("recursion", "false").hdf5("/tmp/", "dbfs:/datasets/physics/LHC/hls4ml_LHCjet_100p_train.tar.gz")

df.show()

// COMMAND ----------

// MAGIC %md
// MAGIC Looks like libraries are needed... One need to recall how the HDF5 connector works and how these files are organized... Raaz

// COMMAND ----------

// MAGIC %py
// MAGIC # try from here ...for mixing python and scala cell-specifically
// MAGIC 1+1

// COMMAND ----------

// MAGIC %md
// MAGIC Parquet-ing the already processed h5 files. 

// COMMAND ----------

dbutils.fs.ls("dbfs:///FileStore/06_LHC")

// COMMAND ----------

dbutils.fs.cp("dbfs:///FileStore/06_LHC", "file:////databricks/driver/06_LHC", recurse=true)

// COMMAND ----------

// MAGIC %sh
// MAGIC ls

// COMMAND ----------

// MAGIC %python
// MAGIC import h5py
// MAGIC import pandas as pd
// MAGIC ## BOOKMARK FROM HERE IMPORTANT
// MAGIC import pyarrow as pa
// MAGIC import pyarrow.parquet as pq
// MAGIC 
// MAGIC def get_unique(xs):
// MAGIC   x0 = next(xs)
// MAGIC   assert(x == x0 for x in xs)
// MAGIC   return x0
// MAGIC 
// MAGIC f = h5py.File('06_LHC/h5/train_multi_20v_100P.h5', 'r')
// MAGIC data_keys =list(f.keys())
// MAGIC row_count = get_unique(f[k].shape[0] for k in data_keys)
// MAGIC #print(len(data_keys))
// MAGIC 
// MAGIC data = { data_keys[0] : list(f[data_keys[0]][:].reshape(row_count,-1)),
// MAGIC          data_keys[1] : list(f[data_keys[1]][:].reshape(row_count,-1)),
// MAGIC          data_keys[2] : list(f[data_keys[2]][:].reshape(row_count,-1)),
// MAGIC          data_keys[3] : list(f[data_keys[3]][:]),
// MAGIC          data_keys[4] : list(f[data_keys[4]][:])}
// MAGIC data_table = pa.Table.from_pandas(pd.DataFrame(data))
// MAGIC writer = None
// MAGIC if writer is None:
// MAGIC   writer = pq.ParquetWriter('train.parquet', data_table.schema)
// MAGIC   
// MAGIC writer.write_table(data_table)
// MAGIC   
// MAGIC writer.close()
// MAGIC 
// MAGIC f = h5py.File('06_LHC/h5/test_multi_20v_100P.h5', 'r')
// MAGIC data_keys =list(f.keys())
// MAGIC row_count = get_unique(f[k].shape[0] for k in data_keys)
// MAGIC #print(len(data_keys))
// MAGIC 
// MAGIC data = { data_keys[0] : list(f[data_keys[0]][:].reshape(row_count,-1)),
// MAGIC          data_keys[1] : list(f[data_keys[1]][:].reshape(row_count,-1)),
// MAGIC          data_keys[2] : list(f[data_keys[2]][:].reshape(row_count,-1)),
// MAGIC          data_keys[3] : list(f[data_keys[3]][:]),
// MAGIC          data_keys[4] : list(f[data_keys[4]][:])}
// MAGIC data_table = pa.Table.from_pandas(pd.DataFrame(data))
// MAGIC writer = None
// MAGIC if writer is None:
// MAGIC   writer = pq.ParquetWriter('test.parquet', data_table.schema)
// MAGIC   
// MAGIC writer.write_table(data_table)
// MAGIC   
// MAGIC writer.close()

// COMMAND ----------

dbutils.fs.cp("file:////databricks/driver/train.parquet", "dbfs:///datasets/physics/LHC/train/", recurse=true)
dbutils.fs.cp("file:////databricks/driver/test.parquet", "dbfs:///datasets/physics/LHC/test/", recurse=true)

// COMMAND ----------

dbutils.fs.mv("dbfs:///datasets/physics/LHC/train", "dbfs:///datasets/physics/LHC/train.parquet")
dbutils.fs.mv("dbfs:///datasets/physics/LHC/test", "dbfs:///datasets/physics/LHC/test.parquet")

// COMMAND ----------

// MAGIC %sh
// MAGIC mv train.parquet 06_LHC
// MAGIC mv test.parquet 06_LHC

// COMMAND ----------

// MAGIC %md
// MAGIC Preparing data for test
// MAGIC 
// MAGIC OK THIS WASN'T SO EASY.... ARRAYS NEED TO BE RESHAPED INTO LISTS. SO READING FROM PARQUET WILL BE COMPLICATED. BEGGING THE QUESTION.... WHY BOTHER???

// COMMAND ----------

// MAGIC %sh
// MAGIC pip install tensorflow==1.15 

// COMMAND ----------

// MAGIC %python
// MAGIC from tensorflow import keras
// MAGIC 
// MAGIC rank = 0
// MAGIC size= 1
// MAGIC 
// MAGIC (x_train, y_train), (x_test, y_test) = keras.datasets.mnist.load_data('MNIST-data-%d' % rank)
// MAGIC 
// MAGIC x_train = x_train[rank::size]
// MAGIC 
// MAGIC y_train = y_train[rank::size]
// MAGIC 
// MAGIC x_test = x_test[rank::size]
// MAGIC 
// MAGIC y_test = y_test[rank::size]
// MAGIC 
// MAGIC x_train = x_train.reshape(x_train.shape[0], 28, 28, 1)
// MAGIC 
// MAGIC x_test = x_test.reshape(x_test.shape[0], 28, 28, 1)
// MAGIC 
// MAGIC x_train = x_train.astype('float32')
// MAGIC 
// MAGIC x_test = x_test.astype('float32')
// MAGIC 
// MAGIC x_train /= 255
// MAGIC 
// MAGIC x_test /= 255
// MAGIC 
// MAGIC #y_train = keras.utils.to_categorical(y_train, num_classes)
// MAGIC 
// MAGIC #y_test = keras.utils.to_categorical(y_test, num_classes)

// COMMAND ----------

// MAGIC %python
// MAGIC x_test.shape

// COMMAND ----------

// MAGIC %python
// MAGIC import pandas as pd
// MAGIC ## BOOKMARK FROM HERE IMPORTANT
// MAGIC import pyarrow as pa
// MAGIC import pyarrow.parquet as pq
// MAGIC 
// MAGIC def get_unique(xs):
// MAGIC   x0 = next(xs)
// MAGIC   assert(x == x0 for x in xs)
// MAGIC   return x0
// MAGIC 
// MAGIC #print(len(data_keys))
// MAGIC 
// MAGIC data = { 'x_train' : x_train,
// MAGIC          'y_train' : y_train}
// MAGIC 
// MAGIC data_table = pa.Table.from_pandas(pd.DataFrame(data))
// MAGIC writer = None
// MAGIC if writer is None:
// MAGIC   writer = pq.ParquetWriter('horovod_data_train.parquet', data_table.schema)
// MAGIC   
// MAGIC writer.write_table(data_table)
// MAGIC   
// MAGIC writer.close()
// MAGIC 
// MAGIC data = { 'x_test' : x_test,
// MAGIC          'y_test' : y_test}
// MAGIC 
// MAGIC data_table = pa.Table.from_pandas(pd.DataFrame(data))
// MAGIC writer = None
// MAGIC if writer is None:
// MAGIC   writer = pq.ParquetWriter('horovod_data_test.parquet', data_table.schema)
// MAGIC   
// MAGIC writer.write_table(data_table)
// MAGIC   
// MAGIC writer.close()

// COMMAND ----------

dbutils.fs.cp("file:////databricks/driver/horovod_data_train.parquet", "dbfs:///datasets/physics/LHC/horovod_data_train/", recurse=true)
dbutils.fs.cp("file:////databricks/driver/horovod_data_test.parquet", "dbfs:///datasets/physics/LHC/horovod_data_test/", recurse=true)

// COMMAND ----------

dbutils.fs.mv("dbfs:///datasets/physics/LHC/horovod_data_train", "dbfs:///datasets/physics/LHC/horovod_data_train.parquet")
dbutils.fs.mv("dbfs:///datasets/physics/LHC/horovod_data_test", "dbfs:///datasets/physics/LHC/horovod_data_test.parquet")

// COMMAND ----------

// MAGIC %python
// MAGIC pq.read_table('horovod_data_test.parquet', columns=['x_test'])

// COMMAND ----------

// MAGIC %python
// MAGIC new_x_test

// COMMAND ----------

// MAGIC %python
// MAGIC import numpy as np
// MAGIC x_test

// COMMAND ----------

