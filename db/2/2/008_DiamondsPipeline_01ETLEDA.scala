// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/7RVirnJuUNg/0.jpg)](https://www.youtube.com/embed/7RVirnJuUNg?start=0&end=2860&autoplay=1)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Diamonds ML Pipeline Workflow - DataFrame ETL and EDA Part
// MAGIC 
// MAGIC This is the Spark SQL parts that are focussed on extract-transform-Load (ETL) and exploratory-data-analysis (EDA) parts of an end-to-end example of a Machine Learning (ML) workflow.
// MAGIC 
// MAGIC **Why are we using DataFrames?** *This is because of the **Announcement** in the Spark MLlib Main Guide for Spark 2.2* [https://spark.apache.org/docs/latest/ml-guide.html](https://spark.apache.org/docs/latest/ml-guide.html) that *"DataFrame-based API is primary API"*.
// MAGIC 
// MAGIC This notebook is a scala*rific* break-down of the python*ic* 'Diamonds ML Pipeline Workflow' from the Databricks Guide.
// MAGIC 
// MAGIC **We will see this example again in the sequel**
// MAGIC 
// MAGIC For this example, we analyze the Diamonds dataset from the R Datasets hosted on DBC.  
// MAGIC 
// MAGIC Later on, we will use the [DecisionTree algorithm](http://spark.apache.org/docs/latest/ml-classification-regression.html#decision-trees) to predict the price of a diamond from its characteristics.
// MAGIC 
// MAGIC Here is an outline of our pipeline:
// MAGIC 
// MAGIC * **Step 1. *Load data*: Load data as DataFrame**
// MAGIC * **Step 2. *Understand the data*: Compute statistics and create visualizations to get a better understanding of the data.**
// MAGIC * Step 3. *Hold out data*: Split the data randomly into training and test sets.  We will not look at the test data until *after* learning.
// MAGIC * Step 4. On the training dataset:
// MAGIC   * *Extract features*: We will index categorical (String-valued) features so that DecisionTree can handle them.
// MAGIC   * *Learn a model*: Run DecisionTree to learn how to predict a diamond's price from a description of the diamond.
// MAGIC   * *Tune the model*: Tune the tree depth (complexity) using the training data.  (This process is also called *model selection*.)
// MAGIC * Step 5. *Evaluate the model*: Now look at the test dataset.  Compare the initial model with the tuned model to see the benefit of tuning parameters.
// MAGIC * Step 6. *Understand the model*: We will examine the learned model and results to gain further insight.
// MAGIC 
// MAGIC In this notebook, we will only cover **Step 1** and **Step 2.** above. The other Steps will be revisited in the sequel.

// COMMAND ----------

// MAGIC %md
// MAGIC ### Step 1. Load data as DataFrame
// MAGIC 
// MAGIC This section loads a dataset as a DataFrame and examines a few rows of it to understand the schema.
// MAGIC 
// MAGIC For more info, see the DB guide on [importing data](https://docs.databricks.com/user-guide/importing-data.html).

// COMMAND ----------

// We'll use the Diamonds dataset from the R datasets hosted on DBC.
val diamondsFilePath = "dbfs:/databricks-datasets/Rdatasets/data-001/csv/ggplot2/diamonds.csv"

// COMMAND ----------

sc.textFile(diamondsFilePath).take(2) // looks like a csv file as it should

// COMMAND ----------

val diamondsRawDF = sqlContext.read    // we can use sqlContext instead of SparkSession for backwards compatibility to 1.x
    .format("com.databricks.spark.csv") // use spark.csv package
    .option("header", "true") // Use first line of all files as header
    .option("inferSchema", "true") // Automatically infer data types
    //.option("delimiter", ",") // Specify the delimiter as comma or ',' DEFAULT
    .load(diamondsFilePath)

// COMMAND ----------

//There are 10 columns.  We will try to predict the price of diamonds, treating the other 9 columns as features.
diamondsRawDF.printSchema()

// COMMAND ----------

// MAGIC %md
// MAGIC *Note:* `(nullable = true)` simply means if the value is allowed to be `null`.
// MAGIC 
// MAGIC Let us count the number of rows in ``diamondsDF``.

// COMMAND ----------

diamondsRawDF.count() // Ctrl+Enter

// COMMAND ----------

