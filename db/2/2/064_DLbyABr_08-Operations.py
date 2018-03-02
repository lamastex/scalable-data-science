# Databricks notebook source
# MAGIC %md
# MAGIC 
# MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
# MAGIC 
# MAGIC This is used in a non-profit educational setting with kind permission of [Adam Breindel](https://www.linkedin.com/in/adbreind).
# MAGIC This is not licensed by Adam for use in a for-profit setting. Please contact Adam directly at `adbreind@gmail.com` to request or report such use cases or abuses. 
# MAGIC A few minor modifications and additional mathematical statistical pointers have been added by Raazesh Sainudiin when teaching PhD students in Uppsala University.

# COMMAND ----------

# MAGIC %md 
# MAGIC # Operations in the Real World
# MAGIC 
# MAGIC ## Practical Options, Tools, Patterns, and Considerations for Deep Learning

# COMMAND ----------

# MAGIC %md 
# MAGIC There are various ways to use deep learning in an enterprise setting that may not require designing your own networks!
# MAGIC 
# MAGIC ### Ways to Use Deep Learning
# MAGIC 
# MAGIC (in order from least complex/expensive investment to most)
# MAGIC 
# MAGIC [1] Load and use a pretrained model
# MAGIC 
# MAGIC Many of the existing toolkit projects offer models pretrained on datasets, including
# MAGIC 
# MAGIC * natural language corpus models
# MAGIC * image datasets like ImageNet (http://www.image-net.org/) or Google's Open Image Dataset (https://research.googleblog.com/2016/09/introducing-open-images-dataset.html)
# MAGIC * video datasets like the YouTube 8 million video dataset (https://research.googleblog.com/2016/09/announcing-youtube-8m-large-and-diverse.html)
# MAGIC 
# MAGIC [2] Augmenting a pretrained model with new training data, or using it in a related context (see Transfer Learning)
# MAGIC 
# MAGIC [3] Use a known, established network type (topology) but train on your own data
# MAGIC 
# MAGIC [4] Modify established network models for your specific problem
# MAGIC 
# MAGIC [5] Research and experiment with new types of models
# MAGIC 
# MAGIC __Just because Google DeepMind, Facebook, and Microsoft are getting press for doing a lot of new research doesn't mean you have to do it too.__
# MAGIC 
# MAGIC <img src="http://i.imgur.com/XczCfNR.png" width=500>
# MAGIC <img src="http://i.imgur.com/vcaj99I.jpg" width=500>
# MAGIC 
# MAGIC Data science and machine learning is challenging in general for enterprises (though some industries, such as pharma, have been doing it for a long time). Deep learning takes that even further, since deep learning experiments may require new kinds of hardware ... in some ways, it's more like chemistry than the average IT project!

# COMMAND ----------

