#!/bin/bash
# make a zip and tgz ball for the course with first few notebooks, data/ and images/ directoy
pushd 2019/
rm -rf as as.zip as.tgz
mkdir -p as
mkdir -p as/jp
cp -r jp/data as/jp/
cp -r jp/images as/jp/
cp jp/*.ipynb as/jp/
ls -al as/jp/
ls -al as/jp/data/
ls -al as/jp/images/
zip -r as.zip as
tar zcvf as.tgz as
du -sh as.*
rm -rf as 
mv as.zip scribed/arch/
mv as.tgz scribed/arch/
popd

