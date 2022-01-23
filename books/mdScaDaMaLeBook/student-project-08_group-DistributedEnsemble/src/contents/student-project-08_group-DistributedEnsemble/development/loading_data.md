<div class="cell markdown">

**TOY DATA**

Data with Gaussian clusters

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
from pyspark.mllib.random import RandomRDDs
from random import randrange
import numpy as np
import torch
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
def create_gaussian_RDD(means, variances, num_observations, class_proportions, train_test_split=False):
  """Create toy Gaussian classification data
  Let C := number of clusters/classes and P := number of data features
  
  Args: 
    means (np.array[float]): mean vector of shape (C, P)
    variances (np.array[float]): vector of variances, shape (C, P)
    num_observations (scalar[int]): the total number of observations in the final data set
    class_proportions (np.array[float]): vector of class proportions, length C
    train_test_split: whether to split the data into train/test sets or not
    
  Returns:
    Gaussian data, RDD of tuples (list(features), int(label))
  """
  
  assert means.shape[0] == variances.shape[0]
  assert means.shape[1] == variances.shape[1]
  assert class_proportions.sum() == 1
    
  num_classes = means.shape[0]
  num_features = means.shape[1]
  
  data_rdd = sc.emptyRDD() 
  for k in range(num_classes):
    
    # Generate standard normal data
    class_size = int(num_observations * class_proportions[k])
    class_rdd = RandomRDDs.normalVectorRDD(sc, numRows=class_size, numCols=num_features, numPartitions=1) #, seed=123)

    # Map to true distribution
    class_rdd_transformed = class_rdd.map(lambda v: means[k, :] + (variances[k, :]**0.5) * v)
    
    # Add labels
    class_rdd_w_label = class_rdd_transformed.map(lambda v: (v, k)) 
    
    data_rdd = data_rdd.union(class_rdd_w_label)
    
  # We will shuffle and repartition the data
  num_partitions = 10
  shuffled_rdd =  data_rdd.sortBy(lambda v: randrange(num_observations)).repartition(num_partitions)
  final_rdd = shuffled_rdd.map(tuple).map(lambda v: (list(v[0]), int(v[1])))
  
  if train_test_split:
    train_rdd, test_rdd = final_rdd.randomSplit(weights=[0.8, 0.2], seed=12)
    final_rdd = (train_rdd, test_rdd)
    
  return final_rdd                                   
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
# Example use
# Test: 3 classes, 2 features
means = np.array([[-1, -10], [1, 10], [0, -1]])
variances = np.ones((3, 2))

# We should shuffle this
num_observations=1000
class_proportions = np.array([0.3, 0.3, 0.4]) # Sorry, this should have been an array
my_rdd = create_gaussian_RDD(means, variances, num_observations, class_proportions)
my_rdd.take(5)
```

</div>

<div class="cell markdown">

**FRAUD DATA**

This data is from https://www.kaggle.com/ntnu-testimon/paysim1 \[1\]. It is actually synthetic but should have more complex features than the toy data above. Number of data points in total: 6,362,620. Number of features (kept): 7 (wherof one is categorical). Number of classes: 2 (fraud/not fraud).

\[1\] E. A. Lopez-Rojas , A. Elmir, and S. Axelsson. "PaySim: A financial mobile money simulator for fraud detection". In: The 28th European Modeling and Simulation Symposium-EMSS, Larnaca, Cyprus. 2016 (PaySim first paper of the simulator)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
from pyspark.ml.feature import StringIndexer, OneHotEncoder, StandardScaler, VectorAssembler
from pyspark.ml import Pipeline
import torch
```

</div>

<div class="cell code" execution_count="1" scrolled="true">

``` python
def load_fraud_data(train_test_split=False):
  """Load and preprocess synthetic fraud data set
  
  Args:
     train_test_split: whether to split the data into train/test sets or not
    
  Returns:
    Fraud data, RDD of tuples (list(features), int(label))
  """
  
  # File location and type
  file_location = "/FileStore/tables/financial_fraud_data.csv"  # It seems perhaps like these files are not saved for long?
  file_type = "csv"

  # CSV options
  infer_schema = "true"
  first_row_is_header = "true"
  delimiter = ","

  # Load the data from file
  df = spark.read.format(file_type) \
      .option("inferSchema", infer_schema) \
      .option("header", first_row_is_header) \
      .option("sep", delimiter) \
      .load(file_location).select("step", "type", "amount", "oldbalanceOrg", "newbalanceOrig", "oldbalanceDest", "newbalanceDest", "isFraud") \
  
  col_num = ["step", "amount", "oldbalanceOrg", "newbalanceOrig", "oldbalanceDest", "newbalanceDest"]
  col_cat = "type"

  # Rename target column
  df_renamed = df.withColumnRenamed("isFraud", "label")

  # Convert qualitative variable to one-hot
  indexer = StringIndexer(inputCol = col_cat, outputCol = "type_ind")
  oh_encoder = OneHotEncoder(inputCol = "type_ind", outputCol = "type_oh")

  # Scale numerical features
  va = VectorAssembler(inputCols = col_num_f, outputCol = "numerical_features")
  scaler = StandardScaler(inputCol = "numerical_features", outputCol="scaled")

  # Merge all features in one column
  va2 = VectorAssembler(inputCols = ["scaled", "type_oh"], outputCol = "features")

  # Apply pipeline
  pipeline = Pipeline(stages = [indexer, oh_encoder, va, scaler, va2])
  final_df = pipeline.fit(df_renamed).transform(df_renamed).select("features", "label")

  # Convert to RDD 
  final_rdd = final_df.rdd.map(tuple).map(lambda v: (list(v[0]), int(v[1])))
  
  if train_test_split:
    train_rdd, test_rdd = final_rdd.randomSplit(weights=[0.8, 0.2], seed=12)
    final_rdd = (train_rdd, test_rdd)
  
  return final_rdd
```

<div class="output execute_result tabular_result" execution_count="1">

