// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/jpRpd8VlMYs/0.jpg)](https://www.youtube.com/embed/jpRpd8VlMYs?start=1713&end=3826&autoplay=1)

// COMMAND ----------

//imports
import org.apache.spark.sql.types.{StructType, StructField, StringType};
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame,Row}
import org.apache.spark.sql.types._
import sqlContext.implicits
import org.apache.spark.graphx._
import org.apache.spark._
import math._
import org.apache.spark.mllib.rdd.RDDFunctions._

// COMMAND ----------

// MAGIC %md
// MAGIC # Load data

// COMMAND ----------

// just read from dbfs
val TTTsDF = sqlContext.read.parquet("/datasets/MEP/GB/TTTsDFAsParquetDF")
val miniTTTsDF = spark.createDataFrame(TTTsDF.rdd, TTTsDF.schema).select("CurrentTweetDate","CurrentTwID","CPostUserID","CPostUserSN","OPostUserIDinRT","OPostUserSNinRT", "OPostUserIDinQT", "OPostUserSNinQT", "OPostUserIDinReply", "OPostUserSNinReply", "URLs", "hashTags", "UMentionAsID", "UMentionAsSN", "TweetType", "CurrentTweet", "Weight")

// COMMAND ----------

val augmentedTweetsTTTDF =  sqlContext.read.parquet("dbfs:/datasets/MEP/GB/AugmentedTTTsDFAsParquet/")
val augmentedTweetsTTTfiltered = augmentedTweetsTTTDF.filter($"CurrentTweetDate" >= "2017-05-01T00" && $"CurrentTweetDate" <= "2017-06-30T00")

// COMMAND ----------

augmentedTweetsTTTfiltered.count()

// COMMAND ----------

val unionTTTsDF = TTTsDF.union(augmentedTweetsTTTDF)  // merge retrospective augmented data with the original tweets

// COMMAND ----------


val TTTDF = spark.createDataFrame(unionTTTsDF.rdd, unionTTTsDF.schema).select("CurrentTweetDate","CurrentTwID","CPostUserID","CPostUserSN","OPostUserIDinRT","OPostUserSNinRT", "OPostUserIDinQT", "OPostUserSNinQT", "OPostUserIDinReply", "OPostUserSNinReply", "URLs", "hashTags", "UMentionAsID", "UMentionAsSN", "TweetType", "CurrentTweet", "Weight").filter($"CurrentTweetDate" >= "2017-05-01T00" && $"CurrentTweetDate" <= "2017-06-30T00")  

// COMMAND ----------

TTTDF.coalesce(40)
TTTDF.count()

// COMMAND ----------

// MAGIC %md
// MAGIC # Extract the retweet network

// COMMAND ----------

import org.apache.spark.sql.functions.explode_outer

// Retweet Network containing URLs
val retweetNetwork = TTTDF.withColumn("URL", explode_outer($"URLs"))  
  .na.fill("",Seq("URL"))
  .filter($"TweetType"==="ReTweet")
  .select("OPostUserIDinRT", "OPostUserSNinRT", "CPostUserID", "CPostUserSN", "URL", "Weight")

// COMMAND ----------

/*
* Map distance
*/
def mapDistances(x: Integer): Integer = {
  if (x <= 5) {
    return x
  }
  //else if (x <= 6) {
  //  return 4
  //}
  else {
    return 6
  }
}

/*
/ Makes the weighted network of retweets, and computes the geometric probabilities.
*/
def makeWeightedNetwork(allRetweetsSrcIdDstId: DataFrame, srcColName: String, dstColName: String, weightColName: String): DataFrame = {
  allRetweetsSrcIdDstId.withColumn("w",lit(1.0))
    .groupBy(srcColName,dstColName)
    .agg(sum("w").as(weightColName))
    .select(srcColName,dstColName,weightColName)
    .withColumn("one",lit(1.0))
    .withColumn("GeomProb",$"one"/($"one"+col(weightColName))).drop("one")
}

import scala.reflect.ClassTag
import scala.util.Random
import org.apache.spark.graphx._

/*
/ Shortest path Sequence from source vertex to every other vertex
/ By: Ivan Sadikov
*/
object Dijkstra extends Serializable {
  import org.apache.spark.graphx._

  type VertexId = Long
  // vertex type contains tracking vertex and state (path, parent)
  type VertexType = (Double, List[(VertexId, Double)])
  // null value for vertex that does not have any parent (start vertex)
  private val NULL_PARENT = -1L
  // initial and infinity values, use to relax edges
  private val INITIAL = 0.0
  private val INFINITY = Int.MaxValue.toDouble
  
