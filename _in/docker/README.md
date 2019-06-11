# Docker for sagemath for Applied Stats course

Use the Makefile as follows:

Go one level ip.
Terminal 0:
`make build` builds a new docker image from `sagemath/sagemath`.

`make run` runs the image so one can work with jupyter notebook by mounting the current directory.

Terminal 1:
Also you may want to run a plain sageMathbash docker to work in a pure sage bash environment. For this do:

- `make runSageMathBash` to get into another docker container

## For Self/Co-Authorship

The `.py` and `.sh` files are not relesed yet, if interested email me!

Terminal 1:
- python makeAllNotesFromMaster.py  # to generate all the student versions of .ipynb and assignment ? notebooks in 2019/jp/...
- ./get-html.sh to generate html from .ipynb (this needs to go into the .py script)			

Terminal 2 (outside docker but in same dir!):
Finally commit to github public course page _as/2019

- ./update-sds-as.sh


