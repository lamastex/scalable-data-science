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
# MAGIC Archived YouTube video (no sound, sorry) of this live unedited lab-lecture:
# MAGIC 
# MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/McRp3HsQjLg/0.jpg)](https://www.youtube.com/embed/McRp3HsQjLg?start=0&end=3008&autoplay=1) 

# COMMAND ----------

# MAGIC %md 
# MAGIC # Convolutional Neural Networks
# MAGIC ## aka CNN, ConvNet

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
# MAGIC * Output layer has one neuron per category, with softmax activation
# MAGIC * __Loss function is cross-entropy loss__
# MAGIC * Accuracy metric is categorical accuracy

# COMMAND ----------

# MAGIC %md
# MAGIC Let's hold pointers into wikipedia for these new concepts.

# COMMAND ----------

# MAGIC %scala
# MAGIC //This allows easy embedding of publicly available information into any other notebook
# MAGIC //Example usage:
# MAGIC // displayHTML(frameIt("https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation#Topics_in_LDA",250))
# MAGIC def frameIt( u:String, h:Int ) : String = {
# MAGIC       """<iframe 
# MAGIC  src=""""+ u+""""
# MAGIC  width="95%" height="""" + h + """"
# MAGIC  sandbox>
# MAGIC   <p>
# MAGIC     <a href="http://spark.apache.org/docs/latest/index.html">
# MAGIC       Fallback link for browsers that, unlikely, don't support frames
# MAGIC     </a>
# MAGIC   </p>
# MAGIC </iframe>"""
# MAGIC    }
# MAGIC displayHTML(frameIt("https://en.wikipedia.org/wiki/Cross_entropy#Cross-entropy_error_function_and_logistic_regression",500))

# COMMAND ----------

# MAGIC %scala
# MAGIC displayHTML(frameIt("https://en.wikipedia.org/wiki/Softmax_function",380))

# COMMAND ----------

# MAGIC %md
# MAGIC The following is from: [https://www.quora.com/How-does-Keras-calculate-accuracy](https://www.quora.com/How-does-Keras-calculate-accuracy).
# MAGIC  
# MAGIC **Categorical accuracy:**
# MAGIC 
# MAGIC ```%python
# MAGIC def categorical_accuracy(y_true, y_pred):
# MAGIC  return K.cast(K.equal(K.argmax(y_true, axis=-1),
# MAGIC  K.argmax(y_pred, axis=-1)),
# MAGIC  K.floatx())
# MAGIC  ```
# MAGIC  
# MAGIC > `K.argmax(y_true)` takes the highest value to be the prediction and matches against the comparative set.

# COMMAND ----------

# MAGIC %md
# MAGIC Watch (1:39) 
# MAGIC * [![Udacity: Deep Learning by Vincent Vanhoucke - Cross-entropy](http://img.youtube.com/vi/tRsSi_sqXjI/0.jpg)](https://www.youtube.com/watch?v=tRsSi_sqXjI)
# MAGIC 
# MAGIC Watch (1:54)
# MAGIC * [![Udacity: Deep Learning by Vincent Vanhoucke - Minimizing Cross-entropy](http://img.youtube.com/vi/x449QQDhMDE/0.jpg)](https://www.youtube.com/watch?v=x449QQDhMDE)

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

