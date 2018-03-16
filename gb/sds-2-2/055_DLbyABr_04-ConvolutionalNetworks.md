[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

This is used in a non-profit educational setting with kind permission of [Adam Breindel](https://www.linkedin.com/in/adbreind).
This is not licensed by Adam for use in a for-profit setting. Please contact Adam directly at `adbreind@gmail.com` to request or report such use cases or abuses.
A few minor modifications and additional mathematical statistical pointers have been added by Raazesh Sainudiin when teaching PhD students in Uppsala University.

Archived YouTube video (no sound, sorry) of this live unedited lab-lecture:

[![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/McRp3HsQjLg/0.jpg)](https://www.youtube.com/embed/McRp3HsQjLg?start=0&end=3008&autoplay=1) [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/4N7yiC7-MqU/0.jpg)](https://www.youtube.com/embed/4N7yiC7-MqU?start=0&end=1763&autoplay=1)

Convolutional Neural Networks
=============================

aka CNN, ConvNet
----------------

As a baseline, let's start a lab running with what we already know.

We'll take our deep feed-forward multilayer perceptron network, with ReLU activations and reasonable initializations, and apply it to learning the MNIST digits.

The main part of the code looks like the following (full code you can run is in the next cell):

\`\`\`

imports, setup, load data sets
==============================

model = Sequential()
model.add(Dense(20, input*dim=784, kernel*initializer='normal', activation='relu'))
model.add(Dense(15, kernel*initializer='normal', activation='relu'))
model.add(Dense(10, kernel*initializer='normal', activation='softmax'))
model.compile(loss='categorical*crossentropy', optimizer='adam', metrics=\['categorical*accuracy'\])

categorical*labels = to*categorical(y*train, num*classes=10)

history = model.fit(X*train, categorical*labels, epochs=100, batch\_size=100)

print metrics, plot errors
==========================

\`\`\`

Note the changes, which are largely about building a classifier instead of a regression model:
\* Output layer has one neuron per category, with softmax activation
\* **Loss function is cross-entropy loss**
\* Accuracy metric is categorical accuracy

Let's hold pointers into wikipedia for these new concepts.

<p class="htmlSandbox"><iframe 
 src="https://en.wikipedia.org/wiki/Cross_entropy#Cross-entropy_error_function_and_logistic_regression"
 width="95%" height="500"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe></p>

<p class="htmlSandbox"><iframe 
 src="https://en.wikipedia.org/wiki/Softmax_function"
 width="95%" height="380"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe></p>

The following is from: <https://www.quora.com/How-does-Keras-calculate-accuracy>.

**Categorical accuracy:**

`%python def categorical_accuracy(y_true, y_pred):  return K.cast(K.equal(K.argmax(y_true, axis=-1),  K.argmax(y_pred, axis=-1)),  K.floatx())`

> `K.argmax(y_true)` takes the highest value to be the prediction and matches against the comparative set.

Watch (1:39)
\* [![Udacity: Deep Learning by Vincent Vanhoucke - Cross-entropy](http://img.youtube.com/vi/tRsSi_sqXjI/0.jpg)](https://www.youtube.com/watch?v=tRsSi_sqXjI)

Watch (1:54)
\* [![Udacity: Deep Learning by Vincent Vanhoucke - Minimizing Cross-entropy](http://img.youtube.com/vi/x449QQDhMDE/0.jpg)](https://www.youtube.com/watch?v=x449QQDhMDE)

``` python
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
```

>     Using TensorFlow backend.
>     Train on 54000 samples, validate on 6000 samples
>     Epoch 1/40
>     1s - loss: 0.4799 - categorical_accuracy: 0.8438 - val_loss: 0.1881 - val_categorical_accuracy: 0.9443
>     Epoch 2/40
>     1s - loss: 0.2151 - categorical_accuracy: 0.9355 - val_loss: 0.1650 - val_categorical_accuracy: 0.9503
>     Epoch 3/40
>     1s - loss: 0.1753 - categorical_accuracy: 0.9484 - val_loss: 0.1367 - val_categorical_accuracy: 0.9587
>     Epoch 4/40
>     1s - loss: 0.1533 - categorical_accuracy: 0.9543 - val_loss: 0.1321 - val_categorical_accuracy: 0.9627
>     Epoch 5/40
>     1s - loss: 0.1386 - categorical_accuracy: 0.9587 - val_loss: 0.1321 - val_categorical_accuracy: 0.9618
>     Epoch 6/40
>     1s - loss: 0.1287 - categorical_accuracy: 0.9612 - val_loss: 0.1332 - val_categorical_accuracy: 0.9613
>     Epoch 7/40
>     1s - loss: 0.1199 - categorical_accuracy: 0.9643 - val_loss: 0.1339 - val_categorical_accuracy: 0.9602
>     Epoch 8/40
>     1s - loss: 0.1158 - categorical_accuracy: 0.9642 - val_loss: 0.1220 - val_categorical_accuracy: 0.9658
>     Epoch 9/40
>     1s - loss: 0.1099 - categorical_accuracy: 0.9660 - val_loss: 0.1342 - val_categorical_accuracy: 0.9648
>     Epoch 10/40
>     1s - loss: 0.1056 - categorical_accuracy: 0.9675 - val_loss: 0.1344 - val_categorical_accuracy: 0.9622
>     Epoch 11/40
>     1s - loss: 0.0989 - categorical_accuracy: 0.9691 - val_loss: 0.1266 - val_categorical_accuracy: 0.9643
>     Epoch 12/40
>     1s - loss: 0.0982 - categorical_accuracy: 0.9699 - val_loss: 0.1226 - val_categorical_accuracy: 0.9650
>     Epoch 13/40
>     1s - loss: 0.0945 - categorical_accuracy: 0.9717 - val_loss: 0.1274 - val_categorical_accuracy: 0.9648
>     Epoch 14/40
>     1s - loss: 0.0915 - categorical_accuracy: 0.9711 - val_loss: 0.1374 - val_categorical_accuracy: 0.9637
>     Epoch 15/40
>     1s - loss: 0.0885 - categorical_accuracy: 0.9719 - val_loss: 0.1541 - val_categorical_accuracy: 0.9590
>     Epoch 16/40
>     1s - loss: 0.0845 - categorical_accuracy: 0.9736 - val_loss: 0.1307 - val_categorical_accuracy: 0.9658
>     Epoch 17/40
>     1s - loss: 0.0835 - categorical_accuracy: 0.9742 - val_loss: 0.1451 - val_categorical_accuracy: 0.9613
>     Epoch 18/40
>     1s - loss: 0.0801 - categorical_accuracy: 0.9749 - val_loss: 0.1352 - val_categorical_accuracy: 0.9647
>     Epoch 19/40
>     1s - loss: 0.0780 - categorical_accuracy: 0.9754 - val_loss: 0.1343 - val_categorical_accuracy: 0.9638
>     Epoch 20/40
>     1s - loss: 0.0800 - categorical_accuracy: 0.9746 - val_loss: 0.1306 - val_categorical_accuracy: 0.9682
>     Epoch 21/40
>     1s - loss: 0.0745 - categorical_accuracy: 0.9765 - val_loss: 0.1465 - val_categorical_accuracy: 0.9665
>     Epoch 22/40
>     1s - loss: 0.0722 - categorical_accuracy: 0.9769 - val_loss: 0.1390 - val_categorical_accuracy: 0.9657
>     Epoch 23/40
>     1s - loss: 0.0728 - categorical_accuracy: 0.9771 - val_loss: 0.1511 - val_categorical_accuracy: 0.9633
>     Epoch 24/40
>     1s - loss: 0.0717 - categorical_accuracy: 0.9772 - val_loss: 0.1515 - val_categorical_accuracy: 0.9638
>     Epoch 25/40
>     1s - loss: 0.0686 - categorical_accuracy: 0.9784 - val_loss: 0.1670 - val_categorical_accuracy: 0.9618
>     Epoch 26/40
>     1s - loss: 0.0666 - categorical_accuracy: 0.9789 - val_loss: 0.1446 - val_categorical_accuracy: 0.9655
>     Epoch 27/40
>     1s - loss: 0.0663 - categorical_accuracy: 0.9789 - val_loss: 0.1537 - val_categorical_accuracy: 0.9623
>     Epoch 28/40
>     1s - loss: 0.0671 - categorical_accuracy: 0.9792 - val_loss: 0.1541 - val_categorical_accuracy: 0.9677
>     Epoch 29/40
>     1s - loss: 0.0630 - categorical_accuracy: 0.9804 - val_loss: 0.1642 - val_categorical_accuracy: 0.9632
>     Epoch 30/40
>     1s - loss: 0.0660 - categorical_accuracy: 0.9797 - val_loss: 0.1585 - val_categorical_accuracy: 0.9648
>     Epoch 31/40
>     1s - loss: 0.0646 - categorical_accuracy: 0.9801 - val_loss: 0.1574 - val_categorical_accuracy: 0.9640
>     Epoch 32/40
>     1s - loss: 0.0640 - categorical_accuracy: 0.9789 - val_loss: 0.1506 - val_categorical_accuracy: 0.9657
>     Epoch 33/40
>     1s - loss: 0.0623 - categorical_accuracy: 0.9801 - val_loss: 0.1583 - val_categorical_accuracy: 0.9662
>     Epoch 34/40
>     1s - loss: 0.0630 - categorical_accuracy: 0.9803 - val_loss: 0.1626 - val_categorical_accuracy: 0.9630
>     Epoch 35/40
>     1s - loss: 0.0579 - categorical_accuracy: 0.9815 - val_loss: 0.1647 - val_categorical_accuracy: 0.9640
>     Epoch 36/40
>     1s - loss: 0.0605 - categorical_accuracy: 0.9815 - val_loss: 0.1693 - val_categorical_accuracy: 0.9635
>     Epoch 37/40
>     1s - loss: 0.0586 - categorical_accuracy: 0.9821 - val_loss: 0.1753 - val_categorical_accuracy: 0.9635
>     Epoch 38/40
>     1s - loss: 0.0583 - categorical_accuracy: 0.9812 - val_loss: 0.1703 - val_categorical_accuracy: 0.9653
>     Epoch 39/40
>     1s - loss: 0.0593 - categorical_accuracy: 0.9807 - val_loss: 0.1736 - val_categorical_accuracy: 0.9638
>     Epoch 40/40
>     1s - loss: 0.0538 - categorical_accuracy: 0.9826 - val_loss: 0.1817 - val_categorical_accuracy: 0.9635
>        32/10000 [..............................] - ETA: 0s 1952/10000 [====>.........................] - ETA: 0s 3872/10000 [==========>...................] - ETA: 0s 5792/10000 [================>.............] - ETA: 0s 7712/10000 [======================>.......] - ETA: 0s 9632/10000 [===========================>..] - ETA: 0s
>     loss: 0.205790
>     categorical_accuracy: 0.959600
>     Start: 2017-12-07 09:30:17.311756
>     End: 2017-12-07 09:31:14.504506
>     Elapse: 0:00:57.192750

after about a minute we have:

\`\`\`
...

Epoch 40/40
1s - loss: 0.0610 - categorical*accuracy: 0.9809 - val*loss: 0.1918 - val*categorical*accuracy: 0.9583

...

loss: 0.216120

categorical\_accuracy: 0.955000

Start: 2017-12-06 07:35:33.948102

End: 2017-12-06 07:36:27.046130

Elapse: 0:00:53.098028
\`\`\`

``` python
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
```

What are the big takeaways from this experiment?

1.  We get pretty impressive "apparent error" accuracy right from the start! A small network gets us to training accuracy 97% by epoch 20
2.  The model *appears* to continue to learn if we let it run, although it does slow down and oscillate a bit.
3.  Our test accuracy is about 95% after 5 epochs and never gets better ... it gets worse!
4.  Therefore, we are overfitting very quickly... most of the "training" turns out to be a waste.
5.  For what it's worth, we get 95% accuracy without much work.

This is not terrible compared to other, non-neural-network approaches to the problem. After all, we could probably tweak this a bit and do even better.

But we talked about using deep learning to solve "95%" problems or "98%" problems ... where one error in 20, or 50 simply won't work. If we can get to "multiple nines" of accuracy, then we can do things like automate mail sorting and translation, create cars that react properly (all the time) to street signs, and control systems for robots or drones that function autonomously.

Try two more experiments (try them separately):
1. Add a third, hidden layer.
2. Increase the size of the hidden layers.

Adding another layer slows things down a little (why?) but doesn't seem to make a difference in accuracy.

Adding a lot more neurons into the first topology slows things down significantly -- 10x as many neurons, and only a marginal increase in accuracy. Notice also (in the plot) that the learning clearly degrades after epoch 50 or so.

... We need a new approach!

------------------------------------------------------------------------

... let's think about this:

### What is layer 2 learning from layer 1? Combinations of pixels

#### Combinations of pixels contain information but...

There are a lot of them (combinations) and they are "fragile"

In fact, in our last experiment, we basically built a model that memorizes a bunch of "magic" pixel combinations.

What might be a better way to build features?

-   When humans perform this task, we look not at arbitrary pixel combinations, but certain geometric patterns -- lines, curves, loops.
-   These features are made up of combinations of pixels, but they are far from arbitrary
-   We identify these features regardless of translation, rotation, etc.

Is there a way to get the network to do the same thing?

I.e., in layer one, identify pixels. Then in layer 2+, identify abstractions over pixels that are translation-invariant 2-D shapes?

We could look at where a "filter" that represents one of these features (e.g., and edge) matches the image.

How would this work?

### Convolution

Convolution in the general mathematical sense is define as follows:

<img src="https://i.imgur.com/lurC2Cx.png" width=300>

The convolution we deal with in deep learning is a simplified case. We want to compare two signals. Here are two visualizations, courtesy of Wikipedia, that help communicate how convolution emphasizes features:

<img src="http://i.imgur.com/EDCaMl2.png" width=500>

------------------------------------------------------------------------

#### Here's an animation (where we change \\({\tau}\\))

<img src="http://i.imgur.com/0BFcnaw.gif">

**In one sense, the convolution captures and quantifies the pattern matching over space**

If we perform this in two dimensions, we can achieve effects like highlighting edges:

<img src="http://i.imgur.com/DKEXIII.png">

The matrix here, also called a convolution kernel, is one of the functions we are convolving. Other convolution kernels can blur, "sharpen," etc.

### So we'll drop in a number of convolution kernels, and the network will learn where to use them? Nope. Better than that.

We'll program in the *idea* of discrete convolution, and the network will learn what kernels extract meaningful features!
-------------------------------------------------------------------------------------------------------------------------

The values in a (fixed-size) convolution kernel matrix will be variables in our deep learning model. Although inuitively it seems like it would be hard to learn useful params, in fact, since those variables are used repeatedly across the image data, it "focuses" the error on a smallish number of parameters with a lot of influence -- so it should be vastly *less* expensive to train than just a huge fully connected layer like we discussed above.

This idea was developed in the late 1980s, and by 1989, Yann LeCun (at AT&T/Bell Labs) had built a practical high-accuracy system (used in the 1990s for processing handwritten checks and mail).

**How do we hook this into our neural networks?**

-   First, we can preserve the geometric properties of our data by "shaping" the vectors as 2D instead of 1D.

-   Then we'll create a layer whose value is not just activation applied to weighted sum of inputs, but instead it's the result of a dot-product (element-wise multiply and sum) between the kernel and a patch of the input vector (image).
    -   This value will be our "pre-activation" and optionally feed into an activation function (or "detector")

<img src="http://i.imgur.com/ECyi9lL.png">

If we perform this operation at lots of positions over the image, we'll get lots of outputs, as many as one for every input pixel.

<img src="http://i.imgur.com/WhOrJ0Y.jpg">

-   So we'll add another layer that "picks" the highest convolution pattern match from nearby pixels, which
    -   makes our pattern match a little bit translation invariant (a fuzzy location match)
    -   reduces the number of outputs significantly
-   This layer is commonly called a pooling layer, and if we pick the "maximum match" then it's a "max pooling" layer.

<img src="http://i.imgur.com/9iPpfpb.png">

**The end result is that the kernel or filter together with max pooling creates a value in a subsequent layer which represents the appearance of a pattern in a local area in a prior layer.**

**Again, the network will be given a number of "slots" for these filters and will learn (by minimizing error) what filter values produce meaningful features. This is the key insight into how modern image-recognition networks are able to generalize -- i.e., learn to tell 6s from 7s or cats from dogs.**

<img src="http://i.imgur.com/F8eH3vj.png">

Ok, let's build our first ConvNet:
----------------------------------

First, we want to explicity shape our data into a 2-D configuration. We'll end up with a 4-D tensor where the first dimension is the training examples, then each example is 28x28 pixels, and we'll explicitly say it's 1-layer deep. (Why? with color images, we typically process over 3 or 4 channels in this last dimension)

A step by step animation follows:
\* http://cs231n.github.io/assets/conv-demo/index.html

``` python
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
```

Now the model:

``` python
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
```

... and the training loop and output:

``` python
start = datetime.datetime.today()

history = model.fit(X_train, y_train, batch_size=128, epochs=8, verbose=2, validation_split=0.1)

scores = model.evaluate(X_test, y_test, verbose=1)

print
for i in range(len(model.metrics_names)):
	print("%s: %f" % (model.metrics_names[i], scores[i]))
```

>     Train on 54000 samples, validate on 6000 samples
>     Epoch 1/8
>     12s - loss: 0.3183 - acc: 0.9115 - val_loss: 0.1114 - val_acc: 0.9713
>     Epoch 2/8
>     13s - loss: 0.1021 - acc: 0.9700 - val_loss: 0.0714 - val_acc: 0.9803
>     Epoch 3/8
>     12s - loss: 0.0706 - acc: 0.9792 - val_loss: 0.0574 - val_acc: 0.9852
>     Epoch 4/8
>     15s - loss: 0.0518 - acc: 0.9851 - val_loss: 0.0538 - val_acc: 0.9857
>     Epoch 5/8
>     16s - loss: 0.0419 - acc: 0.9875 - val_loss: 0.0457 - val_acc: 0.9872
>     Epoch 6/8
>     16s - loss: 0.0348 - acc: 0.9896 - val_loss: 0.0473 - val_acc: 0.9867
>     Epoch 7/8
>     16s - loss: 0.0280 - acc: 0.9917 - val_loss: 0.0445 - val_acc: 0.9875
>     Epoch 8/8
>     14s - loss: 0.0225 - acc: 0.9934 - val_loss: 0.0485 - val_acc: 0.9868
>        32/10000 [..............................] - ETA: 0s  672/10000 [=>............................] - ETA: 0s 1312/10000 [==>...........................] - ETA: 0s 1952/10000 [====>.........................] - ETA: 0s 2560/10000 [======>.......................] - ETA: 0s 3072/10000 [========>.....................] - ETA: 0s 3712/10000 [==========>...................] - ETA: 0s 4384/10000 [============>.................] - ETA: 0s 5024/10000 [==============>...............] - ETA: 0s 5632/10000 [===============>..............] - ETA: 0s 6304/10000 [=================>............] - ETA: 0s 6976/10000 [===================>..........] - ETA: 0s 7616/10000 [=====================>........] - ETA: 0s 8256/10000 [=======================>......] - ETA: 0s 8960/10000 [=========================>....] - ETA: 0s 9600/10000 [===========================>..] - ETA: 0s
>     loss: 0.054931
>     acc: 0.983000

``` python
fig, ax = plt.subplots()
fig.set_size_inches((5,5))
plt.plot(history.history['loss'])
plt.plot(history.history['val_loss'])
plt.title('model loss')
plt.ylabel('loss')
plt.xlabel('epoch')
plt.legend(['train', 'val'], loc='upper left')
display(fig)
```

### Our MNIST ConvNet

In our first convolutional MNIST experiment, we get to almost 99% validation accuracy in just a few epochs (a minutes or so on CPU)!

The training accuracy is effectively 100%, though, so we've almost completely overfit (i.e., memorized the training data) by this point and need to do a little work if we want to keep learning.

Let's add another convolutional layer:

``` python
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
```

>     Train on 54000 samples, validate on 6000 samples
>     Epoch 1/15
>     21s - loss: 0.2698 - acc: 0.9242 - val_loss: 0.0763 - val_acc: 0.9790
>     Epoch 2/15
>     22s - loss: 0.0733 - acc: 0.9772 - val_loss: 0.0593 - val_acc: 0.9840
>     Epoch 3/15
>     21s - loss: 0.0522 - acc: 0.9838 - val_loss: 0.0479 - val_acc: 0.9867
>     Epoch 4/15
>     21s - loss: 0.0394 - acc: 0.9876 - val_loss: 0.0537 - val_acc: 0.9828
>     Epoch 5/15
>     21s - loss: 0.0301 - acc: 0.9907 - val_loss: 0.0434 - val_acc: 0.9885
>     Epoch 6/15
>     21s - loss: 0.0255 - acc: 0.9916 - val_loss: 0.0444 - val_acc: 0.9867
>     Epoch 7/15
>     21s - loss: 0.0192 - acc: 0.9941 - val_loss: 0.0416 - val_acc: 0.9892
>     Epoch 8/15
>     21s - loss: 0.0155 - acc: 0.9952 - val_loss: 0.0405 - val_acc: 0.9900
>     Epoch 9/15
>     21s - loss: 0.0143 - acc: 0.9953 - val_loss: 0.0490 - val_acc: 0.9860
>     Epoch 10/15
>     22s - loss: 0.0122 - acc: 0.9963 - val_loss: 0.0478 - val_acc: 0.9877
>     Epoch 11/15
>     21s - loss: 0.0087 - acc: 0.9972 - val_loss: 0.0508 - val_acc: 0.9877
>     Epoch 12/15
>     21s - loss: 0.0098 - acc: 0.9967 - val_loss: 0.0401 - val_acc: 0.9902
>     Epoch 13/15
>     21s - loss: 0.0060 - acc: 0.9984 - val_loss: 0.0450 - val_acc: 0.9897
>     Epoch 14/15
>     21s - loss: 0.0061 - acc: 0.9981 - val_loss: 0.0523 - val_acc: 0.9883
>     Epoch 15/15
>     21s - loss: 0.0069 - acc: 0.9977 - val_loss: 0.0534 - val_acc: 0.9885
>        32/10000 [..............................] - ETA: 1s  448/10000 [>.............................] - ETA: 1s  864/10000 [=>............................] - ETA: 1s 1280/10000 [==>...........................] - ETA: 1s 1696/10000 [====>.........................] - ETA: 1s 2112/10000 [=====>........................] - ETA: 0s 2560/10000 [======>.......................] - ETA: 0s 2976/10000 [=======>......................] - ETA: 0s 3392/10000 [=========>....................] - ETA: 0s 3840/10000 [==========>...................] - ETA: 0s 4320/10000 [===========>..................] - ETA: 0s 4736/10000 [=============>................] - ETA: 0s 5152/10000 [==============>...............] - ETA: 0s 5568/10000 [===============>..............] - ETA: 0s 5952/10000 [================>.............] - ETA: 0s 6368/10000 [==================>...........] - ETA: 0s 6816/10000 [===================>..........] - ETA: 0s 7200/10000 [====================>.........] - ETA: 0s 7648/10000 [=====================>........] - ETA: 0s 8000/10000 [=======================>......] - ETA: 0s 8384/10000 [========================>.....] - ETA: 0s 8800/10000 [=========================>....] - ETA: 0s 9216/10000 [==========================>...] - ETA: 0s 9632/10000 [===========================>..] - ETA: 0s
>     loss: 0.043144
>     acc: 0.989100

While that's running, let's look at a number of "famous" convolutional networks!

### LeNet (Yann LeCun, 1998)

<img src="http://i.imgur.com/k5hMtMK.png">

<img src="http://i.imgur.com/ERV9pHW.gif">

<img src="http://i.imgur.com/TCN9C4P.png">

### AlexNet (2012)

<img src="http://i.imgur.com/CpokDKV.jpg">

<img src="http://i.imgur.com/Ld2QhXr.jpg">

### Back to our labs: Still Overfitting

We're making progress on our test error -- about 99% -- but just a bit for all the additional time, due to the network overfitting the data.

There are a variety of techniques we can take to counter this -- forms of regularization.

Let's try a relatively simple solution solution that works surprisingly well: add a pair of `Dropout` filters, a layer that randomly omits a fraction of neurons from each training batch (thus exposing each neuron to only part of the training data).

We'll add more convolution kernels but shrink them to 3x3 as well.

``` python
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
```

>     Epoch 1/15
>     122s - loss: 0.2673 - acc: 0.9186
>     Epoch 2/15
>     123s - loss: 0.0903 - acc: 0.9718
>     Epoch 3/15
>     124s - loss: 0.0696 - acc: 0.9787
>     Epoch 4/15
>     130s - loss: 0.0598 - acc: 0.9821
>     Epoch 5/15
>     140s - loss: 0.0507 - acc: 0.9842
>     Epoch 6/15
>     148s - loss: 0.0441 - acc: 0.9864
>     Epoch 7/15
>     152s - loss: 0.0390 - acc: 0.9878
>     Epoch 8/15
>     153s - loss: 0.0353 - acc: 0.9888
>     Epoch 9/15
>     161s - loss: 0.0329 - acc: 0.9894
>     Epoch 10/15
>     171s - loss: 0.0323 - acc: 0.9894
>     Epoch 11/15
>     172s - loss: 0.0288 - acc: 0.9906
>     Epoch 12/15
>     71s - loss: 0.0255 - acc: 0.9914
>     Epoch 13/15
>     86s - loss: 0.0245 - acc: 0.9916
>     Epoch 14/15
>     91s - loss: 0.0232 - acc: 0.9922
>     Epoch 15/15
>     86s - loss: 0.0218 - acc: 0.9931
>
>     loss: 0.029627
>     acc: 0.991400

While that's running, let's look at some more recent ConvNet architectures:

### VGG16 (2014)

<img src="http://i.imgur.com/gl4kZDf.png">

### GoogLeNet (2014)

<img src="http://i.imgur.com/hvmtDqN.png">

*"Inception" layer: parallel convolutions at different resolutions*

### Residual Networks (2015-)

Skip layers to improve training (error propagation). Residual layers learn from details at multiple previous layers.

<img src="http://i.imgur.com/32g8Ykl.png">

------------------------------------------------------------------------

> **ASIDE: Atrous / Dilated Convolutions**

> An atrous or dilated convolution is a convolution filter with "holes" in it. Effectively, it is a way to enlarge the filter spatially while not adding as many parameters or attending to every element in the input.

> Why? Covering a larger input volume allows recognizing coarser-grained patterns; restricting the number of parameters is a way of regularizing or constraining the capacity of the model, making training easier.

------------------------------------------------------------------------

*Lab Wrapup*
------------

From the last lab, you should have a test accuracy of over 99.1%

For one more activity, try changing the optimizer to old-school "sgd" -- just to see how far we've come with these modern gradient descent techniques in the last few years.

Accuracy will end up noticeably worse ... about 96-97% test accuracy. Two key takeaways:

-   Without a good optimizer, even a very powerful network design may not achieve results
-   In fact, we could replace the word "optimizer" there with
    -   initialization
    -   activation
    -   regularization
    -   (etc.)
-   All of these elements we've been working with operate together in a complex way to determine final performance

Of course this world evolves fast - see the new kid in the CNN block -- **capsule networks**

> Hinton: “The pooling operation used in convolutional neural networks is a big mistake and the fact that it works so well is a disaster.”

Well worth the 8 minute read:
\* <https://medium.com/ai%C2%B3-theory-practice-business/understanding-hintons-capsule-networks-part-i-intuition-b4b559d1159b>

To understand deeper:
\* original paper: <https://arxiv.org/abs/1710.09829>