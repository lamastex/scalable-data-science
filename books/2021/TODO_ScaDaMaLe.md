# List of TODOs to fix in [ScaDaMaLe Book](https://lamastex.github.io/ScaDaMaLe)

## Needs Fixing next

- images not linked by students to their own github repos as strongly recommended
  - this requires manual downloads by Raaz into `extra-resources-student-projects/...`

## Partiall Fixed

- latex inline in progress
- html inline in progress

## Jens' Fix-It List:

- ~~I would also suggest that you temporarily add a page to the book with a timestamp for when the output was generated, and a list of to-dos, so everyone knows the progress.~~

### ScaDaMaLe Student Group Project Number 05

- Databricks: https://dbc-635ca498-e5f1.cloud.databricks.com/?o=445287446643905#notebook/738986526041611
- Book: https://lamastex.github.io/ScaDaMaLe/000_0-sds-3-x-projects/student-project-05_group-LundDirichletAnalysts/01_Wikipedia_LDA_Analysis.html

#### Feedback for html version (possibly better solved in conversion script rather than in notebook)

- % intro is stripped from code cells. They are not irrelevant. See "%sh".
- The "default language" of the workbook should be mentioned somewhere. If it's mentioned elsewehere that everything is Scala, go for that and mark the exceptions, or let each workbook or cell mention language.
- Long output is boxed in Databricks, not in book.
  - https://jupyterbook.org/content/code-outputs.html#scrolling-cell-outputs
- Plain URLs are not links in book. Make that markup instead?
- Plain URLs should not be treated as markdown (Preserve _ * # etc.)
- Code output from "display()" becomes [TABLE] only.
- "Before we begin filtering" md doesn't produce the numbered list correctly. And "Clean xml and markup".
- html image link fails. See "Smoothed LDA". Make that markup instead?
- Should not color code in //-comments.
- The runtime duration is not included in the html book. Since the topic is sometimes efficient pipelines, it would be nice to include it (but not the "by").
- The video link is temporary. I think the workbook stands fine without it.

### ScaDaMaLe Student Group Project Number 07

- Databricks: https://dbc-635ca498-e5f1.cloud.databricks.com/?o=445287446643905#notebook/2313997410018270/command/2313997410018271
- Book: https://lamastex.github.io/ScaDaMaLe/000_0-sds-3-x-projects/student-project-07_group-MathAtKTH/01_Coding_Motifs.html

#### Feedback for html version (possibly better solved in conversion script rather than in notebook)

- ```Latex style K_n shows verbatim in (Chrome) on Databricks and like "graph K∗n and the structure of the “ratbrain*graph”" in html book.```
  -  ```structure of the graph $K_n$ and the structure of the "rat_brain_graph".```
- Should not color code in comments.
- A very relevant question is if this graph is connected. If it is not
- The runtime duration is not included in the html book. Since the topic is sometimes efficient pipelines, it would be nice to include it (but not the "by").

#### Feedback on proof-reading

- "Application" should not be first-level header.
- Use a constant for path (helps other reuse and you can anonymize it for publication if you want)
  - "/FileStore/shared_uploads/petterre@kth.se/"
- Proof-read for spelling.

### ScaDaMaLe Student Group Project Number 11

- Databricks: https://dbc-635ca498-e5f1.cloud.databricks.com/?o=445287446643905#notebook/2030651056538771/command/1767923094595281
- Book: https://lamastex.github.io/ScaDaMaLe/000_0-sds-3-x-projects/student-project-11_group-Sketchings/00_QuantileEstimation.html

#### Feedback for html version (possibly better solved in conversion script rather than in notebook)

- Numbered headers aren't detected as headers.
- Latex style formula shows fine in (Chrome) on Databricks but not in html.
- Interactive graphs show as [TABLE]. Static Graphs does show however.
- Should not color code in comments.
- The runtime duration is not included in the html book. Since the topic is sometimes efficient pipelines, it would be nice to include it (but not the "by").
- The video link is temporary. I think the workbook stands fine without it.
