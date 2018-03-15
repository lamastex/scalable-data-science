[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

This is an elaboration of the <http://spark.apache.org/docs/latest/sql-programming-guide.html> by Ivan Sadikov and Raazesh Sainudiin.

Getting Started
===============

Spark Sql Programming Guide
---------------------------

-   Getting Started
    -   Starting Point: SQLContext
    -   Creating DataFrames
    -   DataFrame Operations
    -   Running SQL Queries Programmatically
    -   Creating Datasets
    -   Interoperating with RDDs
        -   Inferring the Schema Using Reflection
        -   Programmatically Specifying the Schema

Getting Started
===============

Starting Point: SQLContext
--------------------------

The entry point into all functionality in Spark SQL is the
[`SparkSession`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.SparkSession) class and/or `SQLContext`/`HiveContext`. Spark session is created for you as `spark` when you start **spark-shell** or **pyspark**. You will need to create `SparkSession` usually when building an application (running on production-like on-premises cluster). n this case follow code below to create Spark session.

> import org.apache.spark.sql.SparkSession
>
> val spark = SparkSession.builder().appName("Spark SQL basic example").getOrCreate()
>
> // you could get SparkContext and SQLContext from SparkSession
> val sc = spark.sparkContext
> val sqlContext = spark.sqlContext
>
> // This is used to implicitly convert an RDD or Seq to a DataFrame (see examples below)
> import spark.implicits.\_

But in Databricks notebook (similar to `spark-shell`) `SparkSession` is already created for you and is available as `spark`.

``` scala
// Evaluation of the cell by Ctrl+Enter will print spark session available in notebook
spark
```

>     res0: org.apache.spark.sql.SparkSession = org.apache.spark.sql.SparkSession@2d0c6c9

After evaluation you should see something like this:
`res0: org.apache.spark.sql.SparkSession = org.apache.spark.sql.SparkSession@2d0c6c9`
In order to enable Hive support use `enableHiveSupport()` method on builder when constructing Spark session, which provides richer functionality over standard Spark SQL context, for example, usage of Hive user-defined functions or loading and writing data from/into Hive. Note that most of the SQL functionality is available regardless Hive support.

Creating DataFrames
-------------------

With a `SparkSessions`, applications can create [Dataset](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.Dataset) or [`DataFrame`](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.DataFrame) from an [existing `RDD`](sql-programming-guide.html#interoperating-with-rdds), from a Hive table, or from various [datasources](sql-programming-guide.html#data-sources).

Just to recap, a DataFrame is a distributed collection of data organized into named columns. You can think of it as an organized into table RDD of case class `Row` (which is not exactly true). DataFrames, in comparison to RDDs, are backed by rich optimizations, including tracking their own schema, adaptive query execution, code generation including whole stage codegen, extensible Catalyst optimizer, and project [Tungsten](https://databricks.com/blog/2015/04/28/project-tungsten-bringing-spark-closer-to-bare-metal.html).

Dataset provides type-safety when working with SQL, since `Row` is mapped to a case class, so that each column can be referenced by property of that class.

> Note that performance for Dataset/DataFrames is the same across languages Scala, Java, Python, and R. This is due to the fact that the planning phase is just language-specific, only logical plan is constructed in Python, and all the physical execution is compiled and executed as JVM bytecode.

``` scala
// Spark has some of the pre-built methods to create simple Dataset/DataFrame

// 1. Empty Dataset/DataFrame, not really interesting, is it?
println(spark.emptyDataFrame)
println(spark.emptyDataset[Int])
```

>     []
>     [value: int]

``` scala
// 2. Range of numbers, note that Spark automatically names column as "id"
val range = spark.range(0, 10)

// In order to get a preview of data in DataFrame use "show()"
range.show(3)
```

>     +---+
>     | id|
>     +---+
>     |  0|
>     |  1|
>     |  2|
>     +---+
>     only showing top 3 rows
>
>     range: org.apache.spark.sql.Dataset[Long] = [id: bigint]

You can also use different datasources that will be shown later or load Hive tables directly into Spark.

We have already created a table of social media usage from NYC

> See the **Appendix** section below to create this `social_media_usage` table from raw data.

First let's make sure this table is available for us.

``` scala
// Let's find out what tables are already available for loading
spark.catalog.listTables.show()
```

>     +--------------------+--------+-----------+---------+-----------+
>     |                name|database|description|tableType|isTemporary|
>     +--------------------+--------+-----------+---------+-----------+
>     |          cities_csv| default|       null| EXTERNAL|      false|
>     |       cleaned_taxes| default|       null|  MANAGED|      false|
>     |commdettrumpclint...| default|       null|  MANAGED|      false|
>     |   donaldtrumptweets| default|       null| EXTERNAL|      false|
>     |             linkage| default|       null| EXTERNAL|      false|
>     |             nations| default|       null| EXTERNAL|      false|
>     |           newmplist| default|       null| EXTERNAL|      false|
>     |       ny_baby_names| default|       null|  MANAGED|      false|
>     |       nzmpsandparty| default|       null| EXTERNAL|      false|
>     |    pos_neg_category| default|       null| EXTERNAL|      false|
>     |                 rna| default|       null|  MANAGED|      false|
>     |                samh| default|       null| EXTERNAL|      false|
>     |  social_media_usage| default|       null| EXTERNAL|      false|
>     |              table1| default|       null| EXTERNAL|      false|
>     |          test_table| default|       null| EXTERNAL|      false|
>     |             uscites| default|       null| EXTERNAL|      false|
>     +--------------------+--------+-----------+---------+-----------+

It looks like the table `social_media_usage` is available as a permanent table (`isTemporary` set as `false`).

Next let us do the following:

-   load this table as a DataFrame
-   print its schema and
-   show the first 20 rows.

``` scala
val df = spark.table("social_media_usage") // Ctrl+Enter
```

>     df: org.apache.spark.sql.DataFrame = [agency: string, platform: string ... 3 more fields]

As you can see the immutable value `df` is a DataFrame and more specifically it is:

> `org.apache.spark.sql.DataFrame = [agency: string, platform: string, url: string, visits: int]`.

Now let us print schema of the DataFrame `df` and have a look at the actual data:

``` scala
// Ctrl+Enter
df.printSchema() // prints schema of the DataFrame
df.show() // shows first n (default is 20) rows
```

>     root
>      |-- agency: string (nullable = true)
>      |-- platform: string (nullable = true)
>      |-- url: string (nullable = true)
>      |-- date: string (nullable = true)
>      |-- visits: integer (nullable = true)
>
>     +----------+----------+--------------------+--------------------+------+
>     |    agency|  platform|                 url|                date|visits|
>     +----------+----------+--------------------+--------------------+------+
>     |       OEM|       SMS|                null|02/17/2012 12:00:...| 61652|
>     |       OEM|       SMS|                null|11/09/2012 12:00:...| 44547|
>     |       EDC|    Flickr|http://www.flickr...|05/09/2012 12:00:...|  null|
>     |     NYCHA|Newsletter|                null|05/09/2012 12:00:...|  null|
>     |       DHS|   Twitter|www.twitter.com/n...|06/13/2012 12:00:...|   389|
>     |       DHS|   Twitter|www.twitter.com/n...|08/02/2012 12:00:...|   431|
>     |       DOH|   Android|       Condom Finder|08/08/2011 12:00:...|  5026|
>     |       DOT|   Android|         You The Man|08/08/2011 12:00:...|  null|
>     |      MOME|   Android|      MiNY Venor app|08/08/2011 12:00:...|   313|
>     |       DOT|Broadcastr|                null|08/08/2011 12:00:...|  null|
>     |       DPR|Broadcastr|http://beta.broad...|08/08/2011 12:00:...|  null|
>     |     ENDHT|  Facebook|http://www.facebo...|08/08/2011 12:00:...|     3|
>     |       VAC|  Facebook|https://www.faceb...|08/08/2011 12:00:...|    36|
>     |    PlaNYC|  Facebook|http://www.facebo...|08/08/2011 12:00:...|    47|
>     |      DFTA|  Facebook|http://www.facebo...|08/08/2011 12:00:...|    90|
>     | energyNYC|  Facebook|http://www.facebo...|08/08/2011 12:00:...|   105|
>     |      MOIA|  Facebook|http://www.facebo...|08/08/2011 12:00:...|   123|
>     |City Store|  Facebook|http://www.facebo...|08/08/2011 12:00:...|   119|
>     |      OCDV|  Facebook|http://www.facebo...|08/08/2011 12:00:...|   148|
>     |       HIA|  Facebook|http://www.facebo...|08/08/2011 12:00:...|   197|
>     +----------+----------+--------------------+--------------------+------+
>     only showing top 20 rows

> Note that `(nullable = true)` simply means if the value is allowed to be `null`.

Let us count the number of rows in `df`.

``` scala
df.count() // Ctrl+Enter
```

>     res7: Long = 5899

So there are 5899 records or rows in the DataFrame `df`. Pretty good! You can also select individual columns using so-called DataFrame API, as follows:

``` scala
val platforms = df.select("platform") // Shift+Enter
```

>     platforms: org.apache.spark.sql.DataFrame = [platform: string]

``` scala
platforms.count() // Shift+Enter to count the number of rows
```

>     res8: Long = 5899

``` scala
platforms.show(5) // Ctrl+Enter to show top 5 rows
```

>     +----------+
>     |  platform|
>     +----------+
>     |       SMS|
>     |       SMS|
>     |    Flickr|
>     |Newsletter|
>     |   Twitter|
>     +----------+
>     only showing top 5 rows

You can also apply `.distinct()` to extract only unique entries as follows:

``` scala
val uniquePlatforms = df.select("platform").distinct() // Shift+Enter
```

>     uniquePlatforms: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [platform: string]

``` scala
uniquePlatforms.count() // Ctrl+Enter to count the number of distinct platforms
```

>     res10: Long = 23

Let's see all the rows of the DataFrame `uniquePlatforms`.

> Note that `display(uniquePlatforms)` unlike `uniquePlatforms.show()` displays all rows of the DataFrame + gives you ability to select different view, e.g. charts.

``` scala
display(uniquePlatforms) // Ctrl+Enter to show all rows; use the scroll-bar on the right of the display to see all platforms
```

| platform                  |
|---------------------------|
| nyc.gov                   |
| Flickr                    |
| Vimeo                     |
| iPhone                    |
| YouTube                   |
| WordPress                 |
| SMS                       |
| iPhone App                |
| Youtube                   |
| Instagram                 |
| iPhone app                |
| Linked-In                 |
| Twitter                   |
| TOTAL                     |
| Tumblr                    |
| Newsletter                |
| Pinterest                 |
| Broadcastr                |
| Android                   |
| Foursquare                |
| Google+                   |
| Foursquare (Badge Unlock) |
| Facebook                  |

### Spark SQL and DataFrame API

Spark SQL provides DataFrame API that can perform relational operations on both external data sources and internal collections, which is similar to widely used data frame concept in R, but evaluates operations support lazily (remember RDDs?), so that it can perform relational optimizations. This API is also available in Java, Python and R, but some functionality may not be available, although with every release of Spark people minimize this gap.

So we give some examples how to query data in Python and R, but continue with Scala. You can do all DataFrame operations in this notebook using Python or R.

``` python
# Ctrl+Enter to evaluate this python cell, recall '#' is the pre-comment character in python
# Using Python to query our "social_media_usage" table
pythonDF = spark.table("social_media_usage").select("platform").distinct()
pythonDF.show(3)
```

>     +--------+
>     |platform|
>     +--------+
>     | nyc.gov|
>     |  Flickr|
>     |   Vimeo|
>     +--------+
>     only showing top 3 rows

``` sql
-- Ctrl+Enter to achieve the same result using standard SQL syntax!
select distinct platform from social_media_usage
```

| platform                  |
|---------------------------|
| nyc.gov                   |
| Flickr                    |
| Vimeo                     |
| iPhone                    |
| YouTube                   |
| WordPress                 |
| SMS                       |
| iPhone App                |
| Youtube                   |
| Instagram                 |
| iPhone app                |
| Linked-In                 |
| Twitter                   |
| TOTAL                     |
| Tumblr                    |
| Newsletter                |
| Pinterest                 |
| Broadcastr                |
| Android                   |
| Foursquare                |
| Google+                   |
| Foursquare (Badge Unlock) |
| Facebook                  |

Now it is time for some tips around how you use `select` and what the difference is between `$"a"`, `col("a")`, `df("a")`.

As you probably have noticed by now, you can specify individual columns to select by providing String values in select statement. But sometimes you need to:
- distinguish between columns with the same name
- use it to filter (actually you can still filter using full String expression)
- do some "magic" with joins and user-defined functions (this will be shown later)

So Spark gives you ability to actually specify columns when you select. Now the difference between all those three notations is ... none, those things are just aliases for a `Column` in Spark SQL, which means following expressions yield the same result:

\`\`\`
// Using string expressions
df.select("agency", "visits")

// Using "$" alias for column
df.select($"agency", $"visits")

// Using "col" alias for column
df.select(col("agency"), col("visits"))

// Using DataFrame name for column
df.select(df("agency"), df("visits"))
\`\`\`

This "same-difference" applies to filtering, i.e. you can either use full expression to filter, or column as shown in the following example:

\`\`\`
// Using column to filter
df.select("visits").filter($"visits" &gt; 100)

// Or you can use full expression as string
df.select("visits").filter("visits &gt; 100")
\`\`\`

> Note that `$"visits" > 100` expression looks amazing, but under the hood it is just another column, and it equals to `df("visits").>(100)`, where, thanks to Scala paradigm `>` is just another function that you can define.

``` scala
val sms = df.select($"agency", $"platform", $"visits").filter($"platform" === "SMS")
sms.show() // Ctrl+Enter
```

>     +------+--------+------+
>     |agency|platform|visits|
>     +------+--------+------+
>     |   OEM|     SMS| 61652|
>     |   OEM|     SMS| 44547|
>     |   DOE|     SMS|   382|
>     | NYCHA|     SMS|  null|
>     |   OEM|     SMS| 61652|
>     |   DOE|     SMS|   382|
>     | NYCHA|     SMS|  null|
>     |   OEM|     SMS| 61652|
>     |   OEM|     SMS|  null|
>     |   DOE|     SMS|  null|
>     | NYCHA|     SMS|  null|
>     |   OEM|     SMS|  null|
>     |   DOE|     SMS|  null|
>     | NYCHA|     SMS|  null|
>     |   DOE|     SMS|   382|
>     | NYCHA|     SMS|  null|
>     |   OEM|     SMS| 61652|
>     |   DOE|     SMS|   382|
>     | NYCHA|     SMS|  null|
>     |   OEM|     SMS| 61652|
>     +------+--------+------+
>     only showing top 20 rows
>
>     sms: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [agency: string, platform: string ... 1 more field]

Again you could have written the query above using any column aliases or String names or even writing the query directly.

For example, we can do it using String names, as follows:

``` scala
// Ctrl+Enter Note that we are using "platform = 'SMS'" since it will be evaluated as actual SQL
val sms = df.select(df("agency"), df("platform"), df("visits")).filter("platform = 'SMS'")
sms.show(5)
```

>     +------+--------+------+
>     |agency|platform|visits|
>     +------+--------+------+
>     |   OEM|     SMS| 61652|
>     |   OEM|     SMS| 44547|
>     |   DOE|     SMS|   382|
>     | NYCHA|     SMS|  null|
>     |   OEM|     SMS| 61652|
>     +------+--------+------+
>     only showing top 5 rows
>
>     sms: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [agency: string, platform: string ... 1 more field]

Refer to the [DataFrame API](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.DataFrame) for more detailed API. In addition to simple column references and expressions, DataFrames also have a rich library of functions including string manipulation, date
arithmetic, common math operations and more. The complete list is available in the [DataFrame Function Reference](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.functions$).

Let's next explore some of the functionality that is available by transforming this DataFrame `df` into a new DataFrame called `fixedDF`.

-   First, note that some columns are not exactly what we want them to be.
    -   For example date column should be standard Date/Timestamp SQL column, and
    -   visits should not contain null values, but `0`s instead.
-   Let us fix it using some code that is briefly explained here (don't worry if you don't get it completely now, you will get the hang of it by playing more)
    -   The `coalesce` function is similar to `if-else` statement, i.e., if first column in expression is `null`, then the value of the second column is used and so on.
    -   `lit` just means column of constant value (`lit`erally speaking!).
    -   the "funky" time conversion is essentially conversion from current format -&gt; unix timestamp as a number -&gt; Spark SQL Date format
    -   we also remove `TOTAL` value from `platform` column.

``` scala
// Ctrl+Enter to make fixedDF

// import the needed sql functions
import org.apache.spark.sql.functions.{coalesce, from_unixtime, lit, to_date, unix_timestamp}

// make the fixedDF DataFrame
val fixedDF = df.
   select(
     $"agency", 
     $"platform", 
     $"url", 
     to_date(from_unixtime(unix_timestamp($"date", "MM/dd/yyyy hh:mm:ss aaa"))).as("date"), 
     coalesce($"visits", lit(0)).as("visits")).
   filter($"platform" !== "TOTAL")

fixedDF.printSchema() // print its schema 
// and show the top 20 records fully
fixedDF.show(false) // the false argument does not truncate the rows, so you will not see something like this "anot..."
```

>     root
>      |-- agency: string (nullable = true)
>      |-- platform: string (nullable = true)
>      |-- url: string (nullable = true)
>      |-- date: date (nullable = true)
>      |-- visits: integer (nullable = false)
>
>     +----------+----------+---------------------------------------------------------------------------------------+----------+------+
>     |agency    |platform  |url                                                                                    |date      |visits|
>     +----------+----------+---------------------------------------------------------------------------------------+----------+------+
>     |OEM       |SMS       |null                                                                                   |2012-02-17|61652 |
>     |OEM       |SMS       |null                                                                                   |2012-11-09|44547 |
>     |EDC       |Flickr    |http://www.flickr.com/nycedc                                                           |2012-05-09|0     |
>     |NYCHA     |Newsletter|null                                                                                   |2012-05-09|0     |
>     |DHS       |Twitter   |www.twitter.com/nycdhs                                                                 |2012-06-13|389   |
>     |DHS       |Twitter   |www.twitter.com/nycdhs                                                                 |2012-08-02|431   |
>     |DOH       |Android   |Condom Finder                                                                          |2011-08-08|5026  |
>     |DOT       |Android   |You The Man                                                                            |2011-08-08|0     |
>     |MOME      |Android   |MiNY Venor app                                                                         |2011-08-08|313   |
>     |DOT       |Broadcastr|null                                                                                   |2011-08-08|0     |
>     |DPR       |Broadcastr|http://beta.broadcastr.com/Echo.html?audioId=670026-4001                               |2011-08-08|0     |
>     |ENDHT     |Facebook  |http://www.facebook.com/pages/NYC-Lets-End-Human-Trafficking/125730490795659?sk=wall   |2011-08-08|3     |
>     |VAC       |Facebook  |https://www.facebook.com/pages/NYC-Voter-Assistance-Commission/110226709012110         |2011-08-08|36    |
>     |PlaNYC    |Facebook  |http://www.facebook.com/pages/New-York-NY/PlaNYC/160454173971169?ref=ts                |2011-08-08|47    |
>     |DFTA      |Facebook  |http://www.facebook.com/pages/NYC-Department-for-the-Aging/109028655823590             |2011-08-08|90    |
>     |energyNYC |Facebook  |http://www.facebook.com/EnergyNYC?sk=wall                                              |2011-08-08|105   |
>     |MOIA      |Facebook  |http://www.facebook.com/ihwnyc                                                         |2011-08-08|123   |
>     |City Store|Facebook  |http://www.facebook.com/citystorenyc                                                   |2011-08-08|119   |
>     |OCDV      |Facebook  |http://www.facebook.com/pages/NYC-Healthy-Relationship-Training-Academy/134637829901065|2011-08-08|148   |
>     |HIA       |Facebook  |http://www.facebook.com/pages/New-York-City-Health-Insurance-Link/145920551598         |2011-08-08|197   |
>     +----------+----------+---------------------------------------------------------------------------------------+----------+------+
>     only showing top 20 rows
>
>     <console>:47: warning: method !== in class Column is deprecated: !== does not have the same precedence as ===, use =!= instead
>               filter($"platform" !== "TOTAL")
>                                  ^
>     import org.apache.spark.sql.functions.{coalesce, from_unixtime, lit, to_date, unix_timestamp}
>     fixedDF: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [agency: string, platform: string ... 3 more fields]

Okay, this is better, but `url`s are still inconsistent.

Let's fix this by writing our own UDF (user-defined function) to deal with special cases.

Note that if you **CAN USE Spark functions library**, do it. But for the sake of the example, custom UDF is shown below.

We take value of a column as String type and return the same String type, but ignore values that do not start with `http`.

``` scala
// Ctrl+Enter to evaluate this UDF which takes a input String called "value"
// and converts it into lower-case if it begins with http and otherwise leaves it as null, so we sort of remove non valid web-urls
val cleanUrl = udf((value: String) => if (value != null && value.startsWith("http")) value.toLowerCase() else null)
```

>     cleanUrl: org.apache.spark.sql.expressions.UserDefinedFunction = UserDefinedFunction(<function1>,StringType,Some(List(StringType)))

Let us apply our UDF on `fixedDF` to create a new DataFrame called `cleanedDF` as follows:

``` scala
// Ctrl+Enter
val cleanedDF = fixedDF.select($"agency", $"platform", cleanUrl($"url").as("url"), $"date", $"visits")
```

>     cleanedDF: org.apache.spark.sql.DataFrame = [agency: string, platform: string ... 3 more fields]

Now, let's check that it actually worked by seeing the first 5 rows of the `cleanedDF` whose `url` `isNull` and `isNotNull`, as follows:

``` scala
// Shift+Enter
cleanedDF.filter($"url".isNull).show(5)
```

>     +------+----------+----+----------+------+
>     |agency|  platform| url|      date|visits|
>     +------+----------+----+----------+------+
>     |   OEM|       SMS|null|2012-02-17| 61652|
>     |   OEM|       SMS|null|2012-11-09| 44547|
>     | NYCHA|Newsletter|null|2012-05-09|     0|
>     |   DHS|   Twitter|null|2012-06-13|   389|
>     |   DHS|   Twitter|null|2012-08-02|   431|
>     +------+----------+----+----------+------+
>     only showing top 5 rows

``` scala
// Ctrl+Enter
cleanedDF.filter($"url".isNotNull).show(5, false) // false in .show(5, false) shows rows untruncated
```

>     +------+----------+------------------------------------------------------------------------------------+----------+------+
>     |agency|platform  |url                                                                                 |date      |visits|
>     +------+----------+------------------------------------------------------------------------------------+----------+------+
>     |EDC   |Flickr    |http://www.flickr.com/nycedc                                                        |2012-05-09|0     |
>     |DPR   |Broadcastr|http://beta.broadcastr.com/echo.html?audioid=670026-4001                            |2011-08-08|0     |
>     |ENDHT |Facebook  |http://www.facebook.com/pages/nyc-lets-end-human-trafficking/125730490795659?sk=wall|2011-08-08|3     |
>     |VAC   |Facebook  |https://www.facebook.com/pages/nyc-voter-assistance-commission/110226709012110      |2011-08-08|36    |
>     |PlaNYC|Facebook  |http://www.facebook.com/pages/new-york-ny/planyc/160454173971169?ref=ts             |2011-08-08|47    |
>     +------+----------+------------------------------------------------------------------------------------+----------+------+
>     only showing top 5 rows

Now there is a suggestion from you manager's manager's manager that due to some perceived privacy concerns we want to replace `agency` with some unique identifier.

So we need to do the following:

-   create unique list of agencies with ids and
-   join them with main DataFrame.

Sounds easy, right? Let's do it.

``` scala
// Crtl+Enter
// Import Spark SQL function that will give us unique id across all the records in this DataFrame
import org.apache.spark.sql.functions.monotonically_increasing_id

// We append column as SQL function that creates unique ids across all records in DataFrames 
val agencies = cleanedDF.select(cleanedDF("agency"))
                        .distinct()
                        .withColumn("id", monotonically_increasing_id())
agencies.show(5)
```

>     +--------------------+-----------+
>     |              agency|         id|
>     +--------------------+-----------+
>     |              PlaNYC|34359738368|
>     |                 HIA|34359738369|
>     |NYC Digital: exte...|34359738370|
>     |           NYCGLOBAL|42949672960|
>     |              nycgov|68719476736|
>     +--------------------+-----------+
>     only showing top 5 rows
>
>     import org.apache.spark.sql.functions.monotonically_increasing_id
>     agencies: org.apache.spark.sql.DataFrame = [agency: string, id: bigint]

Those who want to understand left/right inner/outer joins can see the video lectures in Module 3 of Anthony Joseph's Introduction to Big data edX course.

``` scala
// Ctrl+Enter
// And join with the rest of the data, note how join condition is specified 
val anonym = cleanedDF.join(agencies, cleanedDF("agency") === agencies("agency"), "inner").select("id", "platform", "url", "date", "visits")

// We also cache DataFrame since it can be quite expensive to recompute join
anonym.cache()

// Display result
anonym.show(5)
```

>     +-------------+----------+--------------------+----------+------+
>     |           id|  platform|                 url|      date|visits|
>     +-------------+----------+--------------------+----------+------+
>     |1580547964928|       SMS|                null|2012-02-17| 61652|
>     |1580547964928|       SMS|                null|2012-11-09| 44547|
>     | 412316860416|    Flickr|http://www.flickr...|2012-05-09|     0|
>     |1649267441664|Newsletter|                null|2012-05-09|     0|
>     |1529008357376|   Twitter|                null|2012-06-13|   389|
>     +-------------+----------+--------------------+----------+------+
>     only showing top 5 rows
>
>     anonym: org.apache.spark.sql.DataFrame = [id: bigint, platform: string ... 3 more fields]

``` scala
spark.catalog.listTables().show() // look at the available tables
```

>     +--------------------+--------+-----------+---------+-----------+
>     |                name|database|description|tableType|isTemporary|
>     +--------------------+--------+-----------+---------+-----------+
>     |          cities_csv| default|       null| EXTERNAL|      false|
>     |       cleaned_taxes| default|       null|  MANAGED|      false|
>     |commdettrumpclint...| default|       null|  MANAGED|      false|
>     |   donaldtrumptweets| default|       null| EXTERNAL|      false|
>     |             linkage| default|       null| EXTERNAL|      false|
>     |             nations| default|       null| EXTERNAL|      false|
>     |           newmplist| default|       null| EXTERNAL|      false|
>     |       ny_baby_names| default|       null|  MANAGED|      false|
>     |       nzmpsandparty| default|       null| EXTERNAL|      false|
>     |    pos_neg_category| default|       null| EXTERNAL|      false|
>     |                 rna| default|       null|  MANAGED|      false|
>     |                samh| default|       null| EXTERNAL|      false|
>     |  social_media_usage| default|       null| EXTERNAL|      false|
>     |              table1| default|       null| EXTERNAL|      false|
>     |          test_table| default|       null| EXTERNAL|      false|
>     |             uscites| default|       null| EXTERNAL|      false|
>     +--------------------+--------+-----------+---------+-----------+

``` sql
-- to remove a TempTable if it exists already
drop table if exists anonym
```

``` scala
// Register table for Spark SQL, we also import "month" function 
import org.apache.spark.sql.functions.month

anonym.createOrReplaceTempView("anonym")
```

>     import org.apache.spark.sql.functions.month

``` sql
-- Interesting. Now let's do some aggregation. Display platform, month, visits
-- Date column allows us to extract month directly

select platform, month(date) as month, sum(visits) as visits from anonym group by platform, month(date)
```

| platform                  | month | visits    |
|---------------------------|-------|-----------|
| Instagram                 | 9.0   | 27891.0   |
| Linked-In                 | 10.0  | 60156.0   |
| Foursquare (Badge Unlock) | 6.0   | 0.0       |
| iPhone                    | 8.0   | 10336.0   |
| Instagram                 | 1.0   | 0.0       |
| Twitter                   | 9.0   | 819290.0  |
| Vimeo                     | 11.0  | 0.0       |
| Linked-In                 | 1.0   | 19007.0   |
| iPhone app                | 9.0   | 33348.0   |
| SMS                       | 10.0  | 54100.0   |
| YouTube                   | 2.0   | 4937.0    |
| Instagram                 | 11.0  | 58968.0   |
| YouTube                   | 3.0   | 6066.0    |
| iPhone                    | 2.0   | 0.0       |
| Newsletter                | 11.0  | 3079091.0 |
| Google+                   | 2.0   | 0.0       |
| Android                   | 4.0   | 724.0     |
| Instagram                 | 2.0   | 0.0       |
| Android                   | 3.0   | 343.0     |
| Youtube                   | 10.0  | 429.0     |
| Android                   | 11.0  | 11259.0   |
| Newsletter                | 12.0  | 1606654.0 |
| iPhone App                | 4.0   | 55960.0   |
| iPhone app                | 12.0  | 21352.0   |
| Facebook                  | 3.0   | 291971.0  |
| Google+                   | 5.0   | 0.0       |
| Newsletter                | 5.0   | 847813.0  |
| Instagram                 | 10.0  | 28145.0   |
| Linked-In                 | 7.0   | 31758.0   |
| Tumblr                    | 5.0   | 26932.0   |

Truncated to 30 rows

Note, that we could have done aggregation using DataFrame API instead of Spark SQL.

Alright, now let's see some *cool* operations with window functions.

Our next task is to compute `(daily visits / monthly average)` for all platforms.

``` scala
import org.apache.spark.sql.functions.{dayofmonth, month, row_number, sum}
import org.apache.spark.sql.expressions.Window

val coolDF = anonym.select($"id", $"platform", dayofmonth($"date").as("day"), month($"date").as("month"), $"visits").
  groupBy($"id", $"platform", $"day", $"month").agg(sum("visits").as("visits"))

// Run window aggregation on visits per month and platform
val window = coolDF.select($"id", $"day", $"visits", sum($"visits").over(Window.partitionBy("platform", "month")).as("monthly_visits"))

// Create and register percent table
val percent = window.select($"id", $"day", ($"visits" / $"monthly_visits").as("percent"))

percent.createOrReplaceTempView("percent")
```

>     import org.apache.spark.sql.functions.{dayofmonth, month, row_number, sum}
>     import org.apache.spark.sql.expressions.Window
>     coolDF: org.apache.spark.sql.DataFrame = [id: bigint, platform: string ... 3 more fields]
>     window: org.apache.spark.sql.DataFrame = [id: bigint, day: int ... 2 more fields]
>     percent: org.apache.spark.sql.DataFrame = [id: bigint, day: int ... 1 more field]

``` sql
-- A little bit of visualization as result of our efforts
select id, day, `percent` from percent where `percent` > 0.3 and day = 2
```

| id                | day | percent             |
|-------------------|-----|---------------------|
| 6.52835028992e11  | 2.0 | 1.0                 |
| 5.06806140929e11  | 2.0 | 0.4993894993894994  |
| 6.52835028992e11  | 2.0 | 0.446576072475353   |
| 9.01943132161e11  | 2.0 | 0.5                 |
| 6.52835028992e11  | 2.0 | 0.3181818181818182  |
| 9.01943132161e11  | 2.0 | 0.6180914042150131  |
| 2.147483648e11    | 2.0 | 0.3663035756571158  |
| 1.322849927168e12 | 2.0 | 0.5265514047545539  |
| 1.322849927168e12 | 2.0 | 0.3109034021149352  |
| 1.408749273089e12 | 2.0 | 0.6937119675456389  |
| 6.52835028992e11  | 2.0 | 0.6765082509845611  |
| 5.06806140929e11  | 2.0 | 1.0                 |
| 6.52835028992e11  | 2.0 | 0.5                 |
| 4.12316860416e11  | 2.0 | 0.3408084980820301  |
| 1.580547964928e12 | 2.0 | 0.383582757848692   |
| 6.52835028992e11  | 2.0 | 0.38833874233724447 |
| 2.06158430208e11  | 2.0 | 0.9262507474586407  |
| 1.666447310848e12 | 2.0 | 0.9473684210526315  |
| 2.06158430208e11  | 2.0 | 0.5                 |
| 1.408749273089e12 | 2.0 | 0.394240317775571   |
| 6.8719476736e10   | 2.0 | 0.38461538461538464 |
| 1.640677507072e12 | 2.0 | 0.44748143897901344 |
| 9.01943132161e11  | 2.0 | 1.0                 |
| 6.52835028992e11  | 2.0 | 0.8449612403100775  |
| 9.01943132161e11  | 2.0 | 0.3060168545490231  |

``` sql
-- You also could just use plain SQL to write query above, note that you might need to group by id and day as well.
with aggr as (
  select id, dayofmonth(date) as day, visits / sum(visits) over (partition by (platform, month(date))) as percent
  from anonym
)
select * from aggr where day = 2 and percent > 0.3
```

| id                | day | percent             |
|-------------------|-----|---------------------|
| 6.52835028992e11  | 2.0 | 1.0                 |
| 5.06806140929e11  | 2.0 | 0.4993894993894994  |
| 6.52835028992e11  | 2.0 | 0.446576072475353   |
| 9.01943132161e11  | 2.0 | 0.5                 |
| 6.52835028992e11  | 2.0 | 0.3181818181818182  |
| 2.147483648e11    | 2.0 | 0.3663035756571158  |
| 9.01943132161e11  | 2.0 | 0.6180914042150131  |
| 1.322849927168e12 | 2.0 | 0.4718608035989944  |
| 1.408749273089e12 | 2.0 | 0.6937119675456389  |
| 6.52835028992e11  | 2.0 | 0.6765082509845611  |
| 5.06806140929e11  | 2.0 | 1.0                 |
| 6.52835028992e11  | 2.0 | 0.5                 |
| 4.12316860416e11  | 2.0 | 0.3408084980820301  |
| 1.580547964928e12 | 2.0 | 0.383582757848692   |
| 6.52835028992e11  | 2.0 | 0.38833874233724447 |
| 2.06158430208e11  | 2.0 | 0.9262507474586407  |
| 1.666447310848e12 | 2.0 | 0.9473684210526315  |
| 2.06158430208e11  | 2.0 | 0.5                 |
| 1.408749273089e12 | 2.0 | 0.394240317775571   |
| 6.8719476736e10   | 2.0 | 0.38461538461538464 |
| 1.640677507072e12 | 2.0 | 0.44748143897901344 |
| 9.01943132161e11  | 2.0 | 1.0                 |
| 6.52835028992e11  | 2.0 | 0.8449612403100775  |
| 9.01943132161e11  | 2.0 | 0.3060168545490231  |

Interoperating with RDDs
------------------------

Spark SQL supports two different methods for converting existing RDDs into DataFrames. The first method uses reflection to infer the schema of an RDD that contains specific types of objects. This reflection based approach leads to more concise code and works well when you already know the schema.

The second method for creating DataFrames is through a programmatic interface that allows you to construct a schema and then apply it to an
existing RDD. While this method is more verbose, it allows you to construct DataFrames when the columns and their types are not known until runtime.

### Inferring the Schema Using Reflection

The Scala interface for Spark SQL supports automatically converting an RDD containing case classes to a DataFrame. The case class defines the
schema of the table. The names of the arguments to the case class are read using reflection and become the names of the columns. Case classes
can also be nested or contain complex types such as Sequences or Arrays. This RDD can be implicitly converted to a DataFrame and then be registered as a table.

``` scala
// Define case class that will be our schema for DataFrame
case class Hubot(name: String, year: Int, manufacturer: String, version: Array[Int], details: Map[String, String])

// You can process a text file, for example, to convert every row to our Hubot, but we will create RDD manually
val rdd = sc.parallelize(
  Array(
    Hubot("Jerry", 2015, "LCorp", Array(1, 2, 3), Map("eat" -> "yes", "sleep" -> "yes", "drink" -> "yes")),
    Hubot("Mozart", 2010, "LCorp", Array(1, 2), Map("eat" -> "no", "sleep" -> "no", "drink" -> "no")),
    Hubot("Einstein", 2012, "LCorp", Array(1, 2, 3), Map("eat" -> "yes", "sleep" -> "yes", "drink" -> "no"))
  )
)
```

>     defined class Hubot
>     rdd: org.apache.spark.rdd.RDD[Hubot] = ParallelCollectionRDD[23107] at parallelize at <console>:45

``` scala
// In order to convert RDD into DataFrame you need to do this:
val hubots = rdd.toDF()

// Display DataFrame, note how array and map fields are displayed
hubots.printSchema()
hubots.show()
```

>     root
>      |-- name: string (nullable = true)
>      |-- year: integer (nullable = false)
>      |-- manufacturer: string (nullable = true)
>      |-- version: array (nullable = true)
>      |    |-- element: integer (containsNull = false)
>      |-- details: map (nullable = true)
>      |    |-- key: string
>      |    |-- value: string (valueContainsNull = true)
>
>     +--------+----+------------+---------+--------------------+
>     |    name|year|manufacturer|  version|             details|
>     +--------+----+------------+---------+--------------------+
>     |   Jerry|2015|       LCorp|[1, 2, 3]|Map(eat -> yes, s...|
>     |  Mozart|2010|       LCorp|   [1, 2]|Map(eat -> no, sl...|
>     |Einstein|2012|       LCorp|[1, 2, 3]|Map(eat -> yes, s...|
>     +--------+----+------------+---------+--------------------+
>
>     hubots: org.apache.spark.sql.DataFrame = [name: string, year: int ... 3 more fields]

``` scala
// You can query complex type the same as you query any other column
// By the way you can use `sql` function to invoke Spark SQL to create DataFrame
hubots.createOrReplaceTempView("hubots")

val onesThatEat = sqlContext.sql("select name, details.eat from hubots where details.eat = 'yes'")

onesThatEat.show()
```

>     +--------+---+
>     |    name|eat|
>     +--------+---+
>     |   Jerry|yes|
>     |Einstein|yes|
>     +--------+---+
>
>     onesThatEat: org.apache.spark.sql.DataFrame = [name: string, eat: string]

### Programmatically Specifying the Schema

When case classes cannot be defined ahead of time (for example, the structure of records is encoded in a string, or a text dataset will be parsed and fields will be projected differently for different users), a `DataFrame` can be created programmatically with three steps.

1.  Create an RDD of `Row`s from the original RDD
2.  Create the schema represented by a [StructType](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.types.StructType) and [StructField](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.types.StructField) classes matching the structure of `Row`s in the RDD created in Step 1.
3.  Apply the schema to the RDD of `Row`s via `createDataFrame` method provided by `SQLContext`.

``` scala
import org.apache.spark.sql.types._

// Let's say we have an RDD of String and we need to convert it into a DataFrame with schema "name", "year", and "manufacturer"
// As you can see every record is space-separated.
val rdd = sc.parallelize(
  Array(
    "Jerry 2015 LCorp",
    "Mozart 2010 LCorp",
    "Einstein 2012 LCorp"
  )
)

// Create schema as StructType //
val schema = StructType(
  StructField("name", StringType, false) :: 
  StructField("year", IntegerType, false) :: 
  StructField("manufacturer", StringType, false) :: 
  Nil
)

// Prepare RDD[Row]
val rows = rdd.map { entry => 
  val arr = entry.split("\\s+")
  val name = arr(0)
  val year = arr(1).toInt
  val manufacturer = arr(2)
  
  Row(name, year, manufacturer)
}

// Create DataFrame
val bots = sqlContext.createDataFrame(rows, schema)
bots.printSchema()
bots.show()
```

>     root
>      |-- name: string (nullable = false)
>      |-- year: integer (nullable = false)
>      |-- manufacturer: string (nullable = false)
>
>     +--------+----+------------+
>     |    name|year|manufacturer|
>     +--------+----+------------+
>     |   Jerry|2015|       LCorp|
>     |  Mozart|2010|       LCorp|
>     |Einstein|2012|       LCorp|
>     +--------+----+------------+
>
>     import org.apache.spark.sql.types._
>     rdd: org.apache.spark.rdd.RDD[String] = ParallelCollectionRDD[23118] at parallelize at <console>:47
>     schema: org.apache.spark.sql.types.StructType = StructType(StructField(name,StringType,false), StructField(year,IntegerType,false), StructField(manufacturer,StringType,false))
>     rows: org.apache.spark.rdd.RDD[org.apache.spark.sql.Row] = MapPartitionsRDD[23119] at map at <console>:64
>     bots: org.apache.spark.sql.DataFrame = [name: string, year: int ... 1 more field]

Creating Datasets
-----------------

A [Dataset](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.Dataset) is a strongly-typed, immutable collection of objects that are mapped to a relational schema. At the core of the Dataset API is a new concept called an [encoder](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.Encoder), which is responsible for converting between JVM objects and tabular representation. The tabular representation is stored using Spark’s internal Tungsten binary format, allowing for operations on serialized data and improved memory utilization. Spark 2.2 comes with support for automatically generating encoders for a wide variety of types, including primitive types (e.g. String, Integer, Long), and Scala case classes.

> Simply put, you will get all the benefits of DataFrames with fair amount of flexibility of RDD API.

``` scala
// We can start working with Datasets by using our "hubots" table

// To create Dataset from DataFrame do this (assuming that case class Hubot exists):
val ds = hubots.as[Hubot]
ds.show()
```

>     +--------+----+------------+---------+--------------------+
>     |    name|year|manufacturer|  version|             details|
>     +--------+----+------------+---------+--------------------+
>     |   Jerry|2015|       LCorp|[1, 2, 3]|Map(eat -> yes, s...|
>     |  Mozart|2010|       LCorp|   [1, 2]|Map(eat -> no, sl...|
>     |Einstein|2012|       LCorp|[1, 2, 3]|Map(eat -> yes, s...|
>     +--------+----+------------+---------+--------------------+
>
>     ds: org.apache.spark.sql.Dataset[Hubot] = [name: string, year: int ... 3 more fields]

> **Side-note:** Dataset API is first-class citizen in Spark, and DataFrame is an alias for Dataset\[Row\]. Note that Python and R still use DataFrames (since they are dynamically typed), but it is essentially a Dataset.

Finally
-------

DataFrames and Datasets can simplify and improve most of the applications pipelines by bringing concise syntax and performance optimizations. We would highly recommend you to check out the official API documentation, specifically around

-   [DataFrame API](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.DataFrame),
-   [Spark SQL functions library](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.functions$),
-   [GroupBy clause and aggregated functions](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.GroupedData).

Unfortunately, this is just *a getting started quickly* course, and we skip features like custom aggregations, types, pivoting, etc., but if you are keen to know then start from the links above and this notebook and others in this directory.

Appendix
--------

### How to download data and make a table

Okay, so how did we actually make table `social_media_usage`? Databricks allows us to upload/link external data and make it available as registerd SQL table. It involves several steps:

1.  Find interesting set of data - Google can be your friend for most cases here, or you can have your own dataset as CSV file, for example. Good source of data can also be found here: http://www.data.gov/
2.  Download / prepare it to be either on S3, or human-readable format like CSV, or JSON
3.  Go to Databricks cloud (where you log in to use Databricks notebooks) and open tab **Tables**
4.  On the very top of the left sub-menu you will see button **+ Create table**, click on it
5.  You will see page with drop-down menu of the list of sources you can provide, **File** means any file (Parquet, Avro, CSV), but it works the best with CSV format
6.  Once you have chosen file and loaded it, you can change column names, or tweak types (mainly for CSV format)
7.  That is it. Just click on final button to create table. After that you can refer to the table using `sqlContext.table("YOUR_TABLE_NAME")`.