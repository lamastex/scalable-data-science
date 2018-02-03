[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

### Power Plant ML Pipeline Application - DataFrame Part

This is the Spark SQL parts of an end-to-end example of using a number of different machine learning algorithms to solve a supervised regression problem.

This is a break-down of *Power Plant ML Pipeline Application* from databricks.

**This will be a recurring example in the sequel**

##### Table of Contents

-   **Step 1: Business Understanding**
-   **Step 2: Load Your Data**
-   **Step 3: Explore Your Data**
-   **Step 4: Visualize Your Data**
-   *Step 5: Data Preparation*
-   *Step 6: Data Modeling*
-   *Step 7: Tuning and Evaluation*
-   *Step 8: Deployment*

*We are trying to predict power output given a set of readings from various sensors in a gas-fired power generation plant. Power generation is a complex process, and understanding and predicting power output is an important element in managing a plant and its connection to the power grid.*

-   Given this business problem, we need to translate it to a Machine Learning task (actually a *Statistical* Machine Learning task).
-   The ML task here is *regression* since the label (or target) we will be trying to predict takes a *continuous numeric* value
-   Note: if the labels took values from a finite discrete set, such as, `Spam`/`Not-Spam` or `Good`/`Bad`/`Ugly`, then the ML task would be *classification*.

**Today, we will only cover Steps 1, 2, 3 and 4 above**. You need introductions to linear algebra, stochastic gradient descent and decision trees before we can accomplish the **applied ML task** with some intuitive understanding. If you can't wait for ML then **check out [Spark MLLib Programming Guide](https://spark.apache.org/docs/latest/mllib-guide.html) for comming attractions!**

The example data is provided by UCI at [UCI Machine Learning Repository Combined Cycle Power Plant Data Set](https://archive.ics.uci.edu/ml/datasets/Combined+Cycle+Power+Plant)

You can read the background on the UCI page, but in summary: \* we have collected a number of readings from sensors at a Gas Fired Power Plant (also called a Peaker Plant) and \* want to use those sensor readings to predict how much power the plant will generate in a couple weeks from now. \* Again, today we will just focus on Steps 1-4 above that pertain to DataFrames.

More information about Peaker or Peaking Power Plants can be found on Wikipedia <https://en.wikipedia.org/wiki/Peaking_power_plant>.

<a href="https://en.wikipedia.org/wiki/Peaking_power_plant">https://en.wikipedia.org/wiki/Peaking_power_plant</a>

<a href="https://archive.ics.uci.edu/ml/datasets/Combined+Cycle+Power+Plant">https://archive.ics.uci.edu/ml/datasets/Combined+Cycle+Power+Plant</a>

    // a good habit to ensure the code is being run on the appropriate version of Spark - we are using Spark 2.2 actually if we use SparkSession object spark down the road...
    require(sc.version.replace(".", "").toInt >= 140, "Spark 1.4.0+ is required to run this notebook. Please attach it to a Spark 1.4.0+ cluster.")

Step 1: Business Understanding
------------------------------

The first step in any machine learning task is to understand the business need.

As described in the overview we are trying to predict power output given a set of readings from various sensors in a gas-fired power generation plant.

The problem is a regression problem since the label (or target) we are trying to predict is numeric

Step 2: Load Your Data
----------------------

Now that we understand what we are trying to do, we need to load our data and describe it, explore it and verify it.

Data was downloaded already as these five Tab-separated-variable or tsv files.

    display(dbutils.fs.ls("/databricks-datasets/power-plant/data")) // Ctrl+Enter

| dbfs:/databricks-datasets/power-plant/data/Sheet1.tsv | Sheet1.tsv | 308693.0 |
|-------------------------------------------------------|------------|----------|
| dbfs:/databricks-datasets/power-plant/data/Sheet2.tsv | Sheet2.tsv | 308693.0 |
| dbfs:/databricks-datasets/power-plant/data/Sheet3.tsv | Sheet3.tsv | 308693.0 |
| dbfs:/databricks-datasets/power-plant/data/Sheet4.tsv | Sheet4.tsv | 308693.0 |
| dbfs:/databricks-datasets/power-plant/data/Sheet5.tsv | Sheet5.tsv | 308693.0 |

Now let us load the data from the Tab-separated-variable or tsv text file into an `RDD[String]` using the familiar `textFile` method.

    val powerPlantRDD = sc.textFile("/databricks-datasets/power-plant/data/Sheet1.tsv") // Ctrl+Enter

> powerPlantRDD: org.apache.spark.rdd.RDD\[String\] = /databricks-datasets/power-plant/data/Sheet1.tsv MapPartitionsRDD\[363800\] at textFile at &lt;console&gt;:34

    powerPlantRDD.take(5).foreach(println) // Ctrl+Enter to print first 5 lines

> AT V AP RH PE 14.96 41.76 1024.07 73.17 463.26 25.18 62.96 1020.04 59.08 444.37 5.11 39.4 1012.16 92.14 488.56 20.86 57.32 1010.24 76.64 446.48

    // this reads the tsv file and turns it into a dataframe
    val powerPlantDF = spark.read // use 'sqlContext.read' instead if you want to use older Spark version > 1.3  see 008_ notebook
        .format("csv") // use spark.csv package
        .option("header", "true") // Use first line of all files as header
        .option("inferSchema", "true") // Automatically infer data types
        .option("delimiter", "\t") // Specify the delimiter as Tab or '\t'
        .load("/databricks-datasets/power-plant/data/Sheet1.tsv")

> powerPlantDF: org.apache.spark.sql.DataFrame = \[AT: double, V: double ... 3 more fields\]

    powerPlantDF.printSchema // print the schema of the DataFrame that was inferred

> root |-- AT: double (nullable = true) |-- V: double (nullable = true) |-- AP: double (nullable = true) |-- RH: double (nullable = true) |-- PE: double (nullable = true)

### 2.1. Alternatively, load data via the upload GUI feature in databricks

USE THIS FOR OTHER SMALLish DataSets you want to import to your CE
------------------------------------------------------------------

Since the dataset is relatively small, we will use the upload feature in Databricks to upload the data as a table.

First download the Data Folder from [UCI Machine Learning Repository Combined Cycle Power Plant Data Set](https://archive.ics.uci.edu/ml/datasets/Combined+Cycle+Power+Plant)

The file is a multi-tab Excel document so you will need to save each tab as a Text file export.

I prefer exporting as a Tab-Separated-Values (TSV) since it is more consistent than CSV.

Call each file Folds5x2\_pp<Sheet 1..5>.tsv and save to your machine.

Go to the Databricks Menu &gt; Tables &gt; Create Table

Select Datasource as "File"

Upload *ALL* 5 files at once.

See screenshots below (but refer <https://docs.databricks.com/user-guide/importing-data.html> for latest methods to import data):

**2.1.1. Create Table** \_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_

When you import your data, name your table `power_plant`, specify all of the columns with the datatype `Double` and make sure you check the `First row is header` box.

![alt text](http://training.databricks.com/databricks_guide/1_4_ML_Power_Plant_Import_Table.png)

**2.1.2. Review Schema** \_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_

Your table schema and preview should look like this after you click `Create Table`:

![alt text](http://training.databricks.com/databricks_guide/1_4_ML_Power_Plant_Import_Table_Schema.png)


Step 3: Explore Your Data
-------------------------

Now that we understand what we are trying to do, we need to load our data and describe it, explore it and verify it.

#### Viewing as DataFrame

This is almost necessary for a data scientist to gain visual insights into all pair-wise relationships between the several (3 to 6 or so) variables in question.

    display(powerPlantDF)

| 14.96 | 41.76 | 1024.07 | 73.17 | 463.26 |
|-------|-------|---------|-------|--------|
| 25.18 | 62.96 | 1020.04 | 59.08 | 444.37 |
| 5.11  | 39.4  | 1012.16 | 92.14 | 488.56 |
| 20.86 | 57.32 | 1010.24 | 76.64 | 446.48 |
| 10.82 | 37.5  | 1009.23 | 96.62 | 473.9  |
| 26.27 | 59.44 | 1012.23 | 58.77 | 443.67 |
| 15.89 | 43.96 | 1014.02 | 75.24 | 467.35 |
| 9.48  | 44.71 | 1019.12 | 66.43 | 478.42 |
| 14.64 | 45.0  | 1021.78 | 41.25 | 475.98 |
| 11.74 | 43.56 | 1015.14 | 70.72 | 477.5  |
| 17.99 | 43.72 | 1008.64 | 75.04 | 453.02 |
| 20.14 | 46.93 | 1014.66 | 64.22 | 453.99 |
| 24.34 | 73.5  | 1011.31 | 84.15 | 440.29 |
| 25.71 | 58.59 | 1012.77 | 61.83 | 451.28 |
| 26.19 | 69.34 | 1009.48 | 87.59 | 433.99 |
| 21.42 | 43.79 | 1015.76 | 43.08 | 462.19 |
| 18.21 | 45.0  | 1022.86 | 48.84 | 467.54 |
| 11.04 | 41.74 | 1022.6  | 77.51 | 477.2  |
| 14.45 | 52.75 | 1023.97 | 63.59 | 459.85 |
| 13.97 | 38.47 | 1015.15 | 55.28 | 464.3  |
| 17.76 | 42.42 | 1009.09 | 66.26 | 468.27 |
| 5.41  | 40.07 | 1019.16 | 64.77 | 495.24 |
| 7.76  | 42.28 | 1008.52 | 83.31 | 483.8  |
| 27.23 | 63.9  | 1014.3  | 47.19 | 443.61 |
| 27.36 | 48.6  | 1003.18 | 54.93 | 436.06 |
| 27.47 | 70.72 | 1009.97 | 74.62 | 443.25 |
| 14.6  | 39.31 | 1011.11 | 72.52 | 464.16 |
| 7.91  | 39.96 | 1023.57 | 88.44 | 475.52 |
| 5.81  | 35.79 | 1012.14 | 92.28 | 484.41 |
| 30.53 | 65.18 | 1012.69 | 41.85 | 437.89 |

Truncated to 30 rows

    powerPlantDF.count() // count the number of rows in DF

> res14: Long = 9568

#### Viewing as Table via SQL

Let us look at what tables are already available, as follows:

    sqlContext.tables.show() // Ctrl+Enter to see available tables

> +--------+--------------------+-----------+ |database| tableName|isTemporary| +--------+--------------------+-----------+ | default| cities\_csv| false| | default| cleaned\_taxes| false| | default|commdettrumpclint...| false| | default| donaldtrumptweets| false| | default| linkage| false| | default| nations| false| | default| newmplist| false| | default| ny\_baby\_names| false| | default| nzmpsandparty| false| | default| pos\_neg\_category| false| | default| rna| false| | default| samh| false| | default| table1| false| | default| test\_table| false| | default| uscites| false| +--------+--------------------+-----------+

We need to create a temporary view of the DataFrame as a table before being able to access it via SQL.

    powerPlantDF.createOrReplaceTempView("power_plant_table") // Shift+Enter

Note that table names are in lower-case only!

**You Try!**

    //sqlContext // uncomment and put . after sqlContext and hit Tab to see what methods are available

> res23: org.apache.spark.sql.hive.HiveContext = org.apache.spark.sql.hive.HiveContext@67a5d97d

    //sqlContext.dropTempTable("power_plant_table") // uncomment and Ctrl+Enter if you want to remove the table!

The following SQL statement simply selects all the columns (due to `*`) from `powerPlantTable`.

    -- Ctrl+Enter to query the rows via SQL
    SELECT * FROM power_plant_table

| 14.96 | 41.76 | 1024.07 | 73.17 | 463.26 |
|-------|-------|---------|-------|--------|
| 25.18 | 62.96 | 1020.04 | 59.08 | 444.37 |
| 5.11  | 39.4  | 1012.16 | 92.14 | 488.56 |
| 20.86 | 57.32 | 1010.24 | 76.64 | 446.48 |
| 10.82 | 37.5  | 1009.23 | 96.62 | 473.9  |
| 26.27 | 59.44 | 1012.23 | 58.77 | 443.67 |
| 15.89 | 43.96 | 1014.02 | 75.24 | 467.35 |
| 9.48  | 44.71 | 1019.12 | 66.43 | 478.42 |
| 14.64 | 45.0  | 1021.78 | 41.25 | 475.98 |
| 11.74 | 43.56 | 1015.14 | 70.72 | 477.5  |
| 17.99 | 43.72 | 1008.64 | 75.04 | 453.02 |
| 20.14 | 46.93 | 1014.66 | 64.22 | 453.99 |
| 24.34 | 73.5  | 1011.31 | 84.15 | 440.29 |
| 25.71 | 58.59 | 1012.77 | 61.83 | 451.28 |
| 26.19 | 69.34 | 1009.48 | 87.59 | 433.99 |
| 21.42 | 43.79 | 1015.76 | 43.08 | 462.19 |
| 18.21 | 45.0  | 1022.86 | 48.84 | 467.54 |
| 11.04 | 41.74 | 1022.6  | 77.51 | 477.2  |
| 14.45 | 52.75 | 1023.97 | 63.59 | 459.85 |
| 13.97 | 38.47 | 1015.15 | 55.28 | 464.3  |
| 17.76 | 42.42 | 1009.09 | 66.26 | 468.27 |
| 5.41  | 40.07 | 1019.16 | 64.77 | 495.24 |
| 7.76  | 42.28 | 1008.52 | 83.31 | 483.8  |
| 27.23 | 63.9  | 1014.3  | 47.19 | 443.61 |
| 27.36 | 48.6  | 1003.18 | 54.93 | 436.06 |
| 27.47 | 70.72 | 1009.97 | 74.62 | 443.25 |
| 14.6  | 39.31 | 1011.11 | 72.52 | 464.16 |
| 7.91  | 39.96 | 1023.57 | 88.44 | 475.52 |
| 5.81  | 35.79 | 1012.14 | 92.28 | 484.41 |
| 30.53 | 65.18 | 1012.69 | 41.85 | 437.89 |

Truncated to 30 rows

Note that the output of the above command is the same as `display(powerPlantDF)` we did earlier.

We can use the SQL `desc` command to describe the schema. This is the SQL equivalent of `powerPlantDF.printSchema` we saw earlier.

**Schema Definition**

Our schema definition from UCI appears below:

-   AT = Atmospheric Temperature in C
-   V = Exhaust Vaccum Speed
-   AP = Atmospheric Pressure
-   RH = Relative Humidity
-   PE = Power Output

PE is our label or target. This is the value we are trying to predict given the measurements.

*Reference [UCI Machine Learning Repository Combined Cycle Power Plant Data Set](https://archive.ics.uci.edu/ml/datasets/Combined+Cycle+Power+Plant)*

Let's do some basic statistical analysis of all the columns.

We can use the describe function with no parameters to get some basic stats for each column like count, mean, max, min and standard deviation. More information can be found in the [Spark API docs](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.DataFrame)

    display(powerPlantDF.describe())

| count  | 9568               | 9568               | 9568               | 9568               | 9568               |
|--------|--------------------|--------------------|--------------------|--------------------|--------------------|
| mean   | 19.65123118729102  | 54.30580372073601  | 1013.2590781772603 | 73.30897784280926  | 454.3650094063554  |
| stddev | 7.4524732296110825 | 12.707892998326784 | 5.938783705811581  | 14.600268756728964 | 17.066994999803402 |
| min    | 1.81               | 25.36              | 992.89             | 25.56              | 420.26             |
| max    | 37.11              | 81.56              | 1033.3             | 100.16             | 495.76             |

Step 4: Visualize Your Data
---------------------------

To understand our data, we will look for correlations between features and the label. This can be important when choosing a model. E.g., if features and a label are linearly correlated, a linear model like Linear Regression can do well; if the relationship is very non-linear, more complex models such as Decision Trees or neural networks can be better. We use the Databricks built in visualization to view each of our predictors in relation to the label column as a scatter plot to see the correlation between the predictors and the label.

| 14.96 | 463.26 |
|-------|--------|
| 25.18 | 444.37 |
| 5.11  | 488.56 |
| 20.86 | 446.48 |
| 10.82 | 473.9  |
| 26.27 | 443.67 |
| 15.89 | 467.35 |
| 9.48  | 478.42 |
| 14.64 | 475.98 |
| 11.74 | 477.5  |
| 17.99 | 453.02 |
| 20.14 | 453.99 |
| 24.34 | 440.29 |
| 25.71 | 451.28 |
| 26.19 | 433.99 |
| 21.42 | 462.19 |
| 18.21 | 467.54 |
| 11.04 | 477.2  |
| 14.45 | 459.85 |
| 13.97 | 464.3  |
| 17.76 | 468.27 |
| 5.41  | 495.24 |
| 7.76  | 483.8  |
| 27.23 | 443.61 |
| 27.36 | 436.06 |
| 27.47 | 443.25 |
| 14.6  | 464.16 |
| 7.91  | 475.52 |
| 5.81  | 484.41 |
| 30.53 | 437.89 |

Truncated to 30 rows

From the above plot, it looks like there is strong linear correlation between temperature and Power Output!

| 41.76 | 463.26 |
|-------|--------|
| 62.96 | 444.37 |
| 39.4  | 488.56 |
| 57.32 | 446.48 |
| 37.5  | 473.9  |
| 59.44 | 443.67 |
| 43.96 | 467.35 |
| 44.71 | 478.42 |
| 45.0  | 475.98 |
| 43.56 | 477.5  |
| 43.72 | 453.02 |
| 46.93 | 453.99 |
| 73.5  | 440.29 |
| 58.59 | 451.28 |
| 69.34 | 433.99 |
| 43.79 | 462.19 |
| 45.0  | 467.54 |
| 41.74 | 477.2  |
| 52.75 | 459.85 |
| 38.47 | 464.3  |
| 42.42 | 468.27 |
| 40.07 | 495.24 |
| 42.28 | 483.8  |
| 63.9  | 443.61 |
| 48.6  | 436.06 |
| 70.72 | 443.25 |
| 39.31 | 464.16 |
| 39.96 | 475.52 |
| 35.79 | 484.41 |
| 65.18 | 437.89 |

Truncated to 30 rows

The linear correlation is not as strong between Exhaust Vacuum Speed and Power Output but there is some semblance of a pattern.

| 1024.07 | 463.26 |
|---------|--------|
| 1020.04 | 444.37 |
| 1012.16 | 488.56 |
| 1010.24 | 446.48 |
| 1009.23 | 473.9  |
| 1012.23 | 443.67 |
| 1014.02 | 467.35 |
| 1019.12 | 478.42 |
| 1021.78 | 475.98 |
| 1015.14 | 477.5  |
| 1008.64 | 453.02 |
| 1014.66 | 453.99 |
| 1011.31 | 440.29 |
| 1012.77 | 451.28 |
| 1009.48 | 433.99 |
| 1015.76 | 462.19 |
| 1022.86 | 467.54 |
| 1022.6  | 477.2  |
| 1023.97 | 459.85 |
| 1015.15 | 464.3  |
| 1009.09 | 468.27 |
| 1019.16 | 495.24 |
| 1008.52 | 483.8  |
| 1014.3  | 443.61 |
| 1003.18 | 436.06 |
| 1009.97 | 443.25 |
| 1011.11 | 464.16 |
| 1023.57 | 475.52 |
| 1012.14 | 484.41 |
| 1012.69 | 437.89 |

Truncated to 30 rows

| 73.17 | 463.26 |
|-------|--------|
| 59.08 | 444.37 |
| 92.14 | 488.56 |
| 76.64 | 446.48 |
| 96.62 | 473.9  |
| 58.77 | 443.67 |
| 75.24 | 467.35 |
| 66.43 | 478.42 |
| 41.25 | 475.98 |
| 70.72 | 477.5  |
| 75.04 | 453.02 |
| 64.22 | 453.99 |
| 84.15 | 440.29 |
| 61.83 | 451.28 |
| 87.59 | 433.99 |
| 43.08 | 462.19 |
| 48.84 | 467.54 |
| 77.51 | 477.2  |
| 63.59 | 459.85 |
| 55.28 | 464.3  |
| 66.26 | 468.27 |
| 64.77 | 495.24 |
| 83.31 | 483.8  |
| 47.19 | 443.61 |
| 54.93 | 436.06 |
| 74.62 | 443.25 |
| 72.52 | 464.16 |
| 88.44 | 475.52 |
| 92.28 | 484.41 |
| 41.85 | 437.89 |

Truncated to 30 rows

...and atmospheric pressure and relative humidity seem to have little to no linear correlation.

These pairwise plots can also be done directly using `display` on `select`ed columns of the DataFrame `powerPlantDF`.

In general **we will shy from SQL as much as possible** to focus on ML pipelines written with DataFrames and DataSets with occassional in-and-out of RDDs.

The illustations in `%sql` above are to mainly reassure those with a RDBMS background and SQL that their SQL expressibility can be directly used in Apache Spark and in databricks notebooks.

    display(powerPlantDF.select($"RH", $"PE"))

| 73.17 | 463.26 |
|-------|--------|
| 59.08 | 444.37 |
| 92.14 | 488.56 |
| 76.64 | 446.48 |
| 96.62 | 473.9  |
| 58.77 | 443.67 |
| 75.24 | 467.35 |
| 66.43 | 478.42 |
| 41.25 | 475.98 |
| 70.72 | 477.5  |
| 75.04 | 453.02 |
| 64.22 | 453.99 |
| 84.15 | 440.29 |
| 61.83 | 451.28 |
| 87.59 | 433.99 |
| 43.08 | 462.19 |
| 48.84 | 467.54 |
| 77.51 | 477.2  |
| 63.59 | 459.85 |
| 55.28 | 464.3  |
| 66.26 | 468.27 |
| 64.77 | 495.24 |
| 83.31 | 483.8  |
| 47.19 | 443.61 |
| 54.93 | 436.06 |
| 74.62 | 443.25 |
| 72.52 | 464.16 |
| 88.44 | 475.52 |
| 92.28 | 484.41 |
| 41.85 | 437.89 |

Truncated to 30 rows

Furthermore, you can interactively start playing with `display` on the full DataFrame!

    display(powerPlantDF) // just as we did for the diamonds dataset

| 14.96 | 41.76 | 1024.07 | 73.17 | 463.26 |
|-------|-------|---------|-------|--------|
| 25.18 | 62.96 | 1020.04 | 59.08 | 444.37 |
| 5.11  | 39.4  | 1012.16 | 92.14 | 488.56 |
| 20.86 | 57.32 | 1010.24 | 76.64 | 446.48 |
| 10.82 | 37.5  | 1009.23 | 96.62 | 473.9  |
| 26.27 | 59.44 | 1012.23 | 58.77 | 443.67 |
| 15.89 | 43.96 | 1014.02 | 75.24 | 467.35 |
| 9.48  | 44.71 | 1019.12 | 66.43 | 478.42 |
| 14.64 | 45.0  | 1021.78 | 41.25 | 475.98 |
| 11.74 | 43.56 | 1015.14 | 70.72 | 477.5  |
| 17.99 | 43.72 | 1008.64 | 75.04 | 453.02 |
| 20.14 | 46.93 | 1014.66 | 64.22 | 453.99 |
| 24.34 | 73.5  | 1011.31 | 84.15 | 440.29 |
| 25.71 | 58.59 | 1012.77 | 61.83 | 451.28 |
| 26.19 | 69.34 | 1009.48 | 87.59 | 433.99 |
| 21.42 | 43.79 | 1015.76 | 43.08 | 462.19 |
| 18.21 | 45.0  | 1022.86 | 48.84 | 467.54 |
| 11.04 | 41.74 | 1022.6  | 77.51 | 477.2  |
| 14.45 | 52.75 | 1023.97 | 63.59 | 459.85 |
| 13.97 | 38.47 | 1015.15 | 55.28 | 464.3  |
| 17.76 | 42.42 | 1009.09 | 66.26 | 468.27 |
| 5.41  | 40.07 | 1019.16 | 64.77 | 495.24 |
| 7.76  | 42.28 | 1008.52 | 83.31 | 483.8  |
| 27.23 | 63.9  | 1014.3  | 47.19 | 443.61 |
| 27.36 | 48.6  | 1003.18 | 54.93 | 436.06 |
| 27.47 | 70.72 | 1009.97 | 74.62 | 443.25 |
| 14.6  | 39.31 | 1011.11 | 72.52 | 464.16 |
| 7.91  | 39.96 | 1023.57 | 88.44 | 475.52 |
| 5.81  | 35.79 | 1012.14 | 92.28 | 484.41 |
| 30.53 | 65.18 | 1012.69 | 41.85 | 437.89 |

Truncated to 30 rows

We will do the following steps in the sequel. - *Step 5: Data Preparation* - *Step 6: Data Modeling* - *Step 7: Tuning and Evaluation* - *Step 8: Deployment*

Datasource References: \* Pinar Tüfekci, Prediction of full load electrical power output of a base load operated combined cycle power plant using machine learning methods, International Journal of Electrical Power & Energy Systems, Volume 60, September 2014, Pages 126-140, ISSN 0142-0615, [Web Link](http://www.journals.elsevier.com/international-journal-of-electrical-power-and-energy-systems/) \* Heysem Kaya, Pinar Tüfekci , Sadik Fikret Gürgen: Local and Global Learning Methods for Predicting Power of a Combined Gas & Steam Turbine, Proceedings of the International Conference on Emerging Trends in Computer and Electronics Engineering ICETCEE 2012, pp. 13-18 (Mar. 2012, Dubai) [Web Link](http://www.cmpe.boun.edu.tr/~kaya/kaya2012gasturbine.pdf)

    // let us make sure we are using Spark version greater than 2.2 - we need a version closer to 2.0 if we want to use SparkSession and SQLContext 
    require(sc.version.replace(".", "").toInt >= 220, "Spark 2.2.0+ is required to run this notebook. Please attach it to a Spark 2.2.0+ cluster.")

    powerPlantDF.count

> res8: Long = 9568

#### Viewing the table as text

By uisng `.show` method we can see some of the contents of the table in plain text.

This works in pure Apache Spark, say in `Spark-Shell` without any notebook layer on top of Spark like databricks, zeppelin or jupyter.

It is a good idea to use this method when possible.

    powerPlantDF.show(10) // try putting 1000 here instead of 10

> +-----+-----+-------+-----+------+ | AT| V| AP| RH| PE| +-----+-----+-------+-----+------+ |14.96|41.76|1024.07|73.17|463.26| |25.18|62.96|1020.04|59.08|444.37| | 5.11| 39.4|1012.16|92.14|488.56| |20.86|57.32|1010.24|76.64|446.48| |10.82| 37.5|1009.23|96.62| 473.9| |26.27|59.44|1012.23|58.77|443.67| |15.89|43.96|1014.02|75.24|467.35| | 9.48|44.71|1019.12|66.43|478.42| |14.64| 45.0|1021.78|41.25|475.98| |11.74|43.56|1015.14|70.72| 477.5| +-----+-----+-------+-----+------+ only showing top 10 rows

    spark.catalog.listDatabases.show(false)

> +-------+---------------------+-------------------------+ |name |description |locationUri | +-------+---------------------+-------------------------+ |default|Default Hive database|dbfs:/user/hive/warehouse| +-------+---------------------+-------------------------+

    spark.catalog.listTables.show(false)

> +--------------------------+--------+-----------+---------+-----------+ |name |database|description|tableType|isTemporary| +--------------------------+--------+-----------+---------+-----------+ |cities\_csv |default |null |EXTERNAL |false | |cleaned\_taxes |default |null |MANAGED |false | |commdettrumpclintonretweet|default |null |MANAGED |false | |donaldtrumptweets |default |null |EXTERNAL |false | |linkage |default |null |EXTERNAL |false | |nations |default |null |EXTERNAL |false | |newmplist |default |null |EXTERNAL |false | |ny\_baby\_names |default |null |MANAGED |false | |nzmpsandparty |default |null |EXTERNAL |false | |pos\_neg\_category |default |null |EXTERNAL |false | |rna |default |null |MANAGED |false | |samh |default |null |EXTERNAL |false | |table1 |default |null |EXTERNAL |false | |test\_table |default |null |EXTERNAL |false | |uscites |default |null |EXTERNAL |false | +--------------------------+--------+-----------+---------+-----------+

We can also access the list of tables and databases using `spark.catalog` methods as explained here: \* <https://databricks.com/blog/2016/08/15/how-to-use-sparksession-in-apache-spark-2-0.html>

    sqlContext.tables.show() 

> +--------+--------------------+-----------+ |database| tableName|isTemporary| +--------+--------------------+-----------+ | default| cities\_csv| false| | default| cleaned\_taxes| false| | default|commdettrumpclint...| false| | default| donaldtrumptweets| false| | default| linkage| false| | default| nations| false| | default| newmplist| false| | default| ny\_baby\_names| false| | default| nzmpsandparty| false| | default| pos\_neg\_category| false| | default| rna| false| | default| samh| false| | default| table1| false| | default| test\_table| false| | default| uscites| false| | | power\_plant\_table| true| +--------+--------------------+-----------+

