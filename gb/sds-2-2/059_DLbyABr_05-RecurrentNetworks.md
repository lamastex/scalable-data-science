[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

This is used in a non-profit educational setting with kind permission of [Adam Breindel](https://www.linkedin.com/in/adbreind). This is not licensed by Adam for use in a for-profit setting. Please contact Adam directly at `adbreind@gmail.com` to request or report such use cases or abuses. A few minor modifications and additional mathematical statistical pointers have been added by Raazesh Sainudiin when teaching PhD students in Uppsala University.

Please feel free to refer to basic concepts here:

-   Udacity's course on Deep Learning <https://www.udacity.com/course/deep-learning--ud730> by Google engineers: Arpan Chakraborty and Vincent Vanhoucke and their full video playlist:
    -   <https://www.youtube.com/watch?v=X_B9NADf2wk&index=2&list=PLAwxTw4SYaPn_OWPFT9ulXLuQrImzHfOV>

Entering the 4th Dimension
==========================

Networks for Understanding Time-Oriented Patterns in Data
---------------------------------------------------------

Common time-based problems include \* Sequence modeling: "What comes next?" \* Likely next letter, word, phrase, category, cound, action, value \* Sequence-to-Sequence modeling: "What alternative sequence is a pattern match?" (i.e., similar probability distribution) \* Machine translation, text-to-speech/speech-to-text, connected handwriting (specific scripts)

<img src="http://i.imgur.com/tnxf9gV.jpg">

### Simplified Approaches

-   If we know all of the sequence states and the probabilities of state transition...
    -   ... then we have a simple Markov Chain model.
-   If we *don't* know all of the states or probabilities (yet) but can make constraining assumptions and acquire solid information from observing (sampling) them...
    -   ... we can use a Hidden Markov Model approach.

These approached have only limited capacity because they are effectively stateless and so have some degree of "extreme retrograde amnesia."

### Can we use a neural network to learn the "next" record in a sequence?

First approach, using what we already know, might look like \* Clamp input sequence to a vector of neurons in a feed-forward network \* Learn a model on the class of the next input record

Let's try it! This can work in some situations, although it's more of a setup and starting point for our next development.

We will make up a simple example of the English alphabet sequence wehere we try to predict the next alphabet from a sequence of length 3.

``` python
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
```

>     ('ABC', '->', 'D')
>     ('BCD', '->', 'E')
>     ('CDE', '->', 'F')
>     ('DEF', '->', 'G')
>     ('EFG', '->', 'H')
>     ('FGH', '->', 'I')
>     ('GHI', '->', 'J')
>     ('HIJ', '->', 'K')
>     ('IJK', '->', 'L')
>     ('JKL', '->', 'M')
>     ('KLM', '->', 'N')
>     ('LMN', '->', 'O')
>     ('MNO', '->', 'P')
>     ('NOP', '->', 'Q')
>     ('OPQ', '->', 'R')
>     ('PQR', '->', 'S')
>     ('QRS', '->', 'T')
>     ('RST', '->', 'U')
>     ('STU', '->', 'V')
>     ('TUV', '->', 'W')
>     ('UVW', '->', 'X')
>     ('VWX', '->', 'Y')
>     ('WXY', '->', 'Z')

``` python
# dataX is just a reindexing of the alphabets in consecutive triplets of numbers
dataX
```

>     Out[2]: 
>     [[0, 1, 2],
>      [1, 2, 3],
>      [2, 3, 4],
>      [3, 4, 5],
>      [4, 5, 6],
>      [5, 6, 7],
>      [6, 7, 8],
>      [7, 8, 9],
>      [8, 9, 10],
>      [9, 10, 11],
>      [10, 11, 12],
>      [11, 12, 13],
>      [12, 13, 14],
>      [13, 14, 15],
>      [14, 15, 16],
>      [15, 16, 17],
>      [16, 17, 18],
>      [17, 18, 19],
>      [18, 19, 20],
>      [19, 20, 21],
>      [20, 21, 22],
>      [21, 22, 23],
>      [22, 23, 24]]

``` python
dataY # just a reindexing of the following alphabet after each consecutive triplet of numbers
```

>     Out[3]: 
>     [3,
>      4,
>      5,
>      6,
>      7,
>      8,
>      9,
>      10,
>      11,
>      12,
>      13,
>      14,
>      15,
>      16,
>      17,
>      18,
>      19,
>      20,
>      21,
>      22,
>      23,
>      24,
>      25]

Train a network on that data:

``` python
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
```

>     ('ABC', '->', 'D')
>     ('BCD', '->', 'E')
>     ('CDE', '->', 'F')
>     ('DEF', '->', 'G')
>     ('EFG', '->', 'H')
>     ('FGH', '->', 'I')
>     ('GHI', '->', 'J')
>     ('HIJ', '->', 'K')
>     ('IJK', '->', 'L')
>     ('JKL', '->', 'M')
>     ('KLM', '->', 'N')
>     ('LMN', '->', 'O')
>     ('MNO', '->', 'P')
>     ('NOP', '->', 'Q')
>     ('OPQ', '->', 'R')
>     ('PQR', '->', 'S')
>     ('QRS', '->', 'T')
>     ('RST', '->', 'U')
>     ('STU', '->', 'V')
>     ('TUV', '->', 'W')
>     ('UVW', '->', 'X')
>     ('VWX', '->', 'Y')
>     ('WXY', '->', 'Z')
>     Epoch 1/1000
>     0s - loss: 3.2624 - acc: 0.0000e+00
>     Epoch 2/1000
>     0s - loss: 3.2588 - acc: 0.0870
>     Epoch 3/1000
>     0s - loss: 3.2567 - acc: 0.0435
>     Epoch 4/1000
>     0s - loss: 3.2550 - acc: 0.0435
>     Epoch 5/1000
>     0s - loss: 3.2530 - acc: 0.0435
>     Epoch 6/1000
>     0s - loss: 3.2511 - acc: 0.0435
>     Epoch 7/1000
>     0s - loss: 3.2497 - acc: 0.0435
>     Epoch 8/1000
>     0s - loss: 3.2476 - acc: 0.0435
>     Epoch 9/1000
>     0s - loss: 3.2458 - acc: 0.0435
>     Epoch 10/1000
>     0s - loss: 3.2437 - acc: 0.0435
>     Epoch 11/1000
>     0s - loss: 3.2419 - acc: 0.0435
>     Epoch 12/1000
>     0s - loss: 3.2401 - acc: 0.0435
>     Epoch 13/1000
>     0s - loss: 3.2378 - acc: 0.0435
>     Epoch 14/1000
>     0s - loss: 3.2356 - acc: 0.0435
>     Epoch 15/1000
>     0s - loss: 3.2340 - acc: 0.0435
>     Epoch 16/1000
>     0s - loss: 3.2315 - acc: 0.0435
>     Epoch 17/1000
>     0s - loss: 3.2290 - acc: 0.0435
>     Epoch 18/1000
>     0s - loss: 3.2268 - acc: 0.0435
>     Epoch 19/1000
>     0s - loss: 3.2243 - acc: 0.0435
>     Epoch 20/1000
>     0s - loss: 3.2219 - acc: 0.0435
>     Epoch 21/1000
>     0s - loss: 3.2192 - acc: 0.0435
>     Epoch 22/1000
>     0s - loss: 3.2163 - acc: 0.0435
>     Epoch 23/1000
>     0s - loss: 3.2135 - acc: 0.0435
>     Epoch 24/1000
>     0s - loss: 3.2110 - acc: 0.0435
>     Epoch 25/1000
>     0s - loss: 3.2081 - acc: 0.0435
>     Epoch 26/1000
>     0s - loss: 3.2048 - acc: 0.0435
>     Epoch 27/1000
>     0s - loss: 3.2021 - acc: 0.0435
>     Epoch 28/1000
>     0s - loss: 3.1987 - acc: 0.0435
>     Epoch 29/1000
>     0s - loss: 3.1956 - acc: 0.0435
>     Epoch 30/1000
>     0s - loss: 3.1922 - acc: 0.0435
>     Epoch 31/1000
>     0s - loss: 3.1890 - acc: 0.0435
>     Epoch 32/1000
>     0s - loss: 3.1860 - acc: 0.0435
>     Epoch 33/1000
>     0s - loss: 3.1822 - acc: 0.0435
>     Epoch 34/1000
>     0s - loss: 3.1782 - acc: 0.0435
>     Epoch 35/1000
>     0s - loss: 3.1748 - acc: 0.0435
>     Epoch 36/1000
>     0s - loss: 3.1715 - acc: 0.0435
>     Epoch 37/1000
>     0s - loss: 3.1681 - acc: 0.0435
>     Epoch 38/1000
>     0s - loss: 3.1635 - acc: 0.0435
>     Epoch 39/1000
>     0s - loss: 3.1604 - acc: 0.0435
>     Epoch 40/1000
>     0s - loss: 3.1567 - acc: 0.0435
>     Epoch 41/1000
>     0s - loss: 3.1530 - acc: 0.0435
>     Epoch 42/1000
>     0s - loss: 3.1496 - acc: 0.0435
>     Epoch 43/1000
>     0s - loss: 3.1454 - acc: 0.0435
>     Epoch 44/1000
>     0s - loss: 3.1424 - acc: 0.0435
>     Epoch 45/1000
>     0s - loss: 3.1378 - acc: 0.0435
>     Epoch 46/1000
>     0s - loss: 3.1343 - acc: 0.0435
>     Epoch 47/1000
>     0s - loss: 3.1304 - acc: 0.0435
>     Epoch 48/1000
>     0s - loss: 3.1276 - acc: 0.0435
>     Epoch 49/1000
>     0s - loss: 3.1235 - acc: 0.0435
>     Epoch 50/1000
>     0s - loss: 3.1203 - acc: 0.0435
>     Epoch 51/1000
>     0s - loss: 3.1166 - acc: 0.0435
>     Epoch 52/1000
>     0s - loss: 3.1130 - acc: 0.0435
>     Epoch 53/1000
>     0s - loss: 3.1095 - acc: 0.0435
>     Epoch 54/1000
>     0s - loss: 3.1065 - acc: 0.0435
>     Epoch 55/1000
>     0s - loss: 3.1030 - acc: 0.0435
>     Epoch 56/1000
>     0s - loss: 3.0994 - acc: 0.0435
>     Epoch 57/1000
>     0s - loss: 3.0963 - acc: 0.0435
>     Epoch 58/1000
>     0s - loss: 3.0930 - acc: 0.0435
>     Epoch 59/1000
>     0s - loss: 3.0897 - acc: 0.0435
>     Epoch 60/1000
>     0s - loss: 3.0870 - acc: 0.0435
>     Epoch 61/1000
>     0s - loss: 3.0835 - acc: 0.0435
>     Epoch 62/1000
>     0s - loss: 3.0799 - acc: 0.0435
>     Epoch 63/1000
>     0s - loss: 3.0767 - acc: 0.0435
>     Epoch 64/1000
>     0s - loss: 3.0735 - acc: 0.0435
>     Epoch 65/1000
>     0s - loss: 3.0701 - acc: 0.0435
>     Epoch 66/1000
>     0s - loss: 3.0671 - acc: 0.0435
>     Epoch 67/1000
>     0s - loss: 3.0641 - acc: 0.0435
>     Epoch 68/1000
>     0s - loss: 3.0608 - acc: 0.0435
>     Epoch 69/1000
>     0s - loss: 3.0577 - acc: 0.0435
>     Epoch 70/1000
>     0s - loss: 3.0546 - acc: 0.0435
>     Epoch 71/1000
>     0s - loss: 3.0514 - acc: 0.0435
>     Epoch 72/1000
>     0s - loss: 3.0490 - acc: 0.0435
>     Epoch 73/1000
>     0s - loss: 3.0455 - acc: 0.0435
>     Epoch 74/1000
>     0s - loss: 3.0424 - acc: 0.0435
>     Epoch 75/1000
>     0s - loss: 3.0392 - acc: 0.0435
>     Epoch 76/1000
>     0s - loss: 3.0362 - acc: 0.0435
>     Epoch 77/1000
>     0s - loss: 3.0328 - acc: 0.0435
>     Epoch 78/1000
>     0s - loss: 3.0293 - acc: 0.0435
>     Epoch 79/1000
>     0s - loss: 3.0265 - acc: 0.0435
>     Epoch 80/1000
>     0s - loss: 3.0235 - acc: 0.0435
>     Epoch 81/1000
>     0s - loss: 3.0203 - acc: 0.0435
>     Epoch 82/1000
>     0s - loss: 3.0173 - acc: 0.0435
>     Epoch 83/1000
>     0s - loss: 3.0138 - acc: 0.0435
>     Epoch 84/1000
>     0s - loss: 3.0109 - acc: 0.0435
>     Epoch 85/1000
>     0s - loss: 3.0076 - acc: 0.0435
>     Epoch 86/1000
>     0s - loss: 3.0045 - acc: 0.0435
>     Epoch 87/1000
>     0s - loss: 3.0013 - acc: 0.0870
>     Epoch 88/1000
>     0s - loss: 2.9983 - acc: 0.0870
>     Epoch 89/1000
>     0s - loss: 2.9951 - acc: 0.0870
>     Epoch 90/1000
>     0s - loss: 2.9923 - acc: 0.0870
>     Epoch 91/1000
>     0s - loss: 2.9892 - acc: 0.0870
>     Epoch 92/1000
>     0s - loss: 2.9865 - acc: 0.0870
>     Epoch 93/1000
>     0s - loss: 2.9830 - acc: 0.0870
>     Epoch 94/1000
>     0s - loss: 2.9798 - acc: 0.0870
>     Epoch 95/1000
>     0s - loss: 2.9767 - acc: 0.0870
>     Epoch 96/1000
>     0s - loss: 2.9740 - acc: 0.0870
>     Epoch 97/1000
>     0s - loss: 2.9709 - acc: 0.0870
>     Epoch 98/1000
>     0s - loss: 2.9675 - acc: 0.0870
>     Epoch 99/1000
>     0s - loss: 2.9647 - acc: 0.0870
>     Epoch 100/1000
>     0s - loss: 2.9615 - acc: 0.0870
>     Epoch 101/1000
>     0s - loss: 2.9588 - acc: 0.0870
>     Epoch 102/1000
>     0s - loss: 2.9559 - acc: 0.0870
>     Epoch 103/1000
>     0s - loss: 2.9527 - acc: 0.0870
>     Epoch 104/1000
>     0s - loss: 2.9497 - acc: 0.0870
>     Epoch 105/1000
>     0s - loss: 2.9463 - acc: 0.0870
>     Epoch 106/1000
>     0s - loss: 2.9433 - acc: 0.0870
>     Epoch 107/1000
>     0s - loss: 2.9399 - acc: 0.0870
>     Epoch 108/1000
>     0s - loss: 2.9374 - acc: 0.0870
>     Epoch 109/1000
>     0s - loss: 2.9344 - acc: 0.0870
>     Epoch 110/1000
>     0s - loss: 2.9311 - acc: 0.0870
>     Epoch 111/1000
>     0s - loss: 2.9283 - acc: 0.0870
>     Epoch 112/1000
>     0s - loss: 2.9253 - acc: 0.0870
>     Epoch 113/1000
>     0s - loss: 2.9225 - acc: 0.0870
>     Epoch 114/1000
>     0s - loss: 2.9196 - acc: 0.0870
>     Epoch 115/1000
>     0s - loss: 2.9162 - acc: 0.1304
>     Epoch 116/1000
>     0s - loss: 2.9135 - acc: 0.1304
>     Epoch 117/1000
>     0s - loss: 2.9105 - acc: 0.1739
>     Epoch 118/1000
>     0s - loss: 2.9076 - acc: 0.1304
>     Epoch 119/1000
>     0s - loss: 2.9044 - acc: 0.1304
>     Epoch 120/1000
>     0s - loss: 2.9014 - acc: 0.1304
>     Epoch 121/1000
>     0s - loss: 2.8985 - acc: 0.1304
>     Epoch 122/1000
>     0s - loss: 2.8957 - acc: 0.1304
>     Epoch 123/1000
>     0s - loss: 2.8923 - acc: 0.1304
>     Epoch 124/1000
>     0s - loss: 2.8895 - acc: 0.0870
>     Epoch 125/1000
>     0s - loss: 2.8866 - acc: 0.0870
>     Epoch 126/1000
>     0s - loss: 2.8835 - acc: 0.0870
>     Epoch 127/1000
>     0s - loss: 2.8804 - acc: 0.0870
>     Epoch 128/1000
>     0s - loss: 2.8775 - acc: 0.0870
>     Epoch 129/1000
>     0s - loss: 2.8745 - acc: 0.0870
>     Epoch 130/1000
>     0s - loss: 2.8713 - acc: 0.0870
>     Epoch 131/1000
>     0s - loss: 2.8685 - acc: 0.0870
>     Epoch 132/1000
>     0s - loss: 2.8654 - acc: 0.0870
>     Epoch 133/1000
>     0s - loss: 2.8624 - acc: 0.0870
>     Epoch 134/1000
>     0s - loss: 2.8596 - acc: 0.0870
>     Epoch 135/1000
>     0s - loss: 2.8565 - acc: 0.0870
>     Epoch 136/1000
>     0s - loss: 2.8538 - acc: 0.0870
>     Epoch 137/1000
>     0s - loss: 2.8507 - acc: 0.0870
>     Epoch 138/1000
>     0s - loss: 2.8473 - acc: 0.0870
>     Epoch 139/1000
>     0s - loss: 2.8446 - acc: 0.0870
>     Epoch 140/1000
>     0s - loss: 2.8417 - acc: 0.0870
>     Epoch 141/1000
>     0s - loss: 2.8385 - acc: 0.0870
>     Epoch 142/1000
>     0s - loss: 2.8353 - acc: 0.0870
>     Epoch 143/1000
>     0s - loss: 2.8326 - acc: 0.0870
>     Epoch 144/1000
>     0s - loss: 2.8291 - acc: 0.0870
>     Epoch 145/1000
>     0s - loss: 2.8263 - acc: 0.0870
>     Epoch 146/1000
>     0s - loss: 2.8229 - acc: 0.0870
>     Epoch 147/1000
>     0s - loss: 2.8201 - acc: 0.0870
>     Epoch 148/1000
>     0s - loss: 2.8179 - acc: 0.0870
>     Epoch 149/1000
>     0s - loss: 2.8142 - acc: 0.0870
>     Epoch 150/1000
>     0s - loss: 2.8108 - acc: 0.0870
>     Epoch 151/1000
>     0s - loss: 2.8082 - acc: 0.0870
>     Epoch 152/1000
>     0s - loss: 2.8055 - acc: 0.0870
>     Epoch 153/1000
>     0s - loss: 2.8024 - acc: 0.0870
>     Epoch 154/1000
>     0s - loss: 2.7995 - acc: 0.0870
>     Epoch 155/1000
>     0s - loss: 2.7967 - acc: 0.0870
>     Epoch 156/1000
>     0s - loss: 2.7938 - acc: 0.0870
>     Epoch 157/1000
>     0s - loss: 2.7910 - acc: 0.0870
>     Epoch 158/1000
>     0s - loss: 2.7879 - acc: 0.0870
>     Epoch 159/1000
>     0s - loss: 2.7853 - acc: 0.0870
>     Epoch 160/1000
>     0s - loss: 2.7828 - acc: 0.0870
>     Epoch 161/1000
>     0s - loss: 2.7799 - acc: 0.0870
>     Epoch 162/1000
>     0s - loss: 2.7766 - acc: 0.0870
>     Epoch 163/1000
>     0s - loss: 2.7738 - acc: 0.0870
>     Epoch 164/1000
>     0s - loss: 2.7714 - acc: 0.1304
>     Epoch 165/1000
>     0s - loss: 2.7685 - acc: 0.1739
>     Epoch 166/1000
>     0s - loss: 2.7660 - acc: 0.1304
>     Epoch 167/1000
>     0s - loss: 2.7627 - acc: 0.1304
>     Epoch 168/1000
>     0s - loss: 2.7603 - acc: 0.1304
>     Epoch 169/1000
>     0s - loss: 2.7572 - acc: 0.1304
>     Epoch 170/1000
>     0s - loss: 2.7547 - acc: 0.1304
>     Epoch 171/1000
>     0s - loss: 2.7518 - acc: 0.1304
>     Epoch 172/1000
>     0s - loss: 2.7489 - acc: 0.1304
>     Epoch 173/1000
>     0s - loss: 2.7467 - acc: 0.1304
>     Epoch 174/1000
>     0s - loss: 2.7436 - acc: 0.1304
>     Epoch 175/1000
>     0s - loss: 2.7410 - acc: 0.1304
>     Epoch 176/1000
>     0s - loss: 2.7383 - acc: 0.0870
>     Epoch 177/1000
>     0s - loss: 2.7362 - acc: 0.0870
>     Epoch 178/1000
>     0s - loss: 2.7329 - acc: 0.0870
>     Epoch 179/1000
>     0s - loss: 2.7304 - acc: 0.0870
>     Epoch 180/1000
>     0s - loss: 2.7282 - acc: 0.0870
>     Epoch 181/1000
>     0s - loss: 2.7259 - acc: 0.1304
>     Epoch 182/1000
>     0s - loss: 2.7230 - acc: 0.1304
>     Epoch 183/1000
>     0s - loss: 2.7206 - acc: 0.0870
>     Epoch 184/1000
>     0s - loss: 2.7175 - acc: 0.0870
>     Epoch 185/1000
>     0s - loss: 2.7152 - acc: 0.0870
>     Epoch 186/1000
>     0s - loss: 2.7124 - acc: 0.1304
>     Epoch 187/1000
>     0s - loss: 2.7097 - acc: 0.1304
>     Epoch 188/1000
>     0s - loss: 2.7073 - acc: 0.1304
>     Epoch 189/1000
>     0s - loss: 2.7052 - acc: 0.1739
>     Epoch 190/1000
>     0s - loss: 2.7024 - acc: 0.1304
>     Epoch 191/1000
>     0s - loss: 2.6999 - acc: 0.1739
>     Epoch 192/1000
>     0s - loss: 2.6977 - acc: 0.1304
>     Epoch 193/1000
>     0s - loss: 2.6950 - acc: 0.1304
>     Epoch 194/1000
>     0s - loss: 2.6928 - acc: 0.1304
>     Epoch 195/1000
>     0s - loss: 2.6896 - acc: 0.1304
>     Epoch 196/1000
>     0s - loss: 2.6871 - acc: 0.1304
>     Epoch 197/1000
>     0s - loss: 2.6847 - acc: 0.1304
>     Epoch 198/1000
>     0s - loss: 2.6822 - acc: 0.1304
>     Epoch 199/1000
>     0s - loss: 2.6796 - acc: 0.1304
>     Epoch 200/1000
>     0s - loss: 2.6775 - acc: 0.1304
>     Epoch 201/1000
>     0s - loss: 2.6752 - acc: 0.1304
>     Epoch 202/1000
>     0s - loss: 2.6724 - acc: 0.1304
>     Epoch 203/1000
>     0s - loss: 2.6698 - acc: 0.1304
>     Epoch 204/1000
>     0s - loss: 2.6674 - acc: 0.1304
>     Epoch 205/1000
>     0s - loss: 2.6647 - acc: 0.1304
>     Epoch 206/1000
>     0s - loss: 2.6627 - acc: 0.1304
>     Epoch 207/1000
>     0s - loss: 2.6602 - acc: 0.1304
>     Epoch 208/1000
>     0s - loss: 2.6579 - acc: 0.1304
>     Epoch 209/1000
>     0s - loss: 2.6554 - acc: 0.1304
>     Epoch 210/1000
>     0s - loss: 2.6532 - acc: 0.1304
>     Epoch 211/1000
>     0s - loss: 2.6505 - acc: 0.1304
>     Epoch 212/1000
>     0s - loss: 2.6480 - acc: 0.1304
>     Epoch 213/1000
>     0s - loss: 2.6456 - acc: 0.1304
>     Epoch 214/1000
>     0s - loss: 2.6432 - acc: 0.1304
>     Epoch 215/1000
>     0s - loss: 2.6408 - acc: 0.1304
>     Epoch 216/1000
>     0s - loss: 2.6386 - acc: 0.1739
>     Epoch 217/1000
>     0s - loss: 2.6359 - acc: 0.1739
>     Epoch 218/1000
>     0s - loss: 2.6337 - acc: 0.1739
>     Epoch 219/1000
>     0s - loss: 2.6314 - acc: 0.1739
>     Epoch 220/1000
>     0s - loss: 2.6293 - acc: 0.1739
>     Epoch 221/1000
>     0s - loss: 2.6269 - acc: 0.1739
>     Epoch 222/1000
>     0s - loss: 2.6246 - acc: 0.1739
>     Epoch 223/1000
>     0s - loss: 2.6223 - acc: 0.1739
>     Epoch 224/1000
>     0s - loss: 2.6198 - acc: 0.1304
>     Epoch 225/1000
>     0s - loss: 2.6177 - acc: 0.1739
>     Epoch 226/1000
>     0s - loss: 2.6155 - acc: 0.1739
>     Epoch 227/1000
>     0s - loss: 2.6136 - acc: 0.1739
>     Epoch 228/1000
>     0s - loss: 2.6108 - acc: 0.1739
>     Epoch 229/1000
>     0s - loss: 2.6092 - acc: 0.1739
>     Epoch 230/1000
>     0s - loss: 2.6070 - acc: 0.1739
>     Epoch 231/1000
>     0s - loss: 2.6045 - acc: 0.1739
>     Epoch 232/1000
>     0s - loss: 2.6021 - acc: 0.1739
>     Epoch 233/1000
>     0s - loss: 2.6003 - acc: 0.1739
>     Epoch 234/1000
>     0s - loss: 2.5978 - acc: 0.1739
>     Epoch 235/1000
>     0s - loss: 2.5960 - acc: 0.1739
>     Epoch 236/1000
>     0s - loss: 2.5937 - acc: 0.1739
>     Epoch 237/1000
>     0s - loss: 2.5920 - acc: 0.1739
>     Epoch 238/1000
>     0s - loss: 2.5894 - acc: 0.1739
>     Epoch 239/1000
>     0s - loss: 2.5871 - acc: 0.1739
>     Epoch 240/1000
>     0s - loss: 2.5854 - acc: 0.1739
>     Epoch 241/1000
>     0s - loss: 2.5832 - acc: 0.1739
>     Epoch 242/1000
>     0s - loss: 2.5810 - acc: 0.1739
>     Epoch 243/1000
>     0s - loss: 2.5787 - acc: 0.1739
>     Epoch 244/1000
>     0s - loss: 2.5770 - acc: 0.1739
>     Epoch 245/1000
>     0s - loss: 2.5747 - acc: 0.1739
>     Epoch 246/1000
>     0s - loss: 2.5721 - acc: 0.1739
>     Epoch 247/1000
>     0s - loss: 2.5708 - acc: 0.1739
>     Epoch 248/1000
>     0s - loss: 2.5681 - acc: 0.1739
>     Epoch 249/1000
>     0s - loss: 2.5668 - acc: 0.1739
>     Epoch 250/1000
>     0s - loss: 2.5642 - acc: 0.1739
>     Epoch 251/1000
>     0s - loss: 2.5622 - acc: 0.1739
>     Epoch 252/1000
>     0s - loss: 2.5600 - acc: 0.1739
>     Epoch 253/1000
>     0s - loss: 2.5581 - acc: 0.1739
>     Epoch 254/1000
>     0s - loss: 2.5564 - acc: 0.2174
>     Epoch 255/1000
>     0s - loss: 2.5544 - acc: 0.2174
>     Epoch 256/1000
>     0s - loss: 2.5523 - acc: 0.2174
>     Epoch 257/1000
>     0s - loss: 2.5508 - acc: 0.2174
>     Epoch 258/1000
>     0s - loss: 2.5485 - acc: 0.2174
>     Epoch 259/1000
>     0s - loss: 2.5466 - acc: 0.2174
>     Epoch 260/1000
>     0s - loss: 2.5444 - acc: 0.2174
>     Epoch 261/1000
>     0s - loss: 2.5428 - acc: 0.2174
>     Epoch 262/1000
>     0s - loss: 2.5406 - acc: 0.2174
>     Epoch 263/1000
>     0s - loss: 2.5387 - acc: 0.2174
>     Epoch 264/1000
>     0s - loss: 2.5365 - acc: 0.2174
>     Epoch 265/1000
>     0s - loss: 2.5347 - acc: 0.2174
>     Epoch 266/1000
>     0s - loss: 2.5330 - acc: 0.2174
>     Epoch 267/1000
>     0s - loss: 2.5310 - acc: 0.2174
>     Epoch 268/1000
>     0s - loss: 2.5297 - acc: 0.2174
>     Epoch 269/1000
>     0s - loss: 2.5271 - acc: 0.2174
>     Epoch 270/1000
>     0s - loss: 2.5254 - acc: 0.2174
>     Epoch 271/1000
>     0s - loss: 2.5237 - acc: 0.2174
>     Epoch 272/1000
>     0s - loss: 2.5223 - acc: 0.2174
>     Epoch 273/1000
>     0s - loss: 2.5204 - acc: 0.2174
>     Epoch 274/1000
>     0s - loss: 2.5179 - acc: 0.2174
>     Epoch 275/1000
>     0s - loss: 2.5167 - acc: 0.2174
>     Epoch 276/1000
>     0s - loss: 2.5141 - acc: 0.2174
>     Epoch 277/1000
>     0s - loss: 2.5126 - acc: 0.2174
>     Epoch 278/1000
>     0s - loss: 2.5111 - acc: 0.2174
>     Epoch 279/1000
>     0s - loss: 2.5091 - acc: 0.2174
>     Epoch 280/1000
>     0s - loss: 2.5070 - acc: 0.2174
>     Epoch 281/1000
>     0s - loss: 2.5056 - acc: 0.2174
>     Epoch 282/1000
>     0s - loss: 2.5036 - acc: 0.2174
>     Epoch 283/1000
>     0s - loss: 2.5014 - acc: 0.2174
>     Epoch 284/1000
>     0s - loss: 2.4996 - acc: 0.2174
>     Epoch 285/1000
>     0s - loss: 2.4982 - acc: 0.2174
>     Epoch 286/1000
>     0s - loss: 2.4966 - acc: 0.2174
>     Epoch 287/1000
>     0s - loss: 2.4946 - acc: 0.2174
>     Epoch 288/1000
>     0s - loss: 2.4923 - acc: 0.1739
>     Epoch 289/1000
>     0s - loss: 2.4907 - acc: 0.1739
>     Epoch 290/1000
>     0s - loss: 2.4894 - acc: 0.1739
>     Epoch 291/1000
>     0s - loss: 2.4879 - acc: 0.1739
>     Epoch 292/1000
>     0s - loss: 2.4856 - acc: 0.1739
>     Epoch 293/1000
>     0s - loss: 2.4839 - acc: 0.1739
>     Epoch 294/1000
>     0s - loss: 2.4820 - acc: 0.1739
>     Epoch 295/1000
>     0s - loss: 2.4807 - acc: 0.1739
>     Epoch 296/1000
>     0s - loss: 2.4788 - acc: 0.1739
>     Epoch 297/1000
>     0s - loss: 2.4772 - acc: 0.1739
>     Epoch 298/1000
>     0s - loss: 2.4754 - acc: 0.1739
>     Epoch 299/1000
>     0s - loss: 2.4737 - acc: 0.1739
>     Epoch 300/1000
>     0s - loss: 2.4719 - acc: 0.1739
>     Epoch 301/1000
>     0s - loss: 2.4706 - acc: 0.1739
>     Epoch 302/1000
>     0s - loss: 2.4682 - acc: 0.1739
>     Epoch 303/1000
>     0s - loss: 2.4666 - acc: 0.1739
>     Epoch 304/1000
>     0s - loss: 2.4652 - acc: 0.1739
>     Epoch 305/1000
>     0s - loss: 2.4635 - acc: 0.1739
>     Epoch 306/1000
>     0s - loss: 2.4616 - acc: 0.2174
>     Epoch 307/1000
>     0s - loss: 2.4603 - acc: 0.1739
>     Epoch 308/1000
>     0s - loss: 2.4585 - acc: 0.2174
>     Epoch 309/1000
>     0s - loss: 2.4568 - acc: 0.1739
>     Epoch 310/1000
>     0s - loss: 2.4551 - acc: 0.1739
>     Epoch 311/1000
>     0s - loss: 2.4528 - acc: 0.1739
>     Epoch 312/1000
>     0s - loss: 2.4524 - acc: 0.2174
>     Epoch 313/1000
>     0s - loss: 2.4498 - acc: 0.1739
>     Epoch 314/1000
>     0s - loss: 2.4485 - acc: 0.1739
>     Epoch 315/1000
>     0s - loss: 2.4466 - acc: 0.2174
>     Epoch 316/1000
>     0s - loss: 2.4454 - acc: 0.1739
>     Epoch 317/1000
>     0s - loss: 2.4444 - acc: 0.1739
>     Epoch 318/1000
>     0s - loss: 2.4424 - acc: 0.1739
>     Epoch 319/1000
>     0s - loss: 2.4408 - acc: 0.1739
>     Epoch 320/1000
>     0s - loss: 2.4390 - acc: 0.1739
>     Epoch 321/1000
>     0s - loss: 2.4376 - acc: 0.1739
>     Epoch 322/1000
>     0s - loss: 2.4364 - acc: 0.1739
>     Epoch 323/1000
>     0s - loss: 2.4343 - acc: 0.1739
>     Epoch 324/1000
>     0s - loss: 2.4327 - acc: 0.1739
>     Epoch 325/1000
>     0s - loss: 2.4312 - acc: 0.1739
>     Epoch 326/1000
>     0s - loss: 2.4298 - acc: 0.1739
>     Epoch 327/1000
>     0s - loss: 2.4283 - acc: 0.1739
>     Epoch 328/1000
>     0s - loss: 2.4266 - acc: 0.1739
>     Epoch 329/1000
>     0s - loss: 2.4253 - acc: 0.1739
>     Epoch 330/1000
>     0s - loss: 2.4232 - acc: 0.1739
>     Epoch 331/1000
>     0s - loss: 2.4220 - acc: 0.1739
>     Epoch 332/1000
>     0s - loss: 2.4200 - acc: 0.2174
>     Epoch 333/1000
>     0s - loss: 2.4190 - acc: 0.2174
>     Epoch 334/1000
>     0s - loss: 2.4172 - acc: 0.2174
>     Epoch 335/1000
>     0s - loss: 2.4154 - acc: 0.1739
>     Epoch 336/1000
>     0s - loss: 2.4139 - acc: 0.1739
>     Epoch 337/1000
>     0s - loss: 2.4118 - acc: 0.1739
>     Epoch 338/1000
>     0s - loss: 2.4110 - acc: 0.1739
>     Epoch 339/1000
>     0s - loss: 2.4099 - acc: 0.2174
>     Epoch 340/1000
>     0s - loss: 2.4083 - acc: 0.2174
>     Epoch 341/1000
>     0s - loss: 2.4074 - acc: 0.2174
>     Epoch 342/1000
>     0s - loss: 2.4052 - acc: 0.2174
>     Epoch 343/1000
>     0s - loss: 2.4037 - acc: 0.1739
>     Epoch 344/1000
>     0s - loss: 2.4022 - acc: 0.1739
>     Epoch 345/1000
>     0s - loss: 2.4004 - acc: 0.1739
>     Epoch 346/1000
>     0s - loss: 2.3993 - acc: 0.1739
>     Epoch 347/1000
>     0s - loss: 2.3976 - acc: 0.1739
>     Epoch 348/1000
>     0s - loss: 2.3963 - acc: 0.1739
>     Epoch 349/1000
>     0s - loss: 2.3946 - acc: 0.2174
>     Epoch 350/1000
>     0s - loss: 2.3935 - acc: 0.2174
>     Epoch 351/1000
>     0s - loss: 2.3914 - acc: 0.2174
>     Epoch 352/1000
>     0s - loss: 2.3906 - acc: 0.2174
>     Epoch 353/1000
>     0s - loss: 2.3887 - acc: 0.2174
>     Epoch 354/1000
>     0s - loss: 2.3873 - acc: 0.2174
>     Epoch 355/1000
>     0s - loss: 2.3860 - acc: 0.2174
>     Epoch 356/1000
>     0s - loss: 2.3842 - acc: 0.2174
>     Epoch 357/1000
>     0s - loss: 2.3826 - acc: 0.2609
>     Epoch 358/1000
>     0s - loss: 2.3818 - acc: 0.2609
>     Epoch 359/1000
>     0s - loss: 2.3798 - acc: 0.2609
>     Epoch 360/1000
>     0s - loss: 2.3784 - acc: 0.2609
>     Epoch 361/1000
>     0s - loss: 2.3770 - acc: 0.2609
>     Epoch 362/1000
>     0s - loss: 2.3759 - acc: 0.2609
>     Epoch 363/1000
>     0s - loss: 2.3744 - acc: 0.2174
>     Epoch 364/1000
>     0s - loss: 2.3731 - acc: 0.2174
>     Epoch 365/1000
>     0s - loss: 2.3716 - acc: 0.2609
>     Epoch 366/1000
>     0s - loss: 2.3702 - acc: 0.2609
>     Epoch 367/1000
>     0s - loss: 2.3686 - acc: 0.2609
>     Epoch 368/1000
>     0s - loss: 2.3670 - acc: 0.2609
>     Epoch 369/1000
>     0s - loss: 2.3654 - acc: 0.2174
>     Epoch 370/1000
>     0s - loss: 2.3644 - acc: 0.2174
>     Epoch 371/1000
>     0s - loss: 2.3634 - acc: 0.2174
>     Epoch 372/1000
>     0s - loss: 2.3614 - acc: 0.2174
>     Epoch 373/1000
>     0s - loss: 2.3602 - acc: 0.2174
>     Epoch 374/1000
>     0s - loss: 2.3582 - acc: 0.2174
>     Epoch 375/1000
>     0s - loss: 2.3569 - acc: 0.2174
>     Epoch 376/1000
>     0s - loss: 2.3562 - acc: 0.2174
>     Epoch 377/1000
>     0s - loss: 2.3547 - acc: 0.2609
>     Epoch 378/1000
>     0s - loss: 2.3533 - acc: 0.2609
>     Epoch 379/1000
>     0s - loss: 2.3515 - acc: 0.2609
>     Epoch 380/1000
>     0s - loss: 2.3502 - acc: 0.2609
>     Epoch 381/1000
>     0s - loss: 2.3493 - acc: 0.2609
>     Epoch 382/1000
>     0s - loss: 2.3479 - acc: 0.3043
>     Epoch 383/1000
>     0s - loss: 2.3465 - acc: 0.3043
>     Epoch 384/1000
>     0s - loss: 2.3452 - acc: 0.3043
>     Epoch 385/1000
>     0s - loss: 2.3437 - acc: 0.3043
>     Epoch 386/1000
>     0s - loss: 2.3424 - acc: 0.3043
>     Epoch 387/1000
>     0s - loss: 2.3405 - acc: 0.3043
>     Epoch 388/1000
>     0s - loss: 2.3390 - acc: 0.3043
>     Epoch 389/1000
>     0s - loss: 2.3383 - acc: 0.3043
>     Epoch 390/1000
>     0s - loss: 2.3369 - acc: 0.3043
>     Epoch 391/1000
>     0s - loss: 2.3357 - acc: 0.3043
>     Epoch 392/1000
>     0s - loss: 2.3344 - acc: 0.2609
>     Epoch 393/1000
>     0s - loss: 2.3326 - acc: 0.2609
>     Epoch 394/1000
>     0s - loss: 2.3316 - acc: 0.2609
>     Epoch 395/1000
>     0s - loss: 2.3301 - acc: 0.2609
>     Epoch 396/1000
>     0s - loss: 2.3288 - acc: 0.2609
>     Epoch 397/1000
>     0s - loss: 2.3276 - acc: 0.3043
>     Epoch 398/1000
>     0s - loss: 2.3263 - acc: 0.3043
>     Epoch 399/1000
>     0s - loss: 2.3249 - acc: 0.3043
>     Epoch 400/1000
>     0s - loss: 2.3234 - acc: 0.3043
>     Epoch 401/1000
>     0s - loss: 2.3221 - acc: 0.3043
>     Epoch 402/1000
>     0s - loss: 2.3213 - acc: 0.3043
>     Epoch 403/1000
>     0s - loss: 2.3196 - acc: 0.3043
>     Epoch 404/1000
>     0s - loss: 2.3182 - acc: 0.3043
>     Epoch 405/1000
>     0s - loss: 2.3174 - acc: 0.3043
>     Epoch 406/1000
>     0s - loss: 2.3158 - acc: 0.2609
>     Epoch 407/1000
>     0s - loss: 2.3151 - acc: 0.2609
>     Epoch 408/1000
>     0s - loss: 2.3132 - acc: 0.2609
>     Epoch 409/1000
>     0s - loss: 2.3123 - acc: 0.2609
>     Epoch 410/1000
>     0s - loss: 2.3108 - acc: 0.3043
>     Epoch 411/1000
>     0s - loss: 2.3092 - acc: 0.3043
>     Epoch 412/1000
>     0s - loss: 2.3085 - acc: 0.3043
>     Epoch 413/1000
>     0s - loss: 2.3072 - acc: 0.3043
>     Epoch 414/1000
>     0s - loss: 2.3061 - acc: 0.3043
>     Epoch 415/1000
>     0s - loss: 2.3045 - acc: 0.2609
>     Epoch 416/1000
>     0s - loss: 2.3028 - acc: 0.2609
>     Epoch 417/1000
>     0s - loss: 2.3017 - acc: 0.2609
>     Epoch 418/1000
>     0s - loss: 2.3003 - acc: 0.2609
>     Epoch 419/1000
>     0s - loss: 2.2998 - acc: 0.2174
>     Epoch 420/1000
>     0s - loss: 2.2984 - acc: 0.2174
>     Epoch 421/1000
>     0s - loss: 2.2968 - acc: 0.2609
>     Epoch 422/1000
>     0s - loss: 2.2960 - acc: 0.2609
>     Epoch 423/1000
>     0s - loss: 2.2946 - acc: 0.2174
>     Epoch 424/1000
>     0s - loss: 2.2931 - acc: 0.2174
>     Epoch 425/1000
>     0s - loss: 2.2921 - acc: 0.2174
>     Epoch 426/1000
>     0s - loss: 2.2905 - acc: 0.2609
>     Epoch 427/1000
>     0s - loss: 2.2896 - acc: 0.3043
>     Epoch 428/1000
>     0s - loss: 2.2880 - acc: 0.3043
>     Epoch 429/1000
>     0s - loss: 2.2873 - acc: 0.3043
>     Epoch 430/1000
>     0s - loss: 2.2859 - acc: 0.3043
>     Epoch 431/1000
>     0s - loss: 2.2847 - acc: 0.3043
>     Epoch 432/1000
>     0s - loss: 2.2835 - acc: 0.3043
>     Epoch 433/1000
>     0s - loss: 2.2822 - acc: 0.3043
>     Epoch 434/1000
>     0s - loss: 2.2810 - acc: 0.3043
>     Epoch 435/1000
>     0s - loss: 2.2802 - acc: 0.3043
>     Epoch 436/1000
>     0s - loss: 2.2786 - acc: 0.3043
>     Epoch 437/1000
>     0s - loss: 2.2775 - acc: 0.3043
>     Epoch 438/1000
>     0s - loss: 2.2765 - acc: 0.3043
>     Epoch 439/1000
>     0s - loss: 2.2753 - acc: 0.3043
>     Epoch 440/1000
>     0s - loss: 2.2744 - acc: 0.3043
>     Epoch 441/1000
>     0s - loss: 2.2725 - acc: 0.3043
>     Epoch 442/1000
>     0s - loss: 2.2711 - acc: 0.3043
>     Epoch 443/1000
>     0s - loss: 2.2703 - acc: 0.3043
>     Epoch 444/1000
>     0s - loss: 2.2684 - acc: 0.3043
>     Epoch 445/1000
>     0s - loss: 2.2682 - acc: 0.3043
>     Epoch 446/1000
>     0s - loss: 2.2669 - acc: 0.3043
>     Epoch 447/1000
>     0s - loss: 2.2654 - acc: 0.3043
>     Epoch 448/1000
>     0s - loss: 2.2640 - acc: 0.3043
>     Epoch 449/1000
>     0s - loss: 2.2633 - acc: 0.3043
>     Epoch 450/1000
>     0s - loss: 2.2618 - acc: 0.3043
>     Epoch 451/1000
>     0s - loss: 2.2609 - acc: 0.3043
>     Epoch 452/1000
>     0s - loss: 2.2600 - acc: 0.3043
>     Epoch 453/1000
>     0s - loss: 2.2585 - acc: 0.3043
>     Epoch 454/1000
>     0s - loss: 2.2567 - acc: 0.3043
>     Epoch 455/1000
>     0s - loss: 2.2559 - acc: 0.3043
>     Epoch 456/1000
>     0s - loss: 2.2548 - acc: 0.3043
>     Epoch 457/1000
>     0s - loss: 2.2535 - acc: 0.3043
>     Epoch 458/1000
>     0s - loss: 2.2531 - acc: 0.3478
>     Epoch 459/1000
>     0s - loss: 2.2513 - acc: 0.3043
>     Epoch 460/1000
>     0s - loss: 2.2503 - acc: 0.3043
>     Epoch 461/1000
>     0s - loss: 2.2488 - acc: 0.3043
>     Epoch 462/1000
>     0s - loss: 2.2475 - acc: 0.3478
>     Epoch 463/1000
>     0s - loss: 2.2470 - acc: 0.3478
>     Epoch 464/1000
>     0s - loss: 2.2461 - acc: 0.3478
>     Epoch 465/1000
>     0s - loss: 2.2447 - acc: 0.3478
>     Epoch 466/1000
>     0s - loss: 2.2439 - acc: 0.3478
>     Epoch 467/1000
>     0s - loss: 2.2427 - acc: 0.3478
>     Epoch 468/1000
>     0s - loss: 2.2410 - acc: 0.3478
>     Epoch 469/1000
>     0s - loss: 2.2402 - acc: 0.3478
>     Epoch 470/1000
>     0s - loss: 2.2393 - acc: 0.3478
>     Epoch 471/1000
>     0s - loss: 2.2380 - acc: 0.3478
>     Epoch 472/1000
>     0s - loss: 2.2371 - acc: 0.3478
>     Epoch 473/1000
>     0s - loss: 2.2356 - acc: 0.3478
>     Epoch 474/1000
>     0s - loss: 2.2343 - acc: 0.3478
>     Epoch 475/1000
>     0s - loss: 2.2337 - acc: 0.3478
>     Epoch 476/1000
>     0s - loss: 2.2322 - acc: 0.3913
>     Epoch 477/1000
>     0s - loss: 2.2308 - acc: 0.3913
>     Epoch 478/1000
>     0s - loss: 2.2293 - acc: 0.3913
>     Epoch 479/1000
>     0s - loss: 2.2289 - acc: 0.3913
>     Epoch 480/1000
>     0s - loss: 2.2280 - acc: 0.3913
>     Epoch 481/1000
>     0s - loss: 2.2265 - acc: 0.3913
>     Epoch 482/1000
>     0s - loss: 2.2259 - acc: 0.3913
>     Epoch 483/1000
>     0s - loss: 2.2240 - acc: 0.3913
>     Epoch 484/1000
>     0s - loss: 2.2233 - acc: 0.3913
>     Epoch 485/1000
>     0s - loss: 2.2217 - acc: 0.3913
>     Epoch 486/1000
>     0s - loss: 2.2209 - acc: 0.3913
>     Epoch 487/1000
>     0s - loss: 2.2196 - acc: 0.3913
>     Epoch 488/1000
>     0s - loss: 2.2183 - acc: 0.3913
>     Epoch 489/1000
>     0s - loss: 2.2173 - acc: 0.3913
>     Epoch 490/1000
>     0s - loss: 2.2157 - acc: 0.3913
>     Epoch 491/1000
>     0s - loss: 2.2158 - acc: 0.3913
>     Epoch 492/1000
>     0s - loss: 2.2143 - acc: 0.3478
>     Epoch 493/1000
>     0s - loss: 2.2131 - acc: 0.3478
>     Epoch 494/1000
>     0s - loss: 2.2120 - acc: 0.3478
>     Epoch 495/1000
>     0s - loss: 2.2116 - acc: 0.3478
>     Epoch 496/1000
>     0s - loss: 2.2096 - acc: 0.3478
>     Epoch 497/1000
>     0s - loss: 2.2084 - acc: 0.3478
>     Epoch 498/1000
>     0s - loss: 2.2078 - acc: 0.3478
>     Epoch 499/1000
>     0s - loss: 2.2063 - acc: 0.3478
>     Epoch 500/1000
>     0s - loss: 2.2053 - acc: 0.3478
>     Epoch 501/1000
>     0s - loss: 2.2040 - acc: 0.3478
>     Epoch 502/1000
>     0s - loss: 2.2032 - acc: 0.3478
>     Epoch 503/1000
>     0s - loss: 2.2020 - acc: 0.3478
>     Epoch 504/1000
>     0s - loss: 2.2013 - acc: 0.3478
>     Epoch 505/1000
>     0s - loss: 2.2002 - acc: 0.3478
>     Epoch 506/1000
>     0s - loss: 2.1993 - acc: 0.3478
>     Epoch 507/1000
>     0s - loss: 2.1981 - acc: 0.3913
>     Epoch 508/1000
>     0s - loss: 2.1968 - acc: 0.3478
>     Epoch 509/1000
>     0s - loss: 2.1957 - acc: 0.3913
>     Epoch 510/1000
>     0s - loss: 2.1943 - acc: 0.4348
>     Epoch 511/1000
>     0s - loss: 2.1935 - acc: 0.4348
>     Epoch 512/1000
>     0s - loss: 2.1920 - acc: 0.4348
>     Epoch 513/1000
>     0s - loss: 2.1916 - acc: 0.4348
>     Epoch 514/1000
>     0s - loss: 2.1898 - acc: 0.4348
>     Epoch 515/1000
>     0s - loss: 2.1891 - acc: 0.4348
>     Epoch 516/1000
>     0s - loss: 2.1882 - acc: 0.3913
>     Epoch 517/1000
>     0s - loss: 2.1875 - acc: 0.3913
>     Epoch 518/1000
>     0s - loss: 2.1863 - acc: 0.4348
>     Epoch 519/1000
>     0s - loss: 2.1849 - acc: 0.4348
>     Epoch 520/1000
>     0s - loss: 2.1846 - acc: 0.4348
>     Epoch 521/1000
>     0s - loss: 2.1831 - acc: 0.4348
>     Epoch 522/1000
>     0s - loss: 2.1823 - acc: 0.4348
>     Epoch 523/1000
>     0s - loss: 2.1811 - acc: 0.4348
>     Epoch 524/1000
>     0s - loss: 2.1795 - acc: 0.4783
>     Epoch 525/1000
>     0s - loss: 2.1798 - acc: 0.4348
>     Epoch 526/1000
>     0s - loss: 2.1776 - acc: 0.3913
>     Epoch 527/1000
>     0s - loss: 2.1773 - acc: 0.3913
>     Epoch 528/1000
>     0s - loss: 2.1752 - acc: 0.3913
>     Epoch 529/1000
>     0s - loss: 2.1744 - acc: 0.3913
>     Epoch 530/1000
>     0s - loss: 2.1736 - acc: 0.3913
>     Epoch 531/1000
>     0s - loss: 2.1726 - acc: 0.4348
>     Epoch 532/1000
>     0s - loss: 2.1721 - acc: 0.4348
>     Epoch 533/1000
>     0s - loss: 2.1710 - acc: 0.3913
>     Epoch 534/1000
>     0s - loss: 2.1694 - acc: 0.3913
>     Epoch 535/1000
>     0s - loss: 2.1684 - acc: 0.3913
>     Epoch 536/1000
>     0s - loss: 2.1668 - acc: 0.3913
>     Epoch 537/1000
>     0s - loss: 2.1667 - acc: 0.3913
>     Epoch 538/1000
>     0s - loss: 2.1658 - acc: 0.3913
>     Epoch 539/1000
>     0s - loss: 2.1647 - acc: 0.3913
>     Epoch 540/1000
>     0s - loss: 2.1634 - acc: 0.3913
>     Epoch 541/1000
>     0s - loss: 2.1622 - acc: 0.3913
>     Epoch 542/1000
>     0s - loss: 2.1617 - acc: 0.3913
>     Epoch 543/1000
>     0s - loss: 2.1598 - acc: 0.3478
>     Epoch 544/1000
>     0s - loss: 2.1594 - acc: 0.4348
>     Epoch 545/1000
>     0s - loss: 2.1588 - acc: 0.3913
>     Epoch 546/1000
>     0s - loss: 2.1567 - acc: 0.3913
>     Epoch 547/1000
>     0s - loss: 2.1563 - acc: 0.3913
>     Epoch 548/1000
>     0s - loss: 2.1553 - acc: 0.3913
>     Epoch 549/1000
>     0s - loss: 2.1549 - acc: 0.4348
>     Epoch 550/1000
>     0s - loss: 2.1531 - acc: 0.4348
>     Epoch 551/1000
>     0s - loss: 2.1529 - acc: 0.4348
>     Epoch 552/1000
>     0s - loss: 2.1518 - acc: 0.3913
>     Epoch 553/1000
>     0s - loss: 2.1507 - acc: 0.3913
>     Epoch 554/1000
>     0s - loss: 2.1498 - acc: 0.3913
>     Epoch 555/1000
>     0s - loss: 2.1485 - acc: 0.4783
>     Epoch 556/1000
>     0s - loss: 2.1474 - acc: 0.4783
>     Epoch 557/1000
>     0s - loss: 2.1464 - acc: 0.4348
>     Epoch 558/1000
>     0s - loss: 2.1453 - acc: 0.4348
>     Epoch 559/1000
>     0s - loss: 2.1446 - acc: 0.4348
>     Epoch 560/1000
>     0s - loss: 2.1431 - acc: 0.4348
>     Epoch 561/1000
>     0s - loss: 2.1427 - acc: 0.4348
>     Epoch 562/1000
>     0s - loss: 2.1415 - acc: 0.4348
>     Epoch 563/1000
>     0s - loss: 2.1407 - acc: 0.4348
>     Epoch 564/1000
>     0s - loss: 2.1396 - acc: 0.4783
>     Epoch 565/1000
>     0s - loss: 2.1387 - acc: 0.4783
>     Epoch 566/1000
>     0s - loss: 2.1374 - acc: 0.4783
>     Epoch 567/1000
>     0s - loss: 2.1365 - acc: 0.4783
>     Epoch 568/1000
>     0s - loss: 2.1359 - acc: 0.4783
>     Epoch 569/1000
>     0s - loss: 2.1349 - acc: 0.4783
>     Epoch 570/1000
>     0s - loss: 2.1335 - acc: 0.4783
>     Epoch 571/1000
>     0s - loss: 2.1324 - acc: 0.4783
>     Epoch 572/1000
>     0s - loss: 2.1314 - acc: 0.4348
>     Epoch 573/1000
>     0s - loss: 2.1308 - acc: 0.4348
>     Epoch 574/1000
>     0s - loss: 2.1302 - acc: 0.4348
>     Epoch 575/1000
>     0s - loss: 2.1291 - acc: 0.4348
>     Epoch 576/1000
>     0s - loss: 2.1280 - acc: 0.4348
>     Epoch 577/1000
>     0s - loss: 2.1267 - acc: 0.4348
>     Epoch 578/1000
>     0s - loss: 2.1256 - acc: 0.4348
>     Epoch 579/1000
>     0s - loss: 2.1252 - acc: 0.4348
>     Epoch 580/1000
>     0s - loss: 2.1246 - acc: 0.4348
>     Epoch 581/1000
>     0s - loss: 2.1234 - acc: 0.4348
>     Epoch 582/1000
>     0s - loss: 2.1219 - acc: 0.4348
>     Epoch 583/1000
>     0s - loss: 2.1214 - acc: 0.4348
>     Epoch 584/1000
>     0s - loss: 2.1206 - acc: 0.4348
>     Epoch 585/1000
>     0s - loss: 2.1192 - acc: 0.4348
>     Epoch 586/1000
>     0s - loss: 2.1180 - acc: 0.4348
>     Epoch 587/1000
>     0s - loss: 2.1178 - acc: 0.4348
>     Epoch 588/1000
>     0s - loss: 2.1163 - acc: 0.4348
>     Epoch 589/1000
>     0s - loss: 2.1160 - acc: 0.4348
>     Epoch 590/1000
>     0s - loss: 2.1152 - acc: 0.4348
>     Epoch 591/1000
>     0s - loss: 2.1139 - acc: 0.4348
>     Epoch 592/1000
>     0s - loss: 2.1132 - acc: 0.3913
>     Epoch 593/1000
>     0s - loss: 2.1123 - acc: 0.4348
>     Epoch 594/1000
>     0s - loss: 2.1109 - acc: 0.4348
>     Epoch 595/1000
>     0s - loss: 2.1105 - acc: 0.4348
>     Epoch 596/1000
>     0s - loss: 2.1091 - acc: 0.4348
>     Epoch 597/1000
>     0s - loss: 2.1089 - acc: 0.4348
>     Epoch 598/1000
>     0s - loss: 2.1081 - acc: 0.4783
>     Epoch 599/1000
>     0s - loss: 2.1068 - acc: 0.5217
>     Epoch 600/1000
>     0s - loss: 2.1060 - acc: 0.4783
>     Epoch 601/1000
>     0s - loss: 2.1048 - acc: 0.4348
>     Epoch 602/1000
>     0s - loss: 2.1040 - acc: 0.4348
>     Epoch 603/1000
>     0s - loss: 2.1033 - acc: 0.4783
>     Epoch 604/1000
>     0s - loss: 2.1025 - acc: 0.5217
>     Epoch 605/1000
>     0s - loss: 2.1014 - acc: 0.4783
>     Epoch 606/1000
>     0s - loss: 2.1002 - acc: 0.4783
>     Epoch 607/1000
>     0s - loss: 2.0992 - acc: 0.4783
>     Epoch 608/1000
>     0s - loss: 2.0991 - acc: 0.4783
>     Epoch 609/1000
>     0s - loss: 2.0977 - acc: 0.4348
>     Epoch 610/1000
>     0s - loss: 2.0966 - acc: 0.4783
>     Epoch 611/1000
>     0s - loss: 2.0965 - acc: 0.4783
>     Epoch 612/1000
>     0s - loss: 2.0952 - acc: 0.4783
>     Epoch 613/1000
>     0s - loss: 2.0937 - acc: 0.4783
>     Epoch 614/1000
>     0s - loss: 2.0930 - acc: 0.4783
>     Epoch 615/1000
>     0s - loss: 2.0929 - acc: 0.4783
>     Epoch 616/1000
>     0s - loss: 2.0913 - acc: 0.4348
>     Epoch 617/1000
>     0s - loss: 2.0897 - acc: 0.4783
>     Epoch 618/1000
>     0s - loss: 2.0890 - acc: 0.4783
>     Epoch 619/1000
>     0s - loss: 2.0885 - acc: 0.5217
>     Epoch 620/1000
>     0s - loss: 2.0874 - acc: 0.4783
>     Epoch 621/1000
>     0s - loss: 2.0862 - acc: 0.4783
>     Epoch 622/1000
>     0s - loss: 2.0862 - acc: 0.4783
>     Epoch 623/1000
>     0s - loss: 2.0854 - acc: 0.4783
>     Epoch 624/1000
>     0s - loss: 2.0840 - acc: 0.4783
>     Epoch 625/1000
>     0s - loss: 2.0832 - acc: 0.5217
>     Epoch 626/1000
>     0s - loss: 2.0823 - acc: 0.5217
>     Epoch 627/1000
>     0s - loss: 2.0810 - acc: 0.5652
>     Epoch 628/1000
>     0s - loss: 2.0804 - acc: 0.5652
>     Epoch 629/1000
>     0s - loss: 2.0794 - acc: 0.5652
>     Epoch 630/1000
>     0s - loss: 2.0790 - acc: 0.5217
>     Epoch 631/1000
>     0s - loss: 2.0775 - acc: 0.5217
>     Epoch 632/1000
>     0s - loss: 2.0765 - acc: 0.5217
>     Epoch 633/1000
>     0s - loss: 2.0758 - acc: 0.5652
>     Epoch 634/1000
>     0s - loss: 2.0748 - acc: 0.5652
>     Epoch 635/1000
>     0s - loss: 2.0747 - acc: 0.4783
>     Epoch 636/1000
>     0s - loss: 2.0735 - acc: 0.4348
>     Epoch 637/1000
>     0s - loss: 2.0728 - acc: 0.4348
>     Epoch 638/1000
>     0s - loss: 2.0722 - acc: 0.4348
>     Epoch 639/1000
>     0s - loss: 2.0708 - acc: 0.4348
>     Epoch 640/1000
>     0s - loss: 2.0703 - acc: 0.4783
>     Epoch 641/1000
>     0s - loss: 2.0684 - acc: 0.5652
>     Epoch 642/1000
>     0s - loss: 2.0680 - acc: 0.5652
>     Epoch 643/1000
>     0s - loss: 2.0671 - acc: 0.5217
>     Epoch 644/1000
>     0s - loss: 2.0665 - acc: 0.5217
>     Epoch 645/1000
>     0s - loss: 2.0655 - acc: 0.5217
>     Epoch 646/1000
>     0s - loss: 2.0654 - acc: 0.4783
>     Epoch 647/1000
>     0s - loss: 2.0645 - acc: 0.4348
>     Epoch 648/1000
>     0s - loss: 2.0635 - acc: 0.4348
>     Epoch 649/1000
>     0s - loss: 2.0619 - acc: 0.3913
>     Epoch 650/1000
>     0s - loss: 2.0611 - acc: 0.4348
>     Epoch 651/1000
>     0s - loss: 2.0603 - acc: 0.4783
>     Epoch 652/1000
>     0s - loss: 2.0591 - acc: 0.4783
>     Epoch 653/1000
>     0s - loss: 2.0601 - acc: 0.4348
>     Epoch 654/1000
>     0s - loss: 2.0581 - acc: 0.4783
>     Epoch 655/1000
>     0s - loss: 2.0576 - acc: 0.5217
>     Epoch 656/1000
>     0s - loss: 2.0568 - acc: 0.4783
>     Epoch 657/1000
>     0s - loss: 2.0560 - acc: 0.4783
>     Epoch 658/1000
>     0s - loss: 2.0550 - acc: 0.3913
>     Epoch 659/1000
>     0s - loss: 2.0543 - acc: 0.4348
>     Epoch 660/1000
>     0s - loss: 2.0530 - acc: 0.4348
>     Epoch 661/1000
>     0s - loss: 2.0529 - acc: 0.4783
>     Epoch 662/1000
>     0s - loss: 2.0517 - acc: 0.4348
>     Epoch 663/1000
>     0s - loss: 2.0502 - acc: 0.4348
>     Epoch 664/1000
>     0s - loss: 2.0495 - acc: 0.4348
>     Epoch 665/1000
>     0s - loss: 2.0489 - acc: 0.4348
>     Epoch 666/1000
>     0s - loss: 2.0481 - acc: 0.4348
>     Epoch 667/1000
>     0s - loss: 2.0472 - acc: 0.3913
>     Epoch 668/1000
>     0s - loss: 2.0464 - acc: 0.3913
>     Epoch 669/1000
>     0s - loss: 2.0455 - acc: 0.3913
>     Epoch 670/1000
>     0s - loss: 2.0450 - acc: 0.3913
>     Epoch 671/1000
>     0s - loss: 2.0433 - acc: 0.3913
>     Epoch 672/1000
>     0s - loss: 2.0429 - acc: 0.4348
>     Epoch 673/1000
>     0s - loss: 2.0417 - acc: 0.4348
>     Epoch 674/1000
>     0s - loss: 2.0412 - acc: 0.4348
>     Epoch 675/1000
>     0s - loss: 2.0404 - acc: 0.4783
>     Epoch 676/1000
>     0s - loss: 2.0398 - acc: 0.4783
>     Epoch 677/1000
>     0s - loss: 2.0386 - acc: 0.5652
>     Epoch 678/1000
>     0s - loss: 2.0383 - acc: 0.5652
>     Epoch 679/1000
>     0s - loss: 2.0377 - acc: 0.5652
>     Epoch 680/1000
>     0s - loss: 2.0364 - acc: 0.5652
>     Epoch 681/1000
>     0s - loss: 2.0352 - acc: 0.4348
>     Epoch 682/1000
>     0s - loss: 2.0350 - acc: 0.5217
>     Epoch 683/1000
>     0s - loss: 2.0342 - acc: 0.5652
>     Epoch 684/1000
>     0s - loss: 2.0336 - acc: 0.5217
>     Epoch 685/1000
>     0s - loss: 2.0334 - acc: 0.4783
>     Epoch 686/1000
>     0s - loss: 2.0315 - acc: 0.5217
>     Epoch 687/1000
>     0s - loss: 2.0312 - acc: 0.4783
>     Epoch 688/1000
>     0s - loss: 2.0305 - acc: 0.5217
>     Epoch 689/1000
>     0s - loss: 2.0293 - acc: 0.4783
>     Epoch 690/1000
>     0s - loss: 2.0296 - acc: 0.5652
>     Epoch 691/1000
>     0s - loss: 2.0274 - acc: 0.5652
>     Epoch 692/1000
>     0s - loss: 2.0268 - acc: 0.5652
>     Epoch 693/1000
>     0s - loss: 2.0262 - acc: 0.4783
>     Epoch 694/1000
>     0s - loss: 2.0250 - acc: 0.4348
>     Epoch 695/1000
>     0s - loss: 2.0245 - acc: 0.4348
>     Epoch 696/1000
>     0s - loss: 2.0234 - acc: 0.4783
>     Epoch 697/1000
>     0s - loss: 2.0224 - acc: 0.5217
>     Epoch 698/1000
>     0s - loss: 2.0219 - acc: 0.5217
>     Epoch 699/1000
>     0s - loss: 2.0214 - acc: 0.5217
>     Epoch 700/1000
>     0s - loss: 2.0198 - acc: 0.5217
>     Epoch 701/1000
>     0s - loss: 2.0194 - acc: 0.5217
>     Epoch 702/1000
>     0s - loss: 2.0185 - acc: 0.5217
>     Epoch 703/1000
>     0s - loss: 2.0185 - acc: 0.4783
>     Epoch 704/1000
>     0s - loss: 2.0178 - acc: 0.4783
>     Epoch 705/1000
>     0s - loss: 2.0164 - acc: 0.5652
>     Epoch 706/1000
>     0s - loss: 2.0151 - acc: 0.5652
>     Epoch 707/1000
>     0s - loss: 2.0148 - acc: 0.5652
>     Epoch 708/1000
>     0s - loss: 2.0140 - acc: 0.5217
>     Epoch 709/1000
>     0s - loss: 2.0124 - acc: 0.4783
>     Epoch 710/1000
>     0s - loss: 2.0126 - acc: 0.4783
>     Epoch 711/1000
>     0s - loss: 2.0115 - acc: 0.4348
>     Epoch 712/1000
>     0s - loss: 2.0117 - acc: 0.4348
>     Epoch 713/1000
>     0s - loss: 2.0101 - acc: 0.4783
>     Epoch 714/1000
>     0s - loss: 2.0096 - acc: 0.4783
>     Epoch 715/1000
>     0s - loss: 2.0093 - acc: 0.4783
>     Epoch 716/1000
>     0s - loss: 2.0078 - acc: 0.4783
>     Epoch 717/1000
>     0s - loss: 2.0066 - acc: 0.5652
>     Epoch 718/1000
>     0s - loss: 2.0059 - acc: 0.5652
>     Epoch 719/1000
>     0s - loss: 2.0058 - acc: 0.5217
>     Epoch 720/1000
>     0s - loss: 2.0050 - acc: 0.5217
>     Epoch 721/1000
>     0s - loss: 2.0046 - acc: 0.5217
>     Epoch 722/1000
>     0s - loss: 2.0036 - acc: 0.5217
>     Epoch 723/1000
>     0s - loss: 2.0024 - acc: 0.5217
>     Epoch 724/1000
>     0s - loss: 2.0016 - acc: 0.5217
>     Epoch 725/1000
>     0s - loss: 2.0008 - acc: 0.5652
>     Epoch 726/1000
>     0s - loss: 2.0006 - acc: 0.5217
>     Epoch 727/1000
>     0s - loss: 1.9986 - acc: 0.5652
>     Epoch 728/1000
>     0s - loss: 1.9988 - acc: 0.5652
>     Epoch 729/1000
>     0s - loss: 1.9981 - acc: 0.6087
>     Epoch 730/1000
>     0s - loss: 1.9969 - acc: 0.5652
>     Epoch 731/1000
>     0s - loss: 1.9962 - acc: 0.5652
>     Epoch 732/1000
>     0s - loss: 1.9954 - acc: 0.5652
>     Epoch 733/1000
>     0s - loss: 1.9946 - acc: 0.5217
>     Epoch 734/1000
>     0s - loss: 1.9936 - acc: 0.4783
>     Epoch 735/1000
>     0s - loss: 1.9916 - acc: 0.4783
>     Epoch 736/1000
>     0s - loss: 1.9926 - acc: 0.5217
>     Epoch 737/1000
>     0s - loss: 1.9922 - acc: 0.4783
>     Epoch 738/1000
>     0s - loss: 1.9910 - acc: 0.4783
>     Epoch 739/1000
>     0s - loss: 1.9900 - acc: 0.4783
>     Epoch 740/1000
>     0s - loss: 1.9897 - acc: 0.5217
>     Epoch 741/1000
>     0s - loss: 1.9889 - acc: 0.5652
>     Epoch 742/1000
>     0s - loss: 1.9883 - acc: 0.6087
>     Epoch 743/1000
>     0s - loss: 1.9876 - acc: 0.5652
>     Epoch 744/1000
>     0s - loss: 1.9870 - acc: 0.5652
>     Epoch 745/1000
>     0s - loss: 1.9856 - acc: 0.4783
>     Epoch 746/1000
>     0s - loss: 1.9852 - acc: 0.5217
>     Epoch 747/1000
>     0s - loss: 1.9841 - acc: 0.5217
>     Epoch 748/1000
>     0s - loss: 1.9834 - acc: 0.5652
>     Epoch 749/1000
>     0s - loss: 1.9829 - acc: 0.5217
>     Epoch 750/1000
>     0s - loss: 1.9827 - acc: 0.5217
>     Epoch 751/1000
>     0s - loss: 1.9807 - acc: 0.5217
>     Epoch 752/1000
>     0s - loss: 1.9805 - acc: 0.5217
>     Epoch 753/1000
>     0s - loss: 1.9795 - acc: 0.5217
>     Epoch 754/1000
>     0s - loss: 1.9793 - acc: 0.4783
>     Epoch 755/1000
>     0s - loss: 1.9781 - acc: 0.5652
>     Epoch 756/1000
>     0s - loss: 1.9770 - acc: 0.5652
>     Epoch 757/1000
>     0s - loss: 1.9768 - acc: 0.5652
>     Epoch 758/1000
>     0s - loss: 1.9750 - acc: 0.5217
>     Epoch 759/1000
>     0s - loss: 1.9751 - acc: 0.4783
>     Epoch 760/1000
>     0s - loss: 1.9741 - acc: 0.4348
>     Epoch 761/1000
>     0s - loss: 1.9732 - acc: 0.4348
>     Epoch 762/1000
>     0s - loss: 1.9728 - acc: 0.4348
>     Epoch 763/1000
>     0s - loss: 1.9720 - acc: 0.4783
>     Epoch 764/1000
>     0s - loss: 1.9716 - acc: 0.4783
>     Epoch 765/1000
>     0s - loss: 1.9704 - acc: 0.4783
>     Epoch 766/1000
>     0s - loss: 1.9701 - acc: 0.4783
>     Epoch 767/1000
>     0s - loss: 1.9699 - acc: 0.5217
>     Epoch 768/1000
>     0s - loss: 1.9683 - acc: 0.4783
>     Epoch 769/1000
>     0s - loss: 1.9676 - acc: 0.4783
>     Epoch 770/1000
>     0s - loss: 1.9668 - acc: 0.4783
>     Epoch 771/1000
>     0s - loss: 1.9664 - acc: 0.4348
>     Epoch 772/1000
>     0s - loss: 1.9664 - acc: 0.4348
>     Epoch 773/1000
>     0s - loss: 1.9653 - acc: 0.4348
>     Epoch 774/1000
>     0s - loss: 1.9646 - acc: 0.4348
>     Epoch 775/1000
>     0s - loss: 1.9647 - acc: 0.4783
>     Epoch 776/1000
>     0s - loss: 1.9623 - acc: 0.5217
>     Epoch 777/1000
>     0s - loss: 1.9619 - acc: 0.5217
>     Epoch 778/1000
>     0s - loss: 1.9613 - acc: 0.4783
>     Epoch 779/1000
>     0s - loss: 1.9604 - acc: 0.4783
>     Epoch 780/1000
>     0s - loss: 1.9598 - acc: 0.4348
>     Epoch 781/1000
>     0s - loss: 1.9595 - acc: 0.4348
>     Epoch 782/1000
>     0s - loss: 1.9592 - acc: 0.4348
>     Epoch 783/1000
>     0s - loss: 1.9578 - acc: 0.4348
>     Epoch 784/1000
>     0s - loss: 1.9570 - acc: 0.4348
>     Epoch 785/1000
>     0s - loss: 1.9558 - acc: 0.4348
>     Epoch 786/1000
>     0s - loss: 1.9558 - acc: 0.4348
>     Epoch 787/1000
>     0s - loss: 1.9555 - acc: 0.4348
>     Epoch 788/1000
>     0s - loss: 1.9548 - acc: 0.3913
>     Epoch 789/1000
>     0s - loss: 1.9536 - acc: 0.4348
>     Epoch 790/1000
>     0s - loss: 1.9534 - acc: 0.4783
>     Epoch 791/1000
>     0s - loss: 1.9529 - acc: 0.4783
>     Epoch 792/1000
>     0s - loss: 1.9519 - acc: 0.4783
>     Epoch 793/1000
>     0s - loss: 1.9518 - acc: 0.4348
>     Epoch 794/1000
>     0s - loss: 1.9507 - acc: 0.4348
>     Epoch 795/1000
>     0s - loss: 1.9494 - acc: 0.4348
>     Epoch 796/1000
>     0s - loss: 1.9492 - acc: 0.4348
>     Epoch 797/1000
>     0s - loss: 1.9486 - acc: 0.4348
>     Epoch 798/1000
>     0s - loss: 1.9472 - acc: 0.4348
>     Epoch 799/1000
>     0s - loss: 1.9464 - acc: 0.4348
>     Epoch 800/1000
>     0s - loss: 1.9454 - acc: 0.3913
>     Epoch 801/1000
>     0s - loss: 1.9450 - acc: 0.4348
>     Epoch 802/1000
>     0s - loss: 1.9449 - acc: 0.4348
>     Epoch 803/1000
>     0s - loss: 1.9444 - acc: 0.4348
>     Epoch 804/1000
>     0s - loss: 1.9428 - acc: 0.4348
>     Epoch 805/1000
>     0s - loss: 1.9427 - acc: 0.4348
>     Epoch 806/1000
>     0s - loss: 1.9421 - acc: 0.3913
>     Epoch 807/1000
>     0s - loss: 1.9406 - acc: 0.4348
>     Epoch 808/1000
>     0s - loss: 1.9401 - acc: 0.4348
>     Epoch 809/1000
>     0s - loss: 1.9398 - acc: 0.4783
>     Epoch 810/1000
>     0s - loss: 1.9382 - acc: 0.4783
>     Epoch 811/1000
>     0s - loss: 1.9384 - acc: 0.4348
>     Epoch 812/1000
>     0s - loss: 1.9370 - acc: 0.4783
>     Epoch 813/1000
>     0s - loss: 1.9364 - acc: 0.5217
>     Epoch 814/1000
>     0s - loss: 1.9361 - acc: 0.5217
>     Epoch 815/1000
>     0s - loss: 1.9364 - acc: 0.5652
>     Epoch 816/1000
>     0s - loss: 1.9348 - acc: 0.5652
>     Epoch 817/1000
>     0s - loss: 1.9343 - acc: 0.5217
>     Epoch 818/1000
>     0s - loss: 1.9327 - acc: 0.5652
>     Epoch 819/1000
>     0s - loss: 1.9328 - acc: 0.5217
>     Epoch 820/1000
>     0s - loss: 1.9328 - acc: 0.4783
>     Epoch 821/1000
>     0s - loss: 1.9313 - acc: 0.4783
>     Epoch 822/1000
>     0s - loss: 1.9309 - acc: 0.4783
>     Epoch 823/1000
>     0s - loss: 1.9298 - acc: 0.4783
>     Epoch 824/1000
>     0s - loss: 1.9282 - acc: 0.5217
>     Epoch 825/1000
>     0s - loss: 1.9286 - acc: 0.5217
>     Epoch 826/1000
>     0s - loss: 1.9269 - acc: 0.4783
>     Epoch 827/1000
>     0s - loss: 1.9274 - acc: 0.5217
>     Epoch 828/1000
>     0s - loss: 1.9270 - acc: 0.4783
>     Epoch 829/1000
>     0s - loss: 1.9262 - acc: 0.5217
>     Epoch 830/1000
>     0s - loss: 1.9252 - acc: 0.5217
>     Epoch 831/1000
>     0s - loss: 1.9251 - acc: 0.5652
>     Epoch 832/1000
>     0s - loss: 1.9239 - acc: 0.5217
>     Epoch 833/1000
>     0s - loss: 1.9237 - acc: 0.5652
>     Epoch 834/1000
>     0s - loss: 1.9224 - acc: 0.5652
>     Epoch 835/1000
>     0s - loss: 1.9213 - acc: 0.5217
>     Epoch 836/1000
>     0s - loss: 1.9203 - acc: 0.5217
>     Epoch 837/1000
>     0s - loss: 1.9210 - acc: 0.5217
>     Epoch 838/1000
>     0s - loss: 1.9193 - acc: 0.4783
>     Epoch 839/1000
>     0s - loss: 1.9194 - acc: 0.5217
>     Epoch 840/1000
>     0s - loss: 1.9185 - acc: 0.5217
>     Epoch 841/1000
>     0s - loss: 1.9170 - acc: 0.5217
>     Epoch 842/1000
>     0s - loss: 1.9170 - acc: 0.4783
>     Epoch 843/1000
>     0s - loss: 1.9160 - acc: 0.4783
>     Epoch 844/1000
>     0s - loss: 1.9149 - acc: 0.5652
>     Epoch 845/1000
>     0s - loss: 1.9151 - acc: 0.5217
>     Epoch 846/1000
>     0s - loss: 1.9147 - acc: 0.4783
>     Epoch 847/1000
>     0s - loss: 1.9143 - acc: 0.5652
>     Epoch 848/1000
>     0s - loss: 1.9129 - acc: 0.5652
>     Epoch 849/1000
>     0s - loss: 1.9134 - acc: 0.5652
>     Epoch 850/1000
>     0s - loss: 1.9115 - acc: 0.5652
>     Epoch 851/1000
>     0s - loss: 1.9111 - acc: 0.4783
>     Epoch 852/1000
>     0s - loss: 1.9109 - acc: 0.5217
>     Epoch 853/1000
>     0s - loss: 1.9103 - acc: 0.5652
>     Epoch 854/1000
>     0s - loss: 1.9091 - acc: 0.5652
>     Epoch 855/1000
>     0s - loss: 1.9085 - acc: 0.4783
>     Epoch 856/1000
>     0s - loss: 1.9076 - acc: 0.4783
>     Epoch 857/1000
>     0s - loss: 1.9074 - acc: 0.4783
>     Epoch 858/1000
>     0s - loss: 1.9072 - acc: 0.4783
>     Epoch 859/1000
>     0s - loss: 1.9061 - acc: 0.4783
>     Epoch 860/1000
>     0s - loss: 1.9057 - acc: 0.5217
>     Epoch 861/1000
>     0s - loss: 1.9047 - acc: 0.5217
>     Epoch 862/1000
>     0s - loss: 1.9046 - acc: 0.5217
>     Epoch 863/1000
>     0s - loss: 1.9034 - acc: 0.5217
>     Epoch 864/1000
>     0s - loss: 1.9028 - acc: 0.5652
>     Epoch 865/1000
>     0s - loss: 1.9017 - acc: 0.5652
>     Epoch 866/1000
>     0s - loss: 1.9012 - acc: 0.6087
>     Epoch 867/1000
>     0s - loss: 1.9005 - acc: 0.5652
>     Epoch 868/1000
>     0s - loss: 1.8999 - acc: 0.5652
>     Epoch 869/1000
>     0s - loss: 1.8991 - acc: 0.4783
>     Epoch 870/1000
>     0s - loss: 1.8983 - acc: 0.5217
>     Epoch 871/1000
>     0s - loss: 1.8980 - acc: 0.5217
>     Epoch 872/1000
>     0s - loss: 1.8978 - acc: 0.5217
>     Epoch 873/1000
>     0s - loss: 1.8968 - acc: 0.5217
>     Epoch 874/1000
>     0s - loss: 1.8961 - acc: 0.5217
>     Epoch 875/1000
>     0s - loss: 1.8958 - acc: 0.5217
>     Epoch 876/1000
>     0s - loss: 1.8949 - acc: 0.5217
>     Epoch 877/1000
>     0s - loss: 1.8942 - acc: 0.5217
>     Epoch 878/1000
>     0s - loss: 1.8930 - acc: 0.4783
>     Epoch 879/1000
>     0s - loss: 1.8919 - acc: 0.4783
>     Epoch 880/1000
>     0s - loss: 1.8919 - acc: 0.4783
>     Epoch 881/1000
>     0s - loss: 1.8909 - acc: 0.4783
>     Epoch 882/1000
>     0s - loss: 1.8906 - acc: 0.4783
>     Epoch 883/1000
>     0s - loss: 1.8910 - acc: 0.4783
>     Epoch 884/1000
>     0s - loss: 1.8900 - acc: 0.5217
>     Epoch 885/1000
>     0s - loss: 1.8890 - acc: 0.5217
>     Epoch 886/1000
>     0s - loss: 1.8880 - acc: 0.4783
>     Epoch 887/1000
>     0s - loss: 1.8880 - acc: 0.4783
>     Epoch 888/1000
>     0s - loss: 1.8875 - acc: 0.4783
>     Epoch 889/1000
>     0s - loss: 1.8864 - acc: 0.6522
>     Epoch 890/1000
>     0s - loss: 1.8855 - acc: 0.5652
>     Epoch 891/1000
>     0s - loss: 1.8848 - acc: 0.5652
>     Epoch 892/1000
>     0s - loss: 1.8835 - acc: 0.5652
>     Epoch 893/1000
>     0s - loss: 1.8841 - acc: 0.5217
>     Epoch 894/1000
>     0s - loss: 1.8841 - acc: 0.4783
>     Epoch 895/1000
>     0s - loss: 1.8834 - acc: 0.5217
>     Epoch 896/1000
>     0s - loss: 1.8821 - acc: 0.4783
>     Epoch 897/1000
>     0s - loss: 1.8825 - acc: 0.4783
>     Epoch 898/1000
>     0s - loss: 1.8807 - acc: 0.6087
>     Epoch 899/1000
>     0s - loss: 1.8809 - acc: 0.6087
>     Epoch 900/1000
>     0s - loss: 1.8799 - acc: 0.5217
>     Epoch 901/1000
>     0s - loss: 1.8788 - acc: 0.5217
>     Epoch 902/1000
>     0s - loss: 1.8780 - acc: 0.5217
>     Epoch 903/1000
>     0s - loss: 1.8774 - acc: 0.5652
>     Epoch 904/1000
>     0s - loss: 1.8761 - acc: 0.5652
>     Epoch 905/1000
>     0s - loss: 1.8756 - acc: 0.6087
>     Epoch 906/1000
>     0s - loss: 1.8759 - acc: 0.5652
>     Epoch 907/1000
>     0s - loss: 1.8751 - acc: 0.5217
>     Epoch 908/1000
>     0s - loss: 1.8746 - acc: 0.4783
>     Epoch 909/1000
>     0s - loss: 1.8730 - acc: 0.4783
>     Epoch 910/1000
>     0s - loss: 1.8723 - acc: 0.4783
>     Epoch 911/1000
>     0s - loss: 1.8730 - acc: 0.4783
>     Epoch 912/1000
>     0s - loss: 1.8721 - acc: 0.4783
>     Epoch 913/1000
>     0s - loss: 1.8715 - acc: 0.4783
>     Epoch 914/1000
>     0s - loss: 1.8713 - acc: 0.4783
>     Epoch 915/1000
>     0s - loss: 1.8704 - acc: 0.4783
>     Epoch 916/1000
>     0s - loss: 1.8695 - acc: 0.4783
>     Epoch 917/1000
>     0s - loss: 1.8686 - acc: 0.4783
>     Epoch 918/1000
>     0s - loss: 1.8684 - acc: 0.5217
>     Epoch 919/1000
>     0s - loss: 1.8676 - acc: 0.5217
>     Epoch 920/1000
>     0s - loss: 1.8666 - acc: 0.5217
>     Epoch 921/1000
>     0s - loss: 1.8660 - acc: 0.5217
>     Epoch 922/1000
>     0s - loss: 1.8658 - acc: 0.5217
>     Epoch 923/1000
>     0s - loss: 1.8659 - acc: 0.4783
>     Epoch 924/1000
>     0s - loss: 1.8643 - acc: 0.4783
>     Epoch 925/1000
>     0s - loss: 1.8637 - acc: 0.4783
>     Epoch 926/1000
>     0s - loss: 1.8633 - acc: 0.5217
>     Epoch 927/1000
>     0s - loss: 1.8625 - acc: 0.5217
>     Epoch 928/1000
>     0s - loss: 1.8614 - acc: 0.5217
>     Epoch 929/1000
>     0s - loss: 1.8616 - acc: 0.4783
>     Epoch 930/1000
>     0s - loss: 1.8609 - acc: 0.5217
>     Epoch 931/1000
>     0s - loss: 1.8599 - acc: 0.5217
>     Epoch 932/1000
>     0s - loss: 1.8588 - acc: 0.5217
>     Epoch 933/1000
>     0s - loss: 1.8591 - acc: 0.5217
>     Epoch 934/1000
>     0s - loss: 1.8589 - acc: 0.5217
>     Epoch 935/1000
>     0s - loss: 1.8578 - acc: 0.5217
>     Epoch 936/1000
>     0s - loss: 1.8573 - acc: 0.5217
>     Epoch 937/1000
>     0s - loss: 1.8565 - acc: 0.5217
>     Epoch 938/1000
>     0s - loss: 1.8567 - acc: 0.5217
>     Epoch 939/1000
>     0s - loss: 1.8554 - acc: 0.5652
>     Epoch 940/1000
>     0s - loss: 1.8552 - acc: 0.5217
>     Epoch 941/1000
>     0s - loss: 1.8546 - acc: 0.5217
>     Epoch 942/1000
>     0s - loss: 1.8536 - acc: 0.5652
>     Epoch 943/1000
>     0s - loss: 1.8533 - acc: 0.5652
>     Epoch 944/1000
>     0s - loss: 1.8535 - acc: 0.5217
>     Epoch 945/1000
>     0s - loss: 1.8517 - acc: 0.5217
>     Epoch 946/1000
>     0s - loss: 1.8512 - acc: 0.5217
>     Epoch 947/1000
>     0s - loss: 1.8505 - acc: 0.5217
>     Epoch 948/1000
>     0s - loss: 1.8501 - acc: 0.5217
>     Epoch 949/1000
>     0s - loss: 1.8492 - acc: 0.5217
>     Epoch 950/1000
>     0s - loss: 1.8482 - acc: 0.5217
>     Epoch 951/1000
>     0s - loss: 1.8484 - acc: 0.6087
>     Epoch 952/1000
>     0s - loss: 1.8476 - acc: 0.5652
>     Epoch 953/1000
>     0s - loss: 1.8462 - acc: 0.5652
>     Epoch 954/1000
>     0s - loss: 1.8466 - acc: 0.5652
>     Epoch 955/1000
>     0s - loss: 1.8460 - acc: 0.5217
>     Epoch 956/1000
>     0s - loss: 1.8453 - acc: 0.5217
>     Epoch 957/1000
>     0s - loss: 1.8447 - acc: 0.5217
>     Epoch 958/1000
>     0s - loss: 1.8437 - acc: 0.5217
>     Epoch 959/1000
>     0s - loss: 1.8432 - acc: 0.5217
>     Epoch 960/1000
>     0s - loss: 1.8424 - acc: 0.5652
>     Epoch 961/1000
>     0s - loss: 1.8412 - acc: 0.5652
>     Epoch 962/1000
>     0s - loss: 1.8424 - acc: 0.5652
>     Epoch 963/1000
>     0s - loss: 1.8401 - acc: 0.6087
>     Epoch 964/1000
>     0s - loss: 1.8412 - acc: 0.6522
>     Epoch 965/1000
>     0s - loss: 1.8402 - acc: 0.6522
>     Epoch 966/1000
>     0s - loss: 1.8395 - acc: 0.5652
>     Epoch 967/1000
>     0s - loss: 1.8385 - acc: 0.5652
>     Epoch 968/1000
>     0s - loss: 1.8383 - acc: 0.5652
>     Epoch 969/1000
>     0s - loss: 1.8374 - acc: 0.5652
>     Epoch 970/1000
>     0s - loss: 1.8361 - acc: 0.5652
>     Epoch 971/1000
>     0s - loss: 1.8361 - acc: 0.5217
>     Epoch 972/1000
>     0s - loss: 1.8366 - acc: 0.5217
>     Epoch 973/1000
>     0s - loss: 1.8361 - acc: 0.6087
>     Epoch 974/1000
>     0s - loss: 1.8341 - acc: 0.5652
>     Epoch 975/1000
>     0s - loss: 1.8339 - acc: 0.5217
>     Epoch 976/1000
>     0s - loss: 1.8333 - acc: 0.5217
>     Epoch 977/1000
>     0s - loss: 1.8334 - acc: 0.4783
>     Epoch 978/1000
>     0s - loss: 1.8322 - acc: 0.4783
>     Epoch 979/1000
>     0s - loss: 1.8327 - acc: 0.5217
>     Epoch 980/1000
>     0s - loss: 1.8312 - acc: 0.5652
>     Epoch 981/1000
>     0s - loss: 1.8315 - acc: 0.5217
>     Epoch 982/1000
>     0s - loss: 1.8304 - acc: 0.4783
>     Epoch 983/1000
>     0s - loss: 1.8300 - acc: 0.4783
>     Epoch 984/1000
>     0s - loss: 1.8291 - acc: 0.5217
>     Epoch 985/1000
>     0s - loss: 1.8284 - acc: 0.5217
>     Epoch 986/1000
>     0s - loss: 1.8267 - acc: 0.5217
>     Epoch 987/1000
>     0s - loss: 1.8273 - acc: 0.5217
>     Epoch 988/1000
>     0s - loss: 1.8266 - acc: 0.5217
>     Epoch 989/1000
>     0s - loss: 1.8263 - acc: 0.4783
>     Epoch 990/1000
>     0s - loss: 1.8254 - acc: 0.5217
>     Epoch 991/1000
>     0s - loss: 1.8250 - acc: 0.4783
>     Epoch 992/1000
>     0s - loss: 1.8241 - acc: 0.4783
>     Epoch 993/1000
>     0s - loss: 1.8235 - acc: 0.4783
>     Epoch 994/1000
>     0s - loss: 1.8223 - acc: 0.4783
>     Epoch 995/1000
>     0s - loss: 1.8224 - acc: 0.5217
>     Epoch 996/1000
>     0s - loss: 1.8223 - acc: 0.5217
>     Epoch 997/1000
>     0s - loss: 1.8211 - acc: 0.5652
>     Epoch 998/1000
>     0s - loss: 1.8199 - acc: 0.6087
>     Epoch 999/1000
>     0s - loss: 1.8204 - acc: 0.5652
>     Epoch 1000/1000
>     0s - loss: 1.8194 - acc: 0.5652
>     23/23 [==============================] - 0s
>     Model Accuracy: 0.61 
>     (['A', 'B', 'C'], '->', 'D')
>     (['B', 'C', 'D'], '->', 'E')
>     (['C', 'D', 'E'], '->', 'F')
>     (['D', 'E', 'F'], '->', 'G')
>     (['E', 'F', 'G'], '->', 'H')
>     (['F', 'G', 'H'], '->', 'I')
>     (['G', 'H', 'I'], '->', 'J')
>     (['H', 'I', 'J'], '->', 'K')
>     (['I', 'J', 'K'], '->', 'K')
>     (['J', 'K', 'L'], '->', 'M')
>     (['K', 'L', 'M'], '->', 'O')
>     (['L', 'M', 'N'], '->', 'O')
>     (['M', 'N', 'O'], '->', 'O')
>     (['N', 'O', 'P'], '->', 'Q')
>     (['O', 'P', 'Q'], '->', 'S')
>     (['P', 'Q', 'R'], '->', 'S')
>     (['Q', 'R', 'S'], '->', 'T')
>     (['R', 'S', 'T'], '->', 'V')
>     (['S', 'T', 'U'], '->', 'Y')
>     (['T', 'U', 'V'], '->', 'Y')
>     (['U', 'V', 'W'], '->', 'Y')
>     (['V', 'W', 'X'], '->', 'Z')
>     (['W', 'X', 'Y'], '->', 'Z')

``` python
X.shape[1], y.shape[1] # get a sense of the shapes to understand the network architecture
```

>     Out[3]: (3, 26)

The network does learn, and could be trained to get a good accuracy. But what's really going on here?

Let's leave aside for a moment the simplistic training data (one fun experiment would be to create corrupted sequences and augment the data with those, forcing the network to pay attention to the whole sequence).

Because the model is fundamentally symmetric and stateless (in terms of the sequence; naturally it has weights), this model would need to learn every sequential feature relative to every single sequence position. That seems difficult, inflexible, and inefficient.

Maybe we could add layers, neurons, and extra connections to mitigate parts of the problem. We could also do things like a 1D convolution to pick up frequencies and some patterns.

But instead, it might make more sense to explicitly model the sequential nature of the data (a bit like how we explictly modeled the 2D nature of image data with CNNs).

Recurrent Neural Network Concept
--------------------------------

**Let's take the neuron's output from one time (t) and feed it into that same neuron at a later time (t+1), in combination with other relevant inputs. Then we would have a neuron with memory.**

We can weight the "return" of that value and train the weight -- so the neuron learns how important the previous value is relative to the current one.

Different neurons might learn to "remember" different amounts of prior history.

This concept is called a *Recurrent Neural Network*, originally developed around the 1980s.

Let's recall some pointers from the crash intro to Deep learning.

### Watch following videos now for 12 minutes for the fastest introduction to RNNs and LSTMs

[Udacity: Deep Learning by Vincent Vanhoucke - Recurrent Neural network](https://youtu.be/LTbLxm6YIjE?list=PLAwxTw4SYaPn_OWPFT9ulXLuQrImzHfOV)

#### Recurrent neural network

![Recurrent neural network](http://colah.github.io/posts/2015-08-Understanding-LSTMs/img/RNN-unrolled.png)
<http://colah.github.io/posts/2015-08-Understanding-LSTMs/>

<http://karpathy.github.io/2015/05/21/rnn-effectiveness/>
\*\*\*

------------------------------------------------------------------------

##### LSTM - Long short term memory

![LSTM](http://colah.github.io/posts/2015-08-Understanding-LSTMs/img/LSTM3-chain.png)

------------------------------------------------------------------------

##### GRU - Gated recurrent unit

![Gated Recurrent unit](http://colah.github.io/posts/2015-08-Understanding-LSTMs/img/LSTM3-var-GRU.png) <http://arxiv.org/pdf/1406.1078v3.pdf>

### Training a Recurrent Neural Network

<img src="http://i.imgur.com/iPGNMvZ.jpg">

We can train an RNN using backpropagation with a minor twist: since RNN neurons with different states over time can be "unrolled" (i.e., are analogous) to a sequence of neurons with the "remember" weight linking directly forward from (t) to (t+1), we can backpropagate through time as well as the physical layers of the network.

This is, in fact, called **Backpropagation Through Time** (BPTT)

The idea is sound but -- since it creates patterns similar to very deep networks -- it suffers from the same challenges: \* Vanishing gradient \* Exploding gradient \* Saturation \* etc.

i.e., many of the same problems with early deep feed-forward networks having lots of weights.

10 steps back in time for a single layer is a not as bad as 10 layers (since there are fewer connections and, hence, weights) but it does get expensive.

------------------------------------------------------------------------

> **ASIDE: Hierarchical and Recursive Networks, Bidirectional RNN**

> Network topologies can be built to reflect the relative structure of the data we are modeling. E.g., for natural language, grammar constraints mean that both hierarchy and (limited) recursion may allow a physically smaller model to achieve more effective capacity.

> A bi-directional RNN includes values from previous and subsequent time steps. This is less strange than it sounds at first: after all, in many problems, such as sentence translation (where BiRNNs are very popular) we usually have the entire source sequence at one time. In that case, a BiDiRNN is really just saying that both prior and subsequent words can influence the interpretation of each word, something we humans take for granted.

> Recent versions of neural net libraries have support for bidirectional networks, although you may need to write (or locate) a little code yourself if you want to experiment with hierarchical networks.

------------------------------------------------------------------------

Long Short-Term Memory (LSTM)
-----------------------------

"Pure" RNNs were never very successful. Sepp Hochreiter and Jürgen Schmidhuber (1997) made a game-changing contribution with the publication of the Long Short-Term Memory unit. How game changing? It's effectively state of the art today.

<sup>(Credit and much thanks to Chris Olah, http://colah.github.io/about.html, Research Scientist at Google Brain, for publishing the following excellent diagrams!)</sup>

*In the following diagrams, pay close attention that the output value is "split" for graphical purposes -- so the two *h\* arrows/signals coming out are the same signal.\*

**RNN Cell:** <img src="http://i.imgur.com/DfYyKaN.png" width=600>

**LSTM Cell:**

<img src="http://i.imgur.com/pQiMLjG.png" width=600>

An LSTM unit is a neuron with some bonus features: \* Cell state propagated across time \* Input, Output, Forget gates \* Learns retention/discard of cell state \* Admixture of new data \* Output partly distinct from state \* Use of **addition** (not multiplication) to combine input and cell state allows state to propagate unimpeded across time (addition of gradient)

------------------------------------------------------------------------

> **ASIDE: Variations on LSTM**

> ... include "peephole" where gate functions have direct access to cell state; convolutional; and bidirectional, where we can "cheat" by letting neurons learn from future time steps and not just previous time steps.

------------------------------------------------------------------------

Slow down ... exactly what's getting added to where? For a step-by-step walk through, read Chris Olah's full post http://colah.github.io/posts/2015-08-Understanding-LSTMs/

### Do LSTMs Work Reasonably Well?

**Yes!** These architectures are in production (2017) for deep-learning-enabled products at Baidu, Google, Microsoft, Apple, and elsewhere. They are used to solve problems in time series analysis, speech recognition and generation, connected handwriting, grammar, music, and robot control systems.

### Let's Code an LSTM Variant of our Sequence Lab

(this great demo example courtesy of Jason Brownlee: http://machinelearningmastery.com/understanding-stateful-lstm-recurrent-neural-networks-python-keras/)

``` python
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
```

>     ('ABC', '->', 'D')
>     ('BCD', '->', 'E')
>     ('CDE', '->', 'F')
>     ('DEF', '->', 'G')
>     ('EFG', '->', 'H')
>     ('FGH', '->', 'I')
>     ('GHI', '->', 'J')
>     ('HIJ', '->', 'K')
>     ('IJK', '->', 'L')
>     ('JKL', '->', 'M')
>     ('KLM', '->', 'N')
>     ('LMN', '->', 'O')
>     ('MNO', '->', 'P')
>     ('NOP', '->', 'Q')
>     ('OPQ', '->', 'R')
>     ('PQR', '->', 'S')
>     ('QRS', '->', 'T')
>     ('RST', '->', 'U')
>     ('STU', '->', 'V')
>     ('TUV', '->', 'W')
>     ('UVW', '->', 'X')
>     ('VWX', '->', 'Y')
>     ('WXY', '->', 'Z')
>     Epoch 1/400
>     0s - loss: 3.2743 - acc: 0.0435
>     Epoch 2/400
>     0s - loss: 3.2586 - acc: 0.0000e+00
>     Epoch 3/400
>     0s - loss: 3.2516 - acc: 0.0435
>     Epoch 4/400
>     0s - loss: 3.2441 - acc: 0.0435
>     Epoch 5/400
>     0s - loss: 3.2361 - acc: 0.0435
>     Epoch 6/400
>     0s - loss: 3.2288 - acc: 0.0435
>     Epoch 7/400
>     0s - loss: 3.2203 - acc: 0.0435
>     Epoch 8/400
>     0s - loss: 3.2117 - acc: 0.0435
>     Epoch 9/400
>     0s - loss: 3.2021 - acc: 0.0435
>     Epoch 10/400
>     0s - loss: 3.1926 - acc: 0.0435
>     Epoch 11/400
>     0s - loss: 3.1793 - acc: 0.0000e+00
>     Epoch 12/400
>     0s - loss: 3.1653 - acc: 0.0000e+00
>     Epoch 13/400
>     0s - loss: 3.1483 - acc: 0.0435
>     Epoch 14/400
>     0s - loss: 3.1319 - acc: 0.0435
>     Epoch 15/400
>     0s - loss: 3.1135 - acc: 0.0435
>     Epoch 16/400
>     0s - loss: 3.0969 - acc: 0.0435
>     Epoch 17/400
>     0s - loss: 3.0802 - acc: 0.0435
>     Epoch 18/400
>     0s - loss: 3.0600 - acc: 0.0435
>     Epoch 19/400
>     0s - loss: 3.0419 - acc: 0.0435
>     Epoch 20/400
>     0s - loss: 3.0283 - acc: 0.0435
>     Epoch 21/400
>     0s - loss: 3.0073 - acc: 0.0435
>     Epoch 22/400
>     0s - loss: 2.9810 - acc: 0.0435
>     Epoch 23/400
>     0s - loss: 2.9582 - acc: 0.0870
>     Epoch 24/400
>     0s - loss: 2.9321 - acc: 0.0870
>     Epoch 25/400
>     0s - loss: 2.9076 - acc: 0.1304
>     Epoch 26/400
>     0s - loss: 2.8777 - acc: 0.1304
>     Epoch 27/400
>     0s - loss: 2.8500 - acc: 0.1304
>     Epoch 28/400
>     0s - loss: 2.8169 - acc: 0.0870
>     Epoch 29/400
>     0s - loss: 2.7848 - acc: 0.0870
>     Epoch 30/400
>     0s - loss: 2.7524 - acc: 0.0870
>     Epoch 31/400
>     0s - loss: 2.7157 - acc: 0.1304
>     Epoch 32/400
>     0s - loss: 2.6844 - acc: 0.0870
>     Epoch 33/400
>     0s - loss: 2.6552 - acc: 0.0435
>     Epoch 34/400
>     0s - loss: 2.6242 - acc: 0.0870
>     Epoch 35/400
>     0s - loss: 2.5939 - acc: 0.0870
>     Epoch 36/400
>     0s - loss: 2.5631 - acc: 0.1739
>     Epoch 37/400
>     0s - loss: 2.5370 - acc: 0.1304
>     Epoch 38/400
>     0s - loss: 2.5116 - acc: 0.0435
>     Epoch 39/400
>     0s - loss: 2.4850 - acc: 0.0870
>     Epoch 40/400
>     0s - loss: 2.4573 - acc: 0.0870
>     Epoch 41/400
>     0s - loss: 2.4295 - acc: 0.1304
>     Epoch 42/400
>     0s - loss: 2.4097 - acc: 0.0870
>     Epoch 43/400
>     0s - loss: 2.3830 - acc: 0.0870
>     Epoch 44/400
>     0s - loss: 2.3630 - acc: 0.1739
>     Epoch 45/400
>     0s - loss: 2.3409 - acc: 0.1739
>     Epoch 46/400
>     0s - loss: 2.3123 - acc: 0.1739
>     Epoch 47/400
>     0s - loss: 2.2926 - acc: 0.2174
>     Epoch 48/400
>     0s - loss: 2.2692 - acc: 0.2174
>     Epoch 49/400
>     0s - loss: 2.2514 - acc: 0.1739
>     Epoch 50/400
>     0s - loss: 2.2240 - acc: 0.2174
>     Epoch 51/400
>     0s - loss: 2.2059 - acc: 0.3043
>     Epoch 52/400
>     0s - loss: 2.1808 - acc: 0.1739
>     Epoch 53/400
>     0s - loss: 2.1677 - acc: 0.2609
>     Epoch 54/400
>     0s - loss: 2.1425 - acc: 0.2174
>     Epoch 55/400
>     0s - loss: 2.1271 - acc: 0.2609
>     Epoch 56/400
>     0s - loss: 2.1025 - acc: 0.2174
>     Epoch 57/400
>     0s - loss: 2.0917 - acc: 0.1739
>     Epoch 58/400
>     0s - loss: 2.0679 - acc: 0.3043
>     Epoch 59/400
>     0s - loss: 2.0529 - acc: 0.2609
>     Epoch 60/400
>     0s - loss: 2.0383 - acc: 0.3043
>     Epoch 61/400
>     0s - loss: 2.0245 - acc: 0.2609
>     Epoch 62/400
>     0s - loss: 1.9908 - acc: 0.2609
>     Epoch 63/400
>     0s - loss: 1.9841 - acc: 0.2174
>     Epoch 64/400
>     0s - loss: 1.9660 - acc: 0.2609
>     Epoch 65/400
>     0s - loss: 1.9557 - acc: 0.3043
>     Epoch 66/400
>     0s - loss: 1.9353 - acc: 0.3478
>     Epoch 67/400
>     0s - loss: 1.9223 - acc: 0.3043
>     Epoch 68/400
>     0s - loss: 1.9027 - acc: 0.3913
>     Epoch 69/400
>     0s - loss: 1.8819 - acc: 0.4783
>     Epoch 70/400
>     0s - loss: 1.8755 - acc: 0.4348
>     Epoch 71/400
>     0s - loss: 1.8581 - acc: 0.3913
>     Epoch 72/400
>     0s - loss: 1.8466 - acc: 0.3478
>     Epoch 73/400
>     0s - loss: 1.8299 - acc: 0.3478
>     Epoch 74/400
>     0s - loss: 1.8144 - acc: 0.4348
>     Epoch 75/400
>     0s - loss: 1.7991 - acc: 0.4783
>     Epoch 76/400
>     0s - loss: 1.7894 - acc: 0.4348
>     Epoch 77/400
>     0s - loss: 1.7762 - acc: 0.3913
>     Epoch 78/400
>     0s - loss: 1.7623 - acc: 0.3913
>     Epoch 79/400
>     0s - loss: 1.7544 - acc: 0.5217
>     Epoch 80/400
>     0s - loss: 1.7341 - acc: 0.6522
>     Epoch 81/400
>     0s - loss: 1.7215 - acc: 0.4783
>     Epoch 82/400
>     0s - loss: 1.7217 - acc: 0.4348
>     Epoch 83/400
>     0s - loss: 1.7050 - acc: 0.4348
>     Epoch 84/400
>     0s - loss: 1.6934 - acc: 0.3913
>     Epoch 85/400
>     0s - loss: 1.6764 - acc: 0.5217
>     Epoch 86/400
>     0s - loss: 1.6713 - acc: 0.4783
>     Epoch 87/400
>     0s - loss: 1.6584 - acc: 0.4348
>     Epoch 88/400
>     0s - loss: 1.6584 - acc: 0.4783
>     Epoch 89/400
>     0s - loss: 1.6426 - acc: 0.6522
>     Epoch 90/400
>     0s - loss: 1.6340 - acc: 0.5652
>     Epoch 91/400
>     0s - loss: 1.6225 - acc: 0.5652
>     Epoch 92/400
>     0s - loss: 1.6102 - acc: 0.6087
>     Epoch 93/400
>     0s - loss: 1.6009 - acc: 0.6522
>     Epoch 94/400
>     0s - loss: 1.6024 - acc: 0.4783
>     Epoch 95/400
>     0s - loss: 1.5845 - acc: 0.5652
>     Epoch 96/400
>     0s - loss: 1.5774 - acc: 0.6087
>     Epoch 97/400
>     0s - loss: 1.5728 - acc: 0.5217
>     Epoch 98/400
>     0s - loss: 1.5606 - acc: 0.6522
>     Epoch 99/400
>     0s - loss: 1.5581 - acc: 0.6087
>     Epoch 100/400
>     0s - loss: 1.5504 - acc: 0.5217
>     Epoch 101/400
>     0s - loss: 1.5345 - acc: 0.6522
>     Epoch 102/400
>     0s - loss: 1.5317 - acc: 0.6087
>     Epoch 103/400
>     0s - loss: 1.5255 - acc: 0.6522
>     Epoch 104/400
>     0s - loss: 1.5155 - acc: 0.6087
>     Epoch 105/400
>     0s - loss: 1.5071 - acc: 0.6087
>     Epoch 106/400
>     0s - loss: 1.4973 - acc: 0.6522
>     Epoch 107/400
>     0s - loss: 1.4914 - acc: 0.6087
>     Epoch 108/400
>     0s - loss: 1.4756 - acc: 0.6957
>     Epoch 109/400
>     0s - loss: 1.4717 - acc: 0.6522
>     Epoch 110/400
>     0s - loss: 1.4654 - acc: 0.6522
>     Epoch 111/400
>     0s - loss: 1.4631 - acc: 0.6522
>     Epoch 112/400
>     0s - loss: 1.4436 - acc: 0.6087
>     Epoch 113/400
>     0s - loss: 1.4416 - acc: 0.6087
>     Epoch 114/400
>     0s - loss: 1.4367 - acc: 0.6522
>     Epoch 115/400
>     0s - loss: 1.4240 - acc: 0.6522
>     Epoch 116/400
>     0s - loss: 1.4160 - acc: 0.6522
>     Epoch 117/400
>     0s - loss: 1.4207 - acc: 0.7391
>     Epoch 118/400
>     0s - loss: 1.4087 - acc: 0.6957
>     Epoch 119/400
>     0s - loss: 1.3908 - acc: 0.8261
>     Epoch 120/400
>     0s - loss: 1.4012 - acc: 0.6957
>     Epoch 121/400
>     0s - loss: 1.3864 - acc: 0.7391
>     Epoch 122/400
>     0s - loss: 1.3738 - acc: 0.7391
>     Epoch 123/400
>     0s - loss: 1.3751 - acc: 0.7391
>     Epoch 124/400
>     0s - loss: 1.3631 - acc: 0.7391
>     Epoch 125/400
>     0s - loss: 1.3601 - acc: 0.6957
>     Epoch 126/400
>     0s - loss: 1.3559 - acc: 0.7391
>     Epoch 127/400
>     0s - loss: 1.3523 - acc: 0.6522
>     Epoch 128/400
>     0s - loss: 1.3407 - acc: 0.8261
>     Epoch 129/400
>     0s - loss: 1.3344 - acc: 0.7826
>     Epoch 130/400
>     0s - loss: 1.3207 - acc: 0.7391
>     Epoch 131/400
>     0s - loss: 1.3185 - acc: 0.6522
>     Epoch 132/400
>     0s - loss: 1.3149 - acc: 0.7391
>     Epoch 133/400
>     0s - loss: 1.3060 - acc: 0.7826
>     Epoch 134/400
>     0s - loss: 1.2958 - acc: 0.8261
>     Epoch 135/400
>     0s - loss: 1.2924 - acc: 0.7826
>     Epoch 136/400
>     0s - loss: 1.2810 - acc: 0.7826
>     Epoch 137/400
>     0s - loss: 1.2812 - acc: 0.7826
>     Epoch 138/400
>     0s - loss: 1.2766 - acc: 0.7826
>     Epoch 139/400
>     0s - loss: 1.2774 - acc: 0.7391
>     Epoch 140/400
>     0s - loss: 1.2650 - acc: 0.7826
>     Epoch 141/400
>     0s - loss: 1.2537 - acc: 0.6957
>     Epoch 142/400
>     0s - loss: 1.2554 - acc: 0.7391
>     Epoch 143/400
>     0s - loss: 1.2400 - acc: 0.7826
>     Epoch 144/400
>     0s - loss: 1.2441 - acc: 0.7826
>     Epoch 145/400
>     0s - loss: 1.2429 - acc: 0.7391
>     Epoch 146/400
>     0s - loss: 1.2346 - acc: 0.7826
>     Epoch 147/400
>     0s - loss: 1.2283 - acc: 0.7391
>     Epoch 148/400
>     0s - loss: 1.2205 - acc: 0.7826
>     Epoch 149/400
>     0s - loss: 1.2170 - acc: 0.8261
>     Epoch 150/400
>     0s - loss: 1.2034 - acc: 0.8261
>     Epoch 151/400
>     0s - loss: 1.1980 - acc: 0.8696
>     Epoch 152/400
>     0s - loss: 1.1997 - acc: 0.7826
>     Epoch 153/400
>     0s - loss: 1.1896 - acc: 0.8261
>     Epoch 154/400
>     0s - loss: 1.1875 - acc: 0.7826
>     Epoch 155/400
>     0s - loss: 1.1804 - acc: 0.7826
>     Epoch 156/400
>     0s - loss: 1.1722 - acc: 0.7391
>     Epoch 157/400
>     0s - loss: 1.1683 - acc: 0.7826
>     Epoch 158/400
>     0s - loss: 1.1695 - acc: 0.7826
>     Epoch 159/400
>     0s - loss: 1.1658 - acc: 0.8696
>     Epoch 160/400
>     0s - loss: 1.1545 - acc: 0.8696
>     Epoch 161/400
>     0s - loss: 1.1484 - acc: 0.7826
>     Epoch 162/400
>     0s - loss: 1.1481 - acc: 0.8261
>     Epoch 163/400
>     0s - loss: 1.1380 - acc: 0.9130
>     Epoch 164/400
>     0s - loss: 1.1335 - acc: 0.9130
>     Epoch 165/400
>     0s - loss: 1.1334 - acc: 0.8696
>     Epoch 166/400
>     0s - loss: 1.1307 - acc: 0.8261
>     Epoch 167/400
>     0s - loss: 1.1232 - acc: 0.8261
>     Epoch 168/400
>     0s - loss: 1.1192 - acc: 0.8261
>     Epoch 169/400
>     0s - loss: 1.1100 - acc: 0.8696
>     Epoch 170/400
>     0s - loss: 1.1034 - acc: 0.9130
>     Epoch 171/400
>     0s - loss: 1.1050 - acc: 0.8261
>     Epoch 172/400
>     0s - loss: 1.0945 - acc: 0.9130
>     Epoch 173/400
>     0s - loss: 1.0874 - acc: 0.8696
>     Epoch 174/400
>     0s - loss: 1.0870 - acc: 0.9130
>     Epoch 175/400
>     0s - loss: 1.0844 - acc: 0.8696
>     Epoch 176/400
>     0s - loss: 1.0787 - acc: 0.7826
>     Epoch 177/400
>     0s - loss: 1.0700 - acc: 0.9130
>     Epoch 178/400
>     0s - loss: 1.0641 - acc: 0.7826
>     Epoch 179/400
>     0s - loss: 1.0618 - acc: 0.8696
>     Epoch 180/400
>     0s - loss: 1.0549 - acc: 0.9130
>     Epoch 181/400
>     0s - loss: 1.0520 - acc: 0.8696
>     Epoch 182/400
>     0s - loss: 1.0470 - acc: 0.9130
>     Epoch 183/400
>     0s - loss: 1.0408 - acc: 0.8261
>     Epoch 184/400
>     0s - loss: 1.0369 - acc: 0.8261
>     Epoch 185/400
>     0s - loss: 1.0347 - acc: 0.8261
>     Epoch 186/400
>     0s - loss: 1.0267 - acc: 0.9130
>     Epoch 187/400
>     0s - loss: 1.0250 - acc: 0.8696
>     Epoch 188/400
>     0s - loss: 1.0177 - acc: 0.9130
>     Epoch 189/400
>     0s - loss: 1.0121 - acc: 0.8696
>     Epoch 190/400
>     0s - loss: 1.0109 - acc: 0.9130
>     Epoch 191/400
>     0s - loss: 1.0108 - acc: 0.9130
>     Epoch 192/400
>     0s - loss: 0.9983 - acc: 0.8696
>     Epoch 193/400
>     0s - loss: 1.0024 - acc: 0.9130
>     Epoch 194/400
>     0s - loss: 0.9909 - acc: 0.9565
>     Epoch 195/400
>     0s - loss: 0.9919 - acc: 0.9130
>     Epoch 196/400
>     0s - loss: 0.9900 - acc: 0.8696
>     Epoch 197/400
>     0s - loss: 0.9831 - acc: 0.9130
>     Epoch 198/400
>     0s - loss: 0.9760 - acc: 0.9130
>     Epoch 199/400
>     0s - loss: 0.9719 - acc: 0.8261
>     Epoch 200/400
>     0s - loss: 0.9693 - acc: 0.8696
>     Epoch 201/400
>     0s - loss: 0.9626 - acc: 0.8696
>     Epoch 202/400
>     0s - loss: 0.9559 - acc: 0.9130
>     Epoch 203/400
>     0s - loss: 0.9547 - acc: 0.9130
>     Epoch 204/400
>     0s - loss: 0.9546 - acc: 0.8261
>     Epoch 205/400
>     0s - loss: 0.9417 - acc: 0.8696
>     Epoch 206/400
>     0s - loss: 0.9437 - acc: 0.8696
>     Epoch 207/400
>     0s - loss: 0.9479 - acc: 0.9130
>     Epoch 208/400
>     0s - loss: 0.9315 - acc: 0.9130
>     Epoch 209/400
>     0s - loss: 0.9294 - acc: 0.8696
>     Epoch 210/400
>     0s - loss: 0.9275 - acc: 0.9130
>     Epoch 211/400
>     0s - loss: 0.9209 - acc: 0.9130
>     Epoch 212/400
>     0s - loss: 0.9140 - acc: 0.8261
>     Epoch 213/400
>     0s - loss: 0.9152 - acc: 0.8261
>     Epoch 214/400
>     0s - loss: 0.9085 - acc: 0.9565
>     Epoch 215/400
>     0s - loss: 0.9109 - acc: 0.9130
>     Epoch 216/400
>     0s - loss: 0.8986 - acc: 0.9565
>     Epoch 217/400
>     0s - loss: 0.9070 - acc: 0.8696
>     Epoch 218/400
>     0s - loss: 0.8911 - acc: 0.8696
>     Epoch 219/400
>     0s - loss: 0.8951 - acc: 0.8261
>     Epoch 220/400
>     0s - loss: 0.8834 - acc: 0.9130
>     Epoch 221/400
>     0s - loss: 0.8808 - acc: 0.9130
>     Epoch 222/400
>     0s - loss: 0.8745 - acc: 0.8261
>     Epoch 223/400
>     0s - loss: 0.8695 - acc: 0.8696
>     Epoch 224/400
>     0s - loss: 0.8679 - acc: 0.8696
>     Epoch 225/400
>     0s - loss: 0.8731 - acc: 0.8696
>     Epoch 226/400
>     0s - loss: 0.8598 - acc: 0.8696
>     Epoch 227/400
>     0s - loss: 0.8542 - acc: 0.9565
>     Epoch 228/400
>     0s - loss: 0.8545 - acc: 0.9565
>     Epoch 229/400
>     0s - loss: 0.8501 - acc: 0.9130
>     Epoch 230/400
>     0s - loss: 0.8572 - acc: 0.9565
>     Epoch 231/400
>     0s - loss: 0.8541 - acc: 0.8696
>     Epoch 232/400
>     0s - loss: 0.8435 - acc: 0.9130
>     Epoch 233/400
>     0s - loss: 0.8386 - acc: 0.9130
>     Epoch 234/400
>     0s - loss: 0.8345 - acc: 0.8261
>     Epoch 235/400
>     0s - loss: 0.8275 - acc: 0.9130
>     Epoch 236/400
>     0s - loss: 0.8183 - acc: 0.9565
>     Epoch 237/400
>     0s - loss: 0.8159 - acc: 0.9130
>     Epoch 238/400
>     0s - loss: 0.8180 - acc: 0.9130
>     Epoch 239/400
>     0s - loss: 0.8133 - acc: 0.9565
>     Epoch 240/400
>     0s - loss: 0.8061 - acc: 0.9565
>     Epoch 241/400
>     0s - loss: 0.8066 - acc: 0.8696
>     Epoch 242/400
>     0s - loss: 0.8010 - acc: 0.9565
>     Epoch 243/400
>     0s - loss: 0.7944 - acc: 0.8696
>     Epoch 244/400
>     0s - loss: 0.7976 - acc: 0.8696
>     Epoch 245/400
>     0s - loss: 0.7874 - acc: 0.8696
>     Epoch 246/400
>     0s - loss: 0.7844 - acc: 0.9130
>     Epoch 247/400
>     0s - loss: 0.7784 - acc: 0.9130
>     Epoch 248/400
>     0s - loss: 0.7813 - acc: 0.8696
>     Epoch 249/400
>     0s - loss: 0.7746 - acc: 0.8696
>     Epoch 250/400
>     0s - loss: 0.7759 - acc: 0.8696
>     Epoch 251/400
>     0s - loss: 0.7639 - acc: 0.9130
>     Epoch 252/400
>     0s - loss: 0.7679 - acc: 0.9130
>     Epoch 253/400
>     0s - loss: 0.7583 - acc: 0.9565
>     Epoch 254/400
>     0s - loss: 0.7524 - acc: 0.9565
>     Epoch 255/400
>     0s - loss: 0.7491 - acc: 0.9130
>     Epoch 256/400
>     0s - loss: 0.7545 - acc: 0.9565
>     Epoch 257/400
>     0s - loss: 0.7560 - acc: 0.9565
>     Epoch 258/400
>     0s - loss: 0.7480 - acc: 0.8696
>     Epoch 259/400
>     0s - loss: 0.7346 - acc: 0.9130
>     Epoch 260/400
>     0s - loss: 0.7394 - acc: 0.9565
>     Epoch 261/400
>     0s - loss: 0.7319 - acc: 0.9565
>     Epoch 262/400
>     0s - loss: 0.7261 - acc: 0.9130
>     Epoch 263/400
>     0s - loss: 0.7262 - acc: 0.9565
>     Epoch 264/400
>     0s - loss: 0.7237 - acc: 0.9565
>     Epoch 265/400
>     0s - loss: 0.7194 - acc: 0.9565
>     Epoch 266/400
>     0s - loss: 0.7169 - acc: 0.9565
>     Epoch 267/400
>     0s - loss: 0.7194 - acc: 0.9130
>     Epoch 268/400
>     0s - loss: 0.7137 - acc: 0.9130
>     Epoch 269/400
>     0s - loss: 0.7024 - acc: 0.9130
>     Epoch 270/400
>     0s - loss: 0.6985 - acc: 1.0000
>     Epoch 271/400
>     0s - loss: 0.6999 - acc: 0.9130
>     Epoch 272/400
>     0s - loss: 0.6928 - acc: 0.9130
>     Epoch 273/400
>     0s - loss: 0.6908 - acc: 0.9130
>     Epoch 274/400
>     0s - loss: 0.6869 - acc: 0.9565
>     Epoch 275/400
>     0s - loss: 0.6850 - acc: 0.9565
>     Epoch 276/400
>     0s - loss: 0.6853 - acc: 0.9130
>     Epoch 277/400
>     0s - loss: 0.6771 - acc: 0.9565
>     Epoch 278/400
>     0s - loss: 0.6819 - acc: 0.9565
>     Epoch 279/400
>     0s - loss: 0.6730 - acc: 0.9130
>     Epoch 280/400
>     0s - loss: 0.6750 - acc: 0.9565
>     Epoch 281/400
>     0s - loss: 0.6668 - acc: 0.9565
>     Epoch 282/400
>     0s - loss: 0.6681 - acc: 0.9565
>     Epoch 283/400
>     0s - loss: 0.6619 - acc: 0.9130
>     Epoch 284/400
>     0s - loss: 0.6566 - acc: 0.9130
>     Epoch 285/400
>     0s - loss: 0.6577 - acc: 0.9565
>     Epoch 286/400
>     0s - loss: 0.6541 - acc: 0.9130
>     Epoch 287/400
>     0s - loss: 0.6557 - acc: 0.9130
>     Epoch 288/400
>     0s - loss: 0.6504 - acc: 0.9565
>     Epoch 289/400
>     0s - loss: 0.6478 - acc: 0.9565
>     Epoch 290/400
>     0s - loss: 0.6401 - acc: 0.9130
>     Epoch 291/400
>     0s - loss: 0.6375 - acc: 0.9130
>     Epoch 292/400
>     0s - loss: 0.6381 - acc: 0.9130
>     Epoch 293/400
>     0s - loss: 0.6302 - acc: 0.9130
>     Epoch 294/400
>     0s - loss: 0.6268 - acc: 0.9565
>     Epoch 295/400
>     0s - loss: 0.6243 - acc: 0.9565
>     Epoch 296/400
>     0s - loss: 0.6259 - acc: 1.0000
>     Epoch 297/400
>     0s - loss: 0.6258 - acc: 0.9130
>     Epoch 298/400
>     0s - loss: 0.6176 - acc: 0.9565
>     Epoch 299/400
>     0s - loss: 0.6166 - acc: 0.9565
>     Epoch 300/400
>     0s - loss: 0.6122 - acc: 0.9565
>     Epoch 301/400
>     0s - loss: 0.6079 - acc: 1.0000
>     Epoch 302/400
>     0s - loss: 0.6090 - acc: 0.9130
>     Epoch 303/400
>     0s - loss: 0.6068 - acc: 1.0000
>     Epoch 304/400
>     0s - loss: 0.6046 - acc: 0.9130
>     Epoch 305/400
>     0s - loss: 0.5953 - acc: 0.9130
>     Epoch 306/400
>     0s - loss: 0.5975 - acc: 0.9565
>     Epoch 307/400
>     0s - loss: 0.5980 - acc: 0.9130
>     Epoch 308/400
>     0s - loss: 0.5904 - acc: 0.9130
>     Epoch 309/400
>     0s - loss: 0.5873 - acc: 0.9565
>     Epoch 310/400
>     0s - loss: 0.5882 - acc: 0.9130
>     Epoch 311/400
>     0s - loss: 0.5809 - acc: 0.9565
>     Epoch 312/400
>     0s - loss: 0.5785 - acc: 1.0000
>     Epoch 313/400
>     0s - loss: 0.5739 - acc: 1.0000
>     Epoch 314/400
>     0s - loss: 0.5763 - acc: 0.9130
>     Epoch 315/400
>     0s - loss: 0.5720 - acc: 0.9130
>     Epoch 316/400
>     0s - loss: 0.5694 - acc: 1.0000
>     Epoch 317/400
>     0s - loss: 0.5692 - acc: 0.9565
>     Epoch 318/400
>     0s - loss: 0.5673 - acc: 0.9565
>     Epoch 319/400
>     0s - loss: 0.5622 - acc: 0.9565
>     Epoch 320/400
>     0s - loss: 0.5620 - acc: 0.9565
>     Epoch 321/400
>     0s - loss: 0.5639 - acc: 0.9565
>     Epoch 322/400
>     0s - loss: 0.5637 - acc: 1.0000
>     Epoch 323/400
>     0s - loss: 0.5539 - acc: 0.9130
>     Epoch 324/400
>     0s - loss: 0.5500 - acc: 0.9130
>     Epoch 325/400
>     0s - loss: 0.5503 - acc: 0.9565
>     Epoch 326/400
>     0s - loss: 0.5500 - acc: 1.0000
>     Epoch 327/400
>     0s - loss: 0.5457 - acc: 0.9130
>     Epoch 328/400
>     0s - loss: 0.5495 - acc: 0.9130
>     Epoch 329/400
>     0s - loss: 0.5386 - acc: 0.9565
>     Epoch 330/400
>     0s - loss: 0.5372 - acc: 0.9565
>     Epoch 331/400
>     0s - loss: 0.5326 - acc: 1.0000
>     Epoch 332/400
>     0s - loss: 0.5325 - acc: 0.9565
>     Epoch 333/400
>     0s - loss: 0.5314 - acc: 1.0000
>     Epoch 334/400
>     0s - loss: 0.5308 - acc: 1.0000
>     Epoch 335/400
>     0s - loss: 0.5222 - acc: 1.0000
>     Epoch 336/400
>     0s - loss: 0.5188 - acc: 1.0000
>     Epoch 337/400
>     0s - loss: 0.5223 - acc: 0.9565
>     Epoch 338/400
>     0s - loss: 0.5160 - acc: 0.9130
>     Epoch 339/400
>     0s - loss: 0.5225 - acc: 0.9130
>     Epoch 340/400
>     0s - loss: 0.5185 - acc: 0.9565
>     Epoch 341/400
>     0s - loss: 0.5132 - acc: 0.9565
>     Epoch 342/400
>     0s - loss: 0.5172 - acc: 1.0000
>     Epoch 343/400
>     0s - loss: 0.5129 - acc: 1.0000
>     Epoch 344/400
>     0s - loss: 0.5087 - acc: 0.9565
>     Epoch 345/400
>     0s - loss: 0.5109 - acc: 0.9130
>     Epoch 346/400
>     0s - loss: 0.5060 - acc: 0.9565
>     Epoch 347/400
>     0s - loss: 0.5037 - acc: 0.9565
>     Epoch 348/400
>     0s - loss: 0.5007 - acc: 0.9565
>     Epoch 349/400
>     0s - loss: 0.4991 - acc: 0.9565
>     Epoch 350/400
>     0s - loss: 0.4937 - acc: 0.9565
>     Epoch 351/400
>     0s - loss: 0.4909 - acc: 0.9565
>     Epoch 352/400
>     0s - loss: 0.4932 - acc: 1.0000
>     Epoch 353/400
>     0s - loss: 0.4860 - acc: 0.9565
>     Epoch 354/400
>     0s - loss: 0.4910 - acc: 0.9565
>     Epoch 355/400
>     0s - loss: 0.4851 - acc: 1.0000
>     Epoch 356/400
>     0s - loss: 0.4872 - acc: 0.9130
>     Epoch 357/400
>     0s - loss: 0.4806 - acc: 1.0000
>     Epoch 358/400
>     0s - loss: 0.4778 - acc: 0.9565
>     Epoch 359/400
>     0s - loss: 0.4817 - acc: 1.0000
>     Epoch 360/400
>     0s - loss: 0.4736 - acc: 0.9565
>     Epoch 361/400
>     0s - loss: 0.4748 - acc: 1.0000
>     Epoch 362/400
>     0s - loss: 0.4734 - acc: 1.0000
>     Epoch 363/400
>     0s - loss: 0.4729 - acc: 1.0000
>     Epoch 364/400
>     0s - loss: 0.4739 - acc: 0.9565
>     Epoch 365/400
>     0s - loss: 0.4714 - acc: 0.9565
>     Epoch 366/400
>     0s - loss: 0.4665 - acc: 0.9130
>     Epoch 367/400
>     0s - loss: 0.4648 - acc: 1.0000
>     Epoch 368/400
>     0s - loss: 0.4696 - acc: 0.9565
>     Epoch 369/400
>     0s - loss: 0.4650 - acc: 0.9130
>     Epoch 370/400
>     0s - loss: 0.4579 - acc: 0.9565
>     Epoch 371/400
>     0s - loss: 0.4562 - acc: 1.0000
>     Epoch 372/400
>     0s - loss: 0.4562 - acc: 1.0000
>     Epoch 373/400
>     0s - loss: 0.4575 - acc: 0.9565
>     Epoch 374/400
>     0s - loss: 0.4541 - acc: 0.9130
>     Epoch 375/400
>     0s - loss: 0.4528 - acc: 1.0000
>     Epoch 376/400
>     0s - loss: 0.4483 - acc: 1.0000
>     Epoch 377/400
>     0s - loss: 0.4477 - acc: 1.0000
>     Epoch 378/400
>     0s - loss: 0.4482 - acc: 0.9565
>     Epoch 379/400
>     0s - loss: 0.4422 - acc: 0.9565
>     Epoch 380/400
>     0s - loss: 0.4400 - acc: 0.9565
>     Epoch 381/400
>     0s - loss: 0.4416 - acc: 1.0000
>     Epoch 382/400
>     0s - loss: 0.4365 - acc: 1.0000
>     Epoch 383/400
>     0s - loss: 0.4410 - acc: 1.0000
>     Epoch 384/400
>     0s - loss: 0.4383 - acc: 1.0000
>     Epoch 385/400
>     0s - loss: 0.4348 - acc: 0.9565
>     Epoch 386/400
>     0s - loss: 0.4353 - acc: 1.0000
>     Epoch 387/400
>     0s - loss: 0.4314 - acc: 0.9130
>     Epoch 388/400
>     0s - loss: 0.4381 - acc: 0.9565
>     Epoch 389/400
>     0s - loss: 0.4314 - acc: 1.0000
>     Epoch 390/400
>     0s - loss: 0.4319 - acc: 0.9130
>     Epoch 391/400
>     0s - loss: 0.4292 - acc: 0.9565
>     Epoch 392/400
>     0s - loss: 0.4490 - acc: 0.9565
>     Epoch 393/400
>     0s - loss: 0.4347 - acc: 0.9565
>     Epoch 394/400
>     0s - loss: 0.4517 - acc: 0.9565
>     Epoch 395/400
>     0s - loss: 0.4294 - acc: 0.9130
>     Epoch 396/400
>     0s - loss: 0.4178 - acc: 0.9565
>     Epoch 397/400
>     0s - loss: 0.4142 - acc: 0.9565
>     Epoch 398/400
>     0s - loss: 0.4175 - acc: 1.0000
>     Epoch 399/400
>     0s - loss: 0.4123 - acc: 1.0000
>     Epoch 400/400
>     0s - loss: 0.4123 - acc: 0.9565
>     23/23 [==============================] - 0s
>     Model Accuracy: 100.00%
>     (['A', 'B', 'C'], '->', 'D')
>     (['B', 'C', 'D'], '->', 'E')
>     (['C', 'D', 'E'], '->', 'F')
>     (['D', 'E', 'F'], '->', 'G')
>     (['E', 'F', 'G'], '->', 'H')
>     (['F', 'G', 'H'], '->', 'I')
>     (['G', 'H', 'I'], '->', 'J')
>     (['H', 'I', 'J'], '->', 'K')
>     (['I', 'J', 'K'], '->', 'L')
>     (['J', 'K', 'L'], '->', 'M')
>     (['K', 'L', 'M'], '->', 'N')
>     (['L', 'M', 'N'], '->', 'O')
>     (['M', 'N', 'O'], '->', 'P')
>     (['N', 'O', 'P'], '->', 'Q')
>     (['O', 'P', 'Q'], '->', 'R')
>     (['P', 'Q', 'R'], '->', 'S')
>     (['Q', 'R', 'S'], '->', 'T')
>     (['R', 'S', 'T'], '->', 'U')
>     (['S', 'T', 'U'], '->', 'V')
>     (['T', 'U', 'V'], '->', 'W')
>     (['U', 'V', 'W'], '->', 'X')
>     (['V', 'W', 'X'], '->', 'Y')
>     (['W', 'X', 'Y'], '->', 'Z')

``` python
(X.shape[1], X.shape[2]) # the input shape to LSTM layer with 32 neurons is given by dimensions of time-steps and features
```

>     Out[5]: (3, 1)

``` python
X.shape[0], y.shape[1] # number of examples and number of categorical outputs
```

>     Out[7]: (23, 26)

**Memory and context**

If this network is learning the way we would like, it should be robust to noise and also understand the relative context (in this case, where a prior letter occurs in the sequence).

I.e., we should be able to give it corrupted sequences, and it should produce reasonably correct predictions.

Make the following change to the code to test this out:

You Try!
--------

-   We'll use "W" for our erroneous/corrupted data element
-   Add code at the end to predict on the following sequences:
    -   'WBC', 'WKL', 'WTU', 'DWF', 'MWO', 'VWW', 'GHW', 'JKW', 'PQW'
-   Notice any pattern? Hard to tell from a small sample, but if you play with it (trying sequences from different places in the alphabet, or different "corruption" letters, you'll notice patterns that give a hint at what the network is learning

The solution is in `060_DLByABr_05a-LSTM-Solution` if you are lazy right now or get stuck.

**Pretty cool... BUT**

This alphabet example does seem a bit like "tennis without the net" since the original goal was to develop networks that could extract patterns from complex, ambiguous content like natural language or music, and we've been playing with a sequence (Roman alphabet) that is 100% deterministic and tiny in size.

First, go ahead and start `061_DLByABr_05b-LSTM-Language` since it will take several minutes to produce its first output.

This latter script is taken 100% exactly as-is from the Keras library examples folder (https://github.com/fchollet/keras/blob/master/examples/lstm\_text\_generation.py) and uses precisely the logic we just learned, in order to learn and synthesize English language text from a single-author corpuse. The amazing thing is that the text is learned and generated one letter at a time, just like we did with the alphabet.

Compared to our earlier examples... \* there is a minor difference in the way the inputs are encoded, using 1-hot vectors \* and there is a *significant* difference in the way the outputs (predictions) are generated: instead of taking just the most likely output class (character) via argmax as we did before, this time we are treating the output as a distribution and sampling from the distribution.

Let's take a look at the code ... but even so, this will probably be something to come back to after fika or a long break, as the training takes about 5 minutes per epoch (late 2013 MBP CPU) and we need around 20 epochs (80 minutes!) to get good output.

``` python
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
```

Gated Recurrent Unit (GRU)
--------------------------

In 2014, a new, promising design for RNN units called Gated Recurrent Unit was published (https://arxiv.org/abs/1412.3555)

GRUs have performed similarly to LSTMs, but are slightly simpler in design:

-   GRU has just two gates: "update" and "reset" (instead of the input, output, and forget in LSTM)
-   **update** controls how to modify (weight and keep) cell state
-   **reset** controls how new input is mixed (weighted) with/against memorized state
-   there is no output gate, so the cell state is propagated out -- i.e., there is **no "hidden" state** that is separate from the generated output state

<img src="http://i.imgur.com/nnATBmC.png" width=700>

Which one should you use for which applications? The jury is still out -- this is an area for experimentation!

### Using GRUs in Keras

... is as simple as using the built-in GRU class (https://keras.io/layers/recurrent/)

If you are working with RNNs, spend some time with docs to go deeper, as we have just barely scratched the surface here, and there are many "knobs" to turn that will help things go right (or wrong).

``` python
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
```

>     corpus length: 600901
>     total chars: 59
>     nb sequences: 200287
>     Vectorization...

``` python
len(sentences), maxlen, len(chars)
```

>     Out[11]: (200287, 40, 59)

``` python
X
```

>     Out[10]: 
>     array([[[False, False, False, ..., False, False, False],
>             [False, False, False, ..., False, False, False],
>             [False, False, False, ..., False, False, False],
>             ..., 
>             [False, False, False, ..., False, False, False],
>             [False, False, False, ..., False, False, False],
>             [False, False, False, ..., False, False, False]],
>
>            [[False, False, False, ..., False, False, False],
>             [False, False, False, ..., False, False, False],
>             [False, False, False, ..., False, False, False],
>             ..., 
>             [False, False, False, ..., False, False, False],
>             [False, False, False, ..., False, False, False],
>             [False, False, False, ..., False, False, False]],
>
>            [[False, False, False, ..., False, False, False],
>             [ True, False, False, ..., False, False, False],
>             [ True, False, False, ..., False, False, False],
>             ..., 
>             [False, False, False, ..., False, False, False],
>             [False, False, False, ..., False, False, False],
>             [False, False, False, ..., False, False, False]],
>
>            ..., 
>            [[False, False, False, ..., False, False, False],
>             [False, False, False, ..., False, False, False],
>             [False, False, False, ..., False, False, False],
>             ..., 
>             [False,  True, False, ..., False, False, False],
>             [False, False, False, ..., False, False, False],
>             [False, False, False, ..., False, False, False]],
>
>            [[False, False, False, ..., False, False, False],
>             [False, False, False, ..., False, False, False],
>             [False, False, False, ..., False, False, False],
>             ..., 
>             [False, False, False, ..., False, False, False],
>             [False, False, False, ..., False, False, False],
>             [False, False, False, ..., False, False, False]],
>
>            [[False, False, False, ..., False, False, False],
>             [False, False, False, ..., False, False, False],
>             [False, False, False, ..., False, False, False],
>             ..., 
>             [False, False, False, ..., False, False, False],
>             [False, False, False, ..., False, False, False],
>             [False, False, False, ..., False, False, False]]], dtype=bool)

### What Does Our Nietzsche Generator Produce?

Here are snapshots from middle and late in a training run.

#### Iteration 19

    Iteration 19
    Epoch 1/1
    200287/200287 [==============================] - 262s - loss: 1.3908     

    ----- diversity: 0.2
    ----- Generating with seed: " apart from the value of such assertions"
     apart from the value of such assertions of the present of the supersially and the soul. the spirituality of the same of the soul. the protect and in the states to the supersially and the soul, in the supersially the supersially and the concerning and in the most conscience of the soul. the soul. the concerning and the substances, and the philosophers in the sing"--that is the most supersiall and the philosophers of the supersially of t

    ----- diversity: 0.5
    ----- Generating with seed: " apart from the value of such assertions"
     apart from the value of such assertions are more there is the scientific modern to the head in the concerning in the same old will of the excited of science. many all the possible concerning such laugher according to when the philosophers sense of men of univerself, the most lacked same depresse in the point, which is desires of a "good (who has senses on that one experiencess which use the concerning and in the respect of the same ori

    ----- diversity: 1.0
    ----- Generating with seed: " apart from the value of such assertions"
     apart from the value of such assertions expressions--are interest person from indeed to ordinapoon as or one of
    the uphamy, state is rivel stimromannes are lot man of soul"--modile what he woulds hope in a riligiation, is conscience, and you amy, surposit to advanced torturily
    and whorlon and perressing for accurcted with a lot us in view, of its own vanity of their natest"--learns, and dis predeceared from and leade, for oted those wi

    ----- diversity: 1.2
    ----- Generating with seed: " apart from the value of such assertions"
     apart from the value of such assertions of
    rutould chinates
    rested exceteds to more saarkgs testure carevan, accordy owing before fatherly rifiny,
    thrurgins of novelts "frous inventive earth as dire!ition he
    shate out of itst sacrifice, in this
    mectalical
    inworle, you
    adome enqueres to its ighter. he often. once even with ded threaten"! an eebirelesifist.

    lran innoting
    with we canone acquire at them crarulents who had prote will out t

#### Iteration 32

    Iteration 32
    Epoch 1/1
    200287/200287 [==============================] - 255s - loss: 1.3830     

    ----- diversity: 0.2
    ----- Generating with seed: " body, as a part of this external
    world,"
     body, as a part of this external
    world, and in the great present of the sort of the strangern that is and in the sologies and the experiences and the present of the present and science of the probably a subject of the subject of the morality and morality of the soul the experiences the morality of the experiences of the conscience in the soul and more the experiences the strangere and present the rest the strangere and individual of th

    ----- diversity: 0.5
    ----- Generating with seed: " body, as a part of this external
    world,"
     body, as a part of this external
    world, and in the morality of which we knows upon the english and insigning things be exception of
    consequences of the man and explained its more in the senses for the same ordinary and the sortarians and subjects and simily in a some longing the destiny ordinary. man easily that has been the some subject and say, and and and and does not to power as all the reasonable and distinction of this one betray

    ----- diversity: 1.0
    ----- Generating with seed: " body, as a part of this external
    world,"
     body, as a part of this external
    world, surrespossifilice view and life fundamental worthing more sirer. holestly
    and whan to be
    dream. in whom hand that one downgk edplenius will almost eyes brocky that we wills stupid dor
    oborbbill to be dimorable
    great excet of ifysabless. the good take the historical yet right by guntend, and which fuens the irrelias in literals in finally to the same flild, conditioned when where prom. it has behi

    ----- diversity: 1.2
    ----- Generating with seed: " body, as a part of this external
    world,"
     body, as a part of this external
    world, easily achosed time mantur makeches on this
    vanity, obcame-scompleises. but inquire-calr ever powerfully smorais: too-wantse; when thoue
    conducting
    unconstularly without least gainstyfyerfulled to wo
    has upos
    among uaxqunct what is mell "loves and
    lamacity what mattery of upon the a. and which oasis seour schol
    to power: the passion sparabrated will. in his europers raris! what seems to these her

### Take alook at the anomalous behavior that started late in the training on one run ... What might have happened?

#### Iteration 38

    Iteration 38
    Epoch 1/1
    200287/200287 [==============================] - 256s - loss: 7.6662     

    ----- diversity: 0.2
    ----- Generating with seed: "erable? for there is no
    longer any ought"
    erable? for there is no
    longer any oughteesen a a  a= at ae i is es4 iei aatee he a a ac  oyte  in ioie  aan a atoe aie ion a atias a ooe o e tin exanat moe ao is aon e a ntiere t i in ate an on a  e as the a ion aisn ost  aed i  i ioiesn les?ane i ee to i o ate   o igice thi io an a xen an ae an teane one ee e alouieis asno oie on i a a ae s as n io a an e a ofe e  oe ehe it aiol  s a aeio st ior ooe an io e  ot io  o i  aa9em aan ev a

    ----- diversity: 0.5
    ----- Generating with seed: "erable? for there is no
    longer any ought"
    erable? for there is no
    longer any oughteese a on eionea] aooooi ate uo e9l hoe atae s in eaae an  on io]e nd ast aais  ta e  od iia ng ac ee er ber  in ==st a se is ao  o e as aeian iesee tee otiane o oeean a ieatqe o  asnone anc 
     oo a t
    tee sefiois to an at in ol asnse an o e e oo  ie oae asne at a ait iati oese se a e p ie peen iei ien   o oot inees engied evone t oen oou atipeem a sthen ion assise ti a a s itos io ae an  eees as oi

    ----- diversity: 1.0
    ----- Generating with seed: "erable? for there is no
    longer any ought"
    erable? for there is no
    longer any oughteena te e ore te beosespeehsha ieno atit e ewge ou ino oo oee coatian aon ie ac aalle e a o  die eionae oa att uec a acae ao a  an eess as
     o  i a io  a   oe a  e is as oo in ene xof o  oooreeg ta m eon al iii n p daesaoe n ite o ane tio oe anoo t ane
    s i e tioo ise s a asi e ana ooe ote soueeon io on atieaneyc ei it he se it is ao e an ime  ane on eronaa ee itouman io e ato an ale  a mae taoa ien

    ----- diversity: 1.2
    ----- Generating with seed: "erable? for there is no
    longer any ought"
    erable? for there is no
    longer any oughti o aa e2senoees yi i e datssateal toeieie e a o zanato aal arn aseatli oeene aoni le eoeod t aes a isoee tap  e o . is  oi astee an ea titoe e a exeeee thui itoan ain eas a e bu inen ao ofa ie e e7n anae ait ie a ve  er inen  ite
    as oe of  heangi eestioe orasb e fie o o o  a  eean o ot odeerean io io oae ooe ne " e  istee esoonae e terasfioees asa ehainoet at e ea ai esoon   ano a p eesas e aitie

(raaz) \#\# 'Mind the Hype' around AI

Pay attention to biases in various media.

-   **Guardian**: *'He began to eat Hermione's family': bot tries to write Harry Potter book – and fails in magic ways After being fed all seven Potter tales, a predictive keyboard has produced a tale that veers from almost genuine to gloriously bonkers*
-   <https://www.theguardian.com/books/booksblog/2017/dec/13/harry-potter-botnik-jk-rowling>
-   **Business Insider**: *There is a new chapter in Harry Potter's story — and it was written by artificial intelligence ... The writing is mostly weird and borderline comical, but the machine managed to partly reproduce original writer J.K. Rowling's writing style.*
-   <http://nordic.businessinsider.com/there-is-a-new-chapter-in-harry-potters-story-and-it-was-written-by-artificial-intelligence-2017-12>

When your managers get "psyched" about how AI will solve all the problems and your sales teams are dreaming hard - keep it cool and manage their expectations as a practical data scientist who is humbled by the hard reality of additions, multiplications and conditionals under the hood.

'Mind the Ethics'
-----------------

Don't forget to ask how your data science pipelines could adversely affect peoples: \* A great X-mas gift to yourself: <https://weaponsofmathdestructionbook.com/> \* Another one to make you braver and calmer: <https://www.schneier.com/books/data_and_goliath/>

Don't forget that Data Scientists can be put behind bars for "following orders" from your boss to "make magic happen". \* <https://www.datasciencecentral.com/profiles/blogs/doing-illegal-data-science-without-knowing-it> \* <https://timesofindia.indiatimes.com/india/forecast-of-poll-results-illegal-election-commission/articleshow/57927839.cms> \* <https://spectrum.ieee.org/cars-that-think/at-work/education/vw-scandal-shocking-but-not-surprising-ethicists-say> \* ...