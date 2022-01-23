<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Stream to parquet file
======================

This notebook allows for setup and execution of the data streaming and querying into a parquet file. The idea is thereafter to perform analysis on the parquet file.

Note that this notebooks assumes one has already has downloaded several "Our World in Data" dataset csv files. This can be done by first running "DownloadFilesPeriodicallyScript" at least once.

Content is based on "038\_StructuredStreamingProgGuide" by Raazesh Sainudiin.

</div>

<div class="cell markdown">

start by copying latest downloaded csv data to data analysis folder

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
dbutils.fs.cp("file:///databricks/driver/projects/group12/logsEveryXSecs/","/datasets/group12/",true)
```

<div class="output execute_result plain_result" execution_count="1">

    res0: Boolean = true

</div>

</div>

<div class="cell markdown">

check that data is in the group12 folder

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

check the schema for the csv files.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val df_csv = spark.read.format("csv").option("header", "true").option("inferSchema", "true").csv("/datasets/group12/21_01_07_09_05_33.csv")
```

<div class="output execute_result plain_result" execution_count="1">

    df_csv: org.apache.spark.sql.DataFrame = [iso_code: string, continent: string ... 52 more fields]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
df_csv.printSchema
```

</div>

<div class="cell markdown">

The stream requires a user defined schema. Note that the January 2021 schema is different compared to the December 2020 schema. Below, the user defined schemas are created.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.sql.types._

val OurWorldinDataSchema2021 = new StructType()                      
                      .add("iso_code", "string")
                      .add("continent", "string")
                      .add("location", "string")
                      .add("date", "string")
                      .add("total_cases","double")
                      .add("new_cases","double")
                      .add("new_cases_smoothed","double")
                      .add("total_deaths","double")
                      .add("new_deaths","double")
                      .add("new_deaths_smoothed","double")
                      .add("total_cases_per_million","double")
                      .add("new_cases_per_million","double")
                      .add("new_cases_smoothed_per_million","double")
                      .add("total_deaths_per_million","double")
                      .add("new_deaths_per_million","double")
                      .add("new_deaths_smoothed_per_million","double")
                      .add("reproduction_rate", "double")
                      .add("icu_patients", "double")
                      .add("icu_patients_per_million", "double")
                      .add("hosp_patients", "double")
                      .add("hosp_patients_per_million", "double")
                      .add("weekly_icu_admissions", "double")
                      .add("weekly_icu_admissions_per_million", "double")
                      .add("weekly_hosp_admissions", "double")
                      .add("weekly_hosp_admissions_per_million", "double")
                      .add("new_tests", "double")
                      .add("total_tests", "double")
                      .add("total_tests_per_thousand", "double")
                      .add("new_tests_per_thousand", "double")
                      .add("new_tests_smoothed", "double")
                      .add("new_tests_smoothed_per_thousand", "double")
                      .add("positive_rate", "double")
                      .add("tests_per_case", "double")
                      .add("tests_units", "double")
                      .add("total_vaccinations", "double")
                      .add("new_vaccinations", "double")
                      .add("stringency_index","double")
                      .add("population","double")
                      .add("population_density","double")
                      .add("median_age", "double")
                      .add("aged_65_older", "double")
                      .add("aged_70_older", "double")
                      .add("gdp_per_capita","double")
                      .add("extreme_poverty","double")
                      .add("cardiovasc_death_rate","double")
                      .add("diabetes_prevalence","double")
                      .add("female_smokers", "double")
                      .add("male_smokers", "double")
                      .add("handwashing_facilities", "double")
                      .add("hospital_beds_per_thousand", "double")
                      .add("life_expectancy","double")
                      .add("human_development_index","double")

val OurWorldinDataSchema2020 = new StructType()                      
                      .add("iso_code", "string")
                      .add("continent", "string")
                      .add("location", "string")
                      .add("date", "string")
                      .add("total_cases","double")
                      .add("new_cases","double")
                      .add("new_cases_smoothed","double")
                      .add("total_deaths","double")
                      .add("new_deaths","double")
                      .add("new_deaths_smoothed","double")
                      .add("total_cases_per_million","double")
                      .add("new_cases_per_million","double")
                      .add("new_cases_smoothed_per_million","double")
                      .add("total_deaths_per_million","double")
                      .add("new_deaths_per_million","double")
                      .add("new_deaths_smoothed_per_million","double")
                      .add("reproduction_rate", "double")
                      .add("icu_patients", "double")
                      .add("icu_patients_per_million", "double")
                      .add("hosp_patients", "double")
                      .add("hosp_patients_per_million", "double")
                      .add("weekly_icu_admissions", "double")
                      .add("weekly_icu_admissions_per_million", "double")
                      .add("weekly_hosp_admissions", "double")
                      .add("weekly_hosp_admissions_per_million", "double")
                      .add("total_tests", "double")
                      .add("new_tests", "double")
                      .add("total_tests_per_thousand", "double")
                      .add("new_tests_per_thousand", "double")
                      .add("new_tests_smoothed", "double")
                      .add("new_tests_smoothed_per_thousand", "double")
                      .add("tests_per_case", "double")
                      .add("positive_rate", "double")
                      .add("tests_units", "double")
                      .add("stringency_index","double")
                      .add("population","double")
                      .add("population_density","double")
                      .add("median_age", "double")
                      .add("aged_65_older", "double")
                      .add("aged_70_older", "double")
                      .add("gdp_per_capita","double")
                      .add("extreme_poverty","double")
                      .add("cardiovasc_death_rate","double")
                      .add("diabetes_prevalence","double")
                      .add("female_smokers", "double")
                      .add("male_smokers", "double")
                      .add("handwashing_facilities", "double")
                      .add("hospital_beds_per_thousand", "double")
                      .add("life_expectancy","double")
                      .add("human_development_index","double")
```