// MAGIC %md
// MAGIC So there are 53940 records or rows in the DataFrame.
// MAGIC 
// MAGIC Use the `show(n)` method to see the first `n` (default is 20) rows of the DataFrame, as folows:

// COMMAND ----------

diamondsRawDF.show(10)

// COMMAND ----------

// MAGIC %md
// MAGIC If you notice the schema of `diamondsRawDF` you will see that the automatic schema inference of `SqlContext.read` method has cast the values in the column `price` as `integer`. 
// MAGIC 
// MAGIC To cleanup:
// MAGIC 
// MAGIC * let's recast the column `price` as `double` for downstream ML tasks later and 
// MAGIC * let's also get rid of the first column of row indices. 

// COMMAND ----------

import org.apache.spark.sql.types.DoubleType
//we will convert price column from int to double for being able to model, fit and predict in downstream ML task
val diamondsDF = diamondsRawDF.select($"carat", $"cut", $"color", $"clarity", $"depth", $"table",$"price".cast(DoubleType).as("price"), $"x", $"y", $"z")
diamondsDF.cache() // let's cache it for reuse
diamondsDF.printSchema // print schema

// COMMAND ----------

diamondsDF.show(10,false) // notice that price column has Double values that end in '.0' now

// COMMAND ----------

//View DataFrame in databricks
// note this 'display' is a databricks notebook specific command that is quite powerful for visual interaction with the data
// other notebooks like zeppelin have similar commands for interactive visualisation
display(diamondsDF) 

// COMMAND ----------

// MAGIC %md
// MAGIC ### Step 2. Understand the data
// MAGIC 
// MAGIC Let's examine the data to get a better understanding of what is there.  We only examine a couple of features (columns), but it gives an idea of the type of exploration you might do to understand a new dataset.
// MAGIC 
// MAGIC For more examples of using Databricks's visualization (even across languages) see [https://docs.databricks.com/user-guide/visualizations/index.html](https://docs.databricks.com/user-guide/visualizations/index.html) NOW.

// COMMAND ----------

// MAGIC %md 
// MAGIC We can see that we have a mix of 
// MAGIC 
// MAGIC * categorical features (`cut`, `color`, `clarity`) and 
// MAGIC * continuous features (`depth`, `x`, `y`, `z`).  
// MAGIC 
// MAGIC ##### Let's first look at the categorical features.

// COMMAND ----------

// MAGIC %md
// MAGIC You can also select one or more individual columns using so-called DataFrame API. 
// MAGIC 
// MAGIC Let us `select` the column `cut` from `diamondsDF` and create a new DataFrame called `cutsDF` and then display it as follows:

// COMMAND ----------

val cutsDF = diamondsDF.select("cut") // Shift+Enter

// COMMAND ----------

cutsDF.show(10) // Ctrl+Enter

// COMMAND ----------

// MAGIC %md
// MAGIC Let us use `distinct` to find the distinct types of `cut`'s in the dataset. 

// COMMAND ----------

// View distinct diamond cuts in dataset
val cutsDistinctDF = diamondsDF.select("cut").distinct()

// COMMAND ----------

cutsDistinctDF.show()

// COMMAND ----------

// MAGIC %md 
// MAGIC Clearly, there are just 5 kinds of cuts.

// COMMAND ----------

// View distinct diamond colors in dataset
val colorsDistinctDF = diamondsDF.select("color").distinct() //.collect()
colorsDistinctDF.show()

// COMMAND ----------

// View distinct diamond clarities in dataset
val claritiesDistinctDF = diamondsDF.select("clarity").distinct() // .collect()
claritiesDistinctDF.show()

// COMMAND ----------

// MAGIC %md
// MAGIC We can examine the distribution of a particular feature by using display(), 
// MAGIC 
// MAGIC **You Try!**
// MAGIC 
// MAGIC 1. Click on the chart icon and Plot Options, and setting:
// MAGIC  * Value=`<id>` 
// MAGIC  * Series groupings='cut'
// MAGIC  * and Aggregation=`COUNT`.
// MAGIC 2. You can also try this using columns "color" and "clarity"

// COMMAND ----------

display(diamondsDF.select("cut"))

// COMMAND ----------

// come on do the same for color NOW!

// COMMAND ----------

// and clarity too...

// COMMAND ----------

