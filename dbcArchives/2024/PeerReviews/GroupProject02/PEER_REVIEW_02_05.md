# Peer-review of ScaDaMaLe Student Group Project Number <2>

# Reviewer

Choose one of the following option: 

- Anonymous 
- Fill-in-your-FirstName Fill-in-your-LastName

Xavante Erickson

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

Recall/watch the live/video presentation carefully and decide on a score between 0 and 2.

Choose one of the following options:

- 0 : The presentation was completely incomprehensible after accounting for my background knowledge or lack thereof. 
- 1 : The presentation was reasonably comprehensible and it helped me follow the codes and explanations relatively easily.
- 2 : The presentation was easily comprehensible and it helped me follow the project easily. I think I can definitely use the codes in the repository and adapt them if I were to encounter similar problems in the future, with possibly more in-depth self-study as needed.

Feedback: 2
The Presentation was very comprehensible. They clearly stated the goal, method and results.
They presented the method well and included both visual cues and actual code snippets.
Personally, the presentation could be improved with a bit less of a wall of text (excluding code snippet of course).
Self study would be necessary, but because of the code but rather the details of the maths behind.

# 2. Structure of Project Repository

The structure of the codes, including directory structure and coding/software-engineering practices,  were  

Choose one of the following options:

- 0 : Extremely poorly organised and incomprehensible.
- 1 : Reasonably organised and could be improved (please consider making suggestions for improvement in Section 8 below) for easier comprehension.
- 2 : Extremely organised and easily comprehensible and/or replicable.

Feedback: 1.5
The structure was well explained in the README, it wasn't fully self-explanatory.
Detailed instructions on how to set up and execute with a bit of manual work and setup.
Remains of commented blocks, most likely as example code for inference and the like. It would,
have been less confusing to keep it as a function and/or option and making it clear what it is for.

# 3. Comments and explanations in code:

Choose one of the following options:

- 0 : There were no comments at all in the code and not enough markdown/documentation explaining what the code was supposed to be doing. 
- 1 : There were minimal comments in the code but the markdown/documentation explained briefly what the code was supposed to be doing at least at a high level.
- 2 : The code was well-commented so I could easily follow what was happening in most code blocks.

Feedback: 2
Very well detailed comments, explains thoroughly what everything does.
Does not follow standard python practises of here/docstrings for functions (nitpicking).
Can't really complain.

# 4. Originality or Difficulty of the Project

Choose one of the following options:

- 0 : The project is just a minor adaptation (less than 10%) of an existing openly available project that is very close to this, including data analysed, tools used, identical versions of software, and conclusions reached.
- 1 : The project is similar to an existing openly available project but significant contributions seem to be made in software versions, data used, explanations given and conclusions reached.
- 2 : The project seems to have tackled a very challenging problem and has made significant contributions to aid other researchers or practitioners in a particular domain of expertise.

Feedback: 1
From what I understand most of the fundamental parts of the project have been done before, maths and data.
(If the background of the project is novel, please correct me and I will upgrade the scoring).
The prominent part of the project is the federated learning from which interesting conclusions were made.

# 5. Scalability of the Project

Choose one of the following options:

- 0 : The project is not at all scalable. There is no need to do this project in a distributed computing environment (explain in Section 8 below why you think so).  
- 1 : The project has some scalable aspects or ideas on how scalability can be achieved. 
- 2 : The project is truly implementing a *scalable data science process* and the same code can work with arbitrarily large input data if enough computing resources are available.

Feedback: 2
The project is truly scalable. The federated learning can be scaled, including the data without larger issues.
The project did prove this in practise across Sweden and with the most machines I saw from the presentations,
they also provided more than adequate explanation as to why theoretically.

# 6. Total Grade

Add up all the scores from the above 5 Categories and report it below.

The Total Grade is: 8.5

# 7. Completing Peer-review Process

- Add this **Total Grade** as an integer to the LMS/Studium to complete your peer-review by the deadline.
- Attach you completed version of this file `PEER_REVIEW.md` to the LMS to complete the peer-review by the deadline.

# 8. Detailed Constructive Comments

Please feel free to make detailed comments here in a constructive manner to help the group you are reviewing, so that they can make their presentation much better for the ScaDaMaLe-WASP Peer-reviewed Group Projects.

Feedback:
Truly a very fun project to follow, the idea and actual demonstration with a larger number of machines was wonderful.
If I remember correctly, noone else really showed the scale you did, neither across distance, making the project even better.
Overall, the code is well structured and instructions very clear.
I would have liked to see a bit fewer setup steps. I believe they could be automated with a makefile,
or well constructed shell script. To nitpick very much, I would like to see docstrings for functions,
but in reality the comments you have now are infinitely better for adoption/development than if the reverse was true.
And please, either be a bit more clear on why the blocks of code are commented, that is what they are for, what the intent is,
or just remove it.
Disregarding the code snippets in the presentation (which this template seem to encourage), it would have been
more engaging with a bit less text, or at least some animation/steps/focus to make it easier to follow.
Very well executed project, most things is nitpicking (in my opinion).

# 2. Repeated feedback as per guidelines of Template.
Feedback:
The structure was well explained in the README, it wasn't fully self-explanatory.
Detailed instructions on how to set up and execute with a bit of manual work and setup.
Remains of commented blocks, most likely as example code for inference and the like. It would,
have been less confusing to keep it as a function and/or option and making it clear what it is for.
