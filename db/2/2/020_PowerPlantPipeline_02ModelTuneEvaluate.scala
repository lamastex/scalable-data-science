// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/rBZih2BQZuw/0.jpg)](https://www.youtube.com/embed/rBZih2BQZuw?start=428&end=3039&autoplay=1)

// COMMAND ----------

// MAGIC %md 
// MAGIC #Power Plant ML Pipeline Application
// MAGIC This is an end-to-end example of using a number of different machine learning algorithms to solve a supervised regression problem.
// MAGIC 
// MAGIC ###Table of Contents
// MAGIC 
// MAGIC - *Step 1: Business Understanding*
// MAGIC - *Step 2: Load Your Data*
// MAGIC - *Step 3: Explore Your Data*
// MAGIC - *Step 4: Visualize Your Data*
// MAGIC - *Step 5: Data Preparation*
// MAGIC - *Step 6: Data Modeling*
// MAGIC - *Step 7: Tuning and Evaluation*
// MAGIC - *Step 8: Deployment*
// MAGIC 
// MAGIC 
// MAGIC 
// MAGIC *We are trying to predict power output given a set of readings from various sensors in a gas-fired power generation plant.  Power generation is a complex process, and understanding and predicting power output is an important element in managing a plant and its connection to the power grid.*
// MAGIC 
// MAGIC More information about Peaker or Peaking Power Plants can be found on Wikipedia https://en.wikipedia.org/wiki/Peaking_power_plant
// MAGIC 
// MAGIC 
// MAGIC Given this business problem, we need to translate it to a Machine Learning task.  The ML task is regression since the label (or target) we are trying to predict is numeric.
// MAGIC 
// MAGIC 
// MAGIC The example data is provided by UCI at [UCI Machine Learning Repository Combined Cycle Power Plant Data Set](https://archive.ics.uci.edu/ml/datasets/Combined+Cycle+Power+Plant)
// MAGIC 
// MAGIC You can read the background on the UCI page, but in summary we have collected a number of readings from sensors at a Gas Fired Power Plant
// MAGIC 
// MAGIC (also called a Peaker Plant) and now we want to use those sensor readings to predict how much power the plant will generate.
// MAGIC 
// MAGIC 
// MAGIC More information about Machine Learning with Spark can be found in the [Spark MLLib Programming Guide](https://spark.apache.org/docs/latest/mllib-guide.html)
// MAGIC 
// MAGIC 
// MAGIC *Please note this example only works with Spark version 1.4 or higher*

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC ***

// COMMAND ----------

// MAGIC %md
// MAGIC To **Rerun Steps 1-4** done in the notebook at:
// MAGIC 
// MAGIC * [Workspace -> scalable-data-science -> sds-2-2 -> 009_PowerPlantPipeline_01ETLEDA](/#workspace/scalable-data-science/sds-2-2/009_PowerPlantPipeline_01ETLEDA)
// MAGIC 
// MAGIC just `run` the following command as shown in the cell below: 
// MAGIC 
// MAGIC   ```%scala
// MAGIC   %run "/scalable-data-science/sds-2-2/009_PowerPlantPipeline_01ETLEDA"
// MAGIC   ```
// MAGIC   
// MAGIC    * *Note:* If you already evaluated the `%run ...` command above then:
// MAGIC    
// MAGIC      * first delete the cell by pressing on `x` on the top-right corner of the cell and 
// MAGIC      * revaluate the `run` command above.

// COMMAND ----------

// MAGIC %run "/scalable-data-science/sds-2-2/009_PowerPlantPipeline_01ETLEDA"

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC ***

// COMMAND ----------

// MAGIC %md
// MAGIC # Now we will do the following Steps:
// MAGIC ## Step 5: Data Preparation, 
// MAGIC ## Step 6: Modeling, and 
// MAGIC ## Step 7: Tuning and Evaluation 
// MAGIC 
// MAGIC We will do *Step 8: Deployment* later after we get introduced to SparkStreaming.

// COMMAND ----------

// MAGIC %md
// MAGIC ##Step 5: Data Preparation
// MAGIC 
// MAGIC The next step is to prepare the data. Since all of this data is numeric and consistent, this is a simple task for us today.
// MAGIC 
// MAGIC We will need to convert the predictor features from columns to Feature Vectors using the org.apache.spark.ml.feature.VectorAssembler
// MAGIC 
// MAGIC The VectorAssembler will be the first step in building our ML pipeline.

