<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Distributed ensembles
=====================

*Amanda Olmin, Amirhossein Ahmadian and Jakob Lindqvist*

[Video presentation](https://www.youtube.com/watch?v=zbYewn3nDtk)

</div>

<div class="cell markdown">

Python version: python 3.7

**Library dependencies** - PySpark - PyTorch - toolz - matplotlib

</div>

<div class="cell markdown">

Introduction
------------

In this project, we create a distributed ensemble of neural networks that we can train and make predictions with in a distributed fashion, and we also apply this model to the out-of-distribution detection problem \[2\] (detecting inputs that are highly dissimilar from the training data).

#### Why ensembles?

Ensembles of neural networks - often have better predictive performance than single ensemble members \[1\] - have shown to provide reliable uncertainty estimates

The latter quality is beneficial in itself but is especially useful when it comes to tasks such as out-of-distribution detection, where a model’s uncertainty estimates can be used to determine if a sample is in-distribution or not. We demonstrate this in the experiments below.

#### Distributed ensembles

In Spark, it is common to distribute *data* over several worker nodes. In this way, the same function is performed on several nodes on different parts of the data. The result from each node is then communicated and aggregated to a final function output. Similarily, we can train a neural network (a single ensemble member) in a distributed way by distributing the data that we use to train it. This can for example be done using the built-in MLP and MLPC classes in Pyspark \[3\]. However, this approach requires continuous communication between nodes to update model weights (possibly at every iteration) since every node keeps its own version of the model weights. The approach therefore scales badly as - the number of model parameters grow (more information to communicate between nodes) - when the complexity of the training algorithm increases, e.g. we wish to use a stochastic training algorithm

In this regard, the communication becomes a bottleneck. Asynchronous updating can reduce the amount of communication, but might also hurt model performance \[4\].

Considering that the ensemble members are independent models, they never need to communicate during the training phase. Hence, training ensemble members in a way that requires the otherwise independent training processes to integrate or synchronize, would cause unnecessary costs, for example since the training processes all need to communicate through the driver node. The same holds for prediction; no communication is needed between ensemble members except at the very end when the predictions are aggregated.

To avoid unnecessary communication, we distribute the *ensemble members* and train them on separate worker nodes such that we - are able to train several ensemble members in parallell (limited by the number of nodes in our cluster) and independently - avoid communication between worker nodes

To achieve this, we implement our own training processes below. In addition, we implement our own MLP class with the help of PyTorch. MLP objects and their training data are then distributed on worker nodes using Spark. This is not only to avoid distributing the training data over several nodes during training but also to package the ensemble members in a way that makes it possible for us to send them between the driver and the worker nodes prior to and at the end of training.

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/8_4.png?raw=true)

</div>

<div class="cell markdown">

Imports
-------

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
from random import randrange
import random
from pathlib import Path
# External libs added to cluster
from pyspark.mllib.random import RandomRDDs
from pyspark.ml.feature import StringIndexer, OneHotEncoder, StandardScaler, VectorAssembler
from pyspark.ml import Pipeline
from pyspark.rdd import PipelinedRDD
from toolz.itertoolz import partition_all
from toolz.itertoolz import cons
import numpy as np
import torch
import torch.nn as nn
import torch.optim as optim
from matplotlib import pyplot as plt
```

</div>

<div class="cell markdown">

Data
----

We introduce the functions that we use to load the data for the experiments that we conduct. We split the available training data between ensemble members using sampling with or without replacement. The number of training data points that we can distribute to each ensemble member is only limited by the memory available to each worker node.

</div>

<div class="cell markdown">

**TOY DATA**

We create a function for generating data consisting of Gaussian clusters. The function takes as input, user defined means and variances for each cluster in the data as well as the total number of observations and a vector of intended class proportions. It also comes with an option to split the final RDD into train and test sets.

We will use this data later on to demonstrate our distributed ensembles framework as well as to generate out-of-distribution data for OOD detection.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
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
```

</div>

