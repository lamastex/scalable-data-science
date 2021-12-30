// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md 
// MAGIC # [The Alternating Least Squares method (ALS)]() 

// COMMAND ----------

// MAGIC %md 
// MAGIC 
// MAGIC ## The ALS algorithm 
// MAGIC 
// MAGIC 
// MAGIC The ALS algorithm was proposed in 2008 by *F. Zhang, E.Shang, Y. Xu and X. Wu* in a paper titled : **Large-scale Parallel Colaborative Filtering for the Netflix Prize** [(paper)](https://link.springer.com/chapter/10.1007/978-3-540-68880-8_32). We will briefly describe the main ideas behind the ALS algorithm. 
// MAGIC #### What are we learning ?
// MAGIC In order to finding the missing values of the rating matrix R, the authors of the ALS algorithm considered approximating this matrix by a product of two tall matrices  U and M of low rank. In other words, the goal is to find a low rank approximation of the ratings matrix R:  
// MAGIC 
// MAGIC $$
// MAGIC R \approx U M^\top = \begin{bmatrix} u_1 & \dots & u_N \end{bmatrix}^\top  \begin{bmatrix}
// MAGIC m_1 & \dots & m_M \end{bmatrix} \qquad \text{where} \qquad U \in \mathbb{R}^{N \times K}, M \in \mathbb{R}^{M \times K}
// MAGIC $$
// MAGIC 
// MAGIC Intuitively we think of U (resp. M) as a matrix of users' features (resp. movies features) and we may rewrite this approximation entrywise as 
// MAGIC 
// MAGIC $$
// MAGIC \forall i,j \qquad r_{i,j} \approx u_i^\top m_j. 
// MAGIC $$
// MAGIC 
// MAGIC #### The loss function 
// MAGIC If all entries of the rating matrix R were known, one may use an SVD decomposition to reconstruct U and M. However, not all ratings are known therefore one has to learn the matrices U and M. The authors of the paper proposed to minimize the following loss which corresponds to the sum of squares errors with a Thikonov rigularization that weighs the users matrix U (resp. the movies matrix M) using the Gamma_U (resp. Gamma_M)
// MAGIC 
// MAGIC $$
// MAGIC \mathcal{L}_{U,M}^{wheighted}(R) = \sum_{(i,j)\in S}
// MAGIC (r_{i,j} - u_i^\top m_j)^2 + \lambda \Vert M \Gamma_m \Vert^2 + \lambda \Vert U \Gamma_u \Vert^2 
// MAGIC $$
// MAGIC 
// MAGIC where S corresponds to the set of known ratings, \lambda is a regularaziation parameter. In fact this loss corresponds to the Alternating Least Squares with Weigted Regularization (ALS-WR). We will be using a variant of that algorithm a.k.a. the ALS algorithm which corresponds to minimizing the following slighltly similar loss without wheighing:
// MAGIC 
// MAGIC $$
// MAGIC \mathcal{L}_{U,M}(R) = \sum_{(i,j)\in S}
// MAGIC (r_{i,j} - u_i^\top m_j)^2 + \lambda \Vert M \Vert^2 + \lambda \Vert U \Vert^2 
// MAGIC $$
// MAGIC 
// MAGIC and the  goal of the algorithm will be find a condidate (U,M) that 
// MAGIC 
// MAGIC $$
// MAGIC \min_{U,M} \mathcal{L}_{U,M}(R)
// MAGIC $$
// MAGIC 
// MAGIC #### The ALS algorithm 
// MAGIC The authors approach to solve the aforementioned minimization problem as follows: 
// MAGIC - **Step 1.** Initialize matrix M, by assigning the average rating for that movie as the first row and small random numbers for the remaining entries.
// MAGIC - **Step 2.** Fix M, Solve for U by minimizing the aformentioned loss.
// MAGIC - **Step 3.** Fix U, solve for M by minimizing the aformentioned loss similarly. 
// MAGIC - **Step 4.** Repeat Steps 2 and 3 until a stopping criterion is satisfied. 
// MAGIC 
// MAGIC Note that when one of the matrices is fixed, say M, the loss becomes quadratic in U and the solution corresponds to that of the least squares.
// MAGIC 
// MAGIC #### Key parameters of the algorithm 
// MAGIC The key parameters of the lagorithm are the **rank K**, the **regularization parameter lambda**, and the **number of iterations** befor stopping the algorithm. Indeed, since we don not have full knowledge of the matrix R, we do not know its rank. To find the best rank we will use cross-validation and dedicate part of the data to that. There is no straight way to choosing the regularization parameter, we will base our choice on reported values that work for the considered datasets. As for the number of iterations, we will proceed similarly.
// MAGIC 
// MAGIC 
// MAGIC #### Practically speaking 
// MAGIC We will use the following mllib library in scala wich contain classes dedicated to recommendation systems 
// MAGIC (See [http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.recommendation.ALS](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.recommendation.ALS)). More specifically, it contains the ALS class which allows for using the ALS algorithm as described earlier.

// COMMAND ----------

import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.mllib.recommendation.Rating

// COMMAND ----------

// MAGIC %md 
// MAGIC # [On a small dataset]() 
// MAGIC 
// MAGIC This part of the notebook is borrowed from the notebook on the ALS we had in the course.

// COMMAND ----------

display(dbutils.fs.ls("/databricks-datasets/cs100/lab4/data-001/")) // The data is already here

// COMMAND ----------

// MAGIC %md
// MAGIC ### Loading the data
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

timedRatingsRDD.take(10).map(println)

// COMMAND ----------

