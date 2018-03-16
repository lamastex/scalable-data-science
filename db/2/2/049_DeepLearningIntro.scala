// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/4mhl6fbQv-Y/0.jpg)](https://www.youtube.com/embed/4mhl6fbQv-Y?start=0&end=2754&autoplay=1)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC This is Raaz's update of Siva's whirl-wind compression of the free Google's DL course in Udacity [https://www.youtube.com/watch?v=iDyeK3GvFpo](https://www.youtube.com/watch?v=iDyeK3GvFpo) for Adam Briendel's DL modules that will follow.

// COMMAND ----------

// MAGIC %md
// MAGIC # Deep learning: A Crash Introduction
// MAGIC 
// MAGIC This notebook provides an introduction to Deep Learning. It is meant to help you descend more fully into these learning resources and references:
// MAGIC 
// MAGIC * Udacity's course on Deep Learning [https://www.udacity.com/course/deep-learning--ud730](https://www.udacity.com/course/deep-learning--ud730) by Google engineers: Arpan Chakraborty and Vincent Vanhoucke and their full video playlist:
// MAGIC     * [https://www.youtube.com/watch?v=X_B9NADf2wk&index=2&list=PLAwxTw4SYaPn_OWPFT9ulXLuQrImzHfOV](https://www.youtube.com/watch?v=X_B9NADf2wk&index=2&list=PLAwxTw4SYaPn_OWPFT9ulXLuQrImzHfOV)
// MAGIC * Neural networks and deep learning [http://neuralnetworksanddeeplearning.com/](http://neuralnetworksanddeeplearning.com/) by Michael Nielsen 
// MAGIC * Deep learning book [http://www.deeplearningbook.org/](http://www.deeplearningbook.org/) by Ian Goodfellow, Yoshua Bengio and Aaron Courville

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC * Deep learning - buzzword for Artifical Neural Networks
// MAGIC * What is it?
// MAGIC   * Supervised learning model - Classifier
// MAGIC   * Unsupervised model - Anomaly detection (say via auto-encoders)
// MAGIC * Needs lots of data
// MAGIC * Online learning model - backpropogation
// MAGIC * Optimization - Stochastic gradient descent   
// MAGIC * Regularization - L1, L2, Dropout
// MAGIC 
// MAGIC ***
// MAGIC ***
// MAGIC 
// MAGIC * Supervised
// MAGIC   - Fully connected network
// MAGIC   - Convolutional neural network - Eg: For classifying images
// MAGIC   - Recurrent neural networks - Eg: For use on text, speech
// MAGIC * Unsupervised
// MAGIC   - Autoencoder

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC ***
// MAGIC 
// MAGIC ### A quick recap of logistic regression / linear models
// MAGIC 
// MAGIC **(watch now 46 seconds from 4 to 50)**:
// MAGIC 
// MAGIC [![Udacity: Deep Learning by Vincent Vanhoucke - Training a logistic classifier](http://img.youtube.com/vi/G8eNWzxOgqE/0.jpg)](https://www.youtube.com/watch?v=G8eNWzxOgqE?rel=0&autoplay=1&modestbranding=1&start=4&end=50)
// MAGIC 
// MAGIC ***
// MAGIC 
// MAGIC -- Video Credit: Udacity's deep learning by Arpan Chakraborthy and Vincent Vanhoucke
// MAGIC 
// MAGIC ***
// MAGIC #### Regression
// MAGIC ![Regression](https://upload.wikimedia.org/wikipedia/commons/3/3a/Linear_regression.svg)  
// MAGIC y = mx + c

// COMMAND ----------

// MAGIC %md
// MAGIC **Another way to look at a linear model**  
// MAGIC 
// MAGIC ![Another way to look at a linear model](http://neuralnetworksanddeeplearning.com/images/tikz0.png)
// MAGIC 
// MAGIC -- Image Credit: Michael Nielsen  

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC ***
// MAGIC 
// MAGIC ### Recap - Gradient descent
// MAGIC 
// MAGIC **(1:54 seconds)**:
// MAGIC 
// MAGIC [![Udacity: Deep Learning by Vincent Vanhoucke - Gradient descent](http://img.youtube.com/vi/x449QQDhMDE/0.jpg)](https://www.youtube.com/watch?v=x449QQDhMDE)
// MAGIC 
// MAGIC ***
// MAGIC -- Video Credit: Udacity's deep learning by Arpan Chakraborthy and Vincent Vanhoucke

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC ***
// MAGIC 
// MAGIC ### Recap - Stochastic Gradient descent
// MAGIC 
// MAGIC **(2:25 seconds)**:
// MAGIC 
// MAGIC [![Udacity: Deep Learning by Vincent Vanhoucke - Stochastic Gradient descent (SGD)](http://img.youtube.com/vi/hMLUgM6kTp8/0.jpg)](https://www.youtube.com/watch?v=hMLUgM6kTp8)
// MAGIC 
// MAGIC **(1:28 seconds)**:
// MAGIC 
// MAGIC [![Udacity: Deep Learning by Vincent Vanhoucke - Momentum and learning rate decay in SGD](http://img.youtube.com/vi/s6jC7Wc9iMI/0.jpg)](https://www.youtube.com/watch?v=s6jC7Wc9iMI)
// MAGIC 
// MAGIC ***
// MAGIC -- Video Credit: Udacity's deep learning by Arpan Chakraborthy and Vincent Vanhoucke

