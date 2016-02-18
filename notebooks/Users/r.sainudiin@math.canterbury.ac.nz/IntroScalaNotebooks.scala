// Databricks notebook source exported at Thu, 18 Feb 2016 04:57:04 UTC
// MAGIC %md
// MAGIC ![Databricks Logo](http://training.databricks.com/databricks_guide/databricks_logo_400px.png)
// MAGIC # **Introduction to Scala Notebooks** 
// MAGIC 
// MAGIC * This introduction notebook describes how to get started running Scala code in Notebooks.
// MAGIC * If you have not already done so, please review the [Welcome to Databricks](/#workspace/databricks_guide/00 Welcome to Databricks) guide.

// COMMAND ----------

// MAGIC %md
// MAGIC ### Clone Or Import This Notebook this notebook
// MAGIC * From the **File** menu at the top left of this notebook, choose **Clone** or click **Import Notebook** on the top right. This will allow you to interactively execute code cells as you proceed through the notebook.
// MAGIC 
// MAGIC ![Menu Bar Clone Notebook](http://training.databricks.com/databricks_guide/2.8/clone.png) 
// MAGIC * Enter a name and a desired location for your cloned notebook (i.e. Perhaps clone to your own user directory or the "Shared" directory.)
// MAGIC * Navigate to the location you selected (e.g. click Menu > Workspace > `Your cloned location`)

// COMMAND ----------

// MAGIC %md
// MAGIC ### **Attach** the Notebook to a **cluster**
// MAGIC * A **Cluster** is a group of machines which can run commands in cells.
// MAGIC * Check the upper left corner of your notebook to see if it is **Attached** or **Detached**.
// MAGIC * If **Detached**, click on the right arrow and select a cluster to attach your notebook to. 
// MAGIC   * If there is no running cluster, create one as described in the [Welcome to Databricks](/#workspace/databricks_guide/00 Welcome to Databricks) guide.
// MAGIC 
// MAGIC ![Attach Notebook](http://training.databricks.com/databricks_guide/2.8/detached.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC #### ![Quick Note](http://training.databricks.com/databricks_guide/icon_note3_s.png) **Cells** are units that make up notebooks
// MAGIC ![A Cell](http://training.databricks.com/databricks_guide/cell.png)
// MAGIC 
// MAGIC Cells each have a type - including **scala**, **python**, **sql**, **R**, **markdown**, **filesystem**, and **shell**.
// MAGIC * While cells default to the type of the Notebook, other cell types are supported as well.
// MAGIC * This cell is in **markdown** and is used for documentation. [Markdown](http://en.wikipedia.org/wiki/Markdown) is a simple text formatting syntax.
// MAGIC ***

// COMMAND ----------

// MAGIC %md 
// MAGIC ### ** Create** and **Edit** a New Markdown Cell in this Notebook
// MAGIC * When you mouse between cells, a + sign will pop up in the center that you can click on to create a new cell.
// MAGIC 
// MAGIC  ![New Cell](http://training.databricks.com/databricks_guide/create_new_cell.png)
// MAGIC * Type **``%md Hello, world!``** into your new cell (**``%md``** indicates the cell is markdown).
// MAGIC 
// MAGIC 
// MAGIC 
// MAGIC * Click out of the cell to see the cell contents update.
// MAGIC   
// MAGIC   ![Run cell](http://training.databricks.com/databricks_guide/run_cell.png)
// MAGIC   

// COMMAND ----------

// MAGIC %md Hello, world!

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC #### ![Quick Note](http://training.databricks.com/databricks_guide/icon_note3_s.png) **Markdown Cell Tips**
// MAGIC * To change a non-markdown cell to markdown, add **%md** to very start of the cell.
// MAGIC * After updating the contents of a markdown cell, click out of the cell to update the formatted contents of a markdown cell.
// MAGIC * To edit an existing markdown cell, **doubleclick** the cell.
// MAGIC ***

// COMMAND ----------

// MAGIC %md
// MAGIC ### Run a **Scala Cell**
// MAGIC * Run the following scala cell.
// MAGIC * Note: There is no need for any special indicator (such as ``%md``) necessary to create a Scala cell in a Scala notebook.
// MAGIC * Make sure the cell contents updates before moving on.
// MAGIC * Press **Shift+Enter** when in the cell to run it and proceed to the next cell.
// MAGIC   * The cells contents should update.
// MAGIC   * Alternately, press **Ctrl+Enter** when in a cell to **run** it, but not proceed to the next cell.

// COMMAND ----------

println(System.currentTimeMillis)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Running **Spark**
// MAGIC The variable **sc** allows you to access a Spark Context to run your Spark programs.
// MAGIC * For more information about Spark, please refer to [Spark Overview](https://spark.apache.org/docs/latest/)
// MAGIC 
// MAGIC **NOTE: Do not create the *sc* variable - it is already initialized for you. **

// COMMAND ----------

val words = sc.parallelize(Array("hello", "world", "goodbye", "hello", "again"))
val wordcounts = words.map(s => (s, 1)).reduceByKey(_ + _).collect() 

// COMMAND ----------

// Exercise: Calculate the number of unique words in the "words" RDD here.
// (Hint: The answer should be 4.)

// COMMAND ----------

