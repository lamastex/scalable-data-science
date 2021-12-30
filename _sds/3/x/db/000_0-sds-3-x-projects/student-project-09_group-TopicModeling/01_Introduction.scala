// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md # Topic Modeling with SARS-Cov-2 Genome 🧬

// COMMAND ----------

// MAGIC %md Group Project Authors:
// MAGIC 
// MAGIC - Hugo Werner
// MAGIC - Gizem Çaylak  [e-mail](mailto:caylak@kth.se)

// COMMAND ----------

// MAGIC %md Video link: https://kth.box.com/s/y3jsb9lgp6cll6op15o6z77rchaefh24

// COMMAND ----------

// MAGIC %md #### Problem description: 
// MAGIC SARS-CoV-2 is spreading across the world and as it spreads mutations are occuring. A way to understand the spreading and the mutations is to explore the structure and information hidden in the genome. 

// COMMAND ----------

// MAGIC %md #### Project goal: 
// MAGIC The Goal of this project is to explore a SARS-CoV-2 genome dataset and try to predict the origin of a SARS-CoV-2 genome sample.

// COMMAND ----------

// MAGIC %md #### Data: 
// MAGIC We will use publicly available NCBI SARS-CoV-2 genome with their geographic region information. 
// MAGIC 
// MAGIC | Geographic region 	| #samples 	|
// MAGIC |:-----------------:	|:--------:	|
// MAGIC |    Africa    	|    397   	|	
// MAGIC |   Asia  	|     2534    	|
// MAGIC |      Europe       	|     1418    	|
// MAGIC |       North America       	|     26836    	|
// MAGIC |       Oceania      	|     13304    	|
// MAGIC |        South America        	|     158    	|
// MAGIC 
// MAGIC Data link: https://www.ncbi.nlm.nih.gov/labs/virus/vssi/#/virus?SeqType_s=Nucleotide&VirusLineage_ss=Severe%20acute%20respiratory%20syndrome%20coronavirus%202%20(SARS-CoV-2),%20taxid:2697049

// COMMAND ----------

// MAGIC %md #### Background: 
// MAGIC * Genome: Sequence of nucleotides (A-T-G-C) [https://en.wikipedia.org/wiki/Genome]
// MAGIC * k-mer: Subsequences of length k of a genome [https://en.wikipedia.org/wiki/K-mer]
// MAGIC * Sequence analysis of SARS-CoV-2 genome reveals features important for vaccine design [https://www.nature.com/articles/s41598-020-72533-2]
// MAGIC * Latent Dirichlet Allocation (LDA) tutorial from the course

// COMMAND ----------

// MAGIC %md #### Challenges:
// MAGIC * How to encode genome sequence?
// MAGIC   * *Project solution :* 
// MAGIC     * Represent genome as k-mers and use countVectorizer to convert k-mers into a matrix of token counts (term-frequency table)
// MAGIC     * Extract features with Latent Dirichlet Allocation (LDA) by considering each genome sequence as a document and each 3-mer as a word. So, we have a collection of genomes consisting of 3-mers.
// MAGIC * How to relate encoded features to the origins?
// MAGIC   * *Project solution:* We used a Random Forest Classifier and tried both topic distributions, LDA output, and k-mer frequencies directly. One advantage is interpretability: we can understand the positive or negative relations a topic has on the origin. 
// MAGIC * How to solve unbalanced class problem? E.g. North America has 26836 samples but South America has only 158
// MAGIC   * *Project solution:* Use f1 measure as metric

// COMMAND ----------

// MAGIC %md #### Project steps:
// MAGIC 1. Get SARS-CoV-2 data from NCBI 
// MAGIC 1. Process data:
// MAGIC   1. Extract 3-mers:
// MAGIC   1. Split train/test dataset with split ratio 0.7
// MAGIC 1. Extract topic features: We used Latent Dirichlet Allocation to extract patterns from k-mers features.
// MAGIC 1. Classify: We used Random Forest Classifier
// MAGIC     * (Classification directly on k-mers features) To find a mapping from extracted k-mers features to labels (multiclass problem).
// MAGIC     * (Classification on LDA features) To find a mapping from extracted topic distributions to labels (multiclass problem).
// MAGIC 1. Evaluation: We use (accuracy and f1) measure as our evaluation metrics. We compared Classification on LDA features vs Classification directly on k-mers features to see whether LDA is capable of summarizing the data (and thus reducing the feature dimensionality)

// COMMAND ----------

// MAGIC %md #### What we lack mainly:
// MAGIC * A biological interpretation of the results (whether found terms in topic distributions are significant/connected in a biological network).