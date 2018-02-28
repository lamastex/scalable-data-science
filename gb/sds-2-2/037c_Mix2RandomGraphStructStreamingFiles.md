[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

Task - do now
=============

Write a mixture of two random graph models for file streaming later
-------------------------------------------------------------------

We will use it as a basic simulator for timeseries of network data. This can be extended for specific domains like network security where extra fields can be added for protocols, ports, etc.

The raw ingredients are here... more or less.

Read the code from github \* https://github.com/apache/spark/blob/master/graphx/src/main/scala/org/apache/spark/graphx/util/GraphGenerators.scala \* Also check out: https://github.com/graphframes/graphframes/blob/master/src/main/scala/org/graphframes/examples/Graphs.scala

Let's focus on the two of the simplest (deterministic) graphs.

``` scala
import scala.util.Random

import org.apache.spark.graphx.{Graph, VertexId}
import org.apache.spark.graphx.util.GraphGenerators
import org.apache.spark.sql.functions.lit // import the lit function in sql
import org.graphframes._

/*
// A graph with edge attributes containing distances
val graph: Graph[Long, Double] = GraphGenerators.logNormalGraph(sc, numVertices = 50, seed=12345L).mapEdges { e => 
  // to make things nicer we assign 0 distance to itself
  if (e.srcId == e.dstId) 0.0 else Random.nextDouble()
}
*/

val graph: Graph[(Int,Int), Double] = GraphGenerators.gridGraph(sc, 5,5)
```

>     import scala.util.Random
>     import org.apache.spark.graphx.{Graph, VertexId}
>     import org.apache.spark.graphx.util.GraphGenerators
>     import org.apache.spark.sql.functions.lit
>     import org.graphframes._
>     graph: org.apache.spark.graphx.Graph[(Int, Int),Double] = org.apache.spark.graphx.impl.GraphImpl@2afd1b5c

``` scala
val g = GraphFrame.fromGraphX(graph)
val gE= g.edges.select($"src", $"dst".as("dest"), lit(1L).as("count")) // for us the column count is just an edge incidence
```

>     g: org.graphframes.GraphFrame = GraphFrame(v:[id: bigint, attr: struct<_1: int, _2: int>], e:[src: bigint, dst: bigint ... 1 more field])
>     gE: org.apache.spark.sql.DataFrame = [src: bigint, dest: bigint ... 1 more field]

>     Warning: classes defined within packages cannot be redefined without a cluster restart.
>     Compilation successful.

``` scala
d3.graphs.force(
  height = 500,
  width = 500,
  clicks = gE.as[d3.Edge])
```

<p class="htmlSandbox">
<style>

.node_circle {
  stroke: #777;
  stroke-width: 1.3px;
}

.node_label {
  pointer-events: none;
}

.link {
  stroke: #777;
  stroke-opacity: .2;
}

.node_count {
  stroke: #777;
  stroke-width: 1.0px;
  fill: #999;
}

text.legend {
  font-family: Verdana;
  font-size: 13px;
  fill: #000;
}

.node text {
  font-family: "Helvetica Neue","Helvetica","Arial",sans-serif;
  font-size: 17px;
  font-weight: 200;
}

</style>

<div id="clicks-graph">
<script src="//d3js.org/d3.v3.min.js"></script>
<script>

var graph = {"nodes":[{"name":"12"},{"name":"8"},{"name":"19"},{"name":"23"},{"name":"4"},{"name":"15"},{"name":"11"},{"name":"9"},{"name":"22"},{"name":"13"},{"name":"24"},{"name":"16"},{"name":"5"},{"name":"10"},{"name":"21"},{"name":"6"},{"name":"1"},{"name":"17"},{"name":"14"},{"name":"0"},{"name":"20"},{"name":"2"},{"name":"18"},{"name":"7"},{"name":"3"}],"links":[{"source":19,"target":16,"value":1},{"source":19,"target":12,"value":1},{"source":16,"target":21,"value":1},{"source":16,"target":15,"value":1},{"source":21,"target":24,"value":1},{"source":21,"target":23,"value":1},{"source":24,"target":4,"value":1},{"source":24,"target":1,"value":1},{"source":4,"target":7,"value":1},{"source":12,"target":15,"value":1},{"source":12,"target":13,"value":1},{"source":15,"target":23,"value":1},{"source":15,"target":6,"value":1},{"source":23,"target":1,"value":1},{"source":23,"target":0,"value":1},{"source":1,"target":7,"value":1},{"source":1,"target":9,"value":1},{"source":7,"target":18,"value":1},{"source":13,"target":6,"value":1},{"source":13,"target":5,"value":1},{"source":6,"target":0,"value":1},{"source":6,"target":11,"value":1},{"source":0,"target":9,"value":1},{"source":0,"target":17,"value":1},{"source":9,"target":18,"value":1},{"source":9,"target":22,"value":1},{"source":18,"target":2,"value":1},{"source":5,"target":11,"value":1},{"source":5,"target":20,"value":1},{"source":11,"target":17,"value":1},{"source":11,"target":14,"value":1},{"source":17,"target":22,"value":1},{"source":17,"target":8,"value":1},{"source":22,"target":2,"value":1},{"source":22,"target":3,"value":1},{"source":2,"target":10,"value":1},{"source":20,"target":14,"value":1},{"source":14,"target":8,"value":1},{"source":8,"target":3,"value":1},{"source":3,"target":10,"value":1}]};

var width = 500,
    height = 500;

var color = d3.scale.category20();

var force = d3.layout.force()
    .charge(-700)
    .linkDistance(180)
    .size([width, height]);

var svg = d3.select("#clicks-graph").append("svg")
    .attr("width", width)
    .attr("height", height);
    
force
    .nodes(graph.nodes)
    .links(graph.links)
    .start();

var link = svg.selectAll(".link")
    .data(graph.links)
    .enter().append("line")
    .attr("class", "link")
    .style("stroke-width", function(d) { return Math.sqrt(d.value); });

var node = svg.selectAll(".node")
    .data(graph.nodes)
    .enter().append("g")
    .attr("class", "node")
    .call(force.drag);

node.append("circle")
    .attr("r", 10)
    .style("fill", function (d) {
    if (d.name.startsWith("other")) { return color(1); } else { return color(2); };
})

node.append("text")
      .attr("dx", 10)
      .attr("dy", ".35em")
      .text(function(d) { return d.name });
      
//Now we are giving the SVGs co-ordinates - the force layout is generating the co-ordinates which this code is using to update the attributes of the SVG elements
force.on("tick", function () {
    link.attr("x1", function (d) {
        return d.source.x;
    })
        .attr("y1", function (d) {
        return d.source.y;
    })
        .attr("x2", function (d) {
        return d.target.x;
    })
        .attr("y2", function (d) {
        return d.target.y;
    });
    d3.selectAll("circle").attr("cx", function (d) {
        return d.x;
    })
        .attr("cy", function (d) {
        return d.y;
    });
    d3.selectAll("text").attr("x", function (d) {
        return d.x;
    })
        .attr("y", function (d) {
        return d.y;
    });
});
</script>
</div>
</p>

``` scala
val graphStar: Graph[Int, Int] = GraphGenerators.starGraph(sc, 10)
val gS = GraphFrame.fromGraphX(graphStar)
val gSE= gS.edges.select($"src", $"dst".as("dest"), lit(1L).as("count")) // for us the column count is just an edge incidence
d3.graphs.force(
  height = 500,
  width = 500,
  clicks = gSE.as[d3.Edge])
```

<p class="htmlSandbox">
<style>

.node_circle {
  stroke: #777;
  stroke-width: 1.3px;
}

.node_label {
  pointer-events: none;
}

.link {
  stroke: #777;
  stroke-opacity: .2;
}

.node_count {
  stroke: #777;
  stroke-width: 1.0px;
  fill: #999;
}

text.legend {
  font-family: Verdana;
  font-size: 13px;
  fill: #000;
}

.node text {
  font-family: "Helvetica Neue","Helvetica","Arial",sans-serif;
  font-size: 17px;
  font-weight: 200;
}

</style>

<div id="clicks-graph">
<script src="//d3js.org/d3.v3.min.js"></script>
<script>

var graph = {"nodes":[{"name":"8"},{"name":"4"},{"name":"9"},{"name":"5"},{"name":"6"},{"name":"1"},{"name":"0"},{"name":"2"},{"name":"7"},{"name":"3"}],"links":[{"source":5,"target":6,"value":1},{"source":7,"target":6,"value":1},{"source":9,"target":6,"value":1},{"source":1,"target":6,"value":1},{"source":3,"target":6,"value":1},{"source":4,"target":6,"value":1},{"source":8,"target":6,"value":1},{"source":0,"target":6,"value":1},{"source":2,"target":6,"value":1}]};

var width = 500,
    height = 500;

var color = d3.scale.category20();

var force = d3.layout.force()
    .charge(-700)
    .linkDistance(180)
    .size([width, height]);

var svg = d3.select("#clicks-graph").append("svg")
    .attr("width", width)
    .attr("height", height);
    
force
    .nodes(graph.nodes)
    .links(graph.links)
    .start();

var link = svg.selectAll(".link")
    .data(graph.links)
    .enter().append("line")
    .attr("class", "link")
    .style("stroke-width", function(d) { return Math.sqrt(d.value); });

var node = svg.selectAll(".node")
    .data(graph.nodes)
    .enter().append("g")
    .attr("class", "node")
    .call(force.drag);

node.append("circle")
    .attr("r", 10)
    .style("fill", function (d) {
    if (d.name.startsWith("other")) { return color(1); } else { return color(2); };
})

node.append("text")
      .attr("dx", 10)
      .attr("dy", ".35em")
      .text(function(d) { return d.name });
      
//Now we are giving the SVGs co-ordinates - the force layout is generating the co-ordinates which this code is using to update the attributes of the SVG elements
force.on("tick", function () {
    link.attr("x1", function (d) {
        return d.source.x;
    })
        .attr("y1", function (d) {
        return d.source.y;
    })
        .attr("x2", function (d) {
        return d.target.x;
    })
        .attr("y2", function (d) {
        return d.target.y;
    });
    d3.selectAll("circle").attr("cx", function (d) {
        return d.x;
    })
        .attr("cy", function (d) {
        return d.y;
    });
    d3.selectAll("text").attr("x", function (d) {
        return d.x;
    })
        .attr("y", function (d) {
        return d.y;
    });
});
</script>
</div>
</p>

Now, write code to simulate from a mixture of graphs models
===========================================================

-   See `037a_...` and `037b_...` notebooks for the file writing pattern.
-   First try, grid and star with 98%-2% mixture, respectively
-   Second, try a truly random graph like lognormal degree distributed random graph and star
-   Try to make a simulation of random networks that is closer to your domain of application (you can always drop in to python and R for this part - even using non-distributed algorithms for simulating large enough networks per burst).

``` scala
val graphGrid: Graph[(Int,Int), Double] = GraphGenerators.gridGraph(sc, 50,50)
val gG = GraphFrame.fromGraphX(graphGrid)
gG.edges.count
```

>     graphGrid: org.apache.spark.graphx.Graph[(Int, Int),Double] = org.apache.spark.graphx.impl.GraphImpl@160a43de
>     gG: org.graphframes.GraphFrame = GraphFrame(v:[id: bigint, attr: struct<_1: int, _2: int>], e:[src: bigint, dst: bigint ... 1 more field])
>     res10: Long = 4900

``` scala
val graphStar: Graph[Int, Int] = GraphGenerators.starGraph(sc, 101)
val gS = GraphFrame.fromGraphX(graphStar)
gS.edges.count
```

>     graphStar: org.apache.spark.graphx.Graph[Int,Int] = org.apache.spark.graphx.impl.GraphImpl@4395a75
>     gS: org.graphframes.GraphFrame = GraphFrame(v:[id: bigint, attr: int], e:[src: bigint, dst: bigint ... 1 more field])
>     res13: Long = 100

``` scala
val gAllEdges = gS.edges.union(gG.edges)
gAllEdges.count
```

>     gAllEdges: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [src: bigint, dst: bigint ... 1 more field]
>     res16: Long = 5000

``` scala
100.0/5000.0
```

>     res20: Double = 0.02