  def run(graph: Graph[VertexId, Double], start: VertexId): Graph[VertexType, Double] = {
    val spGraph = graph.mapVertices { (vid, attr) =>
      if (start == vid) (INITIAL, List[(VertexId, Double)]()) else (INFINITY, List[(VertexId, Double)]())
    }

    val initialMessage = (INFINITY, List[(VertexId,Double)]())

    def vertexProgram(id: VertexId, dst0: VertexType, dst1: VertexType): VertexType = {
      select(dst0, dst1)
    }

    def sendMessage(edge: EdgeTriplet[VertexType, Double]): Iterator[(VertexId, VertexType)] = {
      val weight = edge.attr
      if (edge.srcAttr._1 + weight < edge.dstAttr._1) {
        //Iterator((edge.dstId, (edge.srcAttr._1 + weight, (edge.srcId, edge.srcAttr._1) +: edge.srcAttr._2)))
        Iterator((edge.dstId, (edge.srcAttr._1 + weight, (edge.srcId, weight) +: edge.srcAttr._2)))
      } else {
        Iterator.empty
      }
    }
  
    def select(dst0: VertexType, dst1: VertexType): VertexType = {
      if (dst0._1 > dst1._1) dst1 else dst0
    }

    Pregel(spGraph, initialMessage)(vertexProgram, sendMessage, select)
  }
}

def runDijkstraOnce(nodeIdColName: String, g: Graph[Long, Double], srcNodeId: Long, srcNodeName: String): DataFrame = {
  
  val result = Dijkstra.run(g, srcNodeId)
  // note that for each unreachable vertex with shortest distance to souce vertex = 2.147483647E9, 
  // the path is an empty list and x._2._2.map(d => (1.0 - d._2)/d._2 ).sum = the sum of this empty list is 0.0, 
  // and 0.0 id the right Expectation of the number of Geometricaly distributed Retweet events between the source vertex and the unreachable vertex
  //val df = result.vertices.map(x =>  (x._1, x._2._1, x._2._2.map(d => (1.0 - d._2)/d._2 ).sum )).toDF(nodeIdColName,srcNodeName+"GeoProbsSum",srcNodeName+"GeomMeansSum")
  return result.vertices.map(x =>  {val maxRTPath = x._2._2.map(d => (1.0 - d._2)/d._2 )
                                      val meanMaxPath = if (maxRTPath.isEmpty) 0.0 else maxRTPath.sum/maxRTPath.length
                                      (x._1, meanMaxPath, mapDistances(maxRTPath.length)) // NOTE! here maxRTPath.length=0 means thre is NO Path!
                                      }).toDF(nodeIdColName,srcNodeName+"ESumGeom", srcNodeName+"PathLen")
}

// COMMAND ----------

// MAGIC %md 
// MAGIC #Make the graph

// COMMAND ----------

val gF = makeWeightedNetwork(retweetNetwork,"OPostUserIDinRT","CPostUserID","Weight")
// Need to add srcVid, dstVid and dist
// directed - need to switch src and dst because in shortest path algorithm we want distance from each vertex to each landmark vertex which are the original/source tweets of the retweet
val eDF = gF.select($"OPostUserIDinRT".as("srcVid"), $"CPostUserID".as("dstVid"), $"GeomProb")
val ex = eDF.rdd.map{ case Row(id1: Long, id2: Long, w: Double) => Edge(id1, id2, w) } //.rdd.map(row => Edge(row.getLong(0), row.getLong(1), row.getDouble(2))) // convert DF into rdd
val exr = eDF.rdd.map{ case Row(id1: Long, id2: Long, w: Double) => Edge(id2, id1, w) }
val vDF = eDF.select("srcVid").union(eDF.select("dstVid")).distinct().withColumnRenamed("srcVid","id")
val vx = vDF.rdd.map(row => (row.getLong(0),row.getLong(0)))
// A graph with edge attributes containing distances
val graph = Graph(vx, ex).cache()
val graphr = Graph(vx, exr)  // reversed edges

// COMMAND ----------

// MAGIC %md
// MAGIC # Check that the network is mainly one connected component

// COMMAND ----------

val ccV = graph.connectedComponents().vertices
val ccIDs = ccV.map {
  case (a) => a._2
}.toDF("CCId").withColumn("w",lit(1.0)).groupBy("CCId").sum("w")

