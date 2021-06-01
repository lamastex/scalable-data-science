# Databricks notebook source
from pyspark.ml.feature import StringIndexer, OneHotEncoder, StandardScaler, VectorAssembler
from pyspark.ml import Pipeline


def load_firewall_data():
  # File location and type
  file_location_fw = "/FileStore/shared_uploads/amanda.olmin@liu.se/fire_wall_data.csv" 
  file_type = "csv"

  # CSV options
  infer_schema = "true"
  first_row_is_header = "true"
  delimiter = ","

  # Read the data
  df_fw = spark.read.format(file_type) \
    .option("inferSchema", infer_schema) \
    .option("header", first_row_is_header) \
    .option("sep", delimiter) \
    .load(file_location_fw)
  
  # Preprocess data
  col_num_fw = ["Source Port", "Destination Port", "NAT Source Port", "NAT Destination Port", "Bytes", "Bytes Sent", "Bytes Received", "Packets", "Elapsed Time (sec)", "pkts_sent", "pkts_received"]

  # Index qualitative variable
  indexer_fw = StringIndexer(inputCol = "Action", outputCol = "label")

  # Some scaling
  va_fw = VectorAssembler(inputCols = col_num_fw, outputCol = "numerical_features") 
  scaler_fw = StandardScaler(inputCol = "numerical_features", outputCol = "features")

  # Pipeline 
  pipeline_fw = Pipeline(stages=[indexer_fw, va_fw, scaler_fw])

  df_final_fw = pipeline_fw.fit(df_fw).transform(df_fw).select("features", "label")

  # Convert to RDD
  #firewall_rdd = df_final_fw.rdd.map(tuple).map(lambda v: (torch.tensor(v[0], dtype=torch.float64), torch.tensor(v[1], dtype=torch.int32)))
  firewall_rdd = df_final_fw.rdd.map(tuple).map(lambda v: (list(v[0]), int(v[1])))
  
  return firewall_rdd

def load_fraud_data():
  # File location and type
  file_location_f = "/FileStore/shared_uploads/amanda.olmin@liu.se/financial_fraud_data.csv"  # It seems perhaps like these files are not saved for long?
  file_type = "csv"

  # CSV options
  infer_schema = "true"
  first_row_is_header = "true"
  delimiter = ","

  # Read the data
  df_f = spark.read.format(file_type) \
    .option("inferSchema", infer_schema) \
    .option("header", first_row_is_header) \
    .option("sep", delimiter) \
    .load(file_location_f).select("step", "type", "amount", "oldbalanceOrg", "newbalanceOrig", "oldbalanceDest", "newbalanceDest", "isFraud") \

  # removed features flaggedFraud, nameOrig, nameDest, perhaps we could have kept at least flaggedFraud
  # Preprocess data
  
  # There should be no missing values and "type" should have 5 (?) levels
  col_num_f = ["step", "amount", "oldbalanceOrg", "newbalanceOrig", "oldbalanceDest", "newbalanceDest"]
  col_cat_f = "type"

  # Rename target column
  df_renamed_f = df_f.withColumnRenamed("isFraud", "label")

  # Convert qualitative variable to onehot
  indexer_f = StringIndexer(inputCol = col_cat_f, outputCol = "type_ind")
  oh_encoder_f = OneHotEncoder(inputCol = "type_ind", outputCol = "type_oh")

  # We will do some scaling
  va_f = VectorAssembler(inputCols = col_num_f, outputCol = "numerical_features")
  scaler_f = StandardScaler(inputCol = "numerical_features", outputCol="scaled")

  # Now, merge all features in one column
  va2_f = VectorAssembler(inputCols = ["scaled", "type_oh"], outputCol = "features")

  # Pipeline
  pipeline_f = Pipeline(stages = [indexer_f, oh_encoder_f, va_f, scaler_f, va2_f])

  df_final_f = pipeline_f.fit(df_renamed_f).transform(df_renamed_f).select("features", "label")

  # Convert to RDD and tensors
  fraud_rdd = df_final_f.rdd.map(tuple).map(lambda v: (list(v[0]), int(v[1])))
  
  return fraud_rdd


from toolz.itertoolz import partition_all

batch_size=2000

def get_batched_iterator(input_rdd):
  #return input_rdd.mapPartitions(lambda partition: partition_all(batch_size, partition)).toLocalIterator()
  return input_rdd.mapPartitions(lambda partition: partition_all(batch_size, partition))

# COMMAND ----------

import torch
import torch.nn as nn
import torch.optim as optim
import random


#Feedforward network for classification
class MLP(nn.Module):
  
  def __init__(self,shape):
    #shape: number of neurons in each layer (including the input and output layers)
    super(MLP,self).__init__()
    
    self.units=nn.ModuleList()
    for i in range(len(shape)-1):
      self.units.append(nn.Linear(shape[i],shape[i+1]))
    
    self._shape=shape
    self._nlayers=len(shape)
  
  def forward(self,x):
    
    y=x
    
    for i,layer in enumerate(self.units):
      if i<self._nlayers-2:
        y=nn.functional.tanh(layer(y))
      else:
        y=nn.functional.softmax(layer(y),dim=1)
    
    return y
  
  # Suggestion for helper function / Jakob
  @staticmethod
  def from_state_dict(state_dict, net_shape):
      net=MLP(net_shape)
      net.load_state_dict(state_dict)
      return net

# COMMAND ----------