<div class="cell markdown">

**FIRE WALL DATA**

We will also consider some real data. The dataset that we will use consits of traffic from a firewall tracking record. We have accessed it through the UCI Machine Learning repository \[4\]: https://archive.ics.uci.edu/ml/datasets/Internet+Firewall+Data.

-   Number of data points: 65,532.

-   Number of features: 11 (all numerical).

-   Number of classes: 4 (allow/deny/drop/reset both).

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
def load_firewall_data(train_test_split=False,file_location="/FileStore/shared_uploads/amanda.olmin@liu.se/fire_wall_data.csv"):
  """Load and preprocess firewall data
  Args:
     file_location: file location from which to load the data
     train_test_split: whether to split the data into train/test sets or not
  
  Returns:
     Firewall data, RDD of tuples (list(features), int(label))
  """
  
  # File location and type
  # file_location = "/FileStore/shared_uploads/amanda.olmin@liu.se/fire_wall_data.csv" 
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
```

</div>

<div class="cell markdown">

\*\* RDD partition \*\*

Below, we provide a function that partitions an RDD. We will use it to distribute data between ensemble members.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
def get_partitioned_rdd(input_rdd, partition_size=1000):
  """Partition RDD 
  
  Args:
    input_rdd: RDD to be partitioned
  
  Returns:
    Partitioned RDD
  """
  
  return input_rdd.mapPartitions(lambda partition: partition_all(partition_size, partition))
```

</div>

<div class="cell markdown">

Distributed ensemble of neural networks
---------------------------------------

</div>

<div class="cell markdown">

**PyTorch Model**

</div>

<div class="cell markdown">

To implement the ensemble members, we first write an ordinary feedforward (MLP) neural network class using PyTorch, which has a Softmax output and Tanh activation functions. The number of layers and neurons in each layer is passed as an argument to the constructor of this class. Moreover, any instance of this network class (parameters and structure) can be easily stored in and loaded from a state dictionary (state\_dict) object.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
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
  
  #constructing an instance of this class based on a state dictionary (network parameters)
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
```

</div>

<div class="cell markdown">

**Functions for training and testing networks**

</div>

<div class="cell markdown">

Here we have some functions that are used to train/test each individual network in the ensemble. The *Train* function takes the initial weights of a network, trains it on a set of input-taraget data based on stochastic gradient optimization and cross-entropy loss, and returns the state dictionary of the trained network. PyTorch's backpropagation and optimization tools are used to implement this function as usual. The *Predict* function simply takes the state dictionary corresponding to a network as well as a data point (or batch of data), and returns the output (probabilities) of the network at that point.

We note that Spark can automatically distribute these functions on the nodes, and thus writing them for a distributed ensemble is not basically different from a local setup.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
#utility class for pytorch data loader
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

#The main training function (is run on worker nodes)
def Train(net_params,x,y):
  #net_params: initial parameters of the feedforward network (state dictionary) 
  #x,y: training data (pytorch tensors)

  n_epochs=100
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

#Get the output of a feedforward network given an input tensor
def Predict(net_params, x):
  #net_params: parameters (state dictionary) of the network
  #x: input (pytorch tensor)
  net = MLP.from_state_dict(net_params)
  net.eval()
  return net(x)

#Reshaping and converting the tuples stored in a dataset RDD into input and target tensors
def Totensor(d):
  #d: the dataset (list of tuples)
  
  x=[v[0] for v in d]
  y=[v[1] for v in d]
  x=torch.tensor(x,dtype=torch.float)
  y=torch.tensor(y,dtype=torch.long)
  
  return (x,y)

def make_prediction(state_dict, x):
  print(state_dict)
  return Predict(state_dict, x)
```

</div>

<div class="cell markdown">

**Creating an ensemble of networks, and training them in parallel**

</div>

<div class="cell markdown">

