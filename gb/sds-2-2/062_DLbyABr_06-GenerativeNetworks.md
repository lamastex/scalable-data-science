Generative Networks
===================

### Concept:

If a set of network weights can convert an image of the numeral 8 or a cat <br/>into the classification "8" or "cat" ...

### Does it contain enough information to do the reverse?

I.e., can we ask a network what "8" looks like and get a picture?

Let's think about this for a second. Clearly the classifications have far fewer bits of entropy than the source images' theoretical limit.

-   Cat (in cat-vs-dog) has just 1 bit, where perhaps a 256x256 grayscale image has up to 512k bits.
-   8 (in MNIST) has \\({log \_2 10}\\) or a little over 3 bits, where a 28x28 grayscale image has over 6000 bits.

So at first, this would seem difficult or impossible.

**But** ... let's do a thought experiment.

-   Children can do this easily
-   We could create a lookup table of, say, digit -&gt; image trivially, and use that as a first approximation

Those approaches seem like cheating. But let's think about how they work.

If a child (or adult) draws a cat (or number 8), they are probably not drawing any specific cat (or 8). They are drawing a general approximation of a cat based on 1. All of the cats they've seen 2. What they remember as the key elements of a cat (4 legs, tail, pointy ears) 3. A lookup table substitutes one specific cat or 8 ... and, especially in the case of the 8, that may be fine. The only thing we "lose" is the diversity of things that all mapped to cat (or 8) -- and discarding that information was exactly our goal when building a classifier

The "8" is even simpler: we learn that a number is an idea, not a specific instance, so anything that another human recognizes as 8 is good enough. We are not even trying to make a particular shape, just one that represents our encoded information that distinguishes 8 from other possible symbols in the context and the eye of the viewer.

This should remind you a bit of the KL Divergence we talked about at the start: we are providing just enough information (entropy or surprise) to distinguish the cat or "8" from the other items that a human receiver might be expecting to see.

And where do our handwritten "pixels" come from? No image in particular -- they are totally synthetic based on a probability distribution.

*These considerations make it sound more likely that a computer could perform the same task.*

But what would the weights really represent? what could they generate?

**The weights we learn in classification represent the distinguishing features of a class, across all training examples of the class, modified to overlap minimally with features of other classes.** KL divergence again.

To be concrete, if we trained a model on just a few dozen MNIST images using pixels, it would probably learn the 3 or 4 "magic" pixels that *happened* to distinguish the 8s in that dataset. Trying to generate from that information would yield strong confidence about those magic pixels, but would look like dots to us humans.

On the other hand, if we trained on a very large number of MNIST images -- say we use the convnet this time -- the model's weights should represent general filters of masks for features that distinguish an 8. And if we try to reverse the process by amplifying just those filters, we should get a blurry statistical distribution of those very features. The approximate shape of the Platonic "8"!

``` md ## Mechanically, How Could This Work?

Let's start with a simpler model called an auto-encoder.

An autoencoder's job is to take a large representation of a record and find weights that represent that record in a smaller encoding, subject to the constraint that the decoded version should match the original as closely as possible.

A bit like training a JPEG encoder to compress images by scoring it with the loss between the original image and the decompressed version of the lossy compressed image.

<img src="http://i.imgur.com/oTRvlB6.png" width=450>

One nice aspect of this is that it is *unsupervised* -- i.e., we do not need any ground-truth or human-generated labels in order to find the error and train. The error is always the difference between the output and the input, and the goal is to minimize this over many examples, thus minimize in the general case.

We can do this with a simple multilayer perceptron network. Or, we can get fancier and do this with a convolutional network. In reverse, the convolution (typically called "transposed convolution" or "deconvolution") is an upsampling operation across space (in images) or space & time (in audio/video).
```

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
model.add(Dense(30, input_dim=784, kernel_initializer='normal', activation='relu'))
model.add(Dense(784, kernel_initializer='normal', activation='relu'))
model.compile(loss='mean_squared_error', optimizer='adam', metrics=['mean_squared_error', 'binary_crossentropy'])

