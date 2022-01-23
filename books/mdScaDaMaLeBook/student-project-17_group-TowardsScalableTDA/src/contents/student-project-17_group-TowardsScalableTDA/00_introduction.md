<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Density Estimation via Voronoi Diagrams in High Dimensions
==========================================================

</div>

<div class="cell markdown">

Robert Gieselmann and Vladislav Polianskii

[Video of project presentation](https://drive.google.com/file/d/14E_igECN6hDZieWNn9VVTepCo5mu-rzy/view?usp=sharing)

</div>

<div class="cell markdown">

Introduction
------------

</div>

<div class="cell markdown">

**Density estimation** is a wide sub-area of statistics, tasked with understanding an underlying probability distribution of a given set of points, sampled from an unknown distribution. It can be used as a way of data investigation, like determining the location of low- and high-density regions in data, clusters and outliers, as well as for visualization purposes.

A histogram can be considered as a simple density estimator. Other well-known methods include: - a k-nearest-neighbor density estimator, which describes the density *p()* at a point *x* as \\[p(x) \cong \frac{1}{d_k(x)}\\] where d\_k(x) is the distance to the *k*th nearest neighbor of *x*; - a kernel density estimator, which requires a selection of a kernel probability distribution *K* and a bandwidth *h* and essentially places the distributions at the data points, giving the density estimation \\[p(x) \cong \sum_i K(\frac{x - x_i}{h})\\]

All of the mentioned methods are sensitive to parameter selection, such as choosing the right number of neighbors or a fitting bandwidth.

</div>

<div class="cell markdown">

**Voronoi diagrams** are widely used in many areas, including computer science, and provide a natural cell decomposition of space based on the nearest-neighbor rule. For a given data point *x*, its corresponding cell contains all the points of the metric space, for which *x* is the closest point among all in the dataset.

An example of a 2D Voronoi diagram built over a set of points sampled from a normal distribution can be seen below in the methodology part.

One of the biggest drawbacks of Voronoi diagrams is their geometric complexity, which grows exponentially with dimensionality and essentially prevents their exact computation in dimensions above 6 for a reasonable number of points. In the worst case, the number of geometric elements of the diagram (such as Voronoi vertices, edges and polyhedra of different dimensions that arise on the cell boundaries) grows as

\\[O(n^{\lceil{d/2}\rceil})\\]

</div>

<div class="cell markdown">

**Our method.** In this work, we use some intuition about the Voronoi diagrams to develop a new method of density estimation. In addition, we apply a methodology from our previous work which allows one to work with Voronoi diagrams in high dimensions without their explicit construction.

</div>

<div class="cell markdown">

Methodology
-----------

</div>

<div class="cell markdown">

**Intuition:** if we construct a Voronoi diagram over a set of points sampled from an unknown distribution then Voronoi cells in regions with higher density will be of a smaller *size*.

Consider the image below, which depicts a Voronoi diagram in a two-dimensional space built over points sampled from a Gaussian distribution. Voronoi cells in the center of the distribution appear naturally smaller in comparison with other cells, and the cell size increases when we move away from the center.

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/voronoi_gaussian.png?raw=true)

This intuition follows, in a way, a one-nearest-neighbor density estimator: the distance *d* to the nearest neighbor is inversly proportional to the estimated density of the point, and at the same time, a ball of radius *d/2* centered at the query point always fits into (and touches the boundary of) the Voronoi cell.

On the discussed image, one of the cells is marked with a blue color. Assume that the point inside that cell is our query point, at which we want to understand the density, and all other points are the training (unlabeled) data that provides information about the density. Then, let us try to find a reasonable approximation of the density in a form of

\\[p(x) = \frac{c}{size(Cell(x))}\\]

where *c* is some constant, *Cell* denotes the Voronoi cell of *x*, and *size* is some measure of a cell.

Note: at any moment, the Voronoi diagram consists of only one query point and all dataset points.

</div>

<div class="cell markdown">

**Volume function**

Let us assume for a while that cell's geometry is known to us. What would be a natural way to describe the size of the cell?

Perhaps, one of the first ideas that comes to mind is to use the cell's *volume* as a size measure. Here we run into an issue of infinite cells, whose volume would also be infinite. Potentially, this could be resolved by computing a weighted volume with an integrable weight function that rapidly decays at infinity.

However, instead, we propose a way to describe the size via *volume functions*, inspired by how alpha-complexes are motivated and constructed in the area of topological data analysis, where we consider a set of balls of an increasing radius with intersection with voronoi cells:

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/alpha_1.png?raw=true) ![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/alpha_2.png?raw=true) ![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/alpha_3.png?raw=true)

We define the volume function as follows:

\\[\overline{Vol}_d(x)(r) = \frac{Vol_d(Cell(x) \cap B_r(x))}{Vol_d(B_r)}\\]

Here, *r* is a positive radius, *Vol()* denotes the standard d-dimensional volume, and \*B\_r(x)\* is a d-dimensional ball of radius *r* centered at *x*. The volume function of *x* returns a function that takes a radius *r* and returns a ratio of the volume of the intersection of the ball with the cell to the whole volume of the ball. Clearly, at the limit to zero, the ratio is equal to 1 (when the ball fully fits inside the cell), but starts to decrease as soon as parts of the ball start to leave the boundary.

