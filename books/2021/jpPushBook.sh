#!/bin/bash
## pushing to book repository
pushd ~/all/git/lamastex/ScaDaMaLe &&
git checkout gh-pages &&
## git checkout main and setup CI... TW
## suggesated use of fork's to make new books and versions... TW
## git push --set-upstream origin gh-pages
##cp -r ~/all/git/lamastex/scalable-data-science/books/2021/ScaDaMaLe/_build/html/* ~/all/git/lamastex/ScaDaMaLe/
cp -r ~/all/git/lamastex/scalable-data-science/books/2021/ScaDaMaLe/_build/html/* . &&
git commit -a -m "book draft 4.9 time-stamped versions in progress" &&
git push origin gh-pages &&
popd

