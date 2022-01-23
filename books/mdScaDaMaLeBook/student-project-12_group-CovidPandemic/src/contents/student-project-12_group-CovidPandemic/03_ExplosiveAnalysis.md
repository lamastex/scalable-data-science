<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

### This notebook is for explosive analysis of features in data.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` run
"./DataPreprocess"
```

</div>

<div class="cell markdown">

### Statistics of invariant features

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(valid_distinct_features.describe())
```

<div class="output execute_result tabular_result" execution_count="1">

<table>
<thead>
<tr class="header">
<th>summary</th>
<th>iso_code</th>
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
<td>126</td>
<td>126</td>
<td>126</td>
<td>126</td>
<td>126</td>
<td>126</td>
<td>126</td>
<td>126</td>
<td>126</td>
<td>126</td>
<td>126</td>
<td>126</td>
<td>126</td>
<td>126</td>
<td>126</td>
</tr>
<tr class="even">
<td>mean</td>
<td>null</td>
<td>null</td>
<td>5.448349223809524E7</td>
<td>227.40285714285716</td>
<td>32.72619047619046</td>
<td>10.100476190476186</td>
<td>6.477539682539681</td>
<td>22517.798674603175</td>
<td>249.51723809523807</td>
<td>7.5796031746031725</td>
<td>10.470634920634918</td>
<td>32.03650793650794</td>
<td>3.1610476190476198</td>
<td>74.50119047619042</td>
<td>0.7466904761904759</td>
</tr>
<tr class="odd">
<td>stddev</td>
<td>null</td>
<td>null</td>
<td>1.8094803349108434E8</td>
<td>737.9315775155493</td>
<td>8.810413643605422</td>
<td>6.507230389299932</td>
<td>4.50202071901193</td>
<td>21194.388486506883</td>
<td>120.66734039269643</td>
<td>3.8247238647083845</td>
<td>10.346516843539705</td>
<td>13.459477571879843</td>
<td>2.4548683837864473</td>
<td>6.634961896757854</td>
<td>0.1496665875490299</td>
</tr>
<tr class="even">
<td>min</td>
<td>ALB</td>
<td>Albania</td>
<td>98340.0</td>
<td>1.98</td>
<td>15.1</td>
<td>1.144</td>
<td>0.526</td>
<td>752.788</td>
<td>79.37</td>
<td>0.99</td>
<td>0.1</td>
<td>7.7</td>
<td>0.1</td>
<td>59.31</td>
<td>0.354</td>
</tr>
<tr class="odd">
<td>max</td>
<td>ZWE</td>
<td>Zimbabwe</td>
<td>1.439323774E9</td>
<td>7915.731</td>
<td>48.2</td>
<td>27.049</td>
<td>18.493</td>
<td>116935.6</td>
<td>724.417</td>
<td>22.02</td>
<td>44.0</td>
<td>78.1</td>
<td>13.05</td>
<td>84.63</td>
<td>0.953</td>
</tr>
</tbody>
</table>

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(valid_distinct_features.select($"iso_code", $"population"))
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_03_1.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(valid_distinct_features.select($"iso_code", $"population_density"))
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_03_2.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(valid_distinct_features.select($"iso_code", $"median_age"))
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_03_3.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(valid_distinct_features.select($"iso_code", $"aged_65_older"))
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_03_4.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(valid_distinct_features.select($"iso_code", $"aged_70_older"))
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_03_5.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(valid_distinct_features.select($"iso_code", $"gdp_per_capita"))
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_03_6.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(valid_distinct_features.select($"iso_code", $"cardiovasc_death_rate"))
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_03_7.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(valid_distinct_features.select($"iso_code", $"diabetes_prevalence"))
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_03_8.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(valid_distinct_features.select($"iso_code", $"female_smokers"))
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_03_9.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(valid_distinct_features.select($"iso_code", $"male_smokers"))
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_03_10.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(valid_distinct_features.select($"iso_code", $"hospital_beds_per_thousand"))
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_03_11.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(valid_distinct_features.select($"iso_code", $"life_expectancy"))
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_03_12.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(valid_distinct_features.select($"iso_code", $"human_development_index"))
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_03_13.JPG?raw=true)

</div>

<div class="cell markdown">

### Correlation between invariant features

There are some pairs of features are highly correlated i.e. 1. median*age, aged*65*older 2. median*age, human*development*index 3. median*age, life*expectancy 4. gdp*per*capita, human*development*index 5. gdp*per*capita, life*expectancy 6. human*development*index, life*expectancy

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(valid_distinct_features.drop("iso_code","location"))
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_03_14.png?raw=true)

</div>

<div class="cell markdown">

### Correlation between new case per million, total case, new death per million, total death per million, reproduction rate and stringency index.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(df_cleaned_time_series.drop("iso_code", "continent", "location",
                                    "date", "icu_patients", "icu_patients_per_million",
                                    "hosp_patients", "hosp_patients_per_million", "weekly_icu_admissions",
                                    "weekly_icu_admissions_per_million", "weekly_hosp_admissions", "weekly_hosp_admissions_per_million",
                                    "total_tests", "new_tests", "total_tests_per_thousand",
                                    "new_tests_per_thousand", "new_tests_smoothed", "new_tests_smoothed_per_thousand",
                                    "positive_rate", "tests_per_case", "tests_units",
                                    "extreme_poverty", "handwashing_facilities"))

```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_03_15.png?raw=true)

</div>
