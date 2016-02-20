We split each page of the visualapi: 

* [http://training.databricks.com/visualapi.pdf](http://training.databricks.com/visualapi.pdf)

as follows:
```
$ convert -density 300 visualapi.pdf -quality 100 visualapi.png
```

```
convert visualapi-3.png -resize 484 small/visualapi-3.png

convert visualapi-3.png -resize 1000 med/visualapi-3.png

```

We are not modifying the content in each page and simply using them in databricks context. We believe this respects the CC-BY-NC-ND license of the [visualapi.pdf](http://training.databricks.com/visualapi.pdf).

One can always start from the MIT-licensed images contributed by [Jeff Thompson](https://www.linkedin.com/profile/view?id=128303555) from:

* [http://nbviewer.ipython.org/github/jkthompson/pyspark-pictures/blob/master/pyspark-pictures.ipynb](http://data-frack.blogspot.com/2015/01/visual-mnemonics-for-pyspark-api.html)

as blogged here:

* [http://data-frack.blogspot.com/2015/01/visual-mnemonics-for-pyspark-api.html](http://data-frack.blogspot.com/2015/01/visual-mnemonics-for-pyspark-api.html)

We are grateful to databricks for commissioning [Adam Breindel](https://www.linkedin.com/profile/view?id=8052185) to further evolve Jeff’s work into the diagrams exploded page-by-page here.


