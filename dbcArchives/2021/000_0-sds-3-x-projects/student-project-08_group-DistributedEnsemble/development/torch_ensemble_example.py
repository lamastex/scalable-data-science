# Databricks notebook source
# Imports
import torch
import torch.nn as nn


# COMMAND ----------

# Some data creation
num_samples = 500
means = torch.tensor([-2, 2], dtype=float)
stds = torch.tensor([1, 1], dtype=float)
x1 = torch.normal(mean=means[0], std=stds[0], size=(num_samples, 1))
x2 = torch.normal(mean=means[1], std=stds[1], size=(num_samples, 1))
X = torch.cat((x1, x2), dim=0)

y1 = torch.zeros((num_samples, 1))
y2 = torch.ones((num_samples, 1))
Y = torch.cat((y1, y2), dim=0)

print(X.size())
print(Y.size())


# COMMAND ----------

# shuffle data
inds = torch.randperm(X.size(0))
X = X[inds, :]
Y = Y[inds, :]

num_train = 800
train_X = X[:num_train, :]
train_Y = Y[:num_train, :]

test_X = X[num_train:, :]
test_Y = Y[num_train:, :]

# COMMAND ----------

# simple linear model
class SimpleModel(nn.Module):
  def __init__(self):
    super(SimpleModel, self).__init__()
  
    input_dim = 1
    num_classes = 2
    self.fc = nn.Linear(input_dim, num_classes)
        
  def forward(self,x):
      return self.fc(x)
      
  def predict(self,x):
    return torch.argmax(nn.Softmax(dim=-1)(self.forward(x)), dim=-1)

# COMMAND ----------

def train_model(data, test_X, num_epochs=10):
  model = SimpleModel()
  loss_func = nn.CrossEntropyLoss()
  optimizer = torch.optim.SGD(model.parameters(),lr = 0.1)
  model.train()
  inputs, labels = data
  for epoch in range(num_epochs):
    running_loss = 0
    optimizer.zero_grad()
    output = model(inputs)
    loss = loss_func(output,labels[:, 0].long())
    loss.backward()
    optimizer.step()
    running_loss += loss.item()

  
  print("Epoch: {} \tRunning Loss: {:.6f}".format(epoch+1, running_loss))
  
  return model.state_dict()

# COMMAND ----------

import numpy as np

# Sequential test (note: won't work now that train_model returns model.state_dict, but we could send state_dict to the predict function and use model.load_state_dict())
M1 = SimpleModel()
M2 = SimpleModel()
M3 = SimpleModel()

ensemble_list = [M1, M2, M3]

# Sequential training and testing
trained_ensemble = [train_model(ensemble_member, (train_X, train_Y)) for ensemble_member in ensemble_list]
predicted_labels = [ensemble_member.predict(test_X) for ensemble_member in trained_ensemble]
test_acc = [np.mean(p.data.numpy() == test_Y.data.numpy()) for p in predicted_labels]
print(test_acc)

# COMMAND ----------

# Attempt at parallel training
#ensemble_data_list = [(M1, (train_X, train_Y)), (M2, (train_X, train_Y)), (M3, (train_X, train_Y))]
ensemble_data_list = [(train_X, train_Y), (train_X, train_Y), (train_X, train_Y)]
par_ensemble_data = sc.parallelize(ensemble_data_list)
trained_ensemble = par_ensemble_data.map(lambda ensemble_member: train_model(ensemble_member, test_X)).collect()
print(trained_ensemble)
#output_ens = trained_ensemble.map(lambda ensemble_member: next(ensemble_member[0].parameters()))

#predicted_labels = trained_ensemble.map(lambda ensemble_member: ensemble_member.predict(test_X))
#test_acc = predicted_labels.map(lambda p: np.mean(p.data.numpy() == test_Y.data.numpy())).collect()

# COMMAND ----------



