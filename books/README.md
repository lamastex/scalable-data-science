From:

- https://gitlab.com/tilowiklund/pinot/-/issues/1#note_478647799

# Short Jupyter book tutorial 

## Install jupyter-book, e.g. in a virtual environment

```
mkdir book-project # mkdir ~/all/git/lamastex/scalable-data-science/books
cd book-project
virtualenv .
source bin/activate # or source bin/activate.fish ...
pip install -U jupyter-book
```

See `2021/makeBook.sh` to make ScaDaMaLe Book.

Here are generic instructions

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