# MAGIC %md 
# MAGIC after about a minute we have:
# MAGIC 
# MAGIC ```
# MAGIC ...
# MAGIC 
# MAGIC Epoch 40/40
# MAGIC 1s - loss: 0.0610 - categorical_accuracy: 0.9809 - val_loss: 0.1918 - val_categorical_accuracy: 0.9583
# MAGIC 
# MAGIC ...
# MAGIC  
# MAGIC loss: 0.216120
# MAGIC 
# MAGIC categorical_accuracy: 0.955000
# MAGIC 
# MAGIC Start: 2017-12-06 07:35:33.948102
# MAGIC 
# MAGIC End: 2017-12-06 07:36:27.046130
# MAGIC 
# MAGIC Elapse: 0:00:53.098028
# MAGIC ```

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
# MAGIC Try two more experiments (try them separately):
# MAGIC 1. Add a third, hidden layer.
# MAGIC 2. Increase the size of the hidden layers.
# MAGIC 
# MAGIC Adding another layer slows things down a little (why?) but doesn't seem to make a difference in accuracy.
# MAGIC 
# MAGIC Adding a lot more neurons into the first topology slows things down significantly -- 10x as many neurons, and only a marginal increase in accuracy. Notice also (in the plot) that the learning clearly degrades after epoch 50 or so.
# MAGIC 
# MAGIC ... We need a new approach!
# MAGIC 
# MAGIC ---
# MAGIC 
# MAGIC ... let's think about this:
# MAGIC 
# MAGIC ### What is layer 2 learning from layer 1? Combinations of pixels
# MAGIC 
# MAGIC #### Combinations of pixels contain information but...
# MAGIC 
# MAGIC There are a lot of them (combinations) and they are "fragile" 
# MAGIC 
# MAGIC In fact, in our last experiment, we basically built a model that memorizes a bunch of "magic" pixel combinations.
# MAGIC 
# MAGIC What might be a better way to build features?
# MAGIC 
# MAGIC * When humans perform this task, we look not at arbitrary pixel combinations, but certain geometric patterns -- lines, curves, loops.
# MAGIC * These features are made up of combinations of pixels, but they are far from arbitrary
# MAGIC * We identify these features regardless of translation, rotation, etc.
# MAGIC 
# MAGIC Is there a way to get the network to do the same thing?
# MAGIC 
# MAGIC I.e., in layer one, identify pixels. Then in layer 2+, identify abstractions over pixels that are translation-invariant 2-D shapes?
# MAGIC 
# MAGIC We could look at where a "filter" that represents one of these features (e.g., and edge) matches the image.
# MAGIC 
# MAGIC How would this work?
# MAGIC 
# MAGIC ### Convolution
# MAGIC 
# MAGIC Convolution in the general mathematical sense is define as follows:
# MAGIC 
# MAGIC <img src="https://i.imgur.com/lurC2Cx.png" width=300>
# MAGIC 
# MAGIC The convolution we deal with in deep learning is a simplified case. We want to compare two signals. Here are two visualizations, courtesy of Wikipedia, that help communicate how convolution emphasizes features:
# MAGIC 
# MAGIC <img src="http://i.imgur.com/EDCaMl2.png" width=500>
# MAGIC 
# MAGIC ---
# MAGIC 
# MAGIC #### Here's an animation (where we change \\({\tau}\\)) 
# MAGIC <img src="http://i.imgur.com/0BFcnaw.gif">
# MAGIC 
# MAGIC __In one sense, the convolution captures and quantifies the pattern matching over space__
# MAGIC 
# MAGIC If we perform this in two dimensions, we can achieve effects like highlighting edges:
# MAGIC 
# MAGIC <img src="http://i.imgur.com/DKEXIII.png">
# MAGIC 
# MAGIC The matrix here, also called a convolution kernel, is one of the functions we are convolving. Other convolution kernels can blur, "sharpen," etc.
# MAGIC 
# MAGIC ### So we'll drop in a number of convolution kernels, and the network will learn where to use them? Nope. Better than that.
# MAGIC 
# MAGIC ## We'll program in the *idea* of discrete convolution, and the network will learn what kernels extract meaningful features!
# MAGIC 
# MAGIC The values in a (fixed-size) convolution kernel matrix will be variables in our deep learning model. Although inuitively it seems like it would be hard to learn useful params, in fact, since those variables are used repeatedly across the image data, it "focuses" the error on a smallish number of parameters with a lot of influence -- so it should be vastly *less* expensive to train than just a huge fully connected layer like we discussed above.
# MAGIC 
# MAGIC This idea was developed in the late 1980s, and by 1989, Yann LeCun (at AT&T/Bell Labs) had built a practical high-accuracy system (used in the 1990s for processing handwritten checks and mail).
# MAGIC 
# MAGIC __How do we hook this into our neural networks?__
# MAGIC 
# MAGIC * First, we can preserve the geometric properties of our data by "shaping" the vectors as 2D instead of 1D.
# MAGIC 
# MAGIC * Then we'll create a layer whose value is not just activation applied to weighted sum of inputs, but instead it's the result of a dot-product (element-wise multiply and sum) between the kernel and a patch of the input vector (image).
# MAGIC     * This value will be our "pre-activation" and optionally feed into an activation function (or "detector")
# MAGIC 
# MAGIC <img src="http://i.imgur.com/ECyi9lL.png">
# MAGIC 
# MAGIC 
# MAGIC If we perform this operation at lots of positions over the image, we'll get lots of outputs, as many as one for every input pixel. 
# MAGIC 
# MAGIC 
# MAGIC <img src="http://i.imgur.com/WhOrJ0Y.jpg">
# MAGIC 
# MAGIC * So we'll add another layer that "picks" the highest convolution pattern match from nearby pixels, which
# MAGIC     * makes our pattern match a little bit translation invariant (a fuzzy location match)
# MAGIC     * reduces the number of outputs significantly
# MAGIC * This layer is commonly called a pooling layer, and if we pick the "maximum match" then it's a "max pooling" layer.
# MAGIC 
# MAGIC <img src="http://i.imgur.com/9iPpfpb.png">
# MAGIC 
# MAGIC __The end result is that the kernel or filter together with max pooling creates a value in a subsequent layer which represents the appearance of a pattern in a local area in a prior layer.__
# MAGIC 
# MAGIC __Again, the network will be given a number of "slots" for these filters and will learn (by minimizing error) what filter values produce meaningful features. This is the key insight into how modern image-recognition networks are able to generalize -- i.e., learn to tell 6s from 7s or cats from dogs.__
# MAGIC 
# MAGIC <img src="http://i.imgur.com/F8eH3vj.png">
# MAGIC 
# MAGIC ## Ok, let's build our first ConvNet:
# MAGIC 
# MAGIC First, we want to explicity shape our data into a 2-D configuration. We'll end up with a 4-D tensor where the first dimension is the training examples, then each example is 28x28 pixels, and we'll explicitly say it's 1-layer deep. (Why? with color images, we typically process over 3 or 4 channels in this last dimension)
# MAGIC 
# MAGIC A step by step animation follows:
# MAGIC * http://cs231n.github.io/assets/conv-demo/index.html

