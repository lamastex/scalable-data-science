<div class="cell code" execution_count="1" scrolled="false">

``` python
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
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
#Updates the model parameters with one step of stochastic gradient descent given a batch of labeled data
def SGDStep(net_params,net_shape,x,y,lr=0.1):
  
  #x=torch.Tensor(x)
  #y=torch.Tensor(y)
  
  net=MLP(net_shape)
  net.load_state_dict(net_params)
  
  opt=optim.SGD(net.parameters(),lr)
  opt.zero_grad()
  loss=nn.CrossEntropyLoss()
  
  yhat=net(x)
  err=loss(yhat,y)
  err.backward()
  
  opt.step()
  
  lossval=float(err.detach().numpy())
  
  #returns updated parameters, network shape, and loss
  return (net.state_dict(),net_shape,lossval)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
n_models=5 #ensemble size
model_data=[] #pairs of model parameters and their training data
shapes=[] #shape of networks
inputdims=10 #features dimensions
nclasses=2 #number of classes

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
  
  #-to be replaced with batch loader
  x=torch.rand([10,inputdims])
  y=torch.ones([10,]).long()
  #-
  
  model_data.append((net.state_dict(),shape,x,y))
  

#main training loop
numepochs=6
for epoch in range(numepochs):
  
  model_data_par=sc.parallelize(model_data)

  updated_models= model_data_par.map(lambda t: SGDStep(*t)) 
  
  updated_models=updated_models.collect()
  print("loss:")
  print([u[2] for u in updated_models])
  
  #loading batches of data, and reconstructing the model-data array
  model_data=[]
  for i in range(n_models):
    #-to be replaced with batch loader
    x=torch.rand([10,inputdims])
    y=torch.ones([10,]).long()
    #-
    model_data.append((updated_models[i][0],shapes[i],x,y))
```

<div class="output execute_result plain_result" execution_count="1">

    loss:
    [0.7414336800575256, 0.5925530195236206, 0.6060353517532349, 0.6709138751029968, 0.6918854117393494]
    loss:
    [0.708870530128479, 0.5514313578605652, 0.5812880396842957, 0.6448978781700134, 0.6799639463424683]
    loss:
    [0.6816596984863281, 0.5315981507301331, 0.5583221316337585, 0.6201316118240356, 0.6510061025619507]
    loss:
    [0.654758870601654, 0.5064709186553955, 0.5390928983688354, 0.597870945930481, 0.6252564191818237]
    loss:
    [0.631588876247406, 0.4890592098236084, 0.5212031602859497, 0.5759772658348083, 0.6056066751480103]
    loss:
    [0.6112779378890991, 0.4709406793117523, 0.5067512392997742, 0.5547378063201904, 0.5784258842468262]

</div>

</div>
