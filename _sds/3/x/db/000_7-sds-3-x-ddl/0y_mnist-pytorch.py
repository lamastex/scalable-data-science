# Databricks notebook source
# MAGIC %md
# MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)
# MAGIC 
# MAGIC The following is from databricks blog with minor adaptations with help from Tilo Wiklund.

# COMMAND ----------

# MAGIC %md # Distributed deep learning training using PyTorch with HorovodRunner for MNIST
# MAGIC This notebook demonstrates how to train a model for the MNIST dataset using PyTorch. 
# MAGIC It first shows how to train the model on a single node, and then shows how to adapt the code using HorovodRunner for distributed training. 
# MAGIC 
# MAGIC **Requirements**
# MAGIC 
# MAGIC * This notebook runs on CPU or GPU clusters.
# MAGIC * To run the notebook, create a cluster with
# MAGIC  - Two workers

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC ## Cluster Specs on databricks
# MAGIC 
# MAGIC Run on `tiny-debug-cluster-(no)gpu` or another cluster with the following runtime specifications with CPU/non-GPU and GPU clusters, respectively:
# MAGIC 
# MAGIC 
# MAGIC * Runs on non-GPU cluster with 3 (or more) nodes on 7.4 ML runtime (nodes are 1+2 x m4.xlarge)
# MAGIC * Runs on GPU cluster with 3 (or more) nodes on 7.4 ML GPU runtime (nodes are 1+2 x g4dn.xlarge)
# MAGIC 
# MAGIC You do not need to "install" anything else in databricks as everything needed is pre-installed in the runtime environment on the right nodes.

# COMMAND ----------

# MAGIC %md ## Set up checkpoint location
# MAGIC The next cell creates a directory for saved checkpoint models. Databricks recommends saving training data under `dbfs:/ml`, which maps to `file:/dbfs/ml` on driver and worker nodes.

# COMMAND ----------

PYTORCH_DIR = '/dbfs/ml/horovod_pytorch'

# COMMAND ----------

# MAGIC %md ## Prepare single-node code
# MAGIC 
# MAGIC First you need to have working single-node PyTorch code. This is modified from   [Horovod's PyTorch MNIST Example](https://github.com/horovod/horovod/blob/master/examples/pytorch/pytorch_mnist.py).

# COMMAND ----------

# MAGIC %md ### Define a simple convolutional network

# COMMAND ----------

import torch
import torch.nn as nn
import torch.nn.functional as F

class Net(nn.Module):
    def __init__(self):
        super(Net, self).__init__()
        self.conv1 = nn.Conv2d(1, 10, kernel_size=5)
        self.conv2 = nn.Conv2d(10, 20, kernel_size=5)
        self.conv2_drop = nn.Dropout2d()
        self.fc1 = nn.Linear(320, 50)
        self.fc2 = nn.Linear(50, 10)

    def forward(self, x):
        x = F.relu(F.max_pool2d(self.conv1(x), 2))
        x = F.relu(F.max_pool2d(self.conv2_drop(self.conv2(x)), 2))
        x = x.view(-1, 320)
        x = F.relu(self.fc1(x))
        x = F.dropout(x, training=self.training)
        x = self.fc2(x)
        return F.log_softmax(x)

# COMMAND ----------

# MAGIC %md ###Configure single-node training

# COMMAND ----------

# Specify training parameters
batch_size = 100
num_epochs = 5
momentum = 0.5
log_interval = 100

# COMMAND ----------

def train_one_epoch(model, device, data_loader, optimizer, epoch):
    model.train()
    for batch_idx, (data, target) in enumerate(data_loader):
        data, target = data.to(device), target.to(device)
        optimizer.zero_grad()
        output = model(data)
        loss = F.nll_loss(output, target)
        loss.backward()
        optimizer.step()
        if batch_idx % log_interval == 0:
            print('Train Epoch: {} [{}/{} ({:.0f}%)]\tLoss: {:.6f}'.format(
                epoch, batch_idx * len(data), len(data_loader) * len(data),
                100. * batch_idx / len(data_loader), loss.item()))

# COMMAND ----------

# MAGIC %md ### Prepare log directory

# COMMAND ----------

from time import time
import os

LOG_DIR = os.path.join(PYTORCH_DIR, str(time()), 'MNISTDemo')
os.makedirs(LOG_DIR)

# COMMAND ----------

# MAGIC %md ### Create method for checkpointing and persisting model

# COMMAND ----------

def save_checkpoint(model, optimizer, epoch):
  filepath = LOG_DIR + '/checkpoint-{epoch}.pth.tar'.format(epoch=epoch)
  state = {
    'model': model.state_dict(),
    'optimizer': optimizer.state_dict(),
  }
  torch.save(state, filepath)

# COMMAND ----------

