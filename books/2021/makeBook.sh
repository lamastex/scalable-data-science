#!/bin/bash
# make ScaDaMaLe Book
rm -r ScaDaMaLe/ &&
jupyter-book create ScaDaMaLe &&
cp _config.yml ScaDaMaLe/_config.yml &&
cp logo.png ScaDaMaLe/logo.png &&
pushd ~/all/git/tilowiklund/pinot &&
# to ignore temporarily the .dbc files as they change do
# git update-index --assume-unchanged ../../dbcArchives/2021/000_1-sds-3-x.dbc
# to undo the above do:
# git update-index --no-assume-unchanged ../../dbcArchives/2021/000_1-sds-3-x.dbc
stack exec pinot -- --from databricks --to jupyter ~/all/git/lamastex/scalable-data-science/dbcArchives/2021/000_1-sds-3-x.dbc -o ~/all/git/lamastex/scalable-data-science/books/2021/ScaDaMaLe/ &&
popd &&
# find ScaDaMaLe -iname '*.ipynb' | sort -h | cut -d'/' -f 2- | xargs printf "- file: %s\n" > ScaDaMaLe/_toc.yml
cp _toc.yml ScaDaMaLe/_toc.yml &&
jupyter-book build ScaDaMaLe &&
pushd ~/all/git/lamastex/ScaDaMaLe &&
# git checkout main and setup CI... TW
# suggesated use of fork's to make new books and versions... TW
git checkout gh-pages
# git push --set-upstream origin gh-pages
#cp -r ~/all/git/lamastex/scalable-data-science/books/2021/ScaDaMaLe/_build/html/* ~/all/git/lamastex/ScaDaMaLe/
cp -r ~/all/git/lamastex/scalable-data-science/books/2021/ScaDaMaLe/_build/html/* . &&
git commit -a -m "book draft in progress" &&
git push origin gh-pages &&
popd
