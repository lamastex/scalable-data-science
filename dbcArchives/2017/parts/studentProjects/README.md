# Student Projects for sds-2.2, Scalable Data Science form Atlantis 
## Uppsala University Fall 2017

This is a directory for students to publicly make pull reqiests of their projects as they mature. You may just keep your `.dbc` archive of your notebook or directory of notebooks here for the course coordinator to give feedback before new year a tthe lates.

# Instructions for making Pull requests

First *fork* the course github repository:
* [https://github.com/lamastex/scalable-data-science](https://github.com/lamastex/scalable-data-science)
  * Steps for this (forllow instructions for each of these steps in the browser):
    * get a github account with your email address
    * go to the above link and hit the `fork` button

Second, we will get a local sparse copy of the forked git repository on your local machine.

The explanations of each line of shell command is here:
* this is just to make a direcoty to save your .dbc file from databricks community edition

```
$ mkdir sds-project  
$ ls
$ cd sds-project
$ git init
$ git remote add -f origin https://github.com/YourGitUserName/scalable-data-science.git
$ ls
$ git config core.sparseCheckout true
$ echo "/dbcArchives/2017/parts/studentProjects" >> .git/info/sparse-checkout
$ ls
$ git pull origin master
$ ls
$ cd dbcArchives/2017/parts/studentProjects/
$ ls
```

Now go to your browser, open the databricks community edition and download the `.dbc` archive of your project into this directory: `.../dbcArchives/2017/parts/studentProjects/'.

```
git status
git add projectfileName.dbc
ls
git push
git push --set-upstream origin master
git status  # make sure all is ok for what will be added!!!
git commit -a -m "adding my project into my fork"
git push
``

Finally, go to your fork and make a pull request in the browser so your course coordinator can accept it.



