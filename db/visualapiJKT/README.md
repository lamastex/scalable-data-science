We start from the MIT-licensed images contributed by [Jeff Thompson](https://www.linkedin.com/profile/view?id=128303555) from:

* [http://nbviewer.ipython.org/github/jkthompson/pyspark-pictures/blob/master/pyspark-pictures.ipynb](http://data-frack.blogspot.com/2015/01/visual-mnemonics-for-pyspark-api.html)

as blogged here:

* [http://data-frack.blogspot.com/2015/01/visual-mnemonics-for-pyspark-api.html](http://data-frack.blogspot.com/2015/01/visual-mnemonics-for-pyspark-api.html)

We split each page of the PDF: 

* [https://github.com/jkthompson/pyspark-pictures/blob/master/images/pyspark-pictures.pdf](https://github.com/jkthompson/pyspark-pictures/blob/master/images/pyspark-pictures.pdf)

as follows:
```
$ convert -density 300 visualapiJTK.pdf -quality 100 visualapiJTK.png
```

```
$ for file in *.png; do convert $file -resize 1000 med/$file; done
```

