import org.apache.spark.SparkContext
import org.apache.spark.SparkConf

object SimpleScalaSpark {

  def main(args: Array[String]) {
    // you need to change this to some local file you would like to read
    val myFile = "/home/raazesh/all/git/scalable-data-science/index.html" // Should be some file on your system
    val conf = new SparkConf().setAppName("Simple Application").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val myData = sc.textFile(myFile, 2).cache()
    val num_data_s = myData.filter(line => line.contains("data")).count()
    println("Lines with data: %s".format(num_data_s))
  }

}
