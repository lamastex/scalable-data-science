# Databricks notebook source
# MAGIC %md
# MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC # Intrusion Detection
# MAGIC 
# MAGIC **Group Project Team**:
# MAGIC 
# MAGIC - MohamedReza Faridghasemnia
# MAGIC - Javad Forough
# MAGIC - Quantao Yang
# MAGIC - Arman Rahbar 
# MAGIC 
# MAGIC **Video **
# MAGIC 
# MAGIC https://chalmersuniversity.box.com/s/hbfurlolj3ax8aarxow0fdcuzjxeo206
# MAGIC 
# MAGIC 
# MAGIC **Dataset Source **
# MAGIC 
# MAGIC https://www.unsw.adfa.edu.au/unsw-canberra-cyber/cybersecurity/ADFA-NB15-Datasets/

# COMMAND ----------

# MAGIC %md
# MAGIC ## Problem Definition
# MAGIC With the evolution and pervasive usage of the computer networks and cloud environments, Cyber-attacks such as Distributed Denial of Service (DDoS) become major threads for such environments. For an example, DDoS attacks can prohibit normal usage of the web services through saturating their underlying system’s resources and even in most recent type of attacks namely Low-rate DDoS attacks, they drop the Quality of Service (QoS) of the cloud service providers significantly and bypass the detection systems by behaving similar to the normal users. Modern networked business environments require a high level of security to ensure safe and trusted communication of information between various organizations and to counter such attacks. An Intrusion Detection System is the foremost important step against such threads happening in the network and act as an adaptable safeguard technology for system security after traditional technologies fail. As the network attacks become more sophisticated, it is crucial to equip the system with the state-of-the-art intrusion detection systems. In this project, we investigate different types of learning-based Intrusion detection systems and evaluate them based on different metrics on a large benchmark dataset in a distributed manner using Apache Spark, which is an open-source distributed general-purpose cluster-computing framework.

# COMMAND ----------

