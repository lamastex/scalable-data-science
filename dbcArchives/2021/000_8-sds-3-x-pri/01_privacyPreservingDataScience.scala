// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// DBTITLE 1,Privacy Preserving Data Science: A Guide With Implementations 
// MAGIC %md
// MAGIC 
// MAGIC ### [by Christoffer Långström](https://www.linkedin.com/in/christoffer-l%C3%A5ngstr%C3%B6m-7a460514b/)
// MAGIC This notebook series will give an overview on the current state of privacy protection mechanisms, both in terms of data sanitation (which involves enforces privacy guarantees on the data itself) as well as algorithmic sanitation, which involves privacy inducing mechanism in statistical learning algorithms. This is part of a masters project in applied mathematics 
// MAGIC by Christoffer Långström under the supervision of Raazeesh Sainudiin and is partly inspired by the great work presented in the following talk at the 2018 Spark AI summit by Sim Semeonov & Slater Victoroff.
// MAGIC 
// MAGIC This work aims to recreate, elaborate & extend the material presented above, going through the current state of privacy in statistical learning, with both technical & 
// MAGIC mathematical perspectives. Later, a full reconstruction of the Databricks notebooks presented above will be given with comments and explanations in order to facilitate 
// MAGIC use. 

// COMMAND ----------

// DBTITLE 1,The Problem Of Data Privacy
// MAGIC %md
// MAGIC 
// MAGIC Protecting the privacy of individuals included in a data set is both highly important & suprisingly difficult. The importance is clear; sensitive information being leaked to adversarial parties can be harmful to anyone with personal information contained in a data set (which in this age is almost everyone), yet institutions such as medical researchers & academics are in great need of such data. Thankfully, personally identifiable information (here on referred to as pii) such as names and social numbers are rarely of any interest to the research, and is merely used by the data collector to ensure that the data corresponds to an actual individual. A solution could then be to use Pseudonomyous identifiers, where unique identifier is generated for each data entry in such a way that one cannot rediscover the original identity from the data set alone. This is the main focus in the talk by Simenov & Victoroff, which is described in part 2. 
// MAGIC 
// MAGIC As it turns out however, this is not the only privacy measure needed. In a very interesting case study, [Latanya Sweeney showed](http://www.cs.pomona.edu/~sara/classes/cs190-fall12/k-anonymity.pdf ) that given anonymised medical records, along with publicly avaliable voter registration records, she was able to discover medical information (such as conditions, treatments & medications prescribed) regarding the then governor of Massachusetts. She did this by a join attack; the voter registration data contained name, address, ZIP code, birth date, and gender of each voter and in the medical data there was only one person with the governors ZIP code, gender and date of birth. This introduces the notion of quasi-identifiers, pieces of information that while not sensitive on its own, can be combined together with other pieces of non-sensitive data to reveal sensitive information. This can be adressed through the concepts of k-anonymity and some derivative methods that introduce "reasonable doubt" to the identity of the person behind the data. 
// MAGIC 
// MAGIC Further, we will take a look at differential privacy, which adresses the problem presented in example 1, that inclusion/removal is itself privacy compromising. Differential privacy aims to provide a statistical framework to measure how private an estimator is, more specifically how sensitive the output of an estimation procedure is to the inclusion/removal of an entry in the set. The main approach to increasing the privacy of a given estimator is by introducing some stochastic distortion to the computations to obtain a desired degree of privacy. This approach is not without its flaws; most notably how it decreases the accuracy of estimators.  

// COMMAND ----------