# MAGIC %md 
# MAGIC ### Tools and Processes for your Deep Learning Pipeline
# MAGIC 
# MAGIC #### Data Munging
# MAGIC 
# MAGIC Most of the deep learning toolkits are focused on model-building performance or flexibility, less on production data processing.
# MAGIC 
# MAGIC However, Google recently introduced `tf.Transform`, a data processing pipeline project: https://github.com/tensorflow/transform 
# MAGIC 
# MAGIC and Dataset, an API for data processing: https://www.tensorflow.org/api_docs/python/tf/contrib/data/Dataset
# MAGIC 
# MAGIC TensorFlow can read from HDFS and run on Hadoop although it *does not* scale out automatically on a Hadoop/Spark cluster: https://www.tensorflow.org/deploy/hadoop
# MAGIC 
# MAGIC Falling back to "regular" tools, we have Apache Spark for big data, and the Python family of pandas, sklearn, scipy, numpy.
# MAGIC 
# MAGIC #### Experimenting and Training
# MAGIC 
# MAGIC Once you want to scale beyond your laptop, there are few options...
# MAGIC 
# MAGIC * AWS GPU-enabled instances
# MAGIC * Deep-learning-infrastructure as a Service
# MAGIC     * EASY
# MAGIC         * "Floyd aims to be the Heroku of Deep Learning" https://www.floydhub.com/
# MAGIC         * "Effortless infrastructure for deep learning" https://www.crestle.com/
# MAGIC         * "GitHub of Machine Learning / We provide machine learning platform-as-a-service." https://valohai.com/     
# MAGIC         * "Machine Learning for Everyone" (may be in closed beta) https://machinelabs.ai
# MAGIC         *  Algorithms as a service / model deployment https://algorithmia.com/  
# MAGIC     * MEDIUM Google Cloud Platform "Cloud Machine Learning Engine" https://cloud.google.com/ml-engine/
# MAGIC     * HARDER Amazon Deep Learning AMI + CloudFormation https://aws.amazon.com/blogs/compute/distributed-deep-learning-made-easy/
# MAGIC * On your own infrastructure or VMs
# MAGIC     * Distributed TensorFlow is free, OSS
# MAGIC     * Apache Spark combined with Intel BigDL (CPU) or DeepLearning4J (GPU)
# MAGIC     * TensorFlowOnSpark
# MAGIC     * CERN Dist Keras (Spark + Keras) https://github.com/cerndb/dist-keras
# MAGIC     
# MAGIC #### Frameworks
# MAGIC 
# MAGIC We've focused on TensorFlow and Keras, because that's where the "center of mass" is at the moment.
# MAGIC 
# MAGIC But there are lots of others. Major ones include:
# MAGIC 
# MAGIC * Caffe
# MAGIC * PaddlePaddle
# MAGIC * Theano
# MAGIC * CNTK
# MAGIC * MXNet
# MAGIC * DeepLearning4J
# MAGIC * BigDL
# MAGIC * Torch/PyTorch
# MAGIC * NVIDIA Digits
# MAGIC 
# MAGIC and there are at least a dozen more minor ones.
# MAGIC 
# MAGIC #### Taking Your Trained Model to Production
# MAGIC 
# MAGIC Most trained models can predict in production in near-zero time. (Recall the forward pass is just a bunch of multiplication and addition with a few other calculations thrown in.)
# MAGIC 
# MAGIC For a neat example, you can persist Keras models and load them to run live in a browser with Keras.js
# MAGIC 
# MAGIC See Keras.js for code and demos: https://github.com/transcranial/keras-js
# MAGIC 
# MAGIC <img src="http://i.imgur.com/5xx62zw.png" width=700>
# MAGIC 
# MAGIC TensorFlow has an Android example at https://github.com/tensorflow/tensorflow/tree/master/tensorflow/examples/android
# MAGIC 
# MAGIC and Apple CoreML supports Keras models: https://developer.apple.com/documentation/coreml/converting_trained_models_to_core_ml
# MAGIC 
# MAGIC (remember, the model is already trained, we're just predicting here)
# MAGIC 
# MAGIC #### And for your server-side model update-and-serve tasks, or bulk prediction at scale...
# MAGIC 
# MAGIC (imagine classifying huge batches of images, or analyzing millions of chat messages or emails)
# MAGIC 
# MAGIC * TensorFlow has a project called TensorFlow Serving: https://tensorflow.github.io/serving/
# MAGIC * Spark Deep Learning Pipelines (bulk/SQL inference) https://github.com/databricks/spark-deep-learning
# MAGIC * Apache Spark + (DL4J | BigDL | TensorFlowOnSpark)
# MAGIC 
# MAGIC * DeepLearning4J can import your Keras model: https://deeplearning4j.org/model-import-keras
# MAGIC     * (which is a really nice contribution, but not magic -- remember the model is just a pile of weights, convolution kernels, etc. ... in the worst case, many thousands of floats)
# MAGIC 
# MAGIC * http://pipeline.io/ by Netflix and Databricks alum Chris Fregly
# MAGIC * MLeap http://mleap-docs.combust.ml/
# MAGIC 
# MAGIC ### Security and Robustness
# MAGIC 
# MAGIC A recent (3/2017) paper on general key failure modes is __Failures of Deep Learning__: https://arxiv.org/abs/1703.07950
# MAGIC 
# MAGIC Deep learning models are subject to a variety of unexpected perturbations and adversarial data -- even when they seem to "understand," they definitely don't understand in a way that is similar to us.
# MAGIC 
# MAGIC <img src="http://i.imgur.com/3LjF9xl.png">
# MAGIC 
# MAGIC Ian Goodfellow has distilled and referenced some of the research here: https://openai.com/blog/adversarial-example-research/
# MAGIC 
# MAGIC * He is also maintainer of an open-source project to measure robustness to adversarial examples, Clever Hans: https://github.com/tensorflow/cleverhans
# MAGIC * Another good project in that space is Foolbox: https://github.com/bethgelab/foolbox
# MAGIC 
# MAGIC ##### It's all fun and games until a few tiny stickers that a human won't even notice ... turn a stop sign into a "go" sign for your self-driving car ... __and that's exactly what this team of researchers has done__ in _Robust Physical-World Attacks on Machine Learning Models_: https://arxiv.org/pdf/1707.08945v1.pdf