</div>

<div class="cell markdown">

### Start stream

In January 2021, the schema was updated compared to the schema in December 2020. Below, one can choose which type of csv files to stream below.

</div>

<div class="cell markdown">

Stream for 2020

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.sql.types._

val OurWorldinDataStream = spark
  .readStream
  .schema(OurWorldinDataSchema2020) 
  .option("MaxFilesPerTrigger", 1)
  .option("latestFirst", "true")
  .format("csv")
  .option("header", "true")
  .load("/datasets/group12/20*.csv")
  .dropDuplicates()
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.sql.types._
    OurWorldinDataStream: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [iso_code: string, continent: string ... 48 more fields]

</div>

</div>

<div class="cell markdown">

Stream for 2021

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.sql.types._

val OurWorldinDataStream2021 = spark
  .readStream
  .schema(OurWorldinDataSchema2021) 
  .option("MaxFilesPerTrigger", 1)
  .option("latestFirst", "true")
  .format("csv")
  .option("header", "true")
  .load("/datasets/group12/21*.csv")
  .dropDuplicates()
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.sql.types._
    OurWorldinDataStream2021: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [iso_code: string, continent: string ... 50 more fields]

</div>

</div>

<div class="cell markdown">

display stream 2020

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
OurWorldinDataStream.isStreaming
```

<div class="output execute_result plain_result" execution_count="1">

    res81: Boolean = true

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(OurWorldinDataStream) 
```

</div>

<div class="cell markdown">

### Query to File (2020)

query that saves file into a parquet file at periodic intervalls. Analysis will thereafter be performed on the parquet file

</div>

<div class="cell markdown">

create folders for parquet file and checkpoint data

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// remove any previous folders if exists
dbutils.fs.rm("datasets/group12/chkpoint",recurse=true)
dbutils.fs.rm("datasets/group12/analysis",recurse=true)
```

<div class="output execute_result plain_result" execution_count="1">

    res14: Boolean = true

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
dbutils.fs.mkdirs("datasets/group12/chkpoint")
```

