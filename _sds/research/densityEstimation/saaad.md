---
layout: single
title: Potential Research - Project SAAAD
permalink: /sds/research/densityEstimation/saaad/
author_profile: true
header:
  overlay_color: "#5e616c"
  overlay_image: /sds/research/densityEstimation/sahde/notes/MDE_20161010_141708_01.jpg
  caption: 
excerpt: 'for Scalable Mathematical Statistical Experiments.<br /><br /><br />'
---

{% include toc %}

# Project SAAAD:<br /> Scalable Adaptive Auto-encoded Anomaly Detection

Project SAAD is an effort to evaluate the possibilites for further extensions of [Project SAHDE](https://lamastex.github.io/scalable-data-science/sds/research/densityEstimation/sahde/) via *auto-encoder mapped regular pavings* as *auto-encoded statistical regular pavings* that are ultimately capable of appropriate tree arithmetics formalised and explored in *Relevant Codes and Papers* section below.

This programme is partly supported by:

* [databricks academic partners program](https://academics.cloud.databricks.com) for distributed cloud computing
* research time for this project was party due to:
	* 2015, 2016 by the project [CORCON: Correctness by Construction](http://corcon.net/about/), Seventh Framework Programme of the European Union, Marie Curie Actions-People, International Research Staff Exchange Scheme with counter-part funding by The Royal Society of New Zealand 
	* 2017, Researcher Position, Department of Mathematics, Uppsala University, Uppsala, Sweden. 

## Relevant Codes and Papers

<ul>
<li>Data-adaptive histograms through statistical regular pavings, Raazesh Sainudiin, Gloria Teng, Jennifer Harlow and Warwick Tucker, 2016 (<a class="linkitem" href="http://lamastex.org/preprints/20161121optMAPMDE.pdf">PDF</a> 1.8MB)</li>

<li>MRS 2.0: A C++ Class Library for Statistical Set Processing and Computer-Aided Proofs in Statistics (Version 2.0) [Software], Jennifer Harlow, Raazesh Sainudiin, Gloria Teng, Warwick Tucker and Thomas York, Available from <a class="linkitem" href="https://github.com/lamastex/mrs2">https://github.com/lamastex/mrs2</a>, 2017</li>

<li>An auto-validating, trans-dimensional, universal rejection sampler for locally Lipschitz arithmetical expressions, Raazesh Sainudiin and Thomas York, <a class="linkitem" href="http://interval.louisiana.edu/reliable-computing-journal/volume-18/reliable-computing-18-pp-015-054.pdf">Reliable Computing, vol.18, pp.15-54</a>, 2013 (<a class="linkitem" href="http://lamastex.org/preprints/avs_rc_2013.pdf">PDF</a> 2612KB)</li>

<li>Posterior expectation of regularly paved random histograms, Raazesh Sainudiin, Gloria Teng, Jennifer Harlow and Dominic Lee, <a class="linkitem" href="http://dx.doi.org/10.1145/2414416.2414422">ACM Trans. Model. Comput. Simul. 23, 1, Article 6, 20 pages</a>, 2013 (<a class="linkitem" href="http://lamastex.org/preprints/SubPavingMCMC.pdf">PDF</a> 1864KB)</li>

<li>Mapped Regular Pavings, Jennifer Harlow, Raazesh Sainudiin and Warwick Tucker, <a class="linkitem" href="http://interval.louisiana.edu/reliable-computing-journal/volume-16/reliable-computing-16-pp-252-282.pdf">Reliable Computing, vol. 16, pp. 252-282</a>, 2012 (<a class="linkitem" href="http://lamastex.org/preprints/MappedRegularPaving.pdf">PDF</a> 972KB)</li>

<li>Statistical regular pavings to analyze massive data of aircraft trajectories, Gloria Teng, Kenneth Kuhn and Raazesh Sainudiin, <a class="linkitem" href="http://arc.aiaa.org/doi/abs/10.2514/1.I010015">Journal of Aerospace Computing, Information, and Communication, Vol. 9, No. 1, pp. 14-25</a>, doi: 10.2514/1.I010015, 2012 (<a class="linkitem" href="http://lamastex.org/preprints/AAIASubPavingATC.ps">PS</a> 31MB or lossy <a class="linkitem" href="http://lamastex.org/preprints/AAIASubPavingATC.pdf">PDF</a> 2.9MB or <a class="linkitem" href="http://lamastex.org/preprints/AAIASubPavingATC_PNG.zip">zipped 26 PNG pages</a>) </li>
</ul>

# Anomaly Detection with Autoencoders

## References
* Some quick mathematical, statistical, computational background readings:
  * [Anomaly detection - Wikipedia](https://en.wikipedia.org/wiki/Anomaly_detection)
  * [Auto Encoders Chapter - Deep Learning Book](http://www.deeplearningbook.org/contents/autoencoders.html) (see references therein)
  * [Autoencoders - Stanford Tutorial](http://ufldl.stanford.edu/tutorial/unsupervised/Autoencoders/)
  * [Digit recognition (Image Search)](http://elkews.com/preview/565650)
  * ...
* Computing frameworks for scalable deep learning and auto-encoders in particular
  * [H2O Deep learning](http://h2o2016.wpengine.com/wp-content/themes/h2o2016/images/resources/DeepLearningBooklet.pdf)
  * Sparkling Water - H2O with Spark
  * IBM's BigDL in Spark
  * TensorFlow and Spark TensorFrames

 
# Some Background on Existing Industrial Solutions

* Some interesting industrial solutions already exist, including:
  * [https://community.tibco.com/wiki/anomaly-detection-autoencoder-machine-learning-template-tibco-spotfirer](https://community.tibco.com/wiki/anomaly-detection-autoencoder-machine-learning-template-tibco-spotfirer)
  * and others...

The content in the next section is just a 
```%sh
$ wget -k https://community.tibco.com/wiki/anomaly-detection-autoencoder-machine-learning-template-tibco-spotfirer
$ pandoc -f html -t markdown anomaly-detection-autoencoder-machine-learning-template-tibco-spotfirer > ex.md
$ vim ex.md
$ !date
$ Fri Aug 18 18:33:05 CEST 2017
```
It is meant to give a reasonably standard industrial solution to the problem and help set the context for industrially beneficial research directions. There are other competing solutions, but we will focus on this example for concreteness.

---
---

## Anomaly Detection with Autoencoder Machine Learning - Template for TIBCO Spotfire®

By Venkata Jagannath from [https://community.tibco.com/users/venkata-jagannath](https://community.tibco.com/users/venkata-jagannath)

Anomaly detection is a way of detecting abnormal behavior. The technique
first uses machine learning models to specify expected behavior and then
monitors new data to match and highlight unexpected behavior.


Use cases for Anomaly detection
-------------------------------

**Fighting Financial Crime** – In the financial world, trillions of
dollars’ worth of transactions happen every minute. Identifying
suspicious ones in real time can provide organizations the necessary
competitive edge in the market. Over the last few years, leading
financial companies have increasingly adopted big data analytics to
identify abnormal transactions, clients, suppliers, or other players.
Machine Learning models are used extensively to make predictions that
are more accurate.

**Monitoring Equipment Sensors** – Many different types of equipment,
vehicles and machines now have sensors.  Monitoring these sensor outputs
can be crucial to detecting and preventing breakdowns and disruptions.
 Unsupervised learning algorithms like Auto encoders are widely used to
detect anomalous data patterns that may predict impending problems. 

**Healthcare claims fraud** – Insurance fraud is a common occurrence in
the healthcare industry. It is vital for insurance companies to identify
claims that are fraudulent and ensure that no payout is made for those
claims. The economist recently published an article that estimated \$98
Billion as the cost of insurance fraud and expenses involved in fighting
it. This amount would account for around 10% of annual Medicare &
Medicaid spending. In the past few years, many companies have invested
heavily in big data analytics to build supervised, unsupervised and
semi-supervised models to predict insurance fraud.

**Manufacturing detects** – Auto encoders are also used in manufacturing
for finding defects. Manual inspection to find anomalies is a laborious
& offline process and building machine-learning models for each part of
the system is difficult. Therefore, some companies implemented an auto
encoder based process where sensor equipment data on manufactured
components is continuously fed into a database and any defects (i.e.
anomalies) are detected using the auto encoder model that scores the new
data.
[Example](http://webserv.lurpa.ens-cachan.fr/dcds_series/dcds11/fileadmin/user_upload/pdf/tilbury-dcds2011.pdf)

Techniques for Anomaly detection
--------------------------------

Companies around the world have used many different techniques to fight
fraud in their markets. While the below list is not comprehensive, three
anomaly detection techniques have been popular -

**Visual Discovery** - Anomaly detection can also be accomplished
through visual discovery. In this process, a team of data
analysts/business analysts etc. builds bar charts; scatter plots etc. to
find unexpected behavior in their business. This technique often
requires prior business knowledge in the industry of operation and a lot
of creative thinking to use the right visualizations to find the
answers.

**Supervised Learning** - Supervised Learning is an improvement over
visual discovery. In this technique, persons with business knowledge in
the particular industry label a set of data points as normal or anomaly.
An analyst then uses this labelled data to build machine learning models
that will be able to predict anomalies on unlabeled new data.

**Unsupervised Learning** - Another technique that is very effective but
is not as popular is Unsupervised learning. In this technique, unlabeled
data is used to build unsupervised machine learning models.  These
models are then used to predict new data. Since the model is tailored to
fit normal data, the small number of data points that are anomalies
stand out.

Some examples of unsupervised learning algorithms are -

**Auto encoders** – Unsupervised neural networks or auto encoders are
used to replicate the input dataset by restricting the number of hidden
layers in a neural network. A reconstruction error is generated upon
prediction. Higher the reconstruction error, higher the possibility of
that data point being an anomaly.

**Clustering** – In this technique, the analyst attempts to classify
each data point into one of many pre-defined clusters by minimizing the
within cluster variance. Models such as K-means clustering, K-nearest
neighbors etc. used for this purpose. A K-means or a KNN model serves
the purpose effectively since they assign a separate cluster for all
those data points that do not look similar to normal data.

**One-class support vector machine** – In a support vector machine, the
effort is to find a hyperplane that best divides a set of labelled data
into two classes. For this purpose, the distance between the two nearest
data points that lie on either side of the hyperplane is maximized. For
anomaly detection, a One-class support vector machine is used and those
data points that lie much farther away than the rest of the data are
considered anomalies.

**Time Series techniques** – Anomalies can also be detected through time
series analytics by building models that capture trend, seasonality and
levels in time series data. These models are then used along with new
data to find anomalies. [Industry example](http://nerds.airbnb.com/anomaly-detection/)

Auto encoders explained
-----------------------

Autoencoders use unsupervised neural networks that are both similar to
and different from a traditional feed forward neural network. It is
similar in that it uses the same principles (i.e. Backpropagation) to
build a model. It is different in that, it does not use a labelled
dataset containing a target variable for building the model. An
unsupervised neural network also known as an Auto encoder uses the
training dataset and attempts to replicate the output dataset by
restricting the hidden layers/nodes.

![](https://d2wh20haedxe3f.cloudfront.net/sites/default/files/autoencode_doc1.png)

The focus on this model is to learn an identity function or an
approximation of it that would allow it to predict an output that is
similar the input. The identity function achieves this by placing
restrictions on the number of hidden units in the data. For example, if
we have 10 columns in a dataset and only five hidden units, the neural
network is forced to learn a more restricted representation of the
input. By limiting the hidden units, we can force the model to learn a
pattern in the data if there indeed exists one.

Not restricting the number of hidden units and instead specifying a
‘sparsity’ constraint on the neural network can also find an interesting
structure.

Each of the hidden units can be either active or inactive and an
activation function such as ‘tanh’ or ‘Rectifier’ can be applied to the
input at these hidden units to change their state.

![](https://d2wh20haedxe3f.cloudfront.net/sites/default/files/autoencode_doc2.png)

Some forms of auto encoders are as follows –

-   Under complete Auto encoders
-   Regularized Auto encoders
-   Representational Power, Layer Size and Depth
-   Stochastic Encoders and Decoders
-   Denoising Auto encoders

A detailed explanation of each of these types of auto encoders is
available
[here](http://www.deeplearningbook.org/contents/autoencoders.html).

Spotfire Template for Anomaly detection
---------------------------------------

TIBCO Spotfire’s Anomaly detection template uses an auto encoder trained
in H2O for best in the market training performance. It can be configured
with document properties on Spotfire pages and used as a point and click
functionality.

[Download the
template](https://community.tibco.com/modules/anomaly-detection-template-tibco-spotfirer)
from the Component Exchange.  See documentation in the download
distribution for details on how to use this template

---
---

# Questions for AIM Day

## Methods in Anomaly Detection for Semi-supervised Problems with Human in the Loop
...

## Interactive Visualisation of the Autoencoder's Layers
...


## Relevant Papers




## Codes

Some interesting random codes that need exploring and experimenting:

* [https://github.com/jramapuram/LSTM_Anomaly_Detector](https://github.com/jramapuram/LSTM_Anomaly_Detector)
* ...

## LICENSE

Copyright 2016, 2017 Raazesh Sainudiin

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.



## Whiteboard discussion notes on 2017-08-18.
 
We will eventually lua/la/ka-tex mathematically from here...

![auto-encoder mapped regular pavings and naive probing](notes/saaad0.jpg)

## Random thoughts regarding anomaly detection with emphasis on fraud detection
Normally, anomaly detection is treated as an **unsupervised learning problem**, where the machine tries to build a model of the training data. Since an anomaly per definition is a data point that in some way is uncommon, it will not fit the machine's model, and the model can flag it as an anomaly. In the case of fraud detection, it is often the case that a small fraction (perhaps 1 ppm) of the data points represent known cases of fraud attempts. It would be wasteful to throw away this information when building the model. This would mean that we no longer have a unsupervised learning problem, but a **semi-supervised learning problem**. In a typical semi-supervised learning problem, the problem is to assign predefined labels to datapoints when the correct label is only known for a small fraction of the dataset. This is a very interesting idea, since it allows for using the power of big data, without having to pay the cost of correctly labeling the whole dataset. If we translate our fraud detection problem to this setting it means that we hava a big collection of datapoints which we want to label as either "fraud" or non-fraud". However, in contrary to the standard setup for semi-supervised learning, we only have known examples from the "fraud" class. This (at least formally) puts us in another class of learning problems, namely **PU learning**, where the dateset is split into two classes, the positive class, *P*, and the unlabel class, *U*.

After running the fraud detection model on our data, we are likely to be given suspected cases of fraud, which can be correctly labelled by an expert. This means that we can improve our dataset with more labelled points and retrain our model using the new information. At this point however, we probably have some confirmed examples of the non-fraud class, and from this point we no longer have a PU learning problem.

One important note about running a semi-supervised learning for anomaly detection is that some people actually advise against training a model on known anomalies, since this will produce a model that is good at detecting anomalies of known kinds, but which probabliy is less able to detect new kind of anomalies. This is extra important in fraud detection, since fraudsters are likely to continually come up with new schemes to trick the model.

Another related thought is that with the relabelling scheme described above, all datapoints labelled non-fraud will be such points which the model originally had problems distiguishing from fraud. This is likely to introduce instability to the model.   For this reason, it might be worthwile to put some effort into correctly classifying also some datapointcalls ts which are very unlikely to be fradulent. With respect to this, it might be interesting to read some of the literature on **active learning**. Active learning is an iterative type of semi-supervised learning where the learning algorithm is active and can purposefully queary a user for correct data labels. Ideally, the algorithm can determine which unlabelled points it would be most befefitial to learn the correct labels for.
