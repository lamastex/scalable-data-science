<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

### Download Files Periodically

This notebook allows for setup and execution of a script to periodically download files. In this case the "Our World in Data" dataset csv files which are updated daily.

Content is based on "037a\_AnimalNamesStructStreamingFiles" by Raazesh Sainudiin.

</div>

<div class="cell markdown">

To be able to later kill a .sh process, we need to make this installation

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` sh
apt-get install -y psmisc 
```

</div>

<div class="cell markdown">

create a new directory for our files if needed

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
dbutils.fs.mkdirs("file:///databricks/driver/projects/group12")
```

<div class="output execute_result plain_result" execution_count="1">

    res0: Boolean = true

</div>

</div>

<div class="cell markdown">

create a shell script to periodically download the dataset (currently set to download once per day). 1. shell 2. remove the previous shell script 3. write to script: bash binaries 4. write to script: remove folder where previous downloaded files are located 5. write to script: make new directory to put downloaded files 6. write to script: while loop: 6.1) remove old downloaded csv dataset 6.2) download new csv dataset 6.3) copy the csv file to the newly created directory using the timestamp as name 7. print the contents of the shell script

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` sh
rm -f projects/group12/group12downloadFiles.sh &&
echo "#!/bin/bash" >> projects/group12/group12downloadFiles.sh &&
echo "rm -rf projects/group12/logsEveryXSecs" >> projects/group12/group12downloadFiles.sh &&
echo "mkdir -p projects/group12/logsEveryXSecs" >> projects/group12/group12downloadFiles.sh &&
echo "while true; rm owid-covid-data.csv; wget https://covid.ourworldindata.org/data/owid-covid-data.csv; do echo \$( date --rfc-3339=second )\; | cp owid-covid-data.csv projects/group12/logsEveryXSecs/\$( date '+%y_%m_%d_%H_%M_%S.csv' ); sleep 216000; done" >> projects/group12/group12downloadFiles.sh &&
cat projects/group12/group12downloadFiles.sh
```

<div class="output execute_result plain_result" execution_count="1">

    #!/bin/bash
    rm -rf projects/group12/logsEveryXSecs
    mkdir -p projects/group12/logsEveryXSecs
    while true; rm owid-covid-data.csv; wget https://covid.ourworldindata.org/data/owid-covid-data.csv; do echo $( date --rfc-3339=second )\; | cp owid-covid-data.csv projects/group12/logsEveryXSecs/$( date '+%y_%m_%d_%H_%M_%S.csv' ); sleep 216000; done

</div>

</div>

<div class="cell markdown">

make the shell script executable

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` sh
chmod 744 projects/group12/group12downloadFiles.sh
```

</div>

<div class="cell markdown">

execute the shell script

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` sh
nohup projects/group12/group12downloadFiles.sh
```

</div>

<div class="cell markdown">

look at the files

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` sh
pwd
ls -al projects/group12/logsEveryXSecs
```

<div class="output execute_result plain_result" execution_count="1">

    /databricks/driver
    total 14244
    drwxr-xr-x 2 root root     4096 Jan  7 09:05 .
    drwxr-xr-x 3 root root     4096 Jan  7 09:05 ..
    -rw-r--r-- 1 root root 14577033 Jan  7 09:05 21_01_07_09_05_33.csv

</div>

</div>

<div class="cell markdown">

look at the file content

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` sh
cat projects/group12/logsEveryXSecs/XXXX.csv
```

<div class="output execute_result plain_result" execution_count="1">

    cat: projects/group12/logsEveryXSecs/XXXX.csv: No such file or directory

</div>

</div>

<div class="cell markdown">

kill the .sh process

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` sh
killall group12downloadFiles.sh
```

</div>

<div class="cell markdown">

move downloaded files to another location to make sure we don't delete the datasets

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// dbutils.fs.mkdirs("/datasets/group12/")
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
dbutils.fs.cp("file:///databricks/driver/projects/group12/logsEveryXSecs/","/datasets/group12/",true)
```

<div class="output execute_result plain_result" execution_count="1">

    res5: Boolean = true

</div>

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
</tbody>
</table>

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>