<div class="output execute_result plain_result" execution_count="1">

    res15: Boolean = true

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
dbutils.fs.mkdirs("/datasets/group12/analysis")
```

<div class="output execute_result plain_result" execution_count="1">

    res16: Boolean = true

</div>

</div>

<div class="cell markdown">

initialize query to store data in parquet files based on column selection

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger

val query = OurWorldinDataStream
                 .select($"iso_code", $"continent", $"location", $"date", $"total_cases", $"new_cases", $"new_cases_smoothed", $"total_deaths", $"new_deaths",$"new_deaths_smoothed", $"total_cases_per_million", $"new_cases_per_million", $"new_cases_smoothed_per_million", $"total_deaths_per_million", $"new_deaths_per_million", $"new_deaths_smoothed_per_million", $"reproduction_rate", $"icu_patients", $"icu_patients_per_million", $"hosp_patients", $"hosp_patients_per_million", $"weekly_icu_admissions", $"weekly_icu_admissions_per_million", $"weekly_hosp_admissions", $"weekly_hosp_admissions_per_million", $"total_tests",$"new_tests", $"total_tests_per_thousand", $"new_tests_per_thousand", $"new_tests_smoothed",$"new_tests_smoothed_per_thousand", $"tests_per_case", $"positive_rate", $"tests_units", $"stringency_index", $"population", $"population_density", $"median_age", $"aged_65_older", $"aged_70_older", $"gdp_per_capita", $"extreme_poverty", $"cardiovasc_death_rate", $"diabetes_prevalence", $"female_smokers", $"male_smokers", $"handwashing_facilities", $"hospital_beds_per_thousand", $"life_expectancy", $"human_development_index")
                 .writeStream
                 //.trigger(Trigger.ProcessingTime("20 seconds")) // debugging
                 .trigger(Trigger.ProcessingTime("216000 seconds")) // for each day
                 .option("checkpointLocation", "/datasets/group12/chkpoint")
                 .format("parquet")  
                 .option("path", "/datasets/group12/analysis")
                 .start()
                 
query.awaitTermination() // hit cancel to terminate
```

</div>

<div class="cell markdown">

check saved parquet file contents

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(dbutils.fs.ls("/datasets/group12/analysis"))
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val parquetFileDF = spark.read.parquet("dbfs:/datasets/group12/analysis/*.parquet")
```

<div class="output execute_result plain_result" execution_count="1">

    parquetFileDF: org.apache.spark.sql.DataFrame = [iso_code: string, continent: string ... 48 more fields]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(parquetFileDF.describe())
```

<div class="output execute_result tabular_result" execution_count="1">

