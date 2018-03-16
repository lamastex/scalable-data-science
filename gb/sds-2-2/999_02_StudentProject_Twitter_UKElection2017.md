[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

Archived YouTube video of this live unedited lab-lecture:

[![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/jpRpd8VlMYs/0.jpg)](https://www.youtube.com/embed/jpRpd8VlMYs?start=1713&end=3826&autoplay=1)

``` scala
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
```

>     import org.apache.spark.sql.types.{StructType, StructField, StringType}
>     import org.apache.spark.sql.functions._
>     import org.apache.spark.sql.{DataFrame, Row}
>     import org.apache.spark.sql.types._
>     import sqlContext.implicits
>     import org.apache.spark.graphx._
>     import org.apache.spark._
>     import math._
>     import org.apache.spark.mllib.rdd.RDDFunctions._

Load data
=========

``` scala
// just read from dbfs
val TTTsDF = sqlContext.read.parquet("/datasets/MEP/GB/TTTsDFAsParquetDF")
val miniTTTsDF = spark.createDataFrame(TTTsDF.rdd, TTTsDF.schema).select("CurrentTweetDate","CurrentTwID","CPostUserID","CPostUserSN","OPostUserIDinRT","OPostUserSNinRT", "OPostUserIDinQT", "OPostUserSNinQT", "OPostUserIDinReply", "OPostUserSNinReply", "URLs", "hashTags", "UMentionAsID", "UMentionAsSN", "TweetType", "CurrentTweet", "Weight")
```

>     TTTsDF: org.apache.spark.sql.DataFrame = [CurrentTweetDate: timestamp, CurrentTwID: bigint ... 34 more fields]
>     miniTTTsDF: org.apache.spark.sql.DataFrame = [CurrentTweetDate: timestamp, CurrentTwID: bigint ... 15 more fields]

``` scala
val augmentedTweetsTTTDF =  sqlContext.read.parquet("dbfs:/datasets/MEP/GB/AugmentedTTTsDFAsParquet/")
val augmentedTweetsTTTfiltered = augmentedTweetsTTTDF.filter($"CurrentTweetDate" >= "2017-05-01T00" && $"CurrentTweetDate" <= "2017-06-30T00")
```

>     augmentedTweetsTTTDF: org.apache.spark.sql.DataFrame = [CurrentTweetDate: timestamp, CurrentTwID: bigint ... 34 more fields]
>     augmentedTweetsTTTfiltered: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [CurrentTweetDate: timestamp, CurrentTwID: bigint ... 34 more fields]

``` scala
augmentedTweetsTTTfiltered.count()
```

>     res1: Long = 5248765

``` scala
val unionTTTsDF = TTTsDF.union(augmentedTweetsTTTDF)  // merge retrospective augmented data with the original tweets
```

>     unionTTTsDF: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [CurrentTweetDate: timestamp, CurrentTwID: bigint ... 34 more fields]

``` scala

val TTTDF = spark.createDataFrame(unionTTTsDF.rdd, unionTTTsDF.schema).select("CurrentTweetDate","CurrentTwID","CPostUserID","CPostUserSN","OPostUserIDinRT","OPostUserSNinRT", "OPostUserIDinQT", "OPostUserSNinQT", "OPostUserIDinReply", "OPostUserSNinReply", "URLs", "hashTags", "UMentionAsID", "UMentionAsSN", "TweetType", "CurrentTweet", "Weight").filter($"CurrentTweetDate" >= "2017-05-01T00" && $"CurrentTweetDate" <= "2017-06-30T00")  
```

>     TTTDF: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [CurrentTweetDate: timestamp, CurrentTwID: bigint ... 15 more fields]

``` scala
TTTDF.coalesce(40)
TTTDF.count()
```

>     res2: Long = 11084903

Extract the retweet network
===========================

``` scala
import org.apache.spark.sql.functions.explode_outer

// Retweet Network containing URLs
val retweetNetwork = TTTDF.withColumn("URL", explode_outer($"URLs"))  
  .na.fill("",Seq("URL"))
  .filter($"TweetType"==="ReTweet")
  .select("OPostUserIDinRT", "OPostUserSNinRT", "CPostUserID", "CPostUserSN", "URL", "Weight")
```

>     import org.apache.spark.sql.functions.explode_outer
>     retweetNetwork: org.apache.spark.sql.DataFrame = [OPostUserIDinRT: bigint, OPostUserSNinRT: string ... 4 more fields]

``` scala
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
```

>     mapDistances: (x: Integer)Integer
>     makeWeightedNetwork: (allRetweetsSrcIdDstId: org.apache.spark.sql.DataFrame, srcColName: String, dstColName: String, weightColName: String)org.apache.spark.sql.DataFrame
>     import scala.reflect.ClassTag
>     import scala.util.Random
>     import org.apache.spark.graphx._
>     defined object Dijkstra
>     runDijkstraOnce: (nodeIdColName: String, g: org.apache.spark.graphx.Graph[Long,Double], srcNodeId: Long, srcNodeName: String)org.apache.spark.sql.DataFrame

Make the graph
==============

``` scala
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
```

>     gF: org.apache.spark.sql.DataFrame = [OPostUserIDinRT: bigint, CPostUserID: bigint ... 2 more fields]
>     eDF: org.apache.spark.sql.DataFrame = [srcVid: bigint, dstVid: bigint ... 1 more field]
>     ex: org.apache.spark.rdd.RDD[org.apache.spark.graphx.Edge[Double]] = MapPartitionsRDD[48785] at map at <console>:67
>     exr: org.apache.spark.rdd.RDD[org.apache.spark.graphx.Edge[Double]] = MapPartitionsRDD[48786] at map at <console>:68
>     vDF: org.apache.spark.sql.DataFrame = [id: bigint]
>     vx: org.apache.spark.rdd.RDD[(Long, Long)] = MapPartitionsRDD[48798] at map at <console>:70
>     graph: org.apache.spark.graphx.Graph[Long,Double] = org.apache.spark.graphx.impl.GraphImpl@7d39c1a0
>     graphr: org.apache.spark.graphx.Graph[Long,Double] = org.apache.spark.graphx.impl.GraphImpl@54e5bb09

Check that the network is mainly one connected component
========================================================

``` scala
val ccV = graph.connectedComponents().vertices
val ccIDs = ccV.map {
  case (a) => a._2
}.toDF("CCId").withColumn("w",lit(1.0)).groupBy("CCId").sum("w")
```

>     ccV: org.apache.spark.graphx.VertexRDD[org.apache.spark.graphx.VertexId] = VertexRDDImpl[48947] at RDD at VertexRDD.scala:57
>     ccIDs: org.apache.spark.sql.DataFrame = [CCId: bigint, sum(w): double]

``` scala
display(ccIDs.orderBy($"sum(w)".desc))  // just one main component
```

| CCId          | sum(w)    |
|---------------|-----------|
| 12.0          | 2408064.0 |
| 2.4539804e7   | 11.0      |
| 5.58985624e8  | 10.0      |
| 1.4684809e7   | 6.0       |
| 3.248055651e9 | 4.0       |
| 4.3825543e7   | 4.0       |
| 2.80909035e8  | 4.0       |
| 4.887288238e9 | 4.0       |
| 1.4996293e7   | 4.0       |
| 3.0995308e7   | 4.0       |
| 2.43057875e8  | 4.0       |
| 9.03181812e8  | 4.0       |
| 3.2634036e7   | 4.0       |
| 1.92591747e8  | 4.0       |
| 2.80481025e8  | 3.0       |
| 4.6928976e7   | 3.0       |
| 5.81466861e8  | 3.0       |
| 1.32871344e8  | 3.0       |
| 3.064832195e9 | 3.0       |
| 1.8373162e7   | 3.0       |
| 5.38790007e8  | 3.0       |
| 2.860616215e9 | 3.0       |
| 5.3000535e7   | 3.0       |
| 1.178506776e9 | 3.0       |
| 1.13820551e8  | 3.0       |
| 5.0740237e7   | 3.0       |
| 3.085141858e9 | 3.0       |
| 1.8116791e7   | 3.0       |
| 1.1685838e8   | 3.0       |
| 4.09958044e8  | 3.0       |

Truncated to 30 rows

Define landmarks
================

Find a way to define a good set of landmarks. One way could be data driven by chosing the top nodes by some centrality measure, e.g. out degree, out neighbourhood or pagerank. Or domain expertise the landmarks can by chosen manually.

``` scala
val landmarkVertices = Array((117777690L,"jeremycorbyn"), (747807250819981312L,	"theresa_may"), (222748037L,"AngelaRayner"), (3131144855L,"BorisJohnson"), (16973333L,	"Independent"), (16343974L, "Telegraph"), (14157134L, "Peston"), (216299334L,"piersmorgan"), (60886384L,"Kevin_Maguire"), (65045121L,"OwenJones84"), (19811190L, "paulmasonnews"), (19346439L, "LouiseMensch"), (465973L,"GuidoFawkes"))
```

>     landmarkVertices: Array[(Long, String)] = Array((117777690,jeremycorbyn), (747807250819981312,theresa_may), (222748037,AngelaRayner), (3131144855,BorisJohnson), (16973333,Independent), (16343974,Telegraph), (14157134,Peston), (216299334,piersmorgan), (60886384,Kevin_Maguire), (65045121,OwenJones84), (19811190,paulmasonnews), (19346439,LouiseMensch), (465973,GuidoFawkes))

``` scala
val landmarkOutNghbd = retweetNetwork.select("OPostUSerIDinRT", "OPostUserSNinRT", "CPostUSerID", "Weight").distinct().groupBy("OPostUSerIDinRT", "OPostUserSNinRT").agg(sum($"Weight").as("OutNghbd")).toDF("Id", "SN", "OutNghbd").alias("landmarkOutNghbd")
val landmarkInNghbd = retweetNetwork.select("OPostUSerIDinRT", "CPostUserSN", "CPostUSerID", "Weight").distinct().groupBy("CPostUserID", "CPostUserSN").agg(sum($"Weight").as("InNghbd")).toDF("Id", "SN", "InNghbd").alias("landmarkInNghbd")
val landmarkOutDegree = retweetNetwork.groupBy("OPostUSerIDinRT", "OPostUserSNinRT").agg(sum($"Weight").as("OutDegree")).toDF("Id", "SN", "OutDegree").alias("landmarkOutDegree")
val landmarkInDegree = retweetNetwork.groupBy("CPostUserId", "CPostUserSN").agg(sum($"Weight").as("InDegree")).toDF("Id", "SN", "InDegree").alias("landmarkInDegree")
```

>     landmarkOutNghbd: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [Id: bigint, SN: string ... 1 more field]
>     landmarkInNghbd: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [Id: bigint, SN: string ... 1 more field]
>     landmarkOutDegree: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [Id: bigint, SN: string ... 1 more field]
>     landmarkInDegree: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [Id: bigint, SN: string ... 1 more field]

Display popular vertices

``` scala
display(landmarkOutDegree.join(landmarkOutNghbd.drop("SN"), Seq("ID")).join(landmarkInDegree.drop("SN"), Seq("ID"), joinType="outer").join(landmarkInNghbd.drop("SN"), Seq("ID"), joinType="outer").orderBy($"OutDegree".desc))
```

| Id                     | SN              | OutDegree | OutNghbd | InDegree | InNghbd |
|------------------------|-----------------|-----------|----------|----------|---------|
| 1.1777769e8            | jeremycorbyn    | 516833.0  | 184236.0 | 21.0     | 20.0    |
| 6.5045121e7            | OwenJones84     | 202084.0  | 78548.0  | 261.0    | 192.0   |
| 1.6973333e7            | Independent     | 195573.0  | 67341.0  | 681.0    | 22.0    |
| 1.541364193e9          | britainelects   | 130161.0  | 46921.0  | 15.0     | 14.0    |
| 2.16299334e8           | piersmorgan     | 118588.0  | 79514.0  | 157.0    | 128.0   |
| 1.28216887e8           | jonsnowC4       | 90555.0   | 53637.0  | 94.0     | 28.0    |
| 1.981119e7             | paulmasonnews   | 74207.0   | 27358.0  | 309.0    | 222.0   |
| 1.6343974e7            | Telegraph       | 60732.0   | 29500.0  | 95.0     | 15.0    |
| 1.9346439e7            | LouiseMensch    | 53739.0   | 16916.0  | 3287.0   | 916.0   |
| 1.4157134e7            | Peston          | 48052.0   | 29552.0  | 25.0     | 8.0     |
| 7.47807250819981312e17 | theresa\_may    | 47791.0   | 31075.0  | null     | null    |
| 2.2812734e7            | faisalislam     | 46715.0   | 21148.0  | 101.0    | 75.0    |
| 2.22748037e8           | AngelaRayner    | 45272.0   | 15751.0  | 101.0    | 68.0    |
| 1.8020612e7            | DavidLammy      | 43043.0   | 27350.0  | 29.0     | 21.0    |
| 3.331501e7             | davidallengreen | 39141.0   | 15527.0  | 183.0    | 95.0    |
| 6.1183568e7            | bbclaurak       | 37683.0   | 18288.0  | 85.0     | 29.0    |
| 2.1202851e7            | IanDunt         | 36600.0   | 16069.0  | 203.0    | 157.0   |
| 8.62264836306214913e17 | LordBuckethead  | 36436.0   | 28899.0  | 13.0     | 10.0    |
| 6.0886384e7            | Kevin\_Maguire  | 36378.0   | 17015.0  | 5.0      | 1.0     |
| 1.5439395e7            | stephenfry      | 32521.0   | 26379.0  | 2.0      | 2.0     |
| 6.178126e7             | Ed\_Miliband    | 32264.0   | 23832.0  | 9.0      | 9.0     |
| 1.5438913e7            | MailOnline      | 31988.0   | 15781.0  | 594.0    | 10.0    |
| 1.9335378e7            | johnprescott    | 31906.0   | 23329.0  | 51.0     | 29.0    |
| 465973.0               | GuidoFawkes     | 29033.0   | 10410.0  | 78.0     | 37.0    |
| 1.4700117e7            | MayorofLondon   | 27816.0   | 20162.0  | 44.0     | 17.0    |
| 2.5275453e7            | jimwaterson     | 27480.0   | 18512.0  | 20.0     | 19.0    |
| 7.7234984e7            | johnmcdonnellMP | 26587.0   | 13841.0  | 40.0     | 22.0    |
| 3.4655603e7            | TheSun          | 26458.0   | 13462.0  | 476.0    | 23.0    |
| 1.53810216e8           | HackneyAbbott   | 25869.0   | 16886.0  | 151.0    | 56.0    |
| 1.5143478e7            | RichardDawkins  | 25059.0   | 16605.0  | 3.0      | 3.0     |

Truncated to 30 rows

Display popular vertices of some chategory of accounts, e.g. newspapers or politicians.

``` scala
val allTracked = sc.textFile("/FileStore/tables/gj2ee8j11500983679360/ids_with_labels2-7c031.csv")
  .map(x => x.split(","))
  .map(x => (x(0).toLong, x(1).toString, x(2).toString, x(3).toString))
  .toDF(Seq("UserId", "UserSN", "RealName", "Affiliation"): _*)
```

>     allTracked: org.apache.spark.sql.DataFrame = [UserId: bigint, UserSN: string ... 2 more fields]

``` scala
display(retweetNetwork.select("OPostUserSNinRT", "OPostUserIDinRT").distinct().toDF("SN", "Id")
  .join(allTracked.filter($"Affiliation" === "Newspaper"), $"UserId" === $"Id")
  //.join(landmarkPageRanks.toDF(Seq("Id" ,"PageRank"): _*), "Id")
  .join(landmarkOutNghbd.drop("SN"), "Id")
  //.join(landmarkInNghbd.drop("SN"), "Id")
  //.join(landmarkOutDegree.drop("SN"), "Id")
  //.join(landmarkInDegree.drop("SN"), "Id")
  .orderBy($"OutNghbd".desc))
```

| Id           | SN             | UserId       | UserSN         | RealName            | Affiliation | OutNghbd |
|--------------|----------------|--------------|----------------|---------------------|-------------|----------|
| 1.6973333e7  | Independent    | 1.6973333e7  | Ind            | The Ind             | Newspaper   | 67341.0  |
| 1.6343974e7  | Telegraph      | 1.6343974e7  | Telegraph      | The Telegraph       | Newspaper   | 29500.0  |
| 1.5438913e7  | MailOnline     | 1.5438913e7  | MailOnline     | Daily Mail Online   | Newspaper   | 15781.0  |
| 3.4655603e7  | TheSun         | 3.4655603e7  | TheSun         | The Sun             | Newspaper   | 13462.0  |
| 1.8949452e7  | FT             | 1.8949452e7  | FT             | Financial Times     | Newspaper   | 12885.0  |
| 1.6887175e7  | DailyMirror    | 1.6887175e7  | DailyMirror    | Daily Mirror        | Newspaper   | 12335.0  |
| 3.814238e7   | standardnews   | 3.814238e7   | standardnews   | Evening Standard    | Newspaper   | 9017.0   |
| 1.789582e7   | Daily\_Express | 1.789582e7   | Daily\_Express | Daily Express       | Newspaper   | 6381.0   |
| 1.4138785e7  | TelegraphNews  | 1.4138785e7  | TelegraphNews  | Telegraph News      | Newspaper   | 6336.0   |
| 6107422.0    | thetimes       | 6107422.0    | thetimes       | The Times of London | Newspaper   | 5877.0   |
| 4898091.0    | FinancialTimes | 4898091.0    | FinancialTimes | Financial Times     | Newspaper   | 5626.0   |
| 2.05770556e8 | theipaper      | 2.05770556e8 | theipaper      | i newspaper         | Newspaper   | 4395.0   |
| 2.044293e7   | Daily\_Star    | 2.044293e7   | Daily\_Star    | Daily Star          | Newspaper   | 4154.0   |
| 788524.0     | guardiannews   | 788524.0     | guardiannews   | Guardian news       | Newspaper   | 2673.0   |
| 1.30778462e8 | BrookesTimes   | 1.30778462e8 | BrookesTimes   | Peter Brookes       | Newspaper   | 728.0    |
| 3.4904355e7  | ftweekend      | 3.4904355e7  | ftweekend      | FT Weekend          | Newspaper   | 1.0      |
| 7.2811888e7  | dailyexpressuk | 7.2811888e7  | dailyexpressuk | Daily Express UK    | Newspaper   | 1.0      |

``` scala
display(retweetNetwork.select("OPostUserSNinRT", "OPostUserIDinRT").distinct().toDF("SN", "Id")
  .join(allTracked.filter($"Affiliation" =!= "Newspaper").filter($"Affiliation" =!= "Other"), $"UserId" === $"Id")
  //.join(landmarkPageRanks.toDF(Seq("Id" ,"PageRank"): _*), "Id")
  .join(landmarkOutNghbd, "Id")
  //.join(landmarkInNghbd, "Id")
  //.join(landmarkOutDegree, "Id")
  //.join(landmarkInDegree, "Id")
  .orderBy($"OutNghbd".desc))
```

| Id                     | SN              | UserId                 | UserSN          | RealName         | Affiliation | SN              | OutNghbd |
|------------------------|-----------------|------------------------|-----------------|------------------|-------------|-----------------|----------|
| 1.1777769e8            | jeremycorbyn    | 1.1777769e8            | jeremycorbyn    | Jeremy Corbyn    | LP          | jeremycorbyn    | 184236.0 |
| 7.47807250819981312e17 | theresa\_may    | 7.47807250819981312e17 | theresa\_may    | Theresa May      | CP          | theresa\_may    | 31075.0  |
| 8.62264836306214913e17 | LordBuckethead  | 8.62264836306214913e17 | LordBuckethead  | Lord Buckethead  | Ind         | LordBuckethead  | 28899.0  |
| 1.8020612e7            | DavidLammy      | 1.8020612e7            | DavidLammy      | David Lammy      | LP          | DavidLammy      | 27350.0  |
| 6.178126e7             | Ed\_Miliband    | 6.178126e7             | Ed\_Miliband    | Ed Miliband      | LP          | Ed\_Miliband    | 23832.0  |
| 1.53810216e8           | HackneyAbbott   | 1.53810216e8           | HackneyAbbott   | Diane Abbott     | LP          | HackneyAbbott   | 16886.0  |
| 2.22748037e8           | AngelaRayner    | 2.22748037e8           | AngelaRayner    | Angela Rayner    | LP          | AngelaRayner    | 15751.0  |
| 7.7234984e7            | johnmcdonnellMP | 7.7234984e7            | johnmcdonnellMP | John McDonnell   | LP          | johnmcdonnellMP | 13841.0  |
| 8.08029e7              | CarolineLucas   | 8.08029e7              | CarolineLucas   | Caroline Lucas   | GP          | CarolineLucas   | 11349.0  |
| 2.36786367e8           | AlexSalmond     | 2.36786367e8           | AlexSalmond     | Alex Salmond     | SNP         | AlexSalmond     | 11229.0  |
| 5.45081356e8           | RichardBurgon   | 5.45081356e8           | RichardBurgon   | Richard Burgon   | LP          | RichardBurgon   | 8692.0   |
| 3.3300246e7            | ChukaUmunna     | 3.3300246e7            | ChukaUmunna     | Chuka Umunna     | LP          | ChukaUmunna     | 8191.0   |
| 2.23539098e8           | nw\_nicholas    | 2.23539098e8           | nw\_nicholas    | Nicholas Wilson  | Ind         | nw\_nicholas    | 7609.0   |
| 3.131144855e9          | BorisJohnson    | 3.131144855e9          | BorisJohnson    | Boris Johnson    | CP          | BorisJohnson    | 7333.0   |
| 3.28634628e8           | YvetteCooperMP  | 3.28634628e8           | YvetteCooperMP  | Yvette Cooper    | LP          | YvetteCooperMP  | 6695.0   |
| 2.425571623e9          | Keir\_Starmer   | 2.425571623e9          | Keir\_Starmer   | Keir Starmer     | LP          | Keir\_Starmer   | 6040.0   |
| 8.0021045e7            | timfarron       | 8.0021045e7            | timfarron       | Tim Farron       | LD          | timfarron       | 5512.0   |
| 1.5484198e7            | georgegalloway  | 1.5484198e7            | georgegalloway  | George Galloway  | Ind         | georgegalloway  | 5379.0   |
| 1.4321261e8            | JonAshworth     | 1.4321261e8            | JonAshworth     | Jon Ashworth     | Lab Co-op   | JonAshworth     | 4745.0   |
| 1.07722321e8           | BarryGardiner   | 1.07722321e8           | BarryGardiner   | Barry Gardiner   | LP          | BarryGardiner   | 4605.0   |
| 1.64226176e8           | EmilyThornberry | 1.64226176e8           | EmilyThornberry | Emily Thornberry | LP          | EmilyThornberry | 4468.0   |
| 3.6924726e7            | labourlewis     | 3.6924726e7            | labourlewis     | Clive Lewis      | LP          | labourlewis     | 4288.0   |
| 7.21026242e8           | JCHannah77      | 7.21026242e8           | JCHannah77      | Jon Hannah       | LD          | JCHannah77      | 3783.0   |
| 1.4190551e7            | tom\_watson     | 1.4190551e7            | tom\_watson     | Tom Watson       | LP          | tom\_watson     | 3375.0   |
| 4.26116125e8           | heidiallen75    | 4.26116125e8           | heidiallen75    | Heidi Allen      | CP          | heidiallen75    | 3059.0   |
| 1.5010349e7            | nick\_clegg     | 1.5010349e7            | nick\_clegg     | Nick Clegg       | LD          | nick\_clegg     | 2909.0   |
| 1.91807697e8           | jon\_trickett   | 1.91807697e8           | jon\_trickett   | Jon Trickett     | LP          | jon\_trickett   | 2805.0   |
| 2.0000725e7            | jessphillips    | 2.0000725e7            | jessphillips    | Jess Phillips    | LP          | jessphillips    | 2522.0   |
| 9.48015937e8           | SarahChampionMP | 9.48015937e8           | SarahChampionMP | Sarah Champion   | LP          | SarahChampionMP | 2457.0   |
| 1.4077382e7            | JamesCleverly   | 1.4077382e7            | JamesCleverly   | James Cleverly   | CP          | JamesCleverly   | 2009.0   |

Truncated to 30 rows

``` scala
val totalNumberOfRetweeters = retweetNetwork.select("CPostUserId").distinct().count() // 776801
```

>     totalNumberOfRetweeters: Long = 808388

Find the shortest paths from each landmark to every other node
==============================================================

This is done using Dijkstra's algorithm, where the weights of each edge is inversely proportional to the number of retweets.

``` scala
// Run Dijkstra on the graph for all the landmarks
var df = vDF //.drop("srcVid")
for (landmark <- landmarkVertices) {
//for (landmark <-   Array(landmarkVertices(0),landmarkVertices(6)) ){
  val temp = runDijkstraOnce("id", graph, landmark._1, landmark._2)
  df = df.join(temp, "id")
  df.cache()
  df.count()
}
```

>     df: org.apache.spark.sql.DataFrame = [id: bigint, jeremycorbynESumGeom: double ... 25 more fields]

``` scala
df.cache()
df.count() // 790811 //1038382
```

>     res4: Long = 1083714

``` scala
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
```

>     import org.apache.spark.sql.functions.col
>     import scala.collection.mutable.ArrayBuffer
>     temp: scala.collection.mutable.ArrayBuffer[String] = ArrayBuffer(jeremycorbynPathLen, theresa_mayPathLen, AngelaRaynerPathLen, BorisJohnsonPathLen, IndependentPathLen, TelegraphPathLen, PestonPathLen, piersmorganPathLen, Kevin_MaguirePathLen, OwenJones84PathLen, paulmasonnewsPathLen, LouiseMenschPathLen, GuidoFawkesPathLen)
>     pathLens: Seq[String] = ArrayBuffer(jeremycorbynPathLen, theresa_mayPathLen, AngelaRaynerPathLen, BorisJohnsonPathLen, IndependentPathLen, TelegraphPathLen, PestonPathLen, piersmorganPathLen, Kevin_MaguirePathLen, OwenJones84PathLen, paulmasonnewsPathLen, LouiseMenschPathLen, GuidoFawkesPathLen)
>     temp2: scala.collection.mutable.ArrayBuffer[String] = ArrayBuffer(jeremycorbynESumGeom, theresa_mayESumGeom, AngelaRaynerESumGeom, BorisJohnsonESumGeom, IndependentESumGeom, TelegraphESumGeom, PestonESumGeom, piersmorganESumGeom, Kevin_MaguireESumGeom, OwenJones84ESumGeom, paulmasonnewsESumGeom, LouiseMenschESumGeom, GuidoFawkesESumGeom)
>     sumGeoms: Seq[String] = ArrayBuffer(jeremycorbynESumGeom, theresa_mayESumGeom, AngelaRaynerESumGeom, BorisJohnsonESumGeom, IndependentESumGeom, TelegraphESumGeom, PestonESumGeom, piersmorganESumGeom, Kevin_MaguireESumGeom, OwenJones84ESumGeom, paulmasonnewsESumGeom, LouiseMenschESumGeom, GuidoFawkesESumGeom)

``` scala
// Filter out a bunch of stuff:
val minRT=1/(1+1.0)  // only keep users who have retweeted at least one of the landmarks 2 or more times.

val df1 = df  // couldn't come up wiht a smarter way to do this... 
 .filter((col(sumGeoms(0))>minRT||col(sumGeoms(1))>minRT||col(sumGeoms(2))>minRT||col(sumGeoms(3))>minRT||col(sumGeoms(4))>minRT||col(sumGeoms(5))>minRT||col(sumGeoms(6))>minRT||col(sumGeoms(7))>minRT||col(sumGeoms(8))>minRT||col(sumGeoms(9))>minRT)||col(sumGeoms(10))>minRT||col(sumGeoms(11))>minRT||col(sumGeoms(12))>minRT)//||col(sumGeoms(13))>minRT||col(sumGeoms(14))>minRT||col(sumGeoms(15))>minRT||col(sumGeoms(16))>minRT))//||col(sumGeoms(17))>minRT||col(sumGeoms(18))>minRT)) 
//&& 
```

>     minRT: Double = 0.5
>     df1: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [id: bigint, jeremycorbynESumGeom: double ... 25 more fields]

``` scala
val df2 = df1.select(pathLens.head, pathLens.tail: _*).withColumn("count",lit(1.0)).groupBy(pathLens.map(col(_)): _*).agg(sum($"count")).orderBy($"sum(count)".desc)  // Aggregate users with the same profiles. 
```

>     df2: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [jeremycorbynPathLen: int, theresa_mayPathLen: int ... 12 more fields]

``` scala
// We want to groupBy and apply different agg. operations to different columns. Find the average of "...sumGeoms" and sum "counts".
// don't need this anymore, but it is clever so I'll keep it here if I need some other time. 
val exprs = (sumGeoms.map((_ -> "mean")) ++ Seq("count").map((_ -> "sum"))).toMap
// val df2 = df1.withColumn("count",lit(1.0)).groupBy(pathLens.map(col(_)): _*).agg(exprs)
```

>     exprs: scala.collection.immutable.Map[String,String] = Map(count -> sum, GuidoFawkesESumGeom -> mean, paulmasonnewsESumGeom -> mean, theresa_mayESumGeom -> mean, PestonESumGeom -> mean, LouiseMenschESumGeom -> mean, Kevin_MaguireESumGeom -> mean, TelegraphESumGeom -> mean, AngelaRaynerESumGeom -> mean, jeremycorbynESumGeom -> mean, OwenJones84ESumGeom -> mean, IndependentESumGeom -> mean, BorisJohnsonESumGeom -> mean, piersmorganESumGeom -> mean)

``` scala
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
```

>     dfZipWithIndex: (df: org.apache.spark.sql.DataFrame, offset: Int, colName: String, inFront: Boolean)org.apache.spark.sql.DataFrame

``` scala
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
```

>     import org.apache.spark.sql.functions._
>     import org.apache.spark.sql.expressions.Window
>     columnNames: Seq[String] = ArrayBuffer(jeremycorbynPathLen, theresa_mayPathLen, AngelaRaynerPathLen, BorisJohnsonPathLen, IndependentPathLen, TelegraphPathLen, PestonPathLen, piersmorganPathLen, Kevin_MaguirePathLen, OwenJones84PathLen, paulmasonnewsPathLen, LouiseMenschPathLen, GuidoFawkesPathLen, sum(count))
>     sumFreqs: Double = 736034.0
>     pathLengthFeatures: org.apache.spark.sql.DataFrame = [id: bigint, percentage: double ... 15 more fields]

``` scala
pathLengthFeatures.count()
```

>     res5: Long = 132295

``` scala
display(pathLengthFeatures.filter($"theresa_mayPathLen"===1).agg(sum("sum(count)")))
```

| sum(sum(count)) |
|-----------------|
| 30051.0         |

``` scala
display(pathLengthFeatures)  // this is the df which is used to compute the neighbourjoining-tree
```

| id   | percentage          | cs                 | jeremycorbynPathLen | theresa\_mayPathLen | AngelaRaynerPathLen | BorisJohnsonPathLen | IndependentPathLen | TelegraphPathLen | PestonPathLen | piersmorganPathLen | Kevin\_MaguirePathLen |
|------|---------------------|--------------------|---------------------|---------------------|---------------------|---------------------|--------------------|------------------|---------------|--------------------|-----------------------|
| 1.0  | 10.111897004757932  | 10.111897004757932 | 1.0                 | 3.0                 | 2.0                 | 5.0                 | 3.0                | 5.0              | 4.0           | 4.0                | 4.0                   |
| 2.0  | 7.631576802158596   | 17.743473806916526 | 3.0                 | 2.0                 | 3.0                 | 3.0                 | 3.0                | 2.0              | 3.0           | 1.0                | 3.0                   |
| 3.0  | 3.2983530652116615  | 21.041826872128187 | 4.0                 | 6.0                 | 6.0                 | 4.0                 | 1.0                | 4.0              | 4.0           | 4.0                | 6.0                   |
| 4.0  | 2.630856726727298   | 23.672683598855485 | 2.0                 | 3.0                 | 3.0                 | 3.0                 | 2.0                | 3.0              | 2.0           | 3.0                | 3.0                   |
| 5.0  | 1.864451913906151   | 25.537135512761637 | 3.0                 | 2.0                 | 3.0                 | 3.0                 | 3.0                | 3.0              | 3.0           | 3.0                | 3.0                   |
| 6.0  | 1.8249156968292226  | 27.36205120959086  | 4.0                 | 5.0                 | 3.0                 | 5.0                 | 4.0                | 3.0              | 3.0           | 3.0                | 3.0                   |
| 7.0  | 1.56867753391827    | 28.93072874350913  | 0.0                 | 1.0                 | 0.0                 | 0.0                 | 0.0                | 0.0              | 0.0           | 0.0                | 0.0                   |
| 8.0  | 1.533081352220142   | 30.463810095729272 | 4.0                 | 6.0                 | 4.0                 | 6.0                 | 6.0                | 1.0              | 4.0           | 4.0                | 5.0                   |
| 9.0  | 1.5140604917707607  | 31.977870587500032 | 4.0                 | 3.0                 | 5.0                 | 3.0                 | 2.0                | 4.0              | 4.0           | 4.0                | 2.0                   |
| 10.0 | 1.435395647483676   | 33.41326623498371  | 5.0                 | 5.0                 | 6.0                 | 6.0                 | 5.0                | 6.0              | 5.0           | 6.0                | 6.0                   |
| 11.0 | 1.3597197955529228  | 34.77298603053663  | 5.0                 | 6.0                 | 5.0                 | 5.0                 | 5.0                | 5.0              | 5.0           | 5.0                | 5.0                   |
| 12.0 | 1.3315960947456231  | 36.10458212528225  | 3.0                 | 5.0                 | 4.0                 | 5.0                 | 4.0                | 3.0              | 5.0           | 3.0                | 5.0                   |
| 13.0 | 1.2518443441471454  | 37.356426469429394 | 3.0                 | 3.0                 | 6.0                 | 3.0                 | 3.0                | 3.0              | 4.0           | 4.0                | 6.0                   |
| 14.0 | 1.0627226459647243  | 38.41914911539412  | 3.0                 | 3.0                 | 4.0                 | 3.0                 | 3.0                | 3.0              | 3.0           | 3.0                | 5.0                   |
| 15.0 | 0.9384077365991246  | 39.357556851993245 | 5.0                 | 6.0                 | 5.0                 | 5.0                 | 5.0                | 4.0              | 3.0           | 4.0                | 5.0                   |
| 16.0 | 0.8386840825287962  | 40.19624093452204  | 4.0                 | 3.0                 | 4.0                 | 3.0                 | 4.0                | 6.0              | 3.0           | 3.0                | 4.0                   |
| 17.0 | 0.699016621514767   | 40.8952575560368   | 4.0                 | 3.0                 | 5.0                 | 6.0                 | 4.0                | 6.0              | 3.0           | 6.0                | 4.0                   |
| 18.0 | 0.6435843996337126  | 41.53884195567051  | 3.0                 | 5.0                 | 5.0                 | 5.0                 | 4.0                | 5.0              | 5.0           | 4.0                | 3.0                   |
| 19.0 | 0.6380140047878223  | 42.17685596045833  | 3.0                 | 5.0                 | 4.0                 | 5.0                 | 4.0                | 5.0              | 5.0           | 3.0                | 4.0                   |
| 20.0 | 0.48408089843675706 | 42.66093685889509  | 2.0                 | 3.0                 | 3.0                 | 4.0                 | 2.0                | 3.0              | 2.0           | 3.0                | 3.0                   |
| 21.0 | 0.44590331424907    | 43.106840173144164 | 5.0                 | 5.0                 | 5.0                 | 5.0                 | 4.0                | 4.0              | 1.0           | 4.0                | 6.0                   |
| 22.0 | 0.4340832081126687  | 43.54092338125683  | 1.0                 | 3.0                 | 3.0                 | 3.0                 | 2.0                | 3.0              | 2.0           | 3.0                | 3.0                   |
| 23.0 | 0.42824108668893013 | 43.96916446794576  | 2.0                 | 4.0                 | 4.0                 | 4.0                 | 3.0                | 3.0              | 4.0           | 3.0                | 3.0                   |
| 24.0 | 0.3847648342332012  | 44.35392930217896  | 5.0                 | 6.0                 | 6.0                 | 6.0                 | 5.0                | 3.0              | 4.0           | 4.0                | 5.0                   |
| 25.0 | 0.37104264205186177 | 44.72497194423082  | 3.0                 | 3.0                 | 3.0                 | 3.0                 | 3.0                | 4.0              | 2.0           | 6.0                | 5.0                   |
| 26.0 | 0.3694122825847719  | 45.09438422681559  | 3.0                 | 2.0                 | 5.0                 | 2.0                 | 4.0                | 2.0              | 3.0           | 2.0                | 2.0                   |
| 27.0 | 0.34903278924614894 | 45.443417016061744 | 4.0                 | 3.0                 | 4.0                 | 4.0                 | 4.0                | 4.0              | 4.0           | 4.0                | 4.0                   |
| 28.0 | 0.34060926533285146 | 45.784026281394596 | 4.0                 | 4.0                 | 3.0                 | 4.0                 | 3.0                | 5.0              | 2.0           | 4.0                | 3.0                   |
| 29.0 | 0.3278381161739811  | 46.111864397568574 | 3.0                 | 5.0                 | 5.0                 | 5.0                 | 4.0                | 3.0              | 4.0           | 5.0                | 5.0                   |
| 30.0 | 0.3275663895961328  | 46.43943078716471  | 3.0                 | 3.0                 | 3.0                 | 3.0                 | 3.0                | 3.0              | 4.0           | 5.0                | 3.0                   |

Truncated to 30 rows

Truncated to 12 cols

``` scala
// Copy-Paste into Matlab! 
print("[")
df2.select(pathLens.head, pathLens.tail: _*).take(10).foreach(x => println(x.toString + ";..."))
print("];")
```

>     [[1,3,2,5,3,5,4,4,4,3,3,5,4];...
>     [3,2,3,3,3,2,3,1,3,2,3,4,3];...
>     [4,6,6,4,1,4,4,4,6,3,4,6,3];...
>     [2,3,3,3,2,3,2,3,3,1,3,3,4];...
>     [3,2,3,3,3,3,3,3,3,3,3,1,3];...
>     [4,5,3,5,4,3,3,3,3,3,5,4,4];...
>     [0,1,0,0,0,0,0,0,0,0,0,0,0];...
>     [4,6,4,6,6,1,4,4,5,6,4,5,5];...
>     [4,3,5,3,2,4,4,4,2,3,3,2,3];...
>     [5,5,6,6,5,6,5,6,6,5,6,6,6];...
>     ];

Communication in the neighbourjoing tree
========================================

Now we find the frequency of retweets across different branches in the trre

``` scala
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
```

>     import spark.implicits._
>     profiles: Array[Array[Double]] = Array(Array(1.0, 3.0, 2.0, 5.0, 3.0, 5.0, 4.0, 4.0, 4.0, 3.0, 3.0, 5.0, 4.0), Array(3.0, 2.0, 3.0, 3.0, 3.0, 2.0, 3.0, 1.0, 3.0, 2.0, 3.0, 4.0, 3.0), Array(4.0, 6.0, 6.0, 4.0, 1.0, 4.0, 4.0, 4.0, 6.0, 3.0, 4.0, 6.0, 3.0), Array(2.0, 3.0, 3.0, 3.0, 2.0, 3.0, 2.0, 3.0, 3.0, 1.0, 3.0, 3.0, 4.0), Array(3.0, 2.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 1.0, 3.0), Array(4.0, 5.0, 3.0, 5.0, 4.0, 3.0, 3.0, 3.0, 3.0, 3.0, 5.0, 4.0, 4.0), Array(0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), Array(4.0, 6.0, 4.0, 6.0, 6.0, 1.0, 4.0, 4.0, 5.0, 6.0, 4.0, 5.0, 5.0), Array(4.0, 3.0, 5.0, 3.0, 2.0, 4.0, 4.0, 4.0, 2.0, 3.0, 3.0, 2.0, 3.0), Array(5.0, 5.0, 6.0, 6.0, 5.0, 6.0, 5.0, 6.0, 6.0, 5.0, 6.0, 6.0, 6.0), Array(5.0, 6.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 4.0, 5.0, 5.0, 6.0), Array(3.0, 5.0, 4.0, 5.0, 4.0, 3.0, 5.0, 3.0, 5.0, 3.0, 4.0, 5.0, 3.0), Array(3.0, 3.0, 6.0, 3.0, 3.0, 3.0, 4.0, 4.0, 6.0, 6.0, 4.0, 4.0, 4.0), Array(3.0, 3.0, 4.0, 3.0, 3.0, 3.0, 3.0, 3.0, 5.0, 3.0, 5.0, 3.0, 3.0), Array(5.0, 6.0, 5.0, 5.0, 5.0, 4.0, 3.0, 4.0, 5.0, 5.0, 6.0, 6.0, 5.0), Array(4.0, 3.0, 4.0, 3.0, 4.0, 6.0, 3.0, 3.0, 4.0, 5.0, 3.0, 4.0, 6.0), Array(4.0, 3.0, 5.0, 6.0, 4.0, 6.0, 3.0, 6.0, 4.0, 4.0, 5.0, 4.0, 6.0), Array(3.0, 5.0, 5.0, 5.0, 4.0, 5.0, 5.0, 4.0, 3.0, 5.0, 5.0, 6.0, 6.0), Array(3.0, 5.0, 4.0, 5.0, 4.0, 5.0, 5.0, 3.0, 4.0, 3.0, 4.0, 4.0, 4.0), Array(2.0, 3.0, 3.0, 4.0, 2.0, 3.0, 2.0, 3.0, 3.0, 3.0, 1.0, 4.0, 4.0), Array(5.0, 5.0, 5.0, 5.0, 4.0, 4.0, 1.0, 4.0, 6.0, 4.0, 6.0, 5.0, 6.0), Array(1.0, 3.0, 3.0, 3.0, 2.0, 3.0, 2.0, 3.0, 3.0, 1.0, 3.0, 3.0, 4.0), Array(2.0, 4.0, 4.0, 4.0, 3.0, 3.0, 4.0, 3.0, 3.0, 2.0, 3.0, 4.0, 4.0), Array(5.0, 6.0, 6.0, 6.0, 5.0, 3.0, 4.0, 4.0, 5.0, 4.0, 6.0, 6.0, 5.0), Array(3.0, 3.0, 3.0, 3.0, 3.0, 4.0, 2.0, 6.0, 5.0, 4.0, 4.0, 5.0, 6.0), Array(3.0, 2.0, 5.0, 2.0, 4.0, 2.0, 3.0, 2.0, 2.0, 5.0, 3.0, 6.0, 2.0), Array(4.0, 3.0, 4.0, 4.0, 4.0, 4.0, 4.0, 4.0, 4.0, 4.0, 4.0, 2.0, 4.0), Array(4.0, 4.0, 3.0, 4.0, 3.0, 5.0, 2.0, 4.0, 3.0, 3.0, 4.0, 4.0, 4.0), Array(3.0, 5.0, 5.0, 5.0, 4.0, 3.0, 4.0, 5.0, 5.0, 5.0, 3.0, 6.0, 6.0), Array(3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 4.0, 5.0, 3.0, 3.0, 3.0, 4.0, 6.0), Array(5.0, 4.0, 5.0, 5.0, 5.0, 4.0, 5.0, 3.0, 5.0, 4.0, 5.0, 6.0, 5.0), Array(4.0, 3.0, 5.0, 6.0, 4.0, 3.0, 3.0, 4.0, 5.0, 3.0, 5.0, 6.0, 3.0), Array(6.0, 5.0, 6.0, 5.0, 6.0, 4.0, 4.0, 5.0, 4.0, 6.0, 5.0, 5.0, 6.0), Array(3.0, 4.0, 5.0, 4.0, 4.0, 5.0, 3.0, 3.0, 5.0, 3.0, 3.0, 4.0, 4.0), Array(1.0, 3.0, 2.0, 3.0, 2.0, 4.0, 4.0, 4.0, 2.0, 3.0, 3.0, 2.0, 3.0), Array(3.0, 4.0, 4.0, 3.0, 3.0, 3.0, 4.0, 4.0, 3.0, 4.0, 4.0, 4.0, 4.0), Array(3.0, 3.0, 4.0, 4.0, 2.0, 3.0, 3.0, 3.0, 3.0, 2.0, 3.0, 4.0, 4.0), Array(2.0, 4.0, 3.0, 5.0, 5.0, 6.0, 4.0, 5.0, 6.0, 5.0, 5.0, 6.0, 5.0), Array(5.0, 6.0, 4.0, 6.0, 6.0, 2.0, 5.0, 5.0, 6.0, 6.0, 5.0, 6.0, 6.0), Array(1.0, 3.0, 2.0, 4.0, 1.0, 4.0, 4.0, 4.0, 4.0, 3.0, 4.0, 5.0, 3.0), Array(2.0, 2.0, 5.0, 2.0, 5.0, 3.0, 4.0, 5.0, 5.0, 3.0, 4.0, 3.0, 5.0), Array(2.0, 3.0, 2.0, 3.0, 3.0, 2.0, 2.0, 3.0, 3.0, 2.0, 3.0, 3.0, 3.0), Array(3.0, 5.0, 4.0, 3.0, 5.0, 5.0, 3.0, 4.0, 3.0, 5.0, 6.0, 6.0, 4.0), Array(6.0, 3.0, 6.0, 1.0, 5.0, 4.0, 5.0, 5.0, 5.0, 6.0, 5.0, 6.0, 4.0), Array(4.0, 4.0, 5.0, 3.0, 4.0, 5.0, 3.0, 4.0, 4.0, 3.0, 3.0, 4.0, 1.0), Array(3.0, 5.0, 5.0, 5.0, 3.0, 4.0, 3.0, 5.0, 5.0, 3.0, 3.0, 3.0, 4.0), Array(1.0, 2.0, 2.0, 3.0, 3.0, 2.0, 3.0, 1.0, 3.0, 2.0, 3.0, 5.0, 3.0), Array(1.0, 3.0, 2.0, 5.0, 3.0, 3.0, 4.0, 4.0, 4.0, 3.0, 3.0, 5.0, 4.0), Array(1.0, 3.0, 2.0, 3.0, 2.0, 3.0, 2.0, 4.0, 3.0, 1.0, 3.0, 3.0, 4.0), Array(4.0, 5.0, 5.0, 5.0, 4.0, 3.0, 3.0, 6.0, 4.0, 3.0, 6.0, 6.0, 5.0), Array(5.0, 3.0, 3.0, 3.0, 3.0, 4.0, 3.0, 3.0, 3.0, 4.0, 3.0, 4.0, 3.0), Array(3.0, 2.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 2.0, 3.0, 2.0, 4.0, 5.0), Array(4.0, 6.0, 6.0, 4.0, 1.0, 1.0, 4.0, 4.0, 6.0, 3.0, 4.0, 6.0, 3.0), Array(1.0, 3.0, 2.0, 3.0, 3.0, 3.0, 4.0, 4.0, 4.0, 3.0, 4.0, 5.0, 4.0), Array(4.0, 5.0, 4.0, 3.0, 3.0, 3.0, 3.0, 4.0, 4.0, 4.0, 4.0, 3.0, 4.0), Array(3.0, 4.0, 2.0, 5.0, 3.0, 3.0, 3.0, 3.0, 4.0, 3.0, 4.0, 5.0, 4.0), Array(2.0, 4.0, 3.0, 6.0, 4.0, 6.0, 5.0, 5.0, 5.0, 4.0, 4.0, 6.0, 5.0), Array(3.0, 5.0, 5.0, 5.0, 4.0, 5.0, 5.0, 4.0, 1.0, 5.0, 5.0, 6.0, 6.0), Array(6.0, 6.0, 6.0, 4.0, 6.0, 5.0, 6.0, 5.0, 6.0, 6.0, 6.0, 6.0, 5.0), Array(6.0, 3.0, 6.0, 3.0, 5.0, 4.0, 5.0, 5.0, 5.0, 6.0, 5.0, 6.0, 4.0), Array(3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 5.0, 3.0, 3.0, 5.0, 4.0, 5.0), Array(4.0, 3.0, 5.0, 4.0, 4.0, 4.0, 3.0, 3.0, 4.0, 3.0, 3.0, 4.0, 4.0), Array(5.0, 2.0, 6.0, 2.0, 4.0, 3.0, 4.0, 4.0, 4.0, 6.0, 4.0, 5.0, 3.0), Array(1.0, 3.0, 3.0, 4.0, 2.0, 3.0, 2.0, 3.0, 3.0, 3.0, 1.0, 4.0, 4.0), Array(3.0, 3.0, 3.0, 4.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 5.0, 3.0), Array(3.0, 2.0, 6.0, 6.0, 5.0, 6.0, 3.0, 6.0, 4.0, 5.0, 6.0, 4.0, 6.0), Array(2.0, 3.0, 1.0, 3.0, 2.0, 5.0, 5.0, 3.0, 3.0, 3.0, 2.0, 3.0, 5.0), Array(2.0, 3.0, 3.0, 6.0, 3.0, 5.0, 4.0, 5.0, 4.0, 3.0, 4.0, 4.0, 5.0), Array(3.0, 6.0, 6.0, 6.0, 3.0, 6.0, 3.0, 6.0, 4.0, 5.0, 4.0, 4.0, 5.0), Array(4.0, 6.0, 5.0, 4.0, 4.0, 4.0, 3.0, 5.0, 5.0, 3.0, 4.0, 4.0, 5.0), Array(2.0, 3.0, 3.0, 3.0, 2.0, 2.0, 3.0, 4.0, 3.0, 2.0, 2.0, 4.0, 5.0), Array(2.0, 3.0, 3.0, 3.0, 2.0, 4.0, 2.0, 4.0, 2.0, 1.0, 3.0, 2.0, 3.0), Array(3.0, 1.0, 3.0, 3.0, 3.0, 2.0, 3.0, 1.0, 3.0, 2.0, 3.0, 4.0, 3.0), Array(3.0, 4.0, 4.0, 3.0, 3.0, 3.0, 2.0, 3.0, 4.0, 4.0, 2.0, 6.0, 3.0), Array(3.0, 5.0, 4.0, 6.0, 5.0, 6.0, 4.0, 6.0, 4.0, 5.0, 5.0, 4.0, 5.0), Array(1.0, 1.0, 2.0, 5.0, 3.0, 5.0, 4.0, 4.0, 4.0, 3.0, 3.0, 5.0, 4.0), Array(6.0, 6.0, 6.0, 6.0, 6.0, 6.0, 6.0, 6.0, 6.0, 5.0, 6.0, 6.0, 6.0), Array(3.0, 4.0, 4.0, 4.0, 4.0, 5.0, 3.0, 6.0, 5.0, 3.0, 5.0, 6.0, 6.0), Array(2.0, 3.0, 3.0, 3.0, 2.0, 3.0, 2.0, 3.0, 3.0, 1.0, 3.0, 2.0, 4.0), Array(3.0, 4.0, 4.0, 6.0, 3.0, 4.0, 6.0, 3.0, 3.0, 5.0, 6.0, 5.0, 5.0), Array(3.0, 3.0, 4.0, 4.0, 2.0, 3.0, 3.0, 4.0, 4.0, 2.0, 2.0, 3.0, 3.0), Array(4.0, 3.0, 5.0, 3.0, 4.0, 3.0, 3.0, 5.0, 5.0, 3.0, 5.0, 5.0, 5.0), Array(4.0, 5.0, 5.0, 5.0, 4.0, 4.0, 3.0, 5.0, 5.0, 3.0, 5.0, 5.0, 5.0), Array(4.0, 4.0, 5.0, 4.0, 5.0, 4.0, 6.0, 6.0, 6.0, 4.0, 3.0, 4.0, 5.0), Array(4.0, 5.0, 5.0, 5.0, 5.0, 3.0, 3.0, 3.0, 5.0, 5.0, 3.0, 4.0, 3.0), Array(4.0, 3.0, 3.0, 5.0, 2.0, 3.0, 4.0, 3.0, 5.0, 3.0, 4.0, 4.0, 5.0), Array(3.0, 2.0, 3.0, 3.0, 3.0, 1.0, 3.0, 1.0, 3.0, 2.0, 3.0, 4.0, 3.0), Array(2.0, 4.0, 3.0, 4.0, 3.0, 4.0, 4.0, 5.0, 3.0, 2.0, 5.0, 6.0, 5.0), Array(4.0, 5.0, 5.0, 5.0, 4.0, 5.0, 4.0, 5.0, 5.0, 3.0, 5.0, 5.0, 6.0), Array(4.0, 3.0, 4.0, 3.0, 4.0, 6.0, 3.0, 3.0, 4.0, 5.0, 3.0, 4.0, 4.0), Array(1.0, 3.0, 2.0, 5.0, 3.0, 3.0, 3.0, 4.0, 3.0, 3.0, 3.0, 5.0, 4.0), Array(3.0, 3.0, 4.0, 3.0, 3.0, 4.0, 3.0, 4.0, 4.0, 2.0, 4.0, 5.0, 4.0), Array(5.0, 4.0, 5.0, 5.0, 5.0, 4.0, 5.0, 3.0, 5.0, 4.0, 5.0, 5.0, 5.0), Array(5.0, 3.0, 6.0, 3.0, 5.0, 3.0, 3.0, 3.0, 2.0, 6.0, 4.0, 6.0, 3.0), Array(3.0, 5.0, 4.0, 3.0, 5.0, 5.0, 5.0, 4.0, 5.0, 5.0, 6.0, 6.0, 4.0), Array(4.0, 4.0, 5.0, 4.0, 4.0, 4.0, 4.0, 4.0, 6.0, 4.0, 6.0, 4.0, 4.0), Array(5.0, 6.0, 6.0, 5.0, 2.0, 5.0, 5.0, 5.0, 6.0, 4.0, 5.0, 6.0, 4.0), Array(3.0, 4.0, 3.0, 4.0, 3.0, 4.0, 4.0, 5.0, 3.0, 3.0, 3.0, 5.0, 4.0), Array(3.0, 3.0, 5.0, 3.0, 3.0, 3.0, 3.0, 4.0, 5.0, 3.0, 3.0, 4.0, 5.0), Array(2.0, 3.0, 3.0, 4.0, 1.0, 3.0, 2.0, 3.0, 3.0, 1.0, 3.0, 3.0, 3.0))
>     filterIdsWithProfile: (df: org.apache.spark.sql.DataFrame, pathLens: Seq[String], profile: Array[Integer], label: Integer)org.apache.spark.sql.DataFrame

``` scala
// TODO: assign all users to a profile. Say we take the 100 most common profiles, then each user with a different profile form the 100 first must be assign to one of the 100. This can be done using k-mean for example. 
```

``` scala
profiles.map(Vectors.dense(_))
```

>     res41: Array[org.apache.spark.mllib.linalg.Vector] = Array([1.0,3.0,2.0,5.0,3.0,5.0,4.0,4.0,4.0,3.0,3.0,5.0,4.0], [3.0,2.0,3.0,3.0,3.0,2.0,3.0,1.0,3.0,2.0,3.0,4.0,3.0])

``` scala
Array("[0.6,  0.6]", "[8.0,  8.0]").map(Vectors.parse(_))
```

>     res37: Array[org.apache.spark.mllib.linalg.Vector] = Array([0.6,0.6], [8.0,8.0])

``` scala
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

```

>     import org.apache.spark.mllib.clustering.{KMeans, KMeansModel}
>     import org.apache.spark.mllib.linalg.Vectors
>     initialModel: org.apache.spark.mllib.clustering.KMeansModel = org.apache.spark.mllib.clustering.KMeansModel@16053b15
>     kmeans: org.apache.spark.mllib.clustering.KMeans = org.apache.spark.mllib.clustering.KMeans@61881d02

``` scala
df1.count()
```

>     res120: Long = 736034

``` scala
val idPointRDD = df1.select(columnNames.head, columnNames.tail: _*).rdd.map(s => (s.getLong(0), Vectors.dense(s.getInt(1).toDouble,s.getInt(2).toDouble,s.getInt(3).toDouble,s.getInt(4).toDouble,s.getInt(5).toDouble,s.getInt(6).toDouble,s.getInt(7).toDouble,s.getInt(8).toDouble,s.getInt(9).toDouble,s.getInt(10).toDouble,s.getInt(11).toDouble,s.getInt(12).toDouble,s.getInt(13).toDouble))).cache()
val clusters = kmeans.run(idPointRDD.map(_._2))
val clustersRDD = clusters.predict(idPointRDD.map(_._2))
val idClusterRDD = idPointRDD.map(_._1).zip(clustersRDD)
```

>     idPointRDD: org.apache.spark.rdd.RDD[(Long, org.apache.spark.mllib.linalg.Vector)] = MapPartitionsRDD[51605] at map at <console>:83
>     clusters: org.apache.spark.mllib.clustering.KMeansModel = org.apache.spark.mllib.clustering.KMeansModel@3a146e5c
>     clustersRDD: org.apache.spark.rdd.RDD[Int] = MapPartitionsRDD[51651] at map at KMeansModel.scala:71
>     idClusterRDD: org.apache.spark.rdd.RDD[(Long, Int)] = ZippedPartitionsRDD2[51653] at zip at <console>:86

``` scala
val idCluster = idClusterRDD.toDF("id", "cluster").cache()
```

>     idCluster: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [id: bigint, cluster: int]

``` scala
val tm = df1.filter(col(pathLens(0))===0&&col(pathLens(1))===1&&col(pathLens(2))===0&&col(pathLens(3))===0&&col(pathLens(4))===0&&col(pathLens(5))===0&&col(pathLens(6))===0&&col(pathLens(7))===0&&col(pathLens(8))===0&&col(pathLens(9))===0&&col(pathLens(10))===0&&col(pathLens(11))===0&&col(pathLens(12))===0)
```

>     tm: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [id: bigint, jeremycorbynESumGeom: double ... 25 more fields]

``` scala
tm.count() //11572
```

>     res17: Long = 11546

``` scala
val tmNetwork = retweetNetwork.join(tm, $"CPostUserID"===$"id")
```

>     tmNetwork: org.apache.spark.sql.DataFrame = [OPostUserIDinRT: bigint, OPostUserSNinRT: string ... 31 more fields]

``` scala
tmNetwork.count()
```

>     res20: Long = 20732

``` scala
display(tmNetwork.groupBy("OPostUserSNinRT").agg(sum("Weight").as("sum")).orderBy($"sum".desc)) 
```

| OPostUserSNinRT | sum     |
|-----------------|---------|
| theresa\_may    | 14140.0 |
| MailOnline      | 158.0   |
| realDonaldTrump | 147.0   |
| RichardDawkins  | 128.0   |
| JaimeRicardoRam | 95.0    |
| dbdevletbahceli | 76.0    |
| MattyBRaps      | 48.0    |
| narendramodi    | 47.0    |
| giphz           | 44.0    |
| BishopJakes     | 44.0    |
| Daily\_Express  | 43.0    |
| DailyMailUK     | 43.0    |
| pilard2017      | 42.0    |
| AlArabiya\_Brk  | 42.0    |
| Reyhan\_News    | 40.0    |
| KBRILondon      | 32.0    |
| BillGates       | 31.0    |
| FinancialTimes  | 29.0    |
| TRobinsonNewEra | 26.0    |
| youthparty\_ng  | 25.0    |
| shanedawson     | 24.0    |
| rioferdy5       | 23.0    |
| Cristiano       | 22.0    |
| CNBCArabia      | 22.0    |
| akshaykumar     | 22.0    |
| Pontifex        | 20.0    |
| naosejatrouxa   | 20.0    |
| kartalanalizcom | 18.0    |
| Cooper4SAE      | 18.0    |
| SenusiTekkesi   | 18.0    |

Truncated to 30 rows

``` scala
println(df1.count())  //1083714 
println(idCluster.count())
//736034
//736034
```

>     736034
>     736034

``` scala
val clusterTotal = idCluster.select("cluster").withColumn("w", lit(1)).groupBy("cluster").agg(sum("w").as("total"))
```

>     clusterTotal: org.apache.spark.sql.DataFrame = [cluster: int, total: bigint]

``` scala
df1.count()
```

>     res160: Long = 736034

``` scala
display(clusterTotal.agg(sum("total")))
```

| sum(total) |
|------------|
| 736034.0   |

``` scala
import org.apache.spark.ml.feature.Interaction
import org.apache.spark.ml.feature.VectorAssembler
val columnNames = Seq("Id") ++ pathLens
val assembler1 = new VectorAssembler().
  setInputCols(pathLens.toArray).
  setOutputCol("features")
val assembled1 = assembler1.transform(df1.select(columnNames.head, columnNames.tail: _*)).select("id", "features")
display(assembled1)
```

``` scala
display(eDF.join(idCluster, $"Id" === $"SrcVid"))
```

| srcVid                 | dstVid                 | GeomProb              | id                     | cluster |
|------------------------|------------------------|-----------------------|------------------------|---------|
| 1.541364193e9          | 8.5871359392320717e17  | 5.8823529411764705e-2 | 1.541364193e9          | 13.0    |
| 1.541364193e9          | 2.172538184e9          | 0.1111111111111111    | 1.541364193e9          | 13.0    |
| 3.30717019e8           | 7.44856700969193472e17 | 2.631578947368421e-2  | 3.30717019e8           | 8.0     |
| 1.541364193e9          | 1.5245722e7            | 0.5                   | 1.541364193e9          | 13.0    |
| 1.4085096e7            | 1.4623866e7            | 0.1111111111111111    | 1.4085096e7            | 4.0     |
| 2.2021978e7            | 3.161348518e9          | 7.692307692307693e-2  | 2.2021978e7            | 21.0    |
| 1.9902709e7            | 8.44126543819476992e17 | 3.4482758620689655e-2 | 1.9902709e7            | 21.0    |
| 2.5275453e7            | 7.1741696e7            | 0.3333333333333333    | 2.5275453e7            | 13.0    |
| 1.1777769e8            | 1.71061628e9           | 0.5                   | 1.1777769e8            | 21.0    |
| 1.7061815e7            | 8.16610692648357888e17 | 0.3333333333333333    | 1.7061815e7            | 3.0     |
| 3.331501e7             | 6.34912541e8           | 5.405405405405406e-3  | 3.331501e7             | 3.0     |
| 3.751450582e9          | 2.40871024e8           | 0.3333333333333333    | 3.751450582e9          | 21.0    |
| 8.63729074103078912e17 | 8.84734946e8           | 0.3333333333333333    | 8.63729074103078912e17 | 21.0    |
| 2.653613168e9          | 1.49984507e8           | 0.16666666666666666   | 2.653613168e9          | 12.0    |
| 1.5010349e7            | 7.53447445611307008e17 | 0.2                   | 1.5010349e7            | 18.0    |
| 5.45081356e8           | 9.06412351e8           | 0.5                   | 5.45081356e8           | 21.0    |
| 2.91372292e9           | 7.6436317e7            | 0.25                  | 2.91372292e9           | 5.0     |
| 3.331501e7             | 2.0116884e8            | 0.25                  | 3.331501e7             | 3.0     |
| 1.6973333e7            | 1.0275162e7            | 0.1                   | 1.6973333e7            | 2.0     |
| 1.1239873e8            | 3.311830742e9          | 0.5                   | 1.1239873e8            | 24.0    |
| 6.5045121e7            | 3.87899173e8           | 8.333333333333333e-2  | 6.5045121e7            | 21.0    |
| 6.1183568e7            | 4.459625361e9          | 0.3333333333333333    | 6.1183568e7            | 4.0     |
| 6.2123765e7            | 2.41904099e8           | 0.5                   | 6.2123765e7            | 1.0     |
| 1.4515799e7            | 7.96686724885807104e17 | 0.5                   | 1.4515799e7            | 4.0     |
| 6.5045121e7            | 2.777899492e9          | 0.5                   | 6.5045121e7            | 21.0    |
| 1.6139649e7            | 7.76728465231978496e17 | 0.25                  | 1.6139649e7            | 13.0    |
| 5.24235256e8           | 2.4036615e7            | 0.5                   | 5.24235256e8           | 2.0     |
| 1.1777769e8            | 2.78142484e9           | 9.523809523809525e-3  | 1.1777769e8            | 21.0    |
| 1.6973333e7            | 2.214619884e9          | 0.125                 | 1.6973333e7            | 2.0     |
| 5.3674515e8            | 2.22631703e8           | 0.5                   | 5.3674515e8            | 19.0    |

Truncated to 30 rows

``` scala
object TupleUDFs {
  import org.apache.spark.sql.functions.udf      
  // type tag is required, as we have a generic udf
  import scala.reflect.runtime.universe.{TypeTag, typeTag}

  def toTuple2[S: TypeTag, T: TypeTag] = 
    udf[(S, T), S, T]((x: S, y: T) => (x, y))
}
```

>     defined object TupleUDFs

``` scala
val edges = eDF.select("srcVid", "dstVid").join(idCluster.toDF("id","srcCluster"), $"srcVid"===$"id").drop("id").join(idCluster.toDF("id", "dstCluster"), $"dstVid"===$"id").drop("id").withColumn(
  "attr", TupleUDFs.toTuple2[Int, Int].apply($"srcCluster", $"dstCluster")
)
```

>     edges: org.apache.spark.sql.DataFrame = [srcVid: bigint, dstVid: bigint ... 3 more fields]

``` scala
display(edges)
```

``` scala
val res = ArrayBuffer[(Int,Int,Long)]()
for (i <- Range(0, 25); j <- Range(i, 25)) {
  var count = edges.filter($"srcCluster"===i).filter($"dstCluster"===j).count()
  println((i,j)+","+count);
  res += ((i,j, count))
}
```

>     (0,0),7371
>     (0,1),3727
>     (0,2),218
>     (0,3),17154
>     (0,4),3248
>     (0,5),868
>     (0,6),164
>     (0,7),41
>     (0,8),633
>     (0,9),7
>     (0,10),22
>     (0,11),320
>     (0,12),196
>     (0,13),1299
>     (0,14),18
>     (0,15),134
>     (0,16),353
>     (0,17),4292
>     (0,18),2313
>     (0,19),14386
>     (0,20),102
>     (0,21),31880
>     (0,22),7535
>     (0,23),121
>     (0,24),2273
>     (1,1),113474
>     (1,2),238
>     (1,3),18615
>     (1,4),10124
>     (1,5),2367
>     (1,6),160
>     (1,7),16
>     (1,8),2636
>     (1,9),6
>     (1,10),23
>     (1,11),1531
>     (1,12),4584
>     (1,13),25047
>     (1,14),70
>     (1,15),250
>     (1,16),49
>     (1,17),108
>     (1,18),2153
>     (1,19),9363
>     (1,20),174
>     (1,21),32431
>     (1,22),6530
>     (1,23),549
>     (1,24),1871
>     (2,2),31418
>     (2,3),9530
>     (2,4),3135
>     (2,5),673
>     (2,6),19
>     (2,7),4
>     (2,8),2052
>     (2,9),47
>     (2,10),30
>     (2,11),111
>     (2,12),484
>     (2,13),2657
>     (2,14),10
>     (2,15),99
>     (2,16),55
>     (2,17),98
>     (2,18),1479
>     (2,19),5207
>     (2,20),61
>     (2,21),11799
>     (2,22),6156
>     (2,23),163
>     (2,24),939
>     (3,3),48104
>     (3,4),9617
>     (3,5),22609
>     (3,6),484
>     (3,7),18
>     (3,8),3735
>     (3,9),4
>     (3,10),75
>     (3,11),537
>     (3,12),362
>     (3,13),9298
>     (3,14),33
>     (3,15),449
>     (3,16),299
>     (3,17),199
>     (3,18),16555
>     (3,19),32148
>     (3,20),445
>     (3,21),72902
>     (3,22),17320
>     (3,23),433
>     (3,24),9529
>     (4,4),56089
>     (4,5),3203
>     (4,6),224
>     (4,7),15
>     (4,8),21621
>     (4,9),3
>     (4,10),68
>     (4,11),355
>     (4,12),1375
>     (4,13),37974
>     (4,14),15
>     (4,15),1423
>     (4,16),284
>     (4,17),118
>     (4,18),6513
>     (4,19),23076
>     (4,20),368
>     (4,21),64318
>     (4,22),10097
>     (4,23),363
>     (4,24),8721
>     (5,5),1919
>     (5,6),128
>     (5,7),141
>     (5,8),764
>     (5,9),2
>     (5,10),106
>     (5,11),206
>     (5,12),545
>     (5,13),2566
>     (5,14),8019
>     (5,15),127
>     (5,16),318
>     (5,17),2479
>     (5,18),1300
>     (5,19),4650
>     (5,20),239
>     (5,21),10322
>     (5,22),2603
>     (5,23),473
>     (5,24),998
>     (6,6),129
>     (6,7),5
>     (6,8),53
>     (6,9),17
>     (6,10),0
>     (6,11),25
>     (6,12),228
>     (6,13),422
>     (6,14),0
>     (6,15),18
>     (6,16),15
>     (6,17),14
>     (6,18),38
>     (6,19),459
>     (6,20),5
>     (6,21),1006
>     (6,22),237
>     (6,23),83
>     (6,24),174
>     (7,7),14743
>     (7,8),445
>     (7,9),15
>     (7,10),30
>     (7,11),459
>     (7,12),1130
>     (7,13),2654
>     (7,14),486
>     (7,15),3
>     (7,16),0
>     (7,17),83
>     (7,18),177
>     (7,19),1016
>     (7,20),92
>     (7,21),2937
>     (7,22),2571
>     (7,23),397
>     (7,24),989
>     (8,8),2782
>     (8,9),2
>     (8,10),92
>     (8,11),67
>     (8,12),1038
>     (8,13),2460
>     (8,14),7
>     (8,15),218
>     (8,16),577
>     (8,17),66
>     (8,18),2142
>     (8,19),7024
>     (8,20),120
>     (8,21),15079
>     (8,22),2215
>     (8,23),304
>     (8,24),1120
>     (9,9),1056
>     (9,10),135
>     (9,11),77
>     (9,12),193
>     (9,13),595
>     (9,14),28
>     (9,15),36
>     (9,16),17
>     (9,17),55
>     (9,18),248
>     (9,19),746
>     (9,20),31
>     (9,21),1213
>     (9,22),523
>     (9,23),162
>     (9,24),211
>     (10,10),487
>     (10,11),138
>     (10,12),318
>     (10,13),1185
>     (10,14),78
>     (10,15),81
>     (10,16),80
>     (10,17),141
>     (10,18),759
>     (10,19),2033
>     (10,20),61
>     (10,21),3839
>     (10,22),1135
>     (10,23),344
>     (10,24),574
>     (11,11),458
>     (11,12),440
>     (11,13),1815
>     (11,14),3080
>     (11,15),64
>     (11,16),50
>     (11,17),140
>     (11,18),938
>     (11,19),4189
>     (11,20),101
>     (11,21),9321
>     (11,22),3069
>     (11,23),662
>     (11,24),996
>     (12,12),10057
>     (12,13),6212
>     (12,14),186
>     (12,15),127
>     (12,16),62
>     (12,17),213
>     (12,18),716
>     (12,19),2728
>     (12,20),100
>     (12,21),6047
>     (12,22),1679
>     (12,23),915
>     (12,24),1489
>     (13,13),20811
>     (13,14),351
>     (13,15),590
>     (13,16),469
>     (13,17),1085
>     (13,18),4599
>     (13,19),17273
>     (13,20),665
>     (13,21),39454
>     (13,22),13080
>     (13,23),8016
>     (13,24),5179
>     (14,14),209
>     (14,15),10
>     (14,16),19
>     (14,17),106
>     (14,18),197
>     (14,19),521
>     (14,20),23
>     (14,21),989
>     (14,22),439
>     (14,23),102
>     (14,24),205
>     (15,15),7203
>     (15,16),435
>     (15,17),104
>     (15,18),684
>     (15,19),1665
>     (15,20),83
>     (15,21),2969
>     (15,22),990
>     (15,23),308
>     (15,24),1138
>     (16,16),6964
>     (16,17),41
>     (16,18),1026
>     (16,19),3187
>     (16,20),112
>     (16,21),5325
>     (16,22),1459
>     (16,23),323
>     (16,24),939
>     (17,17),6152
>     (17,18),690
>     (17,19),2245
>     (17,20),48
>     (17,21),4696
>     (17,22),1527
>     (17,23),136
>     (17,24),588
>     (18,18),7507
>     (18,19),13163
>     (18,20),345
>     (18,21),24793
>     (18,22),6124
>     (18,23),3036
>     (18,24),2741
>     (19,19),24247
>     (19,20),101
>     (19,21),50396
>     (19,22),7392
>     (19,23),285
>     (19,24),3380
>     (20,20),4778
>     (20,21),9191
>     (20,22),603
>     (20,23),65
>     (20,24),759
>     (21,21),202621
>     (21,22),58087
>     (21,23),178
>     (21,24),12625
>     (22,22),13929
>     (22,23),2318
>     (22,24),4548
>     (23,23),1332
>     (23,24),1806
>     (24,24),7828
>     res: scala.collection.mutable.ArrayBuffer[(Int, Int, Long)] = ArrayBuffer((0,0,7371), (0,1,3727), (0,2,218), (0,3,17154), (0,4,3248), (0,5,868), (0,6,164), (0,7,41), (0,8,633), (0,9,7), (0,10,22), (0,11,320), (0,12,196), (0,13,1299), (0,14,18), (0,15,134), (0,16,353), (0,17,4292), (0,18,2313), (0,19,14386), (0,20,102), (0,21,31880), (0,22,7535), (0,23,121), (0,24,2273), (1,1,113474), (1,2,238), (1,3,18615), (1,4,10124), (1,5,2367), (1,6,160), (1,7,16), (1,8,2636), (1,9,6), (1,10,23), (1,11,1531), (1,12,4584), (1,13,25047), (1,14,70), (1,15,250), (1,16,49), (1,17,108), (1,18,2153), (1,19,9363), (1,20,174), (1,21,32431), (1,22,6530), (1,23,549), (1,24,1871), (2,2,31418), (2,3,9530), (2,4,3135), (2,5,673), (2,6,19), (2,7,4), (2,8,2052), (2,9,47), (2,10,30), (2,11,111), (2,12,484), (2,13,2657), (2,14,10), (2,15,99), (2,16,55), (2,17,98), (2,18,1479), (2,19,5207), (2,20,61), (2,21,11799), (2,22,6156), (2,23,163), (2,24,939), (3,3,48104), (3,4,9617), (3,5,22609), (3,6,484), (3,7,18), (3,8,3735), (3,9,4), (3,10,75), (3,11,537), (3,12,362), (3,13,9298), (3,14,33), (3,15,449), (3,16,299), (3,17,199), (3,18,16555), (3,19,32148), (3,20,445), (3,21,72902), (3,22,17320), (3,23,433), (3,24,9529), (4,4,56089), (4,5,3203), (4,6,224), (4,7,15), (4,8,21621), (4,9,3), (4,10,68), (4,11,355), (4,12,1375), (4,13,37974), (4,14,15), (4,15,1423), (4,16,284), (4,17,118), (4,18,6513), (4,19,23076), (4,20,368), (4,21,64318), (4,22,10097), (4,23,363), (4,24,8721), (5,5,1919), (5,6,128), (5,7,141), (5,8,764), (5,9,2), (5,10,106), (5,11,206), (5,12,545), (5,13,2566), (5,14,8019), (5,15,127), (5,16,318), (5,17,2479), (5,18,1300), (5,19,4650), (5,20,239), (5,21,10322), (5,22,2603), (5,23,473), (5,24,998), (6,6,129), (6,7,5), (6,8,53), (6,9,17), (6,10,0), (6,11,25), (6,12,228), (6,13,422), (6,14,0), (6,15,18), (6,16,15), (6,17,14), (6,18,38), (6,19,459), (6,20,5), (6,21,1006), (6,22,237), (6,23,83), (6,24,174), (7,7,14743), (7,8,445), (7,9,15), (7,10,30), (7,11,459), (7,12,1130), (7,13,2654), (7,14,486), (7,15,3), (7,16,0), (7,17,83), (7,18,177), (7,19,1016), (7,20,92), (7,21,2937), (7,22,2571), (7,23,397), (7,24,989), (8,8,2782), (8,9,2), (8,10,92), (8,11,67), (8,12,1038), (8,13,2460), (8,14,7), (8,15,218), (8,16,577), (8,17,66), (8,18,2142), (8,19,7024), (8,20,120), (8,21,15079), (8,22,2215), (8,23,304), (8,24,1120), (9,9,1056), (9,10,135), (9,11,77), (9,12,193), (9,13,595), (9,14,28), (9,15,36), (9,16,17), (9,17,55), (9,18,248), (9,19,746), (9,20,31), (9,21,1213), (9,22,523), (9,23,162), (9,24,211), (10,10,487), (10,11,138), (10,12,318), (10,13,1185), (10,14,78), (10,15,81), (10,16,80), (10,17,141), (10,18,759), (10,19,2033), (10,20,61), (10,21,3839), (10,22,1135), (10,23,344), (10,24,574), (11,11,458), (11,12,440), (11,13,1815), (11,14,3080), (11,15,64), (11,16,50), (11,17,140), (11,18,938), (11,19,4189), (11,20,101), (11,21,9321), (11,22,3069), (11,23,662), (11,24,996), (12,12,10057), (12,13,6212), (12,14,186), (12,15,127), (12,16,62), (12,17,213), (12,18,716), (12,19,2728), (12,20,100), (12,21,6047), (12,22,1679), (12,23,915), (12,24,1489), (13,13,20811), (13,14,351), (13,15,590), (13,16,469), (13,17,1085), (13,18,4599), (13,19,17273), (13,20,665), (13,21,39454), (13,22,13080), (13,23,8016), (13,24,5179), (14,14,209), (14,15,10), (14,16,19), (14,17,106), (14,18,197), (14,19,521), (14,20,23), (14,21,989), (14,22,439), (14,23,102), (14,24,205), (15,15,7203), (15,16,435), (15,17,104), (15,18,684), (15,19,1665), (15,20,83), (15,21,2969), (15,22,990), (15,23,308), (15,24,1138), (16,16,6964), (16,17,41), (16,18,1026), (16,19,3187), (16,20,112), (16,21,5325), (16,22,1459), (16,23,323), (16,24,939), (17,17,6152), (17,18,690), (17,19,2245), (17,20,48), (17,21,4696), (17,22,1527), (17,23,136), (17,24,588), (18,18,7507), (18,19,13163), (18,20,345), (18,21,24793), (18,22,6124), (18,23,3036), (18,24,2741), (19,19,24247), (19,20,101), (19,21,50396), (19,22,7392), (19,23,285), (19,24,3380), (20,20,4778), (20,21,9191), (20,22,603), (20,23,65), (20,24,759), (21,21,202621), (21,22,58087), (21,23,178), (21,24,12625), (22,22,13929), (22,23,2318), (22,24,4548), (23,23,1332), (23,24,1806), (24,24,7828))

``` scala
// Now we loop through all subgraphs with respect to each cluster index. 
val connectionsInSubgraph = collection.mutable.Map[Int, Long]()
for (i <- Range(0,3)) {
  for (j <- Range(0,3)) {
    
  }
  val subgraph = graph.subgraph(epred = (id, attr) => attr._2 == i)
  connectionsInSubgraph(i) = subgraph.edges.count()
}
```

``` scala
// Now we loop through all subgraphs with respect to each cluster index. 
val connectionsInSubgraph = collection.mutable.Map[Int, Long]()
for (i <- Range(0,3)) {
  val subgraph = graph.subgraph(vpred = (id, attr) => attr._2 == i)
  connectionsInSubgraph(i) = subgraph.edges.count()
}
```

>     connectionsInSubgraph: scala.collection.mutable.Map[Int,Long] = Map(2 -> 31418, 1 -> 113474, 0 -> 7371)

URLS
====

``` scala
val allUrlAndDomains = (sc.textFile("/FileStore/tables/7yaczwjd1501068230338/checkedUrlWithNetLocFinal.csv")
  .map(x => x.split(","))
  .map(x => (x(0).toString, x(1).toString))
  .toDF(Seq("URL", "Domain"): _*))
```

>     allUrlAndDomains: org.apache.spark.sql.DataFrame = [URL: string, Domain: string]

``` scala
allUrlAndDomains.count
```

>     res169: Long = 30562

``` scala
val urlNetwork = retweetNetwork.join(allUrlAndDomains, Seq("URL"), "outer")
```

>     urlNetwork: org.apache.spark.sql.DataFrame = [URL: string, OPostUserIDinRT: bigint ... 5 more fields]

``` scala
display(urlNetwork)
```

| URL                                                                                                                                                           | OPostUserIDinRT        | OPostUserSNinRT | CPostUserID            | CPostUserSN      | Weight | Domain                     |
|---------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------|-----------------|------------------------|------------------|--------|----------------------------|
| http://53eig.ht/2rp4MtB                                                                                                                                       | 2.62872637e8           | MoisesNaim      | 3.21589427e8           | stephenWalt      | 1.0    | null                       |
| http://Cambridge-United.co.uk                                                                                                                                 | 1.69046895e8           | CambridgeUtdFC  | 2.0317326e7            | Annkell          | 1.0    | www.cambridge-united.co.uk |
| http://Cambridge-United.co.uk                                                                                                                                 | 1.69046895e8           | CambridgeUtdFC  | 4.0623515e7            | AGraizevsky      | 1.0    | www.cambridge-united.co.uk |
| http://Cambridge-United.co.uk                                                                                                                                 | 1.69046895e8           | CambridgeUtdFC  | 1.439542537e9          | DSole1           | 1.0    | www.cambridge-united.co.uk |
| http://FilmFreeway.com                                                                                                                                        | 6.9775165e7            | Jeff\_Hansen    | 1.7156e7               | seashepherd      | 1.0    | null                       |
| http://FilmFreeway.com                                                                                                                                        | 4.5830001e7            | Wilygoose       | 6.3141362e7            | corinstuart      | 1.0    | null                       |
| http://FilmFreeway.com                                                                                                                                        | 4.5830001e7            | Wilygoose       | 6.3141362e7            | corinstuart      | 1.0    | null                       |
| http://FilmFreeway.com                                                                                                                                        | 4.5830001e7            | Wilygoose       | 6.3141362e7            | corinstuart      | 1.0    | null                       |
| http://FilmFreeway.com                                                                                                                                        | 4.5830001e7            | Wilygoose       | 6.3141362e7            | corinstuart      | 1.0    | null                       |
| http://FilmFreeway.com                                                                                                                                        | 1.405527242e9          | PFMediaUK       | 2.190444236e9          | DannersJameson   | 1.0    | null                       |
| http://FilmFreeway.com                                                                                                                                        | 2.791892884e9          | LiTphils        | 3.31294754e8           | KellConnery      | 1.0    | null                       |
| http://FilmFreeway.com                                                                                                                                        | 7.04984124952436736e17 | punks4westpapua | 2.3586483e7            | evaa31           | 1.0    | null                       |
| http://FilmFreeway.com                                                                                                                                        | 2.177985414e9          | GenesiusPicture | 3.130056581e9          | DebbieGfilm      | 1.0    | null                       |
| http://FilmFreeway.com                                                                                                                                        | 4.22048034e8           | JackGrewar      | 5.6108861e7            | bmattyb323       | 1.0    | null                       |
| http://Gov.uk/register-to-vote                                                                                                                                | 1.544495239e9          | ShefHallamLab   | 4.904071421e9          | Stannington\_Lab | 1.0    | null                       |
| http://Wonderful.org                                                                                                                                          | 3.909202761e9          | wonderful\_org  | 2.40869183e8           | al\_ritchie      | 1.0    | null                       |
| http://Www.kendalpoetryfestival.co.uk                                                                                                                         | 7.05434144910741504e17 | KendalPoetry    | 5.97696492e8           | hannahlowepoet   | 1.0    | null                       |
| http://a.msn.com/01/en-gb/BBCnjbg?ocid=st                                                                                                                     | 2.04864531e8           | EtonOldBoys     | 1.82342346e8           | HerefordLabour   | 1.0    | www.independent.co.uk      |
| http://a.msn.com/01/en-gb/BBCnjbg?ocid=st                                                                                                                     | 2.04864531e8           | EtonOldBoys     | 1.82342346e8           | HerefordLabour   | 1.0    | www.independent.co.uk      |
| http://a.msn.com/r/2/BBByhyv?a=1&m=EN-GB                                                                                                                      | 5.26621053e8           | FountainsCourt  | 8.111994504881152e17   | amicon\_13       | 1.0    | null                       |
| http://abc7.la/2s9Fov8                                                                                                                                        | 1.6374678e7            | ABC7            | 7.4837469e7            | sicvic24         | 1.0    | null                       |
| http://absrad.io/2rB8GD7                                                                                                                                      | 1.6085557e7            | absoluteradio   | 1.1361392e8            | NoelGallagher    | 1.0    | null                       |
| http://agbr.me/2tiss6K                                                                                                                                        | 1.123082485e9          | arianagrandebr  | 8.49084665394864128e17 | ArianaG04076749  | 1.0    | null                       |
| http://aje.io/mjjs                                                                                                                                            | 4970411.0              | AJEnglish       | 4.85254523e8           | HamdunH          | 1.0    | null                       |
| http://aje.io/y9ql                                                                                                                                            | 4970411.0              | AJEnglish       | 7.26485502141014016e17 | TurdWorldWar     | 1.0    | null                       |
| http://allafrica.com/stories/201705020090.html?utm\_campaign=allafrica%3Ainternal&utm\_medium=social&utm\_source=twitter&utm\_content=promote%3Aaans%3Aabafbt | 1.6683014e7            | allafrica       | 1.288430442e9          | eyesopenershaw   | 1.0    | null                       |
| http://amzn.to/2aTHU2e                                                                                                                                        | 1.412560921e9          | myrddinsheir    | 3.18686456e8           | mystery1165      | 1.0    | null                       |
| http://apne.ws/2qL6k0x                                                                                                                                        | 5.1241574e7            | AP              | 6.22938139e8           | HUBBE\_PAKISTAN  | 1.0    | null                       |
| http://apne.ws/2qL6k0x                                                                                                                                        | 5.1241574e7            | AP              | 2.531129465e9          | ZoraGouhary      | 1.0    | null                       |
| http://apne.ws/2syD0y1                                                                                                                                        | 5.1241574e7            | AP              | 8.49027382090416128e17 | bluenewsnow      | 1.0    | null                       |

Truncated to 30 rows

``` scala
urlNetwork.count()
```

>     res167: Long = 6382207

``` scala
urlNetwork.filter($"URL"==="").count()
```

>     res175: Long = 5344405

``` scala
6382207-5344405 // number of tweets with URLs
```

>     res176: Int = 1037802

``` scala
urlNetwork.select("URL").distinct().count()
```

>     res168: Long = 271684

``` scala
urlNetwork.select("URL","Domain").distinct().count()
```

``` scala
dbutils.fs.rm("/datasets/MEP/GB/RetweetUrlNetworkAsParquetDF",true)
```

>     res172: Boolean = true

``` scala
// write this urlNetwork to parquet for processing later
urlNetwork.write.parquet("/datasets/MEP/GB/RetweetUrlNetworkAsParquetDF") 
```