// COMMAND ----------

//Let's quickly recall the schema and make sure our table is here now
table("power_plant_table").printSchema

// COMMAND ----------

powerPlantDF // make sure we have the DataFrame too

// COMMAND ----------

import org.apache.spark.ml.feature.VectorAssembler

// make a DataFrame called dataset from the table
val dataset = sqlContext.table("power_plant_table") 

val vectorizer =  new VectorAssembler()
                      .setInputCols(Array("AT", "V", "AP", "RH"))
                      .setOutputCol("features")


// COMMAND ----------

// MAGIC %md
// MAGIC ##Step 6: Data Modeling
// MAGIC Now let's model our data to predict what the power output will be given a set of sensor readings
// MAGIC 
// MAGIC Our first model will be based on simple linear regression since we saw some linear patterns in our data based on the scatter plots during the exploration stage.

// COMMAND ----------

// MAGIC %md
// MAGIC ### Linear Regression Model
// MAGIC 
// MAGIC * Linear Regression is one of the most useful work-horses of statistical learning
// MAGIC * See Chapter 7 of Kevin Murphy's Machine Learning froma  Probabilistic Perspective for a good mathematical and algorithmic introduction.
// MAGIC * You should have already seen Ameet's treatment of the topic from earlier notebook.

// COMMAND ----------

// MAGIC %md
// MAGIC Let's open [http://spark.apache.org/docs/latest/mllib-linear-methods.html#regression](http://spark.apache.org/docs/latest/mllib-linear-methods.html#regression) for some details.

// COMMAND ----------

// First let's hold out 20% of our data for testing and leave 80% for training
var Array(split20, split80) = dataset.randomSplit(Array(0.20, 0.80), 1800009193L)

// COMMAND ----------

// Let's cache these datasets for performance
val testSet = split20.cache()
val trainingSet = split80.cache()

// COMMAND ----------

testSet.count() // action to actually cache

// COMMAND ----------

trainingSet.count() // action to actually cache

// COMMAND ----------

// MAGIC %md
// MAGIC Let's take a few elements of the three DataFrames.

// COMMAND ----------

dataset.take(3)

// COMMAND ----------

testSet.take(3)

// COMMAND ----------

trainingSet.take(3)

// COMMAND ----------

// ***** LINEAR REGRESSION MODEL ****

import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.ml.regression.LinearRegressionModel
import org.apache.spark.ml.Pipeline

// Let's initialize our linear regression learner
val lr = new LinearRegression()

// COMMAND ----------

// We use explain params to dump the parameters we can use
lr.explainParams()

// COMMAND ----------

// MAGIC %md
// MAGIC The cell below is based on the Spark ML pipeline API. More information can be found in the Spark ML Programming Guide at https://spark.apache.org/docs/latest/ml-guide.html

// COMMAND ----------

// Now we set the parameters for the method
lr.setPredictionCol("Predicted_PE")
  .setLabelCol("PE")
  .setMaxIter(100)
  .setRegParam(0.1)
// We will use the new spark.ml pipeline API. If you have worked with scikit-learn this will be very familiar.
val lrPipeline = new Pipeline()
lrPipeline.setStages(Array(vectorizer, lr))
// Let's first train on the entire dataset to see what we get
val lrModel = lrPipeline.fit(trainingSet)

// COMMAND ----------

// MAGIC %md 
// MAGIC Since Linear Regression is simply a line of best fit over the data that minimizes the square of the error, given multiple input dimensions we can express each predictor as a line function of the form:
// MAGIC 
// MAGIC $$ y = b_0 + b_1 x_1 + b_2 x_2 + b_3 x_3 + \ldots + b_i x_i + \ldots + b_k x_k $$
// MAGIC 
// MAGIC where \\(b_0\\) is the intercept and \\(b_i\\)'s are coefficients.
// MAGIC 
// MAGIC To express the coefficients of that line we can retrieve the Estimator stage from the fitted, linear-regression pipeline model named `lrModel` and express the weights and the intercept for the function.

// COMMAND ----------

// The intercept is as follows:
val intercept = lrModel.stages(1).asInstanceOf[LinearRegressionModel].intercept

// COMMAND ----------

// The coefficents (i.e. weights) are as follows:

val weights = lrModel.stages(1).asInstanceOf[LinearRegressionModel].coefficients.toArray

// COMMAND ----------

