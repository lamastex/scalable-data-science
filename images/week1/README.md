Screenshots were grabbed on the latest databricks cloud instance and converted as follows:
```%sh
$ pushd ~/Pictures/

~/Pictures$ ls
dbLogin_01_sds_2016S1.png  dbLogin_03_sds_2016S1.png  dbLogin_05_sds_2016S1.png  dbLogin_07_sds_2016S1.png
dbLogin_02_sds_2016S1.png  dbLogin_04_sds_2016S1.png  dbLogin_06_sds_2016S1.png

~/Pictures$ for file in *.png; do convert $file -resize 1000 ~/.../git/scalable-data-science/images/week1/$file; done

$ popd

.../git/scalable-data-science$ file images/week1/*.png
images/week1/dbLogin_01_sds_2016S1.png:                       PNG image data, 1000 x 593, 8-bit/color RGB, non-interlaced
images/week1/dbLogin_02_sds_2016S1.png:                       PNG image data, 1000 x 674, 8-bit/color RGB, non-interlaced
images/week1/dbLogin_03_sds_2016S1.png:                       PNG image data, 1000 x 546, 8-bit/color RGBA, non-interlaced
images/week1/dbLogin_04_sds_2016S1.png:                       PNG image data, 1000 x 546, 8-bit/color RGBA, non-interlaced
images/week1/dbLogin_05_sds_2016S1.png:                       PNG image data, 1000 x 546, 8-bit/color RGBA, non-interlaced
images/week1/dbLogin_06_sds_2016S1.png:                       PNG image data, 1000 x 546, 8-bit/color RGBA, non-interlaced
images/week1/dbLogin_07_sds_2016S1.png:                       PNG image data, 1000 x 546, 8-bit/color RGBA, non-interlaced
images/week1/stateofthebdasunionAmpCamp6Stoica-5_YTCover.png: PNG image data, 484 x 374, 16-bit/color RGB, non-interlaced
images/week1/tabCompletionAfterSDot.png:                      PNG image data, 655 x 264, 8-bit/color RGB, non-interlaced
```
