<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Methodology
===========

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.mllib.random.RandomRDDs
import org.apache.spark.mllib.feature.Normalizer
import org.apache.spark.rdd.RDD
import breeze.linalg._
import breeze.numerics._
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.mllib.random.RandomRDDs
    import org.apache.spark.mllib.feature.Normalizer
    import org.apache.spark.rdd.RDD
    import breeze.linalg._
    import breeze.numerics._

</div>

</div>

<div class="cell markdown">

Constants required for the method.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Constants
val N = 250 // train size
val M = 250 // test size
val D = 2 // dimensionality
val T = 500 // number of rays
val one_vs_all = true

assert((!one_vs_all) || N == M)
```

<div class="output execute_result plain_result" execution_count="1">

    N: Int = 250
    M: Int = 250
    D: Int = 2
    T: Int = 500
    one_vs_all: Boolean = true

</div>

</div>

<div class="cell markdown">

Generate points from standard Gaussian distribution.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val train_data = RandomRDDs.normalVectorRDD(sc, N, D).zipWithIndex().map { case (v, i) => (i, new DenseVector(v.toArray)) }
val test_data = if(one_vs_all) train_data else RandomRDDs.normalVectorRDD(sc, M, D).zipWithIndex().map { case (v, i) => (i, new DenseVector(v.toArray)) }
```

<div class="output execute_result plain_result" execution_count="1">

    train_data: org.apache.spark.rdd.RDD[(Long, breeze.linalg.DenseVector[Double])] = MapPartitionsRDD[10486] at map at command-1767923094595286:1
    test_data: org.apache.spark.rdd.RDD[(Long, breeze.linalg.DenseVector[Double])] = MapPartitionsRDD[10486] at map at command-1767923094595286:1

</div>

</div>

<div class="cell markdown">

Generate T random rays

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
def get_uni_sphere() = {
  var u = RandomRDDs.normalVectorRDD(sc, T, D)
  u = new Normalizer().transform(u)
  var t = u.zipWithIndex().map { case (v, i) => (i, new DenseVector(v.toArray)) }
  t
}
  
val rays = get_uni_sphere()
```

<div class="output execute_result plain_result" execution_count="1">

    get_uni_sphere: ()org.apache.spark.rdd.RDD[(Long, breeze.linalg.DenseVector[Double])]
    rays: org.apache.spark.rdd.RDD[(Long, breeze.linalg.DenseVector[Double])] = MapPartitionsRDD[10490] at map at command-685894176423986:4

</div>

</div>

<div class="cell markdown">

Compute optimizations: all squared distances and all dot products of points with directions vectors.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
def compute_dst_sq() = { // (N, M)
  // dst[n, m] = |x_n - x'_m|^2
  val dst = train_data.cartesian(test_data).map { case ((n, train_vec), (m, test_vec)) => ((n, m), sum(((train_vec - test_vec) *:* (train_vec - test_vec)) ^:^ 2.0) ) }
  dst
}

def compute_pu(data: RDD[(Long, DenseVector[Double])]) = { // (data.N, T)
  // pu[n, t] = <data_n, ray_t>
  val pu = data.cartesian(rays).map { case ((n, data_vec), (t, ray_vec)) => ((n, t), data_vec dot ray_vec) }
  pu
}

val dst = compute_dst_sq()
val pu_train = compute_pu(train_data)
val pu_test = compute_pu(test_data)
```

<div class="output execute_result plain_result" execution_count="1">

    compute_dst_sq: ()org.apache.spark.rdd.RDD[((Long, Long), Double)]
    compute_pu: (data: org.apache.spark.rdd.RDD[(Long, breeze.linalg.DenseVector[Double])])org.apache.spark.rdd.RDD[((Long, Long), Double)]
    dst: org.apache.spark.rdd.RDD[((Long, Long), Double)] = MapPartitionsRDD[10492] at map at command-685894176423990:3
    pu_train: org.apache.spark.rdd.RDD[((Long, Long), Double)] = MapPartitionsRDD[10494] at map at command-685894176423990:9
    pu_test: org.apache.spark.rdd.RDD[((Long, Long), Double)] = MapPartitionsRDD[10496] at map at command-685894176423990:9

