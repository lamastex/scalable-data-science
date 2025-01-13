# Peer-review of ScaDaMaLe Student Group Project Number 3

# Reviewer

- Anton Matsson

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
- Please attach the completed version of this template plain text markdown file `PEER_REVIEW.md` to Canvas to complete the peer-review by the deadline.

# 1. Live/Video Presentation

Recall/watch the live/video presentation carefully and decide on a score between 0 and 2.

- 1 : The presentation was reasonably comprehensible and it helped me follow the codes and explanations relatively easily.

# 2. Structure of Project Repository

The structure of the codes, including directory structure and coding/software-engineering practices, were  

- 1 : Reasonably organised and could be improved (please consider making suggestions for improvement in Section 8 below) for easier comprehension.

# 3. Comments and explanations in code:

- 2 : The code was well-commented so I could easily follow what was happening in most code blocks.

# 4. Originality or Difficulty of the Project

- 1 : The project is similar to an existing openly available project but significant contributions seem to be made in software versions, data used, explanations given and conclusions reached.

# 5. Scalability of the Project

- 2 : The project is truly implementing a *scalable data science process* and the same code can work with arbitrarily large input data if enough computing resources are available.

# 6. Total Grade

Add up all the scores from the above 5 Categories and report it below.

The Total Grade is: 7

# 7. Completing Peer-review Process

- Add this **Total Grade** as an integer to the LMS/Studium to complete your peer-review by the deadline.
- Attach you completed version of this file `PEER_REVIEW.md` to the LMS to complete the peer-review by the deadline.

# 8. Detailed Constructive Comments

Please feel free to make detailed comments here in a constructive manner to help the group you are reviewing, so that they can make their presentation much better for the ScaDaMaLe-WASP Peer-reviewed Group Projects.

- The overall structure of the code is solid, but the readability could be enhanced by removing unused parts. For instance, the examples in the demo-micro-sam folder don’t appear to be utilized. Additionally, if I understood your presentation correctly, it seems that a Ray cluster on Kubernetes hasn’t been set up yet, so the content in the kuberay-cluster folder might not be necessary at this stage. Finally, since the content of the notebook sam_finetuning_ray.ipynb appears to have been copied to demo_sam_finetuning_ray.py, you could consider removing the notebook to reduce redundancy.
- The core idea of the project is highly scalable, but to further showcase this scalability, providing a working cluster setup would be beneficial.
