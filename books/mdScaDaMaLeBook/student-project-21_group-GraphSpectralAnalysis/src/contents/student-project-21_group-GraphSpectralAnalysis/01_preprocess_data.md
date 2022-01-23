<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Preprocess the data
===================

Here the raw Ethereum transaction data read from google big query is preprocessed. - Remove any rows with nulls - Drop all self-loops - Enumerate all the distict addresses - Make a canonical ordering for the edges - Each edge will point from lower to higher index - The sign of the transaction is changed for flipped edges - Aggregate transactions based on src, dst pair - Enumerate the edges with a unique edge id

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
import pyspark.sql.functions as F
from pyspark.sql.window import Window
```

</div>

<div class="cell markdown">

### Load data into DataFrame

And drop nans and self-loop

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
data_path = "FileStore/tables/ethereum_march_2018_2020"

df = spark.read.format('csv').option("header", "true").load(data_path)\
  .select(F.col("from_address"), F.col("to_address"), F.col("value"))\
  .na.drop()\
  .where(F.col("from_address") != F.col("to_address"))
```

</div>

<div class="cell markdown">

### Enumerate the addresses with a unique id

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
addresses = df.select(F.col("from_address").alias("address")).union(df.select(F.col("to_address").alias("address"))).distinct()
address_window = Window.orderBy("address")
addresses = addresses.withColumn("id", F.row_number().over(address_window))
```

</div>

<div class="cell markdown">

### Make the edges canonical

-   Each edge will point from lower to higher index
-   The sign of the transaction is changed for flipped edges

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
# Exchange string addresses for node ids
df_with_ids = df.join(addresses.withColumnRenamed("address", "to_address").withColumnRenamed("id", "dst__"), on="to_address")\
  .join(addresses.withColumnRenamed("address", "from_address").withColumnRenamed("id", "src__"), on="from_address")

canonical_edges = df_with_ids.withColumn("src",
  F.when(F.col("dst__") > F.col("src__"), F.col("src__")).otherwise(F.col("dst__"))
).withColumn("dst",
  F.when(F.col("dst__") > F.col("src__"), F.col("dst__")).otherwise(F.col("src__"))
).withColumn("direction__",
  F.when(F.col("dst__") > F.col("src__"), 1).otherwise(-1)
).withColumn("flow",
F.col("value") * F.col("direction__")
)
```

</div>

<div class="cell markdown">

### Group the edges by source (src) and destination (dst) by taking the sum of the flow

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
grouped_canonical_edges = canonical_edges.select(F.col("src"), F.col("dst"), F.col("flow")).groupBy(F.col("src"), F.col("dst")).agg(F.sum(F.col("flow")).alias("flow"))
```

</div>

<div class="cell markdown">

### Enumerate the edges with a unique edge id

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
edges_window = Window.orderBy(F.col("src"), F.col("dst"))
grouped_canonical_edges = grouped_canonical_edges.withColumn("id", F.row_number().over(edges_window))
```

</div>

<div class="cell markdown">

### Save the results in parquet files

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
preprocessed_edges_path = "/projects/group21/test_ethereum_canonical_edges"
preprocessed_addresses_path = "/projects/group21/test_ethereum_addresses"

grouped_canonical_edges.write.format('parquet').mode("overwrite").save(preprocessed_edges_path)
addresses.write.format('parquet').mode("overwrite").save(preprocessed_addresses_path)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>
