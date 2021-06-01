# Databricks notebook source
import torch

# COMMAND ----------

# Should be false, this is not the GPU cluster
torch.cuda.is_available()

# COMMAND ----------

data = sc.parallelize([1, 2, 3, 4, 5])

data.map(lambda x: torch.tensor(x, dtype=torch.double)).sum()

# COMMAND ----------

def true_lin(x: torch.Tensor):
  true_w = 2
  true_b = 3
  return true_w * x + true_b

def calc_grad(x: torch.Tensor, y_true: torch.Tensor):
    w = torch.tensor(0.0, requires_grad=True)
    b = torch.tensor(0.0, requires_grad=True)
    y_hat = w * x + b
    loss = (y_true - y_hat)**2
    loss.backward()
    return w.grad, b.grad
    
data.map(lambda x: torch.tensor(x, dtype=torch.double)).map(lambda x: (x, true_lin(x))).map(lambda pair: calc_grad(pair[0], pair[1])).collect()

# COMMAND ----------

def true_lin(x: torch.Tensor):
  true_w = 2
  true_b = 3
  return true_w * x + true_b

LR = 0.01
def calc_grad(x: torch.Tensor, y_true: torch.Tensor):
    w = torch.tensor(0.0, requires_grad=True)
    b = torch.tensor(0.0, requires_grad=True)
    for i in range(1, 10):
      y_hat = w * x + b
      loss = (y_true - y_hat)**2
      loss.backward()
      with torch.no_grad():
        w -= LR * w.grad
        b -= LR * b.grad
        w.grad.zero_()
        b.grad.zero_()
    return w, b
    
data.map(lambda x: torch.tensor(x, dtype=torch.double)).map(lambda x: (x, true_lin(x))).map(lambda pair: calc_grad(pair[0], pair[1])).collect()

# COMMAND ----------

def true_lin(x: torch.Tensor):
  true_w = 2
  true_b = 3
  return true_w * x + true_b

LR = 0.01



def train_model(x: torch.Tensor, y_true: torch.Tensor):
    w = torch.tensor(0.0, requires_grad=True)
    b = torch.tensor(0.0, requires_grad=True)
    for i in range(1, 10):
      y_hat = w * x + b
      loss = (y_true - y_hat)**2
      loss.backward()
      with torch.no_grad():
        w -= LR * w.grad
        b -= LR * b.grad
        w.grad.zero_()
        b.grad.zero_()
    return w, b
    
data.map(lambda x: torch.tensor(x, dtype=torch.double)).map(lambda x: (x, true_lin(x))).map(lambda pair: calc_grad(pair[0], pair[1])).collect()

# COMMAND ----------

model_ids = sc.parallelize(["model_1", "model_2"]);
model_ids.map(lambda id: load_model(id)).map(lambda model: model.train())

# N data points in full data
# Limit m point in node.
gen_list(m, N) # M ensemble member 

# COMMAND ----------

ensemble = sc.parallelize(gen_ensembel(num_members))
for epoch in np.arange(1, num_epochs):
  for batch in dataloader:
    batch_iter = sc.parallelize(batch)
    list_of_new_models = ensemble.zip(batch_iter).map(lambda model, data: model.train(data))
    ensemble = list_of_new_models

# COMMAND ----------

def true_lin(x: torch.Tensor):
  true_w = 2
  true_b = 3
  return true_w * x + true_b

LR = 0.01
def calc_grad(x: torch.Tensor, y_true: torch.Tensor):
    w = torch.tensor(0.0, requires_grad=True)
    b = torch.tensor(0.0, requires_grad=True)
    for i in range(1, 10):
      y_hat = w * x + b
      loss = (y_true - y_hat)**2
      loss.backward()
      with torch.no_grad():
        w -= LR * w.grad
        b -= LR * b.grad
        w.grad.zero_()
        b.grad.zero_()
    return w, b

#data = sc.parallelize(list(range(0,10000))) #distributed
data = list(range(0,10000)) #non-distributed

#data.map(lambda x: torch.tensor(x, dtype=torch.double)).map(lambda x: (x, true_lin(x))).map(lambda pair: calc_grad(pair[0], pair[1])).collect()
m1=map(lambda x:torch.tensor(x, dtype=torch.double),data)
m2=map(lambda x: (x, true_lin(x)),m1)
m3=map(lambda pair: calc_grad(pair[0], pair[1]),m2)

print(list(m3))

# COMMAND ----------

