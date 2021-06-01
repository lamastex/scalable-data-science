#!/bin/bash
## pushing to book repository
pushd ~/all/git/lamastex/ScaDaMaLe &&
git checkout gh-pages &&

## git checkout main and setup CI... TW
## suggesated use of fork's to make new books and versions... TW
## git push --set-upstream origin gh-pages
git pull &&
git rm -r * &&
git commit -a -m "clean wipe" &&
git push &&
cp -rp ~/all/git/lamastex/scalable-data-science/books/mdScaDaMaLeBook/book/* . &&
git add -A &&
git status &&
git commit -a -m "book draft 5.1 version via mdbook in progress - md table broken" &&
git push origin gh-pages &&
popd

