# Generating markdown output from Databricks archive

1. Install `stack` from repositories or from
   [https://docs.haskellstack.org/en/stable/README/](https://docs.haskellstack.org/en/stable/README/)

2. Run `git clone https://github.com/TiloWiklund/pinot.git` in a directory of
   your choosing.

3. Run `cd pinot`

4. Run `stack setup`

5. Run `stack build` (if this fails, run the command with the `-fast` flag)

6. Run `stack exec dbcflatten <infile> <outfile>` where `<infile>` is the
   Databricks archive from which you want to remove all the `iframes` and
   `<outfile>` is the name of the file you want the new Databricks archive to be
   saved to.

7. Run `stack exec -- pinot -f databricks -t markdown <infile> -o <outpath>`
   where `<infile>` is the Databricks archive you want to transform to markdown
   and `<outpath>` is the folder you wish to put the outputted markdown in.
