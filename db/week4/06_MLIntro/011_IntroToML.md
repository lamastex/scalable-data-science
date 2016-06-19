// Databricks notebook source exported at Sun, 19 Jun 2016 08:36:55 UTC


# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)





The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/week4/06_MLIntro/011_IntroToML.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):

[![sds/uji/week4/06_MLIntro/011_IntroToML](http://img.youtube.com/vi/_Lxtxmn0L-w/0.jpg)](https://www.youtube.com/v/_Lxtxmn0L-w?rel=0&autoplay=1&modestbranding=1&start=0&end=1494)





**Some very useful resources we will weave around** for Statistical Learning, Data Mining, Machine Learning:
*  January 2014, Stanford University professors Trevor Hastie and Rob Tibshirani (authors of the legendary Elements of Statistical Learning textbook) taught an online course based on their newest textbook, An Introduction to Statistical Learning with Applications in R (ISLR).
  * [http://www.r-bloggers.com/in-depth-introduction-to-machine-learning-in-15-hours-of-expert-videos/](http://www.r-bloggers.com/in-depth-introduction-to-machine-learning-in-15-hours-of-expert-videos/)
  * free PDF of the ISLR book: [http://www-bcf.usc.edu/~gareth/ISL/](http://www-bcf.usc.edu/~gareth/ISL/)
* A more theoretically sound book with interesting aplications is *Elements of Statistical Learning* by the Stanford Gang of 3 (Hastie, Tibshirani and Friedman):
  * free PDF of the 10th printing: [http://statweb.stanford.edu/~tibs/ElemStatLearn/printings/ESLII_print10.pdf](http://statweb.stanford.edu/~tibs/ElemStatLearn/printings/ESLII_print10.pdf)
  * Solutions: [http://waxworksmath.com/Authors/G_M/Hastie/WriteUp/weatherwax_epstein_hastie_solutions_manual.pdf](http://waxworksmath.com/Authors/G_M/Hastie/WriteUp/weatherwax_epstein_hastie_solutions_manual.pdf)
* 2015, UCLA Ass. Professor Ameet Talwalkar (from Spark team) in BerkeleyX: CS190.1x Scalable Machine Learning in edX Archive from 2015
* Those at UC, Ilam may want to take the following honours courses:
  * Machine Learning from Computer Science Department for a broader vew of ML (logic, search, probabilistic/statistical learning) and/or 
  * Data Mining from School of Mathematics and Statistics for applied statistical learning methods
* My preferred book for statistical learning is by [Kevin P. Murphy, Machine Learning: A Probabilistic Perspective](https://www.cs.ubc.ca/~murphyk/MLbook/).





**Note:** This is an applied course in data science and we will quickly move to doing things with data.  You have to do work to get a deeper mathematical understanding or take other courses.  We will focus on intution here and the distributed ML Pipeline in action.

I may consider teaching a theoretical course in statistical learning if there is enough interest for those with background in:
* Real Analysis, 
* Geometry, 
* Combinatorics and 
* Probability, 
in the future.

Such a course could be an expanded version of the following notes built on the classic works of [Luc Devroye](http://luc.devroye.org/) and the L1-School of Statistical Learning:
  * Short course on Non-parametric Density Estimation at Centre for Mathematics and its Applications, Ecole Polytechnique, Palaiseau, France 
     * [http://www.math.canterbury.ac.nz/~r.sainudiin/courses/Short_Courses/EcolePolytechnique_MDE_2013.pdf](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/Short_Courses/EcolePolytechnique_MDE_2013.pdf)





### Machine Learning Introduction 

#### ML Intro high-level by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning
**(watch now 4:14)**:

[![ML Intro high-level by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/NJZht7aV2NQ/0.jpg)](https://www.youtube.com/v/NJZht7aV2NQ?rel=0&autoplay=1&modestbranding=1&start=1)





### Ameet's Summary of Machine Learning at a High Level

* rough definition of machine learning.
    * constructing and studying methods that learn from and make predictions on data.

* This broad area involves tools and ideas from various domains, including:
  *  computer science, 
  * probability and statistics, 
  * optimization, and 
  * linear algebra.

 * Common examples of ML, include:
    * face recognition, 
    * link prediction, 
    * text or document classification, eg.::
      * spam detection, 
      * protein structure prediction
      * teaching computers to play games (go!)
    
#### Some common terminology 
##### using example of spam detection as a running example.
    
* the data points we learn from are call observations:
    * they are items or entities used for::
      * learning or 
      * evaluation.

* in the context of spam detection,
    * emails are our observations.
    * Features are attributes used to represent an observation.
    * Features are typically numeric, 
      *  and in spam detection, they can be:
        * the length,
        * the date, or 
        * the presence or absence of keywords in emails.
    * Labels are values or categories assigned to observations.
      *  and in spam detection, they can be:
        * an email being defined as spam or not spam.

* Training and test data sets are the observations that we use to train and evaluate a learning algorithm.
    ...
    
* **Pop-Quiz**
  * What is the difference between supervised and unsupervised learning?





##### For a Stats@Stanford Hastie-Tibshirani Perspective on Supervised and Unsupervised Learning:

**(watch later 12:12)**:

[![Supervised and Unsupervised Learning (12:12)](http://img.youtube.com/vi/LvaTokhYnDw/0.jpg)](https://www.youtube.com/v/LvaTokhYnDw?rel=0&autoplay=1&modestbranding=1&start=1)





#### Typical Supervised Learning Pipeline by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning
**(watch now 2:07)**:

[![Typical Supervised Learning Pipeline by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/XPxOL1--GXo/0.jpg)](https://www.youtube.com/v/XPxOL1--GXo?rel=0&autoplay=1&modestbranding=1&start=1)





Take your own notes if you want ....





#### Sample Classification Pipeline (Spam Filter) by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning
**(watch later 7:48)**:

[![Typical Supervised Learning Pipeline by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/Q7hxwkG9hNE/0.jpg)](https://www.youtube.com/v/Q7hxwkG9hNE?rel=0&autoplay=1&modestbranding=1&start=1)






# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)
