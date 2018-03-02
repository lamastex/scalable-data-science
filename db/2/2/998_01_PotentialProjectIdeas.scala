// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC # List of pointers to potential course projects
// MAGIC 
// MAGIC ## from 2016 and 2017

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ## Theoretical Projects in Scalable Data Science
// MAGIC 
// MAGIC 1. Exact Matrix Completion via Convex Optimization
// MAGIC  * **Abstract** 
// MAGIC     - Suppose that one observes an incomplete subset of entries selected from a low-rank matrix. When is it possible to complete the matrix and recover the entries that have not been seen? We demonstrate that in very general settings, one can perfectly recover all of the missing entries from most sufficiently large subsets by solving a convex programming problem that finds the matrix with the minimum nuclear norm agreeing with the observed entries. The techniques used in this analysis draw upon parallels in the field of compressed sensing, demonstrating that objects other than signals and images can be perfectly reconstructed from very limited information.
// MAGIC 
// MAGIC  * As published in [DOI:10.1145/2184319.2184343](http://dl.acm.org/citation.cfm?id=2184319.2184343)
// MAGIC  * Originally published in [FCM 9, 6 (2009)](http://link.springer.com/article/10.1007/s10208-009-9045-5#page-1)
// MAGIC  
// MAGIC * Data Science and Prediction
// MAGIC  -  Big data promises automated actionable knowledge creation and predictive models for use by both humans and computers.
// MAGIC  - [DOI:10.1145/2500499](http://dl.acm.org/citation.cfm?id=2500499)
// MAGIC  
// MAGIC 
// MAGIC ## Applied Projects in Scalable Data Science
// MAGIC 
// MAGIC  1. Content Recommendation on Web Portals
// MAGIC    * How to offer recommendations to users when they have not specified what they want.
// MAGIC    * [DOI:10.1145/2461256.2461277](http://dl.acm.org/citation.cfm?id=2461277)
// MAGIC  * Techniques and Applications for Sentiment Analysis
// MAGIC    * The main applications and challenges of one of the hottest research areas in computer science.
// MAGIC    * [DOI:10.1145/2436256.2436274](http://dl.acm.org/citation.cfm?id=2436274&picked=formats)
// MAGIC  * Looking back at big data (Digital humanities)
// MAGIC    * As computational tools open up new ways of understanding history, historians and computer scientists are working together to explore the possibilities.
// MAGIC    * [DOI:10.1145/2436256.2436263](http://dl.acm.org/citation.cfm?id=2436256.2436263)
// MAGIC  * Computational Epidemiology
// MAGIC    * The challenge of developing and using computer models to understand and control the diffusion of disease through populations.
// MAGIC    * [DOI:10.1145/2483852.2483871](http://dl.acm.org/citation.cfm?id=2483871&picked=formats)
// MAGIC  * Replicated Data Consistency Explained Through Baseball
// MAGIC    * A broader class of consistency guarantees can, and perhaps should, be offered to clients that read shared data.
// MAGIC    * [DOI:10.1145/2500500](http://dl.acm.org/citation.cfm?id=2500500)
// MAGIC  * Community Sense and Response Systems: Your phone as a quake detector
// MAGIC    * The Caltech CSN project collects sensor data from thousands of personal devices for real-time response to dangerous earthquakes.
// MAGIC    * [DOI:10.1145/2622628.2622633](http://dl.acm.org/citation.cfm?id=2622633&dl=ACM&coll=DL&CFID=585754227&CFTOKEN=14268563)
// MAGIC  * Reshaping (non-State) Terrorist Networks (fields: State/national security, social psychology, counter-recruitment, counter/de-radicalization...)
// MAGIC    * To destabilize terrorist organizations, the <code>STONE</code> algorithms identify a set of operatives whose removal would maximally reduce lethality.
// MAGIC    * [DOI:10.1145/2632661.2632664](http://dl.acm.org/citation.cfm?id=2632664)
// MAGIC  * Rise of Hate Groups in the US (fields: social psychology, understanding online emergence of "hate groups", ...)
// MAGIC    * watch Democracy Now story on [This Year (2016) in Hate and Extremism](http://www.democracynow.org/2016/2/25/winning_his_race_is_trump_the)
// MAGIC    * read [https://www.splcenter.org/intelligence-report](https://www.splcenter.org/intelligence-report), The Intelligence Report is the Southern Poverty Law Center's award-winning magazine. The quarterly publication provides comprehensive updates to law enforcement agencies, the media and the general public. See several articles on different 'hate groups' published on February 17, 2016.
// MAGIC  * New News Aggregator Apps (ML at work)
// MAGIC     * How apps like Inkl and SmartNews are overcoming the challenges of aggregation to win over content publishers and users alike
// MAGIC     * [DOI:10.1145/2800445](http://dl.acm.org/citation.cfm?id=2800445)
// MAGIC     * __References:__
// MAGIC        * [Himabindu Lakkaraju , Angshu Rai , Srujana Merugu, Smart news feeds for social networks using scalable joint latent factor models, Proceedings of the 20th international conference companion on World wide web, March 28-April 01, 2011, Hyderabad, India](http://dl.acm.org/citation.cfm?id=1963230&CFID=758221069&CFTOKEN=17750149)
// MAGIC        * [Benedict C. May , Nathan Korda , Anthony Lee , David S. Leslie, Optimistic Bayesian sampling in contextual-bandit problems, The Journal of Machine Learning Research, 13, p.2069-2106, 3/1/2012](http://dl.acm.org/citation.cfm?id=2343711&CFID=758221069&CFTOKEN=17750149)
// MAGIC        * [Eli Pariser, The Filter Bubble: What the Internet Is Hiding from You, Penguin Group , The, 2011](http://www.amazon.com/The-Filter-Bubble-Personalized-Changing/dp/0143121235)
// MAGIC        * [Rikiya Takahashi, Tetsuro Morimura, Predicting Preference Reversals via Gaussian Process Uncertainty Aversion](http://jmlr.org/proceedings/papers/v38/takahashi15.html)
// MAGIC  * Natural Language Translation at the Intersection of AI and HCI
// MAGIC    * __Abstract__: The fields of artificial intelligence (AI) and human-computer interaction (HCI) are influencing each other like never before. Widely used systems such as Google Translate, Facebook Graph Search, and RelateIQ hide the complexity of large-scale AI systems behind intuitive interfaces. But relations were not always so auspicious. The two fields emerged at different points in the history of computer science, with different influences, ambitions, and attendant biases. AI aimed to construct a rival, and perhaps a successor, to the human intellect. Early AI researchers such as McCarthy, Minsky, and Shannon were mathematicians by training, so theorem-proving and formal models were attractive research directions. In contrast, HCI focused more on empirical approaches to usability and human factors, both of which generally aim to make machines more useful to humans. Many attendees at the first CHI conference in 1983 were psychologists and engineers. Presented papers had titles such as "Design Principles for Human-Computer Interfaces" and "Psychological Issues in the Use of Icons in Command Menus," hardly appealing fare for mainstream AI researchers. 
// MAGIC 
// MAGIC     Since the 1960s, HCI has often been ascendant when setbacks in AI occurred, with successes and failures in the two fields redirecting mindshare and research funding.14 Although early figures such as Allen Newell and Herbert Simon made fundamental contributions to both fields, the competition and relative lack of dialogue between AI and HCI are curious. Both fields are broadly concerned with the connection between machines and intelligent human agents. What has changed recently is the deployment and adoption of user-facing AI systems. These systems need interfaces, leading to natural meeting points between the two fields.
// MAGIC    * [DOI:10.1145/2767151](http://dl.acm.org/citation.cfm?id=2767151&picked=formats)
// MAGIC  * Sensing Emotions
// MAGIC    * How computer systems detect the internal emotional states of users.
// MAGIC    * [DOI:10.1145/2800498](http://dl.acm.org/citation.cfm?id=2800498)
// MAGIC    * Further Reading:
// MAGIC      * [Rosalind W. Picard, Affective computing, MIT Press, Cambridge, MA, 1997](https://mitpress.mit.edu/books/affective-computing)
// MAGIC       * The latest scientific findings indicate that emotions play an essential role in decision making, perception, learning, and more—that is, they influence the very mechanisms of rational thinking. Not only too much, but too little emotion can impair decision making. According to Rosalind Picard, if we want computers to be genuinely intelligent and to interact naturally with us, we must give computers the ability to recognize, understand, even to have and express emotions.
// MAGIC      * [Rafael A. Calvo , Sidney D'Mello , Jonathan Gratch , Arvid Kappas, The Oxford Handbook of Affective Computing, Oxford University Press, Oxford, 2014](http://www.oxfordhandbooks.com/view/10.1093/oxfordhb/9780199942237.001.0001/oxfordhb-9780199942237)
// MAGIC       * The Oxford Handbook of Affective Computing is a definitive reference in the burgeoning field of affective computing (AC)
// MAGIC      * [Bartlett, M., Littlewort, G., Frank, M., and Lee, K. Automated Detection of Deceptive Facial Expressions of Pain, Current Biology, 2014.](http://www.sciencedirect.com/science/article/pii/S096098221400147X)
// MAGIC        * Highlights
// MAGIC          * Untrained human observers cannot differentiate faked from genuine pain expressions
// MAGIC          * With training, human performance is above chance but remains poor
// MAGIC          * A computer vision system distinguishes faked from genuine pain better than humans
// MAGIC          * The system detected distinctive dynamic features of expression missed by humans
// MAGIC     * [Carlos Busso , Zhigang Deng , Serdar Yildirim , Murtaza Bulut , Chul Min Lee , Abe Kazemzadeh , Sungbok Lee , Ulrich Neumann , Shrikanth Narayanan, Analysis of emotion recognition using facial expressions, speech and multimodal information, Proceedings of the 6th international conference on Multimodal interfaces, October 13-15, 2004, State College, PA, USA](http://dl.acm.org/citation.cfm?id=1027968&CFID=758221069&CFTOKEN=17750149)
// MAGIC          * __Abstract:__The interaction between human beings and computers will be more natural if computers are able to perceive and respond to human non-verbal communication such as emotions. Although several approaches have been proposed to recognize human emotions based on facial expressions or speech, relatively limited work has been done to fuse these two, and other, modalities to improve the accuracy and robustness of the emotion recognition system. This paper analyzes the strengths and the limitations of systems based only on facial expressions or acoustic information. It also discusses two approaches used to fuse these two modalities: decision level and feature level integration. Using a database recorded from an actress, four emotions were classified: sadness, anger, happiness, and neutral state. By the use of markers on her face, detailed facial motions were captured with motion capture, in conjunction with simultaneous speech recordings. The results reveal that the system based on facial expression gave better performance than the system based on just acoustic information for the emotions considered. Results also show the complementarily of the two modalities and that when these two modalities are fused, the performance and the robustness of the emotion recognition system improve measurably.
// MAGIC  * Putting the Data Science into Journalism
// MAGIC    * News organizations increasingly use techniques like data mining, Web scraping, and data visualization to uncover information that would be impossible to identify and present manually.
// MAGIC    * [DOI:10.1145/2742484](http://dl.acm.org/citation.cfm?id=2742484&picked=formats)
// MAGIC 
// MAGIC  * Big Data Meets Big Science (Extra Reading)
// MAGIC    * Next-generation scientific instruments are forcing researchers to question the limits of massively parallel computing.
// MAGIC    * [DOI:10.1145/2617660](http://dl.acm.org/citation.cfm?id=2617660)
// MAGIC   * Big Data and its techincal challenges (Extra reading)
// MAGIC    * Exploring the inherent technical challenges in realizing the potential of Big Data.
// MAGIC    * [DOI:10.1145/2611567](http://dl.acm.org/citation.cfm?id=2622628.2611567)
// MAGIC  * Exascale Computing and Big Data (Extra Reading)
// MAGIC    * Scientific discovery and engineering innovation requires unifying traditionally separated high-performance computing and big data analytics.
// MAGIC      The twin ecosystems of HPC and big data and the challenges facing both
// MAGIC    * [DOI:10.1145/2699414](http://dl.acm.org/citation.cfm?doid=2797100.2699414)
// MAGIC    * Watch [https://www.youtube.com/watch?list=PLn0nrSd4xjjbIHhktZoVlZuj2MbrBBC_f&v=eLMChVev6hw](https://www.youtube.com/watch?list=PLn0nrSd4xjjbIHhktZoVlZuj2MbrBBC_f&v=eLMChVev6hw)
// MAGIC  * Battling Evil: Dark Silicon (Extra Reading)
// MAGIC    * The changing nature of computing as chips with more transistors than can be concurrently activated become more commonplace.
// MAGIC    * Read [http://www.hpcdan.org/reeds_ruminations/2011/05/battling-evil-dark-silicon.html](http://www.hpcdan.org/reeds_ruminations/2011/05/battling-evil-dark-silicon.html)
// MAGIC  
// MAGIC  * TensorFlow: Google Open Sources Their Machine Learning Tool (see [InfoQ](http://www.infoq.com/news/2015/11/tensorflow))
// MAGIC     * TensorFlow is a machine learning library created by the Brain Team researchers at Google and now open sourced under the Apache License 2.0. TensorFlow is detailed in the whitepaper TensorFlow: Large-Scale Machine Learning on Heterogeneous Distributed Systems. The source code can be found on Google Git. It is a tool for writing and executing machine learning algorithms. Computations are done in a data flow graph where the nodes are mathematical operations and the edges are tensors (multidimensional data arrays) that are exchanged between nodes. An user constructs the graph and writes the algorithms that executed on each node. TensorFlow takes care of executing the code asynchronously on different devices, cores, and threads.... TensorFlow is used by Google for GMail (SmartReply), Search (RankBrain), Pictures (Inception Image Classification Model), Translator (Character Recognition), and other products.   
// MAGIC     * See [https://databricks.com/blog/2016/01/25/deep-learning-with-spark-and-tensorflow.html](https://databricks.com/blog/2016/01/25/deep-learning-with-spark-and-tensorflow.html)

// COMMAND ----------

// MAGIC %md
// MAGIC Keep reading... I have not updated since early 2017!!!
// MAGIC 
// MAGIC * Association for Computing Machinery (ACM) Communications is a nice central point for a quick overview into current computationallu focussed mathematical sciences.
// MAGIC * PNAS/Science/Nature - usual popular science venues
// MAGIC * Hacker News
// MAGIC * ...

// COMMAND ----------

// MAGIC %md
// MAGIC # Shared Student Notebooks for sds-2.2
// MAGIC  Several notebooks that stduents tried along the course are part of the course content 
// MAGIC  
// MAGIC * Yevgen [text analysis of Russian words](https://databricks-prod-cloudfront.cloud.databricks.com/public/4027ec902e239c93eaaa8714f173bcfc/6005499967952011/3389795813726420/4712481027151699/latest.html)