// Exercise: Create an RDD of numbers, and find the mean.

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC %md
// MAGIC ### Working with **Spark SQL and DataFrames**
// MAGIC The variable **sqlContext** allows you to access a Spark SQL Context to work with Spark SQL and DataFrames.
// MAGIC * Scala can be used to create Spark [DataFrames](http://spark.apache.org/docs/latest/sql-programming-guide.html) - a distributed collection of data organized into named columns.
// MAGIC * DataFrames are created by appending ``.toDF()`` to the Scala RDD
// MAGIC 
// MAGIC **NOTE: Do not create the *sqlContext* variable - it is already initialized for you. **

// COMMAND ----------

// Define the schema using a case class
case class MyCaseClass(key: String, group: String, value: Int)

// Create the RDD (using sc.parallelize) and transforms it into a DataFrame
val df = sc.parallelize(Array(MyCaseClass("f", "consonants", 1),
   MyCaseClass("g", "consonants", 2),
   MyCaseClass("h", "consonants", 3),
   MyCaseClass("i", "vowels", 4),
   MyCaseClass("j", "consonants", 5))
 ).toDF()

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC #### **Show this data**
// MAGIC Use the **``display``** command to view a DataFrame in a notebook.

// COMMAND ----------

display(df)

// COMMAND ----------

// MAGIC %md 
// MAGIC ***
// MAGIC #### ![Quick Note](http://training.databricks.com/databricks_guide/icon_note3_s.png) **The visualization above is interactive**
// MAGIC * Click on the **Chart Button** ![Chart Button](http://training.databricks.com/databricks_guide/chart_button.png) to toggle the view.
// MAGIC * Try different types of graphs by clicking on the arrow next to the chart button.
// MAGIC * If you have selected a graph, you can click on **Plot Options...** for even more ways to customize the view.
// MAGIC ***

// COMMAND ----------

// Exercise: Create a DataFrame and display it. 
// Can you use the "Plot Options" to plot the group vs. the sum of the values in that group?
// (Hint: Vowels = 6, and consonants = 9)

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC ## ![Quick Note](http://training.databricks.com/databricks_guide/icon_note3_s.png) **Where to Go Next**
// MAGIC 
// MAGIC We've now covered the basics of a Databricks Scala Notebook.  Practice the optional exercises below for more scala exercises, look to the following notebooks in other languages, or go to the Databricks Product Overview.
// MAGIC * [Notebook Tutorials in Python](/#workspace/databricks_guide/01 Intro Notebooks/1 Intro Python Notebooks)
// MAGIC * [Notebook Tutorials in SQL](/#workspace/databricks_guide/01 Intro Notebooks/3 Intro SQL Notebooks)
// MAGIC * [Notebook Tutorials in R](/#workspace/databricks_guide/01 Intro Notebooks/4 Intro R Notebooks)
// MAGIC * [Databricks Product Overview](/#workspace/databricks_guide/02 Product Overview/00 Product Overview)
// MAGIC ***

// COMMAND ----------

// MAGIC %md ## **Optional Tasks**

// COMMAND ----------

// MAGIC %md
// MAGIC ### **Importing Standard Scala and Java libraries**
// MAGIC * For other libraries that are not available by default, you can upload other libraries to the Workspace.
// MAGIC * Refer to the **[Libraries](/#workspace/databricks_guide/02 Product Overview/07 Libraries)** guide for more details.

// COMMAND ----------

import scala.math._
val x = min(1, 10)

// COMMAND ----------

import java.util.HashMap
val map = new HashMap[String, Int]()
map.put("a", 1)
map.put("b", 2)
map.put("c", 3)
map.put("d", 4)
map.put("e", 5)


// COMMAND ----------

// MAGIC %md
// MAGIC ### **Using Spark SQL within a Scala Notebook**
// MAGIC You can use execute SQL commands within a scala notebook by invoking **``%sql``** or using **``sqlContext.sql(...)``**.

// COMMAND ----------

// MAGIC %sql show databases

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC #### **Use ``registerTempTable`` on a DataFrame** 
// MAGIC * Register the above DataFrame table (built in scala) for SQL queries.
// MAGIC * Temporary tables are not meant to be persistent, i.e. they will not survive cluster restarts.

// COMMAND ----------

df.registerTempTable("ScalaTempTable")

// COMMAND ----------

// MAGIC %sql describe ScalaTempTable

// COMMAND ----------

// MAGIC %md
// MAGIC #### **Visualizations and Spark SQL**
// MAGIC * A visualization appears automatically for the output of a **SQL select** statement in notebooks (no need to call ``display``).

// COMMAND ----------

// MAGIC %sql select * from ScalaTempTable

// COMMAND ----------

// MAGIC %md
// MAGIC #### ** Persist DataFrames into Tables **
// MAGIC Use **``saveAsTable``** to persist tables to be used in other notebooks.
// MAGIC * These table definitions will persist even after cluster restarts.

// COMMAND ----------

sqlContext.sql("DROP TABLE IF EXISTS ScalaTestTable")
df.write.saveAsTable("ScalaTestTable")

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC ![Quick Note](http://training.databricks.com/databricks_guide/icon_note3_s.png) For more information on working with Scala and Spark SQL, please refer to [Spark SQL Programming Guide](https://spark.apache.org/docs/latest/sql-programming-guide.html)
// MAGIC ***

// COMMAND ----------

// MAGIC %md
// MAGIC ### ** Display HTML **
// MAGIC Display HTML within your notebook, using the **displayHTML** command.

// COMMAND ----------

displayHTML("<h3 style=\"color:blue\">Blue Text</h3>")