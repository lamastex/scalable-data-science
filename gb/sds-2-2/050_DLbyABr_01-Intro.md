[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

This is used in a non-profit educational setting with kind permission of [Adam Breindel](https://www.linkedin.com/in/adbreind).
This is not licensed by Adam for use in a for-profit setting. Please contact Adam directly at `adbreind@gmail.com` to request or report such use cases or abuses.
A few minor modifications and additional mathematical statistical pointers have been added by Raazesh Sainudiin when teaching PhD students in Uppsala University.

Archived YouTube video of this live unedited lab-lecture:

[![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/eJBR6sm4p2g/0.jpg)](https://www.youtube.com/embed/eJBR6sm4p2g?start=0&end=2654&autoplay=1) [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/-LLL3MUl9ps/0.jpg)](https://www.youtube.com/embed/-LLL3MUl9ps?start=0&end=2467&autoplay=1)

Introduction to Deep Learning
=============================

Theory and Practice with TensorFlow and Keras
---------------------------------------------

<img src="http://i.imgur.com/Gk8rv2Z.jpg" width=700>

https://arxiv.org/abs/1508.06576<br/>
*by the end of this course, this paper and project will be accessible to you!*

### Schedule

-   Intro
-   TensorFlow Basics
-   Artificial Neural Networks
-   Multilayer ("Deep") Feed-Forward Networks
-   Training Neural Nets
-   Convolutional Networks
-   Recurrent Nets, LSTM, GRU
-   Generative Networks / Patterns
-   Intro to Reinforcement Learning
-   Operations in the Real World

### Instructor: Adam Breindel

<img src="http://i.imgur.com/lpMDU9j.jpg" width=200 align=left style="margin-right:2em;margin-top:1em">

#### Contact: https://www.linkedin.com/in/adbreind - adbreind@gmail.com

-   Almost 20 years building systems for startups and large enterprises
-   10 years teaching front- and back-end technology

#### Interesting projects...

-   My first full-time job in tech involved streaming neural net fraud scoring (debit cards)
-   Realtime & offline analytics for banking
-   Music synchronization and licensing for networked jukeboxes

#### Industries

-   Finance / Insurance, Travel, Media / Entertainment, Government

**Note:**
Minor mathematical statistical details and other editing for LaTeX added by Dr. Raazesh Sainudiin (Raaz) for the PhD level inter-faculty course at Uppsala University, Spring Semester with Adam's explicit permission for use in a non-profit setting.\* All edits by Raaz are qualified by *(raaz)*. Please get Adam's permission before using this in a for-profit non-University setting.

### Class Goals

-   Understand deep learning!
    -   Acquire an intiution and feeling for how and why and when it works, so you can use it!
    -   No magic! (or at least very little magic)
-   We *don't* want to have a workshop where we install and demo some magical, fairly complicated thing, and we watch it do something awesome, and handwave, and go home
    -   That's great for generating excitement, but leaves
        -   Theoretical mysteries -- what's going on? do I need a Ph.D. in Math or Statistics to do this?
        -   Practical problems -- I have 10 lines of code but they never run because my tensor is the wrong shape!
-   We'll focus on TensorFlow and Keras
    -   But 95% should be knowledge you can use with frameworks too: Intel BigDL, Baidu PaddlePaddle, NVIDIA Digits, MXNet, etc.

Deep Learning is About Machines Finding Patterns and Solving Problems
---------------------------------------------------------------------

So let's start by diving right in and discussing an interesing problem:

MNIST Digits Dataset
--------------------

### Mixed National Institute of Standards and Technology

#### Called the "Drosophila" of Machine Learning

Likely the most common single dataset out there in deep learning, just complex enough to be interesting and useful for benchmarks.

"If your code works on MNIST, that doesn't mean it will work everywhere; but if it doesn't work on MNIST, it probably won't work anywhere" :)

<img src="http://i.imgur.com/uggRlE7.png" width=600>

### What is the goal?

Convert an image of a handwritten character into the correct classification (i.e., which character is it?)

This is nearly trivial for a human to do! Most toddlers can do this with near 100% accuracy, even though they may not be able to count beyond 10 or perform addition.

Traditionally this had been quite a hard task for a computer to do. 99% was not achieved until ~1998. Consistent, easy success at that level was not until 2003 or so.

#### Let's describe the specific problem in a little more detail

-   Each image is a 28x28 pixel image
    -   originally monochrome; smoothed to gray; typically inverted, so that "blank" pixels are black (zeros)
-   So the **predictors** are 784 (28 \* 28 = 784) values, ranging from 0 (black / no ink when inverted) to 255 (white / full ink when inverted)

-   The **response** -- what we're trying to predict -- is the number that the image represents
    -   the response is a value from 0 to 9
    -   since there are a small number of discrete catagories for the responses, this is a **classification** problem, not a **regression** problem
    -   there are 10 classes
-   We have, for each data record, predictors and a response to train against, so this is a **supervised** learning task

-   The dataset happens to come partitioned into a **training set** (60,000 records) and a **test set** (10,000 records)
    -   We'll "hold out" the test set and not train on it
-   Once our model is trained, we'll use it to **predict** (or perform **inference**) on the test records, and see how well our trained model performs on unseen test data
    -   We might want to further split the training set into a **validation set** or even several **K-fold partitions** to evaluate as we go
-   As humans, we'll probably measure our success by using **accuracy** as a metric: What fraction of the examples are correctly classified by the model?
    -   However, for training, it makes more sense to use **cross-entropy** to measure, correct, and improve the model. Cross-entropy has the advantage that instead of just counting "right or wrong" answers, it provides a continuous measure of "how wrong (or right)" an answer is. For example, if the correct answer is "1" then the answer "probably a 7, maybe a 1" is wrong, but *less wrong* than the answer "definitely a 7"
-   Do we need to pre-process the data? Depending on the model we use, we may want to ...
    -   **Scale** the values, so that they range from 0 to 1, or so that they measure in standard deviations
    -   **Center** the values so that 0 corresponds to the (raw central) value 127.5, or so that 0 corresponds to the mean

#### What might be characteristics of a good solution?

-   As always, we need to balance **variance** (malleability of the model in the face of variation in the sample training data) and **bias** (strength/inflexibility of assumptions built in to the modeling method)
-   We a model with a good amount of **capacity** to represent different patterns in the training data (e.g., different handwriting styles) while not **overfitting** and learning too much about specific training instances
-   We'd like a **probabalistic model** that tells us the most likely classes given the data and assumptions (for example, in the U.S., a one is often written with a vertical stroke, whereas in Germany it's usually written with 2 strokes, closer to a U.S. 7)

Going a little further,

-   an ideal modeling approach might perform **feature selection** on its own deciding which pixels and combinations of pixels are most informative
-   in order to be robust to varying data, a good model might learn **hierarchical or abstract features** like lines, angles, curves and loops that we as humans use to teach, learn, and distinguish Arabic numerals from each other
-   it would be nice to add some basic **domain knowledge** like these features aren't arbitrary slots in a vector, but are parts of a 2-dimensional image where the contents are roughly **axis-aligned** and **translation invariant** -- after all, a "7" is still a "7" even if we move it around a bit on the page

Lastly, it would be great to have a framework that is flexible enough to adapt to similar tasks -- say, Greek, Cyrillic, or Chinese handwritten characters, not just digits.

#### Let's compare some modeling techniques...

##### Decision Tree

👍 High capacity

👎 Can be hard to generalize; prone to overfit; fragile for this kind of task

👎 Dedicated training algorithm (traditional approach is not directly a gradient-descent optimization problem)

👍 Performs feature selection / PCA implicitly

##### (Multiclass) Logistic Regression

👎 Low capacity/variance -&gt; High bias

👍 Less overfitting

👎 Less fitting (accuracy)

<img src="http://i.imgur.com/1x80BDA.png" width=400>

##### Kernelized Support Vector Machine (e.g., RBF)

👍 Robust capacity, good bias-variance balance

👎 Expensive to scale in terms of features or instances

👍 Amenable to "online" learning (http://www.isn.ucsd.edu/papers/nips00\_inc.pdf)

👍 State of the art for MNIST prior to the widespread use of deep learning!

### Deep Learning

It turns out that a model called a convolutional neural network meets all of our goals and can be trained to human-level accuracy on this task in just a few minutes. We will solve MNIST with this sort of model today.

But we will build up to it starting with the simplest neural model.

**Mathematical statistical caveat**: Note that ML algorithmic performance measures such as 99% or 99.99% as well as their justification by comparisons to "typical" human performance measures from a randomised surveyable population actually often make significant mathematical assumptions that may be violated *under the carpet*. Some concrete examples include, the size and nature of the training data and their generalizability to live decision problems based on empirical risk minisation principles like cross-validation.
These assumpitons are usually harmless and can be time-saving for most problems like recommending songs in Spotify or shoes in Amazon. It is important to bear in mind that there are problems that should guarantee *worst case scenario avoidance*, like accidents with self-driving cars or global extinction event cause by mathematically ambiguous assumptions in the learning algorithms of say near-Earth-Asteroid mining artificially intelligent robots!

Installations
\* tensorflow==1.3.0
\* keras==2.0.8
\* dist-keras==0.2.0