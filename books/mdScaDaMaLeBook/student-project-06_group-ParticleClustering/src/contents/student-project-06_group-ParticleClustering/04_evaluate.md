<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Evaluation
----------

In this notebook we evaluate the trained model on new data. The data has already been downloaded in notebook 11 and is stored in the h5 directory together with the training data. At the end of this notebook we will have the clustering accuracy of the model on this new data.

</div>

<div class="cell markdown">

Import packages needed for the script and set the correct paths

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
import argparse
from argparse import Namespace
from math import *
import numpy as np
from datetime import datetime
import json
import os, ast
import sys
import socket
from sklearn.cluster import KMeans
np.set_printoptions(edgeitems=1000)
from scipy.optimize import linear_sum_assignment
import h5py
import tensorflow as tf
from tqdm import tqdm


from scipy.optimize import linear_sum_assignment

BASE_DIR = os.path.join(os.getcwd(), '06_LHC','scripts')  
sys.path.append(BASE_DIR)
sys.path.append(os.path.join(BASE_DIR, '..', 'models'))
sys.path.append(os.path.join(BASE_DIR, '..', 'utils'))
import provider
import gapnet_classify as MODEL
```

</div>

<div class="cell markdown">

Default settings

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
parserdict = {'gpu':0, #help='GPUs to use [default: 0]')
              'n_clusters':3,# type=int, default=3, #help='Number of clusters [Default: 3]')
              'max_dim':3, #type=int, default=3, #help='Dimension of the encoding layer [Default: 3]')
              'log_dir': 'log',#default='log', #help='Log dir [default: log]')
              'batch':1024,# type=int, default=512, #help='Batch Size  during training [default: 512]')
              'num_point':100, # type=int, default=100, #help='Point Number [default: 100]')
              'data_dir':'../h5/', #default='../h5', #help='directory with data [default: ../h5]')
              'nfeat':8,# type=int, default=8, #help='Number of features [default: 8]')
              'ncat':20, # type=int, default=20, #help='Number of categories [default: 20]')
              'name': "evaluation", #default="", #help='name of the output file')
              'h5_folder':'../h5/', #default="../h5/", #help='folder to store output files')
              'full_train':True,# default=False, action='store_true',
                    #help='load full training results [default: False]')
              'checkpoint_folder':'/dbfs/databricks/driver/06_LHC/logs/train/', #help: The folder where the checkpoint is saved. The script
                   #will retrieved the latest checkpoint created here.
             }

FLAGS = Namespace(**parserdict)
#LOG_DIR = os.path.join('..', 'logs', FLAGS.log_dir)
LOG_DIR = os.path.join(os.getcwd(), '06_LHC', 'logs', FLAGS.log_dir)
DATA_DIR = FLAGS.data_dir
H5_DIR = os.path.join(BASE_DIR, DATA_DIR)
H5_OUT = FLAGS.h5_folder
CHECKPOINT_PATH = FLAGS.checkpoint_folder
if not os.path.exists(H5_OUT): os.mkdir(H5_OUT)
```

</div>

<div class="cell markdown">

Some helper functions

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
#Calculate the clustering accuracy
def cluster_acc(y_true, y_pred):
    """
    Calculate clustering accuracy. Require scikit-learn installed
    """
    y_true = y_true.astype(np.int64)
    D = max(y_pred.max(), y_true.max()) + 1
    w = np.zeros((D, D), dtype=np.int64)
    for i in range(y_pred.size):
        w[y_pred[i], y_true[i]] += 1
    ind = linear_sum_assignment(w.max() - w)
    ind = np.asarray(ind)
    ind = np.transpose(ind)
    return sum([w[i, j] for i, j in ind]) * 1.0 / y_pred.size
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
# Find the latest checkpoint (the training script saves one every 1000th step)
def find_ckpt(path,base):
  files = os.listdir(os.path.join(path,os.listdir(path)[-1]))
  s=base+".ckpt-"
  ckpts = [r for r in files if s in r]
  numbers = [int(r.split('.')[1].split('-')[1]) for r in ckpts]
  ckpt = base+'.ckpt-'+str(np.max(numbers))
  return os.path.join(path,os.listdir(path)[-1],ckpt)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` sh
ls /dbfs/databricks/driver/06_LHC/logs/train/
```

