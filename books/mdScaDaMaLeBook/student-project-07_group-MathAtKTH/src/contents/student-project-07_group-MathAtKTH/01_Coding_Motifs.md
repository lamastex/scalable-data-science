<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Motif Finding
=============

Finding motifs in graphs is no just fun, it also has applications! Here we study the possibility to use GraphFrames as a tool to be used in practice.

Math tells us that motifs has important implications on the general structure of the graphs. For example, two DAG models are the same if they have the same of two motifs, v-structures and skeletons.

Motif finding in graphframes uses a domain specific language (DSL). Here we mention the restrictions of that language and why another might be desirable. One problem is that more complicated queries are (seemingly) not supported. Our man probem however will be the way we count the motifs. GraphFrames uses motif finding algorithms and returns a list of all found subgraphs. Thus we will quickly run out of memory. More specialized software can preform this counting, but uses highly specialized tools not suitable for general motif finding.

Link to intro video: https://www.youtube.com/watch?v=GFG5MGKxJTs

</div>

<div class="cell markdown">

First we load the packages we need.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.sql._
import org.apache.spark.sql.functions._

import org.graphframes._
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.sql._
    import org.apache.spark.sql.functions._
    import org.graphframes._

</div>

</div>

<div class="cell markdown">

Special Graphs
--------------

Here we code some usual motifs that are common within graph theory.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
/* 
** A function counting the number of multiple edges.
*/
def count_mult_edges(input_graph: GraphFrame) = {
  input_graph.find("(a)-[e1]->(b); (a)-[e2]->(b)").filter("e1 != e2").count
}
```

<div class="output execute_result plain_result" execution_count="1">

    count_mult_edges: (input_graph: org.graphframes.GraphFrame)Long

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
/* 
** A function counting the number of 3-cycles.
*/
def count_3_cycle(input_graph: GraphFrame): Long = {
  input_graph.find("(a)-[]->(b); (b)-[]->(c); (c)-[]->(a)").count
}
```

<div class="output execute_result plain_result" execution_count="1">

    count_3_cycle: (input_graph: org.graphframes.GraphFrame)Long

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
/* 
** A function counting the number of loop edges.
*/
def count_loop(input_graph: GraphFrame) = {
  input_graph.find("(a)-[]->(a)").count
}
```

<div class="output execute_result plain_result" execution_count="1">

    count_loop: (input_graph: org.graphframes.GraphFrame)Long

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
/* 
** A function counting the number of bidirected edges.
*/
def count_bidir_edges(input_graph: GraphFrame) = {
  input_graph.find("(a)-[]->(b); (b)-[]->(a)").filter("a.id != b.id").count
}
```

<div class="output execute_result plain_result" execution_count="1">

    count_bidir_edges: (input_graph: org.graphframes.GraphFrame)Long

</div>

</div>

<div class="cell markdown">

Complete graphs
---------------

We code the motifs of the simplest complete graphs, we include an edge i-\>j if i comes before j alphabetically. That is, we code for the edges a-\>b and a-\>c but not for c-\>b. As we will see later the graphs of interest does not contain any loops, thus we do not have to filter the result ensuring that all nodes are distinct.

For simplicial abstract complexes these graphs correspond to the faces. Thus counting them corresponds to finding entries in the f-vector.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
/* 
** Two functions counting the number of 2- and 4-dimensional faces of a 
** simplicial graphical simplex. It is assumed the the input_graph does
** not contain any loops.
*/

def count_K2(input_graph: GraphFrame): Long = { // Should be the same as the number of edges.
  input_graph.find("(a)-[]->(b)").count
}

def count_K3(input_graph: GraphFrame): Long = {
  input_graph.find("(a)-[]->(b); (a)-[]->(c); (b)-[]->(c)").count
}

def count_K4(input_graph: GraphFrame): Long = {
  input_graph.find("(a)-[]->(b); (a)-[]->(c); (a)-[]->(d); (b)-[]->(c); (b)-[]->(d); (c)-[]->(d)").count
}
```

<div class="output execute_result plain_result" execution_count="1">

    count_K2: (input_graph: org.graphframes.GraphFrame)Long
    count_K3: (input_graph: org.graphframes.GraphFrame)Long
    count_K4: (input_graph: org.graphframes.GraphFrame)Long

</div>

</div>

<div class="cell markdown">

Application
===========

Here we will see an example where we have done motif fining in a rats brain-network. Here we will also see the restrictions of what we can do without more specialized code.

</div>

<div class="cell markdown">

Read the edges.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
/* 
** The file edges.csv contains lines on the form "1,5,edge" indicating
** we have an edge from vertex 1 to vertex 5. As we do not care about the 
** third entry (i just says "edge") we choose just the two first entries.
*/

val edges = spark.read.format("csv").option("sep",",").load("/FileStore/shared_uploads/petterre@kth.se/edges.csv").withColumnRenamed("_c0", "src").withColumnRenamed("_c1", "dst").select("src", "dst")

edges.count
```

