# Peer-review of ScaDaMaLe Student Group Project Number 13

# Reviewer

Lukas Borggren

Choose one of the following option: 

- Anonymous 
- Fill-in-your-FirstName Fill-in-your-LastName

This is a brief template to guide you through the peer-review process.
It is meant to make recommendations for assigning between 0 and 10 points as a grade.
This grade will be used to decide the *Best Group Project Prize*.

Note that your feedback will be valuable to the course instructor, Raazesh Sainudiin (Raaz).
But more crucially, it can help improve the presentation of the project so the group can take your suggestions to improve the final version that will be published by the end of the course in:

- https://lamastex.github.io/ScaDaMaLe/

IMPORTANT: The decision of whether a group project meets the minimal requirements to pass the course (this is just a Pass/Fail course) will be made by your instructor by taking your peer-review into account.

# Set-up and Overview

- Go to the public link for the Group Project repository that you have been assigned to for your peer-review.
- You will be doing the peer-review constructively (You may clone the repository and try out codes yourself).
- Recall/watch the live/video presentation and go through the repository according to the below guidelines.
- You will be giving points under various categories and you have to report the final grade in the Course Canvas page.
- Please attach the completed version of this template plain text markdown file `PEER_REVIEW.md` to  Canvas to complete the peer-review by the deadline.

# 1. Live/Video Presentation

2

Recall/watch the live/video presentation carefully and decide on a score between 0 and 2.

Choose one of the following options:

- 0 : The presentation was completely incomprehensible after accounting for my background knowledge or lack thereof. 
- 1 : The presentation was reasonably comprehensible and it helped me follow the codes and explanations relatively easily.
- 2 : The presentation was easily comprehensible and it helped me follow the project easily. I think I can definitely use the codes in the repository and adapt them if I were to encounter similar problems in the future, with possibly more in-depth self-study as needed.

# 2. Structure of Project Repository

2

The structure of the codes, including directory structure and coding/software-engineering practices,  were  

Choose one of the following options:

- 0 : Extremely poorly organised and incomprehensible.
- 1 : Reasonably organised and could be improved (please consider making suggestions for improvement in Section 8 below) for easier comprehension.
- 2 : Extremely organised and easily comprehensible and/or replicable.

# 3. Comments and explanations in code:

2

Choose one of the following options:

- 0 : There were no comments at all in the code and not enough markdown/documentation explaining what the code was supposed to be doing. 
- 1 : There were minimal comments in the code but the markdown/documentation explained briefly what the code was supposed to be doing at least at a high level.
- 2 : The code was well-commented so I could easily follow what was happening in most code blocks.

# 4. Originality or Difficulty of the Project

1

Choose one of the following options:

- 0 : The project is just a minor adaptation (less than 10%) of an existing openly available project that is very close to this, including data analysed, tools used, identical versions of software, and conclusions reached.
- 1 : The project is similar to an existing openly available project but significant contributions seem to be made in software versions, data used, explanations given and conclusions reached.
- 2 : The project seems to have tackled a very challenging problem and has made significant contributions to aid other researchers or practitioners in a particular domain of expertise.
# 5. Scalability of the Project

2

Choose one of the following options:

- 0 : The project is not at all scalable. There is no need to do this project in a distributed computing environmenfinnickyt (explain in Section 8 below why you think so).  
- 1 : The project has some scalable aspects or ideas on how scalability can be achieved. 
- 2 : The project is truly implementing a *scalable data science process* and the same code can work with arbitrarily large input data if enough computing resources are available.

# 6. Total Grade

Add up all the scores from the above 5 Categories and report it below.

The Total Grade is: 9

# 7. Completing Peer-review Process

- Add this **Total Grade** as an integer to the LMS/Studium to complete your peer-review by the deadline.
- Attach you completed version of this file `PEER_REVIEW.md` to the LMS to complete the peer-review by the deadline.

# 8. Detailed Constructive Comments

Very thorough motivation, presentation and documentation of the project, both of the methodology and the actual implementation. The `report.md` is a great resource for grasping the overall problem statement and approach taken, making the code base easily comprehensible afterwards. The code itself is clearly and consistently written and the repository is well-organized. If I would slightly nitpick, the codebase could be even cleaner and more navigable if it was divided into Python modules/scripts, rather than being a single Jupyter notebook. I would also assume that the training could be launched in a more robust manner that way, since it can be bit finicky to keep a Jupyter kernel alive for extended period of times if, for example, the training data would be larger.

To my knowledge – and after doing some complementary Googling – using autoencoders for time series anomaly detection is seemingly an established practice, with some projects even applying the method to the ECG5000 dataset. However, I believe the project has its novelty merits in the distributed ensemble learning approach, as well as through the use of PySpark and TorchDistrbutor. To my eyes, the project is – possibly with some slight modifications of the codebase – inherently scalable, since it allows for horizontal scaling, allowing larger data sizes. Additionally, vertical scaling would also allow larger model sizes, to a certain extent.

Please feel free to make detailed comments here in a constructive manner to help the group you are reviewing, so that they can make their presentation much better for the ScaDaMaLe-WASP Peer-reviewed Group Projects.