# COMMAND ----------

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

# MAGIC %md 
# MAGIC Now the model:

# COMMAND ----------

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

# COMMAND ----------

# MAGIC %md 
# MAGIC ... and the training loop and output:

# COMMAND ----------

start = datetime.datetime.today()

history = model.fit(X_train, y_train, batch_size=128, epochs=8, verbose=2, validation_split=0.1)

scores = model.evaluate(X_test, y_test, verbose=1)

print
for i in range(len(model.metrics_names)):
	print("%s: %f" % (model.metrics_names[i], scores[i]))

# COMMAND ----------

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
# MAGIC ### Our MNIST ConvNet
# MAGIC 
# MAGIC In our first convolutional MNIST experiment, we get to almost 99% validation accuracy in just a few epochs (a minutes or so on CPU)!
# MAGIC 
# MAGIC The training accuracy is effectively 100%, though, so we've almost completely overfit (i.e., memorized the training data) by this point and need to do a little work if we want to keep learning.
# MAGIC 
# MAGIC Let's add another convolutional layer:

# COMMAND ----------

model = Sequential()

model.add(Conv2D(8, # number of kernels 
						(4, 4), # kernel size
                        padding='valid',
                        input_shape=(28, 28, 1)))

model.add(Activation('relu'))

model.add(Conv2D(8, (4, 4)))
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
# MAGIC While that's running, let's look at a number of "famous" convolutional networks!
# MAGIC 
# MAGIC ### LeNet (Yann LeCun, 1998)
# MAGIC 
# MAGIC <img src="http://i.imgur.com/k5hMtMK.png">
# MAGIC 
# MAGIC <img src="http://i.imgur.com/ERV9pHW.gif">

# COMMAND ----------

# MAGIC %md <img src="http://i.imgur.com/TCN9C4P.png">

# COMMAND ----------

# MAGIC %md 
# MAGIC ### AlexNet (2012)
# MAGIC 
# MAGIC <img src="http://i.imgur.com/CpokDKV.jpg">
# MAGIC 
# MAGIC <img src="http://i.imgur.com/Ld2QhXr.jpg">

# COMMAND ----------

# MAGIC %md 
# MAGIC ### Back to our labs: Still Overfitting
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

model.add(Dropout(0.25)) # <- regularize
model.add(Flatten())
model.add(Dense(128))
model.add(Activation('relu'))

model.add(Dropout(0.5)) # <-regularize
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
# MAGIC While that's running, let's look at some more recent ConvNet architectures:
# MAGIC 
# MAGIC ### VGG16 (2014)
# MAGIC 
# MAGIC <img src="http://i.imgur.com/gl4kZDf.png">

# COMMAND ----------

# MAGIC %md 
# MAGIC ### GoogLeNet (2014)
# MAGIC 
# MAGIC <img src="http://i.imgur.com/hvmtDqN.png">
# MAGIC 
# MAGIC *"Inception" layer: parallel convolutions at different resolutions*
# MAGIC 
# MAGIC ### Residual Networks (2015-)
# MAGIC 
# MAGIC Skip layers to improve training (error propagation). Residual layers learn from details at multiple previous layers.
# MAGIC 
# MAGIC <img src="http://i.imgur.com/32g8Ykl.png">

# COMMAND ----------

# MAGIC %md 
# MAGIC ---
# MAGIC 
# MAGIC > __ASIDE: Atrous / Dilated Convolutions__
# MAGIC 
# MAGIC > An atrous or dilated convolution is a convolution filter with "holes" in it. Effectively, it is a way to enlarge the filter spatially while not adding as many parameters or attending to every element in the input.
# MAGIC 
# MAGIC > Why? Covering a larger input volume allows recognizing coarser-grained patterns; restricting the number of parameters is a way of regularizing or constraining the capacity of the model, making training easier.
# MAGIC 
# MAGIC ---

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

# COMMAND ----------

# MAGIC %md
# MAGIC Of course this world evolves fast - see the new kid in the CNN block -- **capsule networks**
# MAGIC 
# MAGIC > Hinton: “The pooling operation used in convolutional neural networks is a big mistake and the fact that it works so well is a disaster.”
# MAGIC 
# MAGIC Well worth the 8 minute read:
# MAGIC * [https://medium.com/ai%C2%B3-theory-practice-business/understanding-hintons-capsule-networks-part-i-intuition-b4b559d1159b](https://medium.com/ai%C2%B3-theory-practice-business/understanding-hintons-capsule-networks-part-i-intuition-b4b559d1159b)
# MAGIC 
# MAGIC To understand deeper:
# MAGIC * original paper: [https://arxiv.org/abs/1710.09829](https://arxiv.org/abs/1710.09829)