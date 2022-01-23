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

<div class="cell markdown">

Load parquet file

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val df = spark.read.parquet("dbfs:/datasets/group12/analysis/*.parquet")

display(df)
```

</div>

<div class="cell markdown">

Load csv file

</div>

<div class="cell markdown">

-\> %scala //if want to load csv

val file*location = "/datasets/group12/20*12*04*10*47*08.csv" val file\_type = "csv"

// CSV options val infer*schema = "true" val first*row*is*header = "true" val delimiter = ","

// The applied options are for CSV files. For other file types, these will be ignored. val df = spark.read.format(file*type) .option("inferSchema", infer*schema) .option("header", first*row*is*header) .option("sep", delimiter) .load(file*location)

display(df)

</div>

<div class="cell markdown">

Number of data

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
df.count()
```

<div class="output execute_result plain_result" execution_count="1">

    res19: Long = 60544

</div>

</div>

<div class="cell markdown">

Missing Features in data due to multiple web resource

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.sql.functions._

for (c <- df.columns) {
  println(c + ": " + df.filter(col(c).isNull).count())
}
```

</div>

<div class="cell markdown">

Preprocessing
-------------

### 0. filter out data of HongKong and unknown location

</div>

<div class="cell markdown">

Here shows HK does not have meaningful value and there is one unknown international location in data.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(df.filter($"location"==="Hong Kong" || $"iso_code".isNull)) //HK data iteself is not complete for all dates, and all available data is null! HAVE TO FILTER IT OUT COMPLETELY
```

</div>

<div class="cell markdown">

190 valid countries data to continue

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val df_filteredLocation = df.filter($"iso_code"=!="HKG").filter($"iso_code".isNotNull)
display(df_filteredLocation.select($"location").distinct()) // 190 valid countries 
```

</div>

<div class="cell markdown">

Fill missing continent value for World aggregate data NOTE: it will be filled as "World"

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(df_filteredLocation.where($"continent".isNull))
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val df_fillContinentNull = df_filteredLocation.na.fill("World",Array("continent"))
display(df_fillContinentNull)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
df_fillContinentNull.count()
```

<div class="output execute_result plain_result" execution_count="1">

    res27: Long = 60158

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.sql.functions._

for (c <- df_fillContinentNull.columns) {
  println(c + ": " + df_fillContinentNull.filter(col(c).isNull).count())
}
```

</div>

<div class="cell markdown">

### 1. filter dates only from 2020-01-23 (to ensure all countries having 316 days logging)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(df_fillContinentNull.select($"date",$"iso_code").groupBy($"iso_code").count())  // some country starts logging data earlier
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val df_filtered_date = df_fillContinentNull.filter($"date">"2020-01-22")
```

<div class="output execute_result plain_result" execution_count="1">

    df_filtered_date: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [iso_code: string, continent: string ... 48 more fields]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(df_filtered_date.select($"date",$"iso_code").groupBy($"iso_code").count())  // all countries have 316 days logging
```

</div>

<div class="cell markdown">

### 2. Fill missing value for total*cases, total*deaths, new*cases*smoothed, new*deaths*smoothed

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(df_filtered_date.select($"date",$"iso_code", $"total_cases", $"total_deaths", $"new_cases", $"new_deaths", $"new_cases_smoothed", $"new_deaths_smoothed").filter($"new_cases_smoothed".isNull || $"new_deaths_smoothed".isNull))
```

</div>

<div class="cell markdown">

All missing data of new*cases*smoothed and new*deaths*smoothed from early, so just fill with 0

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(df_filtered_date.select($"date",$"iso_code", $"total_cases", $"total_deaths", $"new_cases", $"new_deaths", $"new_cases_smoothed", $"new_deaths_smoothed")
        .filter($"new_cases_smoothed".isNull || $"new_deaths_smoothed".isNull).select($"date").distinct())
```

<div class="output execute_result tabular_result" execution_count="1">

<table>
<thead>
<tr class="header">
<th>date</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>2020-01-23</td>
</tr>
<tr class="even">
<td>2020-01-27</td>
</tr>
<tr class="odd">
<td>2020-01-24</td>
</tr>
<tr class="even">
<td>2020-01-26</td>
</tr>
<tr class="odd">
<td>2020-01-25</td>
</tr>
</tbody>
</table>

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val df_fillNullForSmooth = df_filtered_date.na.fill(0,Array("new_cases_smoothed"))
                           .na.fill(0,Array("new_deaths_smoothed"))
display(df_fillNullForSmooth)
```

</div>

<div class="cell markdown">

Fill total*deaths and total*cases null value

Strictly, when new*cases is always 0, total*cases could be imputed as 0. The same apply to total\_deaths

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val df_NULL_total_cases = df_fillNullForSmooth.select($"date",$"iso_code", $"total_cases", $"total_deaths", $"new_cases", $"new_deaths", $"new_cases_smoothed", $"new_deaths_smoothed")
                          .filter($"total_cases".isNull)


display(df_NULL_total_cases.filter($"new_cases"===0).groupBy("iso_code").count())
```

</div>

<div class="cell markdown">

When total*case is Null, all previous new*cases is always 0.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
df_NULL_total_cases.filter($"total_cases".isNull).groupBy("iso_code").count().except(df_NULL_total_cases.filter($"new_cases"===0).groupBy("iso_code").count()).show() // When total_case is Null, all new_cases is always 0
```

<div class="output execute_result plain_result" execution_count="1">

    +--------+-----+
    |iso_code|count|
    +--------+-----+
    +--------+-----+

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val df_fillNullForTotalCases = df_fillNullForSmooth.na.fill(0, Array("total_cases"))
                               
display(df_fillNullForTotalCases)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val df_NULL_total_death = df_fillNullForTotalCases.select($"date",$"iso_code", $"total_cases", $"total_deaths", $"new_cases", $"new_deaths", $"new_cases_smoothed", $"new_deaths_smoothed")
                          .filter($"total_deaths".isNull)