start = datetime.datetime.today()

history = model.fit(X_train, X_train, epochs=5, batch_size=100, validation_split=0.1, verbose=2)

scores = model.evaluate(X_test, X_test)

print
for i in range(len(model.metrics_names)):
	print("%s: %f" % (model.metrics_names[i], scores[i]))

print ("Start: " + str(start))
end = datetime.datetime.today()
print ("End: " + str(end))
print ("Elapse: " + str(end-start))

fig, ax = plt.subplots()
fig.set_size_inches((4,4))
plt.plot(history.history['loss'])
plt.plot(history.history['val_loss'])
plt.title('model loss')
plt.ylabel('loss')
plt.xlabel('epoch')
plt.legend(['train', 'val'], loc='upper left')
display(fig)
```

``` python
fig.set_size_inches((5,5))
ax.imshow(np.reshape(X_test[61], (28,28)), cmap='gray')
display(fig)
```

``` python
encode_decode = model.predict(np.reshape(X_test[61], (1, 784)))
```

``` python
ax.imshow(np.reshape(encode_decode, (28,28)), cmap='gray')
display(fig)
```

``` md ### Pretty cool. So we're all done now, right? Now quite...

The problem with the autoencoder is it's "too good" at its task.

It is optimized to compress exactly the input record set, so it is trained only to create records it has seen. If the middle layer, or information bottleneck, is tight enough, the coded records use all of the information space in the middle layer.

So any value in the middle layer decodes to exactly one already-seen exemplar.

In our example, and most autoencoders, there is more space in the middle layer but the coded values are not distributed in any sensible way. So we can decode a random vector and we'll probably just get garbage.
```

``` python
v = np.random.randn(30)
v = np.array(np.reshape(v, (1, 30)))
```

``` python
import tensorflow as tf
t = tf.convert_to_tensor(v, dtype='float32')
```

``` python
out = model.layers[1](t)
```

``` python
from keras import backend as K

with K.get_session().as_default():
    output = out.eval()
    ax.imshow(np.reshape(output, (28,28)), cmap='gray')
    display(fig)
    
```

``` md ### The Goal is to Generate a Variety of New Output From a Variety of New Inputs
... Where the Class/Category is Common (i.e., all 8s or Cats)

Some considerations:

* Is "generative content" something new? Or something true?
    * In a Platonic sense, maybe, but in reality it's literally a probabilistic guess based on the training data!
    * E.g., law enforcement photo enhancment
    
* How do we train?
    * If we score directly against the training data (like in the autoencoder), the network will be very conservative, generating only examples that it has seen.
    * In extreme cases, it will always generate a single (or small number) of examples, since those score well. This is known as __mode collapse__, since the network learns to locate the modes in the input distribution.
    
### Two principal approaches / architectures (2015-)
    
__Generative Adversarial Networks (GAN)__ and __Variational Autoencoders (VAE)__
```

``` md ## Variational Autoencoder (VAE)

Our autoencoder was able to generate images, but the problem was that arbitrary input vectors don't map to anything meaningful. As discussed, this is partly by design -- the training of the VAE is for effectively for compressing a specific input dataset.

What we would like, is that if we start with a valid input vector and move a bit on some direction, we get a plausible output that is also changed in some way.

---
> __ASIDE: Manifold Hypothesis__

> The manifold hypothesis is that the interesting, relevant, or critical subspaces in the space of all vector inputs are actually low(er) dimensional manifolds. A manifold is a space where each point has a neighborhood that behaves like (is homeomorphic to) \\({\Bbb R^n}\\). So we would like to be able to move a small amount and have only a small amount of change, not a sudden discontinuous change.

---

The key feature of Variational Autoencoders is that we add a constraint on the encoded representation of our data: namely, that it follows a Gaussian distribution. Since the Gaussian is determined by its mean and variance (or standard deviation), we can model it as a k-variate Gaussian with these two parameters (\\({\mu}\\) and \\({\sigma}\\)) for each value of k.

