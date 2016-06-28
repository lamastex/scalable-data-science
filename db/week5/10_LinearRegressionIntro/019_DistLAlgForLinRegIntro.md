// Databricks notebook source exported at Sat, 18 Jun 2016 23:40:46 UTC


# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)





The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/week5/10_LinearRegressionIntro/019_DistLAlgForLinRegIntro.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):

[![sds/uji/week5/10_LinearRegressionIntro/019_DistLAlgForLinRegIntro](http://img.youtube.com/vi/y6F-e6m1m2s/0.jpg)](https://www.youtube.com/v/y6F-e6m1m2s?rel=0&autoplay=1&modestbranding=1&start=3919&end=4399)





**HOMEWORK:** 
* read: [http://arxiv.org/pdf/1509.02256.pdf](http://arxiv.org/pdf/1509.02256.pdf)  (also see References and Appendix A).
* and go through the notebooks here: [Data Types - MLlib Programming Guide](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/000_dataTypesProgGuide)





#### Distributed Machine Learning: Computation and Storage by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning
**(watch now/later? 8:48)**:

[![Distributed Machine Learning: Computation and Storageby Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/r2EWm82rneY/0.jpg)](https://www.youtube.com/v/r2EWm82rneY?rel=0&autoplay=1&modestbranding=1&start=1)





This is a transcript of Ameet's lecture in the video above:

**HOMEWORK:** Make sure you are understanding everything he is trying to convey!

**Recall** from week 1's lectures that we are weaving over Ameet's course here to get to advanced applications "in a hurry" and it is assumed you have already done the edX Big Data Series from 2015 or will be doing the 2016 version, ideally with certification :)

In this segment, we'll begin our discussion
of **distributed machine learning principles related
to computation and storage**.
We'll use linear regression as a running example
to illustrate these ideas.
As we previously discussed, the size of datasets
is rapidly growing.
And this has led to scalability issues
for many standard machine learning methods.
For instance, consider the least squares regression model.
The fact that a closed-form solution exists
is an appealing property of this method.
But what happens when we try to solve this closed-form solution
at scale?
In this segment, we'll focus on computational issues associated
with linear regression.
Though I should note that these same ideas apply
in the context of ridge regression,
as it has a very similar computational profile.
So let's figure out the time and space
complexity of solving for this closed-form solution.
We'll start with time complexity,
considering arithmetic operations
as the basic units of computation
when discussing big O complexity.
Looking at the expression for w, if we
perform each of the required operations separately,
we see that computing X transpose
X takes O of nd squared time, and inverting this resulting
matrix takes O of d cubed time.
Since matrix multiplication is an associative operation,
we can multiply X transpose and y in O of nd time
to get some intermediate d dimensional vector,
and then perform the multiplication
between the inverse matrix and this intermediate d dimensional
vector in O of d squared time.
Hence, in summary, the two computational bottlenecks
in this process involve computing X transpose X,
and subsequently computing its inverse.
There are other methods to solve this equation that
can be faster in practice, but they
are only faster in terms of constants,
and therefore they all have the same overall time complexity.
In summary, we say that computing
the closed-form solution for linear regression
takes O of nd squared plus d cubed time.
Now let's consider the space complexity.
And recall that our basic unit of storage
here is the storage required to store a single float, which
is typically 8 bytes.
In order to compute w, we must first
store the data matrix, which requires O of nd floats.
Additionally, we must compute X transpose X and its inverse.
In order to solve for w, each of these matrices
are d by d and thus require O of d squared floats.
These are the two bottlenecks storage-wise.
And thus our space complexity is O of nd plus d squared.
So now that we've considered the time and space
complexity required to solve for the closed-form solution, let's
consider what happens as our data grows large.
The first situation we'll consider
is one where n, or the number of observations,
is large, while d, or the number of features,
is relatively small.
Specifically, we'll assume that we're
in a setting where d is small enough such that O of d cubed
operate computation and O of d squared storage
is feasible on a single machine.
In this scenario, the terms in our big O complexity involving
n are the ones that dominate, and thus
storing X and computing X transpose X
are the bottlenecks.
It turns out that in this scenario,
we're well suited for a distributed computation.
First, we can store the data points or rows of X
across several machines, thus reducing the storage burden.
Second, we can compute X transpose X
in parallel across the machines by treating this multiplication
as a sum of outer products.
To understand this alternative interpretation
of matrix multiplication in terms of outer products,
let's first recall our typical definition
of matrix multiplication.
We usually think about each entry
of the output matrix being computed
via an inner product between rows and columns of the input
matrices.
So, for instance, in the example on the slide,
to compute the top left entry, we
compute the inner product between the first row
of the left input matrix and the first column of the right input
matrix.
Similarly, to compute the top right entry,
we compute the inner product between the first row
of the left input matrix and the second column
of the right input matrix.
We perform additional inner products
to compute the two remaining entries of the output matrix.
There is, however, an alternative interpretation
of matrix multiplication as the sum
of outer products between corresponding rows and columns
of the input matrices.
Let's look at the same example from the last slide
to get a better sense of what this means.
First consider the first column of the left input matrix
and the first row of the right input matrix.
We can compute their outer product
with the result being the 2 by 2 matrix
on the bottom of the slide.
Next, we can consider the second column of the left input matrix
and the second row of the right input matrix,
and again compute their outer product, resulting in another 2
by 2 matrix.
We can repeat this process a third time
to generate a third outer product or 2 by 2 matrix.
The sum of these outer products matches the result
we obtained in the previous slide using
the traditional definition of matrix multiplication.
And more generally, taking a sum of outer products
of corresponding rows and columns of the input matrices,
always returns the desired matrix multiplication result.
Now we can use this new interpretation
of matrix multiplication to our benefit
when distributing the computation of X transpose
X for linear regression.
Let's first represent X visually by its rows or data points.
Then we can express this matrix multiplication
as a sum of outer products where each outer product involves
only a single row of X or a single data point.
Let's see how we can use this insight
to effectively distribute our computation.
Consider a toy example where we have
a cluster of three workers and a data set with six data points.
We can distribute the storage of our six data points
across the three workers so that each worker
is storing two data points.
Now we can express matrix multiplication
as a simple MapReduce operation.
In the map step, we take each point
and compute its outer product with itself.
And in the subsequent reduce step,
we simply sum over all of these outer products.
We can then solve for the final linear regression model
locally, which includes computing the inverse
of this resulting matrix.
Now let's look at the storage and computation
involved at each step.
In the first step, we're not doing any computation,
but we need to store the input data, which
requires O of nd storage.
This is a bottleneck in our setting since n is large.
However, the storage can be distributed
over several machines.
Next, during the map step, we perform an outer product
for each data point.
Each outer product takes O of d squared time,
and we have to compute n of these outer products.
This is the computational bottleneck in our setting,
but again, it is distributed across multiple workers.
In terms of storage, we must store the outer products
computed on each machine.
Note that although we may be computing
several outer products per machine,
we can keep a running sum of these outer products,
so the local storage required for each machine
is O of d squared.
Finally, in the reduce step, we must
take the sum of these outer products,
though the computational bottleneck
is, in fact, inverting the resulting matrix, which
is cubic nd.
However, we're assuming that d is small enough
for this computation to be feasible on a single machine.
Similarly, the O of d squared storage required
to store X transpose X and its inverse
is also feasible on a single machine by assumption.
This entire process can be concisely
summarized via the following Spark code snippet.
In this code, train data is an RDD of rows of X.
In the map step, we compute an outer product for each row.
And in the reduce step, we sum these outer products
and invert the resulting matrix.
In the final reduce step, we can also
perform the remaining steps required to obtain
our final regression model.





#### Distributed Machine Learning: Computation and Storage (Part 2) by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning
**(watch now/later? 4:02)**:

[![Distributed Machine Learning: Computation and Storage part 2 by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/CYMZKbnDKsU/0.jpg)](https://www.youtube.com/v/CYMZKbnDKsU?rel=0&autoplay=1&modestbranding=1&start=1)





This is a transcript of Ameet's lecture in the video above:

**Homework:** Make sure you are understanding everything he is trying to convey!

In this segment, we'll continue our discussion
of distributed machine learning principles related
to computation and storage.
We'll focus on the problem when D, the number of features,
grows large.
In the previous segment, we discussed the big N small D
setting.
In this setting, we can naturally
use a distributed computing environment
to solve for the linear regression closed
form solution.
To do this, we store our data across multiple machines
and we compute X transpose X as a sum of outer products.
This strategy can be written as a simple MapReduce
operation, expressed very concisely in Spark.
Now, let's consider what happens when D grows large.
As before, storing X and computing X transpose X
are bottlenecks.
However, storing and operating on X transpose X
is now also a bottleneck.
And we can no longer use our previous strategy.
So let's see what goes wrong.
Here's what our strategy looks like in the small D
setting with data stored across workers,
outer products computed in the map step,
and sum of these outer products performed in the reduced step.
However, we can no longer perform D cubed operations
locally or store D squared floats locally
in our new setting.
This issue leads to a more general rule of thumb,
which is that when N and D are large,
we need the computation and storage complexity to be
at most linear in N and D.
So how do we devise methods that are linear in space and time
complexity?
One idea is to exploit sparsity.
Sparse data is quite prevalent in practice.
Some data is inherently sparse, such as rating information
and collaborative filtering problems or social networking
or other grafted.
Additionally, we often generate sparse features
during a process of feature extraction,
such as when we represent text documents
via a bag-of-words features or when
we convert categorical features into numerical representations.
Accounting for sparsity can lead to orders
of magnitudes of savings in terms
of storage and computation.
A second idea is to make a late and sparsity assumption,
whereby we make the assumption that our high dimensional data
can in fact be represented in a more succinct fashion,
either exactly or approximately.
For example, we can make a low rank modeling assumption
where we might assume that our data matrix can in fact be
represented by the product of two skinny matrices, where
the skinny dimension R is much smaller than either N or D.
Exploiting this assumption can also
yield significant computational and storage gigs.
A third option is to use different algorithms.
For instance, instead of learning a linear regression
model via the closed form solution,
we could alternatively use gradient descent.
Gradient descent is an iterative algorithm
that requires layer computation and storage at each iteration
thus making it attractive in the big N and big D setting.
So let's see how gradient descent stacks up
with a closed form solution in our toy example on a cluster
with three machines.
As before, we can store the data across the worker machines.
Now in the map step, we require O of ND computation,
and this computation is distributed across workers.
And we also require O of D storage locally.
In the reduced step, we require O of D local computation
as well as O of D local storage.
Moreover, unlike the closed form case,
we need to repeat this process several times
since gradient descent is an iterative algorithm.
At this point, I haven't really told you
how these question marks work.
And in the next segment, we'll talk
about what actually is going on with gradient
decent.





#### Communication Hierarchy by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning
**(watch later 2:32)**:

[![Communication Hierarchy by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/ABkUwJWn1d8/0.jpg)](https://www.youtube.com/v/ABkUwJWn1d8?rel=0&autoplay=1&modestbranding=1&start=1)





##### SUMMARY: Access rates fall sharply with distance.

* roughly 50 x gap between reading from memory and reading from either disk or the network.

We must take this communication hierarchy into consideration when developing parallel and distributed algorithms.





#### Distributed Machine Learning: Communication Principles by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning
**(watch later 11:28)**:

[![Distributed Machine Learning: Communication Principles by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/VT5F9tsV4hY/0.jpg)](https://www.youtube.com/v/VT5F9tsV4hY?rel=0&autoplay=1&modestbranding=1&start=1)





#### Focusing on strategies to reduce communication costs.

* access rates fall sharply with distance.
* so this communication hierarchy needs to be accounted for when developing parallel and distributed algorithms.

**Lessons:**
* parallelism makes our computation faster
* but network communication slows us down

* BINGO: perform parallel and in-memory computation.
* Persisting in memory is a particularly attractive option when working with iterative algorithms that
read the same data multiple times, as is the case in gradient descent.

* Several machine learning algorithms are iterative!

* Limits of multi-core scaling (powerful multicore machine with several CPUs,
and a huge amount of RAM).
  * advantageous: 
      * sidestep any network communication when working with a single multicore machine
      * can indeed handle fairly large data sets, and they're an attractive option in many settings.
  * disadvantages: 
      * can be quite expensive (due to specialized hardware),
      * not as widely accessible as commodity computing nodes.
      * this approach does have scalability limitations, as we'll eventually hit a wall when the data grows large enough! This is not the case for a distributed environment (like the AWS EC2 cloud under the hood here).



#### Simple strategies for algorithms in a distributed setting: to reduce network communication, simply keep large objects local
* In the big n, small d case for linear regression 
    * we can solve the problem via a closed form solution.
    * And this requires us to communicate $$O(d)^2$$ intermediate data.
    * the largest object in this example is our initial data, which we store in a distributed fashion and never communicate! This is a *data parallel setting*.
* In the big n, big d case:
  * for linear regression.
    * we use gradient descent to iteratively train our model and are again in a *data parallel setting*.
    * At each iteration we communicate the current parameter vector $$w_i$$ and the required $$O(d)$$ communication is feasible even for fairly large d.

* In the small n, small d case:
  * for ridge regression
    * we can communicate the small data to all of the workers.
    * this is an example of a *model parallel setting* where we can train the model for each hyper-parameter in parallel.

* Linear regression with big n and huge d is an example of both data and model parallelism.

**HOMEWORK:** Watch the video and find out why Linear regression with big n and huge d is an example of both data and model parallelism.

In this setting, since our data is large,
we must still store it across multiple machines.
We can still use gradient descent, or stochastic variants
of gradient descent to train our model,
but we may not want to communicate
the entire d dimensional parameter
vector at each iteration, when we have 10s,
or hundreds of millions of features.
In this setting we often rely on sparsity
to reduce the communication.
So far we discussed how we can reduce communication
by keeping large data local.





#### Simple strategies for algorithms in a distributed setting: compute more and communicate less per iteration
**HOMEWORK:** watch the video and understand why it is important at each iteration of an iterative algorithm
to compute more and communicate less.





**Recall** from week 1's lecture that the ideal mathematical preparation to fully digest this material requires a set of self-tutorials from Reza Zadeh's course in Distributed Algorithms and Optimization from Stanford:

* [http://stanford.edu/~rezab/dao/](http://stanford.edu/~rezab/dao/).

This is a minimal pre-requisite for designing new algorithms or improving exixting ones!!!






# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)