display(df_NULL_total_death.filter($"new_deaths"===0).groupBy("iso_code").count().sort())
```

</div>

<div class="cell markdown">

If total*deaths is Null when all new*deaths is always 0, then we could simply assign 0 for NULL, otherwise need to investigate more.

Three countries (ISL, PNG, SVK) have abnormal correction on new\_cases data.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val abnormal_countries = df_NULL_total_death.filter($"total_deaths".isNull).groupBy("iso_code").count().except(df_NULL_total_death.filter($"new_deaths"===0).groupBy("iso_code").count())
abnormal_countries.show()
df_NULL_total_death.filter($"new_deaths"===0).groupBy("iso_code").count().except(df_NULL_total_death.filter($"total_deaths".isNull).groupBy("iso_code").count()).show()
```

<div class="output execute_result plain_result" execution_count="1">

    +--------+-----+
    |iso_code|count|
    +--------+-----+
    |     PNG|  186|
    |     SVK|   65|
    |     ISL|   54|
    +--------+-----+

    +--------+-----+
    |iso_code|count|
    +--------+-----+
    |     PNG|  185|
    |     SVK|   64|
    |     ISL|   52|
    +--------+-----+

    abnormal_countries: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [iso_code: string, count: bigint]

</div>

</div>

<div class="cell markdown">

