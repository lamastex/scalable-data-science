// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video (No Sound - Sorry!) of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/cVXjBz4B2q4/0.jpg)](https://www.youtube.com/embed/cVXjBz4B2q4?start=0&end=5910&autoplay=1) 

// COMMAND ----------

// MAGIC %md
// MAGIC # Movie Recommender using Alternating Least Squares
// MAGIC 
// MAGIC 
// MAGIC Most of the content below is just a scarification of from Module 5 of Anthony Joseph's edX course CS100-1x from the Community Edition of databricks.

// COMMAND ----------

// MAGIC %md
// MAGIC ## What is Collaborarive Filtering?

// COMMAND ----------

//This allows easy embedding of publicly available information into any other notebook
//when viewing in git-book just ignore this block - you may have to manually chase the URL in frameIt("URL").
//Example usage:
// displayHTML(frameIt("https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation#Topics_in_LDA",250))
def frameIt( u:String, h:Int ) : String = {
      """<iframe 
 src=""""+ u+""""
 width="95%" height="""" + h + """"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe>"""
   }
displayHTML(frameIt("https://en.wikipedia.org/wiki/Collaborative_filtering", 450))

// COMMAND ----------

// MAGIC %md
// MAGIC SOURCE: Module 5 of AJ's course.
// MAGIC 
// MAGIC 
// MAGIC ## **Collaborative Filtering**
// MAGIC 
// MAGIC **(watch now 61 seconds - from 18 to 79 seconds)**:
// MAGIC 
// MAGIC [![AJ's What is Collaborative Filtering](http://img.youtube.com/vi/0HGELVQSHb8/0.jpg)](https://www.youtube.com/watch?v=0HGELVQSHb8)
// MAGIC 
// MAGIC 
// MAGIC Let us use MLlib to make personalized movie recommendations.
// MAGIC  
// MAGIC We are going to use a technique called [collaborative filtering][collab]. Collaborative filtering is a method of making automatic predictions (filtering) about the interests of a user by collecting preferences or taste information from many users (collaborating). The underlying assumption of the collaborative filtering approach is that if a person A has the same opinion as a person B on an issue, A is more likely to have B's opinion on a different issue x than to have the opinion on x of a person chosen randomly. You can read more about collaborative filtering [here][collab2].
// MAGIC  
// MAGIC The image below (from [Wikipedia][collab]) shows an example of predicting of the user's rating using collaborative filtering. At first, people rate different items (like videos, images, games). After that, the system is making predictions about a user's rating for an item, which the user has not rated yet. These predictions are built upon the existing ratings of other users, who have similar ratings with the active user. For instance, in the image below the system has made a prediction, that the active user will not like the video.
// MAGIC 
// MAGIC ![collaborative filtering](https://courses.edx.org/c4x/BerkeleyX/CS100.1x/asset/Collaborative_filtering.gif)
// MAGIC  
// MAGIC [mllib]: https://spark.apache.org/mllib/
// MAGIC [collab]: https://en.wikipedia.org/?title=Collaborative_filtering
// MAGIC [collab2]: http://recommender-systems.org/collaborative-filtering/
// MAGIC  
// MAGIC **Resources:**
// MAGIC 
// MAGIC * [mllib](https://spark.apache.org/mllib/)
// MAGIC * [Wikipedia - collaborative filtering](https://en.wikipedia.org/?title=Collaborative_filtering)
// MAGIC * [Recommender Systems - collaborative filtering](http://recommender-systems.org/collaborative-filtering/)

// COMMAND ----------

// MAGIC %md
// MAGIC For movie recommendations, we start with a matrix whose entries are movie ratings by users (shown in red in the diagram below).  Each row represents a user and each column represents a particular movie. Thus the entry \\(r_{ij}\\) represents the rating of user \\(i\\) for movie \\(j\\).
// MAGIC  
// MAGIC Since not all users have rated all movies, we do not know all of the entries in this matrix, which is precisely why we need collaborative filtering.  For each user, we have ratings for only a subset of the movies.  With collaborative filtering, *the idea is to approximate the ratings matrix by factorizing it as the product of two matrices*: one that describes properties of each user (shown in green), and one that describes properties of each movie (shown in blue).
// MAGIC  
// MAGIC ![factorization](http://spark-mooc.github.io/web-assets/images/matrix_factorization.png)
// MAGIC 
// MAGIC We want to select these two matrices such that the error for the users/movie pairs where we know the correct ratings is minimized. 
// MAGIC The [Alternating Least Squares](https://bugra.github.io/work/notes/2014-04-19/alternating-least-squares-method-for-collaborative-filtering/) algorithm expands on the [least squares method][als] by:
// MAGIC 
// MAGIC 1. first randomly filling the users matrix with values and then 
// MAGIC 2. optimizing the value of the movies such that the error is minimized.  
// MAGIC 3. Then, it holds the movies matrix constant and optimizes the value of the users matrix.  
// MAGIC 
// MAGIC This alternation between which matrix to optimize is the reason for the "alternating" in the name.
// MAGIC  
// MAGIC This optimization is what's being shown on the right in the image above.  Given a fixed set of user factors (i.e., values in the users matrix), we use the known ratings to find the best values for the movie factors using the optimization written at the bottom of the figure.  Then we "alternate" and pick the best user factors given fixed movie factors.
// MAGIC  
// MAGIC For a simple example of what the users and movies matrices might look like, check out the [videos from Lecture 8][videos] or the [slides from Lecture 8][slides] of AJ's Introduction to Data Science course.
// MAGIC 
// MAGIC [videos]: https://courses.edx.org/courses/BerkeleyX/CS100.1x/1T2015/courseware/00eb8b17939b4889a41a6d8d2f35db83/3bd3bba368be4102b40780550d3d8da6/
// MAGIC [slides]: https://courses.edx.org/c4x/BerkeleyX/CS100.1x/asset/Week4Lec8.pdf
// MAGIC [als]: https://en.wikiversity.org/wiki/Least-Squares_Method

