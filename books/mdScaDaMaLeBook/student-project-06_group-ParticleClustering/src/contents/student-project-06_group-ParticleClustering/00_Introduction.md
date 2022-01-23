<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Unsupervised clustering of particle physics data with distributed training
==========================================================================

Authors: Karl Bengtsson Bernander, Colin Desmarais, Daniel Gedon, Olga Sunneborn Gudnadottir
Video walk-through of the notebooks: https://drive.google.com/file/d/1D6DPETd2qVMpSJOLTRiVPjIdz\_-VbNVn/view?usp=sharing

</div>

<div class="cell markdown">

Note
====

This project was presented at the [**25th International Conference on Computing in High-Energy and Nuclear Physics**](https://indico.cern.ch/event/948465/timetable/?view=standard). A public recording of the presentation can be found [here](https://cds.cern.ch/record/2767279).

</div>

<div class="cell markdown">

This notebook contains a short introduction to the collider particle physics needed to understand the data and the model, a short introcution to the method and a short motivation for developing the method. If you want to jump directly to the code, skip to the next notebook!

</div>

<div class="cell markdown">

Introduction
------------

</div>

<div class="cell markdown">

At the European Organization for Nuclear Research, CERN, the inner workings of particle physics are probed by accelerating particles to close to the speed of light and letting them collide. In the collisions, the energy contained in the colliding particles reforms into new particles, and by studying this process a lot can be learned about their interactions. Several experiments have operated as part of CERN since it was founded in [1955](https://home.cern/about/who-we-are/our-history) and as of 2019, a total of [330 petabytes](https://home.cern/science/computing/data-preservation) of particle physics data was stored by the organization. By 2030 the volume of the stored data is expected to be of the order of exabytes.

In addition to the disk space needed for such datasets, the experiments also require immense computing resources. These are used for translating the electrical signals of the particle detectors into formats appropriate for data analysis, simulating particle collisions and detectors, and analysing data. Much data processing is parallelized and distributed among machines connected to the [Worldwide LHC Computing Grid](https://home.cern/science/computing/grid).

</div>

<div class="cell markdown">

### Look around inside the CERN computing center

</div>

<div class="cell code" execution_count="1" scrolled="auto">

``` python
displayHTML("""<iframe width="99%" height="340" src="https://my.matterport.com/show/?m=yYCddcrq6Zj" frameborder="0" allowfullscreen allow="xr-spatial-tracking"></iframe>""")
```

<div class="output execute_result html_result" execution_count="1">

<iframe width="99%" height="340" src="https://my.matterport.com/show/?m=yYCddcrq6Zj" frameborder="0" allowfullscreen allow="xr-spatial-tracking"></iframe>

</div>

</div>

<div class="cell markdown">

### See the activity of the Grid

[source](https://home.cern/science/computing/grid)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
displayHTML("""<iframe width="99%" height="340" src="https://videos.cern.ch/video/OPEN-VIDEO-2018-041-001" frameborder="0" allowfullscreen allow="xr-spatial-tracking"></iframe>""")
```

</div>

<div class="cell markdown">

### In these notebooks

</div>

<div class="cell markdown">

As the datasets collected at CERN get bigger and the effects searched for in data get smaller, the challenge is to find new and more efficient methods to process data. Not surprisingly, machine learning is garnering more and more attention within the experiments, and a lot of machine learning methods have been developed in recent years to do everything from simulating detectors to data analysis. With datasets sometimes on the order of TBs even after preprocessing, however, distributed learning is a valuable tool. In this and the accompanying notebooks we present the UCluster method developed by Mikuni and Canelli for unsupervised clustering of particle physics data. We have adapted the code for use in notebooks and added the functionality of distributed training. The original code and the paper accompanying it can be found below.

[Original Code](https://major.io/wp-content/uploads/2014/08/github-150x150.png)<br> [Paper](https://assets2.sorryapp.com/brand_logos/files/000/005/662/original/arxiv-lg-bold-512-cropped.png?1575381539)

</div>

<div class="cell markdown">

Background
----------

</div>

<div class="cell markdown">

### Elementary particles

</div>

<div class="cell markdown">

Everything around us -- that we can see, touch, and interact with -- is made up of tiny particles called atoms, which in turn are made up of even smaller particles: protons, neutrons and electrons. The protons and neutrons are also made up of even smaller particles -- the quarks. As far as we know, the quarks and the electrons are elementary particles, which means that they cannot be divided further into other particles. These three particles, two quarks and the electron, actually make up everything in our ordinary life. That's not the whole picture, though. Both the quarks and the electron exist in three generations, each generation heavier than the last but sharing the same fundamental nature. These are all matter particles, fermions, which includes also the almost massless neutrinos. In addition there are the force carriers, bosons, which is how the matter particles interact, and the Higgs boson which gives mass to the fermions. These particles and how they interact is contained in the Standard Model of Particle Physics, schematically depicted below:

</div>

<div class="cell markdown">

![The Standard Model](https://upload.wikimedia.org/wikipedia/commons/thumb/0/00/Standard_Model_of_Elementary_Particles.svg/1280px-Standard_Model_of_Elementary_Particles.svg.png)

</div>

<div class="cell markdown">

### The Large Hadron Collider and particle detectors

</div>

<div class="cell markdown">

To create the heavier particles of the Standard model than the ones we are surrounded with daily, we need higher energies. This is because mass and energy are related through Einstein's famous formula \\[E=mc^2\\] At CERN, The Large Hadron Collider (LHC) gives kinetic energy to protons by accelerating them through a long chain of more and more powerful accelerators. They are then made to collide with each other, and in that collision new particles form using the total energy that the protons had when they collided. At the collision points of the LHC there are particle detectors designed to detect all of the different products of the collision and their properties. Below is a simulation of the CMS detector, one of the two general purpose detectors at the LHC. Going inside the detector, we follow the two protons (in blue) as they collide and produce new particles. The tracks coming out from the collision are made by charged particles, and the rectangles are the different modules of the detector that register a signal as the particles transversed the detector.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
displayHTML("""<iframe width="99%" height="340" src="https://videos.cern.ch/video/CERN-VIDEO-2011-192-001" frameborder="0" allowfullscreen allow="xr-spatial-tracking"></iframe>""")
```

</div>

<div class="cell markdown">

### Jets

</div>

<div class="cell markdown">

Using sophisticated algorithms developed over decades the collisions are reconstructed from the electric signals from the detector. They determine which types of particles were present in the products of the collision and their kinetic properties, energy and momentum. Some particles need to be reconstructed in several steps, since they decay to other particles before they even reach the detector. The aforementioned quarks decay into sprays of many other particles, and we call this a jet. They are identified by clustering the particles together into a cone, as is shown on the left in the picture below. In some cases, the jet is part of a collimated system of decay products, such as the one shown on the right below. This happens at high energies and is called a boosted system. In that case, resolving individual jets is hard, and so the whole system is made into one "fat jet". In the picture below, a boosted top quark (the only quark that decays before it reaches the detector) decays into a b-quark giving rise to a jet and a W boson that then decays into two quarks that also give rise to jets. <a> <img border="0" alt="Jet cone with tracks" src="https://www.quantumdiaries.org/wp-content/uploads/2011/06/JetConeAndPFJetCALVIEW3.png" width="300" height="300"> </a> <a>!\[1\]</a> \[1\]: https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTML-vd7DejQvpeHGmpV-CDVOc1yw78luh\_YQ&usqp=CAU

</div>

<div class="cell markdown">

The UClusted algorithm
----------------------

</div>

<div class="cell markdown">

Now that we have the particle data background needed, let's try to understand the code and the data we will be working with. Most, if not all, of the algorithms used to reconstruct particles at the large LHC experiments right now are either traditional algorithms without machine learning or supervised machine learning. These methods could have the disadvantage of being biased, however, when it comes to discovering new particles or interactions. A lot of machine learning interested physicists are therefore looking toward unsupervised methods for object (particle) reconstruction and data analysis. One such approach is taken by **V. Mikuni and F. Canellia** in the 2020 paper, [**Unsupervised clustering for collider physics**](https://arxiv.org/pdf/2010.07106.pdf).

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
displayHTML("""<a href="https://arxiv.org/pdf/2010.07106.pdf">
<img border="0" alt="UCluster paper" src="https://paperswithcode.com/static/thumbs/2010.07106.jpg" width="300" height="300">
</a>""")
```

</div>

<div class="cell markdown">

### UCluster

</div>

<div class="cell markdown">

In the paper, Mikuni and Canelli present UCluster, which is an unsupervised clustering algorithm for particle physics data. In the paper, they apply it to one multiclass classification problem and one anomaly detection problem. In these notebooks, we present only the first.

### Jet classification

Given a jet, in the form of a list of particles contained in it and their properties, the task is to match it to the particle it came from. We choose three types of particles that can be reconstructed using fat jets: W bosons, Z bosons and top quarks. The dataset can be found [here](https://zenodo.org/record/3602254#.X8f8oRNKjP8). We start by preprocessing it to get it on the format we want and throwing away information we don't need. We keep only the names and properties of the constituent particles. The properties include trajectory angles, energy, momentum and distances to center of jet. They are used as input feature in a deep neural graph net, in which each particle is represented by a node. It is pre-trained, and then a clustering step is added, before the whole thing is trained again. The authors report a 81% classification accuracy using the Hungarian method. The clusters formed can be seen below to the right and should be compared to the ground truth shown on the left.

![1](https://inspirehep.net/files/b6a600d849d1a252d7d6e2510ee29354) ![2](https://inspirehep.net/files/05018da0d0f3d6aaef76c17ec17f6dee)

This type of task arises in many particle physics data analyses

</div>

<div class="cell markdown">

Motivation
----------

The type of task described above, in which particles are classified according to which process they come from, is a common one in particle physics data analyses. Whether a new process is searched for or the parameters of an already known process are measured, the analysis boils down to extracting a small signal from a large dataset. Most of the data is easy to get rid of -- if it doesn't contain the particles that the sought after decay produces for example -- but a lot of it becomes a background that needs to be accounted for. In many cases, Monte Carlo simulations exist to accurately enough estimate this background, but in others they don't. In those cases datadriven methods have to be used, which can quickly become a very complicated task if background from more than one process has to be estimated that way. Unsupervised classification could be used directly on data to estimate the background from different processes.

</div>

<div class="cell markdown">

Our contribution
----------------

The code we use comes from the UCluster git repository. Our contribution was to add the functionality of training the model in a distributed fashion. To do this, we use the Horovod runner, which necessitated a migration to TensorFlow 2 (from TensorFlow 1).

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>
