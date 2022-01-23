<div class="cell markdown">

Data and preprocessing
----------------------

</div>

<div class="cell markdown">

We start by seeing if the files necessary to run our notebooks are already in the distributed file system

</div>

<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
dbutils.fs.ls("dbfs:///FileStore/06_LHC")
```

<div class="output execute_result plain_result" execution_count="1">

    res0: Seq[com.databricks.backend.daemon.dbutils.FileInfo] = WrappedArray(FileInfo(dbfs:/FileStore/06_LHC/LICENSE, LICENSE, 1071), FileInfo(dbfs:/FileStore/06_LHC/README.md, README.md, 3150), FileInfo(dbfs:/FileStore/06_LHC/data/, data/, 0), FileInfo(dbfs:/FileStore/06_LHC/h5/, h5/, 0), FileInfo(dbfs:/FileStore/06_LHC/models/, models/, 0), FileInfo(dbfs:/FileStore/06_LHC/scripts/, scripts/, 0), FileInfo(dbfs:/FileStore/06_LHC/utils/, utils/, 0))

</div>

</div>

<div class="cell markdown">

### Important!

Run the command line above to see if the required files and data are already available in the distributed file system, you should see the following:

Seq\[com.databricks.backend.daemon.dbutils.FileInfo\] = WrappedArray(FileInfo(dbfs:/FileStore/06*LHC/LICENSE, LICENSE, 1071), FileInfo(dbfs:/FileStore/06*LHC/README.md, README.md, 3150), FileInfo(dbfs:/FileStore/06*LHC/data/, data/, 0), FileInfo(dbfs:/FileStore/06*LHC/h5/, h5/, 0), FileInfo(dbfs:/FileStore/06*LHC/models/, models/, 0), FileInfo(dbfs:/FileStore/06*LHC/scripts/, scripts/, 0), FileInfo(dbfs:/FileStore/06\_LHC/utils/, utils/, 0))

If these items appear, then skip most of this notebook, and go to Command Cell 21 to import data to the local driver

</div>

<div class="cell markdown">

### Get the data

</div>

<div class="cell markdown">

We start by installing everything necessary

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
pip install h5py
```

</div>

<div class="cell markdown">

We start by preparing a folder on the local driver to process our files

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` sh
rm -r 06_LHC
mkdir 06_LHC
```

</div>

<div class="cell markdown">

Now get all necessary files from the project repository on Github

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` sh
cd 06_LHC

wget https://github.com/dgedon/ProjectParticleClusteringv2/archive/main.zip
unzip main.zip
mv ProjectParticleClusteringv2-main/* .
rm -r ProjectParticleClusteringv2-main/ main.zip
```

</div>

<div class="cell markdown">

We get the necessarry data (first training, then validation) and untar the file

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` sh
cd 06_LHC
mkdir data
cd data


wget https://zenodo.org/record/3602254/files/hls4ml_LHCjet_100p_train.tar.gz
#mkdir data
#mv hls4ml_LHCjet_100p_train.tar.gz data
#cd data
tar --no-same-owner -xvf hls4ml_LHCjet_100p_train.tar.gz
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` sh
cd 06_LHC/data
wget https://zenodo.org/record/3602254/files/hls4ml_LHCjet_100p_val.tar.gz
tar --no-same-owner -xvf hls4ml_LHCjet_100p_val.tar.gz
```

</div>

<div class="cell markdown">

Now we preprocess the data. This transforms the data somehow in a useful way

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` sh
cd 06_LHC/scripts
python prepare_data_multi.py --dir ../data/
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` sh
cd 06_LHC/scripts
python prepare_data_multi.py --dir ../data/ --make_eval
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
rm -r ../data
```

</div>

<div class="cell markdown">

Finally move everything onto the distributed file system. All necessary files are stored at FileStore/06\_LHC

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
dbutils.fs.cp ("file:////databricks/driver/06_LHC", "dbfs:///FileStore/06_LHC", recurse=true) 
```

</div>

<div class="cell markdown">

### Important (continued from above)

### Import files to local driver

Now for future notebooks, run the following command line below to import the files to the local driver. This may take a minute

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
dbutils.fs.cp("dbfs:///FileStore/06_LHC", "file:////databricks/driver/06_LHC", recurse=true)
```

<div class="output execute_result plain_result" execution_count="1">

    res0: Boolean = true

</div>

</div>

<div class="cell markdown">

Run the command line below to list the items in the 06\_LHC folder. You should see the following:

-   LICENSE
-   README.md
-   h5
-   models
-   scripts
-   utils

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` sh
ls 06_LHC/
```

</div>

<div class="cell markdown">

You are now ready to go!

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>
