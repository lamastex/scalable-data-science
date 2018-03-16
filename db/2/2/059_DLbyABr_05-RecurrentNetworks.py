# Databricks notebook source
# MAGIC %md
# MAGIC 
# MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
# MAGIC 
# MAGIC This is used in a non-profit educational setting with kind permission of [Adam Breindel](https://www.linkedin.com/in/adbreind).
# MAGIC This is not licensed by Adam for use in a for-profit setting. Please contact Adam directly at `adbreind@gmail.com` to request or report such use cases or abuses. 
# MAGIC A few minor modifications and additional mathematical statistical pointers have been added by Raazesh Sainudiin when teaching PhD students in Uppsala University.
# MAGIC 
# MAGIC Please feel free to refer to basic concepts here:
# MAGIC 
# MAGIC * Udacity's course on Deep Learning [https://www.udacity.com/course/deep-learning--ud730](https://www.udacity.com/course/deep-learning--ud730) by Google engineers: Arpan Chakraborty and Vincent Vanhoucke and their full video playlist:
# MAGIC     * [https://www.youtube.com/watch?v=X_B9NADf2wk&index=2&list=PLAwxTw4SYaPn_OWPFT9ulXLuQrImzHfOV](https://www.youtube.com/watch?v=X_B9NADf2wk&index=2&list=PLAwxTw4SYaPn_OWPFT9ulXLuQrImzHfOV)

# COMMAND ----------

# MAGIC %md
# MAGIC Archived YouTube video of this live unedited lab-lecture:
# MAGIC 
# MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/nHYXMZAHM1c/0.jpg)](https://www.youtube.com/embed/nHYXMZAHM1c?start=0&end=2465&autoplay=1) [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/4cD8ieyHVh4/0.jpg)](https://www.youtube.com/embed/4cD8ieyHVh4?start=0&end=2353&autoplay=1)

# COMMAND ----------

# MAGIC %md 
# MAGIC # Entering the 4th Dimension
# MAGIC ## Networks for Understanding Time-Oriented Patterns in Data
# MAGIC 
# MAGIC Common time-based problems include
# MAGIC * Sequence modeling: "What comes next?" 
# MAGIC     * Likely next letter, word, phrase, category, cound, action, value
# MAGIC * Sequence-to-Sequence modeling: "What alternative sequence is a pattern match?" (i.e., similar probability distribution)
# MAGIC     * Machine translation, text-to-speech/speech-to-text, connected handwriting (specific scripts)
# MAGIC     
# MAGIC <img src="http://i.imgur.com/tnxf9gV.jpg">

# COMMAND ----------

# MAGIC %md 
# MAGIC ### Simplified Approaches
# MAGIC 
# MAGIC * If we know all of the sequence states and the probabilities of state transition...
# MAGIC     * ... then we have a simple Markov Chain model.
# MAGIC 
# MAGIC * If we *don't* know all of the states or probabilities (yet) but can make constraining assumptions and acquire solid information from observing (sampling) them...
# MAGIC     * ... we can use a Hidden Markov Model approach.
# MAGIC     
# MAGIC These approached have only limited capacity because they are effectively stateless and so have some degree of "extreme retrograde amnesia."
# MAGIC 
# MAGIC ### Can we use a neural network to learn the "next" record in a sequence?
# MAGIC 
# MAGIC First approach, using what we already know, might look like
# MAGIC * Clamp input sequence to a vector of neurons in a feed-forward network
# MAGIC * Learn a model on the class of the next input record
# MAGIC 
# MAGIC Let's try it! This can work in some situations, although it's more of a setup and starting point for our next development.

# COMMAND ----------

# MAGIC %md
# MAGIC We will make up a simple example of the English alphabet sequence wehere we try to predict the next alphabet from a sequence of length 3.

# COMMAND ----------

alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
char_to_int = dict((c, i) for i, c in enumerate(alphabet))
int_to_char = dict((i, c) for i, c in enumerate(alphabet))

seq_length = 3
dataX = []
dataY = []
for i in range(0, len(alphabet) - seq_length, 1):
    seq_in = alphabet[i:i + seq_length]
    seq_out = alphabet[i + seq_length]
    dataX.append([char_to_int[char] for char in seq_in])
    dataY.append(char_to_int[seq_out])
    print (seq_in, '->', seq_out)

# COMMAND ----------

# dataX is just a reindexing of the alphabets in consecutive triplets of numbers
dataX

# COMMAND ----------

dataY # just a reindexing of the following alphabet after each consecutive triplet of numbers

# COMMAND ----------

# MAGIC %md 
# MAGIC Train a network on that data:

# COMMAND ----------

import numpy
from keras.models import Sequential
from keras.layers import Dense
from keras.layers import LSTM # <- this is the Long-Short-term memory layer
from keras.utils import np_utils

