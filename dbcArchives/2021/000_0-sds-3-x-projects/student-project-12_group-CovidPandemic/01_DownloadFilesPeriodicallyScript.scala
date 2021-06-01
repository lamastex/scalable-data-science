// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Download Files Periodically
// MAGIC This notebook allows for setup and execution of a script to periodically download files. In this case the "Our World in Data" dataset csv files which are updated daily.
// MAGIC 
// MAGIC Content is based on "037a_AnimalNamesStructStreamingFiles" by Raazesh Sainudiin.

// COMMAND ----------

// MAGIC %md
// MAGIC To be able to later kill a .sh process, we need to make this installation

// COMMAND ----------

// MAGIC %sh
// MAGIC apt-get install -y psmisc 

// COMMAND ----------

// MAGIC %md
// MAGIC create a new directory for our files if needed

// COMMAND ----------

dbutils.fs.mkdirs("file:///databricks/driver/projects/group12")

// COMMAND ----------

// MAGIC %md create a shell script to periodically download the dataset (currently set to download once per day).
// MAGIC 1. shell
// MAGIC 2. remove the previous shell script
// MAGIC 3. write to script: bash binaries
// MAGIC 4. write to script: remove folder where previous downloaded files are located
// MAGIC 5. write to script: make new directory to put downloaded files
// MAGIC 6. write to script: while loop:
// MAGIC 6.1) remove old downloaded csv dataset
// MAGIC 6.2) download new csv dataset
// MAGIC 6.3) copy the csv file to the newly created directory using the timestamp as name
// MAGIC 7. print the contents of the shell script

// COMMAND ----------

// MAGIC %sh
// MAGIC rm -f projects/group12/group12downloadFiles.sh &&
// MAGIC echo "#!/bin/bash" >> projects/group12/group12downloadFiles.sh &&
// MAGIC echo "rm -rf projects/group12/logsEveryXSecs" >> projects/group12/group12downloadFiles.sh &&
// MAGIC echo "mkdir -p projects/group12/logsEveryXSecs" >> projects/group12/group12downloadFiles.sh &&
// MAGIC echo "while true; rm owid-covid-data.csv; wget https://covid.ourworldindata.org/data/owid-covid-data.csv; do echo \$( date --rfc-3339=second )\; | cp owid-covid-data.csv projects/group12/logsEveryXSecs/\$( date '+%y_%m_%d_%H_%M_%S.csv' ); sleep 216000; done" >> projects/group12/group12downloadFiles.sh &&
// MAGIC cat projects/group12/group12downloadFiles.sh

// COMMAND ----------

// MAGIC %md
// MAGIC make the shell script executable

// COMMAND ----------

// MAGIC %sh 
// MAGIC chmod 744 projects/group12/group12downloadFiles.sh

// COMMAND ----------

// MAGIC %md
// MAGIC execute the shell script

// COMMAND ----------

// MAGIC %sh
// MAGIC nohup projects/group12/group12downloadFiles.sh

// COMMAND ----------

// MAGIC %md
// MAGIC look at the files

// COMMAND ----------

// MAGIC %sh
// MAGIC pwd
// MAGIC ls -al projects/group12/logsEveryXSecs

// COMMAND ----------

// MAGIC %md
// MAGIC look at the file content

// COMMAND ----------

// MAGIC %sh
// MAGIC cat projects/group12/logsEveryXSecs/XXXX.csv

// COMMAND ----------

// MAGIC %md
// MAGIC kill the .sh process

// COMMAND ----------

// MAGIC %sh
// MAGIC killall group12downloadFiles.sh

// COMMAND ----------

// MAGIC %md move downloaded files to another location to make sure we don't delete the datasets

// COMMAND ----------

// dbutils.fs.mkdirs("/datasets/group12/")

// COMMAND ----------

dbutils.fs.cp("file:///databricks/driver/projects/group12/logsEveryXSecs/","/datasets/group12/",true)

// COMMAND ----------

display(dbutils.fs.ls("/datasets/group12/"))

// COMMAND ----------