<img src="http://i.imgur.com/OFLDweH.jpg" width=600>
<div style="text-align: right"><sup>(credit to Miram Shiffman, http://blog.fastforwardlabs.com/2016/08/22/under-the-hood-of-the-variational-autoencoder-in.html)</sup></div>

<img src="http://i.imgur.com/LbvJI5q.jpg">
<div style="text-align: right"><sup>(credit to Kevin Franz, http://kvfrans.com/variational-autoencoders-explained/)</sup></div>

One challenge is how to balance accurate reproduction of the input (traditional autoencoder loss) with the requirement that we match a Gaussian distribution. We can force the network to optimize both of these goals by creating a custom error function that sums up two components:
* How well we match the input, calculated as binary crossentropy or MSE loss
* How well we match a Gaussian, calculated as KL divergence from the Gaussian distribution

We can easily implement a custom loss function and pass it as a parameter to the optimizer in Keras.

The Keras source examples folder contains an elegant simple implementation, which we'll discuss below. It's a little more complex than the code we've seen so far, but we'll clarify the innovations:

* Custom loss functions that combined KL divergence and cross-entropy loss
* Custom "Lambda" layer that provides the sampling from the encoded distribution

Overall it's probably simpler than you might expect. Let's start it (since it takes a few minutes to train) and discuss the code:
```

``` python
import numpy as np
import matplotlib.pyplot as plt
from scipy.stats import norm

from keras.layers import Input, Dense, Lambda
from keras.models import Model
from keras import backend as K
from keras import objectives
from keras.datasets import mnist
import sklearn.datasets

batch_size = 100
original_dim = 784
latent_dim = 2
intermediate_dim = 256
nb_epoch = 50
epsilon_std = 1.0

x = Input(batch_shape=(batch_size, original_dim))
h = Dense(intermediate_dim, activation='relu')(x)
z_mean = Dense(latent_dim)(h)
z_log_var = Dense(latent_dim)(h)

def sampling(args):
    z_mean, z_log_var = args
    epsilon = K.random_normal(shape=(batch_size, latent_dim), mean=0.,
                              stddev=epsilon_std)
    return z_mean + K.exp(z_log_var / 2) * epsilon

# note that "output_shape" isn't necessary with the TensorFlow backend
z = Lambda(sampling, output_shape=(latent_dim,))([z_mean, z_log_var])

# we instantiate these layers separately so as to reuse them later
decoder_h = Dense(intermediate_dim, activation='relu')
decoder_mean = Dense(original_dim, activation='sigmoid')
h_decoded = decoder_h(z)
x_decoded_mean = decoder_mean(h_decoded)

def vae_loss(x, x_decoded_mean):
    xent_loss = original_dim * objectives.binary_crossentropy(x, x_decoded_mean)
    kl_loss = - 0.5 * K.sum(1 + z_log_var - K.square(z_mean) - K.exp(z_log_var), axis=-1)
    return xent_loss + kl_loss

vae = Model(x, x_decoded_mean)
vae.compile(optimizer='rmsprop', loss=vae_loss)

train_libsvm = "/dbfs/databricks-datasets/mnist-digits/data-001/mnist-digits-train.txt"
test_libsvm = "/dbfs/databricks-datasets/mnist-digits/data-001/mnist-digits-test.txt"

x_train, y_train = sklearn.datasets.load_svmlight_file(train_libsvm, n_features=784)
x_train = x_train.toarray()
x_test, y_test = sklearn.datasets.load_svmlight_file(test_libsvm, n_features=784)
x_test = x_test.toarray()

x_train = x_train.astype('float32') / 255.
x_test = x_test.astype('float32') / 255.
x_train = x_train.reshape((len(x_train), np.prod(x_train.shape[1:])))
x_test = x_test.reshape((len(x_test), np.prod(x_test.shape[1:])))

vae.fit(x_train, x_train,
        shuffle=True,
        epochs=nb_epoch,
        batch_size=batch_size,
        validation_data=(x_test, x_test), verbose=2)

# build a model to project inputs on the latent space
encoder = Model(x, z_mean)
```

>     Train on 60000 samples, validate on 10000 samples
>     Epoch 1/50
>     7s - loss: 189.5566 - val_loss: 171.5702
>     Epoch 2/50
>     7s - loss: 170.1357 - val_loss: 168.0328
>     Epoch 3/50
>     7s - loss: 167.0335 - val_loss: 165.7566
>     Epoch 4/50
>     6s - loss: 164.9101 - val_loss: 163.9281
>     Epoch 5/50
>     7s - loss: 163.4593 - val_loss: 163.0182
>     Epoch 6/50
>     7s - loss: 162.3859 - val_loss: 162.1783
>     Epoch 7/50
>     6s - loss: 161.4627 - val_loss: 161.5879
>     Epoch 8/50
>     7s - loss: 160.6776 - val_loss: 160.6174
>     Epoch 9/50
>     7s - loss: 160.0041 - val_loss: 159.9206
>     Epoch 10/50
>     7s - loss: 159.4207 - val_loss: 159.6900
>     Epoch 11/50
>     6s - loss: 158.8789 - val_loss: 159.0153
>     Epoch 12/50
>     7s - loss: 158.4164 - val_loss: 158.4364
>     Epoch 13/50
>     7s - loss: 158.0075 - val_loss: 158.2960
>     Epoch 14/50
>     7s - loss: 157.5837 - val_loss: 158.1914
>     Epoch 15/50
>     7s - loss: 157.1951 - val_loss: 157.4292
>     Epoch 16/50
>     7s - loss: 156.8248 - val_loss: 157.0372
>     Epoch 17/50
>     6s - loss: 156.5035 - val_loss: 157.0941
>     Epoch 18/50
>     7s - loss: 156.1256 - val_loss: 156.6662
>     Epoch 19/50
>     7s - loss: 155.8246 - val_loss: 156.4601
>     Epoch 20/50
>     6s - loss: 155.5078 - val_loss: 156.0376
>     Epoch 21/50
>     7s - loss: 155.1785 - val_loss: 155.7231
>     Epoch 22/50
>     7s - loss: 154.8927 - val_loss: 155.6962
>     Epoch 23/50
>     7s - loss: 154.6112 - val_loss: 155.1788
>     Epoch 24/50
>     7s - loss: 154.3493 - val_loss: 155.1463
>     Epoch 25/50
>     7s - loss: 154.0696 - val_loss: 155.2021
>     Epoch 26/50
>     7s - loss: 153.8619 - val_loss: 154.6276
>     Epoch 27/50
>     7s - loss: 153.6298 - val_loss: 154.6071
>     Epoch 28/50
>     6s - loss: 153.4155 - val_loss: 154.6075
>     Epoch 29/50
>     7s - loss: 153.1875 - val_loss: 154.2212
>     Epoch 30/50
>     7s - loss: 153.0052 - val_loss: 154.0680
>     Epoch 31/50
>     6s - loss: 152.8207 - val_loss: 154.0867
>     Epoch 32/50
>     7s - loss: 152.6386 - val_loss: 154.1210
>     Epoch 33/50
>     7s - loss: 152.4709 - val_loss: 153.9641
>     Epoch 34/50
>     7s - loss: 152.3196 - val_loss: 153.5455
>     Epoch 35/50
>     7s - loss: 152.1351 - val_loss: 154.1738
>     Epoch 36/50
>     7s - loss: 152.0088 - val_loss: 153.5806
>     Epoch 37/50
>     7s - loss: 151.8396 - val_loss: 153.3500
>     Epoch 38/50
>     6s - loss: 151.7401 - val_loss: 153.3898
>     Epoch 39/50
>     7s - loss: 151.6202 - val_loss: 153.6636
>     Epoch 40/50
>     7s - loss: 151.4900 - val_loss: 153.0393
>     Epoch 41/50
>     7s - loss: 151.3560 - val_loss: 153.0634
>     Epoch 42/50
>     7s - loss: 151.2323 - val_loss: 153.1308
>     Epoch 43/50
>     6s - loss: 151.1024 - val_loss: 153.9157
>     Epoch 44/50
>     7s - loss: 151.0152 - val_loss: 152.6684
>     Epoch 45/50
>     7s - loss: 150.8696 - val_loss: 153.6238
>     Epoch 46/50
>     6s - loss: 150.7822 - val_loss: 153.0252
>     Epoch 47/50
>     7s - loss: 150.6835 - val_loss: 152.9415
>     Epoch 48/50
>     7s - loss: 150.6136 - val_loss: 153.5125
>     Epoch 49/50
>     7s - loss: 150.4753 - val_loss: 152.7204
>     Epoch 50/50
>     7s - loss: 150.4084 - val_loss: 153.0446

``` python
# display a 2D plot of the digit classes in the latent space
x_test_encoded = encoder.predict(x_test, batch_size=batch_size)
fig, ax = plt.subplots()
fig.set_size_inches((8,7))
plt.scatter(x_test_encoded[:, 0], x_test_encoded[:, 1], c=y_test)
plt.colorbar()
display(fig)
```

``` python
# build a digit generator that can sample from the learned distribution
decoder_input = Input(shape=(latent_dim,))
_h_decoded = decoder_h(decoder_input)
_x_decoded_mean = decoder_mean(_h_decoded)
generator = Model(decoder_input, _x_decoded_mean)

# display a 2D manifold of the digits
n = 15  # figure with 15x15 digits
digit_size = 28
figure = np.zeros((digit_size * n, digit_size * n))
# linearly spaced coordinates on the unit square were transformed through the inverse CDF (ppf) of the Gaussian
# to produce values of the latent variables z, since the prior of the latent space is Gaussian
grid_x = norm.ppf(np.linspace(0.05, 0.95, n))
grid_y = norm.ppf(np.linspace(0.05, 0.95, n))

for i, yi in enumerate(grid_x):
    for j, xi in enumerate(grid_y):
        z_sample = np.array([[xi, yi]])
        x_decoded = generator.predict(z_sample)
        digit = x_decoded[0].reshape(digit_size, digit_size)
        figure[i * digit_size: (i + 1) * digit_size,
               j * digit_size: (j + 1) * digit_size] = digit

fig, ax = plt.subplots()
fig.set_size_inches((7,7))
ax.imshow(figure, cmap='Greys_r')
display(fig)
```

``` md Note that it is blurry, and "manipulable" by moving through the latent space!

---
> It is *not* intuitively obvious where the calculation of the KL divergence comes from, and in general there is not a simple analytic way to derive KL divergence for arbitrary distributions. Because we have assumptions about Gaussians here, this is a special case -- the derivation is included in the Auto-Encoding Variational Bayes paper (2014; https://arxiv.org/pdf/1312.6114.pdf)

---
```

``` md ## Generative Adversarial Network (GAN)

The GAN, popularized recently by Ian Goodfellow's work, consists of __two networks__:

1. Generator network (that initially generates output from noise)
2. Discriminator network (trained with real data, to simply distinguish 2 class: real and fake)
    * The discriminator is also sometimes called the "A" or adversarial network
    
The basic procedure for building a GAN is to train both neworks in tandem according to the following simple procedure:

1. Generate bogus output from "G"
2. Train "D" with real and bogus data, labeled properly
3. Train "G" to target the "real/true/1" label by 
    * taking the "stacked" G + D model
    * feeding noise in at the start (G) end
    * and backpropagating from the real/true/1 distribution at the output (D) end
    
As always, there are lots of variants! But this is the core idea, as illustrated in the following code.

Zackory Erickson's example is so elegant and clear, I've used included it from https://github.com/Zackory/Keras-MNIST-GAN

Once again, we'll start it running first, since it takes a while to train.
```

``` python
import os
import numpy as np
import matplotlib.pyplot as plt

from keras.layers import Input
from keras.models import Model, Sequential
from keras.layers.core import Reshape, Dense, Dropout, Flatten
from keras.layers.advanced_activations import LeakyReLU
from keras.layers.convolutional import Convolution2D, UpSampling2D
from keras.layers.normalization import BatchNormalization
from keras.regularizers import l1, l1_l2
from keras.optimizers import Adam
from keras import backend as K
from keras import initializers
import sklearn.datasets

K.set_image_data_format('channels_last')

# Deterministic output.
# Tired of seeing the same results every time? Remove the line below.
np.random.seed(1000)

# The results are a little better when the dimensionality of the random vector is only 10.
# The dimensionality has been left at 100 for consistency with other GAN implementations.
randomDim = 100

train_libsvm = "/dbfs/databricks-datasets/mnist-digits/data-001/mnist-digits-train.txt"
test_libsvm = "/dbfs/databricks-datasets/mnist-digits/data-001/mnist-digits-test.txt"

X_train, y_train = sklearn.datasets.load_svmlight_file(train_libsvm, n_features=784)
X_train = X_train.toarray()
X_test, y_test = sklearn.datasets.load_svmlight_file(test_libsvm, n_features=784)
X_test = X_test.toarray()

X_train = (X_train.astype(np.float32) - 127.5)/127.5
X_train = X_train.reshape(60000, 784)

# Function for initializing network weights
def initNormal():
    return initializers.normal(stddev=0.02)

# Optimizer
adam = Adam(lr=0.0002, beta_1=0.5)

generator = Sequential()
generator.add(Dense(256, input_dim=randomDim, kernel_initializer=initializers.normal(stddev=0.02)))
generator.add(LeakyReLU(0.2))
generator.add(Dense(512))
generator.add(LeakyReLU(0.2))
generator.add(Dense(1024))
generator.add(LeakyReLU(0.2))
generator.add(Dense(784, activation='tanh'))
generator.compile(loss='binary_crossentropy', optimizer=adam)

discriminator = Sequential()
discriminator.add(Dense(1024, input_dim=784, kernel_initializer=initializers.normal(stddev=0.02)))
discriminator.add(LeakyReLU(0.2))
discriminator.add(Dropout(0.3))
discriminator.add(Dense(512))
discriminator.add(LeakyReLU(0.2))
discriminator.add(Dropout(0.3))
discriminator.add(Dense(256))
discriminator.add(LeakyReLU(0.2))
discriminator.add(Dropout(0.3))
discriminator.add(Dense(1, activation='sigmoid'))
discriminator.compile(loss='binary_crossentropy', optimizer=adam)

# Combined network
discriminator.trainable = False
ganInput = Input(shape=(randomDim,))
x = generator(ganInput)
ganOutput = discriminator(x)
gan = Model(inputs=ganInput, outputs=ganOutput)
gan.compile(loss='binary_crossentropy', optimizer=adam)

dLosses = []
gLosses = []

# Plot the loss from each batch
def plotLoss(epoch):
    plt.figure(figsize=(10, 8))
    plt.plot(dLosses, label='Discriminitive loss')
    plt.plot(gLosses, label='Generative loss')
    plt.xlabel('Epoch')
    plt.ylabel('Loss')
    plt.legend()
    plt.savefig('/dbfs/FileStore/gan_loss_epoch_%d.png' % epoch)

# Create a wall of generated MNIST images
def plotGeneratedImages(epoch, examples=100, dim=(10, 10), figsize=(10, 10)):
    noise = np.random.normal(0, 1, size=[examples, randomDim])
    generatedImages = generator.predict(noise)
    generatedImages = generatedImages.reshape(examples, 28, 28)

    plt.figure(figsize=figsize)
    for i in range(generatedImages.shape[0]):
        plt.subplot(dim[0], dim[1], i+1)
        plt.imshow(generatedImages[i], interpolation='nearest', cmap='gray_r')
        plt.axis('off')
    plt.tight_layout()
    plt.savefig('/dbfs/FileStore/gan_generated_image_epoch_%d.png' % epoch)

# Save the generator and discriminator networks (and weights) for later use
def saveModels(epoch):
    generator.save('/tmp/gan_generator_epoch_%d.h5' % epoch)
    discriminator.save('/tmp/gan_discriminator_epoch_%d.h5' % epoch)

def train(epochs=1, batchSize=128):
    batchCount = X_train.shape[0] // batchSize
    print('Epochs:', epochs)
    print('Batch size:', batchSize)
    print('Batches per epoch:', batchCount)

    for e in range(1, epochs+1):
        print('-'*15, 'Epoch %d' % e, '-'*15)
        for _ in range(batchCount):
            # Get a random set of input noise and images
            noise = np.random.normal(0, 1, size=[batchSize, randomDim])
            imageBatch = X_train[np.random.randint(0, X_train.shape[0], size=batchSize)]

            # Generate fake MNIST images
            generatedImages = generator.predict(noise)
            # print np.shape(imageBatch), np.shape(generatedImages)
            X = np.concatenate([imageBatch, generatedImages])

            # Labels for generated and real data
            yDis = np.zeros(2*batchSize)
            # One-sided label smoothing
            yDis[:batchSize] = 0.9

            # Train discriminator
            discriminator.trainable = True
            dloss = discriminator.train_on_batch(X, yDis)

            # Train generator
            noise = np.random.normal(0, 1, size=[batchSize, randomDim])
            yGen = np.ones(batchSize)
            discriminator.trainable = False
            gloss = gan.train_on_batch(noise, yGen)

        # Store loss of most recent batch from this epoch
        dLosses.append(dloss)
        gLosses.append(gloss)

        if e == 1 or e % 10 == 0:
            plotGeneratedImages(e)
            saveModels(e)

    # Plot losses from every epoch
    plotLoss(e)

train(10, 128)
```

>     ('Epochs:', 10)
>     ('Batch size:', 128)
>     ('Batches per epoch:', 468)
>     ('---------------', 'Epoch 1', '---------------')
>     ('---------------', 'Epoch 2', '---------------')
>     ('---------------', 'Epoch 3', '---------------')
>     ('---------------', 'Epoch 4', '---------------')
>     ('---------------', 'Epoch 5', '---------------')
>     ('---------------', 'Epoch 6', '---------------')
>     ('---------------', 'Epoch 7', '---------------')
>     ('---------------', 'Epoch 8', '---------------')
>     ('---------------', 'Epoch 9', '---------------')
>     ('---------------', 'Epoch 10', '---------------')

``` md ### Sample generated digits: epoch 1

<img src="/files/gan_generated_image_epoch_1.png" width=800>
```

### Sample generated digits: epoch 10

### Generator/Discriminator Loss

``` md ## Which Strategy to Use?

This is definitely an area of active research, so you'll want to experiment with both of these approaches.

GANs typically produce "sharper pictures" -- the adversarial loss is better than the combined MSE/XE + KL loss used in VAEs, but then again, that's partly by design.

VAEs are -- as seen above -- blurrier but more manipulable. One way of thinking about the multivariate Gaussian representation is that VAEs are trained to find some "meaning" in variation along each dimensin. And, in fact, with specific training it is possible to get them to associate specific meanings like color, translation, rotation, etc. to those dimensions.
```

[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

This is used in a non-profit educational setting with kind permission of [Adam Breindel](https://www.linkedin.com/in/adbreind). This is not licensed by Adam for use in a for-profit setting. Please contact Adam directly at `adbreind@gmail.com` to request or report such use cases or abuses. A few minor modifications and additional mathematical statistical pointers have been added by Raazesh Sainudiin when teaching PhD students in Uppsala University.