<table>
<thead>
<tr class="header">
<th>summary</th>
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
<td>count</td>
<td>62184</td>
<td>61867</td>
<td>62500</td>
<td>62500</td>
<td>53761</td>
<td>62377</td>
<td>61421</td>
<td>45901</td>
<td>62377</td>
<td>61421</td>
<td>53460</td>
<td>62061</td>
<td>61110</td>
<td>45613</td>
<td>62061</td>
<td>61110</td>
<td>41680</td>
<td>5582</td>
<td>5582</td>
<td>6730</td>
<td>6730</td>
<td>538</td>
<td>538</td>
<td>885</td>
<td>885</td>
<td>25647</td>
<td>25606</td>
<td>25647</td>
<td>25606</td>
<td>28408</td>
<td>28408</td>
<td>26927</td>
<td>26336</td>
<td>0</td>
<td>54546</td>
<td>62184</td>
<td>60592</td>
<td>59340</td>
<td>58383</td>
<td>59024</td>
<td>59329</td>
<td>40812</td>
<td>59902</td>
<td>60595</td>
<td>46712</td>
<td>46080</td>
<td>30696</td>
<td>54818</td>
<td>61868</td>
<td>59646</td>
</tr>
<tr class="even">
<td>mean</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>235416.1718904038</td>
<td>2397.8407425814003</td>
<td>2367.056380553872</td>
<td>8908.311256835363</td>
<td>55.19233050643667</td>
<td>54.80704391006325</td>
<td>3798.2407507482267</td>
<td>43.13754119334201</td>
<td>42.41964747177225</td>
<td>108.27332865630407</td>
<td>0.761575578865955</td>
<td>0.7475491572574053</td>
<td>1.049197216890596</td>
<td>1171.3697599426728</td>
<td>18.08625438910784</td>
<td>5656.88558692422</td>
<td>104.44401604754825</td>
<td>320.92</td>
<td>10.748325278810405</td>
<td>2961.1632870056505</td>
<td>81.23565310734462</td>
<td>3233707.097438297</td>
<td>37267.77532609545</td>
<td>103.08093254571695</td>
<td>1.1276385222213543</td>
<td>35891.313679245286</td>
<td>1.0969730005632223</td>
<td>0.07456953986704766</td>
<td>171.59424362089905</td>
<td>null</td>
<td>53.312537674623144</td>
<td>8.17052256214782E7</td>
<td>307.3537588625577</td>
<td>30.42868217054277</td>
<td>8.773048729938651</td>
<td>5.539936330984043</td>
<td>19213.552059313115</td>
<td>13.465191610310459</td>
<td>258.8075386297629</td>
<td>7.891889759881105</td>
<td>10.66726918136675</td>
<td>32.47178591579883</td>
<td>50.99304310007871</td>
<td>3.0230664197891057</td>
<td>73.0352230555375</td>
<td>0.7137129396774315</td>
</tr>
<tr class="odd">
<td>stddev</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>2063317.939351184</td>
<td>20722.406766273503</td>
<td>20188.721091973344</td>
<td>62835.10460453239</td>
<td>420.0315418848828</td>
<td>404.7550597418351</td>
<td>7537.842859063191</td>
<td>144.63668342422923</td>
<td>115.82591210719889</td>
<td>201.80463357477137</td>
<td>3.003047037105768</td>
<td>2.17917077365699</td>
<td>0.38591731058680295</td>
<td>2792.1448587365685</td>
<td>24.026044243287902</td>
<td>13218.496002944645</td>
<td>149.63443300155708</td>
<td>596.5733008531096</td>
<td>25.857485153686934</td>
<td>6410.029575144745</td>
<td>233.24453357627317</td>
<td>1.45235647300503E7</td>
<td>144743.48405262225</td>
<td>209.81522035476976</td>
<td>2.1450449484564924</td>
<td>134865.99995119465</td>
<td>1.9561605087188363</td>
<td>0.09616514922644583</td>
<td>861.1206108534275</td>
<td>null</td>
<td>27.591030251926593</td>
<td>5.716608354719421E8</td>
<td>1525.663259783739</td>
<td>9.09237871864885</td>
<td>6.230404954288745</td>
<td>4.248965298848258</td>
<td>20089.06946024988</td>
<td>19.988201128228035</td>
<td>120.99438607360678</td>
<td>4.172554929946806</td>
<td>10.429062017422085</td>
<td>13.415141793587788</td>
<td>31.83056463184429</td>
<td>2.4310237026014865</td>
<td>7.529274919111957</td>
<td>0.15483301083721898</td>
</tr>
<tr class="even">
<td>min</td>
<td>AFG</td>
<td>Africa</td>
<td>Afghanistan</td>
<td>2020-01-01</td>
<td>1.0</td>
<td>-10034.0</td>
<td>-525.0</td>
<td>1.0</td>
<td>-1918.0</td>
<td>-232.143</td>
<td>0.001</td>
<td>-2153.437</td>
<td>-276.825</td>
<td>0.001</td>
<td>-76.445</td>
<td>-10.921</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>1.0</td>
<td>-3743.0</td>
<td>0.0</td>
<td>-0.398</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>1.4</td>
<td>null</td>
<td>0.0</td>
<td>809.0</td>
<td>1.98</td>
<td>15.1</td>
<td>1.144</td>
<td>0.526</td>
<td>661.24</td>
<td>0.1</td>
<td>79.37</td>
<td>0.99</td>
<td>0.1</td>
<td>7.7</td>
<td>1.188</td>
<td>0.1</td>
<td>53.28</td>
<td>0.354</td>
</tr>
<tr class="odd">
<td>max</td>
<td>ZWE</td>
<td>South America</td>
<td>Zimbabwe</td>
<td>2020-12-03</td>
<td>6.5220557E7</td>
<td>690127.0</td>
<td>600775.429</td>
<td>1506251.0</td>
<td>12825.0</td>
<td>10538.571</td>
<td>89354.818</td>
<td>8652.658</td>
<td>2648.773</td>
<td>1469.678</td>
<td>218.329</td>
<td>63.14</td>
<td>6.68</td>
<td>19396.0</td>
<td>127.183</td>
<td>100226.0</td>
<td>988.567</td>
<td>4375.407</td>
<td>190.051</td>
<td>50887.393</td>
<td>2645.194</td>
<td>1.92122147E8</td>
<td>1949384.0</td>
<td>2248.144</td>
<td>26.154</td>
<td>1703410.0</td>
<td>19.244</td>
<td>0.729</td>
<td>44258.7</td>
<td>null</td>
<td>100.0</td>
<td>7.794798729E9</td>
<td>19347.5</td>
<td>48.2</td>
<td>27.049</td>
<td>18.493</td>
<td>116935.6</td>
<td>77.6</td>
<td>724.417</td>
<td>30.53</td>
<td>44.0</td>
<td>78.1</td>
<td>98.999</td>
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
display(parquetFileDF.orderBy($"date".desc))
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
parquetFileDF.count()
```

<div class="output execute_result plain_result" execution_count="1">

    res5: Long = 62500

</div>

</div>

<div class="cell markdown">

### Query to File (2021)

query that saves file into a parquet file at periodic intervalls.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// remove any previous folders if exists
dbutils.fs.rm("datasets/group12/chkpoint2021",recurse=true)
dbutils.fs.rm("datasets/group12/analysis2021",recurse=true)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
dbutils.fs.mkdirs("datasets/group12/chkpoint2021")
dbutils.fs.mkdirs("datasets/group12/analysis2021")
```