# begin data generation ------------------------------------------
# this is just a repeat of what we did above
alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
char_to_int = dict((c, i) for i, c in enumerate(alphabet))
int_to_char = dict((i, c) for i, c in enumerate(alphabet))

seq_length = 3
dataX = []
dataY = []
for i in range(0, len(alphabet) - seq_length, 1):
	seq_in = alphabet[i:i + seq_length]
	seq_out = alphabet[i + seq_length]
	dataX.append([char_to_int[char] for char in seq_in])
	dataY.append(char_to_int[seq_out])
	print (seq_in, '->', seq_out)
# end data generation ---------------------------------------------

X = numpy.reshape(dataX, (len(dataX), seq_length))
X = X / float(len(alphabet)) # normalize the mapping of alphabets from integers into [0, 1]
y = np_utils.to_categorical(dataY) # make the output we want to predict to be categorical

# keras architecturing of a feed forward dense or fully connected Neural Network
model = Sequential()
# draw the architecture of the network given by next two lines, hint: X.shape[1] = 3, y.shape[1] = 26
model.add(Dense(30, input_dim=X.shape[1], kernel_initializer='normal', activation='relu'))
model.add(Dense(y.shape[1], activation='softmax'))

# keras compiling and fitting
model.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['accuracy'])
model.fit(X, y, epochs=1000, batch_size=5, verbose=2)

scores = model.evaluate(X, y)
print("Model Accuracy: %.2f " % scores[1])

for pattern in dataX:
	x = numpy.reshape(pattern, (1, len(pattern)))
	x = x / float(len(alphabet))
	prediction = model.predict(x, verbose=0) # get prediction from fitted model
	index = numpy.argmax(prediction)
	result = int_to_char[index]
	seq_in = [int_to_char[value] for value in pattern]
	print (seq_in, "->", result) # print the predicted outputs

# COMMAND ----------

X.shape[1], y.shape[1] # get a sense of the shapes to understand the network architecture

# COMMAND ----------

# MAGIC %md 
# MAGIC The network does learn, and could be trained to get a good accuracy. But what's really going on here?
# MAGIC 
# MAGIC Let's leave aside for a moment the simplistic training data (one fun experiment would be to create corrupted sequences and augment the data with those, forcing the network to pay attention to the whole sequence).
# MAGIC 
# MAGIC Because the model is fundamentally symmetric and stateless (in terms of the sequence; naturally it has weights), this model would need to learn every sequential feature relative to every single sequence position. That seems difficult, inflexible, and inefficient.
# MAGIC 
# MAGIC Maybe we could add layers, neurons, and extra connections to mitigate parts of the problem. We could also do things like a 1D convolution to pick up frequencies and some patterns.
# MAGIC 
# MAGIC But instead, it might make more sense to explicitly model the sequential nature of the data (a bit like how we explictly modeled the 2D nature of image data with CNNs).

# COMMAND ----------

