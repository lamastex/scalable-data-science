<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

[The Alternating Least Squares method (ALS)]()
==============================================

</div>

<div class="cell markdown">

The ALS algorithm
-----------------

The ALS algorithm was proposed in 2008 by *F. Zhang, E.Shang, Y. Xu and X. Wu* in a paper titled : **Large-scale Parallel Colaborative Filtering for the Netflix Prize** [(paper)](https://link.springer.com/chapter/10.1007/978-3-540-68880-8_32). We will briefly describe the main ideas behind the ALS algorithm.

#### What are we learning ?

In order to finding the missing values of the rating matrix R, the authors of the ALS algorithm considered approximating this matrix by a product of two tall matrices U and M of low rank. In other words, the goal is to find a low rank approximation of the ratings matrix R:

\\[
R \approx U M^\top = \begin{bmatrix} u_1 & \dots & u_N \end{bmatrix}^\top  \begin{bmatrix}
m_1 & \dots & m_M \end{bmatrix} \qquad \text{where} \qquad U \in \mathbb{R}^{N \times K}, M \in \mathbb{R}^{M \times K}
\\]

Intuitively we think of U (resp. M) as a matrix of users' features (resp. movies features) and we may rewrite this approximation entrywise as

\\[
\forall i,j \qquad r_{i,j} \approx u_i^\top m_j. 
\\]

#### The loss function

If all entries of the rating matrix R were known, one may use an SVD decomposition to reconstruct U and M. However, not all ratings are known therefore one has to learn the matrices U and M. The authors of the paper proposed to minimize the following loss which corresponds to the sum of squares errors with a Thikonov rigularization that weighs the users matrix U (resp. the movies matrix M) using the Gamma*U (resp. Gamma*M)

\\[
\mathcal{L}_{U,M}^{wheighted}(R) = \sum_{(i,j)\in S}
(r_{i,j} - u_i^\top m_j)^2 + \lambda \Vert M \Gamma_m \Vert^2 + \lambda \Vert U \Gamma_u \Vert^2 
\\]

where S corresponds to the set of known ratings, \\lambda is a regularaziation parameter. In fact this loss corresponds to the Alternating Least Squares with Weigted Regularization (ALS-WR). We will be using a variant of that algorithm a.k.a. the ALS algorithm which corresponds to minimizing the following slighltly similar loss without wheighing:

\\[
\mathcal{L}_{U,M}(R) = \sum_{(i,j)\in S}
(r_{i,j} - u_i^\top m_j)^2 + \lambda \Vert M \Vert^2 + \lambda \Vert U \Vert^2 
\\]

and the goal of the algorithm will be find a condidate (U,M) that

\\[
\min_{U,M} \mathcal{L}_{U,M}(R)
\\]

#### The ALS algorithm

The authors approach to solve the aforementioned minimization problem as follows: - **Step 1.** Initialize matrix M, by assigning the average rating for that movie as the first row and small random numbers for the remaining entries. - **Step 2.** Fix M, Solve for U by minimizing the aformentioned loss. - **Step 3.** Fix U, solve for M by minimizing the aformentioned loss similarly. - **Step 4.** Repeat Steps 2 and 3 until a stopping criterion is satisfied.

Note that when one of the matrices is fixed, say M, the loss becomes quadratic in U and the solution corresponds to that of the least squares.

#### Key parameters of the algorithm

The key parameters of the lagorithm are the **rank K**, the **regularization parameter lambda**, and the **number of iterations** befor stopping the algorithm. Indeed, since we don not have full knowledge of the matrix R, we do not know its rank. To find the best rank we will use cross-validation and dedicate part of the data to that. There is no straight way to choosing the regularization parameter, we will base our choice on reported values that work for the considered datasets. As for the number of iterations, we will proceed similarly.

#### Practically speaking

We will use the following mllib library in scala wich contain classes dedicated to recommendation systems (See <http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.recommendation.ALS>). More specifically, it contains the ALS class which allows for using the ALS algorithm as described earlier.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.mllib.recommendation.Rating
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.mllib.recommendation.ALS
    import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
    import org.apache.spark.mllib.recommendation.Rating

</div>

</div>

<div class="cell markdown">

[On a small dataset]()
======================

This part of the notebook is borrowed from the notebook on the ALS we had in the course.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
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

<div class="cell markdown">

### Loading the data

We read in each of the files and create an RDD consisting of parsed lines. Each line in the ratings dataset (`ratings.dat.gz`) is formatted as: `UserID::MovieID::Rating::Timestamp` Each line in the movies (`movies.dat`) dataset is formatted as: `MovieID::Title::Genres` The `Genres` field has the format `Genres1|Genres2|Genres3|...` The format of these files is uniform and simple, so we can use `split()`.

Parsing the two files yields two RDDs

-   For each line in the ratings dataset, we create a tuple of (UserID, MovieID, Rating). We drop the timestamp because we do not need it for this exercise.
-   For each line in the movies dataset, we create a tuple of (MovieID, Title). We drop the Genres because we do not need them for this exercise.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// take a peek at what's in the rating file
sc.textFile("/databricks-datasets/cs100/lab4/data-001/ratings.dat.gz").map { line => line.split("::") }.take(5)
```

<div class="output execute_result plain_result" execution_count="1">

    res33: Array[Array[String]] = Array(Array(1, 1193, 5, 978300760), Array(1, 661, 3, 978302109), Array(1, 914, 3, 978301968), Array(1, 3408, 4, 978300275), Array(1, 2355, 5, 978824291))

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val timedRatingsRDD = sc.textFile("/databricks-datasets/cs100/lab4/data-001/ratings.dat.gz").map { line =>
      val fields = line.split("::")
      // format: (timestamp % 10, Rating(userId, movieId, rating))
      (fields(3).toLong % 10, Rating(fields(0).toInt, fields(1).toInt, fields(2).toDouble))
    }

timedRatingsRDD.take(10).map(println)
```

<div class="output execute_result plain_result" execution_count="1">

    (0,Rating(1,1193,5.0))
    (9,Rating(1,661,3.0))
    (8,Rating(1,914,3.0))
    (5,Rating(1,3408,4.0))
    (1,Rating(1,2355,5.0))
    (8,Rating(1,1197,3.0))
    (9,Rating(1,1287,5.0))
    (9,Rating(1,2804,5.0))
    (8,Rating(1,594,4.0))
    (8,Rating(1,919,4.0))
    timedRatingsRDD: org.apache.spark.rdd.RDD[(Long, org.apache.spark.mllib.recommendation.Rating)] = MapPartitionsRDD[9561] at map at command-3389902380791711:1
    res34: Array[Unit] = Array((), (), (), (), (), (), (), (), (), ())

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val ratingsRDD = sc.textFile("/databricks-datasets/cs100/lab4/data-001/ratings.dat.gz").map { line =>
      val fields = line.split("::")
      // format: Rating(userId, movieId, rating)
      Rating(fields(0).toInt, fields(1).toInt, fields(2).toDouble)
    }

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
    ratingsRDD: org.apache.spark.rdd.RDD[org.apache.spark.mllib.recommendation.Rating] = MapPartitionsRDD[9564] at map at command-3389902380791714:1
    res35: Array[Unit] = Array((), (), (), (), (), (), (), (), (), ())

</div>

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

<div class="cell markdown">

Let's make a data frame to visually explore the data next.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
sc.textFile("/databricks-datasets/cs100/lab4/data-001/ratings.dat.gz").map { line => line.split("::") }.take(5)
```

<div class="output execute_result plain_result" execution_count="1">

    res36: Array[Array[String]] = Array(Array(1, 1193, 5, 978300760), Array(1, 661, 3, 978302109), Array(1, 914, 3, 978301968), Array(1, 3408, 4, 978300275), Array(1, 2355, 5, 978824291))

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val timedRatingsDF = sc.textFile("/databricks-datasets/cs100/lab4/data-001/ratings.dat.gz").map { line =>
      val fields = line.split("::")
      // format: (timestamp % 10, Rating(userId, movieId, rating))
      (fields(3).toLong, fields(0).toInt, fields(1).toInt, fields(2).toDouble)
    }.toDF("timestamp", "userId", "movieId", "rating")


display(timedRatingsDF)
```

</div>

<div class="cell markdown">

Here we simply check the size of the datasets we are using

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
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

<div class="cell markdown">

Now that we have the dataset we need, let's make a recommender system.

**Creating a Training Set, test Set and Validation Set**

Before we jump into using machine learning, we need to break up the `ratingsRDD` dataset into three pieces:

-   A training set (RDD), which we will use to train models
-   A validation set (RDD), which we will use to choose the best model
-   A test set (RDD), which we will use for our experiments

To randomly split the dataset into the multiple groups, we can use the `randomSplit()` transformation. `randomSplit()` takes a set of splits and seed and returns multiple RDDs.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val Array(trainingRDD, validationRDD, testRDD) = ratingsRDD.randomSplit(Array(0.60, 0.20, 0.20), 0L)
// let's find the exact sizes we have next
println(" training data size = " + trainingRDD.count() +
        ", validation data size = " + validationRDD.count() +
        ", test data size = " + testRDD.count() + ".")
```

<div class="output execute_result plain_result" execution_count="1">

     training data size = 292318, validation data size = 97175, test data size = 98157.
    trainingRDD: org.apache.spark.rdd.RDD[org.apache.spark.mllib.recommendation.Rating] = MapPartitionsRDD[9584] at randomSplit at command-3389902380791722:1
    validationRDD: org.apache.spark.rdd.RDD[org.apache.spark.mllib.recommendation.Rating] = MapPartitionsRDD[9585] at randomSplit at command-3389902380791722:1
    testRDD: org.apache.spark.rdd.RDD[org.apache.spark.mllib.recommendation.Rating] = MapPartitionsRDD[9586] at randomSplit at command-3389902380791722:1

</div>

</div>

<div class="cell markdown">

After splitting the dataset, your training set has about 293,000 entries and the validation and test sets each have about 97,000 entries (the exact number of entries in each dataset varies slightly due to the random nature of the `randomSplit()` transformation.

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

Training the recommender system
-------------------------------

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
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
```

<div class="output execute_result plain_result" execution_count="1">

    rank and Mean Squared Error = 4 and 0.8479974514693542
    rank and Mean Squared Error = 8 and 0.9300503484148622
    rank and Mean Squared Error = 12 and 1.02609274473932
    ranks: List[Int] = List(4, 8, 12)
    rank: Int = 0

</div>

</div>

<div class="cell markdown">

Here we have the best model

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
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
```

<div class="output execute_result plain_result" execution_count="1">

    rank and Mean Squared Error for test data = 4 and 0.8339905882351633
    rank: Int = 4
    numIterations: Int = 10
    regularizationParameter: Double = 0.01
    model: org.apache.spark.mllib.recommendation.MatrixFactorizationModel = org.apache.spark.mllib.recommendation.MatrixFactorizationModel@4861c837
    usersProductsTest: org.apache.spark.rdd.RDD[(Int, Int)] = MapPartitionsRDD[10463] at map at command-3389902380791728:7
    predictions: org.apache.spark.rdd.RDD[((Int, Int), Double)] = MapPartitionsRDD[10472] at map at command-3389902380791728:13
    ratesAndPreds: org.apache.spark.rdd.RDD[((Int, Int), (Double, Double))] = MapPartitionsRDD[10476] at join at command-3389902380791728:20
    MSE: Double = 0.8339905882351633

</div>

</div>

<div class="cell markdown">

[On a large dataset - Netflix dataset]()
========================================

Loading the data
----------------

Netflix held a competition to improve recommendation systems. The dataset can be found in [kaggle](https://www.kaggle.com/netflix-inc/netflix-prize-data). Briefly speaking, the dataset contains users' ratings to movies, with 480189 users and 17770 movies. Ratings are given on an integral scale from 1 to 5. The first step is to download the data and store it in databricks. Originally, the dataset is plit into four files each with the following format:

    MovieID:
    UserID, rating, date
    .
    .
    .
    MovieID: 
    UserID, rating, date
    .
    .
    .

We process these files so that each line has the format `MovieID, UserID, rating, date`

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Path where the data is stored 
display(dbutils.fs.ls("/FileStore/tables/Netflix")) 
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
<td>dbfs:/FileStore/tables/Netflix/combined_data_1_tar.xz</td>
<td>combined_data_1_tar.xz</td>
<td>1.19273784e8</td>
</tr>
<tr class="even">
<td>dbfs:/FileStore/tables/Netflix/combined_data_2_tar.xz</td>
<td>combined_data_2_tar.xz</td>
<td>1.33487548e8</td>
</tr>
<tr class="odd">
<td>dbfs:/FileStore/tables/Netflix/combined_data_3_tar.xz</td>
<td>combined_data_3_tar.xz</td>
<td>1.11976904e8</td>
</tr>
<tr class="even">
<td>dbfs:/FileStore/tables/Netflix/combined_data_4_tar.xz</td>
<td>combined_data_4_tar.xz</td>
<td>1.32669964e8</td>
</tr>
<tr class="odd">
<td>dbfs:/FileStore/tables/Netflix/formatted_combined_data_1_txt.gz</td>
<td>formatted_combined_data_1_txt.gz</td>
<td>1.66682858e8</td>
</tr>
<tr class="even">
<td>dbfs:/FileStore/tables/Netflix/formatted_combined_data_2_txt.gz</td>
<td>formatted_combined_data_2_txt.gz</td>
<td>1.87032103e8</td>
</tr>
<tr class="odd">
<td>dbfs:/FileStore/tables/Netflix/formatted_combined_data_3_txt.gz</td>
<td>formatted_combined_data_3_txt.gz</td>
<td>1.56042358e8</td>
</tr>
<tr class="even">
<td>dbfs:/FileStore/tables/Netflix/formatted_combined_data_4_txt.gz</td>
<td>formatted_combined_data_4_txt.gz</td>
<td>1.85177843e8</td>
</tr>
<tr class="odd">
<td>dbfs:/FileStore/tables/Netflix/movie_titles.csv</td>
<td>movie_titles.csv</td>
<td>577547.0</td>
</tr>
</tbody>
</table>

</div>

</div>

<div class="cell markdown">

Let us load first the movie titles.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
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
```

<div class="output execute_result plain_result" execution_count="1">

    Movie(1,2003,Dinosaur Planet)
    Movie(2,2004,Isle of Man TT 2004 Review)
    Movie(3,1997,Character)
    Movie(4,1994,Paula Abdul's Get Up & Dance)
    Movie(5,2004,The Rise and Fall of ECW)
    defined class Movie
    moviesTitlesRDD: org.apache.spark.rdd.RDD[Movie] = MapPartitionsRDD[129] at map at command-3389902380789882:3

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.mllib.recommendation.Rating
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.mllib.recommendation.ALS
    import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
    import org.apache.spark.mllib.recommendation.Rating

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
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
```

<div class="output execute_result plain_result" execution_count="1">

    Rating(2385003,13368,4.0)
    Rating(659432,13368,3.0)
    Rating(751812,13368,2.0)
    Rating(2625420,13368,2.0)
    Rating(1650301,13368,1.0)
    RatingsRDD_1: org.apache.spark.rdd.RDD[org.apache.spark.mllib.recommendation.Rating] = MapPartitionsRDD[258] at map at command-3389902380789875:2
    RatingsRDD_2: org.apache.spark.rdd.RDD[org.apache.spark.mllib.recommendation.Rating] = MapPartitionsRDD[261] at map at command-3389902380789875:8
    RatingsRDD_3: org.apache.spark.rdd.RDD[org.apache.spark.mllib.recommendation.Rating] = MapPartitionsRDD[264] at map at command-3389902380789875:14
    RatingsRDD_4: org.apache.spark.rdd.RDD[org.apache.spark.mllib.recommendation.Rating] = MapPartitionsRDD[267] at map at command-3389902380789875:20

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Concatenating the ratings RDDs (could not find a nice way of doing this)
val r1 = RatingsRDD_1.union(RatingsRDD_2) 
val r2 = r1.union(RatingsRDD_3)
val RatingsRDD = r2.union(RatingsRDD_4)
RatingsRDD.take(5).foreach(println)
```

<div class="output execute_result plain_result" execution_count="1">

    r1: org.apache.spark.rdd.RDD[org.apache.spark.mllib.recommendation.Rating] = UnionRDD[278] at union at command-3389902380791426:2
    r2: org.apache.spark.rdd.RDD[org.apache.spark.mllib.recommendation.Rating] = UnionRDD[279] at union at command-3389902380791426:3
    RatingsRDD: org.apache.spark.rdd.RDD[org.apache.spark.mllib.recommendation.Rating] = UnionRDD[280] at union at command-3389902380791426:4

</div>

</div>

<div class="cell markdown">

Let us put our dataset in a dataframe to visulaize it more nicely

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val RatingsDF = RatingsRDD.toDF
display(RatingsDF)
```

</div>

<div class="cell markdown">

Training the movie recommender system
-------------------------------------

In the training process we will start by splitting the dataset into - a training set (60%) - a validation set (20%) - a test set (20%)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Splitting the dataset 
val Array(trainingRDD, validationRDD, testRDD) = RatingsRDD.randomSplit(Array(0.60, 0.20, 0.20), 0L)
```

<div class="output execute_result plain_result" execution_count="1">

     training data size = 60288922, validation data size = 20097527, test data size = 20094058.
    trainingRDD: org.apache.spark.rdd.RDD[org.apache.spark.mllib.recommendation.Rating] = MapPartitionsRDD[8350] at randomSplit at command-3389902380791433:1
    validationRDD: org.apache.spark.rdd.RDD[org.apache.spark.mllib.recommendation.Rating] = MapPartitionsRDD[8351] at randomSplit at command-3389902380791433:1
    testRDD: org.apache.spark.rdd.RDD[org.apache.spark.mllib.recommendation.Rating] = MapPartitionsRDD[8352] at randomSplit at command-3389902380791433:1

</div>

</div>

<div class="cell markdown">

After splitting the dataset, your training set has about 60,288,922 entries and the validation and test sets each have about 20,097,527 entries (the exact number of entries in each dataset varies slightly due to the random nature of the `randomSplit()` transformation.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// let's find the exact sizes we have next
println(" training data size = " + trainingRDD.count() +
        ", validation data size = " + validationRDD.count() +
        ", test data size = " + testRDD.count() + ".")
```

<div class="output execute_result plain_result" execution_count="1">

     training data size = 60288922, validation data size = 20097527, test data size = 20094058.

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
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
```

<div class="output execute_result plain_result" execution_count="1">

    rank and Mean Squared Error = 50 and 0.7060806826621556
    rank and Mean Squared Error = 100 and 0.7059490573655225
    rank and Mean Squared Error = 150 and 0.7056407494686934

</div>

</div>
