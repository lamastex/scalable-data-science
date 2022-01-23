<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Gaussian Analysis
=================

</div>

<div class="cell markdown">

Here we test the results for normally distributed points.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
import numpy as np
import os
import shutil
import glob
import matplotlib.pyplot as plt
import scipy as sp
import scipy.stats as stats
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
os.listdir('/dbfs/FileStore/group17/data/')
```

</div>

<div class="cell markdown">

Reading the files.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
def read_csv(data_name):
  results = glob.glob('/dbfs/FileStore/group17/data/' + data_name + '/*.csv')
  assert(len(results) == 1)
  filepath = results[0]
  
  csv = np.loadtxt(filepath, delimiter=',')
  csv = csv[csv[:, 0].argsort()]
  return csv
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
train_data = read_csv('gaussian_train')
test_data = read_csv('gaussian_test')
weights = read_csv('gaussian_weights')
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
def display_density(data, weights):
  fig = plt.figure(figsize=(10, 10))
  plt.scatter(data[:, 0], data[:, 1], weights / np.max(weights) * 50)
  display(fig)
```

</div>

<div class="cell markdown">

True density visualization.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
true_density = stats.multivariate_normal.pdf(test_data[:, 1:], mean=np.zeros(2))

display_density(test_data[:, 1:], true_density)
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/17_02_1.JPG?raw=true)

</div>

<div class="cell markdown">

Density, obtained from our method.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
display_density(test_data[:, 1:], weights[:, 1])
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/17_02_2.JPG?raw=true)

</div>

<div class="cell markdown">

Density, obtained from kernel density estimation with tophat kernel.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
from sklearn.neighbors.kde import KernelDensity
kde = KernelDensity(kernel='tophat', bandwidth=0.13).fit(train_data[:, 1:])
kde_weights = kde.score_samples(test_data[:, 1:])
kde_weights = np.exp(kde_weights)

display_density(test_data[:, 1:], kde_weights)
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/17_02_3.JPG?raw=true)

</div>

<div class="cell markdown">

Density, obtained from kernel density estimation with gaussian kernel.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
kde = KernelDensity(kernel='gaussian', bandwidth=0.13).fit(train_data[:, 1:])
gauss_weights = kde.score_samples(test_data[:, 1:])
gauss_weights = np.exp(kde_weights)

display_density(test_data[:, 1:], gauss_weights)
```

</div>

<div class="cell markdown">

A simple computation of the number of inverses.

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/17_02_4.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
def rank_loss(a, b):
  n = a.shape[0]
  assert(n == b.shape[0])
  ans = 0
  for i in range(n):
    for j in range(i + 1, n):
      if (a[i] - a[j]) * (b[i] - b[j]) < 0:
        ans += 1
  return ans
```

</div>

<div class="cell markdown">

Comparison of losses. On this one test, we get the smallest loss.

One of the immediate futher works: do a proper statistical comparison, also on different sizes of data.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
rank_loss(weights[:, 1], true_density)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
rank_loss(kde_weights, true_density)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python

rank_loss(gauss_weights, true_density)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>
