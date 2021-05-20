# ScaDaMaLe/sds-3.x Book

The objective is to build html book from a sequence of executible notebooks in one of the standard formats:

- jupyter
- zeppelin
- databricks
- ...

For now, we are mainly working on converting notebooks in databricks, zeppelin or jupyter formats into html book.

# mdbook

We will use [pinot](https://gitlab.com/tilowiklund/pinot/) and [mdbook](https://rust-lang.github.io/mdBook/index.html) to 
convert a sequence of databricks notebooks in a sequence of .dbc archive files into one indexed and browsable html book by: 

1. converting content of all cells in all the notebooks into markdown (md) format via pinot
- and then use mdbook to convert the extracted md content into html book

Note that the first attempt was via jupyterbook but this method as serious limitations and does not allow for nearly complete notebook format agnosticity while allowing for different programming languages in each cell of each notebook.
See [jupyterBook.md](jupyterBook.md) for this excursion.
 
The details of using pinot and mdbook are as follows:

## Details

See [README of pinot](https://gitlab.com/tilowiklund/pinot/-/blob/master/README.md) to setup pinot and use mdbook.

### Setup pinot

```
:~/all/git$ mkdir -p tilowiklund
:~/all/git$ cd tilowiklund
:~/all/git/tilowiklund$ git clone git@gitlab.com:tilowiklund/pinot.git
:~/all/git/tilowiklund$ cd pinot
:~/all/git/tilowiklund/pinot$ ls
data  LICENSE  old  pinot.cabal  README.md  Setup.hs  shell.nix  src  stack.yaml  stack.yaml.lock
:~/all/git/tilowiklund/pinot$ stack exec
:~/all/git/tilowiklund/pinot$ stack build
```
### Setup mdbook project

The book project is part of [https://github.com/lamastex/scalable-data-science](https://github.com/lamastex/scalable-data-science).
 
```
:~/all/git$ mkdir -p lamastex
:~/all/git$ cd lamastex
:~/all/git/lamastex$ git clone git@github.com:lamastex/scalable-data-science.git
~/all/git/lamastex$ cd scalable-data-science/books
:~/all/git/lamastex/scalable-data-science/books$ 
```

