# Databricks notebook source
# MAGIC %md ##Data and preprocessing

# COMMAND ----------

# MAGIC %md We start by seeing if the files necessary to run our notebooks are already in the distributed file system

# COMMAND ----------

# MAGIC %md
# MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

# COMMAND ----------

# MAGIC %scala
# MAGIC dbutils.fs.ls("dbfs:///FileStore/06_LHC")

# COMMAND ----------

# MAGIC %md 
# MAGIC ###Important! 
# MAGIC 
# MAGIC Run the command line above to see if the required files and data are already available in the distributed file system, you should see the following:
# MAGIC 
# MAGIC Seq[com.databricks.backend.daemon.dbutils.FileInfo] = WrappedArray(FileInfo(dbfs:/FileStore/06_LHC/LICENSE, LICENSE, 1071), FileInfo(dbfs:/FileStore/06_LHC/README.md, README.md, 3150), FileInfo(dbfs:/FileStore/06_LHC/data/, data/, 0), FileInfo(dbfs:/FileStore/06_LHC/h5/, h5/, 0), FileInfo(dbfs:/FileStore/06_LHC/models/, models/, 0), FileInfo(dbfs:/FileStore/06_LHC/scripts/, scripts/, 0), FileInfo(dbfs:/FileStore/06_LHC/utils/, utils/, 0))
# MAGIC 
# MAGIC If these items appear, then skip most of this notebook, and go to Command Cell 21 to import data to the local driver

# COMMAND ----------

# MAGIC %md ### Get the data

# COMMAND ----------

# MAGIC %md We start by installing everything necessary

# COMMAND ----------

pip install h5py

# COMMAND ----------

# MAGIC %md We start by preparing a folder on the local driver to process our files

# COMMAND ----------

# MAGIC %sh
# MAGIC rm -r 06_LHC
# MAGIC mkdir 06_LHC

# COMMAND ----------

# MAGIC %md Now get all necessary files from the project repository on Github

# COMMAND ----------

# MAGIC %sh
# MAGIC cd 06_LHC
# MAGIC 
# MAGIC wget https://github.com/dgedon/ProjectParticleClusteringv2/archive/main.zip
# MAGIC unzip main.zip
# MAGIC mv ProjectParticleClusteringv2-main/* .
# MAGIC rm -r ProjectParticleClusteringv2-main/ main.zip

# COMMAND ----------

# MAGIC %md We get the necessarry data (first training, then validation) and untar the file

# COMMAND ----------

# MAGIC %sh
# MAGIC cd 06_LHC
# MAGIC mkdir data
# MAGIC cd data
# MAGIC 
# MAGIC 
# MAGIC wget https://zenodo.org/record/3602254/files/hls4ml_LHCjet_100p_train.tar.gz
# MAGIC #mkdir data
# MAGIC #mv hls4ml_LHCjet_100p_train.tar.gz data
# MAGIC #cd data
# MAGIC tar --no-same-owner -xvf hls4ml_LHCjet_100p_train.tar.gz

# COMMAND ----------

# MAGIC %sh
# MAGIC cd 06_LHC/data
# MAGIC wget https://zenodo.org/record/3602254/files/hls4ml_LHCjet_100p_val.tar.gz
# MAGIC tar --no-same-owner -xvf hls4ml_LHCjet_100p_val.tar.gz

# COMMAND ----------

# MAGIC %md Now we preprocess the data. This transforms the data somehow in a useful way 

# COMMAND ----------

# MAGIC %sh
# MAGIC cd 06_LHC/scripts
# MAGIC python prepare_data_multi.py --dir ../data/

# COMMAND ----------

# MAGIC %sh
# MAGIC cd 06_LHC/scripts
# MAGIC python prepare_data_multi.py --dir ../data/ --make_eval

# COMMAND ----------

rm -r ../data

# COMMAND ----------

# MAGIC %md
# MAGIC Finally move everything onto the distributed file system. All necessary files are stored at FileStore/06_LHC

# COMMAND ----------

# MAGIC %scala
# MAGIC dbutils.fs.cp ("file:////databricks/driver/06_LHC", "dbfs:///FileStore/06_LHC", recurse=true) 

# COMMAND ----------

# MAGIC %md 
# MAGIC ### Important (continued from above)
# MAGIC ### Import files to local driver
# MAGIC 
# MAGIC Now for future notebooks, run the following command line below to import the files to the local driver. This may take a minute

# COMMAND ----------

# MAGIC %scala
# MAGIC dbutils.fs.cp("dbfs:///FileStore/06_LHC", "file:////databricks/driver/06_LHC", recurse=true)

# COMMAND ----------

# MAGIC %md Run the command line below to list the items in the 06_LHC folder. You should see the following:
# MAGIC 
# MAGIC * LICENSE
# MAGIC * README.md
# MAGIC * h5
# MAGIC * models
# MAGIC * scripts
# MAGIC * utils

# COMMAND ----------

# MAGIC %sh
# MAGIC ls 06_LHC/

# COMMAND ----------

# MAGIC %md You are now ready to go! 

# COMMAND ----------

