// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/1NICbbECaC0/0.jpg)](https://www.youtube.com/embed/1NICbbECaC0?start=2285&end=2880&autoplay=1)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ## We MAY come back to this more detailed deep dive
// MAGIC 
// MAGIC **HOMEWORK:** 
// MAGIC 
// MAGIC * read: [http://arxiv.org/pdf/1509.02256.pdf](http://arxiv.org/pdf/1509.02256.pdf)  (also see References and Appendix A).
// MAGIC * and go through the notebooks here: [Data Types - MLlib Programming Guide](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/MLlibProgrammingGuide/dataTypes/000_dataTypesProgGuide)

// COMMAND ----------

// MAGIC %md
// MAGIC #### Distributed Machine Learning: Computation and Storage by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning
// MAGIC **(watch now/later? 8:48)**:
// MAGIC 
// MAGIC [![Distributed Machine Learning: Computation and Storageby Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/r2EWm82rneY/0.jpg)](https://www.youtube.com/watch?v=r2EWm82rneY)

// COMMAND ----------

// MAGIC %md
// MAGIC This is a transcript of Ameet's lecture in the video above:
// MAGIC 
// MAGIC **HOMEWORK:** Make sure you are understanding everything he is trying to convey!
// MAGIC 
// MAGIC **Recall** from week 1's lectures that we are weaving over Ameet's course here to get to advanced applications "in a hurry" and it is assumed you have already done the edX Big Data Series from 2015 or will be doing the 2016 version, ideally with certification :)
// MAGIC 
// MAGIC In this segment, we'll begin our discussion
// MAGIC of **distributed machine learning principles related
// MAGIC to computation and storage**.
// MAGIC We'll use linear regression as a running example
// MAGIC to illustrate these ideas.
// MAGIC As we previously discussed, the size of datasets
// MAGIC is rapidly growing.
// MAGIC And this has led to scalability issues
// MAGIC for many standard machine learning methods.
// MAGIC For instance, consider the least squares regression model.
// MAGIC The fact that a closed-form solution exists
// MAGIC is an appealing property of this method.
// MAGIC But what happens when we try to solve this closed-form solution
// MAGIC at scale?
// MAGIC In this segment, we'll focus on computational issues associated
// MAGIC with linear regression.
// MAGIC Though I should note that these same ideas apply
// MAGIC in the context of ridge regression,
// MAGIC as it has a very similar computational profile.
// MAGIC So let's figure out the time and space
// MAGIC complexity of solving for this closed-form solution.
// MAGIC We'll start with time complexity,
// MAGIC considering arithmetic operations
// MAGIC as the basic units of computation
// MAGIC when discussing big O complexity.
// MAGIC Looking at the expression for w, if we
// MAGIC perform each of the required operations separately,
// MAGIC we see that computing X transpose
// MAGIC X takes O of nd squared time, and inverting this resulting
// MAGIC matrix takes O of d cubed time.
// MAGIC Since matrix multiplication is an associative operation,
// MAGIC we can multiply X transpose and y in O of nd time
// MAGIC to get some intermediate d dimensional vector,
// MAGIC and then perform the multiplication
// MAGIC between the inverse matrix and this intermediate d dimensional
// MAGIC vector in O of d squared time.
// MAGIC Hence, in summary, the two computational bottlenecks
// MAGIC in this process involve computing X transpose X,
// MAGIC and subsequently computing its inverse.
// MAGIC There are other methods to solve this equation that
// MAGIC can be faster in practice, but they
// MAGIC are only faster in terms of constants,
// MAGIC and therefore they all have the same overall time complexity.
// MAGIC In summary, we say that computing
// MAGIC the closed-form solution for linear regression
// MAGIC takes O of nd squared plus d cubed time.
// MAGIC Now let's consider the space complexity.
// MAGIC And recall that our basic unit of storage
// MAGIC here is the storage required to store a single float, which
// MAGIC is typically 8 bytes.
// MAGIC In order to compute w, we must first
// MAGIC store the data matrix, which requires O of nd floats.
// MAGIC Additionally, we must compute X transpose X and its inverse.
// MAGIC In order to solve for w, each of these matrices
// MAGIC are d by d and thus require O of d squared floats.
// MAGIC These are the two bottlenecks storage-wise.
// MAGIC And thus our space complexity is O of nd plus d squared.
// MAGIC So now that we've considered the time and space
// MAGIC complexity required to solve for the closed-form solution, let's
// MAGIC consider what happens as our data grows large.
// MAGIC The first situation we'll consider
// MAGIC is one where n, or the number of observations,
// MAGIC is large, while d, or the number of features,
// MAGIC is relatively small.
// MAGIC Specifically, we'll assume that we're
// MAGIC in a setting where d is small enough such that O of d cubed
// MAGIC operate computation and O of d squared storage
// MAGIC is feasible on a single machine.
// MAGIC In this scenario, the terms in our big O complexity involving
// MAGIC n are the ones that dominate, and thus
// MAGIC storing X and computing X transpose X
// MAGIC are the bottlenecks.
// MAGIC It turns out that in this scenario,
// MAGIC we're well suited for a distributed computation.
// MAGIC First, we can store the data points or rows of X
// MAGIC across several machines, thus reducing the storage burden.
// MAGIC Second, we can compute X transpose X
// MAGIC in parallel across the machines by treating this multiplication
// MAGIC as a sum of outer products.
// MAGIC To understand this alternative interpretation
// MAGIC of matrix multiplication in terms of outer products,
// MAGIC let's first recall our typical definition
// MAGIC of matrix multiplication.
// MAGIC We usually think about each entry
// MAGIC of the output matrix being computed
// MAGIC via an inner product between rows and columns of the input
// MAGIC matrices.
// MAGIC So, for instance, in the example on the slide,
// MAGIC to compute the top left entry, we
// MAGIC compute the inner product between the first row
// MAGIC of the left input matrix and the first column of the right input
// MAGIC matrix.
// MAGIC Similarly, to compute the top right entry,
// MAGIC we compute the inner product between the first row
// MAGIC of the left input matrix and the second column
// MAGIC of the right input matrix.
// MAGIC We perform additional inner products
// MAGIC to compute the two remaining entries of the output matrix.
// MAGIC There is, however, an alternative interpretation
// MAGIC of matrix multiplication as the sum
// MAGIC of outer products between corresponding rows and columns
// MAGIC of the input matrices.
// MAGIC Let's look at the same example from the last slide
// MAGIC to get a better sense of what this means.
// MAGIC First consider the first column of the left input matrix
// MAGIC and the first row of the right input matrix.
// MAGIC We can compute their outer product
// MAGIC with the result being the 2 by 2 matrix
// MAGIC on the bottom of the slide.
// MAGIC Next, we can consider the second column of the left input matrix
// MAGIC and the second row of the right input matrix,
// MAGIC and again compute their outer product, resulting in another 2
// MAGIC by 2 matrix.
// MAGIC We can repeat this process a third time
// MAGIC to generate a third outer product or 2 by 2 matrix.
// MAGIC The sum of these outer products matches the result
// MAGIC we obtained in the previous slide using
// MAGIC the traditional definition of matrix multiplication.
// MAGIC And more generally, taking a sum of outer products
// MAGIC of corresponding rows and columns of the input matrices,
// MAGIC always returns the desired matrix multiplication result.
// MAGIC Now we can use this new interpretation
// MAGIC of matrix multiplication to our benefit
// MAGIC when distributing the computation of X transpose
// MAGIC X for linear regression.
// MAGIC Let's first represent X visually by its rows or data points.
// MAGIC Then we can express this matrix multiplication
// MAGIC as a sum of outer products where each outer product involves
// MAGIC only a single row of X or a single data point.
// MAGIC Let's see how we can use this insight
// MAGIC to effectively distribute our computation.
// MAGIC Consider a toy example where we have
// MAGIC a cluster of three workers and a data set with six data points.
// MAGIC We can distribute the storage of our six data points
// MAGIC across the three workers so that each worker
// MAGIC is storing two data points.
// MAGIC Now we can express matrix multiplication
// MAGIC as a simple MapReduce operation.
// MAGIC In the map step, we take each point
// MAGIC and compute its outer product with itself.
// MAGIC And in the subsequent reduce step,
// MAGIC we simply sum over all of these outer products.
// MAGIC We can then solve for the final linear regression model
// MAGIC locally, which includes computing the inverse
// MAGIC of this resulting matrix.
// MAGIC Now let's look at the storage and computation
// MAGIC involved at each step.
// MAGIC In the first step, we're not doing any computation,
// MAGIC but we need to store the input data, which
// MAGIC requires O of nd storage.
// MAGIC This is a bottleneck in our setting since n is large.
// MAGIC However, the storage can be distributed
// MAGIC over several machines.
// MAGIC Next, during the map step, we perform an outer product
// MAGIC for each data point.
// MAGIC Each outer product takes O of d squared time,
// MAGIC and we have to compute n of these outer products.
// MAGIC This is the computational bottleneck in our setting,
// MAGIC but again, it is distributed across multiple workers.
// MAGIC In terms of storage, we must store the outer products
// MAGIC computed on each machine.
// MAGIC Note that although we may be computing
// MAGIC several outer products per machine,
// MAGIC we can keep a running sum of these outer products,
// MAGIC so the local storage required for each machine
// MAGIC is O of d squared.
// MAGIC Finally, in the reduce step, we must
// MAGIC take the sum of these outer products,
// MAGIC though the computational bottleneck
// MAGIC is, in fact, inverting the resulting matrix, which
// MAGIC is cubic nd.
// MAGIC However, we're assuming that d is small enough
// MAGIC for this computation to be feasible on a single machine.
// MAGIC Similarly, the O of d squared storage required
// MAGIC to store X transpose X and its inverse
// MAGIC is also feasible on a single machine by assumption.
// MAGIC This entire process can be concisely
// MAGIC summarized via the following Spark code snippet.
// MAGIC In this code, train data is an RDD of rows of X.
// MAGIC In the map step, we compute an outer product for each row.
// MAGIC And in the reduce step, we sum these outer products
// MAGIC and invert the resulting matrix.
// MAGIC In the final reduce step, we can also
// MAGIC perform the remaining steps required to obtain
// MAGIC our final regression model.

