
# Questions for AIM Day

Prepared by Håkan Persson and Raazesh Sainudiin for [Combient AB](https://combient.com/).

We aim to explore the use of autoencoders for anomaly detection in various 'big-data' problems that have the following complexities:

* data volumes are big and one needs distributed in-memory fault-tolerant computing frameworks such as [Apache Spark](http://spark.apache.org/)
* learning is 
  * semi-supervised (so a small fraction of the dataset is labelled) and
  * interactive (with humans-in-the-loop)
* the phenomena is time-varying

These questions are addressed to experts in statistical/machine learning and visualisation or human-computer interactions. 
The background information is given in the list of references below for concreteness.
Plese see [https://tinyurl.com/yaep8k2w](https://lamastex.github.io/scalable-data-science/sds/research/densityEstimation/saaad/) for further industrial/academic context.

![](https://tr3.cbsistatic.com/hub/i/r/2016/04/15/c326870e-5682-40f6-9085-6e95cea67e7e/resize/770x/166a5883e09792f95d22f3382b8c581b/ai2-visual-credit-mit-csail.jpg)

## Semi-supervised Anomaly Detection with Human-in-the-Loop

* What algorithms are there for incorporating [expert human feedback into anomaly detection](http://web.engr.oregonstate.edu/~tgd/publications/das-wong-dietterich-fern-emmott-incorporating-expert-feedback-into-active-anomaly-discovery-icdm2016.pdf), especially with auto-encoders, and what are their limitations when scaling to terabytes of data? 
* Can one incorporate [expert human feedback with anomaly detection](http://people.csail.mit.edu/kalyan/AI2_Paper.pdf) for continuous time series data of large networks (eg. network logs data such as netflow logs)?
* How do you avoid overfitting to known types of anomalies that make up only a small fraction of all events?
* How can you allow for new (yet unknown anomalies) to be discovered by the model, i.e. account for new types of anomalies over time?
* Can [Ladder Networks](http://rinuboney.github.io/2016/01/19/ladder-network.html) which were specially developed for semi-supervised learning be adapted for generic anomaly detection (beyond standard datasets)?
* Can a loss function be specified for an auto-encoder with additional classifier node(s) for rare anomalous events of several types via interaction with the domain expert?    
* Are there natural parametric families of loss functions for tuning hyper-parameters, where the loss functions can account for the budgeting costs of distinct set of humans with different hourly costs and tagging capabilities within a generic human-in-the-loop model for anomaly detection?

Some ideas to start brain-storming:

* For example, the loss function in the last question above could perhaps be justified using notions such as query-efficiency in the sense of involving only a small amount of interaction with the teacher/domain-expert ([Supervised Clustering, NIPS Proceedings, 2010](https://papers.nips.cc/paper/4115-supervised-clustering.pdf)). 
* Do an SVD of the network data when dealing with time-series of large networks that are [tall and skinny](https://github.com/apache/spark/blob/master/examples/src/main/scala/org/apache/spark/examples/mllib/TallSkinnySVD.scala) and look at the distances between the dominant singular vectors, perhaps?

## Interactive Visualisation for the Human-in-the-Loop

Given the crucial requirement for rich visual interactions between the algorithm and the human-in-the-loop, what are natural open-source frameworks for programmatically enriching this human-algorithm interaction via visual inspection and interrogation (such as SVDs of activations of rare anomalous events for instance).

For example, how can open source tools be integrated into Active-Learning and other human-in-the-loop Anomaly Detectors? 
Some such tools include:

* [facets](https://research.googleblog.com/2017/07/facets-open-source-visualization-tool.html) from [https://ai.google/pair](https://ai.google/pair)
* [http://projector.tensorflow.org](http://projector.tensorflow.org/)
* [https://distill.pub/2016/misread-tsne/](https://distill.pub/2016/misread-tsne/)
* [https://github.com/vegas-viz/Vegas](https://github.com/vegas-viz/Vegas)
* ...

Beyond, visualising the ML algorithms, often the Human-in-the-Loop needs to see the details of the raw event that triggered the Anomaly. 
And typically this event needs to be seen in the context of other related and relevant events, including it anomaly score with some some historical comparisons of similar events from a no-SQL query. 
What are some natural frameworks for being able to click the event of interest (say those alerted by the algorithm) and visualise the raw event details (usually a JSON record or a row of a CSV file) in order to make an informed decision. 
Some such frameworks include:

* [https://d3js.org/](https://d3js.org/)
* [https://vega.github.io/vega/](https://vega.github.io/vega/)
* [https://processing.org/](https://processing.org/)
* [https://gephi.org/](https://gephi.org/)
* [http://dygraphs.com/](http://dygraphs.com/)
* [https://github.com/vegas-viz/Vegas](https://github.com/vegas-viz/Vegas)

### Background Readings

* [MIT shows how AI cybersecurity excels by keeping humans in the loop](http://www.techrepublic.com/article/mit-shows-how-ai-cybersecurity-excels-by-keeping-humans-in-the-loop/)
* [AI2: Training a big data machine to defend, Kalyan Veeramachaneni, Ignaciao Arnaldo, Alfredo Cuesta-Infante, Vamsi Korrapati, Costas Bassias and Ke Li, 2016](http://people.csail.mit.edu/kalyan/AI2_Paper.pdf)
* [Supervised Clustering, Pranjal Awasthi and Reza Bosagh Zadeh, NIPS Proceedings, 2010](https://papers.nips.cc/paper/4115-supervised-clustering.pdf)
  * [Video Lecture 4 minutes](http://www.quizover.com/oer/course/supervised-clustering-by-reza-bosagh-zadeh-videolectures-net-oer)
* ["Adversarial Autoencoders" Ian J. Goodfellow et al.](https://arxiv.org/pdf/1511.05644.pdf)
* ["Variational Autoencoder based Anomaly Detection using Reconstruction Probability", Jinwon An and Sungzoon Cho](http://dm.snu.ac.kr/static/docs/TR/SNUDM-TR-2015-03.pdf)
* ["Loda: Lightweight on-line detector of anomalies", Tomáš Pevný](https://link.springer.com/content/pdf/10.1007%2Fs10994-015-5521-0.pdf)
* ["Incorporating Expert Feedback into Active Anomaly Discovery", Das et al.](http://web.engr.oregonstate.edu/~tgd/publications/das-wong-dietterich-fern-emmott-incorporating-expert-feedback-into-active-anomaly-discovery-icdm2016.pdf)
* ["Incorporating Feedback into Tree-based Anomaly Detection", Das et al.](http://poloclub.gatech.edu/idea2017/papers/p25-das.pdf)
* [Embedding Projector: Interactive Visualization and Interpretation of Embeddings, Daniel Smilkov, Nikhil Thorat, Charles Nicholson, Emily Reif, Fernanda B. Viégas, Martin Wattenberg, 2016](https://arxiv.org/abs/1611.05469)
* [Direct-Manipulation Visualization of Deep Networks, Daniel Smilkov, Shan Carter, D. Sculley, Fernanda B. Viégas and Martin Wattenberg, 2017](https://arxiv.org/abs/1708.03788)
* ["Autoencoders, Unsupervised Learning, and Deep Architectures", Pierre Baldi.](http://proceedings.mlr.press/v27/baldi12a/baldi12a.pdf)
* ["Variational Autoencoders Explained", Kevin Frans](http://kvfrans.com/variational-autoencoders-explained/)
* ["Tutorial on Variational Autoencoders", Carl Doersch](https://arxiv.org/abs/1606.05908)
* ["Generative Adversarial Nets", Ian J. Goodfellow et al.](https://arxiv.org/pdf/1406.2661.pdf)
* ["Adversarial Autoencoders", Ian J. Goodfellow et al.](https://arxiv.org/pdf/1511.05644.pdf)
* ["Generative Adversarial Networks Explained", Kevin Frans](http://kvfrans.com/generative-adversial-networks-explained/)
* ["Introduction to Semi-Supervised Learning with Ladder Networks", Rinu Boney](http://rinuboney.github.io/2016/01/19/ladder-network.html)
* ["Semi-Supervised Learning with Ladder Networks", Antti Rasmus et al.](https://arxiv.org/pdf/1507.02672.pdf)