We now use the class and functions defined above to create an ensemble of feedforward neural networks, and train it in a distributed fashion, where each network is trained on a single worker independently from the other ones. Firstly, several networks are initialized using the MLP class with a random number of hidden layers and neurons, and random initial weights. Using randomness helps to increase the diversity in the ensemble (without which, the outputs of ensemble members could get correlated with each other).

As mentioned before, the training data is partioned into equal size parts, and each of the networks in the ensemble is assigned one part. Since the dataset is assumed to be an RDD (to let it be huge), an iterator object is needed which collects one part of the data RDD (transfers it from the cloud to the driver node) in each call. Note that we here implicitly assume that each part of the data (but not the whole dataset) fits into the memory of a single machine.

After constructing the network object and loading data for each member of the ensemble, the state dictionary of the network and its corresponding training data are packed into a tuple, and appended to a list. The list of state\_dict/data tuples is then parallelized to obtain an Spark RDD. We found out that it is difficult to directly put the PyTorch neural network objects in an RDD, apparently becasue Spark does not know by default how to encode these objects and transfer them between nodes. Therefore, we use the state dictionary instead, which contains all the necessary information about a network.

Finally, the network training function (*Train* defined above) is applied to each element of the model/data RDD, in the form of a *map* operation. This tells Spark to run the function on each element in parallel (on worker machines) independently.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
def train_ensemble(n_models, inputdims, nclasses, max_layers, min_neurons, max_neurons, data_iterator):
  """Constructing and training a distributed ensemble of feedforward networks
  
    Args:
      n_models: number of the ensemble memebrs
      inputdims: number of features dimesnions
      nclasses: number of the classes
      max_layers: maximum allowed number of hidden layers for the networks
      min_neurons,max_neurons: the valid range for the number of neurons in each hidden layer
      data_iterator: a Python iterator over the parts of the training data (one part per each member of the ensemble)

    Returns: a list of state dictionaries of the trained networks
  """
  
  # initialization
  model_data=[] # pairs of model parameters and their training data
  for i in range(n_models):
    # pick random number of hidden layers and neurons for each network
    nhidden=random.randint(1, max_layers)
    shape=[inputdims]
    for k in range(nhidden):
      shape.append(random.randint(min_neurons, max_neurons))
    shape.append(nclasses)
    
    net=MLP(shape)
    
    #fetch the next part of data
    d=next(data_iterator)
    x=d[0]
    y=d[1]
  
    model_data.append((net.state_dict(),x,y))

  # distribute the array
  model_data_par= sc.parallelize(model_data)
  # execute the train function on the worker nodes
  models_trained = model_data_par.map(lambda t: Train(*t))
  
  #transfer the trained models and loss values to the driver
  models_trained=models_trained.collect()
  
  #print the training loss values
  print("training losses:")
  print([v[1] for v in models_trained])

  # return the state dicts
  return [v[0] for v in models_trained]
```

</div>

<div class="cell markdown">

\*\* Utility functions for saving and loading the ensemble model from the disk \*\*

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
def save_models_distr(models, dir_, model_names=None):
  dir_ = Path(dir_)
  dir_.mkdir(exist_ok=True, parents=True)
  
  if model_names is None:
    model_names = [f"m{idx}.pt" for idx in range(0, models.count())]
  assert len(model_names) == models.count()
  model_paths = [dir_ / model_name for model_name in model_names]
  model_paths = sc.parallelize(model_paths)
  models.zip(model_paths).foreach(lambda dict_and_path: torch.save(*dict_and_path))

def save_models(models, dir_, model_names=None):
  dir_ = Path(dir_)
  dir_.mkdir(exist_ok=True, parents=True)
  
  if model_names is None:
    model_names = [f"m{idx}.pt" for idx in range(0, len(models))]
  assert len(model_names) == len(models)
  model_paths = [dir_ / model_name for model_name in model_names]
  for state_dict, path in zip(models, model_paths):
    torch.save(state_dict, path)
  
def load_models(model_names, dir_):
  dir_ = Path(dir_)
  model_paths = [dir_ / model_name for model_name in model_names]
  state_dicts = [torch.load(path) for path in model_paths]
  return sc.parallelize(state_dicts)
```

