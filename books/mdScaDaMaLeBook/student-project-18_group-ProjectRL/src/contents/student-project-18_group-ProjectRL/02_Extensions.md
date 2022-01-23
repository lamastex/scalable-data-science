<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Extensions
==========

In this notebook, we introduce multiple improvements to the original algorithm, using the small dataset. We: - First, improve the original algorithm by creating a system that takes user info and outputs suggestions, which is the typical final role of a recommendation system. - Then, we add the functionality that for first time user, we output the top rated movies over all users. - Furthemore, we improve the existing model by including the movie's genres to give better recommendations.

</div>

<div class="cell markdown">

Preliminaries
-------------

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// import the relevant libraries for `mllib`

import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark.sql.expressions.UserDefinedFunction
import scala.collection.mutable.WrappedArray
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.mllib.recommendation.ALS
    import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
    import org.apache.spark.mllib.recommendation.Rating
    import org.apache.spark.sql.expressions.UserDefinedFunction
    import scala.collection.mutable.WrappedArray

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Get the small dataset and display information
display(dbutils.fs.ls("/databricks-datasets/cs100/lab4/data-001/")) // The data is already here
```

<div class="output execute_result tabular_result" execution_count="1">

<table>
<thead>
<tr class="header">
<th>path</th>
<th>name</th>
<th>size</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>dbfs:/databricks-datasets/cs100/lab4/data-001/movies.dat</td>
<td>movies.dat</td>
<td>171308.0</td>
</tr>
<tr class="even">
<td>dbfs:/databricks-datasets/cs100/lab4/data-001/ratings.dat.gz</td>
<td>ratings.dat.gz</td>
<td>2837683.0</td>
</tr>
</tbody>
</table>

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Create the RDD containing the ratings

val ratingsRDD = sc.textFile("/databricks-datasets/cs100/lab4/data-001/ratings.dat.gz").map { line =>
      val fields = line.split("::")
      // format: Rating(userId, movieId, rating)
      Rating(fields(0).toInt, fields(1).toInt, fields(2).toDouble)
}
```

<div class="output execute_result plain_result" execution_count="1">

    ratingsRDD: org.apache.spark.rdd.RDD[org.apache.spark.mllib.recommendation.Rating] = MapPartitionsRDD[21729] at map at command-3389902380791579:3

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// We take a look at the first 10 entries in the Ratings RDD
ratingsRDD.take(10).map(println)
```

<div class="output execute_result plain_result" execution_count="1">

    Rating(1,1193,5.0)
    Rating(1,661,3.0)
    Rating(1,914,3.0)
    Rating(1,3408,4.0)
    Rating(1,2355,5.0)
    Rating(1,1197,3.0)
    Rating(1,1287,5.0)
    Rating(1,2804,5.0)
    Rating(1,594,4.0)
    Rating(1,919,4.0)
    res86: Array[Unit] = Array((), (), (), (), (), (), (), (), (), ())

</div>

</div>

<div class="cell markdown">

A similar command is used to format the movies. For this first part the genre field is ignored. They will considered in the second part of this notebook.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val movies = sc.textFile("/databricks-datasets/cs100/lab4/data-001/movies.dat").map { line =>
      val fields = line.split("::")
      // format: (movieId, movieName)
      (fields(0).toInt, fields(1))
    }.collect.toMap
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// check the size of the small dataset

val numRatings = ratingsRDD.count
val numUsers = ratingsRDD.map(_.user).distinct.count
val numMovies = ratingsRDD.map(_.product).distinct.count

println("Got " + numRatings + " ratings from "
        + numUsers + " users on " + numMovies + " movies.")
```

<div class="output execute_result plain_result" execution_count="1">

    Got 487650 ratings from 2999 users on 3615 movies.
    numRatings: Long = 487650
    numUsers: Long = 2999
    numMovies: Long = 3615

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Creating a Training Set, test Set and Validation Set