// DBTITLE 0,GDPR: What Does A Data Scientist Need To Know?
// MAGIC %md
// MAGIC ### What The GDPR Requires From Data Scientists:
// MAGIC 
// MAGIC The GDPR now puts it into law that the holder and utilizer of data is responisble for its safe keeping by enforcing certain practices regarding the storage of the data. In practice, this is broken down into 3* categories.
// MAGIC 
// MAGIC #### 1) Data Protection & Records of Processing
// MAGIC The GDPR specifes that by design data must be protected and encryption should be done locally, not utilizing any outside service. This will be relevant in the process of pseudonymization decribed below.
// MAGIC Whenever the data is processed, records must be kept of the processing done, its purpose, what data was utilized and a projected time plan for how long the result will be used. 
// MAGIC 
// MAGIC Key Points:  
// MAGIC 
// MAGIC * Encryption/decryption must be done locally
// MAGIC * Keep records of what you used the data for; [see](https://gdpr-info.eu/issues/records-of-processing-activities/)
// MAGIC 
// MAGIC ##### Details
// MAGIC 
// MAGIC That part is in reference to this line " In particular, such measures shall ensure that by default personal data are not made accessible without the individual's intervention to an indefinite number of natural persons." from Article 25:
// MAGIC - https://www.privacy-regulation.eu/en/article-25-data-protection-by-design-and-by-default-GDPR.htm
// MAGIC 
// MAGIC In a report by ENISA (Eu Network and information security agency) they make the explicit claim that you should do local encryption (p. 20)
// MAGIC - https://web.archive.org/web/20170405171636/https://www.enisa.europa.eu/publications/privacy-and-data-protection-by-design
// MAGIC 
// MAGIC #### 2) Pseudonymization & The Right Of Access 
// MAGIC 
// MAGIC 
// MAGIC ##### - Direct Identifiers 
// MAGIC 
// MAGIC <img src="http://i.imgur.com/pLIOVvp.png" alt="drawing" width="600">    
// MAGIC 
// MAGIC * An identifier is any attribute that can be used to directly identify an individual in your data set, such as names & social numbers. 
// MAGIC * The GDPR stipulates that the data holder must provide proper protection on identifiers in the form of pseudonymization. 
// MAGIC 
// MAGIC The purpose of pseudonymization is to adress the fact that rarely do data holders need direct identifiers to perform analysis, and so aims to enforce that each such identifier is to be replaced with a pseudonym, useful only to distinguish members of a data set from each other. Another principle of the law is that any individual in the data set should at any time be able to query the holder on what information is being kept about them. Combining these two principles, it becomes clear that the pseudonymization must be caried out in such a way that it is effective, but also (partially) reversible, at least to an extent that given an identifier we can find what information is being kept on this subject.    
// MAGIC 
// MAGIC 
// MAGIC #### 3) The Right Of Erasure
// MAGIC 
// MAGIC A data subject also has the right to at any time request for their data to be removed from the data set. Again, this requires that any pseudonomyzation must be query-able to find a certain individual, and that the pseudonyms cannot be purely destructive. As we well investigate later, this act of removal poses its own risks in terms of privacy exposure as the below example demonstrates.
// MAGIC 
// MAGIC * Note: The right of erasure is a limited right, meaning that the data holder does not always have to comply. 
// MAGIC 
// MAGIC ##### When do we *not* need to delete the data? 
// MAGIC 1) The personal data your company/organisation  holds is needed to exercise the right of freedom of expression
// MAGIC 
// MAGIC 2) There is a legal obligation to keep that data
// MAGIC 
// MAGIC 3) For reasons of public interest (for example public health, scientific, statistical or historical research purposes)
// MAGIC 
// MAGIC * For more details see: (https://gdpr-info.eu/art-17-gdpr/) and [Do we always have to delete personal data if a person asks?](https://ec.europa.eu/info/law/law-topic/data-protection/reform/rules-business-and-organisations/dealing-citizens/do-we-always-have-delete-personal-data-if-person-asks_en)
// MAGIC 
// MAGIC 
// MAGIC * Notice that "Commercial Purposes" does not fit in to any of these options.
// MAGIC 
// MAGIC ##### Example of Privacy Concerns With Erasure: 
// MAGIC Suppose there is a data set containing individuals exercise habits, categorizing them as "runners" or "non-runners". You request the number of runners in the set, and recieve the answer 49. This in itself does not give any information on any individual in the set. Then, Alice requests to have her data removed from the set. You run the query again, and discover that there are now only 48 runners in the set, and so you have obtained information regarding Alice just by her wanting her data removed. This problem leads to the subject of query-level privacy and differential privacy which will be discussed later. 

// COMMAND ----------

