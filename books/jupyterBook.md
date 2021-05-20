# Short Jupyter book tutorial 

Unfortunately, creating a html book from the jupyter notebooks via jupyter book is highly limiting.

The main issues we could not resolve easily include:

- Being unable to have cell-specific progrramming language sytax-highlighting
  -this is due to hard assumption of **the** `code` cell in jupyter notebooks

*Solution:* We will be using mdbook instead and bypass via `md` extracts of the original noebooks before making the html book.

## Install jupyter-book, e.g. in a virtual environment

On Ubuntu 18.04 do:

```
lsb_release -a
python3 -V
sudo apt-get upgrade python3
sudo apt-get install build-essential libssl-dev libffi-dev python-dev
pip3 -V
sudo apt-get update
sudo apt install python3-pip
sudo apt install -y python3-venv
python3 -m venv ScaDaMaLeBook
source ScaDaMaLeBook/bin/activate
pip3 install -U jupyter-book
```

From: https://vitux.com/install-python3-on-ubuntu-and-set-up-a-virtual-programming-environment/

This also works
```
mkdir ~/all/git/lamastex/scalable-data-science/books
sudo apt install python3-virtualenv
virtualenv .
source bin/activate
pip install -U jupyter-book
# jupyter-book create ScaDaMaLe
mkdir 2021
cd 2021/
# jupyter-book create ScaDaMaLe
./makeBook.sh
```


See `2021/makeBook.sh` to make ScaDaMaLe Book.

# Here are generic instructions

From:

- https://gitlab.com/tilowiklund/pinot/-/issues/1#note_478647799

## Install jupyter-book, e.g. in a virtual environment

```
mkdir book-project # 
cd book-project
virtualenv .
source bin/activate # or source bin/activate.fish ...
pip install -U jupyter-book
```

## Start the book project and set it to not automatically run cells (we will rely on pinot's cell results)

```
jupyter-book create mybook
sed -ie "s|execute_notebooks: force|execute_notebooks: 'off'|" mybook/_config.yml
```

## Put a bunch of Jupyter notebooks into the book folder, e.g. with pinot (see README.md)

```
rm mybook/notebooks.ipynb
cd path/to/pinot
stack exec pinot -- --from databricks --to jupyter databricks_notebook.dbc -o path/to/book-project/mybook
```

## Add them to the TOC 

(see template _toc.yml for example for how to organise into sections!)

```
cd path/to/book-project
cp mybook/_toc.yml mybook/_toc.yml.backup
find mybook -iname '*.ipynb' | sort -h | cut -d'/' -f 2- | xargs printf "- file: %s\n" > mybook/_toc.yml
```

## Build the book

```
jupyter-book build mybook
```

Results are now in `mybook/_build/html`

