
## Build
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
All the action called by the spark-shell is in `parseMD.scala`.

There are issues with rendering luaLatex code that renders well in databricks notebook but not always in both githud and in gitbook.
Gitbook is the main target for markdown rendering. The hope is to allow mathematical expressions in latex in GitBook and databricks notebooks.

It would be nice to allow the `displayHTML(frameIt("http:someUrlYouWantToShowInPlace"))` function in the databricks notebook to also appear in gitbook in time.

Raazesh Sainudiin
Sun Jun 19 21:51:39 NZST 2016