</div>

<div class="cell markdown">

Distributed ensembles prediction API
====================================

From the training process we get a distributed iterator `models` over the trained models. (NB. the `train_ensemble` function actually collects the trained models for convenience.) Internally this is an iterator over `torch.state_dicts` holding the param's of each model respectively.

There are different ways in which we can do predictions:

-   Distributed predictions with `ens_preds(models, test_x)`, which maps the combined model and test data to predictions for each data point. This iterator can be collected to a list of the predictions for each ensemble member, or further processed in a distributed and functional manner. This is the most flexible variant since it preserves the prediction of every member on every datapoint. It is also the most expensive (if we do collect all the data).

-   Reduced/aggregated predictions with `ens_preds_reduced(models, test_x, red_fn)`. Working with an ensemble, we are often concerned with some aggregate of the members' predictions, eg., the average prediction. For this we provide an reducing version of `ens_preds` where the user need only supply the reduce function `red_fn`, describing how to combine the predictions of two ensemble members. For instance, if you would like to get the average probability vector of a classifier ensemble for every data point you would use:

    ``` python
    avg_prob_vecs = ens_preds_reduced(models, x, lambda x, y: (x+y)/2)
    ```

    Internally, this simply calls `.reduce(red_fn)` on the iterator returned from `ens_preds`. This is merely a convenience function.

-   Metrics per ensemble member. If the number of test samples is large, we will collect a lot of predictions over the cluster. If we know that we only want an aggregate metric for each member across the whole test data, we use the `ens_metrics` method for aggregation on the worker nodes.

    ``` python
    avg_acc_per_member = ens_metrics(models, test_input, test_true_labels, <list of metric functions>)
    ```

    Note that each metric function must be on the form: f: R^(N x D\_x) x R^(N) --\> T

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
def ens_preds(models, test_x):
  """Distributed ensemble predictions
  Takes a set of models and test data and makes distributed predictions
  Let N := number of data points and D_x := the dimension of a single datapoint x
  
  Args:
    models (list[state_dict]): set of models represented as a list (state_dict, shape)
    test_x (torch.Tensor): Tensor of size (N, D_x)
  
  Returns:
    Distributed iterator over the predictions. E.g. an iterator over probability vectors in the case of a classifier ens.
  """
  pred_iter = _pred_models_iter(models, test_x)
  return pred_iter.map(lambda t: Predict(*t))

def ens_preds_reduced(models, test_x, red_fn):
  """Reduced/aggregated ensemble predictions
  Takes a set of models and test data and makes distributed predictions and reduces them with a provided `red_fn`
  Let N := number of data points and D_x := the dimension of a single datapoint x
  
  Args:
    models (list[state_dict]): set of models represented as a list (state_dict, shape)
    test_x (torch.Tensor): Tensor of size (N, D_x)
    red_fn function: f: R^D_x x R^D_x --> R^D_x
  
  Returns:
    Single reduced/aggregated prediction of the whole ensemble
  """
  return ens_preds(models, test_x).reduce(red_fn)

def ens_metrics(models, test_x, test_y, metrics):
  """Distributed ensemble metrics
  Takes a set of models and test data, predicts probability vectors and calculates the provided metrics
  given true labels `test_y`
  Let N := number of data points and D_x := the dimension of a single datapoint x
  
  Args:
    models (list[state_dict]): set of models represented as a list (state_dict, shape)
    test_x (torch.Tensor): Tensor of size (N, D_x)
    test_y (torch.Tensor): Tensor of size (N). NB: hard labels
    metrics (list[functions]): List of functions where each funcion f: R^(N x D_x) x R^(N) --> T, where T is a generic output type.
  """
  return ens_preds(models, test_x).map(lambda prob_vecs: [metric(prob_vecs, test_y) for metric in metrics])


