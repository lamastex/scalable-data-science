// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC # Utilities Needed for Mass Media Data
// MAGIC 
// MAGIC Johannes Graner ([LinkedIn](https://www.linkedin.com/in/johannes-graner-475677129/)), Albert Nilsson ([LinkedIn](https://www.linkedin.com/in/albert-nilsson-09b62b191/)) and Raazesh Sainudiin ([LinkedIn](https://www.linkedin.com/in/raazesh-sainudiin-45955845/))
// MAGIC 
// MAGIC 2020, Uppsala, Sweden
// MAGIC 
// MAGIC This project was supported by Combient Mix AB through summer internships at:
// MAGIC 
// MAGIC Combient Competence Centre for Data Engineering Sciences, 
// MAGIC Department of Mathematics, 
// MAGIC Uppsala University, Uppsala, Sweden
// MAGIC 
// MAGIC ---
// MAGIC 
// MAGIC Here, certain delta.io tables are loaded.
// MAGIC These tables have been prepared already.
// MAGIC 
// MAGIC You will not be able to load them directly but libraries used in the process are all open-sourced.

// COMMAND ----------

object GdeltUtils {
  private val gdeltV1Path = "s3a://XXXXXX/GDELT/delta/bronze/v1/"
  private val eoiCheckpointPath = "s3a://XXXXXX/.../texata/"
  private val poiCheckpointPath = "s3a://XXXXXX/.../person_graph/"

  
  def getGdeltV1Path = gdeltV1Path
  def getEOICheckpointPath = eoiCheckpointPath
  def getPOICheckpointPath = poiCheckpointPath
}

// COMMAND ----------

// MAGIC %python
// MAGIC class GdeltUtils:
// MAGIC   
// MAGIC   def getEOICheckpointPath():
// MAGIC     return "s3a://XXXXXX/.../texata/"