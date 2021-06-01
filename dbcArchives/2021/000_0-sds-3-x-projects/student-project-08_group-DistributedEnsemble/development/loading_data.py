# Databricks notebook source
# MAGIC %md
# MAGIC **TOY DATA**
# MAGIC 
# MAGIC Data with Gaussian clusters

# COMMAND ----------

from pyspark.mllib.random import RandomRDDs
from random import randrange
import numpy as np
import torch

# COMMAND ----------

def create_gaussian_RDD(means, variances, num_observations, class_proportions, train_test_split=False):
  """Create toy Gaussian classification data
  Let C := number of clusters/classes and P := number of data features
  
  Args: 
    means (np.array[float]): mean vector of shape (C, P)
    variances (np.array[float]): vector of variances, shape (C, P)
    num_observations (scalar[int]): the total number of observations in the final data set
    class_proportions (np.array[float]): vector of class proportions, length C
    train_test_split: whether to split the data into train/test sets or not
    
  Returns:
    Gaussian data, RDD of tuples (list(features), int(label))
  """
  
  assert means.shape[0] == variances.shape[0]
  assert means.shape[1] == variances.shape[1]
  assert class_proportions.sum() == 1
    
  num_classes = means.shape[0]
  num_features = means.shape[1]
  
  data_rdd = sc.emptyRDD() 
  for k in range(num_classes):
    
    # Generate standard normal data
    class_size = int(num_observations * class_proportions[k])
    class_rdd = RandomRDDs.normalVectorRDD(sc, numRows=class_size, numCols=num_features, numPartitions=1) #, seed=123)

    # Map to true distribution
    class_rdd_transformed = class_rdd.map(lambda v: means[k, :] + (variances[k, :]**0.5) * v)
    
    # Add labels
    class_rdd_w_label = class_rdd_transformed.map(lambda v: (v, k)) 
    
    data_rdd = data_rdd.union(class_rdd_w_label)
    
  # We will shuffle and repartition the data
  num_partitions = 10
  shuffled_rdd =  data_rdd.sortBy(lambda v: randrange(num_observations)).repartition(num_partitions)
  final_rdd = shuffled_rdd.map(tuple).map(lambda v: (list(v[0]), int(v[1])))
  
  if train_test_split:
    train_rdd, test_rdd = final_rdd.randomSplit(weights=[0.8, 0.2], seed=12)
    final_rdd = (train_rdd, test_rdd)
    
  return final_rdd                                   

# COMMAND ----------

# Example use
# Test: 3 classes, 2 features
means = np.array([[-1, -10], [1, 10], [0, -1]])
variances = np.ones((3, 2))

# We should shuffle this
num_observations=1000
class_proportions = np.array([0.3, 0.3, 0.4]) # Sorry, this should have been an array
my_rdd = create_gaussian_RDD(means, variances, num_observations, class_proportions)
my_rdd.take(5)

# COMMAND ----------

# MAGIC %md
# MAGIC **FRAUD DATA**
# MAGIC 
# MAGIC This data is from https://www.kaggle.com/ntnu-testimon/paysim1 [1]. It is actually synthetic but should have more complex features than the toy data above. Number of data points in total: 6,362,620. Number of features (kept): 7 (wherof one is categorical). Number of classes: 2 (fraud/not fraud).
# MAGIC 
# MAGIC [1] E. A. Lopez-Rojas , A. Elmir, and S. Axelsson. "PaySim: A financial mobile money simulator for fraud detection". In: The 28th European Modeling and Simulation Symposium-EMSS, Larnaca, Cyprus. 2016
# MAGIC (PaySim first paper of the simulator)

# COMMAND ----------

from pyspark.ml.feature import StringIndexer, OneHotEncoder, StandardScaler, VectorAssembler
from pyspark.ml import Pipeline
import torch

# COMMAND ----------

