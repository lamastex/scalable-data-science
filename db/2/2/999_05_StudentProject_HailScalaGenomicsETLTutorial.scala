// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/xqy5geCNKCg/0.jpg)](https://www.youtube.com/embed/xqy5geCNKCg?start=1456&end=3956&autoplay=1) [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/qMGKAERggU8/0.jpg)](https://www.youtube.com/embed/qMGKAERggU8?start=0&end=3028&autoplay=1)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # Hail Tutorial in Scala for Population Genomics ETL
// MAGIC ## Student Project
// MAGIC by [Dmytro Kryvokhyzha](https://github.com/evodify) and [Yevgen Ryeznik](https://github.com/yevgenryeznik)
// MAGIC 
// MAGIC In this tutorial, we will analyze data from the [1000 Genomes Project](http://www.internationalgenome.org/about) as described in [Analyzing 1000 Genomes with Spark and Hail](https://docs.databricks.com/spark/latest/training/1000-genomes.html) except that we will use [Scala](https://www.scala-lang.org/) instead of [Python](https://www.python.org/).
// MAGIC 
// MAGIC [Hail](https://hail.is) uses Python wrapper on top of [Apache Spark](https://spark.apache.org/docs/latest/index.html). We re-wrote the [Analyzing 1000 Genomes with Spark and Hail](https://docs.databricks.com/spark/latest/training/1000-genomes.html) tutorial in Scala with some modification and more up-to-date code as our course project for the [Scalable Data Science 2.2](https://lamastex.github.io/scalable-data-science/sds/2/2/) course.
// MAGIC 
// MAGIC We believe this tutorial will help to understand the code of Hail better and it will be helpful for those who plan to develop genomic analyses in Scala and contribute to the Hail project.

// COMMAND ----------

// MAGIC %md
// MAGIC ## Introduction
// MAGIC 
// MAGIC Before we start, we would like to provide you a short introduction to how the genomic data is obtained and motivate the need to treat it as Big Data.

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC **The importance of big data analyses in genomics is the best explained by this recent paper.**
// MAGIC 
// MAGIC ## [Big Data: Astronomical or Genomical?](http://journals.plos.org/plosbiology/article?id=10.1371/journal.pbio.1002195)
// MAGIC 
// MAGIC ### Abstract
// MAGIC 
// MAGIC Genomics is a Big Data science and is going to get much bigger, very soon, but it is not known whether the needs of genomics will exceed other Big Data domains. Projecting to the year 2025, we compared genomics with three other major generators of Big Data: astronomy, YouTube, and Twitter. Our estimates show that genomics is a “four-headed beast” - it is either on par with or the most demanding of the domains analyzed here in terms of data acquisition, storage, distribution, and analysis. We discuss aspects of new technologies that will need to be developed to rise up and meet the computational challenges that genomics poses for the near future. Now is the time for concerted, community-wide planning for the “genomical” challenges of the next decade.
// MAGIC 
// MAGIC <img src="http://journals.plos.org/plosbiology/article/figure/image?size=large&id=10.1371/journal.pbio.1002195.g001" width="800">
// MAGIC 
// MAGIC The plot shows the growth of DNA sequencing both in the total number of human genomes sequenced (left axis) as well as the worldwide annual sequencing capacity (right axis: Tera-basepairs (Tbp), Peta-basepairs (Pbp), Exa-basepairs (Ebp), Zetta-basepairs (Zbps)).
// MAGIC 
// MAGIC 
// MAGIC <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/e/e7/Historic_cost_of_sequencing_a_human_genome.svg/800px-Historic_cost_of_sequencing_a_human_genome.svg.png" width="800">
// MAGIC 
// MAGIC The price of genomic data production has also dropped dramatically in the recent years.

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ## DNA sequencing
// MAGIC 
// MAGIC <img src="https://www.yourgenome.org/sites/default/files/illustrations/process/physical_mapping_STS_yourgenome.png" width="800">
// MAGIC 
// MAGIC During the DNA sequencing procedure, DNA is chopped into small fragments and each fragment is then read by the sequencing machine. Using the variant sites in these DNA fragments (called reads), the overlap in these DNA reads can be found and a continuous genome sequence can be assembled.
// MAGIC 
// MAGIC However, the genome assembly from these short reads is not as trivial as it looks on this picture. So, these genomic reads are usually mapped to the reference genome that has been assembled already. You can see how this mapping looks in the cell below.