# MAGIC %md 
# MAGIC ## Recurrent Neural Network Concept
# MAGIC 
# MAGIC __Let's take the neuron's output from one time (t) and feed it into that same neuron at a later time (t+1), in combination with other relevant inputs. Then we would have a neuron with memory.__
# MAGIC 
# MAGIC We can weight the "return" of that value and train the weight -- so the neuron learns how important the previous value is relative to the current one.
# MAGIC 
# MAGIC Different neurons might learn to "remember" different amounts of prior history.
# MAGIC 
# MAGIC This concept is called a *Recurrent Neural Network*, originally developed around the 1980s.
# MAGIC 
# MAGIC Let's recall some pointers from the crash intro to Deep learning.
# MAGIC 
# MAGIC ### Watch following videos now for 12 minutes for the fastest introduction to RNNs and LSTMs
# MAGIC 
# MAGIC [Udacity: Deep Learning by Vincent Vanhoucke - Recurrent Neural network](https://youtu.be/LTbLxm6YIjE?list=PLAwxTw4SYaPn_OWPFT9ulXLuQrImzHfOV)
# MAGIC 
# MAGIC #### Recurrent neural network
# MAGIC ![Recurrent neural network](http://colah.github.io/posts/2015-08-Understanding-LSTMs/img/RNN-unrolled.png)  
# MAGIC [http://colah.github.io/posts/2015-08-Understanding-LSTMs/](http://colah.github.io/posts/2015-08-Understanding-LSTMs/)  
# MAGIC 
# MAGIC 
# MAGIC [http://karpathy.github.io/2015/05/21/rnn-effectiveness/](http://karpathy.github.io/2015/05/21/rnn-effectiveness/)  
# MAGIC ***
# MAGIC 
# MAGIC ***
# MAGIC ##### LSTM - Long short term memory
# MAGIC ![LSTM](http://colah.github.io/posts/2015-08-Understanding-LSTMs/img/LSTM3-chain.png)
# MAGIC 
# MAGIC ***
# MAGIC ##### GRU - Gated recurrent unit
# MAGIC ![Gated Recurrent unit](http://colah.github.io/posts/2015-08-Understanding-LSTMs/img/LSTM3-var-GRU.png)
# MAGIC [http://arxiv.org/pdf/1406.1078v3.pdf](http://arxiv.org/pdf/1406.1078v3.pdf)  
# MAGIC 
# MAGIC 
# MAGIC ### Training a Recurrent Neural Network
# MAGIC 
# MAGIC <img src="http://i.imgur.com/iPGNMvZ.jpg">
# MAGIC 
# MAGIC We can train an RNN using backpropagation with a minor twist: since RNN neurons with different states over time can be "unrolled" (i.e., are analogous) to a sequence of neurons with the "remember" weight linking directly forward from (t) to (t+1), we can backpropagate through time as well as the physical layers of the network.
# MAGIC 
# MAGIC This is, in fact, called __Backpropagation Through Time__ (BPTT)
# MAGIC 
# MAGIC The idea is sound but -- since it creates patterns similar to very deep networks -- it suffers from the same challenges:
# MAGIC * Vanishing gradient
# MAGIC * Exploding gradient
# MAGIC * Saturation
# MAGIC * etc.
# MAGIC 
# MAGIC i.e., many of the same problems with early deep feed-forward networks having lots of weights.
# MAGIC 
# MAGIC 10 steps back in time for a single layer is a not as bad as 10 layers (since there are fewer connections and, hence, weights) but it does get expensive.
# MAGIC 
# MAGIC ---
# MAGIC 
# MAGIC > __ASIDE: Hierarchical and Recursive Networks, Bidirectional RNN__
# MAGIC 
# MAGIC > Network topologies can be built to reflect the relative structure of the data we are modeling. E.g., for natural language, grammar constraints mean that both hierarchy and (limited) recursion may allow a physically smaller model to achieve more effective capacity.
# MAGIC 
# MAGIC > A bi-directional RNN includes values from previous and subsequent time steps. This is less strange than it sounds at first: after all, in many problems, such as sentence translation (where BiRNNs are very popular) we usually have the entire source sequence at one time. In that case, a BiDiRNN is really just saying that both prior and subsequent words can influence the interpretation of each word, something we humans take for granted.
# MAGIC 
# MAGIC > Recent versions of neural net libraries have support for bidirectional networks, although you may need to write (or locate) a little code yourself if you want to experiment with hierarchical networks.
# MAGIC 
# MAGIC ---
# MAGIC 
# MAGIC ## Long Short-Term Memory (LSTM)
# MAGIC 
# MAGIC "Pure" RNNs were never very successful. Sepp Hochreiter and Jürgen Schmidhuber (1997) made a game-changing contribution with the publication of the Long Short-Term Memory unit. How game changing? It's effectively state of the art today.
# MAGIC 
# MAGIC <sup>(Credit and much thanks to Chris Olah, http://colah.github.io/about.html, Research Scientist at Google Brain, for publishing the following excellent diagrams!)</sup>
# MAGIC 
# MAGIC *In the following diagrams, pay close attention that the output value is "split" for graphical purposes -- so the two *h* arrows/signals coming out are the same signal.*
# MAGIC 
# MAGIC __RNN Cell:__
# MAGIC <img src="http://i.imgur.com/DfYyKaN.png" width=600>
# MAGIC 
# MAGIC __LSTM Cell:__
# MAGIC 
# MAGIC <img src="http://i.imgur.com/pQiMLjG.png" width=600>
# MAGIC 
# MAGIC 
# MAGIC An LSTM unit is a neuron with some bonus features:
# MAGIC * Cell state propagated across time
# MAGIC * Input, Output, Forget gates
# MAGIC * Learns retention/discard of cell state
# MAGIC * Admixture of new data
# MAGIC * Output partly distinct from state
# MAGIC * Use of __addition__ (not multiplication) to combine input and cell state allows state to propagate unimpeded across time (addition of gradient)
# MAGIC 
# MAGIC ---
# MAGIC 
# MAGIC > __ASIDE: Variations on LSTM__
# MAGIC 
# MAGIC > ... include "peephole" where gate functions have direct access to cell state; convolutional; and bidirectional, where we can "cheat" by letting neurons learn from future time steps and not just previous time steps.
# MAGIC 
# MAGIC ___
# MAGIC 
# MAGIC Slow down ... exactly what's getting added to where? For a step-by-step walk through, read Chris Olah's full post http://colah.github.io/posts/2015-08-Understanding-LSTMs/ 
# MAGIC 
# MAGIC 
# MAGIC ### Do LSTMs Work Reasonably Well?
# MAGIC 
# MAGIC __Yes!__ These architectures are in production (2017) for deep-learning-enabled products at Baidu, Google, Microsoft, Apple, and elsewhere. They are used to solve problems in time series analysis, speech recognition and generation, connected handwriting, grammar, music, and robot control systems.
# MAGIC 
# MAGIC ### Let's Code an LSTM Variant of our Sequence Lab
# MAGIC 
# MAGIC (this great demo example courtesy of Jason Brownlee: http://machinelearningmastery.com/understanding-stateful-lstm-recurrent-neural-networks-python-keras/)

