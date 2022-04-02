## Step for generating Spark scala notebook (spark_notebook.scala)

1. Prepare *spark_notebook.json* by putting image numbers in the value of the key *slideshow*.

For example, see the dict of one cell below.

```
{
  "desc": "map",
  "slideshow": [
    "12-17",
    "18"
  ]
}
```

*"12-17"* means making gif out of images 12-17 and insert it to the cell.<br>
Then, *"18"* means just appending image 18 to the same cell. <br>
We will get one cell with one gif (12-17) and one image (18).

You may add description in *desc* for the description of this dict, or leave it blank.

2. Run the following command.<br>

`./makeSparkNotebook.sh`

Then, you will get the Spark notebook with a name *spark_notebook.scala* and a *gif* directory storing successful creation of *gifs* images.<br>
It may take some time to complete.

3. You will see in the notebook that the first cell is intentionally left blank. This cell is for you to add a title and description of the notebook.
4. You need to push *gif* directory to GitHub to this dir manually. 

`https://github.com/lamastex/scalable-data-science/blob/master/db/visualapi/med`

You may push gifs after the notebook is generated.