<div class="output execute_result plain_result" execution_count="1">

    1608312111.5291479
    1608312856.6910331
    1608316348.7265332
    1608316368.303585
    1608316645.5256958
    1608317160.1003277
    1608317514.0408154
    1608317816.3961644
    1608545228.287947
    1609765453.8899894
    1614197922.0756063
    1614199473.6461
    1614199773.0030868
    1614199944.5590024
    1614205764.5543048
    1614253383.643093
    1614266637.6848218
    1614283969.6952937
    1614352318.071854
    1614372841.6587343
    1614427683.679712
    1614435113.1720457
    1614435261.5545285
    1614447752.0912156
    1614447848.5286212
    1614516429.048815
    1614516851.6496608
    1618854193.501687

</div>

</div>

<div class="cell markdown">

Run the evaluation script

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
NUM_POINT = FLAGS.num_point
BATCH_SIZE = FLAGS.batch
NFEATURES = FLAGS.nfeat
FULL_TRAINING = FLAGS.full_train

NUM_CATEGORIES = FLAGS.ncat
# Only used to get how many parts per categor
print('#### Batch Size : {0}'.format(BATCH_SIZE))
print('#### Point Number: {0}'.format(NUM_POINT))
print('#### Using GPUs: {0}'.format(FLAGS.gpu))

print('### Starting evaluation')

EVALUATE_FILES = provider.getDataFiles(os.path.join(H5_DIR, 'evaluate_files_wztop.txt'))


def eval():
    with tf.Graph().as_default():
        with tf.device('/gpu:' + str(FLAGS.gpu)):
            pointclouds_pl, labels_pl = MODEL.placeholder_inputs(BATCH_SIZE, NUM_POINT, NFEATURES)
            batch = tf.Variable(0, trainable=False)
            alpha = tf.compat.v1.placeholder(tf.float32, shape=())
            is_training_pl = tf.compat.v1.placeholder(tf.bool, shape=())
            pred, max_pool = MODEL.get_model(pointclouds_pl, is_training=is_training_pl, num_class=NUM_CATEGORIES)
            mu = tf.Variable(tf.zeros(shape=(FLAGS.n_clusters, FLAGS.max_dim)), name="mu",
                             trainable=False)  # k centroids

            classify_loss = MODEL.get_focal_loss(pred, labels_pl, NUM_CATEGORIES)
            kmeans_loss, stack_dist = MODEL.get_loss_kmeans(max_pool, mu, FLAGS.max_dim,
                                                            FLAGS.n_clusters, alpha)

            saver = tf.compat.v1.train.Saver()

        config = tf.compat.v1.ConfigProto()
        config.gpu_options.allow_growth = True
        config.allow_soft_placement = True
        sess = tf.compat.v1.Session(config=config)

        if FULL_TRAINING:
            #saver.restore(sess, os.path.join(LOG_DIR, 'cluster.ckpt'))
            saver.restore(sess, find_ckpt(CHECKPOINT_PATH,'cluster'))
        else:
            saver.restore(sess, find_ckpt(CHECKPOINT_PATH,'model'))
            #saver.restore(sess, os.path.join(LOG_DIR, 'model.ckpt'))
        print('model restored')

        ops = {'pointclouds_pl': pointclouds_pl,
               'labels_pl': labels_pl,
               'stack_dist': stack_dist,
               'kmeans_loss': kmeans_loss,
               'pred': pred,
               'alpha': alpha,
               'max_pool': max_pool,
               'is_training_pl': is_training_pl,
               'classify_loss': classify_loss, }

        eval_one_epoch(sess, ops)