// MAGIC %md
// MAGIC The model has been fit and the intercept and coefficients displayed above.
// MAGIC 
// MAGIC Now, let us do some work to make a string of the model that is easy to understand for an applied data scientist or data analyst.

// COMMAND ----------

val featuresNoLabel = dataset.columns.filter(col => col != "PE")

// COMMAND ----------

val coefficentFeaturePairs = sc.parallelize(weights).zip(sc.parallelize(featuresNoLabel))

// COMMAND ----------

coefficentFeaturePairs.collect() // this just pairs each coefficient with the name of its corresponding feature

// COMMAND ----------

// Now let's sort the coefficients from the largest to the smallest

var equation = s"y = $intercept "
//var variables = Array
coefficentFeaturePairs.sortByKey().collect().foreach({
  case (weight, feature) =>
  { 
        val symbol = if (weight > 0) "+" else "-"
        val absWeight = Math.abs(weight)
        equation += (s" $symbol (${absWeight} * ${feature})")
  }
}
)

// COMMAND ----------

// Finally here is our equation
println("Linear Regression Equation: " + equation)

// COMMAND ----------

// MAGIC %md
// MAGIC Based on examining the fitted Linear Regression Equation above:
// MAGIC 
// MAGIC * There is a strong negative correlation between Atmospheric Temperature (AT) and Power Output due to the coefficient being greater than -1.91.
// MAGIC * But our other dimenensions seem to have little to no correlation with Power Output. 
// MAGIC 
// MAGIC Do you remember **Step 2: Explore Your Data**? When we visualized each predictor against Power Output using a Scatter Plot, only the temperature variable seemed to have a linear correlation with Power Output so our final equation seems logical.
// MAGIC 
// MAGIC 
// MAGIC Now let's see what our predictions look like given this model.

// COMMAND ----------

val predictionsAndLabels = lrModel.transform(testSet)

display(predictionsAndLabels.select("AT", "V", "AP", "RH", "PE", "Predicted_PE"))

// COMMAND ----------

// MAGIC %md
// MAGIC Now that we have real predictions we can use an evaluation metric such as Root Mean Squared Error to validate our regression model. The lower the Root Mean Squared Error, the better our model.

// COMMAND ----------

//Now let's compute some evaluation metrics against our test dataset

import org.apache.spark.mllib.evaluation.RegressionMetrics 

val metrics = new RegressionMetrics(predictionsAndLabels.select("Predicted_PE", "PE").rdd.map(r => (r(0).asInstanceOf[Double], r(1).asInstanceOf[Double])))

// COMMAND ----------

val rmse = metrics.rootMeanSquaredError

// COMMAND ----------

val explainedVariance = metrics.explainedVariance

// COMMAND ----------

val r2 = metrics.r2

// COMMAND ----------

println (f"Root Mean Squared Error: $rmse")
println (f"Explained Variance: $explainedVariance")  
println (f"R2: $r2")

// COMMAND ----------

// MAGIC %md
// MAGIC Generally a good model will have 68% of predictions within 1 RMSE and 95% within 2 RMSE of the actual value. Let's calculate and see if our RMSE meets this criteria.

// COMMAND ----------

display(predictionsAndLabels) // recall the DataFrame predictionsAndLabels

// COMMAND ----------

// First we calculate the residual error and divide it by the RMSE from predictionsAndLabels DataFrame and make another DataFrame that is registered as a temporary table Power_Plant_RMSE_Evaluation
predictionsAndLabels.selectExpr("PE", "Predicted_PE", "PE - Predicted_PE AS Residual_Error", s""" (PE - Predicted_PE) / $rmse AS Within_RSME""").createOrReplaceTempView("Power_Plant_RMSE_Evaluation")

// COMMAND ----------

// MAGIC %sql SELECT * from Power_Plant_RMSE_Evaluation

// COMMAND ----------

// MAGIC %sql -- Now we can display the RMSE as a Histogram. Clearly this shows that the RMSE is centered around 0 with the vast majority of the error within 2 RMSEs.
// MAGIC SELECT Within_RSME  from Power_Plant_RMSE_Evaluation

// COMMAND ----------

// MAGIC %md
// MAGIC We can see this definitively if we count the number of predictions within + or - 1.0 and + or - 2.0 and display this as a pie chart:

// COMMAND ----------

