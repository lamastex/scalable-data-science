// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md # Results and Conclusion

// COMMAND ----------

// MAGIC %md
// MAGIC #### Word count vectors as features for classification, overlapping k-mers
// MAGIC | Confusion matrix|||||
// MAGIC |:--------:	|:--------:	| :--------:	|:--------:	|:--------:	|
// MAGIC |5453.0 | 8.0   |   6.0  |  1.0  |  0.0 |  0.0  | 
// MAGIC |135.0  | 2754.0 | 1.0  |  1.0  |  0.0 |  0.0 |  
// MAGIC |211.0  | 4.0   |  297.0 | 4.0  |  0.0 |  0.0  | 
// MAGIC |115.0  | 0.0   |  3.0  |  275.0 | 0.0 |  0.0   |
// MAGIC |42.0   |  1.0   |  2.0  |  3.0  |  48.0  |0.0   |
// MAGIC |7.0    |   0.0   |  0.0  |  2.0  |  0.0  | 22.0 | 
// MAGIC Summary Statistics
// MAGIC * Accuracy = 0.942
// MAGIC 
// MAGIC #### Word count vectors as features for classification, overlapping k-mers
// MAGIC | Confusion matrix|||||
// MAGIC |:--------:	|:--------:	| :--------:	|:--------:	|:--------:	|
// MAGIC |5446.0 | 8.0   |   10.0  |  4.0  |  0.0 |  0.0  | 
// MAGIC |137.0  | 2746.0 | 0.0  |  1.0  |  0.0 |  0.0 |  
// MAGIC |186.0  | 7.0   |  314.0 | 9.0  |  0.0 |  0.0  | 
// MAGIC |112.0  | 2.0   |  9.0  |  270.0 | 0.0 |  0.0   |
// MAGIC |43.0   |  3.0   |  1.0  |  3.0  |  46.0  |0.0   |
// MAGIC |10.0    |   0.0   |  0.0  |  0.0  |  0.0  | 21.0 | 
// MAGIC Summary Statistics
// MAGIC * Accuracy = 0.941
// MAGIC 
// MAGIC #### Word count vectors as features for classification, overlapping k-mers
// MAGIC | Confusion matrix|||||
// MAGIC |:--------:	|:--------:	| :--------:	|:--------:	|:--------:	|
// MAGIC |5446.0 | 8.0   |   10.0  |  4.0  |  0.0 |  0.0  | 
// MAGIC |137.0  | 2746.0 | 0.0  |  1.0  |  0.0 |  0.0 |  
// MAGIC |186.0  | 7.0   |  314.0 | 9.0  |  0.0 |  0.0  | 
// MAGIC |112.0  | 2.0   |  9.0  |  270.0 | 0.0 |  0.0   |
// MAGIC |43.0   |  3.0   |  1.0  |  3.0  |  46.0  |0.0   |
// MAGIC |10.0    |   0.0   |  0.0  |  0.0  |  0.0  | 21.0 | 
// MAGIC Summary Statistics
// MAGIC * Accuracy = 0.941
// MAGIC #### Number of topics:10 Number of iterations: 100 Nonoverlapping accuracy table:
// MAGIC 
// MAGIC | Confusion matrix |||||
// MAGIC |:--------:	|:--------:	| :--------:	|:--------:	|:--------:	|
// MAGIC |5167.0 | 288.0 | 0.0 | 11.0 |  0.0 | 2.0  |
// MAGIC |2414.0 | 472.0 | 0.0 | 1.0  |  0.0 | 4.0 | 
// MAGIC |496.0 |  20.0  | 0.0 | 0.0  |  0.0 | 0.0 | 
// MAGIC |217.0 |  5.0  |  0.0 | 170.0 | 0.0 | 1.0 | 
// MAGIC |94.0  |  2.0  |  0.0 | 0.0  |  0.0 | 0.0  |
// MAGIC |28.0  |  0.0  |  0.0 | 3.0  |  0.0 | 0.0 | 
// MAGIC Summary Statistics
// MAGIC * Accuracy = 0.618
// MAGIC * fm0 = 0.744
// MAGIC * fm1 = 0.257
// MAGIC * fm2 = 0.0
// MAGIC * fm3 = 0.588
// MAGIC * fm4 = 0.0
// MAGIC 
// MAGIC #### Number of topics:10 Number of iterations: 1000 Nonoverlapping accuracy table:
// MAGIC |Confusion matrix||||||
// MAGIC |:--------:	|:--------:	| :--------:	|:--------:	|:--------:	|
// MAGIC |5290.0 | 113.0  | 10.0 | 3.0 |   0.0 | 0.0 | 
// MAGIC |1012.0 | 1667.0 | 22.0 | 0.0  |  3.0 | 1.0 | 
// MAGIC |430.0  | 10.0  |  76.0 | 0.0  |  0.0 | 0.0  |
// MAGIC |213.0 |  6.0   |  3.0 |  169.0 | 0.0 | 0.0  |
// MAGIC |93.0  |  2.0   |  1.0 |  0.0  |  0.0 | 0.0  |
// MAGIC |31.0  |  0.0   |  0.0 |  0.0  |  0.0 | 0.0  |
// MAGIC Summary Statistics
// MAGIC * Accuracy = 0.787
// MAGIC * fm0 = 0.847
// MAGIC * fm1 = 0.740
// MAGIC * fm2 = 0.242
// MAGIC * fm3 = 0.600
// MAGIC * fm4 = 0.0
// MAGIC 
// MAGIC #### Number of topics:20 Number of iterations: 100 Nonoverlapping accuracy table:
// MAGIC | Confusion matrix |||||
// MAGIC |:--------:	|:--------:	| :--------:	|:--------:	|:--------:	|
// MAGIC | 4174.0 |  1286.0 |  0.0 |  8.0 |    0.0 |  0.0 |  
// MAGIC | 829.0 |   2056.0|   0.0 |  6.0  |   0.0 |  0.0  | 
// MAGIC | 408.0  |  108.0 |   0.0 |  0.0   |  0.0 |  0.0  | 
// MAGIC | 189.0 |   34.0  |   0.0 |  170.0|   0.0 |  0.0  | 
// MAGIC | 48.0   |  48.0  |   0.0 |  0.0  |   0.0 |  0.0  | 
// MAGIC | 29.0   |  1.0   |   0.0 |  1.0  |   0.0 |  0.0  | 
// MAGIC Summary Statistics
// MAGIC * Accuracy = 0.681
// MAGIC * fm0 = 0.749
// MAGIC * fm1 = 0.640
// MAGIC * fm2 = 0.0
// MAGIC * fm3 = 0.588
// MAGIC * fm4 = 0.0
// MAGIC 
// MAGIC #### Number of topics:20 Number of iterations: 20000 Nonverlapping accuracy table:
// MAGIC 
// MAGIC |Confusion matrix|||||
// MAGIC |:--------:	|:--------:	| :--------:	|:--------:	|:--------:	|
// MAGIC |5373.0 | 89.0  |  3.0 |  3.0 |   0.0 | 0.0|   
// MAGIC |368.0  | 2522.0 | 0.0 |  1.0 |   0.0 | 0.0 |  
// MAGIC |478.0  | 15.0 |   20.0|  3.0  |  0.0 | 0.0  | 
// MAGIC |193.0 |  8.0  |   0.0 |  192.0 | 0.0 | 0.0  | 
// MAGIC |89.0  |  5.0  |   0.0 |  1.0  |  1.0 | 0.0   |
// MAGIC |14.0  |  0.0   |  0.0 |  0.0 |   0.0 | 17.0  |
// MAGIC Summary Statistics
// MAGIC * Accuracy = 0.865
// MAGIC * fm0 = 0.897
// MAGIC * fm1 = 0.912
// MAGIC * fm2 = 0.074
// MAGIC * fm3 = 0.648
// MAGIC * fm4 = 0.021
// MAGIC 
// MAGIC #### Number of topics:20 Number of iterations: 15000 Overlapping accuracy table:
// MAGIC |Confusion matrix|||||
// MAGIC |:--------:	|:--------:	| :--------:	|:--------:	|:--------:	|
// MAGIC |5419.0 | 25.0   | 12.0 | 12.0 |  0.0 | 0.0   
// MAGIC |190.0 |  2687.0 | 7.0  | 7.0  |  0.0 | 0.0   
// MAGIC |398.0  | 13.0  |  89.0 | 15.0 |  0.0 | 1.0   
// MAGIC |193.0 |  2.0  |   2.0 |  196.0|  0.0 | 0.0   
// MAGIC |89.0  |  2.0  |   3.0  | 1.0  |  1.0 | 0.0   
// MAGIC |11.0  |  0.0  |   0.0 |  0.0  |  0.0|  20.0  
// MAGIC Summary Statistics
// MAGIC * Accuracy = 0.895
// MAGIC * fm0 = 0.921
// MAGIC * fm1 = 0.956
// MAGIC * fm2 = 0.283
// MAGIC * fm3 = 0.628
// MAGIC * fm4 = 0.021
// MAGIC 
// MAGIC #### Number of topics:50 Number of iterations: 100 Nonoverlapping accuracy table:
// MAGIC 
// MAGIC |Confusion matrix|||||
// MAGIC |:--------:	|:--------:	| :--------:	|:--------:	|:--------:	|
// MAGIC |5250.0  |217.0 | 0.0 | 1.0 |   0.0 | 0.0  |
// MAGIC |2667.0 | 224.0 | 0.0 | 0.0 |   0.0|  0.0  |
// MAGIC |503.0  | 13.0  | 0.0 | 0.0  |  0.0 | 0.0  |
// MAGIC |220.0  | 3.0  |  0.0 | 170.0 | 0.0 | 0.0  |
// MAGIC |94.0  |  2.0  |  0.0 | 0.0 |   0.0 | 0.0  |
// MAGIC |30.0   | 1.0  |  0.0 | 0.0  |  0.0 | 0.0  |
// MAGIC Summary Statistics
// MAGIC * Accuracy = 0.601
// MAGIC * fm0 = 0.738
// MAGIC * fm1 = 0.134
// MAGIC * fm2 = 0.0
// MAGIC * fm3 = 0.603
// MAGIC * fm4 = 0.0

