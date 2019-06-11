FROM sagemath/sagemath
MAINTAINER Raazesh Sainudiin <raazesh.sainudiin@gmail.com>
RUN echo "inside sagemath container!"
RUN sudo apt update -y && sudo apt upgrade -y && sudo apt install -y man && sudo apt install -y vim
CMD ["bash"]
CMD ["sage -n jupyter --ip=0.0.0.0"]