# MAGIC %md
# MAGIC ## Loading and Preprocessing Data 
# MAGIC For detecting intrusion in the network, we use a dataset named UNSW-NB15, a collection of network traffic data collected by Australian Centre for Cyber Security (ACCS). 
# MAGIC 
# MAGIC ![UNSW-NB15 Testbed](https://www.unsw.adfa.edu.au/unsw-canberra-cyber/cybersecurity/ADFA-NB15-Datasets/img/unsw-nb15-testbed.jpg "UNSW-NB15 Testbed")
# MAGIC UNSW-NB15 Testbed. Image from [UNSW-NB15 website](https://www.unsw.adfa.edu.au/unsw-canberra-cyber/cybersecurity/ADFA-NB15-Datasets/)
# MAGIC 
# MAGIC 
# MAGIC The raw data of UNSW-NB15 Dataset is a pcap file of network traffic with the size of 100gb, that 49 features (including labels) is extracted from the dataset using Argus, Bro-IDS tools and twelve other algorithms. The extracted features desription is given below.
# MAGIC 
# MAGIC |No.|Name            |Type     |Description                                                                                                                                                       |
# MAGIC |---|----------------|---------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
# MAGIC |0  |srcip           |nominal  |Source IP address                                                                                                                                                 |
# MAGIC |1  |sport           |integer  |Source port number                                                                                                                                                |
# MAGIC |2  |dstip           |nominal  |Destination IP address                                                                                                                                            |
# MAGIC |3  |dsport          |integer  |Destination port number                                                                                                                                           |
# MAGIC |4  |proto           |nominal  |Transaction protocol                                                                                                                                              |
# MAGIC |5  |state           |nominal  |Indicates to the state and its dependent protocol, e.g. ACC, CLO, CON, ECO, ECR, FIN, INT, MAS, PAR, REQ, RST, TST, TXD, URH, URN, and (-) (if not used state)    |
# MAGIC |6  |dur             |Float    |Record total duration                                                                                                                                             |
# MAGIC |7  |sbytes          |Integer  |Source to destination transaction bytes                                                                                                                           |
# MAGIC |8  |dbytes          |Integer  |Destination to source transaction bytes                                                                                                                           |
# MAGIC |9  |sttl            |Integer  |Source to destination time to live value                                                                                                                          |
# MAGIC |10 |dttl            |Integer  |Destination to source time to live value                                                                                                                          |
# MAGIC |11 |sloss           |Integer  |Source packets retransmitted or dropped                                                                                                                           |
# MAGIC |12 |dloss           |Integer  |Destination packets retransmitted or dropped                                                                                                                      |
# MAGIC |13 |service         |nominal  |http, ftp, smtp, ssh, dns, ftp-data ,irc  and (-) if not much used service                                                                                        |
# MAGIC |14 |Sload           |Float    |Source bits per second                                                                                                                                            |
# MAGIC |15 |Dload           |Float    |Destination bits per second                                                                                                                                       |
# MAGIC |16 |Spkts           |integer  |Source to destination packet count                                                                                                                                |
# MAGIC |17 |Dpkts           |integer  |Destination to source packet count                                                                                                                                |
# MAGIC |18 |swin            |integer  |Source TCP window advertisement value                                                                                                                             |
# MAGIC |19 |dwin            |integer  |Destination TCP window advertisement value                                                                                                                        |
# MAGIC |20 |stcpb           |integer  |Source TCP base sequence number                                                                                                                                   |
# MAGIC |21 |dtcpb           |integer  |Destination TCP base sequence number                                                                                                                              |
# MAGIC |22 |smeansz         |integer  |Mean of the ?ow packet size transmitted by the src                                                                                                                |
# MAGIC |23 |dmeansz         |integer  |Mean of the ?ow packet size transmitted by the dst                                                                                                                |
# MAGIC |24 |trans_depth     |integer  |Represents the pipelined depth into the connection of http request/response transaction                                                                           |
# MAGIC |25 |res_bdy_len     |integer  |Actual uncompressed content size of the data transferred from the server’s http service.                                                                          |
# MAGIC |26 |Sjit            |Float    |Source jitter (mSec)                                                                                                                                              |
# MAGIC |27 |Djit            |Float    |Destination jitter (mSec)                                                                                                                                         |
# MAGIC |28 |Stime           |Timestamp|record start time                                                                                                                                                 |
# MAGIC |29 |Ltime           |Timestamp|record last time                                                                                                                                                  |
# MAGIC |30 |Sintpkt         |Float    |Source interpacket arrival time (mSec)                                                                                                                            |
# MAGIC |31 |Dintpkt         |Float    |Destination interpacket arrival time (mSec)                                                                                                                       |
# MAGIC |32 |tcprtt          |Float    |TCP connection setup round-trip time, the sum of ’synack’ and ’ackdat’.                                                                                           |
# MAGIC |33 |synack          |Float    |TCP connection setup time, the time between the SYN and the SYN_ACK packets.                                                                                      |
# MAGIC |34 |ackdat          |Float    |TCP connection setup time, the time between the SYN_ACK and the ACK packets.                                                                                      |
# MAGIC |35 |is_sm_ips_ports |Binary   |If source (1) and destination (3)IP addresses equal and port numbers (2)(4)  equal then, this variable takes value 1 else 0                                       |
# MAGIC |36 |ct_state_ttl    |Integer  |No. for each state (6) according to specific range of values for source/destination time to live (10) (11).                                                       |
# MAGIC |37 |ct_flw_http_mthd|Integer  |No. of flows that has methods such as Get and Post in http service.                                                                                               |
# MAGIC |38 |is_ftp_login    |Binary   |If the ftp session is accessed by user and password then 1 else 0.                                                                                                |
# MAGIC |39 |ct_ftp_cmd      |integer  |No of flows that has a command in ftp session.                                                                                                                    |
# MAGIC |40 |ct_srv_src      |integer  |No. of connections that contain the same service (14) and source address (1) in 100 connections according to the last time (26).                                  |
# MAGIC |41 |ct_srv_dst      |integer  |No. of connections that contain the same service (14) and destination address (3) in 100 connections according to the last time (26).                             |
# MAGIC |42 |ct_dst_ltm      |integer  |No. of connections of the same destination address (3) in 100 connections according to the last time (26).                                                        |
# MAGIC |43 |ct_src_ ltm     |integer  |No. of connections of the same source address (1) in 100 connections according to the last time (26).                                                             |
# MAGIC |44 |ct_src_dport_ltm|integer  |No of connections of the same source address (1) and the destination port (4) in 100 connections according to the last time (26).                                 |
# MAGIC |45 |ct_dst_sport_ltm|integer  |No of connections of the same destination address (3) and the source port (2) in 100 connections according to the last time (26).                                 |
# MAGIC |46 |ct_dst_src_ltm  |integer  |No of connections of the same source (1) and the destination (3) address in in 100 connections according to the last time (26).                                   |
# MAGIC |47 |attack_cat      |nominal  |The name of each attack category. In this data set , nine categories e.g. Fuzzers, Analysis, Backdoors, DoS Exploits, Generic, Reconnaissance, Shellcode and Worms|
# MAGIC |48 |Label           |binary   |0 for normal and 1 for attack records                                                                                                                             |
# MAGIC 
# MAGIC 
# MAGIC The data is accessible from the [dataset source](https://www.unsw.adfa.edu.au/unsw-canberra-cyber/cybersecurity/ADFA-NB15-Datasets/).
# MAGIC 
# MAGIC We used Spark csv for reading the dataset. In the following cell a spark dataframe from csv is created. In this dataset, the last label is the label, indicating whether an attack happened or not. The problem is a binary classification problem, which the machine learning algorithm has to predict the attack record label.