show abnormal death correction

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(df_fillNullForSmooth.filter($"iso_code"==="ISL").sort("date").filter($"date">"2020-03-13" && $"date"<"2020-03-22")) // death data correction between 2020-03-14 and 2020-03-21, total_deaths -> all 0, new_deaths -> all 0, new_deaths_smoothed -> all 0
```

<div class="output execute_result tabular_result" execution_count="1">

<table>
<thead>
<tr class="header">
<th>iso_code</th>
<th>continent</th>
<th>location</th>
<th>date</th>
<th>total_cases</th>
<th>new_cases</th>
<th>new_cases_smoothed</th>
<th>total_deaths</th>
<th>new_deaths</th>
<th>new_deaths_smoothed</th>
<th>total_cases_per_million</th>
<th>new_cases_per_million</th>
<th>new_cases_smoothed_per_million</th>
<th>total_deaths_per_million</th>
<th>new_deaths_per_million</th>
<th>new_deaths_smoothed_per_million</th>
<th>reproduction_rate</th>
<th>icu_patients</th>
<th>icu_patients_per_million</th>
<th>hosp_patients</th>
<th>hosp_patients_per_million</th>
<th>weekly_icu_admissions</th>
<th>weekly_icu_admissions_per_million</th>
<th>weekly_hosp_admissions</th>
<th>weekly_hosp_admissions_per_million</th>
<th>total_tests</th>
<th>new_tests</th>
<th>total_tests_per_thousand</th>
<th>new_tests_per_thousand</th>
<th>new_tests_smoothed</th>
<th>new_tests_smoothed_per_thousand</th>
<th>tests_per_case</th>
<th>positive_rate</th>
<th>tests_units</th>
<th>stringency_index</th>
<th>population</th>
<th>population_density</th>
<th>median_age</th>
<th>aged_65_older</th>
<th>aged_70_older</th>
<th>gdp_per_capita</th>
<th>extreme_poverty</th>
<th>cardiovasc_death_rate</th>
<th>diabetes_prevalence</th>
<th>female_smokers</th>
<th>male_smokers</th>
<th>handwashing_facilities</th>
<th>hospital_beds_per_thousand</th>
<th>life_expectancy</th>
<th>human_development_index</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>ISL</td>
<td>Europe</td>
<td>Iceland</td>
<td>2020-03-14</td>
<td>156.0</td>
<td>22.0</td>
<td>15.143</td>
<td>null</td>
<td>0.0</td>
<td>0.0</td>
<td>457.143</td>
<td>64.469</td>
<td>44.375</td>
<td>null</td>
<td>0.0</td>
<td>0.0</td>
<td>1.62</td>
<td>1.0</td>
<td>2.93</td>
<td>3.0</td>
<td>8.791</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>1827.0</td>
<td>323.0</td>
<td>5.354</td>
<td>0.947</td>
<td>198.0</td>
<td>0.58</td>
<td>7.6e-2</td>
<td>13.1</td>
<td>null</td>
<td>16.67</td>
<td>341250.0</td>
<td>3.404</td>
<td>37.3</td>
<td>14.431</td>
<td>9.207</td>
<td>46482.958</td>
<td>0.2</td>
<td>117.992</td>
<td>5.31</td>
<td>14.3</td>
<td>15.2</td>
<td>null</td>
<td>2.91</td>
<td>82.99</td>
<td>0.935</td>
</tr>
<tr class="even">
<td>ISL</td>
<td>Europe</td>
<td>Iceland</td>
<td>2020-03-15</td>
<td>171.0</td>
<td>15.0</td>
<td>17.286</td>
<td>5.0</td>
<td>5.0</td>
<td>0.714</td>
<td>501.099</td>
<td>43.956</td>
<td>50.654</td>
<td>14.652</td>
<td>14.652</td>
<td>2.093</td>
<td>1.61</td>
<td>2.0</td>
<td>5.861</td>
<td>3.0</td>
<td>8.791</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>2902.0</td>
<td>1075.0</td>
<td>8.504</td>
<td>3.15</td>
<td>346.0</td>
<td>1.014</td>
<td>5.0e-2</td>
<td>20.0</td>
<td>null</td>
<td>25.0</td>
<td>341250.0</td>
<td>3.404</td>
<td>37.3</td>
<td>14.431</td>
<td>9.207</td>
<td>46482.958</td>
<td>0.2</td>
<td>117.992</td>
<td>5.31</td>
<td>14.3</td>
<td>15.2</td>
<td>null</td>
<td>2.91</td>
<td>82.99</td>
<td>0.935</td>
</tr>
<tr class="odd">
<td>ISL</td>
<td>Europe</td>
<td>Iceland</td>
<td>2020-03-16</td>
<td>180.0</td>
<td>9.0</td>
<td>17.429</td>
<td>null</td>
<td>-5.0</td>
<td>0.0</td>
<td>527.473</td>
<td>26.374</td>
<td>51.073</td>
<td>null</td>
<td>-14.652</td>
<td>0.0</td>
<td>1.63</td>
<td>2.0</td>
<td>5.861</td>
<td>4.0</td>
<td>11.722</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>4609.0</td>
<td>1707.0</td>
<td>13.506</td>
<td>5.002</td>
<td>576.0</td>
<td>1.688</td>
<td>3.0e-2</td>
<td>33.0</td>
<td>null</td>
<td>50.93</td>
<td>341250.0</td>
<td>3.404</td>
<td>37.3</td>
<td>14.431</td>
<td>9.207</td>
<td>46482.958</td>
<td>0.2</td>
<td>117.992</td>
<td>5.31</td>
<td>14.3</td>
<td>15.2</td>
<td>null</td>
<td>2.91</td>
<td>82.99</td>
<td>0.935</td>
</tr>
<tr class="even">
<td>ISL</td>
<td>Europe</td>
<td>Iceland</td>
<td>2020-03-17</td>
<td>220.0</td>
<td>40.0</td>
<td>21.571</td>
<td>1.0</td>
<td>1.0</td>
<td>0.143</td>
<td>644.689</td>
<td>117.216</td>
<td>63.213</td>
<td>2.93</td>
<td>2.93</td>
<td>0.419</td>
<td>1.7</td>
<td>2.0</td>
<td>5.861</td>
<td>5.0</td>
<td>14.652</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>6009.0</td>
<td>1400.0</td>
<td>17.609</td>
<td>4.103</td>
<td>752.0</td>
<td>2.204</td>
<td>2.9e-2</td>
<td>34.9</td>
<td>null</td>
<td>50.93</td>
<td>341250.0</td>
<td>3.404</td>
<td>37.3</td>
<td>14.431</td>
<td>9.207</td>
<td>46482.958</td>
<td>0.2</td>
<td>117.992</td>
<td>5.31</td>
<td>14.3</td>
<td>15.2</td>
<td>null</td>
<td>2.91</td>
<td>82.99</td>
<td>0.935</td>
</tr>
<tr class="odd">
<td>ISL</td>
<td>Europe</td>
<td>Iceland</td>
<td>2020-03-18</td>
<td>250.0</td>
<td>30.0</td>
<td>23.571</td>
<td>1.0</td>
<td>0.0</td>
<td>0.143</td>
<td>732.601</td>
<td>87.912</td>
<td>69.074</td>
<td>2.93</td>
<td>0.0</td>
<td>0.419</td>
<td>1.73</td>
<td>0.0</td>
<td>0.0</td>
<td>6.0</td>
<td>17.582</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>7837.0</td>
<td>1828.0</td>
<td>22.966</td>
<td>5.357</td>
<td>992.0</td>
<td>2.907</td>
<td>2.4e-2</td>
<td>42.1</td>
<td>null</td>
<td>50.93</td>
<td>341250.0</td>
<td>3.404</td>
<td>37.3</td>
<td>14.431</td>
<td>9.207</td>
<td>46482.958</td>
<td>0.2</td>
<td>117.992</td>
<td>5.31</td>
<td>14.3</td>
<td>15.2</td>
<td>null</td>
<td>2.91</td>
<td>82.99</td>
<td>0.935</td>
</tr>
<tr class="even">
<td>ISL</td>
<td>Europe</td>
<td>Iceland</td>
<td>2020-03-19</td>
<td>330.0</td>
<td>80.0</td>
<td>32.429</td>
<td>1.0</td>
<td>0.0</td>
<td>0.143</td>
<td>967.033</td>
<td>234.432</td>
<td>95.029</td>
<td>2.93</td>
<td>0.0</td>
<td>0.419</td>
<td>1.78</td>
<td>1.0</td>
<td>2.93</td>
<td>6.0</td>
<td>17.582</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>9148.0</td>
<td>1311.0</td>
<td>26.807</td>
<td>3.842</td>
<td>1143.0</td>
<td>3.349</td>
<td>2.8e-2</td>
<td>35.2</td>
<td>null</td>
<td>50.93</td>
<td>341250.0</td>
<td>3.404</td>
<td>37.3</td>
<td>14.431</td>
<td>9.207</td>
<td>46482.958</td>
<td>0.2</td>
<td>117.992</td>
<td>5.31</td>
<td>14.3</td>
<td>15.2</td>
<td>null</td>
<td>2.91</td>
<td>82.99</td>
<td>0.935</td>
</tr>
<tr class="odd">
<td>ISL</td>
<td>Europe</td>
<td>Iceland</td>
<td>2020-03-20</td>
<td>409.0</td>
<td>79.0</td>
<td>39.286</td>
<td>null</td>
<td>-1.0</td>
<td>0.0</td>
<td>1198.535</td>
<td>231.502</td>
<td>115.123</td>
<td>null</td>
<td>-2.93</td>
<td>0.0</td>
<td>1.75</td>
<td>1.0</td>
<td>2.93</td>
<td>10.0</td>
<td>29.304</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>9727.0</td>
<td>579.0</td>
<td>28.504</td>
<td>1.697</td>
<td>1175.0</td>
<td>3.443</td>
<td>3.3e-2</td>
<td>29.9</td>
<td>null</td>
<td>53.7</td>
<td>341250.0</td>
<td>3.404</td>
<td>37.3</td>
<td>14.431</td>
<td>9.207</td>
<td>46482.958</td>
<td>0.2</td>
<td>117.992</td>
<td>5.31</td>
<td>14.3</td>
<td>15.2</td>
<td>null</td>
<td>2.91</td>
<td>82.99</td>
<td>0.935</td>
</tr>
<tr class="even">
<td>ISL</td>
<td>Europe</td>
<td>Iceland</td>
<td>2020-03-21</td>
<td>473.0</td>
<td>64.0</td>
<td>45.286</td>
<td>1.0</td>
<td>1.0</td>
<td>0.143</td>
<td>1386.081</td>
<td>187.546</td>
<td>132.705</td>
<td>2.93</td>
<td>2.93</td>
<td>0.419</td>
<td>1.68</td>
<td>1.0</td>
<td>2.93</td>
<td>12.0</td>
<td>35.165</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>10077.0</td>
<td>350.0</td>
<td>29.53</td>
<td>1.026</td>
<td>1179.0</td>
<td>3.455</td>
<td>3.8e-2</td>
<td>26.0</td>
<td>null</td>
<td>53.7</td>
<td>341250.0</td>
<td>3.404</td>
<td>37.3</td>
<td>14.431</td>
<td>9.207</td>
<td>46482.958</td>
<td>0.2</td>
<td>117.992</td>
<td>5.31</td>
<td>14.3</td>
<td>15.2</td>
<td>null</td>
<td>2.91</td>
<td>82.99</td>
<td>0.935</td>
</tr>
</tbody>
</table>

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(df_fillNullForSmooth.filter($"iso_code"==="PNG").sort("date").filter($"date">"2020-07-19" && $"date"<"2020-07-24" )) // death data correction between 2020-07-20 and 2020-07-22, total_deaths -> all 0, new_deaths -> all 0, new_deaths_smoothed -> all 0
```