def _pred_models_iter(models, test_x):
  """Helper function to generate a distributed iterator over models and test data
  NB: the same `test_x` is given to all elements in the iterator
  
  Args:
    models (list[state_dict]): set of models represented as a list (state_dict, shape)
    test_x (torch.Tensor): Tensor of size (N, D_x)
  """
  if isinstance(models, PipelinedRDD):
    return models.map(lambda model: (model, test_x))
  elif isinstance(models, list):
    models_and_data = [(params, test_x) for params in models]
    return sc.parallelize(models_and_data)
  else:
    raise TypeError("'models' must be an RDD or a list")

def avg_accuracy(prob_vecs, labels):
  """Example metrics function: average accuracy
  Let N := number of data points and C := the number of classes
  
  Args:
    prob_vecs (torch.Tensor): Tensor of size (N, C)
    labels (torch.Tensor): Tensor of size (N), hard labels, with classes corresponding to indices 0, ..., C-1
  
  Returns:
    torch.Tensor: Tensor of size (N), average accuracy over all datapoints.
  """
  hard_preds = torch.argmax(prob_vecs, 1)
  return (hard_preds == labels).float().mean()

def entropy(prob_vecs):
  return - (prob_vecs * torch.log(prob_vecs)).sum(1)

def avg_entropy(prob_vec_1, prob_vec_2):
  e_1 = entropy(prob_vec_1)
  e_2 = entropy(prob_vec_2)
  return (e_1 + e_2)
```

</div>

<div class="cell markdown">

Application example: Distributed predictions
============================================

Let's first demonstrate our distributed ensembles with a simple toy example. We'll create gaussian toy data with three slightly overlapping clusters:

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
means = np.array([(0, 0), (1,0), (1, 1)])
variances = 0.1 * np.ones((3, 2))
num_observations = 5000
class_proportions = np.array([1/3, 1/3, 1/3])
data_train, data_test = create_gaussian_RDD(means, variances, num_observations, class_proportions, train_test_split=True)
```

</div>

<div class="cell markdown">

Now we'll create and distributedly train a classifier ensemble and save it to file. This is not necessary, we can -- in fact -- make predictions with the trained ensemble without ever collecting it from the worker nodes, but in most use cases it will be convenient to save the ensemble on disk.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
data_iterator=get_partitioned_rdd(data_train).map(Totensor).toLocalIterator()
n_models=5 # ensemble size
inputdims=2 # features dimensions
nclasses=3 # number of classes
max_layers=2
min_neurons=2
max_neurons=5

models_trained = train_ensemble(n_models, inputdims, nclasses, max_layers, min_neurons, max_neurons, data_iterator)
saved_models_dir = Path("saved_models/gaussian")
save_models(models_trained, saved_models_dir)
```

<div class="output execute_result plain_result" execution_count="1">

    training losses:
    [0.6597320437431335, 0.6314507126808167, 0.6506103277206421, 0.6424266695976257, 0.657072901725769]

</div>

</div>

<div class="cell markdown">

Making distributed predictions
------------------------------

With the trained ensemble we can make predictions and calculate metrics, all in a distributed manner.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
test_xx, test_yy = Totensor(data_test.collect())
model_names = [f"m{idx}.pt" for idx in range(n_models)]
models = load_models(model_names, saved_models_dir).collect()
avg_prob_vecs = ens_preds_reduced(models, test_xx, lambda x, y: (x+y)/2) # (A single) Average prob. vec for all data points.
avg_acc = ens_metrics(models, test_xx, test_yy, [avg_accuracy]).collect() # Average acc. for each ens. over all data points

print(f"Average accuracy for each ensemble member: {[acc[0].item() for acc in avg_acc]}")
print(f"Average accuracy for the whole ensemble: {avg_accuracy(avg_prob_vecs, test_yy).item()}")
```

<div class="output execute_result plain_result" execution_count="1">

    Average accuracy for each ensemble member: [0.9240759015083313, 0.9240759015083313, 0.9160839319229126, 0.9110888838768005, 0.9220778942108154]
    Average accuracy for the whole ensemble: 0.9210789203643799

