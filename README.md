Scalable Data Science
=======

Scalable data science is a technical course in the area of Big Data, aimed at the needs of the
emerging data industry in Christchurch and those of certain academic domain experts across
University of Canterbury's Colleges, including, Arts, Science and Engineering. This course uses
Apache Spark, a fast and general engine for large-scale data processing via databricks to compute
with datasets that won't fit in a single computer. The course will introduce Spark’s core concepts
via hands-on coding, including resilient distributed datasets and map-reduce algorithms, DataFrame
and Spark SQL on Catalyst, scalable machine-learning pipelines in MlLib and vertex programs using
the distributed graph processing framework of GraphX. We will solve instances of real-world big data
decision problems from various scientific domains.

This is being prepared by Raazesh Sainudiin and Sivanand Sivaram
with assistance from Paul Brouwers, Dillon George and Ivan Sadikov.

See [http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/) for content as cloud-uploadable and locally browsable notebooks. 

The Gitbook version of this content is [https://www.gitbook.com/book/raazesh-sainudiin/scalable-data-science/details](https://www.gitbook.com/book/raazesh-sainudiin/scalable-data-science/details).

All course projects by seven enrolled and four observing students for Semester 1 of 2016 at UC, Ilam are part of this content.


## Contribute

All course content is currently being pushed by Raazesh Sainudiin after it has been tested in
Databricks cloud (mostly under Spark 1.6 and some involving Magellan under Spark 1.5.1).

The markdown version for `gitbook` is generated from the Databricks `.scala` and other source codes.
The gitbook version will lag behind the Databricks version available in the Databricks cloud. The following issues need to be resolved:

* dialects of Tex between databricks and github are different (need to babel out into github-compatible tex from databricks compatible tex)
* need to find a stable solution for the output of various databricks cells to be shown in gitbook, including those from `display_HTML` and `frameIt` with their in-place embeds of web content.

Please feel free to fork the github repository: 

* [https://github.com/raazesh-sainudiin/scalable-data-science](https://github.com/raazesh-sainudiin/scalable-data-science).

Unfortunately, pull requests cannot be accepted until the end of June 2016 when the course completes
along with the course projects.

Furthermore, due to the anticipation of Spark 2.0 this mostly Spark 1.6 version could be enhanced with a 2.0 version-specific upgrade. 

Please send any typos or suggestions to raazesh.sainudiin@gmail.com

Please read a note on [babel](https://github.com/raazesh-sainudiin/scalable-data-science/babel/README.md) to understand how the gitbook is generated from the `.scala` source of the databricks notebook.

## Supported By
[Databricks Academic Partners Program](https://databricks.com/academic) and [Amazon Web Services Educate](https://www.awseducate.com/microsite/CommunitiesEngageHome).

Raazesh Sainudiin, 
Laboratory for Mathematical Statistical Experiments, Christchurch Centre 
and School of Mathematics and Statistics, 
University of Canterbury, 
Private Bag 4800, 
Christchurch 8041, 
Aotearoa New Zealand 

Sun Jun 19 21:59:19 NZST 2016