// COMMAND ----------

// MAGIC %md
// MAGIC See
// MAGIC [http://spark.apache.org/docs/latest/mllib-collaborative-filtering.html#collaborative-filtering](http://spark.apache.org/docs/latest/mllib-collaborative-filtering.html#collaborative-filtering).

// COMMAND ----------

display(dbutils.fs.ls("/databricks-datasets/cs100/lab4/data-001/")) // The data is already here

// COMMAND ----------

// MAGIC %md
// MAGIC #### **Import**
// MAGIC Let us import the relevant libraries for `mllib`.

// COMMAND ----------

import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.mllib.recommendation.Rating

// COMMAND ----------

// MAGIC %md
// MAGIC #### **Preliminaries**
// MAGIC We read in each of the files and create an RDD consisting of parsed lines.
// MAGIC Each line in the ratings dataset (`ratings.dat.gz`) is formatted as:
// MAGIC   `UserID::MovieID::Rating::Timestamp`
// MAGIC Each line in the movies (`movies.dat`) dataset is formatted as:
// MAGIC   `MovieID::Title::Genres`
// MAGIC The `Genres` field has the format
// MAGIC   `Genres1|Genres2|Genres3|...`
// MAGIC The format of these files is uniform and simple, so we can use `split()`.
// MAGIC 
// MAGIC Parsing the two files yields two RDDs
// MAGIC 
// MAGIC * For each line in the ratings dataset, we create a tuple of (UserID, MovieID, Rating). We drop the timestamp because we do not need it for this exercise.
// MAGIC * For each line in the movies dataset, we create a tuple of (MovieID, Title). We drop the Genres because we do not need them for this exercise.

// COMMAND ----------

// take a peek at what's in the rating file
sc.textFile("/databricks-datasets/cs100/lab4/data-001/ratings.dat.gz").map { line => line.split("::") }.take(5)

// COMMAND ----------

val timedRatingsRDD = sc.textFile("/databricks-datasets/cs100/lab4/data-001/ratings.dat.gz").map { line =>
      val fields = line.split("::")
      // format: (timestamp % 10, Rating(userId, movieId, rating))
      (fields(3).toLong % 10, Rating(fields(0).toInt, fields(1).toInt, fields(2).toDouble))
    }

// COMMAND ----------

// MAGIC %md
// MAGIC We have a look at the first 10 entries in the Ratings RDD to check it's ok

// COMMAND ----------

timedRatingsRDD.take(10).map(println)

// COMMAND ----------

// MAGIC %md
// MAGIC The timestamp is unused here so we want to remove it

// COMMAND ----------

val ratingsRDD = sc.textFile("/databricks-datasets/cs100/lab4/data-001/ratings.dat.gz").map { line =>
      val fields = line.split("::")
      // format: Rating(userId, movieId, rating)
      Rating(fields(0).toInt, fields(1).toInt, fields(2).toDouble)
    }

// COMMAND ----------

// MAGIC %md
// MAGIC Now our final ratings RDD looks as follows:

// COMMAND ----------

ratingsRDD.take(10).map(println)

// COMMAND ----------

// MAGIC %md
// MAGIC A similar command is used to format the movies. We ignore the genres in this recommender

// COMMAND ----------

val movies = sc.textFile("/databricks-datasets/cs100/lab4/data-001/movies.dat").map { line =>
      val fields = line.split("::")
      // format: (movieId, movieName)
      (fields(0).toInt, fields(1))
    }.collect.toMap

// COMMAND ----------

// MAGIC %md
// MAGIC Let's make a data frame to visually explore the data next.

