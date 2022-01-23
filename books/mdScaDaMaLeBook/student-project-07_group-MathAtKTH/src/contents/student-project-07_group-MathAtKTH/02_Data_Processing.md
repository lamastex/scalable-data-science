<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Data Processing
===============

Here we will load the data as it was given to us, as a ".npy" file, and rewrite it in a simpler format. Why we do this is motivated in "Coding\_Motifs", subsection "application".

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
# Load packages

import numpy as np
```

</div>

<div class="cell markdown">

Read the data as a numpy object (since we got it like that), and save it as a ".csv" since scala can read that.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python

M = np.load('/dbfs/FileStore/shared_uploads/adlindhe@kth.se/M.npy')
np.savetxt("/dbfs/FileStore/shared_uploads/adlindhe@kth.se/M.csv", M, delimiter=",")
```

</div>

<div class="cell markdown">

Right now the data is an adjacency matrix of size 31346 times 31346 but only 0.7% of all entries are 1. Thus we would like to save it down as something a little easier to handle. We can read the adjacency matrix directly as a dataframe, but that does not work well with graphframes. Instead we want it as an edgelist.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
/*
** Example on how we can read the data.
*/
val Ms = spark.read.format("csv").option("sep",",").option("MaxColumns",40000).load("/FileStore/shared_uploads/adlindhe@kth.se/M.csv")
```

</div>

<div class="cell markdown">

Thus we rewrite it as a edgelist. Thus we can more easily load it into graphframes.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
# Loop over the whole matrix. This takes time (~ 1h).

edges_file = open("/dbfs/FileStore/shared_uploads/petterre@kth.se/edges.csv", "w")

for i in range(len(M)):
  for j in range(len(M[i])):
    for k in range(M[i][j]):
      edges_file.write(str(i) + "," + str(j) + ",edge\n")

edges_file.close()
```

</div>

<div class="cell markdown">

Look at it to see if it looks ok.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` sh
head /dbfs/FileStore/shared_uploads/petterre@kth.se/edges.csv
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
Ms.cache // cache the DataFrame this time
Ms.count // now after this action, the next count will be fast...
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(Ms)
```

</div>