<div class="output execute_result plain_result" execution_count="1">

    edges: org.apache.spark.sql.DataFrame = [src: string, dst: string]
    res0: Long = 7822274

</div>

</div>

<div class="cell markdown">

Get the vertices from the edges.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
/* 
** For simplicity we read all the nodes from the column "scr". Notice 
** that we use the fact that every node is the source of at least one 
** edge. Thus we can get the vertices like this.
*/

val vertices = edges.select("src").groupBy("src").count().select("src").sort(desc("src")).withColumnRenamed("src", "id")

vertices.count
```

<div class="output execute_result plain_result" execution_count="1">

    vertices: org.apache.spark.sql.DataFrame = [id: string]
    res2: Long = 31346

</div>

</div>

<div class="cell markdown">

Since every node is the source of another, we are good to go.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
/* 
** Now we can create out graph of interest.
*/

val rat_brain_graph = GraphFrame(vertices, edges)
```

<div class="output execute_result plain_result" execution_count="1">

    rat_brain_graph: org.graphframes.GraphFrame = GraphFrame(v:[id: string], e:[src: string, dst: string])

</div>

</div>

<div class="cell markdown">

Graph specifics
---------------

As we can see above we have 31,346 vertices and 7,822,274 edges. The adjacancy matrix would be very sparse with only 0.7% of it's entries being 1. Thus it is generally better to store this as a list of edges (as graphframes does) as opposed to an adjacancy matrix. Despite the matrix being sparse, this is a rather dense graph with the average degree of each node being just above 499.

Here we will look closer at some specifics of this graph. Some of these are very important for designing better algorithms.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
/* 
** As mentioned before, we do not want loops in this graph. Thus we
** call our above function and check whether we have any.
*/
count_loop(rat_brain_graph) // 0
```

<div class="output execute_result plain_result" execution_count="1">

    res4: Long = 0

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
/* 
** A very relevant question is if this graph is connected. If it is not
** it would be more efficient to just look at the induvidual components.
** That is not the case.
*/

spark.sparkContext.setCheckpointDir("/FileStore/shared_uploads/petterre@kth.se/")

val rat_brain_graph_connected_components = rat_brain_graph.connectedComponents.run()
rat_brain_graph_connected_components.select("component").describe().show()
```

<div class="output execute_result plain_result" execution_count="1">

    +-------+---------+
    |summary|component|
    +-------+---------+
    |  count|    31346|
    |   mean|      0.0|
    | stddev|      0.0|
    |    min|        0|
    |    max|        0|
    +-------+---------+

    rat_brain_graph_connected_components: org.apache.spark.sql.DataFrame = [id: string, component: bigint]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
/* 
** Let us continue to look at some stastistics for this graph. We can 
** count the number of 3-cycles.
*/

count_3_cycle(rat_brain_graph) //25 630 728
```

<div class="output execute_result plain_result" execution_count="1">

    res7: Long = 25630728

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
/* 
** Let us continue to look at some stastistics for this graph. We can 
** count the number of multiple edges.
*/

count_mult_edges(rat_brain_graph) // 0
```

<div class="output execute_result plain_result" execution_count="1">

    res8: Long = 0

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
/* 
** Let us continue to look at some stastistics for this graph. We can 
** count the number of bidirected edges.
*/

count_bidir_edges(rat_brain_graph) //165 220
```

<div class="output execute_result plain_result" execution_count="1">

    res9: Long = 165220

</div>

</div>

<div class="cell markdown">

Count complete graphs
---------------------

Now we will (try to) run the motif finding algorithms and see how they preform.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
/* 
** First we look after K2. As that is the graph a->b, we expect this to 
** be equal to the number of edges, otherwise something is wrong.
*/
count_K2(rat_brain_graph) //7 822 274
```

<div class="output execute_result plain_result" execution_count="1">

    res11: Long = 7822274

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
/* 
** Now we find K3. As we will see, this takes a lot of time, longer than
** we want it to. This is because 'find' does a general search algorithm.
** See below for a discussion.
*/

count_K3(rat_brain_graph) //35 976 731
```

<div class="output execute_result plain_result" execution_count="1">

    res12: Long = 35976731

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
//count_K4(rat_brain_graph)
```

</div>

<div class="cell markdown">

Troubles and fixes
------------------

As we saw above finding K3 takes a lot of time, and when running "count*K4(rat*brain*graph)" we run out of memory. This is because we do not use any of the structure of the graphs. Finding graphical simplicies can be made a lot quicker since we can use the structure of the graph \\(K_n\\) and the structure of the "rat*brain\_graph".