// COMMAND ----------

// MAGIC %md
// MAGIC #### Distributed Machine Learning: Computation and Storage (Part 2) by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning
// MAGIC **(watch now/later? 4:02)**:
// MAGIC 
// MAGIC [![Distributed Machine Learning: Computation and Storage part 2 by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/CYMZKbnDKsU/0.jpg)](https://www.youtube.com/watch?v=CYMZKbnDKsU)

// COMMAND ----------

// MAGIC %md
// MAGIC This is a transcript of Ameet's lecture in the video above:
// MAGIC 
// MAGIC **Homework:** Make sure you are understanding everything he is trying to convey!
// MAGIC 
// MAGIC In this segment, we'll continue our discussion
// MAGIC of distributed machine learning principles related
// MAGIC to computation and storage.
// MAGIC We'll focus on the problem when D, the number of features,
// MAGIC grows large.
// MAGIC In the previous segment, we discussed the big N small D
// MAGIC setting.
// MAGIC In this setting, we can naturally
// MAGIC use a distributed computing environment
// MAGIC to solve for the linear regression closed
// MAGIC form solution.
// MAGIC To do this, we store our data across multiple machines
// MAGIC and we compute X transpose X as a sum of outer products.
// MAGIC This strategy can be written as a simple MapReduce
// MAGIC operation, expressed very concisely in Spark.
// MAGIC Now, let's consider what happens when D grows large.
// MAGIC As before, storing X and computing X transpose X
// MAGIC are bottlenecks.
// MAGIC However, storing and operating on X transpose X
// MAGIC is now also a bottleneck.
// MAGIC And we can no longer use our previous strategy.
// MAGIC So let's see what goes wrong.
// MAGIC Here's what our strategy looks like in the small D
// MAGIC setting with data stored across workers,
// MAGIC outer products computed in the map step,
// MAGIC and sum of these outer products performed in the reduced step.
// MAGIC However, we can no longer perform D cubed operations
// MAGIC locally or store D squared floats locally
// MAGIC in our new setting.
// MAGIC This issue leads to a more general rule of thumb,
// MAGIC which is that when N and D are large,
// MAGIC we need the computation and storage complexity to be
// MAGIC at most linear in N and D.
// MAGIC So how do we devise methods that are linear in space and time
// MAGIC complexity?
// MAGIC One idea is to exploit sparsity.
// MAGIC Sparse data is quite prevalent in practice.
// MAGIC Some data is inherently sparse, such as rating information
// MAGIC and collaborative filtering problems or social networking
// MAGIC or other grafted.
// MAGIC Additionally, we often generate sparse features
// MAGIC during a process of feature extraction,
// MAGIC such as when we represent text documents
// MAGIC via a bag-of-words features or when
// MAGIC we convert categorical features into numerical representations.
// MAGIC Accounting for sparsity can lead to orders
// MAGIC of magnitudes of savings in terms
// MAGIC of storage and computation.
// MAGIC A second idea is to make a late and sparsity assumption,
// MAGIC whereby we make the assumption that our high dimensional data
// MAGIC can in fact be represented in a more succinct fashion,
// MAGIC either exactly or approximately.
// MAGIC For example, we can make a low rank modeling assumption
// MAGIC where we might assume that our data matrix can in fact be
// MAGIC represented by the product of two skinny matrices, where
// MAGIC the skinny dimension R is much smaller than either N or D.
// MAGIC Exploiting this assumption can also
// MAGIC yield significant computational and storage gigs.
// MAGIC A third option is to use different algorithms.
// MAGIC For instance, instead of learning a linear regression
// MAGIC model via the closed form solution,
// MAGIC we could alternatively use gradient descent.
// MAGIC Gradient descent is an iterative algorithm
// MAGIC that requires layer computation and storage at each iteration
// MAGIC thus making it attractive in the big N and big D setting.
// MAGIC So let's see how gradient descent stacks up
// MAGIC with a closed form solution in our toy example on a cluster
// MAGIC with three machines.
// MAGIC As before, we can store the data across the worker machines.
// MAGIC Now in the map step, we require O of ND computation,
// MAGIC and this computation is distributed across workers.
// MAGIC And we also require O of D storage locally.
// MAGIC In the reduced step, we require O of D local computation
// MAGIC as well as O of D local storage.
// MAGIC Moreover, unlike the closed form case,
// MAGIC we need to repeat this process several times
// MAGIC since gradient descent is an iterative algorithm.
// MAGIC At this point, I haven't really told you
// MAGIC how these question marks work.
// MAGIC And in the next segment, we'll talk
// MAGIC about what actually is going on with gradient
// MAGIC decent.