# COMMAND ----------

import numpy
from keras.models import Sequential
from keras.layers import Dense
from keras.layers import LSTM
from keras.utils import np_utils

alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
char_to_int = dict((c, i) for i, c in enumerate(alphabet))
int_to_char = dict((i, c) for i, c in enumerate(alphabet))

seq_length = 3
dataX = []
dataY = []
for i in range(0, len(alphabet) - seq_length, 1):
	seq_in = alphabet[i:i + seq_length]
	seq_out = alphabet[i + seq_length]
	dataX.append([char_to_int[char] for char in seq_in])
	dataY.append(char_to_int[seq_out])
	print (seq_in, '->', seq_out)

# reshape X to be .......[samples, time steps, features]
X = numpy.reshape(dataX, (len(dataX), seq_length, 1))
X = X / float(len(alphabet))
y = np_utils.to_categorical(dataY)

# Let’s define an LSTM network with 32 units and an output layer with a softmax activation function for making predictions. 
# a naive implementation of LSTM
model = Sequential()
model.add(LSTM(32, input_shape=(X.shape[1], X.shape[2]))) # <- LSTM layer...
model.add(Dense(y.shape[1], activation='softmax'))
model.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['accuracy'])
model.fit(X, y, epochs=400, batch_size=1, verbose=2)

scores = model.evaluate(X, y)
print("Model Accuracy: %.2f%%" % (scores[1]*100))

for pattern in dataX:
	x = numpy.reshape(pattern, (1, len(pattern), 1))
	x = x / float(len(alphabet))
	prediction = model.predict(x, verbose=0)
	index = numpy.argmax(prediction)
	result = int_to_char[index]
	seq_in = [int_to_char[value] for value in pattern]
	print (seq_in, "->", result)

# COMMAND ----------

(X.shape[1], X.shape[2]) # the input shape to LSTM layer with 32 neurons is given by dimensions of time-steps and features

# COMMAND ----------

X.shape[0], y.shape[1] # number of examples and number of categorical outputs

# COMMAND ----------

# MAGIC %md 
# MAGIC __Memory and context__
# MAGIC 
# MAGIC If this network is learning the way we would like, it should be robust to noise and also understand the relative context (in this case, where a prior letter occurs in the sequence).
# MAGIC 
# MAGIC I.e., we should be able to give it corrupted sequences, and it should produce reasonably correct predictions.
# MAGIC 
# MAGIC Make the following change to the code to test this out:
# MAGIC 
# MAGIC ## You Try!
# MAGIC * We'll use "W" for our erroneous/corrupted data element
# MAGIC * Add code at the end to predict on the following sequences:
# MAGIC     * 'WBC', 'WKL', 'WTU', 'DWF', 'MWO', 'VWW', 'GHW', 'JKW', 'PQW'
# MAGIC * Notice any pattern? Hard to tell from a small sample, but if you play with it (trying sequences from different places in the alphabet, or different "corruption" letters, you'll notice patterns that give a hint at what the network is learning
# MAGIC 
# MAGIC The solution is in `060_DLByABr_05a-LSTM-Solution` if you are lazy right now or get stuck.
# MAGIC 
# MAGIC __Pretty cool... BUT__
# MAGIC 
# MAGIC This alphabet example does seem a bit like "tennis without the net" since the original goal was to develop networks that could extract patterns from complex, ambiguous content like natural language or music, and we've been playing with a sequence (Roman alphabet) that is 100% deterministic and tiny in size.
# MAGIC 
# MAGIC First, go ahead and start `061_DLByABr_05b-LSTM-Language` since it will take several minutes to produce its first output.
# MAGIC 
# MAGIC This latter script is taken 100% exactly as-is from the Keras library examples folder (https://github.com/fchollet/keras/blob/master/examples/lstm_text_generation.py) and uses precisely the logic we just learned, in order to learn and synthesize English language text from a single-author corpuse. The amazing thing is that the text is learned and generated one letter at a time, just like we did with the alphabet.
# MAGIC 
# MAGIC Compared to our earlier examples...
# MAGIC * there is a minor difference in the way the inputs are encoded, using 1-hot vectors
# MAGIC * and there is a *significant* difference in the way the outputs (predictions) are generated: instead of taking just the most likely output class (character) via argmax as we did before, this time we are treating the output as a distribution and sampling from the distribution.
# MAGIC 
# MAGIC Let's take a look at the code ... but even so, this will probably be something to come back to after fika or a long break, as the training takes about 5 minutes per epoch (late 2013 MBP CPU) and we need around 20 epochs (80 minutes!) to get good output.