val ratingsRDD = sc.textFile("/databricks-datasets/cs100/lab4/data-001/ratings.dat.gz").map { line =>
      val fields = line.split("::")
      // format: Rating(userId, movieId, rating)
      Rating(fields(0).toInt, fields(1).toInt, fields(2).toDouble)
    }

ratingsRDD.take(10).map(println)

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
// let's find the exact sizes we have next
println(" training data size = " + trainingRDD.count() +
        ", validation data size = " + validationRDD.count() +
        ", test data size = " + testRDD.count() + ".")

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
// MAGIC ## Training the recommender system

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
// MAGIC Here we have the best model

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

// MAGIC 
// MAGIC %md
// MAGIC 
// MAGIC # [On a large dataset - Netflix dataset]()
// MAGIC 
// MAGIC ## Loading the data
// MAGIC 
// MAGIC Netflix held a competition to improve recommendation systems. The dataset can be found in [kaggle](https://www.kaggle.com/netflix-inc/netflix-prize-data). Briefly speaking, the dataset contains users' ratings to movies, with 480189 users and 17770 movies. Ratings are given on an integral scale from 1 to 5. The first step is to download the data and store it in databricks. Originally, the dataset is plit into four files each with the following format: 
// MAGIC 
// MAGIC ```
// MAGIC MovieID:
// MAGIC UserID, rating, date
// MAGIC .
// MAGIC .
// MAGIC .
// MAGIC MovieID: 
// MAGIC UserID, rating, date
// MAGIC .
// MAGIC .
// MAGIC .
// MAGIC ```
// MAGIC 
// MAGIC We process these files so that each line has the format `MovieID, UserID, rating, date`

// COMMAND ----------

// Path where the data is stored 
display(dbutils.fs.ls("/FileStore/tables/Netflix")) 

// COMMAND ----------

// MAGIC %md Let us load first the movie titles.

// COMMAND ----------

// Create a Movie class 
case class Movie(movieID: Int, year: Int, tilte: String)

// Load the movie titles in an RDD
val moviesTitlesRDD: RDD[Movie] = sc.textFile("/FileStore/tables/Netflix/movie_titles.csv").map { line =>
      val fields = line.split(",")
      // format: Rating(movieId, year, title)
      Movie(fields(0).toInt, fields(1).toInt, fields(2))
    }

// Print the titles of the first 3 movies 
moviesTitlesRDD.take(5).foreach(println)

// COMMAND ----------

import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.mllib.recommendation.Rating

// COMMAND ----------


val RatingsRDD_1 = sc.textFile("/FileStore/tables/Netflix/formatted_combined_data_1_txt.gz").map { line =>
      val fields = line.split(",")
      // format: Rating(userId, movieId, rating))
      Rating(fields(1).toInt, fields(0).toInt, fields(2).toDouble)
    }

val RatingsRDD_2 = sc.textFile("/FileStore/tables/Netflix/formatted_combined_data_2_txt.gz").map { line =>
      val fields = line.split(",")
      // format: Rating(userId, movieId, rating))
      Rating(fields(1).toInt, fields(0).toInt, fields(2).toDouble)
    }

val RatingsRDD_3 = sc.textFile("/FileStore/tables/Netflix/formatted_combined_data_3_txt.gz").map { line =>
      val fields = line.split(",")
      // format: Rating(userId, movieId, rating))
      Rating(fields(1).toInt, fields(0).toInt, fields(2).toDouble)
    }

val RatingsRDD_4 = sc.textFile("/FileStore/tables/Netflix/formatted_combined_data_4_txt.gz").map { line =>
      val fields = line.split(",")
      // format: Rating(userId, movieId, rating))
      Rating(fields(1).toInt, fields(0).toInt, fields(2).toDouble)
    }


RatingsRDD_4.take(5).foreach(println)

// COMMAND ----------

// Concatenating the ratings RDDs (could not find a nice way of doing this)
val r1 = RatingsRDD_1.union(RatingsRDD_2) 
val r2 = r1.union(RatingsRDD_3)
val RatingsRDD = r2.union(RatingsRDD_4)
RatingsRDD.take(5).foreach(println)

// COMMAND ----------

// MAGIC %md 
// MAGIC Let us put our dataset in a dataframe to visulaize it more nicely

// COMMAND ----------

val RatingsDF = RatingsRDD.toDF
display(RatingsDF)

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Training the movie recommender system 
// MAGIC 
// MAGIC In the training process we will start by splitting the dataset into 
// MAGIC - a training set   (60%)
// MAGIC - a validation set (20%)
// MAGIC - a test set       (20%)

// COMMAND ----------

// Splitting the dataset 
val Array(trainingRDD, validationRDD, testRDD) = RatingsRDD.randomSplit(Array(0.60, 0.20, 0.20), 0L)

// COMMAND ----------

// MAGIC %md
// MAGIC After splitting the dataset, your training set has about 60,288,922 entries and the validation and test sets each have about 20,097,527 entries (the exact number of entries in each dataset varies slightly due to the random nature of the `randomSplit()` transformation.

// COMMAND ----------

// let's find the exact sizes we have next
println(" training data size = " + trainingRDD.count() +
        ", validation data size = " + validationRDD.count() +
        ", test data size = " + testRDD.count() + ".")

// COMMAND ----------

// Build the recommendation model using ALS by fitting to the validation data
// just trying three different hyper-parameter (rank) values to optimise over
val ranks = List(50, 100, 150, 300, 400, 500); 
var rank=0;
for ( rank <- ranks ){
  val numIterations = 12
  val regularizationParameter = 0.05
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