<div class="output execute_result tabular_result" execution_count="1">

<table>
<thead>
<tr class="header">
<th>iso_code</th>
<th>continent</th>
<th>location</th>
<th>date</th>
<th>total_cases</th>
<th>new_cases</th>
<th>new_cases_smoothed</th>
<th>total_deaths</th>
<th>new_deaths</th>
<th>new_deaths_smoothed</th>
<th>total_cases_per_million</th>
<th>new_cases_per_million</th>
<th>new_cases_smoothed_per_million</th>
<th>total_deaths_per_million</th>
<th>new_deaths_per_million</th>
<th>new_deaths_smoothed_per_million</th>
<th>reproduction_rate</th>
<th>icu_patients</th>
<th>icu_patients_per_million</th>
<th>hosp_patients</th>
<th>hosp_patients_per_million</th>
<th>weekly_icu_admissions</th>
<th>weekly_icu_admissions_per_million</th>
<th>weekly_hosp_admissions</th>
<th>weekly_hosp_admissions_per_million</th>
<th>total_tests</th>
<th>new_tests</th>
<th>total_tests_per_thousand</th>
<th>new_tests_per_thousand</th>
<th>new_tests_smoothed</th>
<th>new_tests_smoothed_per_thousand</th>
<th>tests_per_case</th>
<th>positive_rate</th>
<th>tests_units</th>
<th>stringency_index</th>
<th>population</th>
<th>population_density</th>
<th>median_age</th>
<th>aged_65_older</th>
<th>aged_70_older</th>
<th>gdp_per_capita</th>
<th>extreme_poverty</th>
<th>cardiovasc_death_rate</th>
<th>diabetes_prevalence</th>
<th>female_smokers</th>
<th>male_smokers</th>
<th>handwashing_facilities</th>
<th>hospital_beds_per_thousand</th>
<th>life_expectancy</th>
<th>human_development_index</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>PNG</td>
<td>Oceania</td>
<td>Papua New Guinea</td>
<td>2020-07-20</td>
<td>19.0</td>
<td>3.0</td>
<td>1.143</td>
<td>1.0</td>
<td>1.0</td>
<td>0.143</td>
<td>2.124</td>
<td>0.335</td>
<td>0.128</td>
<td>0.112</td>
<td>0.112</td>
<td>1.6e-2</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>45.37</td>
<td>8947027.0</td>
<td>18.22</td>
<td>22.6</td>
<td>3.808</td>
<td>2.142</td>
<td>3823.194</td>
<td>null</td>
<td>561.494</td>
<td>17.65</td>
<td>23.5</td>
<td>48.8</td>
<td>null</td>
<td>null</td>
<td>64.5</td>
<td>0.544</td>
</tr>
<tr class="even">
<td>PNG</td>
<td>Oceania</td>
<td>Papua New Guinea</td>
<td>2020-07-21</td>
<td>27.0</td>
<td>8.0</td>
<td>2.286</td>
<td>1.0</td>
<td>0.0</td>
<td>0.143</td>
<td>3.018</td>
<td>0.894</td>
<td>0.255</td>
<td>0.112</td>
<td>0.0</td>
<td>1.6e-2</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>45.37</td>
<td>8947027.0</td>
<td>18.22</td>
<td>22.6</td>
<td>3.808</td>
<td>2.142</td>
<td>3823.194</td>
<td>null</td>
<td>561.494</td>
<td>17.65</td>
<td>23.5</td>
<td>48.8</td>
<td>null</td>
<td>null</td>
<td>64.5</td>
<td>0.544</td>
</tr>
<tr class="odd">
<td>PNG</td>
<td>Oceania</td>
<td>Papua New Guinea</td>
<td>2020-07-22</td>
<td>30.0</td>
<td>3.0</td>
<td>2.714</td>
<td>null</td>
<td>-1.0</td>
<td>0.0</td>
<td>3.353</td>
<td>0.335</td>
<td>0.303</td>
<td>null</td>
<td>-0.112</td>
<td>0.0</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>45.37</td>
<td>8947027.0</td>
<td>18.22</td>
<td>22.6</td>
<td>3.808</td>
<td>2.142</td>
<td>3823.194</td>
<td>null</td>
<td>561.494</td>
<td>17.65</td>
<td>23.5</td>
<td>48.8</td>
<td>null</td>
<td>null</td>
<td>64.5</td>
<td>0.544</td>
</tr>
<tr class="even">
<td>PNG</td>
<td>Oceania</td>
<td>Papua New Guinea</td>
<td>2020-07-23</td>
<td>31.0</td>
<td>1.0</td>
<td>2.857</td>
<td>null</td>
<td>0.0</td>
<td>0.0</td>
<td>3.465</td>
<td>0.112</td>
<td>0.319</td>
<td>null</td>
<td>0.0</td>
<td>0.0</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>45.37</td>
<td>8947027.0</td>
<td>18.22</td>
<td>22.6</td>
<td>3.808</td>
<td>2.142</td>
<td>3823.194</td>
<td>null</td>
<td>561.494</td>
<td>17.65</td>
<td>23.5</td>
<td>48.8</td>
<td>null</td>
<td>null</td>
<td>64.5</td>
<td>0.544</td>
</tr>
</tbody>
</table>

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(df_fillNullForSmooth.filter($"iso_code"==="SVK").sort("date").filter($"date">"2020-03-16" && $"date"<"2020-03-23")) // death data correction between 2020-03-18 and 2020-03-22, total_deaths -> all 0, new_deaths -> all 0, new_deaths_smoothed -> all 0
```

<div class="output execute_result tabular_result" execution_count="1">

<table>
<thead>
<tr class="header">
<th>iso_code</th>
<th>continent</th>
<th>location</th>
<th>date</th>
<th>total_cases</th>
<th>new_cases</th>
<th>new_cases_smoothed</th>
<th>total_deaths</th>
<th>new_deaths</th>
<th>new_deaths_smoothed</th>
<th>total_cases_per_million</th>
<th>new_cases_per_million</th>
<th>new_cases_smoothed_per_million</th>
<th>total_deaths_per_million</th>
<th>new_deaths_per_million</th>
<th>new_deaths_smoothed_per_million</th>
<th>reproduction_rate</th>
<th>icu_patients</th>
<th>icu_patients_per_million</th>
<th>hosp_patients</th>
<th>hosp_patients_per_million</th>
<th>weekly_icu_admissions</th>
<th>weekly_icu_admissions_per_million</th>
<th>weekly_hosp_admissions</th>
<th>weekly_hosp_admissions_per_million</th>
<th>total_tests</th>
<th>new_tests</th>
<th>total_tests_per_thousand</th>
<th>new_tests_per_thousand</th>
<th>new_tests_smoothed</th>
<th>new_tests_smoothed_per_thousand</th>
<th>tests_per_case</th>
<th>positive_rate</th>
<th>tests_units</th>
<th>stringency_index</th>
<th>population</th>
<th>population_density</th>
<th>median_age</th>
<th>aged_65_older</th>
<th>aged_70_older</th>
<th>gdp_per_capita</th>
<th>extreme_poverty</th>
<th>cardiovasc_death_rate</th>
<th>diabetes_prevalence</th>
<th>female_smokers</th>
<th>male_smokers</th>
<th>handwashing_facilities</th>
<th>hospital_beds_per_thousand</th>
<th>life_expectancy</th>
<th>human_development_index</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>SVK</td>
<td>Europe</td>
<td>Slovakia</td>
<td>2020-03-17</td>
<td>72.0</td>
<td>9.0</td>
<td>9.286</td>
<td>null</td>
<td>0.0</td>
<td>0.0</td>
<td>13.188</td>
<td>1.648</td>
<td>1.701</td>
<td>null</td>
<td>0.0</td>
<td>0.0</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>1913.0</td>
<td>318.0</td>
<td>0.35</td>
<td>5.8e-2</td>
<td>173.0</td>
<td>3.2e-2</td>
<td>5.4e-2</td>
<td>18.6</td>
<td>null</td>
<td>75.0</td>
<td>5459643.0</td>
<td>113.128</td>
<td>41.2</td>
<td>15.07</td>
<td>9.167</td>
<td>30155.152</td>
<td>0.7</td>
<td>287.959</td>
<td>7.29</td>
<td>23.1</td>
<td>37.7</td>
<td>null</td>
<td>5.82</td>
<td>77.54</td>
<td>0.855</td>
</tr>
<tr class="even">
<td>SVK</td>
<td>Europe</td>
<td>Slovakia</td>
<td>2020-03-18</td>
<td>105.0</td>
<td>33.0</td>
<td>13.571</td>
<td>1.0</td>
<td>1.0</td>
<td>0.143</td>
<td>19.232</td>
<td>6.044</td>
<td>2.486</td>
<td>0.183</td>
<td>0.183</td>
<td>2.6e-2</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>2138.0</td>
<td>225.0</td>
<td>0.392</td>
<td>4.1e-2</td>
<td>192.0</td>
<td>3.5e-2</td>
<td>7.1e-2</td>
<td>14.1</td>
<td>null</td>
<td>75.0</td>
<td>5459643.0</td>
<td>113.128</td>
<td>41.2</td>
<td>15.07</td>
<td>9.167</td>
<td>30155.152</td>
<td>0.7</td>
<td>287.959</td>
<td>7.29</td>
<td>23.1</td>
<td>37.7</td>
<td>null</td>
<td>5.82</td>
<td>77.54</td>
<td>0.855</td>
</tr>
<tr class="odd">
<td>SVK</td>
<td>Europe</td>
<td>Slovakia</td>
<td>2020-03-19</td>
<td>123.0</td>
<td>18.0</td>
<td>15.286</td>
<td>1.0</td>
<td>0.0</td>
<td>0.143</td>
<td>22.529</td>
<td>3.297</td>
<td>2.8</td>
<td>0.183</td>
<td>0.0</td>
<td>2.6e-2</td>
<td>1.19</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>2439.0</td>
<td>301.0</td>
<td>0.447</td>
<td>5.5e-2</td>
<td>221.0</td>
<td>4.0e-2</td>
<td>6.9e-2</td>
<td>14.5</td>
<td>null</td>
<td>75.0</td>
<td>5459643.0</td>
<td>113.128</td>
<td>41.2</td>
<td>15.07</td>
<td>9.167</td>
<td>30155.152</td>
<td>0.7</td>
<td>287.959</td>
<td>7.29</td>
<td>23.1</td>
<td>37.7</td>
<td>null</td>
<td>5.82</td>
<td>77.54</td>
<td>0.855</td>
</tr>
<tr class="even">
<td>SVK</td>
<td>Europe</td>
<td>Slovakia</td>
<td>2020-03-20</td>
<td>137.0</td>
<td>14.0</td>
<td>15.0</td>
<td>1.0</td>
<td>0.0</td>
<td>0.143</td>
<td>25.093</td>
<td>2.564</td>
<td>2.747</td>
<td>0.183</td>
<td>0.0</td>
<td>2.6e-2</td>
<td>1.19</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>2807.0</td>
<td>368.0</td>
<td>0.514</td>
<td>6.7e-2</td>
<td>265.0</td>
<td>4.9e-2</td>
<td>5.7e-2</td>
<td>17.7</td>
<td>null</td>
<td>75.0</td>
<td>5459643.0</td>
<td>113.128</td>
<td>41.2</td>
<td>15.07</td>
<td>9.167</td>
<td>30155.152</td>
<td>0.7</td>
<td>287.959</td>
<td>7.29</td>
<td>23.1</td>
<td>37.7</td>
<td>null</td>
<td>5.82</td>
<td>77.54</td>
<td>0.855</td>
</tr>
<tr class="odd">
<td>SVK</td>
<td>Europe</td>
<td>Slovakia</td>
<td>2020-03-21</td>
<td>178.0</td>
<td>41.0</td>
<td>19.143</td>
<td>1.0</td>
<td>0.0</td>
<td>0.143</td>
<td>32.603</td>
<td>7.51</td>
<td>3.506</td>
<td>0.183</td>
<td>0.0</td>
<td>2.6e-2</td>
<td>1.19</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>3247.0</td>
<td>440.0</td>
<td>0.595</td>
<td>8.1e-2</td>
<td>300.0</td>
<td>5.5e-2</td>
<td>6.4e-2</td>
<td>15.7</td>
<td>null</td>
<td>75.0</td>
<td>5459643.0</td>
<td>113.128</td>
<td>41.2</td>
<td>15.07</td>
<td>9.167</td>
<td>30155.152</td>
<td>0.7</td>
<td>287.959</td>
<td>7.29</td>
<td>23.1</td>
<td>37.7</td>
<td>null</td>
<td>5.82</td>
<td>77.54</td>
<td>0.855</td>
</tr>
<tr class="even">
<td>SVK</td>
<td>Europe</td>
<td>Slovakia</td>
<td>2020-03-22</td>
<td>185.0</td>
<td>7.0</td>
<td>18.714</td>
<td>null</td>
<td>-1.0</td>
<td>0.0</td>
<td>33.885</td>
<td>1.282</td>
<td>3.428</td>
<td>null</td>
<td>-0.183</td>
<td>0.0</td>
<td>1.18</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>3489.0</td>
<td>242.0</td>
<td>0.639</td>
<td>4.4e-2</td>
<td>293.0</td>
<td>5.4e-2</td>
<td>6.4e-2</td>
<td>15.7</td>
<td>null</td>
<td>75.0</td>
<td>5459643.0</td>
<td>113.128</td>
<td>41.2</td>
<td>15.07</td>
<td>9.167</td>
<td>30155.152</td>
<td>0.7</td>
<td>287.959</td>
<td>7.29</td>
<td>23.1</td>
<td>37.7</td>
<td>null</td>
<td>5.82</td>
<td>77.54</td>
<td>0.855</td>
</tr>
</tbody>
</table>

</div>

</div>

<div class="cell markdown">

Correct new\_deaths correction back to 0

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
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

```

