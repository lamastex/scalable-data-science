# Peer-review of ScaDaMaLe Student Group Project Number <XY> 3

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

Choose one of the following options: 1

- 0 : The presentation was completely incomprehensible after accounting for my background knowledge or lack thereof. 
- 1 : The presentation was reasonably comprehensible and it helped me follow the codes and explanations relatively easily.
- 2 : The presentation was easily comprehensible and it helped me follow the project easily. I think I can definitely use the codes in the repository and adapt them if I were to encounter similar problems in the future, with possibly more in-depth self-study as needed.

# 2. Structure of Project Repository

The structure of the codes, including directory structure and coding/software-engineering practices,  were  

Choose one of the following options: 1

- 0 : Extremely poorly organised and incomprehensible.
- 1 : Reasonably organised and could be improved (please consider making suggestions for improvement in Section 8 below) for easier comprehension.
- 2 : Extremely organised and easily comprehensible and/or replicable.

# 3. Comments and explanations in code:

Choose one of the following options: 1

- 0 : There were no comments at all in the code and not enough markdown/documentation explaining what the code was supposed to be doing. 
- 1 : There were minimal comments in the code but the markdown/documentation explained briefly what the code was supposed to be doing at least at a high level.
- 2 : The code was well-commented so I could easily follow what was happening in most code blocks.

# 4. Originality or Difficulty of the Project

Choose one of the following options: 1

- 0 : The project is just a minor adaptation (less than 10%) of an existing openly available project that is very close to this, including data analysed, tools used, identical versions of software, and conclusions reached.
- 1 : The project is similar to an existing openly available project but significant contributions seem to be made in software versions, data used, explanations given and conclusions reached.
- 2 : The project seems to have tackled a very challenging problem and has made significant contributions to aid other researchers or practitioners in a particular domain of expertise.

# 5. Scalability of the Project

Choose one of the following options: 2

- 0 : The project is not at all scalable. There is no need to do this project in a distributed computing environment (explain in Section 8 below why you think so).  
- 1 : The project has some scalable aspects or ideas on how scalability can be achieved. 
- 2 : The project is truly implementing a *scalable data science process* and the same code can work with arbitrarily large input data if enough computing resources are available.

# 6. Total Grade

Add up all the scores from the above 5 Categories and report it below.

The Total Grade is: <TOTAL-Score-between-0-And-10> 6

# 7. Completing Peer-review Process

- Add this **Total Grade** as an integer to the LMS/Studium to complete your peer-review by the deadline.
- Attach you completed version of this file `PEER_REVIEW.md` to the LMS to complete the peer-review by the deadline.

# 8. Detailed Constructive Comments

Please feel free to make detailed comments here in a constructive manner to help the group you are reviewing, so that they can make their presentation much better for the ScaDaMaLe-WASP Peer-reviewed Group Projects.
    
## Comments  

### Presentation  
The slides and presentation were well-prepared, however, the presentation exceeded the 20-30 minute time limit. 

### Repository  

The `README` file is well-structured at first glance, but there are many files and folders that are either unexplained or not referenced. This makes it unclear for me how to replicate your results.  

#### Unexplained and duplicate Files  
- Several files, such as `demo_sam_finetuning_ray.py`, are mentioned in the `README`, but others, like tutorials in the `notebooks` folder, are not. 
- For instance, `sam_finetuning_ray.ipynb` appears to duplicate `demo_sam_finetuning_ray.py`. The latter is mentioned briefly in the `README` while the first is not.  
- It would be helpful to eliminate duplicates, clarify the purpose of each file, and document them comprehensively in the main `README`.  

#### Repository structure and multiple `README` files  
- The abundance of `README` files adds confusion:  
  - A main `README` in the root directory.  
  - A `README\libs` folder with unclear contents.  
  - A `kuberay-cluster\README`, which is referenced in the main `README`.  
  - A `ray-sam/demo-micro-sam/readme.md`, explaining Docker Image usage but not folder contents.  
- It would be clearer to have a single main `README` describing the overall contents and structure of the repository. If additional `README` files are needed, they could start with a brief explanation of the specific folder's contents.
    
### Code comments and explanation  
- The repository’s structure made it challenging to identify the main file and understand the code at a higher level.  
- While `ray-sam/demo-micro-sam/automatic_segmentation.ipynb` is well-commented and easy to follow, it’s unclear if this is the central file for your project.
- I assume that `demo_sam_finetuning_ray.py` might be the central file, as it is mentioned in the `README`, but it is less clear and harder to understand.  
- For example, the `demo_sam_finetuning_ray.py` file includes a `debug_notebook` function, which seems to derive from the original notebook (`sam_finetuning_ray.ipynb`). This is somewhat confusing.  

### Adddional theory or presentation slides  
Including your presentation slides or a document summarizing the theory behind the project could improve clarity and make the repository more self-contained and accessible.