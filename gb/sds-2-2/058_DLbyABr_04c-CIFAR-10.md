[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

This is used in a non-profit educational setting with kind permission of [Adam Breindel](https://www.linkedin.com/in/adbreind). This is not licensed by Adam for use in a for-profit setting. Please contact Adam directly at `adbreind@gmail.com` to request or report such use cases or abuses. A few minor modifications and additional mathematical statistical pointers have been added by Raazesh Sainudiin when teaching PhD students in Uppsala University.

``` md #CIFAR 10

Details at: https://www.cs.toronto.edu/~kriz/cifar.html

Summary (taken from that page): 

The CIFAR-10 and CIFAR-100 are labeled subsets of the 80 million tiny images dataset. They were collected by Alex Krizhevsky, Vinod Nair, and Geoffrey Hinton. The CIFAR-10 dataset consists of 60000 32x32 colour images in 10 classes, with 6000 images per class. There are 50000 training images and 10000 test images. 

The dataset is divided into five training batches and one test batch, each with 10000 images. The test batch contains exactly 1000 randomly-selected images from each class. The training batches contain the remaining images in random order, but some training batches may contain more images from one class than another. Between them, the training batches contain exactly 5000 images from each class.

First, we'll mount the S3 bucket where I'm hosting the data:
```

``` python
# you may have to host the data yourself! - this should not work unless you can descramble
ACCESS="...SPORAA...KIAJZEH...PW46CWPUWUN...QPODO" # scrambled up
SECRET="...P7d7Sp7r1...Q9DuUvV...QAy1D+hjC...NxakJF+PXrAb...MXD1tZwBpGyN...1Ns5r5n1" # scrambled up
BUCKET = "cool-data"
MOUNT = "/mnt/cifar"

try:
  dbutils.fs.mount("s3a://"+ ACCESS + ":" + SECRET + "@" + BUCKET, MOUNT)
except:
  print("Error mounting ... possibly already mounted")
```

``` md This is in DBFS, which is available (via FUSE) at /dbfs ...

So the CIFAR data can be listed through following regular Linux shell command:
```

>     total 0
>     drwxr-xr-x 1 root root        0 Jan  1  1970 .
>     drwxr-xr-x 1 root root        0 Jan  1  1970 ..
>     -rw-r--r-- 1 root root      158 Jan  1  1970 batches.meta
>     -rw-r--r-- 1 root root 31035704 Jan  1  1970 data_batch_1
>     -rw-r--r-- 1 root root 31035320 Jan  1  1970 data_batch_2
>     -rw-r--r-- 1 root root 31035999 Jan  1  1970 data_batch_3
>     -rw-r--r-- 1 root root 31035696 Jan  1  1970 data_batch_4
>     -rw-r--r-- 1 root root 31035623 Jan  1  1970 data_batch_5
>     -rw-r--r-- 1 root root       88 Jan  1  1970 readme.html
>     -rw-r--r-- 1 root root 31035526 Jan  1  1970 test_batch

``` md Recall the classes are: __airplane, automobile, bird, cat, deer, dog, frog, horse, ship, truck__

Here is the code to unpickle the batches. 

Loaded in this way, each of the batch files contains a dictionary with the following elements:
* data - a 10000x3072 numpy array of uint8s. Each row of the array stores a 32x32 colour image. The first 1024 entries contain the red channel values, the next 1024 the green, and the final 1024 the blue. The image is stored in row-major order, so that the first 32 entries of the array are the red channel values of the first row of the image.
* labels - a list of 10000 numbers in the range 0-9. The number at index i indicates the label of the ith image in the array data.
```

``` python
def unpickle(file):
    import pickle
    with open(file, 'rb') as fo:
        dict = pickle.load(fo)# for Python 3, add the following param: encoding='bytes'
    return dict

dir = '/dbfs/mnt/cifar/batches/'

batches = [unpickle(dir + 'data_batch_' + str(1+n)) for n in range(5)]
```

``` python
import numpy as np

def decode(xy):
  x_train = np.reshape(xy[b'data'], (10000, 3, 32, 32)).transpose(0, 2, 3, 1)
  y_train = np.reshape(xy[b'labels'], (10000, 1))
  return (x_train, y_train)

decoded = [decode(data) for data in batches]

x_train = np.concatenate([data[0] for data in decoded])
y_train = np.concatenate([data[1] for data in decoded])

(x_test, y_test) = decode(unpickle(dir + 'test_batch'))

print('x_train shape:', x_train.shape)
print(x_train.shape[0], 'train samples')
print(x_test.shape[0], 'test samples')
```

>     ('x_train shape:', (50000, 32, 32, 3))
>     (50000, 'train samples')
>     (10000, 'test samples')

``` python
import matplotlib.pyplot as plt

fig = plt.figure()
for i in range(36):
  fig.add_subplot(6, 6, i+1)
  plt.imshow(x_train[i])

display(fig)
```

``` md Recall that we are getting a categorical output via softmax across 10 neurons, corresponding to the output categories.

So we want to reshape our target values (training labels) to be 1-hot encoded, and Keras can calculate categorical crossentropy between its output layer and the target:
```

``` python
import keras
from keras.models import Sequential
from keras.layers import Dense, Dropout, Activation, Flatten
from keras.layers import Conv2D, MaxPooling2D

num_classes = 10

# Convert class vectors to binary class matrices.
y_train_1hot = keras.utils.to_categorical(y_train, num_classes)
y_test_1hot = keras.utils.to_categorical(y_test, num_classes)
```

>     Using TensorFlow backend.

``` md Here's a simple convolutional net to get you started. It will get you to over 57% accuracy in 5 epochs.

As inspiration, with a suitable network and parameters, it's possible to get over 99% test accuracy, although you won't have time to get there in today's session on this hardware.

*note: if your network is not learning anything at all -- meaning regardless of settings, you're seeing a loss that doesn't decrease and a validation accuracy that is 10% (i.e., random chance) -- then restart your cluster*
```

``` python
model = Sequential()

model.add(Conv2D(32, (3, 3), padding='same', input_shape=x_train.shape[1:]))
model.add(Activation('relu'))

model.add(Flatten())
model.add(Dense(64))
model.add(Activation('relu'))
model.add(Dense(num_classes))
model.add(Activation('softmax'))

model.compile(loss='categorical_crossentropy', optimizer="adam", metrics=['accuracy'])

x_train = x_train.astype('float32')
x_test = x_test.astype('float32')
x_train /= 255
x_test /= 255

history = model.fit(x_train, y_train_1hot,
              batch_size=64,
              epochs=5,
              validation_data=(x_test, y_test_1hot),
              verbose=2)
```

>     Train on 50000 samples, validate on 10000 samples
>     Epoch 1/5
>     42s - loss: 1.6253 - acc: 0.4202 - val_loss: 1.3802 - val_acc: 0.5053
>     Epoch 2/5
>     42s - loss: 1.2808 - acc: 0.5467 - val_loss: 1.2657 - val_acc: 0.5493
>     Epoch 3/5
>     42s - loss: 1.1541 - acc: 0.5940 - val_loss: 1.2050 - val_acc: 0.5742
>     Epoch 4/5
>     42s - loss: 1.0697 - acc: 0.6235 - val_loss: 1.2062 - val_acc: 0.5742
>     Epoch 5/5
>     42s - loss: 0.9984 - acc: 0.6496 - val_loss: 1.1442 - val_acc: 0.6008

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