<div class="output execute_result plain_result" execution_count="1">

    df_fillNullForTotalDeathsSpecial: org.apache.spark.sql.DataFrame = [iso_code: string, continent: string ... 51 more fields]

</div>

</div>

<div class="cell markdown">

Expect to see an empty table, so correction is right

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val df_NULL_total_death_ = df_fillNullForTotalDeathsSpecial.select($"date",$"iso_code", $"total_cases", $"total_deaths_correct", $"new_cases", $"new_deaths_correct", $"new_cases_smoothed", $"new_deaths_smoothed_correct")
                          .filter($"total_deaths_correct".isNull)


df_NULL_total_death_.filter($"total_deaths_correct".isNull).groupBy("iso_code").count().except(df_NULL_total_death_.filter($"new_deaths_correct"===0).groupBy("iso_code").count()).show()
```

<div class="output execute_result plain_result" execution_count="1">

    +--------+-----+
    |iso_code|count|
    +--------+-----+
    +--------+-----+

    df_NULL_total_death_: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [date: string, iso_code: string ... 6 more fields]

</div>

</div>

<div class="cell markdown">

fill rest NULL value for total\_death.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val df_fillNullForTotalDeaths = df_fillNullForTotalDeathsSpecial
                                  .drop("new_deaths", "total_deaths", "new_deaths_smoothed") // drop old column to rename
                                  .withColumnRenamed("new_deaths_correct","new_deaths")
                                  .withColumnRenamed("total_deaths_correct","total_deaths")
                                  .withColumnRenamed("new_deaths_smoothed_correct","new_deaths_smoothed")
                                  .na.fill(0, Array("total_deaths"))
                                  .select(df.columns.head, df.columns.tail: _*)
display(df_fillNullForTotalDeaths)
```

