// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # Performance Tuning
// MAGIC ## Spark Sql Programming Guide
// MAGIC 
// MAGIC If you have read the spark-SQL paper and have some idea of how distributed sorting and joining work then you will need to know the following part of the programming guide to tune the performance of Spark SQL queries:
// MAGIC 
// MAGIC  - https://spark.apache.org/docs/latest/sql-performance-tuning.html

// COMMAND ----------

//This allows easy embedding of publicly available information into any other notebook
//Example usage:
// displayHTML(frameIt("https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation#Topics_in_LDA",250))
def frameIt( u:String, h:Int ) : String = {
      """<iframe 
 src=""""+ u+""""
 width="95%" height="""" + h + """">
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe>"""
   }
displayHTML(frameIt("https://spark.apache.org/docs/latest/sql-performance-tuning.html",700))

// COMMAND ----------

