// Databricks notebook source
// MAGIC %md
// MAGIC # [ScaDaMaLe, Scalable Data Science and Distributed Machine Learning](https://lamastex.github.io/scalable-data-science/sds/3/x/)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC Dear Researcher,
// MAGIC 
// MAGIC The main form of assessment for day 01 of module 01 and both days of module 03 is most likely going to be a fairly open-ended project in groups of appropriate sizes (with some structure TBD) that is close to your research interests. The exact details will be given at the end of day 02 of module 03. There will most likely be dedicated Office Hours after module 03 to support you with the project (admin, infrastructure, etc).
// MAGIC 
// MAGIC Towards this, as one possibility for project, I strongly encourage you to apply for Twitter Developer Account (you need a Twitter user account first). This process can take couple of weeks. With Twitter developer account you can do your own experiments in Twitter and it would be an interesting application of streaming.
// MAGIC 
// MAGIC The instructions are roughly as follows (Twitter will ask different questions to different users... rules keep evolving... just make it clear you are just wanting credentials for learning Spark streaming. Keep it simple.):
// MAGIC https://lamastex.github.io/scalable-data-science/sds/basics/instructions/getTwitterDevCreds/ (Links to an external site.)
// MAGIC 
// MAGIC You can still follow the lab/lectures without your own Twitter Developer credentials IF I/we dynamically decide to go through Twitter examples of Streaming (provided at least one of you has applied for the developer credentials and is interested in such experiments), but then you will not be able to do your own experiments live in Twitter as I will be doing mine.
// MAGIC 
// MAGIC Twitter can be a fun source of interesting projects from uniformly sampled social media interactions in the world (but there are many other projects, especially those coming from your own research questions).
// MAGIC 
// MAGIC Cheers!
// MAGIC 
// MAGIC Raaz

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ## How to convince Twitter to give you developer credentials
// MAGIC 
// MAGIC Dear Student,
// MAGIC 
// MAGIC Great that you are trying to get Twitter Developer Credentials. Let me try to answer your question here so it can be of use to others. First, I have copy-pasted your query below. I will answer inline further below. But, in general, let's continue such discussions in studium here under this thread if other students are interested in Twitter projects. It is time-consuming to copy-paste responses from email threads.
// MAGIC 
// MAGIC Student
// MAGIC 
// MAGIC Hej Raazesh, Thank you for all your efforts in this course. I am trying to sign up for the Twitter developer account. However, I may have made a mistake by checking the box that I will analyze tweets. They have now twice sent me emails asking for the following information:
// MAGIC 
// MAGIC The core use case, intent, or business purpose for your use of the Twitter APIs.
// MAGIC Please note, “business purpose” in this context includes uses not necessarily connected to a commercial business. We require information about the problem, user story, or the overall goal your use of Twitter content is intended to address.
// MAGIC If you are a student, learning to code, or just getting started with the Twitter APIs, please provide details about potential projects, or areas of focus.
// MAGIC If you intend to analyze Tweets, Twitter users, or their content, please share details about the analyses you plan to conduct, and the methods or techniques.
// MAGIC Note that “analyze” in this context includes any form of processing performed on Twitter content. Please provide as detailed and exhaustive an explanation as possible of your intended use case.
// MAGIC If your use involves Tweeting, Retweeting, or liking content, share how you will interact with Twitter users or their content.
// MAGIC If you’ll display Twitter content off of Twitter, please explain how, and where, Tweets and Twitter content will be displayed to users of your product or service, including whether Tweets and Twitter content will be displayed at row level, or aggregated.
// MAGIC 
// MAGIC I tried to explain that I will only use the account for educational purposes, but I suspect I may have to give some details about a potential project if I intend to analyze tweets. Do you have a tip for how to proceed? Should I say I will not analyze tweets? Best regards, and have a nice weekend,
// MAGIC 
// MAGIC Student
// MAGIC 
// MAGIC On Fri, Nov 6, 2020 at 9:29 PM Student wrote:
// MAGIC 
// MAGIC Hej Raazesh,
// MAGIC 
// MAGIC  
// MAGIC Thank you for all your efforts in this course.
// MAGIC 
// MAGIC  
// MAGIC 
// MAGIC I am trying to sign up for the Twitter developer account. However, I may have made a mistake by checking the box that I will analyze tweets. They have now twice sent me emails asking for the following information:
// MAGIC 
// MAGIC No worries. Twitter has been tightening its use of the Developer access more and more. You just need to reassure them that you don't have any intentions to violate local laws or those of the Twitter user rules and Twitter Developer rules (you can find these online as they keep evolving). But let me help you make a quick response to them.
// MAGIC 
// MAGIC The core use case, intent, or business purpose for your use of the Twitter APIs.
// MAGIC Please note, “business purpose” in this context includes uses not necessarily connected to a commercial business. We require information about the problem, user story, or the overall goal your use of Twitter content is intended to address.
// MAGIC If you are a student, learning to code, or just getting started with the Twitter APIs, please provide details about potential projects, or areas of focus.
// MAGIC 
// MAGIC The core use case is to merely use Twitter as an streaming source to learn Apache Spark streaming and structured streaming in a PhD-level course in Sweden in the WASP AI-track Graduate School titled 'Scalable Data Science and Distributed Machine Learning - Module 3'. See https://wasp-sweden.org/graduate-school/ai-graduate-school-courses/ (Links to an external site.) (NOTE: give them the GitHub link to ScaDaMaLe or the studium link  if they ask for further details). I am a student learning to code and may do a potential project with Twitter data. Potential twitter projects, according to course instructor Raazesh Sainudiin (Department of Mathematics, Uppsala University, Sweden), need to conform strictly to Twitter user and developer rules. The project can be a demonstration of one of the streaming algorithms we will be learning in class or its latest variants in the literature. I will be working on databricks spark cluster under 'Databricks University Alliance supported by AWS' for this project. Examples of projects will be using the free 1% from the public Twitter streams and all data collected will be stored securely in AWS s3 buckets securely. Upon completion of the course, the data will be deleted. I am thinking of doing a project to look at XXX (REPLACE XXX with something you may want to do... PLEASE don't copy-paste this!!! Each student should write in your own words and have some potential project ideas in mind. Some examples of XXX are: 'looking at top hashtags of Tweets containing Swedish words after a simple language detection model based on total variation distance to the most frequent Swedish characters',  'using histogram sketching of the characters in the stream to quantify invariants in the global character distribution through time, looking at the distribution of Tweets mentioning US election related news in Swedish tweets during December after the US election, using hyperloglog to approximate the number of distinct users and compare it to the actual number of users for computational efficiency between different streaming implementations, etc.)
// MAGIC 
// MAGIC  
// MAGIC 
// MAGIC If you intend to analyze Tweets, Twitter users, or their content, please share details about the analyses you plan to conduct, and the methods or techniques.
// MAGIC Note that “analyze” in this context includes any form of processing performed on Twitter content. Please provide as detailed and exhaustive an explanation as possible of your intended use case.
// MAGIC 
// MAGIC You can use your XXX above to elaborate here. I understand that this is not easy as you are just getting started but Twitter wants to know if you will do something that will violate their user and developer rules. For example, do NOT say you will be analysing the location data of users in any geographical region and map their trajectories to manipulate their spatio-temporal location and status update habits... This is a direct violation of Twitter developer rules. READ: https://developer.twitter.com/en/developer-terms/agreement-and-policy (Links to an external site.) so you know what the parameters are in law.
// MAGIC 
// MAGIC  
// MAGIC 
// MAGIC If your use involves Tweeting, Retweeting, or liking content, share how you will interact with Twitter users or their content.
// MAGIC 
// MAGIC No, I will not be interacting with Twitter users or their content in any way. I will only be using the Twitter data to do my course project that is focused on applying my XXX/algorithm for the course project.
// MAGIC 
// MAGIC  
// MAGIC 
// MAGIC If you’ll display Twitter content off of Twitter, please explain how, and where, Tweets and Twitter content will be displayed to users of your product or service, including whether Tweets and Twitter content will be displayed at row level, or aggregated.
// MAGIC 
// MAGIC No, I will not be displaying any Twitter content off of Twitter. This is one of the instructions for any Twitter project from the course instructor Raazesh Sainudiin.
// MAGIC 
// MAGIC  
// MAGIC 
// MAGIC I tried to explain that I will only use the account for educational purposes, but I suspect I may have to give some details about a potential project if I intend to analyze tweets. Do you have a tip for how to proceed? Should I say I will not analyze tweets?
// MAGIC 
// MAGIC  
// MAGIC 
// MAGIC Best regards, and have a nice weekend,
// MAGIC 
// MAGIC Student
// MAGIC 
// MAGIC 
// MAGIC 
// MAGIC Raazesh Sainudiin
// MAGIC lamastex.org