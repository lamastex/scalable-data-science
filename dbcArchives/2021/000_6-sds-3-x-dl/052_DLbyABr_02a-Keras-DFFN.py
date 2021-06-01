# Databricks notebook source
# MAGIC %md
# MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)
# MAGIC 
# MAGIC This is a 2019-2021 augmentation and update of [Adam Breindel](https://www.linkedin.com/in/adbreind)'s initial notebooks.
# MAGIC 
# MAGIC _Thanks to [Christian von Koch](https://www.linkedin.com/in/christianvonkoch/) and [William Anzén](https://www.linkedin.com/in/william-anz%C3%A9n-b52003199/) for their contributions towards making these materials Spark 3.0.1 and Python 3+ compliant._

# COMMAND ----------

# MAGIC %md 
# MAGIC ##Keras Deep Feed-Forward Network
# MAGIC ### (solution)

# COMMAND ----------

from keras.models import Sequential
from keras.layers import Dense
import numpy as np
import pandas as pd

input_file = "/dbfs/databricks-datasets/Rdatasets/data-001/csv/ggplot2/diamonds.csv"

df = pd.read_csv(input_file, header = 0)
df.drop(df.columns[0], axis=1, inplace=True)
df = pd.get_dummies(df, prefix=['cut_', 'color_', 'clarity_'])

y = df.iloc[:,3:4].as_matrix().flatten()
y.flatten()

X = df.drop(df.columns[3], axis=1).as_matrix()
np.shape(X)

from sklearn.model_selection import train_test_split

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.25, random_state=42)

model = Sequential()
model.add(Dense(30, input_dim=26, kernel_initializer='normal', activation='relu')) 
model.add(Dense(20, kernel_initializer='normal', activation='relu')) # <--- CHANGE IS HERE
model.add(Dense(1, kernel_initializer='normal', activation='linear'))

model.compile(loss='mean_squared_error', optimizer='adam', metrics=['mean_squared_error'])
history = model.fit(X_train, y_train, epochs=1000, batch_size=100, validation_split=0.1, verbose=2)

scores = model.evaluate(X_test, y_test)
print("\nroot %s: %f" % (model.metrics_names[1], np.sqrt(scores[1])))

# COMMAND ----------