# COMMAND ----------

import sys
sys.exit(0) #just to keep from accidentally running this code (that is already in 061_DLByABr_05b-LSTM-Language) HERE

'''Example script to generate text from Nietzsche's writings.

At least 20 epochs are required before the generated text
starts sounding coherent.

It is recommended to run this script on GPU, as recurrent
networks are quite computationally intensive.

If you try this script on new data, make sure your corpus
has at least ~100k characters. ~1M is better.
'''

from keras.models import Sequential
from keras.layers import Dense, Activation
from keras.layers import LSTM
from keras.optimizers import RMSprop
from keras.utils.data_utils import get_file
import numpy as np
import random
import sys

path = "../data/nietzsche.txt"
text = open(path).read().lower()
print('corpus length:', len(text))

chars = sorted(list(set(text)))
print('total chars:', len(chars))
char_indices = dict((c, i) for i, c in enumerate(chars))
indices_char = dict((i, c) for i, c in enumerate(chars))

# cut the text in semi-redundant sequences of maxlen characters
maxlen = 40
step = 3
sentences = []
next_chars = []
for i in range(0, len(text) - maxlen, step):
    sentences.append(text[i: i + maxlen])
    next_chars.append(text[i + maxlen])
print('nb sequences:', len(sentences))

print('Vectorization...')
X = np.zeros((len(sentences), maxlen, len(chars)), dtype=np.bool)
y = np.zeros((len(sentences), len(chars)), dtype=np.bool)
for i, sentence in enumerate(sentences):
    for t, char in enumerate(sentence):
        X[i, t, char_indices[char]] = 1
    y[i, char_indices[next_chars[i]]] = 1


# build the model: a single LSTM
print('Build model...')
model = Sequential()
model.add(LSTM(128, input_shape=(maxlen, len(chars))))
model.add(Dense(len(chars)))
model.add(Activation('softmax'))

optimizer = RMSprop(lr=0.01)
model.compile(loss='categorical_crossentropy', optimizer=optimizer)


def sample(preds, temperature=1.0):
    # helper function to sample an index from a probability array
    preds = np.asarray(preds).astype('float64')
    preds = np.log(preds) / temperature
    exp_preds = np.exp(preds)
    preds = exp_preds / np.sum(exp_preds)
    probas = np.random.multinomial(1, preds, 1)
    return np.argmax(probas)

# train the model, output generated text after each iteration
for iteration in range(1, 60):
    print()
    print('-' * 50)
    print('Iteration', iteration)
    model.fit(X, y, batch_size=128, epochs=1)

    start_index = random.randint(0, len(text) - maxlen - 1)

    for diversity in [0.2, 0.5, 1.0, 1.2]:
        print()
        print('----- diversity:', diversity)

        generated = ''
        sentence = text[start_index: start_index + maxlen]
        generated += sentence
        print('----- Generating with seed: "' + sentence + '"')
        sys.stdout.write(generated)

        for i in range(400):
            x = np.zeros((1, maxlen, len(chars)))
            for t, char in enumerate(sentence):
                x[0, t, char_indices[char]] = 1.

            preds = model.predict(x, verbose=0)[0]
            next_index = sample(preds, diversity)
            next_char = indices_char[next_index]

            generated += next_char
            sentence = sentence[1:] + next_char

            sys.stdout.write(next_char)
            sys.stdout.flush()
        print()

# COMMAND ----------

# MAGIC %md 
# MAGIC ## Gated Recurrent Unit (GRU)
# MAGIC 
# MAGIC In 2014, a new, promising design for RNN units called Gated Recurrent Unit was published (https://arxiv.org/abs/1412.3555)
# MAGIC 
# MAGIC GRUs have performed similarly to LSTMs, but are slightly simpler in design:
# MAGIC 
# MAGIC * GRU has just two gates: "update" and "reset" (instead of the input, output, and forget in LSTM)
# MAGIC * __update__ controls how to modify (weight and keep) cell state
# MAGIC * __reset__ controls how new input is mixed (weighted) with/against memorized state
# MAGIC * there is no output gate, so the cell state is propagated out -- i.e., there is __no "hidden" state__ that is separate from the generated output state
# MAGIC 
# MAGIC <img src="http://i.imgur.com/nnATBmC.png" width=700>
# MAGIC 
# MAGIC Which one should you use for which applications? The jury is still out -- this is an area for experimentation!
# MAGIC 
# MAGIC ### Using GRUs in Keras
# MAGIC 
# MAGIC ... is as simple as using the built-in GRU class (https://keras.io/layers/recurrent/)
# MAGIC 
# MAGIC If you are working with RNNs, spend some time with docs to go deeper, as we have just barely scratched the surface here, and there are many "knobs" to turn that will help things go right (or wrong).

