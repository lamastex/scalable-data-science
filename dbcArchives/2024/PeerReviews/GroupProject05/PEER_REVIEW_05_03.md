# Peer-review of ScaDaMaLe Student Group Project Number <N/A> Lukas Borggren (No group with Lukas)

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
The presentation was orally and visually easy to follow, explained the goal, the tools used and a high level the method used.
With respect to the code, the Spark parts were easy to follow given the education in the course. However, perhaps not the 
authors fault, the other tools used were themselves very high level and would require deeper reading to understand.
Unfortunately, it is hard to evaluate the code by running, as the many tools need several accounts, a lot of manual setup/registration, and money to actually run on cloud premise.

# 2. Structure of Project Repository

The structure of the codes, including directory structure and coding/software-engineering practices,  were  

Choose one of the following options:

- 0 : Extremely poorly organised and incomprehensible.
- 1 : Reasonably organised and could be improved (please consider making suggestions for improvement in Section 8 below) for easier comprehension.
- 2 : Extremely organised and easily comprehensible and/or replicable.

Feedback: 2
The code was reasonably organized, mayhaps, because of the high-level nature of the tools used, much "automagic"
was done where assumed unspecified rules of the tools are used, decreasing readability and predictability to
find files such as configs.

# 3. Comments and explanations in code:

Choose one of the following options:

- 0 : There were no comments at all in the code and not enough markdown/documentation explaining what the code was supposed to be doing. 
- 1 : There were minimal comments in the code but the markdown/documentation explained briefly what the code was supposed to be doing at least at a high level.
- 2 : The code was well-commented so I could easily follow what was happening in most code blocks.

Feedback: 1.5
The code employed the standard practises of Python function commenting, describing each function on a high-level.
However, rarely were detailed comments on the code flow of what and why things were done.

# 4. Originality or Difficulty of the Project

Choose one of the following options:

- 0 : The project is just a minor adaptation (less than 10%) of an existing openly available project that is very close to this, including data analysed, tools used, identical versions of software, and conclusions reached.
- 1 : The project is similar to an existing openly available project but significant contributions seem to be made in software versions, data used, explanations given and conclusions reached.
- 2 : The project seems to have tackled a very challenging problem and has made significant contributions to aid other researchers or practitioners in a particular domain of expertise.

Feedback: 1
The project is similar to the project outline by Google on Gemini for fine-tuning.
However, the data used differentiates the project, and the conclusions made were interesting.

# 5. Scalability of the Project

Choose one of the following options:

- 0 : The project is not at all scalable. There is no need to do this project in a distributed computing environment (explain in Section 8 below why you think so).  
- 1 : The project has some scalable aspects or ideas on how scalability can be achieved. 
- 2 : The project is truly implementing a *scalable data science process* and the same code can work with arbitrarily large input data if enough computing resources are available.

Feedback: 2
With the exception of minHashing, filtering the duplicates, everything in the project seems to be truly scalable with
standard tooling that can easily be used to arbitrarily scale. Post-processing, the model
should see benefits as the data scales and only bottlenecked by number of machines (to a reasonable point).

# 6. Total Grade

Add up all the scores from the above 5 Categories and report it below.

The Total Grade is: 8.5

# 7. Completing Peer-review Process

- Add this **Total Grade** as an integer to the LMS/Studium to complete your peer-review by the deadline.
- Attach you completed version of this file `PEER_REVIEW.md` to the LMS to complete the peer-review by the deadline.

# 8. Detailed Constructive Comments

Please feel free to make detailed comments here in a constructive manner to help the group you are reviewing, so that they can make their presentation much better for the ScaDaMaLe-WASP Peer-reviewed Group Projects.

I enjoyed the project overall, interesting and seems useful. Personally I would not change anything in the presentation.
However, maybe with respect to the guidelines outlined here, a slight inclusion/overview of the code could have been beneficial.
The code was unfortunately, and probably because of the tools used a bit too high-level to be comfortably easy to read and understand.
I would have perhaps included a few more detailed comments, I did love that almost all functions were high-level detailed
with respect to function and what parameters did.
Unfortunately, to me, it seems a bit too standard a project for me to give you 2 points. While I deduct half a point here
for the comments I will round up and give 9 on Studium.

I provided slightly more detailed comment on each point my thoughts.

