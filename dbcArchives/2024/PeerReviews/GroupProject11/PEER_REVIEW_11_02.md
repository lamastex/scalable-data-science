# Peer-review of ScaDaMaLe Student Group Project Newton Mwai

# Reviewer

- Sofia Andersson

# 1. Live/Video Presentation

Recall/watch the live/video presentation carefully and decide on a score between 0 and 2.

Choose one of the following options:

- 1 : The presentation was reasonably comprehensible and it helped me follow the codes and explanations relatively easily.

# 2. Structure of Project Repository

The structure of the codes, including directory structure and coding/software-engineering practices,  were  

Choose one of the following options:

- 1 : Reasonably organised and could be improved (please consider making suggestions for improvement in Section 8 below) for easier comprehension.

# 3. Comments and explanations in code:

Choose one of the following options:

- 2 : The code was well-commented so I could easily follow what was happening in most code blocks.

# 4. Originality or Difficulty of the Project

Choose one of the following options:

- 1 : The project is similar to an existing openly available project but significant contributions seem to be made in software versions, data used, explanations given and conclusions reached.

# 5. Scalability of the Project

Choose one of the following options:

- 2 : The project is truly implementing a *scalable data science process* and the same code can work with arbitrarily large input data if enough computing resources are available.

# 6. Total Grade

Add up all the scores from the above 5 Categories and report it below.

The Total Grade is: 7/10

# 7. Completing Peer-review Process

- Add this **Total Grade** as an integer to the LMS/Studium to complete your peer-review by the deadline.
- Attach you completed version of this file `PEER_REVIEW.md` to the LMS to complete the peer-review by the deadline.

# 8. Detailed Constructive Comments

You have the repo layout in your presentation, but not in the repo itself. I would add it to the repo as well to make it easier for others to understand the structure of the project, as it is quite complex. The code is well commented, but some parts are a bit confusing. In `src/utils.py`, the majority of the code is commented out with only one function present. I would have refactored this to be a part of `src/main.py` as it feels overkill to have an additional file for one function, and you already define functions within `src/main.py`.

Speaking of code, it is very well commented. I am unfamiliar with Spark and Scala, as well as MA, but I could follow along with the code and understand what was happening. I would perhaps have appreciated some explicit definitions of the function variables at times, e.g. what alpha is in the `init` function of `PySparkMARFClassifier`, but as this could simply be something I am unfamiliar with, it is not a big issue. Also, `matrees/tree.py` is entirely uncommented, so bringing that up to the standard of the rest of the repo would be great. I also think your `main.py` could do with a bit of extra commenting love, particularly as it is the send file in your repo called `main.py`, so emphasising how it is different from the other file would be good.

I think a bit more documentation of the purpose of your project and its structure would be good in the README. While your step-by-step instructions are great, it would be beneficial to have a bit more of an overview of what the project is about and what it aims to achieve. I would also add a section on the structure of the repo, as mentioned above. There are also some typos in the README. Overall, a very interesting and well-executed project. Best of luck with writing your paper!