# COMMAND ----------

'''Example script to generate text from Nietzsche's writings.

At least 20 epochs are required before the generated text
starts sounding coherent.

It is recommended to run this script on GPU, as recurrent
networks are quite computationally intensive.

If you try this script on new data, make sure your corpus
has at least ~100k characters. ~1M is better.
'''

from __future__ import print_function
from keras.models import Sequential
from keras.layers import Dense, Activation
from keras.layers import LSTM
from keras.optimizers import RMSprop
from keras.utils.data_utils import get_file
import numpy as np
import random
import sys

path = get_file('nietzsche.txt', origin='https://s3.amazonaws.com/text-datasets/nietzsche.txt')
text = open(path).read().lower()
print('corpus length:', len(text))

chars = sorted(list(set(text)))
print('total chars:', len(chars))
char_indices = dict((c, i) for i, c in enumerate(chars))
indices_char = dict((i, c) for i, c in enumerate(chars))

# cut the text in semi-redundant sequences of maxlen characters
maxlen = 40
step = 3
sentences = []
next_chars = []
for i in range(0, len(text) - maxlen, step):
    sentences.append(text[i: i + maxlen])
    next_chars.append(text[i + maxlen])
print('nb sequences:', len(sentences))

print('Vectorization...')
X = np.zeros((len(sentences), maxlen, len(chars)), dtype=np.bool)
y = np.zeros((len(sentences), len(chars)), dtype=np.bool)
for i, sentence in enumerate(sentences):
    for t, char in enumerate(sentence):
        X[i, t, char_indices[char]] = 1
    y[i, char_indices[next_chars[i]]] = 1

# COMMAND ----------

len(sentences), maxlen, len(chars)

# COMMAND ----------

X

# COMMAND ----------

# MAGIC %md 
# MAGIC ### What Does Our Nietzsche Generator Produce?
# MAGIC 
# MAGIC Here are snapshots from middle and late in a training run.
# MAGIC 
# MAGIC #### Iteration 19
# MAGIC 
# MAGIC ```
# MAGIC Iteration 19
# MAGIC Epoch 1/1
# MAGIC 200287/200287 [==============================] - 262s - loss: 1.3908     
# MAGIC 
# MAGIC ----- diversity: 0.2
# MAGIC ----- Generating with seed: " apart from the value of such assertions"
# MAGIC  apart from the value of such assertions of the present of the supersially and the soul. the spirituality of the same of the soul. the protect and in the states to the supersially and the soul, in the supersially the supersially and the concerning and in the most conscience of the soul. the soul. the concerning and the substances, and the philosophers in the sing"--that is the most supersiall and the philosophers of the supersially of t
# MAGIC 
# MAGIC ----- diversity: 0.5
# MAGIC ----- Generating with seed: " apart from the value of such assertions"
# MAGIC  apart from the value of such assertions are more there is the scientific modern to the head in the concerning in the same old will of the excited of science. many all the possible concerning such laugher according to when the philosophers sense of men of univerself, the most lacked same depresse in the point, which is desires of a "good (who has senses on that one experiencess which use the concerning and in the respect of the same ori
# MAGIC 
# MAGIC ----- diversity: 1.0
# MAGIC ----- Generating with seed: " apart from the value of such assertions"
# MAGIC  apart from the value of such assertions expressions--are interest person from indeed to ordinapoon as or one of
# MAGIC the uphamy, state is rivel stimromannes are lot man of soul"--modile what he woulds hope in a riligiation, is conscience, and you amy, surposit to advanced torturily
# MAGIC and whorlon and perressing for accurcted with a lot us in view, of its own vanity of their natest"--learns, and dis predeceared from and leade, for oted those wi
# MAGIC 
# MAGIC ----- diversity: 1.2
# MAGIC ----- Generating with seed: " apart from the value of such assertions"
# MAGIC  apart from the value of such assertions of
# MAGIC rutould chinates
# MAGIC rested exceteds to more saarkgs testure carevan, accordy owing before fatherly rifiny,
# MAGIC thrurgins of novelts "frous inventive earth as dire!ition he
# MAGIC shate out of itst sacrifice, in this
# MAGIC mectalical
# MAGIC inworle, you
# MAGIC adome enqueres to its ighter. he often. once even with ded threaten"! an eebirelesifist.
# MAGIC 
# MAGIC lran innoting
# MAGIC with we canone acquire at them crarulents who had prote will out t
# MAGIC ```