// MAGIC %md
// MAGIC ** You Try!**
// MAGIC 
// MAGIC Now play around with display of the entire DF and choosing what you want in the GUI as opposed to a `.select(...)` statement earlier.
// MAGIC 
// MAGIC For instance, the following `display(diamondsDF)` shows the counts of the colors by choosing in the `Plot Options` a `bar-chart` that is `grouped` with `Series Grouping` as `color`, `values` as `<id>` and `Aggregation` as `COUNT`. You can click on `Plot Options` to see these settings and can change them as you wish by dragging and dropping. 

// COMMAND ----------

 display(diamondsDF)

// COMMAND ----------

// MAGIC %md 
// MAGIC Now let's examine one of the continuous features as an example.

// COMMAND ----------

//Select: "Plot Options..." --> "Display type" --> "histogram plot" and choose to "Plot over all results" OTHERWISE you get the image from first 1000 rows only
display(diamondsDF.select("carat"))

// COMMAND ----------

// MAGIC %md 
// MAGIC The above histogram of the diamonds' carat ratings shows that carats have a skewed distribution: Many diamonds are small, but there are a number of diamonds in the dataset which are much larger.  
// MAGIC 
// MAGIC * Extremely skewed distributions can cause problems for some algorithms (e.g., Linear Regression).  
// MAGIC * However, Decision Trees handle skewed distributions very naturally.
// MAGIC 
// MAGIC Note: When you call `display` to create a histogram like that above, **it will plot using a subsample from the dataset** (for efficiency), but you can plot using the full dataset by selecting "Plot over all results".  For our dataset, the two plots can actually look very different due to the long-tailed distribution.
// MAGIC 
// MAGIC We will not examine the label distribution for now.  It can be helpful to examine the label distribution, but it is best to do so only on the training set, not on the test set which we will hold out for evaluation.  These will be seen in the sequel

// COMMAND ----------

// MAGIC %md
// MAGIC **You Try!**
// MAGIC Of course knock youself out visually exploring the dataset more...

// COMMAND ----------

display(diamondsDF.select("cut","carat"))

// COMMAND ----------

// MAGIC %md
// MAGIC Try scatter plot to see pairwise scatter plots of continuous features.

// COMMAND ----------

display(diamondsDF) //Ctrl+Enter 

// COMMAND ----------

// MAGIC %md
// MAGIC Note that columns of type string are not in the scatter plot!

// COMMAND ----------

diamondsDF.printSchema // Ctrl+Enter

// COMMAND ----------

// MAGIC %md
// MAGIC ### Let us run through some basic inteactive SQL queries next
// MAGIC * HiveQL supports =, <, >, <=, >= and != operators. It also supports LIKE operator for fuzzy matching of Strings
// MAGIC * Enclose Strings in single quotes
// MAGIC * Multiple conditions can be combined using `and` and `or`
// MAGIC * Enclose conditions in `()` for precedence
// MAGIC * ...
// MAGIC * ...
// MAGIC 
// MAGIC **Why do I need to learn interactive SQL queries?**
// MAGIC 
// MAGIC Such queries in the widely known declarative SQL language can help us explore the data and thereby inform the modeling process!!!

// COMMAND ----------

// MAGIC %md
// MAGIC Using DataFrame API, we can apply a `filter` after `select` to transform the DataFrame `diamondsDF` to the new DataFrame `diamondsDColoredDF`.
// MAGIC 
// MAGIC Below, `$` is an alias for column. 
// MAGIC 
// MAGIC Let as select the columns named `carat`, `colour`, `price` where `color` value is equal to `D`.

// COMMAND ----------

val diamondsDColoredDF = diamondsDF.select("carat", "color", "price").filter($"color" === "D") // Shift+Enter

// COMMAND ----------

diamondsDColoredDF.show(10) // Ctrl+Enter

// COMMAND ----------

// MAGIC %md
// MAGIC As you can see all the colors are now 'D'. But to really confirm this we can do the following for fun:

// COMMAND ----------

diamondsDColoredDF.select("color").distinct().show

// COMMAND ----------

// MAGIC %md
// MAGIC Let's try to do the same in SQL for those who know SQL from before.
// MAGIC 
// MAGIC First we need to see if the table is registerd (not just the DataFrame), and if not we ened to register our DataFrame as a temporary table.

// COMMAND ----------

sqlContext.tables.show() // Ctrl+Enter to see available tables

// COMMAND ----------

