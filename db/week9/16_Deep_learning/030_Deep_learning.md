// Databricks notebook source exported at Tue, 28 Jun 2016 09:28:40 UTC


# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)





The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/week9/16_Deep_learning/030_Deep_learning.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):

[![sds/uji/week9/16_Deep_learning/030_Deep_learning](http://img.youtube.com/vi/iDyeK3GvFpo/0.jpg)](https://www.youtube.com/v/iDyeK3GvFpo?rel=0&autoplay=1&modestbranding=1&end=3092)





# Deep learning: A Crash Introduction

This notebook provides an introduction to Deep Learning. It is meant to help you descend more fully into these learning resources and references:
* Udacity's course on Deep Learning [https://www.udacity.com/course/deep-learning--ud730](https://www.udacity.com/course/deep-learning--ud730) by Arpan Chakraborty and Vincent Vanhoucke
* Neural networks and deep learning [http://neuralnetworksanddeeplearning.com/](http://neuralnetworksanddeeplearning.com/) by Michael Nielsen 
* Deep learning book [http://www.deeplearningbook.org/](http://www.deeplearningbook.org/) by Ian Goodfellow, Yoshua Bengio and Aaron Courville





* Deep learning - buzzword for Artifical Neural Networks
* What is it?
  * Supervised learning model - Classifier
  * Unsupervised model - Anomaly detection
* Needs lots of data
* Online learning model - backpropogation
* Optimization - Stochastic gradient descent   
* Regularization - L1, L2, Dropout
***
***
* Supervised
  - Fully connected network
  - Convolutional neural network - Eg: For classifying images
  - Recurrent neural networks - Eg: For use on text, speech
* Unsupervised
  - Autoencoder





***
***

### A quick recap of logistic regression / linear models

**(watch now 46 seconds)**:

[![Udacity: Deep Learning by Vincent Vanhoucke - Training a logistic classifier](http://img.youtube.com/vi/G8eNWzxOgqE/0.jpg)](https://www.youtube.com/v/G8eNWzxOgqE?rel=0&autoplay=1&modestbranding=1&start=4&end=50)

***

-- Video Credit: Udacity's deep learning by Arpan Chakraborthy and Vincent Vanhoucke

***
#### Regression
![Regression](https://upload.wikimedia.org/wikipedia/commons/3/3a/Linear_regression.svg)  
y = mx + c





**Another way to look at a linear model**  

![Another way to look at a linear model](http://neuralnetworksanddeeplearning.com/images/tikz0.png)

-- Image Credit: Michael Nielsen  





***
***

### Recap - Gradient descent

**(1:54 seconds)**:

[![Udacity: Deep Learning by Vincent Vanhoucke - Gradient descent](http://img.youtube.com/vi/x449QQDhMDE/0.jpg)](https://www.youtube.com/v/x449QQDhMDE?rel=0&autoplay=1&modestbranding=1)

***
-- Video Credit: Udacity's deep learning by Arpan Chakraborthy and Vincent Vanhoucke





***
***

### Recap - Stochastic Gradient descent

**(2:25 seconds)**:

[![Udacity: Deep Learning by Vincent Vanhoucke - Stochastic Gradient descent](http://img.youtube.com/vi/hMLUgM6kTp8/0.jpg)](https://www.youtube.com/v/hMLUgM6kTp8?rel=0&autoplay=1&modestbranding=1)

***
-- Video Credit: Udacity's deep learning by Arpan Chakraborthy and Vincent Vanhoucke




 
HOGWILD! Parallel SGD without locks [http://i.stanford.edu/hazy/papers/hogwild-nips.pdf](http://i.stanford.edu/hazy/papers/hogwild-nips.pdf)





***
***

### Why deep learning? - Linear model

**(24 seconds)**:

[![Udacity: Deep Learning by Vincent Vanhoucke - Linear model](http://img.youtube.com/vi/PfNfY1xmkLs/0.jpg)](https://www.youtube.com/v/PfNfY1xmkLs?rel=0&autoplay=1&modestbranding=1&start=15&end=39)

***
-- Video Credit: Udacity's deep learning by Arpan Chakraborthy and Vincent Vanhoucke





**ReLU - Rectified linear unit or Rectifier** - max(0, x)

![ReLU](https://upload.wikimedia.org/wikipedia/en/6/6c/Rectifier_and_softplus_functions.svg)

-- Image Credit: Wikipedia





***
*** 

**Neural Network** 
 
Watch now (45 seconds) 
  
[![Udacity: Deep Learning by Vincent Vanhoucke - Neural network](http://img.youtube.com/vi/Opg63pan_YQ/0.jpg)](https://www.youtube.com/v/Opg63pan_YQ?rel=0&autoplay=1&modestbranding=1&start=0&end=45)
***
-- Video Credit: Udacity's deep learning by Arpan Chakraborthy and Vincent Vanhoucke





Is decision tree a linear model?  
[http://datascience.stackexchange.com/questions/6787/is-decision-tree-algorithm-a-linear-or-nonlinear-algorithm](http://datascience.stackexchange.com/questions/6787/is-decision-tree-algorithm-a-linear-or-nonlinear-algorithm)  





***
**Neural Network**
*** 
![Neural network](https://upload.wikimedia.org/wikipedia/commons/thumb/4/46/Colored_neural_network.svg/500px-Colored_neural_network.svg.png)
***
-- Image credit: Wikipedia





**Multiple hidden layers**
  
![Many hidden layers](http://neuralnetworksanddeeplearning.com/images/tikz36.png)
***
-- Image credit: Michael Nielsen





***
*** 

**What does it mean to go deep? What do each of the hidden layers learn?**
 
Watch now (1:13 seconds) 
  
[![Udacity: Deep Learning by Vincent Vanhoucke - Neural network](http://img.youtube.com/vi/_TcMRoWFppo/0.jpg)](https://www.youtube.com/v/_TcMRoWFppo?rel=0&autoplay=1&modestbranding=1&start=0)
***
-- Video Credit: Udacity's deep learning by Arpan Chakraborthy and Vincent Vanhoucke





### Chain rule
(f o g)' = (f' o g) . g'

***
*** 

**Chain rule in neural networks**
 
Watch later (55 seconds) 
  
[![Udacity: Deep Learning by Vincent Vanhoucke - Neural network](http://img.youtube.com/vi/fDeAJspBEnM/0.jpg)](https://www.youtube.com/v/fDeAJspBEnM?rel=0&autoplay=1&modestbranding=1&start=0)
***
-- Video Credit: Udacity's deep learning by Arpan Chakraborthy and Vincent Vanhoucke





### Backpropogation

***
 
Watch later (9:55 seconds) 
  
[![Backpropogation](http://img.youtube.com/vi/mgceQli6ZKQ/0.jpg)](https://www.youtube.com/v/mgceQli6ZKQ?rel=0&autoplay=1&modestbranding=1&start=0)
***
***
  
Watch now (1: 54 seconds)  
[![Backpropogation](http://img.youtube.com/vi/83bMCcPmFvE/0.jpg)](https://www.youtube.com/v/83bMCcPmFvE?rel=0&autoplay=1&modestbranding=1&start=0)
***





####How do you set the learning rate? - Step size in SGD?  
* [ADADELTA: Adaptive learning rate](http://arxiv.org/pdf/1212.5701v1.pdf)
* [ADAGRAD](http://www.jmlr.org/papers/volume12/duchi11a/duchi11a.pdf)





#### Convolutional Neural Networks
<img src="http://colah.github.io/posts/2014-07-Conv-Nets-Modular/img/Conv2-9x5-Conv2Conv2.png" width=800>  
***
Watch (3:55)  
[![Udacity: Deep Learning by Vincent Vanhoucke - Convolutional Neural network](http://img.youtube.com/vi/jajksuQW4mc/0.jpg)](https://www.youtube.com/v/jajksuQW4mc?rel=0&autoplay=1&modestbranding=1&start=0)  
***
* Alex Krizhevsky,  Ilya Sutskever, Geoffrey E. Hinton - [https://papers.nips.cc/paper/4824-imagenet-classification-with-deep-convolutional-neural-networks.pdf](https://papers.nips.cc/paper/4824-imagenet-classification-with-deep-convolutional-neural-networks.pdf)  
* Convolutional Neural networks blog - [http://colah.github.io/posts/2014-07-Conv-Nets-Modular/](http://colah.github.io/posts/2014-07-Conv-Nets-Modular/)





#### Recurrent neural network
![Recurrent neural network](http://colah.github.io/posts/2015-08-Understanding-LSTMs/img/RNN-unrolled.png)  
[http://colah.github.io/posts/2015-08-Understanding-LSTMs/](http://colah.github.io/posts/2015-08-Understanding-LSTMs/)  


[http://karpathy.github.io/2015/05/21/rnn-effectiveness/](http://karpathy.github.io/2015/05/21/rnn-effectiveness/)  
***
Watch (3:55)  
[![Udacity: Deep Learning by Vincent Vanhoucke - Recurrent Neural network](http://img.youtube.com/vi/H3ciJF2eCJI/0.jpg)](https://www.youtube.com/v/H3ciJF2eCJI?rel=0&autoplay=1&modestbranding=1&start=0)  
***
##### LSTM - Long short term memory
![LSTM](http://colah.github.io/posts/2015-08-Understanding-LSTMs/img/LSTM3-chain.png)

***
##### GRU - Gated recurrent unit
![Gated Recurrent unit](http://colah.github.io/posts/2015-08-Understanding-LSTMs/img/LSTM3-var-GRU.png)
[http://arxiv.org/pdf/1406.1078v3.pdf](http://arxiv.org/pdf/1406.1078v3.pdf)  





### Autoencoder
![Autoencoder](http://deeplearning4j.org/img/deep_autoencoder.png)
***
Watch (3:51)  
[![Autoencoder](http://img.youtube.com/vi/s96mYcicbpE/0.jpg)](https://www.youtube.com/v/s96mYcicbpE?rel=0&autoplay=1&modestbranding=1&start=0)  
***






# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)