// COMMAND ----------

// MAGIC %md 
// MAGIC HOGWILD! Parallel SGD without locks [http://i.stanford.edu/hazy/papers/hogwild-nips.pdf](http://i.stanford.edu/hazy/papers/hogwild-nips.pdf)

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC ***
// MAGIC 
// MAGIC ### Why deep learning? - Linear model
// MAGIC 
// MAGIC **(24 seconds - 15 to 39)**:
// MAGIC 
// MAGIC [![Udacity: Deep Learning by Vincent Vanhoucke - Linear model](http://img.youtube.com/vi/PfNfY1xmkLs/0.jpg)](https://www.youtube.com/watch?v=PfNfY1xmkLs)
// MAGIC 
// MAGIC ***
// MAGIC -- Video Credit: Udacity's deep learning by Arpan Chakraborthy and Vincent Vanhoucke

// COMMAND ----------

// MAGIC %md
// MAGIC **ReLU - Rectified linear unit or Rectifier** - max(0, x)
// MAGIC 
// MAGIC ![ReLU](https://upload.wikimedia.org/wikipedia/en/6/6c/Rectifier_and_softplus_functions.svg)
// MAGIC 
// MAGIC -- Image Credit: Wikipedia

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC *** 
// MAGIC 
// MAGIC **Neural Network** 
// MAGIC  
// MAGIC Watch now (45 seconds, 0-45) 
// MAGIC   
// MAGIC [![Udacity: Deep Learning by Vincent Vanhoucke - Neural network](http://img.youtube.com/vi/Opg63pan_YQ/0.jpg)](https://www.youtube.com/watch?v=Opg63pan_YQ)
// MAGIC ***
// MAGIC -- Video Credit: Udacity's deep learning by Arpan Chakraborthy and Vincent Vanhoucke

// COMMAND ----------

// MAGIC %md
// MAGIC Is decision tree a linear model?  
// MAGIC [http://datascience.stackexchange.com/questions/6787/is-decision-tree-algorithm-a-linear-or-nonlinear-algorithm](http://datascience.stackexchange.com/questions/6787/is-decision-tree-algorithm-a-linear-or-nonlinear-algorithm)  

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC **Neural Network**
// MAGIC *** 
// MAGIC ![Neural network](https://upload.wikimedia.org/wikipedia/commons/thumb/4/46/Colored_neural_network.svg/500px-Colored_neural_network.svg.png)
// MAGIC ***
// MAGIC -- Image credit: Wikipedia

// COMMAND ----------

// MAGIC %md
// MAGIC **Multiple hidden layers**
// MAGIC   
// MAGIC ![Many hidden layers](http://neuralnetworksanddeeplearning.com/images/tikz36.png)
// MAGIC ***
// MAGIC -- Image credit: Michael Nielsen

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC *** 
// MAGIC 
// MAGIC **What does it mean to go deep? What do each of the hidden layers learn?**
// MAGIC  
// MAGIC Watch now (1:13 seconds) 
// MAGIC   
// MAGIC [![Udacity: Deep Learning by Vincent Vanhoucke - Neural network](http://img.youtube.com/vi/_TcMRoWFppo/0.jpg)](https://www.youtube.com/watch?v=_TcMRoWFppo)
// MAGIC ***
// MAGIC -- Video Credit: Udacity's deep learning by Arpan Chakraborthy and Vincent Vanhoucke

// COMMAND ----------

// MAGIC %md
// MAGIC ### Chain rule
// MAGIC $$
// MAGIC (f \circ g)\prime = (f\prime \circ g) \cdot g\prime
// MAGIC $$
// MAGIC ***
// MAGIC *** 
// MAGIC 
// MAGIC **Chain rule in neural networks**
// MAGIC  
// MAGIC Watch later (55 seconds) 
// MAGIC   
// MAGIC [![Udacity: Deep Learning by Vincent Vanhoucke - Neural network](http://img.youtube.com/vi/fDeAJspBEnM/0.jpg)](https://www.youtube.com/watch?v=fDeAJspBEnM)
// MAGIC ***
// MAGIC -- Video Credit: Udacity's deep learning by Arpan Chakraborthy and Vincent Vanhoucke

// COMMAND ----------

// MAGIC %md
// MAGIC ### Backpropogation
// MAGIC 
// MAGIC ***
// MAGIC 
// MAGIC To properly understand this you are going to minimally need 20 minutes or so, depending on how rusty your maths is now.
// MAGIC 
// MAGIC First go through this carefully:
// MAGIC * [https://stats.stackexchange.com/questions/224140/step-by-step-example-of-reverse-mode-automatic-differentiation](https://stats.stackexchange.com/questions/224140/step-by-step-example-of-reverse-mode-automatic-differentiation)
// MAGIC 
// MAGIC Watch **later** (9:55 seconds) 
// MAGIC   
// MAGIC [![Backpropogation](http://img.youtube.com/vi/mgceQli6ZKQ/0.jpg)](https://www.youtube.com/watch?v=mgceQli6ZKQ)
// MAGIC ***
// MAGIC ***
// MAGIC   
// MAGIC Watch now (1: 54 seconds)  
// MAGIC [![Backpropogation](http://img.youtube.com/vi/83bMCcPmFvE/0.jpg)](https://www.youtube.com/watch?v=83bMCcPmFvE)
// MAGIC ***