</div>

<div class="cell markdown">

### All first 10 column is clean now!

(All code above is for illustration, for processing just run cell below )

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// filter unknow and HK data
val df_filteredLocation = df.filter($"iso_code"=!="HKG").filter($"iso_code".isNotNull)

// fill missing continent value for World data
val df_fillContinentNull = df_filteredLocation.na.fill("World",Array("continent")).cache
df_filteredLocation.unpersist()

// filter date before 2020-01-23
val df_filtered_date = df_fillContinentNull.filter($"date">"2020-01-22").cache
df_fillContinentNull.unpersist()

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

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.sql.functions._

for (c <- df_cleaned.columns) {
  println(c + ": " + df_cleaned.filter(col(c).isNull).count())
}
```

</div>

<div class="cell markdown">

### 3. select invariant (during pandemic) features for clustering

double check whether they are constant for each country, and if not, change all the value to mean and filter out countries that have missing constant features

Candidate list: - population - population*density - median*age - aged*65*older - aged*70*older - gdp*per*capita

-   cardiovasc*death*rate
-   diabetes\_prevalence
-   female\_smokers
-   male\_smokers
-   hospital*beds*per\_thousand
-   life\_expectancy
-   human*development*index

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val df_invariantFeatures = df_cleaned.select($"location", $"population",$"population_density",
                                             $"median_age", $"aged_65_older",
                                             $"aged_70_older",$"gdp_per_capita",
                                             $"cardiovasc_death_rate",$"diabetes_prevalence",
                                             $"female_smokers",$"male_smokers",$"hospital_beds_per_thousand",
                                             $"life_expectancy",$"human_development_index")
display(df_invariantFeatures)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(df_invariantFeatures.describe())
```

<div class="output execute_result tabular_result" execution_count="1">

