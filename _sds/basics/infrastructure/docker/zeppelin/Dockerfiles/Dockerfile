# from sadikovi to add matplotlib support
FROM dylanmei/zeppelin

ENV DEBIAN_FRONTEND noninteractive

# instal matplotlib
RUN apt-get update && apt-get install -y software-properties-common python3-matplotlib

# update matplotlib config, otherwise display fails
RUN sed -i -e 's/backend      : TkAgg/backend      : Agg/g' /etc/matplotlibrc

# make python3 default (hack!)
RUN rm /usr/bin/python && ln -s python3 /usr/bin/python