</div>

</div>

<div class="cell markdown">

Compute the lengths of all rays. The most expensive step.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
def compute_ray_lengths() = { // (M, T)
  // lengths[m, t, n] = dst[n, m] / (2 * (pu_train[n, t] - pu_test[m, t]))
  def compute_length(n: Long, m: Long, dst_val: Double, pu_train_val: Double, pu_test_val: Double) = {
    if (one_vs_all && n == m) {
      Double.PositiveInfinity
    } else {
      val res = dst_val / (2 * (pu_train_val - pu_test_val))
      if (res < 0) Double.PositiveInfinity else res
    }
  }
  
  def my_min(a: Double, b: Double) = {min(a, b)}
        
  val lengths = dst.cartesian(sc.range(0, T))
    .map { case (((n, m), dst_val), t) => ((n, t), (m, dst_val)) }  
    .join(pu_train) 
    .map { case ((n, t), ((m, dst_val), pu_train_val)) => ((m, t), (n, dst_val, pu_train_val)) }
    .join(pu_test) 
    .map { case ((m, t), ((n, dst_val, pu_train_val), pu_test_val)) => ((m, t), compute_length(n, m, dst_val, pu_train_val, pu_test_val)) } 
    .aggregateByKey(Double.PositiveInfinity)(my_min, my_min)  
  lengths
}

val lengths = compute_ray_lengths()
```

<div class="output execute_result plain_result" execution_count="1">

    compute_ray_lengths: ()org.apache.spark.rdd.RDD[((Long, Long), Double)]
    lengths: org.apache.spark.rdd.RDD[((Long, Long), Double)] = ShuffledRDD[10509] at aggregateByKey at command-685894176423991:20

</div>

</div>

<div class="cell markdown">

Compute the approximated weights.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
def compute_weights() = { // (M, )
  def agg_f(a: (Double, Double), b: (Double, Double)) = { (a._1 + b._1, a._2 + b._2) }
  
  val weights = lengths.map { case ((m, t), length) => (m, if (!length.isInfinity) (1.0, length) else (0.0, 0.0)) }
    .aggregateByKey((0.0, 0.0))(agg_f, agg_f)
    .map { case (m, (val1, val2)) => (m, if (val1 > 0) val1 / val2 else 0.0) }
  weights
}

val weights = compute_weights()
```

<div class="output execute_result plain_result" execution_count="1">

    compute_weights: ()org.apache.spark.rdd.RDD[(Long, Double)]
    weights: org.apache.spark.rdd.RDD[(Long, Double)] = MapPartitionsRDD[10512] at map at command-685894176424002:6

</div>

</div>

<div class="cell markdown">

Save obtained data in csv.

Note: we repartition the tables here to work with one csv only; this should be removed for larger data.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
def save_data(name: String, data: RDD[(Long, DenseVector[Double])]) = {
  data.map { case (k, v) => k.toString() + "," + v.toArray.mkString(",")}
    .toDF.repartition(1).write.format("csv").mode(SaveMode.Overwrite).option("quote", " ").save("dbfs:/FileStore/group17/data/" + name)
}

def save_weights(name: String, data: RDD[(Long, Double)]) = {
  data.map { case (k, v) => k.toString() + "," + v.toString}
    .toDF.repartition(1).write.format("csv").mode(SaveMode.Overwrite).option("quote", " ").save("dbfs:/FileStore/group17/data/" + name)
}

save_data("gaussian_train", train_data)
save_data("gaussian_test", test_data)
save_weights("gaussian_weights", weights)
```

<div class="output execute_result plain_result" execution_count="1">

    save_data: (name: String, data: org.apache.spark.rdd.RDD[(Long, breeze.linalg.DenseVector[Double])])Unit
    save_weights: (name: String, data: org.apache.spark.rdd.RDD[(Long, Double)])Unit

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>