</div>

</div>

<div class="cell markdown">

We can also make use of the uncertainty description provided by the ensemble. We'll plot the test data, each point coloured the predicted distribution, which will illustrate the certain predictions with distinct colur and uncertain with muddied colours.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
preds = avg_prob_vecs.detach().numpy()
hard_preds = avg_prob_vecs.argmax(1).detach().numpy()
every_nth = 5
train_xx, train_yy = Totensor(data_train.collect())

(fig, (ax_1, ax_2)) = plt.subplots(1, 2)
# For the train data we use the true labels to simulate a completely certain prediction.
color_map = {0: [1, 0 ,0], 1: [0, 1, 0], 2: [0, 0, 1]}
ax_1.scatter(train_xx[:, 0], train_xx[:, 1], c=[color_map[class_.item()] for class_ in train_yy], label="Train")
ax_2.scatter(test_xx[::every_nth, 0], test_xx[::every_nth, 1], c=preds[::every_nth], label="Test")
ax_1.set_title("Train")
ax_2.set_title("Test")
plt.show()
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/8_1.JPG?raw=true)

</div>

<div class="cell markdown">

Application example: Out of distribution detection
==================================================

Our distributed ensemble can be used for out of distribution (OOD) detection. A simple way is to measure the entropy of the combined ensemble prediction; high entropy signals weird data, not seen in the training distribution.

"Real world" out of distribution data can be hard to come by, but a typical example would be images in different contexts. E.g. scenic vistas or pathology scans may share the same feature space but have very different distribution. For the data we have collected, no such OOD set exists, so we will showcase it with an OOD set of gaussian noise. Of course, noise that is very far from the in distribution (ID) data will saturate the classifiers softmax for one element, actually yielding very confident, low entropy, nonsense predictions.

Regardless, let's see how to do this with the distributed ensemble. First, we train it and again, save the trained parameters to file

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
data_train, data_test = load_firewall_data(True)
data_iterator=get_partitioned_rdd(data_train).map(Totensor).toLocalIterator()

n_models=10
models_trained=train_ensemble(n_models,
                              inputdims=11,
                              nclasses=4,
                              max_layers=4,
                              min_neurons=5,
                              max_neurons=15,
                              data_iterator=data_iterator) 
saved_models_dir = Path("saved_models/firewall")
save_models(models_trained, saved_models_dir)
```

<div class="output execute_result plain_result" execution_count="1">

    training losses:
    [0.7487057447433472, 0.750811755657196, 0.7578539252281189, 0.7506877183914185, 0.7623474597930908, 0.7521470785140991, 0.7517384886741638, 0.7481321692466736, 0.750828742980957, 0.7547193765640259]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
def gen_ood_data(test_x, num_samples):
  num_test_samples, dim_x = test_x.size()
  random_mean = np.random.rand(dim_x).reshape(1, dim_x)
  random_cov = np.random.rand(dim_x).reshape(1, dim_x) * 10
  ood_x, _ = Totensor(create_gaussian_RDD(random_mean, random_cov, num_test_samples, np.array([1.0]), train_test_split=False).collect())
  return ood_x

  
data = data_test.collect()
batch_size = -1
batch = data[0:batch_size]
test_xx, test_yy = Totensor(batch)
ood_x = gen_ood_data(test_xx, batch_size)

models_p = load_models(model_names, saved_models_dir).collect()

# We can either calculate the average entropy of the ensemble members
avg_entropy_id = ens_preds(models_p, test_xx).map(entropy).reduce(lambda x, y: (x+y)/2).detach().numpy()
avg_entropy_ood = ens_preds(models_p, ood_x).map(entropy).reduce(lambda x, y: (x+y)/2).detach().numpy()

# ... or we the entropy of the average ensemble prediction.
entropy_avg_id = entropy(ens_preds_reduced(models_p, test_xx, lambda x, y: (x+y)/2)).detach().numpy()
entropy_avg_ood = entropy(ens_preds_reduced(models_p, ood_x, lambda x, y: (x+y)/2)).detach().numpy()

# Set entropy measure
entropy_id = avg_entropy_id
entropy_ood = avg_entropy_ood
```

