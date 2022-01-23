<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Loading data
------------

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(dbutils.fs.ls("/datasets/group12/"))
```

<div class="output execute_result tabular_result" execution_count="1">

<table>
<thead>
<tr class="header">
<th>path</th>
<th>name</th>
<th>size</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>dbfs:/datasets/group12/20_12_04_08_31_44.csv</td>
<td>20_12_04_08_31_44.csv</td>
<td>1.4181338e7</td>
</tr>
<tr class="even">
<td>dbfs:/datasets/group12/20_12_04_08_32_40.csv</td>
<td>20_12_04_08_32_40.csv</td>
<td>1.4181338e7</td>
</tr>
<tr class="odd">
<td>dbfs:/datasets/group12/20_12_04_10_47_08.csv</td>
<td>20_12_04_10_47_08.csv</td>
<td>1.4190774e7</td>
</tr>
<tr class="even">
<td>dbfs:/datasets/group12/21_01_07_08_50_05.csv</td>
<td>21_01_07_08_50_05.csv</td>
<td>1.4577033e7</td>
</tr>
<tr class="odd">
<td>dbfs:/datasets/group12/21_01_07_09_05_33.csv</td>
<td>21_01_07_09_05_33.csv</td>
<td>1.4577033e7</td>
</tr>
<tr class="even">
<td>dbfs:/datasets/group12/analysis/</td>
<td>analysis/</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>dbfs:/datasets/group12/chkpoint/</td>
<td>chkpoint/</td>
<td>0.0</td>
</tr>
</tbody>
</table>

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val df = spark.read.parquet("dbfs:/datasets/group12/analysis/*.parquet")

display(df)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
df.count()
```

<div class="output execute_result plain_result" execution_count="1">

    res10: Long = 60544

</div>

</div>

<div class="cell markdown">

Preprocessing
-------------

### 0. filter out data of HongKong and unknown location

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.sql.functions._
    import org.apache.spark.sql.expressions.Window

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// filter unknow and HK data
val df_filteredLocation = df.filter($"iso_code"=!="HKG").filter($"iso_code".isNotNull)

// fill missing continent value for World data
val df_fillContinentNull = df_filteredLocation.na.fill("World",Array("continent")).cache
df_filteredLocation.unpersist()
```

<div class="output execute_result plain_result" execution_count="1">

    df_filteredLocation: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [iso_code: string, continent: string ... 48 more fields]
    df_fillContinentNull: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [iso_code: string, continent: string ... 48 more fields]
    res4: df_filteredLocation.type = [iso_code: string, continent: string ... 48 more fields]

</div>

</div>

<div class="cell markdown">

### 1. fill dates from 2020-01-23 (to ensure all countries having 316 days loggings)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// filter date before 2020-01-23
val df_filtered_date = df_fillContinentNull.filter($"date">"2020-01-22").cache
df_fillContinentNull.unpersist()
```

<div class="output execute_result plain_result" execution_count="1">

    df_filtered_date: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [iso_code: string, continent: string ... 48 more fields]
    res6: df_fillContinentNull.type = [iso_code: string, continent: string ... 48 more fields]

</div>

</div>

<div class="cell markdown">

### 2. Fill missing value for total*cases, total*deaths, new*cases*smoothed, new*deaths*smoothed

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala

// fill missing for new_cases_smoothed and new_deaths_smoothed
val df_fillNullForSmooth = df_filtered_date.na.fill(0,Array("new_cases_smoothed"))
                           .na.fill(0,Array("new_deaths_smoothed"))
                           .cache
df_filtered_date.unpersist()

// fill missing for total_cases
val df_fillNullForTotalCases = df_fillNullForSmooth.na.fill(0, Array("total_cases")).cache
df_fillNullForSmooth.unpersist()

