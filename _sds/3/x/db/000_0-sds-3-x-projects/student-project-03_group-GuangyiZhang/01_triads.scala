// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC #Signed Triads in Social Media

// COMMAND ----------

// MAGIC %md
// MAGIC By **Guangyi Zhang** (guaz@kth.se)
// MAGIC 
// MAGIC Please click [HERE](https://drive.google.com/file/d/1TrxhdSxsU1qKk_SywKf2nUA8mUAO4CG4/view?usp=sharing) to watch the accompanying video.

// COMMAND ----------

// MAGIC %md
// MAGIC ## Introduction

// COMMAND ----------

// MAGIC %md
// MAGIC This project aims to verify the friend-foe motifs in a large-scale signed social network.
// MAGIC 
// MAGIC A signed network is a graph that contains both positive and negative links.
// MAGIC The sign of a link contains rich semantics in different appliations.
// MAGIC For example, in a social network, positive links can indicate friendly relationships, while negative ones indicate antagonistic interactions.
// MAGIC 
// MAGIC In on-line discussion sites such as Slashdot, users can tag other users as “friends” and “foes”.
// MAGIC These provide us exemplary datasets to study a online signed network.
// MAGIC In this notebook we explore the a dataset from Epinions, which contains up to 119,217 nodes, 841,200 edges, and millions of motifs.
// MAGIC Epinions is the trust network of the Epinions product review web site,
// MAGIC where users can indicate their trust or distrust of the reviews of others.
// MAGIC We analyze the network data in an undirected representation.
// MAGIC 
// MAGIC References:
// MAGIC 
// MAGIC Leskovec, Jure, Daniel Huttenlocher, and Jon Kleinberg. "Signed networks in social media." Proceedings of the SIGCHI conference on human factors in computing systems. 2010.

// COMMAND ----------

// MAGIC %md
// MAGIC Regarding the motifs, we investigate several interesting triads that are related to *structural balance theory* in an online social signed network.
// MAGIC Structural balance originates in social psychology in the mid-20th-century, and considers the possible ways in which triangles on three individuals can be signed.
// MAGIC 
// MAGIC Let us explain different types of triads, which is shown in the figure below, 
// MAGIC 
// MAGIC - T3: “the friend of my friend is my friend” 
// MAGIC - T1: “the friend of my enemy is my enemy,” “the enemy of my friend is my enemy” and “the enemy of my enemy is my friend”
// MAGIC - T2 and T0: does not quite make sense in social network. For example, two friends of mine are unlikely to be enemy to each other.
// MAGIC 
// MAGIC Our goal is to compare the numbers of different triads in our appointed dataset.
// MAGIC 
// MAGIC 
// MAGIC ![triads](https://drive.google.com/uc?export=view&id=1QY9ouqxbVqpH3KLl-x-QyR72yxtE0vSX)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Download dataset

// COMMAND ----------

// MAGIC %sh
// MAGIC pwd

// COMMAND ----------

// MAGIC %sh
// MAGIC wget http://snap.stanford.edu/data/soc-sign-epinions.txt.gz

// COMMAND ----------

// MAGIC %sh
// MAGIC ls -l

// COMMAND ----------

// MAGIC %sh
// MAGIC gunzip soc-sign-epinions.txt.gz

// COMMAND ----------

// MAGIC %sh
// MAGIC ls -l

// COMMAND ----------

// MAGIC %sh
// MAGIC head soc-sign-epinions.txt

// COMMAND ----------

// MAGIC %sh
// MAGIC mkdir -p epinions
// MAGIC mv soc-sign-epinions.txt epinions/

// COMMAND ----------

// MAGIC %sh
// MAGIC ls -l /dbfs/FileStore
// MAGIC mv epinions /dbfs/FileStore/

// COMMAND ----------

// MAGIC %fs ls /

// COMMAND ----------

// MAGIC %fs ls /FileStore

// COMMAND ----------

// MAGIC %fs ls file:/databricks/driver

// COMMAND ----------

//%fs mv file:///databricks/driver/epinions /FileStore/
%fs ls /FileStore/epinions/

// COMMAND ----------

// MAGIC %md
// MAGIC ## Preprocess dataset

// COMMAND ----------

