# Databricks notebook source
# MAGIC %md
# MAGIC 
# MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
# MAGIC 
# MAGIC This is used in a non-profit educational setting with kind permission of [Adam Breindel](https://www.linkedin.com/in/adbreind).
# MAGIC This is not licensed by Adam for use in a for-profit setting. Please contact Adam directly at `adbreind@gmail.com` to request or report such use cases or abuses. 
# MAGIC A few minor modifications and additional mathematical statistical pointers have been added by Raazesh Sainudiin when teaching PhD students in Uppsala University.

# COMMAND ----------

# MAGIC %md 
# MAGIC #### As we dive into more hands-on works, let's recap some basic guidelines:
# MAGIC 
# MAGIC 0. Structure of your network is the first thing to work with, before worrying about the precise number of neurons, size of convolution filters etc.
# MAGIC 
# MAGIC 1. "Business records" or fairly (ideally?) uncorrelated predictors -- use Dense Perceptron Layer(s)
# MAGIC 
# MAGIC 2. Data that has 2-D patterns: 2D Convolution layer(s)
# MAGIC 
# MAGIC 3. For activation of hidden layers, when in doubt, use ReLU
# MAGIC 
# MAGIC 4. Output: 
# MAGIC   * Regression: 1 neuron with linear activation
# MAGIC   * For k-way classification: k neurons with softmax activation 
# MAGIC 
# MAGIC 5. Deeper networks are "smarter" than wider networks (in terms of abstraction)
# MAGIC 
# MAGIC 6. More neurons & layers \\( \to \\) more capacity \\( \to \\)  more data \\( \to \\)  more regularization (to prevent overfitting)
# MAGIC 
# MAGIC 7. If you don't have any specific reason not to use the "adam" optimizer, use that one
# MAGIC 
# MAGIC 8. Errors: 
# MAGIC   * For regression or "wide" content matching (e.g., large image similarity), use mean-square-error; 
# MAGIC   * For classification or narrow content matching, use cross-entropy
# MAGIC 
# MAGIC 9. As you simplify and abstract from your raw data, you should need less features/parameters, so your layers probably become smaller and simpler.

# COMMAND ----------

# MAGIC %md 
# MAGIC As a baseline, let's start a lab running with what we already know.
# MAGIC 
# MAGIC We'll take our deep feed-forward multilayer perceptron network, with ReLU activations and reasonable initializations, and apply it to learning the MNIST digits.
# MAGIC 
# MAGIC The main part of the code looks like the following (full code you can run is in the next cell):
# MAGIC 
# MAGIC ```
# MAGIC # imports, setup, load data sets
# MAGIC 
# MAGIC model = Sequential()
# MAGIC model.add(Dense(20, input_dim=784, kernel_initializer='normal', activation='relu'))
# MAGIC model.add(Dense(15, kernel_initializer='normal', activation='relu'))
# MAGIC model.add(Dense(10, kernel_initializer='normal', activation='softmax'))
# MAGIC model.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['categorical_accuracy'])
# MAGIC 
# MAGIC categorical_labels = to_categorical(y_train, num_classes=10)
# MAGIC 
# MAGIC history = model.fit(X_train, categorical_labels, epochs=100, batch_size=100)
# MAGIC 
# MAGIC # print metrics, plot errors
# MAGIC ```
# MAGIC 
# MAGIC Note the changes, which are largely about building a classifier instead of a regression model:
# MAGIC 
# MAGIC * Output layer has one neuron per category, with softmax activation
# MAGIC * __Loss function is cross-entropy loss__
# MAGIC * Accuracy metric is categorical accuracy

# COMMAND ----------

from keras.models import Sequential
from keras.layers import Dense
from keras.utils import to_categorical
import sklearn.datasets
import datetime
import matplotlib.pyplot as plt
import numpy as np

train_libsvm = "/dbfs/databricks-datasets/mnist-digits/data-001/mnist-digits-train.txt"
test_libsvm = "/dbfs/databricks-datasets/mnist-digits/data-001/mnist-digits-test.txt"

X_train, y_train = sklearn.datasets.load_svmlight_file(train_libsvm, n_features=784)
X_train = X_train.toarray()

X_test, y_test = sklearn.datasets.load_svmlight_file(test_libsvm, n_features=784)
X_test = X_test.toarray()

model = Sequential()
model.add(Dense(20, input_dim=784, kernel_initializer='normal', activation='relu'))
model.add(Dense(15, kernel_initializer='normal', activation='relu'))
model.add(Dense(10, kernel_initializer='normal', activation='softmax'))
model.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['categorical_accuracy'])

categorical_labels = to_categorical(y_train, num_classes=10)
start = datetime.datetime.today()

history = model.fit(X_train, categorical_labels, epochs=40, batch_size=100, validation_split=0.1, verbose=2)

scores = model.evaluate(X_test, to_categorical(y_test, num_classes=10))

print
for i in range(len(model.metrics_names)):
	print("%s: %f" % (model.metrics_names[i], scores[i]))

print ("Start: " + str(start))
end = datetime.datetime.today()
print ("End: " + str(end))
print ("Elapse: " + str(end-start))

# COMMAND ----------

import matplotlib.pyplot as plt

fig, ax = plt.subplots()
fig.set_size_inches((5,5))
plt.plot(history.history['loss'])
plt.plot(history.history['val_loss'])
plt.title('model loss')
plt.ylabel('loss')
plt.xlabel('epoch')
plt.legend(['train', 'val'], loc='upper left')
display(fig)

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC What are the big takeaways from this experiment?
# MAGIC 
# MAGIC 1. We get pretty impressive "apparent error" accuracy right from the start! A small network gets us to training accuracy 97% by epoch 20
# MAGIC 2. The model *appears* to continue to learn if we let it run, although it does slow down and oscillate a bit.
# MAGIC 3. Our test accuracy is about 95% after 5 epochs and never gets better ... it gets worse!
# MAGIC 4. Therefore, we are overfitting very quickly... most of the "training" turns out to be a waste.
# MAGIC 5. For what it's worth, we get 95% accuracy without much work.
# MAGIC 
# MAGIC This is not terrible compared to other, non-neural-network approaches to the problem. After all, we could probably tweak this a bit and do even better.
# MAGIC 
# MAGIC But we talked about using deep learning to solve "95%" problems or "98%" problems ... where one error in 20, or 50 simply won't work. If we can get to "multiple nines" of accuracy, then we can do things like automate mail sorting and translation, create cars that react properly (all the time) to street signs, and control systems for robots or drones that function autonomously.
# MAGIC 
# MAGIC ## You Try Now!
# MAGIC 
# MAGIC Try two more experiments (try them separately):
# MAGIC 
# MAGIC 1. Add a third, hidden layer.
# MAGIC 2. Increase the size of the hidden layers.
# MAGIC 
# MAGIC Adding another layer slows things down a little (why?) but doesn't seem to make a difference in accuracy.
# MAGIC 
# MAGIC Adding a lot more neurons into the first topology slows things down significantly -- 10x as many neurons, and only a marginal increase in accuracy. Notice also (in the plot) that the learning clearly degrades after epoch 50 or so.