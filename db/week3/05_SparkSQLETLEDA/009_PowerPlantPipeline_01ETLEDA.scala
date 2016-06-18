// Databricks notebook source exported at Sat, 18 Jun 2016 08:58:54 UTC
// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)

// COMMAND ----------

// MAGIC %md
// MAGIC The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/week3/05_SparkSQLETLEDA/009_PowerPlantPipeline_01ETLEDA.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):
// MAGIC 
// MAGIC [![sds/uji/week3/05_SparkSQLETLEDA/008_DiamondsPipeline](http://img.youtube.com/vi/6NoPvmTBVz0/0.jpg)](https://www.youtube.com/v/6NoPvmTBVz0?rel=0&autoplay=1&modestbranding=1&start=4070&end=5401)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Power Plant ML Pipeline Application - DataFrame Part
// MAGIC This is the Spark SQL parts of an end-to-end example of using a number of different machine learning algorithms to solve a supervised regression problem.
// MAGIC This is a break-down of [Power Plant ML Pipeline Application](/#workspace/databricks_guide/07 Spark MLlib/7 Power Plant Pipeline)
// MAGIC 
// MAGIC **This will be a recurring example in the sequel***
// MAGIC 
// MAGIC ##### Table of Contents
// MAGIC 
// MAGIC - **Step 1: Business Understanding** 
// MAGIC - **Step 2: Load Your Data**
// MAGIC - **Step 3: Explore Your Data**
// MAGIC - **Step 4: Visualize Your Data**
// MAGIC - *Step 5: Data Preparation*
// MAGIC - *Step 6: Data Modeling*
// MAGIC - *Step 7: Tuning and Evaluation*
// MAGIC - *Step 8: Deployment*
// MAGIC 
// MAGIC *We are trying to predict power output given a set of readings from various sensors in a gas-fired power generation plant.  Power generation is a complex process, and understanding and predicting power output is an important element in managing a plant and its connection to the power grid.*
// MAGIC 
// MAGIC * Given this business problem, we need to translate it to a Machine Learning task.  
// MAGIC * The ML task is regression since the label (or target) we will be trying to predict is numeric.
// MAGIC 
// MAGIC **Today, we will only cover Steps 1, 2, 3 and 4 above**. You need introductions to linear algebra, stochastic gradient descent and decision trees before we can accomplish the **applied ML task** with some intuitive understanding. If you can't wait for ML then **check out [Spark MLLib Programming Guide](https://spark.apache.org/docs/latest/mllib-guide.html) for comming attractions!**
// MAGIC 
// MAGIC The example data is provided by UCI at [UCI Machine Learning Repository Combined Cycle Power Plant Data Set](https://archive.ics.uci.edu/ml/datasets/Combined+Cycle+Power+Plant)
// MAGIC 
// MAGIC You can read the background on the UCI page, but in summary:
// MAGIC * we have collected a number of readings from sensors at a Gas Fired Power Plant (also called a Peaker Plant) and 
// MAGIC * want to use those sensor readings to predict how much power the plant will generate in a couple weeks from now.
// MAGIC * Again, today we will just focus on Steps 1-4 above that pertain to DataFrames.
// MAGIC 
// MAGIC More information about Peaker or Peaking Power Plants can be found on Wikipedia [https://en.wikipedia.org/wiki/Peaking_power_plant](https://en.wikipedia.org/wiki/Peaking_power_plant).

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
displayHTML(frameIt("https://en.wikipedia.org/wiki/Peaking_power_plant",300))

// COMMAND ----------

displayHTML(frameIt("https://archive.ics.uci.edu/ml/datasets/Combined+Cycle+Power+Plant",500))

// COMMAND ----------

require(sc.version.replace(".", "").toInt >= 140, "Spark 1.4.0+ is required to run this notebook. Please attach it to a Spark 1.4.0+ cluster.")

// COMMAND ----------

// MAGIC %md
// MAGIC ##Step 1: Business Understanding
// MAGIC The first step in any machine learning task is to understand the business need. 
// MAGIC 
// MAGIC As described in the overview we are trying to predict power output given a set of readings from various sensors in a gas-fired power generation plant.
// MAGIC 
// MAGIC The problem is a regression problem since the label (or target) we are trying to predict is numeric

// COMMAND ----------

// MAGIC %md
// MAGIC ##Step 2: Load Your Data
// MAGIC Now that we understand what we are trying to do, we need to load our data and describe it, explore it and verify it.

// COMMAND ----------

// MAGIC %md
// MAGIC Data was downloaded already as these five Tab-separated-variable or tsv files.

// COMMAND ----------

display(dbutils.fs.ls("/databricks-datasets/power-plant/data")) // Ctrl+Enter

// COMMAND ----------

