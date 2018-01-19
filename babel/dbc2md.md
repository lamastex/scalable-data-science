# Generating markdown output from Databricks archive

1. Install `stack` from repositories or from
   [https://docs.haskellstack.org/en/stable/README/](https://docs.haskellstack.org/en/stable/README/)

2. Run `git clone https://github.com/TiloWiklund/pinot.git` in a directory of
   your choosing.

3. Run `cd pinot`

4. Run `stack setup`

5. Run `stack build`

6. Run `stack exec dbcflatten <infile> <outfile>` where `<infile>` is the
   Databricks archive from which you want to remove all the `iframes` and
   `<outfile>` is the name of the file you want the new Databricks archive to be
   saved to.

7. Run `stack exec -- pinot -f databricks -t markdown <infile> -o <outpath>`
   where `<infile>` is the Databricks archive you want to transform to markdown
   and `<outpath>` is the folder you wish to put the outputted markdown in.


## To make gitbook

After the 7 Steps above while you are in the pinot directory do:

```%sh
~/all/git/pinot$ stack exec dbcflatten ~/all/git/scalable-data-science/dbcArchives/2017/sds-2-2.dbc ~/all/git/scalable-data-science/gitbook/sds-2-2-flat.dbc 

~/all/git/pinot$ stack exec -- pinot -f databricks -t markdown ~/all/git/scalable-data-science/gitbook/sds-2-2-flat.dbc -o ~/all/git/scalable-data-science/gitbook/

~/all/git/pinot$ rm ~/all/git/scalable-data-science/gitbook/sds-2-2-flat.dbc
```
