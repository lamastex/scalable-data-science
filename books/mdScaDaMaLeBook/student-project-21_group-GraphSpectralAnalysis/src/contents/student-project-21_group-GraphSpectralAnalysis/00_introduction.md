<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Graph Spectral Analysis
=======================

##### Project by Ciwan Ceylan and Hanna Hultin

Link to project video: "https://drive.google.com/file/d/1ctILEsMskFgpsVnu-6ucCMZqM1TLXfEB/view?usp=sharing"

</div>

<div class="cell markdown">

Background on graphs
--------------------

A graph can be represented by its incidence matrix **B*0**. Each row of **B*0** corresponds to an edge in the graph and each column to a node. Say that row *k* corresponds to edge *i* -\> *j*. Then element *i* of row *k* is *-1* and element *j* is *1*. All other elements are zero. See the figure below for an example of the indicence matrix with the corresponding graph.

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/incidence_matrix.png?raw=true) ![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/simple_graph.png?raw=true)

</div>

<div class="cell markdown">

### Graph Laplacian

The Laplacian lies at the center of *specral graph theory*. Its spectrum (its eigenvalues) encodes the geometry of the graph and can be used in various applications ranging from computer graphics to machine learning. Therefore, one approximative approach for comparing graphs (a problem which is NP-hard) is to compare their spectra. Graphs with similar geometry are expected to have similar spectrum and vice-versa. Below is an example of the Laplacian for the graph seen in the cell above. The diagonal elements contain the degree of the corresponding node, while all other elements at index (*i*,*j*) are -1 if there is an edge between the nodes *i* and *j* and zero otherwise.

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/simple_laplacian.png?raw=true)

The Laplacian can be constructed from the indicence matrix as \\[ \mathbf{L} = \mathbf{B}_0^T \mathbf{B}_0 \\] Thus, we can compute the top eigenvalues of **L** by instead computing the top singular values of \*\*B\_0\*\*. This follows from the following: \\[ \mathbf{B}_0 = \mathbf{U} \mathbf{D}^{1/2} \mathbf{V}^T \\] \\[ \mathbf{L}= \mathbf{V}  \mathbf{D}^{1/2} \mathbf{U}^T \mathbf{U} \mathbf{D}^{1/2} \mathbf{V}^T =  \mathbf{V}  \mathbf{D} \mathbf{V}^T \\]

#### Scaling to large graphs using randomized SVD

In the new age of big data, it is often interesting to analyze very large graphs of for example financial transactions. Doing the spectral graph analysis for these large graphs is challenging, since the full singular value decomposition of an *m x n* matrix scales as *O(m n min(m,n))*. To handle this, we turn to low rank approximations and specifically we use Randomized SVD.

Randomized SVD was introduced in 2011 in the article "Finding structure with randomness: Probabilistic algorithms for constructing approximate matrix decompositions" (https://arxiv.org/abs/0909.4061), and is a smart way of finding a low-rank approximation for the singular value decomposition using Gaussian vectors.

The basic idea is that given the *m x n* matrix *A*, we can create a sampling matrix *Y = AG* where *G* is a *n x k* Gaussian random matrix and it turns out that *Y* is then a quite good approximate basis for the column space of A.

A nice summary of the methods and some variations written by one of the authors of the original article can be found in the following link: https://sinews.siam.org/Details-Page/randomized-projection-methods-in-linear-algebra-and-data-analysis

</div>

<div class="cell markdown">

### Methods for generating random graphs

#### Erdős–Rényi model

In "On the Evoluation of Random Graphs" (https://users.renyi.hu/\~p\_erdos/1960-10.pdf), Erdős and Rényi describes the random graph with *n* vertices and *N* edges where the *N* edges are chosen at random among all the undirected possible edges.

#### R-MAT model

The Recursive Matrix (R-MAT) model introduced in the article "R-MAT: A Recursive Model for Graph Mining" (https://kilthub.cmu.edu/articles/R-MAT*A*Recursive*Model*for*Graph*Mining/6609113/files/12101195.pdf) is described as follows by the authors:

> "The basic idea behind R-MAT is to recursively subdivide the adjacency matrix into four equal-sized partitions, and distribute edges with in these partitions with unequal probabilities: starting off with an empty adjacency matrix, we "drop" edges into the matrix one at a time. Each edge chooses one of the four partitions with probabilities a; b; c; d respectively (see Figure1). Of course, a+b+c+d=1. The chosen partition is again subdivided into four smaller partitions, and the procedure is repeated until we reach a simplecell (=1 x 1 partition). This is the cell of the adjacency matrix occupied by the edge."

This is visualized in the following image.

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/rmat_picture.png?raw=true)

</div>

<div class="cell markdown">

Project specifications
----------------------

The goal of the project is to compare spectra of the Laplacian for different graphs.

### Data

-   Ethereum transactions:
    -   Original data from google cloud (https://cloud.google.com/blog/products/data-analytics/ethereum-bigquery-public-dataset-smart-contract-analytics)
    -   The dataset contains transactions from March 2018 to March 2020, aggregating per edge (same sender and receiver) and only keeping edges with at least 10 transactions with positive value
-   Randomly generated graphs using the two different methods explained above

### Notebooks

-   **01*preprocess*data**: preprocesses the Ethereum data using Python and PySpark and saves the graph information as parquet file
-   **02*generate*graphs**: generates random graphs in Scala using Spark (SQL and GraphX) and saves the graph information as parquet files
-   **03*compute*rsvd**: computes RSVD for the different graphs in Scala using Spark and the library Spark-RSVD and saves the singular values as parquet files
-   **04*analyse*eigenvalues**: computes the eigenvalues from the singular values and plots these for different graphs

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>
