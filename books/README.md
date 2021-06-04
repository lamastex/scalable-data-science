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
2. and then use mdbook to convert the extracted md content into html book

Note that the first attempt was via jupyterbook but this method had serious limitations and does not allow for nearly complete notebook format agnosticity while allowing for syntax-highlighting of different programming languages in each cell of each notebook.
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
# to reinstall stack for haskell
:~/all/git/tilowiklund/pinot$ curl -sSL https://get.haskellstack.org/ | sh -s - -f
:~/all/git/tilowiklund/pinot$ stack setup
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

mdbook needs rust. So, install it as instructed here:

- https://rust-lang.github.io/mdBook/cli/index.html#pre-requisite
  - https://www.rust-lang.org/tools/install

```
:~/all/git/lamastex/scalable-data-science/books$ curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh
:~/all/git/lamastex/scalable-data-science/books$ ls ~/.cargo/bin
cargo
cargo-clippy
cargo-fmt
cargo-miri
clippy-driver
rls
rustc
rustdoc
rustfmt
rust-gdb
rust-lldb
rustup
```

Then logout and log back in for PATH to be updated:

```
:~/all/git/lamastex/scalable-data-science/books$ rustc --version
rustc 1.52.1 (9bc8c42bb 2021-05-09)
```

Now, install mdbook:

```
:~/all/git/lamastex/scalable-data-science/books$ cargo install mdbook
~/all/git/lamastex/scalable-data-science/books$ mdbook -h
mdbook v0.4.8
Mathieu David <mathieudavid@mathieudavid.org>
Creates a book from markdown files

USAGE:
    mdbook [SUBCOMMAND]

FLAGS:
    -h, --help       Prints help information
    -V, --version    Prints version information

SUBCOMMANDS:
    build    Builds a book from its markdown files
    clean    Deletes a built book
    help     Prints this message or the help of the given subcommand(s)
    init     Creates the boilerplate structure and files for a new book
    serve    Serves a book at http://localhost:3000, and rebuilds it on changes
    test     Tests that a book's Rust code samples compile
    watch    Watches a book's files and rebuilds it on changes

For more information about a specific command, try `mdbook <command> --help`
The source code for mdBook is available at: https://github.com/rust-lang/mdBook
```

Now you can start building the book:

- [https://rust-lang.github.io/mdBook/cli/build.html](https://rust-lang.github.io/mdBook/cli/build.html)

```
:~/all/git/lamastex/scalable-data-science/books$ mkdir -p mdScaDaMaLeBook/
:~/all/git/lamastex/scalable-data-science/books$ cd mdScaDaMaLeBook/
:~/all/git/lamastex/scalable-data-science/books/mdScaDaMaLeBook$ mdbook init .

Do you want a .gitignore to be created? (y/n)
y
What title would you like to give the book? 
ScaDaMaLe/sds-3.x        # -><- sds-3.x/ScaDaMaLe <deWASPableResourceCircumnavigability>    
2021-05-20 16:00:08 [INFO] (mdbook::book::init): Creating a new book with stub content

All done, no errors...
```


