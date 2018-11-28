#!/bin/bash
# from https://askubuntu.com/questions/493584/convert-images-to-pdf
rm -r new.pdf
ls *.jpeg | xargs -I% convert % -quality 60 %.pdf
pdftk *.pdf cat output new.pdf && rm *.jpeg.pdf
#pdftk ../arch/soFar.pdf new.pdf cat output soFar.pdf