# COMMAND ----------

# MAGIC %md 
# MAGIC #### Iteration 32
# MAGIC 
# MAGIC ```
# MAGIC Iteration 32
# MAGIC Epoch 1/1
# MAGIC 200287/200287 [==============================] - 255s - loss: 1.3830     
# MAGIC 
# MAGIC ----- diversity: 0.2
# MAGIC ----- Generating with seed: " body, as a part of this external
# MAGIC world,"
# MAGIC  body, as a part of this external
# MAGIC world, and in the great present of the sort of the strangern that is and in the sologies and the experiences and the present of the present and science of the probably a subject of the subject of the morality and morality of the soul the experiences the morality of the experiences of the conscience in the soul and more the experiences the strangere and present the rest the strangere and individual of th
# MAGIC 
# MAGIC ----- diversity: 0.5
# MAGIC ----- Generating with seed: " body, as a part of this external
# MAGIC world,"
# MAGIC  body, as a part of this external
# MAGIC world, and in the morality of which we knows upon the english and insigning things be exception of
# MAGIC consequences of the man and explained its more in the senses for the same ordinary and the sortarians and subjects and simily in a some longing the destiny ordinary. man easily that has been the some subject and say, and and and and does not to power as all the reasonable and distinction of this one betray
# MAGIC 
# MAGIC ----- diversity: 1.0
# MAGIC ----- Generating with seed: " body, as a part of this external
# MAGIC world,"
# MAGIC  body, as a part of this external
# MAGIC world, surrespossifilice view and life fundamental worthing more sirer. holestly
# MAGIC and whan to be
# MAGIC dream. in whom hand that one downgk edplenius will almost eyes brocky that we wills stupid dor
# MAGIC oborbbill to be dimorable
# MAGIC great excet of ifysabless. the good take the historical yet right by guntend, and which fuens the irrelias in literals in finally to the same flild, conditioned when where prom. it has behi
# MAGIC 
# MAGIC ----- diversity: 1.2
# MAGIC ----- Generating with seed: " body, as a part of this external
# MAGIC world,"
# MAGIC  body, as a part of this external
# MAGIC world, easily achosed time mantur makeches on this
# MAGIC vanity, obcame-scompleises. but inquire-calr ever powerfully smorais: too-wantse; when thoue
# MAGIC conducting
# MAGIC unconstularly without least gainstyfyerfulled to wo
# MAGIC has upos
# MAGIC among uaxqunct what is mell "loves and
# MAGIC lamacity what mattery of upon the a. and which oasis seour schol
# MAGIC to power: the passion sparabrated will. in his europers raris! what seems to these her
# MAGIC 
# MAGIC ```

# COMMAND ----------

# MAGIC %md 
# MAGIC ### Take alook at the anomalous behavior that started late in the training on one run ... What might have happened?

# COMMAND ----------