// COMMAND ----------

display(ccIDs.orderBy($"sum(w)".desc))  // just one main component

// COMMAND ----------

// MAGIC %md
// MAGIC # Define landmarks
// MAGIC Find a way to define a good set of landmarks. One way could be data driven by chosing the top nodes by some centrality measure, e.g. out degree, out neighbourhood or pagerank. Or domain expertise the landmarks can by chosen manually. 

// COMMAND ----------

val landmarkVertices = Array((117777690L,"jeremycorbyn"), (747807250819981312L,	"theresa_may"), (222748037L,"AngelaRayner"), (3131144855L,"BorisJohnson"), (16973333L,	"Independent"), (16343974L, "Telegraph"), (14157134L, "Peston"), (216299334L,"piersmorgan"), (60886384L,"Kevin_Maguire"), (65045121L,"OwenJones84"), (19811190L, "paulmasonnews"), (19346439L, "LouiseMensch"), (465973L,"GuidoFawkes"))

// COMMAND ----------

val landmarkOutNghbd = retweetNetwork.select("OPostUSerIDinRT", "OPostUserSNinRT", "CPostUSerID", "Weight").distinct().groupBy("OPostUSerIDinRT", "OPostUserSNinRT").agg(sum($"Weight").as("OutNghbd")).toDF("Id", "SN", "OutNghbd").alias("landmarkOutNghbd")
val landmarkInNghbd = retweetNetwork.select("OPostUSerIDinRT", "CPostUserSN", "CPostUSerID", "Weight").distinct().groupBy("CPostUserID", "CPostUserSN").agg(sum($"Weight").as("InNghbd")).toDF("Id", "SN", "InNghbd").alias("landmarkInNghbd")
val landmarkOutDegree = retweetNetwork.groupBy("OPostUSerIDinRT", "OPostUserSNinRT").agg(sum($"Weight").as("OutDegree")).toDF("Id", "SN", "OutDegree").alias("landmarkOutDegree")
val landmarkInDegree = retweetNetwork.groupBy("CPostUserId", "CPostUserSN").agg(sum($"Weight").as("InDegree")).toDF("Id", "SN", "InDegree").alias("landmarkInDegree")

// COMMAND ----------

// MAGIC %md
// MAGIC Display popular vertices

// COMMAND ----------

display(landmarkOutDegree.join(landmarkOutNghbd.drop("SN"), Seq("ID")).join(landmarkInDegree.drop("SN"), Seq("ID"), joinType="outer").join(landmarkInNghbd.drop("SN"), Seq("ID"), joinType="outer").orderBy($"OutDegree".desc))

// COMMAND ----------

// MAGIC %md
// MAGIC Display popular vertices of some chategory of accounts, e.g. newspapers or politicians.

// COMMAND ----------

val allTracked = sc.textFile("/FileStore/tables/gj2ee8j11500983679360/ids_with_labels2-7c031.csv")
  .map(x => x.split(","))
  .map(x => (x(0).toLong, x(1).toString, x(2).toString, x(3).toString))
  .toDF(Seq("UserId", "UserSN", "RealName", "Affiliation"): _*)

// COMMAND ----------

display(retweetNetwork.select("OPostUserSNinRT", "OPostUserIDinRT").distinct().toDF("SN", "Id")
  .join(allTracked.filter($"Affiliation" === "Newspaper"), $"UserId" === $"Id")
  //.join(landmarkPageRanks.toDF(Seq("Id" ,"PageRank"): _*), "Id")
  .join(landmarkOutNghbd.drop("SN"), "Id")
  //.join(landmarkInNghbd.drop("SN"), "Id")
  //.join(landmarkOutDegree.drop("SN"), "Id")
  //.join(landmarkInDegree.drop("SN"), "Id")
  .orderBy($"OutNghbd".desc))

// COMMAND ----------

display(retweetNetwork.select("OPostUserSNinRT", "OPostUserIDinRT").distinct().toDF("SN", "Id")
  .join(allTracked.filter($"Affiliation" =!= "Newspaper").filter($"Affiliation" =!= "Other"), $"UserId" === $"Id")
  //.join(landmarkPageRanks.toDF(Seq("Id" ,"PageRank"): _*), "Id")
  .join(landmarkOutNghbd, "Id")
  //.join(landmarkInNghbd, "Id")
  //.join(landmarkOutDegree, "Id")
  //.join(landmarkInDegree, "Id")
  .orderBy($"OutNghbd".desc))