// COMMAND ----------

// MAGIC %md
// MAGIC #### Communication Hierarchy by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning
// MAGIC **(watch later 2:32)**:
// MAGIC 
// MAGIC [![Communication Hierarchy by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/ABkUwJWn1d8/0.jpg)](https://www.youtube.com/watch?v=ABkUwJWn1d8)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ##### SUMMARY: Access rates fall sharply with distance.
// MAGIC 
// MAGIC * roughly 50 x gap between reading from memory and reading from either disk or the network.
// MAGIC 
// MAGIC We must take this communication hierarchy into consideration when developing parallel and distributed algorithms.

// COMMAND ----------

// MAGIC %md
// MAGIC #### Distributed Machine Learning: Communication Principles by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning
// MAGIC **(watch later 11:28)**:
// MAGIC 
// MAGIC [![Distributed Machine Learning: Communication Principles by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/VT5F9tsV4hY/0.jpg)](https://www.youtube.com/watch?v=VT5F9tsV4hY)

// COMMAND ----------

// MAGIC %md
// MAGIC #### Focusing on strategies to reduce communication costs.
// MAGIC 
// MAGIC * access rates fall sharply with distance.
// MAGIC * so this communication hierarchy needs to be accounted for when developing parallel and distributed algorithms.
// MAGIC 
// MAGIC **Lessons:**
// MAGIC 
// MAGIC * parallelism makes our computation faster
// MAGIC * but network communication slows us down
// MAGIC 
// MAGIC * BINGO: perform parallel and in-memory computation.
// MAGIC * Persisting in memory is a particularly attractive option when working with iterative algorithms that
// MAGIC read the same data multiple times, as is the case in gradient descent.
// MAGIC 
// MAGIC * Several machine learning algorithms are iterative!
// MAGIC 
// MAGIC * Limits of multi-core scaling (powerful multicore machine with several CPUs,
// MAGIC and a huge amount of RAM).
// MAGIC   * advantageous: 
// MAGIC       * sidestep any network communication when working with a single multicore machine
// MAGIC       * can indeed handle fairly large data sets, and they're an attractive option in many settings.
// MAGIC   * disadvantages: 
// MAGIC       * can be quite expensive (due to specialized hardware),
// MAGIC       * not as widely accessible as commodity computing nodes.
// MAGIC       * this approach does have scalability limitations, as we'll eventually hit a wall when the data grows large enough! This is not the case for a distributed environment (like the AWS EC2 cloud under the hood here).
// MAGIC 
// MAGIC 
// MAGIC 
// MAGIC #### Simple strategies for algorithms in a distributed setting: to reduce network communication, simply keep large objects local
// MAGIC 
// MAGIC * In the big n, small d case for linear regression 
// MAGIC     * we can solve the problem via a closed form solution.
// MAGIC     * And this requires us to communicate \\(O(d)^2\\) intermediate data.
// MAGIC     * the largest object in this example is our initial data, which we store in a distributed fashion and never communicate! This is a *data parallel setting*.
// MAGIC * In the big n, big d case:
// MAGIC   * for linear regression.
// MAGIC     * we use gradient descent to iteratively train our model and are again in a *data parallel setting*.
// MAGIC     * At each iteration we communicate the current parameter vector \\(w_i\\) and the required \\(O(d)\\) communication is feasible even for fairly large d.
// MAGIC 
// MAGIC * In the small n, small d case:
// MAGIC   * for ridge regression
// MAGIC     * we can communicate the small data to all of the workers.
// MAGIC     * this is an example of a *model parallel setting* where we can train the model for each hyper-parameter in parallel.
// MAGIC 
// MAGIC * Linear regression with big n and huge d is an example of both data and model parallelism.
// MAGIC 
// MAGIC **HOMEWORK:** Watch the video and find out why Linear regression with big n and huge d is an example of both data and model parallelism.
// MAGIC 
// MAGIC In this setting, since our data is large,
// MAGIC we must still store it across multiple machines.
// MAGIC We can still use gradient descent, or stochastic variants
// MAGIC of gradient descent to train our model,
// MAGIC but we may not want to communicate
// MAGIC the entire d dimensional parameter
// MAGIC vector at each iteration, when we have 10s,
// MAGIC or hundreds of millions of features.
// MAGIC In this setting we often rely on sparsity
// MAGIC to reduce the communication.
// MAGIC So far we discussed how we can reduce communication
// MAGIC by keeping large data local.

// COMMAND ----------

// MAGIC %md
// MAGIC #### Simple strategies for algorithms in a distributed setting: compute more and communicate less per iteration
// MAGIC **HOMEWORK:** watch the video and understand why it is important at each iteration of an iterative algorithm
// MAGIC to compute more and communicate less.

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC **Recall** from week 1's lecture that the ideal mathematical preparation to fully digest this material requires a set of self-tutorials from Reza Zadeh's course in Distributed Algorithms and Optimization from Stanford:
// MAGIC 
// MAGIC * [http://stanford.edu/~rezab/dao/](http://stanford.edu/~rezab/dao/).
// MAGIC 
// MAGIC This is a minimal pre-requisite for designing new algorithms or improving exixting ones!!!