// MAGIC %sql 
// MAGIC SELECT case when Within_RSME <= 1.0 and Within_RSME >= -1.0 then 1  when  Within_RSME <= 2.0 and Within_RSME >= -2.0 then 2 else 3 end RSME_Multiple, COUNT(*) count  from Power_Plant_RMSE_Evaluation
// MAGIC group by case when Within_RSME <= 1.0 and Within_RSME >= -1.0 then 1  when  Within_RSME <= 2.0 and Within_RSME >= -2.0 then 2 else 3 end

// COMMAND ----------

// MAGIC %md
// MAGIC So we have about 70% of our training data within 1 RMSE and about 97% (70% + 27%) within 2 RMSE. So the model is pretty decent. Let's see if we can tune the model to improve it further.
// MAGIC 
// MAGIC **NOTE:** these numbers will vary across runs due to the seed in random sampling of training and test set, number of iterations, and other stopping rules in optimization, for example.

// COMMAND ----------

// MAGIC %md
// MAGIC #Step 7: Tuning and Evaluation
// MAGIC 
// MAGIC Now that we have a model with all of the data let's try to make a better model by tuning over several parameters.

// COMMAND ----------

import org.apache.spark.ml.tuning.{ParamGridBuilder, CrossValidator}
import org.apache.spark.ml.evaluation._

// COMMAND ----------

// MAGIC %md
// MAGIC First let's use a cross validator to split the data into training and validation subsets. See [http://spark.apache.org/docs/latest/ml-tuning.html](http://spark.apache.org/docs/latest/ml-tuning.html).

// COMMAND ----------

//Let's set up our evaluator class to judge the model based on the best root mean squared error
val regEval = new RegressionEvaluator()
regEval.setLabelCol("PE")
  .setPredictionCol("Predicted_PE")
  .setMetricName("rmse")

// COMMAND ----------

// MAGIC %md
// MAGIC We now treat the `lrPipeline` as an `Estimator`, wrapping it in a `CrossValidator` instance.
// MAGIC 
// MAGIC This will allow us to jointly choose parameters for all Pipeline stages.
// MAGIC 
// MAGIC A `CrossValidator` requires an `Estimator`, an `Evaluator` (which we `set` next).

// COMMAND ----------

//Let's create our crossvalidator with 3 fold cross validation
val crossval = new CrossValidator()
crossval.setEstimator(lrPipeline)
crossval.setNumFolds(3)
crossval.setEvaluator(regEval)

// COMMAND ----------

// MAGIC %md
// MAGIC A `CrossValidator` also requires a set of `EstimatorParamMaps` which we `set` next.
// MAGIC 
// MAGIC For this we need a regularization parameter (more generally a hyper-parameter that is model-specific).

// COMMAND ----------

// MAGIC %md
// MAGIC Now, let's tune over our regularization parameter from 0.01 to 0.10.

// COMMAND ----------

val regParam = ((1 to 10) toArray).map(x => (x /100.0))

// COMMAND ----------

// MAGIC %md
// MAGIC Check out the scala docs for syntactic details on [org.apache.spark.ml.tuning.ParamGridBuilder](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.ml.tuning.ParamGridBuilder).

// COMMAND ----------

val paramGrid = new ParamGridBuilder()
                    .addGrid(lr.regParam, regParam)
                    .build()
crossval.setEstimatorParamMaps(paramGrid)

// COMMAND ----------

//Now let's create our model
val cvModel = crossval.fit(trainingSet)

// COMMAND ----------

// MAGIC %md
// MAGIC In addition to `CrossValidator` Spark also offers `TrainValidationSplit` for hyper-parameter tuning. `TrainValidationSplit` only evaluates each combination of parameters once as opposed to k times in case of `CrossValidator`. It is therefore less expensive, but will not produce as reliable results when the training dataset is not sufficiently large.
// MAGIC 
// MAGIC * [http://spark.apache.org/docs/latest/ml-tuning.html#train-validation-split](http://spark.apache.org/docs/latest/ml-tuning.html#train-validation-split)

// COMMAND ----------

// MAGIC %md
// MAGIC Now that we have tuned let's see what we got for tuning parameters and what our RMSE was versus our intial model

// COMMAND ----------

val predictionsAndLabels = cvModel.transform(testSet)
val metrics = new RegressionMetrics(predictionsAndLabels.select("Predicted_PE", "PE").rdd.map(r => (r(0).asInstanceOf[Double], r(1).asInstanceOf[Double])))

