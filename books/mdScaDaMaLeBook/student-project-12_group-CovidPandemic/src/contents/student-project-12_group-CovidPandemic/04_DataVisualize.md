<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
pip install plotly
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` run
"./DataPreprocess"
```

</div>

<div class="cell markdown">

### Show reproduction rate of selected countries i.e. Sweden, Germany, Danmark, Finland, Norway

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(df_cleaned_time_series.select($"reproduction_rate", $"location", $"date").filter($"reproduction_rate".isNotNull).filter(
                                                                                       $"location"==="Sweden" ||
                                                                                       $"location"==="Germany" ||
                                                                                       $"location"==="Danmark" ||
                                                                                       $"location"==="Finland" ||
                                                                                       $"location"==="Norway").sort("date"))
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_04_1.JPG?raw=true)

</div>

<div class="cell markdown">

### Visualize total cases, total deaths, new cases and new deaths during pandemic

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
df_cleaned_time_series.createOrReplaceTempView("visual_rdd")
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
import pandas as pd
import numpy as np
import plotly.express as px
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
test_table = spark.table("visual_rdd")

country = np.array(test_table.select("iso_code").rdd.map(lambda l: l[0]).collect())
dates = np.array(test_table.select("date").rdd.map(lambda l: l[0]).collect())
total_cases = np.array(test_table.select("total_cases").rdd.map(lambda l: l[0]).collect())
total_deaths = np.array(test_table.select("total_deaths").rdd.map(lambda l: l[0]).collect())
new_cases = np.array(test_table.select("new_cases").rdd.map(lambda l: l[0]).collect())
new_deaths = np.array(test_table.select("new_deaths").rdd.map(lambda l: l[0]).collect())

visual_data = {'country':country.tolist(), 'total_cases':total_cases, 'date':dates, 
             'total_deaths': total_deaths, 'new_cases': new_cases, 'new_deaths': new_deaths}
visual_df = pd.DataFrame(data = visual_data).sort_values(by='date')
visual_df
```

</div>

<div class="cell markdown">

Total Cases

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
fig = px.choropleth(visual_df[~visual_df.country.str.contains("WLD", na=False)], locations="country",
                     color="total_cases", # total_cases is a column of gapminder
                     hover_name="country", # column to add to hover information
                     color_continuous_scale=px.colors.sequential.Plasma,
                     animation_frame = 'date')
fig.show()
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_04_2.JPG?raw=true)

</div>

<div class="cell markdown">

### Total Deaths

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
fig = px.choropleth(visual_df[~visual_df.country.str.contains("WLD", na=False)], locations="country",
                    color="total_deaths", # total_deaths is a column of gapminder
                    hover_name="country", # column to add to hover information
                    color_continuous_scale=px.colors.sequential.Plasma,
                    animation_frame = 'date')
fig.show()
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_04_3.JPG?raw=true)

</div>

<div class="cell markdown">

### New Cases

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
fig = px.choropleth(visual_df[~visual_df.country.str.contains("WLD", na=False)], locations="country",
                    color="new_cases", # new_cases is a column of gapminder
                    hover_name="country", # column to add to hover information
                    color_continuous_scale=px.colors.sequential.Plasma,
                    animation_frame = 'date')
fig.show()
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_04_4.JPG?raw=true)

</div>

<div class="cell markdown">

### New Deaths

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
fig = px.choropleth(visual_df[~visual_df.country.str.contains("WLD", na=False)], locations="country",
                    color="new_deaths", # new_deaths is a column of gapminder
                    hover_name="country", # column to add to hover information
                    color_continuous_scale=px.colors.sequential.Plasma,
                    animation_frame = 'date')
fig.show()
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_04_5.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>
