# Databricks notebook source
# MAGIC %md # Distributed Deep Learning 
# MAGIC ## CNN's with horovod, MLFlow and hypertuning through SparkTrials
# MAGIC William Anzén ([Linkedin](https://www.linkedin.com/in/william-anz%C3%A9n-b52003199/)), Christian von Koch ([Linkedin](https://www.linkedin.com/in/christianvonkoch/))
# MAGIC 
# MAGIC 2021, Stockholm, Sweden
# MAGIC 
# MAGIC This project was supported by Combient Mix AB under the industrial supervision of Razesh Sainudiin and Max Fischer.
# MAGIC 
# MAGIC However, all the neuromusculature credit goes to William and Christian, they absorbed the WASP PhD course over their X-mas holidays
# MAGIC 
# MAGIC **Caveat** These notebooks were done an databricks shard on Azure, as opposed to AWS.
# MAGIC 
# MAGIC So one has to take some care up to databricks' [terraform](https://www.terraform.io/) pipes. Loading data should be independent of the underlying cloud-provider as the data is loaded through Tensorflow Datasets, although the following notebooks have not been tested on this AWS infrastructure with their kind support of a total of USD 7,500 AWS credits through The databricks University Alliance which waived the DBU-units on a professional enterprise-grade shard for WASP SCadaMaLe/sds-3-x course with voluntary research students at any Swedish University who go through the curriculum first. Raazesh Sainudiin is most grateful to Rob Reed for the most admirable administration of The databricks University Alliance.
# MAGIC 
# MAGIC ** Resources: **
# MAGIC 
# MAGIC These notebooks were inspired by Tensorflow's tutorial on [Image Segmentation](https://www.tensorflow.org/tutorials/images/segmentation).
# MAGIC 
# MAGIC ###01a_image_segmentation_unet
# MAGIC In this chapter a simple [U-Net](https://arxiv.org/abs/1505.04597) architecture is implemented and evaluated against the [Oxford Pets Data set](https://www.robots.ox.ac.uk/~vgg/data/pets/). The model achieves a validation accuracy of 81.51% and a validation loss of 0.7251 after 38/50 epochs (3.96 min full 50 epochs).
# MAGIC 
# MAGIC ### exjobbsOfCombientMix2021_02a_image_segmenation_pspnet
# MAGIC In this chapter a [PSPNet](https://arxiv.org/abs/1612.01105) architecture is implemented and evaluated against the [Oxford Pets Data set](https://www.robots.ox.ac.uk/~vgg/data/pets/). The model achieves a validation accuracy of 90.30% and a validation loss of 0.3145 after 42/50 epochs (39.64 min full 50 epochs).
# MAGIC 
# MAGIC ### exjobbsOfCombientMix2021_03a_image_segmenation_icnet
# MAGIC In this chapter the [ICNet](https://arxiv.org/abs/1704.08545) architecture is implemented and evaluated against the [Oxford Pets Data set](https://www.robots.ox.ac.uk/~vgg/data/pets/). The model achieves a validation accuracy of 86.64% and a validation loss of 0.3750 after 31/50 epochs (12.50 min full 50 epochs).
# MAGIC 
# MAGIC ### exjobbsOfCombientMix2021_04a_pspnet_tuning_parallel
# MAGIC In this chapter we run hyperparameter tuning with [hyperopt & SparkTrials](https://docs.databricks.com/_static/notebooks/hyperopt-spark-mlflow.html) allowing the hyperparameter tuning to be made in parallel across multiple workers. Achieved 0.56 loss with parameters({'batch_size': 16, 'learning_rate': 0.0001437661898681224}) (1.56 hours - 4 workers)
# MAGIC 
# MAGIC ### exjobbsOfCombientMix2021_05_pspnet_horovod
# MAGIC In this chapter we add [horovod](https://arxiv.org/abs/1802.05799) to the notebook, allowing distributed training of the model. Achieved a validation accuracy of 89.87% and validation loss of loss: 0.2861 after 49/50 epochs (33.93 min - 4 workers). 