val rmse = metrics.rootMeanSquaredError
val explainedVariance = metrics.explainedVariance
val r2 = metrics.r2

// COMMAND ----------

println (f"Root Mean Squared Error: $rmse")
println (f"Explained Variance: $explainedVariance")  
println (f"R2: $r2")

// COMMAND ----------

// MAGIC %md
// MAGIC ## Let us explore other models to see if we can predict the power output better
// MAGIC 
// MAGIC There are several families of models in Spark's scalable machine learning library:
// MAGIC 
// MAGIC * [http://spark.apache.org/docs/latest/ml-classification-regression.html](http://spark.apache.org/docs/latest/ml-classification-regression.html)

// COMMAND ----------

// MAGIC %md
// MAGIC So our initial untuned and tuned linear regression models are statistically identical.
// MAGIC 
// MAGIC Given that the only linearly correlated variable is Temperature, it makes sense try another machine learning method such a Decision Tree to handle non-linear data and see if we can improve our model
// MAGIC 
// MAGIC A Decision Tree creates a model based on splitting variables using a tree structure. We will first start with a single decision tree model.
// MAGIC 
// MAGIC Reference Decision Trees: https://en.wikipedia.org/wiki/Decision_tree_learning

// COMMAND ----------

//Let's build a decision tree pipeline
import org.apache.spark.ml.regression.DecisionTreeRegressor

// we are using a Decision Tree Regressor as opposed to a classifier we used for the hand-written digit classification problem
val dt = new DecisionTreeRegressor()
dt.setLabelCol("PE")
dt.setPredictionCol("Predicted_PE")
dt.setFeaturesCol("features")
dt.setMaxBins(100)

val dtPipeline = new Pipeline()
dtPipeline.setStages(Array(vectorizer, dt))

// COMMAND ----------

//Let's just resuse our CrossValidator
crossval.setEstimator(dtPipeline)

// COMMAND ----------

val paramGrid = new ParamGridBuilder()
                     .addGrid(dt.maxDepth, Array(2, 3))
                     .build()

// COMMAND ----------

crossval.setEstimatorParamMaps(paramGrid)

// COMMAND ----------

val dtModel = crossval.fit(trainingSet) // fit decitionTree with cv

// COMMAND ----------

import org.apache.spark.ml.regression.DecisionTreeRegressionModel
import org.apache.spark.ml.PipelineModel
dtModel.bestModel.asInstanceOf[PipelineModel].stages.last.asInstanceOf[DecisionTreeRegressionModel].toDebugString

// COMMAND ----------

// MAGIC %md 
// MAGIC The line above will pull the Decision Tree model from the Pipeline and display it as an if-then-else string.
// MAGIC 
// MAGIC Next let's visualize it as a decision tree for regression.

// COMMAND ----------

display(dtModel.bestModel.asInstanceOf[PipelineModel].stages.last.asInstanceOf[DecisionTreeRegressionModel])

// COMMAND ----------

// MAGIC %md
// MAGIC Now let's see how our DecisionTree model compares to our LinearRegression model

// COMMAND ----------


val predictionsAndLabels = dtModel.bestModel.transform(testSet)
val metrics = new RegressionMetrics(predictionsAndLabels.select("Predicted_PE", "PE").map(r => (r(0).asInstanceOf[Double], r(1).asInstanceOf[Double])).rdd)

val rmse = metrics.rootMeanSquaredError
val explainedVariance = metrics.explainedVariance
val r2 = metrics.r2

println (f"Root Mean Squared Error: $rmse")
println (f"Explained Variance: $explainedVariance")  
println (f"R2: $r2")

// COMMAND ----------

// MAGIC %md
// MAGIC So our DecisionTree was slightly worse than our LinearRegression model (LR: 4.6 vs DT: 5.2). Maybe we can try an Ensemble method such as Gradient-Boosted Decision Trees to see if we can strengthen our model by using an ensemble of weaker trees with weighting to reduce the error in our model.
// MAGIC 
// MAGIC *Note since this is a complex model, the cell below can take about *16 minutes* or so to run on a small cluster with a couple nodes with about 6GB RAM, go out and grab a coffee and come back :-).*

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC This GBTRegressor code will be way faster on a larger cluster of course.

// COMMAND ----------