// COMMAND ----------

// MAGIC %md
// MAGIC ## Aligning millions of small DNA sequences (reads) to a reference genome
// MAGIC 
// MAGIC You can obtain this picture by loading your alignment in [BAM](http://software.broadinstitute.org/software/igv/bam) format to the [Integrative Genomics Viewer (IGV)](http://software.broadinstitute.org/software/igv/)
// MAGIC 
// MAGIC <img src="https://github.com/evodify/genomic-analyses_in_apache-spark/raw/master/vcf_filtering_tutorial/genome_read_mapping.png" width="1000">
// MAGIC 
// MAGIC The top panel shows the genomic position in the reference. Next, you can see the vertical bars indicating the depth of coverage in a given position. Those grey horizontal bars represent DNA reads. The grey color in a read means that an allele (nucleotide) is identical to the reference in that position. If the genotype in a certain position is different fro the reference, the alternative allele (nucleotide) is shown. For example, you can see the highlighted heterozygous position that has two alleles. The allele A is covered by 22 reads and it is the reference allele. The allele G is an alternative allele that is why it is designated in reads and it is covered by 15 reads.
// MAGIC 
// MAGIC Given this alignment, one can obtain a table of variants in the variant call format (VCF).

// COMMAND ----------

// MAGIC %scala
// MAGIC 
// MAGIC // This allows easy embedding of publicly available information into any other notebook
// MAGIC // when viewing in git-book just ignore this block - you may have to manually chase the URL in frameIt("URL").
// MAGIC // Example usage:
// MAGIC // displayHTML(frameIt("https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation#Topics_in_LDA",250))
// MAGIC def frameIt( u:String, h:Int ) : String = {
// MAGIC       """<iframe 
// MAGIC  src=""""+ u+""""
// MAGIC  width="95%" height="""" + h + """"
// MAGIC  sandbox>
// MAGIC   <p>
// MAGIC     <a href="http://spark.apache.org/docs/latest/index.html">
// MAGIC       Fallback link for browsers that, unlikely, don't support frames
// MAGIC     </a>
// MAGIC   </p>
// MAGIC </iframe>"""
// MAGIC    }
// MAGIC displayHTML(frameIt("https://en.wikipedia.org/wiki/Variant_Call_Format",500))

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ## VCF in Hail
// MAGIC 
// MAGIC Hail uses the following scheme for the VCF.
// MAGIC 
// MAGIC <img src="https://hail.is/docs/stable/_images/hail-vds-rep.png" width="600">

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # Hail
// MAGIC 
// MAGIC [Hail](https://hail.is) is an open-source, scalable framework for exploring and analyzing genomic data. Its functionality is exposed through **Python** and backed by distributed algorithms built on top of **Apache Spark** to efficiently analyze gigabyte-scale data on a laptop or terabyte-scale data on a cluster, without the need to manually chop up data or manage job failures. Users can script pipelines or explore data interactively through **Jupyter notebooks** that flow between Hail with methods for genomics, *PySpark* with scalable *SQL* and *machine learning algorithms*, and *pandas* with *scikit-learn* and *Matplotlib* for results that fit on one machine. Hail also provides a flexible domain language to express complex quality control and analysis pipelines with concise, readable code.
// MAGIC 
// MAGIC #### Scaling Genetic Data Analysis with Apache Spark
// MAGIC [![Scaling Genetic Data Analysis with Apache Spark](http://img.youtube.com/vi/pyeQusIN5Ao/0.jpg)](https://www.youtube.com/embed/pyeQusIN5Ao)

// COMMAND ----------

