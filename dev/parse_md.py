#!/usr/bin/env python

import sys, os

class Cell(object):
    def __init__(self, content=[], is_markdown=False):
        self.is_markdown = is_markdown
        self.content = content
    def add(self, line):
        self.content.append(line)

def show_help():
    print ""
    print "*** Parsing exported Scala notebook into .md document. ***"
    print "Usage: python parse_md.py [input] [output]"
    print "where "
    print "- 'input' is .scala file exported from Databricks cloud"
    print "- 'output' is .md file to save content. You can also provide target directory"
    print "In this case file name will be based on input file"
    print ""

# Strictly verify if provided path is a file
def verify_file(f):
    path = os.path.realpath(f)
    if not os.path.isfile(path):
        print "[ERROR] Not a file: %s" % f
        sys.exit(1)
    return path

# Fix file if provided path is directory by using suffix specified
def fix_file(f, suffixpath):
    path = os.path.realpath(f)
    if os.path.isdir(f):
        head, tail = os.path.split(suffixpath)
        suffix = ".".join(tail.split(".")[:-1])
        return os.path.join(path, (suffix if suffix else "part-00000") + ".md")
    # Otherwise return unmodified path
    return path

# Read provided file as scala notebook exported from Databricks
def read_scala_notebook(scala_notebook):
    # total cells in notebook
    cells = []
    # we read file using while loop to preserve new line characters and structure of exported
    # notebook. `for` loop over lines ignores new lines
    with open(scala_notebook, "r") as f:
        cell = Cell([], False)
        while True:
            line = f.readline()
            if not line:
                break
            elif line.startswith("// MAGIC"):
                # we remove "// MAGIC" prefix and add to the content, note that markdown can also
                # be one line string
                if line.startswith("// MAGIC %md"):
                    cell.is_markdown = True
                    cell.add(line[12:].strip())
                else:
                    cell.add(line[8:].strip())
            elif line.startswith("// COMMAND ----------"):
                cells.append(cell)
                cell = None
                cell = Cell([], False)
            else:
                cell.add(line)
        if cell and cell.content:
            cells.append(cell)
    return cells

# Helper function to write content of the cell into stream
def write_cell_content(f, content):
    for line in content:
        f.write(line)
        if not line.endswith("\n"):
            f.write("\n")

# Helper function to write markdown cell
def write_markdown_cell(f, cell):
    write_cell_content(f, cell.content)

# Helper function to write scala cell
def write_scala_cell(f, cell):
    f.write("```scala")
    write_cell_content(f, cell.content)
    f.write("```\n")

# Write content into provided file as markdown document
def write_markdown(md_document, cells):
    with open(md_document, "w") as f:
        for cell in cells:
            if cell.is_markdown:
                write_markdown_cell(f, cell)
            else:
                write_scala_cell(f, cell)

def main(args):
    # We run only on OS X and Linux
    if not (sys.platform.startswith("darwin") or sys.platform.startswith("linux")):
        print "[ERROR] Only OS X and Linux are supported"
        sys.exit(1)
    # If list of arguments is empty, fail and display help message
    if len(args) < 2:
        print "[ERROR] Short argument list"
        show_help()
        sys.exit(1)

    input_file = verify_file(args[0])
    output_file = fix_file(args[1], input_file)

    print "[INFO] Input: %s" % input_file
    print "[INFO] Output: %s" % output_file

    cells = read_scala_notebook(input_file)
    print "[INFO] Read %s cells (markdown: %s, scala: %s)" % (len(cells),
        len([x for x in cells if x.is_markdown]), len([x for x in cells if not x.is_markdown]))
    write_markdown(output_file, cells)
    print "[INFO] Notebook converted into .md document %s" % output_file

if __name__ == '__main__':
    args = sys.argv[1:]
    main(args)
