# Peer-review of ScaDaMaLe Student Group Project Number 2

# Reviewer

Thibault Marette

# Set-up and Overview

- Go to the public link for the Group Project repository that you have been assigned to for your peer-review.
- You will be doing the peer-review constructively (You may clone the repository and try out codes yourself).
- Recall/watch the live/video presentation and go through the repository according to the below guidelines.
- You will be giving points under various categories and you have to report the final grade in the Course Canvas page.
- Please attach the completed version of this template plain text markdown file `PEER_REVIEW.md` to  Canvas to complete the peer-review by the deadline.

# 1. Live/Video Presentation

- 2 : The presentation was easily comprehensible and it helped me follow the project easily. I think I can definitely use the codes in the repository and adapt them if I were to encounter similar problems in the future, with possibly more in-depth self-study as needed.


# 2. Structure of Project Repository


- 1 : Reasonably organised and could be improved (please consider making suggestions for improvement in Section 8 below) for easier comprehension.


# 3. Comments and explanations in code:


- 2 : The code was well-commented so I could easily follow what was happening in most code blocks.

# 4. Originality or Difficulty of the Project


- 1 : The project is similar to an existing openly available project but significant contributions seem to be made in software versions, data used, explanations given and conclusions reached.

# 5. Scalability of the Project

- 2 : The project is truly implementing a *scalable data science process* and the same code can work with arbitrarily large input data if enough computing resources are available.

# 6. Total Grade

The Total Grade is: 8

# 7. Completing Peer-review Process

- Add this **Total Grade** as an integer to the LMS/Studium to complete your peer-review by the deadline.
- Attach you completed version of this file `PEER_REVIEW.md` to the LMS to complete the peer-review by the deadline.

# 8. Detailed Constructive Comments


It was a great idea to add spotify links in the presentation, to get an idea of what kind of actual data you worked with.

For reproducibility: It would be great to add some file to automatically install missing modules in python, with a config file for instance (you have one but it is missing some modules and not part of the tutorial). Moreover, running step 1 of the data-processing instructions require the file `data/spotify/mpd.slice.0-999.json` which is absent from the repository. You provide an alternative using a google drive with the 99 pre-processed files, but it is tedious to download (when downloading the folder, we get 9 different zip files containing part of the folders, which you then have to merge manually. Uploading a unique zip file would have been nicer (smaller file and easier to manage, for instance). But it is nice that you provided a backup solution in case the pre-processing is faulty.

Federated recommendation system is a promenent research area (as you pointed our in your readme and also in your presentation), but you applied it in a unique context with extensive experiments and very cool (actual) scalability experiments.





