// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC # Extensions
// MAGIC 
// MAGIC In this notebook, we introduce multiple improvements to the original algorithm, using the small dataset. We:
// MAGIC -  First, improve the original algorithm by creating a system that takes user info and outputs suggestions, which is the typical final role of a recommendation system.
// MAGIC -  Then, we add the functionality that for first time user, we output the top rated movies over all users.
// MAGIC -  Furthemore, we improve the existing model by including the movie's genres to give better recommendations.

// COMMAND ----------

// MAGIC %md
// MAGIC ## Preliminaries

// COMMAND ----------

// import the relevant libraries for `mllib`

import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark.sql.expressions.UserDefinedFunction
import scala.collection.mutable.WrappedArray

// COMMAND ----------

// Get the small dataset and display information
display(dbutils.fs.ls("/databricks-datasets/cs100/lab4/data-001/")) // The data is already here

// COMMAND ----------

// Create the RDD containing the ratings

val ratingsRDD = sc.textFile("/databricks-datasets/cs100/lab4/data-001/ratings.dat.gz").map { line =>
      val fields = line.split("::")
      // format: Rating(userId, movieId, rating)
      Rating(fields(0).toInt, fields(1).toInt, fields(2).toDouble)
}

// COMMAND ----------

// We take a look at the first 10 entries in the Ratings RDD
ratingsRDD.take(10).map(println)

// COMMAND ----------

// MAGIC %md
// MAGIC A similar command is used to format the movies. For this first part the genre field is ignored. They will considered in the second part of this notebook.

// COMMAND ----------

val movies = sc.textFile("/databricks-datasets/cs100/lab4/data-001/movies.dat").map { line =>
      val fields = line.split("::")
      // format: (movieId, movieName)
      (fields(0).toInt, fields(1))
    }.collect.toMap

// COMMAND ----------

// check the size of the small dataset

val numRatings = ratingsRDD.count
val numUsers = ratingsRDD.map(_.user).distinct.count
val numMovies = ratingsRDD.map(_.product).distinct.count

println("Got " + numRatings + " ratings from "
        + numUsers + " users on " + numMovies + " movies.")

// COMMAND ----------

// Creating a Training Set, test Set and Validation Set

val Array(trainingRDD, validationRDD, testRDD) = ratingsRDD.randomSplit(Array(0.60, 0.20, 0.20), 0L)

// COMMAND ----------

// let's find the exact sizes we have next
println(" training data size = " + trainingRDD.count() +
        ", validation data size = " + validationRDD.count() +
        ", test data size = " + testRDD.count() + ".")

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ## Ratings distribution
// MAGIC 
// MAGIC For curiosity, we start by plotting the histogram of the ratings present in this dataset

// COMMAND ----------

// Create a DataFrame with the data
import org.apache.spark.sql.functions._

val ratingsDF = sc.textFile("/databricks-datasets/cs100/lab4/data-001/ratings.dat.gz").map { line =>
      val fields = line.split("::")
      // format: (timestamp % 10, Rating(userId, movieId, rating))
      (fields(0).toInt, fields(1).toInt, fields(2).toDouble)
    }.toDF("userID", "movieID", "rating")
display(ratingsDF)

// COMMAND ----------

val history = ratingsDF.groupBy("rating").count().orderBy(asc("rating"))
history.show()

// COMMAND ----------

display(history)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Create a system that takes user info and outputs suggestions.
// MAGIC 
// MAGIC user info = ((movieID,rating),(movieID,rating)). It is basically an (incomplete) line in the ratings matrix.
// MAGIC 
// MAGIC - Choose an user
// MAGIC - Run the model and fill the columns - predict the ratings for the movies
// MAGIC - Output the ones with the best predicted score

// COMMAND ----------

// Train the model as usually
  val rank = 4
  val numIterations = 10
  val regularizationParameter = 0.01
  val model = ALS.train(trainingRDD, rank, numIterations, regularizationParameter)


// COMMAND ----------