# COMMAND ----------

# load csv with pyspark
# File location and type
file_location = "/FileStore/tables/IDdataset.csv"
file_type = "csv"

# CSV options
infer_schema = "false"
first_row_is_header = "false"
delimiter = ","

# The applied options are for CSV files. For other file types, these will be ignored.
df = spark.read.format(file_type) \
  .option("inferSchema", infer_schema) \
  .option("header", first_row_is_header) \
  .option("sep", delimiter) \
  .load(file_location)

display(df)


# COMMAND ----------

# MAGIC %md
# MAGIC ### Removing unnecessary data
# MAGIC 
# MAGIC In the dataaset, ip and port of source and destination are not useful, so we drop those columns.

# COMMAND ----------

#dropping ip and port of source and destination 
df= df.drop("_c0","_c1","_c2", "_c3", "_c47")

display(df)

# COMMAND ----------

# MAGIC %md
# MAGIC ### Numerization
# MAGIC 
# MAGIC Now we have to change categorical data (that are columns 4,5,13) to number, that is called ordinal encoding, and we can do it by StringIndexer. 
# MAGIC 
# MAGIC The next step is to convert all columns types to Double. It is neccesssary, as it seems pyspark returned a string dataframe from csv, and doesnot change data types for numbers.

# COMMAND ----------


#handling categorical data
from pyspark.ml.feature import StringIndexer


indexer = StringIndexer(inputCols=["_c4", "_c5", "_c13"], outputCols=["c4", "c5", "c13"])

dff = indexer.fit(df).transform(df)

dff = dff.drop("_c4", "_c5", "_c13")


#changing type to double
from pyspark.sql.types import DoubleType


for col in dff.columns:
  dff = dff.withColumn(col, dff[col].cast(DoubleType()))


# COMMAND ----------

# MAGIC %md
# MAGIC ### Handling null values
# MAGIC #### Check which cells have null values
# MAGIC 
# MAGIC One another step in preprocessing is to check if the data has null values. For this, we use .isNull() over rows of each column, and count null values in each column.

# COMMAND ----------

#check if it has missing data

#showing number of null data  
from pyspark.sql.functions import isnan, when, count, col

for cl in dff.columns:
  dff.select([count(when(col(cl).isNull(),True))]).show()



# COMMAND ----------

# MAGIC %md
# MAGIC #### Filling null values
# MAGIC 
# MAGIC We noticed that column 37(ct_flw_http_mthd) has 1348145, column 38(is_ftp_login) has 1429879, and column 39(ct_ftp_cmd) has 1429879 null values. So we fill them by using Imputer function of pyspark. This function fill the missing values with the mean of the column. 

# COMMAND ----------

#handling null data

from pyspark.ml.feature import Imputer

dff= Imputer(inputCols= dff.columns, outputCols=dff.columns).fit(dff).transform(dff)

#for cl in dff.columns:
#  dff.select([count(when(col(cl).isNull(),True))]).show()

# COMMAND ----------

