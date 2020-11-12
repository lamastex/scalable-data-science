-- Databricks notebook source
-- MAGIC %md
-- MAGIC # [ScaDaMaLe, Scalable Data Science and Distributed Machine Learning](https://lamastex.github.io/scalable-data-science/sds/3/x/)

-- COMMAND ----------

-- MAGIC %md
-- MAGIC # SQL Pivoting since Spark 2.4
-- MAGIC ## SQL Pivot: Converting Rows to Columns
-- MAGIC This is from the following blogpost:
-- MAGIC  - https://databricks.com/blog/2018/11/01/sql-pivot-converting-rows-to-columns.html
-- MAGIC  
-- MAGIC  This is a useful trick to know when having to do ETL before exploring datasets that need row to column conversions.

-- COMMAND ----------

-- MAGIC %md 
-- MAGIC # Load Data
-- MAGIC Create tables and load temperature data

-- COMMAND ----------

CREATE OR REPLACE TEMPORARY VIEW high_temps
  USING csv
  OPTIONS (path "/databricks-datasets/weather/high_temps", header "true", mode "FAILFAST")

-- COMMAND ----------

CREATE OR REPLACE TEMPORARY VIEW low_temps
  USING csv
  OPTIONS (path "/databricks-datasets/weather/low_temps", header "true", mode "FAILFAST")

-- COMMAND ----------

-- MAGIC %md # Pivoting in SQL
-- MAGIC Getting the monthly average high temperatures with month as columns and year as rows.

-- COMMAND ----------

SELECT * FROM (
  SELECT year(date) year, month(date) month, temp
  FROM high_temps
  WHERE date BETWEEN DATE '2015-01-01' AND DATE '2018-08-31'
)
PIVOT (
  CAST(avg(temp) AS DECIMAL(4, 1))
  FOR month in (
    1 JAN, 2 FEB, 3 MAR, 4 APR, 5 MAY, 6 JUN,
    7 JUL, 8 AUG, 9 SEP, 10 OCT, 11 NOV, 12 DEC
  )
)
ORDER BY year DESC


-- COMMAND ----------

-- MAGIC %md # Pivoting with Multiple Aggregate Expressions
-- MAGIC Getting monthly average and maximum high temperatures with month as columns and year as rows.

-- COMMAND ----------

SELECT * FROM (
  SELECT year(date) year, month(date) month, temp
  FROM high_temps
  WHERE date BETWEEN DATE '2015-01-01' AND DATE '2018-08-31'
)
PIVOT (
  CAST(avg(temp) AS DECIMAL(4, 1)) avg, max(temp) max
  FOR month in (6 JUN, 7 JUL, 8 AUG, 9 SEP)
)
ORDER BY year DESC


-- COMMAND ----------

-- MAGIC %md # Pivoting with Multiple Grouping Columns
-- MAGIC Getting monthly average high and average low temperatures with month as columns and (year, hi/lo) as rows.

-- COMMAND ----------

SELECT * FROM (
  SELECT year(date) year, month(date) month, temp, flag `H/L`
  FROM (
    SELECT date, temp, 'H' as flag
    FROM high_temps
    UNION ALL
    SELECT date, temp, 'L' as flag
    FROM low_temps
  )
  WHERE date BETWEEN DATE '2015-01-01' AND DATE '2018-08-31'
)
PIVOT (
  CAST(avg(temp) AS DECIMAL(4, 1))
  FOR month in (6 JUN, 7 JUL, 8 AUG, 9 SEP)
)
ORDER BY year DESC, `H/L` ASC


-- COMMAND ----------

-- MAGIC %md # Pivoting with Multiple Pivot Columns
-- MAGIC Getting monthly average high and average low temperatures with (month, hi/lo) as columns and year as rows.

-- COMMAND ----------

SELECT * FROM (
  SELECT year(date) year, month(date) month, temp, flag
  FROM (
    SELECT date, temp, 'H' as flag
    FROM high_temps
    UNION ALL
    SELECT date, temp, 'L' as flag
    FROM low_temps
  )
  WHERE date BETWEEN DATE '2015-01-01' AND DATE '2018-08-31'
)
PIVOT (
  CAST(avg(temp) AS DECIMAL(4, 1))
  FOR (month, flag) in (
    (6, 'H') JUN_hi, (6, 'L') JUN_lo,
    (7, 'H') JUL_hi, (7, 'L') JUL_lo,
    (8, 'H') AUG_hi, (8, 'L') AUG_lo,
    (9, 'H') SEP_hi, (9, 'L') SEP_lo
  )
)
ORDER BY year DESC


-- COMMAND ----------