// COMMAND ----------

val totalNumberOfRetweeters = retweetNetwork.select("CPostUserId").distinct().count() // 776801

// COMMAND ----------

// MAGIC %md
// MAGIC # Find the shortest paths from each landmark to every other node
// MAGIC This is done using Dijkstra's algorithm, where the weights of each edge is inversely proportional to the number of retweets. 

// COMMAND ----------

// Run Dijkstra on the graph for all the landmarks
var df = vDF //.drop("srcVid")
for (landmark <- landmarkVertices) {
//for (landmark <-   Array(landmarkVertices(0),landmarkVertices(6)) ){
  val temp = runDijkstraOnce("id", graph, landmark._1, landmark._2)
  df = df.join(temp, "id")
  df.cache()
  df.count()
}


// COMMAND ----------

df.cache()
df.count() // 790811 //1038382

// COMMAND ----------

import org.apache.spark.sql.functions.col
import scala.collection.mutable.ArrayBuffer
val temp = ArrayBuffer[String]()
for (landmark <- landmarkVertices) {
  temp += landmark._2+"PathLen"
}
val pathLens = temp.toSeq  // the column names corresponding to path lengths

val temp2 = ArrayBuffer[String]()
for (landmark <- landmarkVertices) {
  temp2 += landmark._2+"ESumGeom"
}
val sumGeoms = temp2.toSeq  // the column names corresponding to sum geoms 

// COMMAND ----------

// Filter out a bunch of stuff:
val minRT=1/(1+1.0)  // only keep users who have retweeted at least one of the landmarks 2 or more times.

val df1 = df  // couldn't come up wiht a smarter way to do this... 
 .filter((col(sumGeoms(0))>minRT||col(sumGeoms(1))>minRT||col(sumGeoms(2))>minRT||col(sumGeoms(3))>minRT||col(sumGeoms(4))>minRT||col(sumGeoms(5))>minRT||col(sumGeoms(6))>minRT||col(sumGeoms(7))>minRT||col(sumGeoms(8))>minRT||col(sumGeoms(9))>minRT)||col(sumGeoms(10))>minRT||col(sumGeoms(11))>minRT||col(sumGeoms(12))>minRT)//||col(sumGeoms(13))>minRT||col(sumGeoms(14))>minRT||col(sumGeoms(15))>minRT||col(sumGeoms(16))>minRT))//||col(sumGeoms(17))>minRT||col(sumGeoms(18))>minRT)) 
//&& 

// COMMAND ----------

val df2 = df1.select(pathLens.head, pathLens.tail: _*).withColumn("count",lit(1.0)).groupBy(pathLens.map(col(_)): _*).agg(sum($"count")).orderBy($"sum(count)".desc)  // Aggregate users with the same profiles. 

// COMMAND ----------

// We want to groupBy and apply different agg. operations to different columns. Find the average of "...sumGeoms" and sum "counts".
// don't need this anymore, but it is clever so I'll keep it here if I need some other time. 
val exprs = (sumGeoms.map((_ -> "mean")) ++ Seq("count").map((_ -> "sum"))).toMap
// val df2 = df1.withColumn("count",lit(1.0)).groupBy(pathLens.map(col(_)): _*).agg(exprs)

// COMMAND ----------

// Dataframe-ified zipWithIndex
def dfZipWithIndex(
  df: DataFrame,
  offset: Int = 1,
  colName: String = "id",
  inFront: Boolean = true
) : DataFrame = {
  df.sqlContext.createDataFrame(
    df.rdd.zipWithIndex.map(ln =>
      Row.fromSeq(
        (if (inFront) Seq(ln._2 + offset) else Seq())
          ++ ln._1.toSeq ++
        (if (inFront) Seq() else Seq(ln._2 + offset))
      )
    ),
    StructType(
      (if (inFront) Array(StructField(colName,LongType,false)) else Array[StructField]()) 
        ++ df.schema.fields ++ 
      (if (inFront) Array[StructField]() else Array(StructField(colName,LongType,false)))
    )
  ) 
}

// COMMAND ----------

import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window

val columnNames = pathLens ++ Seq("sum(count)")
val sumFreqs =  df2.agg(sum("sum(count)")).first.getDouble(0)