# MAGIC %md
# MAGIC ### Creating dataset
# MAGIC For creating dataset, we take the following steps:
# MAGIC #### Vectorizing
# MAGIC We firstly have to put all features into one big vector, using VectorAssembler. We take all the columns of data, except the first 4 columns(are irrelevant) an the last two (are the labels) into "features" column. Notice that VectorAssembler generates either Sparse, or Dense vectors, in favour of the memory.
# MAGIC #### Normalization
# MAGIC Next, we normalize data that is vectorized in one column.  For this dataset, VEctorAssembler returned a sparse vector, and we chose a normalizer that is compatible with sparse vectors. So we used ml.feature.Normalizer for normalizing data.
# MAGIC 
# MAGIC #### Sparse to Dense
# MAGIC After normalization, we convert the sparse vectors of features to dense vectors. For this we defined a UDF function.
# MAGIC #### Selecting columns of features and labels
# MAGIC Finally, we select two columns in the dataframe to use in further steps, that are "labels", and "features".

# COMMAND ----------


from pyspark.ml.feature import StandardScaler, VectorAssembler
from pyspark.ml.feature import MinMaxScaler


from pyspark.sql import functions as F
from pyspark.ml.linalg import SparseVector, DenseVector,VectorUDT, Vector
from pyspark.sql import types as T
from pyspark.ml.feature import Normalizer


numCols = ['c4', 'c5', '_c6', '_c7', '_c8', '_c9', '_c10', '_c11', '_c12', 'c13', '_c14', '_c15', '_c16', '_c17', '_c18', '_c19', '_c20', '_c21', '_c22', '_c23', '_c24', '_c25', '_c26', '_c27', '_c28', '_c29', '_c30', '_c31', '_c32', '_c33', '_c34', '_c35', '_c36', '_c37', '_c38', '_c39', '_c40', '_c41', '_c42', '_c43', '_c44', '_c45', '_c46']


dfff = VectorAssembler(inputCols=numCols, outputCol="features").transform(dff)


nrm = Normalizer(inputCol="features", outputCol="features_norm", p=1).transform(dfff)



def sparse_to_array(v):
  v = DenseVector(v)
  new_array = list([float(x) for x in v])
  return new_array


sparse_to_array_udf = F.udf(sparse_to_array, T.ArrayType(T.FloatType()))
featArr = nrm.withColumn('featuresArray', sparse_to_array_udf('features_norm'))


featArr=featArr.withColumnRenamed("_c48", "labels")


trainSet = featArr.select('labels', "featuresArray")

display(trainSet)



# COMMAND ----------

# MAGIC %md
# MAGIC # Preparing the DataFrame for training classifers
# MAGIC In order to use a dataframe for training the classifiers in the Spark ML Library, we should have a particular format. Specifically, we need to have a single columns for all features and another column for the labels. In this section we first create the the desired format and then use the resulting dataframe for training different classifiers.

# COMMAND ----------

# MAGIC %md
# MAGIC Based on the DataFrame created in the preprocessing step (trainSet), we first create an rdd from all available columns (features_to_assemble). To this end, we map all rows in trainSet using the method "functionForRdd".

# COMMAND ----------

features_to_assemble = []
for f in range(2,45):
  features_to_assemble.append('_'+str(f))
print(features_to_assemble)

# COMMAND ----------

# random forest implementation
def functionForRdd(r):
  l = []
  l.append(r[0])
  l = l+list(r[1])
  return l

trainSetRdd = trainSet.rdd.map(functionForRdd)
randomForestDf = trainSetRdd.toDF()



# COMMAND ----------

# MAGIC %md
# MAGIC ## Using VectorAssembler for creating the DataFrame
# MAGIC The best option to create the required format, is using VectorAssembler. Calling the transform function of the assembler object gives us a new DataFrame which includes a new columns called "features". This column together with the labels column ("_1") will be used for training.
# MAGIC We also divide our data into train and test in the cell below.

# COMMAND ----------

assembler = VectorAssembler(
    inputCols=features_to_assemble,
    outputCol="features")

randomForestDf = assembler.transform(randomForestDf)
train, test = randomForestDf.randomSplit([0.7, 0.3], seed = 2018)
newtrainSet = train.sample(fraction=0.00001)

# COMMAND ----------

# MAGIC %md
# MAGIC ## RandomForestClassifier
# MAGIC The first classifier we use is the RandomForestClassifier avaiable in Spark ML. As mentioned before this classifier requires a single columns for all attributes (features) and a label column (_1). We specify these columns before training and then we use the method fit to train the classifer.

# COMMAND ----------

from pyspark.ml.classification import RandomForestClassifier
rf = RandomForestClassifier(featuresCol = 'features', labelCol = '_1')
rfModel = rf.fit(train)