# COMMAND ----------

# MAGIC %md 
# MAGIC # Final Notes
# MAGIC 
# MAGIC The research and projects are coming so fast that this will probably be outdated by the time you see it ...
# MAGIC 
# MAGIC #### 2017 is the last ILSVRC! http://image-net.org/challenges/beyond_ilsvrc.php
# MAGIC 
# MAGIC Try visualizing principal components of high-dimensional data with __TensorFlow Embedding Projector__ http://projector.tensorflow.org/
# MAGIC 
# MAGIC Or explore with Google / PAIR's Facets tool: https://pair-code.github.io/facets/
# MAGIC 
# MAGIC Visualize the behavior of Keras models with keras-vis: https://raghakot.github.io/keras-vis/
# MAGIC 
# MAGIC Want more out of Keras without coding it yourself? See if your needs are covered in the extension repo for keras, keras-contrib: https://github.com/farizrahman4u/keras-contrib
# MAGIC 
# MAGIC Interested in a slightly different approach to APIs, featuring interactive (imperative) execution? In the past year, a lot of people have started using PyTorch: http://pytorch.org/
# MAGIC 
# MAGIC __XLA__, an experimental compiler to make TensorFlow even faster: https://www.tensorflow.org/versions/master/experimental/xla/
# MAGIC 
# MAGIC ...and in addition to refinements of what we've already talked about, there is bleeding-edge work in
# MAGIC 
# MAGIC * Neural Turing Machines
# MAGIC * Code-generating Networks
# MAGIC * Network-designing Networks
# MAGIC * Evolution Strategies (ES) as an alternative to DQL / PG: https://arxiv.org/abs/1703.03864

# COMMAND ----------

# MAGIC %md 
# MAGIC # Books
# MAGIC 
# MAGIC To fill in gaps, refresh your memory, gain deeper intuition and understanding, and explore theoretical underpinnings of deep learning...
# MAGIC 
# MAGIC #### Easier intro books (less math)
# MAGIC 
# MAGIC *Hands-On Machine Learning with Scikit-Learn and TensorFlow: Concepts, Tools, and Techniques to Build Intelligent Systems* by Aurélien Géron 
# MAGIC 
# MAGIC *Deep Learning with Python* by Francois Chollet
# MAGIC 
# MAGIC *Fundamentals of Machine Learning for Predictive Data Analytics: Algorithms, Worked Examples, and Case Studies* by John D. Kelleher, Brian Mac Namee, Aoife D'Arcy
# MAGIC 
# MAGIC #### More thorough books (more math)
# MAGIC 
# MAGIC *Deep Learning* by Ian Goodfellow, Yoshua Bengio, Aaron Courville
# MAGIC 
# MAGIC *Information Theory, Inference and Learning Algorithms 1st Edition* by David J. C. MacKay 

# COMMAND ----------