val Array(trainingRDD, validationRDD, testRDD) = ratingsRDD.randomSplit(Array(0.60, 0.20, 0.20), 0L)
```

<div class="output execute_result plain_result" execution_count="1">

    trainingRDD: org.apache.spark.rdd.RDD[org.apache.spark.mllib.recommendation.Rating] = MapPartitionsRDD[21741] at randomSplit at command-3389902380791584:3
    validationRDD: org.apache.spark.rdd.RDD[org.apache.spark.mllib.recommendation.Rating] = MapPartitionsRDD[21742] at randomSplit at command-3389902380791584:3
    testRDD: org.apache.spark.rdd.RDD[org.apache.spark.mllib.recommendation.Rating] = MapPartitionsRDD[21743] at randomSplit at command-3389902380791584:3

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// let's find the exact sizes we have next
println(" training data size = " + trainingRDD.count() +
        ", validation data size = " + validationRDD.count() +
        ", test data size = " + testRDD.count() + ".")
```

<div class="output execute_result plain_result" execution_count="1">

     training data size = 292318, validation data size = 97175, test data size = 98157.

</div>

</div>

<div class="cell markdown">

Ratings distribution
--------------------

For curiosity, we start by plotting the histogram of the ratings present in this dataset

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Create a DataFrame with the data
import org.apache.spark.sql.functions._

val ratingsDF = sc.textFile("/databricks-datasets/cs100/lab4/data-001/ratings.dat.gz").map { line =>
      val fields = line.split("::")
      // format: (timestamp % 10, Rating(userId, movieId, rating))
      (fields(0).toInt, fields(1).toInt, fields(2).toDouble)
    }.toDF("userID", "movieID", "rating")
display(ratingsDF)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val history = ratingsDF.groupBy("rating").count().orderBy(asc("rating"))
history.show()
```

<div class="output execute_result plain_result" execution_count="1">

    +------+------+
    |rating| count|
    +------+------+
    |   1.0| 27472|
    |   2.0| 53838|
    |   3.0|127216|
    |   4.0|170579|
    |   5.0|108545|
    +------+------+

    history: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [rating: double, count: bigint]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(history)
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/18_02_1.JPG?raw=true)

</div>

<div class="cell markdown">

Create a system that takes user info and outputs suggestions.
-------------------------------------------------------------

user info = ((movieID,rating),(movieID,rating)). It is basically an (incomplete) line in the ratings matrix.

-   Choose an user
-   Run the model and fill the columns - predict the ratings for the movies
-   Output the ones with the best predicted score

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Train the model as usually
  val rank = 4
  val numIterations = 10
  val regularizationParameter = 0.01
  val model = ALS.train(trainingRDD, rank, numIterations, regularizationParameter)
```

<div class="output execute_result plain_result" execution_count="1">

    rank: Int = 4
    numIterations: Int = 10
    regularizationParameter: Double = 0.01
    model: org.apache.spark.mllib.recommendation.MatrixFactorizationModel = org.apache.spark.mllib.recommendation.MatrixFactorizationModel@570cf4b3

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Choose any random user,which is going to be our test user
val newUserID = 1000

// Create a list with the MovieIds we want to predict its rating to
val newUser = Array(Rating(newUserID, 1, 0),Rating(newUserID, 2, 0),Rating(newUserID.toInt, 3, 0),Rating(newUserID.toInt, 4, 0),Rating(newUserID.toInt, 5, 0))
newUser.map(println)

// Convert it to an RDD
val newTest = sc.parallelize(newUser)
newTest.map(println)
```

<div class="output execute_result plain_result" execution_count="1">

    Rating(1000,1,0.0)
    Rating(1000,2,0.0)
    Rating(1000,3,0.0)
    Rating(1000,4,0.0)
    Rating(1000,5,0.0)
    newUserID: Int = 1000
    newUser: Array[org.apache.spark.mllib.recommendation.Rating] = Array(Rating(1000,1,0.0), Rating(1000,2,0.0), Rating(1000,3,0.0), Rating(1000,4,0.0), Rating(1000,5,0.0))
    newTest: org.apache.spark.rdd.RDD[org.apache.spark.mllib.recommendation.Rating] = ParallelCollectionRDD[21968] at parallelize at command-3389902380791591:9
    res94: org.apache.spark.rdd.RDD[Unit] = MapPartitionsRDD[21969] at map at command-3389902380791591:10

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
  // Evaluate the model on this test user
  val usersProductsTest = newTest.map { case Rating(user, product, rate) =>
                                              (user, product)
  }
```

