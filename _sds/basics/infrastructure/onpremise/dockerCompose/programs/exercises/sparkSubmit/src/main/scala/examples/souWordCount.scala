package org.lamastex.exercises


import org.apache.spark._

/** Computes lines in sou */
object souWordCount {
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("souWordCount")
    val spark = new SparkContext(conf)
    val souRdd = spark.textFile("hdfs:///datasets/sou/*")
    val count = souRdd.count()
    println("SOU lin ecount = ", count)
    // write more code from snippets you learnt in 006_WordCount notebook
    // your goal is to count the number of each word across all state of 
    // the union addresses and report the 100 most frequently used words
    // and the 100 least frequently used words
    spark.stop()
  }
}
