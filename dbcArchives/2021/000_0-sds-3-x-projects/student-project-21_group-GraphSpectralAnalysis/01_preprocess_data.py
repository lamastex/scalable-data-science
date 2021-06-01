# Databricks notebook source
# MAGIC %md
# MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

# COMMAND ----------

# MAGIC %md
# MAGIC # Preprocess the data
# MAGIC Here the raw Ethereum transaction data read from google big query is preprocessed.
# MAGIC - Remove any rows with nulls
# MAGIC - Drop all self-loops
# MAGIC - Enumerate all the distict addresses
# MAGIC - Make a canonical ordering for the edges
# MAGIC   - Each edge will point from lower to higher index
# MAGIC   - The sign of the transaction is changed for flipped edges
# MAGIC - Aggregate transactions based on src, dst pair
# MAGIC - Enumerate the edges with a unique edge id

# COMMAND ----------

import pyspark.sql.functions as F
from pyspark.sql.window import Window

# COMMAND ----------

# MAGIC %md
# MAGIC ### Load data into DataFrame
# MAGIC And drop nans and self-loop

# COMMAND ----------

data_path = "FileStore/tables/ethereum_march_2018_2020"

df = spark.read.format('csv').option("header", "true").load(data_path)\
  .select(F.col("from_address"), F.col("to_address"), F.col("value"))\
  .na.drop()\
  .where(F.col("from_address") != F.col("to_address"))

# COMMAND ----------

# MAGIC %md 
# MAGIC ### Enumerate the addresses with a unique id

# COMMAND ----------

addresses = df.select(F.col("from_address").alias("address")).union(df.select(F.col("to_address").alias("address"))).distinct()
address_window = Window.orderBy("address")
addresses = addresses.withColumn("id", F.row_number().over(address_window))

# COMMAND ----------

# MAGIC %md
# MAGIC ### Make the edges canonical
# MAGIC - Each edge will point from lower to higher index
# MAGIC - The sign of the transaction is changed for flipped edges

# COMMAND ----------

# Exchange string addresses for node ids
df_with_ids = df.join(addresses.withColumnRenamed("address", "to_address").withColumnRenamed("id", "dst__"), on="to_address")\
  .join(addresses.withColumnRenamed("address", "from_address").withColumnRenamed("id", "src__"), on="from_address")

canonical_edges = df_with_ids.withColumn("src",
  F.when(F.col("dst__") > F.col("src__"), F.col("src__")).otherwise(F.col("dst__"))
).withColumn("dst",
  F.when(F.col("dst__") > F.col("src__"), F.col("dst__")).otherwise(F.col("src__"))
).withColumn("direction__",
  F.when(F.col("dst__") > F.col("src__"), 1).otherwise(-1)
).withColumn("flow",
F.col("value") * F.col("direction__")
)

# COMMAND ----------

# MAGIC %md
# MAGIC ### Group the edges by source (src) and destination (dst) by taking the sum of the flow

# COMMAND ----------

grouped_canonical_edges = canonical_edges.select(F.col("src"), F.col("dst"), F.col("flow")).groupBy(F.col("src"), F.col("dst")).agg(F.sum(F.col("flow")).alias("flow"))

# COMMAND ----------

# MAGIC %md
# MAGIC ### Enumerate the edges with a unique edge id

# COMMAND ----------

edges_window = Window.orderBy(F.col("src"), F.col("dst"))
grouped_canonical_edges = grouped_canonical_edges.withColumn("id", F.row_number().over(edges_window))

# COMMAND ----------

# MAGIC %md
# MAGIC ### Save the results in parquet files

# COMMAND ----------

preprocessed_edges_path = "/projects/group21/test_ethereum_canonical_edges"
preprocessed_addresses_path = "/projects/group21/test_ethereum_addresses"

grouped_canonical_edges.write.format('parquet').mode("overwrite").save(preprocessed_edges_path)
addresses.write.format('parquet').mode("overwrite").save(preprocessed_addresses_path)

# COMMAND ----------