def get_batch(data, label, start_idx, end_idx):
    batch_label = label[start_idx:end_idx]
    batch_data = data[start_idx:end_idx, :, :]
    return batch_data, batch_label


def eval_one_epoch(sess, ops):
    is_training = False

    eval_idxs = np.arange(0, len(EVALUATE_FILES))
    y_val = []
    acc = 0
    
    for fn in range(len(EVALUATE_FILES)):
        current_file = os.path.join(H5_DIR, EVALUATE_FILES[eval_idxs[fn]])
        current_data, current_label, current_cluster = provider.load_h5_data_label_seg(current_file)
        adds = provider.load_add(current_file, ['masses'])

        current_label = np.squeeze(current_label)

        file_size = current_data.shape[0]
        num_batches = file_size // BATCH_SIZE
       # num_batches = 5

        for batch_idx in range(num_batches):
            start_idx = batch_idx * BATCH_SIZE
            end_idx = (batch_idx + 1) * BATCH_SIZE

            batch_data, batch_label = get_batch(current_data, current_label, start_idx, end_idx)
            batch_cluster = current_cluster[start_idx:end_idx]
            cur_batch_size = end_idx - start_idx

            feed_dict = {ops['pointclouds_pl']: batch_data,
                         ops['labels_pl']: batch_label,
                         ops['alpha']: 1,  # No impact on evaluation,
                         ops['is_training_pl']: is_training,
                         }

            loss, dist, max_pool = sess.run([ops['kmeans_loss'], ops['stack_dist'],
                                             ops['max_pool']], feed_dict=feed_dict)
            cluster_assign = np.zeros((cur_batch_size), dtype=int)
            for i in range(cur_batch_size):
                index_closest_cluster = np.argmin(dist[:, i])
                cluster_assign[i] = index_closest_cluster

            batch_cluster = np.array([np.where(r == 1)[0][0] for r in current_cluster[start_idx:end_idx]])

            if len(y_val) == 0:
                y_val = batch_cluster
                y_assign = cluster_assign
                y_pool = np.squeeze(max_pool)
                y_mass = adds['masses'][start_idx:end_idx]
            else:
                y_val = np.concatenate((y_val, batch_cluster), axis=0)
                y_assign = np.concatenate((y_assign, cluster_assign), axis=0)
                y_pool = np.concatenate((y_pool, np.squeeze(max_pool)), axis=0)
                y_mass = np.concatenate((y_mass, adds['masses'][start_idx:end_idx]), axis=0)

    with h5py.File(os.path.join(H5_OUT, '{0}.h5'.format(FLAGS.name)), "w") as fh5:
        dset = fh5.create_dataset("pid", data=y_val)  # Real jet categories
        dset = fh5.create_dataset("label", data=y_assign)  # Cluster labeling
        dset = fh5.create_dataset("max_pool", data=y_pool)
        dset = fh5.create_dataset("masses", data=y_mass)
    
    print("Clustering accuracy is ",cluster_acc(y_val,y_assign))

################################################


if __name__ == '__main__':
    eval()
```

<div class="output execute_result plain_result" execution_count="1">

    #### Batch Size : 1024
    #### Point Number: 100
    #### Using GPUs: 0
    ### Starting evaluation
    INFO:tensorflow:Restoring parameters from /dbfs/databricks/driver/06_LHC/logs/train/1614516851.6496608/cluster.ckpt-13000
    model restored
    loaded 143984 events
    loaded 302664 events
    loaded 75666 events
    Clustering accuracy is  0.5005421075295275

</div>

</div>

<div class="cell markdown">

The clustering accuracy for the fully trained model should be output above. In addition, the .h5 file ../h5/evaluate.h5 containing information about true and predicted labels (as well as masses and pooling) has been written. This can be used to make visualizations such as the one in the introduction notebook (taken from the paper).

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
os.listdir(H5_OUT)
from tsne import bh_sne
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>
