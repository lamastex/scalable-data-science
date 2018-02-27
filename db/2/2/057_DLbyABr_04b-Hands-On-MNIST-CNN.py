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
# MAGIC Try out the first ConvNet -- the one we looked at earlier.
# MAGIC 
# MAGIC This code is the same, but we'll run to 20 epochs so we can get a better feel for fitting/validation/overfitting trend.

# COMMAND ----------

from keras.utils import to_categorical
import sklearn.datasets

train_libsvm = "/dbfs/databricks-datasets/mnist-digits/data-001/mnist-digits-train.txt"
test_libsvm = "/dbfs/databricks-datasets/mnist-digits/data-001/mnist-digits-test.txt"

X_train, y_train = sklearn.datasets.load_svmlight_file(train_libsvm, n_features=784)
X_train = X_train.toarray()

X_test, y_test = sklearn.datasets.load_svmlight_file(test_libsvm, n_features=784)
X_test = X_test.toarray()

X_train = X_train.reshape( (X_train.shape[0], 28, 28, 1) )
X_train = X_train.astype('float32')
X_train /= 255
y_train = to_categorical(y_train, num_classes=10)

X_test = X_test.reshape( (X_test.shape[0], 28, 28, 1) )
X_test = X_test.astype('float32')
X_test /= 255
y_test = to_categorical(y_test, num_classes=10)

# COMMAND ----------

from keras.models import Sequential
from keras.layers import Dense, Dropout, Activation, Flatten, Conv2D, MaxPooling2D

model = Sequential()

model.add(Conv2D(8, # number of kernels 
				(4, 4), # kernel size
                padding='valid', # no padding; output will be smaller than input
                input_shape=(28, 28, 1)))

model.add(Activation('relu'))

model.add(MaxPooling2D(pool_size=(2,2)))

model.add(Flatten())
model.add(Dense(128))
model.add(Activation('relu')) # alternative syntax for applying activation

model.add(Dense(10))
model.add(Activation('softmax'))

model.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['accuracy'])

history = model.fit(X_train, y_train, batch_size=128, epochs=20, verbose=2, validation_split=0.1)

scores = model.evaluate(X_test, y_test, verbose=1)

print
for i in range(len(model.metrics_names)):
	print("%s: %f" % (model.metrics_names[i], scores[i]))

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
# MAGIC Next, let's try adding another convolutional layer:

# COMMAND ----------

model = Sequential()

model.add(Conv2D(8, # number of kernels 
						(4, 4), # kernel size
                        padding='valid',
                        input_shape=(28, 28, 1)))

model.add(Activation('relu'))

model.add(Conv2D(8, (4, 4))) # <-- additional Conv layer
model.add(Activation('relu'))

model.add(MaxPooling2D(pool_size=(2,2)))

model.add(Flatten())
model.add(Dense(128))
model.add(Activation('relu'))

model.add(Dense(10))
model.add(Activation('softmax'))

model.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['accuracy'])

history = model.fit(X_train, y_train, batch_size=128, epochs=15, verbose=2, validation_split=0.1)

scores = model.evaluate(X_test, y_test, verbose=1)

print
for i in range(len(model.metrics_names)):
	print("%s: %f" % (model.metrics_names[i], scores[i]))

# COMMAND ----------

# MAGIC %md 
# MAGIC ### Still Overfitting
# MAGIC 
# MAGIC We're making progress on our test error -- about 99% -- but just a bit for all the additional time, due to the network overfitting the data.
# MAGIC 
# MAGIC There are a variety of techniques we can take to counter this -- forms of regularization. 
# MAGIC 
# MAGIC Let's try a relatively simple solution solution that works surprisingly well: add a pair of `Dropout` filters, a layer that randomly omits a fraction of neurons from each training batch (thus exposing each neuron to only part of the training data).
# MAGIC 
# MAGIC We'll add more convolution kernels but shrink them to 3x3 as well.

# COMMAND ----------

model = Sequential()

model.add(Conv2D(32, # number of kernels 
						(3, 3), # kernel size
                        padding='valid',
                        input_shape=(28, 28, 1)))

model.add(Activation('relu'))

model.add(Conv2D(32, (3, 3)))
model.add(Activation('relu'))

model.add(MaxPooling2D(pool_size=(2,2)))

model.add(Dropout(0.25))
model.add(Flatten())
model.add(Dense(128))
model.add(Activation('relu'))

model.add(Dropout(0.5))
model.add(Dense(10))
model.add(Activation('softmax'))

model.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['accuracy'])
history = model.fit(X_train, y_train, batch_size=128, epochs=15, verbose=2)

scores = model.evaluate(X_test, y_test, verbose=2)

print
for i in range(len(model.metrics_names)):
	print("%s: %f" % (model.metrics_names[i], scores[i]))

# COMMAND ----------

# MAGIC %md 
# MAGIC ## *Lab Wrapup*
# MAGIC 
# MAGIC From the last lab, you should have a test accuracy of over 99.1%
# MAGIC 
# MAGIC For one more activity, try changing the optimizer to old-school "sgd" -- just to see how far we've come with these modern gradient descent techniques in the last few years.
# MAGIC 
# MAGIC Accuracy will end up noticeably worse ... about 96-97% test accuracy. Two key takeaways:
# MAGIC 
# MAGIC * Without a good optimizer, even a very powerful network design may not achieve results
# MAGIC * In fact, we could replace the word "optimizer" there with
# MAGIC     * initialization
# MAGIC     * activation
# MAGIC     * regularization
# MAGIC     * (etc.)
# MAGIC * All of these elements we've been working with operate together in a complex way to determine final performance