// COMMAND ----------

// MAGIC %md #### Summary Tables for (LDA + Classification) and Classification
// MAGIC * (LDA + Classification) Varying number of topics and fixed number of iterations = 100:
// MAGIC 
// MAGIC | # topics | Accuracy |
// MAGIC |:--------:	|:--------:	| 
// MAGIC | 10 | 0.618|
// MAGIC | 20 | 0.681|
// MAGIC | 50 | 0.601|
// MAGIC *Conclusion*: 20, approximately the number of aminoacids, is a good candidate for the number of topics
// MAGIC 
// MAGIC * We tried both nonoverlapping and overlapping k-mer features directly on classifier:
// MAGIC 
// MAGIC | Data type | Accuracy |
// MAGIC | :----:| :----:|
// MAGIC | Nonoverlapping k-mers | 0.941|
// MAGIC | Overlapping k-mers | 0.942|
// MAGIC 
// MAGIC * Also, on (LDA + classification) for number of topics = 20. While for nonoverlapping the number of iterations are 20000, for overlapping the number of iterations are 15000. Due to 'unexpected shutdown' of clusters we couldn't run LDA on overlapping clusters for 20000 iterations. However results suggests that overlapping k-mers helps LDA to learn structure better in terms of prediction power (but not efficiently) :
// MAGIC 
// MAGIC | Data type | Accuracy |
// MAGIC | :----:| :----:|
// MAGIC | Nonoverlapping k-mers | 0.865|
// MAGIC | Overlapping k-mers | 0.895|
// MAGIC 
// MAGIC *Conclusion*: For classifier, there is not much difference on overlapping or nonoverlapping k-mer features; however for LDA having overlapping helps to learn structure more. But increased time complexity of overlapping k-mers requires LDA to have more iterations to learn.
// MAGIC 
// MAGIC * (LDA + Classification) Varying number of iterations and topics :
// MAGIC 
// MAGIC | (# iterations, # topics, overlapping) | Accuracy |
// MAGIC | :----:| :----:|
// MAGIC |(100, 10, false) | 0.618|
// MAGIC |(1000, 10, false) | 0.787|
// MAGIC |(100, 20, false) | 0.681|
// MAGIC |(15000, 20, true) | 0.895|
// MAGIC |(20000, 20, false) | 0.865|
// MAGIC *Conclusion*: Also considering the topic summaries, we can conclude that the number of iterations highly affect the topic diversity, and thus classifier performance. Although we can check convergence of EM algorithm to stop, this migh be problematic due to increasing computation time with the number of iterations. 
// MAGIC 
// MAGIC * The best of (LDA + Classification) where the number of topics is 20 and the number of iterations is 15000 and vs direct Classification on k-mers performance comparision.
// MAGIC 
// MAGIC | Method | Accuracy |
// MAGIC | :----:| :----:|
// MAGIC |(LDA + Classification)  | 0.895|
// MAGIC | Direct Classification | 0.942|
// MAGIC *Conclusion*: We couldn't perform higher number of iterations (due to limited time and cluster restarts); however, this result shows that LDA is capable of summarizing k-mer features. Once, we learn the mapping from k-mers to reduced topic distribution space via LDA, then we can use this reduced number of features to train classifier. This makes the data more scalable and may save computation time in the long run.

