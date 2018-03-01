[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

This is used in a non-profit educational setting with kind permission of [Adam Breindel](https://www.linkedin.com/in/adbreind).
This is not licensed by Adam for use in a for-profit setting. Please contact Adam directly at `adbreind@gmail.com` to request or report such use cases or abuses.
A few minor modifications and additional mathematical statistical pointers have been added by Raazesh Sainudiin when teaching PhD students in Uppsala University.

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

# reshape X to be [samples, time steps, features]
X = numpy.reshape(dataX, (len(dataX), seq_length, 1))
X = X / float(len(alphabet))
y = np_utils.to_categorical(dataY)

model = Sequential()
model.add(LSTM(32, input_shape=(X.shape[1], X.shape[2])))
model.add(Dense(y.shape[1], activation='softmax'))
model.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['accuracy'])
model.fit(X, y, epochs=400, batch_size=1, verbose=2)

scores = model.evaluate(X, y)
print("Model Accuracy: %.2f%%" % (scores[1]*100))

for pattern in ['WBC', 'WKL', 'WTU', 'DWF', 'MWO', 'VWW', 'GHW', 'JKW', 'PQW']:
	pattern = [char_to_int[c] for c in pattern]
	x = numpy.reshape(pattern, (1, len(pattern), 1))
	x = x / float(len(alphabet))
	prediction = model.predict(x, verbose=0)
	index = numpy.argmax(prediction)
	result = int_to_char[index]
	seq_in = [int_to_char[value] for value in pattern]
	print (seq_in, "->", result)
```

>     Using TensorFlow backend.
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
>     0s - loss: 3.2754 - acc: 0.0000e+00
>     Epoch 2/400
>     0s - loss: 3.2605 - acc: 0.0435
>     Epoch 3/400
>     0s - loss: 3.2533 - acc: 0.0435
>     Epoch 4/400
>     0s - loss: 3.2470 - acc: 0.0435
>     Epoch 5/400
>     0s - loss: 3.2404 - acc: 0.0435
>     Epoch 6/400
>     0s - loss: 3.2345 - acc: 0.0435
>     Epoch 7/400
>     0s - loss: 3.2273 - acc: 0.0435
>     Epoch 8/400
>     0s - loss: 3.2202 - acc: 0.0435
>     Epoch 9/400
>     0s - loss: 3.2125 - acc: 0.0435
>     Epoch 10/400
>     0s - loss: 3.2043 - acc: 0.0435
>     Epoch 11/400
>     0s - loss: 3.1951 - acc: 0.0435
>     Epoch 12/400
>     0s - loss: 3.1857 - acc: 0.0435
>     Epoch 13/400
>     0s - loss: 3.1731 - acc: 0.0435
>     Epoch 14/400
>     0s - loss: 3.1614 - acc: 0.0435
>     Epoch 15/400
>     0s - loss: 3.1469 - acc: 0.0435
>     Epoch 16/400
>     0s - loss: 3.1320 - acc: 0.0435
>     Epoch 17/400
>     0s - loss: 3.1151 - acc: 0.0435
>     Epoch 18/400
>     0s - loss: 3.0969 - acc: 0.0435
>     Epoch 19/400
>     0s - loss: 3.0793 - acc: 0.0435
>     Epoch 20/400
>     0s - loss: 3.0609 - acc: 0.0435
>     Epoch 21/400
>     0s - loss: 3.0459 - acc: 0.0435
>     Epoch 22/400
>     0s - loss: 3.0171 - acc: 0.0435
>     Epoch 23/400
>     0s - loss: 2.9935 - acc: 0.0435
>     Epoch 24/400
>     0s - loss: 2.9669 - acc: 0.0870
>     Epoch 25/400
>     0s - loss: 2.9331 - acc: 0.0870
>     Epoch 26/400
>     0s - loss: 2.8985 - acc: 0.1304
>     Epoch 27/400
>     0s - loss: 2.8610 - acc: 0.0870
>     Epoch 28/400
>     0s - loss: 2.8236 - acc: 0.1304
>     Epoch 29/400
>     0s - loss: 2.7759 - acc: 0.1304
>     Epoch 30/400
>     0s - loss: 2.7317 - acc: 0.1304
>     Epoch 31/400
>     0s - loss: 2.6916 - acc: 0.1304
>     Epoch 32/400
>     0s - loss: 2.6509 - acc: 0.1304
>     Epoch 33/400
>     0s - loss: 2.6134 - acc: 0.0870
>     Epoch 34/400
>     0s - loss: 2.5798 - acc: 0.1304
>     Epoch 35/400
>     0s - loss: 2.5445 - acc: 0.0870
>     Epoch 36/400
>     0s - loss: 2.5142 - acc: 0.0870
>     Epoch 37/400
>     0s - loss: 2.4821 - acc: 0.1304
>     Epoch 38/400
>     0s - loss: 2.4543 - acc: 0.1304
>     Epoch 39/400
>     0s - loss: 2.4259 - acc: 0.1304
>     Epoch 40/400
>     0s - loss: 2.4005 - acc: 0.0870
>     Epoch 41/400
>     0s - loss: 2.3685 - acc: 0.1304
>     Epoch 42/400
>     0s - loss: 2.3379 - acc: 0.1304
>     Epoch 43/400
>     0s - loss: 2.3107 - acc: 0.1304
>     Epoch 44/400
>     0s - loss: 2.2847 - acc: 0.1304
>     Epoch 45/400
>     0s - loss: 2.2603 - acc: 0.1304
>     Epoch 46/400
>     0s - loss: 2.2349 - acc: 0.1304
>     Epoch 47/400
>     0s - loss: 2.2082 - acc: 0.1304
>     Epoch 48/400
>     0s - loss: 2.1838 - acc: 0.1739
>     Epoch 49/400
>     0s - loss: 2.1651 - acc: 0.1304
>     Epoch 50/400
>     0s - loss: 2.1458 - acc: 0.1304
>     Epoch 51/400
>     0s - loss: 2.1273 - acc: 0.1304
>     Epoch 52/400
>     0s - loss: 2.1024 - acc: 0.1739
>     Epoch 53/400
>     0s - loss: 2.0876 - acc: 0.1739
>     Epoch 54/400
>     0s - loss: 2.0608 - acc: 0.1304
>     Epoch 55/400
>     0s - loss: 2.0524 - acc: 0.2174
>     Epoch 56/400
>     0s - loss: 2.0220 - acc: 0.2174
>     Epoch 57/400
>     0s - loss: 2.0093 - acc: 0.3043
>     Epoch 58/400
>     0s - loss: 1.9878 - acc: 0.2609
>     Epoch 59/400
>     0s - loss: 1.9765 - acc: 0.2174
>     Epoch 60/400
>     0s - loss: 1.9638 - acc: 0.2609
>     Epoch 61/400
>     0s - loss: 1.9433 - acc: 0.2174
>     Epoch 62/400
>     0s - loss: 1.9250 - acc: 0.3913
>     Epoch 63/400
>     0s - loss: 1.9182 - acc: 0.2174
>     Epoch 64/400
>     0s - loss: 1.8968 - acc: 0.4348
>     Epoch 65/400
>     0s - loss: 1.8848 - acc: 0.2609
>     Epoch 66/400
>     0s - loss: 1.8733 - acc: 0.3043
>     Epoch 67/400
>     0s - loss: 1.8573 - acc: 0.3043
>     Epoch 68/400
>     0s - loss: 1.8468 - acc: 0.3913
>     Epoch 69/400
>     0s - loss: 1.8292 - acc: 0.3478
>     Epoch 70/400
>     0s - loss: 1.8210 - acc: 0.3913
>     Epoch 71/400
>     0s - loss: 1.8048 - acc: 0.3913
>     Epoch 72/400
>     0s - loss: 1.8039 - acc: 0.3043
>     Epoch 73/400
>     0s - loss: 1.7860 - acc: 0.3478
>     Epoch 74/400
>     0s - loss: 1.7774 - acc: 0.3478
>     Epoch 75/400
>     0s - loss: 1.7643 - acc: 0.3913
>     Epoch 76/400
>     0s - loss: 1.7528 - acc: 0.5217
>     Epoch 77/400
>     0s - loss: 1.7423 - acc: 0.4783
>     Epoch 78/400
>     0s - loss: 1.7387 - acc: 0.3913
>     Epoch 79/400
>     0s - loss: 1.7281 - acc: 0.3913
>     Epoch 80/400
>     0s - loss: 1.7052 - acc: 0.4783
>     Epoch 81/400
>     0s - loss: 1.7095 - acc: 0.3478
>     Epoch 82/400
>     0s - loss: 1.6930 - acc: 0.4783
>     Epoch 83/400
>     0s - loss: 1.6776 - acc: 0.5217
>     Epoch 84/400
>     0s - loss: 1.6728 - acc: 0.4783
>     Epoch 85/400
>     0s - loss: 1.6603 - acc: 0.5217
>     Epoch 86/400
>     0s - loss: 1.6446 - acc: 0.5217
>     Epoch 87/400
>     0s - loss: 1.6423 - acc: 0.5217
>     Epoch 88/400
>     0s - loss: 1.6368 - acc: 0.5217
>     Epoch 89/400
>     0s - loss: 1.6279 - acc: 0.5217
>     Epoch 90/400
>     0s - loss: 1.6140 - acc: 0.5217
>     Epoch 91/400
>     0s - loss: 1.6029 - acc: 0.4348
>     Epoch 92/400
>     0s - loss: 1.6050 - acc: 0.5652
>     Epoch 93/400
>     0s - loss: 1.5897 - acc: 0.6087
>     Epoch 94/400
>     0s - loss: 1.5799 - acc: 0.6087
>     Epoch 95/400
>     0s - loss: 1.5733 - acc: 0.6522
>     Epoch 96/400
>     0s - loss: 1.5675 - acc: 0.5652
>     Epoch 97/400
>     0s - loss: 1.5568 - acc: 0.6522
>     Epoch 98/400
>     0s - loss: 1.5497 - acc: 0.6522
>     Epoch 99/400
>     0s - loss: 1.5377 - acc: 0.6957
>     Epoch 100/400
>     0s - loss: 1.5336 - acc: 0.7391
>     Epoch 101/400
>     0s - loss: 1.5230 - acc: 0.6087
>     Epoch 102/400
>     0s - loss: 1.5188 - acc: 0.6957
>     Epoch 103/400
>     0s - loss: 1.5106 - acc: 0.6087
>     Epoch 104/400
>     0s - loss: 1.4985 - acc: 0.6087
>     Epoch 105/400
>     0s - loss: 1.4934 - acc: 0.6087
>     Epoch 106/400
>     0s - loss: 1.4816 - acc: 0.6957
>     Epoch 107/400
>     0s - loss: 1.4782 - acc: 0.6522
>     Epoch 108/400
>     0s - loss: 1.4646 - acc: 0.7826
>     Epoch 109/400
>     0s - loss: 1.4680 - acc: 0.6087
>     Epoch 110/400
>     0s - loss: 1.4589 - acc: 0.6957
>     Epoch 111/400
>     0s - loss: 1.4480 - acc: 0.7391
>     Epoch 112/400
>     0s - loss: 1.4424 - acc: 0.8261
>     Epoch 113/400
>     0s - loss: 1.4343 - acc: 0.7826
>     Epoch 114/400
>     0s - loss: 1.4287 - acc: 0.7391
>     Epoch 115/400
>     0s - loss: 1.4189 - acc: 0.6957
>     Epoch 116/400
>     0s - loss: 1.4169 - acc: 0.6957
>     Epoch 117/400
>     0s - loss: 1.4082 - acc: 0.6522
>     Epoch 118/400
>     0s - loss: 1.3956 - acc: 0.7826
>     Epoch 119/400
>     0s - loss: 1.3907 - acc: 0.6957
>     Epoch 120/400
>     0s - loss: 1.3888 - acc: 0.7391
>     Epoch 121/400
>     0s - loss: 1.3803 - acc: 0.7391
>     Epoch 122/400
>     0s - loss: 1.3739 - acc: 0.6957
>     Epoch 123/400
>     0s - loss: 1.3734 - acc: 0.7391
>     Epoch 124/400
>     0s - loss: 1.3684 - acc: 0.7391
>     Epoch 125/400
>     0s - loss: 1.3591 - acc: 0.7391
>     Epoch 126/400
>     0s - loss: 1.3483 - acc: 0.7826
>     Epoch 127/400
>     0s - loss: 1.3419 - acc: 0.6522
>     Epoch 128/400
>     0s - loss: 1.3414 - acc: 0.6957
>     Epoch 129/400
>     0s - loss: 1.3355 - acc: 0.7826
>     Epoch 130/400
>     0s - loss: 1.3250 - acc: 0.8261
>     Epoch 131/400
>     0s - loss: 1.3229 - acc: 0.7826
>     Epoch 132/400
>     0s - loss: 1.3222 - acc: 0.7391
>     Epoch 133/400
>     0s - loss: 1.3140 - acc: 0.7391
>     Epoch 134/400
>     0s - loss: 1.3002 - acc: 0.8696
>     Epoch 135/400
>     0s - loss: 1.2944 - acc: 0.7826
>     Epoch 136/400
>     0s - loss: 1.2899 - acc: 0.7391
>     Epoch 137/400
>     0s - loss: 1.2843 - acc: 0.8261
>     Epoch 138/400
>     0s - loss: 1.2723 - acc: 0.7826
>     Epoch 139/400
>     0s - loss: 1.2669 - acc: 0.7826
>     Epoch 140/400
>     0s - loss: 1.2664 - acc: 0.8261
>     Epoch 141/400
>     0s - loss: 1.2633 - acc: 0.7826
>     Epoch 142/400
>     0s - loss: 1.2487 - acc: 0.7826
>     Epoch 143/400
>     0s - loss: 1.2486 - acc: 0.7826
>     Epoch 144/400
>     0s - loss: 1.2456 - acc: 0.7826
>     Epoch 145/400
>     0s - loss: 1.2351 - acc: 0.8261
>     Epoch 146/400
>     0s - loss: 1.2233 - acc: 0.7826
>     Epoch 147/400
>     0s - loss: 1.2244 - acc: 0.8261
>     Epoch 148/400
>     0s - loss: 1.2115 - acc: 0.8261
>     Epoch 149/400
>     0s - loss: 1.2059 - acc: 0.8261
>     Epoch 150/400
>     0s - loss: 1.1988 - acc: 0.7826
>     Epoch 151/400
>     0s - loss: 1.1978 - acc: 0.8261
>     Epoch 152/400
>     0s - loss: 1.1928 - acc: 0.7826
>     Epoch 153/400
>     0s - loss: 1.1813 - acc: 0.8261
>     Epoch 154/400
>     0s - loss: 1.1858 - acc: 0.8261
>     Epoch 155/400
>     0s - loss: 1.1776 - acc: 0.7826
>     Epoch 156/400
>     0s - loss: 1.1724 - acc: 0.8696
>     Epoch 157/400
>     0s - loss: 1.1686 - acc: 0.8696
>     Epoch 158/400
>     0s - loss: 1.1629 - acc: 0.8696
>     Epoch 159/400
>     0s - loss: 1.1553 - acc: 0.8696
>     Epoch 160/400
>     0s - loss: 1.1483 - acc: 0.8261
>     Epoch 161/400
>     0s - loss: 1.1470 - acc: 0.7826
>     Epoch 162/400
>     0s - loss: 1.1314 - acc: 0.8261
>     Epoch 163/400
>     0s - loss: 1.1339 - acc: 0.7826
>     Epoch 164/400
>     0s - loss: 1.1337 - acc: 0.8261
>     Epoch 165/400
>     0s - loss: 1.1224 - acc: 0.8696
>     Epoch 166/400
>     0s - loss: 1.1214 - acc: 0.8261
>     Epoch 167/400
>     0s - loss: 1.1189 - acc: 0.8261
>     Epoch 168/400
>     0s - loss: 1.1118 - acc: 0.8696
>     Epoch 169/400
>     0s - loss: 1.1065 - acc: 0.9130
>     Epoch 170/400
>     0s - loss: 1.0941 - acc: 0.9130
>     Epoch 171/400
>     0s - loss: 1.0917 - acc: 0.8696
>     Epoch 172/400
>     0s - loss: 1.0808 - acc: 0.8696
>     Epoch 173/400
>     0s - loss: 1.0836 - acc: 0.8696
>     Epoch 174/400
>     0s - loss: 1.0705 - acc: 0.8261
>     Epoch 175/400
>     0s - loss: 1.0609 - acc: 0.8696
>     Epoch 176/400
>     0s - loss: 1.0694 - acc: 0.7826
>     Epoch 177/400
>     0s - loss: 1.0591 - acc: 0.8696
>     Epoch 178/400
>     0s - loss: 1.0523 - acc: 0.9130
>     Epoch 179/400
>     0s - loss: 1.0498 - acc: 0.8261
>     Epoch 180/400
>     0s - loss: 1.0455 - acc: 0.8261
>     Epoch 181/400
>     0s - loss: 1.0473 - acc: 0.8261
>     Epoch 182/400
>     0s - loss: 1.0413 - acc: 0.8696
>     Epoch 183/400
>     0s - loss: 1.0246 - acc: 0.8261
>     Epoch 184/400
>     0s - loss: 1.0243 - acc: 0.8696
>     Epoch 185/400
>     0s - loss: 1.0261 - acc: 0.8696
>     Epoch 186/400
>     0s - loss: 1.0187 - acc: 0.8261
>     Epoch 187/400
>     0s - loss: 1.0087 - acc: 0.8696
>     Epoch 188/400
>     0s - loss: 1.0028 - acc: 0.9130
>     Epoch 189/400
>     0s - loss: 0.9928 - acc: 0.9130
>     Epoch 190/400
>     0s - loss: 0.9925 - acc: 0.9130
>     Epoch 191/400
>     0s - loss: 0.9868 - acc: 0.9130
>     Epoch 192/400
>     0s - loss: 0.9807 - acc: 0.8696
>     Epoch 193/400
>     0s - loss: 0.9789 - acc: 0.8696
>     Epoch 194/400
>     0s - loss: 0.9742 - acc: 0.9130
>     Epoch 195/400
>     0s - loss: 0.9653 - acc: 0.8696
>     Epoch 196/400
>     0s - loss: 0.9668 - acc: 0.8696
>     Epoch 197/400
>     0s - loss: 0.9640 - acc: 0.9130
>     Epoch 198/400
>     0s - loss: 0.9573 - acc: 0.7826
>     Epoch 199/400
>     0s - loss: 0.9461 - acc: 0.9130
>     Epoch 200/400
>     0s - loss: 0.9386 - acc: 0.8696
>     Epoch 201/400
>     0s - loss: 0.9415 - acc: 0.9130
>     Epoch 202/400
>     0s - loss: 0.9299 - acc: 0.9130
>     Epoch 203/400
>     0s - loss: 0.9226 - acc: 0.9565
>     Epoch 204/400
>     0s - loss: 0.9304 - acc: 0.9130
>     Epoch 205/400
>     0s - loss: 0.9176 - acc: 0.9130
>     Epoch 206/400
>     0s - loss: 0.9255 - acc: 0.9565
>     Epoch 207/400
>     0s - loss: 0.9133 - acc: 0.9130
>     Epoch 208/400
>     0s - loss: 0.9048 - acc: 0.9130
>     Epoch 209/400
>     0s - loss: 0.9015 - acc: 0.9130
>     Epoch 210/400
>     0s - loss: 0.8925 - acc: 0.9130
>     Epoch 211/400
>     0s - loss: 0.8931 - acc: 0.9130
>     Epoch 212/400
>     0s - loss: 0.8890 - acc: 0.9130
>     Epoch 213/400
>     0s - loss: 0.8807 - acc: 0.9565
>     Epoch 214/400
>     0s - loss: 0.8740 - acc: 0.8696
>     Epoch 215/400
>     0s - loss: 0.8757 - acc: 0.9130
>     Epoch 216/400
>     0s - loss: 0.8719 - acc: 0.8696
>     Epoch 217/400
>     0s - loss: 0.8650 - acc: 0.9565
>     Epoch 218/400
>     0s - loss: 0.8576 - acc: 0.9565
>     Epoch 219/400
>     0s - loss: 0.8509 - acc: 0.9565
>     Epoch 220/400
>     0s - loss: 0.8436 - acc: 0.9130
>     Epoch 221/400
>     0s - loss: 0.8426 - acc: 0.9130
>     Epoch 222/400
>     0s - loss: 0.8421 - acc: 0.9565
>     Epoch 223/400
>     0s - loss: 0.8311 - acc: 0.9130
>     Epoch 224/400
>     0s - loss: 0.8289 - acc: 0.9130
>     Epoch 225/400
>     0s - loss: 0.8293 - acc: 0.9130
>     Epoch 226/400
>     0s - loss: 0.8243 - acc: 0.9130
>     Epoch 227/400
>     0s - loss: 0.8245 - acc: 0.9130
>     Epoch 228/400
>     0s - loss: 0.8143 - acc: 0.9130
>     Epoch 229/400
>     0s - loss: 0.8130 - acc: 0.9565
>     Epoch 230/400
>     0s - loss: 0.8077 - acc: 0.9565
>     Epoch 231/400
>     0s - loss: 0.7966 - acc: 0.9130
>     Epoch 232/400
>     0s - loss: 0.7960 - acc: 0.9130
>     Epoch 233/400
>     0s - loss: 0.7924 - acc: 0.9130
>     Epoch 234/400
>     0s - loss: 0.7862 - acc: 0.9130
>     Epoch 235/400
>     0s - loss: 0.7874 - acc: 0.9130
>     Epoch 236/400
>     0s - loss: 0.7842 - acc: 0.9565
>     Epoch 237/400
>     0s - loss: 0.7763 - acc: 0.9565
>     Epoch 238/400
>     0s - loss: 0.7729 - acc: 0.9565
>     Epoch 239/400
>     0s - loss: 0.7658 - acc: 0.9130
>     Epoch 240/400
>     0s - loss: 0.7597 - acc: 0.9565
>     Epoch 241/400
>     0s - loss: 0.7560 - acc: 0.9130
>     Epoch 242/400
>     0s - loss: 0.7562 - acc: 0.9565
>     Epoch 243/400
>     0s - loss: 0.7511 - acc: 0.9565
>     Epoch 244/400
>     0s - loss: 0.7458 - acc: 0.9565
>     Epoch 245/400
>     0s - loss: 0.7476 - acc: 0.9565
>     Epoch 246/400
>     0s - loss: 0.7385 - acc: 0.9565
>     Epoch 247/400
>     0s - loss: 0.7382 - acc: 0.9130
>     Epoch 248/400
>     0s - loss: 0.7268 - acc: 0.9565
>     Epoch 249/400
>     0s - loss: 0.7262 - acc: 0.9565
>     Epoch 250/400
>     0s - loss: 0.7177 - acc: 0.9565
>     Epoch 251/400
>     0s - loss: 0.7214 - acc: 0.9130
>     Epoch 252/400
>     0s - loss: 0.7255 - acc: 0.9565
>     Epoch 253/400
>     0s - loss: 0.7105 - acc: 0.9130
>     Epoch 254/400
>     0s - loss: 0.7026 - acc: 0.9565
>     Epoch 255/400
>     0s - loss: 0.7076 - acc: 0.9565
>     Epoch 256/400
>     0s - loss: 0.6981 - acc: 0.9565
>     Epoch 257/400
>     0s - loss: 0.6958 - acc: 0.9565
>     Epoch 258/400
>     0s - loss: 0.6942 - acc: 0.9565
>     Epoch 259/400
>     0s - loss: 0.6828 - acc: 0.9565
>     Epoch 260/400
>     0s - loss: 0.6807 - acc: 0.9565
>     Epoch 261/400
>     0s - loss: 0.6821 - acc: 0.9565
>     Epoch 262/400
>     0s - loss: 0.6742 - acc: 0.9565
>     Epoch 263/400
>     0s - loss: 0.6726 - acc: 0.9565
>     Epoch 264/400
>     0s - loss: 0.6665 - acc: 0.9565
>     Epoch 265/400
>     0s - loss: 0.6648 - acc: 0.9565
>     Epoch 266/400
>     0s - loss: 0.6609 - acc: 0.9565
>     Epoch 267/400
>     0s - loss: 0.6596 - acc: 0.9565
>     Epoch 268/400
>     0s - loss: 0.6481 - acc: 0.9565
>     Epoch 269/400
>     0s - loss: 0.6480 - acc: 0.9565
>     Epoch 270/400
>     0s - loss: 0.6562 - acc: 0.9565
>     Epoch 271/400
>     0s - loss: 0.6531 - acc: 0.9565
>     Epoch 272/400
>     0s - loss: 0.6440 - acc: 0.9565
>     Epoch 273/400
>     0s - loss: 0.6373 - acc: 0.9565
>     Epoch 274/400
>     0s - loss: 0.6335 - acc: 0.9565
>     Epoch 275/400
>     0s - loss: 0.6306 - acc: 0.9565
>     Epoch 276/400
>     0s - loss: 0.6285 - acc: 0.9565
>     Epoch 277/400
>     0s - loss: 0.6269 - acc: 0.9565
>     Epoch 278/400
>     0s - loss: 0.6244 - acc: 1.0000
>     Epoch 279/400
>     0s - loss: 0.6191 - acc: 0.9565
>     Epoch 280/400
>     0s - loss: 0.6137 - acc: 0.9565
>     Epoch 281/400
>     0s - loss: 0.6104 - acc: 0.9565
>     Epoch 282/400
>     0s - loss: 0.6024 - acc: 0.9565
>     Epoch 283/400
>     0s - loss: 0.6011 - acc: 0.9565
>     Epoch 284/400
>     0s - loss: 0.5986 - acc: 0.9565
>     Epoch 285/400
>     0s - loss: 0.5967 - acc: 0.9565
>     Epoch 286/400
>     0s - loss: 0.5897 - acc: 0.9565
>     Epoch 287/400
>     0s - loss: 0.5892 - acc: 0.9565
>     Epoch 288/400
>     0s - loss: 0.5838 - acc: 0.9565
>     Epoch 289/400
>     0s - loss: 0.5785 - acc: 0.9565
>     Epoch 290/400
>     0s - loss: 0.5789 - acc: 0.9565
>     Epoch 291/400
>     0s - loss: 0.5783 - acc: 0.9565
>     Epoch 292/400
>     0s - loss: 0.5755 - acc: 0.9565
>     Epoch 293/400
>     0s - loss: 0.5686 - acc: 0.9565
>     Epoch 294/400
>     0s - loss: 0.5693 - acc: 0.9565
>     Epoch 295/400
>     0s - loss: 0.5630 - acc: 0.9565
>     Epoch 296/400
>     0s - loss: 0.5601 - acc: 0.9565
>     Epoch 297/400
>     0s - loss: 0.5581 - acc: 0.9565
>     Epoch 298/400
>     0s - loss: 0.5536 - acc: 0.9565
>     Epoch 299/400
>     0s - loss: 0.5537 - acc: 1.0000
>     Epoch 300/400
>     0s - loss: 0.5537 - acc: 0.9565
>     Epoch 301/400
>     0s - loss: 0.5415 - acc: 0.9565
>     Epoch 302/400
>     0s - loss: 0.5410 - acc: 1.0000
>     Epoch 303/400
>     0s - loss: 0.5474 - acc: 0.9565
>     Epoch 304/400
>     0s - loss: 0.5402 - acc: 0.9565
>     Epoch 305/400
>     0s - loss: 0.5329 - acc: 0.9565
>     Epoch 306/400
>     0s - loss: 0.5242 - acc: 1.0000
>     Epoch 307/400
>     0s - loss: 0.5274 - acc: 0.9565
>     Epoch 308/400
>     0s - loss: 0.5310 - acc: 0.9565
>     Epoch 309/400
>     0s - loss: 0.5223 - acc: 0.9565
>     Epoch 310/400
>     0s - loss: 0.5141 - acc: 0.9565
>     Epoch 311/400
>     0s - loss: 0.5150 - acc: 0.9565
>     Epoch 312/400
>     0s - loss: 0.5119 - acc: 0.9565
>     Epoch 313/400
>     0s - loss: 0.5121 - acc: 0.9565
>     Epoch 314/400
>     0s - loss: 0.5088 - acc: 0.9565
>     Epoch 315/400
>     0s - loss: 0.5025 - acc: 0.9565
>     Epoch 316/400
>     0s - loss: 0.5018 - acc: 0.9565
>     Epoch 317/400
>     0s - loss: 0.5065 - acc: 0.9565
>     Epoch 318/400
>     0s - loss: 0.4982 - acc: 1.0000
>     Epoch 319/400
>     0s - loss: 0.5061 - acc: 0.9565
>     Epoch 320/400
>     0s - loss: 0.4979 - acc: 0.9565
>     Epoch 321/400
>     0s - loss: 0.4934 - acc: 1.0000
>     Epoch 322/400
>     0s - loss: 0.4836 - acc: 0.9565
>     Epoch 323/400
>     0s - loss: 0.4811 - acc: 1.0000
>     Epoch 324/400
>     0s - loss: 0.4752 - acc: 1.0000
>     Epoch 325/400
>     0s - loss: 0.4775 - acc: 1.0000
>     Epoch 326/400
>     0s - loss: 0.4736 - acc: 1.0000
>     Epoch 327/400
>     0s - loss: 0.4713 - acc: 0.9565
>     Epoch 328/400
>     0s - loss: 0.4682 - acc: 0.9565
>     Epoch 329/400
>     0s - loss: 0.4686 - acc: 0.9565
>     Epoch 330/400
>     0s - loss: 0.4685 - acc: 1.0000
>     Epoch 331/400
>     0s - loss: 0.4586 - acc: 1.0000
>     Epoch 332/400
>     0s - loss: 0.4625 - acc: 1.0000
>     Epoch 333/400
>     0s - loss: 0.4610 - acc: 0.9565
>     Epoch 334/400
>     0s - loss: 0.4550 - acc: 1.0000
>     Epoch 335/400
>     0s - loss: 0.4511 - acc: 0.9565
>     Epoch 336/400
>     0s - loss: 0.4502 - acc: 0.9565
>     Epoch 337/400
>     0s - loss: 0.4476 - acc: 0.9565
>     Epoch 338/400
>     0s - loss: 0.4409 - acc: 1.0000
>     Epoch 339/400
>     0s - loss: 0.4459 - acc: 1.0000
>     Epoch 340/400
>     0s - loss: 0.4438 - acc: 1.0000
>     Epoch 341/400
>     0s - loss: 0.4373 - acc: 1.0000
>     Epoch 342/400
>     0s - loss: 0.4333 - acc: 1.0000
>     Epoch 343/400
>     0s - loss: 0.4358 - acc: 1.0000
>     Epoch 344/400
>     0s - loss: 0.4305 - acc: 1.0000
>     Epoch 345/400
>     0s - loss: 0.4283 - acc: 1.0000
>     Epoch 346/400
>     0s - loss: 0.4219 - acc: 1.0000
>     Epoch 347/400
>     0s - loss: 0.4292 - acc: 0.9565
>     Epoch 348/400
>     0s - loss: 0.4220 - acc: 0.9565
>     Epoch 349/400
>     0s - loss: 0.4224 - acc: 1.0000
>     Epoch 350/400
>     0s - loss: 0.4216 - acc: 0.9565
>     Epoch 351/400
>     0s - loss: 0.4148 - acc: 1.0000
>     Epoch 352/400
>     0s - loss: 0.4076 - acc: 0.9565
>     Epoch 353/400
>     0s - loss: 0.4106 - acc: 1.0000
>     Epoch 354/400
>     0s - loss: 0.4091 - acc: 1.0000
>     Epoch 355/400
>     0s - loss: 0.4096 - acc: 1.0000
>     Epoch 356/400
>     0s - loss: 0.4078 - acc: 0.9565
>     Epoch 357/400
>     0s - loss: 0.4021 - acc: 1.0000
>     Epoch 358/400
>     0s - loss: 0.3979 - acc: 0.9565
>     Epoch 359/400
>     0s - loss: 0.3957 - acc: 0.9565
>     Epoch 360/400
>     0s - loss: 0.3937 - acc: 1.0000
>     Epoch 361/400
>     0s - loss: 0.3927 - acc: 1.0000
>     Epoch 362/400
>     0s - loss: 0.3878 - acc: 0.9565
>     Epoch 363/400
>     0s - loss: 0.3879 - acc: 1.0000
>     Epoch 364/400
>     0s - loss: 0.3882 - acc: 1.0000
>     Epoch 365/400
>     0s - loss: 0.3817 - acc: 1.0000
>     Epoch 366/400
>     0s - loss: 0.3822 - acc: 1.0000
>     Epoch 367/400
>     0s - loss: 0.3773 - acc: 1.0000
>     Epoch 368/400
>     0s - loss: 0.3770 - acc: 1.0000
>     Epoch 369/400
>     0s - loss: 0.3787 - acc: 1.0000
>     Epoch 370/400
>     0s - loss: 0.3731 - acc: 1.0000
>     Epoch 371/400
>     0s - loss: 0.3704 - acc: 1.0000
>     Epoch 372/400
>     0s - loss: 0.3694 - acc: 0.9565
>     Epoch 373/400
>     0s - loss: 0.3666 - acc: 1.0000
>     Epoch 374/400
>     0s - loss: 0.3621 - acc: 1.0000
>     Epoch 375/400
>     0s - loss: 0.3631 - acc: 1.0000
>     Epoch 376/400
>     0s - loss: 0.3634 - acc: 1.0000
>     Epoch 377/400
>     0s - loss: 0.3634 - acc: 1.0000
>     Epoch 378/400
>     0s - loss: 0.3577 - acc: 1.0000
>     Epoch 379/400
>     0s - loss: 0.3568 - acc: 0.9565
>     Epoch 380/400
>     0s - loss: 0.3537 - acc: 1.0000
>     Epoch 381/400
>     0s - loss: 0.3521 - acc: 1.0000
>     Epoch 382/400
>     0s - loss: 0.3603 - acc: 1.0000
>     Epoch 383/400
>     0s - loss: 0.3721 - acc: 1.0000
>     Epoch 384/400
>     0s - loss: 0.3637 - acc: 1.0000
>     Epoch 385/400
>     0s - loss: 0.3499 - acc: 1.0000
>     Epoch 386/400
>     0s - loss: 0.3426 - acc: 1.0000
>     Epoch 387/400
>     0s - loss: 0.3396 - acc: 1.0000
>     Epoch 388/400
>     0s - loss: 0.3380 - acc: 1.0000
>     Epoch 389/400
>     0s - loss: 0.3431 - acc: 1.0000
>     Epoch 390/400
>     0s - loss: 0.3352 - acc: 1.0000
>     Epoch 391/400
>     0s - loss: 0.3366 - acc: 1.0000
>     Epoch 392/400
>     0s - loss: 0.3360 - acc: 1.0000
>     Epoch 393/400
>     0s - loss: 0.3333 - acc: 1.0000
>     Epoch 394/400
>     0s - loss: 0.3276 - acc: 1.0000
>     Epoch 395/400
>     0s - loss: 0.3262 - acc: 1.0000
>     Epoch 396/400
>     0s - loss: 0.3222 - acc: 1.0000
>     Epoch 397/400
>     0s - loss: 0.3202 - acc: 1.0000
>     Epoch 398/400
>     0s - loss: 0.3222 - acc: 1.0000
>     Epoch 399/400
>     0s - loss: 0.3219 - acc: 1.0000
>     Epoch 400/400
>     0s - loss: 0.3246 - acc: 1.0000
>     23/23 [==============================] - 0s
>     Model Accuracy: 100.00%
>     (['W', 'B', 'C'], '->', 'Y')
>     (['W', 'K', 'L'], '->', 'Y')
>     (['W', 'T', 'U'], '->', 'Z')
>     (['D', 'W', 'F'], '->', 'I')
>     (['M', 'W', 'O'], '->', 'Q')
>     (['V', 'W', 'W'], '->', 'Y')
>     (['G', 'H', 'W'], '->', 'J')
>     (['J', 'K', 'W'], '->', 'M')
>     (['P', 'Q', 'W'], '->', 'S')