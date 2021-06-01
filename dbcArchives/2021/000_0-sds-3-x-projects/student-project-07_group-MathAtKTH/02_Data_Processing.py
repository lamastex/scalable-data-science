# Databricks notebook source
# MAGIC %md
# MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

# COMMAND ----------

# MAGIC %md
# MAGIC # Data Processing
# MAGIC Here we will load the data as it was given to us, as a ".npy" file, and rewrite it in a simpler format. Why we do this is motivated in "Coding_Motifs", subsection "application".

# COMMAND ----------

# Load packages

import numpy as np

# COMMAND ----------

# MAGIC %md
# MAGIC Read the data as a numpy object (since we got it like that), and save it as a ".csv" since scala can read that.

# COMMAND ----------



M = np.load('/dbfs/FileStore/shared_uploads/adlindhe@kth.se/M.npy')
np.savetxt("/dbfs/FileStore/shared_uploads/adlindhe@kth.se/M.csv", M, delimiter=",")


# COMMAND ----------

# MAGIC %md 
# MAGIC Right now the data is an adjacency matrix of size 31346 times 31346 but only 0.7% of all entries are 1.
# MAGIC Thus we would like to save it down as something a little easier to handle. We can read the adjacency matrix directly as 
# MAGIC a dataframe, but that does not work well with graphframes. Instead we want it as an edgelist.

# COMMAND ----------

# MAGIC %scala
# MAGIC /*
# MAGIC ** Example on how we can read the data.
# MAGIC */
# MAGIC val Ms = spark.read.format("csv").option("sep",",").option("MaxColumns",40000).load("/FileStore/shared_uploads/adlindhe@kth.se/M.csv")

# COMMAND ----------

# MAGIC %md
# MAGIC Thus we rewrite it as a edgelist. Thus we can more easily load it into graphframes.

# COMMAND ----------

# Loop over the whole matrix. This takes time (~ 1h).

edges_file = open("/dbfs/FileStore/shared_uploads/petterre@kth.se/edges.csv", "w")

for i in range(len(M)):
  for j in range(len(M[i])):
    for k in range(M[i][j]):
      edges_file.write(str(i) + "," + str(j) + ",edge\n")

edges_file.close()


# COMMAND ----------

# MAGIC %md
# MAGIC Look at it to see if it looks ok.

# COMMAND ----------

# MAGIC %sh 
# MAGIC 
# MAGIC head /dbfs/FileStore/shared_uploads/petterre@kth.se/edges.csv

# COMMAND ----------

# MAGIC %scala
# MAGIC Ms.cache // cache the DataFrame this time
# MAGIC Ms.count // now after this action, the next count will be fast...

# COMMAND ----------

# MAGIC %scala
# MAGIC display(Ms)