import org.apache.spark.sql._
import org.apache.spark.sql.functions._

import org.graphframes._

// This import is needed to use the $-notation
import spark.implicits._

// COMMAND ----------

var df = spark.read.format("csv")
//   .option("header", "true")
  .option("inferSchema", "true")
  .option("comment", "#")
  .option("sep", "\t")
  .load("/FileStore/epinions")

// COMMAND ----------

df.count()

// COMMAND ----------

df.rdd.getNumPartitions 

// COMMAND ----------

df.head(3)

// COMMAND ----------

df.printSchema()

// COMMAND ----------

val newNames = Seq("src", "dst", "rela")
val e = df.toDF(newNames: _*)

// COMMAND ----------

e.printSchema()

// COMMAND ----------

// Vertex DataFrame
val v = spark.range(1, 131827).toDF("id")

// COMMAND ----------

val g = GraphFrame(v, e)

// COMMAND ----------

g.edges.take(3)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Count triads

// COMMAND ----------

// val results = g.triangleCount.run()

// COMMAND ----------

// MAGIC %md
// MAGIC We can not make use of the convenient API `triangleCount()` because it does not take the sign of edges into consideration.
// MAGIC We need to write our own code to find triads.

// COMMAND ----------

// MAGIC %md
// MAGIC First, a triad should be undirected, but our graph concists of only directed edges.
// MAGIC 
// MAGIC One strategy is to keep only bi-direction edges of the same sign.
// MAGIC But we need to examine how large is the proportion of edges we will lose.

// COMMAND ----------

// Search for pairs of vertices with edges in both directions between them, i.e., find undirected or bidirected edges.
val pair = g.find("(a)-[e1]->(b); (b)-[e2]->(a)")
println(pair.count())
val filtered = pair.filter("e1.rela == e2.rela")
println(filtered.count())

// COMMAND ----------

// MAGIC %md
// MAGIC Fortunately, we only lose a very small amount of edges.
// MAGIC 
// MAGIC It also makes sense for this dataset, because if A trusts B, then it is quite unlikely that B does not trust A.

// COMMAND ----------

// MAGIC %md
// MAGIC In order to count different triads, first we have to find all triads.

// COMMAND ----------

val triad = g.find("(a)-[eab]->(b); (b)-[eba]->(a); (b)-[ebc]->(c); (c)-[ecb]->(b); (c)-[eca]->(a); (a)-[eac]->(c)")
println(triad.count())

// COMMAND ----------

// MAGIC %md
// MAGIC After finding all triads, we find each type by filtering.

// COMMAND ----------

val t111 = triad.filter("eab.rela = 1 AND eab.rela = ebc.rela AND ebc.rela = eca.rela")
println(t111.count())

// COMMAND ----------

val t000 = triad.filter("eab.rela = -1 AND eab.rela = ebc.rela AND ebc.rela = eca.rela")
println(t000.count())

// COMMAND ----------

val t110 = triad.filter("eab.rela + ebc.rela + eca.rela = 1")
println(t110.count())

// COMMAND ----------

val t001 = triad.filter("eab.rela + ebc.rela + eca.rela = -1")
println(t001.count())

// COMMAND ----------

val n111 = t111.count()
val n001 = t001.count()
val n000 = t000.count() 
val n110 = t110.count()
val imbalanced = n000 + n110
val balanced = n111 + n001

// COMMAND ----------

// MAGIC %md
// MAGIC As we can see, the number of balanced triads overwhelms the number of imbalanced ones,
// MAGIC which verifies the effectiveness of structural balance theory.

// COMMAND ----------

// MAGIC %md
// MAGIC ## Duplicates

// COMMAND ----------

// MAGIC %md
// MAGIC Some tests about duplicated motifs

// COMMAND ----------

val g: GraphFrame = examples.Graphs.friends

// COMMAND ----------

display(g.edges)

// COMMAND ----------

val motifs = g.find("(a)-[e]->(b); (b)-[e2]->(a)")
motifs.show()

// COMMAND ----------

// MAGIC %md
// MAGIC As shown above, bi-direction edges are reported twice.
// MAGIC Therefore, each triad is counted three times.
// MAGIC However, this does not matter in our project, because the ratios between different triads remain the same.

// COMMAND ----------

