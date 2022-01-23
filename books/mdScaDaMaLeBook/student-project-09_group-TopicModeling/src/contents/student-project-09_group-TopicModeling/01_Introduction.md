<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Topic Modeling with SARS-Cov-2 Genome 🧬
=======================================

</div>

<div class="cell markdown">

Group Project Authors:

-   Hugo Werner
-   Gizem Çaylak [e-mail](mailto:caylak@kth.se)

</div>

<div class="cell markdown">

Video link: https://kth.box.com/s/y3jsb9lgp6cll6op15o6z77rchaefh24

</div>

<div class="cell markdown">

#### Problem description:

SARS-CoV-2 is spreading across the world and as it spreads mutations are occuring. A way to understand the spreading and the mutations is to explore the structure and information hidden in the genome.

</div>

<div class="cell markdown">

#### Project goal:

The Goal of this project is to explore a SARS-CoV-2 genome dataset and try to predict the origin of a SARS-CoV-2 genome sample.

</div>

<div class="cell markdown">

#### Data:

We will use publicly available NCBI SARS-CoV-2 genome with their geographic region information.

| Geographic region \| \# | \#samples \| |
|:-----------------------:|:------------:|
|          Africa         |    397 \|    |
|         Asia \|         |    2534 \|   |
|        Europe \|        |    1418 \|   |
|     North America \|    |     26836    |
|        Oceania \|       |     13304    |
|     South America \|    |    158 \|    |

Data link: https://www.ncbi.nlm.nih.gov/labs/virus/vssi/\#/virus?SeqType*s=Nucleotide&VirusLineage*ss=Severe%20acute%20respiratory%20syndrome%20coronavirus%202%20(SARS-CoV-2),%20taxid:2697049

</div>

<div class="cell markdown">

#### Background:

-   Genome: Sequence of nucleotides (A-T-G-C) \[https://en.wikipedia.org/wiki/Genome\]
-   k-mer: Subsequences of length k of a genome \[https://en.wikipedia.org/wiki/K-mer\]
-   Sequence analysis of SARS-CoV-2 genome reveals features important for vaccine design \[https://www.nature.com/articles/s41598-020-72533-2\]
-   Latent Dirichlet Allocation (LDA) tutorial from the course

</div>

<div class="cell markdown">

#### Challenges:

-   How to encode genome sequence?
    -   *Project solution :*
        -   Represent genome as k-mers and use countVectorizer to convert k-mers into a matrix of token counts (term-frequency table)
        -   Extract features with Latent Dirichlet Allocation (LDA) by considering each genome sequence as a document and each 3-mer as a word. So, we have a collection of genomes consisting of 3-mers.
-   How to relate encoded features to the origins?
    -   *Project solution:* We used a Random Forest Classifier and tried both topic distributions, LDA output, and k-mer frequencies directly. One advantage is interpretability: we can understand the positive or negative relations a topic has on the origin.
-   How to solve unbalanced class problem? E.g. North America has 26836 samples but South America has only 158
    -   *Project solution:* Use f1 measure as metric

</div>

<div class="cell markdown">

#### Project steps:

1.  Get SARS-CoV-2 data from NCBI
2.  Process data:
3.  Extract 3-mers:
4.  Split train/test dataset with split ratio 0.7
5.  Extract topic features: We used Latent Dirichlet Allocation to extract patterns from k-mers features.
6.  Classify: We used Random Forest Classifier
    -   (Classification directly on k-mers features) To find a mapping from extracted k-mers features to labels (multiclass problem).
    -   (Classification on LDA features) To find a mapping from extracted topic distributions to labels (multiclass problem).
7.  Evaluation: We use (accuracy and f1) measure as our evaluation metrics. We compared Classification on LDA features vs Classification directly on k-mers features to see whether LDA is capable of summarizing the data (and thus reducing the feature dimensionality)

</div>

<div class="cell markdown">

#### What we lack mainly:

-   A biological interpretation of the results (whether found terms in topic distributions are significant/connected in a biological network).

</div>
