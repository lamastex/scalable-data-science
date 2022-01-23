<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Analyse the eigenvalue spectrum
===============================

</div>

<div class="cell markdown">

-   Load the singular values computed in 03*compute*rsvd, sort them and convert to eigenvalues taking the square
-   Plot the spectrum for each graph in a semi-log plot for comparison

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
import pyspark.sql.functions as F
import numpy as np
import pandas as pd
import seaborn as sns
%matplotlib inline
import matplotlib.pyplot as plt
```

</div>

<div class="cell markdown">

### Function for getting sorted eigenvalues of graph Laplacian L from singular values of incidence matrix B

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
def to_eigen(singular_values):
  singular_values = singular_values.sort_values(by='value', ascending=False)
  eigen_values = np.power(singular_values, 2)
  return eigen_values
```

</div>

<div class="cell markdown">

### Get eigenvalues of Ethereum graph

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
data_path = "/projects/group21/test_ethereum_SingularValues"
singular_values_eth = spark.read.format('parquet').load(data_path).toPandas()
eigen_values_eth = to_eigen(singular_values_eth)
```

</div>

<div class="cell markdown">

### Get eigenvalues of Erdös-Renyi graphs

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
eigen_values_uniform = []
for i in range(10):
  data_path = "/projects/group21/uniform_random_graph_SingularValues" + str(i)
  singular_values = spark.read.format('parquet').load(data_path).toPandas()
  eigen_values_uniform.append(to_eigen(singular_values))
```

</div>

<div class="cell markdown">

### Get eigenvalues of R-MAT graphs

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
eigen_values_rmat = []
for i in range(10):
  data_path = "/projects/group21/rmat_random_graph_SingularValues" + str(i)
  singular_values = spark.read.format('parquet').load(data_path).toPandas()
  eigen_values_rmat.append(to_eigen(singular_values))
```

</div>

<div class="cell markdown">

### Plot sorted eigenvalues for all graphs

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
colors = sns.color_palette()
fig, ax = plt.subplots(figsize=(16, 9))
x = np.arange(len(eigen_values_eth))
ax = sns.lineplot(x=x, y=eigen_values_eth.to_numpy().ravel(), color=colors[0], label='ethereum')
for i in range(9):
  ax = sns.lineplot(x=x, y=eigen_values_uniform[i].to_numpy().ravel(), color=colors[1], alpha=0.4)
  ax = sns.lineplot(x=x, y=eigen_values_rmat[i].to_numpy().ravel(), color=colors[2], alpha=0.4)
  
ax = sns.lineplot(x=x, y=eigen_values_uniform[9].to_numpy().ravel(), color=colors[1], alpha=0.4, label='erdös-renyi')
ax = sns.lineplot(x=x, y=eigen_values_rmat[9].to_numpy().ravel(), color=colors[2], alpha=0.4, label='rmat')
ax.set_yscale('log')
ax.legend()
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/21_04_1.JPG?raw=true)

</div>

<div class="cell markdown">

Conclusion
----------

We observe a large descrepency in the spectrums between the Erdös-Renyi, R-MAT and Ethereum transaction graphs. As can be expected, the spectrum of the Erdös-Renyi graphs is almost constant due to the isotropy of the graph topology. The Ethereum transaction graph has very large eigenvalues compared to the random graphs. A likely explanation is the presence of nodes of very high degree in the graph.

We can see that the R-MAT graph lies in between uniform Erdös-Renyi and Ethereum graph. This is also as expected since the R-MAT model is designed to better mimic the behaviour of real graphs. In this project we used the default parameters for the R-MAT graph and it is likely that with further experimentation one could find a setting which better fit the spectum of the transaction graph.

</div>
