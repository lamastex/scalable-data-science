<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Music Recommendation System
---------------------------

In general, [recommender systems](https://en.wikipedia.org/wiki/Recommender_system) are algorithms designed for suggesting relevant items/products to users. In the last decades they have gained much interest because of the potential of increasing the user experience at the same time as generating more profit to companies. Nowadays, these systems can be found in several well-known services like Netfilx, Amazon, YouTube and Spotify. As an indicator of how valuable these algorithms are for such companies: back in 2006 Netflix announced the open [Netflix Prize Competition](https://www.netflixprize.com/) for the best algorithm to predict users movie ratings based on collected data. The winning team with the best algorithm improving the state-of-the-art performance with at least 10% was promised an award of 1 000 000$. **In this notebook we are going to develope a system for recommending musical artists to users given their listening history**. We will implement a model related to matrix factorization discussed in the preceeding chapter.

</div>

<div class="cell markdown">

### Problem Setting

We let \\(U\\) be the set containing all \\(m\\) users and let \\(I\\) be the set containing all \\(n\\) available items. Now, we introduce the matrix \\(R\in \mathbb{R}^{m \times n}\\) with elements \\(r{_u}{_i}\\) as values encoding possible interactions between users \\(u\in U\\) and items \\(i \in I\\). This matrix is often very sparse because of the huge number of possible user-item interactions never observed. Depending on the type of information encoded in the interaction matrix \\(R\\) one usally refers to either *explicit* or *implicit* data.

For explicit data, \\(r{_u}{_i}\\) contains information directly related to user \\(u\\)'s preference for item \\(i\\), e.g movie ratings. In the case of implicit data, \\(r{_u}{_i}\\) contains indirect information of a user's preference for an item by observing past user behavior. Examples could be the number of times a user played a song or visited a webpage. Note that in the implicit case we are lacking information about items that the user dislikes because e.g if a user of a music service has not played any songs from a particular artist it could either mean that the user simply doesn't like that artist or that the user hasn't encountered that artist before but would potentially like it if the user had discovered the artist.

Given the observations in the interaction matrix \\(R\\), we would like our model to suggest unseen items relevant to the users.

</div>

<div class="cell markdown">

### Collaborative Filtering

Broadly speaking, recommender algorithms can be divided into two categories: [content based](https://en.wikipedia.org/wiki/Recommender_system#Content-based_filtering) and [collaborative filtering](https://en.wikipedia.org/wiki/Collaborative_filtering) (CF). Here, we will just focus on collaborative filtering which is a technique using patterns of user-item interactions and discarding any additional information about the users or items themselves. It is based on the assumption that if a user similar to you likes an item, then there is a high probability that you also like that particular item. In other words, similar users have similar tastes.

There are different approaches to CF, and we have chosen a laten factor model approach inspired by low-rank SVD factorization of matrices. The aim is to uncover latent features explaining the observed \\(r{_u}{_i}\\) values. Each user \\(u\\) is associated to a user-feature vector \\(x{_u}\in \mathbb{R}^f\\) and similarly each item \\(i\\) is associated to an item-feature vector \\(y{_i} \in \mathbb{R}^f\\). Then we want the dot products \\(x{_u}^Ty{_i}\\) to explain the observed \\(r{_u}{_i}\\) values. With all user- and item-features at hand in the latent space \\(\mathbb{R}^f\\) we can estimate a user \\(u\\)'s preference for an unseen item \\(j\\) by simply computing \\(x{_u}^Ty{_j}\\).

We transorm the problem of finding the vectors \\(x{_u}, y{_i}\\) into a minimization problem as suggested in the paper [Collaborative Filtering for Implicit Feedback Datasets](https://www.researchgate.net/publication/220765111_Collaborative_Filtering_for_Implicit_Feedback_Datasets). First we introduce the binarized quantitiy \\(p{_u}{_i}\\) defined by:

\\[p_{ui}=\begin{cases}1 \text{   if  } r_{ui}>0, \\\ 0 \text{   if  } r_{ui}=0,\end{cases}\\] encoding whether user \\(u\\) has interacted with and supposedly likes item \\(i\\). However, our confidence that user \\(u\\) likes item \\(i\\) given that \\(p{_u}{_i}=1\\) should vary with the actual observed \\(r{_u}{_i}\\) value. As an example, we would be more confident that a user likes an artist he/she has listened to hundreds of times than an artist played by the user only once. Therefore we introduce the confidence \\(c{_u}{_i}\\):

\\[c_{ui}=1+\alpha r_{ui}\\],

where \\(\alpha\\) is a hyperparameter. From the above equation we can see that the confidence for non observed user-item interaction defaults to 1. Now we formulize the minimization problem:

\\[\min_{X,Y}\sum_{u\in U,i \in I}c_{ui}(p_{ui}-x_u^Ty_i)^2+\lambda(\sum_{u\in U}||x_u||^2+\sum_{i\in I}||y_i||^2),\\] where \\(X,Y\\) are matrices holding the \\(x_u,y_i\\) as columns respectively. In addition, we also have a regularization term to avoid overfitting. Notice that this is closely related to regularized low-rank matrix factorization of the matrix \\(P\\) with \\(p{_u}{_i}\\) as elements. We want to approximate \\(P\approx X^TY\\) where both \\(X,Y\\) have low rank (\\(f\\)). Because of the weights \\(c{_u}{_i}\\) we care more about recover entries in \\(P\\) with high confidence, directly related to the observations.

</div>

<div class="cell markdown">

### Dataset

For this application we use a [dataset](https://grouplens.org/datasets/hetrec-2011/) containing user-artist listening information from the online music service [Last.fm](http://www.last.fm).

One of the available files contains triplets (`userID` `artistID` `play_count`) describing the number of times a user has played an artist. Another file contains tuples (`artistID` `name`) mapping the artistID:s to actual artist names. There are a total of 92834 (`userID` `artistID` `play_count`) triplets containing 1892 unique `userID`s and 17632 unique `artistID`s. Since the observations in the dataset do not contain direct information about artist preferences, this is an implicit dataset as discussed erlier. Based on this dataset we want our model to give artist recommendations to the users.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import spark.implicits._
import org.apache.spark.sql.functions._
```

<div class="output execute_result plain_result" execution_count="1">

    import spark.implicits._
    import org.apache.spark.sql.functions._

</div>

</div>

<div class="cell markdown">

**Lets load the data!**

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Load the (userID, artistID, play_count) triplets.
val fileName_data="dbfs:/FileStore/tables/project4/hetrec2011-lastfm-2k/user_artists.dat"
val df_raw = spark.read.format("csv").option("header", "true").option("delimiter", "\t").option("inferSchema","true").load(fileName_data).withColumnRenamed("weight","play_count")
df_raw.cache()
df_raw.orderBy(rand()).show(5)

// Load the (artistID, name) tuples.
val fileName_names="dbfs:/FileStore/tables/project4/hetrec2011-lastfm-2k/artists.dat"
val artist_names = spark.read.format("csv").option("header", "true").option("delimiter", "\t").option("inferSchema","true").load(fileName_names).withColumnRenamed("id","artistID").select("artistID","name")
artist_names.cache()
artist_names.show(5)
```

<div class="output execute_result plain_result" execution_count="1">

    +------+--------+----------+
    |userID|artistID|play_count|
    +------+--------+----------+
    |  1553|    2478|       171|
    |    65|    1858|       254|
    |   496|       9|       161|
    |   304|     163|        19|
    |   747|     507|       626|
    +------+--------+----------+
    only showing top 5 rows

    +--------+-----------------+
    |artistID|             name|
    +--------+-----------------+
    |       1|     MALICE MIZER|
    |       2|  Diary of Dreams|
    |       3|Carpathian Forest|
    |       4|     Moi dix Mois|
    |       5|      Bella Morte|
    +--------+-----------------+
    only showing top 5 rows

    fileName_data: String = dbfs:/FileStore/tables/project4/hetrec2011-lastfm-2k/user_artists.dat
    df_raw: org.apache.spark.sql.DataFrame = [userID: int, artistID: int ... 1 more field]
    fileName_names: String = dbfs:/FileStore/tables/project4/hetrec2011-lastfm-2k/artists.dat
    artist_names: org.apache.spark.sql.DataFrame = [artistID: int, name: string]

</div>

</div>

<div class="cell markdown">

We print some statistics and visualize the raw data.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val n_data = df_raw.count().asInstanceOf[Long].floatValue(); // Number of observations
val n_users = df_raw.agg(countDistinct("userID")).collect()(0)(0).asInstanceOf[Long].floatValue(); // Number of unique users
val n_artists = df_raw.agg(countDistinct("artistID")).collect()(0)(0).asInstanceOf[Long].floatValue(); // Number of unique artists
val sparsity = 1-n_data/(n_users*n_artists) //Sparsity of the data

println("Number of data points: " + n_data)
println("Number of users: " + n_users)
println("Number of artists: " + n_artists)
print("Sparsity:" + sparsity.toString + "\n")
```

<div class="output execute_result plain_result" execution_count="1">

    Number of data points: 92834.0
    Number of users: 1892.0
    Number of artists: 17632.0
    Sparsity:0.9972172
    n_data: Float = 92834.0
    n_users: Float = 1892.0
    n_artists: Float = 17632.0
    sparsity: Float = 0.9972172

</div>

</div>

<div class="cell markdown">

Below we see that the play count variable tends to vary over a large range. From 1 to over 350 000.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(df_raw.select("play_count"))
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/4_02_1.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(df_raw.select("play_count").filter($"play_count"<1000))
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/4_02_2.JPG?raw=true)

</div>

<div class="cell markdown">

We count the total plays and number of unique listeners for each artist.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Compute some statistics for the artists.
val artist_data_raw = df_raw.groupBy("artistID").agg(count("artistID") as "unique_users",
                                                      sum("play_count") as "total_plays_artist")
artist_data_raw.sort(desc("total_plays_artist")).join(artist_names,"artistID").show(5) // Top artists based on total plays
artist_data_raw.sort(desc("unique_users")).join(artist_names,"artistID").show(5) // Top artists based on number of unique listener
```

<div class="output execute_result plain_result" execution_count="1">

    +--------+------------+------------------+------------------+
    |artistID|unique_users|total_plays_artist|              name|
    +--------+------------+------------------+------------------+
    |     289|         522|           2393140|    Britney Spears|
    |      72|         282|           1301308|      Depeche Mode|
    |      89|         611|           1291387|         Lady Gaga|
    |     292|         407|           1058405|Christina Aguilera|
    |     498|         399|            963449|          Paramore|
    +--------+------------+------------------+------------------+
    only showing top 5 rows

    +--------+------------+------------------+--------------+
    |artistID|unique_users|total_plays_artist|          name|
    +--------+------------+------------------+--------------+
    |      89|         611|           1291387|     Lady Gaga|
    |     289|         522|           2393140|Britney Spears|
    |     288|         484|            905423|       Rihanna|
    |     227|         480|            662116|   The Beatles|
    |     300|         473|            532545|    Katy Perry|
    +--------+------------+------------------+--------------+
    only showing top 5 rows

    artist_data_raw: org.apache.spark.sql.DataFrame = [artistID: int, unique_users: bigint ... 1 more field]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(artist_data_raw.select("total_plays_artist"))
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/4_02_3.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(artist_data_raw.select("total_plays_artist").filter($"total_plays_artist"<10000))
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/4_02_4.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(artist_data_raw.select("unique_users"))
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/4_02_5.JPG?raw=true)

</div>

<div class="cell markdown">

We count the total plays and the number of unique artists each user has listened to.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Compute statistics for each user.
val user_data_raw = df_raw.groupBy("userID").agg(count("userID") as "unique_artists",
                                                  sum("play_count") as "total_plays_user")
user_data_raw.sort(desc("total_plays_user")).show(5) // Show users with most total plays
```

<div class="output execute_result plain_result" execution_count="1">

    +------+--------------+----------------+
    |userID|unique_artists|total_plays_user|
    +------+--------------+----------------+
    |   757|            50|          480039|
    |  2000|            50|          468409|
    |  1418|            50|          416349|
    |  1642|            50|          388251|
    |  1094|            50|          379125|
    +------+--------------+----------------+
    only showing top 5 rows

    user_data_raw: org.apache.spark.sql.DataFrame = [userID: int, unique_artists: bigint ... 1 more field]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(user_data_raw.select("total_plays_user"))
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/4_02_6.JPG?raw=true)

</div>

<div class="cell markdown">

Now we join all statistics into a single dataframe.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Merge all statistics and data into a single dataframe.
val df_joined = df_raw.join(artist_data_raw, "artistID").join(user_data_raw, "userID").join(artist_names,"artistID").select("userID", "artistID","play_count", "name", "unique_artists","unique_users", "total_plays_user","total_plays_artist")
df_joined.show(5)
```

<div class="output execute_result plain_result" execution_count="1">

    +------+--------+----------+-------------+--------------+------------+----------------+------------------+
    |userID|artistID|play_count|         name|unique_artists|unique_users|total_plays_user|total_plays_artist|
    +------+--------+----------+-------------+--------------+------------+----------------+------------------+
    |     2|      51|     13883|  Duran Duran|            50|         111|          168737|            348919|
    |     2|      52|     11690|    Morcheeba|            50|          23|          168737|             18787|
    |     2|      53|     11351|          Air|            50|          75|          168737|             44230|
    |     2|      54|     10300| Hooverphonic|            50|          18|          168737|             15927|
    |     2|      55|      8983|Kylie Minogue|            50|         298|          168737|            449292|
    +------+--------+----------+-------------+--------------+------------+----------------+------------------+
    only showing top 5 rows

    df_joined: org.apache.spark.sql.DataFrame = [userID: int, artistID: int ... 6 more fields]

</div>

</div>

<div class="cell markdown">

Collaborative filtering models suffer from the [cold-start problem](https://yuspify.com/blog/cold-start-problem-recommender-systems/), meaning they have difficulties in making inference of new users or items. Therefore we will filter out artists with fewer than 20 unique listeners and users that have listened to less than 5 artists.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Remove artists with less than 20 unique users, and recompute the statistics.
val df_filtered_1 = df_joined.filter($"unique_users">=20).select(df_joined("userID"),df_joined("artistID"),df_joined("play_count"))
val artist_data_1 = df_filtered_1.groupBy("artistID").agg(count("artistID") as "unique_users",
                                                          sum("play_count") as "total_plays_artist")
                                                     .withColumnRenamed("artistID","artistID_1")

val user_data_1 = df_filtered_1.groupBy("userID").agg(count("userID") as "unique_artists",
                                                          sum("play_count") as "total_plays_user")
                                                     .withColumnRenamed("userID","userID_1")

val df_joined_filtered_1 = df_filtered_1.join(artist_data_1, artist_data_1("artistID_1")===df_filtered_1("artistID"))
                                        .join(user_data_1, user_data_1("userID_1")===df_filtered_1("userID"))
                                        .select(df_filtered_1("userID"),df_filtered_1("artistID"),df_filtered_1("play_count"), 
                                                 artist_data_1("unique_users"),artist_data_1("total_plays_artist"),
                                                 user_data_1("unique_artists"), user_data_1("total_plays_user"))

// Remove users with less than 5 unique users, and recompute the statistics.
val df_filtered_2 = df_joined_filtered_1.filter($"unique_artists">=5).select(df_filtered_1("userID"),df_filtered_1("artistID"),
                                                                             df_filtered_1("play_count"))

val artist_data = df_filtered_2.groupBy("artistID").agg(count("artistID") as "unique_users",
                                                        sum("play_count") as "total_plays_artist")
                                                   .withColumnRenamed("artistID","artistID_2")

val user_data = df_filtered_2.groupBy("userID").agg(count("userID") as "unique_artists",
                                                         sum("play_count") as "total_plays_user")
                                                   .withColumnRenamed("userID","userID_2")

// Now we collect our new filtered data.
val user_artist_data = df_filtered_2.join(artist_data, artist_data("artistID_2")===df_filtered_2("artistID"))
                                    .join(user_data, user_data("userID_2")===df_filtered_2("userID"))
                                    .select("userID","artistID","play_count","unique_users","total_plays_artist","unique_artists","total_plays_user")

user_artist_data.show(5)
```

<div class="output execute_result plain_result" execution_count="1">

    +------+--------+----------+------------+------------------+--------------+----------------+
    |userID|artistID|play_count|unique_users|total_plays_artist|unique_artists|total_plays_user|
    +------+--------+----------+------------+------------------+--------------+----------------+
    |   148|    1118|       214|          62|             53915|            12|            3026|
    |   148|    1206|       245|          50|             32827|            12|            3026|
    |   148|     206|       214|          83|             36944|            12|            3026|
    |   148|     233|       170|         138|            160317|            12|            3026|
    |   148|     429|       430|         162|             91740|            12|            3026|
    +------+--------+----------+------------+------------------+--------------+----------------+
    only showing top 5 rows

    df_filtered_1: org.apache.spark.sql.DataFrame = [userID: int, artistID: int ... 1 more field]
    artist_data_1: org.apache.spark.sql.DataFrame = [artistID_1: int, unique_users: bigint ... 1 more field]
    user_data_1: org.apache.spark.sql.DataFrame = [userID_1: int, unique_artists: bigint ... 1 more field]
    df_joined_filtered_1: org.apache.spark.sql.DataFrame = [userID: int, artistID: int ... 5 more fields]
    df_filtered_2: org.apache.spark.sql.DataFrame = [userID: int, artistID: int ... 1 more field]
    artist_data: org.apache.spark.sql.DataFrame = [artistID_2: int, unique_users: bigint ... 1 more field]
    user_data: org.apache.spark.sql.DataFrame = [userID_2: int, unique_artists: bigint ... 1 more field]
    user_artist_data: org.apache.spark.sql.DataFrame = [userID: int, artistID: int ... 5 more fields]

</div>

</div>

<div class="cell markdown">

Below we can see that we have reduced the amount of data. The number of users are quite similar as before but the number of artists is significantly reduced indicating there were many artists in the raw data only played by a small fraction of users.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val n_data_new = user_artist_data.count().asInstanceOf[Long].floatValue(); // Number of observations
val n_users_new = user_artist_data.agg(countDistinct("userID")).collect()(0)(0).asInstanceOf[Long].floatValue(); // Number of unique users
val n_artists_new = user_artist_data.agg(countDistinct("artistID")).collect()(0)(0).asInstanceOf[Long].floatValue(); // Number of unique artists
val sparsity_new = 1-n_data/(n_users*n_artists) // Compute the sparsity

println("Number of data points: " + n_data_new)
println("Number of users: " + n_users_new)
println("Number of artists: " + n_artists_new)
print("Sparsity:" + sparsity.toString + "\n")
```

<div class="output execute_result plain_result" execution_count="1">

    Number of data points: 53114.0
    Number of users: 1819.0
    Number of artists: 804.0
    Sparsity:0.9972172
    n_data_new: Float = 53114.0
    n_users_new: Float = 1819.0
    n_artists_new: Float = 804.0
    sparsity_new: Float = 0.9972172

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(user_artist_data.select("play_count"))
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/4_02_7.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(artist_data.select("total_plays_artist"))
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/4_02_8.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(artist_data.select("unique_users"))
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/4_02_9.JPG?raw=true)

</div>

<div class="cell markdown">

The total number of plays are correlated to the number of unique listeners (as expected) as illustrated in the figure below.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(artist_data)
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/4_02_10.JPG?raw=true)

</div>

<div class="cell markdown">

In the [paper](https://www.researchgate.net/publication/220765111_Collaborative_Filtering_for_Implicit_Feedback_Datasets) mentioned above, the authors suggest scaling the \\(r{_u}{_i}\\) if the values tends to vary over large range as in our case. They presented a log scaling scheme but after testing different approaches we found that scaling by taking the square root of the observed play counts (thus reducing the range) worked best.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
//Scaling the play_counts
val user_artist_data_scaled = user_artist_data
.withColumn("scaled_value", sqrt(col("play_count"))).drop("play_count").withColumnRenamed("scaled_value","play_count")
user_artist_data_scaled.show(5)
```

<div class="output execute_result plain_result" execution_count="1">

    +------+--------+------------+------------------+--------------+----------------+------------------+
    |userID|artistID|unique_users|total_plays_artist|unique_artists|total_plays_user|        play_count|
    +------+--------+------------+------------------+--------------+----------------+------------------+
    |   148|     436|         124|             88270|            12|            3026| 18.33030277982336|
    |   148|    1206|          50|             32827|            12|            3026|15.652475842498529|
    |   148|     512|          67|             62933|            12|            3026|15.620499351813308|
    |   148|     429|         162|             91740|            12|            3026| 20.73644135332772|
    |   148|    1943|          25|             13035|            12|            3026| 17.26267650163207|
    +------+--------+------------+------------------+--------------+----------------+------------------+
    only showing top 5 rows

    user_artist_data_scaled: org.apache.spark.sql.DataFrame = [userID: int, artistID: int ... 5 more fields]

</div>

</div>

<div class="cell markdown">

Plotting the scaled data. We ca see that the range is smaller after the scaling.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(user_artist_data_scaled.select("play_count"))
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/4_02_9.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(user_artist_data_scaled.select("play_count"))
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/4_02_12.JPG?raw=true)

</div>

<div class="cell markdown">

We split our scaled dataset into training, validation and test sets.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Split data into training, validation and test sets.
val Array(training_set, validation_set, test_set) = user_artist_data_scaled.select("userID","artistID","play_count").randomSplit(Array(0.6, 0.2, 0.2))
training_set.cache()
validation_set.cache()
test_set.cache()
```

<div class="output execute_result plain_result" execution_count="1">

    training_set: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [userID: int, artistID: int ... 1 more field]
    validation_set: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [userID: int, artistID: int ... 1 more field]
    test_set: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [userID: int, artistID: int ... 1 more field]
    res109: test_set.type = [userID: int, artistID: int ... 1 more field]

</div>

</div>

<div class="cell markdown">

### Alternating Least Squares

By looking at the minimization problem again, we see that if one of \\(X\\) and \\(Y\\) is fixed, the cost function is just quadratic and hence the minimum can be computed easily. Thus, we can alternate between re-computing the user and artist features while holding the other one fixed. It turns out that the over all const function is guaranteed to decrease in each iteration. This procedure is called [Alternating Least Squares](https://datasciencemadesimpler.wordpress.com/tag/alternating-least-squares/) and is available in Spark. \\[\min_{X,Y}\sum_{u\in U,i \in I}c_{ui}(p_{ui}-x_u^Ty_i)^2+\lambda(\sum_{u\in U}||x_u||^2+\sum_{i\in I}||y_i||^2),\\]

The solution to the respective quadratic problems are:

\\[x_u=(Y^TC^uY+\lambda Id )^{-1}Y^TC^up(u) \quad \forall u\in U,\\] \\[y_i=(X^TC^iX+\lambda Id )^{-1}X^TC^ip(i) \quad \forall i\in I,\\]

where \\(C^u, C^i\\) are a diagonal matrices with diagonal entries \\(c{_u}{_i}\\) \\(i \in I\\) and \\(c{_u}{_i}\\) \\(u \in U\\) respectively. The \\(p(u)\\) and \\(p(i)\\) are vectors containing all binarized user and artist observations for user \\(u\\) and artist \\(i\\) respectively. The computational bottlneck is to compute the \\(Y^TC^uY\\) (require time \\(O(f^2n)\\) for each user). However, we can rewrite the product as \\(Y^TC^uY=Y^TY+Y^T(C^u-I)Y\\) and now we see that the term \\(Y^TY\\) does not depend on \\(u\\) and that \\((C^u-I)\\) will only have a number of non-zero entries equal to the number of artists user \\(u\\) has interacted with (which is usually much smaller than the total number of artists). Hence, that representation is much more beneficial computationally. A similar approach can be applied to \\(X^TC^iX\\). The matrix inversions need to be done on matrices of size \\(f \times f\\) where \\(f\\) is the dimension of the latent feature space and thus relatively small compared to \\(m,n\\).

When we have all the user and artist features we can produce a recommendation list of artist for user \\(u\\) by taking the dot products \\(x{_u}^Ty{_i}\\) for all artists and arrange them in a list in descending order with respect to these computed values.

</div>

<div class="cell markdown">

### Evaluation

One approach to measure the performance of the model would be to measure the RMSE:

\\[\sqrt{\frac{1}{\\#\text{observations}}\sum_{u, i}(p_{ui}^t-x_u^Ty_i)^2},\\] where \\(p_{ui}^t\\) is the binarized observations from the test set. However, this metric is not very suitable for this particular application since for the zero entries of \\(p^t{_u}{_i}\\) we don't know if the user dislikes the artist or just hasn't discovered it. In the [paper](https://www.researchgate.net/publication/220765111_Collaborative_Filtering_for_Implicit_Feedback_Datasets) they suggest the mean percentile rank metric:

\\[\overline{rank}=\frac{\sum_{u, i}r^t_{ui}rank_{ui}}{\sum_{u, i} r^t_{ui}},\\] where \\(rank_{ui}\\) is the percentile rank of artist \\(i\\) in the produced recommendation list for user \\(u\\). Hence if artist \\(j\\) is in the first place in the list for user \\(u\\) we get that \\(rank{_u}{_j}=0\%\\) and if it is in the last place we get \\(rank{_u}{_j}=100\%\\). Thus, this metric is an weighted average of the percentiles of the artists the users have listened to. If user \\(u\\) has listened to artist \\(j\\) many times we have a large \\(r{_u}{_j}\\) value, but if the artist is ranked very low in the recommendation list for this user, it will increase the value of \\(\overline{rank}\\) drastically. If the model instead ranks this artist correctly in the top, the product \\(r{_u}{_j}rank{_u}{_j}\\) will get small. Hence, low values of \\(\overline{rank}\\) is desired.

</div>

<div class="cell markdown">

Unfortunately, the \\(\overline{rank}\\) metric is not implemented in Spark yet, so below we have written our own function for computing it given the ranked artist lists for each user. We also remove an artist from the recommendation list for a user if we have observed that the user listened to that artist in the training data. This eliminates the easy recommendation, that is, recommending the same artists we know that the user has already listened to.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.Dataset 
import org.apache.spark.sql.Row
import org.apache.spark.sql.DataFrame

// Function for computing the mean rank metric.
// Input: 
// - prediction_scores_new: DataFrame with userIDs and corresponding recommendation lists.
// - training_set: DataFrame with observations in training set
// - validation_set: DataFrame with the observations needed for the evaluation of the metric.
// Output: Float corresponind to the mean_rank score.
def eval_model(predictions_scores_new: DataFrame, training_set: DataFrame, validation_set: DataFrame) : Float = {
  
  val predictions_scores = predictions_scores_new.withColumnRenamed("userID","userID_new") // Avoinding duplicate column names.
  val recommendations = predictions_scores.withColumn("recommendations", explode($"recommendations")) // Rearrange the recommendation lists.
                                      .select("userID_new","recommendations.artistID", "recommendations.rating")
  
  val recommendations_filtered = recommendations.join(training_set, training_set("userID")===recommendations("userID_new") && training_set("artistID")===recommendations("artistID"), "leftanti") // Erase artists appearing in the training for each user.
  
  // Compute ranking percentiles.
  val recommendations_percentiles = recommendations_filtered.withColumn("rank",percent_rank()
                                                            .over(Window.partitionBy("userID_new").orderBy(desc("rating")))) 
  // Store everything in single DataFrame.
  val table_data = recommendations_percentiles.join(validation_set, recommendations_percentiles("userID_new")===validation_set("userID") && recommendations_percentiles("artistID")===validation_set("artistID"))
  
  // Compute the sum in the numerator for the metric.
  val numerator = table_data.withColumn("ru1rankui", $"rank"*$"play_count"*100.0)
                            .agg(sum("ru1rankui"))
                            .collect()(0)(0).asInstanceOf[Double]
  
  // Compute the sum in the denominator for the metric.
  val denumerator = table_data.agg(sum("play_count"))
                              .collect()(0)(0)
                              .asInstanceOf[Double]
  // Compute the mean percentile rank.
  val rank_score = numerator/denumerator
  rank_score.toFloat
}
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.sql.expressions.Window
    import org.apache.spark.sql.Dataset
    import org.apache.spark.sql.Row
    import org.apache.spark.sql.DataFrame
    eval_model: (predictions_scores_new: org.apache.spark.sql.DataFrame, training_set: org.apache.spark.sql.DataFrame, validation_set: org.apache.spark.sql.DataFrame)Float

</div>

</div>

<div class="cell markdown">

Now we import the [ALS module](https://spark.apache.org/docs/latest/api/scala/org/apache/spark/ml/recommendation/ALSModel.html) from Spark and start the training. We perform a grid search over the hyper-parameters: the latent dimension \\(f\\), confidence parameter \\(\alpha\\) and regularization parameter \\(\lambda\\). We choose the parameter combinations based on the performance on the validation set.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.ml.recommendation.ALS
// Number of iterations in the ALS algorithm
val numIter = 10

 
 val ranks = List(10,50,100,150) // Dimension of latent feature space
 val lambdas=List(0.1, 1.0, 2.0) // Regularization parameter
 val alphas=List(0.5, 1.0, 5.0) // Confidence parameter

// Loop over all parameter combinations
for ( alpha <- alphas ){
  for ( lambda <- lambdas ){
    for ( rank <- ranks ){
      val als = new ALS()
        .setRank(rank)
        .setMaxIter(numIter)
        .setRegParam(lambda)
        .setUserCol("userID")
        .setItemCol("artistID")
        .setRatingCol("play_count")
        .setImplicitPrefs(true) // Indicate we have implicit data
        .setAlpha(alpha)
        .setNonnegative(true) // Constrain to non-negative values
      
      // Fit the model
      val model = als.fit(training_set)
      
      model.setColdStartStrategy("drop") // This is to ensure we handle unseen users or unseen artist saftely during the prediction.
           .setUserCol("userID")
           .setItemCol("artistID")
      // Generate the recommendations
      val predictions_scores = model.recommendForUserSubset(validation_set,n_artists_new.toInt)
      
      // Evaluate the model
      println("rank=" + rank + ", alpha=" + alpha + ", lambda=" + lambda + ", mean_rank=" + eval_model(predictions_scores, training_set, validation_set))
    }
  }
}
```

</div>

<div class="cell markdown">

We get our final model by choosing \\(f=150, \alpha=0.5\\) and \\(\lambda=2.0\\) train the model again and evaluating it on the test set. We observe a test error of 7.75 %.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Retrain the best model.

val numIter_final=10
val rank_final=150
val alpha_final=0.5
val lambda_final=2.0
val als_final = new ALS()
        .setRank(rank_final)
        .setMaxIter(numIter_final)
        .setRegParam(lambda_final)
        .setUserCol("userID")
        .setItemCol("artistID")
        .setRatingCol("play_count")
        .setImplicitPrefs(true)
        .setAlpha(alpha_final)
        .setNonnegative(true)
val model_final = als_final.fit(training_set)
model_final.setColdStartStrategy("drop")
     .setUserCol("userID")
     .setItemCol("artistID")

// Evaluate on the validation set.
val predictions_scores_val = model_final.recommendForUserSubset(validation_set,n_artists_new.toInt)
println("Validation set: mean_rank=" + eval_model(predictions_scores_val, training_set, validation_set))

// Evaluate on the test set.
val predictions_scores_test = model_final.recommendForUserSubset(test_set,n_artists_new.toInt)
println("Test set: mean_rank=" + eval_model(predictions_scores_val, training_set, test_set))
```

<div class="output execute_result plain_result" execution_count="1">

    Validation set: mean_rank=7.7979865
    Test set: mean_rank=7.75016
    numIter_final: Int = 10
    rank_final: Int = 150
    alpha_final: Double = 0.5
    lambda_final: Double = 2.0
    als_final: org.apache.spark.ml.recommendation.ALS = als_f778f6cc23db
    model_final: org.apache.spark.ml.recommendation.ALSModel = als_f778f6cc23db
    predictions_scores_val: org.apache.spark.sql.DataFrame = [userID: int, recommendations: array<struct<artistID:int,rating:float>>]
    predictions_scores_test: org.apache.spark.sql.DataFrame = [userID: int, recommendations: array<struct<artistID:int,rating:float>>]

</div>

</div>

<div class="cell markdown">

### Model Comparison

We compare our model with two naive ones.

**Random Recommendations**: First we just produce a ranom recommendation list for each user and evaluate the metric. Note that for a random ranking the expected ranking percentile for an artist would be 50%, expected value of the mean percentile rank should be: \\(\mathbb{E}(\overline{rank})=\mathbb{E}(\frac{\sum{_u}{_i}r^t{_u}{_i}rank{_u}{_i}}{\sum{_u}{_i} r^t{_u}{_i}} ) = \frac{\sum{_u}{_i}r^t{_u}{_i}\mathbb{E}(rank{_u}{_i})}{\sum{_u}{_i} r^t{_u}{_i}}= \frac{\sum{_u}{_i}r^t{_u}{_i}\cdot 0.5}{\sum{_u}{_i} r^t{_u}{_i}}=0.5\\) for this random model.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
case class Rating(artistID: Int, rating: Float) // Simple class for getting the recommendations in suitable form.

// Generating random array of artistIDs.
val random = artist_data.select("artistID_2").distinct().orderBy(rand()).withColumn("idx",monotonically_increasing_id)
           .withColumn("rownumber",row_number.over(Window.orderBy(desc("idx")))).drop("idx").sort(desc("rownumber"))
          .collect.map(row =>Rating(row.getInt(0),row.getInt(1).toFloat))

val test_users = test_set.select("userID").distinct()

//Append the arrays to DataFrame.
val prediction_scores = user_artist_data.select("userID").distinct().withColumn("recommendations",typedLit(random))
                                        .join(test_users,"userID")
```

</div>

<div class="cell markdown">

The actual value we get is \\(\overline{rank}\approx 50.86 \%\\) which agrees with the above reasoning.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
println("Random_model: mean_rank=" + eval_model(prediction_scores, training_set, test_set))
```

<div class="output execute_result plain_result" execution_count="1">

    Random_model: mean_rank=50.860817

</div>

</div>

<div class="cell markdown">

**Popular Recommendations:** We recommend each user the list of artist sorted by the number of total plays in the training dataset. Hence the list with the over all most popular artist will be presented as the recommendations independent of the user. Hence, this is not personalized recommenations.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
//Generating arrays of artistIDs w.r.t most plays.
val most_popular = artist_data.select("artistID_2", "total_plays_artist").sort(desc("total_plays_artist"))
                              .collect.map(row =>Rating(row.getInt(0),row.getLong(1).toFloat))
val test_users = test_set.select("userID").distinct()

//Append the arrays to DataFrame.
val prediction_scores = user_artist_data.select("userID").distinct().withColumn("recommendations",typedLit(most_popular))
                                        .join(test_users,"userID")
```

</div>

<div class="cell markdown">

For this model we get \\(\overline{rank}\approx 24.6 \%\\) which is better than the random one but much worse than our ALS model that got \\(\overline{rank}\approx 7.75 \%\\)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
println("Popular_model: mean_rank=" + eval_model(prediction_scores, training_set, test_set))
```

<div class="output execute_result plain_result" execution_count="1">

    Popular_model: mean_rank=24.553421

</div>

</div>

<div class="cell markdown">

Below we define one functions for presenting a users top artists based on observations in the train set and recommended undiscovered artists generated by our model.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.ml.recommendation.ALSModel

// Function for showing the favorit artists for a given user based on the training set.
// Input:
// - userID: Int, the id of the user.
// - n: Int, number of top artists that should be presented.
// - user_artist_data: DataFrame with observations.
// - artist_names: Dataframe mapping artistIDs to actual artist names
// Output:
// - DataFrame with the users top artists
def userHistory(userID: Int, n: Int, user_artist_data: DataFrame, artist_names: DataFrame): DataFrame = {
  
  // Filter the userID and sort the artists w.r.t the play count. Append the actual artist names. 
  val data = user_artist_data.filter($"userID"===userID).sort(desc("play_count")).join(artist_names, "artistID")
  data.select("userID","artistID","name").show(n)                            
  data.select("userID","artistID","name")
}

// Function for presenting recommended artist for a user.
// Input:
// - Model: ALSModel, the trained model
// - userID: DataFrame, with userID
// - n: Int, number of top artists that should be presented.
// - training_set: DataFrame used during the training.
// - artist_names: Dataframe mapping artistIDs to actual artist names
// Output:
// - DataFrame with the users recommended artists
def recommendToUser(model: ALSModel, userID: DataFrame, n: Int, training_set: DataFrame, artist_names: DataFrame) : DataFrame = {
  // Generate recommendations using the model.
  val recommendations = model.recommendForUserSubset(userID, n_artists_new.toInt).withColumn("recommendations", explode($"recommendations"))
                                      .select("userID","recommendations.artistID", "recommendations.rating").join(artist_names, "artistID").select("userID","artistID","name","rating")
  
  // Remove possible artists observed in the training set
  recommendations.join(training_set,training_set("userID")===recommendations("userID") && training_set("artistID")===recommendations("artistID"),"leftanti")
}

```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.ml.recommendation.ALSModel
    userHistory: (userID: Int, n: Int, user_artist_data: org.apache.spark.sql.DataFrame, artist_names: org.apache.spark.sql.DataFrame)org.apache.spark.sql.DataFrame
    recommendToUser: (model: org.apache.spark.ml.recommendation.ALSModel, userID: org.apache.spark.sql.DataFrame, n: Int, training_set: org.apache.spark.sql.DataFrame, artist_names: org.apache.spark.sql.DataFrame)org.apache.spark.sql.DataFrame

</div>

</div>

<div class="cell markdown">

Let's generate some recommenations for a user.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
println("Listening history:")
// Print top 5 artists for userID 302
val sub_data = userHistory(302, 5, training_set, artist_names)

// Generate top 5 recommendations of undiscovered artists,
val recommendations = recommendToUser(model_final, sub_data, 5, training_set, artist_names)
println("Recommendations:")
recommendations.show(5)
```

<div class="output execute_result plain_result" execution_count="1">

    Listening history:
    +------+--------+--------------+
    |userID|artistID|          name|
    +------+--------+--------------+
    |   302|      55| Kylie Minogue|
    |   302|      89|     Lady Gaga|
    |   302|     265|   Céline Dion|
    |   302|     288|       Rihanna|
    |   302|     299|Jennifer Lopez|
    +------+--------+--------------+
    only showing top 5 rows

    Recommendations:
    +------+--------+------------------+----------+
    |userID|artistID|              name|    rating|
    +------+--------+------------------+----------+
    |   302|     289|    Britney Spears|0.88509434|
    |   302|     292|Christina Aguilera| 0.8353728|
    |   302|     300|        Katy Perry| 0.7862937|
    |   302|      67|           Madonna| 0.7838166|
    |   302|     295|           Beyoncé|0.76221865|
    +------+--------+------------------+----------+
    only showing top 5 rows

    sub_data: org.apache.spark.sql.DataFrame = [userID: int, artistID: int ... 1 more field]
    recommendations: org.apache.spark.sql.DataFrame = [userID: int, artistID: int ... 2 more fields]

</div>

</div>