val pathLengthFeatures = dfZipWithIndex(
  df2.select(columnNames.head, columnNames.tail: _*)
  .withColumn("percentage", $"sum(count)"/sumFreqs * 100.0)
  .withColumn("cs", sum($"percentage").over(Window.orderBy($"percentage".desc).rowsBetween(Long.MinValue, 0)))
  .select((Seq("percentage", "cs") ++ columnNames).head, (Seq("percentage", "cs") ++ columnNames).tail: _*))
//  .cache() takes about 37 minutes with cache

// COMMAND ----------

pathLengthFeatures.count()

// COMMAND ----------

display(pathLengthFeatures.filter($"theresa_mayPathLen"===1).agg(sum("sum(count)")))

// COMMAND ----------

display(pathLengthFeatures)  // this is the df which is used to compute the neighbourjoining-tree

// COMMAND ----------

// Copy-Paste into Matlab! 
print("[")
df2.select(pathLens.head, pathLens.tail: _*).take(10).foreach(x => println(x.toString + ";..."))
print("];")

// COMMAND ----------



// COMMAND ----------

// MAGIC %md
// MAGIC # Communication in the neighbourjoing tree
// MAGIC Now we find the frequency of retweets across different branches in the trre

// COMMAND ----------

import spark.implicits._
val profiles = pathLengthFeatures.select(pathLens.head, pathLens.tail: _*).rdd.map{r => 
      val array = r.toSeq.toArray 
      array.map(_.asInstanceOf[Integer]).map(_.toDouble) }.take(100)  // first we get the number of profiles we want to have in our tree.

/*
/ Find all ids with the profile. Return them in a df.
*/
def filterIdsWithProfile(df: DataFrame, pathLens: Seq[String], profile: Array[Integer], label: Integer): DataFrame = {
  val columnNames = pathLens ++ Seq("id")
  return df.select(columnNames.head, columnNames.tail: _*)
 .filter(col(pathLens(0))===profile(0)&&col(pathLens(1))===profile(1)&&col(pathLens(2))===profile(2)&&col(pathLens(3))===profile(3)&&col(pathLens(4))===profile(4)&&col(pathLens(5))===profile(5))
 .select("id").withColumn("label", lit(label))
}

// COMMAND ----------

// TODO: assign all users to a profile. Say we take the 100 most common profiles, then each user with a different profile form the 100 first must be assign to one of the 100. This can be done using k-mean for example. 

// COMMAND ----------

profiles.map(Vectors.dense(_))

// COMMAND ----------

Array("[0.6,  0.6]", "[8.0,  8.0]").map(Vectors.parse(_))

// COMMAND ----------

import org.apache.spark.mllib.clustering.{KMeans, KMeansModel}
//import org.apache.spark.ml.clustering.KMeans
//import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.mllib.linalg.Vectors


val initialModel = new KMeansModel(
   profiles.map(Vectors.dense(_))
  //.map(Vectors.parse(_))
)

val kmeans = new KMeans()
  .setK(10)
  .setInitialModel(initialModel)



// COMMAND ----------

df1.count()

// COMMAND ----------

val idPointRDD = df1.select(columnNames.head, columnNames.tail: _*).rdd.map(s => (s.getLong(0), Vectors.dense(s.getInt(1).toDouble,s.getInt(2).toDouble,s.getInt(3).toDouble,s.getInt(4).toDouble,s.getInt(5).toDouble,s.getInt(6).toDouble,s.getInt(7).toDouble,s.getInt(8).toDouble,s.getInt(9).toDouble,s.getInt(10).toDouble,s.getInt(11).toDouble,s.getInt(12).toDouble,s.getInt(13).toDouble))).cache()
val clusters = kmeans.run(idPointRDD.map(_._2))
val clustersRDD = clusters.predict(idPointRDD.map(_._2))
val idClusterRDD = idPointRDD.map(_._1).zip(clustersRDD)

// COMMAND ----------

val idCluster = idClusterRDD.toDF("id", "cluster").cache()

// COMMAND ----------

val tm = df1.filter(col(pathLens(0))===0&&col(pathLens(1))===1&&col(pathLens(2))===0&&col(pathLens(3))===0&&col(pathLens(4))===0&&col(pathLens(5))===0&&col(pathLens(6))===0&&col(pathLens(7))===0&&col(pathLens(8))===0&&col(pathLens(9))===0&&col(pathLens(10))===0&&col(pathLens(11))===0&&col(pathLens(12))===0)

// COMMAND ----------

tm.count() //11572

// COMMAND ----------