// correct total_deaths, new_deaths, new_deaths_smoothed
val df_fillNullForTotalDeathsSpecial = df_fillNullForTotalCases.withColumn("total_deaths_correct", 
                                        when(col("iso_code").equalTo("ISL")&&(col("date")>"2020-03-13" && col("date")<"2020-03-22"),0)
                                       .when(col("iso_code").equalTo("PNG")&&(col("date")>"2020-07-19" && col("date")<"2020-07-23"),0)
                                       .when(col("iso_code").equalTo("SVK")&&(col("date")>"2020-03-17" && col("date")<"2020-03-23"),0).otherwise(col("total_deaths")))
                            .withColumn("new_deaths_correct", 
                                        when(col("iso_code").equalTo("ISL")&&(col("date")>"2020-03-13" && col("date")<"2020-03-22"),0)
                                       .when(col("iso_code").equalTo("PNG")&&(col("date")>"2020-07-19" && col("date")<"2020-07-23"),0)
                                       .when(col("iso_code").equalTo("SVK")&&(col("date")>"2020-03-17" && col("date")<"2020-03-23"),0).otherwise(col("new_deaths")))
                            .withColumn("new_deaths_smoothed_correct", 
                                        when(col("iso_code").equalTo("ISL")&&(col("date")>"2020-03-13" && col("date")<"2020-03-22"),0)
                                       .when(col("iso_code").equalTo("PNG")&&(col("date")>"2020-07-19" && col("date")<"2020-07-23"),0)
                                       .when(col("iso_code").equalTo("SVK")&&(col("date")>"2020-03-17" && col("date")<"2020-03-23"),0).otherwise(col("new_deaths_smoothed")))
                            .cache
df_fillNullForTotalCases.unpersist()

val df_cleaned = df_fillNullForTotalDeathsSpecial
                                  .drop("new_deaths", "total_deaths", "new_deaths_smoothed") // drop old column to rename
                                  .withColumnRenamed("new_deaths_correct","new_deaths")
                                  .withColumnRenamed("total_deaths_correct","total_deaths")
                                  .withColumnRenamed("new_deaths_smoothed_correct","new_deaths_smoothed")
                                  .na.fill(0, Array("total_deaths"))
                                  .select(df.columns.head, df.columns.tail: _*)
                                  .cache
df_fillNullForTotalDeathsSpecial.unpersist()

display(df_cleaned)
```

</div>

<div class="cell markdown">

### 3. Select invariant (during pandemic) features for clustering and filter out countries that have missing invariant features

Invariant feature list: - population - population*density - median*age - aged*65*older - aged*70*older - gdp*per*capita - cardiovasc*death*rate - diabetes*prevalence - female*smokers - male*smokers - hospital*beds*per*thousand - life*expectancy - human*development\_index

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// select invariant features
val df_invariantFeatures = df_cleaned.select($"iso_code",$"location", $"population",$"population_density",
                                             $"median_age", $"aged_65_older",
                                             $"aged_70_older",$"gdp_per_capita",
                                             $"cardiovasc_death_rate",$"diabetes_prevalence",
                                             $"female_smokers",$"male_smokers",$"hospital_beds_per_thousand",
                                             $"life_expectancy",$"human_development_index").cache

// Extract valid distrinct features RDD
val valid_distinct_features = df_invariantFeatures.distinct()
                                 .filter($"population".isNotNull && $"population_density".isNotNull && $"median_age".isNotNull && 
                                 $"aged_65_older".isNotNull && $"aged_70_older".isNotNull && $"gdp_per_capita".isNotNull &&
                                 $"cardiovasc_death_rate".isNotNull && $"diabetes_prevalence".isNotNull && $"female_smokers".isNotNull && 
                                 $"male_smokers".isNotNull && $"hospital_beds_per_thousand".isNotNull && $"life_expectancy".isNotNull &&
                                 $"human_development_index".isNotNull)


// filter out NULL feature countries
val df_cleaned_feature = df_cleaned.filter($"location".isin(valid_distinct_features.select($"location").rdd.map(r => r(0)).collect().toSeq: _*)).cache

df_cleaned.unpersist()

display(df_cleaned_feature)
```