// MAGIC %md
// MAGIC Now let us load the data from the Tab-separated-variable or tsv text file into an `RDD[String]` using the familiar `textFile` method.

// COMMAND ----------

val powerPlantRDD = sc.textFile("/databricks-datasets/power-plant/data/Sheet1.tsv") // Ctrl+Enter

// COMMAND ----------

powerPlantRDD.take(5).foreach(println) // Ctrl+Enter to print first 5 lines

// COMMAND ----------

// this reads the tsv file and turns it into a dataframe
val powerPlantDF = sqlContext.read    
    .format("com.databricks.spark.csv") // use spark.csv package
    .option("header", "true") // Use first line of all files as header
    .option("inferSchema", "true") // Automatically infer data types
    .option("delimiter", "\t") // Specify the delimiter as Tab or '\t'
    .load("/databricks-datasets/power-plant/data/Sheet1.tsv")

// COMMAND ----------

powerPlantDF.printSchema // print the schema of the DataFrame that was inferred

// COMMAND ----------

// MAGIC %md
// MAGIC ### 2.1. Alternatively, load data via the upload GUI feature in databricks
// MAGIC Since the dataset is relatively small, we will use the upload feature in Databricks to upload the data as a table.
// MAGIC 
// MAGIC First download the Data Folder from [UCI Machine Learning Repository Combined Cycle Power Plant Data Set](https://archive.ics.uci.edu/ml/datasets/Combined+Cycle+Power+Plant)
// MAGIC 
// MAGIC The file is a multi-tab Excel document so you will need to save each tab as a Text file export. 
// MAGIC 
// MAGIC I prefer exporting as a Tab-Separated-Values (TSV) since it is more consistent than CSV.
// MAGIC 
// MAGIC Call each file Folds5x2_pp<Sheet 1..5>.tsv and save to your machine.
// MAGIC 
// MAGIC Go to the Databricks Menu > Tables > Create Table
// MAGIC 
// MAGIC Select Datasource as "File"
// MAGIC 
// MAGIC Upload *ALL* 5 files at once.
// MAGIC 
// MAGIC See screenshots below:
// MAGIC 
// MAGIC 
// MAGIC **2.1.1. Create Table**
// MAGIC   _________________
// MAGIC 
// MAGIC When you import your data, name your table `power_plant`, specify all of the columns with the datatype `Double` and make sure you check the `First row is header` box.
// MAGIC 
// MAGIC ![alt text](http://training.databricks.com/databricks_guide/1_4_ML_Power_Plant_Import_Table.png)
// MAGIC 
// MAGIC **2.1.2. Review Schema**
// MAGIC   __________________
// MAGIC 
// MAGIC Your table schema and preview should look like this after you click ```Create Table```:
// MAGIC 
// MAGIC ![alt text](http://training.databricks.com/databricks_guide/1_4_ML_Power_Plant_Import_Table_Schema.png)

// COMMAND ----------

// MAGIC %md Now that your data is loaded let's explore it.

// COMMAND ----------

// MAGIC %md
// MAGIC ##Step 3: Explore Your Data
// MAGIC Now that we understand what we are trying to do, we need to load our data and describe it, explore it and verify it.

// COMMAND ----------

// MAGIC %md
// MAGIC #### Viewing as DataFrame

// COMMAND ----------

display(powerPlantDF)

// COMMAND ----------

powerPlantDF.count() // count the number of rows in DF

// COMMAND ----------

// MAGIC %md
// MAGIC #### Viewing as Table via SQL
// MAGIC Let us look at what tables are already available, as follows:

// COMMAND ----------

sqlContext.tables.show() // Ctrl+Enter to see available tables

// COMMAND ----------

// MAGIC %md
// MAGIC We need to register the DF as a temporary table before being able to access it via SQL.

// COMMAND ----------

powerPlantDF.registerTempTable("power_plant_table") // Shift+Enter

// COMMAND ----------

sqlContext.tables.show() // Ctrl+Enter to see available tables

// COMMAND ----------

// MAGIC %md
// MAGIC Note that table names are in lower-case only!

// COMMAND ----------

// MAGIC %md
// MAGIC **You Try!**

// COMMAND ----------

sqlContext // put . after sqlContext and hit Tab to see what methods are available

// COMMAND ----------

//sqlContext.dropTempTable("power_plant_table") // uncomment and Ctrl+Enter if you want to remove the table!

// COMMAND ----------

// MAGIC %md
// MAGIC The following SQL statement simply selects all the columns (due to `*`) from `powerPlantTable`.

// COMMAND ----------

// MAGIC %sql 
// MAGIC -- Ctrl+Enter to query the rows via SQL
// MAGIC SELECT * FROM power_plant_table

// COMMAND ----------