// MAGIC %md 
// MAGIC 
// MAGIC # Cluster setup
// MAGIC 
// MAGIC First, download Hail source code and build `hail-all-spark.jar` file from the source code locally on your computer.
// MAGIC 
// MAGIC On Debian-based Linux, install compilers:
// MAGIC 
// MAGIC ```
// MAGIC sudo apt-get install g++ cmake
// MAGIC ```
// MAGIC On Mac OS X, install Xcode, available through the App Store, for the C++ compiler. CMake can be downloaded from the CMake website or through Homebrew. To install with Homebrew, run
// MAGIC ```
// MAGIC  brew install cmake
// MAGIC ```
// MAGIC 
// MAGIC Clone the Hail repository and build:
// MAGIC ```
// MAGIC git clone --branch 0.1 https://github.com/broadinstitute/hail.git
// MAGIC cd hail
// MAGIC ./gradlew -Dspark.version=2.0.2 shadowJar
// MAGIC ```
// MAGIC You can also download the pre-built `hail-all-spark.jar` from [here](https://github.com/lamastex/scalable-data-science/tree/master/dbcArchives/2017/parts/studentProjects/Kryvokhyzha-Ryeznik/hail-all-spark.jar).
// MAGIC 
// MAGIC Upload the built `build/libs/hail-all-spark.jar` to Databricks `Workspace > Shared > Libraries > Create Library` and click `Drop library JAR here to upload`
// MAGIC 
// MAGIC Next click the `Clusters` icon on the left sidebar and then `+Create Cluster`. For `Apache Spark Version`, select `Spark 2.0 (Auto-updating, Scala 2.11)`. In the Databricks cluster creation dialog, click on the `Spark` tab, and paste the text below into the `Spark config` box.
// MAGIC 
// MAGIC ```
// MAGIC spark.hadoop.io.compression.codecs org.apache.hadoop.io.compress.DefaultCodec,is.hail.io.compress.BGzipCodec,org.apache.hadoop.io.compress.GzipCodec
// MAGIC spark.sql.files.openCostInBytes 1099511627776
// MAGIC spark.sql.files.maxPartitionBytes 1099511627776
// MAGIC spark.hadoop.mapreduce.input.fileinputformat.split.minsize 1099511627776
// MAGIC spark.hadoop.parquet.block.size 1099511627776
// MAGIC ```
// MAGIC 
// MAGIC Go back to `Workspace > Shared > Libraries` click on the `hail-all-spark.jar` library and attach it to the created cluster.
// MAGIC 
// MAGIC Start the cluster and attach this notebook to it by clicking on your cluster name in menu `Detached` at the top left of this notebook.

// COMMAND ----------

// MAGIC %md ## Additional Cluster configuration
// MAGIC Some functions may not work and give the error: `java.lang.NoClassDefFoundError: is/hail/asm4s/AsmFunction2`. This is because Spark executors do not see the uploaded jar file.
// MAGIC 
// MAGIC To fix it, do the following steps.

// COMMAND ----------

// MAGIC %md
// MAGIC Find out the temporary name assigned to your jar file by Databricks:

// COMMAND ----------

val hailJar = dbutils.fs.ls("/FileStore/jars/")
  .map(_.name)
  .filter(_.contains("hail_all_spark"))
  .headOption
  .getOrElse(sys.error("Failed to find hail jar, make sure that steps above are run"))
println(s"Found the jar: $hailJar")

// COMMAND ----------

// MAGIC %md
// MAGIC Copy this temporary jar file (`$hailJar`) to all executors:

// COMMAND ----------

dbutils.fs.put("/databricks/init/install_hail.sh", s"""
#!/bin/bash
mkdir -p /mnt/driver-daemon/jars
mkdir -p /mnt/jars/driver-daemon
cp /dbfs/FileStore/jars/$hailJar /mnt/driver-daemon/jars/ 
cp /dbfs/FileStore/jars/$hailJar /mnt/jars/driver-daemon/
""", true)

// COMMAND ----------

// MAGIC %md Restart the cluster and you are ready to go.

// COMMAND ----------

// MAGIC %md # Processing

// COMMAND ----------

// MAGIC %md
// MAGIC ## Import Hail and create Hail Context

// COMMAND ----------

import is.hail._
val hc = HailContext(sc)

// COMMAND ----------

// MAGIC %md ## Specify the path to files 
// MAGIC These files are already uploaded and available in Databricks. If you decide to use your own files, please upload them to Databricks and move to the dbfs file system.

// COMMAND ----------

val vcf_path =  "/databricks-datasets/hail/data-001/1kg_sample.vcf.bgz"
val annotation_path = "/databricks-datasets/hail/data-001/1kg_annotations.txt"

// COMMAND ----------

