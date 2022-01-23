<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Intro
=====

In this notebook we create a code-writing program. Coding a specific motif generally makes for very long strings, the length grows as \\(n^2\\). Thus coding them directly is very inefficient. We solved this problem via a simple program. Here we can simply feed the adjacancy matrix of the motif we want to find, and it gives us the scala code.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
import numpy as np
```

</div>

<div class="cell markdown">

In this funtion we input the adjacancy matrix of the motif we want to find. Then it produces the text of a scala command. Than command can then be copy-pasted into another notebook.

Here we simply go through the adjacancy matrix and fix the string thereafter. In this function the motif we want to find is induced by the graph. Thus each edge is either wanted or forbidden.

loops - Do we care about loops or not.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
def matrix_to_string(input_matrix, input_function_name, loops = True):
  ret_string = "def " + input_function_name + "(input_graph: GraphFrame) = {\n\tval string_" +input_function_name+ " = \""
  filter_string = ""
  pos_edges = ""
  neg_edges = ""
  alphabet = ['a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z']
  
  for i in range(len(input_matrix)):
    for j in range(len(input_matrix[i])):
      if i == j:
        if loops and input_matrix[i][j] != 0:
          pos_edges += "(" + alphabet[i] + ")-[]->(" + alphabet[j] + "); "
        elif loops:
          neg_edges += "!(" + alphabet[i] + ")-[]->(" + alphabet[j] + "); "
      elif input_matrix[i][j] != 0:
        pos_edges += "(" + alphabet[i] + ")-[]->(" + alphabet[j] + "); "
      else:
        neg_edges += "!(" + alphabet[i] + ")-[]->(" + alphabet[j] + "); "
      if i > j:
        filter_string += ".filter(\"" + alphabet[j] + ".id != " + alphabet[i] + ".id\")"
  ret_string += pos_edges + neg_edges
  ret_string = ret_string[0:-2]
  ret_string += "\"\n\tinput_graph.find(g_1)"
  ret_string += filter_string
  ret_string += ".count\n}\n"
  return ret_string
  
  
  
```

</div>

<div class="cell markdown">

In this funtion we input the signed adjacancy matrix of the motif we want to find. Then it produces the text of a scala command. Than command can then be copy-pasted into another notebook.

Here we simply go through the adjacancy matrix and fix the string thereafter. In this function each edge can either be demanded, forbidden, or allowed. The three states are represented by 1, -1, and all other values.

loops - Do we care about loops or not.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
def matrix_to_string_signed(input_matrix, input_function_name):
  ret_string = "def " + input_function_name + "(input_graph: GraphFrame) = {\n\tval string_" +input_function_name+ " = \""
  filter_string = ""
  pos_edges = ""
  neg_edges = ""
  alphabet = ['a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z']
  
  for i in range(len(input_matrix)):
    for j in range(len(input_matrix[i])):
      if input_matrix[i][j] == 1:
        pos_edges += "(" + alphabet[i] + ")-[]->(" + alphabet[j] + "); "
      elif input_matrix[i][j] == -1:
        neg_edges += "!(" + alphabet[i] + ")-[]->(" + alphabet[j] + "); "
      if i > j:
        filter_string += ".filter(\"" + alphabet[j] + ".id != " + alphabet[i] + ".id\")"
  ret_string += pos_edges + neg_edges
  ret_string = ret_string[0:-2]
  ret_string += "\"\n\tinput_graph.find(g_1)"
  ret_string += filter_string
  ret_string += ".count\n}\n"
  return ret_string
```

</div>

<div class="cell markdown">

Examples
========

Here is a quick example where we have coded for a "claw". Another name for the claw is a star of size 3, meaning it has 4 vertices.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
print(matrix_to_string([[0,0,0,1],[0,0,0,1],[0,0,0,1],[0,0,0,0]], "count_claw", loops = False))
```

<div class="output execute_result plain_result" execution_count="1">

    def count_claw(input_graph: GraphFrame) = {
    	val g_1 = "(a)-[]->(d); (b)-[]->(d); (c)-[]->(d); !(a)-[]->(b); !(a)-[]->(c); !(b)-[]->(a); !(b)-[]->(c); !(c)-[]->(a); !(c)-[]->(b); !(d)-[]->(a); !(d)-[]->(b); !(d)-[]->(c)"
    	input_graph.find(g_1).filter("a.id != b.id").filter("a.id != c.id").filter("b.id != c.id").filter("a.id != d.id").filter("b.id != d.id").filter("c.id != d.id").count
    }

</div>

</div>

<div class="cell markdown">

