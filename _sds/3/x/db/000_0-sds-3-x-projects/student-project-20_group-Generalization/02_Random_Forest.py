# Databricks notebook source
# MAGIC %md
# MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

# COMMAND ----------

# MAGIC %md
# MAGIC ## Random Forests and MixUp
# MAGIC 
# MAGIC First off, we will implement MixUp for a Random Forest applied to the Fashion-MNIST data set. Fashion-MNIST consists of black and white 28x28 images of clothing items [[6]]. We will use the scikit-learn package to implement the Random Forest algorithm, and then perform a distributed hyperparameter search with Ray tune. Thus, scalability enters this part of the project through the hyperparameter search.
# MAGIC 
# MAGIC First, we will just train the Random Forest using the basic training data and observe the performance. Next, we will do the same but utilizing MixUp. Typically, MixUp is used for iterative algorithms, where a new batch of MixUp data is created at each iteration. However, since a Random Forest is not trained iteratively, we use MixUp to augment our data set by adding a number of MixUp data points to our original data set.
# MAGIC 
# MAGIC First, we will load the data set.
# MAGIC 
# MAGIC [6]: https://github.com/zalandoresearch/fashion-mnist

# COMMAND ----------

# Loading Fashion-mnist

import tensorflow as tf
(X, y),(testX,testY) = tf.keras.datasets.fashion_mnist.load_data()
X = X.reshape(60000, 28*28)

from sklearn.preprocessing import LabelBinarizer

enc = LabelBinarizer()
y = enc.fit_transform(y)

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC Next, we define a function that can be used to generated new MixUp data.

# COMMAND ----------

# Function to create MixUp data

def create_mixup(X, y, beta_param):
  n = np.shape(X)[0]
  shuffled_indices = np.arange(n).tolist()
  np.random.shuffle(shuffled_indices)
  X_s = X[shuffled_indices]
  y_s = y[shuffled_indices]
  mixup_l = np.random.beta(beta_param,beta_param)
  X_mixed = X*(1-mixup_l) + mixup_l*X_s
  y_mixed = y*(1-mixup_l) + (mixup_l)*(y_s)
  return X_mixed, y_mixed

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC Next, we split the data into training and validation sets.

# COMMAND ----------

# Fixes the issue "AttributeError: 'ConsoleBuffer has no attribute 'fileno'"
import sys
sys.stdout.fileno = lambda: False

import numpy as np

from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestRegressor

# Prepare the data
num_classes = 10
np.random.seed(1)
X_train, X_test, y_train, y_test = train_test_split(X, y, random_state = 1, test_size=0.5)
X_train_base = X_train.copy()
y_train_base = y_train.copy()

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC We now define the training function that will be used by Ray Tune. For each set of hyperparameters, we initialize a Random Forest and train on the data, either with or without added folds of MixUp data. We then evaluate on some metrics of interest.

# COMMAND ----------

# Fixes the issue "AttributeError: 'ConsoleBuffer has no attribute 'fileno'"
import sys
sys.stdout.fileno = lambda: False

from sklearn import metrics
import numpy as np

from sklearn.ensemble import RandomForestRegressor

def training_function(config, checkpoint_dir=None):
    # Hyperparameters
    n_estimators, max_depth, mixup_folds = config["n_estimators"], config["max_depth"], config["mixup_folds"]
    
    X_train_data = X_train_base.copy()
    y_train_data = y_train_base.copy()
    
    for i in range(mixup_folds):
      X_mixed, y_mixed = create_mixup(X_train_base, y_train_base, 0.2)
      X_train_data = np.concatenate([X_train_data, X_mixed])
      y_train_data = np.concatenate([y_train_data, y_mixed])
    
    # Instantiate model with n_estimators decision trees
    rf = RandomForestRegressor(n_estimators = n_estimators, max_depth = max_depth, random_state = 1)
    # Train the model on training data
    rf.fit(X_train_data, y_train_data)
    
    """
    Logg the results
    """
    
    #x_mix, y_mix = mixup_data( x_val, y_val)
    #mix_loss, mix_acc = model.evaluate( x_mix, y_mix )
    y_pred_probs = rf.predict(X_test)
    y_pred = np.zeros_like(y_pred_probs)
    y_pred[np.arange(len(y_pred_probs)), y_pred_probs.argmax(1)] = 1
    val_acc = np.mean(np.argmax(y_test,1) == np.argmax(y_pred,1))
    
    y_pred_probs = rf.predict(X_train_base)
    y_pred = np.zeros_like(y_pred_probs)
    y_pred[np.arange(len(y_pred_probs)), y_pred_probs.argmax(1)] = 1
    train_acc = np.mean(np.argmax(y_train_base,1) == np.argmax(y_pred,1))
    
    mean_loss = 1
    
    tune.report(mean_loss=mean_loss, train_accuracy = train_acc, val_accuracy = val_acc)

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC Finally, we run the actual hyperparameter search in a distributed fashion. Regarding the amount of MixUp data, we try using no MixUp, or we add 2 folds, effectively tripling the size of the data set.

# COMMAND ----------

from ray import tune
from ray.tune import CLIReporter
# Limit the number of rows.
reporter = CLIReporter(max_progress_rows=10)

reporter.add_metric_column("val_accuracy")
reporter.add_metric_column("train_accuracy")



analysis = tune.run(
    training_function,
    config={
      'n_estimators': tune.grid_search([10, 20]),
      'max_depth': tune.grid_search([5, 10]),
      'mixup_folds': tune.grid_search([0, 2])
    },
    local_dir='ray_results',
    progress_reporter=reporter
) 

print("Best config: ", analysis.get_best_config(
    metric="val_accuracy", mode="max"))

#Get a dataframe for analyzing trial results.
df = analysis.results_df

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC Let's look at the data from the different trials to see if we can conclude anything about the efficacy of MixUp.

# COMMAND ----------

df[['config.n_estimators', 'config.max_depth', 'config.mixup_folds', 'train_accuracy', 'val_accuracy']]

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC **Conclusions**
# MAGIC 
# MAGIC Based on the results, MixUp does not seem to help in this context. The validation accuracy achieved with MixUp is actually slightly lower than without it. The reasons for this may be that the data is too simple, that Random Forests cannot fully utilize the power of MixUp augmentation due to not being iterative, or that the piecewise constant nature Decision Trees means that MixUp cannot help too much.