# COMMAND ----------

# MAGIC %md
# MAGIC ## Evaluation
# MAGIC In this part we use the BinaryClassificationMetrics and MulticlassMetrics for evaluating our classifiers. First we need to use our trained model to predict the labels in the test DataFrame. To this aim, we use the transform method. This method add some columns to the test DataFrame. We use the prediction columns which is the predicted classes for data pionts in our test set. 
# MAGIC BinaryClassificationMetrics and MulticlassMetrics require an rdd of (prediction, truelabel) tuples. We create this rdd using the map function on prediction (rdd) and then calculate different metrics.

# COMMAND ----------

from pyspark.mllib.evaluation import BinaryClassificationMetrics

prediction = rfModel.transform(test)


# COMMAND ----------

predictionAndLabels = prediction.rdd.map(lambda r: (r.prediction, r._1))

metrics = BinaryClassificationMetrics(predictionAndLabels)
print("Area under ROC = %s" % metrics.areaUnderROC)

from pyspark.mllib.evaluation import MulticlassMetrics
metrics2 = MulticlassMetrics(predictionAndLabels)
print("Precions = %s" % metrics2.precision(1.0))
print("Recall = %s" % metrics2.recall(1.0))


# COMMAND ----------

print("Accuracy = %s" % metrics2.accuracy)

# COMMAND ----------

# MAGIC %md
# MAGIC ## Logistic Regression classifier
# MAGIC We have trained and tested the Logistic Regression classifer on our training and testing set respectively as follows:

# COMMAND ----------

#Logistic Regression Classification
from pyspark.ml.classification import LogisticRegression
logr = LogisticRegression(featuresCol = 'features', labelCol = '_1')
logrmodel = logr.fit(train)


# COMMAND ----------

from pyspark.mllib.evaluation import BinaryClassificationMetrics

lr_prediction = logrmodel.transform(test)

# COMMAND ----------

# MAGIC %md 
# MAGIC ### Logistic Regression classifier evaluation
# MAGIC We have evaluated the Logistic Regression classifier using binary and also multi-class evaluation metrics as follows: 

# COMMAND ----------

#Logistic Regression Evaluation
lr_predictionAndLabels = lr_prediction.rdd.map(lambda r: (r.prediction, r._1))


metrics = BinaryClassificationMetrics(lr_predictionAndLabels)
print("Area under ROC = %s" % metrics.areaUnderROC)

from pyspark.mllib.evaluation import MulticlassMetrics
metrics2 = MulticlassMetrics(lr_predictionAndLabels)
print("Precions = %s" % metrics2.precision(1.0))
print("Recall = %s" % metrics2.recall(1.0))

# COMMAND ----------

# MAGIC %md
# MAGIC ## Gradient-Boosted Trees (GBTs) classifier
# MAGIC We have trained and tested the GBTs classifer on our training and testing set respectively as follows:

# COMMAND ----------

from pyspark.ml.classification import GBTClassifier

gbt = GBTClassifier(featuresCol = 'features', labelCol = '_1', maxDepth=5)
gbtmodel = gbt.fit(train)

# COMMAND ----------

from pyspark.mllib.evaluation import BinaryClassificationMetrics

gbt_prediction = gbtmodel.transform(test)

# COMMAND ----------

# MAGIC %md 
# MAGIC ### Gradient-Boosted Trees (GBTs) classifier evaluation
# MAGIC We have evaluated the GBTs classifier using binary and also multi-class evaluation metrics as follows: 

# COMMAND ----------

gbt_predictionAndLabels = gbt_prediction.rdd.map(lambda r: (r.prediction, r._1))

metrics = BinaryClassificationMetrics(gbt_predictionAndLabels)
print("Area under ROC = %s" % metrics.areaUnderROC)

from pyspark.mllib.evaluation import MulticlassMetrics
metrics2 = MulticlassMetrics(gbt_predictionAndLabels)
print("Precions = %s" % metrics2.precision(1.0))
print("Recall = %s" % metrics2.recall(1.0))

# COMMAND ----------

# import sys
# # !{sys.executable} -m pip install tensorflow
# # !{sys.executable} -m pip uninstall keras
# # !{sys.executable} -m pip install keras
# # !{sys.executable} -m pip install dist-keras
# # !{sys.executable} -m pip install elephas


# import keras
# from keras.optimizers import *
# from keras.models import Sequential
# from keras.layers import Dense, Dropout, Activation