// COMMAND ----------

sc.textFile("/databricks-datasets/cs100/lab4/data-001/ratings.dat.gz").map { line => line.split("::") }.take(5)

// COMMAND ----------

val timedRatingsDF = sc.textFile("/databricks-datasets/cs100/lab4/data-001/ratings.dat.gz").map { line =>
      val fields = line.split("::")
      // format: (timestamp % 10, Rating(userId, movieId, rating))
      (fields(3).toLong, fields(0).toInt, fields(1).toInt, fields(2).toDouble)
    }.toDF("timestamp", "userId", "movieId", "rating")

// COMMAND ----------

display(timedRatingsDF)

// COMMAND ----------

// MAGIC %md
// MAGIC Here we simply check the size of the datasets we are using

// COMMAND ----------

val numRatings = ratingsRDD.count
val numUsers = ratingsRDD.map(_.user).distinct.count
val numMovies = ratingsRDD.map(_.product).distinct.count

println("Got " + numRatings + " ratings from "
        + numUsers + " users on " + numMovies + " movies.")

// COMMAND ----------

// MAGIC %md
// MAGIC Now that we have the dataset we need, let's make a recommender system.
// MAGIC 
// MAGIC **Creating a Training Set, test Set and Validation Set**
// MAGIC 
// MAGIC Before we jump into using machine learning, we need to break up the `ratingsRDD` dataset into three pieces:
// MAGIC 
// MAGIC * A training set (RDD), which we will use to train models
// MAGIC * A validation set (RDD), which we will use to choose the best model
// MAGIC * A test set (RDD), which we will use for our experiments
// MAGIC 
// MAGIC To randomly split the dataset into the multiple groups, we can use the `randomSplit()` transformation. `randomSplit()` takes a set of splits and seed and returns multiple RDDs.

// COMMAND ----------

val Array(trainingRDD, validationRDD, testRDD) = ratingsRDD.randomSplit(Array(0.60, 0.20, 0.20), 0L)

// COMMAND ----------

// MAGIC %md
// MAGIC After splitting the dataset, your training set has about 293,000 entries and the validation and test sets each have about 97,000 entries (the exact number of entries in each dataset varies slightly due to the random nature of the `randomSplit()` transformation.

// COMMAND ----------

// let's find the exact sizes we have next
println(" training data size = " + trainingRDD.count() +
        ", validation data size = " + validationRDD.count() +
        ", test data size = " + testRDD.count() + ".")

// COMMAND ----------

// MAGIC %md
// MAGIC See [http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.recommendation.ALS](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.recommendation.ALS).

// COMMAND ----------

// MAGIC %md
// MAGIC **(2c) Using ALS.train()**
// MAGIC  
// MAGIC In this part, we will use the MLlib implementation of Alternating Least Squares, [ALS.train()](https://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.recommendation.ALS). ALS takes a training dataset (RDD) and several parameters that control the model creation process. To determine the best values for the parameters, we will use ALS to train several models, and then we will select the best model and use the parameters from that model in the rest of this lab exercise.
// MAGIC  
// MAGIC The process we will use for determining the best model is as follows:
// MAGIC 
// MAGIC * Pick a set of model parameters. The most important parameter to `ALS.train()` is the *rank*, which is the number of rows in the Users matrix (green in the diagram above) or the number of columns in the Movies matrix (blue in the diagram above). (In general, a lower rank will mean higher error on the training dataset, but a high rank may lead to [overfitting](https://en.wikipedia.org/wiki/Overfitting).)  We will train models with ranks of 4, 8, and 12 using the `trainingRDD` dataset.
// MAGIC * Create a model using `ALS.train(trainingRDD, rank, seed=seed, iterations=iterations, lambda_=regularizationParameter)` with three parameters: an RDD consisting of tuples of the form (UserID, MovieID, rating) used to train the model, an integer rank (4, 8, or 12), a number of iterations to execute (we will use 5 for the `iterations` parameter), and a regularization coefficient (we will use 0.1 for the `regularizationParameter`).
// MAGIC * For the prediction step, create an input RDD, `validationForPredictRDD`, consisting of (UserID, MovieID) pairs that you extract from `validationRDD`. You will end up with an RDD of the form: `[(1, 1287), (1, 594), (1, 1270)]`
// MAGIC * Using the model and `validationForPredictRDD`, we can predict rating values by calling [model.predictAll()](https://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.recommendation.MatrixFactorizationModel.predictAll) with the `validationForPredictRDD` dataset, where `model` is the model we generated with ALS.train().  `predictAll` accepts an RDD with each entry in the format (userID, movieID) and outputs an RDD with each entry in the format (userID, movieID, rating).

// COMMAND ----------