// MAGIC %md
// MAGIC Note that the output of the above command is the same as `display(powerPlantDF)` we did earlier.

// COMMAND ----------

// MAGIC %md
// MAGIC We can use the SQL `desc` command to describe the schema. This is the SQL equivalent of `powerPlantDF.printSchema` we saw earlier.

// COMMAND ----------

// MAGIC %sql desc power_plant_table

// COMMAND ----------

// MAGIC %md
// MAGIC **Schema Definition**
// MAGIC 
// MAGIC Our schema definition from UCI appears below:
// MAGIC 
// MAGIC - AT = Atmospheric Temperature in C
// MAGIC - V = Exhaust Vaccum Speed
// MAGIC - AP = Atmospheric Pressure
// MAGIC - RH = Relative Humidity
// MAGIC - PE = Power Output
// MAGIC 
// MAGIC PE is our label or target. This is the value we are trying to predict given the measurements.
// MAGIC 
// MAGIC *Reference [UCI Machine Learning Repository Combined Cycle Power Plant Data Set](https://archive.ics.uci.edu/ml/datasets/Combined+Cycle+Power+Plant)*

// COMMAND ----------

// MAGIC %md
// MAGIC Let's do some basic statistical analysis of all the columns. 
// MAGIC 
// MAGIC We can use the describe function with no parameters to get some basic stats for each column like count, mean, max, min and standard deviation.  More information can be found in the [Spark API docs](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.DataFrame)

// COMMAND ----------

display(powerPlantDF.describe())

// COMMAND ----------

// MAGIC %md
// MAGIC ##Step 4: Visualize Your Data
// MAGIC 
// MAGIC To understand our data, we will look for correlations between features and the label.  This can be important when choosing a model.  E.g., if features and a label are linearly correlated, a linear model like Linear Regression can do well; if the relationship is very non-linear, more complex models such as Decision Trees can be better. We use the Databricks built in visualization to view each of our predictors in relation to the label column as a scatter plot to see the correlation between the predictors and the label.

// COMMAND ----------

// MAGIC %sql select AT as Temperature, PE as Power from power_plant_table

// COMMAND ----------

// MAGIC %md
// MAGIC It looks like there is strong linear correlation between temperature and Power Output

// COMMAND ----------

// MAGIC %sql select V as ExhaustVaccum, PE as Power from power_plant_table;

// COMMAND ----------

// MAGIC %md
// MAGIC The linear correlation is not as strong between Exhaust Vacuum Speed and Power Output but there is some semblance of a pattern.

// COMMAND ----------

// MAGIC %sql select AP as Pressure, PE as Power from power_plant_table;

// COMMAND ----------

// MAGIC %sql select RH as Humidity, PE as Power from power_plant_table;

// COMMAND ----------

// MAGIC %md 
// MAGIC ...and atmospheric pressure and relative humidity seem to have little to no linear correlation.
// MAGIC 
// MAGIC These pairwise plots can also be done directly using `display` on `select`ed columns of the DataFrame `powerPlantDF`.
// MAGIC 
// MAGIC In general we will shy from SQL as much as possible.  The illustations in `%sql` above are to mainly reassure those with a RDBMS background and SQL that their SQL expressibility can be directly used in Apache Spark and in databricks notebooks.

// COMMAND ----------

display(powerPlantDF.select($"RH", $"PE"))

// COMMAND ----------

// MAGIC %md
// MAGIC Furthermore, you can interactively start playing with `display` on the full DataFrame!

// COMMAND ----------

display(powerPlantDF)

// COMMAND ----------

// MAGIC %md
// MAGIC We will do the following steps in the sequel.
// MAGIC - *Step 5: Data Preparation*
// MAGIC - *Step 6: Data Modeling*
// MAGIC - *Step 7: Tuning and Evaluation*
// MAGIC - *Step 8: Deployment*

// COMMAND ----------

// MAGIC %md
// MAGIC Datasource References:
// MAGIC * Pinar Tüfekci, Prediction of full load electrical power output of a base load operated combined cycle power plant using machine learning methods, International Journal of Electrical Power & Energy Systems, Volume 60, September 2014, Pages 126-140, ISSN 0142-0615, [Web Link](http://www.journals.elsevier.com/international-journal-of-electrical-power-and-energy-systems/)
// MAGIC * Heysem Kaya, Pinar Tüfekci , Sadik Fikret Gürgen: Local and Global Learning Methods for Predicting Power of a Combined Gas & Steam Turbine, Proceedings of the International Conference on Emerging Trends in Computer and Electronics Engineering ICETCEE 2012, pp. 13-18 (Mar. 2012, Dubai) [Web Link](http://www.cmpe.boun.edu.tr/~kaya/kaya2012gasturbine.pdf)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)