#The main training function (is run on driver nodes).
def Train(net_params,net_shape,x,y):
#net_params,net_shape: initial parameters and shape of the feedforward network 
#x,y: training data

  numepochs=50
  batchsize=10
  
  ndata=x.shape[0]
  
  net=MLP.from_state_dict(net_params,net_shape)
  
  opt=optim.Adam(net.parameters())
  loss=nn.CrossEntropyLoss()
  
  for i in range(numepochs):
    for j in range(ndata//batchsize):
      
      opt.zero_grad()
      
      k1=j*batchsize
      k2=min((j+1)*batchsize,ndata)
      xb=x[k1:k2,:]
      yb=y[k1:k2]
      
      yhat=net(xb)
      err=loss(yhat,yb)
      err.backward()
  
      opt.step()
  
  err=loss(net(x),y)
  lossval=float(err.detach().numpy())
  
  #returns parameters of the trained network, network shape, and loss
  return (net.state_dict(),net_shape,lossval)

# COMMAND ----------

def Totensor(d):
  x=[v[0] for v in d]
  y=[v[1] for v in d]
  x=torch.tensor(x,dtype=torch.float)
  y=torch.tensor(y,dtype=torch.long)
  
  return (x,y)

# COMMAND ----------

n_models=5 #ensemble size
model_data=[] #pairs of model parameters and their training data
shapes=[] #shape of networks
inputdims=11 #features dimensions
nclasses=4 #number of classes

dataiterator=get_batched_iterator(load_firewall_data())
dataiterator=dataiterator.map(Totensor).toLocalIterator()

#initialization
for i in range(n_models):
  
  #pick random number of hidden layers and neurons for each network
  nhidden=random.randint(1,4)
  shape=[inputdims]
  for k in range(nhidden):
    shape.append(random.randint(5,15))
  shape.append(nclasses)
  
  net=MLP(shape)
  shapes.append(shape)
  
  #-data loading
  d=next(dataiterator)
  #x=[v[0] for v in d]
  #y=[v[1] for v in d]
  x=d[0]
  y=d[1]
  #x=torch.tensor(x,dtype=torch.float)
  #y=torch.tensor(y,dtype=torch.long)
  #print shapes of input and target tensors
  print(x.shape)
  print(y.shape)
  #-
  
  #model_data.append((net.state_dict(),shape,x,y))
  model_data.append((net.state_dict(),x,y))

#distribute the array
model_data_par= sc.parallelize(model_data)
#main training map
models_trained= model_data_par.map(lambda t: Train(*t))

#models_trained_c=models_trained.collect()
#print("losses:")
#print([t[1] for t in models_trained_c])

# COMMAND ----------

# MAGIC %md
# MAGIC ## MLP with auto-inferred `shapes` param
# MAGIC 
# MAGIC Not a big change, but it is nice to be able to create models from a list of paths to state dicts -- saved to disk -- without having to know the shapes a priori.
# MAGIC 
# MAGIC The important changes are:
# MAGIC - added static methods in `MLP` class
# MAGIC - `Train` function removes the `shapes` argument.

# COMMAND ----------

import torch
import torch.nn as nn
import torch.optim as optim
import random
from toolz.itertoolz import cons

#Feedforward network for classification
class MLP(nn.Module):
  
  def __init__(self,shape):
    #shape: number of neurons in each layer (including the input and output layers)
    super(MLP,self).__init__()
    
    self.units=nn.ModuleList()
    for i in range(len(shape)-1):
      self.units.append(nn.Linear(shape[i],shape[i+1]))
    
    self._shape=shape
    self._nlayers=len(shape)
  
  def forward(self,x):
    
    y=x
    
    for i,layer in enumerate(self.units):
      if i<self._nlayers-2:
        y=nn.functional.tanh(layer(y))
      else:
        y=nn.functional.softmax(layer(y),dim=1)
    
    return y
  
  @staticmethod
  def from_state_dict(state_dict):
    net_shape = MLP.shape_from_state_dict(state_dict)
    net=MLP(net_shape)
    net.load_state_dict(state_dict)
    return net

  @staticmethod
  def shape_from_state_dict(state_dict):
    """Infer MLP layer shapes from state_dict"""
    iter_ = iter(state_dict.items())
    _, input_size = next(iter_)
    bias_tensors = filter(lambda key_val: key_val[0].find("bias") != -1, iter_)
    shapes = map(lambda key_val: key_val[1].size(0), bias_tensors)
    return list(cons(input_size.size(1), shapes))

# COMMAND ----------

class DataSet(torch.utils.data.Dataset):
  def __init__(self, x, y):
    self.x = x
    self.y = y

  def __len__(self):
    return self.x.shape[0]

  def __getitem__(self, ind):
    x = self.x[ind]
    y = self.y[ind]

    return x, y

# COMMAND ----------

#The main training function (is run on driver nodes).
def Train(net_params,x,y):
#net_params: initial parameters of the feedforward network 
#x,y: training data

  n_epochs=400
  batchsize=10
  
  net=MLP.from_state_dict(net_params)
  
  train_data = DataSet(x, y)
  dataloader = torch.utils.data.DataLoader(train_data, batch_size=batchsize)
  
  opt=optim.Adam(net.parameters())
  loss=nn.CrossEntropyLoss()

  for i in range(n_epochs):
    for batch in dataloader:
      
      opt.zero_grad()
      
      xb,yb=batch
      
      yhat=net(xb)
      err=loss(yhat,yb)
      err.backward()
  
      opt.step()
  
  err=loss(net(x),y)
  lossval=float(err.detach().numpy())
  
  #returns parameters of the trained network and loss
  return (net.state_dict(),lossval)


def Predict(net_params, x):
  net = MLP.from_state_dict(net_params)
  return net(x)