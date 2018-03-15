// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
// MAGIC 
// MAGIC ## Supervised Clustering with Decision Trees
// MAGIC 
// MAGIC ### Visual Introduction to decision trees and application to hand-written digit recognition
// MAGIC 
// MAGIC **SOURCE:** This is just a couple of decorations on a notebook published in databricks community edition in 2016.

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/Ib1BIbPDS6U/0.jpg)](https://www.youtube.com/embed/Ib1BIbPDS6U?start=2545&end=2888&autoplay=1)
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/4_bx9WHKpHs/0.jpg)](https://www.youtube.com/embed/4_bx9WHKpHs?autoplay=1)

// COMMAND ----------

// MAGIC %md
// MAGIC # Decision Trees for handwritten digit recognition
// MAGIC 
// MAGIC This notebook demonstrates learning a [Decision Tree](https://en.wikipedia.org/wiki/Decision_tree_learning) using Spark's distributed implementation.  It gives the reader a better understanding of some critical [hyperparameters](https://en.wikipedia.org/wiki/Hyperparameter_optimization) for the tree learning algorithm, using examples to demonstrate how tuning the hyperparameters can improve accuracy.
// MAGIC 
// MAGIC **Background**: To learn more about Decision Trees, check out the resources at the end of this notebook.  [The visual description of ML and Decision Trees](http://www.r2d3.us/visual-intro-to-machine-learning-part-1/) provides nice intuition helpful to understand this notebook, and [Wikipedia](https://en.wikipedia.org/wiki/Decision_tree_learning) gives lots of details.
// MAGIC 
// MAGIC **Data**: We use the classic MNIST handwritten digit recognition dataset.  It is from LeCun et al. (1998) and may be found under ["mnist" at the LibSVM dataset page](https://www.csie.ntu.edu.tw/~cjlin/libsvmtools/datasets/multiclass.html#mnist).
// MAGIC 
// MAGIC **Goal**: Our goal for our data is to learn how to recognize digits (0 - 9) from images of handwriting.  However, we will focus on understanding trees, not on this particular learning problem.
// MAGIC 
// MAGIC **Takeaways**: Decision Trees take several hyperparameters which can affect the accuracy of the learned model.  There is no one "best" setting for these for all datasets.  To get the optimal accuracy, we need to tune these hyperparameters based on our data.

// COMMAND ----------

// MAGIC %md
// MAGIC ## Let's Build Intuition for Learning with Decision Trees
// MAGIC 
// MAGIC * Right-click and open the following link in a new Tab:
// MAGIC   * [The visual description of ML and Decision Trees](http://www.r2d3.us/visual-intro-to-machine-learning-part-1/) which was nominated for a [NSF Vizzie award](http://review.wizehive.com/voting/view/nsfvizziesgallery/27428/3236649).

// COMMAND ----------

//This allows easy embedding of publicly available information into any other notebook
//when viewing in git-book just ignore this block - you may have to manually chase the URL in frameIt("URL").
//Example usage:
// displayHTML(frameIt("https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation#Topics_in_LDA",250))
def frameIt( u:String, h:Int ) : String = {
      """<iframe 
 src=""""+ u+""""
 width="95%" height="""" + h + """"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe>"""
   }
displayHTML(frameIt("https://en.wikipedia.org/wiki/Decision_tree_learning",500))

// COMMAND ----------

// MAGIC %md
// MAGIC ## Load MNIST training and test datasets
// MAGIC 
// MAGIC Our datasets are vectors of pixels representing images of handwritten digits.  For example:
// MAGIC 
// MAGIC ![Image of a digit](http://training.databricks.com/databricks_guide/digit.png)
// MAGIC ![Image of all 10 digits](http://training.databricks.com/databricks_guide/MNIST-small.png)

// COMMAND ----------

// MAGIC %md
// MAGIC These datasets are stored in the popular LibSVM dataset format.  We will load them using MLlib's LibSVM dataset reader utility.

// COMMAND ----------

//-----------------------------------------------------------------------------------------------------------------
// using RDD-based MLlib - ok for Spark 1.x
// MLUtils.loadLibSVMFile returns an RDD.
//import org.apache.spark.mllib.util.MLUtils
//val trainingRDD = MLUtils.loadLibSVMFile(sc, "/databricks-datasets/mnist-digits/data-001/mnist-digits-train.txt")
//val testRDD = MLUtils.loadLibSVMFile(sc, "/databricks-datasets/mnist-digits/data-001/mnist-digits-test.txt")
// We convert the RDDs to DataFrames to use with ML Pipelines.
//val training = trainingRDD.toDF()
//val test = testRDD.toDF()
// Note: In Spark 1.6 and later versions, Spark SQL has a LibSVM data source.  The above lines can be simplified to:
//// val training = sqlContext.read.format("libsvm").load("/mnt/mllib/mnist-digits-csv/mnist-digits-train.txt")
//// val test = sqlContext.read.format("libsvm").load("/mnt/mllib/mnist-digits-csv/mnist-digits-test.txt")
//-----------------------------------------------------------------------------------------------------------------
val training = spark.read.format("libsvm")
                    .option("numFeatures", "780")
                    .load("/databricks-datasets/mnist-digits/data-001/mnist-digits-train.txt")

val test = spark.read.format("libsvm")
                    .option("numFeatures", "780")
                    .load("/databricks-datasets/mnist-digits/data-001/mnist-digits-test.txt")
// Cache data for multiple uses.
training.cache()
test.cache()

println(s"We have ${training.count} training images and ${test.count} test images.")

// COMMAND ----------

// MAGIC %md 
// MAGIC Display our data.  Each image has the true label (the `label` column) and a vector of `features` which represent pixel intensities (see below for details of what is in `training`).

// COMMAND ----------

training.printSchema()

// COMMAND ----------

training.show(3,true) // replace 'true' by 'false' to see the whole row hidden by '...'

// COMMAND ----------

display(training) // this is databricks-specific for interactive visual convenience

// COMMAND ----------

// MAGIC %md
// MAGIC The pixel intensities are represented in `features` as a sparse vector, for example the first observation, as seen in row 1 of the output to `display(training)` or `training.show(2,false)` above, has `label` as `5`, i.e. the hand-written image is for the number 5.  And this hand-written image is the following sparse vector (just click the triangle to the left of the feature in first row to see the following):
// MAGIC ```
// MAGIC type: 0
// MAGIC size: 780
// MAGIC indices: [152,153,155,...,682,683]
// MAGIC values: [3, 18, 18,18,126,...,132,16]
// MAGIC ```
// MAGIC 
// MAGIC Here,
// MAGIC 
// MAGIC * `type: 0` says we have a sparse vector that only represents non-zero entries (as opposed to a dense vector where every entry is represented).
// MAGIC * `size: 780` says the vector has 780 indices in total 
// MAGIC  * these indices from 0,...,779 are a unidimensional indexing of the two-dimensional array of pixels in the image
// MAGIC * `indices: [152,153,155,...,682,683]` are the indices from the `[0,1,...,779]` possible indices with non-zero values 
// MAGIC   * a value is an integer encoding the gray-level at the pixel index
// MAGIC * `values: [3, 18, 18,18,126,...,132,16]` are the actual gray level values, for example:
// MAGIC   * at pixed index `152` the gray-level value is `3`, 
// MAGIC   * at index `153` the gray-level value is `18`,
// MAGIC   * ..., and finally at
// MAGIC   * at index `683` the gray-level value is `18`

// COMMAND ----------

// MAGIC %md
// MAGIC ## Train a Decision Tree
// MAGIC 
// MAGIC We begin by training a decision tree using the default settings.  Before training, we want to tell the algorithm that the labels are categories 0-9, rather than continuous values.  We use the `StringIndexer` class to do this.  We tie this feature preprocessing together with the tree algorithm using a `Pipeline`.  ML Pipelines are tools Spark provides for piecing together Machine Learning algorithms into workflows.  To learn more about Pipelines, check out other ML example notebooks in Databricks and the [ML Pipelines user guide](http://spark.apache.org/docs/latest/ml-guide.html). Also See [mllib-decision-tree.html#basic-algorithm](http://spark.apache.org/docs/latest/mllib-decision-tree.html#basic-algorithm).

// COMMAND ----------

// MAGIC %md
// MAGIC See [http://spark.apache.org/docs/latest/mllib-decision-tree.html#basic-algorithm](http://spark.apache.org/docs/latest/mllib-decision-tree.html#basic-algorithm).

// COMMAND ----------

// MAGIC %md
// MAGIC See [http://spark.apache.org/docs/latest/ml-guide.html#main-concepts-in-pipelines](http://spark.apache.org/docs/latest/ml-guide.html#main-concepts-in-pipelines).

// COMMAND ----------

// Import the ML algorithms we will use.
import org.apache.spark.ml.classification.{DecisionTreeClassifier, DecisionTreeClassificationModel}
import org.apache.spark.ml.feature.StringIndexer
import org.apache.spark.ml.Pipeline

// COMMAND ----------

// StringIndexer: Read input column "label" (digits) and annotate them as categorical values.
val indexer = new StringIndexer().setInputCol("label").setOutputCol("indexedLabel")

// DecisionTreeClassifier: Learn to predict column "indexedLabel" using the "features" column.
val dtc = new DecisionTreeClassifier().setLabelCol("indexedLabel")

// Chain indexer + dtc together into a single ML Pipeline.
val pipeline = new Pipeline().setStages(Array(indexer, dtc))

// COMMAND ----------

// MAGIC %md
// MAGIC Now, let's fit a model to our data.

// COMMAND ----------

val model = pipeline.fit(training)

// COMMAND ----------

// MAGIC %md
// MAGIC We can inspect the learned tree by displaying it using Databricks ML visualization.  (Visualization is available for several but not all models.)

// COMMAND ----------

// The tree is the last stage of the Pipeline.  Display it!
val tree = model.stages.last.asInstanceOf[DecisionTreeClassificationModel]
display(tree)

// COMMAND ----------

// MAGIC %md
// MAGIC Above, we can see how the tree makes predictions.  When classifying a new example, the tree starts at the "root" node (at the top).  Each tree node tests a pixel value and goes either left or right.  At the bottom "leaf" nodes, the tree predicts a digit as the image's label.

// COMMAND ----------

// MAGIC %md
// MAGIC ## Hyperparameter Tuning
// MAGIC Run the next cell and come back into hyper-parameter tuning for a couple minutes.

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/Hyperparameter_optimization", 400))

// COMMAND ----------

// MAGIC %md
// MAGIC ## Exploring "maxDepth": training trees of different sizes
// MAGIC 
// MAGIC In this section, we test tuning a single hyperparameter `maxDepth`, which determines how deep (and large) the tree can be.  We will train trees at varying depths and see how it affects the accuracy on our held-out test set.
// MAGIC 
// MAGIC *Note: The next cell can take about 1 minute to run since it is training several trees which get deeper and deeper.*

// COMMAND ----------

val variedMaxDepthModels = (0 until 8).map { maxDepth =>
  // For this setting of maxDepth, learn a decision tree.
  dtc.setMaxDepth(maxDepth)
  // Create a Pipeline with our feature processing stage (indexer) plus the tree algorithm
  val pipeline = new Pipeline().setStages(Array(indexer, dtc))
  // Run the ML Pipeline to learn a tree.
  pipeline.fit(training)
}

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC We will use the default metric to evaluate the performance of our classifier:
// MAGIC 
// MAGIC  * [https://en.wikipedia.org/wiki/F1_score](https://en.wikipedia.org/wiki/F1_score).

// COMMAND ----------

//This allows easy embedding of publicly available information into any other notebook
//when viewing in git-book just ignore this block - you may have to manually chase the URL in frameIt("URL").
//Example usage:
// displayHTML(frameIt("https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation#Topics_in_LDA",250))
def frameIt( u:String, h:Int ) : String = {
      """<iframe 
 src=""""+ u+""""
 width="95%" height="""" + h + """"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe>"""
   }
displayHTML(frameIt("https://en.wikipedia.org/wiki/F1_score",400))

// COMMAND ----------

// Define an evaluation metric.  In this case, we will use "accuracy".
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
val evaluator = new MulticlassClassificationEvaluator().setLabelCol("indexedLabel").setMetricName("f1") // default MetricName

// COMMAND ----------

// For each maxDepth setting, make predictions on the test data, and compute the classifier's f1 performance metric.
val f1MetricPerformanceMeasures = (0 until 8).map { maxDepth =>
  val model = variedMaxDepthModels(maxDepth)
  // Calling transform() on the test set runs the fitted pipeline.
  // The learned model makes predictions on each test example.
  val predictions = model.transform(test)
  // Calling evaluate() on the predictions DataFrame computes our performance metric.
  (maxDepth, evaluator.evaluate(predictions))
}.toDF("maxDepth", "f1")

// COMMAND ----------

// MAGIC %md
// MAGIC We can display our accuracy results and see immediately that deeper, larger trees are more powerful classifiers, achieving higher accuracies.
// MAGIC 
// MAGIC *Note:* When you run `f1MetricPerformanceMeasures.show()`, you will get a table with f1 score getting better (i.e., approaching 1) with depth.

// COMMAND ----------

f1MetricPerformanceMeasures.show()

// COMMAND ----------

// MAGIC %md
// MAGIC Even though deeper trees are more powerful, they are not always better (recall from the SF/NYC city classification from house features at  [The visual description of ML and Decision Trees](http://www.r2d3.us/visual-intro-to-machine-learning-part-1/)).  If we kept increasing the depth on a rich enough dataset, training would take longer and longer.  We also might risk [overfitting](https://en.wikipedia.org/wiki/Overfitting) (fitting the training data so well that our predictions get worse on test data); it is important to tune parameters *based on [held-out data](https://en.wikipedia.org/wiki/Test_set)* to prevent overfitting. This will ensure that the fitted model generalizes well to yet unseen data, i.e. minimizes [generalization error](https://en.wikipedia.org/wiki/Generalization_error) in a mathematical statistical sense.

// COMMAND ----------

// MAGIC %md
// MAGIC ## Exploring "maxBins": discretization for efficient distributed computing
// MAGIC 
// MAGIC This section explores a more expert-level setting `maxBins`.  For efficient distributed training of Decision Trees, Spark and most other libraries discretize (or "bin") continuous features (such as pixel values) into a finite number of values.  This is an important step for the distributed implementation, but it introduces a tradeoff: Larger `maxBins` mean your data will be more accurately represented, but it will also mean more communication (and slower training).
// MAGIC 
// MAGIC The default value of `maxBins` generally works, but it is interesting to explore on our handwritten digit dataset.  Remember our digit image from above:
// MAGIC 
// MAGIC ![Image of a digit](http://training.databricks.com/databricks_guide/digit.png)
// MAGIC 
// MAGIC It is grayscale.  But if we set `maxBins = 2`, then we are effectively making it a black-and-white image, not grayscale.  Will that affect the accuracy of our model?  Let's see!
// MAGIC 
// MAGIC *Note: The next cell can take about 35 seconds to run since it trains several trees.*
// MAGIC Read the details on `maxBins` at [mllib-decision-tree.html#split-candidates](http://spark.apache.org/docs/latest/mllib-decision-tree.html#split-candidates).

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC See [http://spark.apache.org/docs/latest/mllib-decision-tree.html#split-candidates](http://spark.apache.org/docs/latest/mllib-decision-tree.html#split-candidates).

// COMMAND ----------

dtc.setMaxDepth(6) // Set maxDepth to a reasonable value.
// now try the maxBins "hyper-parameter" which actually acts as a "coarsener" 
//     mathematical researchers should note that it is a sub-algebra of the finite 
//     algebra of observable pixel images at the finest resolution available to us
// giving a compression of the image to fewer coarsely represented pixels
val f1MetricPerformanceMeasures = Seq(2, 4, 8, 16, 32).map { case maxBins =>
  // For this value of maxBins, learn a tree.
  dtc.setMaxBins(maxBins)
  val pipeline = new Pipeline().setStages(Array(indexer, dtc))
  val model = pipeline.fit(training)
  // Make predictions on test data, and compute accuracy.
  val predictions = model.transform(test)
  (maxBins, evaluator.evaluate(predictions))
}.toDF("maxBins", "f1")

// COMMAND ----------

f1MetricPerformanceMeasures.show()

// COMMAND ----------

// MAGIC %md
// MAGIC We can see that extreme discretization (black and white) hurts performance as measured by F1-error, but only a bit.  Using more bins increases the accuracy (but also makes learning more costly).

// COMMAND ----------

// MAGIC %md
// MAGIC #### What's next?
// MAGIC 
// MAGIC * **Explore**: Try out tuning other parameters of trees---or even ensembles like [Random Forests or Gradient-Boosted Trees](http://spark.apache.org/docs/latest/ml-classification-regression.html#tree-ensembles).
// MAGIC * **Automated tuning**: This type of tuning does not have to be done by hand.  (We did it by hand here to show the effects of tuning in detail.)  MLlib provides automated tuning functionality via `CrossValidator`.  Check out the other Databricks ML Pipeline guides or the [Spark ML user guide](http://spark.apache.org/docs/latest/ml-guide.html) for details.
// MAGIC 
// MAGIC **Resources**
// MAGIC 
// MAGIC If you are interested in learning more on these topics, these resources can get you started:
// MAGIC 
// MAGIC * [Excellent visual description of Machine Learning and Decision Trees](http://www.r2d3.us/visual-intro-to-machine-learning-part-1/)
// MAGIC   * *This gives an intuitive visual explanation of ML, decision trees, overfitting, and more.*
// MAGIC * [Blog post on MLlib Random Forests and Gradient-Boosted Trees](https://databricks.com/blog/2015/01/21/random-forests-and-boosting-in-mllib.html)
// MAGIC   * *Random Forests and Gradient-Boosted Trees combine many trees into more powerful ensemble models.  This is the original post describing MLlib's forest and GBT implementations.*
// MAGIC * Wikipedia
// MAGIC   * [Decision tree learning](https://en.wikipedia.org/wiki/Decision_tree_learning)
// MAGIC   * [Overfitting](https://en.wikipedia.org/wiki/Overfitting)
// MAGIC   * [Hyperparameter tuning](https://en.wikipedia.org/wiki/Hyperparameter_optimization)