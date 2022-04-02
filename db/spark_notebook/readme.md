## Step for making Spark scala notebook

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

2. Run the following command. Then, you will get the notebook with a name *spark_notebook.scala* and a *gif* directory storing successful creation of *gifs* images. It may take some time to complete.

`./makeSparkNotebook.sh`

4. You will see in the notebook that the first cell is intentionally left blank. This cell is for you to add a title and description of the notebook.
