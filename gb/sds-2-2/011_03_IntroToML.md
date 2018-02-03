[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

Introduction to Machine Learning
================================

**Some very useful resources we will weave around** for Statistical Learning, Data Mining, Machine Learning:

-   January 2014, Stanford University professors Trevor Hastie and Rob Tibshirani (authors of the legendary Elements of Statistical Learning textbook) taught an online course based on their newest textbook, An Introduction to Statistical Learning with Applications in R (ISLR).
-   <http://www.r-bloggers.com/in-depth-introduction-to-machine-learning-in-15-hours-of-expert-videos/>
-   free PDF of the ISLR book: <http://www-bcf.usc.edu/~gareth/ISL/>
-   A more theoretically sound book with interesting aplications is *Elements of Statistical Learning* by the Stanford Gang of 3 (Hastie, Tibshirani and Friedman):
-   free PDF of the 10th printing: <http://statweb.stanford.edu/~tibs/ElemStatLearn/printings/ESLII_print10.pdf>
-   Solutions: <http://waxworksmath.com/Authors/G_M/Hastie/WriteUp/weatherwax_epstein_hastie_solutions_manual.pdf>
-   2015, UCLA Ass. Professor Ameet Talwalkar (from Spark team) in BerkeleyX: CS190.1x Scalable Machine Learning in edX Archive from 2015
-   My preferred book for statistical learning is by [Kevin P. Murphy, Machine Learning: A Probabilistic Perspective](https://www.cs.ubc.ca/~murphyk/MLbook/).

Deep Learning is a very popular method currently (2017) in Machine Learning and I recommend Andrew Ng's free course in Courseera for this.

**Note:** This is an applied course in data science and we will quickly move to doing things with data. You have to do work to get a deeper mathematical understanding or take other courses. We will focus on intution here and the distributed ML Pipeline in action.

*I am assuming several of you have or are taking ML courses from experts in IT Department at Uppsala (if not, you might want to consider this seriously). In this course we will focus on getting our hads dirty quickly with some level of common understanding across all the disciplines represented here. Please dive deep at your own time to desired depths and tangents based on your background.*

I may consider teaching a short theoretical course in statistical learning if there is enough interest for those with background in: \* Real Analysis, \* Geometry, \* Combinatorics and \* Probability, in the future.

Such a course could be an expanded version of the following notes built on the classic works of [Luc Devroye](http://luc.devroye.org/) and the L1-School of Statistical Learning: \* Short course on Non-parametric Density Estimation at Centre for Mathematics and its Applications, Ecole Polytechnique, Palaiseau, France \* <http://lamastex.org/courses/Short_Courses/EcolePolytechnique_MDE_2013.pdf>

### Machine Learning Introduction

#### ML Intro high-level by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning

**(watch now 4:14)**:

Ameet's course is in databricks guide for your convenience:

-   <https://docs.databricks.com/spark/1.6/training/scalable-machine-learning-cs190x-2015/index.html>

[![ML Intro high-level by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/NJZht7aV2NQ/0.jpg)](https://www.youtube.com/watch?v=NJZht7aV2NQ?rel=0&autoplay=1&modestbranding=1&start=1)

### Ameet's Summary of Machine Learning at a High Level

-   rough definition of machine learning.
    -   constructing and studying methods that learn from and make predictions on data.
-   This broad area involves tools and ideas from various domains, including:
-   computer science,
-   probability and statistics,
-   optimization, and
-   linear algebra.

-   Common examples of ML, include:
    -   face recognition,
    -   link prediction,
    -   text or document classification, eg.::
    -   spam detection,
    -   protein structure prediction
    -   teaching computers to play games (go!)

#### Some common terminology

##### using example of spam detection as a running example.

-   the data points we learn from are call observations:
    -   they are items or entities used for::
    -   learning or
    -   evaluation.
-   in the context of spam detection,
    -   emails are our observations.
    -   Features are attributes used to represent an observation.
    -   Features are typically numeric,
    -   and in spam detection, they can be:
        -   the length,
        -   the date, or
        -   the presence or absence of keywords in emails.
    -   Labels are values or categories assigned to observations.
    -   and in spam detection, they can be:
        -   an email being defined as spam or not spam.
-   Training and test data sets are the observations that we use to train and evaluate a learning algorithm. ...

-   **Pop-Quiz**
-   What is the difference between supervised and unsupervised learning?

##### For a Stats@Stanford Hastie-Tibshirani Perspective on Supervised and Unsupervised Learning:

**(watch later 12:12)**:

[![Supervised and Unsupervised Learning (12:12)](http://img.youtube.com/vi/LvaTokhYnDw/0.jpg)](https://www.youtube.com/watch?v=LvaTokhYnDw?rel=0&autoplay=1&modestbranding=1&start=1)

#### Typical Supervised Learning Pipeline by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning

**(watch now 2:07)**:

This course is in databricks guide for your convenience:

-   <https://docs.databricks.com/spark/1.6/training/scalable-machine-learning-cs190x-2015/index.html>

[![Typical Supervised Learning Pipeline by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/XPxOL1--GXo/0.jpg)](https://www.youtube.com/watch?v=XPxOL1--GXo?rel=0&autoplay=1&modestbranding=1&start=1)

Take your own notes if you want ....

#### Sample Classification Pipeline (Spam Filter) by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning

This course is in databricks guide for your convenience:

-   <https://docs.databricks.com/spark/1.6/training/scalable-machine-learning-cs190x-2015/index.html>

**(watch later 7:48)**:

[![Typical Supervised Learning Pipeline by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/Q7hxwkG9hNE/0.jpg)](https://www.youtube.com/watch?v=Q7hxwkG9hNE?rel=0&autoplay=1&modestbranding=1&start=1)