How this can be done quicker can be read in "Computing persistent homology of directed flag complexes" by Daniel Luetgehetmann, Dejan Govc, Jason Smith, and Ran Levi (https://arxiv.org/abs/1906.10458).

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
This is a direct implementation. 
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
/*
** A implementation of counting cells. It is not parallelized but could be
** if we start collecting the results in a better way.
*/

def count_cells(old_child_set: DataFrame, f_vector: List[Int], edges: DataFrame, cut_of: Int, dim: Int): List[Int] = {
  // Make a new f-vector that is mutable
  var f_vector_new = f_vector;
  // For each new node
  for (vert <- old_child_set.collect()){
    // Update the f-vector 
    f_vector_new = f_vector_new.updated(dim, f_vector_new(dim) +1);
    // If we have not reached our cut off limit
    if (cut_of > dim){
      // Find the children of "vert" and find the intersection.
      // val child_set = old_child_set.intersect(edges.filter(edges("src") === vert(0)).select("dst"));
      // Call this function recursively
      f_vector_new = count_cells(old_child_set.intersect(edges.filter(edges("src") === vert(0)).select("dst")), f_vector_new, edges, cut_of, dim+1);
    }
  }
  // Return the f-vector
  f_vector_new
}
```

<div class="output execute_result plain_result" execution_count="1">

    count_cells: (old_child_set: org.apache.spark.sql.DataFrame, f_vector: List[Int], edges: org.apache.spark.sql.DataFrame, cut_of: Int, dim: Int)List[Int]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
This is a parallelized version. Notice that the parallelization is very naive and it starts to many threads.
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
/*
** Helper function to count_cells_par. Does a component-wise addition. 
** Badly written.
*/

def component_addition(a: List[Int], b:List[Int]): List[Int] = {
  // Do it the dumb way
  List(a(0)+b(0),a(1)+b(1),a(2)+b(2),a(3)+b(3),a(4)+b(4),a(5)+b(5),a(6)+b(6),a(7)+b(7),a(8)+b(8),a(9)+b(9))
}
```

<div class="output execute_result plain_result" execution_count="1">

    component_addition: (a: List[Int], b: List[Int])List[Int]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
/*
** A parallel (?) implementation of counting cells. 
*/

def count_cells_par(old_child_set: DataFrame, edges: DataFrame, cut_of: Int, dim: Int): List[Int] = {
  if ((cut_of > dim) && (old_child_set.count > 0)){
    return old_child_set.collect().par.map(vert => count_cells_par(old_child_set.intersect(edges.filter(edges("src") === vert(0)).select("dst")), edges, cut_of, dim+1)/* vert_to_f-vector*/).reduce(component_addition(_,_)).updated(dim, 1)
  }
  else{
    return List(0,0,0,0,0,0,0,0,0,0).updated(dim, 1);
  }
}
```

<div class="output execute_result plain_result" execution_count="1">

    count_cells_par: (old_child_set: org.apache.spark.sql.DataFrame, edges: org.apache.spark.sql.DataFrame, cut_of: Int, dim: Int)List[Int]

</div>

</div>

<div class="cell markdown">

Here we implement a version with hopefully better parallelization. It does just one step of parallelization, as oppose to starting to many threads.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
/*
** A parallel (?) implementation of counting cells. 
*/

def count_cells_opt(old_child_set: DataFrame, edges: DataFrame, cut_of: Int, dim: Int): List[Int] = {
  if ((cut_of > dim) && (old_child_set.count > 0)){
    return old_child_set.collect().par.map(vert => count_cells_opt_helper(old_child_set.intersect(edges.filter(edges("src") === vert(0)).select("dst")), edges, cut_of, dim+1)/* vert_to_f-vector*/).reduce(component_addition(_,_)).updated(dim, 1)
  }
  else{
    return List(0,0,0,0,0,0,0,0,0,0).updated(dim, 1);
  }
}

def count_cells_opt_helper(old_child_set: DataFrame, edges: DataFrame, cut_of: Int, dim: Int): List[Int] = {
  if ((cut_of > dim) && (old_child_set.count > 0)){
    // The next line should not parallelize the process.
    return old_child_set.collect().map(vert => count_cells_par(old_child_set.intersect(edges.filter(edges("src") === vert(0)).select("dst")), edges, cut_of, dim+1)/* vert_to_f-vector*/).reduce(component_addition(_,_)).updated(dim, 1)
  }
  else{
    return List(0,0,0,0,0,0,0,0,0,0).updated(dim, 1);
  }
}
```

<div class="output execute_result plain_result" execution_count="1">

    count_cells_opt: (old_child_set: org.apache.spark.sql.DataFrame, edges: org.apache.spark.sql.DataFrame, cut_of: Int, dim: Int)List[Int]
    count_cells_opt_helper: (old_child_set: org.apache.spark.sql.DataFrame, edges: org.apache.spark.sql.DataFrame, cut_of: Int, dim: Int)List[Int]

</div>

</div>

<div class="cell markdown">

### GraphFrame calling

Here we have funcions so that we can call the count\_cellst directly on a GraphFrame.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
/*
** Makes it easier to call "count_cells" on a GraphFrame object. Notice
** that we can get wrong results if the input graph has loops. We recommend
** running "count_loops" to see if that is the case.
*/

def f_vector_of_graphframe(graph: GraphFrame, cut_of: Int): List[Int] = {
  var f_vector = List(0,0,0,0,0,0,0,0,0,0);
  if (cut_of > 9){
    print("Too big cut_of")
    f_vector
  }
  count_cells(graph.vertices.select("id"), f_vector, graph.edges.select("src", "dst"), cut_of, 0);
}
```

<div class="output execute_result plain_result" execution_count="1">

    f_vector_of_graphframe: (graph: org.graphframes.GraphFrame, cut_of: Int)List[Int]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
/*
** Makes it easier to call "count_cells_par" on a GraphFrame object. Notice
** that we can get wrong results if the input graph has loops. We recommend
** running "count_loops" to see if that is the case.
*/

def f_vector_of_graphframe_par(graph: GraphFrame, cut_of: Int): List[Int] = {
  var temp_int = 0;
  if (cut_of > 9){
    print("Too big cut_of")
    temp_int = 9;
  }
  else{
    temp_int = cut_of;
  }
  count_cells_par(graph.vertices.select("id"), graph.edges.select("src", "dst"), temp_int, 0);
}
```

<div class="output execute_result plain_result" execution_count="1">

    f_vector_of_graphframe_par: (graph: org.graphframes.GraphFrame, cut_of: Int)List[Int]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
/*
** Makes it easier to call "count_cells_opt" on a GraphFrame object. Notice
** that we can get wrong results if the input graph has loops. We recommend
** running "count_loops" to see if that is the case.
*/

def f_vector_of_graphframe_opt(graph: GraphFrame, cut_of: Int): List[Int] = {
  var f_vector = List(0,0,0,0,0,0,0,0,0,0);
  if (cut_of > 9){
    print("Too big cut_of")
    f_vector
  }
  count_cells_opt(graph.vertices.select("id"), graph.edges.select("src", "dst"), cut_of, 0);
}
```

<div class="output execute_result plain_result" execution_count="1">

    f_vector_of_graphframe_opt: (graph: org.graphframes.GraphFrame, cut_of: Int)List[Int]

</div>

</div>

<div class="cell markdown">

Example
-------

Let us take a small example that we can run in a fair time and show.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
/*
** First we will define a small graph that is manageable by hand. This 
** graph encodes for a simplicial complex with f-vector (6, 11, 7, 1).
*/
// Vertex DataFrame
val v = sqlContext.createDataFrame(List(
  ("a", 1),
  ("b", 2),
  ("c", 3),
  ("d", 4),
  ("e", 5),
  ("f", 6)
)).toDF("id", "no")

// Edge DataFrame
val e = sqlContext.createDataFrame(List(
  ("a", "b"),
  ("a", "c"),
  ("b", "c"),
  ("b", "d"),
  ("d", "c"),
  ("b", "e"),
  ("c", "e"),
  ("d", "e"), 
  ("a", "f"),
  ("c", "a"),
  ("c", "f")
)).toDF("src", "dst")

val g = GraphFrame(v, e);
```

<div class="output execute_result plain_result" execution_count="1">

    v: org.apache.spark.sql.DataFrame = [id: string, no: int]
    e: org.apache.spark.sql.DataFrame = [src: string, dst: string]
    g: org.graphframes.GraphFrame = GraphFrame(v:[id: string, no: int], e:[src: string, dst: string])

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
f_vector_of_graphframe(g, 5)
```

<div class="output execute_result plain_result" execution_count="1">

    res18: List[Int] = List(6, 11, 7, 1, 0, 0, 0, 0, 0, 0)

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
f_vector_of_graphframe_par(g, 5)
```

<div class="output execute_result plain_result" execution_count="1">

    res19: List[Int] = List(1, 6, 11, 7, 1, 0, 0, 0, 0, 0)

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
f_vector_of_graphframe_opt(g, 5)
```

<div class="output execute_result plain_result" execution_count="1">

    res20: List[Int] = List(1, 6, 11, 7, 1, 0, 0, 0, 0, 0)

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
println(g.vertices.count)
println(count_K2(g))
println(count_K3(g))
println(count_K4(g))
```

<div class="output execute_result plain_result" execution_count="1">

    6
    11
    7
    1

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
//f_vector_of_graphframe_opt(rat_brain_graph, cut_of = 2)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>
