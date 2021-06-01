-- Databricks notebook source
-- MAGIC %md
-- MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

-- COMMAND ----------

-- MAGIC %md
-- MAGIC 
-- MAGIC This notebook is originally from:
-- MAGIC 
-- MAGIC  - https://cdn2.hubspot.net/hubfs/438089/notebooks/Mobile_Sample_.html
-- MAGIC  
-- MAGIC You can download a tiny sample dataset from here:
-- MAGIC  
-- MAGIC ```
-- MAGIC wget http://lamastex.org/datasets/public/geospatial/misc/mobile_sample.csv
-- MAGIC ```
-- MAGIC 
-- MAGIC The main purpose is to show how SQL can be used for geospatial data at the resolution of countries.

-- COMMAND ----------

-- MAGIC %md # Mobile Sample Data (Sample)
-- MAGIC This notebook contains various chart examples based on a sample mobile phone dataset.  
-- MAGIC * Note, this dataset joins the mobile sample table and the country codes.
-- MAGIC * Notice that the country names do not match completely hence the use of the case statement within the join.

-- COMMAND ----------

-- MAGIC %md ## Mobile Devices by Geography (Sample Data)
-- MAGIC #### This is a world map of number of mobile phones by country from a sample dataset 

-- COMMAND ----------

select m.ClientID, c.CountryCode3, m.DeviceMake 
from mobile_sample m 
   join countrycodes c 
      on m.Country = c.Country

-- COMMAND ----------

cache table mobile_sample

-- COMMAND ----------

-- MAGIC %md ## Top 10 Device Makes in the US

-- COMMAND ----------

select DeviceMake, count(1) as DeviceCnt from mobile_sample where Country = 'United States' group by DeviceMake order by DeviceCnt desc limit 10

-- COMMAND ----------

-- MAGIC %md ## Mobile Devices by Geography (United States, Sample)

-- COMMAND ----------

select m.clientid, s.StateCodes from mobile_sample m join state_codes s on s.state = m.state

-- COMMAND ----------

-- MAGIC %md ## Device Make Count Box Plot

-- COMMAND ----------

select m.clientid, m.DeviceMake, s.StateCodes from mobile_sample m join state_codes s on s.state = m.state

-- COMMAND ----------

-- MAGIC %md ## Devices by Maker within the US

-- COMMAND ----------

select clientid, DeviceMake from mobile_sample where Country = 'United States' AND DeviceMake IN ('Apple', 'Samsung', 'LG', 'RIM', 'HTC', 'Motorola');