<div class="output execute_result plain_result" execution_count="1">

    usersProductsTest: org.apache.spark.rdd.RDD[(Int, Int)] = MapPartitionsRDD[21970] at map at command-3389902380791592:2

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
  // get the predictions for this test user
  val predictions = model.predict(usersProductsTest)
                         .map { case Rating(user, product, rate)
                                     => ((user, product), rate)
    }

  val ratesAndPreds = newTest.map { case Rating(user, product, rate) 
                                     => ((user, product), rate)
                                   }.join(predictions)

```

<div class="output execute_result plain_result" execution_count="1">

    predictions: org.apache.spark.rdd.RDD[((Int, Int), Double)] = MapPartitionsRDD[21979] at map at command-3389902380791593:3
    ratesAndPreds: org.apache.spark.rdd.RDD[((Int, Int), (Double, Double))] = MapPartitionsRDD[21983] at join at command-3389902380791593:9

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Convert the RDD with the predictions to a DataFrame
val preds2 = ratesAndPreds.map { case ((user, product), (r1, r2)) => (user,product,r2) }

var predsDF = preds2.toDF("userID","movieID","pred")


predsDF.orderBy(asc("movieID"))show()
```

<div class="output execute_result plain_result" execution_count="1">

    +------+-------+------------------+
    |userID|movieID|              pred|
    +------+-------+------------------+
    |  1000|      1|4.3134486083287396|
    |  1000|      2| 3.561695001470941|
    |  1000|      3| 3.251747295342854|
    |  1000|      4|2.9727526635707116|
    |  1000|      5|3.1890542732727987|
    +------+-------+------------------+

    preds2: org.apache.spark.rdd.RDD[(Int, Int, Double)] = MapPartitionsRDD[21984] at map at command-3389902380791594:2
    predsDF: org.apache.spark.sql.DataFrame = [userID: int, movieID: int ... 1 more field]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Order the movies according to the predictions
val orderedPreds = predsDF.orderBy(desc("pred"))
orderedPreds.show()
```

<div class="output execute_result plain_result" execution_count="1">

    +------+-------+------------------+
    |userID|movieID|              pred|
    +------+-------+------------------+
    |  1000|      1|4.3134486083287396|
    |  1000|      2| 3.561695001470941|
    |  1000|      3| 3.251747295342854|
    |  1000|      5|3.1890542732727987|
    |  1000|      4|2.9727526635707116|
    +------+-------+------------------+

    orderedPreds: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [userID: int, movieID: int ... 1 more field]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Return the ID of the highest recommended one

val t = orderedPreds.select("movieID").collect().map(_(0)).toList.take(1)
println("The movie highest recommended for this user is:")
println(movies(t(0).asInstanceOf[Int]))
```

<div class="output execute_result plain_result" execution_count="1">

    The movie highest recommended for this user is:
    Toy Story (1995)
    t: List[Any] = List(1)

</div>

</div>

<div class="cell markdown">

For first time users, the program gives the top rated movies over all users.
----------------------------------------------------------------------------

If newUser: - Check the ratings matrix - Compute the average rating of each column (of each movie) - Return the columns with the highest

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Note: This is only for the ones they said. Doesnt include the ones computed by our model...
import org.apache.spark.sql.functions._

val newUserID = 4000

// Compute the average of each movie
val averageRates = ratingsDF.groupBy("movieID").avg("rating")
averageRates.show()
```

<div class="output execute_result plain_result" execution_count="1">

    +-------+------------------+
    |movieID|       avg(rating)|
    +-------+------------------+
    |   1580|3.7045454545454546|
    |   2366|           3.71875|
    |   1088|  3.29595015576324|
    |   1959|3.6577181208053693|
    |   3175|3.8145454545454545|
    |   1645| 3.367021276595745|
    |    496|3.3846153846153846|
    |   2142|2.8256880733944953|
    |   1591|2.5783132530120483|
    |   2122|2.3434343434343434|
    |    833| 2.130434782608696|
    |    463|2.7222222222222223|
    |    471| 3.665492957746479|
    |   1342|2.8188976377952755|
    |    148| 2.857142857142857|
    |   3918| 2.806896551724138|
    |   3794|               3.4|
    |   1238|3.9526627218934913|
    |   2866|3.7386363636363638|
    |   3749|               4.0|
    +-------+------------------+
    only showing top 20 rows

    import org.apache.spark.sql.functions._
    newUserID: Int = 4000
    averageRates: org.apache.spark.sql.DataFrame = [movieID: int, avg(rating): double]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Order the movies by top ratings
val orderedRates = averageRates.orderBy(desc("avg(rating)")).withColumnRenamed("avg(rating)","avg_rate")
orderedRates.show()
```

