// Databricks notebook source exported at Tue, 2 Feb 2016 01:56:37 UTC

![Databricks Logo](http://training.databricks.com/databricks_guide/databricks_logo_400px.png)
# **Introduction to Scala Notebooks** 

* This introduction notebook describes how to get started running Scala code in Notebooks.
* If you have not already done so, please review the [Welcome to Databricks](/#workspace/databricks_guide/00 Welcome to Databricks) guide.





### Clone Or Import This Notebook this notebook
* From the **File** menu at the top left of this notebook, choose **Clone** or click **Import Notebook** on the top right. This will allow you to interactively execute code cells as you proceed through the notebook.

![Menu Bar Clone Notebook](http://training.databricks.com/databricks_guide/2.8/clone.png) 
* Enter a name and a desired location for your cloned notebook (i.e. Perhaps clone to your own user directory or the "Shared" directory.)
* Navigate to the location you selected (e.g. click Menu > Workspace > `Your cloned location`)





### **Attach** the Notebook to a **cluster**
* A **Cluster** is a group of machines which can run commands in cells.
* Check the upper left corner of your notebook to see if it is **Attached** or **Detached**.
* If **Detached**, click on the right arrow and select a cluster to attach your notebook to. 
  * If there is no running cluster, create one as described in the [Welcome to Databricks](/#workspace/databricks_guide/00 Welcome to Databricks) guide.

![Attach Notebook](http://training.databricks.com/databricks_guide/2.8/detached.png)





***
#### ![Quick Note](http://training.databricks.com/databricks_guide/icon_note3_s.png) **Cells** are units that make up notebooks
![A Cell](http://training.databricks.com/databricks_guide/cell.png)

Cells each have a type - including **scala**, **python**, **sql**, **R**, **markdown**, **filesystem**, and **shell**.
* While cells default to the type of the Notebook, other cell types are supported as well.
* This cell is in **markdown** and is used for documentation. [Markdown](http://en.wikipedia.org/wiki/Markdown) is a simple text formatting syntax.
***




 
### ** Create** and **Edit** a New Markdown Cell in this Notebook
* When you mouse between cells, a + sign will pop up in the center that you can click on to create a new cell.

 ![New Cell](http://training.databricks.com/databricks_guide/create_new_cell.png)
* Type **`` Hello, world!``** into your new cell (**````** indicates the cell is markdown).



* Click out of the cell to see the cell contents update.
  
  ![Run cell](http://training.databricks.com/databricks_guide/run_cell.png)
  




 Hello, world!





***
#### ![Quick Note](http://training.databricks.com/databricks_guide/icon_note3_s.png) **Markdown Cell Tips**
* To change a non-markdown cell to markdown, add **** to very start of the cell.
* After updating the contents of a markdown cell, click out of the cell to update the formatted contents of a markdown cell.
* To edit an existing markdown cell, **doubleclick** the cell.
***





### Run a **Scala Cell**
* Run the following scala cell.
* Note: There is no need for any special indicator (such as ````) necessary to create a Scala cell in a Scala notebook.
* Make sure the cell contents updates before moving on.
* Press **Shift+Enter** when in the cell to run it and proceed to the next cell.
  * The cells contents should update.
  * Alternately, press **Ctrl+Enter** when in a cell to **run** it, but not proceed to the next cell.


```scala

println(System.currentTimeMillis)

```



### Running **Spark**
The variable **sc** allows you to access a Spark Context to run your Spark programs.
* For more information about Spark, please refer to [Spark Overview](https://spark.apache.org/docs/latest/)

**NOTE: Do not create the *sc* variable - it is already initialized for you. **


```scala

val words = sc.parallelize(Array("hello", "world", "goodbye", "hello", "again"))
val wordcounts = words.map(s => (s, 1)).reduceByKey(_ + _).collect() 

```
```scala

// Exercise: Calculate the number of unique words in the "words" RDD here.
// (Hint: The answer should be 4.)

```
```scala

// Exercise: Create an RDD of numbers, and find the mean.

```





### Working with **Spark SQL and DataFrames**
The variable **sqlContext** allows you to access a Spark SQL Context to work with Spark SQL and DataFrames.
* Scala can be used to create Spark [DataFrames](http://spark.apache.org/docs/latest/sql-programming-guide.html) - a distributed collection of data organized into named columns.
* DataFrames are created by appending ``.toDF()`` to the Scala RDD

**NOTE: Do not create the *sqlContext* variable - it is already initialized for you. **


```scala

// Define the schema using a case class
case class MyCaseClass(key: String, group: String, value: Int)

// Create the RDD (using sc.parallelize) and transforms it into a DataFrame
val df = sc.parallelize(Array(MyCaseClass("f", "consonants", 1),
   MyCaseClass("g", "consonants", 2),
   MyCaseClass("h", "consonants", 3),
   MyCaseClass("i", "vowels", 4),
   MyCaseClass("j", "consonants", 5))
 ).toDF()

```




#### **Show this data**
Use the **``display``** command to view a DataFrame in a notebook.


```scala

display(df)

```


 
***
#### ![Quick Note](http://training.databricks.com/databricks_guide/icon_note3_s.png) **The visualization above is interactive**
* Click on the **Chart Button** ![Chart Button](http://training.databricks.com/databricks_guide/chart_button.png) to toggle the view.
* Try different types of graphs by clicking on the arrow next to the chart button.
* If you have selected a graph, you can click on **Plot Options...** for even more ways to customize the view.
***


```scala

// Exercise: Create a DataFrame and display it. 
// Can you use the "Plot Options" to plot the group vs. the sum of the values in that group?
// (Hint: Vowels = 6, and consonants = 9)

```



***
## ![Quick Note](http://training.databricks.com/databricks_guide/icon_note3_s.png) **Where to Go Next**

We've now covered the basics of a Databricks Scala Notebook.  Practice the optional exercises below for more scala exercises, look to the following notebooks in other languages, or go to the Databricks Product Overview.
* [Notebook Tutorials in Python](/#workspace/databricks_guide/01 Intro Notebooks/1 Intro Python Notebooks)
* [Notebook Tutorials in SQL](/#workspace/databricks_guide/01 Intro Notebooks/3 Intro SQL Notebooks)
* [Notebook Tutorials in R](/#workspace/databricks_guide/01 Intro Notebooks/4 Intro R Notebooks)
* [Databricks Product Overview](/#workspace/databricks_guide/02 Product Overview/00 Product Overview)
***




 ## **Optional Tasks**





### **Importing Standard Scala and Java libraries**
* For other libraries that are not available by default, you can upload other libraries to the Workspace.
* Refer to the **[Libraries](/#workspace/databricks_guide/02 Product Overview/07 Libraries)** guide for more details.


```scala

import scala.math._
val x = min(1, 10)

```
```scala

import java.util.HashMap
val map = new HashMap[String, Int]()
map.put("a", 1)
map.put("b", 2)
map.put("c", 3)
map.put("d", 4)
map.put("e", 5)


```



### **Using Spark SQL within a Scala Notebook**
You can use execute SQL commands within a scala notebook by invoking **``%sql``** or using **``sqlContext.sql(...)``**.


```scala

%sql show databases

```




#### **Use ``registerTempTable`` on a DataFrame** 
* Register the above DataFrame table (built in scala) for SQL queries.
* Temporary tables are not meant to be persistent, i.e. they will not survive cluster restarts.


```scala

df.registerTempTable("ScalaTempTable")

```
```scala

%sql describe ScalaTempTable

```



#### **Visualizations and Spark SQL**
* A visualization appears automatically for the output of a **SQL select** statement in notebooks (no need to call ``display``).


```scala

%sql select * from ScalaTempTable

```



#### ** Persist DataFrames into Tables **
Use **``saveAsTable``** to persist tables to be used in other notebooks.
* These table definitions will persist even after cluster restarts.


```scala

sqlContext.sql("DROP TABLE IF EXISTS ScalaTestTable")
df.write.saveAsTable("ScalaTestTable")

```



***
![Quick Note](http://training.databricks.com/databricks_guide/icon_note3_s.png) For more information on working with Scala and Spark SQL, please refer to [Spark SQL Programming Guide](https://spark.apache.org/docs/latest/sql-programming-guide.html)
***





### ** Display HTML **
Display HTML within your notebook, using the **displayHTML** command.


```scala

displayHTML("<h3 style=\"color:blue\">Blue Text</h3>")
```