// Choose any random user,which is going to be our test user
val newUserID = 1000

// Create a list with the MovieIds we want to predict its rating to
val newUser = Array(Rating(newUserID, 1, 0),Rating(newUserID, 2, 0),Rating(newUserID.toInt, 3, 0),Rating(newUserID.toInt, 4, 0),Rating(newUserID.toInt, 5, 0))
newUser.map(println)

// Convert it to an RDD
val newTest = sc.parallelize(newUser)
newTest.map(println)

// COMMAND ----------

  // Evaluate the model on this test user
  val usersProductsTest = newTest.map { case Rating(user, product, rate) =>
                                              (user, product)
  }


// COMMAND ----------

  // get the predictions for this test user
  val predictions = model.predict(usersProductsTest)
                         .map { case Rating(user, product, rate)
                                     => ((user, product), rate)
    }

  val ratesAndPreds = newTest.map { case Rating(user, product, rate) 
                                     => ((user, product), rate)
                                   }.join(predictions)



// COMMAND ----------

// Convert the RDD with the predictions to a DataFrame
val preds2 = ratesAndPreds.map { case ((user, product), (r1, r2)) => (user,product,r2) }

var predsDF = preds2.toDF("userID","movieID","pred")


predsDF.orderBy(asc("movieID"))show()

// COMMAND ----------

// Order the movies according to the predictions
val orderedPreds = predsDF.orderBy(desc("pred"))
orderedPreds.show()

// COMMAND ----------

// Return the ID of the highest recommended one

val t = orderedPreds.select("movieID").collect().map(_(0)).toList.take(1)
println("The movie highest recommended for this user is:")
println(movies(t(0).asInstanceOf[Int]))


// COMMAND ----------

// MAGIC %md
// MAGIC ## For first time users, the program gives the top rated movies over all users.
// MAGIC 
// MAGIC If newUser:
// MAGIC - Check the ratings matrix
// MAGIC - Compute the average rating of each column (of each movie)
// MAGIC - Return the columns with the highest

// COMMAND ----------

// Note: This is only for the ones they said. Doesnt include the ones computed by our model...
import org.apache.spark.sql.functions._

val newUserID = 4000

// Compute the average of each movie
val averageRates = ratingsDF.groupBy("movieID").avg("rating")
averageRates.show()

// COMMAND ----------

// Order the movies by top ratings
val orderedRates = averageRates.orderBy(desc("avg(rating)")).withColumnRenamed("avg(rating)","avg_rate")
orderedRates.show()

// COMMAND ----------

// Return the top 5 movies with highest ratings over all users

val topMovies = orderedRates.take(5)
//println(topMovies)
//topMovies.foreach(t => println(t(0)))
val moviesList = orderedRates.select("movieID").collect().map(_(0)).toList.take(5)
//println(moviesList)

println("The movies recommended for a new user based on the overall rating are:")
for (t <-  moviesList )
    println(movies(t.asInstanceOf[Int]))
 // println(movies(t))


// COMMAND ----------

// In alternative, return the top movies with rating of 5 over all users

val topMovies5 = orderedRates.where("avg_rate == 5").select("movieID").collect().map(_(0)).toList

println("The movies recommended for a new user based on the overall rating are:")
for (t <-  topMovies5 )
    println(movies(t.asInstanceOf[Int]))
 // println(movies(t))



// COMMAND ----------

// MAGIC %md
// MAGIC ## Genres analysis
// MAGIC 
// MAGIC ###### we investigate whether suggestion based on genre can be more accurate. Imagine a scenario in which an user is interested in watching a movie of a particular genre, say an Animation movie, given this information, can we suggest a better film with respect to the film that we would have suggested by only knowing user’s previous ratings on such movie?

// COMMAND ----------

// Read the movies file as a dataframe and display it
val movies_df = sc.textFile("/databricks-datasets/cs100/lab4/data-001/movies.dat").map { line =>
      val fields = line.split("::")
      // format: (movieId, movieName,genre)
      (fields(0).toInt, fields(1),fields(2).split("\\|"))
    }.toDF("movieId", "movieName", "genre")