// MAGIC %md ## Import the VCF file

// COMMAND ----------

var vds = hc.importVCF(vcf_path) // if input.bgz doesn't work, try to load uncompressed data.

// COMMAND ----------

// MAGIC %md ## Split multi-allelic variants
// MAGIC 
// MAGIC This method splits multi-allelic variants into biallelic variants. For example, the variant `1:1000:A:T,C` would become two variants: `1:1000:A:T` and `1:1000:A:C`.

// COMMAND ----------

vds = vds.splitMulti()

// COMMAND ----------

// MAGIC %md ## Annotate samples
// MAGIC This step will load information on each sample from the sample annotations file.
// MAGIC 
// MAGIC First, we import the annotation table. `impute=true` will infer column types automatically.

// COMMAND ----------

val table = hc.importTable(annotation_path, impute=true).keyBy("Sample")

// COMMAND ----------

// MAGIC %md ### Print the annotation table

// COMMAND ----------

display(table.toDF(hc.sqlContext))

// COMMAND ----------

// MAGIC %md ### Apply the annotation schema to the VDS

// COMMAND ----------

vds = vds.annotateSamplesTable(table, root="sa")

// COMMAND ----------

// MAGIC %md ### Print schemas

// COMMAND ----------

// MAGIC %md Sample schema:

// COMMAND ----------

print(vds.saSignature)

// COMMAND ----------

// MAGIC %md %md You can also print it in a more human-readable format: 

// COMMAND ----------

print(vds.saSignature.schema.prettyJson)

// COMMAND ----------

// MAGIC %md Variant schema:

// COMMAND ----------

print(vds.vaSignature.schema.prettyJson)

// COMMAND ----------

// MAGIC %md
// MAGIC Global schema

// COMMAND ----------

// vds.global_schema() in Hail's python API
print(vds.globalSignature)

// COMMAND ----------

// MAGIC %md *We have not applied any global schema, so it is empty.*

// COMMAND ----------

// MAGIC %md ### Count the number of samples

// COMMAND ----------

// MAGIC %md Total number of samples:

// COMMAND ----------

vds.nSamples

// COMMAND ----------

// MAGIC %md Number of samples per population:

// COMMAND ----------

display(table.toDF(hc.sqlContext).groupBy("Population").count())

// COMMAND ----------

// MAGIC %md ## Explore the data

// COMMAND ----------

// MAGIC %md Print summary statistics:

// COMMAND ----------

print(vds.summarize())

// COMMAND ----------

// MAGIC %md
// MAGIC The above output shows:
// MAGIC 
// MAGIC * 710 - samples
// MAGIC * 10961 - variants 
// MAGIC * 0.988 - successfully called genotypes.
// MAGIC * 23 - chromosomes
// MAGIC * 0 - multiallelic variants
// MAGIC * 10961 - SNPs
// MAGIC * 0 - MNP alternate alleles
// MAGIC * 0 - insertions
// MAGIC * 0 - deletions
// MAGIC * 0 - complex alternate alleles
// MAGIC * 0 - Number of star (upstream deletion)
// MAGIC * 2 - Highest number of alleles at any variant.
// MAGIC 
// MAGIC All these annotation variables can be called separately with a few extra steps described below:

// COMMAND ----------

// MAGIC %md Save the summarize output as a separate object:

// COMMAND ----------

val sumVDS = vds.summarize()

// COMMAND ----------

// MAGIC %md Now, place a dot after the summarize object and press TAB. You will be displayed all available variable. Select the variable of interest:

// COMMAND ----------

// sumVDS.       // remove the two back slashes "//" in front of the command, place the cursor after the dot and press TAB to see avaliable options.

// COMMAND ----------

// MAGIC %md ## Filter genotypes
// MAGIC 
// MAGIC Let's filter genotypes based on genotype quality (GQ) and read coverage (DP).
// MAGIC 
// MAGIC Here `g` is genotype, `v` is variant, `s` is sample, and annotations are accessible via `va`, `sa`, and `global`. 

// COMMAND ----------

// MAGIC %md Genotype quality is measured with a Phred quality score. You can read more about it in the next cell.

// COMMAND ----------