<div class="output execute_result plain_result" execution_count="1">

    res18: Boolean = true

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger

val query = OurWorldinDataStream2021
                 .select($"iso_code", $"continent", $"location", $"date", $"total_cases", $"new_cases", $"new_cases_smoothed", $"total_deaths", $"new_deaths",$"new_deaths_smoothed", $"total_cases_per_million", $"new_cases_per_million", $"new_cases_smoothed_per_million", $"total_deaths_per_million", $"new_deaths_per_million", $"new_deaths_smoothed_per_million", $"reproduction_rate", $"icu_patients", $"icu_patients_per_million", $"hosp_patients", $"hosp_patients_per_million", $"weekly_icu_admissions", $"weekly_icu_admissions_per_million", $"weekly_hosp_admissions", $"weekly_hosp_admissions_per_million", $"total_tests",$"new_tests", $"total_tests_per_thousand", $"new_tests_per_thousand", $"new_tests_smoothed",$"new_tests_smoothed_per_thousand", $"tests_per_case", $"positive_rate", $"tests_units", $"stringency_index", $"population", $"population_density", $"median_age", $"aged_65_older", $"aged_70_older", $"gdp_per_capita", $"extreme_poverty", $"cardiovasc_death_rate", $"diabetes_prevalence", $"female_smokers", $"male_smokers", $"handwashing_facilities", $"hospital_beds_per_thousand", $"life_expectancy", $"human_development_index")
                 .writeStream
                 //.trigger(Trigger.ProcessingTime("20 seconds")) // debugging
                 .trigger(Trigger.ProcessingTime("216000 seconds")) // each day
                 .option("checkpointLocation", "/datasets/group12/chkpoint2021")
                 .format("parquet")  
                 .option("path", "/datasets/group12/analysis2021")
                 .start()
                 
