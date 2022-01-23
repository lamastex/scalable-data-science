<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

We start by seeing if the files necessary to run our notebooks are already in the distributed file system

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
dbutils.fs.ls("dbfs:///FileStore/06_LHC")
```

<div class="output execute_result plain_result" execution_count="1">

    res0: Seq[com.databricks.backend.daemon.dbutils.FileInfo] = WrappedArray(FileInfo(dbfs:/FileStore/06_LHC/LICENSE, LICENSE, 1071), FileInfo(dbfs:/FileStore/06_LHC/README.md, README.md, 3150), FileInfo(dbfs:/FileStore/06_LHC/h5/, h5/, 0), FileInfo(dbfs:/FileStore/06_LHC/models/, models/, 0), FileInfo(dbfs:/FileStore/06_LHC/scripts/, scripts/, 0), FileInfo(dbfs:/FileStore/06_LHC/utils/, utils/, 0))

</div>

</div>

<div class="cell markdown">

import the files to the local driver. This takes a minute.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
dbutils.fs.cp("dbfs:///FileStore/06_LHC", "file:////databricks/driver/06_LHC", recurse=true)
```

<div class="output execute_result plain_result" execution_count="1">

    res1: Boolean = true

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` sh
ls 06_LHC/h5 
```

<div class="output execute_result plain_result" execution_count="1">

    eval_multi_20v_100P.h5
    evaluate_files_RD.txt
    evaluate_files_b1.txt
    evaluate_files_b2.txt
    evaluate_files_b3.txt
    evaluate_files_gwztop.txt
    evaluate_files_wztop.txt
    test_files_RD.txt
    test_files_b1.txt
    test_files_b2.txt
    test_files_b3.txt
    test_files_gwztop.txt
    test_files_wztop.txt
    test_multi_20v_100P.h5
    train_files_RD.txt
    train_files_b1.txt
    train_files_b2.txt
    train_files_b3.txt
    train_files_gwztop.txt
    train_files_wztop.txt
    train_multi_20v_100P.h5

</div>

</div>

<div class="cell markdown">

Parquet-fying of the h5 dataset

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
import h5py
import pandas as pd
## BOOKMARK FROM HERE IMPORTANT
import pyarrow as pa
import pyarrow.parquet as pq

def get_unique(xs):
  x0 = next(xs)
  assert(x == x0 for x in xs)
  return x0

f = h5py.File('06_LHC/h5/train_multi_20v_100P.h5', 'r')
data_keys =list(f.keys())
row_count = get_unique(f[k].shape[0] for k in data_keys)
#print(len(data_keys))

data = { data_keys[0] : list(f[data_keys[0]][:].reshape(row_count,-1)),
         data_keys[1] : list(f[data_keys[1]][:].reshape(row_count,-1)),
         data_keys[2] : list(f[data_keys[2]][:].reshape(row_count,-1)),
         data_keys[3] : list(f[data_keys[3]][:]),
         data_keys[4] : list(f[data_keys[4]][:])}
data_table = pa.Table.from_pandas(pd.DataFrame(data))
writer = None
if writer is None:
  writer = pq.ParquetWriter('train.parquet', data_table.schema)
  
writer.write_table(data_table)
  
writer.close()

f = h5py.File('06_LHC/h5/test_multi_20v_100P.h5', 'r')
data_keys =list(f.keys())
row_count = get_unique(f[k].shape[0] for k in data_keys)
#print(len(data_keys))

data = { data_keys[0] : list(f[data_keys[0]][:].reshape(row_count,-1)),
         data_keys[1] : list(f[data_keys[1]][:].reshape(row_count,-1)),
         data_keys[2] : list(f[data_keys[2]][:].reshape(row_count,-1)),
         data_keys[3] : list(f[data_keys[3]][:]),
         data_keys[4] : list(f[data_keys[4]][:])}
data_table = pa.Table.from_pandas(pd.DataFrame(data))
writer = None
if writer is None:
  writer = pq.ParquetWriter('test.parquet', data_table.schema)
  
writer.write_table(data_table)
  
writer.close()
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` sh
ls -lh
```

<div class="output execute_result plain_result" execution_count="1">

    total 1.1G
    drwxr-xr-x 6 root root 4.0K Apr 20 09:08 06_LHC
    drwxr-xr-x 2 root root 4.0K Apr 20 09:00 conf
    -rw-r--r-- 1 root root  732 Apr 20 09:04 derby.log
    drwxr-xr-x 3 root root 4.0K Apr 20 09:04 eventlogs
    drwxr-xr-x 2 root root 4.0K Apr 20 09:04 logs
    -rw-r--r-- 1 root root 211M Apr 20 09:10 test.parquet
    -rw-r--r-- 1 root root 841M Apr 20 09:10 train.parquet

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
dbutils.fs.cp("file:////databricks/driver/train.parquet", "dbfs:///datasets/physics/LHC/train/", recurse=true)
dbutils.fs.cp("file:////databricks/driver/test.parquet", "dbfs:///datasets/physics/LHC/test/", recurse=true)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
dbutils.fs.mv("dbfs:///datasets/physics/LHC/train", "dbfs:///datasets/physics/LHC/train.parquet")
dbutils.fs.mv("dbfs:///datasets/physics/LHC/test", "dbfs:///datasets/physics/LHC/test.parquet")
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
pq.read_table('test.parquet')['pid'].
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
t = pq.read_table('test.parquet', rows=[3])
t_pd = t.to_pandas()
t_pd
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>
