# Databricks notebook source
# MAGIC %md
# MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

# COMMAND ----------

# MAGIC %md # Distributed Deep Learning 
# MAGIC ## CNN's with horovod, MLFlow and hypertuning through SparkTrials
# MAGIC William Anzén ([Linkedin](https://www.linkedin.com/in/william-anz%C3%A9n-b52003199/)), Christian von Koch ([Linkedin](https://www.linkedin.com/in/christianvonkoch/))
# MAGIC 
# MAGIC 2021, Stockholm, Sweden
# MAGIC 
# MAGIC This project was supported by Combient Mix AB through a Master Thesis project at ISY, Computer Vision Laboratory, Linköpings University.
# MAGIC 
# MAGIC ** Resources: **
# MAGIC 
# MAGIC These notebooks were inspired by Tensorflow's tutorial on [Image Segmentation](https://www.tensorflow.org/tutorials/images/segmentation).
# MAGIC 
# MAGIC ### [01_ImageSegmentation_UNet](https://dbc-635ca498-e5f1.cloud.databricks.com/?o=445287446643905#notebook/2616622521301698/command/2616622521301709) 
# MAGIC In this chapter a simple [U-Net](https://arxiv.org/abs/1505.04597) architecture is implemented and evaluated against the [Oxford Pets Data set](https://www.robots.ox.ac.uk/~vgg/data/pets/). The model achieves a validation accuracy of 88.6% and a validation loss of 0.655 after 20 epochs (11.74 min).
# MAGIC 
# MAGIC ### [02_ImageSegmenation_PSPNet](https://dbc-635ca498-e5f1.cloud.databricks.com/?o=445287446643905#notebook/2616622521301710/command/1970952129252495)
# MAGIC In this chapter a [PSPNet](https://arxiv.org/abs/1612.01105) architecture is implemented and evaluated against the [Oxford Pets Data set](https://www.robots.ox.ac.uk/~vgg/data/pets/). The model achieves a validation accuracy of 89.8% and a validation loss of 0.332 after 20 epochs (14.25 min).
# MAGIC 
# MAGIC ### [03_ICNet_Function](https://dbc-635ca498-e5f1.cloud.databricks.com/?o=445287446643905#notebook/752230548183766/command/752230548183767)
# MAGIC In this chapter the [ICNet](https://arxiv.org/abs/1704.08545) architecture is implemented and evaluated against the [Oxford Pets Data set](https://www.robots.ox.ac.uk/~vgg/data/pets/). MLFlow is added to keep track of results and parameters. The model achieves a validation accuracy of 86.1% and a validation loss of 0.363 after 19/20 epochs (6.8 min).
# MAGIC 
# MAGIC ### [04_ICNet_Function_hvd](https://dbc-635ca498-e5f1.cloud.databricks.com/?o=445287446643905#notebook/597736601883146/command/597736601883147)
# MAGIC In this chapter we add [horovod](https://arxiv.org/abs/1802.05799) to the notebook, allowing distributed training of the model. MLFlow is also integrated to keep track of results and parameters. Achieving validation accuracy of 84.4% and validation loss of 0.454 after 16/20 epochs (13.19 min - 2 workers). (2 workers lead to a slower run because of the overhead being too large in comparison to computational gain)
# MAGIC 
# MAGIC ### [05_ICNet_Function_Tuning_parallel](https://dbc-635ca498-e5f1.cloud.databricks.com/?o=445287446643905#notebook/1970952129252457/command/1970952129252458)
# MAGIC In this chapter we run hyperparameter tuning with [hyperopt & SparkTrials](https://docs.databricks.com/_static/notebooks/hyperopt-spark-mlflow.html) allowing the tuning runs to be made in parallel across multiple workers. MLFlow is added to keep track of the outcomes from the parallel hyperparameter tuning runs. Achieved 0.43 loss with parameters({'batch_size': 32, 'learning_rate': 0.007874409614279713})