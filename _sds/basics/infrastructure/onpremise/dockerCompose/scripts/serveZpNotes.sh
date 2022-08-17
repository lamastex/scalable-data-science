#!/bin/bash

set -x -v

pushd zp/
for module in 000_1-sds-3-x-spark 000_1-sds-3-x-sql 000_2-sds-3-x-ml xtraResources #000_3-sds-3-x-st 000_4-sds-3-x-ss 000_5-sds-2-x-geo 000_6-sds-3-x-dl 000_7-sds-3-x-ddl 000_8-sds-3-x-pri 000_9-sds-3-x-trends
do
python3 ../zimport/zimport.py $module
done
popd