Below are two images. On the left, a simple rectangular Voronoi cell with a point, generating it. On the right, a depiction of the volume function for this cell.

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/rect.png?raw=true) ![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/rect_vol.png?raw=true)

If we go into higher dimensions, we will not be able to see the steps that the function makes anymore. Below is an example, which we approximated (with a method described below) on MNIST data (784-dimensional) some time ago of volume functions for different data points:

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/mnist_vol.png?raw=true)

On the picture above, we can guess that, for example, the point with the light-blue volume curve is located in a lower-density region than other given points, based on the fact that its volume function is greater than other functions at every radius.

A couple of things to consider here. 1. If a cell is infinite, then its volume function will not tend to 0 at infinity. Instead, it will tend to the angular size of this infinity. 2. If one cell can be placed inside another cell, identifying their generator points and rotating arbitrarily, the first volume function will be below the second volume function.

The second bullet point provides an idea that maybe we want to integrate this volume functions and compare them: a function with a larger integral would denote a lower-density region. At the same time, the first bullet point tells us that the functions are not always integrable. Thus, in this project we do the following modifications: we do not consider the directions of the balls which end up in infinity. To be more precise, we replace \*B\_r\* with its *sector* where the voronoi cell is finite, in the formula for the volume function. This helps to mitigate the integrability issues.

Before we go into details about the computational aspects, we need to mention another modification to the formula. Instead of computing the d-dimensional volumes of balls, we decided to compute the (d-1)-dimensional volumes of spheres (or, the surface area of the balls). This modification makes the computation much easier. For example, the approximations of the volume functions become piecewise-constant.

Therefore, the formula for the *size(x)* becomes:

\\[size(x) = \int_0^{inf}{\overline{Vol}_{d-1}(x)(r) dr} = \int_0^{inf}{ \frac{Vol_{d-1}(Cell(x) \cap \hat{S}_r(x))}{Vol_{d-1}( \hat{S}_r )} dr}\\]

where \*S\_r(x)\* denotes a hypersphere of radius *r*, and a "^" denotes that we only consider sections of a sphere where the cell is finite.

</div>

<div class="cell markdown">

**Integral computation.**

We perform a Monte-Carlo sampling integration method to approximate the volume function, a motivation for which is described in detail in one of our earlier papers about Voronoi Boundary Classification (http://proceedings.mlr.press/v97/polianskii19a.html).

In short details, we sample random rays in uniform directions (equivalently, we sample points uniformly on the unit hypersphere), starting from the query point. For each ray, we record where it hits the boundary of the Voronoi cell. The length is computed by the following equation:

\\[l(x, m) = \min_{i=1..N, \langle m, x - x_i \rangle > 0} \frac{\lVert x - x_i \rVert^2}{2\langle m, x - x_i \rangle }\\]

Here, *x* is the origin of the ray (the generator/query point), *m* is the directional unit vector, \*x\_i\* are other data points. The "infinite" directions are excluded. The condition in the minimum signifies, that we are only interested in the positive length, i.e. we can't find an intersection behind the ray.

After casting *T* rays from a point, we can approximate the volume function as:

\\[\overline{Vol}_{d-1}(x)(r) = \frac{1}{T}\sum_{t=1}^{T} \mathbb{1}\left[l(x, m_t) \ge r \right]\\]

The integral of the function can be easily computed as a sum of all lengths:

\\[size(x) = \frac{1}{T}\sum_{t=1}^{T} l(x, m_t)\\]

And, our (unnormalized) density:

\\[\tilde{p}(x) = \frac{T}{\sum_{t=1}^{T} l(x, m_t)}\\]

Overall, the method's compexity with some optimizations is:

\\[O(NMT + NMD + NTD + MTD)\\]

where *N* is the number of train points, *M* is the number of query points, *T* is the number of rays from each point and *D* is data dimensionality.

</div>

<div class="cell markdown">

**Ranking loss.**

At the moment, we do not have any proofs that this indeed generates an unnormalized approximation for the density.

However, we are fairly certain (though also without a proof) that the approximation, when the dataset size tends to infinity, approximates the correct "ranking" of the estimates. Namely,

\\[p(x_1) < p(x_2) \Leftrightarrow \tilde{p}(x_1) < \tilde{p}(x_2)\\]

with probability 1 when data size is large enough. Here *p* is the real density used for point sampling, and *\\tilde{p}* is the approximation.

This quality is meaningful in tasks when we need to sort points according to their density. For example, if we want to exclude noise (say, 5% of the all points with the lowest density), or use for density filtration in topological data analysis.

A measure that we use to estimate how well we approximate the correct density ranking works as following: 1. Sort available query points according to their true density. 2. Sort available query points according to the approximated density. 3. Find the number of inverses (swaps of two consecutive elements) required to obtain the first sequence of points from the second one.

The can easily be counted with a merge-sort algorithm in n log n time, but for simplicity and testing purposes (also because we use python for that) we do it in a simple quadratic time.

</div>