val tmNetwork = retweetNetwork.join(tm, $"CPostUserID"===$"id")

// COMMAND ----------

tmNetwork.count()

// COMMAND ----------

display(tmNetwork.groupBy("OPostUserSNinRT").agg(sum("Weight").as("sum")).orderBy($"sum".desc)) 

// COMMAND ----------



// COMMAND ----------

println(df1.count())  //1083714 
println(idCluster.count())
//736034
//736034

// COMMAND ----------

val clusterTotal = idCluster.select("cluster").withColumn("w", lit(1)).groupBy("cluster").agg(sum("w").as("total"))

// COMMAND ----------

df1.count()

// COMMAND ----------

display(clusterTotal.agg(sum("total")))

// COMMAND ----------

import org.apache.spark.ml.feature.Interaction
import org.apache.spark.ml.feature.VectorAssembler
val columnNames = Seq("Id") ++ pathLens
val assembler1 = new VectorAssembler().
  setInputCols(pathLens.toArray).
  setOutputCol("features")
val assembled1 = assembler1.transform(df1.select(columnNames.head, columnNames.tail: _*)).select("id", "features")
display(assembled1)

// COMMAND ----------

display(eDF.join(idCluster, $"Id" === $"SrcVid"))

// COMMAND ----------

object TupleUDFs {
  import org.apache.spark.sql.functions.udf      
  // type tag is required, as we have a generic udf
  import scala.reflect.runtime.universe.{TypeTag, typeTag}

  def toTuple2[S: TypeTag, T: TypeTag] = 
    udf[(S, T), S, T]((x: S, y: T) => (x, y))
}

// COMMAND ----------

val edges = eDF.select("srcVid", "dstVid").join(idCluster.toDF("id","srcCluster"), $"srcVid"===$"id").drop("id").join(idCluster.toDF("id", "dstCluster"), $"dstVid"===$"id").drop("id").withColumn(
  "attr", TupleUDFs.toTuple2[Int, Int].apply($"srcCluster", $"dstCluster")
)

// COMMAND ----------

display(edges)

// COMMAND ----------

val res = ArrayBuffer[(Int,Int,Long)]()
for (i <- Range(0, 25); j <- Range(i, 25)) {
  var count = edges.filter($"srcCluster"===i).filter($"dstCluster"===j).count()
  println((i,j)+","+count);
  res += ((i,j, count))
}

// COMMAND ----------

// Now we loop through all subgraphs with respect to each cluster index. 
val connectionsInSubgraph = collection.mutable.Map[Int, Long]()
for (i <- Range(0,3)) {
  for (j <- Range(0,3)) {
    
  }
  val subgraph = graph.subgraph(epred = (id, attr) => attr._2 == i)
  connectionsInSubgraph(i) = subgraph.edges.count()
}

// COMMAND ----------

// Now we loop through all subgraphs with respect to each cluster index. 
val connectionsInSubgraph = collection.mutable.Map[Int, Long]()
for (i <- Range(0,3)) {
  val subgraph = graph.subgraph(vpred = (id, attr) => attr._2 == i)
  connectionsInSubgraph(i) = subgraph.edges.count()
}

// COMMAND ----------

// MAGIC %md
// MAGIC # URLS

// COMMAND ----------

val allUrlAndDomains = (sc.textFile("/FileStore/tables/7yaczwjd1501068230338/checkedUrlWithNetLocFinal.csv")
  .map(x => x.split(","))
  .map(x => (x(0).toString, x(1).toString))
  .toDF(Seq("URL", "Domain"): _*))

// COMMAND ----------

allUrlAndDomains.count

// COMMAND ----------

val urlNetwork = retweetNetwork.join(allUrlAndDomains, Seq("URL"), "outer")

// COMMAND ----------

display(urlNetwork)

// COMMAND ----------

urlNetwork.count()

// COMMAND ----------

urlNetwork.filter($"URL"==="").count()

// COMMAND ----------

6382207-5344405 // number of tweets with URLs

// COMMAND ----------

urlNetwork.select("URL").distinct().count()

// COMMAND ----------

urlNetwork.select("URL","Domain").distinct().count()

// COMMAND ----------

dbutils.fs.rm("/datasets/MEP/GB/RetweetUrlNetworkAsParquetDF",true)

// COMMAND ----------

// write this urlNetwork to parquet for processing later
urlNetwork.write.parquet("/datasets/MEP/GB/RetweetUrlNetworkAsParquetDF") 