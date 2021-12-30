// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC # Recommender System
// MAGIC 
// MAGIC 
// MAGIC 
// MAGIC **Project members**: 
// MAGIC - Ines De Miranda De Matos Lourenço
// MAGIC - Yassir Jedra
// MAGIC - Filippo Vannella
// MAGIC 
// MAGIC ## Introduction
// MAGIC 
// MAGIC In this project, we develop and analyse a Recommendation System.
// MAGIC 
// MAGIC We guide ourselves by the file 036_ALS_MovieRecommender, which provides an introduction to recommendation systems.
// MAGIC 
// MAGIC The goal of recommendation systems is to predict the preferences of users based on their similarity to other users.

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ## Problem description
// MAGIC 
// MAGIC The recommender problem is the following: we have N users and M movies, and a rating matrix R where each component \\(r_{ij}\\) corresponds to the rating given by user i to the movie j. The problem is that some components of R are missing, meaning that not all users have rated all movies, and the objective is to estimate these ratings to know which movies to recommend to the users. 
// MAGIC 
// MAGIC To do this we will use a method called Alternating Least Squares (ALS), which estimates a Ratings matrix based on a User and a Movies matrices.

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Contents and contributions
// MAGIC 
// MAGIC We divide the work in the following parts, each of which in a different notebook:
// MAGIC 
// MAGIC  - 00_Problem description: In this present notebook we introduce what is a recommendation system and which problems it tries to solve. We finally present the datasets used for this project, which include the original (small) data set, and a Netflix (large) dataset.
// MAGIC  
// MAGIC  - 01_The solution: In the next notebook we present the theory behind Alternating Least Squares to solve recommendation systems, and the solutions for both small and large data sets.
// MAGIC  
// MAGIC    - The main contribution of this part is a mathematical and algorithmic analysis to how the recommender works
// MAGIC     
// MAGIC  - 02_Extensions and future ideas: In the final notebook we create specific functions to improve the original receommendation system and propose future ideas.
// MAGIC  
// MAGIC     - The main contributions of this part are the creation of a system that takes user info and outputs suggestions.
// MAGIC   
// MAGIC     -  For first time users, create an algorithm that gives the top rated movies over all users.
// MAGIC   
// MAGIC     -  Test the performance of the ALS algorithm for reccomendations based on movie's genres.

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
// MAGIC 
// MAGIC ## Data
// MAGIC 
// MAGIC To test the scalability of our approach we use two different datasets, that contain users' ratings to movies:
// MAGIC 
// MAGIC  - The original dataset stored in dbutils.fs.ls("/databricks-datasets/cs100/lab4/data-001/") and used in the original algorithm, consisting of 2999 users and 3615 movies, with a total of 487650 ratings.
// MAGIC  
// MAGIC  - A dataset from [kaggle](https://www.kaggle.com/netflix-inc/netflix-prize-data), used in a competition that Netflix held to improve recommendation systems. The dataset contains 480189 users and 17770 movies. Ratings are given on an integral scale from 1 to 5. The data is stored in dbutils.fs.ls("/FileStore/tables/Netflix"). 

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ## Link to video
// MAGIC 
// MAGIC https://kth.box.com/s/tyccs648wusbxcgd3nr0s24gwo5lmc1x