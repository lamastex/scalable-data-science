#!/bin/bash
#move files into a directory before commiting to another repo
pwd
ls -l $GITHUB_TEMPMDBOOKDIR/dbc
ls -l $GITHUB_TEMPMDBOOKDIR/dbc/scalable-data-science
cd $GITHUB_TEMPMDBOOKDIR/dbc/scalable-data-science/mdbooks
ls $GITHUB_TEMPMDBOOKDIR/dbc/scalable-data-science/mdbooks
#modules='000_5-sds-2-x-geo  000_4-sds-3-x-ss    000_3-sds-3-x-st'

for d in *
do
#echo "${FILENAME%%.*}" >> noext
    if [ $d != "src/" ] ; then
        mkdir -p $GITHUB_MDPUSHDIR/$d     #GITHUB_TEMPMDBOOKDIR = /home/runner/work/_temp/_github_home/temp/mdbooks
        cp -r ${d}/book/* $GITHUB_MDPUSHDIR/$d
    fi 
    
done
ls $GITHUB_MDPUSHDIR/$d