<table>
<thead>
<tr class="header">
<th>summary</th>
<th>location</th>
<th>population</th>
<th>population_density</th>
<th>median_age</th>
<th>aged_65_older</th>
<th>aged_70_older</th>
<th>gdp_per_capita</th>
<th>cardiovasc_death_rate</th>
<th>diabetes_prevalence</th>
<th>female_smokers</th>
<th>male_smokers</th>
<th>hospital_beds_per_thousand</th>
<th>life_expectancy</th>
<th>human_development_index</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>count</td>
<td>60104</td>
<td>60104</td>
<td>58524</td>
<td>57260</td>
<td>56312</td>
<td>56944</td>
<td>57260</td>
<td>57892</td>
<td>58524</td>
<td>44928</td>
<td>44296</td>
<td>52835</td>
<td>59788</td>
<td>57576</td>
</tr>
<tr class="even">
<td>mean</td>
<td>null</td>
<td>8.179034692057101E7</td>
<td>305.56040665368073</td>
<td>30.204722319245512</td>
<td>8.595353423781793</td>
<td>5.418455904046091</td>
<td>18397.155660565862</td>
<td>262.00796225730636</td>
<td>7.89011004032537</td>
<td>10.368771011396003</td>
<td>32.645260520137235</td>
<td>3.000101788587119</td>
<td>72.8302766441427</td>
<td>0.7086279005141026</td>
</tr>
<tr class="odd">
<td>stddev</td>
<td>null</td>
<td>5.801702332960505E8</td>
<td>1534.259203474429</td>
<td>9.077112325166668</td>
<td>6.17972101589387</td>
<td>4.214972529727923</td>
<td>19409.896953039588</td>
<td>120.63832428074626</td>
<td>4.2077256136575105</td>
<td>10.396263307877218</td>
<td>13.558764723459655</td>
<td>2.438486660152889</td>
<td>7.538806415792373</td>
<td>0.15398160968293945</td>
</tr>
<tr class="even">
<td>min</td>
<td>Afghanistan</td>
<td>809.0</td>
<td>1.98</td>
<td>15.1</td>
<td>1.144</td>
<td>0.526</td>
<td>661.24</td>
<td>79.37</td>
<td>0.99</td>
<td>0.1</td>
<td>7.7</td>
<td>0.1</td>
<td>53.28</td>
<td>0.354</td>
</tr>
<tr class="odd">
<td>max</td>
<td>Zimbabwe</td>
<td>7.794798729E9</td>
<td>19347.5</td>
<td>48.2</td>
<td>27.049</td>
<td>18.493</td>
<td>116935.6</td>
<td>724.417</td>
<td>30.53</td>
<td>44.0</td>
<td>78.1</td>
<td>13.8</td>
<td>86.75</td>
<td>0.953</td>
</tr>
</tbody>
</table>

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
for (c <- df_invariantFeatures.columns) {
  println(c + ": " + df_invariantFeatures.filter(col(c).isNull).count())
}
```

<div class="output execute_result plain_result" execution_count="1">

    location: 0
    population: 0
    population_density: 1580
    median_age: 2844
    aged_65_older: 3792
    aged_70_older: 3160
    gdp_per_capita: 2844
    cardiovasc_death_rate: 2212
    diabetes_prevalence: 1580
    female_smokers: 15176
    male_smokers: 15808
    hospital_beds_per_thousand: 7269
    life_expectancy: 316
    human_development_index: 2528

</div>

</div>

<div class="cell markdown">

Although some countries seems like an outlier, it does have constant female*smokers and male*smokers

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val constant_feature_checker = df_cleaned.groupBy("location")
          .agg(
              //stddev("stringency_index").as("std_si"),         
              stddev("population").as("std_pop"),           
              stddev("population_density").as("std_pd"),
              stddev("median_age").as("std_ma"),         
              stddev("aged_65_older").as("std_a65"),           
              stddev("aged_70_older").as("std_a70"),  
              stddev("gdp_per_capita").as("std_gdp"),
              stddev("cardiovasc_death_rate").as("std_cdr"),         
              stddev("diabetes_prevalence").as("std_dp"),           
              stddev("female_smokers").as("std_fs"),      
              stddev("male_smokers").as("std_ms"),        
              stddev("hospital_beds_per_thousand").as("std_hbpt"),           
              stddev("life_expectancy").as("std_le"),
              stddev("human_development_index").as("std_hdi")
            )
           .where(
                  (col("std_pop") > 0) || (col("std_pd") > 1e-20) || (col("std_ma") > 0) || (col("std_a65") > 0) || (col("std_a70") > 0) || (col("std_gdp") > 0 ||
                   col("std_cdr") > 0) || (col("std_dp") > 0) || (col("std_fs") > 0) || (col("std_ms") > 0) || (col("std_hbpt") > 0) || (col("std_le") > 0) || (col("std_hdi") > 0))

display(constant_feature_checker)
```

</div>

<div class="cell markdown">

Each country have some constant features always

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val distinct_features = df_invariantFeatures.distinct()

display(distinct_features)
```

</div>

<div class="cell markdown">

In total, 126 countries have complete features

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val valid_distinct_features = distinct_features.filter($"population".isNotNull && $"population_density".isNotNull && $"median_age".isNotNull && 
                         $"aged_65_older".isNotNull && $"aged_70_older".isNotNull && $"gdp_per_capita".isNotNull &&
                         $"cardiovasc_death_rate".isNotNull && $"diabetes_prevalence".isNotNull && $"female_smokers".isNotNull && 
                         $"male_smokers".isNotNull && $"hospital_beds_per_thousand".isNotNull && $"life_expectancy".isNotNull &&
                         $"human_development_index".isNotNull)
display(valid_distinct_features)
```

</div>

<div class="cell markdown">

country list

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(valid_distinct_features.select($"location"))
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
valid_distinct_features.select($"location").count()
```

<div class="output execute_result plain_result" execution_count="1">

    res69: Long = 126

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val df_cleaned_feature = df_cleaned.filter($"location".isin(valid_distinct_features.select($"location").rdd.map(r => r(0)).collect().toSeq: _*))

display(df_cleaned_feature)
```

</div>

<div class="cell markdown">

### All data contains complete list of invariant time feature

(All code above is for illustration, for processing just run cell below )

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// select invariant features
val df_invariantFeatures = df_cleaned.select($"location", $"population",$"population_density",
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
                                 $"human_development_index".isNotNull).cache

df_invariantFeatures.unpersist()

// filter out NULL feature countries
val df_cleaned_feature = df_cleaned.filter($"location".isin(valid_distinct_features.select($"location").rdd.map(r => r(0)).collect().toSeq: _*)).cache

df_cleaned.unpersist()

display(df_cleaned_feature)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.sql.functions._

