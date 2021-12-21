#!/bin/bash
## pushing to book repository
pushd ~/all/git/lamastex/ScaDaMaLe &&
git checkout gh-pages &&

## git checkout main and setup CI... TW
## suggesated use of fork's to make new books and versions... TW
## git push --set-upstream origin gh-pages
git pull &&
#git rm -r * &&
#git commit -a -m "clean wipe" &&
#git push &&
## cp ~/all/git/lamastex/scalable-data-science/books/mdScaDaMaLeBook/main_index.html ./index.html &&
mkdir -p 000_9-sds-3-x-trends && cp -rp ~/all/git/lamastex/scalable-data-science/books/mdScaDaMaLeBook/000_9-sds-3-x-trends/book/* ./000_9-sds-3-x-trends &&
: '
mkdir -p 000_1-sds-3-x && cp -rp ~/all/git/lamastex/scalable-data-science/books/mdScaDaMaLeBook/000_1-sds-3-x/book/* ./000_1-sds-3-x &&
mkdir -p 000_2-sds-3-x-ml && cp -rp ~/all/git/lamastex/scalable-data-science/books/mdScaDaMaLeBook/000_2-sds-3-x-ml/book/* ./000_2-sds-3-x-ml &&
mkdir -p 000_3-sds-3-x-st && cp -rp ~/all/git/lamastex/scalable-data-science/books/mdScaDaMaLeBook/000_3-sds-3-x-st/book/* ./000_3-sds-3-x-st &&
mkdir -p 000_4-sds-3-x-ss && cp -rp ~/all/git/lamastex/scalable-data-science/books/mdScaDaMaLeBook/000_4-sds-3-x-ss/book/* ./000_4-sds-3-x-ss &&
# 000_5-sds-2-x-geo seems to be missing now
mkdir -p 000_5-sds-2-x-geo && cp -rp ~/all/git/lamastex/scalable-data-science/books/mdScaDaMaLeBook/000_5-sds-2-x-geo/book/* ./000_5-sds-2-x-geo &&
mkdir -p 000_6-sds-3-x-dl && cp -rp ~/all/git/lamastex/scalable-data-science/books/mdScaDaMaLeBook/000_6-sds-3-x-dl/book/* ./000_6-sds-3-x-dl &&
mkdir -p 000_7-sds-3-x-ddl && cp -rp ~/all/git/lamastex/scalable-data-science/books/mdScaDaMaLeBook/000_7-sds-3-x-ddl/book/* ./000_7-sds-3-x-ddl &&
mkdir -p 000_8-sds-3-x-pri && cp -rp ~/all/git/lamastex/scalable-data-science/books/mdScaDaMaLeBook/000_8-sds-3-x-pri/book/* ./000_8-sds-3-x-pri &&
# fix mdbook error with the POI notebook in 000_9-sds-3-x-trends - currently 000_9-sds-3-x-trends/src/SUMMARY.md does not have this notebook
mkdir -p xtraResources && cp -rp ~/all/git/lamastex/scalable-data-science/books/mdScaDaMaLeBook/xtraResources/book/* ./xtraResources &&
'
git add -A &&
git status &&
git commit -a -m "book draft 8 version -- tis 21 dec 2021 21:48:07 CET, Uppsala, Sweden" &&
git push origin gh-pages &&
popd