// COMMAND ----------

// MAGIC %md #### Our conclusions
// MAGIC * What we have tried and failed? What we have learned?
// MAGIC   * Overlapping k-mers with low iteration led poor diversity in topics
// MAGIC   * With expectation maximization LDA required iteration increases significantly. Where to stop the iteration becomes a problem due to computation time concerns.
// MAGIC   * Changing doc or term concentration did not lead better results
// MAGIC   * We tried several number of topics (10, 20, 50) and #topics = 20, which is almost equal to number of aminoacids, yields the best result (coincidence or the number of aminoacids is a good choice for topic number?)
// MAGIC   * Using a fixed vocabulary of k-mers (using nucleotide alphabet A-T-G-C) yields poor topic diversity [since there are sequencing errors in genomes, there are different k-mers such as TAK, TAR. And, we concluded that these errors actually give some insight on the virus]
// MAGIC * The comparison between LDA-based classifier and directly-k-mers classifier demonstrates that with enough iterations LDA is capable of summarising the data.
// MAGIC * There is not much difference between overlapping and nonoverlapping k-mer features when we directly give them to classifier. However, for LDA overlapping features require much higher number of iterations for convergence [or topic divergence]
// MAGIC * Very final conclusion: Directly giving k-mers to classifier works better. However, once we learn from LDA with reduced number of features (from k-mers to topic distributions), we can have a good result on classification which can save computation time and make it scalable (by reducing the number of features to the number of topics chosen).
// MAGIC 
// MAGIC “Everything should be made as simple as possible, but no simpler.” 
// MAGIC 
// MAGIC <img src="/files/shared_uploads/caylak@kth.se/1200px_Albert_Einstein_Head.jpg" alt="drawing" width="200"/>

// COMMAND ----------