# MAGIC %md 
# MAGIC #### Iteration 38
# MAGIC 
# MAGIC ```
# MAGIC Iteration 38
# MAGIC Epoch 1/1
# MAGIC 200287/200287 [==============================] - 256s - loss: 7.6662     
# MAGIC 
# MAGIC ----- diversity: 0.2
# MAGIC ----- Generating with seed: "erable? for there is no
# MAGIC longer any ought"
# MAGIC erable? for there is no
# MAGIC longer any oughteesen a a  a= at ae i is es4 iei aatee he a a ac  oyte  in ioie  aan a atoe aie ion a atias a ooe o e tin exanat moe ao is aon e a ntiere t i in ate an on a  e as the a ion aisn ost  aed i  i ioiesn les?ane i ee to i o ate   o igice thi io an a xen an ae an teane one ee e alouieis asno oie on i a a ae s as n io a an e a ofe e  oe ehe it aiol  s a aeio st ior ooe an io e  ot io  o i  aa9em aan ev a
# MAGIC 
# MAGIC ----- diversity: 0.5
# MAGIC ----- Generating with seed: "erable? for there is no
# MAGIC longer any ought"
# MAGIC erable? for there is no
# MAGIC longer any oughteese a on eionea] aooooi ate uo e9l hoe atae s in eaae an  on io]e nd ast aais  ta e  od iia ng ac ee er ber  in ==st a se is ao  o e as aeian iesee tee otiane o oeean a ieatqe o  asnone anc 
# MAGIC  oo a t
# MAGIC tee sefiois to an at in ol asnse an o e e oo  ie oae asne at a ait iati oese se a e p ie peen iei ien   o oot inees engied evone t oen oou atipeem a sthen ion assise ti a a s itos io ae an  eees as oi
# MAGIC 
# MAGIC ----- diversity: 1.0
# MAGIC ----- Generating with seed: "erable? for there is no
# MAGIC longer any ought"
# MAGIC erable? for there is no
# MAGIC longer any oughteena te e ore te beosespeehsha ieno atit e ewge ou ino oo oee coatian aon ie ac aalle e a o  die eionae oa att uec a acae ao a  an eess as
# MAGIC  o  i a io  a   oe a  e is as oo in ene xof o  oooreeg ta m eon al iii n p daesaoe n ite o ane tio oe anoo t ane
# MAGIC s i e tioo ise s a asi e ana ooe ote soueeon io on atieaneyc ei it he se it is ao e an ime  ane on eronaa ee itouman io e ato an ale  a mae taoa ien
# MAGIC 
# MAGIC ----- diversity: 1.2
# MAGIC ----- Generating with seed: "erable? for there is no
# MAGIC longer any ought"
# MAGIC erable? for there is no
# MAGIC longer any oughti o aa e2senoees yi i e datssateal toeieie e a o zanato aal arn aseatli oeene aoni le eoeod t aes a isoee tap  e o . is  oi astee an ea titoe e a exeeee thui itoan ain eas a e bu inen ao ofa ie e e7n anae ait ie a ve  er inen  ite
# MAGIC as oe of  heangi eestioe orasb e fie o o o  a  eean o ot odeerean io io oae ooe ne " e  istee esoonae e terasfioees asa ehainoet at e ea ai esoon   ano a p eesas e aitie
# MAGIC ```

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC (raaz)
# MAGIC ## 'Mind the Hype' around AI 
# MAGIC 
# MAGIC Pay attention to biases in various media.
# MAGIC 
# MAGIC - **Guardian**: *'He began to eat Hermione's family': bot tries to write Harry Potter book – and fails in magic ways
# MAGIC After being fed all seven Potter tales, a predictive keyboard has produced a tale that veers from almost genuine to gloriously bonkers*
# MAGIC   * [https://www.theguardian.com/books/booksblog/2017/dec/13/harry-potter-botnik-jk-rowling](https://www.theguardian.com/books/booksblog/2017/dec/13/harry-potter-botnik-jk-rowling)
# MAGIC - **Business Insider**: *There is a new chapter in Harry Potter's story — and it was written by artificial intelligence ... The writing is mostly weird and borderline comical, but the machine managed to partly reproduce original writer J.K. Rowling's writing style.*
# MAGIC   * [http://nordic.businessinsider.com/there-is-a-new-chapter-in-harry-potters-story-and-it-was-written-by-artificial-intelligence-2017-12](http://nordic.businessinsider.com/there-is-a-new-chapter-in-harry-potters-story-and-it-was-written-by-artificial-intelligence-2017-12)
# MAGIC   
# MAGIC When your managers get "psyched" about how AI will solve all the problems and your sales teams are dreaming hard - keep it cool and manage their expectations as a practical data scientist who is humbled by the hard reality of additions, multiplications and conditionals under the hood.
# MAGIC 
# MAGIC ## 'Mind the Ethics'
# MAGIC 
# MAGIC Don't forget to ask how your data science pipelines could adversely affect peoples:
# MAGIC  * A great X-mas gift to yourself: [https://weaponsofmathdestructionbook.com/](https://weaponsofmathdestructionbook.com/)
# MAGIC  * Another one to make you braver and calmer: [https://www.schneier.com/books/data_and_goliath/](https://www.schneier.com/books/data_and_goliath/)
# MAGIC  
# MAGIC  Don't forget that Data Scientists can be put behind bars for "following orders" from your boss to "make magic happen".
# MAGIC  * [https://www.datasciencecentral.com/profiles/blogs/doing-illegal-data-science-without-knowing-it](https://www.datasciencecentral.com/profiles/blogs/doing-illegal-data-science-without-knowing-it)
# MAGIC  * [https://timesofindia.indiatimes.com/india/forecast-of-poll-results-illegal-election-commission/articleshow/57927839.cms](https://timesofindia.indiatimes.com/india/forecast-of-poll-results-illegal-election-commission/articleshow/57927839.cms)
# MAGIC  * [https://spectrum.ieee.org/cars-that-think/at-work/education/vw-scandal-shocking-but-not-surprising-ethicists-say](https://spectrum.ieee.org/cars-that-think/at-work/education/vw-scandal-shocking-but-not-surprising-ethicists-say)
# MAGIC * ...