<div class="output execute_result plain_result" execution_count="1">

    +-------+-----------------+
    |movieID|         avg_rate|
    +-------+-----------------+
    |    854|              5.0|
    |    853|              5.0|
    |    787|              5.0|
    |   1830|              5.0|
    |   3881|              5.0|
    |    557|              5.0|
    |   3280|              5.0|
    |    578|              5.0|
    |   2444|              5.0|
    |   3636|              5.0|
    |   3443|              5.0|
    |   3800|              5.0|
    |    989|              5.0|
    |   1002|4.666666666666667|
    |   3232|4.666666666666667|
    |   2839|4.666666666666667|
    |   3245|4.666666666666667|
    |   2905|4.609756097560975|
    |   1743|              4.6|
    |   2019|4.586330935251799|
    +-------+-----------------+
    only showing top 20 rows

    orderedRates: org.apache.spark.sql.DataFrame = [movieID: int, avg_rate: double]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
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
```

<div class="output execute_result plain_result" execution_count="1">

    The movies recommended for a new user based on the overall rating are:
    Dingo (1992)
    Gate of Heavenly Peace, The (1995)
    Hour of the Pig, The (1993)
    Those Who Love Me Can Take the Train (Ceux qui m'aiment prendront le train) (1998)
    Schlafes Bruder (Brother of Sleep) (1995)
    topMovies: Array[org.apache.spark.sql.Row] = Array([989,5.0], [787,5.0], [853,5.0], [578,5.0], [3636,5.0])
    moviesList: List[Any] = List(853, 787, 578, 3636, 989)

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// In alternative, return the top movies with rating of 5 over all users

val topMovies5 = orderedRates.where("avg_rate == 5").select("movieID").collect().map(_(0)).toList

println("The movies recommended for a new user based on the overall rating are:")
for (t <-  topMovies5 )
    println(movies(t.asInstanceOf[Int]))
 // println(movies(t))

```

