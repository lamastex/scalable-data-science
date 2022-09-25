#!/bin/bash

cd $GITHUB_TEMPMDBOOKDIR/dbc/scalable-data-science
ls $GITHUB_TEMPMDBOOKDIR/dbc/scalable-data-science
for d in */ ; do
    if [ $d != "src/" ] ; then
        mkdir -p $GITHUB_MDPUSHDIR/$d     #GITHUB_TEMPMDBOOKDIR = /home/runner/work/_temp/_github_home/temp/mdbooks
        cp -r ${d}book/* $GITHUB_MDPUSHDIR/$d
    fi 
done