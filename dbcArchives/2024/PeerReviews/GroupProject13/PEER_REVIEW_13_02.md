# Peer-review of ScaDaMaLe Student Group Project Number 13 (scalable timeseries)

Github repository: https://github.com/PengKuang/team_scalable_timeseries

# Reviewer

Choose one of the following option: 

- Anonymous

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

- [ ] 0 : The presentation was completely incomprehensible after accounting for my background knowledge or lack thereof. 
- [x] 1 : The presentation was reasonably comprehensible and it helped me follow the codes and explanations relatively easily.
- [ ] 2 : The presentation was easily comprehensible and it helped me follow the project easily. I think I can definitely use the codes in the repository and adapt them if I were to encounter similar problems in the future, with possibly more in-depth self-study as needed.

# 2. Structure of Project Repository

The structure of the codes, including directory structure and coding/software-engineering practices,  were  

Choose one of the following options:

- [ ] 0 : Extremely poorly organised and incomprehensible.
- [ ] 1 : Reasonably organised and could be improved (please consider making suggestions for improvement in Section 8 below) for easier comprehension.
- [x] 2 : Extremely organised and easily comprehensible and/or replicable.

# 3. Comments and explanations in code:

Choose one of the following options:

- [ ] 0 : There were no comments at all in the code and not enough markdown/documentation explaining what the code was supposed to be doing. 
- [ ] 1 : There were minimal comments in the code but the markdown/documentation explained briefly what the code was supposed to be doing at least at a high level.
- [x] 2 : The code was well-commented so I could easily follow what was happening in most code blocks.

# 4. Originality or Difficulty of the Project

Choose one of the following options:

- [ ] 0 : The project is just a minor adaptation (less than 10%) of an existing openly available project that is very close to this, including data analysed, tools used, identical versions of software, and conclusions reached.
- [x] 1 : The project is similar to an existing openly available project but significant contributions seem to be made in software versions, data used, explanations given and conclusions reached.
- [ ] 2 : The project seems to have tackled a very challenging problem and has made significant contributions to aid other researchers or practitioners in a particular domain of expertise.

# 5. Scalability of the Project

Choose one of the following options:

- [ ] 0 : The project is not at all scalable. There is no need to do this project in a distributed computing environment (explain in Section 8 below why you think so).  
- [x] 1 : The project has some scalable aspects or ideas on how scalability can be achieved. 
- [ ] 2 : The project is truly implementing a *scalable data science process* and the same code can work with arbitrarily large input data if enough computing resources are available.

# 6. Total Grade

Add up all the scores from the above 5 Categories and report it below.

The Total Grade is: 7

# 7. Completing Peer-review Process

- Add this **Total Grade** as an integer to the LMS/Studium to complete your peer-review by the deadline.
- Attach you completed version of this file `PEER_REVIEW.md` to the LMS to complete the peer-review by the deadline.

# 8. Detailed Constructive Comments

Please feel free to make detailed comments here in a constructive manner to help the group you are reviewing, so that they can make their presentation much better for the ScaDaMaLe-WASP Peer-reviewed Group Projects.

---

- Consider deploying your `presentation.html` as github pages so that it's directly accessible from the repository. Now you first need to clone it locally to view the presentation.
- In addition to the explanation provided in `docker.md` on how to create the required environment using docker, a ready to use docker image with pre-installed packages and files would increase user friendliness. This could, for example, directly be provided alongside the repository as [Github Packages](https://docs.github.com/en/packages/learn-github-packages/introduction-to-github-packages) using [Github Workflows](https://docs.github.com/en/actions/writing-workflows).
- The Jupyter notebook was a great way to follow what you did in the project. Despite many good comments, I was sometimes missing some explanation why something was done, not only what was done in the flowing code cell.
- At the same time, a Jupyter notebook is not ideal to scale the project up. A better approach might be to create a python module that can be used independently and easier for scaling up the project, with a demonstration usage in a Jupyter notebook.
