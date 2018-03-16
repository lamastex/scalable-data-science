[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

Archived YouTube video of this live unedited lab-lecture:

[![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/TsqWglA3_-0/0.jpg)](https://www.youtube.com/embed/TsqWglA3_-0?start=1324&end=1605&autoplay=1) [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/qBx_pslVBkg/0.jpg)](https://www.youtube.com/embed/qBx_pslVBkg?start=895&end=2160&autoplay=1)

Power Plant ML Pipeline Application
===================================

This is an end-to-end example of using a number of different machine learning algorithms to solve a supervised regression problem.

### Table of Contents

-   *Step 1: Business Understanding*
-   *Step 2: Load Your Data*
-   *Step 3: Explore Your Data*
-   *Step 4: Visualize Your Data*
-   *Step 5: Data Preparation*
-   *Step 6: Data Modeling*
-   *Step 7: Tuning and Evaluation*
-   *Step 8: Deployment*

*We are trying to predict power output given a set of readings from various sensors in a gas-fired power generation plant. Power generation is a complex process, and understanding and predicting power output is an important element in managing a plant and its connection to the power grid.*

More information about Peaker or Peaking Power Plants can be found on Wikipedia https://en.wikipedia.org/wiki/Peaking*power*plant

Given this business problem, we need to translate it to a Machine Learning task. The ML task is regression since the label (or target) we are trying to predict is numeric.

The example data is provided by UCI at [UCI Machine Learning Repository Combined Cycle Power Plant Data Set](https://archive.ics.uci.edu/ml/datasets/Combined+Cycle+Power+Plant)

You can read the background on the UCI page, but in summary we have collected a number of readings from sensors at a Gas Fired Power Plant

(also called a Peaker Plant) and now we want to use those sensor readings to predict how much power the plant will generate.

More information about Machine Learning with Spark can be found in the [Spark MLLib Programming Guide](https://spark.apache.org/docs/latest/mllib-guide.html)

*Please note this example only works with Spark version 1.4 or higher*

------------------------------------------------------------------------

------------------------------------------------------------------------

To **Rerun Steps 1-4** done in the notebook at:
\* [Workspace -&gt; scalable-data-science -&gt; sds-2-2 -&gt; 009*PowerPlantPipeline*01ETLEDA](/#workspace/scalable-data-science/sds-2-2/009_PowerPlantPipeline_01ETLEDA)

just `run` the following command as shown in the cell below:

`%scala   %run "/scalable-data-science/sds-2-2/009_PowerPlantPipeline_01ETLEDA"`

-   *Note:* If you already evaluated the `%run ...` command above then:
    -   first delete the cell by pressing on `x` on the top-right corner of the cell and
    -   revaluate the `run` command above.

``` run
"/scalable-data-science/sds-2-2/009_PowerPlantPipeline_01ETLEDA"
```

------------------------------------------------------------------------

------------------------------------------------------------------------

Now we will do the following Steps:
===================================

Step 5: Data Preparation,
-------------------------

Step 6: Modeling, and
---------------------

Step 7: Tuning and Evaluation
-----------------------------

Step 8: Deployment
------------------

Step 5: Data Preparation
------------------------

The next step is to prepare the data. Since all of this data is numeric and consistent, this is a simple task for us today.

We will need to convert the predictor features from columns to Feature Vectors using the org.apache.spark.ml.feature.VectorAssembler

The VectorAssembler will be the first step in building our ML pipeline.

``` scala
//Let's quickly recall the schema
// the table is available
table("power_plant_table").printSchema
```

>     root
>      |-- AT: double (nullable = true)
>      |-- V: double (nullable = true)
>      |-- AP: double (nullable = true)
>      |-- RH: double (nullable = true)
>      |-- PE: double (nullable = true)

``` scala
//the DataFrame should also be available
powerPlantDF 
```

>     res22: org.apache.spark.sql.DataFrame = [AT: double, V: double ... 3 more fields]

``` scala
import org.apache.spark.ml.feature.VectorAssembler

// make a DataFrame called dataset from the table
val dataset = sqlContext.table("power_plant_table") 

val vectorizer =  new VectorAssembler()
                      .setInputCols(Array("AT", "V", "AP", "RH"))
                      .setOutputCol("features")
```

>     import org.apache.spark.ml.feature.VectorAssembler
>     dataset: org.apache.spark.sql.DataFrame = [AT: double, V: double ... 3 more fields]
>     vectorizer: org.apache.spark.ml.feature.VectorAssembler = vecAssembler_00521eb9630e

Step 6: Data Modeling
---------------------

Now let's model our data to predict what the power output will be given a set of sensor readings

Our first model will be based on simple linear regression since we saw some linear patterns in our data based on the scatter plots during the exploration stage.

### Linear Regression Model

-   Linear Regression is one of the most useful work-horses of statistical learning
-   See Chapter 7 of Kevin Murphy's Machine Learning froma Probabilistic Perspective for a good mathematical and algorithmic introduction.
-   You should have already seen Ameet's treatment of the topic from earlier notebook.

Let's open <http://spark.apache.org/docs/latest/mllib-linear-methods.html#regression> for some details.

``` scala
// First let's hold out 20% of our data for testing and leave 80% for training
var Array(split20, split80) = dataset.randomSplit(Array(0.20, 0.80), 1800009193L)
```

>     split20: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [AT: double, V: double ... 3 more fields]
>     split80: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [AT: double, V: double ... 3 more fields]

``` scala
// Let's cache these datasets for performance
val testSet = split20.cache()
val trainingSet = split80.cache()
```

>     testSet: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [AT: double, V: double ... 3 more fields]
>     trainingSet: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [AT: double, V: double ... 3 more fields]

``` scala
testSet.count() // action to actually cache
```

>     res24: Long = 1966

``` scala
trainingSet.count() // action to actually cache
```

>     res25: Long = 7602

Let's take a few elements of the three DataFrames.

``` scala
dataset.take(3)
```

>     res26: Array[org.apache.spark.sql.Row] = Array([14.96,41.76,1024.07,73.17,463.26], [25.18,62.96,1020.04,59.08,444.37], [5.11,39.4,1012.16,92.14,488.56])

``` scala
testSet.take(3)
```

>     res27: Array[org.apache.spark.sql.Row] = Array([1.81,39.42,1026.92,76.97,490.55], [3.2,41.31,997.67,98.84,489.86], [3.38,41.31,998.79,97.76,489.11])

``` scala
trainingSet.take(3)
```

>     res28: Array[org.apache.spark.sql.Row] = Array([2.34,39.42,1028.47,69.68,490.34], [2.58,39.42,1028.68,69.03,488.69], [2.64,39.64,1011.02,85.24,481.29])

``` scala
// ***** LINEAR REGRESSION MODEL ****

import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.ml.regression.LinearRegressionModel
import org.apache.spark.ml.Pipeline

// Let's initialize our linear regression learner
val lr = new LinearRegression()
```

>     import org.apache.spark.ml.regression.LinearRegression
>     import org.apache.spark.ml.regression.LinearRegressionModel
>     import org.apache.spark.ml.Pipeline
>     lr: org.apache.spark.ml.regression.LinearRegression = linReg_955431dccb4f

>     frameIt: (u: String, h: Int)String

>     frameIt: (u: String, h: Int)String

``` scala
// We use explain params to dump the parameters we can use
lr.explainParams()
```

>     res29: String =
>     aggregationDepth: suggested depth for treeAggregate (>= 2) (default: 2)
>     elasticNetParam: the ElasticNet mixing parameter, in range [0, 1]. For alpha = 0, the penalty is an L2 penalty. For alpha = 1, it is an L1 penalty (default: 0.0)
>     featuresCol: features column name (default: features)
>     fitIntercept: whether to fit an intercept term (default: true)
>     labelCol: label column name (default: label)
>     maxIter: maximum number of iterations (>= 0) (default: 100)
>     predictionCol: prediction column name (default: prediction)
>     regParam: regularization parameter (>= 0) (default: 0.0)
>     solver: the solver algorithm for optimization. If this is not set or empty, default value is 'auto' (default: auto)
>     standardization: whether to standardize the training features before fitting the model (default: true)
>     tol: the convergence tolerance for iterative algorithms (>= 0) (default: 1.0E-6)
>     weightCol: weight column name. If this is not set or empty, we treat all instance weights as 1.0 (undefined)

The cell below is based on the Spark ML pipeline API. More information can be found in the Spark ML Programming Guide at https://spark.apache.org/docs/latest/ml-guide.html

``` scala
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
```

>     lrPipeline: org.apache.spark.ml.Pipeline = pipeline_a16f729772ca
>     lrModel: org.apache.spark.ml.PipelineModel = pipeline_a16f729772ca

Since Linear Regression is simply a line of best fit over the data that minimizes the square of the error, given multiple input dimensions we can express each predictor as a line function of the form:

$$ y = b*0 + b*1 x*1 + b*2 x*2 + b*3 x*3 + \ldots + b*i x*i + \ldots + b*k x\_k $$

where \\(b*0\\) is the intercept and \\(b*i\\)'s are coefficients.

To express the coefficients of that line we can retrieve the Estimator stage from the fitted, linear-regression pipeline model named `lrModel` and express the weights and the intercept for the function.

``` scala
// The intercept is as follows:
val intercept = lrModel.stages(1).asInstanceOf[LinearRegressionModel].intercept
```

>     intercept: Double = 427.9139822165837

``` scala
// The coefficents (i.e. weights) are as follows:

val weights = lrModel.stages(1).asInstanceOf[LinearRegressionModel].coefficients.toArray
```

>     weights: Array[Double] = Array(-1.9083064919040942, -0.25381293007161654, 0.08739350304730673, -0.1474651301033126)

The model has been fit and the intercept and coefficients displayed above.

Now, let us do some work to make a string of the model that is easy to understand for an applied data scientist or data analyst.

``` scala
val featuresNoLabel = dataset.columns.filter(col => col != "PE")
```

>     featuresNoLabel: Array[String] = Array(AT, V, AP, RH)

``` scala
val coefficentFeaturePairs = sc.parallelize(weights).zip(sc.parallelize(featuresNoLabel))
```

>     coefficentFeaturePairs: org.apache.spark.rdd.RDD[(Double, String)] = ZippedPartitionsRDD2[108] at zip at <console>:42

``` scala
coefficentFeaturePairs.collect() // this just pairs each coefficient with the name of its corresponding feature
```

>     res30: Array[(Double, String)] = Array((-1.9083064919040942,AT), (-0.25381293007161654,V), (0.08739350304730673,AP), (-0.1474651301033126,RH))

``` scala
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
```

>     equation: String = y = 427.9139822165837  - (1.9083064919040942 * AT) - (0.25381293007161654 * V) - (0.1474651301033126 * RH) + (0.08739350304730673 * AP)

``` scala
// Finally here is our equation
println("Linear Regression Equation: " + equation)
```

>     Linear Regression Equation: y = 427.9139822165837  - (1.9083064919040942 * AT) - (0.25381293007161654 * V) - (0.1474651301033126 * RH) + (0.08739350304730673 * AP)

Based on examining the fitted Linear Regression Equation above:

-   There is a strong negative correlation between Atmospheric Temperature (AT) and Power Output due to the coefficient being greater than -1.91.
-   But our other dimenensions seem to have little to no correlation with Power Output.

Do you remember **Step 2: Explore Your Data**? When we visualized each predictor against Power Output using a Scatter Plot, only the temperature variable seemed to have a linear correlation with Power Output so our final equation seems logical.

Now let's see what our predictions look like given this model.

``` scala
val predictionsAndLabels = lrModel.transform(testSet)

display(predictionsAndLabels.select("AT", "V", "AP", "RH", "PE", "Predicted_PE"))
```

| AT   | V     | AP      | RH    | PE     | Predicted\_PE      |
|------|-------|---------|-------|--------|--------------------|
| 1.81 | 39.42 | 1026.92 | 76.97 | 490.55 | 492.8503868481024  |
| 3.2  | 41.31 | 997.67  | 98.84 | 489.86 | 483.9368120270272  |
| 3.38 | 41.31 | 998.79  | 97.76 | 489.11 | 483.850459922409   |
| 3.4  | 39.64 | 1011.1  | 83.43 | 459.86 | 487.4251507226833  |
| 3.51 | 35.47 | 1017.53 | 86.56 | 489.07 | 488.37401129434335 |
| 3.63 | 38.44 | 1016.16 | 87.38 | 487.87 | 487.1505396071426  |
| 3.91 | 35.47 | 1016.92 | 86.03 | 488.67 | 487.6355351796776  |
| 3.94 | 39.9  | 1008.06 | 97.49 | 488.81 | 483.9896378767201  |
| 4.0  | 39.9  | 1009.64 | 97.16 | 490.79 | 484.0618847149547  |
| 4.15 | 39.9  | 1007.62 | 95.69 | 489.8  | 483.8158776062654  |
| 4.15 | 39.9  | 1008.84 | 96.68 | 491.22 | 483.77650720118083 |
| 4.23 | 38.44 | 1016.46 | 76.64 | 489.0  | 487.61554926022393 |
| 4.24 | 39.9  | 1009.28 | 96.74 | 491.25 | 483.6343648504441  |
| 4.43 | 38.91 | 1019.04 | 88.17 | 491.9  | 485.6397981724803  |
| 4.44 | 38.44 | 1016.14 | 75.35 | 486.53 | 487.37706899378225 |
| 4.61 | 40.27 | 1012.32 | 77.28 | 492.85 | 485.96972834538735 |
| 4.65 | 35.19 | 1018.23 | 94.78 | 489.36 | 485.11862159667663 |
| 4.69 | 39.42 | 1024.58 | 79.35 | 486.34 | 486.79899634464203 |
| 4.73 | 41.31 | 999.77  | 93.44 | 486.6  | 481.99694115337115 |
| 4.77 | 39.33 | 1011.32 | 68.98 | 494.91 | 487.0395505377602  |
| 4.78 | 42.85 | 1013.39 | 93.36 | 481.47 | 482.7127506383782  |
| 4.83 | 38.44 | 1015.35 | 72.94 | 485.32 | 486.9191795580812  |
| 4.86 | 39.4  | 1012.73 | 91.39 | 488.63 | 483.6685673220653  |
| 4.89 | 45.87 | 1007.58 | 99.35 | 482.69 | 480.3452494934288  |
| 4.95 | 42.07 | 1004.87 | 80.88 | 485.67 | 483.6820847979367  |
| 4.96 | 39.4  | 1003.58 | 92.22 | 486.09 | 482.5556900620063  |
| 4.96 | 40.07 | 1011.8  | 67.38 | 494.75 | 486.76704382567345 |
| 5.07 | 40.07 | 1019.32 | 66.17 | 494.87 | 487.39276206190476 |
| 5.19 | 40.78 | 1025.24 | 95.07 | 482.46 | 483.2391853805797  |
| 5.24 | 38.68 | 1018.03 | 78.65 | 486.67 | 485.46804748846023 |

Truncated to 30 rows

Now that we have real predictions we can use an evaluation metric such as Root Mean Squared Error to validate our regression model. The lower the Root Mean Squared Error, the better our model.

``` scala
//Now let's compute some evaluation metrics against our test dataset

import org.apache.spark.mllib.evaluation.RegressionMetrics 

val metrics = new RegressionMetrics(predictionsAndLabels.select("Predicted_PE", "PE").rdd.map(r => (r(0).asInstanceOf[Double], r(1).asInstanceOf[Double])))
```

>     import org.apache.spark.mllib.evaluation.RegressionMetrics
>     metrics: org.apache.spark.mllib.evaluation.RegressionMetrics = org.apache.spark.mllib.evaluation.RegressionMetrics@78aa5b8c

``` scala
val rmse = metrics.rootMeanSquaredError
```

>     rmse: Double = 4.609375859170583

``` scala
val explainedVariance = metrics.explainedVariance
```

>     explainedVariance: Double = 274.54186073318266

``` scala
val r2 = metrics.r2
```

>     r2: Double = 0.9308377700269259

``` scala
println (f"Root Mean Squared Error: $rmse")
println (f"Explained Variance: $explainedVariance")  
println (f"R2: $r2")
```

>     Root Mean Squared Error: 4.609375859170583
>     Explained Variance: 274.54186073318266
>     R2: 0.9308377700269259

Generally a good model will have 68% of predictions within 1 RMSE and 95% within 2 RMSE of the actual value. Let's calculate and see if our RMSE meets this criteria.

``` scala
display(predictionsAndLabels) // recall the DataFrame predictionsAndLabels
```

``` scala
// First we calculate the residual error and divide it by the RMSE from predictionsAndLabels DataFrame and make another DataFrame that is registered as a temporary table Power_Plant_RMSE_Evaluation
predictionsAndLabels.selectExpr("PE", "Predicted_PE", "PE - Predicted_PE AS Residual_Error", s""" (PE - Predicted_PE) / $rmse AS Within_RSME""").createOrReplaceTempView("Power_Plant_RMSE_Evaluation")
```

``` sql
SELECT * from Power_Plant_RMSE_Evaluation
```

| PE     | Predicted\_PE      | Residual\_Error      | Within\_RSME          |
|--------|--------------------|----------------------|-----------------------|
| 490.55 | 492.8503868481024  | -2.3003868481023915  | -0.49906688419119855  |
| 489.86 | 483.9368120270272  | 5.923187972972812    | 1.2850303715606821    |
| 489.11 | 483.850459922409   | 5.259540077590998    | 1.1410525499080058    |
| 459.86 | 487.4251507226833  | -27.565150722683313  | -5.980234974295072    |
| 489.07 | 488.37401129434335 | 0.6959887056566458   | 0.15099413172652035   |
| 487.87 | 487.1505396071426  | 0.7194603928573997   | 0.1560862934243033    |
| 488.67 | 487.6355351796776  | 1.0344648203223983   | 0.22442622427161782   |
| 488.81 | 483.9896378767201  | 4.820362123279892    | 1.045773282664624     |
| 490.79 | 484.0618847149547  | 6.728115285045305    | 1.4596586372229519    |
| 489.8  | 483.8158776062654  | 5.984122393734594    | 1.2982500400415133    |
| 491.22 | 483.77650720118083 | 7.443492798819193    | 1.6148591536552597    |
| 489.0  | 487.61554926022393 | 1.3844507397760708   | 0.30035535874594327   |
| 491.25 | 483.6343648504441  | 7.615635149555885    | 1.6522052838030554    |
| 491.9  | 485.6397981724803  | 6.260201827519666    | 1.3581452280713195    |
| 486.53 | 487.37706899378225 | -0.8470689937822726  | -0.1837708660917696   |
| 492.85 | 485.96972834538735 | 6.88027165461267     | 1.4926688265015375    |
| 489.36 | 485.11862159667663 | 4.241378403323381    | 0.9201632786974722    |
| 486.34 | 486.79899634464203 | -0.45899634464205974 | -9.957884942900971e-2 |
| 486.6  | 481.99694115337115 | 4.603058846628869    | 0.9986295297379263    |
| 494.91 | 487.0395505377602  | 7.870449462239833    | 1.707487022691192     |
| 481.47 | 482.7127506383782  | -1.2427506383781974  | -0.26961364756264844  |
| 485.32 | 486.9191795580812  | -1.5991795580812322  | -0.346940585220358    |
| 488.63 | 483.6685673220653  | 4.961432677934681    | 1.076378414240979     |
| 482.69 | 480.3452494934288  | 2.344750506571188    | 0.5086915405056825    |
| 485.67 | 483.6820847979367  | 1.9879152020633342   | 0.4312764380253951    |
| 486.09 | 482.5556900620063  | 3.534309937993669    | 0.766765402947556     |
| 494.75 | 486.76704382567345 | 7.982956174326546    | 1.7318952539841284    |
| 494.87 | 487.39276206190476 | 7.477237938095243    | 1.6221801316590196    |
| 482.46 | 483.2391853805797  | -0.7791853805797473  | -0.16904357648108023  |
| 486.67 | 485.46804748846023 | 1.2019525115397869   | 0.26076253016955486   |

Truncated to 30 rows

``` sql
-- Now we can display the RMSE as a Histogram. Clearly this shows that the RMSE is centered around 0 with the vast majority of the error within 2 RMSEs.
SELECT Within_RSME  from Power_Plant_RMSE_Evaluation
```

| Within\_RSME          |
|-----------------------|
| -0.49906688419119855  |
| 1.2850303715606821    |
| 1.1410525499080058    |
| -5.980234974295072    |
| 0.15099413172652035   |
| 0.1560862934243033    |
| 0.22442622427161782   |
| 1.045773282664624     |
| 1.4596586372229519    |
| 1.2982500400415133    |
| 1.6148591536552597    |
| 0.30035535874594327   |
| 1.6522052838030554    |
| 1.3581452280713195    |
| -0.1837708660917696   |
| 1.4926688265015375    |
| 0.9201632786974722    |
| -9.957884942900971e-2 |
| 0.9986295297379263    |
| 1.707487022691192     |
| -0.26961364756264844  |
| -0.346940585220358    |
| 1.076378414240979     |
| 0.5086915405056825    |
| 0.4312764380253951    |
| 0.766765402947556     |
| 1.7318952539841284    |
| 1.6221801316590196    |
| -0.16904357648108023  |
| 0.26076253016955486   |

Truncated to 30 rows

We can see this definitively if we count the number of predictions within + or - 1.0 and + or - 2.0 and display this as a pie chart:

``` sql
SELECT case when Within_RSME <= 1.0 and Within_RSME >= -1.0 then 1  when  Within_RSME <= 2.0 and Within_RSME >= -2.0 then 2 else 3 end RSME_Multiple, COUNT(*) count  from Power_Plant_RMSE_Evaluation
group by case when Within_RSME <= 1.0 and Within_RSME >= -1.0 then 1  when  Within_RSME <= 2.0 and Within_RSME >= -2.0 then 2 else 3 end
```

| RSME\_Multiple | count  |
|----------------|--------|
| 1.0            | 1312.0 |
| 3.0            | 55.0   |
| 2.0            | 599.0  |

So we have about 70% of our training data within 1 RMSE and about 97% (70% + 27%) within 2 RMSE. So the model is pretty decent. Let's see if we can tune the model to improve it further.

**NOTE:** these numbers will vary across runs due to the seed in random sampling of training and test set, number of iterations, and other stopping rules in optimization, for example.

Step 7: Tuning and Evaluation
=============================

Now that we have a model with all of the data let's try to make a better model by tuning over several parameters.

``` scala
import org.apache.spark.ml.tuning.{ParamGridBuilder, CrossValidator}
import org.apache.spark.ml.evaluation._
```

>     import org.apache.spark.ml.tuning.{ParamGridBuilder, CrossValidator}
>     import org.apache.spark.ml.evaluation._

First let's use a cross validator to split the data into training and validation subsets. See <http://spark.apache.org/docs/latest/ml-tuning.html>.

``` scala
//Let's set up our evaluator class to judge the model based on the best root mean squared error
val regEval = new RegressionEvaluator()
regEval.setLabelCol("PE")
  .setPredictionCol("Predicted_PE")
  .setMetricName("rmse")
```

>     regEval: org.apache.spark.ml.evaluation.RegressionEvaluator = regEval_e2be8a782fd8
>     res37: regEval.type = regEval_e2be8a782fd8

We now treat the `lrPipeline` as an `Estimator`, wrapping it in a `CrossValidator` instance.

This will allow us to jointly choose parameters for all Pipeline stages.

A `CrossValidator` requires an `Estimator`, an `Evaluator` (which we `set` next).

``` scala
//Let's create our crossvalidator with 3 fold cross validation
val crossval = new CrossValidator()
crossval.setEstimator(lrPipeline)
crossval.setNumFolds(3)
crossval.setEvaluator(regEval)
```

>     crossval: org.apache.spark.ml.tuning.CrossValidator = cv_414cd3231d9a
>     res38: crossval.type = cv_414cd3231d9a

A `CrossValidator` also requires a set of `EstimatorParamMaps` which we `set` next.

For this we need a regularization parameter (more generally a hyper-parameter that is model-specific).

Now, let's tune over our regularization parameter from 0.01 to 0.10.

``` scala
val regParam = ((1 to 10) toArray).map(x => (x /100.0))
```

>     warning: there was one feature warning; re-run with -feature for details
>     regParam: Array[Double] = Array(0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1)

Check out the scala docs for syntactic details on [org.apache.spark.ml.tuning.ParamGridBuilder](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.ml.tuning.ParamGridBuilder).

``` scala
val paramGrid = new ParamGridBuilder()
                    .addGrid(lr.regParam, regParam)
                    .build()
crossval.setEstimatorParamMaps(paramGrid)
```

>     paramGrid: Array[org.apache.spark.ml.param.ParamMap] =
>     Array({
>     	linReg_955431dccb4f-regParam: 0.01
>     }, {
>     	linReg_955431dccb4f-regParam: 0.02
>     }, {
>     	linReg_955431dccb4f-regParam: 0.03
>     }, {
>     	linReg_955431dccb4f-regParam: 0.04
>     }, {
>     	linReg_955431dccb4f-regParam: 0.05
>     }, {
>     	linReg_955431dccb4f-regParam: 0.06
>     }, {
>     	linReg_955431dccb4f-regParam: 0.07
>     }, {
>     	linReg_955431dccb4f-regParam: 0.08
>     }, {
>     	linReg_955431dccb4f-regParam: 0.09
>     }, {
>     	linReg_955431dccb4f-regParam: 0.1
>     })
>     res39: crossval.type = cv_414cd3231d9a

``` scala
//Now let's create our model
val cvModel = crossval.fit(trainingSet)
```

>     cvModel: org.apache.spark.ml.tuning.CrossValidatorModel = cv_414cd3231d9a

In addition to `CrossValidator` Spark also offers `TrainValidationSplit` for hyper-parameter tuning. `TrainValidationSplit` only evaluates each combination of parameters once as opposed to k times in case of `CrossValidator`. It is therefore less expensive, but will not produce as reliable results when the training dataset is not sufficiently large.

-   <http://spark.apache.org/docs/latest/ml-tuning.html#train-validation-split>

Now that we have tuned let's see what we got for tuning parameters and what our RMSE was versus our intial model

``` scala
val predictionsAndLabels = cvModel.transform(testSet)
val metrics = new RegressionMetrics(predictionsAndLabels.select("Predicted_PE", "PE").rdd.map(r => (r(0).asInstanceOf[Double], r(1).asInstanceOf[Double])))

val rmse = metrics.rootMeanSquaredError
val explainedVariance = metrics.explainedVariance
val r2 = metrics.r2
```

>     predictionsAndLabels: org.apache.spark.sql.DataFrame = [AT: double, V: double ... 5 more fields]
>     metrics: org.apache.spark.mllib.evaluation.RegressionMetrics = org.apache.spark.mllib.evaluation.RegressionMetrics@2b71aef4
>     rmse: Double = 4.599964072968395
>     explainedVariance: Double = 277.2272873387723
>     r2: Double = 0.9311199234339246

``` scala
println (f"Root Mean Squared Error: $rmse")
println (f"Explained Variance: $explainedVariance")  
println (f"R2: $r2")
```

>     Root Mean Squared Error: 4.599964072968395
>     Explained Variance: 277.2272873387723
>     R2: 0.9311199234339246

Let us explore other models to see if we can predict the power output better
----------------------------------------------------------------------------

There are several families of models in Spark's scalable machine learning library:

-   <http://spark.apache.org/docs/latest/ml-classification-regression.html>

So our initial untuned and tuned linear regression models are statistically identical.

Given that the only linearly correlated variable is Temperature, it makes sense try another machine learning method such a Decision Tree to handle non-linear data and see if we can improve our model

A Decision Tree creates a model based on splitting variables using a tree structure. We will first start with a single decision tree model.

Reference Decision Trees: https://en.wikipedia.org/wiki/Decision*tree*learning

``` scala
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
```

>     import org.apache.spark.ml.regression.DecisionTreeRegressor
>     dt: org.apache.spark.ml.regression.DecisionTreeRegressor = dtr_23e04c8c3476
>     dtPipeline: org.apache.spark.ml.Pipeline = pipeline_382103e9e31e
>     res41: dtPipeline.type = pipeline_382103e9e31e

``` scala
//Let's just resuse our CrossValidator
crossval.setEstimator(dtPipeline)
```

>     res42: crossval.type = cv_414cd3231d9a

``` scala
val paramGrid = new ParamGridBuilder()
                     .addGrid(dt.maxDepth, Array(2, 3))
                     .build()
```

>     paramGrid: Array[org.apache.spark.ml.param.ParamMap] =
>     Array({
>     	dtr_23e04c8c3476-maxDepth: 2
>     }, {
>     	dtr_23e04c8c3476-maxDepth: 3
>     })

``` scala
crossval.setEstimatorParamMaps(paramGrid)
```

>     res43: crossval.type = cv_414cd3231d9a

``` scala
val dtModel = crossval.fit(trainingSet) // fit decitionTree with cv
```

>     dtModel: org.apache.spark.ml.tuning.CrossValidatorModel = cv_414cd3231d9a

``` scala
import org.apache.spark.ml.regression.DecisionTreeRegressionModel
import org.apache.spark.ml.PipelineModel
dtModel.bestModel.asInstanceOf[PipelineModel].stages.last.asInstanceOf[DecisionTreeRegressionModel].toDebugString
```

>     import org.apache.spark.ml.regression.DecisionTreeRegressionModel
>     import org.apache.spark.ml.PipelineModel
>     res44: String =
>     "DecisionTreeRegressionModel (uid=dtr_23e04c8c3476) of depth 3 with 15 nodes
>       If (feature 0 <= 17.84)
>        If (feature 0 <= 11.95)
>         If (feature 0 <= 8.75)
>          Predict: 483.5412151067323
>         Else (feature 0 > 8.75)
>          Predict: 475.6305502392345
>        Else (feature 0 > 11.95)
>         If (feature 0 <= 15.33)
>          Predict: 467.63141917293234
>         Else (feature 0 > 15.33)
>          Predict: 460.74754125412574
>       Else (feature 0 > 17.84)
>        If (feature 0 <= 23.02)
>         If (feature 1 <= 47.83)
>          Predict: 457.1077966101695
>         Else (feature 1 > 47.83)
>          Predict: 448.74750213858016
>        Else (feature 0 > 23.02)
>         If (feature 1 <= 66.25)
>          Predict: 442.88544855967086
>         Else (feature 1 > 66.25)
>          Predict: 434.7293710691822
>     "

The line above will pull the Decision Tree model from the Pipeline and display it as an if-then-else string.

Next let's visualize it as a decision tree for regression.

``` scala
display(dtModel.bestModel.asInstanceOf[PipelineModel].stages.last.asInstanceOf[DecisionTreeRegressionModel])
```

| treeNode                                                                                                                           |
|------------------------------------------------------------------------------------------------------------------------------------|
| {"index":7,"featureType":"continuous","prediction":null,"threshold":17.84,"categories":null,"feature":0,"overflow":false}          |
| {"index":3,"featureType":"continuous","prediction":null,"threshold":11.95,"categories":null,"feature":0,"overflow":false}          |
| {"index":1,"featureType":"continuous","prediction":null,"threshold":8.75,"categories":null,"feature":0,"overflow":false}           |
| {"index":0,"featureType":null,"prediction":483.5412151067323,"threshold":null,"categories":null,"feature":null,"overflow":false}   |
| {"index":2,"featureType":null,"prediction":475.6305502392345,"threshold":null,"categories":null,"feature":null,"overflow":false}   |
| {"index":5,"featureType":"continuous","prediction":null,"threshold":15.33,"categories":null,"feature":0,"overflow":false}          |
| {"index":4,"featureType":null,"prediction":467.63141917293234,"threshold":null,"categories":null,"feature":null,"overflow":false}  |
| {"index":6,"featureType":null,"prediction":460.74754125412574,"threshold":null,"categories":null,"feature":null,"overflow":false}  |
| {"index":11,"featureType":"continuous","prediction":null,"threshold":23.02,"categories":null,"feature":0,"overflow":false}         |
| {"index":9,"featureType":"continuous","prediction":null,"threshold":47.83,"categories":null,"feature":1,"overflow":false}          |
| {"index":8,"featureType":null,"prediction":457.1077966101695,"threshold":null,"categories":null,"feature":null,"overflow":false}   |
| {"index":10,"featureType":null,"prediction":448.74750213858016,"threshold":null,"categories":null,"feature":null,"overflow":false} |
| {"index":13,"featureType":"continuous","prediction":null,"threshold":66.25,"categories":null,"feature":1,"overflow":false}         |
| {"index":12,"featureType":null,"prediction":442.88544855967086,"threshold":null,"categories":null,"feature":null,"overflow":false} |
| {"index":14,"featureType":null,"prediction":434.7293710691822,"threshold":null,"categories":null,"feature":null,"overflow":false}  |

Now let's see how our DecisionTree model compares to our LinearRegression model

``` scala
val predictionsAndLabels = dtModel.bestModel.transform(testSet)
val metrics = new RegressionMetrics(predictionsAndLabels.select("Predicted_PE", "PE").map(r => (r(0).asInstanceOf[Double], r(1).asInstanceOf[Double])).rdd)

val rmse = metrics.rootMeanSquaredError
val explainedVariance = metrics.explainedVariance
val r2 = metrics.r2

println (f"Root Mean Squared Error: $rmse")
println (f"Explained Variance: $explainedVariance")  
println (f"R2: $r2")
```

>     Root Mean Squared Error: 5.221342219456633
>     Explained Variance: 269.66550072645475
>     R2: 0.9112539444165726
>     predictionsAndLabels: org.apache.spark.sql.DataFrame = [AT: double, V: double ... 5 more fields]
>     metrics: org.apache.spark.mllib.evaluation.RegressionMetrics = org.apache.spark.mllib.evaluation.RegressionMetrics@1b14107a
>     rmse: Double = 5.221342219456633
>     explainedVariance: Double = 269.66550072645475
>     r2: Double = 0.9112539444165726

So our DecisionTree was slightly worse than our LinearRegression model (LR: 4.6 vs DT: 5.2). Maybe we can try an Ensemble method such as Gradient-Boosted Decision Trees to see if we can strengthen our model by using an ensemble of weaker trees with weighting to reduce the error in our model.

*Note since this is a complex model, the cell below can take about *16 minutes\* or so to run on a small cluster with a couple nodes with about 6GB RAM, go out and grab a coffee and come back :-).\*

This GBTRegressor code will be way faster on a larger cluster of course.

A visual explanation of gradient boosted trees:

-   [http://arogozhnikov.github.io/2016/06/24/gradient*boosting*explained.html](http://arogozhnikov.github.io/2016/06/24/gradient_boosting_explained.html)

Let's see what a boosting algorithm, a type of ensemble method, is all about in more detail.

<p class="htmlSandbox"><iframe 
 src="https://en.wikipedia.org/wiki/Gradient_boosting"
 width="95%" height="500"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe></p>

This can take between 5 - 15 minutes in a shard with 6 workers depending on other workloads (may be longer in the Community Edition).

``` scala
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
```

>     import org.apache.spark.ml.regression.GBTRegressor
>     gbt: org.apache.spark.ml.regression.GBTRegressor = gbtr_9c5ab45fe584
>     gbtPipeline: org.apache.spark.ml.Pipeline = pipeline_e6a84d2d75ba
>     paramGrid: Array[org.apache.spark.ml.param.ParamMap] =
>     Array({
>     	gbtr_9c5ab45fe584-maxDepth: 2
>     }, {
>     	gbtr_9c5ab45fe584-maxDepth: 3
>     })
>     gbtModel: org.apache.spark.ml.tuning.CrossValidatorModel = cv_414cd3231d9a

``` scala
import org.apache.spark.ml.regression.GBTRegressionModel 

val predictionsAndLabels = gbtModel.bestModel.transform(testSet)
val metrics = new RegressionMetrics(predictionsAndLabels.select("Predicted_PE", "PE").map(r => (r(0).asInstanceOf[Double], r(1).asInstanceOf[Double])).rdd)

val rmse = metrics.rootMeanSquaredError
val explainedVariance = metrics.explainedVariance
val r2 = metrics.r2


println (f"Root Mean Squared Error: $rmse")
println (f"Explained Variance: $explainedVariance")  
println (f"R2: $r2")
```

>     Root Mean Squared Error: 3.7616562931536803
>     Explained Variance: 282.4365553123402
>     R2: 0.9539379816689415
>     import org.apache.spark.ml.regression.GBTRegressionModel
>     predictionsAndLabels: org.apache.spark.sql.DataFrame = [AT: double, V: double ... 5 more fields]
>     metrics: org.apache.spark.mllib.evaluation.RegressionMetrics = org.apache.spark.mllib.evaluation.RegressionMetrics@1f1d9c34
>     rmse: Double = 3.7616562931536803
>     explainedVariance: Double = 282.4365553123402
>     r2: Double = 0.9539379816689415

Note that the root mean squared error is smaller now due to the ensemble of 120 trees from Gradient Boosting!

We can use the toDebugString method to dump out what our trees and weighting look like:

``` scala
gbtModel.bestModel.asInstanceOf[PipelineModel].stages.last.asInstanceOf[GBTRegressionModel].toDebugString
```

>     res49: String =
>     "GBTRegressionModel (uid=gbtr_9c5ab45fe584) with 120 trees
>       Tree 0 (weight 1.0):
>         If (feature 0 <= 17.84)
>          If (feature 0 <= 11.95)
>           If (feature 0 <= 8.75)
>            Predict: 483.5412151067323
>           Else (feature 0 > 8.75)
>            Predict: 475.6305502392345
>          Else (feature 0 > 11.95)
>           If (feature 0 <= 15.33)
>            Predict: 467.63141917293234
>           Else (feature 0 > 15.33)
>            Predict: 460.74754125412574
>         Else (feature 0 > 17.84)
>          If (feature 0 <= 23.02)
>           If (feature 1 <= 47.83)
>            Predict: 457.1077966101695
>           Else (feature 1 > 47.83)
>            Predict: 448.74750213858016
>          Else (feature 0 > 23.02)
>           If (feature 1 <= 66.25)
>            Predict: 442.88544855967086
>           Else (feature 1 > 66.25)
>            Predict: 434.7293710691822
>       Tree 1 (weight 0.1):
>         If (feature 2 <= 1009.9)
>          If (feature 1 <= 43.13)
>           If (feature 0 <= 15.33)
>            Predict: -0.31094207231231485
>           Else (feature 0 > 15.33)
>            Predict: 4.302958436537365
>          Else (feature 1 > 43.13)
>           If (feature 0 <= 23.02)
>            Predict: -8.38392506141353
>           Else (feature 0 > 23.02)
>            Predict: -2.399960976520273
>         Else (feature 2 > 1009.9)
>          If (feature 3 <= 86.21)
>           If (feature 0 <= 26.35)
>            Predict: 2.888623754019027
>           Else (feature 0 > 26.35)
>            Predict: -1.1489483044194229
>          Else (feature 3 > 86.21)
>           If (feature 1 <= 39.72)
>            Predict: 3.876324424163314
>           Else (feature 1 > 39.72)
>            Predict: -3.058952828112949
>       Tree 2 (weight 0.1):
>         If (feature 2 <= 1009.63)
>          If (feature 1 <= 43.13)
>           If (feature 0 <= 11.95)
>            Predict: -1.4091086031845625
>           Else (feature 0 > 11.95)
>            Predict: 2.6329466235800942
>          Else (feature 1 > 43.13)
>           If (feature 0 <= 23.02)
>            Predict: -6.795414480322956
>           Else (feature 0 > 23.02)
>            Predict: -2.166560698742912
>         Else (feature 2 > 1009.63)
>          If (feature 3 <= 80.44)
>           If (feature 0 <= 26.98)
>            Predict: 2.878622882275939
>           Else (feature 0 > 26.98)
>            Predict: -1.146426969990865
>          Else (feature 3 > 80.44)
>           If (feature 3 <= 94.55)
>            Predict: -0.35885921725905906
>           Else (feature 3 > 94.55)
>            Predict: -5.75364586186002
>       Tree 3 (weight 0.1):
>         If (feature 0 <= 27.6)
>          If (feature 3 <= 70.2)
>           If (feature 1 <= 40.05)
>            Predict: -0.9480831286616939
>           Else (feature 1 > 40.05)
>            Predict: 3.660397090904016
>          Else (feature 3 > 70.2)
>           If (feature 1 <= 40.64)
>            Predict: 2.1539405832035627
>           Else (feature 1 > 40.64)
>            Predict: -1.2281619807661366
>         Else (feature 0 > 27.6)
>          If (feature 1 <= 65.27)
>           If (feature 2 <= 1005.99)
>            Predict: -15.33433697033558
>           Else (feature 2 > 1005.99)
>            Predict: -5.866095468145647
>          Else (feature 1 > 65.27)
>           If (feature 2 <= 1008.75)
>            Predict: -4.03431044067007
>           Else (feature 2 > 1008.75)
>            Predict: -0.23440867445577207
>       Tree 4 (weight 0.1):
>         If (feature 0 <= 26.35)
>          If (feature 0 <= 23.02)
>           If (feature 1 <= 68.67)
>            Predict: 0.12035773384797814
>           Else (feature 1 > 68.67)
>            Predict: -13.928523073642005
>          Else (feature 0 > 23.02)
>           If (feature 0 <= 24.57)
>            Predict: 5.5622340839882165
>           Else (feature 0 > 24.57)
>            Predict: 1.6938172370244715
>         Else (feature 0 > 26.35)
>          If (feature 1 <= 66.25)
>           If (feature 2 <= 1008.4)
>            Predict: -9.009916879825393
>           Else (feature 2 > 1008.4)
>            Predict: -3.059736394918022
>          Else (feature 1 > 66.25)
>           If (feature 0 <= 30.2)
>            Predict: 0.14704705100738577
>           Else (feature 0 > 30.2)
>            Predict: -3.2914123948921006
>       Tree 5 (weight 0.1):
>         If (feature 2 <= 1010.41)
>          If (feature 1 <= 43.41)
>           If (feature 3 <= 99.27)
>            Predict: 1.2994444710077233
>           Else (feature 3 > 99.27)
>            Predict: -6.649548317319231
>          Else (feature 1 > 43.41)
>           If (feature 0 <= 23.02)
>            Predict: -4.9119452777748
>           Else (feature 0 > 23.02)
>            Predict: -0.9514185089440673
>         Else (feature 2 > 1010.41)
>          If (feature 3 <= 89.83)
>           If (feature 0 <= 31.26)
>            Predict: 1.2914123584761403
>           Else (feature 0 > 31.26)
>            Predict: -5.115001417285994
>          Else (feature 3 > 89.83)
>           If (feature 1 <= 40.64)
>            Predict: 1.5160976219176363
>           Else (feature 1 > 40.64)
>            Predict: -4.202813699523934
>       Tree 6 (weight 0.1):
>         If (feature 2 <= 1007.27)
>          If (feature 0 <= 27.94)
>           If (feature 3 <= 71.09)
>            Predict: 1.616448005210527
>           Else (feature 3 > 71.09)
>            Predict: -2.1313527108274157
>          Else (feature 0 > 27.94)
>           If (feature 1 <= 68.3)
>            Predict: -8.579840063013142
>           Else (feature 1 > 68.3)
>            Predict: -1.915909819494233
>         Else (feature 2 > 1007.27)
>          If (feature 3 <= 95.45)
>           If (feature 0 <= 6.52)
>            Predict: 4.973465595410054
>           Else (feature 0 > 6.52)
>            Predict: 0.3837975458985242
>          Else (feature 3 > 95.45)
>           If (feature 2 <= 1013.43)
>            Predict: -0.8175453481344352
>           Else (feature 2 > 1013.43)
>            Predict: -7.264843604639278
>       Tree 7 (weight 0.1):
>         If (feature 0 <= 26.35)
>          If (feature 3 <= 71.09)
>           If (feature 1 <= 67.83)
>            Predict: 1.9620965187817083
>           Else (feature 1 > 67.83)
>            Predict: 7.953863660960779
>          Else (feature 3 > 71.09)
>           If (feature 1 <= 40.89)
>            Predict: 1.2020440154192213
>           Else (feature 1 > 40.89)
>            Predict: -0.9989659748111419
>         Else (feature 0 > 26.35)
>          If (feature 1 <= 66.25)
>           If (feature 2 <= 1008.4)
>            Predict: -6.230272922553423
>           Else (feature 2 > 1008.4)
>            Predict: -2.654681371247991
>          Else (feature 1 > 66.25)
>           If (feature 2 <= 1004.52)
>            Predict: -3.9527797601131853
>           Else (feature 2 > 1004.52)
>            Predict: -0.21770148036273387
>       Tree 8 (weight 0.1):
>         If (feature 0 <= 29.56)
>          If (feature 3 <= 63.16)
>           If (feature 1 <= 72.24)
>            Predict: 1.9612116105231265
>           Else (feature 1 > 72.24)
>            Predict: 8.756949826030025
>          Else (feature 3 > 63.16)
>           If (feature 0 <= 5.95)
>            Predict: 4.445363585074405
>           Else (feature 0 > 5.95)
>            Predict: -0.4097996897633835
>         Else (feature 0 > 29.56)
>          If (feature 1 <= 68.3)
>           If (feature 2 <= 1009.9)
>            Predict: -7.882200867406393
>           Else (feature 2 > 1009.9)
>            Predict: -1.7273221348184091
>          Else (feature 1 > 68.3)
>           If (feature 2 <= 1013.77)
>            Predict: -0.7219749804525829
>           Else (feature 2 > 1013.77)
>            Predict: -6.492100849806538
>       Tree 9 (weight 0.1):
>         If (feature 3 <= 89.83)
>          If (feature 0 <= 25.72)
>           If (feature 0 <= 23.02)
>            Predict: 0.15450088997272685
>           Else (feature 0 > 23.02)
>            Predict: 3.010254802875794
>          Else (feature 0 > 25.72)
>           If (feature 1 <= 66.25)
>            Predict: -2.5821765284417615
>           Else (feature 1 > 66.25)
>            Predict: -0.3935112713804148
>         Else (feature 3 > 89.83)
>          If (feature 2 <= 1019.52)
>           If (feature 0 <= 7.08)
>            Predict: 3.264389020443774
>           Else (feature 0 > 7.08)
>            Predict: -1.6246048211383168
>          Else (feature 2 > 1019.52)
>           If (feature 0 <= 8.75)
>            Predict: -8.005340799169343
>           Else (feature 0 > 8.75)
>            Predict: -2.9832409167030063
>       Tree 10 (weight 0.1):
>         If (feature 1 <= 56.57)
>          If (feature 0 <= 17.84)
>           If (feature 1 <= 45.87)
>            Predict: 0.26309432916452813
>           Else (feature 1 > 45.87)
>            Predict: -5.716473785544373
>          Else (feature 0 > 17.84)
>           If (feature 2 <= 1012.56)
>            Predict: -0.15863259341493433
>           Else (feature 2 > 1012.56)
>            Predict: 7.899625065937478
>         Else (feature 1 > 56.57)
>          If (feature 0 <= 17.84)
>           If (feature 3 <= 67.72)
>            Predict: -27.101084325134025
>           Else (feature 3 > 67.72)
>            Predict: -12.755339130015875
>          Else (feature 0 > 17.84)
>           If (feature 0 <= 20.6)
>            Predict: 3.8741798886113408
>           Else (feature 0 > 20.6)
>            Predict: -0.8179571837367839
>       Tree 11 (weight 0.1):
>         If (feature 2 <= 1004.52)
>          If (feature 1 <= 74.22)
>           If (feature 1 <= 59.14)
>            Predict: -0.6678644068375302
>           Else (feature 1 > 59.14)
>            Predict: -5.0251736913870495
>          Else (feature 1 > 74.22)
>           If (feature 2 <= 1000.68)
>            Predict: -1.9453153753750236
>           Else (feature 2 > 1000.68)
>            Predict: 3.954565899065237
>         Else (feature 2 > 1004.52)
>          If (feature 3 <= 60.81)
>           If (feature 0 <= 29.27)
>            Predict: 2.256991118214201
>           Else (feature 0 > 29.27)
>            Predict: -0.8956432652281918
>          Else (feature 3 > 60.81)
>           If (feature 0 <= 5.18)
>            Predict: 5.30208686561611
>           Else (feature 0 > 5.18)
>            Predict: -0.2275806642044292
>       Tree 12 (weight 0.1):
>         If (feature 3 <= 93.63)
>          If (feature 0 <= 20.6)
>           If (feature 0 <= 17.84)
>            Predict: -0.13650451477274114
>           Else (feature 0 > 17.84)
>            Predict: 4.26138638419226
>          Else (feature 0 > 20.6)
>           If (feature 0 <= 23.02)
>            Predict: -4.145788149131118
>           Else (feature 0 > 23.02)
>            Predict: 0.45010060784860767
>         Else (feature 3 > 93.63)
>          If (feature 0 <= 11.95)
>           If (feature 0 <= 6.52)
>            Predict: 1.9630864105825856
>           Else (feature 0 > 6.52)
>            Predict: -5.847103580294793
>          Else (feature 0 > 11.95)
>           If (feature 1 <= 57.85)
>            Predict: 1.6850763767018282
>           Else (feature 1 > 57.85)
>            Predict: -3.57522814358917
>       Tree 13 (weight 0.1):
>         If (feature 1 <= 56.57)
>          If (feature 1 <= 49.39)
>           If (feature 0 <= 13.56)
>            Predict: 0.7497248523199469
>           Else (feature 0 > 13.56)
>            Predict: -0.8096048572768345
>          Else (feature 1 > 49.39)
>           If (feature 0 <= 17.84)
>            Predict: -4.9975868045736025
>           Else (feature 0 > 17.84)
>            Predict: 6.70181838603398
>         Else (feature 1 > 56.57)
>          If (feature 0 <= 17.84)
>           If (feature 1 <= 58.62)
>            Predict: -8.139327595464518
>           Else (feature 1 > 58.62)
>            Predict: -11.260696586956563
>          Else (feature 0 > 17.84)
>           If (feature 2 <= 1020.32)
>            Predict: -0.4173502593107514
>           Else (feature 2 > 1020.32)
>            Predict: 7.350524302545053
>       Tree 14 (weight 0.1):
>         If (feature 2 <= 1009.3)
>          If (feature 1 <= 73.67)
>           If (feature 0 <= 26.35)
>            Predict: -0.2834715308768144
>           Else (feature 0 > 26.35)
>            Predict: -2.2855655986052446
>          Else (feature 1 > 73.67)
>           If (feature 0 <= 21.42)
>            Predict: -19.886551554013977
>           Else (feature 0 > 21.42)
>            Predict: 1.8345107899392203
>         Else (feature 2 > 1009.3)
>          If (feature 0 <= 17.84)
>           If (feature 1 <= 46.93)
>            Predict: 0.2012146645141011
>           Else (feature 1 > 46.93)
>            Predict: -5.331252849501989
>          Else (feature 0 > 17.84)
>           If (feature 0 <= 20.6)
>            Predict: 3.9009310043506518
>           Else (feature 0 > 20.6)
>            Predict: 0.05492627340134294
>       Tree 15 (weight 0.1):
>         If (feature 3 <= 80.44)
>          If (feature 0 <= 26.57)
>           If (feature 0 <= 23.02)
>            Predict: 0.24935555983937532
>           Else (feature 0 > 23.02)
>            Predict: 1.9734839371689987
>          Else (feature 0 > 26.57)
>           If (feature 1 <= 66.25)
>            Predict: -2.652691255012269
>           Else (feature 1 > 66.25)
>            Predict: 0.10205623249441657
>         Else (feature 3 > 80.44)
>          If (feature 1 <= 57.85)
>           If (feature 2 <= 1021.65)
>            Predict: 0.3189331596273633
>           Else (feature 2 > 1021.65)
>            Predict: -2.493847422724499
>          Else (feature 1 > 57.85)
>           If (feature 0 <= 23.02)
>            Predict: -4.443277995263894
>           Else (feature 0 > 23.02)
>            Predict: 1.0414575062489446
>       Tree 16 (weight 0.1):
>         If (feature 0 <= 6.52)
>          If (feature 3 <= 67.72)
>           If (feature 1 <= 39.48)
>            Predict: -0.021931809818089017
>           Else (feature 1 > 39.48)
>            Predict: 17.644618798102908
>          Else (feature 3 > 67.72)
>           If (feature 1 <= 42.07)
>            Predict: 2.6927240976688487
>           Else (feature 1 > 42.07)
>            Predict: -3.720328734281554
>         Else (feature 0 > 6.52)
>          If (feature 0 <= 8.75)
>           If (feature 0 <= 7.97)
>            Predict: -1.1870026837027776
>           Else (feature 0 > 7.97)
>            Predict: -6.311604790035118
>          Else (feature 0 > 8.75)
>           If (feature 0 <= 9.73)
>            Predict: 5.036277690956247
>           Else (feature 0 > 9.73)
>            Predict: -0.07156864179175153
>       Tree 17 (weight 0.1):
>         If (feature 2 <= 1005.35)
>          If (feature 1 <= 70.8)
>           If (feature 0 <= 21.14)
>            Predict: 0.2557898848412102
>           Else (feature 0 > 21.14)
>            Predict: -4.092246463553751
>          Else (feature 1 > 70.8)
>           If (feature 0 <= 23.02)
>            Predict: -17.7762740471523
>           Else (feature 0 > 23.02)
>            Predict: 1.4679036019616782
>         Else (feature 2 > 1005.35)
>          If (feature 3 <= 60.81)
>           If (feature 2 <= 1021.17)
>            Predict: 0.8109918761137652
>           Else (feature 2 > 1021.17)
>            Predict: 6.491756407811347
>          Else (feature 3 > 60.81)
>           If (feature 0 <= 25.72)
>            Predict: 0.06495066055048145
>           Else (feature 0 > 25.72)
>            Predict: -1.234843690619109
>       Tree 18 (weight 0.1):
>         If (feature 3 <= 93.63)
>          If (feature 0 <= 13.56)
>           If (feature 0 <= 11.95)
>            Predict: -0.1389635939018028
>           Else (feature 0 > 11.95)
>            Predict: 4.085304226900187
>          Else (feature 0 > 13.56)
>           If (feature 0 <= 15.33)
>            Predict: -3.558076811842663
>           Else (feature 0 > 15.33)
>            Predict: 0.24840255719067195
>         Else (feature 3 > 93.63)
>          If (feature 0 <= 11.95)
>           If (feature 0 <= 6.52)
>            Predict: 1.1725211739721944
>           Else (feature 0 > 6.52)
>            Predict: -4.696815201291802
>          Else (feature 0 > 11.95)
>           If (feature 0 <= 23.42)
>            Predict: -0.1435586215485262
>           Else (feature 0 > 23.42)
>            Predict: 6.017267110381734
>       Tree 19 (weight 0.1):
>         If (feature 0 <= 29.89)
>          If (feature 3 <= 46.38)
>           If (feature 2 <= 1020.32)
>            Predict: 2.734528637686715
>           Else (feature 2 > 1020.32)
>            Predict: 14.229272221061546
>          Else (feature 3 > 46.38)
>           If (feature 1 <= 73.18)
>            Predict: -0.09112932077559661
>           Else (feature 1 > 73.18)
>            Predict: 2.171636618202333
>         Else (feature 0 > 29.89)
>          If (feature 1 <= 68.3)
>           If (feature 2 <= 1012.96)
>            Predict: -4.842672386234583
>           Else (feature 2 > 1012.96)
>            Predict: 0.4656753436410731
>          Else (feature 1 > 68.3)
>           If (feature 1 <= 69.88)
>            Predict: 1.9998755414672877
>           Else (feature 1 > 69.88)
>            Predict: -1.377187598546301
>       Tree 20 (weight 0.1):
>         If (feature 1 <= 40.89)
>          If (feature 0 <= 11.95)
>           If (feature 0 <= 10.74)
>            Predict: 0.3474341793041741
>           Else (feature 0 > 10.74)
>            Predict: -3.2174625433704844
>          Else (feature 0 > 11.95)
>           If (feature 0 <= 13.56)
>            Predict: 6.2521753652461385
>           Else (feature 0 > 13.56)
>            Predict: 0.7467107076401086
>         Else (feature 1 > 40.89)
>          If (feature 1 <= 41.16)
>           If (feature 2 <= 1011.9)
>            Predict: 1.6159428806525291
>           Else (feature 2 > 1011.9)
>            Predict: -5.525791920129847
>          Else (feature 1 > 41.16)
>           If (feature 1 <= 41.48)
>            Predict: 2.3655609293253264
>           Else (feature 1 > 41.48)
>            Predict: -0.18730957785387015
>       Tree 21 (weight 0.1):
>         If (feature 0 <= 7.08)
>          If (feature 1 <= 41.58)
>           If (feature 1 <= 41.16)
>            Predict: 1.9153935195932974
>           Else (feature 1 > 41.16)
>            Predict: 7.0746807427814735
>          Else (feature 1 > 41.58)
>           If (feature 2 <= 1020.77)
>            Predict: -1.256554177586309
>           Else (feature 2 > 1020.77)
>            Predict: -26.29941855196938
>         Else (feature 0 > 7.08)
>          If (feature 0 <= 8.75)
>           If (feature 1 <= 37.8)
>            Predict: -8.544132394601597
>           Else (feature 1 > 37.8)
>            Predict: -2.6184141709801976
>          Else (feature 0 > 8.75)
>           If (feature 0 <= 9.73)
>            Predict: 4.069411815161333
>           Else (feature 0 > 9.73)
>            Predict: -0.06494039395966968
>       Tree 22 (weight 0.1):
>         If (feature 0 <= 23.02)
>          If (feature 0 <= 21.69)
>           If (feature 0 <= 15.33)
>            Predict: -0.48298234147973435
>           Else (feature 0 > 15.33)
>            Predict: 1.2747845905419344
>          Else (feature 0 > 21.69)
>           If (feature 1 <= 66.25)
>            Predict: -3.44223180465188
>           Else (feature 1 > 66.25)
>            Predict: -9.677838572965495
>         Else (feature 0 > 23.02)
>          If (feature 0 <= 24.39)
>           If (feature 1 <= 66.25)
>            Predict: 1.4289485230939327
>           Else (feature 1 > 66.25)
>            Predict: 7.493228657621072
>          Else (feature 0 > 24.39)
>           If (feature 1 <= 66.25)
>            Predict: -1.55164310941819
>           Else (feature 1 > 66.25)
>            Predict: 0.5159038364280375
>       Tree 23 (weight 0.1):
>         If (feature 2 <= 1010.89)
>          If (feature 1 <= 66.93)
>           If (feature 1 <= 43.41)
>            Predict: 0.8366856528539243
>           Else (feature 1 > 43.41)
>            Predict: -2.146264827541657
>          Else (feature 1 > 66.93)
>           If (feature 0 <= 23.02)
>            Predict: -4.593173040738928
>           Else (feature 0 > 23.02)
>            Predict: 0.7595925761507126
>         Else (feature 2 > 1010.89)
>          If (feature 0 <= 15.33)
>           If (feature 0 <= 14.38)
>            Predict: 0.19019050526253845
>           Else (feature 0 > 14.38)
>            Predict: -4.931089744789576
>          Else (feature 0 > 15.33)
>           If (feature 1 <= 56.57)
>            Predict: 2.893896440054576
>           Else (feature 1 > 56.57)
>            Predict: -0.2411893147021192
>       Tree 24 (weight 0.1):
>         If (feature 2 <= 1004.52)
>          If (feature 1 <= 39.13)
>           If (feature 0 <= 16.56)
>            Predict: 5.674347262101248
>           Else (feature 0 > 16.56)
>            Predict: -15.35003850200303
>          Else (feature 1 > 39.13)
>           If (feature 1 <= 70.8)
>            Predict: -2.2136597249782484
>           Else (feature 1 > 70.8)
>            Predict: 0.4854909471410394
>         Else (feature 2 > 1004.52)
>          If (feature 0 <= 23.02)
>           If (feature 0 <= 21.14)
>            Predict: 0.25072963079321764
>           Else (feature 0 > 21.14)
>            Predict: -3.1127381475029745
>          Else (feature 0 > 23.02)
>           If (feature 0 <= 24.98)
>            Predict: 2.513302584995404
>           Else (feature 0 > 24.98)
>            Predict: -0.17126775916442186
>       Tree 25 (weight 0.1):
>         If (feature 3 <= 76.79)
>          If (feature 0 <= 28.75)
>           If (feature 1 <= 66.25)
>            Predict: 0.1271610430935476
>           Else (feature 1 > 66.25)
>            Predict: 2.4600009065275934
>          Else (feature 0 > 28.75)
>           If (feature 1 <= 44.58)
>            Predict: -10.925990145829292
>           Else (feature 1 > 44.58)
>            Predict: -0.7031644656131009
>         Else (feature 3 > 76.79)
>          If (feature 0 <= 20.9)
>           If (feature 0 <= 17.84)
>            Predict: -0.3807566877980857
>           Else (feature 0 > 17.84)
>            Predict: 2.329590528017136
>          Else (feature 0 > 20.9)
>           If (feature 0 <= 23.02)
>            Predict: -3.741947089345415
>           Else (feature 0 > 23.02)
>            Predict: -0.3619479813878585
>       Tree 26 (weight 0.1):
>         If (feature 0 <= 5.18)
>          If (feature 1 <= 42.07)
>           If (feature 3 <= 84.36)
>            Predict: 5.869887042156764
>           Else (feature 3 > 84.36)
>            Predict: 2.3621425360574837
>          Else (feature 1 > 42.07)
>           If (feature 2 <= 1007.82)
>            Predict: -1.4185266335795177
>           Else (feature 2 > 1007.82)
>            Predict: -5.383717178467172
>         Else (feature 0 > 5.18)
>          If (feature 3 <= 53.32)
>           If (feature 2 <= 1021.17)
>            Predict: 0.6349729680247564
>           Else (feature 2 > 1021.17)
>            Predict: 9.504309080910616
>          Else (feature 3 > 53.32)
>           If (feature 0 <= 25.95)
>            Predict: 0.010243524812335326
>           Else (feature 0 > 25.95)
>            Predict: -0.8173343910336555
>       Tree 27 (weight 0.1):
>         If (feature 2 <= 1028.38)
>          If (feature 1 <= 74.87)
>           If (feature 1 <= 56.57)
>            Predict: 0.28085003688072396
>           Else (feature 1 > 56.57)
>            Predict: -0.378551674966564
>          Else (feature 1 > 74.87)
>           If (feature 0 <= 21.42)
>            Predict: -12.321588273833015
>           Else (feature 0 > 21.42)
>            Predict: 1.8659669412137414
>         Else (feature 2 > 1028.38)
>          If (feature 3 <= 89.83)
>           If (feature 3 <= 66.27)
>            Predict: -8.252928408643971
>           Else (feature 3 > 66.27)
>            Predict: -2.023910717088332
>          Else (feature 3 > 89.83)
>           If (feature 0 <= 8.39)
>            Predict: -11.472893448110653
>           Else (feature 0 > 8.39)
>            Predict: -8.030312146910243
>       Tree 28 (weight 0.1):
>         If (feature 3 <= 85.4)
>          If (feature 0 <= 7.55)
>           If (feature 1 <= 40.05)
>            Predict: 0.3456361310433187
>           Else (feature 1 > 40.05)
>            Predict: 4.958188742864418
>          Else (feature 0 > 7.55)
>           If (feature 0 <= 8.75)
>            Predict: -3.0608059226719657
>           Else (feature 0 > 8.75)
>            Predict: 0.16507864507530287
>         Else (feature 3 > 85.4)
>          If (feature 2 <= 1015.63)
>           If (feature 2 <= 1014.19)
>            Predict: -0.3593841710339432
>           Else (feature 2 > 1014.19)
>            Predict: 3.2531365191458024
>          Else (feature 2 > 1015.63)
>           If (feature 1 <= 40.64)
>            Predict: 1.0007657377910708
>           Else (feature 1 > 40.64)
>            Predict: -2.132339394694771
>       Tree 29 (weight 0.1):
>         If (feature 0 <= 30.56)
>          If (feature 3 <= 55.74)
>           If (feature 1 <= 72.24)
>            Predict: 0.8569729911086951
>           Else (feature 1 > 72.24)
>            Predict: 6.358127096088517
>          Else (feature 3 > 55.74)
>           If (feature 1 <= 41.48)
>            Predict: 0.43148253820326676
>           Else (feature 1 > 41.48)
>            Predict: -0.24352278568573174
>         Else (feature 0 > 30.56)
>          If (feature 2 <= 1014.35)
>           If (feature 1 <= 68.3)
>            Predict: -2.5522103291398683
>           Else (feature 1 > 68.3)
>            Predict: -0.21266182300917044
>          Else (feature 2 > 1014.35)
>           If (feature 1 <= 74.87)
>            Predict: -6.498613011225412
>           Else (feature 1 > 74.87)
>            Predict: 0.9765776955731879
>       Tree 30 (weight 0.1):
>         If (feature 0 <= 17.84)
>          If (feature 1 <= 45.08)
>           If (feature 0 <= 15.33)
>            Predict: -0.14424299831222268
>           Else (feature 0 > 15.33)
>            Predict: 1.8754751416891788
>          Else (feature 1 > 45.08)
>           If (feature 2 <= 1020.77)
>            Predict: -3.097730832691005
>           Else (feature 2 > 1020.77)
>            Predict: -8.90070153022011
>         Else (feature 0 > 17.84)
>          If (feature 0 <= 18.71)
>           If (feature 1 <= 49.02)
>            Predict: 1.2726140970398088
>           Else (feature 1 > 49.02)
>            Predict: 6.649324687634596
>          Else (feature 0 > 18.71)
>           If (feature 1 <= 46.93)
>            Predict: -2.818245204603037
>           Else (feature 1 > 46.93)
>            Predict: 0.23586447368304939
>       Tree 31 (weight 0.1):
>         If (feature 2 <= 1004.52)
>          If (feature 1 <= 59.14)
>           If (feature 1 <= 50.66)
>            Predict: -0.8733348655196066
>           Else (feature 1 > 50.66)
>            Predict: 7.928862441716025
>          Else (feature 1 > 59.14)
>           If (feature 1 <= 70.8)
>            Predict: -3.8112988828197807
>           Else (feature 1 > 70.8)
>            Predict: 0.42812840935226704
>         Else (feature 2 > 1004.52)
>          If (feature 0 <= 17.84)
>           If (feature 1 <= 46.93)
>            Predict: 0.07282772802501089
>           Else (feature 1 > 46.93)
>            Predict: -3.3364389464988706
>          Else (feature 0 > 17.84)
>           If (feature 2 <= 1020.32)
>            Predict: 0.18419167853517965
>           Else (feature 2 > 1020.32)
>            Predict: 6.584432032190064
>       Tree 32 (weight 0.1):
>         If (feature 1 <= 56.57)
>          If (feature 1 <= 49.39)
>           If (feature 0 <= 13.56)
>            Predict: 0.36741135502935035
>           Else (feature 0 > 13.56)
>            Predict: -0.7178818728654812
>          Else (feature 1 > 49.39)
>           If (feature 0 <= 17.84)
>            Predict: -1.7883686826457996
>           Else (feature 0 > 17.84)
>            Predict: 4.519745157967235
>         Else (feature 1 > 56.57)
>          If (feature 0 <= 17.84)
>           If (feature 0 <= 17.5)
>            Predict: -4.182857837547887
>           Else (feature 0 > 17.5)
>            Predict: -7.917768935292194
>          Else (feature 0 > 17.84)
>           If (feature 0 <= 19.61)
>            Predict: 2.6880627533068244
>           Else (feature 0 > 19.61)
>            Predict: -0.2998975340288976
>       Tree 33 (weight 0.1):
>         If (feature 0 <= 11.95)
>          If (feature 0 <= 11.03)
>           If (feature 3 <= 93.63)
>            Predict: 0.7278554646891878
>           Else (feature 3 > 93.63)
>            Predict: -2.2492543009893162
>          Else (feature 0 > 11.03)
>           If (feature 2 <= 1024.3)
>            Predict: -5.536706488618952
>           Else (feature 2 > 1024.3)
>            Predict: 4.479707018501001
>         Else (feature 0 > 11.95)
>          If (feature 0 <= 13.08)
>           If (feature 0 <= 12.5)
>            Predict: 5.173128471411881
>           Else (feature 0 > 12.5)
>            Predict: 2.3834255982190755
>          Else (feature 0 > 13.08)
>           If (feature 0 <= 15.33)
>            Predict: -1.5022006203890645
>           Else (feature 0 > 15.33)
>            Predict: 0.15423852245074754
>       Tree 34 (weight 0.1):
>         If (feature 0 <= 8.75)
>          If (feature 0 <= 7.55)
>           If (feature 3 <= 77.56)
>            Predict: 3.015852739381847
>           Else (feature 3 > 77.56)
>            Predict: -0.06103236076131486
>          Else (feature 0 > 7.55)
>           If (feature 3 <= 62.1)
>            Predict: -13.594573386743992
>           Else (feature 3 > 62.1)
>            Predict: -2.6914920546129273
>         Else (feature 0 > 8.75)
>          If (feature 0 <= 10.03)
>           If (feature 3 <= 95.45)
>            Predict: 3.213047453934116
>           Else (feature 3 > 95.45)
>            Predict: -2.3699077010186502
>          Else (feature 0 > 10.03)
>           If (feature 0 <= 11.95)
>            Predict: -1.841483689919706
>           Else (feature 0 > 11.95)
>            Predict: 0.1034719724734039
>       Tree 35 (weight 0.1):
>         If (feature 1 <= 56.57)
>          If (feature 1 <= 49.02)
>           If (feature 1 <= 44.88)
>            Predict: 0.1854471597033813
>           Else (feature 1 > 44.88)
>            Predict: -1.537157071790549
>          Else (feature 1 > 49.02)
>           If (feature 2 <= 1009.77)
>            Predict: -0.7176011396833722
>           Else (feature 2 > 1009.77)
>            Predict: 3.4414962844541495
>         Else (feature 1 > 56.57)
>          If (feature 1 <= 66.25)
>           If (feature 0 <= 21.92)
>            Predict: 0.6042503983890641
>           Else (feature 0 > 21.92)
>            Predict: -1.6430682984491796
>          Else (feature 1 > 66.25)
>           If (feature 0 <= 23.02)
>            Predict: -3.919778656895867
>           Else (feature 0 > 23.02)
>            Predict: 0.8520833743461524
>       Tree 36 (weight 0.1):
>         If (feature 0 <= 27.6)
>          If (feature 0 <= 23.02)
>           If (feature 0 <= 22.1)
>            Predict: 0.08610814822616036
>           Else (feature 0 > 22.1)
>            Predict: -3.39446668206219
>          Else (feature 0 > 23.02)
>           If (feature 1 <= 66.25)
>            Predict: -0.25067209339950686
>           Else (feature 1 > 66.25)
>            Predict: 2.1536703058787143
>         Else (feature 0 > 27.6)
>          If (feature 3 <= 62.1)
>           If (feature 1 <= 74.87)
>            Predict: -0.3912307208100507
>           Else (feature 1 > 74.87)
>            Predict: 2.6168301411252224
>          Else (feature 3 > 62.1)
>           If (feature 1 <= 71.8)
>            Predict: -0.1075335658351684
>           Else (feature 1 > 71.8)
>            Predict: -3.3756176659678685
>       Tree 37 (weight 0.1):
>         If (feature 0 <= 25.35)
>          If (feature 0 <= 23.02)
>           If (feature 1 <= 64.84)
>            Predict: 0.07789630965601392
>           Else (feature 1 > 64.84)
>            Predict: -2.8928836560033093
>          Else (feature 0 > 23.02)
>           If (feature 1 <= 66.25)
>            Predict: 0.13731068060749954
>           Else (feature 1 > 66.25)
>            Predict: 4.15851454889221
>         Else (feature 0 > 25.35)
>          If (feature 1 <= 43.65)
>           If (feature 0 <= 27.19)
>            Predict: -16.475158304770883
>           Else (feature 0 > 27.19)
>            Predict: -7.947134756554647
>          Else (feature 1 > 43.65)
>           If (feature 3 <= 62.7)
>            Predict: 0.1725950049938879
>           Else (feature 3 > 62.7)
>            Predict: -1.0926147971432427
>       Tree 38 (weight 0.1):
>         If (feature 2 <= 1028.38)
>          If (feature 0 <= 30.56)
>           If (feature 3 <= 47.89)
>            Predict: 1.6647926733523803
>           Else (feature 3 > 47.89)
>            Predict: 0.019004190066623235
>          Else (feature 0 > 30.56)
>           If (feature 2 <= 1014.35)
>            Predict: -0.6192794789083232
>           Else (feature 2 > 1014.35)
>            Predict: -4.385760311827676
>         Else (feature 2 > 1028.38)
>          If (feature 1 <= 39.48)
>           If (feature 0 <= 6.52)
>            Predict: 4.573467616169609
>           Else (feature 0 > 6.52)
>            Predict: -1.362091279334777
>          Else (feature 1 > 39.48)
>           If (feature 0 <= 8.75)
>            Predict: -7.0007999537928605
>           Else (feature 0 > 8.75)
>            Predict: -1.617908469279585
>       Tree 39 (weight 0.1):
>         If (feature 2 <= 1017.42)
>          If (feature 2 <= 1014.19)
>           If (feature 1 <= 43.13)
>            Predict: 1.2098492492388833
>           Else (feature 1 > 43.13)
>            Predict: -0.4345828650352739
>          Else (feature 2 > 1014.19)
>           If (feature 3 <= 96.38)
>            Predict: 1.0830640036331665
>           Else (feature 3 > 96.38)
>            Predict: -6.6054777318343785
>         Else (feature 2 > 1017.42)
>          If (feature 2 <= 1019.23)
>           If (feature 1 <= 57.85)
>            Predict: -0.8212874032064794
>           Else (feature 1 > 57.85)
>            Predict: -2.6667829000634105
>          Else (feature 2 > 1019.23)
>           If (feature 0 <= 17.84)
>            Predict: -0.39094381687835245
>           Else (feature 0 > 17.84)
>            Predict: 3.336117383932137
>       Tree 40 (weight 0.1):
>         If (feature 3 <= 75.23)
>          If (feature 1 <= 40.05)
>           If (feature 1 <= 39.96)
>            Predict: -1.2851367407493581
>           Else (feature 1 > 39.96)
>            Predict: -9.117459296991676
>          Else (feature 1 > 40.05)
>           If (feature 1 <= 40.89)
>            Predict: 4.461974679211411
>           Else (feature 1 > 40.89)
>            Predict: 0.25422282080546216
>         Else (feature 3 > 75.23)
>          If (feature 0 <= 21.42)
>           If (feature 0 <= 17.84)
>            Predict: -0.11457026696795661
>           Else (feature 0 > 17.84)
>            Predict: 0.9995406591682215
>          Else (feature 0 > 21.42)
>           If (feature 0 <= 23.02)
>            Predict: -2.664637163988949
>           Else (feature 0 > 23.02)
>            Predict: -0.5023743568762508
>       Tree 41 (weight 0.1):
>         If (feature 2 <= 1001.9)
>          If (feature 1 <= 39.13)
>           If (feature 3 <= 79.95)
>            Predict: 9.0188365708008
>           Else (feature 3 > 79.95)
>            Predict: 2.9702965803786205
>          Else (feature 1 > 39.13)
>           If (feature 3 <= 63.68)
>            Predict: -4.052067945951171
>           Else (feature 3 > 63.68)
>            Predict: -1.0796516186664176
>         Else (feature 2 > 1001.9)
>          If (feature 0 <= 15.33)
>           If (feature 0 <= 14.38)
>            Predict: 0.15316006561614587
>           Else (feature 0 > 14.38)
>            Predict: -3.487291240038168
>          Else (feature 0 > 15.33)
>           If (feature 1 <= 43.13)
>            Predict: 2.5605988792505605
>           Else (feature 1 > 43.13)
>            Predict: 0.03166127813460667
>       Tree 42 (weight 0.1):
>         If (feature 0 <= 11.95)
>          If (feature 0 <= 11.42)
>           If (feature 1 <= 38.25)
>            Predict: -2.0532785635493065
>           Else (feature 1 > 38.25)
>            Predict: 0.4665697970110133
>          Else (feature 0 > 11.42)
>           If (feature 1 <= 44.2)
>            Predict: -4.178641719198364
>           Else (feature 1 > 44.2)
>            Predict: -9.84024023297988
>         Else (feature 0 > 11.95)
>          If (feature 0 <= 13.08)
>           If (feature 1 <= 40.89)
>            Predict: 4.383821312183712
>           Else (feature 1 > 40.89)
>            Predict: 2.000819554066434
>          Else (feature 0 > 13.08)
>           If (feature 0 <= 15.33)
>            Predict: -1.0813581518144955
>           Else (feature 0 > 15.33)
>            Predict: 0.11492139312962121
>       Tree 43 (weight 0.1):
>         If (feature 0 <= 8.75)
>          If (feature 0 <= 7.97)
>           If (feature 3 <= 86.54)
>            Predict: 0.983392336251922
>           Else (feature 3 > 86.54)
>            Predict: -0.8690504742953818
>          Else (feature 0 > 7.97)
>           If (feature 3 <= 62.1)
>            Predict: -20.310342278835464
>           Else (feature 3 > 62.1)
>            Predict: -2.975869736741497
>         Else (feature 0 > 8.75)
>          If (feature 0 <= 9.42)
>           If (feature 2 <= 1015.45)
>            Predict: 5.74314556767472
>           Else (feature 2 > 1015.45)
>            Predict: 2.1033141679659995
>          Else (feature 0 > 9.42)
>           If (feature 1 <= 40.89)
>            Predict: 0.6933339562649613
>           Else (feature 1 > 40.89)
>            Predict: -0.10718368674776323
>       Tree 44 (weight 0.1):
>         If (feature 1 <= 74.87)
>          If (feature 1 <= 71.43)
>           If (feature 1 <= 68.3)
>            Predict: -0.0751396787352361
>           Else (feature 1 > 68.3)
>            Predict: 1.0387569941322914
>          Else (feature 1 > 71.43)
>           If (feature 1 <= 72.86)
>            Predict: -2.5461711201599986
>           Else (feature 1 > 72.86)
>            Predict: -0.0018936704520639966
>         Else (feature 1 > 74.87)
>          If (feature 1 <= 77.3)
>           If (feature 3 <= 73.33)
>            Predict: 3.4362919081871732
>           Else (feature 3 > 73.33)
>            Predict: 0.022595797531833054
>          Else (feature 1 > 77.3)
>           If (feature 2 <= 1012.39)
>            Predict: -2.0026738842740444
>           Else (feature 2 > 1012.39)
>            Predict: 1.7553499174736846
>       Tree 45 (weight 0.1):
>         If (feature 2 <= 1005.35)
>          If (feature 1 <= 72.24)
>           If (feature 1 <= 59.14)
>            Predict: 0.030127466104975898
>           Else (feature 1 > 59.14)
>            Predict: -2.2341894812350676
>          Else (feature 1 > 72.24)
>           If (feature 3 <= 60.09)
>            Predict: 4.41863108135717
>           Else (feature 3 > 60.09)
>            Predict: -0.11040726869235623
>         Else (feature 2 > 1005.35)
>          If (feature 0 <= 31.8)
>           If (feature 1 <= 66.25)
>            Predict: -0.06640264597455495
>           Else (feature 1 > 66.25)
>            Predict: 0.6711276381424462
>          Else (feature 0 > 31.8)
>           If (feature 1 <= 62.44)
>            Predict: 18.071299971628946
>           Else (feature 1 > 62.44)
>            Predict: -1.613111097205577
>       Tree 46 (weight 0.1):
>         If (feature 0 <= 25.95)
>          If (feature 0 <= 23.02)
>           If (feature 0 <= 22.6)
>            Predict: 0.0037802976144726266
>           Else (feature 0 > 22.6)
>            Predict: -3.2702083989998565
>          Else (feature 0 > 23.02)
>           If (feature 1 <= 47.83)
>            Predict: 7.351532379664369
>           Else (feature 1 > 47.83)
>            Predict: 0.6617643737173495
>         Else (feature 0 > 25.95)
>          If (feature 3 <= 62.1)
>           If (feature 0 <= 29.89)
>            Predict: 0.7522949567047181
>           Else (feature 0 > 29.89)
>            Predict: -0.5659530686126862
>          Else (feature 3 > 62.1)
>           If (feature 1 <= 43.41)
>            Predict: -9.179671352130104
>           Else (feature 1 > 43.41)
>            Predict: -0.9646184420761758
>       Tree 47 (weight 0.1):
>         If (feature 0 <= 5.18)
>          If (feature 1 <= 38.62)
>           If (feature 3 <= 77.17)
>            Predict: -4.215696425771664
>           Else (feature 3 > 77.17)
>            Predict: 5.655069692148392
>          Else (feature 1 > 38.62)
>           If (feature 1 <= 39.13)
>            Predict: -12.269101167501105
>           Else (feature 1 > 39.13)
>            Predict: 1.081763483601667
>         Else (feature 0 > 5.18)
>          If (feature 0 <= 8.75)
>           If (feature 0 <= 7.97)
>            Predict: -0.19756946285599916
>           Else (feature 0 > 7.97)
>            Predict: -2.7184931590940438
>          Else (feature 0 > 8.75)
>           If (feature 0 <= 9.42)
>            Predict: 2.558566383813981
>           Else (feature 0 > 9.42)
>            Predict: -0.006722635545763743
>       Tree 48 (weight 0.1):
>         If (feature 2 <= 1028.38)
>          If (feature 2 <= 1010.89)
>           If (feature 1 <= 66.93)
>            Predict: -0.7473456438858288
>           Else (feature 1 > 66.93)
>            Predict: 0.34762458916260297
>          Else (feature 2 > 1010.89)
>           If (feature 1 <= 58.86)
>            Predict: 0.4001213596367478
>           Else (feature 1 > 58.86)
>            Predict: -0.33373941983121597
>         Else (feature 2 > 1028.38)
>          If (feature 1 <= 42.85)
>           If (feature 1 <= 39.48)
>            Predict: 2.1904388134214514
>           Else (feature 1 > 39.48)
>            Predict: -3.2474441160938956
>          Else (feature 1 > 42.85)
>           If (feature 3 <= 71.55)
>            Predict: -1.061140549595708
>           Else (feature 3 > 71.55)
>            Predict: 6.934556118848832
>       Tree 49 (weight 0.1):
>         If (feature 0 <= 11.95)
>          If (feature 0 <= 10.74)
>           If (feature 0 <= 8.75)
>            Predict: -0.48190999213172564
>           Else (feature 0 > 8.75)
>            Predict: 1.0350335598803566
>          Else (feature 0 > 10.74)
>           If (feature 2 <= 1024.3)
>            Predict: -3.057989388513731
>           Else (feature 2 > 1024.3)
>            Predict: 2.162024696272738
>         Else (feature 0 > 11.95)
>          If (feature 0 <= 12.5)
>           If (feature 3 <= 86.91)
>            Predict: 4.627051067913808
>           Else (feature 3 > 86.91)
>            Predict: 0.9386052167341327
>          Else (feature 0 > 12.5)
>           If (feature 1 <= 37.8)
>            Predict: 4.0889321278523685
>           Else (feature 1 > 37.8)
>            Predict: -0.02245818963891235
>       Tree 50 (weight 0.1):
>         If (feature 2 <= 1017.42)
>          If (feature 2 <= 1014.19)
>           If (feature 1 <= 43.13)
>            Predict: 0.9320375696962719
>           Else (feature 1 > 43.13)
>            Predict: -0.31844348507047093
>          Else (feature 2 > 1014.19)
>           If (feature 1 <= 42.42)
>            Predict: -0.5988031510673222
>           Else (feature 1 > 42.42)
>            Predict: 1.3187243855742212
>         Else (feature 2 > 1017.42)
>          If (feature 2 <= 1019.23)
>           If (feature 1 <= 44.2)
>            Predict: -2.0646082455368195
>           Else (feature 1 > 44.2)
>            Predict: -0.4969601265683861
>          Else (feature 2 > 1019.23)
>           If (feature 0 <= 17.84)
>            Predict: -0.2870181057370213
>           Else (feature 0 > 17.84)
>            Predict: 2.6148230736448608
>       Tree 51 (weight 0.1):
>         If (feature 1 <= 38.62)
>          If (feature 0 <= 18.4)
>           If (feature 0 <= 5.18)
>            Predict: 3.850885339006515
>           Else (feature 0 > 5.18)
>            Predict: -0.940687510645146
>          Else (feature 0 > 18.4)
>           If (feature 0 <= 18.98)
>            Predict: -10.80330040562501
>           Else (feature 0 > 18.98)
>            Predict: -18.03404880535599
>         Else (feature 1 > 38.62)
>          If (feature 2 <= 1026.23)
>           If (feature 0 <= 13.56)
>            Predict: 0.5295719576334972
>           Else (feature 0 > 13.56)
>            Predict: -0.052812717813551166
>          Else (feature 2 > 1026.23)
>           If (feature 1 <= 40.22)
>            Predict: -4.371246083031292
>           Else (feature 1 > 40.22)
>            Predict: -1.3541229527292618
>       Tree 52 (weight 0.1):
>         If (feature 1 <= 66.25)
>          If (feature 1 <= 64.84)
>           If (feature 3 <= 41.26)
>            Predict: 3.045631536773922
>           Else (feature 3 > 41.26)
>            Predict: -0.0337837562463145
>          Else (feature 1 > 64.84)
>           If (feature 1 <= 65.27)
>            Predict: -5.921444872611693
>           Else (feature 1 > 65.27)
>            Predict: -0.8270282146869598
>         Else (feature 1 > 66.25)
>          If (feature 0 <= 23.02)
>           If (feature 0 <= 19.83)
>            Predict: 1.5405239234096135
>           Else (feature 0 > 19.83)
>            Predict: -3.1288830506195398
>          Else (feature 0 > 23.02)
>           If (feature 0 <= 25.35)
>            Predict: 3.2672442442602656
>           Else (feature 0 > 25.35)
>            Predict: -0.007592990267182966
>       Tree 53 (weight 0.1):
>         If (feature 0 <= 17.84)
>          If (feature 1 <= 46.93)
>           If (feature 0 <= 17.2)
>            Predict: 0.1228349542857993
>           Else (feature 0 > 17.2)
>            Predict: -2.392588492043597
>          Else (feature 1 > 46.93)
>           If (feature 2 <= 1020.77)
>            Predict: -1.8240349072310669
>           Else (feature 2 > 1020.77)
>            Predict: -6.523289398433308
>         Else (feature 0 > 17.84)
>          If (feature 0 <= 18.4)
>           If (feature 1 <= 47.83)
>            Predict: 0.5318997435908227
>           Else (feature 1 > 47.83)
>            Predict: 4.907584149653537
>          Else (feature 0 > 18.4)
>           If (feature 1 <= 46.93)
>            Predict: -2.110133253015907
>           Else (feature 1 > 46.93)
>            Predict: 0.20708863671712482
>       Tree 54 (weight 0.1):
>         If (feature 3 <= 76.79)
>          If (feature 1 <= 40.05)
>           If (feature 1 <= 39.96)
>            Predict: -0.7416033424896232
>           Else (feature 1 > 39.96)
>            Predict: -6.880323474190146
>          Else (feature 1 > 40.05)
>           If (feature 1 <= 40.89)
>            Predict: 2.887497917363201
>           Else (feature 1 > 40.89)
>            Predict: 0.17777582956662522
>         Else (feature 3 > 76.79)
>          If (feature 0 <= 19.61)
>           If (feature 0 <= 17.84)
>            Predict: -0.09172434324104897
>           Else (feature 0 > 17.84)
>            Predict: 1.9482862934683598
>          Else (feature 0 > 19.61)
>           If (feature 2 <= 1010.6)
>            Predict: -0.15262790703036064
>           Else (feature 2 > 1010.6)
>            Predict: -1.7280878096087295
>       Tree 55 (weight 0.1):
>         If (feature 0 <= 24.79)
>          If (feature 0 <= 23.02)
>           If (feature 1 <= 66.93)
>            Predict: 0.02682576814507517
>           Else (feature 1 > 66.93)
>            Predict: -2.323863726560255
>          Else (feature 0 > 23.02)
>           If (feature 1 <= 47.83)
>            Predict: 6.909290893058579
>           Else (feature 1 > 47.83)
>            Predict: 0.9944889736997976
>         Else (feature 0 > 24.79)
>          If (feature 3 <= 65.24)
>           If (feature 0 <= 28.5)
>            Predict: 0.8432916332803679
>           Else (feature 0 > 28.5)
>            Predict: -0.3680864130080106
>          Else (feature 3 > 65.24)
>           If (feature 1 <= 66.51)
>            Predict: -2.1147474860288
>           Else (feature 1 > 66.51)
>            Predict: -0.3834883036951788
>       Tree 56 (weight 0.1):
>         If (feature 0 <= 15.33)
>          If (feature 0 <= 14.38)
>           If (feature 0 <= 11.95)
>            Predict: -0.3290262091199092
>           Else (feature 0 > 11.95)
>            Predict: 0.8543511625463592
>          Else (feature 0 > 14.38)
>           If (feature 2 <= 1016.21)
>            Predict: -0.7208476709379852
>           Else (feature 2 > 1016.21)
>            Predict: -4.40928839539672
>         Else (feature 0 > 15.33)
>          If (feature 0 <= 16.22)
>           If (feature 2 <= 1013.19)
>            Predict: 4.554268903891635
>           Else (feature 2 > 1013.19)
>            Predict: 1.538781048856137
>          Else (feature 0 > 16.22)
>           If (feature 1 <= 46.93)
>            Predict: -1.1488437756174756
>           Else (feature 1 > 46.93)
>            Predict: 0.1634274865006602
>       Tree 57 (weight 0.1):
>         If (feature 2 <= 1007.46)
>          If (feature 1 <= 73.67)
>           If (feature 1 <= 71.43)
>            Predict: -0.28457458674767294
>           Else (feature 1 > 71.43)
>            Predict: -2.556284198496123
>          Else (feature 1 > 73.67)
>           If (feature 3 <= 60.81)
>            Predict: 4.31886476056719
>           Else (feature 3 > 60.81)
>            Predict: 0.3197495651743129
>         Else (feature 2 > 1007.46)
>          If (feature 0 <= 17.84)
>           If (feature 1 <= 46.93)
>            Predict: 0.04575453109929229
>           Else (feature 1 > 46.93)
>            Predict: -2.141138284310683
>          Else (feature 0 > 17.84)
>           If (feature 1 <= 56.57)
>            Predict: 1.3439965861050847
>           Else (feature 1 > 56.57)
>            Predict: -0.02904919315788331
>       Tree 58 (weight 0.1):
>         If (feature 0 <= 31.8)
>          If (feature 1 <= 66.25)
>           If (feature 1 <= 64.84)
>            Predict: -0.006836636445003446
>           Else (feature 1 > 64.84)
>            Predict: -2.0890363043188134
>          Else (feature 1 > 66.25)
>           If (feature 1 <= 69.05)
>            Predict: 1.8596834938858298
>           Else (feature 1 > 69.05)
>            Predict: -0.2637818907162569
>         Else (feature 0 > 31.8)
>          If (feature 1 <= 69.34)
>           If (feature 2 <= 1009.63)
>            Predict: -4.53407923927751
>           Else (feature 2 > 1009.63)
>            Predict: 1.2479530412848983
>          Else (feature 1 > 69.34)
>           If (feature 1 <= 69.88)
>            Predict: 5.672382101944148
>           Else (feature 1 > 69.88)
>            Predict: -0.7728960613425813
>       Tree 59 (weight 0.1):
>         If (feature 2 <= 1010.89)
>          If (feature 1 <= 68.3)
>           If (feature 1 <= 43.41)
>            Predict: 0.423961936091299
>           Else (feature 1 > 43.41)
>            Predict: -1.0411314850417004
>          Else (feature 1 > 68.3)
>           If (feature 1 <= 68.67)
>            Predict: 7.130757445704555
>           Else (feature 1 > 68.67)
>            Predict: 0.1160942217864609
>         Else (feature 2 > 1010.89)
>          If (feature 3 <= 93.63)
>           If (feature 1 <= 58.86)
>            Predict: 0.41091291246834866
>           Else (feature 1 > 58.86)
>            Predict: -0.2764637915143923
>          Else (feature 3 > 93.63)
>           If (feature 1 <= 41.74)
>            Predict: -3.564757715833512
>           Else (feature 1 > 41.74)
>            Predict: 1.1644353912440248
>       Tree 60 (weight 0.1):
>         If (feature 1 <= 48.6)
>          If (feature 1 <= 44.88)
>           If (feature 2 <= 1016.57)
>            Predict: 0.4410572983039277
>           Else (feature 2 > 1016.57)
>            Predict: -0.44414793681792664
>          Else (feature 1 > 44.88)
>           If (feature 2 <= 1014.35)
>            Predict: -3.0626378082153085
>           Else (feature 2 > 1014.35)
>            Predict: 2.0328536525605063
>         Else (feature 1 > 48.6)
>          If (feature 1 <= 52.05)
>           If (feature 2 <= 1009.9)
>            Predict: 0.24004783900051171
>           Else (feature 2 > 1009.9)
>            Predict: 3.1645061792332916
>          Else (feature 1 > 52.05)
>           If (feature 0 <= 17.84)
>            Predict: -1.95074879327582
>           Else (feature 0 > 17.84)
>            Predict: 0.021106826304965107
>       Tree 61 (weight 0.1):
>         If (feature 1 <= 74.87)
>          If (feature 1 <= 71.43)
>           If (feature 1 <= 68.3)
>            Predict: -0.06241270845694165
>           Else (feature 1 > 68.3)
>            Predict: 0.8051320337219834
>          Else (feature 1 > 71.43)
>           If (feature 0 <= 24.57)
>            Predict: 1.648459594873699
>           Else (feature 0 > 24.57)
>            Predict: -1.2314608832462137
>         Else (feature 1 > 74.87)
>          If (feature 1 <= 77.3)
>           If (feature 0 <= 21.42)
>            Predict: -7.482222216002697
>           Else (feature 0 > 21.42)
>            Predict: 1.8228183337802573
>          Else (feature 1 > 77.3)
>           If (feature 2 <= 1012.39)
>            Predict: -1.4326641812285505
>           Else (feature 2 > 1012.39)
>            Predict: 1.7079353624089986
>       Tree 62 (weight 0.1):
>         If (feature 0 <= 5.18)
>          If (feature 1 <= 42.07)
>           If (feature 3 <= 96.38)
>            Predict: 1.4583097259406885
>           Else (feature 3 > 96.38)
>            Predict: 7.4053761713858615
>          Else (feature 1 > 42.07)
>           If (feature 2 <= 1008.19)
>            Predict: 0.311290850436914
>           Else (feature 2 > 1008.19)
>            Predict: -5.145119802972147
>         Else (feature 0 > 5.18)
>          If (feature 1 <= 38.62)
>           If (feature 0 <= 18.4)
>            Predict: -0.7259884411546618
>           Else (feature 0 > 18.4)
>            Predict: -12.427884135864616
>          Else (feature 1 > 38.62)
>           If (feature 1 <= 39.48)
>            Predict: 1.131291291234381
>           Else (feature 1 > 39.48)
>            Predict: -0.007004055574359982
>       Tree 63 (weight 0.1):
>         If (feature 2 <= 1004.52)
>          If (feature 1 <= 70.8)
>           If (feature 1 <= 69.05)
>            Predict: -0.45566718124370104
>           Else (feature 1 > 69.05)
>            Predict: -3.3633539333883373
>          Else (feature 1 > 70.8)
>           If (feature 3 <= 70.63)
>            Predict: 1.7061073842258219
>           Else (feature 3 > 70.63)
>            Predict: -0.35469491259927843
>         Else (feature 2 > 1004.52)
>          If (feature 0 <= 15.33)
>           If (feature 0 <= 14.13)
>            Predict: 0.13165022513417465
>           Else (feature 0 > 14.13)
>            Predict: -1.8886218519887454
>          Else (feature 0 > 15.33)
>           If (feature 1 <= 43.13)
>            Predict: 2.0897911694212086
>           Else (feature 1 > 43.13)
>            Predict: 0.023571622513158218
>       Tree 64 (weight 0.1):
>         If (feature 1 <= 41.92)
>          If (feature 1 <= 41.58)
>           If (feature 2 <= 1015.45)
>            Predict: 0.6420804366913081
>           Else (feature 2 > 1015.45)
>            Predict: -0.3393001000428116
>          Else (feature 1 > 41.58)
>           If (feature 3 <= 91.38)
>            Predict: -2.959889489145066
>           Else (feature 3 > 91.38)
>            Predict: -14.822621379271645
>         Else (feature 1 > 41.92)
>          If (feature 1 <= 43.13)
>           If (feature 0 <= 15.33)
>            Predict: 0.5584851317693598
>           Else (feature 0 > 15.33)
>            Predict: 5.35806974907062
>          Else (feature 1 > 43.13)
>           If (feature 1 <= 43.65)
>            Predict: -2.5734171913252673
>           Else (feature 1 > 43.65)
>            Predict: 0.06206747847844893
>       Tree 65 (weight 0.1):
>         If (feature 2 <= 1010.89)
>          If (feature 1 <= 66.93)
>           If (feature 0 <= 20.6)
>            Predict: -0.0679333275254979
>           Else (feature 0 > 20.6)
>            Predict: -1.053808811058633
>          Else (feature 1 > 66.93)
>           If (feature 1 <= 67.32)
>            Predict: 7.372080266725638
>           Else (feature 1 > 67.32)
>            Predict: 0.09996335027123535
>         Else (feature 2 > 1010.89)
>          If (feature 3 <= 75.61)
>           If (feature 1 <= 40.05)
>            Predict: -0.9831581524231143
>           Else (feature 1 > 40.05)
>            Predict: 0.5486160789249349
>          Else (feature 3 > 75.61)
>           If (feature 1 <= 58.86)
>            Predict: 0.19399224442246701
>           Else (feature 1 > 58.86)
>            Predict: -1.5652059699408227
>       Tree 66 (weight 0.1):
>         If (feature 0 <= 28.75)
>          If (feature 1 <= 73.18)
>           If (feature 1 <= 71.43)
>            Predict: 0.05143978594106816
>           Else (feature 1 > 71.43)
>            Predict: -1.436513600322334
>          Else (feature 1 > 73.18)
>           If (feature 3 <= 73.33)
>            Predict: 4.1459864582084975
>           Else (feature 3 > 73.33)
>            Predict: 0.34965185037807356
>         Else (feature 0 > 28.75)
>          If (feature 2 <= 1014.54)
>           If (feature 2 <= 1013.43)
>            Predict: -0.4008005884834272
>           Else (feature 2 > 1013.43)
>            Predict: 3.683818693727259
>          Else (feature 2 > 1014.54)
>           If (feature 1 <= 67.83)
>            Predict: -0.82614879352537
>           Else (feature 1 > 67.83)
>            Predict: -4.535981326886069
>       Tree 67 (weight 0.1):
>         If (feature 1 <= 47.83)
>          If (feature 0 <= 23.02)
>           If (feature 0 <= 18.71)
>            Predict: -0.0010074123242523121
>           Else (feature 0 > 18.71)
>            Predict: -3.2926535011699234
>          Else (feature 0 > 23.02)
>           If (feature 2 <= 1012.39)
>            Predict: 1.3034696914565052
>           Else (feature 2 > 1012.39)
>            Predict: 11.235282784300427
>         Else (feature 1 > 47.83)
>          If (feature 1 <= 56.57)
>           If (feature 0 <= 17.84)
>            Predict: -1.039931035628621
>           Else (feature 0 > 17.84)
>            Predict: 1.9905896386111916
>          Else (feature 1 > 56.57)
>           If (feature 1 <= 57.19)
>            Predict: -2.3357601760278204
>           Else (feature 1 > 57.19)
>            Predict: -0.0355403353056693
>       Tree 68 (weight 0.1):
>         If (feature 0 <= 24.79)
>          If (feature 3 <= 41.26)
>           If (feature 1 <= 45.87)
>            Predict: 2.4904273637383265
>           Else (feature 1 > 45.87)
>            Predict: 13.013875696314063
>          Else (feature 3 > 41.26)
>           If (feature 1 <= 49.02)
>            Predict: -0.18642415027276396
>           Else (feature 1 > 49.02)
>            Predict: 0.47121076166963227
>         Else (feature 0 > 24.79)
>          If (feature 1 <= 65.27)
>           If (feature 1 <= 64.84)
>            Predict: -0.5...

### Conclusion

Wow! So our best model is in fact our Gradient Boosted Decision tree model which uses an ensemble of 120 Trees with a depth of 3 to construct a better model than the single decision tree.

Persisting Statistical Machine Learning Models
----------------------------------------------

<p class="htmlSandbox"><iframe 
 src="https://databricks.com/blog/2016/05/31/apache-spark-2-0-preview-machine-learning-model-persistence.html"
 width="95%" height="500"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe></p>

Let's save our best model so we can load it without having to rerun the validation and training again.

``` scala
gbtModel.bestModel.asInstanceOf[PipelineModel].stages.last.asInstanceOf[GBTRegressionModel]
        .write.overwrite().save("dbfs:///databricks/driver/MyTrainedGbtModel")
```

``` scala
val sameModel = GBTRegressionModel.load("dbfs:///databricks/driver/MyTrainedGbtModel/")
```

>     sameModel: org.apache.spark.ml.regression.GBTRegressionModel = GBTRegressionModel (uid=gbtr_9c5ab45fe584) with 120 trees

``` scala
// making sure we have the same model loaded from the file
sameModel.toDebugString
```

>     res53: String =
>     "GBTRegressionModel (uid=gbtr_9c5ab45fe584) with 120 trees
>       Tree 0 (weight 1.0):
>         If (feature 0 <= 17.84)
>          If (feature 0 <= 11.95)
>           If (feature 0 <= 8.75)
>            Predict: 483.5412151067323
>           Else (feature 0 > 8.75)
>            Predict: 475.6305502392345
>          Else (feature 0 > 11.95)
>           If (feature 0 <= 15.33)
>            Predict: 467.63141917293234
>           Else (feature 0 > 15.33)
>            Predict: 460.74754125412574
>         Else (feature 0 > 17.84)
>          If (feature 0 <= 23.02)
>           If (feature 1 <= 47.83)
>            Predict: 457.1077966101695
>           Else (feature 1 > 47.83)
>            Predict: 448.74750213858016
>          Else (feature 0 > 23.02)
>           If (feature 1 <= 66.25)
>            Predict: 442.88544855967086
>           Else (feature 1 > 66.25)
>            Predict: 434.7293710691822
>       Tree 1 (weight 0.1):
>         If (feature 2 <= 1009.9)
>          If (feature 1 <= 43.13)
>           If (feature 0 <= 15.33)
>            Predict: -0.31094207231231485
>           Else (feature 0 > 15.33)
>            Predict: 4.302958436537365
>          Else (feature 1 > 43.13)
>           If (feature 0 <= 23.02)
>            Predict: -8.38392506141353
>           Else (feature 0 > 23.02)
>            Predict: -2.399960976520273
>         Else (feature 2 > 1009.9)
>          If (feature 3 <= 86.21)
>           If (feature 0 <= 26.35)
>            Predict: 2.888623754019027
>           Else (feature 0 > 26.35)
>            Predict: -1.1489483044194229
>          Else (feature 3 > 86.21)
>           If (feature 1 <= 39.72)
>            Predict: 3.876324424163314
>           Else (feature 1 > 39.72)
>            Predict: -3.058952828112949
>       Tree 2 (weight 0.1):
>         If (feature 2 <= 1009.63)
>          If (feature 1 <= 43.13)
>           If (feature 0 <= 11.95)
>            Predict: -1.4091086031845625
>           Else (feature 0 > 11.95)
>            Predict: 2.6329466235800942
>          Else (feature 1 > 43.13)
>           If (feature 0 <= 23.02)
>            Predict: -6.795414480322956
>           Else (feature 0 > 23.02)
>            Predict: -2.166560698742912
>         Else (feature 2 > 1009.63)
>          If (feature 3 <= 80.44)
>           If (feature 0 <= 26.98)
>            Predict: 2.878622882275939
>           Else (feature 0 > 26.98)
>            Predict: -1.146426969990865
>          Else (feature 3 > 80.44)
>           If (feature 3 <= 94.55)
>            Predict: -0.35885921725905906
>           Else (feature 3 > 94.55)
>            Predict: -5.75364586186002
>       Tree 3 (weight 0.1):
>         If (feature 0 <= 27.6)
>          If (feature 3 <= 70.2)
>           If (feature 1 <= 40.05)
>            Predict: -0.9480831286616939
>           Else (feature 1 > 40.05)
>            Predict: 3.660397090904016
>          Else (feature 3 > 70.2)
>           If (feature 1 <= 40.64)
>            Predict: 2.1539405832035627
>           Else (feature 1 > 40.64)
>            Predict: -1.2281619807661366
>         Else (feature 0 > 27.6)
>          If (feature 1 <= 65.27)
>           If (feature 2 <= 1005.99)
>            Predict: -15.33433697033558
>           Else (feature 2 > 1005.99)
>            Predict: -5.866095468145647
>          Else (feature 1 > 65.27)
>           If (feature 2 <= 1008.75)
>            Predict: -4.03431044067007
>           Else (feature 2 > 1008.75)
>            Predict: -0.23440867445577207
>       Tree 4 (weight 0.1):
>         If (feature 0 <= 26.35)
>          If (feature 0 <= 23.02)
>           If (feature 1 <= 68.67)
>            Predict: 0.12035773384797814
>           Else (feature 1 > 68.67)
>            Predict: -13.928523073642005
>          Else (feature 0 > 23.02)
>           If (feature 0 <= 24.57)
>            Predict: 5.5622340839882165
>           Else (feature 0 > 24.57)
>            Predict: 1.6938172370244715
>         Else (feature 0 > 26.35)
>          If (feature 1 <= 66.25)
>           If (feature 2 <= 1008.4)
>            Predict: -9.009916879825393
>           Else (feature 2 > 1008.4)
>            Predict: -3.059736394918022
>          Else (feature 1 > 66.25)
>           If (feature 0 <= 30.2)
>            Predict: 0.14704705100738577
>           Else (feature 0 > 30.2)
>            Predict: -3.2914123948921006
>       Tree 5 (weight 0.1):
>         If (feature 2 <= 1010.41)
>          If (feature 1 <= 43.41)
>           If (feature 3 <= 99.27)
>            Predict: 1.2994444710077233
>           Else (feature 3 > 99.27)
>            Predict: -6.649548317319231
>          Else (feature 1 > 43.41)
>           If (feature 0 <= 23.02)
>            Predict: -4.9119452777748
>           Else (feature 0 > 23.02)
>            Predict: -0.9514185089440673
>         Else (feature 2 > 1010.41)
>          If (feature 3 <= 89.83)
>           If (feature 0 <= 31.26)
>            Predict: 1.2914123584761403
>           Else (feature 0 > 31.26)
>            Predict: -5.115001417285994
>          Else (feature 3 > 89.83)
>           If (feature 1 <= 40.64)
>            Predict: 1.5160976219176363
>           Else (feature 1 > 40.64)
>            Predict: -4.202813699523934
>       Tree 6 (weight 0.1):
>         If (feature 2 <= 1007.27)
>          If (feature 0 <= 27.94)
>           If (feature 3 <= 71.09)
>            Predict: 1.616448005210527
>           Else (feature 3 > 71.09)
>            Predict: -2.1313527108274157
>          Else (feature 0 > 27.94)
>           If (feature 1 <= 68.3)
>            Predict: -8.579840063013142
>           Else (feature 1 > 68.3)
>            Predict: -1.915909819494233
>         Else (feature 2 > 1007.27)
>          If (feature 3 <= 95.45)
>           If (feature 0 <= 6.52)
>            Predict: 4.973465595410054
>           Else (feature 0 > 6.52)
>            Predict: 0.3837975458985242
>          Else (feature 3 > 95.45)
>           If (feature 2 <= 1013.43)
>            Predict: -0.8175453481344352
>           Else (feature 2 > 1013.43)
>            Predict: -7.264843604639278
>       Tree 7 (weight 0.1):
>         If (feature 0 <= 26.35)
>          If (feature 3 <= 71.09)
>           If (feature 1 <= 67.83)
>            Predict: 1.9620965187817083
>           Else (feature 1 > 67.83)
>            Predict: 7.953863660960779
>          Else (feature 3 > 71.09)
>           If (feature 1 <= 40.89)
>            Predict: 1.2020440154192213
>           Else (feature 1 > 40.89)
>            Predict: -0.9989659748111419
>         Else (feature 0 > 26.35)
>          If (feature 1 <= 66.25)
>           If (feature 2 <= 1008.4)
>            Predict: -6.230272922553423
>           Else (feature 2 > 1008.4)
>            Predict: -2.654681371247991
>          Else (feature 1 > 66.25)
>           If (feature 2 <= 1004.52)
>            Predict: -3.9527797601131853
>           Else (feature 2 > 1004.52)
>            Predict: -0.21770148036273387
>       Tree 8 (weight 0.1):
>         If (feature 0 <= 29.56)
>          If (feature 3 <= 63.16)
>           If (feature 1 <= 72.24)
>            Predict: 1.9612116105231265
>           Else (feature 1 > 72.24)
>            Predict: 8.756949826030025
>          Else (feature 3 > 63.16)
>           If (feature 0 <= 5.95)
>            Predict: 4.445363585074405
>           Else (feature 0 > 5.95)
>            Predict: -0.4097996897633835
>         Else (feature 0 > 29.56)
>          If (feature 1 <= 68.3)
>           If (feature 2 <= 1009.9)
>            Predict: -7.882200867406393
>           Else (feature 2 > 1009.9)
>            Predict: -1.7273221348184091
>          Else (feature 1 > 68.3)
>           If (feature 2 <= 1013.77)
>            Predict: -0.7219749804525829
>           Else (feature 2 > 1013.77)
>            Predict: -6.492100849806538
>       Tree 9 (weight 0.1):
>         If (feature 3 <= 89.83)
>          If (feature 0 <= 25.72)
>           If (feature 0 <= 23.02)
>            Predict: 0.15450088997272685
>           Else (feature 0 > 23.02)
>            Predict: 3.010254802875794
>          Else (feature 0 > 25.72)
>           If (feature 1 <= 66.25)
>            Predict: -2.5821765284417615
>           Else (feature 1 > 66.25)
>            Predict: -0.3935112713804148
>         Else (feature 3 > 89.83)
>          If (feature 2 <= 1019.52)
>           If (feature 0 <= 7.08)
>            Predict: 3.264389020443774
>           Else (feature 0 > 7.08)
>            Predict: -1.6246048211383168
>          Else (feature 2 > 1019.52)
>           If (feature 0 <= 8.75)
>            Predict: -8.005340799169343
>           Else (feature 0 > 8.75)
>            Predict: -2.9832409167030063
>       Tree 10 (weight 0.1):
>         If (feature 1 <= 56.57)
>          If (feature 0 <= 17.84)
>           If (feature 1 <= 45.87)
>            Predict: 0.26309432916452813
>           Else (feature 1 > 45.87)
>            Predict: -5.716473785544373
>          Else (feature 0 > 17.84)
>           If (feature 2 <= 1012.56)
>            Predict: -0.15863259341493433
>           Else (feature 2 > 1012.56)
>            Predict: 7.899625065937478
>         Else (feature 1 > 56.57)
>          If (feature 0 <= 17.84)
>           If (feature 3 <= 67.72)
>            Predict: -27.101084325134025
>           Else (feature 3 > 67.72)
>            Predict: -12.755339130015875
>          Else (feature 0 > 17.84)
>           If (feature 0 <= 20.6)
>            Predict: 3.8741798886113408
>           Else (feature 0 > 20.6)
>            Predict: -0.8179571837367839
>       Tree 11 (weight 0.1):
>         If (feature 2 <= 1004.52)
>          If (feature 1 <= 74.22)
>           If (feature 1 <= 59.14)
>            Predict: -0.6678644068375302
>           Else (feature 1 > 59.14)
>            Predict: -5.0251736913870495
>          Else (feature 1 > 74.22)
>           If (feature 2 <= 1000.68)
>            Predict: -1.9453153753750236
>           Else (feature 2 > 1000.68)
>            Predict: 3.954565899065237
>         Else (feature 2 > 1004.52)
>          If (feature 3 <= 60.81)
>           If (feature 0 <= 29.27)
>            Predict: 2.256991118214201
>           Else (feature 0 > 29.27)
>            Predict: -0.8956432652281918
>          Else (feature 3 > 60.81)
>           If (feature 0 <= 5.18)
>            Predict: 5.30208686561611
>           Else (feature 0 > 5.18)
>            Predict: -0.2275806642044292
>       Tree 12 (weight 0.1):
>         If (feature 3 <= 93.63)
>          If (feature 0 <= 20.6)
>           If (feature 0 <= 17.84)
>            Predict: -0.13650451477274114
>           Else (feature 0 > 17.84)
>            Predict: 4.26138638419226
>          Else (feature 0 > 20.6)
>           If (feature 0 <= 23.02)
>            Predict: -4.145788149131118
>           Else (feature 0 > 23.02)
>            Predict: 0.45010060784860767
>         Else (feature 3 > 93.63)
>          If (feature 0 <= 11.95)
>           If (feature 0 <= 6.52)
>            Predict: 1.9630864105825856
>           Else (feature 0 > 6.52)
>            Predict: -5.847103580294793
>          Else (feature 0 > 11.95)
>           If (feature 1 <= 57.85)
>            Predict: 1.6850763767018282
>           Else (feature 1 > 57.85)
>            Predict: -3.57522814358917
>       Tree 13 (weight 0.1):
>         If (feature 1 <= 56.57)
>          If (feature 1 <= 49.39)
>           If (feature 0 <= 13.56)
>            Predict: 0.7497248523199469
>           Else (feature 0 > 13.56)
>            Predict: -0.8096048572768345
>          Else (feature 1 > 49.39)
>           If (feature 0 <= 17.84)
>            Predict: -4.9975868045736025
>           Else (feature 0 > 17.84)
>            Predict: 6.70181838603398
>         Else (feature 1 > 56.57)
>          If (feature 0 <= 17.84)
>           If (feature 1 <= 58.62)
>            Predict: -8.139327595464518
>           Else (feature 1 > 58.62)
>            Predict: -11.260696586956563
>          Else (feature 0 > 17.84)
>           If (feature 2 <= 1020.32)
>            Predict: -0.4173502593107514
>           Else (feature 2 > 1020.32)
>            Predict: 7.350524302545053
>       Tree 14 (weight 0.1):
>         If (feature 2 <= 1009.3)
>          If (feature 1 <= 73.67)
>           If (feature 0 <= 26.35)
>            Predict: -0.2834715308768144
>           Else (feature 0 > 26.35)
>            Predict: -2.2855655986052446
>          Else (feature 1 > 73.67)
>           If (feature 0 <= 21.42)
>            Predict: -19.886551554013977
>           Else (feature 0 > 21.42)
>            Predict: 1.8345107899392203
>         Else (feature 2 > 1009.3)
>          If (feature 0 <= 17.84)
>           If (feature 1 <= 46.93)
>            Predict: 0.2012146645141011
>           Else (feature 1 > 46.93)
>            Predict: -5.331252849501989
>          Else (feature 0 > 17.84)
>           If (feature 0 <= 20.6)
>            Predict: 3.9009310043506518
>           Else (feature 0 > 20.6)
>            Predict: 0.05492627340134294
>       Tree 15 (weight 0.1):
>         If (feature 3 <= 80.44)
>          If (feature 0 <= 26.57)
>           If (feature 0 <= 23.02)
>            Predict: 0.24935555983937532
>           Else (feature 0 > 23.02)
>            Predict: 1.9734839371689987
>          Else (feature 0 > 26.57)
>           If (feature 1 <= 66.25)
>            Predict: -2.652691255012269
>           Else (feature 1 > 66.25)
>            Predict: 0.10205623249441657
>         Else (feature 3 > 80.44)
>          If (feature 1 <= 57.85)
>           If (feature 2 <= 1021.65)
>            Predict: 0.3189331596273633
>           Else (feature 2 > 1021.65)
>            Predict: -2.493847422724499
>          Else (feature 1 > 57.85)
>           If (feature 0 <= 23.02)
>            Predict: -4.443277995263894
>           Else (feature 0 > 23.02)
>            Predict: 1.0414575062489446
>       Tree 16 (weight 0.1):
>         If (feature 0 <= 6.52)
>          If (feature 3 <= 67.72)
>           If (feature 1 <= 39.48)
>            Predict: -0.021931809818089017
>           Else (feature 1 > 39.48)
>            Predict: 17.644618798102908
>          Else (feature 3 > 67.72)
>           If (feature 1 <= 42.07)
>            Predict: 2.6927240976688487
>           Else (feature 1 > 42.07)
>            Predict: -3.720328734281554
>         Else (feature 0 > 6.52)
>          If (feature 0 <= 8.75)
>           If (feature 0 <= 7.97)
>            Predict: -1.1870026837027776
>           Else (feature 0 > 7.97)
>            Predict: -6.311604790035118
>          Else (feature 0 > 8.75)
>           If (feature 0 <= 9.73)
>            Predict: 5.036277690956247
>           Else (feature 0 > 9.73)
>            Predict: -0.07156864179175153
>       Tree 17 (weight 0.1):
>         If (feature 2 <= 1005.35)
>          If (feature 1 <= 70.8)
>           If (feature 0 <= 21.14)
>            Predict: 0.2557898848412102
>           Else (feature 0 > 21.14)
>            Predict: -4.092246463553751
>          Else (feature 1 > 70.8)
>           If (feature 0 <= 23.02)
>            Predict: -17.7762740471523
>           Else (feature 0 > 23.02)
>            Predict: 1.4679036019616782
>         Else (feature 2 > 1005.35)
>          If (feature 3 <= 60.81)
>           If (feature 2 <= 1021.17)
>            Predict: 0.8109918761137652
>           Else (feature 2 > 1021.17)
>            Predict: 6.491756407811347
>          Else (feature 3 > 60.81)
>           If (feature 0 <= 25.72)
>            Predict: 0.06495066055048145
>           Else (feature 0 > 25.72)
>            Predict: -1.234843690619109
>       Tree 18 (weight 0.1):
>         If (feature 3 <= 93.63)
>          If (feature 0 <= 13.56)
>           If (feature 0 <= 11.95)
>            Predict: -0.1389635939018028
>           Else (feature 0 > 11.95)
>            Predict: 4.085304226900187
>          Else (feature 0 > 13.56)
>           If (feature 0 <= 15.33)
>            Predict: -3.558076811842663
>           Else (feature 0 > 15.33)
>            Predict: 0.24840255719067195
>         Else (feature 3 > 93.63)
>          If (feature 0 <= 11.95)
>           If (feature 0 <= 6.52)
>            Predict: 1.1725211739721944
>           Else (feature 0 > 6.52)
>            Predict: -4.696815201291802
>          Else (feature 0 > 11.95)
>           If (feature 0 <= 23.42)
>            Predict: -0.1435586215485262
>           Else (feature 0 > 23.42)
>            Predict: 6.017267110381734
>       Tree 19 (weight 0.1):
>         If (feature 0 <= 29.89)
>          If (feature 3 <= 46.38)
>           If (feature 2 <= 1020.32)
>            Predict: 2.734528637686715
>           Else (feature 2 > 1020.32)
>            Predict: 14.229272221061546
>          Else (feature 3 > 46.38)
>           If (feature 1 <= 73.18)
>            Predict: -0.09112932077559661
>           Else (feature 1 > 73.18)
>            Predict: 2.171636618202333
>         Else (feature 0 > 29.89)
>          If (feature 1 <= 68.3)
>           If (feature 2 <= 1012.96)
>            Predict: -4.842672386234583
>           Else (feature 2 > 1012.96)
>            Predict: 0.4656753436410731
>          Else (feature 1 > 68.3)
>           If (feature 1 <= 69.88)
>            Predict: 1.9998755414672877
>           Else (feature 1 > 69.88)
>            Predict: -1.377187598546301
>       Tree 20 (weight 0.1):
>         If (feature 1 <= 40.89)
>          If (feature 0 <= 11.95)
>           If (feature 0 <= 10.74)
>            Predict: 0.3474341793041741
>           Else (feature 0 > 10.74)
>            Predict: -3.2174625433704844
>          Else (feature 0 > 11.95)
>           If (feature 0 <= 13.56)
>            Predict: 6.2521753652461385
>           Else (feature 0 > 13.56)
>            Predict: 0.7467107076401086
>         Else (feature 1 > 40.89)
>          If (feature 1 <= 41.16)
>           If (feature 2 <= 1011.9)
>            Predict: 1.6159428806525291
>           Else (feature 2 > 1011.9)
>            Predict: -5.525791920129847
>          Else (feature 1 > 41.16)
>           If (feature 1 <= 41.48)
>            Predict: 2.3655609293253264
>           Else (feature 1 > 41.48)
>            Predict: -0.18730957785387015
>       Tree 21 (weight 0.1):
>         If (feature 0 <= 7.08)
>          If (feature 1 <= 41.58)
>           If (feature 1 <= 41.16)
>            Predict: 1.9153935195932974
>           Else (feature 1 > 41.16)
>            Predict: 7.0746807427814735
>          Else (feature 1 > 41.58)
>           If (feature 2 <= 1020.77)
>            Predict: -1.256554177586309
>           Else (feature 2 > 1020.77)
>            Predict: -26.29941855196938
>         Else (feature 0 > 7.08)
>          If (feature 0 <= 8.75)
>           If (feature 1 <= 37.8)
>            Predict: -8.544132394601597
>           Else (feature 1 > 37.8)
>            Predict: -2.6184141709801976
>          Else (feature 0 > 8.75)
>           If (feature 0 <= 9.73)
>            Predict: 4.069411815161333
>           Else (feature 0 > 9.73)
>            Predict: -0.06494039395966968
>       Tree 22 (weight 0.1):
>         If (feature 0 <= 23.02)
>          If (feature 0 <= 21.69)
>           If (feature 0 <= 15.33)
>            Predict: -0.48298234147973435
>           Else (feature 0 > 15.33)
>            Predict: 1.2747845905419344
>          Else (feature 0 > 21.69)
>           If (feature 1 <= 66.25)
>            Predict: -3.44223180465188
>           Else (feature 1 > 66.25)
>            Predict: -9.677838572965495
>         Else (feature 0 > 23.02)
>          If (feature 0 <= 24.39)
>           If (feature 1 <= 66.25)
>            Predict: 1.4289485230939327
>           Else (feature 1 > 66.25)
>            Predict: 7.493228657621072
>          Else (feature 0 > 24.39)
>           If (feature 1 <= 66.25)
>            Predict: -1.55164310941819
>           Else (feature 1 > 66.25)
>            Predict: 0.5159038364280375
>       Tree 23 (weight 0.1):
>         If (feature 2 <= 1010.89)
>          If (feature 1 <= 66.93)
>           If (feature 1 <= 43.41)
>            Predict: 0.8366856528539243
>           Else (feature 1 > 43.41)
>            Predict: -2.146264827541657
>          Else (feature 1 > 66.93)
>           If (feature 0 <= 23.02)
>            Predict: -4.593173040738928
>           Else (feature 0 > 23.02)
>            Predict: 0.7595925761507126
>         Else (feature 2 > 1010.89)
>          If (feature 0 <= 15.33)
>           If (feature 0 <= 14.38)
>            Predict: 0.19019050526253845
>           Else (feature 0 > 14.38)
>            Predict: -4.931089744789576
>          Else (feature 0 > 15.33)
>           If (feature 1 <= 56.57)
>            Predict: 2.893896440054576
>           Else (feature 1 > 56.57)
>            Predict: -0.2411893147021192
>       Tree 24 (weight 0.1):
>         If (feature 2 <= 1004.52)
>          If (feature 1 <= 39.13)
>           If (feature 0 <= 16.56)
>            Predict: 5.674347262101248
>           Else (feature 0 > 16.56)
>            Predict: -15.35003850200303
>          Else (feature 1 > 39.13)
>           If (feature 1 <= 70.8)
>            Predict: -2.2136597249782484
>           Else (feature 1 > 70.8)
>            Predict: 0.4854909471410394
>         Else (feature 2 > 1004.52)
>          If (feature 0 <= 23.02)
>           If (feature 0 <= 21.14)
>            Predict: 0.25072963079321764
>           Else (feature 0 > 21.14)
>            Predict: -3.1127381475029745
>          Else (feature 0 > 23.02)
>           If (feature 0 <= 24.98)
>            Predict: 2.513302584995404
>           Else (feature 0 > 24.98)
>            Predict: -0.17126775916442186
>       Tree 25 (weight 0.1):
>         If (feature 3 <= 76.79)
>          If (feature 0 <= 28.75)
>           If (feature 1 <= 66.25)
>            Predict: 0.1271610430935476
>           Else (feature 1 > 66.25)
>            Predict: 2.4600009065275934
>          Else (feature 0 > 28.75)
>           If (feature 1 <= 44.58)
>            Predict: -10.925990145829292
>           Else (feature 1 > 44.58)
>            Predict: -0.7031644656131009
>         Else (feature 3 > 76.79)
>          If (feature 0 <= 20.9)
>           If (feature 0 <= 17.84)
>            Predict: -0.3807566877980857
>           Else (feature 0 > 17.84)
>            Predict: 2.329590528017136
>          Else (feature 0 > 20.9)
>           If (feature 0 <= 23.02)
>            Predict: -3.741947089345415
>           Else (feature 0 > 23.02)
>            Predict: -0.3619479813878585
>       Tree 26 (weight 0.1):
>         If (feature 0 <= 5.18)
>          If (feature 1 <= 42.07)
>           If (feature 3 <= 84.36)
>            Predict: 5.869887042156764
>           Else (feature 3 > 84.36)
>            Predict: 2.3621425360574837
>          Else (feature 1 > 42.07)
>           If (feature 2 <= 1007.82)
>            Predict: -1.4185266335795177
>           Else (feature 2 > 1007.82)
>            Predict: -5.383717178467172
>         Else (feature 0 > 5.18)
>          If (feature 3 <= 53.32)
>           If (feature 2 <= 1021.17)
>            Predict: 0.6349729680247564
>           Else (feature 2 > 1021.17)
>            Predict: 9.504309080910616
>          Else (feature 3 > 53.32)
>           If (feature 0 <= 25.95)
>            Predict: 0.010243524812335326
>           Else (feature 0 > 25.95)
>            Predict: -0.8173343910336555
>       Tree 27 (weight 0.1):
>         If (feature 2 <= 1028.38)
>          If (feature 1 <= 74.87)
>           If (feature 1 <= 56.57)
>            Predict: 0.28085003688072396
>           Else (feature 1 > 56.57)
>            Predict: -0.378551674966564
>          Else (feature 1 > 74.87)
>           If (feature 0 <= 21.42)
>            Predict: -12.321588273833015
>           Else (feature 0 > 21.42)
>            Predict: 1.8659669412137414
>         Else (feature 2 > 1028.38)
>          If (feature 3 <= 89.83)
>           If (feature 3 <= 66.27)
>            Predict: -8.252928408643971
>           Else (feature 3 > 66.27)
>            Predict: -2.023910717088332
>          Else (feature 3 > 89.83)
>           If (feature 0 <= 8.39)
>            Predict: -11.472893448110653
>           Else (feature 0 > 8.39)
>            Predict: -8.030312146910243
>       Tree 28 (weight 0.1):
>         If (feature 3 <= 85.4)
>          If (feature 0 <= 7.55)
>           If (feature 1 <= 40.05)
>            Predict: 0.3456361310433187
>           Else (feature 1 > 40.05)
>            Predict: 4.958188742864418
>          Else (feature 0 > 7.55)
>           If (feature 0 <= 8.75)
>            Predict: -3.0608059226719657
>           Else (feature 0 > 8.75)
>            Predict: 0.16507864507530287
>         Else (feature 3 > 85.4)
>          If (feature 2 <= 1015.63)
>           If (feature 2 <= 1014.19)
>            Predict: -0.3593841710339432
>           Else (feature 2 > 1014.19)
>            Predict: 3.2531365191458024
>          Else (feature 2 > 1015.63)
>           If (feature 1 <= 40.64)
>            Predict: 1.0007657377910708
>           Else (feature 1 > 40.64)
>            Predict: -2.132339394694771
>       Tree 29 (weight 0.1):
>         If (feature 0 <= 30.56)
>          If (feature 3 <= 55.74)
>           If (feature 1 <= 72.24)
>            Predict: 0.8569729911086951
>           Else (feature 1 > 72.24)
>            Predict: 6.358127096088517
>          Else (feature 3 > 55.74)
>           If (feature 1 <= 41.48)
>            Predict: 0.43148253820326676
>           Else (feature 1 > 41.48)
>            Predict: -0.24352278568573174
>         Else (feature 0 > 30.56)
>          If (feature 2 <= 1014.35)
>           If (feature 1 <= 68.3)
>            Predict: -2.5522103291398683
>           Else (feature 1 > 68.3)
>            Predict: -0.21266182300917044
>          Else (feature 2 > 1014.35)
>           If (feature 1 <= 74.87)
>            Predict: -6.498613011225412
>           Else (feature 1 > 74.87)
>            Predict: 0.9765776955731879
>       Tree 30 (weight 0.1):
>         If (feature 0 <= 17.84)
>          If (feature 1 <= 45.08)
>           If (feature 0 <= 15.33)
>            Predict: -0.14424299831222268
>           Else (feature 0 > 15.33)
>            Predict: 1.8754751416891788
>          Else (feature 1 > 45.08)
>           If (feature 2 <= 1020.77)
>            Predict: -3.097730832691005
>           Else (feature 2 > 1020.77)
>            Predict: -8.90070153022011
>         Else (feature 0 > 17.84)
>          If (feature 0 <= 18.71)
>           If (feature 1 <= 49.02)
>            Predict: 1.2726140970398088
>           Else (feature 1 > 49.02)
>            Predict: 6.649324687634596
>          Else (feature 0 > 18.71)
>           If (feature 1 <= 46.93)
>            Predict: -2.818245204603037
>           Else (feature 1 > 46.93)
>            Predict: 0.23586447368304939
>       Tree 31 (weight 0.1):
>         If (feature 2 <= 1004.52)
>          If (feature 1 <= 59.14)
>           If (feature 1 <= 50.66)
>            Predict: -0.8733348655196066
>           Else (feature 1 > 50.66)
>            Predict: 7.928862441716025
>          Else (feature 1 > 59.14)
>           If (feature 1 <= 70.8)
>            Predict: -3.8112988828197807
>           Else (feature 1 > 70.8)
>            Predict: 0.42812840935226704
>         Else (feature 2 > 1004.52)
>          If (feature 0 <= 17.84)
>           If (feature 1 <= 46.93)
>            Predict: 0.07282772802501089
>           Else (feature 1 > 46.93)
>            Predict: -3.3364389464988706
>          Else (feature 0 > 17.84)
>           If (feature 2 <= 1020.32)
>            Predict: 0.18419167853517965
>           Else (feature 2 > 1020.32)
>            Predict: 6.584432032190064
>       Tree 32 (weight 0.1):
>         If (feature 1 <= 56.57)
>          If (feature 1 <= 49.39)
>           If (feature 0 <= 13.56)
>            Predict: 0.36741135502935035
>           Else (feature 0 > 13.56)
>            Predict: -0.7178818728654812
>          Else (feature 1 > 49.39)
>           If (feature 0 <= 17.84)
>            Predict: -1.7883686826457996
>           Else (feature 0 > 17.84)
>            Predict: 4.519745157967235
>         Else (feature 1 > 56.57)
>          If (feature 0 <= 17.84)
>           If (feature 0 <= 17.5)
>            Predict: -4.182857837547887
>           Else (feature 0 > 17.5)
>            Predict: -7.917768935292194
>          Else (feature 0 > 17.84)
>           If (feature 0 <= 19.61)
>            Predict: 2.6880627533068244
>           Else (feature 0 > 19.61)
>            Predict: -0.2998975340288976
>       Tree 33 (weight 0.1):
>         If (feature 0 <= 11.95)
>          If (feature 0 <= 11.03)
>           If (feature 3 <= 93.63)
>            Predict: 0.7278554646891878
>           Else (feature 3 > 93.63)
>            Predict: -2.2492543009893162
>          Else (feature 0 > 11.03)
>           If (feature 2 <= 1024.3)
>            Predict: -5.536706488618952
>           Else (feature 2 > 1024.3)
>            Predict: 4.479707018501001
>         Else (feature 0 > 11.95)
>          If (feature 0 <= 13.08)
>           If (feature 0 <= 12.5)
>            Predict: 5.173128471411881
>           Else (feature 0 > 12.5)
>            Predict: 2.3834255982190755
>          Else (feature 0 > 13.08)
>           If (feature 0 <= 15.33)
>            Predict: -1.5022006203890645
>           Else (feature 0 > 15.33)
>            Predict: 0.15423852245074754
>       Tree 34 (weight 0.1):
>         If (feature 0 <= 8.75)
>          If (feature 0 <= 7.55)
>           If (feature 3 <= 77.56)
>            Predict: 3.015852739381847
>           Else (feature 3 > 77.56)
>            Predict: -0.06103236076131486
>          Else (feature 0 > 7.55)
>           If (feature 3 <= 62.1)
>            Predict: -13.594573386743992
>           Else (feature 3 > 62.1)
>            Predict: -2.6914920546129273
>         Else (feature 0 > 8.75)
>          If (feature 0 <= 10.03)
>           If (feature 3 <= 95.45)
>            Predict: 3.213047453934116
>           Else (feature 3 > 95.45)
>            Predict: -2.3699077010186502
>          Else (feature 0 > 10.03)
>           If (feature 0 <= 11.95)
>            Predict: -1.841483689919706
>           Else (feature 0 > 11.95)
>            Predict: 0.1034719724734039
>       Tree 35 (weight 0.1):
>         If (feature 1 <= 56.57)
>          If (feature 1 <= 49.02)
>           If (feature 1 <= 44.88)
>            Predict: 0.1854471597033813
>           Else (feature 1 > 44.88)
>            Predict: -1.537157071790549
>          Else (feature 1 > 49.02)
>           If (feature 2 <= 1009.77)
>            Predict: -0.7176011396833722
>           Else (feature 2 > 1009.77)
>            Predict: 3.4414962844541495
>         Else (feature 1 > 56.57)
>          If (feature 1 <= 66.25)
>           If (feature 0 <= 21.92)
>            Predict: 0.6042503983890641
>           Else (feature 0 > 21.92)
>            Predict: -1.6430682984491796
>          Else (feature 1 > 66.25)
>           If (feature 0 <= 23.02)
>            Predict: -3.919778656895867
>           Else (feature 0 > 23.02)
>            Predict: 0.8520833743461524
>       Tree 36 (weight 0.1):
>         If (feature 0 <= 27.6)
>          If (feature 0 <= 23.02)
>           If (feature 0 <= 22.1)
>            Predict: 0.08610814822616036
>           Else (feature 0 > 22.1)
>            Predict: -3.39446668206219
>          Else (feature 0 > 23.02)
>           If (feature 1 <= 66.25)
>            Predict: -0.25067209339950686
>           Else (feature 1 > 66.25)
>            Predict: 2.1536703058787143
>         Else (feature 0 > 27.6)
>          If (feature 3 <= 62.1)
>           If (feature 1 <= 74.87)
>            Predict: -0.3912307208100507
>           Else (feature 1 > 74.87)
>            Predict: 2.6168301411252224
>          Else (feature 3 > 62.1)
>           If (feature 1 <= 71.8)
>            Predict: -0.1075335658351684
>           Else (feature 1 > 71.8)
>            Predict: -3.3756176659678685
>       Tree 37 (weight 0.1):
>         If (feature 0 <= 25.35)
>          If (feature 0 <= 23.02)
>           If (feature 1 <= 64.84)
>            Predict: 0.07789630965601392
>           Else (feature 1 > 64.84)
>            Predict: -2.8928836560033093
>          Else (feature 0 > 23.02)
>           If (feature 1 <= 66.25)
>            Predict: 0.13731068060749954
>           Else (feature 1 > 66.25)
>            Predict: 4.15851454889221
>         Else (feature 0 > 25.35)
>          If (feature 1 <= 43.65)
>           If (feature 0 <= 27.19)
>            Predict: -16.475158304770883
>           Else (feature 0 > 27.19)
>            Predict: -7.947134756554647
>          Else (feature 1 > 43.65)
>           If (feature 3 <= 62.7)
>            Predict: 0.1725950049938879
>           Else (feature 3 > 62.7)
>            Predict: -1.0926147971432427
>       Tree 38 (weight 0.1):
>         If (feature 2 <= 1028.38)
>          If (feature 0 <= 30.56)
>           If (feature 3 <= 47.89)
>            Predict: 1.6647926733523803
>           Else (feature 3 > 47.89)
>            Predict: 0.019004190066623235
>          Else (feature 0 > 30.56)
>           If (feature 2 <= 1014.35)
>            Predict: -0.6192794789083232
>           Else (feature 2 > 1014.35)
>            Predict: -4.385760311827676
>         Else (feature 2 > 1028.38)
>          If (feature 1 <= 39.48)
>           If (feature 0 <= 6.52)
>            Predict: 4.573467616169609
>           Else (feature 0 > 6.52)
>            Predict: -1.362091279334777
>          Else (feature 1 > 39.48)
>           If (feature 0 <= 8.75)
>            Predict: -7.0007999537928605
>           Else (feature 0 > 8.75)
>            Predict: -1.617908469279585
>       Tree 39 (weight 0.1):
>         If (feature 2 <= 1017.42)
>          If (feature 2 <= 1014.19)
>           If (feature 1 <= 43.13)
>            Predict: 1.2098492492388833
>           Else (feature 1 > 43.13)
>            Predict: -0.4345828650352739
>          Else (feature 2 > 1014.19)
>           If (feature 3 <= 96.38)
>            Predict: 1.0830640036331665
>           Else (feature 3 > 96.38)
>            Predict: -6.6054777318343785
>         Else (feature 2 > 1017.42)
>          If (feature 2 <= 1019.23)
>           If (feature 1 <= 57.85)
>            Predict: -0.8212874032064794
>           Else (feature 1 > 57.85)
>            Predict: -2.6667829000634105
>          Else (feature 2 > 1019.23)
>           If (feature 0 <= 17.84)
>            Predict: -0.39094381687835245
>           Else (feature 0 > 17.84)
>            Predict: 3.336117383932137
>       Tree 40 (weight 0.1):
>         If (feature 3 <= 75.23)
>          If (feature 1 <= 40.05)
>           If (feature 1 <= 39.96)
>            Predict: -1.2851367407493581
>           Else (feature 1 > 39.96)
>            Predict: -9.117459296991676
>          Else (feature 1 > 40.05)
>           If (feature 1 <= 40.89)
>            Predict: 4.461974679211411
>           Else (feature 1 > 40.89)
>            Predict: 0.25422282080546216
>         Else (feature 3 > 75.23)
>          If (feature 0 <= 21.42)
>           If (feature 0 <= 17.84)
>            Predict: -0.11457026696795661
>           Else (feature 0 > 17.84)
>            Predict: 0.9995406591682215
>          Else (feature 0 > 21.42)
>           If (feature 0 <= 23.02)
>            Predict: -2.664637163988949
>           Else (feature 0 > 23.02)
>            Predict: -0.5023743568762508
>       Tree 41 (weight 0.1):
>         If (feature 2 <= 1001.9)
>          If (feature 1 <= 39.13)
>           If (feature 3 <= 79.95)
>            Predict: 9.0188365708008
>           Else (feature 3 > 79.95)
>            Predict: 2.9702965803786205
>          Else (feature 1 > 39.13)
>           If (feature 3 <= 63.68)
>            Predict: -4.052067945951171
>           Else (feature 3 > 63.68)
>            Predict: -1.0796516186664176
>         Else (feature 2 > 1001.9)
>          If (feature 0 <= 15.33)
>           If (feature 0 <= 14.38)
>            Predict: 0.15316006561614587
>           Else (feature 0 > 14.38)
>            Predict: -3.487291240038168
>          Else (feature 0 > 15.33)
>           If (feature 1 <= 43.13)
>            Predict: 2.5605988792505605
>           Else (feature 1 > 43.13)
>            Predict: 0.03166127813460667
>       Tree 42 (weight 0.1):
>         If (feature 0 <= 11.95)
>          If (feature 0 <= 11.42)
>           If (feature 1 <= 38.25)
>            Predict: -2.0532785635493065
>           Else (feature 1 > 38.25)
>            Predict: 0.4665697970110133
>          Else (feature 0 > 11.42)
>           If (feature 1 <= 44.2)
>            Predict: -4.178641719198364
>           Else (feature 1 > 44.2)
>            Predict: -9.84024023297988
>         Else (feature 0 > 11.95)
>          If (feature 0 <= 13.08)
>           If (feature 1 <= 40.89)
>            Predict: 4.383821312183712
>           Else (feature 1 > 40.89)
>            Predict: 2.000819554066434
>          Else (feature 0 > 13.08)
>           If (feature 0 <= 15.33)
>            Predict: -1.0813581518144955
>           Else (feature 0 > 15.33)
>            Predict: 0.11492139312962121
>       Tree 43 (weight 0.1):
>         If (feature 0 <= 8.75)
>          If (feature 0 <= 7.97)
>           If (feature 3 <= 86.54)
>            Predict: 0.983392336251922
>           Else (feature 3 > 86.54)
>            Predict: -0.8690504742953818
>          Else (feature 0 > 7.97)
>           If (feature 3 <= 62.1)
>            Predict: -20.310342278835464
>           Else (feature 3 > 62.1)
>            Predict: -2.975869736741497
>         Else (feature 0 > 8.75)
>          If (feature 0 <= 9.42)
>           If (feature 2 <= 1015.45)
>            Predict: 5.74314556767472
>           Else (feature 2 > 1015.45)
>            Predict: 2.1033141679659995
>          Else (feature 0 > 9.42)
>           If (feature 1 <= 40.89)
>            Predict: 0.6933339562649613
>           Else (feature 1 > 40.89)
>            Predict: -0.10718368674776323
>       Tree 44 (weight 0.1):
>         If (feature 1 <= 74.87)
>          If (feature 1 <= 71.43)
>           If (feature 1 <= 68.3)
>            Predict: -0.0751396787352361
>           Else (feature 1 > 68.3)
>            Predict: 1.0387569941322914
>          Else (feature 1 > 71.43)
>           If (feature 1 <= 72.86)
>            Predict: -2.5461711201599986
>           Else (feature 1 > 72.86)
>            Predict: -0.0018936704520639966
>         Else (feature 1 > 74.87)
>          If (feature 1 <= 77.3)
>           If (feature 3 <= 73.33)
>            Predict: 3.4362919081871732
>           Else (feature 3 > 73.33)
>            Predict: 0.022595797531833054
>          Else (feature 1 > 77.3)
>           If (feature 2 <= 1012.39)
>            Predict: -2.0026738842740444
>           Else (feature 2 > 1012.39)
>            Predict: 1.7553499174736846
>       Tree 45 (weight 0.1):
>         If (feature 2 <= 1005.35)
>          If (feature 1 <= 72.24)
>           If (feature 1 <= 59.14)
>            Predict: 0.030127466104975898
>           Else (feature 1 > 59.14)
>            Predict: -2.2341894812350676
>          Else (feature 1 > 72.24)
>           If (feature 3 <= 60.09)
>            Predict: 4.41863108135717
>           Else (feature 3 > 60.09)
>            Predict: -0.11040726869235623
>         Else (feature 2 > 1005.35)
>          If (feature 0 <= 31.8)
>           If (feature 1 <= 66.25)
>            Predict: -0.06640264597455495
>           Else (feature 1 > 66.25)
>            Predict: 0.6711276381424462
>          Else (feature 0 > 31.8)
>           If (feature 1 <= 62.44)
>            Predict: 18.071299971628946
>           Else (feature 1 > 62.44)
>            Predict: -1.613111097205577
>       Tree 46 (weight 0.1):
>         If (feature 0 <= 25.95)
>          If (feature 0 <= 23.02)
>           If (feature 0 <= 22.6)
>            Predict: 0.0037802976144726266
>           Else (feature 0 > 22.6)
>            Predict: -3.2702083989998565
>          Else (feature 0 > 23.02)
>           If (feature 1 <= 47.83)
>            Predict: 7.351532379664369
>           Else (feature 1 > 47.83)
>            Predict: 0.6617643737173495
>         Else (feature 0 > 25.95)
>          If (feature 3 <= 62.1)
>           If (feature 0 <= 29.89)
>            Predict: 0.7522949567047181
>           Else (feature 0 > 29.89)
>            Predict: -0.5659530686126862
>          Else (feature 3 > 62.1)
>           If (feature 1 <= 43.41)
>            Predict: -9.179671352130104
>           Else (feature 1 > 43.41)
>            Predict: -0.9646184420761758
>       Tree 47 (weight 0.1):
>         If (feature 0 <= 5.18)
>          If (feature 1 <= 38.62)
>           If (feature 3 <= 77.17)
>            Predict: -4.215696425771664
>           Else (feature 3 > 77.17)
>            Predict: 5.655069692148392
>          Else (feature 1 > 38.62)
>           If (feature 1 <= 39.13)
>            Predict: -12.269101167501105
>           Else (feature 1 > 39.13)
>            Predict: 1.081763483601667
>         Else (feature 0 > 5.18)
>          If (feature 0 <= 8.75)
>           If (feature 0 <= 7.97)
>            Predict: -0.19756946285599916
>           Else (feature 0 > 7.97)
>            Predict: -2.7184931590940438
>          Else (feature 0 > 8.75)
>           If (feature 0 <= 9.42)
>            Predict: 2.558566383813981
>           Else (feature 0 > 9.42)
>            Predict: -0.006722635545763743
>       Tree 48 (weight 0.1):
>         If (feature 2 <= 1028.38)
>          If (feature 2 <= 1010.89)
>           If (feature 1 <= 66.93)
>            Predict: -0.7473456438858288
>           Else (feature 1 > 66.93)
>            Predict: 0.34762458916260297
>          Else (feature 2 > 1010.89)
>           If (feature 1 <= 58.86)
>            Predict: 0.4001213596367478
>           Else (feature 1 > 58.86)
>            Predict: -0.33373941983121597
>         Else (feature 2 > 1028.38)
>          If (feature 1 <= 42.85)
>           If (feature 1 <= 39.48)
>            Predict: 2.1904388134214514
>           Else (feature 1 > 39.48)
>            Predict: -3.2474441160938956
>          Else (feature 1 > 42.85)
>           If (feature 3 <= 71.55)
>            Predict: -1.061140549595708
>           Else (feature 3 > 71.55)
>            Predict: 6.934556118848832
>       Tree 49 (weight 0.1):
>         If (feature 0 <= 11.95)
>          If (feature 0 <= 10.74)
>           If (feature 0 <= 8.75)
>            Predict: -0.48190999213172564
>           Else (feature 0 > 8.75)
>            Predict: 1.0350335598803566
>          Else (feature 0 > 10.74)
>           If (feature 2 <= 1024.3)
>            Predict: -3.057989388513731
>           Else (feature 2 > 1024.3)
>            Predict: 2.162024696272738
>         Else (feature 0 > 11.95)
>          If (feature 0 <= 12.5)
>           If (feature 3 <= 86.91)
>            Predict: 4.627051067913808
>           Else (feature 3 > 86.91)
>            Predict: 0.9386052167341327
>          Else (feature 0 > 12.5)
>           If (feature 1 <= 37.8)
>            Predict: 4.0889321278523685
>           Else (feature 1 > 37.8)
>            Predict: -0.02245818963891235
>       Tree 50 (weight 0.1):
>         If (feature 2 <= 1017.42)
>          If (feature 2 <= 1014.19)
>           If (feature 1 <= 43.13)
>            Predict: 0.9320375696962719
>           Else (feature 1 > 43.13)
>            Predict: -0.31844348507047093
>          Else (feature 2 > 1014.19)
>           If (feature 1 <= 42.42)
>            Predict: -0.5988031510673222
>           Else (feature 1 > 42.42)
>            Predict: 1.3187243855742212
>         Else (feature 2 > 1017.42)
>          If (feature 2 <= 1019.23)
>           If (feature 1 <= 44.2)
>            Predict: -2.0646082455368195
>           Else (feature 1 > 44.2)
>            Predict: -0.4969601265683861
>          Else (feature 2 > 1019.23)
>           If (feature 0 <= 17.84)
>            Predict: -0.2870181057370213
>           Else (feature 0 > 17.84)
>            Predict: 2.6148230736448608
>       Tree 51 (weight 0.1):
>         If (feature 1 <= 38.62)
>          If (feature 0 <= 18.4)
>           If (feature 0 <= 5.18)
>            Predict: 3.850885339006515
>           Else (feature 0 > 5.18)
>            Predict: -0.940687510645146
>          Else (feature 0 > 18.4)
>           If (feature 0 <= 18.98)
>            Predict: -10.80330040562501
>           Else (feature 0 > 18.98)
>            Predict: -18.03404880535599
>         Else (feature 1 > 38.62)
>          If (feature 2 <= 1026.23)
>           If (feature 0 <= 13.56)
>            Predict: 0.5295719576334972
>           Else (feature 0 > 13.56)
>            Predict: -0.052812717813551166
>          Else (feature 2 > 1026.23)
>           If (feature 1 <= 40.22)
>            Predict: -4.371246083031292
>           Else (feature 1 > 40.22)
>            Predict: -1.3541229527292618
>       Tree 52 (weight 0.1):
>         If (feature 1 <= 66.25)
>          If (feature 1 <= 64.84)
>           If (feature 3 <= 41.26)
>            Predict: 3.045631536773922
>           Else (feature 3 > 41.26)
>            Predict: -0.0337837562463145
>          Else (feature 1 > 64.84)
>           If (feature 1 <= 65.27)
>            Predict: -5.921444872611693
>           Else (feature 1 > 65.27)
>            Predict: -0.8270282146869598
>         Else (feature 1 > 66.25)
>          If (feature 0 <= 23.02)
>           If (feature 0 <= 19.83)
>            Predict: 1.5405239234096135
>           Else (feature 0 > 19.83)
>            Predict: -3.1288830506195398
>          Else (feature 0 > 23.02)
>           If (feature 0 <= 25.35)
>            Predict: 3.2672442442602656
>           Else (feature 0 > 25.35)
>            Predict: -0.007592990267182966
>       Tree 53 (weight 0.1):
>         If (feature 0 <= 17.84)
>          If (feature 1 <= 46.93)
>           If (feature 0 <= 17.2)
>            Predict: 0.1228349542857993
>           Else (feature 0 > 17.2)
>            Predict: -2.392588492043597
>          Else (feature 1 > 46.93)
>           If (feature 2 <= 1020.77)
>            Predict: -1.8240349072310669
>           Else (feature 2 > 1020.77)
>            Predict: -6.523289398433308
>         Else (feature 0 > 17.84)
>          If (feature 0 <= 18.4)
>           If (feature 1 <= 47.83)
>            Predict: 0.5318997435908227
>           Else (feature 1 > 47.83)
>            Predict: 4.907584149653537
>          Else (feature 0 > 18.4)
>           If (feature 1 <= 46.93)
>            Predict: -2.110133253015907
>           Else (feature 1 > 46.93)
>            Predict: 0.20708863671712482
>       Tree 54 (weight 0.1):
>         If (feature 3 <= 76.79)
>          If (feature 1 <= 40.05)
>           If (feature 1 <= 39.96)
>            Predict: -0.7416033424896232
>           Else (feature 1 > 39.96)
>            Predict: -6.880323474190146
>          Else (feature 1 > 40.05)
>           If (feature 1 <= 40.89)
>            Predict: 2.887497917363201
>           Else (feature 1 > 40.89)
>            Predict: 0.17777582956662522
>         Else (feature 3 > 76.79)
>          If (feature 0 <= 19.61)
>           If (feature 0 <= 17.84)
>            Predict: -0.09172434324104897
>           Else (feature 0 > 17.84)
>            Predict: 1.9482862934683598
>          Else (feature 0 > 19.61)
>           If (feature 2 <= 1010.6)
>            Predict: -0.15262790703036064
>           Else (feature 2 > 1010.6)
>            Predict: -1.7280878096087295
>       Tree 55 (weight 0.1):
>         If (feature 0 <= 24.79)
>          If (feature 0 <= 23.02)
>           If (feature 1 <= 66.93)
>            Predict: 0.02682576814507517
>           Else (feature 1 > 66.93)
>            Predict: -2.323863726560255
>          Else (feature 0 > 23.02)
>           If (feature 1 <= 47.83)
>            Predict: 6.909290893058579
>           Else (feature 1 > 47.83)
>            Predict: 0.9944889736997976
>         Else (feature 0 > 24.79)
>          If (feature 3 <= 65.24)
>           If (feature 0 <= 28.5)
>            Predict: 0.8432916332803679
>           Else (feature 0 > 28.5)
>            Predict: -0.3680864130080106
>          Else (feature 3 > 65.24)
>           If (feature 1 <= 66.51)
>            Predict: -2.1147474860288
>           Else (feature 1 > 66.51)
>            Predict: -0.3834883036951788
>       Tree 56 (weight 0.1):
>         If (feature 0 <= 15.33)
>          If (feature 0 <= 14.38)
>           If (feature 0 <= 11.95)
>            Predict: -0.3290262091199092
>           Else (feature 0 > 11.95)
>            Predict: 0.8543511625463592
>          Else (feature 0 > 14.38)
>           If (feature 2 <= 1016.21)
>            Predict: -0.7208476709379852
>           Else (feature 2 > 1016.21)
>            Predict: -4.40928839539672
>         Else (feature 0 > 15.33)
>          If (feature 0 <= 16.22)
>           If (feature 2 <= 1013.19)
>            Predict: 4.554268903891635
>           Else (feature 2 > 1013.19)
>            Predict: 1.538781048856137
>          Else (feature 0 > 16.22)
>           If (feature 1 <= 46.93)
>            Predict: -1.1488437756174756
>           Else (feature 1 > 46.93)
>            Predict: 0.1634274865006602
>       Tree 57 (weight 0.1):
>         If (feature 2 <= 1007.46)
>          If (feature 1 <= 73.67)
>           If (feature 1 <= 71.43)
>            Predict: -0.28457458674767294
>           Else (feature 1 > 71.43)
>            Predict: -2.556284198496123
>          Else (feature 1 > 73.67)
>           If (feature 3 <= 60.81)
>            Predict: 4.31886476056719
>           Else (feature 3 > 60.81)
>            Predict: 0.3197495651743129
>         Else (feature 2 > 1007.46)
>          If (feature 0 <= 17.84)
>           If (feature 1 <= 46.93)
>            Predict: 0.04575453109929229
>           Else (feature 1 > 46.93)
>            Predict: -2.141138284310683
>          Else (feature 0 > 17.84)
>           If (feature 1 <= 56.57)
>            Predict: 1.3439965861050847
>           Else (feature 1 > 56.57)
>            Predict: -0.02904919315788331
>       Tree 58 (weight 0.1):
>         If (feature 0 <= 31.8)
>          If (feature 1 <= 66.25)
>           If (feature 1 <= 64.84)
>            Predict: -0.006836636445003446
>           Else (feature 1 > 64.84)
>            Predict: -2.0890363043188134
>          Else (feature 1 > 66.25)
>           If (feature 1 <= 69.05)
>            Predict: 1.8596834938858298
>           Else (feature 1 > 69.05)
>            Predict: -0.2637818907162569
>         Else (feature 0 > 31.8)
>          If (feature 1 <= 69.34)
>           If (feature 2 <= 1009.63)
>            Predict: -4.53407923927751
>           Else (feature 2 > 1009.63)
>            Predict: 1.2479530412848983
>          Else (feature 1 > 69.34)
>           If (feature 1 <= 69.88)
>            Predict: 5.672382101944148
>           Else (feature 1 > 69.88)
>            Predict: -0.7728960613425813
>       Tree 59 (weight 0.1):
>         If (feature 2 <= 1010.89)
>          If (feature 1 <= 68.3)
>           If (feature 1 <= 43.41)
>            Predict: 0.423961936091299
>           Else (feature 1 > 43.41)
>            Predict: -1.0411314850417004
>          Else (feature 1 > 68.3)
>           If (feature 1 <= 68.67)
>            Predict: 7.130757445704555
>           Else (feature 1 > 68.67)
>            Predict: 0.1160942217864609
>         Else (feature 2 > 1010.89)
>          If (feature 3 <= 93.63)
>           If (feature 1 <= 58.86)
>            Predict: 0.41091291246834866
>           Else (feature 1 > 58.86)
>            Predict: -0.2764637915143923
>          Else (feature 3 > 93.63)
>           If (feature 1 <= 41.74)
>            Predict: -3.564757715833512
>           Else (feature 1 > 41.74)
>            Predict: 1.1644353912440248
>       Tree 60 (weight 0.1):
>         If (feature 1 <= 48.6)
>          If (feature 1 <= 44.88)
>           If (feature 2 <= 1016.57)
>            Predict: 0.4410572983039277
>           Else (feature 2 > 1016.57)
>            Predict: -0.44414793681792664
>          Else (feature 1 > 44.88)
>           If (feature 2 <= 1014.35)
>            Predict: -3.0626378082153085
>           Else (feature 2 > 1014.35)
>            Predict: 2.0328536525605063
>         Else (feature 1 > 48.6)
>          If (feature 1 <= 52.05)
>           If (feature 2 <= 1009.9)
>            Predict: 0.24004783900051171
>           Else (feature 2 > 1009.9)
>            Predict: 3.1645061792332916
>          Else (feature 1 > 52.05)
>           If (feature 0 <= 17.84)
>            Predict: -1.95074879327582
>           Else (feature 0 > 17.84)
>            Predict: 0.021106826304965107
>       Tree 61 (weight 0.1):
>         If (feature 1 <= 74.87)
>          If (feature 1 <= 71.43)
>           If (feature 1 <= 68.3)
>            Predict: -0.06241270845694165
>           Else (feature 1 > 68.3)
>            Predict: 0.8051320337219834
>          Else (feature 1 > 71.43)
>           If (feature 0 <= 24.57)
>            Predict: 1.648459594873699
>           Else (feature 0 > 24.57)
>            Predict: -1.2314608832462137
>         Else (feature 1 > 74.87)
>          If (feature 1 <= 77.3)
>           If (feature 0 <= 21.42)
>            Predict: -7.482222216002697
>           Else (feature 0 > 21.42)
>            Predict: 1.8228183337802573
>          Else (feature 1 > 77.3)
>           If (feature 2 <= 1012.39)
>            Predict: -1.4326641812285505
>           Else (feature 2 > 1012.39)
>            Predict: 1.7079353624089986
>       Tree 62 (weight 0.1):
>         If (feature 0 <= 5.18)
>          If (feature 1 <= 42.07)
>           If (feature 3 <= 96.38)
>            Predict: 1.4583097259406885
>           Else (feature 3 > 96.38)
>            Predict: 7.4053761713858615
>          Else (feature 1 > 42.07)
>           If (feature 2 <= 1008.19)
>            Predict: 0.311290850436914
>           Else (feature 2 > 1008.19)
>            Predict: -5.145119802972147
>         Else (feature 0 > 5.18)
>          If (feature 1 <= 38.62)
>           If (feature 0 <= 18.4)
>            Predict: -0.7259884411546618
>           Else (feature 0 > 18.4)
>            Predict: -12.427884135864616
>          Else (feature 1 > 38.62)
>           If (feature 1 <= 39.48)
>            Predict: 1.131291291234381
>           Else (feature 1 > 39.48)
>            Predict: -0.007004055574359982
>       Tree 63 (weight 0.1):
>         If (feature 2 <= 1004.52)
>          If (feature 1 <= 70.8)
>           If (feature 1 <= 69.05)
>            Predict: -0.45566718124370104
>           Else (feature 1 > 69.05)
>            Predict: -3.3633539333883373
>          Else (feature 1 > 70.8)
>           If (feature 3 <= 70.63)
>            Predict: 1.7061073842258219
>           Else (feature 3 > 70.63)
>            Predict: -0.35469491259927843
>         Else (feature 2 > 1004.52)
>          If (feature 0 <= 15.33)
>           If (feature 0 <= 14.13)
>            Predict: 0.13165022513417465
>           Else (feature 0 > 14.13)
>            Predict: -1.8886218519887454
>          Else (feature 0 > 15.33)
>           If (feature 1 <= 43.13)
>            Predict: 2.0897911694212086
>           Else (feature 1 > 43.13)
>            Predict: 0.023571622513158218
>       Tree 64 (weight 0.1):
>         If (feature 1 <= 41.92)
>          If (feature 1 <= 41.58)
>           If (feature 2 <= 1015.45)
>            Predict: 0.6420804366913081
>           Else (feature 2 > 1015.45)
>            Predict: -0.3393001000428116
>          Else (feature 1 > 41.58)
>           If (feature 3 <= 91.38)
>            Predict: -2.959889489145066
>           Else (feature 3 > 91.38)
>            Predict: -14.822621379271645
>         Else (feature 1 > 41.92)
>          If (feature 1 <= 43.13)
>           If (feature 0 <= 15.33)
>            Predict: 0.5584851317693598
>           Else (feature 0 > 15.33)
>            Predict: 5.35806974907062
>          Else (feature 1 > 43.13)
>           If (feature 1 <= 43.65)
>            Predict: -2.5734171913252673
>           Else (feature 1 > 43.65)
>            Predict: 0.06206747847844893
>       Tree 65 (weight 0.1):
>         If (feature 2 <= 1010.89)
>          If (feature 1 <= 66.93)
>           If (feature 0 <= 20.6)
>            Predict: -0.0679333275254979
>           Else (feature 0 > 20.6)
>            Predict: -1.053808811058633
>          Else (feature 1 > 66.93)
>           If (feature 1 <= 67.32)
>            Predict: 7.372080266725638
>           Else (feature 1 > 67.32)
>            Predict: 0.09996335027123535
>         Else (feature 2 > 1010.89)
>          If (feature 3 <= 75.61)
>           If (feature 1 <= 40.05)
>            Predict: -0.9831581524231143
>           Else (feature 1 > 40.05)
>            Predict: 0.5486160789249349
>          Else (feature 3 > 75.61)
>           If (feature 1 <= 58.86)
>            Predict: 0.19399224442246701
>           Else (feature 1 > 58.86)
>            Predict: -1.5652059699408227
>       Tree 66 (weight 0.1):
>         If (feature 0 <= 28.75)
>          If (feature 1 <= 73.18)
>           If (feature 1 <= 71.43)
>            Predict: 0.05143978594106816
>           Else (feature 1 > 71.43)
>            Predict: -1.436513600322334
>          Else (feature 1 > 73.18)
>           If (feature 3 <= 73.33)
>            Predict: 4.1459864582084975
>           Else (feature 3 > 73.33)
>            Predict: 0.34965185037807356
>         Else (feature 0 > 28.75)
>          If (feature 2 <= 1014.54)
>           If (feature 2 <= 1013.43)
>            Predict: -0.4008005884834272
>           Else (feature 2 > 1013.43)
>            Predict: 3.683818693727259
>          Else (feature 2 > 1014.54)
>           If (feature 1 <= 67.83)
>            Predict: -0.82614879352537
>           Else (feature 1 > 67.83)
>            Predict: -4.535981326886069
>       Tree 67 (weight 0.1):
>         If (feature 1 <= 47.83)
>          If (feature 0 <= 23.02)
>           If (feature 0 <= 18.71)
>            Predict: -0.0010074123242523121
>           Else (feature 0 > 18.71)
>            Predict: -3.2926535011699234
>          Else (feature 0 > 23.02)
>           If (feature 2 <= 1012.39)
>            Predict: 1.3034696914565052
>           Else (feature 2 > 1012.39)
>            Predict: 11.235282784300427
>         Else (feature 1 > 47.83)
>          If (feature 1 <= 56.57)
>           If (feature 0 <= 17.84)
>            Predict: -1.039931035628621
>           Else (feature 0 > 17.84)
>            Predict: 1.9905896386111916
>          Else (feature 1 > 56.57)
>           If (feature 1 <= 57.19)
>            Predict: -2.3357601760278204
>           Else (feature 1 > 57.19)
>            Predict: -0.0355403353056693
>       Tree 68 (weight 0.1):
>         If (feature 0 <= 24.79)
>          If (feature 3 <= 41.26)
>           If (feature 1 <= 45.87)
>            Predict: 2.4904273637383265
>           Else (feature 1 > 45.87)
>            Predict: 13.013875696314063
>          Else (feature 3 > 41.26)
>           If (feature 1 <= 49.02)
>            Predict: -0.18642415027276396
>           Else (feature 1 > 49.02)
>            Predict: 0.47121076166963227
>         Else (feature 0 > 24.79)
>          If (feature 1 <= 65.27)
>           If (feature 1 <= 64.84)
>            Predict: -0.5...

Step 8: Deployment
==================

Now that we have a predictive model it is time to deploy the model into an operational environment.

In our example, let's say we have a series of sensors attached to the power plant and a monitoring station.

The monitoring station will need close to real-time information about how much power that their station will generate so they can relay that to the utility.

So let's create a Spark Streaming utility that we can use for this purpose.

See <http://spark.apache.org/docs/latest/streaming-programming-guide.html> if you can't wait!

After deployment you will be able to use the best predictions from gradient boosed regression trees to feed a real-time dashboard or feed the utility with information on how much power the peaker plant will deliver give current conditions.

``` scala
// Let's set the variable finalModel to our best GBT Model
val finalModel = gbtModel.bestModel
```

>     finalModel: org.apache.spark.ml.Model[_] = pipeline_e6a84d2d75ba

``` scala
//gbtModel.bestModel.asInstanceOf[PipelineModel]//.stages.last.asInstanceOf[GBTRegressionModel]
//        .write.overwrite().save("dbfs:///databricks/driver/MyTrainedGbtPipelineModel")
```

``` scala
//val finalModel = PipelineModel.load("dbfs:///databricks/driver/MyTrainedGbtPipelineModel/")
```

Let's create our table for predictions

``` sql
DROP TABLE IF EXISTS power_plant_predictions ;
CREATE TABLE power_plant_predictions(
  AT Double,
  V Double,
  AP Double,
  RH Double,
  PE Double,
  Predicted_PE Double
);
```

This should be updated to structured streaming - after the break.

Now let's create our streaming job to score new power plant readings in real-time.

**CAUTION**: There can be only one spark streaming context per cluster!!! So please check if a streaming context is already alive first.

``` scala
import java.nio.ByteBuffer
import java.net._
import java.io._
import concurrent._
import scala.io._
import sys.process._
//import org.apache.spark.Logging
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.Minutes
import org.apache.spark.streaming.StreamingContext
//import org.apache.spark.streaming.StreamingContext.toPairDStreamFunctions
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.streaming.receiver.Receiver
import sqlContext._
import net.liftweb.json.DefaultFormats
import net.liftweb.json._

import scala.collection.mutable.SynchronizedQueue


val queue = new SynchronizedQueue[RDD[String]]()

val batchIntervalSeconds = 2

var newContextCreated = false      // Flag to detect whether new context was created or not

// Function to create a new StreamingContext and set it up
def creatingFunc(): StreamingContext = {
    
  // Create a StreamingContext
  val ssc = new StreamingContext(sc, Seconds(batchIntervalSeconds))
  val batchInterval = Seconds(1)
  ssc.remember(Seconds(300))
  val dstream = ssc.queueStream(queue)
  dstream.foreachRDD { 
    rdd =>
      // if the RDD has data
       if(!(rdd.isEmpty())) {
          // Use the final model to transform a JSON message into a dataframe and pass the dataframe to our model's transform method
           finalModel
             .transform(read.json(rdd.toDS).toDF())
         // Select only columns we are interested in
         .select("AT", "V", "AP", "RH", "PE", "Predicted_PE")
         // Append the results to our power_plant_predictions table
         .write.mode(SaveMode.Append).format("hive").saveAsTable("power_plant_predictions")
       } 
  }
  println("Creating function called to create new StreamingContext for Power Plant Predictions")
  newContextCreated = true  
  ssc
}

val ssc = StreamingContext.getActiveOrCreate(creatingFunc)
if (newContextCreated) {
  println("New context created from currently defined creating function") 
} else {
  println("Existing context running or recovered from checkpoint, may not be running currently defined creating function")
}

ssc.start()
```

>     Creating function called to create new StreamingContext for Power Plant Predictions
>     New context created from currently defined creating function
>     <console>:303: warning: class SynchronizedQueue in package mutable is deprecated: Synchronization via selective overriding of methods is inherently unreliable.  Consider java.util.concurrent.ConcurrentLinkedQueue as an alternative.
>            val queue = new SynchronizedQueue[RDD[String]]()
>                            ^
>     import java.nio.ByteBuffer
>     import java.net._
>     import java.io._
>     import concurrent._
>     import scala.io._
>     import sys.process._
>     import org.apache.spark.SparkConf
>     import org.apache.spark.storage.StorageLevel
>     import org.apache.spark.streaming.Seconds
>     import org.apache.spark.streaming.Minutes
>     import org.apache.spark.streaming.StreamingContext
>     import org.apache.log4j.Logger
>     import org.apache.log4j.Level
>     import org.apache.spark.streaming.receiver.Receiver
>     import sqlContext._
>     import net.liftweb.json.DefaultFormats
>     import net.liftweb.json._
>     import scala.collection.mutable.SynchronizedQueue
>     queue: scala.collection.mutable.SynchronizedQueue[org.apache.spark.rdd.RDD[String]] = SynchronizedQueue()
>     batchIntervalSeconds: Int = 2
>     newContextCreated: Boolean = true
>     creatingFunc: ()org.apache.spark.streaming.StreamingContext
>     ssc: org.apache.spark.streaming.StreamingContext = org.apache.spark.streaming.StreamingContext@4a48de26

Now that we have created and defined our streaming job, let's test it with some data. First we clear the predictions table.

``` sql
truncate table power_plant_predictions
```

Let's use data to see how much power output our model will predict.

``` scala
// First we try it with a record from our test set and see what we get:
queue += sc.makeRDD(Seq(s"""{"AT":10.82,"V":37.5,"AP":1009.23,"RH":96.62,"PE":473.9}"""))

// We may need to wait a few seconds for data to appear in the table
Thread.sleep(Seconds(5).milliseconds)
```

``` sql
--and we can query our predictions table
select * from power_plant_predictions
```

| AT    | V    | AP      | RH    | PE    | Predicted\_PE    |
|-------|------|---------|-------|-------|------------------|
| 10.82 | 37.5 | 1009.23 | 96.62 | 473.9 | 472.659932584668 |

Let's repeat with a different test measurement that our model has not seen before:

``` scala
queue += sc.makeRDD(Seq(s"""{"AT":10.0,"V":40,"AP":1000,"RH":90.0,"PE":0.0}"""))
Thread.sleep(Seconds(5).milliseconds)
```

``` sql
--Note you may have to run this a couple of times to see the refreshed data...
select * from power_plant_predictions
```

| AT    | V    | AP      | RH    | PE    | Predicted\_PE     |
|-------|------|---------|-------|-------|-------------------|
| 10.0  | 40.0 | 1000.0  | 90.0  | 0.0   | 474.5912134899266 |
| 10.82 | 37.5 | 1009.23 | 96.62 | 473.9 | 472.659932584668  |

As you can see the Predictions are very close to the real data points.

``` sql
select * from power_plant_table where AT between 10 and 11 and AP between 1000 and 1010 and RH between 90 and 97 and v between 37 and 40 order by PE 
```

| AT    | V     | AP      | RH    | PE     |
|-------|-------|---------|-------|--------|
| 10.37 | 37.83 | 1006.5  | 90.99 | 470.66 |
| 10.22 | 37.83 | 1005.94 | 93.53 | 471.79 |
| 10.66 | 37.5  | 1009.42 | 95.86 | 472.86 |
| 10.82 | 37.5  | 1009.23 | 96.62 | 473.9  |
| 10.48 | 37.5  | 1009.81 | 95.26 | 474.57 |

Now you use the predictions table to feed a real-time dashboard or feed the utility with information on how much power the peaker plant will deliver.

Make sure the streaming context is stopped when you are done, as there can be only one such context per cluster!

``` scala
ssc.stop(stopSparkContext = false) // gotto stop or it ill keep running!!!
```

Datasource References:

-   Pinar Tüfekci, Prediction of full load electrical power output of a base load operated combined cycle power plant using machine learning methods, International Journal of Electrical Power & Energy Systems, Volume 60, September 2014, Pages 126-140, ISSN 0142-0615, [Web Link](http://www.journals.elsevier.com/international-journal-of-electrical-power-and-energy-systems/)
-   Heysem Kaya, Pinar Tüfekci , Sadik Fikret Gürgen: Local and Global Learning Methods for Predicting Power of a Combined Gas & Steam Turbine, Proceedings of the International Conference on Emerging Trends in Computer and Electronics Engineering ICETCEE 2012, pp. 13-18 (Mar. 2012, Dubai) [Web Link](http://www.cmpe.boun.edu.tr/~kaya/kaya2012gasturbine.pdf)