Here we cave coded for a v-structure.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
print(matrix_to_string([[0,0,1],[0,0,1],[0,0,0]], "count_v_struc", loops = False))
```

<div class="output execute_result plain_result" execution_count="1">

    def count_v_struc(input_graph: GraphFrame) = {
    	val g_1 = "(a)-[]->(c); (b)-[]->(c); !(a)-[]->(b); !(b)-[]->(a); !(c)-[]->(a); !(c)-[]->(b)"
    	input_graph.find(g_1).filter("a.id != b.id").filter("a.id != c.id").filter("b.id != c.id").count
    }

</div>

</div>

<div class="cell markdown">

Counting how many loops

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
print(matrix_to_string([[1]], "count_loop"))
```

<div class="output execute_result plain_result" execution_count="1">

    def count_loop(input_graph: GraphFrame) = {
    	val g_1 = "(a)-[]->(a)"
    	input_graph.find(g_1).count
    }

</div>

</div>

<div class="cell markdown">

A very important thing we want to look at is complete graphs.

As these can grow big, we write a function to generate them.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
def adj_matrix_complete_graph(size):
  adj_matrix = np.zeros((size,size))
  for i in range(len(adj_matrix)):
    for j in range(i+1, len(adj_matrix[i])):
      adj_matrix[i][j] = 1
  return adj_matrix
  
```

</div>

<div class="cell markdown">

Now we can generate the code of interest.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
for i in range(3,7+1):
  print(matrix_to_string_signed(adj_matrix_complete_graph(i), "count_K"+str(i)))
```

<div class="output execute_result plain_result" execution_count="1">

    def count_K3(input_graph: GraphFrame) = {
    	val g_1 = "(a)-[]->(b); (a)-[]->(c); (b)-[]->(c)"
    	input_graph.find(g_1).filter("a.id != b.id").filter("a.id != c.id").filter("b.id != c.id").count
    }

    def count_K4(input_graph: GraphFrame) = {
    	val g_1 = "(a)-[]->(b); (a)-[]->(c); (a)-[]->(d); (b)-[]->(c); (b)-[]->(d); (c)-[]->(d)"
    	input_graph.find(g_1).filter("a.id != b.id").filter("a.id != c.id").filter("b.id != c.id").filter("a.id != d.id").filter("b.id != d.id").filter("c.id != d.id").count
    }

    def count_K5(input_graph: GraphFrame) = {
    	val g_1 = "(a)-[]->(b); (a)-[]->(c); (a)-[]->(d); (a)-[]->(e); (b)-[]->(c); (b)-[]->(d); (b)-[]->(e); (c)-[]->(d); (c)-[]->(e); (d)-[]->(e)"
    	input_graph.find(g_1).filter("a.id != b.id").filter("a.id != c.id").filter("b.id != c.id").filter("a.id != d.id").filter("b.id != d.id").filter("c.id != d.id").filter("a.id != e.id").filter("b.id != e.id").filter("c.id != e.id").filter("d.id != e.id").count
    }

    def count_K6(input_graph: GraphFrame) = {
    	val g_1 = "(a)-[]->(b); (a)-[]->(c); (a)-[]->(d); (a)-[]->(e); (a)-[]->(f); (b)-[]->(c); (b)-[]->(d); (b)-[]->(e); (b)-[]->(f); (c)-[]->(d); (c)-[]->(e); (c)-[]->(f); (d)-[]->(e); (d)-[]->(f); (e)-[]->(f)"
    	input_graph.find(g_1).filter("a.id != b.id").filter("a.id != c.id").filter("b.id != c.id").filter("a.id != d.id").filter("b.id != d.id").filter("c.id != d.id").filter("a.id != e.id").filter("b.id != e.id").filter("c.id != e.id").filter("d.id != e.id").filter("a.id != f.id").filter("b.id != f.id").filter("c.id != f.id").filter("d.id != f.id").filter("e.id != f.id").count
    }

    def count_K7(input_graph: GraphFrame) = {
    	val g_1 = "(a)-[]->(b); (a)-[]->(c); (a)-[]->(d); (a)-[]->(e); (a)-[]->(f); (a)-[]->(g); (b)-[]->(c); (b)-[]->(d); (b)-[]->(e); (b)-[]->(f); (b)-[]->(g); (c)-[]->(d); (c)-[]->(e); (c)-[]->(f); (c)-[]->(g); (d)-[]->(e); (d)-[]->(f); (d)-[]->(g); (e)-[]->(f); (e)-[]->(g); (f)-[]->(g)"
    	input_graph.find(g_1).filter("a.id != b.id").filter("a.id != c.id").filter("b.id != c.id").filter("a.id != d.id").filter("b.id != d.id").filter("c.id != d.id").filter("a.id != e.id").filter("b.id != e.id").filter("c.id != e.id").filter("d.id != e.id").filter("a.id != f.id").filter("b.id != f.id").filter("c.id != f.id").filter("d.id != f.id").filter("e.id != f.id").filter("a.id != g.id").filter("b.id != g.id").filter("c.id != g.id").filter("d.id != g.id").filter("e.id != g.id").filter("f.id != g.id").count
    }

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>