<table>
<thead>
<tr class="header">
<th>step</th>
<th>type</th>
<th>amount</th>
<th>oldbalanceOrg</th>
<th>newbalanceOrig</th>
<th>oldbalanceDest</th>
<th>newbalanceDest</th>
<th>isFraud</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>9839.64</td>
<td>170136.0</td>
<td>160296.36</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>1864.28</td>
<td>21249.0</td>
<td>19384.72</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>181.0</td>
<td>181.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>1.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>181.0</td>
<td>181.0</td>
<td>0.0</td>
<td>21182.0</td>
<td>0.0</td>
<td>1.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>11668.14</td>
<td>41554.0</td>
<td>29885.86</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>7817.71</td>
<td>53860.0</td>
<td>46042.29</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>7107.77</td>
<td>183195.0</td>
<td>176087.23</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>7861.64</td>
<td>176087.23</td>
<td>168225.59</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>4024.36</td>
<td>2671.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>DEBIT</td>
<td>5337.77</td>
<td>41720.0</td>
<td>36382.23</td>
<td>41898.0</td>
<td>40348.79</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>DEBIT</td>
<td>9644.94</td>
<td>4465.0</td>
<td>0.0</td>
<td>10845.0</td>
<td>157982.12</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>3099.97</td>
<td>20771.0</td>
<td>17671.03</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>2560.74</td>
<td>5070.0</td>
<td>2509.26</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>11633.76</td>
<td>10127.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>4098.78</td>
<td>503264.0</td>
<td>499165.22</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>229133.94</td>
<td>15325.0</td>
<td>0.0</td>
<td>5083.0</td>
<td>51513.44</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>1563.82</td>
<td>450.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>1157.86</td>
<td>21156.0</td>
<td>19998.14</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>671.64</td>
<td>15123.0</td>
<td>14451.36</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>215310.3</td>
<td>705.0</td>
<td>0.0</td>
<td>22425.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>1373.43</td>
<td>13854.0</td>
<td>12480.57</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>DEBIT</td>
<td>9302.79</td>
<td>11299.0</td>
<td>1996.21</td>
<td>29832.0</td>
<td>16896.7</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>DEBIT</td>
<td>1065.41</td>
<td>1817.0</td>
<td>751.59</td>
<td>10330.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>3876.41</td>
<td>67852.0</td>
<td>63975.59</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>311685.89</td>
<td>10835.0</td>
<td>0.0</td>
<td>6267.0</td>
<td>2719172.89</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>6061.13</td>
<td>443.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>9478.39</td>
<td>116494.0</td>
<td>107015.61</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>8009.09</td>
<td>10968.0</td>
<td>2958.91</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>8901.99</td>
<td>2958.91</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>9920.52</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>3448.92</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>4206.84</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>5885.56</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>5307.88</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>5031.22</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>24213.67</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>8603.42</td>
<td>253.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>2791.42</td>
<td>300481.0</td>
<td>297689.58</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>7413.54</td>
<td>297689.58</td>
<td>290276.03</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>3295.19</td>
<td>233633.0</td>
<td>230337.81</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>1684.81</td>
<td>297.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>DEBIT</td>
<td>5758.59</td>
<td>32604.0</td>
<td>26845.41</td>
<td>209699.0</td>
<td>16997.22</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>110414.71</td>
<td>26845.41</td>
<td>0.0</td>
<td>288800.0</td>
<td>2415.16</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>7823.46</td>
<td>998.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>5086.48</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>5281.48</td>
<td>152019.0</td>
<td>146737.52</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>13875.98</td>
<td>15818.0</td>
<td>1942.02</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>56953.9</td>
<td>1942.02</td>
<td>0.0</td>
<td>70253.0</td>
<td>64106.18</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>5346.89</td>
<td>0.0</td>
<td>0.0</td>
<td>652637.0</td>
<td>6453430.91</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>2204.04</td>
<td>586.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>2641.47</td>
<td>23053.0</td>
<td>20411.53</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>23261.3</td>
<td>20411.53</td>
<td>0.0</td>
<td>25742.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>2330.64</td>
<td>203543.0</td>
<td>201212.36</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>1614.64</td>
<td>41276.0</td>
<td>39661.36</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>9164.71</td>
<td>47235.77</td>
<td>38071.06</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>2970.97</td>
<td>38071.06</td>
<td>35100.09</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>38.66</td>
<td>16174.0</td>
<td>16135.34</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>2252.44</td>
<td>1627.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>62610.8</td>
<td>79114.0</td>
<td>16503.2</td>
<td>517.0</td>
<td>8383.29</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>DEBIT</td>
<td>5529.13</td>
<td>8547.0</td>
<td>3017.87</td>
<td>10206.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>82940.31</td>
<td>3017.87</td>
<td>0.0</td>
<td>132372.0</td>
<td>49864.36</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>DEBIT</td>
<td>4510.22</td>
<td>10256.0</td>
<td>5745.78</td>
<td>10697.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>DEBIT</td>
<td>8727.74</td>
<td>882770.0</td>
<td>874042.26</td>
<td>12636.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>2599.46</td>
<td>874042.26</td>
<td>871442.79</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>DEBIT</td>
<td>4874.49</td>
<td>153.0</td>
<td>0.0</td>
<td>253104.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>6440.78</td>
<td>2192.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>4910.14</td>
<td>41551.0</td>
<td>36640.86</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>6444.64</td>
<td>12019.0</td>
<td>5574.36</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>DEBIT</td>
<td>5149.66</td>
<td>4782.0</td>
<td>0.0</td>
<td>52752.0</td>
<td>24044.18</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>7292.16</td>
<td>216827.0</td>
<td>209534.84</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>47458.86</td>
<td>209534.84</td>
<td>162075.98</td>
<td>52120.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>136872.92</td>
<td>162075.98</td>
<td>25203.05</td>
<td>217806.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>94253.33</td>
<td>25203.05</td>
<td>0.0</td>
<td>99773.0</td>
<td>965870.05</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>2998.04</td>
<td>12030.0</td>
<td>9031.96</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>3454.08</td>
<td>9031.96</td>
<td>5577.88</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>4316.2</td>
<td>10999.0</td>
<td>6682.8</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>2131.84</td>
<td>224.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>12986.61</td>
<td>23350.0</td>
<td>10363.39</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>42712.39</td>
<td>10363.39</td>
<td>0.0</td>
<td>57901.66</td>
<td>24044.18</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>77957.68</td>
<td>0.0</td>
<td>0.0</td>
<td>94900.0</td>
<td>22233.65</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>17231.46</td>
<td>0.0</td>
<td>0.0</td>
<td>24672.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>78766.03</td>
<td>0.0</td>
<td>0.0</td>
<td>103772.0</td>
<td>277515.05</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>224606.64</td>
<td>0.0</td>
<td>0.0</td>
<td>354678.92</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>125872.53</td>
<td>0.0</td>
<td>0.0</td>
<td>348512.0</td>
<td>3420103.09</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>379856.23</td>
<td>0.0</td>
<td>0.0</td>
<td>900180.0</td>
<td>1.916920493e7</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>1505626.01</td>
<td>0.0</td>
<td>0.0</td>
<td>29031.0</td>
<td>5515763.34</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>554026.99</td>
<td>0.0</td>
<td>0.0</td>
<td>579285.56</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>147543.1</td>
<td>0.0</td>
<td>0.0</td>
<td>223220.0</td>
<td>16518.36</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>761507.39</td>
<td>0.0</td>
<td>0.0</td>
<td>1280036.23</td>
<td>1.916920493e7</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>1429051.47</td>
<td>0.0</td>
<td>0.0</td>
<td>2041543.62</td>
<td>1.916920493e7</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>358831.92</td>
<td>0.0</td>
<td>0.0</td>
<td>474384.53</td>
<td>3420103.09</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>367768.4</td>
<td>0.0</td>
<td>0.0</td>
<td>370763.1</td>
<td>16518.36</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>209711.11</td>
<td>0.0</td>
<td>0.0</td>
<td>399214.71</td>
<td>2415.16</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>583848.46</td>
<td>0.0</td>
<td>0.0</td>
<td>667778.0</td>
<td>2107778.11</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>1724887.05</td>
<td>0.0</td>
<td>0.0</td>
<td>3470595.1</td>
<td>1.916920493e7</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>710544.77</td>
<td>0.0</td>
<td>0.0</td>
<td>738531.5</td>
<td>16518.36</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>581294.26</td>
<td>0.0</td>
<td>0.0</td>
<td>5195482.15</td>
<td>1.916920493e7</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>11996.58</td>
<td>0.0</td>
<td>0.0</td>
<td>40255.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>2875.1</td>
<td>15443.0</td>
<td>12567.9</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>8586.98</td>
<td>3763.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>871.75</td>
<td>19869.0</td>
<td>18997.25</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>1035.36</td>
<td>71636.0</td>
<td>70600.64</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>1063.53</td>
<td>83084.0</td>
<td>82020.47</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>1019.9</td>
<td>204237.0</td>
<td>203217.1</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>4059.38</td>
<td>26304.0</td>
<td>22244.62</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>1876.44</td>
<td>182.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>28404.6</td>
<td>0.0</td>
<td>0.0</td>
<td>51744.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>75405.1</td>
<td>0.0</td>
<td>0.0</td>
<td>104209.0</td>
<td>46462.23</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>50101.88</td>
<td>0.0</td>
<td>0.0</td>
<td>67684.0</td>
<td>9940339.29</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>14121.82</td>
<td>0.0</td>
<td>0.0</td>
<td>52679.0</td>
<td>10963.66</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>78292.91</td>
<td>0.0</td>
<td>0.0</td>
<td>121112.0</td>
<td>95508.95</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>176149.9</td>
<td>0.0</td>
<td>0.0</td>
<td>259813.0</td>
<td>46820.71</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>212228.35</td>
<td>0.0</td>
<td>0.0</td>
<td>429747.0</td>
<td>1178808.14</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>85423.63</td>
<td>0.0</td>
<td>0.0</td>
<td>5776776.41</td>
<td>1.916920493e7</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>11648.5</td>
<td>0.0</td>
<td>0.0</td>
<td>260976.0</td>
<td>971418.91</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>6165.58</td>
<td>20925.0</td>
<td>14759.42</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>3705.83</td>
<td>41903.46</td>
<td>38197.63</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>419801.4</td>
<td>38197.63</td>
<td>0.0</td>
<td>499962.0</td>
<td>1517262.16</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>335416.51</td>
<td>144478.0</td>
<td>0.0</td>
<td>295.0</td>
<td>52415.15</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>3372.29</td>
<td>41398.0</td>
<td>38025.71</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>661.43</td>
<td>14078.0</td>
<td>13416.57</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>DEBIT</td>
<td>864.68</td>
<td>69836.0</td>
<td>68971.32</td>
<td>12040.0</td>
<td>43691.09</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>1203.44</td>
<td>29941.0</td>
<td>28737.56</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>330757.04</td>
<td>103657.0</td>
<td>0.0</td>
<td>79676.0</td>
<td>1254956.07</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>1915.43</td>
<td>11450.0</td>
<td>9534.57</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>8.73</td>
<td>81313.0</td>
<td>81304.27</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>DEBIT</td>
<td>3058.8</td>
<td>18138.0</td>
<td>15079.2</td>
<td>11054.0</td>
<td>8917.54</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>5154.97</td>
<td>9476.0</td>
<td>4321.03</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>5213.25</td>
<td>36380.0</td>
<td>31166.75</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>5392.75</td>
<td>50757.0</td>
<td>45364.25</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>25.12</td>
<td>61663.0</td>
<td>61637.88</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>17150.89</td>
<td>61637.88</td>
<td>44486.99</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>679502.24</td>
<td>290.0</td>
<td>0.0</td>
<td>171866.0</td>
<td>3940085.21</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>177652.91</td>
<td>23720.0</td>
<td>0.0</td>
<td>55.0</td>
<td>4894.45</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>5443.26</td>
<td>236997.0</td>
<td>231553.74</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>244.87</td>
<td>257978.49</td>
<td>257733.62</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>8281.57</td>
<td>257733.62</td>
<td>249452.05</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>1175.59</td>
<td>20509.0</td>
<td>19333.41</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>1097.74</td>
<td>24866.0</td>
<td>23768.26</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>184986.8</td>
<td>39588.0</td>
<td>0.0</td>
<td>52340.0</td>
<td>97263.78</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>1718.29</td>
<td>117811.0</td>
<td>116092.71</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>3671.32</td>
<td>100094.0</td>
<td>96422.68</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>DEBIT</td>
<td>3580.26</td>
<td>2565.0</td>
<td>0.0</td>
<td>21185.0</td>
<td>18910.85</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>8550.9</td>
<td>40060.0</td>
<td>31509.1</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>9753.55</td>
<td>60829.0</td>
<td>51075.45</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>3026.98</td>
<td>31139.0</td>
<td>28112.02</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>467177.03</td>
<td>28112.02</td>
<td>0.0</td>
<td>975121.0</td>
<td>22190.99</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>DEBIT</td>
<td>3875.99</td>
<td>259138.0</td>
<td>255262.01</td>
<td>608925.82</td>
<td>2415.16</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>2148.89</td>
<td>31213.0</td>
<td>29064.11</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>466.97</td>
<td>80030.0</td>
<td>79563.03</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>154139.72</td>
<td>79563.03</td>
<td>0.0</td>
<td>410433.04</td>
<td>1254956.07</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>82685.93</td>
<td>0.0</td>
<td>0.0</td>
<td>833216.44</td>
<td>3420103.09</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>83857.23</td>
<td>0.0</td>
<td>0.0</td>
<td>215457.59</td>
<td>16997.22</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>99109.77</td>
<td>0.0</td>
<td>0.0</td>
<td>299314.82</td>
<td>16997.22</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>187726.67</td>
<td>0.0</td>
<td>0.0</td>
<td>190573.0</td>
<td>1567434.81</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>281339.92</td>
<td>0.0</td>
<td>0.0</td>
<td>612801.81</td>
<td>2415.16</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>186447.51</td>
<td>0.0</td>
<td>0.0</td>
<td>5862200.03</td>
<td>1.916920493e7</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>81029.86</td>
<td>0.0</td>
<td>0.0</td>
<td>105343.0</td>
<td>8496.61</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>139054.95</td>
<td>0.0</td>
<td>0.0</td>
<td>182538.03</td>
<td>277515.05</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>154716.2</td>
<td>0.0</td>
<td>0.0</td>
<td>187433.0</td>
<td>3461666.05</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>53631.83</td>
<td>0.0</td>
<td>0.0</td>
<td>83244.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>289645.52</td>
<td>0.0</td>
<td>0.0</td>
<td>871442.79</td>
<td>1412484.09</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>267148.82</td>
<td>0.0</td>
<td>0.0</td>
<td>641975.35</td>
<td>1178808.14</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>47913.58</td>
<td>0.0</td>
<td>0.0</td>
<td>51304.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>193605.38</td>
<td>0.0</td>
<td>0.0</td>
<td>249452.05</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>344464.4</td>
<td>0.0</td>
<td>0.0</td>
<td>1133312.56</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>169409.87</td>
<td>0.0</td>
<td>0.0</td>
<td>909124.16</td>
<td>1178808.14</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>25793.74</td>
<td>0.0</td>
<td>0.0</td>
<td>215312.31</td>
<td>49864.36</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>114712.48</td>
<td>0.0</td>
<td>0.0</td>
<td>145400.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>89038.75</td>
<td>0.0</td>
<td>0.0</td>
<td>189808.0</td>
<td>1186556.81</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>1341.58</td>
<td>0.0</td>
<td>0.0</td>
<td>80341.0</td>
<td>557537.26</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>44443.08</td>
<td>0.0</td>
<td>0.0</td>
<td>6048647.54</td>
<td>1.916920493e7</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>34729.64</td>
<td>0.0</td>
<td>0.0</td>
<td>44853.0</td>
<td>96.88</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>105979.34</td>
<td>0.0</td>
<td>0.0</td>
<td>435962.9</td>
<td>46820.71</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>57794.61</td>
<td>0.0</td>
<td>0.0</td>
<td>99217.58</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>15918.79</td>
<td>0.0</td>
<td>0.0</td>
<td>24553.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>19162.08</td>
<td>0.0</td>
<td>0.0</td>
<td>60169.0</td>
<td>215851.28</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>220691.42</td>
<td>0.0</td>
<td>0.0</td>
<td>6093090.62</td>
<td>1.916920493e7</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>9536.54</td>
<td>0.0</td>
<td>0.0</td>
<td>49161.0</td>
<td>130747.56</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>29784.06</td>
<td>0.0</td>
<td>0.0</td>
<td>63553.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>190445.51</td>
<td>0.0</td>
<td>0.0</td>
<td>203679.0</td>
<td>2107965.39</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>57279.11</td>
<td>0.0</td>
<td>0.0</td>
<td>127206.9</td>
<td>64106.18</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>112086.81</td>
<td>0.0</td>
<td>0.0</td>
<td>292728.0</td>
<td>55974.56</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>80311.89</td>
<td>0.0</td>
<td>0.0</td>
<td>87408.0</td>
<td>63112.23</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>151829.91</td>
<td>0.0</td>
<td>0.0</td>
<td>1161088.31</td>
<td>1412484.09</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>26004.52</td>
<td>0.0</td>
<td>0.0</td>
<td>52251.58</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>3146.16</td>
<td>0.0</td>
<td>0.0</td>
<td>9471.0</td>
<td>593737.38</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>296752.75</td>
<td>0.0</td>
<td>0.0</td>
<td>404814.81</td>
<td>55974.56</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>191702.06</td>
<td>0.0</td>
<td>0.0</td>
<td>272624.5</td>
<td>971418.91</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>365510.05</td>
<td>0.0</td>
<td>0.0</td>
<td>564572.76</td>
<td>1254956.07</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>202332.08</td>
<td>0.0</td>
<td>0.0</td>
<td>342149.2</td>
<td>3461666.05</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>55105.9</td>
<td>0.0</td>
<td>0.0</td>
<td>317952.89</td>
<td>2719172.89</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>171508.59</td>
<td>0.0</td>
<td>0.0</td>
<td>443057.43</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>178439.26</td>
<td>0.0</td>
<td>0.0</td>
<td>278846.75</td>
<td>1186556.81</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>88834.86</td>
<td>0.0</td>
<td>0.0</td>
<td>464326.57</td>
<td>971418.91</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>210370.09</td>
<td>0.0</td>
<td>0.0</td>
<td>1442298.03</td>
<td>22190.99</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>36437.06</td>
<td>0.0</td>
<td>0.0</td>
<td>154606.0</td>
<td>1363368.51</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>82691.56</td>
<td>0.0</td>
<td>0.0</td>
<td>657983.89</td>
<td>6453430.91</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>338767.1</td>
<td>0.0</td>
<td>0.0</td>
<td>544481.28</td>
<td>3461666.05</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>187728.59</td>
<td>0.0</td>
<td>0.0</td>
<td>394124.51</td>
<td>2107965.39</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>271002.7</td>
<td>0.0</td>
<td>0.0</td>
<td>851368.24</td>
<td>3940085.21</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>323105.08</td>
<td>0.0</td>
<td>0.0</td>
<td>378299.67</td>
<td>1567434.81</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>10782.94</td>
<td>0.0</td>
<td>0.0</td>
<td>100585.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>89962.11</td>
<td>0.0</td>
<td>0.0</td>
<td>157012.19</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>101940.14</td>
<td>0.0</td>
<td>0.0</td>
<td>105362.0</td>
<td>92307.65</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>163100.34</td>
<td>0.0</td>
<td>0.0</td>
<td>1078534.03</td>
<td>1178808.14</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>136030.66</td>
<td>0.0</td>
<td>0.0</td>
<td>136875.83</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>68912.23</td>
<td>0.0</td>
<td>0.0</td>
<td>237326.8</td>
<td>97263.78</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>73464.06</td>
<td>0.0</td>
<td>0.0</td>
<td>444779.0</td>
<td>4619798.56</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>106117.28</td>
<td>0.0</td>
<td>0.0</td>
<td>234216.94</td>
<td>51513.44</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>503333.79</td>
<td>0.0</td>
<td>0.0</td>
<td>553161.42</td>
<td>971418.91</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>71991.42</td>
<td>0.0</td>
<td>0.0</td>
<td>81682.58</td>
<td>557537.26</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>91379.93</td>
<td>0.0</td>
<td>0.0</td>
<td>1241634.38</td>
<td>1178808.14</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>26759.05</td>
<td>0.0</td>
<td>0.0</td>
<td>581853.1</td>
<td>2107965.39</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>128487.35</td>
<td>0.0</td>
<td>0.0</td>
<td>701404.75</td>
<td>1567434.81</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>252908.87</td>
<td>0.0</td>
<td>0.0</td>
<td>919763.4</td>
<td>1517262.16</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>64489.64</td>
<td>0.0</td>
<td>0.0</td>
<td>398424.59</td>
<td>16997.22</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>171369.73</td>
<td>0.0</td>
<td>0.0</td>
<td>1122370.94</td>
<td>3940085.21</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>4493.29</td>
<td>8258.0</td>
<td>3764.71</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>12121.5</td>
<td>97058.0</td>
<td>84936.5</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>2278.19</td>
<td>290.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>7707.44</td>
<td>99827.0</td>
<td>92119.56</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>4013.02</td>
<td>92119.56</td>
<td>88106.55</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>5775.15</td>
<td>132329.0</td>
<td>126553.85</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>4843.93</td>
<td>51718.0</td>
<td>46874.07</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>2397.82</td>
<td>20421.0</td>
<td>18023.18</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>4391.44</td>
<td>228.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>DEBIT</td>
<td>4497.87</td>
<td>107388.0</td>
<td>102890.13</td>
<td>19053.0</td>
<td>5417.23</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>7452.89</td>
<td>102397.0</td>
<td>94944.11</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>3930.81</td>
<td>6032.0</td>
<td>2101.19</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>71532.24</td>
<td>2101.19</td>
<td>0.0</td>
<td>74956.0</td>
<td>97128.19</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>61159.51</td>
<td>0.0</td>
<td>0.0</td>
<td>340334.22</td>
<td>51513.44</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>12401.9</td>
<td>0.0</td>
<td>0.0</td>
<td>349763.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>32702.61</td>
<td>0.0</td>
<td>0.0</td>
<td>253053.0</td>
<td>31469.78</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>6926.67</td>
<td>152927.0</td>
<td>146000.33</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>893.57</td>
<td>10676.0</td>
<td>9782.43</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>480222.51</td>
<td>11110.0</td>
<td>0.0</td>
<td>1293740.67</td>
<td>3940085.21</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>3608.41</td>
<td>11150.0</td>
<td>7541.59</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>5946.79</td>
<td>7541.59</td>
<td>1594.8</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>6099.96</td>
<td>42133.0</td>
<td>36033.04</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>372.34</td>
<td>65254.0</td>
<td>64881.66</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>4635.18</td>
<td>6313782.05</td>
<td>6309146.87</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>1267.97</td>
<td>6309146.87</td>
<td>6307878.9</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>6911.99</td>
<td>6307878.9</td>
<td>6300966.92</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>1795.67</td>
<td>6300966.92</td>
<td>6299171.25</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>3199.06</td>
<td>6299171.25</td>
<td>6295972.18</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>10265.17</td>
<td>6295972.18</td>
<td>6285707.02</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>9029.12</td>
<td>25480.0</td>
<td>16450.88</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>7600.57</td>
<td>50747.0</td>
<td>43146.43</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>2057.71</td>
<td>25132.0</td>
<td>23074.29</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>3823.08</td>
<td>10382.0</td>
<td>6558.92</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>2806.0</td>
<td>2806.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>1.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>2806.0</td>
<td>2806.0</td>
<td>0.0</td>
<td>26202.0</td>
<td>0.0</td>
<td>1.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>6737.2</td>
<td>624.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>9161.98</td>
<td>11128.0</td>
<td>1966.02</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>1239.06</td>
<td>396386.0</td>
<td>395146.94</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>365.0</td>
<td>319.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>5237.54</td>
<td>27105.0</td>
<td>21867.46</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>2618.76</td>
<td>21867.46</td>
<td>19248.7</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>339.82</td>
<td>12076.0</td>
<td>11736.18</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>DEBIT</td>
<td>1393.46</td>
<td>100068.0</td>
<td>98674.54</td>
<td>26446.0</td>
<td>318637.36</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>2731.37</td>
<td>17540.0</td>
<td>14808.63</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>8364.73</td>
<td>81.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>6598.71</td>
<td>16392.0</td>
<td>9793.29</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>4304.58</td>
<td>10708.0</td>
<td>6403.42</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>2867.14</td>
<td>943.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>2946.38</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>507.12</td>
<td>87005.0</td>
<td>86497.88</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>3020.86</td>
<td>50361.0</td>
<td>47340.14</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>8948.03</td>
<td>11137.0</td>
<td>2188.97</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>280877.92</td>
<td>2188.97</td>
<td>0.0</td>
<td>462914.23</td>
<td>16997.22</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>1084.9</td>
<td>13727.0</td>
<td>12642.1</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>9456.69</td>
<td>12642.1</td>
<td>3185.41</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>5812.36</td>
<td>3185.41</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>157883.07</td>
<td>46547.0</td>
<td>0.0</td>
<td>72793.0</td>
<td>353532.56</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>2734.77</td>
<td>4511.0</td>
<td>1776.23</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>102153.86</td>
<td>1776.23</td>
<td>0.0</td>
<td>230676.07</td>
<td>353532.56</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>584.52</td>
<td>15479.0</td>
<td>14894.48</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>6113.14</td>
<td>15629.0</td>
<td>9515.86</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>369989.2</td>
<td>9515.86</td>
<td>0.0</td>
<td>518243.06</td>
<td>4619798.56</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>215338.28</td>
<td>0.0</td>
<td>0.0</td>
<td>285755.61</td>
<td>31469.78</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>65521.42</td>
<td>0.0</td>
<td>0.0</td>
<td>93337.06</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>116977.58</td>
<td>0.0</td>
<td>0.0</td>
<td>191043.06</td>
<td>1363368.51</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>262392.36</td>
<td>0.0</td>
<td>0.0</td>
<td>457286.01</td>
<td>1186556.81</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>51121.05</td>
<td>0.0</td>
<td>0.0</td>
<td>308020.64</td>
<td>1363368.51</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>202624.59</td>
<td>0.0</td>
<td>0.0</td>
<td>1056495.21</td>
<td>971418.91</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>118235.16</td>
<td>0.0</td>
<td>0.0</td>
<td>332829.93</td>
<td>353532.56</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>118056.71</td>
<td>0.0</td>
<td>0.0</td>
<td>272906.49</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>498960.91</td>
<td>0.0</td>
<td>0.0</td>
<td>608612.15</td>
<td>2107965.39</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>691738.36</td>
<td>0.0</td>
<td>0.0</td>
<td>6285707.02</td>
<td>1.916920493e7</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>58820.08</td>
<td>0.0</td>
<td>0.0</td>
<td>1107573.06</td>
<td>2107965.39</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>4419.04</td>
<td>52951.0</td>
<td>48531.96</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>4270.02</td>
<td>9516.0</td>
<td>5245.98</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>3007.83</td>
<td>49905.0</td>
<td>46897.17</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>1824.59</td>
<td>25138.0</td>
<td>23313.41</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>993.46</td>
<td>19448.0</td>
<td>18454.54</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>302693.14</td>
<td>18454.54</td>
<td>0.0</td>
<td>306239.03</td>
<td>97263.78</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>1445.06</td>
<td>10892.0</td>
<td>9446.94</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>4059.25</td>
<td>4186.0</td>
<td>126.75</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>3417.18</td>
<td>126.75</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>DEBIT</td>
<td>3428.95</td>
<td>147798.0</td>
<td>144369.05</td>
<td>1534657.01</td>
<td>5515763.34</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>5580.98</td>
<td>7781.0</td>
<td>2200.02</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>DEBIT</td>
<td>3455.85</td>
<td>3509.0</td>
<td>53.15</td>
<td>1333014.31</td>
<td>1178808.14</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>7265.63</td>
<td>14508.0</td>
<td>7242.37</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>3852.64</td>
<td>1449076.27</td>
<td>1445223.63</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>7870.29</td>
<td>1445223.63</td>
<td>1437353.34</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>DEBIT</td>
<td>12872.92</td>
<td>11197.0</td>
<td>0.0</td>
<td>51885.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>6675.34</td>
<td>10107.0</td>
<td>3431.66</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>410201.89</td>
<td>21448.0</td>
<td>0.0</td>
<td>61613.0</td>
<td>424250.45</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>13310.78</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>664.35</td>
<td>102983.0</td>
<td>102318.65</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>2130.3</td>
<td>10958.0</td>
<td>8827.7</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>7771.4</td>
<td>8827.7</td>
<td>1056.3</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>36349.35</td>
<td>1056.3</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>749.34</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>2340.34</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>9686.85</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>15785.85</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>9006.78</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>9014.95</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>14420.66</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>4802.31</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>28527.44</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>4182.93</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>4088.21</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>8491.9</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>DEBIT</td>
<td>6666.78</td>
<td>11876.0</td>
<td>5209.22</td>
<td>24777.0</td>
<td>189534.74</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>6536.86</td>
<td>17319.0</td>
<td>10782.14</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>40684.97</td>
<td>10782.14</td>
<td>0.0</td>
<td>49530.0</td>
<td>66575.5</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>1553.21</td>
<td>31516.0</td>
<td>29962.79</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>DEBIT</td>
<td>7344.92</td>
<td>0.0</td>
<td>0.0</td>
<td>185432.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>106267.21</td>
<td>0.0</td>
<td>0.0</td>
<td>241106.05</td>
<td>49864.36</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>5491.5</td>
<td>36605.0</td>
<td>31113.5</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>3666.42</td>
<td>48117.0</td>
<td>44450.58</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>5507.18</td>
<td>36030.0</td>
<td>30522.82</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>DEBIT</td>
<td>4565.14</td>
<td>30265.0</td>
<td>25699.86</td>
<td>829892.09</td>
<td>1567434.81</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>11348.71</td>
<td>734899.0</td>
<td>723550.29</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>123179.97</td>
<td>7215.0</td>
<td>0.0</td>
<td>12904.68</td>
<td>43691.09</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>7073.6</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>2803.83</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>106.81</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>6297.71</td>
<td>762.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>2610.01</td>
<td>58697.54</td>
<td>56087.54</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>5320.1</td>
<td>56087.54</td>
<td>50767.43</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>1460.63</td>
<td>1132.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>522264.81</td>
<td>24.0</td>
<td>0.0</td>
<td>11361.0</td>
<td>2025098.66</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>DEBIT</td>
<td>3584.21</td>
<td>135496.0</td>
<td>131911.79</td>
<td>100614.05</td>
<td>24044.18</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>3704.97</td>
<td>24553.0</td>
<td>20848.03</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>2773.61</td>
<td>14332.0</td>
<td>11558.39</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>3896.42</td>
<td>11558.39</td>
<td>7661.97</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>18781.84</td>
<td>7661.97</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>6660.33</td>
<td>17710.0</td>
<td>11049.67</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>26923.42</td>
<td>11049.67</td>
<td>0.0</td>
<td>199404.91</td>
<td>95508.95</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>53783.57</td>
<td>0.0</td>
<td>0.0</td>
<td>75514.0</td>
<td>420946.86</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>80333.28</td>
<td>0.0</td>
<td>0.0</td>
<td>321592.98</td>
<td>277515.05</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>211075.91</td>
<td>0.0</td>
<td>0.0</td>
<td>930082.81</td>
<td>1254956.07</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>100677.86</td>
<td>0.0</td>
<td>0.0</td>
<td>701567.56</td>
<td>55974.56</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>473499.51</td>
<td>0.0</td>
<td>0.0</td>
<td>883248.39</td>
<td>3461666.05</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>1538200.39</td>
<td>0.0</td>
<td>0.0</td>
<td>6977445.38</td>
<td>1.916920493e7</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>2421578.09</td>
<td>0.0</td>
<td>0.0</td>
<td>8515645.77</td>
<td>1.916920493e7</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>1349670.68</td>
<td>0.0</td>
<td>0.0</td>
<td>1538085.96</td>
<td>5515763.34</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>314403.14</td>
<td>0.0</td>
<td>0.0</td>
<td>888232.25</td>
<td>4619798.56</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>1457213.54</td>
<td>0.0</td>
<td>0.0</td>
<td>1.093722386e7</td>
<td>1.916920493e7</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>180368.84</td>
<td>0.0</td>
<td>0.0</td>
<td>1437353.34</td>
<td>16518.36</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>445038.71</td>
<td>0.0</td>
<td>0.0</td>
<td>802245.42</td>
<td>55974.56</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>1123206.66</td>
<td>0.0</td>
<td>0.0</td>
<td>1773963.18</td>
<td>3940085.21</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>DEBIT</td>
<td>996.0</td>
<td>46132.0</td>
<td>45136.0</td>
<td>88106.55</td>
<td>1015132.48</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>176334.26</td>
<td>45136.0</td>
<td>0.0</td>
<td>1251626.46</td>
<td>2107778.11</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>161217.61</td>
<td>0.0</td>
<td>0.0</td>
<td>199733.0</td>
<td>5602234.95</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>108443.61</td>
<td>0.0</td>
<td>0.0</td>
<td>246974.3</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>75442.68</td>
<td>0.0</td>
<td>0.0</td>
<td>80469.0</td>
<td>500631.71</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>55536.86</td>
<td>0.0</td>
<td>0.0</td>
<td>915902.37</td>
<td>3420103.09</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>438437.09</td>
<td>0.0</td>
<td>0.0</td>
<td>740675.45</td>
<td>6453430.91</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>928722.69</td>
<td>0.0</td>
<td>0.0</td>
<td>1259119.8</td>
<td>971418.91</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>1478772.04</td>
<td>0.0</td>
<td>0.0</td>
<td>1652668.12</td>
<td>22190.99</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>2545478.01</td>
<td>0.0</td>
<td>0.0</td>
<td>1.23944374e7</td>
<td>1.916920493e7</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>2061082.82</td>
<td>0.0</td>
<td>0.0</td>
<td>1.493991542e7</td>
<td>1.916920493e7</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>983479.66</td>
<td>0.0</td>
<td>0.0</td>
<td>3131440.16</td>
<td>22190.99</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>635507.97</td>
<td>0.0</td>
<td>0.0</td>
<td>834457.23</td>
<td>1567434.81</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>848231.57</td>
<td>0.0</td>
<td>0.0</td>
<td>4114919.82</td>
<td>22190.99</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>739112.93</td>
<td>0.0</td>
<td>0.0</td>
<td>1141158.73</td>
<td>1254956.07</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>324397.94</td>
<td>0.0</td>
<td>0.0</td>
<td>373058.79</td>
<td>2719172.89</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>982862.81</td>
<td>0.0</td>
<td>0.0</td>
<td>2187842.49</td>
<td>971418.91</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>955855.0</td>
<td>0.0</td>
<td>0.0</td>
<td>1179112.54</td>
<td>6453430.91</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>179253.24</td>
<td>0.0</td>
<td>0.0</td>
<td>179614.1</td>
<td>46462.23</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>5446.88</td>
<td>666.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>648.64</td>
<td>873.0</td>
<td>224.36</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>8494.71</td>
<td>11129.0</td>
<td>2634.29</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>373068.26</td>
<td>20034.0</td>
<td>0.0</td>
<td>1427960.73</td>
<td>2107778.11</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>143236.26</td>
<td>0.0</td>
<td>143236.26</td>
<td>608932.17</td>
<td>97263.78</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>228451.89</td>
<td>143236.26</td>
<td>371688.15</td>
<td>719678.38</td>
<td>1186556.81</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>35902.49</td>
<td>371688.15</td>
<td>407590.65</td>
<td>49003.3</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>232953.64</td>
<td>407590.65</td>
<td>640544.28</td>
<td>1172672.27</td>
<td>1517262.16</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>65912.95</td>
<td>640544.28</td>
<td>706457.23</td>
<td>104198.26</td>
<td>24044.18</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>193492.68</td>
<td>706457.23</td>
<td>899949.91</td>
<td>1247284.13</td>
<td>55974.56</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>60836.64</td>
<td>899949.91</td>
<td>960786.56</td>
<td>143436.0</td>
<td>5203.54</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>62325.15</td>
<td>960786.56</td>
<td>1023111.71</td>
<td>1880271.66</td>
<td>1254956.07</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>349640.51</td>
<td>1023111.71</td>
<td>1372752.22</td>
<td>360950.61</td>
<td>5602234.95</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>135324.19</td>
<td>1372752.22</td>
<td>1508076.41</td>
<td>1356747.89</td>
<td>3461666.05</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>380015.14</td>
<td>1508076.41</td>
<td>1888091.55</td>
<td>1336470.16</td>
<td>1178808.14</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>418688.27</td>
<td>1888091.55</td>
<td>2306779.82</td>
<td>956455.03</td>
<td>1178808.14</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>311023.52</td>
<td>2306779.82</td>
<td>2617803.34</td>
<td>1477776.96</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>151862.38</td>
<td>2617803.34</td>
<td>2769665.72</td>
<td>158858.48</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>228710.05</td>
<td>2769665.72</td>
<td>2998375.78</td>
<td>743792.14</td>
<td>16997.22</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>220431.09</td>
<td>2998375.78</td>
<td>3218806.87</td>
<td>2897169.84</td>
<td>3940085.21</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>182861.45</td>
<td>3218806.87</td>
<td>3401668.32</td>
<td>192776.92</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>231881.5</td>
<td>3401668.32</td>
<td>3633549.82</td>
<td>894141.72</td>
<td>2415.16</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>38071.17</td>
<td>3633549.82</td>
<td>3671620.98</td>
<td>122487.0</td>
<td>54985.05</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>87117.29</td>
<td>3671620.98</td>
<td>3758738.27</td>
<td>501093.89</td>
<td>31469.78</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>114271.72</td>
<td>3758738.27</td>
<td>3873009.99</td>
<td>3170705.3</td>
<td>971418.91</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>73199.1</td>
<td>3873009.99</td>
<td>3946209.08</td>
<td>401926.25</td>
<td>277515.05</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>764772.98</td>
<td>3946209.08</td>
<td>4710982.07</td>
<td>3056433.58</td>
<td>971418.91</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>106847.36</td>
<td>4710982.07</td>
<td>4817829.42</td>
<td>335711.51</td>
<td>52415.15</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>6287.28</td>
<td>4817829.42</td>
<td>4824116.71</td>
<td>11274.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>24936.34</td>
<td>4824116.71</td>
<td>4849053.05</td>
<td>104408.0</td>
<td>42450.71</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>55837.16</td>
<td>4849053.05</td>
<td>4904890.21</td>
<td>66800.82</td>
<td>10963.66</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>63255.29</td>
<td>4904890.21</td>
<td>4968145.5</td>
<td>237735.3</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>104605.78</td>
<td>4968145.5</td>
<td>5072751.28</td>
<td>207302.14</td>
<td>92307.65</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>154160.82</td>
<td>5072751.28</td>
<td>5226912.1</td>
<td>157376.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>257348.03</td>
<td>5226912.1</td>
<td>5484260.13</td>
<td>2291660.6</td>
<td>971418.91</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>201073.81</td>
<td>5484260.13</td>
<td>5685333.94</td>
<td>537766.76</td>
<td>1178808.14</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>53560.68</td>
<td>5685333.94</td>
<td>5738894.62</td>
<td>172857.68</td>
<td>22233.65</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>334234.39</td>
<td>5738894.62</td>
<td>6073129.01</td>
<td>358867.35</td>
<td>46462.23</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>227335.16</td>
<td>6073129.01</td>
<td>6300464.17</td>
<td>1817946.5</td>
<td>1254956.07</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>134426.08</td>
<td>6300464.17</td>
<td>6434890.26</td>
<td>228864.16</td>
<td>52415.15</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>2643.45</td>
<td>6434890.26</td>
<td>6437533.71</td>
<td>49974.0</td>
<td>1891.79</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>134977.11</td>
<td>6437533.71</td>
<td>6572510.82</td>
<td>471814.89</td>
<td>424250.45</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>25090.03</td>
<td>6572510.82</td>
<td>6597600.85</td>
<td>80148.6</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>147388.67</td>
<td>6597600.85</td>
<td>6744989.53</td>
<td>184486.01</td>
<td>64106.18</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>217615.09</td>
<td>6744989.53</td>
<td>6962604.61</td>
<td>971439.24</td>
<td>3420103.09</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>355294.5</td>
<td>6962604.61</td>
<td>7317899.11</td>
<td>1469965.2</td>
<td>1567434.81</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>12336.48</td>
<td>7317899.11</td>
<td>7330235.59</td>
<td>21024.0</td>
<td>83845.22</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>349505.89</td>
<td>7330235.59</td>
<td>7679741.48</td>
<td>1.700099823e7</td>
<td>1.916920493e7</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>285185.34</td>
<td>7679741.48</td>
<td>7964926.82</td>
<td>451065.09</td>
<td>353532.56</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>132953.45</td>
<td>7964926.82</td>
<td>8097880.27</td>
<td>1617722.17</td>
<td>16518.36</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>214851.41</td>
<td>8097880.27</td>
<td>8312731.68</td>
<td>939718.63</td>
<td>1517262.16</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>54988.56</td>
<td>8312731.68</td>
<td>8367720.25</td>
<td>60870.0</td>
<td>1.058888527e7</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>62871.06</td>
<td>8367720.25</td>
<td>8430591.3</td>
<td>177707.91</td>
<td>4894.45</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>61150.85</td>
<td>8430591.3</td>
<td>8491742.16</td>
<td>1166393.13</td>
<td>2107965.39</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>131409.27</td>
<td>8491742.16</td>
<td>8623151.43</td>
<td>290276.03</td>
<td>265092.36</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>10844.33</td>
<td>8623151.43</td>
<td>8633995.76</td>
<td>42274.0</td>
<td>290772.6</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>2054.84</td>
<td>22195.0</td>
<td>20140.16</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>313373.56</td>
<td>20140.16</td>
<td>333513.72</td>
<td>2034312.57</td>
<td>971418.91</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>13289.87</td>
<td>333513.72</td>
<td>346803.59</td>
<td>78256.1</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>27070.11</td>
<td>346803.59</td>
<td>373873.7</td>
<td>70595.0</td>
<td>122750.49</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>127954.7</td>
<td>373873.7</td>
<td>501828.4</td>
<td>1590611.34</td>
<td>1254956.07</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>197031.04</td>
<td>501828.4</td>
<td>698859.44</td>
<td>515082.09</td>
<td>16997.22</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>144366.64</td>
<td>698859.44</td>
<td>843226.08</td>
<td>1720939.01</td>
<td>971418.91</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>7965.49</td>
<td>843226.08</td>
<td>851191.57</td>
<td>35100.09</td>
<td>40348.79</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>123103.74</td>
<td>851191.57</td>
<td>974295.31</td>
<td>541942.24</td>
<td>46820.71</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>106634.77</td>
<td>974295.31</td>
<td>1080930.07</td>
<td>4963151.39</td>
<td>22190.99</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>74933.22</td>
<td>1080930.07</td>
<td>1155863.3</td>
<td>1312918.22</td>
<td>1412484.09</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>120234.99</td>
<td>1155863.3</td>
<td>1276098.29</td>
<td>194026.33</td>
<td>965870.05</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>5763.99</td>
<td>1276098.29</td>
<td>1281862.28</td>
<td>24632.95</td>
<td>46462.23</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>156922.36</td>
<td>1281862.28</td>
<td>1438784.65</td>
<td>2887756.64</td>
<td>5515763.34</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>162665.98</td>
<td>1438784.65</td>
<td>1601450.63</td>
<td>362164.9</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>110226.34</td>
<td>1601450.63</td>
<td>1601450.63</td>
<td>1601450.63</td>
<td>1.068123879e7</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>142577.44</td>
<td>1601450.63</td>
<td>1744028.07</td>
<td>174480.01</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>6915.38</td>
<td>1744028.07</td>
<td>1750943.45</td>
<td>49685.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>157108.01</td>
<td>1750943.45</td>
<td>1908051.46</td>
<td>1576572.37</td>
<td>971418.91</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>223555.14</td>
<td>1908051.46</td>
<td>2131606.6</td>
<td>1462656.64</td>
<td>1254956.07</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>146331.12</td>
<td>2131606.6</td>
<td>2277937.72</td>
<td>1053791.45</td>
<td>55974.56</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>141131.23</td>
<td>2277937.72</td>
<td>2419068.96</td>
<td>1419464.36</td>
<td>971418.91</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>222711.47</td>
<td>2419068.96</td>
<td>2641780.43</td>
<td>1.665149234e7</td>
<td>1.916920493e7</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>84980.31</td>
<td>2641780.43</td>
<td>2726760.74</td>
<td>165879.75</td>
<td>353532.56</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>628719.07</td>
<td>2726760.74</td>
<td>3355479.81</td>
<td>1484768.73</td>
<td>16518.36</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>79285.8</td>
<td>3355479.81</td>
<td>3434765.61</td>
<td>4856516.62</td>
<td>22190.99</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>57809.81</td>
<td>3434765.61</td>
<td>3492575.42</td>
<td>59666.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>75043.17</td>
<td>3492575.42</td>
<td>3567618.59</td>
<td>156547.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>248196.92</td>
<td>3567618.59</td>
<td>3815815.51</td>
<td>318051.05</td>
<td>16997.22</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>18851.44</td>
<td>3815815.51</td>
<td>3834666.95</td>
<td>47330.55</td>
<td>1891.79</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>403418.39</td>
<td>3834666.95</td>
<td>4238085.34</td>
<td>1801028.99</td>
<td>2107778.11</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>235564.81</td>
<td>4238085.34</td>
<td>4473650.15</td>
<td>2676738.75</td>
<td>3940085.21</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>32325.24</td>
<td>4473650.15</td>
<td>4505975.39</td>
<td>158866.76</td>
<td>265092.36</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>345347.7</td>
<td>4505975.39</td>
<td>4851323.08</td>
<td>355417.92</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>355500.25</td>
<td>4851323.08</td>
<td>5206823.33</td>
<td>1166753.44</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>103005.78</td>
<td>5206823.33</td>
<td>5309829.12</td>
<td>114836.85</td>
<td>4894.45</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>108803.98</td>
<td>5309829.12</td>
<td>5418633.09</td>
<td>811253.19</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>259753.27</td>
<td>5418633.09</td>
<td>5678386.36</td>
<td>260112.48</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>234094.24</td>
<td>5678386.36</td>
<td>5912480.6</td>
<td>1278333.12</td>
<td>971418.91</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>273305.73</td>
<td>5912480.6</td>
<td>6185786.33</td>
<td>1237985.0</td>
<td>1412484.09</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>186838.95</td>
<td>6185786.33</td>
<td>6372625.28</td>
<td>418838.5</td>
<td>46820.71</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>193161.04</td>
<td>6372625.28</td>
<td>6565786.32</td>
<td>2441173.94</td>
<td>3940085.21</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>152823.85</td>
<td>6565786.32</td>
<td>6718610.17</td>
<td>1202635.39</td>
<td>4619798.56</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>91457.82</td>
<td>6718610.17</td>
<td>6810067.99</td>
<td>662260.23</td>
<td>2415.16</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>277807.48</td>
<td>6810067.99</td>
<td>7087875.47</td>
<td>907460.33</td>
<td>55974.56</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>8679.13</td>
<td>7087875.47</td>
<td>7096554.61</td>
<td>94438.08</td>
<td>52415.15</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>2099.59</td>
<td>7096554.61</td>
<td>7098654.2</td>
<td>40471.79</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>221693.08</td>
<td>7098654.2</td>
<td>7320347.28</td>
<td>629652.85</td>
<td>55974.56</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>206097.65</td>
<td>7320347.28</td>
<td>7526444.93</td>
<td>1239101.5</td>
<td>1254956.07</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>22384.21</td>
<td>7526444.93</td>
<td>7548829.13</td>
<td>42211.0</td>
<td>387263.02</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>73210.99</td>
<td>7548829.13</td>
<td>7622040.12</td>
<td>79582.64</td>
<td>96.88</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>236747.6</td>
<td>7622040.12</td>
<td>7858787.73</td>
<td>390963.21</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>6284.18</td>
<td>7858787.73</td>
<td>7865071.9</td>
<td>328727.16</td>
<td>277515.05</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>100198.77</td>
<td>7865071.9</td>
<td>7965270.68</td>
<td>336837.78</td>
<td>424250.45</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>160347.32</td>
<td>7965270.68</td>
<td>8125617.99</td>
<td>724867.22</td>
<td>1517262.16</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>289272.75</td>
<td>8125617.99</td>
<td>8414890.75</td>
<td>413976.6</td>
<td>31469.78</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>63189.26</td>
<td>8414890.75</td>
<td>8478080.0</td>
<td>101925.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>196725.32</td>
<td>8478080.0</td>
<td>8674805.32</td>
<td>2248012.9</td>
<td>3940085.21</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>5882.32</td>
<td>10252.0</td>
<td>4369.68</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>DEBIT</td>
<td>625.92</td>
<td>30165.0</td>
<td>29539.08</td>
<td>1042.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>5937.72</td>
<td>21779.0</td>
<td>15841.28</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>9796.29</td>
<td>274658.0</td>
<td>264861.71</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>13449.95</td>
<td>11080.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>4749.6</td>
<td>8038.0</td>
<td>3288.4</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>6460.86</td>
<td>31009.0</td>
<td>24548.14</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>3605.26</td>
<td>10120.0</td>
<td>6514.74</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>624.61</td>
<td>52594.0</td>
<td>51969.39</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>3854.92</td>
<td>9241.0</td>
<td>5386.08</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>420532.33</td>
<td>5386.08</td>
<td>0.0</td>
<td>614566.02</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>4402.86</td>
<td>4469.0</td>
<td>66.14</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>147804.02</td>
<td>66.14</td>
<td>0.0</td>
<td>158919.0</td>
<td>23508.22</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>151.2</td>
<td>1904.0</td>
<td>1752.8</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>58.21</td>
<td>14506.0</td>
<td>14447.79</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>5936.82</td>
<td>741.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>145944.67</td>
<td>24376.0</td>
<td>0.0</td>
<td>222.0</td>
<td>46393.85</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>7206.33</td>
<td>24932.0</td>
<td>17725.67</td>
<td>21308.0</td>
<td>18161.79</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>DEBIT</td>
<td>5581.55</td>
<td>530.0</td>
<td>0.0</td>
<td>55058.57</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>DEBIT</td>
<td>5853.21</td>
<td>5039.0</td>
<td>0.0</td>
<td>20018.0</td>
<td>4891090.56</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>2528.1</td>
<td>5037.0</td>
<td>2508.9</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>5655.17</td>
<td>9522.0</td>
<td>3866.83</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>1456.09</td>
<td>101018.0</td>
<td>99561.91</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>4252.82</td>
<td>5545.0</td>
<td>1292.18</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>276460.62</td>
<td>595.0</td>
<td>0.0</td>
<td>1105242.28</td>
<td>2107965.39</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>8811.67</td>
<td>30848.0</td>
<td>22036.33</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>12666.68</td>
<td>891.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>95636.49</td>
<td>0.0</td>
<td>0.0</td>
<td>359141.7</td>
<td>1363368.51</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>195314.34</td>
<td>0.0</td>
<td>0.0</td>
<td>491226.48</td>
<td>1186556.81</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>6049.09</td>
<td>1.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>14306.8</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>1117.34</td>
<td>35611.0</td>
<td>34493.66</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>DEBIT</td>
<td>3179.7</td>
<td>124703.85</td>
<td>121524.14</td>
<td>20767.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>2880.68</td>
<td>121524.14</td>
<td>118643.46</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>3060.54</td>
<td>118643.46</td>
<td>115582.92</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>879.42</td>
<td>115582.92</td>
<td>114703.5</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>3486.78</td>
<td>114703.5</td>
<td>111216.72</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>21530.96</td>
<td>111216.72</td>
<td>89685.77</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>2426.31</td>
<td>89685.77</td>
<td>87259.46</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>5110.1</td>
<td>87259.46</td>
<td>82149.35</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>207.75</td>
<td>82149.35</td>
<td>81941.6</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>18437.04</td>
<td>81941.6</td>
<td>63504.55</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>8690.69</td>
<td>63504.55</td>
<td>54813.86</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>4946.03</td>
<td>54813.86</td>
<td>49867.83</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>5432.09</td>
<td>49867.83</td>
<td>44435.75</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>7649.41</td>
<td>44435.75</td>
<td>36786.34</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>3943.17</td>
<td>36786.34</td>
<td>32843.16</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>3101.33</td>
<td>32843.16</td>
<td>29741.84</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>51719.13</td>
<td>882.0</td>
<td>0.0</td>
<td>59670.0</td>
<td>15375.37</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>13465.91</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>19561.05</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>9762.28</td>
<td>129.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>9778.81</td>
<td>18824.0</td>
<td>9045.19</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>562903.81</td>
<td>9045.19</td>
<td>0.0</td>
<td>564519.9</td>
<td>1517262.16</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>5055.94</td>
<td>30295.0</td>
<td>25239.06</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>2858.89</td>
<td>21820.0</td>
<td>18961.11</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>8531.42</td>
<td>18961.11</td>
<td>10429.69</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>84.22</td>
<td>25828.0</td>
<td>25743.78</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>227478.01</td>
<td>25743.78</td>
<td>0.0</td>
<td>1.642878087e7</td>
<td>1.916920493e7</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>5033.02</td>
<td>23946.7</td>
<td>18913.68</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>100588.8</td>
<td>18913.68</td>
<td>0.0</td>
<td>105223.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>9374.72</td>
<td>21064.0</td>
<td>11689.28</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>598.24</td>
<td>102631.0</td>
<td>102032.76</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>4531.55</td>
<td>20125.0</td>
<td>15593.45</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>2998.2</td>
<td>15593.45</td>
<td>12595.25</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>9269.99</td>
<td>12595.25</td>
<td>3325.26</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>1209.64</td>
<td>3325.26</td>
<td>2115.61</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>1298.7</td>
<td>2115.61</td>
<td>816.91</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>7797.0</td>
<td>816.91</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>5831.03</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>7067.9</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>14895.51</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>3316.21</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>22276.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>16505.51</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>636.95</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>2056.59</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>704.74</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>3222.32</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>11889.61</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>9491.89</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>6131.21</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>11938.61</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>4233.45</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>32643.79</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>622.15</td>
<td>14112.8</td>
<td>13490.65</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>4573.12</td>
<td>13490.65</td>
<td>8917.54</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>6057.8</td>
<td>6996.1</td>
<td>938.3</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>5770.32</td>
<td>938.3</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>9152.97</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>DEBIT</td>
<td>12154.7</td>
<td>39699.0</td>
<td>27544.3</td>
<td>2051287.58</td>
<td>3940085.21</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>8378.01</td>
<td>80899.44</td>
<td>72521.43</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>9616.45</td>
<td>72521.43</td>
<td>62904.98</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>2231.28</td>
<td>12998.0</td>
<td>10766.72</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>4932.54</td>
<td>136555.0</td>
<td>131622.46</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>65413.89</td>
<td>131622.46</td>
<td>197036.35</td>
<td>144605.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>98560.08</td>
<td>197036.35</td>
<td>295596.43</td>
<td>167719.89</td>
<td>63112.23</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>15054.61</td>
<td>295596.43</td>
<td>310651.04</td>
<td>33340.0</td>
<td>185389.8</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>161632.16</td>
<td>310651.04</td>
<td>472283.2</td>
<td>1044238.89</td>
<td>971418.91</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>47529.19</td>
<td>472283.2</td>
<td>519812.39</td>
<td>856049.65</td>
<td>16518.36</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>9577.45</td>
<td>519812.39</td>
<td>529389.85</td>
<td>32204.0</td>
<td>22774.25</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>414238.59</td>
<td>529389.85</td>
<td>943628.44</td>
<td>808520.46</td>
<td>16518.36</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>89327.65</td>
<td>943628.44</td>
<td>1032956.09</td>
<td>2063442.28</td>
<td>3940085.21</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>194900.16</td>
<td>1032956.09</td>
<td>1227856.25</td>
<td>8633995.76</td>
<td>1.249436715e7</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>24470.43</td>
<td>1227856.25</td>
<td>1252326.68</td>
<td>1381702.9</td>
<td>2107965.39</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>217668.38</td>
<td>1252326.68</td>
<td>1469995.06</td>
<td>347373.26</td>
<td>49864.36</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>9067.51</td>
<td>1469995.06</td>
<td>1479062.57</td>
<td>306723.02</td>
<td>23508.22</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>336827.91</td>
<td>1479062.57</td>
<td>1815890.48</td>
<td>401493.73</td>
<td>51513.44</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>45317.95</td>
<td>1815890.48</td>
<td>1861208.43</td>
<td>99578.86</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>230021.01</td>
<td>1861208.43</td>
<td>2091229.45</td>
<td>4777230.82</td>
<td>22190.99</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>154578.28</td>
<td>2091229.45</td>
<td>2245807.73</td>
<td>1033003.86</td>
<td>1254956.07</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>314134.01</td>
<td>2245807.73</td>
<td>2559941.74</td>
<td>570802.41</td>
<td>2415.16</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>206405.92</td>
<td>2559941.74</td>
<td>2766347.65</td>
<td>1221423.7</td>
<td>3461666.05</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>158995.29</td>
<td>2766347.65</td>
<td>2925342.94</td>
<td>8674805.32</td>
<td>1.068123879e7</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>188238.54</td>
<td>2925342.94</td>
<td>3113581.47</td>
<td>702449.21</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>21255.8</td>
<td>3113581.47</td>
<td>3134837.27</td>
<td>52109.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>205955.77</td>
<td>3134837.27</td>
<td>3340793.04</td>
<td>465695.91</td>
<td>97263.78</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>178370.36</td>
<td>3340793.04</td>
<td>3519163.4</td>
<td>1.665625888e7</td>
<td>1.916920493e7</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>42547.96</td>
<td>3519163.4</td>
<td>3561711.36</td>
<td>59693.0</td>
<td>9672.67</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>15152.15</td>
<td>3561711.36</td>
<td>3576863.51</td>
<td>8439095.6</td>
<td>1.249436715e7</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>39249.67</td>
<td>3576863.51</td>
<td>3616113.18</td>
<td>64966.23</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>101669.17</td>
<td>3616113.18</td>
<td>3717782.35</td>
<td>533625.81</td>
<td>2025098.66</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>81694.11</td>
<td>3717782.35</td>
<td>3799476.47</td>
<td>686540.82</td>
<td>1186556.81</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>10266.7</td>
<td>3799476.47</td>
<td>3809743.17</td>
<td>104323.0</td>
<td>69756.86</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>35502.25</td>
<td>3809743.17</td>
<td>3845245.42</td>
<td>41159.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>183250.0</td>
<td>3845245.42</td>
<td>4028495.42</td>
<td>231999.55</td>
<td>46820.71</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>219220.21</td>
<td>4028495.42</td>
<td>4247715.63</td>
<td>1974114.63</td>
<td>3940085.21</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>254895.5</td>
<td>4247715.63</td>
<td>4502611.13</td>
<td>256668.41</td>
<td>2415.16</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>143148.97</td>
<td>4502611.13</td>
<td>4645760.1</td>
<td>4547209.8</td>
<td>22190.99</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>132457.52</td>
<td>4645760.1</td>
<td>4778217.62</td>
<td>8423943.45</td>
<td>1.249436715e7</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>148834.28</td>
<td>4778217.62</td>
<td>4927051.9</td>
<td>514210.68</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>106727.81</td>
<td>4927051.9</td>
<td>5033779.71</td>
<td>2730834.27</td>
<td>5515763.34</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>223123.71</td>
<td>5033779.71</td>
<td>5256903.42</td>
<td>394281.87</td>
<td>16518.36</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>194311.34</td>
<td>5256903.42</td>
<td>5451214.77</td>
<td>1127423.71</td>
<td>1517262.16</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>338964.91</td>
<td>5451214.77</td>
<td>5790179.68</td>
<td>365376.39</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>247575.4</td>
<td>5790179.68</td>
<td>6037755.07</td>
<td>8291485.93</td>
<td>1.249436715e7</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>29467.02</td>
<td>6037755.07</td>
<td>6067222.09</td>
<td>322442.98</td>
<td>277515.05</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>90340.16</td>
<td>6067222.09</td>
<td>6157562.25</td>
<td>1.647788852e7</td>
<td>1.916920493e7</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>198847.32</td>
<td>6157562.25</td>
<td>6356409.58</td>
<td>964679.26</td>
<td>1412484.09</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>155075.41</td>
<td>6356409.58</td>
<td>6511484.99</td>
<td>1035098.36</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>76932.89</td>
<td>6511484.99</td>
<td>6588417.88</td>
<td>765831.94</td>
<td>1412484.09</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>92971.79</td>
<td>6588417.88</td>
<td>6681389.66</td>
<td>111367.94</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>7322.98</td>
<td>6681389.66</td>
<td>6688712.64</td>
<td>64966.0</td>
<td>57643.02</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>222126.95</td>
<td>6688712.64</td>
<td>6910839.59</td>
<td>1397610.6</td>
<td>2107778.11</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>62983.91</td>
<td>6910839.59</td>
<td>6973823.5</td>
<td>454778.18</td>
<td>1363368.51</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>1271.77</td>
<td>6973823.5</td>
<td>6975095.27</td>
<td>697456.73</td>
<td>2719172.89</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>246816.65</td>
<td>6975095.27</td>
<td>7221911.92</td>
<td>933112.37</td>
<td>1517262.16</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>21898.97</td>
<td>7221911.92</td>
<td>7243810.89</td>
<td>1114670.7</td>
<td>1567434.81</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>30811.56</td>
<td>7243810.89</td>
<td>7274622.45</td>
<td>152178.0</td>
<td>651524.92</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>100275.09</td>
<td>7274622.45</td>
<td>7374897.54</td>
<td>878425.58</td>
<td>1254956.07</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>46630.53</td>
<td>7374897.54</td>
<td>7421528.07</td>
<td>604846.71</td>
<td>1186556.81</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>158775.58</td>
<td>7421528.07</td>
<td>7580303.64</td>
<td>688899.05</td>
<td>1412484.09</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>90556.08</td>
<td>7580303.64</td>
<td>7670859.72</td>
<td>186372.86</td>
<td>8496.61</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>173948.89</td>
<td>7670859.72</td>
<td>7844808.61</td>
<td>391794.27</td>
<td>1363368.51</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>66216.71</td>
<td>7844808.61</td>
<td>7911025.32</td>
<td>117785.88</td>
<td>9940339.29</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>216133.99</td>
<td>7911025.32</td>
<td>8127159.31</td>
<td>1092771.73</td>
<td>1567434.81</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>371883.82</td>
<td>8127159.31</td>
<td>8499043.13</td>
<td>880022.94</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>770537.37</td>
<td>8499043.13</td>
<td>8499043.13</td>
<td>8499043.13</td>
<td>1.687464309e7</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>71275.47</td>
<td>8499043.13</td>
<td>8570318.6</td>
<td>136084.66</td>
<td>43691.09</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>336298.77</td>
<td>8570318.6</td>
<td>8906617.38</td>
<td>1049811.54</td>
<td>4619798.56</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>317393.38</td>
<td>8906617.38</td>
<td>9224010.75</td>
<td>1175483.65</td>
<td>2107778.11</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>181002.01</td>
<td>9224010.75</td>
<td>9405012.76</td>
<td>292975.96</td>
<td>277515.05</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>311449.38</td>
<td>9405012.76</td>
<td>9716462.14</td>
<td>713512.77</td>
<td>4619798.56</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>270824.42</td>
<td>9716462.14</td>
<td>9987286.56</td>
<td>8515810.04</td>
<td>1.068123879e7</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>1449.03</td>
<td>80983.0</td>
<td>79533.97</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>3551.48</td>
<td>1015017.79</td>
<td>1011466.31</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>227768.63</td>
<td>1011466.31</td>
<td>783697.68</td>
<td>530123.48</td>
<td>1412484.09</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>172986.7</td>
<td>783697.68</td>
<td>610710.98</td>
<td>686295.71</td>
<td>1517262.16</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>4940.2</td>
<td>48005.0</td>
<td>43064.8</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>2755.96</td>
<td>20987.0</td>
<td>18231.04</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>DEBIT</td>
<td>8807.01</td>
<td>139428.0</td>
<td>130620.99</td>
<td>64809.18</td>
<td>43691.09</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>DEBIT</td>
<td>3111.86</td>
<td>64665.82</td>
<td>61553.96</td>
<td>10070.22</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>10040.52</td>
<td>61553.96</td>
<td>51513.44</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>DEBIT</td>
<td>5363.0</td>
<td>21260.0</td>
<td>15897.0</td>
<td>24765.26</td>
<td>18910.85</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>11990.19</td>
<td>885.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>266.88</td>
<td>4182.0</td>
<td>3915.12</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>1928.55</td>
<td>5096.0</td>
<td>3167.45</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>10648.06</td>
<td>37419.0</td>
<td>26770.94</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>962.43</td>
<td>26770.94</td>
<td>25808.5</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>8348.1</td>
<td>16583.0</td>
<td>8234.9</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>6969.67</td>
<td>61091.0</td>
<td>54121.33</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>20128.0</td>
<td>20128.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>1.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>20128.0</td>
<td>20128.0</td>
<td>0.0</td>
<td>6268.0</td>
<td>12145.85</td>
<td>1.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>154.87</td>
<td>9339.0</td>
<td>9184.13</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>DEBIT</td>
<td>4040.84</td>
<td>1938.0</td>
<td>0.0</td>
<td>51339.0</td>
<td>36757.68</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>6490.17</td>
<td>548504.0</td>
<td>542013.83</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>1101.09</td>
<td>30858.0</td>
<td>29756.91</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>DEBIT</td>
<td>4347.78</td>
<td>20766.0</td>
<td>16418.22</td>
<td>407959.76</td>
<td>55974.56</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>213922.13</td>
<td>16418.22</td>
<td>0.0</td>
<td>431956.63</td>
<td>2025098.66</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>83859.59</td>
<td>0.0</td>
<td>0.0</td>
<td>1.638754836e7</td>
<td>1.916920493e7</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>33269.25</td>
<td>0.0</td>
<td>0.0</td>
<td>111389.13</td>
<td>15375.37</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>51749.94</td>
<td>0.0</td>
<td>0.0</td>
<td>62470.0</td>
<td>132842.64</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>86410.3</td>
<td>0.0</td>
<td>0.0</td>
<td>757892.11</td>
<td>1412484.09</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>10856.63</td>
<td>0.0</td>
<td>0.0</td>
<td>217845.38</td>
<td>1363368.51</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>161613.93</td>
<td>0.0</td>
<td>0.0</td>
<td>402063.39</td>
<td>4619798.56</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>244361.81</td>
<td>0.0</td>
<td>0.0</td>
<td>859282.41</td>
<td>1517262.16</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>596617.87</td>
<td>0.0</td>
<td>0.0</td>
<td>645878.76</td>
<td>2025098.66</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>108600.78</td>
<td>0.0</td>
<td>0.0</td>
<td>146488.24</td>
<td>97128.19</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>377520.94</td>
<td>0.0</td>
<td>0.0</td>
<td>563677.32</td>
<td>4619798.56</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>60758.42</td>
<td>0.0</td>
<td>0.0</td>
<td>155911.68</td>
<td>500631.71</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>86204.31</td>
<td>0.0</td>
<td>0.0</td>
<td>941198.26</td>
<td>4619798.56</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>1234.0</td>
<td>0.0</td>
<td>0.0</td>
<td>29906.0</td>
<td>7550.03</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>18288.91</td>
<td>0.0</td>
<td>0.0</td>
<td>858090.28</td>
<td>2107778.11</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>248979.22</td>
<td>0.0</td>
<td>0.0</td>
<td>1103644.22</td>
<td>1517262.16</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>126774.5</td>
<td>0.0</td>
<td>0.0</td>
<td>1242496.63</td>
<td>2025098.66</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>38428.19</td>
<td>0.0</td>
<td>0.0</td>
<td>43524.89</td>
<td>122750.49</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>109732.52</td>
<td>0.0</td>
<td>0.0</td>
<td>8244985.62</td>
<td>1.068123879e7</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>17851.54</td>
<td>0.0</td>
<td>0.0</td>
<td>32353.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>5223.97</td>
<td>0.0</td>
<td>0.0</td>
<td>10429.69</td>
<td>23568.91</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>63807.17</td>
<td>0.0</td>
<td>0.0</td>
<td>558216.18</td>
<td>1186556.81</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>406297.68</td>
<td>0.0</td>
<td>0.0</td>
<td>882606.73</td>
<td>971418.91</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>4779.93</td>
<td>121.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>16631.14</td>
<td>0.0</td>
<td>0.0</td>
<td>89102.55</td>
<td>1015132.48</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>2771.16</td>
<td>60721.0</td>
<td>57949.84</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>3154.06</td>
<td>24239.0</td>
<td>21084.94</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>7813.63</td>
<td>21084.94</td>
<td>13271.31</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>360.13</td>
<td>38939.0</td>
<td>38578.87</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>431381.17</td>
<td>40481.0</td>
<td>0.0</td>
<td>55566.0</td>
<td>3554299.27</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>43670.84</td>
<td>0.0</td>
<td>0.0</td>
<td>79331.08</td>
<td>215851.28</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>7342.63</td>
<td>19443.0</td>
<td>12100.37</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>7417.09</td>
<td>843.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>8865.26</td>
<td>84415.83</td>
<td>75550.57</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>213490.87</td>
<td>75550.57</td>
<td>0.0</td>
<td>508139.13</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>7444.4</td>
<td>31902.57</td>
<td>24458.17</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>287265.38</td>
<td>24458.17</td>
<td>0.0</td>
<td>486947.17</td>
<td>3554299.27</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>416001.33</td>
<td>0.0</td>
<td>0.0</td>
<td>102.0</td>
<td>9291619.62</td>
<td>1.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>6432.26</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>8690.86</td>
<td>29772.0</td>
<td>21081.14</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>3791.28</td>
<td>32463.0</td>
<td>28671.72</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>4111.34</td>
<td>128052.0</td>
<td>123940.66</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>7163.9</td>
<td>114219.94</td>
<td>107056.04</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>6557.51</td>
<td>107056.04</td>
<td>100498.52</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>17348.25</td>
<td>100498.52</td>
<td>83150.27</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>1586.28</td>
<td>83150.27</td>
<td>81564.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>6811.97</td>
<td>81564.0</td>
<td>74752.02</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>36651.07</td>
<td>74752.02</td>
<td>38100.95</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>4804.68</td>
<td>38100.95</td>
<td>33296.27</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>19375.5</td>
<td>33296.27</td>
<td>13920.76</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>13833.75</td>
<td>13920.76</td>
<td>87.01</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>21008.52</td>
<td>87.01</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>22043.92</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>32635.14</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>11438.39</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>13527.16</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>11881.78</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>9805.46</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>9470.85</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>20560.86</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>13679.84</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>15448.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>1987.4</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>4145.04</td>
<td>97168.0</td>
<td>93022.96</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>3511.45</td>
<td>11310.0</td>
<td>7798.55</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>1118.1</td>
<td>9041.0</td>
<td>7922.9</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>15.06</td>
<td>204682.0</td>
<td>204666.94</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_IN</td>
<td>352568.35</td>
<td>9945.0</td>
<td>362513.35</td>
<td>2624106.46</td>
<td>5515763.34</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>3043.27</td>
<td>36369.0</td>
<td>33325.73</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>9758.09</td>
<td>21217.0</td>
<td>11458.91</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>5924.89</td>
<td>1518.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>9550.43</td>
<td>4021.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>4355.66</td>
<td>120.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>DEBIT</td>
<td>6973.12</td>
<td>50015.0</td>
<td>43041.88</td>
<td>81953.08</td>
<td>122750.49</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>2358.63</td>
<td>70824.0</td>
<td>68465.37</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>DEBIT</td>
<td>374.36</td>
<td>15237.0</td>
<td>14862.64</td>
<td>62904.98</td>
<td>353532.56</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>8946.63</td>
<td>95066.0</td>
<td>86119.37</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>20573.4</td>
<td>24641.0</td>
<td>4067.6</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>8358.12</td>
<td>293.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>1006.76</td>
<td>696008.0</td>
<td>695001.24</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>10498.05</td>
<td>16730.0</td>
<td>6231.95</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>463722.76</td>
<td>349.0</td>
<td>0.0</td>
<td>18285.39</td>
<td>185389.8</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>138857.19</td>
<td>109473.0</td>
<td>0.0</td>
<td>1027402.57</td>
<td>4619798.56</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>5184.9</td>
<td>283509.0</td>
<td>278324.1</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>3626.96</td>
<td>23506.0</td>
<td>19879.04</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>282.42</td>
<td>30603.0</td>
<td>30320.58</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>DEBIT</td>
<td>8703.54</td>
<td>137782.0</td>
<td>129078.46</td>
<td>63279.34</td>
<td>353532.56</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>3673.1</td>
<td>30507.0</td>
<td>26833.9</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>9800.58</td>
<td>12617.16</td>
<td>2816.58</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>88987.11</td>
<td>2816.58</td>
<td>0.0</td>
<td>696184.96</td>
<td>2719172.89</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>4227.19</td>
<td>36054.0</td>
<td>31826.81</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>2881.51</td>
<td>90250.0</td>
<td>87368.49</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>4376.46</td>
<td>9809.0</td>
<td>5432.54</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>11656.12</td>
<td>10181.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>345698.98</td>
<td>40419.0</td>
<td>0.0</td>
<td>20489.94</td>
<td>157982.12</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>3292.21</td>
<td>20484.0</td>
<td>17191.79</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>7367.06</td>
<td>14528.0</td>
<td>7160.94</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>2937.34</td>
<td>19703.0</td>
<td>16765.66</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>DEBIT</td>
<td>1150.14</td>
<td>21634.0</td>
<td>20483.86</td>
<td>15653.66</td>
<td>23568.91</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>3563.36</td>
<td>185397.0</td>
<td>181833.64</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>2380.46</td>
<td>101428.0</td>
<td>99047.54</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>2266.02</td>
<td>64757.92</td>
<td>62491.91</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>32132.45</td>
<td>62491.91</td>
<td>30359.46</td>
<td>59605.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>51627.39</td>
<td>30359.46</td>
<td>0.0</td>
<td>81243.0</td>
<td>32092.07</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>62073.54</td>
<td>0.0</td>
<td>0.0</td>
<td>228702.01</td>
<td>1363368.51</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>13614.91</td>
<td>0.0</td>
<td>0.0</td>
<td>30195.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>2290.73</td>
<td>0.0</td>
<td>0.0</td>
<td>79471.66</td>
<td>42450.71</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>637161.0</td>
<td>0.0</td>
<td>0.0</td>
<td>1166259.76</td>
<td>4619798.56</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>639378.37</td>
<td>0.0</td>
<td>0.0</td>
<td>753824.15</td>
<td>3420103.09</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>34390.85</td>
<td>0.0</td>
<td>0.0</td>
<td>90214.97</td>
<td>66575.5</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>1867849.02</td>
<td>0.0</td>
<td>0.0</td>
<td>2271538.11</td>
<td>5515763.34</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>1193410.46</td>
<td>0.0</td>
<td>0.0</td>
<td>1.647140795e7</td>
<td>1.916920493e7</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>348186.2</td>
<td>0.0</td>
<td>0.0</td>
<td>774212.55</td>
<td>3554299.27</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>1440296.14</td>
<td>0.0</td>
<td>0.0</td>
<td>1.76648184e7</td>
<td>1.916920493e7</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>799483.57</td>
<td>0.0</td>
<td>0.0</td>
<td>1122398.75</td>
<td>3554299.27</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>969631.31</td>
<td>0.0</td>
<td>0.0</td>
<td>2134967.54</td>
<td>6453430.91</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>483544.3</td>
<td>0.0</td>
<td>0.0</td>
<td>876379.19</td>
<td>2107778.11</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>326349.91</td>
<td>0.0</td>
<td>0.0</td>
<td>778150.49</td>
<td>1254956.07</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>DEBIT</td>
<td>7546.88</td>
<td>51368.0</td>
<td>43821.12</td>
<td>11353.0</td>
<td>252055.24</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>2469.75</td>
<td>43821.12</td>
<td>41351.37</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>DEBIT</td>
<td>1741.24</td>
<td>80942.0</td>
<td>79200.76</td>
<td>366188.92</td>
<td>157982.12</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>553.25</td>
<td>7522.0</td>
<td>6968.75</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>6606.31</td>
<td>6968.75</td>
<td>362.44</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>2075.94</td>
<td>110403.0</td>
<td>108327.06</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>84212.94</td>
<td>53199.0</td>
<td>0.0</td>
<td>0.0</td>
<td>84212.94</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>312108.51</td>
<td>0.0</td>
<td>0.0</td>
<td>8043910.53</td>
<td>1.249436715e7</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>1647.93</td>
<td>70889.0</td>
<td>69241.07</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>3533.32</td>
<td>38211.0</td>
<td>34677.68</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>7659.86</td>
<td>544.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>7203.07</td>
<td>290775.55</td>
<td>283572.48</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>116206.75</td>
<td>283572.48</td>
<td>167365.73</td>
<td>216670.1</td>
<td>500631.71</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>30331.38</td>
<td>167365.73</td>
<td>137034.36</td>
<td>105733.69</td>
<td>1015132.48</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>2063.77</td>
<td>16064.0</td>
<td>14000.23</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>3035.35</td>
<td>26657.0</td>
<td>23621.65</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>123797.1</td>
<td>23621.65</td>
<td>0.0</td>
<td>153674.01</td>
<td>557537.26</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>DEBIT</td>
<td>8233.25</td>
<td>7024.0</td>
<td>0.0</td>
<td>14673.0</td>
<td>12901.08</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>2271.34</td>
<td>31346.0</td>
<td>29074.66</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>3801.74</td>
<td>73616.2</td>
<td>69814.46</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>1530.83</td>
<td>69814.46</td>
<td>68283.63</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>4833.96</td>
<td>41005.0</td>
<td>36171.04</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>5842.61</td>
<td>40.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>2871.89</td>
<td>653.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>DEBIT</td>
<td>5297.36</td>
<td>82134.0</td>
<td>76836.64</td>
<td>31443.78</td>
<td>189534.74</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>621561.65</td>
<td>123122.0</td>
<td>0.0</td>
<td>0.0</td>
<td>3997768.55</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>829.07</td>
<td>11199.0</td>
<td>10369.93</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>3418.07</td>
<td>260.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>41924.75</td>
<td>0.0</td>
<td>0.0</td>
<td>132870.39</td>
<td>32092.07</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>173597.57</td>
<td>0.0</td>
<td>0.0</td>
<td>3104598.86</td>
<td>6453430.91</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>77642.84</td>
<td>0.0</td>
<td>0.0</td>
<td>416103.33</td>
<td>9291619.62</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>144964.23</td>
<td>0.0</td>
<td>0.0</td>
<td>785172.07</td>
<td>2719172.89</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>94038.9</td>
<td>0.0</td>
<td>0.0</td>
<td>136065.07</td>
<td>1015132.48</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>26051.33</td>
<td>0.0</td>
<td>0.0</td>
<td>28479.11</td>
<td>1891.79</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>134143.27</td>
<td>0.0</td>
<td>0.0</td>
<td>1754894.42</td>
<td>3940085.21</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>497424.19</td>
<td>0.0</td>
<td>0.0</td>
<td>930136.3</td>
<td>2719172.89</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>607616.73</td>
<td>0.0</td>
<td>0.0</td>
<td>1359923.49</td>
<td>2107778.11</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>681093.57</td>
<td>0.0</td>
<td>0.0</td>
<td>844302.4</td>
<td>1412484.09</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>114057.61</td>
<td>0.0</td>
<td>0.0</td>
<td>1427560.49</td>
<td>2719172.89</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>205279.62</td>
<td>0.0</td>
<td>0.0</td>
<td>1541618.1</td>
<td>2719172.89</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>173512.93</td>
<td>0.0</td>
<td>0.0</td>
<td>8354718.14</td>
<td>1.068123879e7</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>166817.98</td>
<td>0.0</td>
<td>0.0</td>
<td>721630.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>113283.13</td>
<td>0.0</td>
<td>0.0</td>
<td>123001.91</td>
<td>215851.28</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>179030.05</td>
<td>0.0</td>
<td>0.0</td>
<td>1357232.47</td>
<td>2107965.39</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>94332.0</td>
<td>0.0</td>
<td>0.0</td>
<td>621561.65</td>
<td>3997768.55</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>141111.15</td>
<td>0.0</td>
<td>0.0</td>
<td>622023.34</td>
<td>1186556.81</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>45756.23</td>
<td>0.0</td>
<td>0.0</td>
<td>1393202.52</td>
<td>3420103.09</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>6039.61</td>
<td>0.0</td>
<td>0.0</td>
<td>31164.0</td>
<td>37203.61</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>46261.02</td>
<td>0.0</td>
<td>0.0</td>
<td>85758.95</td>
<td>52415.15</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>312471.16</td>
<td>0.0</td>
<td>0.0</td>
<td>1803420.75</td>
<td>4619798.56</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>195490.04</td>
<td>0.0</td>
<td>0.0</td>
<td>1104500.4</td>
<td>1254956.07</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>107308.84</td>
<td>0.0</td>
<td>0.0</td>
<td>124605.82</td>
<td>66575.5</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>48252.82</td>
<td>0.0</td>
<td>0.0</td>
<td>1889037.69</td>
<td>3940085.21</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>25674.03</td>
<td>0.0</td>
<td>0.0</td>
<td>30563.0</td>
<td>6830.83</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>63830.49</td>
<td>0.0</td>
<td>0.0</td>
<td>412307.54</td>
<td>55974.56</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>96178.8</td>
<td>0.0</td>
<td>0.0</td>
<td>111973.96</td>
<td>277515.05</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>155122.86</td>
<td>0.0</td>
<td>0.0</td>
<td>367930.17</td>
<td>157982.12</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>171757.76</td>
<td>0.0</td>
<td>0.0</td>
<td>205811.8</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>8809.04</td>
<td>0.0</td>
<td>0.0</td>
<td>137034.36</td>
<td>1363368.51</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>27320.2</td>
<td>0.0</td>
<td>0.0</td>
<td>79191.11</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>517915.91</td>
<td>0.0</td>
<td>0.0</td>
<td>4404060.84</td>
<td>22190.99</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>34629.16</td>
<td>0.0</td>
<td>0.0</td>
<td>38735.74</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>94296.25</td>
<td>0.0</td>
<td>0.0</td>
<td>336692.95</td>
<td>1178808.14</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>285360.06</td>
<td>0.0</td>
<td>0.0</td>
<td>715893.66</td>
<td>3997768.55</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>161445.91</td>
<td>0.0</td>
<td>0.0</td>
<td>277471.11</td>
<td>557537.26</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>386683.04</td>
<td>0.0</td>
<td>0.0</td>
<td>1352623.44</td>
<td>1517262.16</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>169854.57</td>
<td>0.0</td>
<td>0.0</td>
<td>8528231.07</td>
<td>1.068123879e7</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>126167.7</td>
<td>0.0</td>
<td>0.0</td>
<td>3278196.43</td>
<td>6453430.91</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>152757.58</td>
<td>0.0</td>
<td>0.0</td>
<td>8356019.04</td>
<td>1.249436715e7</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>49661.18</td>
<td>0.0</td>
<td>0.0</td>
<td>1937290.51</td>
<td>3940085.21</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>73513.5</td>
<td>0.0</td>
<td>0.0</td>
<td>236639.0</td>
<td>424250.45</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>166335.32</td>
<td>0.0</td>
<td>0.0</td>
<td>1739306.48</td>
<td>1517262.16</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>234268.82</td>
<td>0.0</td>
<td>0.0</td>
<td>310152.51</td>
<td>424250.45</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>306280.12</td>
<td>0.0</td>
<td>0.0</td>
<td>1536262.52</td>
<td>2107965.39</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>194620.68</td>
<td>0.0</td>
<td>0.0</td>
<td>610710.98</td>
<td>3461666.05</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>128278.74</td>
<td>0.0</td>
<td>0.0</td>
<td>8508776.62</td>
<td>1.249436715e7</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>256773.54</td>
<td>0.0</td>
<td>0.0</td>
<td>888447.97</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>105759.12</td>
<td>0.0</td>
<td>0.0</td>
<td>332876.84</td>
<td>500631.71</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>59390.46</td>
<td>0.0</td>
<td>0.0</td>
<td>73791.34</td>
<td>965870.05</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>131991.26</td>
<td>0.0</td>
<td>0.0</td>
<td>1145221.51</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>65025.3</td>
<td>0.0</td>
<td>0.0</td>
<td>133181.81</td>
<td>965870.05</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>82054.36</td>
<td>0.0</td>
<td>0.0</td>
<td>174795.14</td>
<td>32092.07</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>69708.79</td>
<td>0.0</td>
<td>0.0</td>
<td>71982.88</td>
<td>353532.56</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>9104.26</td>
<td>0.0</td>
<td>0.0</td>
<td>10623.0</td>
<td>19727.26</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>92553.64</td>
<td>0.0</td>
<td>0.0</td>
<td>297655.51</td>
<td>23508.22</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>66312.08</td>
<td>0.0</td>
<td>0.0</td>
<td>544421.33</td>
<td>424250.45</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>142282.63</td>
<td>0.0</td>
<td>0.0</td>
<td>805331.66</td>
<td>3461666.05</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>114499.14</td>
<td>0.0</td>
<td>0.0</td>
<td>129297.57</td>
<td>420946.86</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>76635.24</td>
<td>0.0</td>
<td>0.0</td>
<td>102696.35</td>
<td>92307.65</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>132574.0</td>
<td>0.0</td>
<td>0.0</td>
<td>198207.11</td>
<td>965870.05</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>39485.21</td>
<td>0.0</td>
<td>0.0</td>
<td>9987286.56</td>
<td>1.687464309e7</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>236825.42</td>
<td>0.0</td>
<td>0.0</td>
<td>243796.7</td>
<td>420946.86</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>327066.19</td>
<td>0.0</td>
<td>0.0</td>
<td>947614.29</td>
<td>3461666.05</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>156819.6</td>
<td>0.0</td>
<td>0.0</td>
<td>438635.96</td>
<td>500631.71</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>13136.44</td>
<td>0.0</td>
<td>0.0</td>
<td>1525395.97</td>
<td>1412484.09</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>4306.89</td>
<td>256849.49</td>
<td>252542.6</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>8309.52</td>
<td>252542.6</td>
<td>244233.08</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>5363.35</td>
<td>81503.83</td>
<td>76140.48</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>132.42</td>
<td>76140.48</td>
<td>76008.06</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>19644.08</td>
<td>76008.06</td>
<td>56363.98</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>895.24</td>
<td>56363.98</td>
<td>55468.74</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>7641.08</td>
<td>55468.74</td>
<td>47827.66</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>31188.22</td>
<td>47827.66</td>
<td>16639.44</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>13954.75</td>
<td>16639.44</td>
<td>2684.69</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>10497.33</td>
<td>2684.69</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>5843.75</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>7260.2</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>10285.86</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>6249.78</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>848.74</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>6993.7</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>795.29</td>
<td>10360.0</td>
<td>9564.71</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>9191.46</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>132038.41</td>
<td>0.0</td>
<td>0.0</td>
<td>230103.96</td>
<td>1015132.48</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>6495.27</td>
<td>22040.0</td>
<td>15544.73</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>1679.68</td>
<td>15544.73</td>
<td>13865.06</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>47477.97</td>
<td>6066.0</td>
<td>0.0</td>
<td>144658.38</td>
<td>15375.37</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>DEBIT</td>
<td>1260.62</td>
<td>1483.0</td>
<td>222.38</td>
<td>10086.0</td>
<td>9837.06</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>15034.23</td>
<td>40458.0</td>
<td>25423.77</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>3238.39</td>
<td>28773.0</td>
<td>25534.61</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>251241.14</td>
<td>25534.61</td>
<td>0.0</td>
<td>523053.03</td>
<td>157982.12</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>3644.12</td>
<td>1230.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>283123.48</td>
<td>497409.0</td>
<td>214285.52</td>
<td>16872.0</td>
<td>247063.16</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>2851.83</td>
<td>10646.0</td>
<td>7794.17</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>94726.64</td>
<td>69854.13</td>
<td>164580.77</td>
<td>129704.88</td>
<td>49864.36</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>17020.69</td>
<td>164580.77</td>
<td>147560.08</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>6523.19</td>
<td>147560.08</td>
<td>141036.89</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>1383.56</td>
<td>5309.0</td>
<td>3925.44</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>DEBIT</td>
<td>1580.66</td>
<td>31156.0</td>
<td>29575.34</td>
<td>12298.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>2466.95</td>
<td>34978.24</td>
<td>32511.29</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>1017.85</td>
<td>32511.29</td>
<td>31493.45</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>2270.41</td>
<td>60640.12</td>
<td>58369.71</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>102265.88</td>
<td>58369.71</td>
<td>0.0</td>
<td>1438958.75</td>
<td>3420103.09</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>DEBIT</td>
<td>13758.63</td>
<td>29858.0</td>
<td>16099.37</td>
<td>21305.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>5221.77</td>
<td>1046.0</td>
<td>6267.77</td>
<td>0.0</td>
<td>139555.44</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>12256.66</td>
<td>20800.0</td>
<td>8543.34</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>8227.3</td>
<td>6207.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>527.15</td>
<td>72421.0</td>
<td>71893.85</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>950.25</td>
<td>204944.0</td>
<td>203993.75</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>12728.46</td>
<td>83636.0</td>
<td>70907.54</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>1002.96</td>
<td>8325.0</td>
<td>7322.04</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>6917.1</td>
<td>13555.0</td>
<td>6637.9</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>1977.17</td>
<td>43619.0</td>
<td>41641.83</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>9314.84</td>
<td>104536.0</td>
<td>95221.16</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>1157.05</td>
<td>52867.0</td>
<td>51709.95</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>DEBIT</td>
<td>3766.99</td>
<td>16600.0</td>
<td>12833.01</td>
<td>146166.67</td>
<td>46393.85</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>3464.41</td>
<td>5548.0</td>
<td>2083.59</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>68662.06</td>
<td>2083.59</td>
<td>0.0</td>
<td>145843.39</td>
<td>1363368.51</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>DEBIT</td>
<td>8679.29</td>
<td>150592.0</td>
<td>141912.71</td>
<td>21056.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>15266.05</td>
<td>13679.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>8348.75</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>8419.73</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>6688.74</td>
<td>9207.0</td>
<td>2518.26</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>1979.94</td>
<td>82034.0</td>
<td>80054.06</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>9574.31</td>
<td>52463.0</td>
<td>42888.69</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>5287.68</td>
<td>11424.0</td>
<td>6136.32</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>10627.18</td>
<td>6136.32</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>5809.78</td>
<td>99884.0</td>
<td>94074.22</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>411363.54</td>
<td>39951.0</td>
<td>0.0</td>
<td>52170.0</td>
<td>60738.03</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>4086.5</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>19741.22</td>
<td>81762.38</td>
<td>62021.17</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>5270.72</td>
<td>62021.17</td>
<td>56750.45</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>55827.09</td>
<td>9907.0</td>
<td>65734.09</td>
<td>19873.0</td>
<td>1268668.92</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>174068.82</td>
<td>65734.09</td>
<td>0.0</td>
<td>1905641.8</td>
<td>1517262.16</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>TRANSFER</td>
<td>1277212.77</td>
<td>1277212.77</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>1.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>CASH_OUT</td>
<td>1277212.77</td>
<td>1277212.77</td>
<td>0.0</td>
<td>0.0</td>
<td>2444985.19</td>
<td>1.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_IN</td>
<td>24105.5</td>
<td>144.0</td>
<td>24249.5</td>
<td>20875.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>6854.06</td>
<td>19853.0</td>
<td>12998.94</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>CASH_OUT</td>
<td>111899.09</td>
<td>12998.94</td>
<td>0.0</td>
<td>1.002677177e7</td>
<td>1.687464309e7</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>1231.79</td>
<td>20792.0</td>
<td>19560.21</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>3380.1</td>
<td>19560.21</td>
<td>16180.11</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>DEBIT</td>
<td>2869.65</td>
<td>8768.0</td>
<td>5898.35</td>
<td>15485.0</td>
<td>5484.37</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>1658.87</td>
<td>16543.0</td>
<td>14884.13</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>823.36</td>
<td>14884.13</td>
<td>14060.77</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>7541.47</td>
<td>14060.77</td>
<td>6519.3</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>DEBIT</td>
<td>3635.93</td>
<td>106929.0</td>
<td>103293.07</td>
<td>95816.79</td>
<td>8496.61</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>1328.59</td>
<td>14469.0</td>
<td>13140.41</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>56145.85</td>
<td>21528.0</td>
<td>0.0</td>
<td>5075.0</td>
<td>33008.44</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>973.18</td>
<td>10762.0</td>
<td>9788.82</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>619.24</td>
<td>31960.0</td>
<td>31340.76</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>DEBIT</td>
<td>300.41</td>
<td>39167.0</td>
<td>38866.59</td>
<td>48749.55</td>
<td>46820.71</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>TRANSFER</td>
<td>700967.19</td>
<td>15383.0</td>
<td>0.0</td>
<td>50767.43</td>
<td>130747.56</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>8354.27</td>
<td>5611.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>DEBIT</td>
<td>5423.28</td>
<td>20281.0</td>
<td>14857.72</td>
<td>11523.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>708.59</td>
<td>14857.72</td>
<td>14149.13</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>8358.81</td>
<td>3467.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>2245.62</td>
<td>30910.0</td>
<td>28664.38</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>4593.59</td>
<td>78282.0</td>
<td>73688.41</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>399.66</td>
<td>153760.0</td>
<td>153360.34</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>1617.9</td>
<td>507865.0</td>
<td>506247.1</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>2167.26</td>
<td>13669.0</td>
<td>11501.74</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>9738.95</td>
<td>289748.0</td>
<td>280009.05</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>3396.25</td>
<td>18524.0</td>
<td>15127.75</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>1.0</td>
<td>PAYMENT</td>
<td>6780.78</td>
<td>52640.0</td>
<td>45859.22</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>1.0</td>
<td>PAYMENT</td>
<td>2284.54</td>
<td>539.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
<td>0.0</td>
</tr>
</tbody>
</table>

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
fraud_rdd = load_fraud_data()
fraud_rdd.take(5)
```

</div>

<div class="cell markdown">

**FIRE WALL DATA**

Data from UCI repository: https://archive.ics.uci.edu/ml/datasets/Internet+Firewall+Data \[2\]. Number of data points: 65,532. Number of features: 11 (all numerical). Number of classes: 4 (allow/deny/drop/reset both).

\[2\] Dua, D. and Graff, C. (2019). UCI Machine Learning Repository \[http://archive.ics.uci.edu/ml\]. Irvine, CA: University of California, School of Information and Computer Science.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
from pyspark.ml.feature import StringIndexer, OneHotEncoder, StandardScaler, VectorAssembler
from pyspark.ml import Pipeline
import torch
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
def load_firewall_data(train_test_split=False):
  """Load and preprocess firewall data
  Args:
     train_test_split: whether to split the data into train/test sets or not
  
  Returns:
     Firewall data, RDD of tuples (list(features), int(label))
  """
  
  # File location and type
  file_location = "/FileStore/shared_uploads/amanda.olmin@liu.se/fire_wall_data.csv" 
  file_type = "csv"

  # CSV options
  infer_schema = "true"
  first_row_is_header = "true"
  delimiter = ","

  # Load the data from file
  df = spark.read.format(file_type) \
      .option("inferSchema", infer_schema) \
      .option("header", first_row_is_header) \
      .option("sep", delimiter) \
      .load(file_location)
  
  # Preprocess data
  col_num = ["Source Port", "Destination Port", "NAT Source Port", "NAT Destination Port", "Bytes", "Bytes Sent", "Bytes Received", "Packets", "Elapsed Time (sec)", "pkts_sent", "pkts_received"]

  # Index qualitative variable
  indexer = StringIndexer(inputCol = "Action", outputCol = "label")

  # Scale numerical features
  va = VectorAssembler(inputCols = col_num, outputCol = "numerical_features") 
  scaler = StandardScaler(inputCol = "numerical_features", outputCol = "features")
  
  # Apply pipeline 
  pipeline = Pipeline(stages=[indexer, va, scaler])
  final_df = pipeline.fit(df).transform(df).select("features", "label") 
  
  # Convert to RDD
  final_rdd = final_df.rdd.map(tuple).map(lambda v: (list(v[0]), int(v[1])))
  
  if train_test_split:
    train_rdd, test_rdd = final_rdd.randomSplit(weights=[0.8, 0.2], seed=12)
    final_rdd = (train_rdd, test_rdd)

  return final_rdd
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
firewall_rdd = load_firewall_data()
firewall_rdd.take(5)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
# The functions below will return RDDs with tuples (tensor(features), tensor(label))

# One way to create a "batch" iterator from the RDDs is with itertoolz
from toolz.itertoolz import partition_all

def get_batched_iterator(input_rdd):
  return input_rdd.mapPartitions(lambda partition: partition_all(batch_size, partition)).toLocalIterator()

#print(next(iterator))
```

</div>
