#!/bin/bash
# make a zip and tgz ball for the course with notebooks, data/ and images/ directoy
pushd 2019/
rm -rf in in.zip in.tgz
mkdir -p in
mkdir -p in/jp
cp -r jp/data in/jp/
cp -r jp/images in/jp/
cp jp/*.ipynb in/jp/
ls -al in/jp/
ls -al in/jp/data/
ls -al in/jp/images/
zip -r in.zip in
tar zcvf in.tgz in
du -sh in.*
rm -rf in 
mv in.zip scribed/arch/
mv in.tgz scribed/arch/
popd