def load_fraud_data(train_test_split=False):
  """Load and preprocess synthetic fraud data set
  
  Args:
     train_test_split: whether to split the data into train/test sets or not
    
  Returns:
    Fraud data, RDD of tuples (list(features), int(label))
  """
  
  # File location and type
  file_location = "/FileStore/tables/financial_fraud_data.csv"  # It seems perhaps like these files are not saved for long?
  file_type = "csv"

  # CSV options
  infer_schema = "true"
  first_row_is_header = "true"
  delimiter = ","

  # Load the data from file
  df = spark.read.format(file_type) \
      .option("inferSchema", infer_schema) \
      .option("header", first_row_is_header) \
      .option("sep", delimiter) \
      .load(file_location).select("step", "type", "amount", "oldbalanceOrg", "newbalanceOrig", "oldbalanceDest", "newbalanceDest", "isFraud") \
  
  col_num = ["step", "amount", "oldbalanceOrg", "newbalanceOrig", "oldbalanceDest", "newbalanceDest"]
  col_cat = "type"

  # Rename target column
  df_renamed = df.withColumnRenamed("isFraud", "label")

  # Convert qualitative variable to one-hot
  indexer = StringIndexer(inputCol = col_cat, outputCol = "type_ind")
  oh_encoder = OneHotEncoder(inputCol = "type_ind", outputCol = "type_oh")

  # Scale numerical features
  va = VectorAssembler(inputCols = col_num_f, outputCol = "numerical_features")
  scaler = StandardScaler(inputCol = "numerical_features", outputCol="scaled")

  # Merge all features in one column
  va2 = VectorAssembler(inputCols = ["scaled", "type_oh"], outputCol = "features")

  # Apply pipeline
  pipeline = Pipeline(stages = [indexer, oh_encoder, va, scaler, va2])
  final_df = pipeline.fit(df_renamed).transform(df_renamed).select("features", "label")

  # Convert to RDD 
  final_rdd = final_df.rdd.map(tuple).map(lambda v: (list(v[0]), int(v[1])))
  
  if train_test_split:
    train_rdd, test_rdd = final_rdd.randomSplit(weights=[0.8, 0.2], seed=12)
    final_rdd = (train_rdd, test_rdd)
  
  return final_rdd

# COMMAND ----------

fraud_rdd = load_fraud_data()
fraud_rdd.take(5)

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC **FIRE WALL DATA**
# MAGIC 
# MAGIC Data from UCI repository: https://archive.ics.uci.edu/ml/datasets/Internet+Firewall+Data [2]. Number of data points: 65,532. Number of features: 11 (all numerical). Number of classes: 4 (allow/deny/drop/reset both).
# MAGIC 
# MAGIC [2] Dua, D. and Graff, C. (2019). UCI Machine Learning Repository [http://archive.ics.uci.edu/ml]. Irvine, CA: University of California, School of Information and Computer Science.

# COMMAND ----------

from pyspark.ml.feature import StringIndexer, OneHotEncoder, StandardScaler, VectorAssembler
from pyspark.ml import Pipeline
import torch

# COMMAND ----------

def load_firewall_data(train_test_split=False):
  """Load and preprocess firewall data
  Args:
     train_test_split: whether to split the data into train/test sets or not
  
  Returns:
     Firewall data, RDD of tuples (list(features), int(label))
  """
  
  # File location and type
  file_location = "/FileStore/shared_uploads/amanda.olmin@liu.se/fire_wall_data.csv" 
  file_type = "csv"

  # CSV options
  infer_schema = "true"
  first_row_is_header = "true"
  delimiter = ","

  # Load the data from file
  df = spark.read.format(file_type) \
      .option("inferSchema", infer_schema) \
      .option("header", first_row_is_header) \
      .option("sep", delimiter) \
      .load(file_location)
  
  # Preprocess data
  col_num = ["Source Port", "Destination Port", "NAT Source Port", "NAT Destination Port", "Bytes", "Bytes Sent", "Bytes Received", "Packets", "Elapsed Time (sec)", "pkts_sent", "pkts_received"]

  # Index qualitative variable
  indexer = StringIndexer(inputCol = "Action", outputCol = "label")

  # Scale numerical features
  va = VectorAssembler(inputCols = col_num, outputCol = "numerical_features") 
  scaler = StandardScaler(inputCol = "numerical_features", outputCol = "features")
  
  # Apply pipeline 
  pipeline = Pipeline(stages=[indexer, va, scaler])
  final_df = pipeline.fit(df).transform(df).select("features", "label") 
  
  # Convert to RDD
  final_rdd = final_df.rdd.map(tuple).map(lambda v: (list(v[0]), int(v[1])))
  
  if train_test_split:
    train_rdd, test_rdd = final_rdd.randomSplit(weights=[0.8, 0.2], seed=12)
    final_rdd = (train_rdd, test_rdd)

  return final_rdd

# COMMAND ----------

firewall_rdd = load_firewall_data()
firewall_rdd.take(5)

# COMMAND ----------

# The functions below will return RDDs with tuples (tensor(features), tensor(label))

# One way to create a "batch" iterator from the RDDs is with itertoolz
from toolz.itertoolz import partition_all

def get_batched_iterator(input_rdd):
  return input_rdd.mapPartitions(lambda partition: partition_all(batch_size, partition)).toLocalIterator()

#print(next(iterator))