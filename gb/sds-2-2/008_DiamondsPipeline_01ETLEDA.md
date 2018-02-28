[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

### Diamonds ML Pipeline Workflow - DataFrame ETL and EDA Part

This is the Spark SQL parts that are focussed on extract-transform-Load (ETL) and exploratory-data-analysis (EDA) parts of an end-to-end example of a Machine Learning (ML) workflow.

**Why are we using DataFrames?** *This is because of the **Announcement** in the Spark MLlib Main Guide for Spark 2.2* <https://spark.apache.org/docs/latest/ml-guide.html> that *"DataFrame-based API is primary API"*.

This notebook is a scala*rific* break-down of the python*ic* 'Diamonds ML Pipeline Workflow' from the Databricks Guide.

**We will see this example again in the sequel**

For this example, we analyze the Diamonds dataset from the R Datasets hosted on DBC.

Later on, we will use the [DecisionTree algorithm](http://spark.apache.org/docs/latest/ml-classification-regression.html#decision-trees) to predict the price of a diamond from its characteristics.

Here is an outline of our pipeline: \* **Step 1. *Load data*: Load data as DataFrame** \* **Step 2. *Understand the data*: Compute statistics and create visualizations to get a better understanding of the data.** \* Step 3. *Hold out data*: Split the data randomly into training and test sets. We will not look at the test data until *after* learning. \* Step 4. On the training dataset: \* *Extract features*: We will index categorical (String-valued) features so that DecisionTree can handle them. \* *Learn a model*: Run DecisionTree to learn how to predict a diamond's price from a description of the diamond. \* *Tune the model*: Tune the tree depth (complexity) using the training data. (This process is also called *model selection*.) \* Step 5. *Evaluate the model*: Now look at the test dataset. Compare the initial model with the tuned model to see the benefit of tuning parameters. \* Step 6. *Understand the model*: We will examine the learned model and results to gain further insight.

In this notebook, we will only cover **Step 1** and **Step 2.** above. The other Steps will be revisited in the sequel.

### Step 1. Load data as DataFrame

This section loads a dataset as a DataFrame and examines a few rows of it to understand the schema.

For more info, see the DB guide on [importing data](https://docs.databricks.com/user-guide/importing-data.html).

``` scala
// We'll use the Diamonds dataset from the R datasets hosted on DBC.
val diamondsFilePath = "dbfs:/databricks-datasets/Rdatasets/data-001/csv/ggplot2/diamonds.csv"
```

>     diamondsFilePath: String = dbfs:/databricks-datasets/Rdatasets/data-001/csv/ggplot2/diamonds.csv

``` scala
sc.textFile(diamondsFilePath).take(2) // looks like a csv file as it should
```

>     res2: Array[String] = Array("","carat","cut","color","clarity","depth","table","price","x","y","z", "1",0.23,"Ideal","E","SI2",61.5,55,326,3.95,3.98,2.43)

``` scala
val diamondsRawDF = sqlContext.read    // we can use sqlContext instead of SparkSession for backwards compatibility to 1.x
    .format("com.databricks.spark.csv") // use spark.csv package
    .option("header", "true") // Use first line of all files as header
    .option("inferSchema", "true") // Automatically infer data types
    //.option("delimiter", ",") // Specify the delimiter as comma or ',' DEFAULT
    .load(diamondsFilePath)
```

>     diamondsRawDF: org.apache.spark.sql.DataFrame = [_c0: int, carat: double ... 9 more fields]

``` scala
//There are 10 columns.  We will try to predict the price of diamonds, treating the other 9 columns as features.
diamondsRawDF.printSchema()
```

>     root
>      |-- _c0: integer (nullable = true)
>      |-- carat: double (nullable = true)
>      |-- cut: string (nullable = true)
>      |-- color: string (nullable = true)
>      |-- clarity: string (nullable = true)
>      |-- depth: double (nullable = true)
>      |-- table: double (nullable = true)
>      |-- price: integer (nullable = true)
>      |-- x: double (nullable = true)
>      |-- y: double (nullable = true)
>      |-- z: double (nullable = true)

*Note:* `(nullable = true)` simply means if the value is allowed to be `null`.

Let us count the number of rows in `diamondsDF`.

``` scala
diamondsRawDF.count() // Ctrl+Enter
```

>     res4: Long = 53940

So there are 53940 records or rows in the DataFrame.

Use the `show(n)` method to see the first `n` (default is 20) rows of the DataFrame, as folows:

``` scala
diamondsRawDF.show(10)
```

>     +---+-----+---------+-----+-------+-----+-----+-----+----+----+----+
>     |_c0|carat|      cut|color|clarity|depth|table|price|   x|   y|   z|
>     +---+-----+---------+-----+-------+-----+-----+-----+----+----+----+
>     |  1| 0.23|    Ideal|    E|    SI2| 61.5| 55.0|  326|3.95|3.98|2.43|
>     |  2| 0.21|  Premium|    E|    SI1| 59.8| 61.0|  326|3.89|3.84|2.31|
>     |  3| 0.23|     Good|    E|    VS1| 56.9| 65.0|  327|4.05|4.07|2.31|
>     |  4| 0.29|  Premium|    I|    VS2| 62.4| 58.0|  334| 4.2|4.23|2.63|
>     |  5| 0.31|     Good|    J|    SI2| 63.3| 58.0|  335|4.34|4.35|2.75|
>     |  6| 0.24|Very Good|    J|   VVS2| 62.8| 57.0|  336|3.94|3.96|2.48|
>     |  7| 0.24|Very Good|    I|   VVS1| 62.3| 57.0|  336|3.95|3.98|2.47|
>     |  8| 0.26|Very Good|    H|    SI1| 61.9| 55.0|  337|4.07|4.11|2.53|
>     |  9| 0.22|     Fair|    E|    VS2| 65.1| 61.0|  337|3.87|3.78|2.49|
>     | 10| 0.23|Very Good|    H|    VS1| 59.4| 61.0|  338| 4.0|4.05|2.39|
>     +---+-----+---------+-----+-------+-----+-----+-----+----+----+----+
>     only showing top 10 rows

If you notice the schema of `diamondsRawDF` you will see that the automatic schema inference of `SqlContext.read` method has cast the values in the column `price` as `integer`.

To cleanup: \* let's recast the column `price` as `double` for downstream ML tasks later and \* let's also get rid of the first column of row indices.

``` scala
import org.apache.spark.sql.types.DoubleType
//we will convert price column from int to double for being able to model, fit and predict in downstream ML task
val diamondsDF = diamondsRawDF.select($"carat", $"cut", $"color", $"clarity", $"depth", $"table",$"price".cast(DoubleType).as("price"), $"x", $"y", $"z")
diamondsDF.cache() // let's cache it for reuse
diamondsDF.printSchema // print schema
```

>     root
>      |-- carat: double (nullable = true)
>      |-- cut: string (nullable = true)
>      |-- color: string (nullable = true)
>      |-- clarity: string (nullable = true)
>      |-- depth: double (nullable = true)
>      |-- table: double (nullable = true)
>      |-- price: double (nullable = true)
>      |-- x: double (nullable = true)
>      |-- y: double (nullable = true)
>      |-- z: double (nullable = true)
>
>     import org.apache.spark.sql.types.DoubleType
>     diamondsDF: org.apache.spark.sql.DataFrame = [carat: double, cut: string ... 8 more fields]

``` scala
diamondsDF.show(10,false) // notice that price column has Double values that end in '.0' now
```

>     +-----+---------+-----+-------+-----+-----+-----+----+----+----+
>     |carat|cut      |color|clarity|depth|table|price|x   |y   |z   |
>     +-----+---------+-----+-------+-----+-----+-----+----+----+----+
>     |0.23 |Ideal    |E    |SI2    |61.5 |55.0 |326.0|3.95|3.98|2.43|
>     |0.21 |Premium  |E    |SI1    |59.8 |61.0 |326.0|3.89|3.84|2.31|
>     |0.23 |Good     |E    |VS1    |56.9 |65.0 |327.0|4.05|4.07|2.31|
>     |0.29 |Premium  |I    |VS2    |62.4 |58.0 |334.0|4.2 |4.23|2.63|
>     |0.31 |Good     |J    |SI2    |63.3 |58.0 |335.0|4.34|4.35|2.75|
>     |0.24 |Very Good|J    |VVS2   |62.8 |57.0 |336.0|3.94|3.96|2.48|
>     |0.24 |Very Good|I    |VVS1   |62.3 |57.0 |336.0|3.95|3.98|2.47|
>     |0.26 |Very Good|H    |SI1    |61.9 |55.0 |337.0|4.07|4.11|2.53|
>     |0.22 |Fair     |E    |VS2    |65.1 |61.0 |337.0|3.87|3.78|2.49|
>     |0.23 |Very Good|H    |VS1    |59.4 |61.0 |338.0|4.0 |4.05|2.39|
>     +-----+---------+-----+-------+-----+-----+-----+----+----+----+
>     only showing top 10 rows

``` scala
//View DataFrame in databricks
// note this 'display' is a databricks notebook specific command that is quite powerful for visual interaction with the data
// other notebooks like zeppelin have similar commands for interactive visualisation
display(diamondsDF) 
```

| carat | cut       | color | clarity | depth | table | price | x    | y    | z    |
|-------|-----------|-------|---------|-------|-------|-------|------|------|------|
| 0.23  | Ideal     | E     | SI2     | 61.5  | 55.0  | 326.0 | 3.95 | 3.98 | 2.43 |
| 0.21  | Premium   | E     | SI1     | 59.8  | 61.0  | 326.0 | 3.89 | 3.84 | 2.31 |
| 0.23  | Good      | E     | VS1     | 56.9  | 65.0  | 327.0 | 4.05 | 4.07 | 2.31 |
| 0.29  | Premium   | I     | VS2     | 62.4  | 58.0  | 334.0 | 4.2  | 4.23 | 2.63 |
| 0.31  | Good      | J     | SI2     | 63.3  | 58.0  | 335.0 | 4.34 | 4.35 | 2.75 |
| 0.24  | Very Good | J     | VVS2    | 62.8  | 57.0  | 336.0 | 3.94 | 3.96 | 2.48 |
| 0.24  | Very Good | I     | VVS1    | 62.3  | 57.0  | 336.0 | 3.95 | 3.98 | 2.47 |
| 0.26  | Very Good | H     | SI1     | 61.9  | 55.0  | 337.0 | 4.07 | 4.11 | 2.53 |
| 0.22  | Fair      | E     | VS2     | 65.1  | 61.0  | 337.0 | 3.87 | 3.78 | 2.49 |
| 0.23  | Very Good | H     | VS1     | 59.4  | 61.0  | 338.0 | 4.0  | 4.05 | 2.39 |
| 0.3   | Good      | J     | SI1     | 64.0  | 55.0  | 339.0 | 4.25 | 4.28 | 2.73 |
| 0.23  | Ideal     | J     | VS1     | 62.8  | 56.0  | 340.0 | 3.93 | 3.9  | 2.46 |
| 0.22  | Premium   | F     | SI1     | 60.4  | 61.0  | 342.0 | 3.88 | 3.84 | 2.33 |
| 0.31  | Ideal     | J     | SI2     | 62.2  | 54.0  | 344.0 | 4.35 | 4.37 | 2.71 |
| 0.2   | Premium   | E     | SI2     | 60.2  | 62.0  | 345.0 | 3.79 | 3.75 | 2.27 |
| 0.32  | Premium   | E     | I1      | 60.9  | 58.0  | 345.0 | 4.38 | 4.42 | 2.68 |
| 0.3   | Ideal     | I     | SI2     | 62.0  | 54.0  | 348.0 | 4.31 | 4.34 | 2.68 |
| 0.3   | Good      | J     | SI1     | 63.4  | 54.0  | 351.0 | 4.23 | 4.29 | 2.7  |
| 0.3   | Good      | J     | SI1     | 63.8  | 56.0  | 351.0 | 4.23 | 4.26 | 2.71 |
| 0.3   | Very Good | J     | SI1     | 62.7  | 59.0  | 351.0 | 4.21 | 4.27 | 2.66 |
| 0.3   | Good      | I     | SI2     | 63.3  | 56.0  | 351.0 | 4.26 | 4.3  | 2.71 |
| 0.23  | Very Good | E     | VS2     | 63.8  | 55.0  | 352.0 | 3.85 | 3.92 | 2.48 |
| 0.23  | Very Good | H     | VS1     | 61.0  | 57.0  | 353.0 | 3.94 | 3.96 | 2.41 |
| 0.31  | Very Good | J     | SI1     | 59.4  | 62.0  | 353.0 | 4.39 | 4.43 | 2.62 |
| 0.31  | Very Good | J     | SI1     | 58.1  | 62.0  | 353.0 | 4.44 | 4.47 | 2.59 |
| 0.23  | Very Good | G     | VVS2    | 60.4  | 58.0  | 354.0 | 3.97 | 4.01 | 2.41 |
| 0.24  | Premium   | I     | VS1     | 62.5  | 57.0  | 355.0 | 3.97 | 3.94 | 2.47 |
| 0.3   | Very Good | J     | VS2     | 62.2  | 57.0  | 357.0 | 4.28 | 4.3  | 2.67 |
| 0.23  | Very Good | D     | VS2     | 60.5  | 61.0  | 357.0 | 3.96 | 3.97 | 2.4  |
| 0.23  | Very Good | F     | VS1     | 60.9  | 57.0  | 357.0 | 3.96 | 3.99 | 2.42 |

Truncated to 30 rows

### Step 2. Understand the data

Let's examine the data to get a better understanding of what is there. We only examine a couple of features (columns), but it gives an idea of the type of exploration you might do to understand a new dataset.

For more examples of using Databricks's visualization (even across languages) see <https://docs.databricks.com/user-guide/visualizations/index.html> NOW.

We can see that we have a mix of \* categorical features (`cut`, `color`, `clarity`) and \* continuous features (`depth`, `x`, `y`, `z`).

##### Let's first look at the categorical features.

You can also select one or more individual columns using so-called DataFrame API.

Let us `select` the column `cut` from `diamondsDF` and create a new DataFrame called `cutsDF` and then display it as follows:

``` scala
val cutsDF = diamondsDF.select("cut") // Shift+Enter
```

>     cutsDF: org.apache.spark.sql.DataFrame = [cut: string]

``` scala
cutsDF.show(10) // Ctrl+Enter
```

>     +---------+
>     |      cut|
>     +---------+
>     |    Ideal|
>     |  Premium|
>     |     Good|
>     |  Premium|
>     |     Good|
>     |Very Good|
>     |Very Good|
>     |Very Good|
>     |     Fair|
>     |Very Good|
>     +---------+
>     only showing top 10 rows

Let us use `distinct` to find the distinct types of `cut`'s in the dataset.

``` scala
// View distinct diamond cuts in dataset
val cutsDistinctDF = diamondsDF.select("cut").distinct()
```

>     cutsDistinctDF: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [cut: string]

``` scala
cutsDistinctDF.show()
```

>     +---------+
>     |      cut|
>     +---------+
>     |  Premium|
>     |    Ideal|
>     |     Good|
>     |     Fair|
>     |Very Good|
>     +---------+

Clearly, there are just 5 kinds of cuts.

``` scala
// View distinct diamond colors in dataset
val colorsDistinctDF = diamondsDF.select("color").distinct() //.collect()
colorsDistinctDF.show()
```

>     +-----+
>     |color|
>     +-----+
>     |    F|
>     |    E|
>     |    D|
>     |    J|
>     |    G|
>     |    I|
>     |    H|
>     +-----+
>
>     colorsDistinctDF: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [color: string]

``` scala
// View distinct diamond clarities in dataset
val claritiesDistinctDF = diamondsDF.select("clarity").distinct() // .collect()
claritiesDistinctDF.show()
```

>     +-------+
>     |clarity|
>     +-------+
>     |   VVS2|
>     |    SI1|
>     |     IF|
>     |     I1|
>     |   VVS1|
>     |    VS2|
>     |    SI2|
>     |    VS1|
>     +-------+
>
>     claritiesDistinctDF: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [clarity: string]

We can examine the distribution of a particular feature by using display(),

**You Try!**

1.  Click on the chart icon and Plot Options, and setting:

-   Value=`<id>`
-   Series groupings='cut'
-   and Aggregation=`COUNT`.

1.  You can also try this using columns "color" and "clarity"

``` scala
display(diamondsDF.select("cut"))
```

| cut       |
|-----------|
| Ideal     |
| Premium   |
| Good      |
| Premium   |
| Good      |
| Very Good |
| Very Good |
| Very Good |
| Fair      |
| Very Good |
| Good      |
| Ideal     |
| Premium   |
| Ideal     |
| Premium   |
| Premium   |
| Ideal     |
| Good      |
| Good      |
| Very Good |
| Good      |
| Very Good |
| Very Good |
| Very Good |
| Very Good |
| Very Good |
| Premium   |
| Very Good |
| Very Good |
| Very Good |

Truncated to 30 rows

``` scala
// come on do the same for color NOW!
```

``` scala
// and clarity too...
```

\*\* You Try!\*\*

Now play around with display of the entire DF and choosing what you want in the GUI as opposed to a `.select(...)` statement earlier.

For instance, the following `display(diamondsDF)` shows the counts of the colors by choosing in the `Plot Options` a `bar-chart` that is `grouped` with `Series Grouping` as `color`, `values` as `<id>` and `Aggregation` as `COUNT`. You can click on `Plot Options` to see these settings and can change them as you wish by dragging and dropping.

``` scala
 display(diamondsDF)
```

| carat | cut       | color | clarity | depth | table | price | x    | y    | z    |
|-------|-----------|-------|---------|-------|-------|-------|------|------|------|
| 0.23  | Ideal     | E     | SI2     | 61.5  | 55.0  | 326.0 | 3.95 | 3.98 | 2.43 |
| 0.21  | Premium   | E     | SI1     | 59.8  | 61.0  | 326.0 | 3.89 | 3.84 | 2.31 |
| 0.23  | Good      | E     | VS1     | 56.9  | 65.0  | 327.0 | 4.05 | 4.07 | 2.31 |
| 0.29  | Premium   | I     | VS2     | 62.4  | 58.0  | 334.0 | 4.2  | 4.23 | 2.63 |
| 0.31  | Good      | J     | SI2     | 63.3  | 58.0  | 335.0 | 4.34 | 4.35 | 2.75 |
| 0.24  | Very Good | J     | VVS2    | 62.8  | 57.0  | 336.0 | 3.94 | 3.96 | 2.48 |
| 0.24  | Very Good | I     | VVS1    | 62.3  | 57.0  | 336.0 | 3.95 | 3.98 | 2.47 |
| 0.26  | Very Good | H     | SI1     | 61.9  | 55.0  | 337.0 | 4.07 | 4.11 | 2.53 |
| 0.22  | Fair      | E     | VS2     | 65.1  | 61.0  | 337.0 | 3.87 | 3.78 | 2.49 |
| 0.23  | Very Good | H     | VS1     | 59.4  | 61.0  | 338.0 | 4.0  | 4.05 | 2.39 |
| 0.3   | Good      | J     | SI1     | 64.0  | 55.0  | 339.0 | 4.25 | 4.28 | 2.73 |
| 0.23  | Ideal     | J     | VS1     | 62.8  | 56.0  | 340.0 | 3.93 | 3.9  | 2.46 |
| 0.22  | Premium   | F     | SI1     | 60.4  | 61.0  | 342.0 | 3.88 | 3.84 | 2.33 |
| 0.31  | Ideal     | J     | SI2     | 62.2  | 54.0  | 344.0 | 4.35 | 4.37 | 2.71 |
| 0.2   | Premium   | E     | SI2     | 60.2  | 62.0  | 345.0 | 3.79 | 3.75 | 2.27 |
| 0.32  | Premium   | E     | I1      | 60.9  | 58.0  | 345.0 | 4.38 | 4.42 | 2.68 |
| 0.3   | Ideal     | I     | SI2     | 62.0  | 54.0  | 348.0 | 4.31 | 4.34 | 2.68 |
| 0.3   | Good      | J     | SI1     | 63.4  | 54.0  | 351.0 | 4.23 | 4.29 | 2.7  |
| 0.3   | Good      | J     | SI1     | 63.8  | 56.0  | 351.0 | 4.23 | 4.26 | 2.71 |
| 0.3   | Very Good | J     | SI1     | 62.7  | 59.0  | 351.0 | 4.21 | 4.27 | 2.66 |
| 0.3   | Good      | I     | SI2     | 63.3  | 56.0  | 351.0 | 4.26 | 4.3  | 2.71 |
| 0.23  | Very Good | E     | VS2     | 63.8  | 55.0  | 352.0 | 3.85 | 3.92 | 2.48 |
| 0.23  | Very Good | H     | VS1     | 61.0  | 57.0  | 353.0 | 3.94 | 3.96 | 2.41 |
| 0.31  | Very Good | J     | SI1     | 59.4  | 62.0  | 353.0 | 4.39 | 4.43 | 2.62 |
| 0.31  | Very Good | J     | SI1     | 58.1  | 62.0  | 353.0 | 4.44 | 4.47 | 2.59 |
| 0.23  | Very Good | G     | VVS2    | 60.4  | 58.0  | 354.0 | 3.97 | 4.01 | 2.41 |
| 0.24  | Premium   | I     | VS1     | 62.5  | 57.0  | 355.0 | 3.97 | 3.94 | 2.47 |
| 0.3   | Very Good | J     | VS2     | 62.2  | 57.0  | 357.0 | 4.28 | 4.3  | 2.67 |
| 0.23  | Very Good | D     | VS2     | 60.5  | 61.0  | 357.0 | 3.96 | 3.97 | 2.4  |
| 0.23  | Very Good | F     | VS1     | 60.9  | 57.0  | 357.0 | 3.96 | 3.99 | 2.42 |

Truncated to 30 rows

Now let's examine one of the continuous features as an example.

``` scala
//Select: "Plot Options..." --> "Display type" --> "histogram plot" and choose to "Plot over all results" OTHERWISE you get the image from first 1000 rows only
display(diamondsDF.select("carat"))
```

| carat |
|-------|
| 0.23  |
| 0.21  |
| 0.23  |
| 0.29  |
| 0.31  |
| 0.24  |
| 0.24  |
| 0.26  |
| 0.22  |
| 0.23  |
| 0.3   |
| 0.23  |
| 0.22  |
| 0.31  |
| 0.2   |
| 0.32  |
| 0.3   |
| 0.3   |
| 0.3   |
| 0.3   |
| 0.3   |
| 0.23  |
| 0.23  |
| 0.31  |
| 0.31  |
| 0.23  |
| 0.24  |
| 0.3   |
| 0.23  |
| 0.23  |

Truncated to 30 rows

The above histogram of the diamonds' carat ratings shows that carats have a skewed distribution: Many diamonds are small, but there are a number of diamonds in the dataset which are much larger.

-   Extremely skewed distributions can cause problems for some algorithms (e.g., Linear Regression).
-   However, Decision Trees handle skewed distributions very naturally.

Note: When you call `display` to create a histogram like that above, **it will plot using a subsample from the dataset** (for efficiency), but you can plot using the full dataset by selecting "Plot over all results". For our dataset, the two plots can actually look very different due to the long-tailed distribution.

We will not examine the label distribution for now. It can be helpful to examine the label distribution, but it is best to do so only on the training set, not on the test set which we will hold out for evaluation. These will be seen in the sequel

**You Try!** Of course knock youself out visually exploring the dataset more...

``` scala
display(diamondsDF.select("cut","carat"))
```

| cut       | carat |
|-----------|-------|
| Ideal     | 0.23  |
| Premium   | 0.21  |
| Good      | 0.23  |
| Premium   | 0.29  |
| Good      | 0.31  |
| Very Good | 0.24  |
| Very Good | 0.24  |
| Very Good | 0.26  |
| Fair      | 0.22  |
| Very Good | 0.23  |
| Good      | 0.3   |
| Ideal     | 0.23  |
| Premium   | 0.22  |
| Ideal     | 0.31  |
| Premium   | 0.2   |
| Premium   | 0.32  |
| Ideal     | 0.3   |
| Good      | 0.3   |
| Good      | 0.3   |
| Very Good | 0.3   |
| Good      | 0.3   |
| Very Good | 0.23  |
| Very Good | 0.23  |
| Very Good | 0.31  |
| Very Good | 0.31  |
| Very Good | 0.23  |
| Premium   | 0.24  |
| Very Good | 0.3   |
| Very Good | 0.23  |
| Very Good | 0.23  |

Truncated to 30 rows

Try scatter plot to see pairwise scatter plots of continuous features.

``` scala
display(diamondsDF) //Ctrl+Enter 
```

| carat | cut       | color | clarity | depth | table | price | x    | y    | z    |
|-------|-----------|-------|---------|-------|-------|-------|------|------|------|
| 0.23  | Ideal     | E     | SI2     | 61.5  | 55.0  | 326.0 | 3.95 | 3.98 | 2.43 |
| 0.21  | Premium   | E     | SI1     | 59.8  | 61.0  | 326.0 | 3.89 | 3.84 | 2.31 |
| 0.23  | Good      | E     | VS1     | 56.9  | 65.0  | 327.0 | 4.05 | 4.07 | 2.31 |
| 0.29  | Premium   | I     | VS2     | 62.4  | 58.0  | 334.0 | 4.2  | 4.23 | 2.63 |
| 0.31  | Good      | J     | SI2     | 63.3  | 58.0  | 335.0 | 4.34 | 4.35 | 2.75 |
| 0.24  | Very Good | J     | VVS2    | 62.8  | 57.0  | 336.0 | 3.94 | 3.96 | 2.48 |
| 0.24  | Very Good | I     | VVS1    | 62.3  | 57.0  | 336.0 | 3.95 | 3.98 | 2.47 |
| 0.26  | Very Good | H     | SI1     | 61.9  | 55.0  | 337.0 | 4.07 | 4.11 | 2.53 |
| 0.22  | Fair      | E     | VS2     | 65.1  | 61.0  | 337.0 | 3.87 | 3.78 | 2.49 |
| 0.23  | Very Good | H     | VS1     | 59.4  | 61.0  | 338.0 | 4.0  | 4.05 | 2.39 |
| 0.3   | Good      | J     | SI1     | 64.0  | 55.0  | 339.0 | 4.25 | 4.28 | 2.73 |
| 0.23  | Ideal     | J     | VS1     | 62.8  | 56.0  | 340.0 | 3.93 | 3.9  | 2.46 |
| 0.22  | Premium   | F     | SI1     | 60.4  | 61.0  | 342.0 | 3.88 | 3.84 | 2.33 |
| 0.31  | Ideal     | J     | SI2     | 62.2  | 54.0  | 344.0 | 4.35 | 4.37 | 2.71 |
| 0.2   | Premium   | E     | SI2     | 60.2  | 62.0  | 345.0 | 3.79 | 3.75 | 2.27 |
| 0.32  | Premium   | E     | I1      | 60.9  | 58.0  | 345.0 | 4.38 | 4.42 | 2.68 |
| 0.3   | Ideal     | I     | SI2     | 62.0  | 54.0  | 348.0 | 4.31 | 4.34 | 2.68 |
| 0.3   | Good      | J     | SI1     | 63.4  | 54.0  | 351.0 | 4.23 | 4.29 | 2.7  |
| 0.3   | Good      | J     | SI1     | 63.8  | 56.0  | 351.0 | 4.23 | 4.26 | 2.71 |
| 0.3   | Very Good | J     | SI1     | 62.7  | 59.0  | 351.0 | 4.21 | 4.27 | 2.66 |
| 0.3   | Good      | I     | SI2     | 63.3  | 56.0  | 351.0 | 4.26 | 4.3  | 2.71 |
| 0.23  | Very Good | E     | VS2     | 63.8  | 55.0  | 352.0 | 3.85 | 3.92 | 2.48 |
| 0.23  | Very Good | H     | VS1     | 61.0  | 57.0  | 353.0 | 3.94 | 3.96 | 2.41 |
| 0.31  | Very Good | J     | SI1     | 59.4  | 62.0  | 353.0 | 4.39 | 4.43 | 2.62 |
| 0.31  | Very Good | J     | SI1     | 58.1  | 62.0  | 353.0 | 4.44 | 4.47 | 2.59 |
| 0.23  | Very Good | G     | VVS2    | 60.4  | 58.0  | 354.0 | 3.97 | 4.01 | 2.41 |
| 0.24  | Premium   | I     | VS1     | 62.5  | 57.0  | 355.0 | 3.97 | 3.94 | 2.47 |
| 0.3   | Very Good | J     | VS2     | 62.2  | 57.0  | 357.0 | 4.28 | 4.3  | 2.67 |
| 0.23  | Very Good | D     | VS2     | 60.5  | 61.0  | 357.0 | 3.96 | 3.97 | 2.4  |
| 0.23  | Very Good | F     | VS1     | 60.9  | 57.0  | 357.0 | 3.96 | 3.99 | 2.42 |

Truncated to 30 rows

Note that columns of type string are not in the scatter plot!

``` scala
diamondsDF.printSchema // Ctrl+Enter
```

>     root
>      |-- carat: double (nullable = true)
>      |-- cut: string (nullable = true)
>      |-- color: string (nullable = true)
>      |-- clarity: string (nullable = true)
>      |-- depth: double (nullable = true)
>      |-- table: double (nullable = true)
>      |-- price: double (nullable = true)
>      |-- x: double (nullable = true)
>      |-- y: double (nullable = true)
>      |-- z: double (nullable = true)

### Let us run through some basic inteactive SQL queries next

-   HiveQL supports =, &lt;, &gt;, &lt;=, &gt;= and != operators. It also supports LIKE operator for fuzzy matching of Strings
-   Enclose Strings in single quotes
-   Multiple conditions can be combined using `and` and `or`
-   Enclose conditions in `()` for precedence
-   ...
-   ...

**Why do I need to learn interactive SQL queries?**

Such queries in the widely known declarative SQL language can help us explore the data and thereby inform the modeling process!!!

Using DataFrame API, we can apply a `filter` after `select` to transform the DataFrame `diamondsDF` to the new DataFrame `diamondsDColoredDF`.

Below, `$` is an alias for column.

Let as select the columns named `carat`, `colour`, `price` where `color` value is equal to `D`.

``` scala
val diamondsDColoredDF = diamondsDF.select("carat", "color", "price").filter($"color" === "D") // Shift+Enter
```

>     diamondsDColoredDF: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [carat: double, color: string ... 1 more field]

``` scala
diamondsDColoredDF.show(10) // Ctrl+Enter
```

>     +-----+-----+-----+
>     |carat|color|price|
>     +-----+-----+-----+
>     | 0.23|    D|357.0|
>     | 0.23|    D|402.0|
>     | 0.26|    D|403.0|
>     | 0.26|    D|403.0|
>     | 0.26|    D|403.0|
>     | 0.22|    D|404.0|
>     |  0.3|    D|552.0|
>     |  0.3|    D|552.0|
>     |  0.3|    D|552.0|
>     | 0.24|    D|553.0|
>     +-----+-----+-----+
>     only showing top 10 rows

As you can see all the colors are now 'D'. But to really confirm this we can do the following for fun:

``` scala
diamondsDColoredDF.select("color").distinct().show
```

>     +-----+
>     |color|
>     +-----+
>     |    D|
>     +-----+

Let's try to do the same in SQL for those who know SQL from before.

First we need to see if the table is registerd (not just the DataFrame), and if not we ened to register our DataFrame as a temporary table.

``` scala
sqlContext.tables.show() // Ctrl+Enter to see available tables
```

>     +--------+--------------------+-----------+
>     |database|           tableName|isTemporary|
>     +--------+--------------------+-----------+
>     | default|          cities_csv|      false|
>     | default|       cleaned_taxes|      false|
>     | default|commdettrumpclint...|      false|
>     | default|   donaldtrumptweets|      false|
>     | default|             linkage|      false|
>     | default|             nations|      false|
>     | default|           newmplist|      false|
>     | default|       ny_baby_names|      false|
>     | default|       nzmpsandparty|      false|
>     | default|    pos_neg_category|      false|
>     | default|                 rna|      false|
>     | default|                samh|      false|
>     | default|              table1|      false|
>     | default|          test_table|      false|
>     | default|             uscites|      false|
>     +--------+--------------------+-----------+

Looks like diamonds is already there (if not just execute the following cell).

``` scala
diamondsDF.createOrReplaceTempView("diamonds")
```

``` scala
sqlContext.tables.show() // Ctrl+Enter to see available tables
```

>     +--------+--------------------+-----------+
>     |database|           tableName|isTemporary|
>     +--------+--------------------+-----------+
>     | default|          cities_csv|      false|
>     | default|       cleaned_taxes|      false|
>     | default|commdettrumpclint...|      false|
>     | default|   donaldtrumptweets|      false|
>     | default|             linkage|      false|
>     | default|             nations|      false|
>     | default|           newmplist|      false|
>     | default|       ny_baby_names|      false|
>     | default|       nzmpsandparty|      false|
>     | default|    pos_neg_category|      false|
>     | default|                 rna|      false|
>     | default|                samh|      false|
>     | default|              table1|      false|
>     | default|          test_table|      false|
>     | default|             uscites|      false|
>     |        |            diamonds|       true|
>     +--------+--------------------+-----------+

``` sql
-- Shift+Enter to do the same in SQL
select carat, color, price from diamonds where color='D'
```

| carat | color | price  |
|-------|-------|--------|
| 0.23  | D     | 357.0  |
| 0.23  | D     | 402.0  |
| 0.26  | D     | 403.0  |
| 0.26  | D     | 403.0  |
| 0.26  | D     | 403.0  |
| 0.22  | D     | 404.0  |
| 0.3   | D     | 552.0  |
| 0.3   | D     | 552.0  |
| 0.3   | D     | 552.0  |
| 0.24  | D     | 553.0  |
| 0.26  | D     | 554.0  |
| 0.26  | D     | 554.0  |
| 0.26  | D     | 554.0  |
| 0.75  | D     | 2760.0 |
| 0.71  | D     | 2762.0 |
| 0.61  | D     | 2763.0 |
| 0.71  | D     | 2764.0 |
| 0.71  | D     | 2764.0 |
| 0.7   | D     | 2767.0 |
| 0.71  | D     | 2767.0 |
| 0.73  | D     | 2768.0 |
| 0.7   | D     | 2768.0 |
| 0.71  | D     | 2768.0 |
| 0.71  | D     | 2770.0 |
| 0.76  | D     | 2770.0 |
| 0.73  | D     | 2770.0 |
| 0.75  | D     | 2773.0 |
| 0.7   | D     | 2773.0 |
| 0.7   | D     | 2777.0 |
| 0.53  | D     | 2782.0 |

Truncated to 30 rows

Alternatively, one could just write the SQL statement in scala to create a new DataFrame `diamondsDColoredDF_FromTable` from the table `diamonds` and display it, as follows:

``` scala
val diamondsDColoredDF_FromTable = sqlContext.sql("select carat, color, price from diamonds where color='D'") // Shift+Enter
```

>     diamondsDColoredDF_FromTable: org.apache.spark.sql.DataFrame = [carat: double, color: string ... 1 more field]

``` scala
// or if you like use upper case for SQL then this is equivalent
val diamondsDColoredDF_FromTable = sqlContext.sql("SELECT carat, color, price FROM diamonds WHERE color='D'") // Shift+Enter
```

>     diamondsDColoredDF_FromTable: org.apache.spark.sql.DataFrame = [carat: double, color: string ... 1 more field]

``` scala
// from version 2.x onwards you can call from SparkSession, the pre-made spark in spark-shell or databricks notebook
val diamondsDColoredDF_FromTable = spark.sql("SELECT carat, color, price FROM diamonds WHERE color='D'") // Shift+Enter
```

>     diamondsDColoredDF_FromTable: org.apache.spark.sql.DataFrame = [carat: double, color: string ... 1 more field]

``` scala
display(diamondsDColoredDF_FromTable) // Ctrl+Enter to see the same DF!
```

| carat | color | price  |
|-------|-------|--------|
| 0.23  | D     | 357.0  |
| 0.23  | D     | 402.0  |
| 0.26  | D     | 403.0  |
| 0.26  | D     | 403.0  |
| 0.26  | D     | 403.0  |
| 0.22  | D     | 404.0  |
| 0.3   | D     | 552.0  |
| 0.3   | D     | 552.0  |
| 0.3   | D     | 552.0  |
| 0.24  | D     | 553.0  |
| 0.26  | D     | 554.0  |
| 0.26  | D     | 554.0  |
| 0.26  | D     | 554.0  |
| 0.75  | D     | 2760.0 |
| 0.71  | D     | 2762.0 |
| 0.61  | D     | 2763.0 |
| 0.71  | D     | 2764.0 |
| 0.71  | D     | 2764.0 |
| 0.7   | D     | 2767.0 |
| 0.71  | D     | 2767.0 |
| 0.73  | D     | 2768.0 |
| 0.7   | D     | 2768.0 |
| 0.71  | D     | 2768.0 |
| 0.71  | D     | 2770.0 |
| 0.76  | D     | 2770.0 |
| 0.73  | D     | 2770.0 |
| 0.75  | D     | 2773.0 |
| 0.7   | D     | 2773.0 |
| 0.7   | D     | 2777.0 |
| 0.53  | D     | 2782.0 |

Truncated to 30 rows

``` scala
// You can also use the familiar wildchard character '%' when matching Strings
display(spark.sql("SELECT * FROM diamonds WHERE clarity LIKE 'V%'"))
```

| carat | cut       | color | clarity | depth | table | price | x    | y    | z    |
|-------|-----------|-------|---------|-------|-------|-------|------|------|------|
| 0.23  | Good      | E     | VS1     | 56.9  | 65.0  | 327.0 | 4.05 | 4.07 | 2.31 |
| 0.29  | Premium   | I     | VS2     | 62.4  | 58.0  | 334.0 | 4.2  | 4.23 | 2.63 |
| 0.24  | Very Good | J     | VVS2    | 62.8  | 57.0  | 336.0 | 3.94 | 3.96 | 2.48 |
| 0.24  | Very Good | I     | VVS1    | 62.3  | 57.0  | 336.0 | 3.95 | 3.98 | 2.47 |
| 0.22  | Fair      | E     | VS2     | 65.1  | 61.0  | 337.0 | 3.87 | 3.78 | 2.49 |
| 0.23  | Very Good | H     | VS1     | 59.4  | 61.0  | 338.0 | 4.0  | 4.05 | 2.39 |
| 0.23  | Ideal     | J     | VS1     | 62.8  | 56.0  | 340.0 | 3.93 | 3.9  | 2.46 |
| 0.23  | Very Good | E     | VS2     | 63.8  | 55.0  | 352.0 | 3.85 | 3.92 | 2.48 |
| 0.23  | Very Good | H     | VS1     | 61.0  | 57.0  | 353.0 | 3.94 | 3.96 | 2.41 |
| 0.23  | Very Good | G     | VVS2    | 60.4  | 58.0  | 354.0 | 3.97 | 4.01 | 2.41 |
| 0.24  | Premium   | I     | VS1     | 62.5  | 57.0  | 355.0 | 3.97 | 3.94 | 2.47 |
| 0.3   | Very Good | J     | VS2     | 62.2  | 57.0  | 357.0 | 4.28 | 4.3  | 2.67 |
| 0.23  | Very Good | D     | VS2     | 60.5  | 61.0  | 357.0 | 3.96 | 3.97 | 2.4  |
| 0.23  | Very Good | F     | VS1     | 60.9  | 57.0  | 357.0 | 3.96 | 3.99 | 2.42 |
| 0.23  | Very Good | F     | VS1     | 60.0  | 57.0  | 402.0 | 4.0  | 4.03 | 2.41 |
| 0.23  | Very Good | F     | VS1     | 59.8  | 57.0  | 402.0 | 4.04 | 4.06 | 2.42 |
| 0.23  | Very Good | E     | VS1     | 60.7  | 59.0  | 402.0 | 3.97 | 4.01 | 2.42 |
| 0.23  | Very Good | E     | VS1     | 59.5  | 58.0  | 402.0 | 4.01 | 4.06 | 2.4  |
| 0.23  | Very Good | D     | VS1     | 61.9  | 58.0  | 402.0 | 3.92 | 3.96 | 2.44 |
| 0.23  | Good      | F     | VS1     | 58.2  | 59.0  | 402.0 | 4.06 | 4.08 | 2.37 |
| 0.23  | Good      | E     | VS1     | 64.1  | 59.0  | 402.0 | 3.83 | 3.85 | 2.46 |
| 0.26  | Very Good | D     | VS2     | 60.8  | 59.0  | 403.0 | 4.13 | 4.16 | 2.52 |
| 0.26  | Good      | D     | VS2     | 65.2  | 56.0  | 403.0 | 3.99 | 4.02 | 2.61 |
| 0.26  | Good      | D     | VS1     | 58.4  | 63.0  | 403.0 | 4.19 | 4.24 | 2.46 |
| 0.25  | Very Good | E     | VS2     | 63.3  | 60.0  | 404.0 | 4.0  | 4.03 | 2.54 |
| 0.23  | Ideal     | G     | VS1     | 61.9  | 54.0  | 404.0 | 3.93 | 3.95 | 2.44 |
| 0.22  | Premium   | E     | VS2     | 61.6  | 58.0  | 404.0 | 3.93 | 3.89 | 2.41 |
| 0.22  | Premium   | D     | VS2     | 59.3  | 62.0  | 404.0 | 3.91 | 3.88 | 2.31 |
| 0.35  | Ideal     | I     | VS1     | 60.9  | 57.0  | 552.0 | 4.54 | 4.59 | 2.78 |
| 0.28  | Ideal     | G     | VVS2    | 61.4  | 56.0  | 553.0 | 4.19 | 4.22 | 2.58 |

Truncated to 30 rows

``` scala
// Combining conditions
display(spark.sql("SELECT * FROM diamonds WHERE clarity LIKE 'V%' AND price > 10000"))
```

| carat | cut       | color | clarity | depth | table | price   | x    | y    | z    |
|-------|-----------|-------|---------|-------|-------|---------|------|------|------|
| 1.7   | Ideal     | J     | VS2     | 60.5  | 58.0  | 10002.0 | 7.73 | 7.74 | 4.68 |
| 1.03  | Ideal     | E     | VVS2    | 60.6  | 59.0  | 10003.0 | 6.5  | 6.53 | 3.95 |
| 1.23  | Very Good | G     | VVS2    | 60.6  | 55.0  | 10004.0 | 6.93 | 7.02 | 4.23 |
| 1.25  | Ideal     | F     | VS2     | 61.6  | 55.0  | 10006.0 | 6.93 | 6.96 | 4.28 |
| 1.21  | Very Good | F     | VS1     | 62.3  | 58.0  | 10009.0 | 6.76 | 6.85 | 4.24 |
| 1.51  | Premium   | I     | VS2     | 59.9  | 60.0  | 10010.0 | 7.42 | 7.36 | 4.43 |
| 1.05  | Ideal     | F     | VVS2    | 60.5  | 55.0  | 10011.0 | 6.67 | 6.58 | 4.01 |
| 1.6   | Ideal     | J     | VS1     | 62.0  | 53.0  | 10011.0 | 7.57 | 7.56 | 4.69 |
| 1.35  | Premium   | G     | VS1     | 62.1  | 59.0  | 10012.0 | 7.06 | 7.02 | 4.37 |
| 1.53  | Premium   | I     | VS2     | 62.0  | 58.0  | 10013.0 | 7.36 | 7.41 | 4.58 |
| 1.13  | Ideal     | F     | VS1     | 60.9  | 57.0  | 10016.0 | 6.73 | 6.76 | 4.11 |
| 1.21  | Premium   | F     | VS1     | 62.6  | 59.0  | 10018.0 | 6.81 | 6.76 | 4.25 |
| 1.01  | Very Good | F     | VVS1    | 62.9  | 57.0  | 10019.0 | 6.35 | 6.41 | 4.01 |
| 1.04  | Ideal     | E     | VVS2    | 62.9  | 55.0  | 10019.0 | 6.47 | 6.51 | 4.08 |
| 1.26  | Very Good | G     | VVS2    | 60.9  | 56.0  | 10020.0 | 6.95 | 7.01 | 4.25 |
| 1.5   | Very Good | H     | VS2     | 60.9  | 59.0  | 10023.0 | 7.37 | 7.43 | 4.51 |
| 1.12  | Premium   | F     | VVS2    | 62.4  | 59.0  | 10028.0 | 6.58 | 6.66 | 4.13 |
| 1.27  | Premium   | F     | VS1     | 60.3  | 58.0  | 10028.0 | 7.06 | 7.04 | 4.25 |
| 1.52  | Very Good | I     | VS1     | 62.9  | 59.9  | 10032.0 | 7.27 | 7.31 | 4.59 |
| 1.24  | Premium   | F     | VS1     | 62.5  | 58.0  | 10033.0 | 6.87 | 6.83 | 4.28 |
| 1.23  | Very Good | F     | VS1     | 62.0  | 59.0  | 10035.0 | 6.84 | 6.87 | 4.25 |
| 1.5   | Good      | G     | VS1     | 63.6  | 57.0  | 10036.0 | 7.23 | 7.14 | 4.57 |
| 1.22  | Ideal     | G     | VVS2    | 62.3  | 56.0  | 10038.0 | 6.81 | 6.84 | 4.25 |
| 1.3   | Ideal     | G     | VS1     | 62.0  | 55.0  | 10038.0 | 6.98 | 7.02 | 4.34 |
| 1.59  | Premium   | I     | VS2     | 60.2  | 60.0  | 10039.0 | 7.58 | 7.61 | 4.57 |
| 1.83  | Premium   | I     | VS2     | 60.5  | 60.0  | 10043.0 | 7.93 | 7.86 | 4.78 |
| 1.07  | Ideal     | E     | VVS2    | 61.4  | 56.0  | 10043.0 | 6.65 | 6.55 | 4.05 |
| 1.51  | Very Good | H     | VS1     | 61.5  | 54.0  | 10045.0 | 7.34 | 7.42 | 4.54 |
| 1.08  | Ideal     | F     | VVS2    | 61.6  | 57.0  | 10046.0 | 6.57 | 6.6  | 4.06 |
| 1.0   | Premium   | D     | VVS2    | 61.6  | 60.0  | 10046.0 | 6.41 | 6.36 | 3.93 |

Truncated to 30 rows

``` scala
// selecting a subset of fields
display(spark.sql("SELECT carat, clarity, price FROM diamonds WHERE color = 'D'"))
```

| carat | clarity | price  |
|-------|---------|--------|
| 0.23  | VS2     | 357.0  |
| 0.23  | VS1     | 402.0  |
| 0.26  | VS2     | 403.0  |
| 0.26  | VS2     | 403.0  |
| 0.26  | VS1     | 403.0  |
| 0.22  | VS2     | 404.0  |
| 0.3   | SI1     | 552.0  |
| 0.3   | SI1     | 552.0  |
| 0.3   | SI1     | 552.0  |
| 0.24  | VVS1    | 553.0  |
| 0.26  | VVS2    | 554.0  |
| 0.26  | VVS2    | 554.0  |
| 0.26  | VVS1    | 554.0  |
| 0.75  | SI1     | 2760.0 |
| 0.71  | SI2     | 2762.0 |
| 0.61  | VVS2    | 2763.0 |
| 0.71  | SI1     | 2764.0 |
| 0.71  | SI1     | 2764.0 |
| 0.7   | VS2     | 2767.0 |
| 0.71  | SI2     | 2767.0 |
| 0.73  | SI1     | 2768.0 |
| 0.7   | SI1     | 2768.0 |
| 0.71  | SI2     | 2768.0 |
| 0.71  | VS2     | 2770.0 |
| 0.76  | SI2     | 2770.0 |
| 0.73  | SI2     | 2770.0 |
| 0.75  | SI2     | 2773.0 |
| 0.7   | VS2     | 2773.0 |
| 0.7   | VS1     | 2777.0 |
| 0.53  | VVS2    | 2782.0 |

Truncated to 30 rows

``` scala
//renaming a field using as
display(spark.sql("SELECT carat AS carrot, clarity, price FROM diamonds"))
```

| carrot | clarity | price |
|--------|---------|-------|
| 0.23   | SI2     | 326.0 |
| 0.21   | SI1     | 326.0 |
| 0.23   | VS1     | 327.0 |
| 0.29   | VS2     | 334.0 |
| 0.31   | SI2     | 335.0 |
| 0.24   | VVS2    | 336.0 |
| 0.24   | VVS1    | 336.0 |
| 0.26   | SI1     | 337.0 |
| 0.22   | VS2     | 337.0 |
| 0.23   | VS1     | 338.0 |
| 0.3    | SI1     | 339.0 |
| 0.23   | VS1     | 340.0 |
| 0.22   | SI1     | 342.0 |
| 0.31   | SI2     | 344.0 |
| 0.2    | SI2     | 345.0 |
| 0.32   | I1      | 345.0 |
| 0.3    | SI2     | 348.0 |
| 0.3    | SI1     | 351.0 |
| 0.3    | SI1     | 351.0 |
| 0.3    | SI1     | 351.0 |
| 0.3    | SI2     | 351.0 |
| 0.23   | VS2     | 352.0 |
| 0.23   | VS1     | 353.0 |
| 0.31   | SI1     | 353.0 |
| 0.31   | SI1     | 353.0 |
| 0.23   | VVS2    | 354.0 |
| 0.24   | VS1     | 355.0 |
| 0.3    | VS2     | 357.0 |
| 0.23   | VS2     | 357.0 |
| 0.23   | VS1     | 357.0 |

Truncated to 30 rows

``` scala
//sorting
display(spark.sql("SELECT carat, clarity, price FROM diamonds ORDER BY price DESC"))
```

| carat | clarity | price   |
|-------|---------|---------|
| 2.29  | VS2     | 18823.0 |
| 2.0   | SI1     | 18818.0 |
| 1.51  | IF      | 18806.0 |
| 2.07  | SI2     | 18804.0 |
| 2.0   | SI1     | 18803.0 |
| 2.29  | SI1     | 18797.0 |
| 2.0   | VS1     | 18795.0 |
| 2.04  | SI1     | 18795.0 |
| 1.71  | VS2     | 18791.0 |
| 2.15  | SI2     | 18791.0 |
| 2.8   | SI2     | 18788.0 |
| 2.05  | SI1     | 18787.0 |
| 2.05  | SI2     | 18784.0 |
| 2.03  | SI1     | 18781.0 |
| 1.6   | VS1     | 18780.0 |
| 2.06  | VS2     | 18779.0 |
| 1.51  | VVS1    | 18777.0 |
| 1.71  | VVS2    | 18768.0 |
| 2.55  | VS1     | 18766.0 |
| 2.08  | SI1     | 18760.0 |
| 2.0   | SI1     | 18759.0 |
| 2.03  | SI1     | 18757.0 |
| 2.61  | SI2     | 18756.0 |
| 2.36  | SI2     | 18745.0 |
| 2.01  | SI1     | 18741.0 |
| 2.01  | SI1     | 18741.0 |
| 2.01  | SI1     | 18741.0 |
| 2.01  | SI1     | 18736.0 |
| 1.94  | SI1     | 18735.0 |
| 2.02  | SI1     | 18731.0 |

Truncated to 30 rows

``` scala
diamondsDF.printSchema // since price is double in the DF that was turned into table we can rely on the descenting sort on doubles
```

>     root
>      |-- carat: double (nullable = true)
>      |-- cut: string (nullable = true)
>      |-- color: string (nullable = true)
>      |-- clarity: string (nullable = true)
>      |-- depth: double (nullable = true)
>      |-- table: double (nullable = true)
>      |-- price: double (nullable = true)
>      |-- x: double (nullable = true)
>      |-- y: double (nullable = true)
>      |-- z: double (nullable = true)

``` scala
// sort by multiple fields
display(spark.sql("SELECT carat, clarity, price FROM diamonds ORDER BY carat ASC, price DESC"))
```

| carat | clarity | price |
|-------|---------|-------|
| 0.2   | VS2     | 367.0 |
| 0.2   | VS2     | 367.0 |
| 0.2   | VS2     | 367.0 |
| 0.2   | VS2     | 367.0 |
| 0.2   | VS2     | 367.0 |
| 0.2   | VS2     | 367.0 |
| 0.2   | VS2     | 367.0 |
| 0.2   | VS2     | 367.0 |
| 0.2   | VS2     | 367.0 |
| 0.2   | VS2     | 367.0 |
| 0.2   | VS2     | 367.0 |
| 0.2   | SI2     | 345.0 |
| 0.21  | SI2     | 394.0 |
| 0.21  | VS2     | 386.0 |
| 0.21  | VS2     | 386.0 |
| 0.21  | VS2     | 386.0 |
| 0.21  | VS2     | 386.0 |
| 0.21  | VS2     | 386.0 |
| 0.21  | VS2     | 386.0 |
| 0.21  | VS2     | 386.0 |
| 0.21  | SI1     | 326.0 |
| 0.22  | SI1     | 470.0 |
| 0.22  | VS2     | 404.0 |
| 0.22  | VS2     | 404.0 |
| 0.22  | SI1     | 342.0 |
| 0.22  | VS2     | 337.0 |
| 0.23  | VVS2    | 688.0 |
| 0.23  | VVS1    | 682.0 |
| 0.23  | VVS1    | 680.0 |
| 0.23  | VVS1    | 680.0 |

Truncated to 30 rows

``` scala
// use this to type cast strings into Int when the table is loaded with string-valued columns
//display(spark.sql("select cast(carat as Int) as carat, clarity, cast(price as Int) as price from diamond order by carat asc, price desc"))
```

``` scala
// sort by multiple fields and limit to first 5
// I prefer lowercase for SQL - and you can use either in this course - but in the field do what your Boss or your colleagues prefer :)
display(spark.sql("select carat, clarity, price from diamonds order by carat desc, price desc limit 5"))
```

| carat | clarity | price   |
|-------|---------|---------|
| 5.01  | I1      | 18018.0 |
| 4.5   | I1      | 18531.0 |
| 4.13  | I1      | 17329.0 |
| 4.01  | I1      | 15223.0 |
| 4.01  | I1      | 15223.0 |

``` scala
//aggregate functions
display(spark.sql("select avg(price) as avgprice from diamonds"))
```

| avgprice          |
|-------------------|
| 3932.799721913237 |

``` scala
//average operator is doing an auto-type conversion from int to double
display(spark.sql("select avg(cast(price as Integer)) as avgprice from diamonds"))
```

| avgprice          |
|-------------------|
| 3932.799721913237 |

``` scala
//aggregate function and grouping
display(spark.sql("select color, avg(price) as avgprice from diamonds group by color"))
```

| color | avgprice           |
|-------|--------------------|
| F     | 3724.886396981765  |
| E     | 3076.7524752475247 |
| D     | 3169.9540959409596 |
| J     | 5323.81801994302   |
| G     | 3999.135671271697  |
| I     | 5091.874953891553  |
| H     | 4486.669195568401  |

### Why do we need to know these interactive SQL queries?

Such queries can help us explore the data and thereby inform the modeling process!!!

Of course, if you don't know SQL then don't worry, we will be doing these things in scala using DataFrames.

Finally, those who are planning to take the Spark Developer Exams online, then you can't escape from SQL questions there...

#### Further Preparation

For more on SQL syntax, check the SQL tutorial on [W3Schools](http://www.w3schools.com/sql/default.asp)
Note that [HiveQL](https://cwiki.apache.org/confluence/display/Hive/LanguageManual) supports only a subset of operations supported by SQL

See databricks guide on [tables](https://docs.databricks.com/user-guide/tables.html) **NOW-ish**.