// This allows easy embedding of publicly available information into any other notebook
// when viewing in git-book just ignore this block - you may have to manually chase the URL in frameIt("URL").
// Example usage:
// displayHTML(frameIt("https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation#Topics_in_LDA",250))
def frameIt( u:String, h:Int ) : String = {
      """<iframe 
 src=""""+ u+""""
 width="95%" height="""" + h + """"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe>"""
   }
displayHTML(frameIt("https://en.wikipedia.org/wiki/Phred_quality_score",500))

// COMMAND ----------

// MAGIC %md ### Plot the genotype quality scores

// COMMAND ----------

// MAGIC %md Make a dataframe for genotypes:

// COMMAND ----------

val gtDF = vds.genotypeKT().flatten.toDF(hc.sqlContext)
display(gtDF)

// COMMAND ----------

// MAGIC %md Now extract the scores for genotype depth (`g.dp`) and genotype quality (`g.gq`) from the last column (`g`) with SQL.
// MAGIC 
// MAGIC There are many genotypes and using all of them make plotting computationally difficult. So, we will subset the score by randomly selecting 1% and limiting the number of rows to 3000. This should be enough to obtain general distributions and define cut-offs.

// COMMAND ----------

gtDF.createOrReplaceTempView("genotypes") 
val gtDFdpqc = spark.sql("SELECT g.dp as DEPTH, g.gq as GenQuality from genotypes WHERE g.dp IS NOT NULL AND g.gq IS NOT NULL and RAND() <= .01 LIMIT 3000")
display(gtDFdpqc)

// COMMAND ----------

// MAGIC %md Using Databricks built-in display option, make plots for the extracted scores:

// COMMAND ----------

display(gtDFdpqc)

// COMMAND ----------

// MAGIC %md From the plots, we can define that genotypes with genotype quality less than 10 and coverage less than 2 can be removed:

// COMMAND ----------

var vdsGfilter = vds.filterGenotypes("g.dp >= 2 && g.gq >= 10")

// COMMAND ----------

// MAGIC %md Check the average call rate in the filtered data:

// COMMAND ----------

print(vdsGfilter.summarize().callRate)

// COMMAND ----------

// MAGIC %md Remember, in the cmd 46, the call rate was 0.988. Now, the call rate is 0.84 because many genotypes were filtered out. 

// COMMAND ----------

// MAGIC %md ### Filter samples
// MAGIC If your dataset is large, you can also afford to remove samples with low call rate.

// COMMAND ----------

// MAGIC %md Calculate the samples quality scores:

// COMMAND ----------

vdsGfilter = vdsGfilter.sampleQC()

// COMMAND ----------

// MAGIC %md Extract the samples scores:

// COMMAND ----------

val saDF = vdsGfilter.samplesKT().flatten.toDF(hc.sqlContext)
display(saDF)

// COMMAND ----------

// MAGIC  %md 
// MAGIC Make a plot for `sa.qc.callRate`:

// COMMAND ----------

display(saDF)

// COMMAND ----------

// MAGIC %md Apply the filter:

// COMMAND ----------

var vdsGSfilter = vdsGfilter.filterSamplesExpr("sa.qc.callRate >= 0.60")

// COMMAND ----------

// MAGIC %md Check the results of filtering:

// COMMAND ----------

print(vdsGSfilter.summarize().samples)

// COMMAND ----------

// MAGIC %md There were 710 samples before, now there are 667 samples.

// COMMAND ----------

val saDFfilter = vdsGSfilter.samplesKT().flatten.toDF(hc.sqlContext)
display(saDFfilter)

// COMMAND ----------

// MAGIC %md ### Filter variants
// MAGIC We recommend to use the [GATK Best Practices](https://gatkforums.broadinstitute.org/gatk/discussion/2806/howto-apply-hard-filters-to-a-call-set) for assistance to define cut-off for variant scores.

// COMMAND ----------

// MAGIC %md First, we will annotate the variants because we removed some genotypes and samples, and this changed the scores. We will also use `cache()` for better performance.

// COMMAND ----------

vdsGSfilter = vdsGSfilter.variantQC().cache()

// COMMAND ----------

// MAGIC %md Then we will extract the annotation score to a data frame and show its content.

// COMMAND ----------

val vaDF = vdsGSfilter.variantsKT().flatten.toDF(hc.sqlContext)
display(vaDF)

