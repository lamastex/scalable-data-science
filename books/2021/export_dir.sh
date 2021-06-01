#!/bin/bash
# databricks workspace CLI seems to be not allowing -f HTML file-specifically and only allows source exports
# manual download of html then would be nice to showcase the notebooks with the html output cells and intercative d3 plots
# usage `cat dbDirs2Export` and then do one at a time... `./export_dir.sh 000_1-sds-3-x`,`./export_dir.sh 000_2-sds-3-x-ml`, etc.
databricks workspace export_dir -o /scalable-data-science/$1 ~/all/git/lamastex/scalable-data-science/dbcArchives/2021/$1
