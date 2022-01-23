<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

MixUp and Generalization
========================

Group Project Authors:

-   Olof Zetterqvist

-   Jimmy Aronsson

-   Fredrik Hellström

Video: https://chalmersuniversity.box.com/s/ubij9bjekg6lcov13kw16kjhk01uzsmy

</div>

<div class="cell markdown">

Introduction
------------

The goal of supervised machine learning is to predict labels given examples. Specifically, we want to choose some mapping *f*, referred to as a hypothesis, from a space of examples *X* to a space of labels *Y*. As a concrete example, *X* can be the set of pictures of cats and dogs of a given size, *Y* can be the set *{cat, dog}*, and *f* can be a neural network. To choose *f*, we rely on a set of labelled data. However, our true goal is to perform well on unseen data, i.e., test data. If an algorithm performs similarly well on unseen data as on the training data we used, we say that it *generalizes*.

A pertinent question, then, is to explain why a model generalizes and using the answer to improve learning algorithms. For overparameterized deep learning methods, this question has yet to be answered conclusively. Recently, a training procedure called MixUp was proposed to improve the generalization capabilities of neural networks \[\[1\]\]. The basic idea is that instead of feeding the raw training data to our supervised learning algorithm, we instead use convex combinations of two randomly selected data points. The benefit of this is two-fold. First, it plays the role of data augmentation: the network will never see two completely identical training samples, since we constantly produce new random combinations. Second, the network is encouraged to behave nicely in-between training samples, which has the potential to reduce overfitting. A connection between performance on MixUp data and generalization abilities of networks trained without the MixUp procedure was also studied in \[\[2\]\].

</div>

<div class="cell markdown">

\*\* Project description \*\*

In this project, we will investigate the connection between MixUp and generalization at a large scale by performing a distributed hyperparameter search. We will look at both Random Forests and convolutional neural networks. First, we will the algorithms without MixUp, and study the connection between MixUp performance and test error. Then, we will train the networks on MixUp data, and see whether directly optimizing MixUp performance will yield more beneficial test errors.

To make the hyperparameter search distributed and scalable, we will use the Ray Tune package \[\[3\]\]. We also planned to use Horovod to enable the individual networks to handle data in a distributed fashion \[\[4\]\]. Scalability would then have entered our project in both the scope of the hyperparameter search and the size of the data set. However, we had unexpected GPU problems and were ultimately forced to skip Horovod due to lack of time.

</div>

<div class="cell markdown">

**Summary of findings**

Our findings were as follows. For Random Forests, we did not find any significant improvement when using MixUp. This may be due to the fact that Random Forests, since they are not trained iteratively, cannot efficiently utilize MixUp. Furthermore, since Decision Trees are piecewise constant, it is unclear what it would mean to force them to behave nicely in-between training samples. When training a CNN to classify MNIST images, we found practically no difference between training on MixUp data and normal, untouched data. This may be due to MNIST being "too easy". However, for a CNN trained on CIFAR-10, the benefits of MixUp became noticable. First of all, training the same number of epochs on MixUp data as the normal training data gave a higher accuracy on the validation set. Secondly, while the network started to overfit on normal data, this did not occur to a significant degree when using MixUp data. This indicates that MixUp can be beneficial when the algorithm and data are sufficiently complex.

</div>