query.awaitTermination() // hit cancel to terminate
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val parquetFile2021DF = spark.read.parquet("dbfs:/datasets/group12/analysis2021/*.parquet")
```

<div class="output execute_result plain_result" execution_count="1">

    parquetFile2021DF: org.apache.spark.sql.DataFrame = [iso_code: string, continent: string ... 48 more fields]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(parquetFile2021DF.describe())
```

<div class="output execute_result tabular_result" execution_count="1">

<table>
<thead>
<tr class="header">
<th>summary</th>
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
<td>count</td>
<td>58196</td>
<td>57845</td>
<td>58531</td>
<td>58531</td>
<td>58008</td>
<td>58001</td>
<td>57046</td>
<td>49657</td>
<td>49656</td>
<td>57046</td>
<td>57673</td>
<td>57666</td>
<td>56716</td>
<td>49335</td>
<td>49334</td>
<td>56716</td>
<td>44995</td>
<td>6086</td>
<td>6086</td>
<td>6845</td>
<td>6845</td>
<td>544</td>
<td>544</td>
<td>878</td>
<td>878</td>
<td>27006</td>
<td>27145</td>
<td>27006</td>
<td>27145</td>
<td>30478</td>
<td>30478</td>
<td>28331</td>
<td>28797</td>
<td>0</td>
<td>241</td>
<td>278</td>
<td>52358</td>
<td>58196</td>
<td>56964</td>
<td>55692</td>
<td>55044</td>
<td>55376</td>
<td>55710</td>
<td>38159</td>
<td>56294</td>
<td>56942</td>
<td>44455</td>
<td>43846</td>
<td>28176</td>
<td>51737</td>
</tr>
<tr class="even">
<td>mean</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>280856.96293614677</td>
<td>2977.974862502371</td>
<td>2957.9528833748072</td>
<td>9440.825059910989</td>
<td>75.87175769292735</td>
<td>64.76055386880755</td>
<td>4803.329924592101</td>
<td>55.43429124614151</td>
<td>54.92539389237602</td>
<td>123.92782108036903</td>
<td>1.1725306887744764</td>
<td>0.9930127300938001</td>
<td>1.0397301922435842</td>
<td>924.7643772592836</td>
<td>17.942194544857045</td>
<td>4767.396055514974</td>
<td>112.35477647918188</td>
<td>389.18459375</td>
<td>23.096417279411764</td>
<td>2770.236054669703</td>
<td>90.94211161731207</td>
<td>3296570.453047471</td>
<td>33334.96798673789</td>
<td>113.1671816263053</td>
<td>1.1168154724627004</td>
<td>32438.389395629634</td>
<td>1.0890064636787196</td>
<td>166.36620662878127</td>
<td>0.08254411917908085</td>
<td>null</td>
<td>0.9067634854771786</td>
<td>849.6284532374101</td>
<td>59.32707265365355</td>
<td>9.113860612012853E7</td>
<td>323.8082390457141</td>
<td>30.631115779645334</td>
<td>8.842210231814642</td>
<td>5.6059546915631415</td>
<td>19206.748390809662</td>
<td>13.14487276920222</td>
<td>256.0564316090542</td>
<td>7.763601383864184</td>
<td>10.57440184456196</td>
<td>32.64338559959862</td>
<td>51.16526313174358</td>
<td>3.042046311150612</td>
</tr>
<tr class="odd">
<td>stddev</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>null</td>
<td>2748559.5139726754</td>
<td>26447.176041635634</td>
<td>25947.18346899017</td>
<td>74852.02463395565</td>
<td>554.179232738698</td>
<td>496.3241880037069</td>
<td>9770.196467368594</td>
<td>159.8367692227782</td>
<td>130.23859384700643</td>
<td>231.3831543436838</td>
<td>3.814136613643414</td>
<td>2.592627124113058</td>
<td>0.37314466501106797</td>
<td>2607.447792980141</td>
<td>23.096588581017723</td>
<td>13142.595210484165</td>
<td>162.19963589630862</td>
<td>1062.8669629409615</td>
<td>84.43280553001314</td>
<td>6258.389649510214</td>
<td>237.96388901304604</td>
<td>1.5192379103534836E7</td>
<td>135667.97569510888</td>
<td>232.08417747765577</td>
<td>2.101753078690164</td>
<td>126446.58519497044</td>
<td>1.9266264256088346</td>
<td>842.7776724543986</td>
<td>0.09966680167946733</td>
<td>null</td>
<td>2.447811842168661</td>
<td>2276.8853139301373</td>
<td>22.403193236864983</td>
<td>6.204846769231012E8</td>
<td>1577.4914969226447</td>
<td>9.117030300574706</td>
<td>6.256325795927324</td>
<td>4.27334121579429</td>
<td>19680.321466247937</td>
<td>19.8684806478708</td>
<td>117.99197986122954</td>
<td>3.878859778205247</td>
<td>10.419107338482048</td>
<td>13.45549336530331</td>
<td>31.775955000809844</td>
<td>2.473749904640983</td>
</tr>
<tr class="even">
<td>min</td>
<td>AFG</td>
<td>Africa</td>
<td>Afghanistan</td>
<td>2020-01-01</td>
<td>1.0</td>
<td>-46076.0</td>
<td>-1121.714</td>
<td>1.0</td>
<td>-1918.0</td>
<td>-232.143</td>
<td>0.001</td>
<td>-2153.437</td>
<td>-276.825</td>
<td>0.001</td>
<td>-76.445</td>
<td>-10.921</td>
<td>-0.01</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>1.0</td>
<td>-47510.0</td>
<td>0.0</td>
<td>-0.786</td>
<td>0.0</td>
<td>0.0</td>
<td>1.6</td>
<td>0.0</td>
<td>null</td>
<td>0.0</td>
<td>1.57</td>
<td>0.0</td>
<td>809.0</td>
<td>1.98</td>
<td>15.1</td>
<td>1.144</td>
<td>0.526</td>
<td>661.24</td>
<td>0.1</td>
<td>79.37</td>
<td>0.99</td>
<td>0.1</td>
<td>7.7</td>
<td>1.188</td>
<td>0.1</td>
</tr>
<tr class="odd">
<td>max</td>
<td>ZWE</td>
<td>South America</td>
<td>Zimbabwe</td>
<td>2021-01-06</td>
<td>8.718654E7</td>
<td>780613.0</td>
<td>646630.286</td>
<td>1883761.0</td>
<td>15525.0</td>
<td>11670.429</td>
<td>108043.746</td>
<td>8652.658</td>
<td>2648.773</td>
<td>1826.861</td>
<td>218.329</td>
<td>63.14</td>
<td>6.74</td>
<td>23707.0</td>
<td>127.183</td>
<td>132476.0</td>
<td>922.018</td>
<td>10301.189</td>
<td>888.829</td>
<td>50887.393</td>
<td>2645.194</td>
<td>2.4798603E8</td>
<td>2133928.0</td>
<td>2705.599</td>
<td>28.716</td>
<td>1840248.0</td>
<td>23.701</td>
<td>44258.7</td>
<td>0.636</td>
<td>null</td>
<td>17.14</td>
<td>14497.77</td>
<td>100.0</td>
<td>7.794798729E9</td>
<td>19347.5</td>
<td>48.2</td>
<td>27.049</td>
<td>18.493</td>
<td>116935.6</td>
<td>77.6</td>
<td>724.417</td>
<td>30.53</td>
<td>44.0</td>
<td>78.1</td>
<td>98.999</td>
<td>13.8</td>
</tr>
</tbody>
</table>

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>
