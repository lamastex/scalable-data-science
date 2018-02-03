[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

This is used in a non-profit educational setting with kind permission of [Adam Breindel](https://www.linkedin.com/in/adbreind). This is not licensed by Adam for use in a for-profit setting. Please contact Adam directly at `adbreind@gmail.com` to request or report such use cases or abuses. A few minor modifications and additional mathematical statistical pointers have been added by Raazesh Sainudiin when teaching PhD students in Uppsala University.


    0. Structure of your network is the first thing to work with, before worrying about the precise number of neurons, size of convolution filters etc.

    1. "Business records" or fairly (ideally?) uncorrelated predictors -- use Dense Perceptron Layer(s)

    2. Data that has 2-D patterns: 2D Convolution layer(s)

    3. For activation of hidden layers, when in doubt, use ReLU

    4. Output: 
      * Regression: 1 neuron with linear activation
      * For k-way classification: k neurons with softmax activation 

    5. Deeper networks are "smarter" than wider networks (in terms of abstraction)

    6. More neurons & layers \\( \to \\) more capacity \\( \to \\)  more data \\( \to \\)  more regularization (to prevent overfitting)

    7. If you don't have any specific reason not to use the "adam" optimizer, use that one

    8. Errors: 
      * For regression or "wide" content matching (e.g., large image similarity), use mean-square-error; 
      * For classification or narrow content matching, use cross-entropy

    9. As you simplify and abstract from your raw data, you should need less features/parameters, so your layers probably become smaller and simpler.


    We'll take our deep feed-forward multilayer perceptron network, with ReLU activations and reasonable initializations, and apply it to learning the MNIST digits.

    The main part of the code looks like the following (full code you can run is in the next cell):

    ```
    # imports, setup, load data sets

    model = Sequential()
    model.add(Dense(20, input_dim=784, kernel_initializer='normal', activation='relu'))
    model.add(Dense(15, kernel_initializer='normal', activation='relu'))
    model.add(Dense(10, kernel_initializer='normal', activation='softmax'))
    model.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['categorical_accuracy'])

    categorical_labels = to_categorical(y_train, num_classes=10)

    history = model.fit(X_train, categorical_labels, epochs=100, batch_size=100)

    # print metrics, plot errors
    ```

    Note the changes, which are largely about building a classifier instead of a regression model:
    * Output layer has one neuron per category, with softmax activation
    * __Loss function is cross-entropy loss__
    * Accuracy metric is categorical accuracy

    from keras.models import Sequential
    from keras.layers import Dense
    from keras.utils import to_categorical
    import sklearn.datasets
    import datetime
    import matplotlib.pyplot as plt
    import numpy as np

    train_libsvm = "/dbfs/databricks-datasets/mnist-digits/data-001/mnist-digits-train.txt"
    test_libsvm = "/dbfs/databricks-datasets/mnist-digits/data-001/mnist-digits-test.txt"

    X_train, y_train = sklearn.datasets.load_svmlight_file(train_libsvm, n_features=784)
    X_train = X_train.toarray()

    X_test, y_test = sklearn.datasets.load_svmlight_file(test_libsvm, n_features=784)
    X_test = X_test.toarray()

    model = Sequential()
    model.add(Dense(20, input_dim=784, kernel_initializer='normal', activation='relu'))
    model.add(Dense(15, kernel_initializer='normal', activation='relu'))
    model.add(Dense(10, kernel_initializer='normal', activation='softmax'))
    model.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['categorical_accuracy'])

    categorical_labels = to_categorical(y_train, num_classes=10)
    start = datetime.datetime.today()

    history = model.fit(X_train, categorical_labels, epochs=40, batch_size=100, validation_split=0.1, verbose=2)

    scores = model.evaluate(X_test, to_categorical(y_test, num_classes=10))

    print
    for i in range(len(model.metrics_names)):
    	print("%s: %f" % (model.metrics_names[i], scores[i]))

    print ("Start: " + str(start))
    end = datetime.datetime.today()
    print ("End: " + str(end))
    print ("Elapse: " + str(end-start))

    import matplotlib.pyplot as plt

    fig, ax = plt.subplots()
    fig.set_size_inches((5,5))
    plt.plot(history.history['loss'])
    plt.plot(history.history['val_loss'])
    plt.title('model loss')
    plt.ylabel('loss')
    plt.xlabel('epoch')
    plt.legend(['train', 'val'], loc='upper left')
    display(fig)

What are the big takeaways from this experiment?

1.  We get pretty impressive "apparent error" accuracy right from the start! A small network gets us to training accuracy 97% by epoch 20
2.  The model *appears* to continue to learn if we let it run, although it does slow down and oscillate a bit.
3.  Our test accuracy is about 95% after 5 epochs and never gets better ... it gets worse!
4.  Therefore, we are overfitting very quickly... most of the "training" turns out to be a waste.
5.  For what it's worth, we get 95% accuracy without much work.

This is not terrible compared to other, non-neural-network approaches to the problem. After all, we could probably tweak this a bit and do even better.

But we talked about using deep learning to solve "95%" problems or "98%" problems ... where one error in 20, or 50 simply won't work. If we can get to "multiple nines" of accuracy, then we can do things like automate mail sorting and translation, create cars that react properly (all the time) to street signs, and control systems for robots or drones that function autonomously.

You Try Now!
------------

Try two more experiments (try them separately): 1. Add a third, hidden layer. 2. Increase the size of the hidden layers.

Adding another layer slows things down a little (why?) but doesn't seem to make a difference in accuracy.

Adding a lot more neurons into the first topology slows things down significantly -- 10x as many neurons, and only a marginal increase in accuracy. Notice also (in the plot) that the learning clearly degrades after epoch 50 or so.