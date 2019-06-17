#!/bin/bash
#################################################
# This will make zeppelin notes as .json files from the .dbc archive 
# via [pinot](https://github.com/TiloWiklund/pinot) 
# - use gitlab version of pinot below as github may be behind
#################################################

#################################################
# 1. ##### prepare pinot ########################
#################################################
# cd to pinot directory
# git clone https://gitlab.com/tilo.wiklund/pinot
# follow instructions for pinot
# install pinot https://docs.haskellstack.org/en/stable/install_and_upgrade/#linux
#################################################

#################################################
# 2. ##### zeppelin conversions #################
#################################################
PINOT_DIR=~/all/git/pinot
sds_DIR=~/all/git/scalable-data-science
cd $PINOT_DIR &&
#################################################
####### for file-by-file conversion #############
#################################################
#stack exec pinot -- --from databricks-json --to zeppelin $sds_DIR/dbcArchives/2017/sds-2-2/003_scalaCrashCourse.scala -o $sds_DIR/dbcArchives/2017/zp-sds-2-2/
#################################################
###### for the whole dbc archive ################ 
#################################################
stack exec pinot -- --from databricks --to zeppelin $sds_DIR/dbcArchives/2017/sds-2-2.dbc -o $sds_DIR/zp/2/2/
#################################################
### temporarily write in /tmp/zp if debugging ###
### /tmp/zp should be replaced by $sds_DIR/zp/2/2/ and zeppelin set to git the notebooks for sds-2-2 collaborators
#stack exec pinot -- --from databricks --to zeppelin $sds_DIR/dbcArchives/2017/sds-2-2.dbc -o /tmp/zp/ # $sds_DIR/zp/2/2/
#################################################

#################################################
# 3. # use pinot's adder to inject code into zp #
#################################################
stack exec -- adder -f zeppelin -c $sds_DIR/babel/zeppelinInjectionCodes.txt  $(ls $sds_DIR/zp/2/2/sds-2-2/*.json) -o $sds_DIR/zp/2/2/sds-2-2/
#################################################

#################################################
# 4. ##### gitbook operations ###################
#################################################
#stack exec -- pinot -f databricks -t markdown $sds_DIR/dbcArchives/2017/sds-2-2.dbc -o $sds_DIR/gb/ &&
#rm -rf $sds_DIR/gb/db
#mv ~/all/git/scalable-data-science/gb/sds-2-2/ ~/all/git/scalable-data-science/gb/db 
#################################################

#################################################
# 5. ssh -i ~/.ssh/id_rsa -N -L 8890:ec2-34-240-7-43.eu-west-1.compute.amazonaws.com:8890 Raaz@ec2-xx-xxx-x-xx.eu-west-1.compute.amazonaws.com
#################################################