// MAGIC %md
// MAGIC A visual explanation of gradient boosted trees:
// MAGIC 
// MAGIC * [http://arogozhnikov.github.io/2016/06/24/gradient_boosting_explained.html](http://arogozhnikov.github.io/2016/06/24/gradient_boosting_explained.html)
// MAGIC 
// MAGIC Let's see what a boosting algorithm, a type of ensemble method, is all about in more detail.

// COMMAND ----------

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
displayHTML(frameIt("https://en.wikipedia.org/wiki/Gradient_boosting",500))

// COMMAND ----------

import org.apache.spark.ml.regression.GBTRegressor

val gbt = new GBTRegressor()
gbt.setLabelCol("PE")
gbt.setPredictionCol("Predicted_PE")
gbt.setFeaturesCol("features")
gbt.setSeed(100088121L)
gbt.setMaxBins(100)
gbt.setMaxIter(120)

val gbtPipeline = new Pipeline()
gbtPipeline.setStages(Array(vectorizer, gbt))
//Let's just resuse our CrossValidator

crossval.setEstimator(gbtPipeline)

val paramGrid = new ParamGridBuilder()
  .addGrid(gbt.maxDepth, Array(2, 3))
  .build()
crossval.setEstimatorParamMaps(paramGrid)

//gbt.explainParams
val gbtModel = crossval.fit(trainingSet)

// COMMAND ----------

import org.apache.spark.ml.regression.GBTRegressionModel 

val predictionsAndLabels = gbtModel.bestModel.transform(testSet)
val metrics = new RegressionMetrics(predictionsAndLabels.select("Predicted_PE", "PE").map(r => (r(0).asInstanceOf[Double], r(1).asInstanceOf[Double])).rdd)

val rmse = metrics.rootMeanSquaredError
val explainedVariance = metrics.explainedVariance
val r2 = metrics.r2


println (f"Root Mean Squared Error: $rmse")
println (f"Explained Variance: $explainedVariance")  
println (f"R2: $r2")


// COMMAND ----------

// MAGIC %md 
// MAGIC We can use the toDebugString method to dump out what our trees and weighting look like:

// COMMAND ----------

gbtModel.bestModel.asInstanceOf[PipelineModel].stages.last.asInstanceOf[GBTRegressionModel].toDebugString

// COMMAND ----------

// MAGIC %md
// MAGIC ### Conclusion
// MAGIC 
// MAGIC Wow! So our best model is in fact our Gradient Boosted Decision tree model which uses an ensemble of 120 Trees with a depth of 3 to construct a better model than the single decision tree.

// COMMAND ----------

// MAGIC %md
// MAGIC #Step 8: Deployment will be done later
// MAGIC 
// MAGIC Now that we have a predictive model it is time to deploy the model into an operational environment. 
// MAGIC 
// MAGIC In our example, let's say we have a series of sensors attached to the power plant and a monitoring station.
// MAGIC 
// MAGIC The monitoring station will need close to real-time information about how much power that their station will generate so they can relay that to the utility. 
// MAGIC 
// MAGIC For this we need to create a Spark Streaming utility that we can use for this purpose. For this you need to be introduced to basic concepts in Spark Streaming first.  
// MAGIC See [http://spark.apache.org/docs/latest/streaming-programming-guide.html](http://spark.apache.org/docs/latest/streaming-programming-guide.html) if you can't wait!

// COMMAND ----------

// MAGIC %md
// MAGIC After deployment you will be able to use the best predictions from gradient boosed regression trees to feed a real-time dashboard or feed the utility with information on how much power the peaker plant will deliver give current conditions.

// COMMAND ----------

// MAGIC %md
// MAGIC Datasource References:
// MAGIC 
// MAGIC * Pinar Tüfekci, Prediction of full load electrical power output of a base load operated combined cycle power plant using machine learning methods, International Journal of Electrical Power & Energy Systems, Volume 60, September 2014, Pages 126-140, ISSN 0142-0615, [Web Link](http://www.journals.elsevier.com/international-journal-of-electrical-power-and-energy-systems/)
// MAGIC * Heysem Kaya, Pinar Tüfekci , Sadik Fikret Gürgen: Local and Global Learning Methods for Predicting Power of a Combined Gas & Steam Turbine, Proceedings of the International Conference on Emerging Trends in Computer and Electronics Engineering ICETCEE 2012, pp. 13-18 (Mar. 2012, Dubai) [Web Link](http://www.cmpe.boun.edu.tr/~kaya/kaya2012gasturbine.pdf)