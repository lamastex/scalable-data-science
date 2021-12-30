// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC We did not use databricks for this project
// MAGIC 
// MAGIC     

// COMMAND ----------

// MAGIC %md
// MAGIC ***Please use this [GITHUB LINK](https://github.com/ChrisMats/SWAP_with_DDP) to access our project***

// COMMAND ----------

// MAGIC %md
// MAGIC # SWAP_With_DDP
// MAGIC 
// MAGIC - Christos Matsoukas @ChrisMats
// MAGIC 
// MAGIC - Emir Konuk @emirkonuk
// MAGIC 
// MAGIC - Johan Fredin Haslum @cfredinh
// MAGIC 
// MAGIC - Miquel Marti @miquelmarti
// MAGIC 
// MAGIC [Stochastic Weight Averaging in Parallel (SWAP)](https://openreview.net/pdf?id=rygFWAEFwS) in [PyTorch](https://pytorch.org/)
// MAGIC 
// MAGIC Everything related to the project can be found in [this repository](https://github.com/ChrisMats/SWAP_with_DDP).
// MAGIC 
// MAGIC [Video demonstration](https://youtu.be/O3ZxNvEx48Q)
// MAGIC 
// MAGIC ## Install dependencies etc.
// MAGIC 
// MAGIC - Python 3.8+ 
// MAGIC - Pytorch 1.7+
// MAGIC 
// MAGIC ### Install using conda
// MAGIC - Using comands\
// MAGIC ```conda create -n swap python=3.8 scikit-learn easydict matplotlib wandb tqdm -y```\
// MAGIC ```conda install pytorch torchvision cudatoolkit=10.2 -c pytorch -y```
// MAGIC 
// MAGIC - Using the .yml file\
// MAGIC ```conda env create -f environment.yml```
// MAGIC 
// MAGIC 
// MAGIC ## Docker setup
// MAGIC - Note that the Dockerfile is provided for single machine, multiGPU usage. For multi-machine setups, refer to the SLURM section.
// MAGIC - Dockerfile has its own comments. At the end of the file there are a few lines describing how to build/run the docker image. You can (and should) modify the port numbers depending on your setup. 
// MAGIC - Recommended folder setup is to have _/storage_ in the host machine and _/storage_ in the docker image. Clone [this repository](https://github.com/ChrisMats/SWAP_with_DDP) to the _/storage_ in the host machine, and work from there. You can change the WORKDIR (Line 107) in the Dockerfile if you desire a different folder setup. 
// MAGIC - By default, the image will start with a jupyter notebook running, accessible at port 8855. If you want to login to bash directly, comment/uncomment the respective lines (109 & 111).
// MAGIC - Remember to add your WANDB_API_KEY to the respective line in the Dockerfile.
// MAGIC - You can change your image username (line 31). The default is swapuser.
// MAGIC - If you want to directly clone the repo to the image, you can just add the link and uncomment the respective line (line 103). This is not recommended as you will most likely connect to git from the host for secure access.
// MAGIC - If you need to set up rootless docker with nvidia GPU support, first install [rootless docker](https://docs.docker.com/engine/security/rootless/). Then, install [nvidia-docker](https://github.com/NVIDIA/nvidia-docker). After installation, remember to edit _/etc/nvidia-container-runtime/config.toml_ to have _"no-cgroups = true"_ before restarting the docker daemon.
// MAGIC 
// MAGIC ## Usage
// MAGIC - All input options need to be modified in the _params.json_ file.\
// MAGIC ``` cd your_path/SWAP_with_DDP```\
// MAGIC ```python classification.py --params_path params.json```
// MAGIC - About the params, if you increase the num_workers and notice that it is slow, you should set it back to 0 or 1. This is a problem that occurs occasionally with pytorch DDP.
// MAGIC 
// MAGIC ## Distributed training using SLURM
// MAGIC 
// MAGIC - Before starting training, define necessary resources for each node in the ```cluster_run.sbatch``` file.
// MAGIC - Train on multiple nodes on SLURM cluster using comand \
// MAGIC ``` cd your_path/SWAP_with_DDP```\
// MAGIC ```sbatch cluster_run.sbatch your_conda_env data_location```
// MAGIC - (N-number of nodes)x(P-processes per node) are initiated each running ```main.py```
// MAGIC - All comunications between processes are handled over TCP and a master process adress is set using ```--dist_url```
// MAGIC - The code, conda environment and data location have to be available from all nodes with the same paths
// MAGIC 
// MAGIC ### Results
// MAGIC CIFAR10 - 8 GPUs - 512 per gpu - 150 epochs - Step 2 starts at step 1500 - without SWAP 94.3, with SWAP 95.7
// MAGIC   
// MAGIC Please refer to the [project repository](https://github.com/ChrisMats/SWAP_with_DDP) for the images.

// COMMAND ----------

