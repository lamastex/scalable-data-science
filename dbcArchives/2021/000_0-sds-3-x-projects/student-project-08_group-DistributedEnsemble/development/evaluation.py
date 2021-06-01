# Databricks notebook source
# Based on Amir's Train function
def predict(net_params, net_shape, test_x):
  """Make predictions with the MLP net
  Let N := number of data points and D_x := the dimension of a single datapoint x
  
  Args: 
    net_params (OrderedDict): Trained parameters as a state dict
    net_shape (list[int]): layer sizes
    test_x (torch.Tensor): Test data, tensor of shape (N, D_x)
  
  Returns:
    predictions (torch.Tensor)
  """
  net=MLP(net_shape)
  net.load_state_dict(net_params)
  net.eval()
  
  # I suggest adding this convenience function into the MLP class
  #net = MLP.from_state_dict(net_params, net_shape)
  return net(test_x)

def ens_preds(models, test_x):
  """Distributed ensemble predictions
  Takes a set of models and test data and makes distributed predictions
  Let N := number of data points and D_x := the dimension of a single datapoint x
  
  Args:
    models (list[state_dict, shape]): set of models represented as a list (state_dict, shape)
    test_x (torch.Tensor): Tensor of size (N, D_x)
  
  Returns:
    Distributed iterator over the predictions. E.g. an iterator over probability vectors in the case of a classifier ens.
  """
  pred_iter = pred_models_iter(models, test_x)
  return pred_iter.map(lambda t: predict(*t))

def ens_metrics(models, test_x, test_y, metrics):
  """Distributed ensemble metrics
  Takes a set of models and test data, predicts probability vectors and calculates the provided metrics
  given true labels `test_y`
  Let N := number of data points and D_x := the dimension of a single datapoint x
  
  Args:
    models (list[state_dict, shape]): set of models represented as a list (state_dict, shape)
    test_x (torch.Tensor): Tensor of size (N, D_x)
    test_y (torch.Tensor): Tensor of size (N). NB: hard labels
    metrics (list[functions]): List of functions where each funcion f: R^(N x D_x) x R^(N) --> T, where is a generic output type.
  """
  return ens_preds(models, test_x).map(lambda prob_vecs: [metric(prob_vecs, test_y) for metric in metrics])

def ens_preds_reduced(models, test_x, red_fn):
  """Distributed ensemble predictions
  Takes a set of models and test data and makes distributed predictions and reduces them with a provided `red_fn`
  Let N := number of data points and D_x := the dimension of a single datapoint x
  
  Args:
    models (list[state_dict, shape]): set of models represented as a list (state_dict, shape)
    test_x (torch.Tensor): Tensor of size (N, D_x)
    red_fn function: f: R^D_x x R^D_x --> R^D_x
  
  Returns:
    Single reduced/aggregated prediction of the whole ensemble
  """
  pred_iter = pred_models_iter(models, test_x)
  return pred_iter.map(lambda t: Predict(*t)).reduce(red_fn)


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
  
def _pred_models_iter(models, test_x):
  """Helper function to generate a distributed iterator over models and test data
  NB: the same `test_x` is given to all elements in the iterator
  
  Args:
    models (list[state_dict, shape]): set of models represented as a list (state_dict, shape)
    test_x (torch.Tensor): Tensor of size (N, D_x)
  """
  models_and_data = [(params, shape, test_x) for params, shape in models]
  return sc.parallelize(models_and_data)

def novelty_detection():
  pass
def entropy():
  pass
def auroc():
  pass
# Rather dumb way of getting test data, but it works fine.
data = get_batched_iterator(load_firewall_data())
batch = next(data)
x=[v[0] for v in d]
x=torch.tensor(x,dtype=torch.float)
y=[v[1] for v in d]
y=torch.tensor(y,dtype=torch.long)

# Non-distr. list of models
models = [(params, shape) for params, shape, _ in models_trained_c]

avg_acc = ens_metrics(models, x, y, [avg_accuracy]).collect() # Average acc. for each ens. over all data points
avg_prob_vecs = ens_preds_reduced(models, x, lambda x, y: (x+y)/2) # (A single) Average prob. vec for all data points.