display(movies_df)

// COMMAND ----------

// Select a GENRE, or a set of GENREs and filter the movies dataset according to this genre
val GENRE = "Animation"

def array_contains_any(s:Seq[String]): UserDefinedFunction = {
udf((c: WrappedArray[String]) =>
  c.toList.intersect(s).nonEmpty)}

val b: Array[String] = Array(GENRE)
val genre_df = movies_df.where(array_contains_any(b)($"genre"))
display(genre_df)

val movie_ID_genres = genre_df.select("movieId").rdd.map(r => r(0)).collect()

// COMMAND ----------

// We now read and display the ratings dataframe (without the timestamp field) as a dataframe.
val RatingsDF = sc.textFile("/databricks-datasets/cs100/lab4/data-001/ratings.dat.gz").map { line =>
      val fields = line.split("::")
      // format: (timestamp % 10, Rating(userId, movieId, rating))
      (fields(0).toInt, fields(1).toInt, fields(2).toDouble)
    }.toDF("userId", "movieId", "rating")
display(RatingsDF)

// COMMAND ----------

// Based on the movies id obtained by the filtering on the movie dataset we filter the ratings df and we convert it to rdd format
val Ratings_genre_df = RatingsDF.filter($"movieId".isin(movie_ID_genres:_*))
val genre_rdd = Ratings_genre_df.rdd
display(Ratings_genre_df)

// COMMAND ----------

// Print some dataset statistics
val numRatings = genre_rdd.count
println("Got " + numRatings + " ratings")

// COMMAND ----------

// Create train, test, and evaluation dataset and print some statistics 
val Array(temp_trainingRDD, temp_validationRDD, temp_testRDD) = genre_rdd.randomSplit(Array(0.60, 0.20, 0.20), 0L)

// let's find the exact sizes we have next
println("training data size = " + temp_trainingRDD.count() +
        ", validation data size = " + temp_validationRDD.count() +
        ", test data size = " + temp_testRDD.count() + ".")

// COMMAND ----------

// Map the rdds to the Rating type
val maptrainingRDD = temp_trainingRDD.map(x=>Rating(x(0).asInstanceOf[Int], x(1).asInstanceOf[Int], x(2).asInstanceOf[Double]))
val mapvalidationRDD = temp_validationRDD.map(x=>Rating(x(0).asInstanceOf[Int], x(1).asInstanceOf[Int], x(2).asInstanceOf[Double]))
val maptestRDD = temp_testRDD.map(x=>Rating(x(0).asInstanceOf[Int], x(1).asInstanceOf[Int], x(2).asInstanceOf[Double]))

// COMMAND ----------

// Build the recommendation model using ALS by fitting to the training data (with Hyperparameter tuning)
// trying different hyper-parameter (rank) values to optimise over
val ranks = List(2, 4, 8, 16, 32, 64, 128, 256); 
var rank=0;

for ( rank <- ranks ){
  // using a fixed numIterations=10 and regularisation=0.01
  val numIterations = 10
  val regularizationParameter = 0.01
  val model = ALS.train(maptrainingRDD, rank, numIterations, regularizationParameter)

  // Evaluate the model on test data
  val usersProductsValidate = mapvalidationRDD.map { case Rating(user, product, rate) =>
                                              (user, product)
  }

  // get the predictions on test data
  val predictions = model.predict(usersProductsValidate)
                         .map { case Rating(user, product, rate)
                                     => ((user, product), rate)
    }

  // find the actual ratings and join with predictions
  val ratesAndPreds = mapvalidationRDD.map { case Rating(user, product, rate) 
                                     => ((user, product), rate)
                                   }.join(predictions)
  

  val MSE = ratesAndPreds.map { case ((user, product), (r1, r2)) =>
    val err = (r1 - r2)
    err * err
  }.mean()
  println("rank and Mean Squared Error = " +  rank + " and " + MSE)
} // end of loop over ranks