# MAGIC %md ### Run single-node training with PyTorch

# COMMAND ----------

import torch.optim as optim
from torchvision import datasets, transforms

def train(learning_rate):
  device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')

  train_dataset = datasets.MNIST(
    'data', 
    train=True,
    download=True,
    transform=transforms.Compose([transforms.ToTensor(), transforms.Normalize((0.1307,), (0.3081,))]))
  data_loader = torch.utils.data.DataLoader(train_dataset, batch_size=batch_size, shuffle=True)

  model = Net().to(device)

  optimizer = optim.SGD(model.parameters(), lr=learning_rate, momentum=momentum)

  for epoch in range(1, num_epochs + 1):
    train_one_epoch(model, device, data_loader, optimizer, epoch)
    save_checkpoint(model, optimizer, epoch)

# COMMAND ----------

# MAGIC %md
# MAGIC Run the `train` function you just created to train a model on the driver node.  

# COMMAND ----------

# Runs in  49.65 seconds on 3 node    GPU cluster
# Runs in 118.2 seconds on 3 node non-GPU cluster
train(learning_rate = 0.001)

# COMMAND ----------

# MAGIC %md
# MAGIC ## Migrate to HorovodRunner
# MAGIC 
# MAGIC HorovodRunner takes a Python method that contains deep learning training code with Horovod hooks. HorovodRunner pickles the method on the driver and distributes it to Spark workers. A Horovod MPI job is embedded as a Spark job using barrier execution mode.

# COMMAND ----------

import horovod.torch as hvd
from sparkdl import HorovodRunner

# COMMAND ----------

def train_hvd(learning_rate):
  
  # Initialize Horovod
  hvd.init()  
  device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
  
  if device.type == 'cuda':
    # Pin GPU to local rank
    torch.cuda.set_device(hvd.local_rank())

  train_dataset = datasets.MNIST(
    # Use different root directory for each worker to avoid conflicts
    root='data-%d'% hvd.rank(),  
    train=True, 
    download=True,
    transform=transforms.Compose([transforms.ToTensor(), transforms.Normalize((0.1307,), (0.3081,))])
  )

  from torch.utils.data.distributed import DistributedSampler
  
  # Configure the sampler so that each worker gets a distinct sample of the input dataset
  train_sampler = DistributedSampler(train_dataset, num_replicas=hvd.size(), rank=hvd.rank())
  # Use train_sampler to load a different sample of data on each worker
  train_loader = torch.utils.data.DataLoader(train_dataset, batch_size=batch_size, sampler=train_sampler)

  model = Net().to(device)
  
  # The effective batch size in synchronous distributed training is scaled by the number of workers
  # Increase learning_rate to compensate for the increased batch size
  optimizer = optim.SGD(model.parameters(), lr=learning_rate * hvd.size(), momentum=momentum)

  # Wrap the local optimizer with hvd.DistributedOptimizer so that Horovod handles the distributed optimization
  optimizer = hvd.DistributedOptimizer(optimizer, named_parameters=model.named_parameters())
  
  # Broadcast initial parameters so all workers start with the same parameters
  hvd.broadcast_parameters(model.state_dict(), root_rank=0)

  for epoch in range(1, num_epochs + 1):
    train_one_epoch(model, device, train_loader, optimizer, epoch)
    # Save checkpoints only on worker 0 to prevent conflicts between workers
    if hvd.rank() == 0:
      save_checkpoint(model, optimizer, epoch)

# COMMAND ----------

# MAGIC %md
# MAGIC Now that you have defined a training function with Horovod,  you can use HorovodRunner to distribute the work of training the model. 
# MAGIC 
# MAGIC The HorovodRunner parameter `np` sets the number of processes. This example uses a cluster with two workers, each with a single GPU, so set `np=2`. (If you use `np=-1`, HorovodRunner trains using a single process on the driver node.)

# COMMAND ----------

# Runs in 51.63 seconds on 3 node     GPU cluster
# Runs in 96.6  seconds on 3 node non-GPU cluster
hr = HorovodRunner(np=2) 
hr.run(train_hvd, learning_rate = 0.001)

# COMMAND ----------

# MAGIC %md 
# MAGIC Under the hood, HorovodRunner takes a Python method that contains deep learning training code with Horovod hooks. HorovodRunner pickles the method on the driver and distributes it to Spark workers. A Horovod MPI job is embedded as a Spark job using the barrier execution mode. The first executor collects the IP addresses of all task executors using BarrierTaskContext and triggers a Horovod job using `mpirun`. Each Python MPI process loads the pickled user program, deserializes it, and runs it.
# MAGIC 
# MAGIC For more information, see [HorovodRunner API documentation](https://databricks.github.io/spark-deep-learning/#api-documentation). 