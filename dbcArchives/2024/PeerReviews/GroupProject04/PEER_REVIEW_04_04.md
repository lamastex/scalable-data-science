# Peer-review of ScaDaMaLe Student Group Project Number <XY>

# Reviewer

Anonymous 

# 1. Live/Video Presentation

Recall/watch the live/video presentation carefully and decide on a score between 0 and 2.

2 : The presentation was easily comprehensible and it helped me follow the project easily. I think I can definitely use the codes in the repository and adapt them if I were to encounter similar problems in the future, with possibly more in-depth self-study as needed.

# 2. Structure of Project Repository

The structure of the codes, including directory structure and coding/software-engineering practices,  were  

1 : Reasonably organised and could be improved (please consider making suggestions for improvement in Section 8 below) for easier comprehension.

# 3. Comments and explanations in code:

1 : There were minimal comments in the code but the markdown/documentation explained briefly what the code was supposed to be doing at least at a high level.

# 4. Originality or Difficulty of the Project

2 : The project seems to have tackled a very challenging problem and has made significant contributions to aid other researchers or practitioners in a particular domain of expertise.

# 5. Scalability of the Project

2 : The project is truly implementing a *scalable data science process* and the same code can work with arbitrarily large input data if enough computing resources are available.

# 6. Total Grade

Add up all the scores from the above 5 Categories and report it below.

The Total Grade is: 8

# 7. Completing Peer-review Process

- Add this **Total Grade** as an integer to the LMS/Studium to complete your peer-review by the deadline.
- Attach you completed version of this file `PEER_REVIEW.md` to the LMS to complete the peer-review by the deadline.

# 8. Detailed Constructive Comments

Please feel free to make detailed comments here in a constructive manner to help the group you are reviewing, so that they can make their presentation much better for the ScaDaMaLe-WASP Peer-reviewed Group Projects.

- Live/Video Presentation
    - You did a lot in the project and sometimes I was a bit confused as to what you reported results for. E.g. was it CLIP or ViT, and when did you use the data by Raaz and when not?
- Structure of Project Repository
    - The repository is generally well-structured. However, it would have been nice with a more clear roadmap of how you performed the project. Right now the README for the repo is a collection of topics related to the dataset, federated learning, results etc. and it is sometimes a bit unclear as to how they connect. It would have been nice with e.g. some step-by-step list of how you performed the project with links to parts of your code (something like 1. Collect and annotate the data, 2. Find model hyperparameters, 3. Train the models...). 
    - The `finetune_mae` code seems to be completely uncommented and unexplained - when was it used and what for? 
    - It seems as though you unnecessarily repeat code, e.g. `CLIPClassifier` can be found in both `fedn_supervised/client/model.py` and `pretrain_clip/finetune_model.py`, here you could maybe have made use of imports etc.
- Comments and explanations in code
    - Some parts of the code are more explained than others. E.g. there is no docstring for the `main` function in `fedn_supervised/client/train.py`, while it might have been nice with one. 