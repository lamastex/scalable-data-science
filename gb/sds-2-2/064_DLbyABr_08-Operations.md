[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

This is used in a non-profit educational setting with kind permission of [Adam Breindel](https://www.linkedin.com/in/adbreind).
This is not licensed by Adam for use in a for-profit setting. Please contact Adam directly at `adbreind@gmail.com` to request or report such use cases or abuses.
A few minor modifications and additional mathematical statistical pointers have been added by Raazesh Sainudiin when teaching PhD students in Uppsala University.

Operations in the Real World
============================

Practical Options, Tools, Patterns, and Considerations for Deep Learning
------------------------------------------------------------------------

There are various ways to use deep learning in an enterprise setting that may not require designing your own networks!

### Ways to Use Deep Learning

(in order from least complex/expensive investment to most)

\[1\] Load and use a pretrained model

Many of the existing toolkit projects offer models pretrained on datasets, including
\* natural language corpus models
\* image datasets like ImageNet (http://www.image-net.org/) or Google's Open Image Dataset (https://research.googleblog.com/2016/09/introducing-open-images-dataset.html)
\* video datasets like the YouTube 8 million video dataset (https://research.googleblog.com/2016/09/announcing-youtube-8m-large-and-diverse.html)

\[2\] Augmenting a pretrained model with new training data, or using it in a related context (see Transfer Learning)

\[3\] Use a known, established network type (topology) but train on your own data

\[4\] Modify established network models for your specific problem

\[5\] Research and experiment with new types of models

**Just because Google DeepMind, Facebook, and Microsoft are getting press for doing a lot of new research doesn't mean you have to do it too.**

<img src="http://i.imgur.com/XczCfNR.png" width=500>
<img src="http://i.imgur.com/vcaj99I.jpg" width=500>

Data science and machine learning is challenging in general for enterprises (though some industries, such as pharma, have been doing it for a long time). Deep learning takes that even further, since deep learning experiments may require new kinds of hardware ... in some ways, it's more like chemistry than the average IT project!

### Tools and Processes for your Deep Learning Pipeline

#### Data Munging

Most of the deep learning toolkits are focused on model-building performance or flexibility, less on production data processing.

However, Google recently introduced `tf.Transform`, a data processing pipeline project: https://github.com/tensorflow/transform

and Dataset, an API for data processing: https://www.tensorflow.org/api\_docs/python/tf/contrib/data/Dataset

TensorFlow can read from HDFS and run on Hadoop although it *does not* scale out automatically on a Hadoop/Spark cluster: https://www.tensorflow.org/deploy/hadoop

Falling back to "regular" tools, we have Apache Spark for big data, and the Python family of pandas, sklearn, scipy, numpy.

#### Experimenting and Training

Once you want to scale beyond your laptop, there are few options...

-   AWS GPU-enabled instances
-   Deep-learning-infrastructure as a Service
    -   EASY
        -   "Floyd aims to be the Heroku of Deep Learning" https://www.floydhub.com/
        -   "Effortless infrastructure for deep learning" https://www.crestle.com/
        -   "GitHub of Machine Learning / We provide machine learning platform-as-a-service." https://valohai.com/
        -   "Machine Learning for Everyone" (may be in closed beta) https://machinelabs.ai
        -   Algorithms as a service / model deployment https://algorithmia.com/
    -   MEDIUM Google Cloud Platform "Cloud Machine Learning Engine" https://cloud.google.com/ml-engine/
    -   HARDER Amazon Deep Learning AMI + CloudFormation https://aws.amazon.com/blogs/compute/distributed-deep-learning-made-easy/
-   On your own infrastructure or VMs
    -   Distributed TensorFlow is free, OSS
    -   Apache Spark combined with Intel BigDL (CPU) or DeepLearning4J (GPU)
    -   TensorFlowOnSpark
    -   CERN Dist Keras (Spark + Keras) https://github.com/cerndb/dist-keras

#### Frameworks

We've focused on TensorFlow and Keras, because that's where the "center of mass" is at the moment.

But there are lots of others. Major ones include:
\* Caffe
\* PaddlePaddle
\* Theano
\* CNTK
\* MXNet
\* DeepLearning4J
\* BigDL
\* Torch/PyTorch
\* NVIDIA Digits

and there are at least a dozen more minor ones.

#### Taking Your Trained Model to Production

Most trained models can predict in production in near-zero time. (Recall the forward pass is just a bunch of multiplication and addition with a few other calculations thrown in.)

For a neat example, you can persist Keras models and load them to run live in a browser with Keras.js

See Keras.js for code and demos: https://github.com/transcranial/keras-js

<img src="http://i.imgur.com/5xx62zw.png" width=700>

TensorFlow has an Android example at https://github.com/tensorflow/tensorflow/tree/master/tensorflow/examples/android

and Apple CoreML supports Keras models: https://developer.apple.com/documentation/coreml/converting*trained*models*to*core\_ml

(remember, the model is already trained, we're just predicting here)

#### And for your server-side model update-and-serve tasks, or bulk prediction at scale...

(imagine classifying huge batches of images, or analyzing millions of chat messages or emails)

-   TensorFlow has a project called TensorFlow Serving: https://tensorflow.github.io/serving/
-   Spark Deep Learning Pipelines (bulk/SQL inference) https://github.com/databricks/spark-deep-learning
-   Apache Spark + (DL4J \| BigDL \| TensorFlowOnSpark)

-   DeepLearning4J can import your Keras model: https://deeplearning4j.org/model-import-keras
    -   (which is a really nice contribution, but not magic -- remember the model is just a pile of weights, convolution kernels, etc. ... in the worst case, many thousands of floats)
-   http://pipeline.io/ by Netflix and Databricks alum Chris Fregly
-   MLeap http://mleap-docs.combust.ml/

### Security and Robustness

A recent (3/2017) paper on general key failure modes is **Failures of Deep Learning**: https://arxiv.org/abs/1703.07950

Deep learning models are subject to a variety of unexpected perturbations and adversarial data -- even when they seem to "understand," they definitely don't understand in a way that is similar to us.

<img src="http://i.imgur.com/3LjF9xl.png">

Ian Goodfellow has distilled and referenced some of the research here: https://openai.com/blog/adversarial-example-research/
\* He is also maintainer of an open-source project to measure robustness to adversarial examples, Clever Hans: https://github.com/tensorflow/cleverhans
\* Another good project in that space is Foolbox: https://github.com/bethgelab/foolbox

##### It's all fun and games until a few tiny stickers that a human won't even notice ... turn a stop sign into a "go" sign for your self-driving car ... **and that's exactly what this team of researchers has done** in *Robust Physical-World Attacks on Machine Learning Models*: https://arxiv.org/pdf/1707.08945v1.pdf

Final Notes
===========

The research and projects are coming so fast that this will probably be outdated by the time you see it ...

#### 2017 is the last ILSVRC! http://image-net.org/challenges/beyond\_ilsvrc.php

Try visualizing principal components of high-dimensional data with **TensorFlow Embedding Projector** http://projector.tensorflow.org/

Or explore with Google / PAIR's Facets tool: https://pair-code.github.io/facets/

Visualize the behavior of Keras models with keras-vis: https://raghakot.github.io/keras-vis/

Want more out of Keras without coding it yourself? See if your needs are covered in the extension repo for keras, keras-contrib: https://github.com/farizrahman4u/keras-contrib

Interested in a slightly different approach to APIs, featuring interactive (imperative) execution? In the past year, a lot of people have started using PyTorch: http://pytorch.org/

**XLA**, an experimental compiler to make TensorFlow even faster: https://www.tensorflow.org/versions/master/experimental/xla/

...and in addition to refinements of what we've already talked about, there is bleeding-edge work in
\* Neural Turing Machines
\* Code-generating Networks
\* Network-designing Networks
\* Evolution Strategies (ES) as an alternative to DQL / PG: https://arxiv.org/abs/1703.03864

Books
=====

To fill in gaps, refresh your memory, gain deeper intuition and understanding, and explore theoretical underpinnings of deep learning...

#### Easier intro books (less math)

*Hands-On Machine Learning with Scikit-Learn and TensorFlow: Concepts, Tools, and Techniques to Build Intelligent Systems* by Aurélien Géron

*Deep Learning with Python* by Francois Chollet

*Fundamentals of Machine Learning for Predictive Data Analytics: Algorithms, Worked Examples, and Case Studies* by John D. Kelleher, Brian Mac Namee, Aoife D'Arcy

#### More thorough books (more math)

*Deep Learning* by Ian Goodfellow, Yoshua Bengio, Aaron Courville

*Information Theory, Inference and Learning Algorithms 1st Edition* by David J. C. MacKay