// COMMAND ----------

// MAGIC %md
// MAGIC ####How do you set the learning rate? - Step size in SGD?  
// MAGIC * [ADADELTA: Adaptive learning rate](http://arxiv.org/pdf/1212.5701v1.pdf)
// MAGIC * [ADAGRAD](http://www.jmlr.org/papers/volume12/duchi11a/duchi11a.pdf)
// MAGIC 
// MAGIC there is a lot more... including newer frameworks for automating these knows using probabilistic programs (but in non-distributed settings as of Dec 2017).

// COMMAND ----------

// MAGIC %md
// MAGIC So far we have only seen fully connected neural networks, now let's move into more interesting ones that exploit spatial locality and nearness patterns inherent in certain classes of data, such as image data.

// COMMAND ----------

// MAGIC %md
// MAGIC #### Convolutional Neural Networks
// MAGIC <img src="http://colah.github.io/posts/2014-07-Conv-Nets-Modular/img/Conv2-9x5-Conv2Conv2.png" width=800>  
// MAGIC ***
// MAGIC Watch now (3:55)  
// MAGIC [![Udacity: Deep Learning by Vincent Vanhoucke - Convolutional Neural network](http://img.youtube.com/vi/jajksuQW4mc/0.jpg)](https://www.youtube.com/watch?v=jajksuQW4mc)  
// MAGIC ***
// MAGIC 
// MAGIC * Alex Krizhevsky,  Ilya Sutskever, Geoffrey E. Hinton - [https://papers.nips.cc/paper/4824-imagenet-classification-with-deep-convolutional-neural-networks.pdf](https://papers.nips.cc/paper/4824-imagenet-classification-with-deep-convolutional-neural-networks.pdf)  
// MAGIC * Convolutional Neural networks blog - [http://colah.github.io/posts/2014-07-Conv-Nets-Modular/](http://colah.github.io/posts/2014-07-Conv-Nets-Modular/)

// COMMAND ----------

// MAGIC %md
// MAGIC #### Recurrent neural network
// MAGIC ![Recurrent neural network](http://colah.github.io/posts/2015-08-Understanding-LSTMs/img/RNN-unrolled.png)  
// MAGIC [http://colah.github.io/posts/2015-08-Understanding-LSTMs/](http://colah.github.io/posts/2015-08-Understanding-LSTMs/)  
// MAGIC 
// MAGIC 
// MAGIC [http://karpathy.github.io/2015/05/21/rnn-effectiveness/](http://karpathy.github.io/2015/05/21/rnn-effectiveness/)  
// MAGIC ***
// MAGIC Watch (3:55)  
// MAGIC [![Udacity: Deep Learning by Vincent Vanhoucke - Recurrent Neural network](http://img.youtube.com/vi/H3ciJF2eCJI/0.jpg)](https://www.youtube.com/watch?v=H3ciJF2eCJI?rel=0&autoplay=1&modestbranding=1&start=0)  
// MAGIC ***
// MAGIC ##### LSTM - Long short term memory
// MAGIC ![LSTM](http://colah.github.io/posts/2015-08-Understanding-LSTMs/img/LSTM3-chain.png)
// MAGIC 
// MAGIC ***
// MAGIC ##### GRU - Gated recurrent unit
// MAGIC ![Gated Recurrent unit](http://colah.github.io/posts/2015-08-Understanding-LSTMs/img/LSTM3-var-GRU.png)
// MAGIC [http://arxiv.org/pdf/1406.1078v3.pdf](http://arxiv.org/pdf/1406.1078v3.pdf)  

// COMMAND ----------

// MAGIC %md
// MAGIC ### Autoencoder
// MAGIC ![Autoencoder](http://deeplearning4j.org/img/deep_autoencoder.png)
// MAGIC ***
// MAGIC Watch now (3:51)  
// MAGIC [![Autoencoder](http://img.youtube.com/vi/s96mYcicbpE/0.jpg)](https://www.youtube.com/watch?v=s96mYcicbpE)  
// MAGIC ***

// COMMAND ----------

// MAGIC %md
// MAGIC The more recent improvement over CNNs are called capsule networks by Hinton.
// MAGIC Check them out here if you want to prepare for your future interview question in 2017/2018 or so...:
// MAGIC 
// MAGIC * [https://medium.com/ai%C2%B3-theory-practice-business/understanding-hintons-capsule-networks-part-i-intuition-b4b559d1159b](https://medium.com/ai%C2%B3-theory-practice-business/understanding-hintons-capsule-networks-part-i-intuition-b4b559d1159b)