# from distkeras.trainers import *
# from distkeras.predictors import *
# from distkeras.transformers import *
# from distkeras.evaluators import *
# from distkeras.utils import *

# from pyspark.ml.linalg import Vectors
# from elephas.utils.rdd_utils import to_simple_rdd
# from elephas.spark_model import SparkModel

# trainSett = trainSet.rdd.map(lambda row: Row(
#     labels=row["labels"], 
#     featuresArray=Vectors.dense(row["featuresArray"])
# )).toDF()

# inpDim= len(trainSett.select("featuresArray").first()[0])

# inpDim= len(train.select("features").first()[0])

# model = Sequential()
# model.add(Dense(128, input_dim = inpDim,activation='relu',use_bias=True))
# model.add(Dropout(0.5))
# model.add(Dense(1,activation='sigmoid',use_bias=True)) 
# model.compile(loss='binary_crossentropy', optimizer='adam', metrics=['accuracy'])
# print("compile done")
# # model.build()
# print(model.summary())
# trainer = SingleTrainer(keras_model=model, worker_optimizer='adam', loss='binary_crossentropy', num_epoch=1)
# print("trainer done")  
# trained_model = trainer.train(train)#newtrainSet
# print("training done")
# predictor = ModelPredictor(keras_model=trained_model)
# ff=predictor.predict(test.take(50)[15].features)



# COMMAND ----------

# MAGIC %md
# MAGIC ## Multilayer Perceptron Classifier
# MAGIC 
# MAGIC MLPC consists of multiple layers of nodes. Each layer is fully connected to the next layer in the network. Following code snippet is the implementation of such a model in pyspark. The input layer and the output layer have the size of 43 and 2, with two hidden layers of 5 and 4 neurons respectively.

# COMMAND ----------

# Multilayer Perceptron Classifier
from pyspark.ml.classification import MultilayerPerceptronClassifier
from pyspark.ml.evaluation import MulticlassClassificationEvaluator

# specify layers for the neural network:
# input layer of size 43 (features), two intermediate of size 5 and 4
# and output of size 2 (classes)
layers = [43, 5, 4, 2]

# create the trainer and set its parameters
mlpc = MultilayerPerceptronClassifier(maxIter=100, layers=layers, blockSize=128, seed=1234, featuresCol = 'features', labelCol = '_1')

# train the model
mlpcModel = mlpc.fit(train)

# COMMAND ----------

# MAGIC %md
# MAGIC Finally, a trained MLPC model is returned that is ready to evaluate on the test data. 

# COMMAND ----------

# compute accuracy on the test set
testLabeled = test.withColumnRenamed( '_1', "label")
mlpc_prediction = mlpcModel.transform(testLabeled)


# COMMAND ----------

# MAGIC %md
# MAGIC The model is tested using MulticlassClassificationEvaluator with accuracy as an evaluation metric.

# COMMAND ----------

predictionAndLabels = mlpc_prediction.select("prediction", "label")
evaluator = MulticlassClassificationEvaluator(metricName="accuracy")
print("Test set accuracy = " + str(evaluator.evaluate(predictionAndLabels)))

# COMMAND ----------

# MAGIC %md
# MAGIC ## Decision Tree
# MAGIC The spark.ml implementation supports decision trees for binary and multiclass classification and for regression.

# COMMAND ----------

# Decision Tree
from pyspark.ml.classification import DecisionTreeClassifier

# Train a DecisionTree model.
dt = DecisionTreeClassifier(featuresCol = 'features', labelCol = '_1')
dtModel = dt.fit(train)


# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC The decision tree model is tested for ROC, Precision, and recall. The area under ROC is 0.9738, Precision is 0.9626 and recall is 0.9531.

# COMMAND ----------

# Decision Tree Metrics
prediction = dtModel.transform(test)
predictionAndLabels = prediction.rdd.map(lambda r: (r.prediction, r._1))

from pyspark.mllib.evaluation import BinaryClassificationMetrics
metrics = BinaryClassificationMetrics(predictionAndLabels)
print("Area under ROC = %s" % metrics.areaUnderROC)

from pyspark.mllib.evaluation import MulticlassMetrics
metrics2 = MulticlassMetrics(predictionAndLabels)
print("Precisions = %s" % metrics2.precision(1.0))
print("Recall = %s" % metrics2.recall(1.0))