</div>

<div class="cell markdown">

### 4. Imputing missing data for

-   total*cases*per\_million
-   new*cases*per\_million
-   new*cases*smoothed*per*million
-   total*deaths*per\_million
-   new*deaths*per\_million
-   new*deaths*smoothed*per*million

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val df_cleaned_feature_permillion = df_cleaned_feature.withColumn("total_cases_per_million_correct", df_cleaned_feature("total_cases")/df_cleaned_feature("population")*1000000)
                                                   .withColumn("new_cases_per_million_correct", df_cleaned_feature("new_cases")/df_cleaned_feature("population")*1000000)
                                                   .withColumn("new_cases_smoothed_per_million_correct", df_cleaned_feature("new_cases_smoothed")/df_cleaned_feature("population")*1000000)
                                                   .withColumn("total_deaths_per_million_correct", df_cleaned_feature("total_deaths")/df_cleaned_feature("population")*1000000)
                                                   .withColumn("new_deaths_per_million_correct", df_cleaned_feature("new_deaths")/df_cleaned_feature("population")*1000000)
                                                   .withColumn("new_deaths_smoothed_per_million_correct", df_cleaned_feature("new_deaths_smoothed")/df_cleaned_feature("population")*1000000)
                                                   .drop("total_cases_per_million", "new_cases_per_million", "new_cases_smoothed_per_million", 
                                                         "total_deaths_per_million", "new_deaths_per_million", "new_deaths_smoothed_per_million") // drop old column to rename
                                                   .withColumnRenamed("total_cases_per_million_correct","total_cases_per_million")
                                                   .withColumnRenamed("new_cases_per_million_correct","new_cases_per_million")
                                                   .withColumnRenamed("new_cases_smoothed_per_million_correct","new_cases_smoothed_per_million")
                                                   .withColumnRenamed("total_deaths_per_million_correct","total_deaths_per_million")
                                                   .withColumnRenamed("new_deaths_per_million_correct","new_deaths_per_million")
                                                   .withColumnRenamed("new_deaths_smoothed_per_million_correct","new_deaths_smoothed_per_million")
```

<div class="output execute_result plain_result" execution_count="1">

    df_cleaned_feature_permillion: org.apache.spark.sql.DataFrame = [iso_code: string, continent: string ... 48 more fields]

</div>

</div>

<div class="cell markdown">

### 5. Impute time series data of

-   reproduction\_rate
-   total\_tests
-   stringency\_index
-   total*tests*per\_thousand

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val df_cleaned_time_series = df_cleaned_feature_permillion
                              .withColumn("reproduction_rate", last("reproduction_rate", true)
                                         .over(Window.partitionBy("location").orderBy("date").rowsBetween(-df_cleaned_feature_permillion.count(), 0)))
                              .withColumn("reproduction_rate", first("reproduction_rate", true)
                                         .over(Window.partitionBy("location").orderBy("date").rowsBetween(0, df_cleaned_feature_permillion.count())))
                              .na.fill(0, Array("reproduction_rate"))
                              .withColumn("stringency_index", last("stringency_index", true)
                                         .over(Window.partitionBy("location").orderBy("date").rowsBetween(-df_cleaned_feature_permillion.count(), 0)))
                              .na.fill(0, Array("stringency_index"))
                              .withColumn("total_tests", last("total_tests", true)
                                         .over(Window.partitionBy("location").orderBy("date").rowsBetween(-df_cleaned_feature_permillion.count(), 0)))
                              .withColumn("total_tests", first("total_tests", true)
                                         .over(Window.partitionBy("location").orderBy("date").rowsBetween(0, df_cleaned_feature_permillion.count())))
                              .na.fill(0, Array("total_tests"))
                              .withColumn("total_tests_per_thousand", col("total_tests")/col("population")*1000)
                              .cache

df_cleaned_feature_permillion.unpersist()

display(df_cleaned_time_series)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>