// COMMAND ----------

// MAGIC %md Visualize any variant annotation score with Databricks plot options:

// COMMAND ----------

display(vaDF)

// COMMAND ----------

var vdsGSVfilter = vdsGSfilter.filterVariantsExpr("va.info.MQ >= 55.0 && va.info.QD >= 5.0")

// COMMAND ----------

// MAGIC %md Check how many sites have been removed:

// COMMAND ----------

print(vdsGSVfilter.summarize().variants)

// COMMAND ----------

// MAGIC %md There were 10961 variants beofre filtering. Now, there 10660 variant sites.

// COMMAND ----------

// MAGIC %md You can also make plots for the scores from the filtered dataset to make sure that all the filter worked as intended.

// COMMAND ----------

val vaDFfilter = vdsGSVfilter.variantsKT().flatten.toDF(hc.sqlContext)

// COMMAND ----------

display(vaDFfilter)

// COMMAND ----------

// MAGIC %md ## Write the filtered VDS to a file for future analyses

// COMMAND ----------

val out_path = "/1kg_filtered.vds"
vdsGSVfilter.write(out_path, overwrite=true)

// COMMAND ----------

// MAGIC %md After that you can simply load this already filtered file for the future analyses.

// COMMAND ----------

val vdsFiltered = hc.readVDS(out_path) 

// COMMAND ----------

// MAGIC %md ## PCA
// MAGIC 
// MAGIC To show you a simple analysis you can do with such genetic data, we will check if there is any genetic structure in this data. We will use a principal component analysis (PCA) for that.

// COMMAND ----------

val VDSpca = vdsFiltered.pca("sa.pca", k=2)

// COMMAND ----------

val pcaDF = VDSpca.samplesKT().flatten.toDF(hc.sqlContext)

// COMMAND ----------

// MAGIC %md Replace dots in column names because they are not recognized correctly by SQL

// COMMAND ----------

val pcaDFrenamed = pcaDF.toDF(pcaDF.columns.map(_.replace(".", "_")): _*)
display(pcaDFrenamed)

// COMMAND ----------

// MAGIC %md Extract the PCA scores to a dataframe.

// COMMAND ----------

pcaDFrenamed.createOrReplaceTempView("PCA") 
val pc12DF = spark.sql("SELECT sa_SuperPopulation as pop, sa_pca_PC1 as PC1, sa_pca_PC2 as PC2 from PCA")
display(pc12DF)

// COMMAND ----------

// MAGIC %md Make a scatterplot with `PC1` and `PC2`, and add `Pop` as a grouping key.

// COMMAND ----------

display(pc12DF)

// COMMAND ----------

// MAGIC %md You can see from this PCA that human genetic data is not homogeneous. There is a clear genetic differentiation between populations.

// COMMAND ----------

// MAGIC %md # Visualization
// MAGIC This pipeline can also be modified to make plots with R instead of Databricks' `display()`. R allows making more complex and more flexible plots.
// MAGIC 
// MAGIC Below is an example of an R plot for the PCA. The same approach can be applied to all plots above.

// COMMAND ----------

// MAGIC %md Create an R dataframe from a Spark object:

// COMMAND ----------

// MAGIC %r
// MAGIC rPCAdf <- as.data.frame(sql("SELECT sa_SuperPopulation as Population, sa_pca_PC1 as PC1, sa_pca_PC2 as PC2 from PCA"))
// MAGIC head(rPCAdf)

// COMMAND ----------

// MAGIC %md Using your favourite R library (*ggplot2* in this case), make a plot:

// COMMAND ----------

// MAGIC %r
// MAGIC library(ggplot2)
// MAGIC 
// MAGIC ggplot(rPCAdf, aes(x = PC1, y = PC2, color = Population)) + geom_point()

// COMMAND ----------

// MAGIC %md ## Summary
// MAGIC 
// MAGIC Data filtering:
// MAGIC 
// MAGIC  - Filter genotypes
// MAGIC  - Filter samples
// MAGIC  - Filter variants
// MAGIC  
// MAGIC 
// MAGIC *Variants can be filtered before samples filtering if samples are of greater priority in a study.*
// MAGIC 
// MAGIC Such genetic data can be analyzed in various ways. A PCA is just one simple example.