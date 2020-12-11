// Databricks notebook source
// MAGIC %md
// MAGIC # [ScaDaMaLe, Scalable Data Science and Distributed Machine Learning](https://lamastex.github.io/scalable-data-science/sds/3/x/)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # Sketching

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC - To get an overview of sketching in under 30 minutes see this **Spark-AI 2020 Talk on Apache Sketches by Lee RHodes*:
// MAGIC   - https://databricks.com/session_na20/a-production-quality-sketching-library-for-the-analysis-of-big-data
// MAGIC   - Watch Video of the Talk  Now/Later (29 minutes)
// MAGIC     - [![Spark Transformations by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/WPwCnswDbOU/0.jpg)](https://www.youtube.com/watch?v=WPwCnswDbOU?rel=0&autoplay=1&modestbranding=1)
// MAGIC - **Other great recent talks:**
// MAGIC   - Yeshwant Vijaykumar of Adobe Inc at Spark+AI Summit 2020:
// MAGIC     - [https://databricks.com/session_na20/everyday-probabilistic-data-structures-for-humans](https://databricks.com/session_na20/everyday-probabilistic-data-structures-for-humans)
// MAGIC   - Simeon Simeonov of Swoop at Spark+AI Summit 2020:
// MAGIC     - [https://databricks.com/session_na20/high-performance-analytics-with-probabilistic-data-structures-the-power-of-hyperloglog](https://databricks.com/session_na20/high-performance-analytics-with-probabilistic-data-structures-the-power-of-hyperloglog)

// COMMAND ----------

// MAGIC %md
// MAGIC ## The Challenge: Fast, Approximate Analysis of Big Data
// MAGIC 
// MAGIC  - [https://datasketches.apache.org/docs/Background/TheChallenge.html](https://datasketches.apache.org/docs/Background/TheChallenge.html)

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
displayHTML(frameIt("https://datasketches.apache.org/docs/Background/TheChallenge.html",800))

// COMMAND ----------

// MAGIC %md
// MAGIC ## Sketch Origins
// MAGIC 
// MAGIC - [https://datasketches.apache.org/docs/Background/SketchOrigins.html](https://datasketches.apache.org/docs/Background/SketchOrigins.html)
// MAGIC 
// MAGIC READ: Philippe Flajolet and Nigel Martin (1985), *Probabilistic Counting Algorithms for Data Base Applicaitons*, [http://db.cs.berkeley.edu/cs286/papers/flajoletmartin-jcss1985.pdf](http://db.cs.berkeley.edu/cs286/papers/flajoletmartin-jcss1985.pdf).

// COMMAND ----------

displayHTML(frameIt("https://datasketches.apache.org/docs/Background/SketchOrigins.html",800))

// COMMAND ----------

// MAGIC %md
// MAGIC ## Sketch Elements
// MAGIC 
// MAGIC - [https://datasketches.apache.org/docs/Background/SketchElements.html](https://datasketches.apache.org/docs/Background/SketchElements.html)

// COMMAND ----------

displayHTML(frameIt("https://datasketches.apache.org/docs/Background/SketchElements.html",800))

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### Apache Sketches
// MAGIC - Deep dive into the following three links at [https://datasketches.apache.org/](https://datasketches.apache.org/):
// MAGIC   - Architecture And Design, 
// MAGIC   - Sketch Families and 
// MAGIC   - System Integrations 
// MAGIC   - Community
// MAGIC   - Research
// MAGIC - Sketcheing in Apache Spark: https://datasketches.apache.org/docs/Theta/ThetaSparkExample.html
// MAGIC - See docs:
// MAGIC   - [https://datasketches.apache.org/docs/Background/Presentations.html](https://datasketches.apache.org/docs/Background/Presentations.html)
// MAGIC   - 
// MAGIC 
// MAGIC ### Some general recent talks/blogs on various sketches:
// MAGIC 
// MAGIC - https://databricks.com/session_na20/high-performance-analytics-with-probabilistic-data-structures-the-power-of-hyperloglog
// MAGIC - https://databricks.com/blog/2016/05/19/approximate-algorithms-in-apache-spark-hyperloglog-and-quantiles.html
// MAGIC   - the above has a databricks notebook you can try to self-study
// MAGIC - 
// MAGIC 
// MAGIC 
// MAGIC We will next focus on a specific sketch called **T-Digest** for approximating extreme quantiles:
// MAGIC   - https://databricks.com/session/one-pass-data-science-in-apache-spark-with-generative-t-digests
// MAGIC   - https://databricks.com/session/sketching-data-with-t-digest-in-apache-spark
// MAGIC   