[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

This is Raaz's update of Siva's whirl-wind compression of the free Google's DL course in Udacity <https://www.youtube.com/watch?v=iDyeK3GvFpo> for Adam Briendel's DL modules that will follow.

Deep learning: A Crash Introduction
===================================

This notebook provides an introduction to Deep Learning. It is meant to help you descend more fully into these learning resources and references:
\* Udacity's course on Deep Learning <https://www.udacity.com/course/deep-learning--ud730> by Google engineers: Arpan Chakraborty and Vincent Vanhoucke and their full video playlist:
\* [https://www.youtube.com/watch?v=X*B9NADf2wk&index=2&list=PLAwxTw4SYaPn*OWPFT9ulXLuQrImzHfOV](https://www.youtube.com/watch?v=X_B9NADf2wk&index=2&list=PLAwxTw4SYaPn_OWPFT9ulXLuQrImzHfOV)
\* Neural networks and deep learning <http://neuralnetworksanddeeplearning.com/> by Michael Nielsen
\* Deep learning book <http://www.deeplearningbook.org/> by Ian Goodfellow, Yoshua Bengio and Aaron Courville

-   Deep learning - buzzword for Artifical Neural Networks
-   What is it?
    -   Supervised learning model - Classifier
    -   Unsupervised model - Anomaly detection (say via auto-encoders)
-   Needs lots of data
-   Online learning model - backpropogation
-   Optimization - Stochastic gradient descent
-   Regularization - L1, L2, Dropout
    ***
    ***
-   Supervised
    -   Fully connected network
    -   Convolutional neural network - Eg: For classifying images
    -   Recurrent neural networks - Eg: For use on text, speech
-   Unsupervised
    -   Autoencoder

------------------------------------------------------------------------

------------------------------------------------------------------------

### A quick recap of logistic regression / linear models

**(watch now 46 seconds from 4 to 50)**:

[![Udacity: Deep Learning by Vincent Vanhoucke - Training a logistic classifier](http://img.youtube.com/vi/G8eNWzxOgqE/0.jpg)](https://www.youtube.com/watch?v=G8eNWzxOgqE?rel=0&autoplay=1&modestbranding=1&start=4&end=50)

------------------------------------------------------------------------

-- Video Credit: Udacity's deep learning by Arpan Chakraborthy and Vincent Vanhoucke

------------------------------------------------------------------------

#### Regression

![Regression](https://upload.wikimedia.org/wikipedia/commons/3/3a/Linear_regression.svg)
y = mx + c

**Another way to look at a linear model**

![Another way to look at a linear model](http://neuralnetworksanddeeplearning.com/images/tikz0.png)

-- Image Credit: Michael Nielsen

------------------------------------------------------------------------

------------------------------------------------------------------------

### Recap - Gradient descent

**(1:54 seconds)**:

[![Udacity: Deep Learning by Vincent Vanhoucke - Gradient descent](http://img.youtube.com/vi/x449QQDhMDE/0.jpg)](https://www.youtube.com/watch?v=x449QQDhMDE)

------------------------------------------------------------------------

-- Video Credit: Udacity's deep learning by Arpan Chakraborthy and Vincent Vanhoucke

------------------------------------------------------------------------

------------------------------------------------------------------------

### Recap - Stochastic Gradient descent

**(2:25 seconds)**:

[![Udacity: Deep Learning by Vincent Vanhoucke - Stochastic Gradient descent (SGD)](http://img.youtube.com/vi/hMLUgM6kTp8/0.jpg)](https://www.youtube.com/watch?v=hMLUgM6kTp8)

**(1:28 seconds)**:

[![Udacity: Deep Learning by Vincent Vanhoucke - Momentum and learning rate decay in SGD](http://img.youtube.com/vi/s6jC7Wc9iMI/0.jpg)](https://www.youtube.com/watch?v=s6jC7Wc9iMI)

------------------------------------------------------------------------

-- Video Credit: Udacity's deep learning by Arpan Chakraborthy and Vincent Vanhoucke

HOGWILD! Parallel SGD without locks <http://i.stanford.edu/hazy/papers/hogwild-nips.pdf>

------------------------------------------------------------------------

------------------------------------------------------------------------

### Why deep learning? - Linear model

**(24 seconds - 15 to 39)**:

[![Udacity: Deep Learning by Vincent Vanhoucke - Linear model](http://img.youtube.com/vi/PfNfY1xmkLs/0.jpg)](https://www.youtube.com/watch?v=PfNfY1xmkLs)

------------------------------------------------------------------------

-- Video Credit: Udacity's deep learning by Arpan Chakraborthy and Vincent Vanhoucke

**ReLU - Rectified linear unit or Rectifier** - max(0, x)

![ReLU](https://upload.wikimedia.org/wikipedia/en/6/6c/Rectifier_and_softplus_functions.svg)

-- Image Credit: Wikipedia

------------------------------------------------------------------------

------------------------------------------------------------------------

**Neural Network**

Watch now (45 seconds, 0-45)

[![Udacity: Deep Learning by Vincent Vanhoucke - Neural network](http://img.youtube.com/vi/Opg63pan_YQ/0.jpg)](https://www.youtube.com/watch?v=Opg63pan_YQ)
\*\*\*
-- Video Credit: Udacity's deep learning by Arpan Chakraborthy and Vincent Vanhoucke

Is decision tree a linear model?
<http://datascience.stackexchange.com/questions/6787/is-decision-tree-algorithm-a-linear-or-nonlinear-algorithm>

------------------------------------------------------------------------

**Neural Network**
\*\*\*
![Neural network](https://upload.wikimedia.org/wikipedia/commons/thumb/4/46/Colored_neural_network.svg/500px-Colored_neural_network.svg.png)
\*\*\*
-- Image credit: Wikipedia

**Multiple hidden layers**

![Many hidden layers](http://neuralnetworksanddeeplearning.com/images/tikz36.png)
\*\*\*
-- Image credit: Michael Nielsen

------------------------------------------------------------------------

------------------------------------------------------------------------

**What does it mean to go deep? What do each of the hidden layers learn?**

Watch now (1:13 seconds)

[![Udacity: Deep Learning by Vincent Vanhoucke - Neural network](http://img.youtube.com/vi/_TcMRoWFppo/0.jpg)](https://www.youtube.com/watch?v=_TcMRoWFppo)
\*\*\*
-- Video Credit: Udacity's deep learning by Arpan Chakraborthy and Vincent Vanhoucke

### Chain rule

$$
(f \circ g)\prime = (f\prime \circ g) \cdot g\prime
$$
***
***

**Chain rule in neural networks**

Watch later (55 seconds)

[![Udacity: Deep Learning by Vincent Vanhoucke - Neural network](http://img.youtube.com/vi/fDeAJspBEnM/0.jpg)](https://www.youtube.com/watch?v=fDeAJspBEnM)
\*\*\*
-- Video Credit: Udacity's deep learning by Arpan Chakraborthy and Vincent Vanhoucke

### Backpropogation

------------------------------------------------------------------------

To properly understand this you are going to minimally need 20 minutes or so, depending on how rusty your maths is now.

First go through this carefully:
\* <https://stats.stackexchange.com/questions/224140/step-by-step-example-of-reverse-mode-automatic-differentiation>

Watch **later** (9:55 seconds)

[![Backpropogation](http://img.youtube.com/vi/mgceQli6ZKQ/0.jpg)](https://www.youtube.com/watch?v=mgceQli6ZKQ)
***
***

Watch now (1: 54 seconds)
[![Backpropogation](http://img.youtube.com/vi/83bMCcPmFvE/0.jpg)](https://www.youtube.com/watch?v=83bMCcPmFvE)
\*\*\*

#### How do you set the learning rate? - Step size in SGD?

-   [ADADELTA: Adaptive learning rate](http://arxiv.org/pdf/1212.5701v1.pdf)
-   [ADAGRAD](http://www.jmlr.org/papers/volume12/duchi11a/duchi11a.pdf)

there is a lot more... including newer frameworks for automating these knows using probabilistic programs (but in non-distributed settings as of Dec 2017).

So far we have only seen fully connected neural networks, now let's move into more interesting ones that exploit spatial locality and nearness patterns inherent in certain classes of data, such as image data.

#### Convolutional Neural Networks

<img src="http://colah.github.io/posts/2014-07-Conv-Nets-Modular/img/Conv2-9x5-Conv2Conv2.png" width=800>
***
Watch now (3:55)
[![Udacity: Deep Learning by Vincent Vanhoucke - Convolutional Neural network](http://img.youtube.com/vi/jajksuQW4mc/0.jpg)](https://www.youtube.com/watch?v=jajksuQW4mc)
***
\* Alex Krizhevsky, Ilya Sutskever, Geoffrey E. Hinton - <https://papers.nips.cc/paper/4824-imagenet-classification-with-deep-convolutional-neural-networks.pdf>
\* Convolutional Neural networks blog - <http://colah.github.io/posts/2014-07-Conv-Nets-Modular/>

#### Recurrent neural network

![Recurrent neural network](http://colah.github.io/posts/2015-08-Understanding-LSTMs/img/RNN-unrolled.png)
<http://colah.github.io/posts/2015-08-Understanding-LSTMs/>

<http://karpathy.github.io/2015/05/21/rnn-effectiveness/>
***
Watch (3:55)
[![Udacity: Deep Learning by Vincent Vanhoucke - Recurrent Neural network](http://img.youtube.com/vi/H3ciJF2eCJI/0.jpg)](https://www.youtube.com/watch?v=H3ciJF2eCJI?rel=0&autoplay=1&modestbranding=1&start=0)
***

##### LSTM - Long short term memory

![LSTM](http://colah.github.io/posts/2015-08-Understanding-LSTMs/img/LSTM3-chain.png)

------------------------------------------------------------------------

##### GRU - Gated recurrent unit

![Gated Recurrent unit](http://colah.github.io/posts/2015-08-Understanding-LSTMs/img/LSTM3-var-GRU.png)
<http://arxiv.org/pdf/1406.1078v3.pdf>

### Autoencoder

![Autoencoder](http://deeplearning4j.org/img/deep_autoencoder.png)
***
Watch now (3:51)
[![Autoencoder](http://img.youtube.com/vi/s96mYcicbpE/0.jpg)](https://www.youtube.com/watch?v=s96mYcicbpE)
***

The more recent improvement over CNNs are called capsule networks by Hinton.
Check them out here if you want to prepare for your future interview question in 2017/2018 or so...:
\* <https://medium.com/ai%C2%B3-theory-practice-business/understanding-hintons-capsule-networks-part-i-intuition-b4b559d1159b>