// Build the recommendation model using ALS by fitting to the training data
// using a fixed rank=10, numIterations=10 and regularisation=0.01
val rank = 10
val numIterations = 10
val model = ALS.train(trainingRDD, rank, numIterations, 0.01)

// COMMAND ----------

// Evaluate the model on test data
val usersProductsTest = testRDD.map { case Rating(user, product, rate) =>
  (user, product)
}

// COMMAND ----------

usersProductsTest.take(10) // Checking

// COMMAND ----------

// get the predictions on test data
val predictions =
  model.predict(usersProductsTest).map { case Rating(user, product, rate) =>
    ((user, product), rate)
  }

// COMMAND ----------

// find the actual ratings and join with predictions
val ratesAndPreds = testRDD.map { case Rating(user, product, rate) =>
  ((user, product), rate)
}.join(predictions)

// COMMAND ----------

ratesAndPreds.take(10).map(println) // print first 10 pairs of (user,product) and (true_rating, predicted_rating)

// COMMAND ----------

// MAGIC %md
// MAGIC Let's evaluate the model using Mean Squared Error metric.

// COMMAND ----------

val MSE = ratesAndPreds.map { case ((user, product), (r1, r2)) =>
  val err = (r1 - r2)
  err * err
}.mean()
println("Mean Squared Error = " + MSE)

// COMMAND ----------

// MAGIC %md
// MAGIC Can we improve the MSE by changing one of the hyper parameters?

// COMMAND ----------

// Build the recommendation model using ALS by fitting to the validation data
// just trying three different hyper-parameter (rank) values to optimise over
val ranks = List(4, 8, 12); 
var rank=0;
for ( rank <- ranks ){
  val numIterations = 10
  val regularizationParameter = 0.01
  val model = ALS.train(trainingRDD, rank, numIterations, regularizationParameter)

  // Evaluate the model on test data
  val usersProductsValidate = validationRDD.map { case Rating(user, product, rate) =>
                                              (user, product)
  }

  // get the predictions on test data
  val predictions = model.predict(usersProductsValidate)
                         .map { case Rating(user, product, rate)
                                     => ((user, product), rate)
    }

  // find the actual ratings and join with predictions
  val ratesAndPreds = validationRDD.map { case Rating(user, product, rate) 
                                     => ((user, product), rate)
                                   }.join(predictions)
  

  val MSE = ratesAndPreds.map { case ((user, product), (r1, r2)) =>
    val err = (r1 - r2)
    err * err
  }.mean()
  
  println("rank and Mean Squared Error = " +  rank + " and " + MSE)
} // end of loop over ranks

// COMMAND ----------

// MAGIC %md
// MAGIC Now let us try to apply this to the test data and find the MSE for the best model.

// COMMAND ----------

  val rank = 4
  val numIterations = 10
  val regularizationParameter = 0.01
  val model = ALS.train(trainingRDD, rank, numIterations, regularizationParameter)

  // Evaluate the model on test data
  val usersProductsTest = testRDD.map { case Rating(user, product, rate) =>
                                              (user, product)
  }

  // get the predictions on test data
  val predictions = model.predict(usersProductsTest)
                         .map { case Rating(user, product, rate)
                                     => ((user, product), rate)
    }

  // find the actual ratings and join with predictions
  val ratesAndPreds = testRDD.map { case Rating(user, product, rate) 
                                     => ((user, product), rate)
                                   }.join(predictions)

  val MSE = ratesAndPreds.map { case ((user, product), (r1, r2)) =>
    val err = (r1 - r2)
    err * err
  }.mean()
  
  println("rank and Mean Squared Error for test data = " +  rank + " and " + MSE)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ** Potential flaws of CF **
// MAGIC 
// MAGIC * Cold start for users and items
// MAGIC * Gray sheep: [https://en.wikipedia.org/wiki/Collaborative_filtering#Gray_sheep](https://en.wikipedia.org/wiki/Collaborative_filtering#Gray_sheep)
// MAGIC * Shilling attacks: [https://en.wikipedia.org/wiki/Collaborative_filtering#Shilling_attacks](https://en.wikipedia.org/wiki/Collaborative_filtering#Shilling_attacks)
// MAGIC * Positive feedback problems (rich-get-richer effect): [https://en.wikipedia.org/wiki/Collaborative_filtering#Diversity_and_the_long_tail](https://en.wikipedia.org/wiki/Collaborative_filtering#Diversity_and_the_long_tail)

// COMMAND ----------

// MAGIC %md
// MAGIC ** Areas to improve upon **
// MAGIC 
// MAGIC * Works in theory but didn't manage to produce a system that takes user info and outputs suggestions
// MAGIC * More complete models would include analysing the genres to give better recommendations.
// MAGIC * For first time users, the program could give the top rated movies over all users.
// MAGIC * Could have used bigger dataset