#!/bin/bash
## pushing to book repository main coursepaths index
pushd ~/all/git/lamastex/ScaDaMaLe &&
git checkout gh-pages &&
git pull &&
git rm -r index.html asset-manifest.json favicon.ico logo192.png logo512.png manifest.json robots.txt static &&
git commit -a -m "clean wipe" &&
git push origin gh-pages &&
#./mdMakeBookIndex.sh # make main index coursepaths first, if not done manually
cp -r ~/all/git/lamastex/coursepaths/build/* . &&
git add index.html asset-manifest.json favicon.ico logo192.png logo512.png manifest.json robots.txt static/ &&
git commit -a -m "new book course paths index" &&
git push origin gh-pages &&
popd