<div class="output execute_result plain_result" execution_count="1">

    The movies recommended for a new user based on the overall rating are:
    Dingo (1992)
    Gate of Heavenly Peace, The (1995)
    Hour of the Pig, The (1993)
    Those Who Love Me Can Take the Train (Ceux qui m'aiment prendront le train) (1998)
    Schlafes Bruder (Brother of Sleep) (1995)
    Ballad of Narayama, The (Narayama Bushiko) (1958)
    Baby, The (1973)
    24 7: Twenty Four Seven (1997)
    Born American (1986)
    Criminal Lovers (Les Amants Criminels) (1999)
    Follow the Bitch (1998)
    Bittersweet Motel (2000)
    Mamma Roma (1962)
    topMovies5: List[Any] = List(853, 787, 578, 3636, 989, 854, 3280, 2444, 3443, 3800, 1830, 3881, 557)

</div>

</div>

<div class="cell markdown">

Genres analysis
---------------

###### we investigate whether suggestion based on genre can be more accurate. Imagine a scenario in which an user is interested in watching a movie of a particular genre, say an Animation movie, given this information, can we suggest a better film with respect to the film that we would have suggested by only knowing user’s previous ratings on such movie?

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Read the movies file as a dataframe and display it
val movies_df = sc.textFile("/databricks-datasets/cs100/lab4/data-001/movies.dat").map { line =>
      val fields = line.split("::")
      // format: (movieId, movieName,genre)
      (fields(0).toInt, fields(1),fields(2).split("\\|"))
    }.toDF("movieId", "movieName", "genre")

display(movies_df)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Select a GENRE, or a set of GENREs and filter the movies dataset according to this genre
val GENRE = "Animation"

def array_contains_any(s:Seq[String]): UserDefinedFunction = {
udf((c: WrappedArray[String]) =>
  c.toList.intersect(s).nonEmpty)}

val b: Array[String] = Array(GENRE)
val genre_df = movies_df.where(array_contains_any(b)($"genre"))
display(genre_df)

val movie_ID_genres = genre_df.select("movieId").rdd.map(r => r(0)).collect()
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// We now read and display the ratings dataframe (without the timestamp field) as a dataframe.
val RatingsDF = sc.textFile("/databricks-datasets/cs100/lab4/data-001/ratings.dat.gz").map { line =>
      val fields = line.split("::")
      // format: (timestamp % 10, Rating(userId, movieId, rating))
      (fields(0).toInt, fields(1).toInt, fields(2).toDouble)
    }.toDF("userId", "movieId", "rating")
display(RatingsDF)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Based on the movies id obtained by the filtering on the movie dataset we filter the ratings df and we convert it to rdd format
val Ratings_genre_df = RatingsDF.filter($"movieId".isin(movie_ID_genres:_*))
val genre_rdd = Ratings_genre_df.rdd
display(Ratings_genre_df)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Print some dataset statistics
val numRatings = genre_rdd.count
println("Got " + numRatings + " ratings")
```

<div class="output execute_result plain_result" execution_count="1">

    Got 22080 ratings
    numRatings: Long = 22080

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Create train, test, and evaluation dataset and print some statistics 
val Array(temp_trainingRDD, temp_validationRDD, temp_testRDD) = genre_rdd.randomSplit(Array(0.60, 0.20, 0.20), 0L)

// let's find the exact sizes we have next
println("training data size = " + temp_trainingRDD.count() +
        ", validation data size = " + temp_validationRDD.count() +
        ", test data size = " + temp_testRDD.count() + ".")
```

<div class="output execute_result plain_result" execution_count="1">

    training data size = 13229, validation data size = 4411, test data size = 4440.
    temp_trainingRDD: org.apache.spark.rdd.RDD[org.apache.spark.sql.Row] = MapPartitionsRDD[19929] at randomSplit at command-3389902380791621:2
    temp_validationRDD: org.apache.spark.rdd.RDD[org.apache.spark.sql.Row] = MapPartitionsRDD[19930] at randomSplit at command-3389902380791621:2
    temp_testRDD: org.apache.spark.rdd.RDD[org.apache.spark.sql.Row] = MapPartitionsRDD[19931] at randomSplit at command-3389902380791621:2

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Map the rdds to the Rating type
val maptrainingRDD = temp_trainingRDD.map(x=>Rating(x(0).asInstanceOf[Int], x(1).asInstanceOf[Int], x(2).asInstanceOf[Double]))
val mapvalidationRDD = temp_validationRDD.map(x=>Rating(x(0).asInstanceOf[Int], x(1).asInstanceOf[Int], x(2).asInstanceOf[Double]))
val maptestRDD = temp_testRDD.map(x=>Rating(x(0).asInstanceOf[Int], x(1).asInstanceOf[Int], x(2).asInstanceOf[Double]))
```

<div class="output execute_result plain_result" execution_count="1">

    maptrainingRDD: org.apache.spark.rdd.RDD[org.apache.spark.mllib.recommendation.Rating] = MapPartitionsRDD[19932] at map at command-3389902380791624:2
    mapvalidationRDD: org.apache.spark.rdd.RDD[org.apache.spark.mllib.recommendation.Rating] = MapPartitionsRDD[19933] at map at command-3389902380791624:3
    maptestRDD: org.apache.spark.rdd.RDD[org.apache.spark.mllib.recommendation.Rating] = MapPartitionsRDD[19934] at map at command-3389902380791624:4

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
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
```

<div class="output execute_result plain_result" execution_count="1">

    rank and Mean Squared Error = 2 and 1.0624116959469154
    rank and Mean Squared Error = 4 and 1.3393495403657538
    rank and Mean Squared Error = 8 and 1.6916511125697133
    rank and Mean Squared Error = 16 and 1.63542207107039
    rank and Mean Squared Error = 32 and 1.311227268934932
    rank and Mean Squared Error = 64 and 0.9461947532838
    rank and Mean Squared Error = 128 and 0.8859420827613572
    rank and Mean Squared Error = 256 and 0.8845268169033572
    numIterations: Int = 10
    regularisation: Double = 0.01
    ranks: List[Int] = List(2, 4, 8, 16, 32, 64, 128, 256)
    rank: Int = 0

</div>

</div>
