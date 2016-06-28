1. Gitbook has katex plugin in book.json but databricks uses latex parsing within markdown, so it escapes '\', '(', ')' and '_'. 
Also, katex plugin in Gitbook needs '$$' for start and end of inline math equations like `$$ a_{1,2} $$` and it also needs '$$\n' for block math:
```
$$
f_1 = \sum
$$
```

2. Use the following to grep for the math patterns we need to change in `.md` files:
```
$ grep -r --include "*.md" '\\\\(' .
$ grep -r --include "*.md" '\\\\)' .
$ grep -r --include "*.md" '\\\\\[' .
$ grep -r --include "*.md" '\\\\\]' .
$ grep -r --include "*.md" '\\\\\\\\' .
$ grep -r --include "*.md" '\\\\\\' .
$ grep -r --include "*.md" '\\_' .
```

3. databricks uses `\\(` some math equations `\\)`. These need replacing as follows:
```
vim fileWithMathEqns.md
:%s/\\\\(/$$/g
:%s/\\\\)/$$/g
:%s/\\\\\[/$$/g
:%s/\\\\\]/$$/g
:%s/\\_/_/g
:%s/\\\\\\\\/\\\\/g
```

