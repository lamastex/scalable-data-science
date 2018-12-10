
## Build

All of this is superceded by pinot at https://gitlab.com/tilo.wiklund/pinot

Raazesh Sainudiin

Uppsala, Sweden
Mon Dec 10 15:30:44 IST 2018

## Past Build

Steps to add newly created notebook in Databricks cloud (as `.scala`) into gitbook (`.md`):

- Export notebook from Databricks cloud onto your local machine. We work with `.scala` notebooks
most of the time, so currently we only support those.
- Install Apache Spark locally on your machine (See [http://spark.apache.org/](http://spark.apache.org/)).
- change `FNAME` in `scala2md.sh` as needed.
- Run `scala2md.sh`, an executible bash-shell-script (do `$ chmod 755` to make it executible if needed)  to convert `.scala` notebook into `.md` document. 
```shell
$ ./scala2md.sh
```
- Save converted notebook into git repository with hook into gitbook.

You can easily call the spark job on all the files you want to mark down once the testing is complete.
For now it is being done file by file to test output in git's `.md` renderer and gitbook's `.md` renderer.
This is not done "automagically" in order to allow for gitbook-fielding variations in the ascii `//MAGIC` in the databricks .scala notebook.
Thus `./scala2md.sh` is done step by step on purpose to produce output in `./MDparsed/part-00000` from the input in `./nowparse.scala`.
All the transformations and the final action called by the spark-shell is in `parseMD.scala`.

The issues with rendering the tex code in databricks notebook, github and gitbook have been overcome (but manually on grep and vi). 

Gitbook is the main target for markdown rendering. The hope is to allow mathematical expressions in latex in GitBook and databricks notebooks.

It would be nice to allow the `displayHTML(frameIt("http:someUrlYouWantToShowInPlace"))` function in the databricks notebook to also appear in gitbook in time.

Here are some important tips on the databricksification process!

Install `wget` and `pandoc` first.

These are some instructions for going from the programming guide for Apache Spark to .scala notebooks in databricks cloud.

We use wget and pandoc to get the `.md` from `.html` file by file to be careful with the needed manual edits at the `.scala` end on databricks notebooks.

```%sh 
wget -k http://spark.apache.org/docs/latest/mllib-guide.html
pandoc -f html -t markdown mllib-guide.html > mllib-guide.md
```

To get each `.html` as `.md` repeat the process, for example:

```%sh
wget -k http://spark.apache.org/docs/latest/mllib-data-types.html
pandoc -f html -t markdown mllib-data-types.html > mllib-data-types.md
```

Continue doing this for each major `.html` file that is linked from the programming guide.

Then copy paste each of these `.md` file carefully into scala notebooks.


Have fun!

raaz
Raazesh Sainudiin
Sun Jun 19 21:51:39 NZST 2016
