// Databricks notebook source - raaz: for pure source code $ grep -v -e '^[[:space:]]*$' -e '^//' permutationTest.scala
// MAGIC %md
// MAGIC # A Scalable Permutation Test
// MAGIC 
// MAGIC Raazesh Sainudiin

// COMMAND ----------

// MAGIC %md
// MAGIC ### Example dataframe

// COMMAND ----------

import org.apache.spark.sql.functions._

case class Person(name: String, age: Long)

val data_ds = Seq(Person("Andy", 32),Person("Raaz",46),Person("Kajsa", 28), Person("Danilo",23)).toDS()
//df: org.apache.spark.sql.DataFrame = [key: int]
val data_df = data_ds.toDF()
data_df.show()

// COMMAND ----------

// MAGIC %md
// MAGIC ### Zip with index for randomisation ordering

// COMMAND ----------

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.types.{LongType, StructField, StructType}
import org.apache.spark.sql.Row

def dfZipWithIndex(
  df: DataFrame,
  offset: Int = 1,
  colName: String = "rand_id",
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

// MAGIC %md
// MAGIC ### Permute rows

// COMMAND ----------

def permuteDataFrame(
  data_df: DataFrame,
  permute: Boolean
  // if you want to pass default parameters to dfZipWithIndex
  //offset: Int = 1,
  //colName: String = "rand_id",
  //inFront: Boolean = true
) : DataFrame = {
var outdf = spark.emptyDataFrame
if (permute) {
  outdf = dfZipWithIndex(data_df.withColumn("rand", rand()).sort("rand"))
}
else {
  outdf = dfZipWithIndex(data_df.withColumn("rand", rand()))
}
  outdf.drop("rand")
}

// COMMAND ----------

permuteDataFrame(data_df,false).show // original data without permutation

// COMMAND ----------

permuteDataFrame(data_df,true).show //  data after permutation

// COMMAND ----------

// MAGIC %md
// MAGIC ### A test statistic for permutation test

// COMMAND ----------

def TestStatsDiffOfMeans(
  df: DataFrame,
  colName: String,
  n1: Long // assert n1 < n=df.count and implied that n2 is n-n1
) : Double = {
  val mean1 = df.filter($"rand_id" <= n1)
                .select(colName).agg(avg(colName) as "mean").first.getDouble(0)
  val mean2 = df.filter($"rand_id" > n1)
                .select(colName).agg(avg(colName) as "mean").first.getDouble(0)
  val t = mean1 - mean2 // test statistic
  t
}

// COMMAND ----------

def TestStatsNullDist(
  df: DataFrame,
  DoubleColNameForTestStats: String,
  n1: Long, // assert n1 < n=df.count and implied that n2 is n-n1
  MonteCarloSamples: Integer
) : scala.collection.immutable.Vector[Double] = {
val n = df.count()
assert(n1 < n && n1 > 0 && n > 1)
//val n1 = 2 
// first n1 samples from population 1. Note n2=n-n1 remaining last samples are from pop 2
//val DoubleColNameForTestStats = "age"
val obs_t = TestStatsDiffOfMeans(permuteDataFrame(df,false),DoubleColNameForTestStats,n1)
var NullTestStats = Vector[Double]()
var MonteCarloPValue = 0.0
for (i <- 1 to MonteCarloSamples) {
  val t = TestStatsDiffOfMeans(permuteDataFrame(df,true),DoubleColNameForTestStats,n1)
  NullTestStats = NullTestStats :+ t
  if (t >= obs_t){
    MonteCarloPValue += 1.0 
  }
  //println(i,t)
}
  println(MonteCarloPValue/MonteCarloSamples)
  NullTestStats
}


// COMMAND ----------

TestStatsNullDist(data_df,"age",2,100)

// COMMAND ----------

// MAGIC %md
// MAGIC If you know of a more efficient way to do this then email raazesh.sainudiin@gmail.com
