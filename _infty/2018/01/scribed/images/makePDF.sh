#!/bin/bash
# from https://askubuntu.com/questions/493584/convert-images-to-pdf
rm -r new.pdf
ls *.jpg | xargs -I% convert % -quality 70 %.pdf
pdftk *.pdf cat output new.pdf && rm *.jpg.pdf
#pdftk ../arch/soFar.pdf new.pdf cat output soFar.pdf
