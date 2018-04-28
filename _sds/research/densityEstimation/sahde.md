---
layout: single
title: Current Research - Project SAHDE
permalink: /sds/research/densityEstimation/sahde/
author_profile: true
header:
  overlay_color: "#5e616c"
  overlay_image: /sds/research/densityEstimation/sahde/notes/MDE_20161010_141708_01.jpg
  caption: 
excerpt: 'for Scalable Mathematical Statistical Experiments.<br /><br /><br />'
---

{% include toc %}

## Project SAHDE:<br /> Scalable Adaptive Histogram Density Estimation 

This programme is partly supported by:

* [databricks academic partners program](https://academics.cloud.databricks.com) for distributed cloud computing
* research time for this project was party due to:
	* 2009-2015 by industrial consulting revenues of Raazesh Sainudiin
	* 2015, 2016 by the project [CORCON: Correctness by Construction](http://corcon.net/about/), Seventh Framework Programme of the European Union, Marie Curie Actions-People, International Research Staff Exchange Scheme with counter-part funding by The Royal Society of New Zealand 

Project SAHDE is an effort to create a scalable version of the adaptive histogram density estimators implemented in:
* [MRS 2.0, a C++ class library for statistical set processing and computer-aided proofs in statistics](https://github.com/raazesh-sainudiin/mrs2).

based on mathematical statistical notions in:
* Data-adaptive histograms through statistical regular pavings, Raazesh Sainudiin, Gloria Teng, Jennifer Harlow and Warwick Tucker, 2016 ([PDF](http://lamastex.org/preprints/20161121optMAPMDE.pdf) 1.8MB)

## LICENSE

The license for [mrs2](https://github.com/raazesh-sainudiin/mrs2) is GNU General Public License (GPL) and that for SAHDE Project is [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0).

## Current Sub-Projects of SAHDE 

-  [SparkDensityTree](https://github.com/TiloWiklund/SparkDensityTree) for scalable density estimation using optimally smoothed L2-risk minimizing penalties (*in progress*)
-  [SparkOnlineLearning](https://github.com/BennyAvelin/SparkOnlineLearning) has potential for streaming tree arithmetic by extending from the Scala trees in [SparkDensityTree](https://github.com/TiloWiklund/SparkDensityTree).

### Blackboard discussion notes at LaMaStEx on 2016-10-08.
 
We will eventually lua/la/ka-tex mathematically here..

![Minimum Density Estimation 00](notes/MDE_20161010_141701_00.jpg)

![Minimum Density Estimation 01](notes/MDE_20161010_141708_01.jpg)

![Minimum Density Estimation 02](notes/MDE_20161010_141714_02.jpg)

