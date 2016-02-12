// Databricks notebook source exported at Fri, 12 Feb 2016 04:46:36 UTC


# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)





# **Introduction to Scala through Scala Notebook** 

* This introduction notebook describes how to get started running Scala code in Notebooks.





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




 
***
### ** Create** and **Edit** a New Markdown Cell in this Notebook
* When you mouse between cells, a + sign will pop up in the center that you can click on to create a new cell.

 ![New Cell](http://training.databricks.com/databricks_guide/create_new_cell.png)
* Type **``%md Hello, world!``** into your new cell (**``%md``** indicates the cell is markdown).



* Click out of the cell to see the cell contents update.
  
  ![Run cell](http://training.databricks.com/databricks_guide/run_cell.png)
  ***




 **You Try Now!** 
Just double-click the cell below and modify 
```
> %md Hello, world!
```




 Hello, world!





***
#### ![Quick Note](http://training.databricks.com/databricks_guide/icon_note3_s.png) **Markdown Cell Tips**
* To change a non-markdown cell to markdown, add **%md** to very start of the cell.
* After updating the contents of a markdown cell, click out of the cell to update the formatted contents of a markdown cell.
* To edit an existing markdown cell, **doubleclick** the cell.
***





***
### Run a **Scala Cell**
* Run the following scala cell.
* Note: There is no need for any special indicator (such as ``%md``) necessary to create a Scala cell in a Scala notebook.
* You know it is a scala notebook because of the `` (Scala)`` appended to the name of this notebook.
* Make sure the cell contents updates before moving on.
* Press **Shift+Enter** when in the cell to run it and proceed to the next cell.
  * The cells contents should update.
  * Alternately, press **Ctrl+Enter** when in a cell to **run** it, but not proceed to the next cell.
* characters following ``//`` are comments in scala.
***


```scala

println(System.currentTimeMillis) // println prints its argument as a line

```
```scala



```




## Scala Resources

You will not be learning scala systematically and thoroughly in this course.  You will learn Scala by doing various Spark jobs. 

If you are seriously interested in learning scala properly, then there are various resources, including:

* [scala-lang.org](http://www.scala-lang.org/) is the **core Scala resource**.
* MOOC
  * [courseera: Functional Programming Principles in Scala](https://www.coursera.org/course/progfun)
* [Books](http://www.scala-lang.org/documentation/books.html)
  * [Programming in Scala, 1st Edition, Free Online Reading](http://www.artima.com/pins1ed/)
  
The main sources for the following content are (you are encouraged to read them for more background):

* [Martin Oderski's Scala by example](http://www.scala-lang.org/docu/files/ScalaByExample.pdf)
* [Scala crash course by Holden Karau](http://lintool.github.io/SparkTutorial/slides/day1_Scala_crash_course.pdf)
* [Darren's brief introduction to scala and breeze for statistical computing](https://darrenjw.wordpress.com/2013/12/30/brief-introduction-to-scala-and-breeze-for-statistical-computing/)

  





#Introduction to Scala
## What is Scala?
"Scala  smoothly  integrates  object-oriented  and  functional  programming.  It is designed to express common programming patterns in a concise, elegant, and type-safe way." by Matrin Odersky.

* High-level language for the Java Virtual Machine (JVM)
* Object oriented + functional programming
* Statically typed
* Comparable in speed to Java
* Type inference saves us from having to write explicit types most of the time Interoperates with Java
* Can use any Java class (inherit from, etc.)
* Can be called from Java code

## Why Scala?

* Spark was originally written in Scala, which allows concise function syntax and interactive use
* Spark APIs for other languages include:
  * Java API for standalone use
  * Python API added to reach a wider user community of programmes
  * R API added more recently to reach a wider community of data analyststs 
  * Unfortunately, Python and R APIs are generally behind Spark's native Scala (for eg. GraphX is only available in Scala currently).
* See Darren Wilkinson's 11 reasons for [scala as a platform for statistical computing and data science](https://darrenjw.wordpress.com/2013/12/23/scala-as-a-platform-for-statistical-computing-and-data-science/).





## Functions in Scala
## Operating on collections in Scala


```scala

val a = 5

```
```scala

a+3

```




# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)

