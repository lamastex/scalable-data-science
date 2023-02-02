# Group Projects: ScaDaMaLe WASP Instance 2022-2023

Edited by Oskar Åsbrink and Raazesh Sainudiin.

Peer-reviewed by project authors according to [these instructions](https://github.com/lamastex/scalable-data-science/blob/master/dbcArchives/2021/PEER_REVIEW_INSTRUCTIONS.md) using [this template](https://github.com/lamastex/scalable-data-science/blob/master/dbcArchives/2021/PEER_REVIEW.md).

# Introduction

A total of 42 PhD students in 13 groups did projects of their choosing in Scalable Data Science and Distributed Machine Learning, a mandatory as well as elective course of The WASP Graduate School in 2022-2023. See [ScaDaMaLe Course Pathways](https://lamastex.github.io/ScaDaMaLe/) to appreciate the pre-requisite modules 000_1 through 000_9 for the union of all 13 projects.


## The Best Student Group Projects on the basis of peer-review and industrial feed-back are:
        
- Group 5 on *Scalable Bayesian optimization with distributed Gaussian processes and deep kernel learning* (academic-track)
  - Carl Hvarfner, Lund University
  - Leonard Papenmeier, Lund University 
  - Manu Upadhyaya, Lund University
- Group 9 on  *Predicting the load in wireless networks* (industry-track)
  - Sofia Ek, Department of Information Technology, Uppsala University
  - Oscar Stenhammar, Network and System Engineering, KTH and Ericsson

# Table of Contents

1. [Graph of Wiki](https://lamastex.github.io/ScaDaMaLe/000_0-sds-3-x-projects-2022/contents/student-project-01_group-GraphOfWiki/student-project-01_group-GraphOfWiki/00_Introduction.html) by Vilhelm Agdur, Henrik Ekström, Simon Johansson and Albin Toft.
2. [Visual Question Answering using Transformers](https://lamastex.github.io/ScaDaMaLe/000_0-sds-3-x-projects-2022/contents/student-project-02_group-DDLOfVision/student-project-02_group-DDLOfVision/00_vqa_introduction.html) by Ehsan Doostmohammadi and Hariprasath Govindarajan.
3. [Scalable Analysis of a Massive Knowledge Graph](https://lamastex.github.io/ScaDaMaLe/000_0-sds-3-x-projects-2022/contents/student-project-03_group-WikiKG2/student-project-03_group-WikiKG2/00_ingest_data.html) by Filip Cornell, Yifei Jin, Joel Oskarsson and Tianyi Zho.
4. [Federated Learning for Brain Tumor Segmentation](https://lamastex.github.io/ScaDaMaLe/000_0-sds-3-x-projects-2022/contents/student-project-04_group-FedMLMedicalApp/student-project-04_group-FedMLMedicalApp/00_Notebook_Presentation.html) by Jingru Fu, Lidia Kidane and Romuald Esdras Wandji.
5. [Scalable Bayesian optimization with distributed Gaussian processes and deep kernel learning](https://lamastex.github.io/ScaDaMaLe/000_0-sds-3-x-projects-2022/contents/student-project-05_group-DistOpt/student-project-05_group-DistOpt/00_introduction.html) by Carl Hvarfner, Leonard Papenmeier and Manu Upadhyaya.
6. [Experiments with ZerO initialisation](https://lamastex.github.io/ScaDaMaLe/000_0-sds-3-x-projects-2022/contents/student-project-07_group-ExpsZerOInit/student-project-07_group-ExpsZerOInit/00_introduction_resnet.html) by Livia Qian and Rajmund Nagy.
7. [Smart Search in Wikipedia](https://lamastex.github.io/ScaDaMaLe/000_0-sds-3-x-projects-2022/contents/student-project-08_group-WikiSearch/student-project-08_group-WikiSearch/00_Introduction.html) by David Mohlin, Erik Englesson and Fereidoon Zangeneh.
8. [Distributed Ensembles for 3D Human Pose Estimation](https://lamastex.github.io/ScaDaMaLe/000_0-sds-3-x-projects-2022/contents/student-project-09_group-DistEnsembles/student-project-09_group-DistEnsembles/00_Introduction.html) by Hampus Gummesson Svensson, Xixi Liu, Yaroslava Lochman and Erik Wallin.
9. [Predicting the load in wireless networks](https://lamastex.github.io/ScaDaMaLe/000_0-sds-3-x-projects-2022/contents/student-project-10_group-RDI/student-project-10_group-RDI/00_introduction.html) by Sofia Ek and Oscar Stenhammar.
10. [Collaborative Filtering in Movie Recommender Systems](https://lamastex.github.io/ScaDaMaLe/000_0-sds-3-x-projects-2022/contents/student-project-11_group-CollaborativeFiltering/student-project-11_group-CollaborativeFiltering/01_Introduction.html) by Jacob Lindbäck, Rebecka Winqvist, Robert Bereza and Damianos Tranos.
11. [Federated Learning Using Horovod](https://lamastex.github.io/ScaDaMaLe/000_0-sds-3-x-projects-2022/contents/student-project-12_group-FedLearnOpt/student-project-12_group-FedLearnOpt/01_Federated_Learning_Introduction.html) by Amandine Caut, Ali Dadras, Hoomaan Maskan and Seyedsaeed Razavikia.
12. [Distributed Reinforcement Learning](https://lamastex.github.io/ScaDaMaLe/000_0-sds-3-x-projects-2022/contents/student-project-13_group-DRL/student-project-13_group-DRL/00_DistributedRL.html) by Johan Edstedt, Arvi Jonnarth and Yushan Zhang.
13. [Earth Observation](https://lamastex.github.io/ScaDaMaLe/000_0-sds-3-x-projects-2022/contents/student-project-14_group-EarthObs/student-project-14_group-EarthObs/00_Introduction.html) by Daniel Brunnsåker, Alexander H. Gower and Filip Kronström.
14. [Conclusion and BrIntSuSb](https://lamastex.github.io/ScaDaMaLe/000_0-sds-3-x-projects-2022/contents/student-projects-BrIntSuSvConclusion/student-projects-BrIntSuSvConclusion/BrIntSuSv.html) by Raazesh Sainudiin.
15. [Editors](https://lamastex.github.io/ScaDaMaLe/000_0-sds-3-x-projects-2022/editors.html)

## Invited Talks from Industry

Thanks to the inspiring talks from the following invited speakers from industry:

- Vivian Ribeiro, Nanxu Su and Tomas Carvalho, [trase](https://www.trase.earth/) (Stockholm, Sweden), *Transparency for sustainable trade*. 
- Reza Zadeh, [Matroid](https://www.matroid.com/) and Stanford University (Palo Alto, California, USA), *Computer Vision from an academic perspective*.
- Andreas Hellander, [Scaleout Systems](https://www.scaleoutsystems.com/) and Uppsala University (Uppsala, Sweden), *Taking Federated Learning to Production - towards privacy-preserving ML at scale*. 
- Ali Sarrafi, Christian Von Koch and William Anzen, [Combient Mix](https://www.themix.ai/) (Stockholm, Sweden), *Slag segmentation with deep neural networks at LKAB*.
- Juozas Vaicenavicius, [SENSmetry](https://sensmetry.com/) (Uppsala, Sweden and Vilnius, Lithuania), *Autonomous systems safety: what is so difficult?*
- Jim Dowling, [Logical Clocks, hopsworks](https://www.hopsworks.ai/) and KTH Royal Institute of Technology (Stockholm, Sweden), *Serverless Machine Learning with Hopsworks*.
