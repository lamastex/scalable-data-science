# Databricks notebook source exported at Tue, 28 Jun 2016 09:35:44 UTC


# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Paul Brouwers](https://www.linkedin.com/in/paul-brouwers-5365117a), [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)





** Students of the Scalable Data Science Course at UC, Ilam ** 

* First check if a cluster named `classClusterTensorFlow` is running.
* If it is then just skip this notebook and attach the next notebook to `classClusterTensorFlow`




 
## TensorFlow initialization scripts

> This notebook explains how to install TensorFlow on a large cluster. It is __not__ required for the Databricks Community Edition.

The TensorFlow library needs to be installed directly on all the nodes of the cluster. We show here how to install complex python packages that are not supported yet by the Databricks library manager. Such libraries are directly installed using _cluster initialization scripts_ ("init scripts" for short). These scripts are Bash programs that run on a compute node when this node is being added to a cluster.

For more information, please refer to the init scripts in the Databricks guide.

These scripts require the name of the cluster. If you use this notebook, you will need to change the name of the cluster in the cell below:





## Step 1. Set cluster variable and check


```python

# Change the value to the name of your cluster:
clusterName = "classClusterTensorFlow"

```



To check if the init scripts are already in this cluster.


```python

dbutils.fs.ls("dbfs:/databricks/init/%s/" % clusterName)

```



If ``pillow-install.sh` and `tensorflow-install.sh` are already in this cluster then skip **Step 2** below.





## Step 2. To (re)create init scripts

If the `.sh` files above are not there, then evaluate the cell below and restart the cluster.

**Sub-step 2.1**




 
The following commands create init scripts that install the TensorFlow library on your cluster whenever it gets started or restarted. If you do not want to have TensorFlow installed on this cluster by default, you need to remove the scripts, by running the following command:

  ```python
  dbutils.fs.rm("dbfs:/databricks/init/%s/tensorflow-install.sh" % clusterName)
  dbutils.fs.rm("dbfs:/databricks/init/%s/pillow-install.sh" % clusterName)
  ```




 
The next cell creates the init scripts. You need to restart your cluster after running the following command.


```python

dbutils.fs.mkdirs("dbfs:/databricks/init/")
dbutils.fs.put("dbfs:/databricks/init/%s/tensorflow-install.sh" % clusterName,"""
#!/bin/bash 
/databricks/python/bin/pip install https://storage.googleapis.com/tensorflow/linux/cpu/tensorflow-0.6.0-cp27-none-linux_x86_64.whl
""", True)

# This is just to get nice image visualization
dbutils.fs.put("dbfs:/databricks/init/%s/pillow-install.sh" % clusterName,"""
#!/bin/bash 
echo "------ packages --------"
sudo apt-get -y --force-yes install libtiff5-dev libjpeg8-dev zlib1g-dev
echo "------ python packages --------"
/databricks/python/bin/pip install pillow
""", True)

```


 
**Sub-step 2.2** You now need to restart your cluster.




 
## 3. How to check that the scripts ran correctly after running a cluster (possibly by restarting)

As explained in the Databricks guide, the output of init scripts is stored in DBFS. The following cell accesses the latest content of the logs after a cluster start:


```python

stamp = str(dbutils.fs.ls("/databricks/init/output/%s/" % clusterName)[-1].name)
print("Stamp is %s" % stamp)
files = dbutils.fs.ls("/databricks/init/output/%s/%s" % (clusterName, str(stamp)))
tf_files = [str(fi.path) for fi in files if fi.name.startswith("%s-tensorflow-install" % clusterName)]
logs = [dbutils.fs.head(fname) for fname in tf_files]
for log in logs:
  print "************************"
  print log

```




# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Paul Brouwers](https://www.linkedin.com/in/paul-brouwers-5365117a), [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)