// DBTITLE 1,GDPR: More Details
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

displayHTML(frameIt("https://en.wikipedia.org/wiki/General_Data_Protection_Regulation",350))



// COMMAND ----------

// DBTITLE 1,GDPR Links:
// MAGIC %md
// MAGIC * [EU Offical Site On GDPR](https://ec.europa.eu/commission/priorities/justice-and-fundamental-rights/data-protection/2018-reform-eu-data-protection-rules_en)
// MAGIC 
// MAGIC * [Swedish Agency Datainspektionen on GDPR](https://www.datainspektionen.se/lagar--regler/dataskyddsforordningen/)
// MAGIC 
// MAGIC * [eugdpr.org (Unoffical Resource On GDPR for Organizations & Companies)](https://eugdpr.org/)

// COMMAND ----------

// DBTITLE 1,What Does The GDPR Say About (Deep) Learning Models?
// MAGIC %md 
// MAGIC 
// MAGIC #### 4) The Right Of Explanation*
// MAGIC 
// MAGIC One concept in the GDPR is that individuals have the right to have it "explained" why decisions made by an algorithm using their data were made. This is relevant to any sort of predictive modelling, and classifiers that places individuals in categories. But what exactly does explanation mean? Does "the algorithm said so" suffice? And how does this affect black box models?
// MAGIC 
// MAGIC It is already a well known problem in ML applications that models can lack human interpretations, so how can these be used if explainability is required? 
// MAGIC 
// MAGIC Art. 13 of the GDPR: 
// MAGIC > "... the controller shall, at the time when personal data are obtained, provide the data subject with the following further information necessary to ensure fair and transparent processing:  ...meaningful information about the logic involved, as well as the significance and the envisaged consequences of such processing for the data subject"
// MAGIC 
// MAGIC This makes a statement on both the operational workings, "the logic involved" *and* a more human readable understanding on the meaning of the outcome.  Recital 71 (a non-legally binding explanation of the intents of the law) states: 
// MAGIC 
// MAGIC > “(automated processing)... should be subject to suitable safeguards, which should include specific information to the data subject and the right to obtain human intervention, to express his or her point of view, to obtain an explanation of the decision reached after such assessment and to challenge the decision."
// MAGIC 
// MAGIC The intent seems to be to give members of a data set insight in how to their data being processed might affect them, and to give them sufficient information as to be able to make the decision on whether to be included in the set.  This leads to a "best practices" checklist for model building:  
// MAGIC 
// MAGIC 
// MAGIC 1) **Technical Report**: 
// MAGIC Detail from where the data was gathered, and what features where selected for in the model training. For the model(s) used, write them down and the motivation for their choice. 
// MAGIC 
// MAGIC 2) **Understand Deployment**: 
// MAGIC Make a detailed list on what the model will be used for, and the possible (direct) consequences for an individual included in the data set. Study the effects of false positives and false negatives, since this will help you comply with the "significance" part of art. 13. 
// MAGIC 
// MAGIC 3) **Educate The Subject**: Together with the above material, list the possible pros/cons of being included in the data set and present the information about the model that best relate to these aspects. 
// MAGIC 
// MAGIC The above list aims to help data scientist comply with any questions they may recieve on the "logic" and "significance" part of art. 13.

// COMMAND ----------

// DBTITLE 1,Part 2: Pseudonymization 
// MAGIC %md
// MAGIC See the notebook: 
// MAGIC https://databricks-prod-cloudfront.cloud.databricks.com/public/4027ec902e239c93eaaa8714f173bcfc/806262291388233/586373581842786/4884583572804914/latest.html

// COMMAND ----------

// DBTITLE 1,Part 3: Differential Privacy
// MAGIC %md
// MAGIC 
// MAGIC See the notebook: https://databricks-prod-cloudfront.cloud.databricks.com/public/4027ec902e239c93eaaa8714f173bcfc/806262291388233/365321721214438/4884583572804914/latest.html

// COMMAND ----------

// DBTITLE 1,Part 4: Deep Learning with Privacy
coming soon

// COMMAND ----------

// DBTITLE 1,Part 5: Private Graph Network Analysis
coming soon