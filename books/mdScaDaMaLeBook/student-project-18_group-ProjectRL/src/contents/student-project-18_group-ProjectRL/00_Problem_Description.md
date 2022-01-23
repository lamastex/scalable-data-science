<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Recommender System
==================

**Project members**: - Ines De Miranda De Matos Lourenço - Yassir Jedra - Filippo Vannella

Introduction
------------

In this project, we develop and analyse a Recommendation System.

We guide ourselves by the file 036*ALS*MovieRecommender, which provides an introduction to recommendation systems.

The goal of recommendation systems is to predict the preferences of users based on their similarity to other users.

</div>

<div class="cell markdown">

Problem description
-------------------

The recommender problem is the following: we have N users and M movies, and a rating matrix R where each component \\(r_{ij}\\) corresponds to the rating given by user i to the movie j. The problem is that some components of R are missing, meaning that not all users have rated all movies, and the objective is to estimate these ratings to know which movies to recommend to the users.

To do this we will use a method called Alternating Least Squares (ALS), which estimates a Ratings matrix based on a User and a Movies matrices.

</div>

<div class="cell markdown">

Contents and contributions
--------------------------

We divide the work in the following parts, each of which in a different notebook:

-   00\_Problem description: In this present notebook we introduce what is a recommendation system and which problems it tries to solve. We finally present the datasets used for this project, which include the original (small) data set, and a Netflix (large) dataset.

-   01\_The solution: In the next notebook we present the theory behind Alternating Least Squares to solve recommendation systems, and the solutions for both small and large data sets.

    -   The main contribution of this part is a mathematical and algorithmic analysis to how the recommender works

-   02\_Extensions and future ideas: In the final notebook we create specific functions to improve the original receommendation system and propose future ideas.

    -   The main contributions of this part are the creation of a system that takes user info and outputs suggestions.

    -   For first time users, create an algorithm that gives the top rated movies over all users.

    -   Test the performance of the ALS algorithm for reccomendations based on movie's genres.

</div>

<div class="cell code" execution_count="1" scrolled="auto">

<div class="output execute_result html_result" execution_count="1">

<iframe 
 src="https://en.wikipedia.org/wiki/Collaborative_filtering"
 width="95%" height="450"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe>

</div>

</div>

<div class="cell markdown">

Data
----

To test the scalability of our approach we use two different datasets, that contain users' ratings to movies:

-   The original dataset stored in dbutils.fs.ls("/databricks-datasets/cs100/lab4/data-001/") and used in the original algorithm, consisting of 2999 users and 3615 movies, with a total of 487650 ratings.

-   A dataset from [kaggle](https://www.kaggle.com/netflix-inc/netflix-prize-data), used in a competition that Netflix held to improve recommendation systems. The dataset contains 480189 users and 17770 movies. Ratings are given on an integral scale from 1 to 5. The data is stored in dbutils.fs.ls("/FileStore/tables/Netflix").

</div>

<div class="cell markdown">

Link to video
-------------

https://kth.box.com/s/tyccs648wusbxcgd3nr0s24gwo5lmc1x

</div>