// MAGIC %md
// MAGIC Looks like diamonds is already there (if not just execute the following cell).

// COMMAND ----------

diamondsDF.createOrReplaceTempView("diamonds")

// COMMAND ----------

sqlContext.tables.show() // Ctrl+Enter to see available tables

// COMMAND ----------

// MAGIC %sql -- Shift+Enter to do the same in SQL
// MAGIC select carat, color, price from diamonds where color='D'

// COMMAND ----------

// MAGIC %md
// MAGIC Alternatively, one could just write the SQL statement in scala to create a new DataFrame `diamondsDColoredDF_FromTable` from the table `diamonds` and display it, as follows:

// COMMAND ----------

val diamondsDColoredDF_FromTable = sqlContext.sql("select carat, color, price from diamonds where color='D'") // Shift+Enter

// COMMAND ----------

// or if you like use upper case for SQL then this is equivalent
val diamondsDColoredDF_FromTable = sqlContext.sql("SELECT carat, color, price FROM diamonds WHERE color='D'") // Shift+Enter

// COMMAND ----------

// from version 2.x onwards you can call from SparkSession, the pre-made spark in spark-shell or databricks notebook
val diamondsDColoredDF_FromTable = spark.sql("SELECT carat, color, price FROM diamonds WHERE color='D'") // Shift+Enter

// COMMAND ----------

display(diamondsDColoredDF_FromTable) // Ctrl+Enter to see the same DF!

// COMMAND ----------

// You can also use the familiar wildchard character '%' when matching Strings
display(spark.sql("SELECT * FROM diamonds WHERE clarity LIKE 'V%'"))

// COMMAND ----------

// Combining conditions
display(spark.sql("SELECT * FROM diamonds WHERE clarity LIKE 'V%' AND price > 10000"))

// COMMAND ----------

// selecting a subset of fields
display(spark.sql("SELECT carat, clarity, price FROM diamonds WHERE color = 'D'"))

// COMMAND ----------

//renaming a field using as
display(spark.sql("SELECT carat AS carrot, clarity, price FROM diamonds"))

// COMMAND ----------

//sorting
display(spark.sql("SELECT carat, clarity, price FROM diamonds ORDER BY price DESC"))

// COMMAND ----------

diamondsDF.printSchema // since price is double in the DF that was turned into table we can rely on the descenting sort on doubles

// COMMAND ----------

// sort by multiple fields
display(spark.sql("SELECT carat, clarity, price FROM diamonds ORDER BY carat ASC, price DESC"))

// COMMAND ----------

// use this to type cast strings into Int when the table is loaded with string-valued columns
//display(spark.sql("select cast(carat as Int) as carat, clarity, cast(price as Int) as price from diamond order by carat asc, price desc"))

// COMMAND ----------

// sort by multiple fields and limit to first 5
// I prefer lowercase for SQL - and you can use either in this course - but in the field do what your Boss or your colleagues prefer :)
display(spark.sql("select carat, clarity, price from diamonds order by carat desc, price desc limit 5"))

// COMMAND ----------

//aggregate functions
display(spark.sql("select avg(price) as avgprice from diamonds"))

// COMMAND ----------

//average operator is doing an auto-type conversion from int to double
display(spark.sql("select avg(cast(price as Integer)) as avgprice from diamonds"))

// COMMAND ----------

//aggregate function and grouping
display(spark.sql("select color, avg(price) as avgprice from diamonds group by color"))

// COMMAND ----------

// MAGIC %md
// MAGIC ### Why do we need to know these interactive SQL queries?
// MAGIC Such queries can help us explore the data and thereby inform the modeling process!!!
// MAGIC 
// MAGIC Of course, if you don't know SQL then don't worry, we will be doing these things in scala using DataFrames.
// MAGIC 
// MAGIC Finally, those who are planning to take the Spark Developer Exams online, then you can't escape from SQL questions there...

// COMMAND ----------

// MAGIC %md
// MAGIC #### Further Preparation
// MAGIC For more on SQL syntax, check the SQL tutorial on [W3Schools](http://www.w3schools.com/sql/default.asp)  
// MAGIC Note that [HiveQL](https://cwiki.apache.org/confluence/display/Hive/LanguageManual) supports only a subset of operations supported by SQL
// MAGIC 
// MAGIC See databricks guide on [tables](https://docs.databricks.com/user-guide/tables.html) **NOW-ish**.