</div>

<div class="cell markdown">

**Comparison of the entropy of the ensemble classifier on in-distribution and OOD data**

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
def entropy_hist(id_, ood, n_bins, upper_x_bound):
  (fig, (ax_1, ax_2)) = plt.subplots(2, 1)
  _plot_hist(ax_1, id_, n_bins, "ID", "b", upper_x_bound)
  _plot_hist(ax_2, ood, n_bins, "OOD", "r", upper_x_bound)
  fig.suptitle("Entropy histogram")
  ax_2.set_xlabel("entropy")
  plt.show()

def _plot_hist(ax, counts, n_bins, label, color, upper_x_bound):
    ax.hist(counts, bins=n_bins, label=label, color=color, density=True)
    ax.set_xbound(lower = 0.0, upper = upper_x_bound)
    ax.set_ylabel("rel freq")
    ax.legend()
  
n_bins = 100
entropy_bound = 0.15
entropy_hist(entropy_id, entropy_ood, n_bins, entropy_bound)
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/8_2.JPG?raw=true)

</div>

<div class="cell markdown">

**Evaluation of the OOD detection in terms of ROC curve and area under this curve (AUROC)**

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
def is_ood(entropies, cut_off_entropy):
  return entropies > cut_off_entropy

def fpr_and_tpr(id_, ood, res):
  max_entropy = max(id_.max(), ood.max())
  # max_entropy = id_.max()
  thresholds = np.arange(0.0, max_entropy, max_entropy / res)
  roc = np.array([(fpr(id_, th), tpr(ood, th)) for th in thresholds])
  roc = roc[roc[:,0].argsort()]
  fprs, tprs = (roc[:, 0], roc[:, 1])
  return fprs, tprs

def fpr(id_, th):
  id_pred = is_ood(id_, th)
  fp = id_pred.sum()
  tn = id_pred.shape[0] - fp
  return fp / (tn + fp)

def tpr(ood, th):
  ood_pred = is_ood(ood, th)
  tp = ood_pred.sum()
  fn = ood_pred.shape[0] - tp
  return tp / (tp + fn)

fpr, tpr = fpr_and_tpr(avg_entropy_id, avg_entropy_ood, res = 100)
(fig, ax) = plt.subplots()

ax.plot(fpr, tpr)
ax.set_xlabel("FPR")
ax.set_ylabel("TPR")
ax.set_title("ROC")
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/8_3.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
print(f"AUROC: {np.trapz(tpr, fpr)}")
```

<div class="output execute_result plain_result" execution_count="1">

    AUROC: 0.6480554090292237

</div>

</div>

<div class="cell markdown">

References
----------

\[1\] Lakshminarayanan, B., Pritzel, A., & Blundell, C. (2017). Simple and scalable predictive uncertainty estimation using deep ensembles. In Advances in neural information processing systems (pp. 6402-6413).

\[2\] Ovadia, Y., Fertig, E., Ren, J., Nado, Z., Sculley, D., Nowozin, S., ... & Snoek, J. (2019). Can you trust your model's uncertainty? Evaluating predictive uncertainty under dataset shift. In Advances in Neural Information Processing Systems (pp. 13991-14002).

\[3\] Apache Spark. (2021, 01, 11). Classification and Regression \[https://spark.apache.org/docs/latest/ml-classification-regression.html\].

\[4\] Chen, J., Pan, X., Monga, R., Bengio, S., & Jozefowicz, R. (2016). Revisiting distributed synchronous SGD. arXiv preprint arXiv:1604.00981.

\[5\] Dua, D. and Graff, C. (2019). UCI Machine Learning Repository \[http://archive.ics.uci.edu/ml\]. Irvine, CA: University of California, School of Information and Computer Science.

</div>