for (c <- df_cleaned_feature.columns) {
  println(c + ": " + df_cleaned_feature.filter(col(c).isNull).count())
}
```

</div>

<div class="cell markdown">

### 4. Imputing missing time series data of

-   total*cases*per\_million
-   new*cases*per\_million
-   new*cases*smoothed*per*million
-   total*deaths*per\_million
-   new*deaths*per\_million
-   new*deaths*smoothed*per*million

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val per_million_data = df_cleaned_feature.select($"location", $"date", $"iso_code", $"total_cases", 
                                                 $"total_deaths", $"new_cases", $"new_deaths", $"new_cases_smoothed", 
                                                 $"new_deaths_smoothed", $"population", $"population_density", 
                                                 $"total_cases_per_million", $"new_cases_per_million", $"new_cases_smoothed_per_million", 
                                                 $"total_deaths_per_million", $"new_deaths_per_million", $"new_deaths_smoothed_per_million")

display(per_million_data)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val per_million_data_corrected = per_million_data.withColumn("total_cases_per_million_correct", per_million_data("total_cases")/per_million_data("population")*1000000)
                                                 .withColumn("new_cases_per_million_correct", per_million_data("new_cases")/per_million_data("population")*1000000)
                                                 .withColumn("new_cases_smoothed_per_million_correct", per_million_data("new_cases_smoothed")/per_million_data("population")*1000000)
                                                 .withColumn("total_deaths_per_million_correct", per_million_data("total_deaths")/per_million_data("population")*1000000)
                                                 .withColumn("new_deaths_per_million_correct", per_million_data("new_deaths")/per_million_data("population")*1000000)
                                                 .withColumn("new_deaths_smoothed_per_million_correct", per_million_data("new_deaths_smoothed")/per_million_data("population")*1000000)
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

    per_million_data_corrected: org.apache.spark.sql.DataFrame = [location: string, date: string ... 15 more fields]

</div>

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

### 5. Impute time series of

-   reproduction\_rate
-   total\_tests
-   stringency\_index
-   total*tests*per\_thousand

</div>

<div class="cell markdown">

fill null in reproduction\_rate by last available and next available value

All countries has missing data at beginning or in the end

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(df_cleaned_feature_permillion.select($"reproduction_rate", $"location", $"date").filter($"reproduction_rate".isNull).groupBy("location").count().sort("location"))
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(df_cleaned_feature_permillion.select($"reproduction_rate", $"location", $"date").filter($"reproduction_rate".isNull).groupBy("location").agg(max("date").as("max_date"), min("date").as("min_date")).sort("location"))
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(df_cleaned_feature_permillion.select($"reproduction_rate", $"location", $"date").filter($"location"==="Albania"))
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window

val df_cleaned_reproduction_rate= df_cleaned_feature_permillion.select($"reproduction_rate", $"location", $"date")
                              .withColumn("reproduction_rate", last("reproduction_rate", true)
                                         .over(Window.partitionBy("location").orderBy("date").rowsBetween(-df_cleaned_feature_permillion.count(), 0)))
                              .withColumn("reproduction_rate", first("reproduction_rate", true)
                                         .over(Window.partitionBy("location").orderBy("date").rowsBetween(0, df_cleaned_feature_permillion.count())))
                              .na.fill(0, Array("reproduction_rate"))
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.sql.functions._
    import org.apache.spark.sql.expressions.Window
    df_cleaned_reproduction_rate: org.apache.spark.sql.DataFrame = [reproduction_rate: double, location: string ... 1 more field]

</div>

</div>

<div class="cell markdown">

countries miss stringency\_index value

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(df_cleaned_feature_permillion.select($"stringency_index", $"location", $"date").filter($"stringency_index".isNull).groupBy("location").count().sort("count"))
```

</div>

<div class="cell markdown">

start and end date for null value of stringency\_index for each country

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(df_cleaned_feature_permillion.select($"stringency_index", $"location", $"date").filter($"stringency_index".isNull).groupBy("location").agg(max("date").as("max_date"), min("date").as("min_date")))
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window

val df_cleaned_stringency = df_cleaned_feature_permillion.select($"stringency_index", $"location", $"date")
                              .withColumn("stringency_index_corect", last("stringency_index", true)
                                         .over(Window.partitionBy("location").orderBy("date").rowsBetween(-df_cleaned_feature_permillion.count(), 0)))
display(df_cleaned_stringency.filter($"stringency_index".isNull).filter($"stringency_index_corect".isNull).groupBy("location").count())
```

<div class="output execute_result tabular_result" execution_count="1">

<table>
<thead>
<tr class="header">
<th>location</th>
<th>count</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Comoros</td>
<td>316.0</td>
</tr>
<tr class="even">
<td>Bahamas</td>
<td>316.0</td>
</tr>
<tr class="odd">
<td>Malta</td>
<td>316.0</td>
</tr>
<tr class="even">
<td>Montenegro</td>
<td>316.0</td>
</tr>
<tr class="odd">
<td>Armenia</td>
<td>316.0</td>
</tr>
</tbody>
</table>

</div>

</div>

<div class="cell markdown">

total\_tests, impute by last available or next available value

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window

val df_cleaned_total_cases = df_cleaned_feature_permillion.select($"total_tests", $"location", $"date")
                              .withColumn("total_tests", last("total_tests", true)
                                         .over(Window.partitionBy("location").orderBy("date").rowsBetween(-df_cleaned_feature_permillion.count(), 0)))
                              .withColumn("total_tests", first("total_tests", true)
                                         .over(Window.partitionBy("location").orderBy("date").rowsBetween(0, df_cleaned_feature_permillion.count())))
                              .na.fill(0, Array("total_tests"))
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.sql.functions._
    import org.apache.spark.sql.expressions.Window
    df_cleaned_total_cases: org.apache.spark.sql.DataFrame = [total_tests: double, location: string ... 1 more field]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(df_cleaned_feature_permillion.select($"total_tests", $"location", $"date").filter($"total_tests".isNull).groupBy("location").count())
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val total_tests_date_maxmin = df_cleaned_feature_permillion.select($"total_tests", $"location", $"date").filter($"total_tests".isNull).groupBy("location").agg(max("date").as("max_date"), min("date").as("min_date"))
display(total_tests_date_maxmin)
```

</div>

<div class="cell markdown">

process stringency*index, reproduction*rate, total*tests, total*tests*per*thousand

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window

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
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.sql.functions._
    import org.apache.spark.sql.expressions.Window
    df_cleaned_time_series: org.apache.spark.sql.DataFrame = [iso_code: string, continent: string ... 48 more fields]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(df_cleaned_time_series)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.sql.functions._

for (c <- df_cleaned_time_series.columns) {
  println(c + ": " + df_cleaned_time_series.filter(col(c).isNull).count())
}
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>
