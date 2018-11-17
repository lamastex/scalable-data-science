#!/bin/bash
# from https://askubuntu.com/questions/493584/convert-images-to-pdf
ls *.jpg | xargs -I% convert % -quality 100 %.pdf
pdftk *.pdf cat output new.pdf && rm *.jpg.pdf

