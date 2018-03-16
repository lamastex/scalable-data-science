// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/xqy5geCNKCg/0.jpg)](https://www.youtube.com/embed/xqy5geCNKCg?start=0&end=1456&autoplay=1)

// COMMAND ----------

// MAGIC %md 
// MAGIC # Power Forecasting
// MAGIC ## Student Project 
// MAGIC by [Gustav Björdal](https://www.linkedin.com/in/gustav-bj%C3%B6rdal-180461155/), [Mahmoud Shepero](https://www.linkedin.com/in/mahmoudshepero/) and [Dennis van der Meer](https://www.linkedin.com/in/dennis-van-der-meer-79463b94/)
// MAGIC 
// MAGIC ### Directory structure on databricks
// MAGIC 
// MAGIC To setup notebook with consistent file paths, create a
// MAGIC folder in the `scalable-data-science` directory called
// MAGIC `streaming-forecast` and place all notebooks under
// MAGIC `scalable-data-science/streaming-forecast/`.
// MAGIC 
// MAGIC To run a notebook called `NotebookA` placed in our folder from another notebook, use the following command:
// MAGIC `%run scalable-data-science/streaming-forecast/NotebookA`

// COMMAND ----------

// MAGIC %md 
// MAGIC 
// MAGIC # Data
// MAGIC 
// MAGIC The data used in this project comes from JSON files with the following structure:
// MAGIC ```json
// MAGIC {
// MAGIC   "ID": "A1157_AS3",
// MAGIC   "timeStamp": "2017-05-18T09:59:58.3940000",
// MAGIC   "DataList": {
// MAGIC     "WXT530": {
// MAGIC       "DN": "182.0",
// MAGIC       "DM": "182.0",
// MAGIC       "DX": "182.0",
// MAGIC       "SN": "4.7",
// MAGIC       "SM": "4.7",
// MAGIC       "SX": "4.7",
// MAGIC       "GT3U": "15.7",
// MAGIC       "GT41": "2.0",
// MAGIC       "GM41": "80.5",
// MAGIC       "GP41": "1010.3",
// MAGIC       "RC": "52.27",
// MAGIC       "RD": "0.0",
// MAGIC       "RI": "0.0",
// MAGIC       "HC": "0.0",
// MAGIC       "HD": "0.0",
// MAGIC       "HI": "0.0",
// MAGIC       "RP": "0.0",
// MAGIC       "HP": "0.0"
// MAGIC     },
// MAGIC     "IO": {
// MAGIC       "AI1": "1904",
// MAGIC       "AI2": "2491",
// MAGIC       "AI3": "15000",
// MAGIC       "AI4": "15000",
// MAGIC       "AI5": "15000",
// MAGIC       "AI6": "15000",
// MAGIC       "AI7": "15000",
// MAGIC       "AI8": "15000",
// MAGIC       "GT41_MV": "1904",
// MAGIC       "GT42_MV": "2490",
// MAGIC       "GT43_MV": "15000",
// MAGIC       "GT44_MV": "15000",
// MAGIC       "GT45_MV": "15000",
// MAGIC       "GT46_MV": "15000",
// MAGIC       "GT47_MV": "15000",
// MAGIC       "GT48_MV": "15000"
// MAGIC     },
// MAGIC     "ClusterControl": {
// MAGIC       "V1_IT": "TRUE",
// MAGIC       "V1_P": "7.639001",
// MAGIC       "V1_P1": "2537.0",
// MAGIC       "V1_P2": "2547.0",
// MAGIC       "V1_P3": "2555.0",
// MAGIC       "V1_U1_L1_N": "23289.0",
// MAGIC       "V1_U2_L2_N": "23304.0",
// MAGIC       "V1_U3_L3_N": "23341.0",
// MAGIC       "V1_U1_L1_L2": "131070.0",
// MAGIC       "V1_U2_L2_L3": "131070.0",
// MAGIC       "V1_U3_L3_L1": "131070.0",
// MAGIC       "V1_I1": "10898.0",
// MAGIC       "V1_I2": "10935.0",
// MAGIC       "V1_I3": "10946.0",
// MAGIC       "V1_Yield_Wh": "39266.0",
// MAGIC       "V1_Yield_kWh": "33658.0",
// MAGIC       "V1_Yield_MWh": "33.0",
// MAGIC       "V1_YieldDay_Wh": "15810.0",
// MAGIC       "V1_YieldDay_kWh": "15.0",
// MAGIC       "V1_PV": "131070.0",
// MAGIC       "V1_HZ": "4994.0",
// MAGIC       "V2_IT": "TRUE",
// MAGIC       "V2_P": "7.665001",
// MAGIC       "V2_P1": "2543.0",
// MAGIC       "V2_P2": "2551.0",
// MAGIC       "V2_P3": "2571.0",
// MAGIC       "V2_U1_L1_N": "23304.0",
// MAGIC       "V2_U2_L2_N": "23346.0",
// MAGIC       "V2_U3_L3_N": "23366.0",
// MAGIC       "V2_U1_L1_L2": "131070.0",
// MAGIC       "V2_U2_L2_L3": "131070.0",
// MAGIC       "V2_U3_L3_L1": "131070.0",
// MAGIC       "V2_I1": "10917.0",
// MAGIC       "V2_I2": "10931.0",
// MAGIC       "V2_I3": "11001.0",
// MAGIC       "V2_Yield_Wh": "18065.0",
// MAGIC       "V2_Yield_kWh": "34096.0",
// MAGIC       "V2_Yield_MWh": "34.0",
// MAGIC       "V2_YieldDay_Wh": "15770.0",
// MAGIC       "V2_YieldDay_kWh": "15.0",
// MAGIC       "V2_PV": "131070.0",
// MAGIC       "V2_HZ": "4994.0",
// MAGIC       "V3_IT": "TRUE",
// MAGIC       "V3_P": "1.756",
// MAGIC       "V3_P1": "587.0",
// MAGIC       "V3_P2": "585.0",
// MAGIC       "V3_P3": "584.0",
// MAGIC       "V3_U1_L1_N": "23268.0",
// MAGIC       "V3_U2_L2_N": "23265.0",
// MAGIC       "V3_U3_L3_N": "23311.0",
// MAGIC       "V3_U1_L1_L2": "131070.0",
// MAGIC       "V3_U2_L2_L3": "131070.0",
// MAGIC       "V3_U3_L3_L1": "131070.0",
// MAGIC       "V3_I1": "2525.0",
// MAGIC       "V3_I2": "2518.0",
// MAGIC       "V3_I3": "2509.0",
// MAGIC       "V3_Yield_Wh": "57140.0",
// MAGIC       "V3_Yield_kWh": "7659.0",
// MAGIC       "V3_Yield_MWh": "7.0",
// MAGIC       "V3_YieldDay_Wh": "3680.0",
// MAGIC       "V3_YieldDay_kWh": "3.0",
// MAGIC       "V3_PV": "131070.0",
// MAGIC       "V3_HZ": "4994.0"
// MAGIC     },
// MAGIC     "MX41": {
// MAGIC       "I1": "24.75",
// MAGIC       "I2": "24.73637",
// MAGIC       "I3": "24.94774",
// MAGIC       "U1_U2": "405.5996",
// MAGIC       "U2_U3": "403.0921",
// MAGIC       "U3_U1": "402.1868",
// MAGIC       "U1_N": "#NaN",
// MAGIC       "U2_N": "#NaN",
// MAGIC       "U3_N": "#NaN",
// MAGIC       "P1": "#NaN",
// MAGIC       "P2": "#NaN",
// MAGIC       "P3": "#NaN",
// MAGIC       "P": "17.25",
// MAGIC       "PVAR": "0.4296875",
// MAGIC       "PVA": "17.25",
// MAGIC       "PFA": "0.9960938",
// MAGIC       "HZ": "49.75",
// MAGIC       "WHAI": "9.289301",
// MAGIC       "WHAE": "381.856",
// MAGIC       "VARHI": "224.013",
// MAGIC       "VARHE": "10462.336",
// MAGIC       "WHPAI": "381.2576",
// MAGIC       "WHPAE": "163.013",
// MAGIC       "WHAEI1": "163.013",
// MAGIC       "WHAEI2": "164.009",
// MAGIC       "WHAEI3": "164.044",
// MAGIC       "WHRA": "0.0",
// MAGIC       "WHRB": "0.0",
// MAGIC       "WHRC": "0.0",
// MAGIC       "WHRD": "0.0"
// MAGIC     },
// MAGIC     "Alarm": {
// MAGIC       "BATT_L": "FALSE",
// MAGIC       "PLC_WD": "FALSE",
// MAGIC       "MX41_LC": "FALSE",
// MAGIC       "GW3U_LC": "FALSE",
// MAGIC       "CCtrl_LC": "FALSE",
// MAGIC       "CCtrl_V1_L": "FALSE",
// MAGIC       "CCtrl_V1_LM": "FALSE",
// MAGIC       "CCtrl_V2_L": "FALSE",
// MAGIC       "CCtrl_V2_LM": "FALSE",
// MAGIC       "CCtrl_V3_L": "FALSE",
// MAGIC       "CCtrl_V3_LM": "FALSE",
// MAGIC       "BATT_L_ACK": "FALSE",
// MAGIC       "MX41_LC_FL": "0",
// MAGIC       "GW3U_LC_FL": "0",
// MAGIC       "CCtrl_LC_FL": "0",
// MAGIC       "CCtrl_V1_L_FL": "0",
// MAGIC       "CCtrl_V2_L_FL": "0",
// MAGIC       "CCtrl_V3_L_FL": "0"
// MAGIC     }
// MAGIC   },
// MAGIC   "EventProcessedUtcTime": "2017-05-18T08:00:01.4401635Z",
// MAGIC   "PartitionId": 1,
// MAGIC   "EventEnqueuedUtcTime": "2017-05-18T08:00:02.2980000Z",
// MAGIC   "IoTHub": {
// MAGIC     "MessageId": null,
// MAGIC     "CorrelationId": null,
// MAGIC     "ConnectionDeviceId": "MqttBridge",
// MAGIC     "ConnectionDeviceGenerationId": "636287305803981633",
// MAGIC     "EnqueuedTime": "2017-05-18T08:00:02.6060000Z"
// MAGIC ```
// MAGIC 
// MAGIC 
// MAGIC From this we only need a few fields, so heres how to clean up the data.
// MAGIC 
// MAGIC ## How to clean up the data
// MAGIC 
// MAGIC The cleanup is performed by the JSON tool (http://trentm.com/json/#INSTALL-PROJECT-BUGS) for node.js (https://nodejs.org/en/)
// MAGIC 
// MAGIC ### Clean a single file
// MAGIC 
// MAGIC After installing node.js and JSON use the following command to clean up
// MAGIC a datafile:
// MAGIC 
// MAGIC  ```bash
// MAGIC  cat <filename> | json -ga -e 'delete this.IoTHub;
// MAGIC  delete this.PartitionId;
// MAGIC  delete this.EventEnqueuedUtcTime;
// MAGIC  delete this.EventProcessedUtcTime;
// MAGIC  delete this.DataList.ClusterControl;
// MAGIC  delete this.DataList.IO;
// MAGIC  delete this.DataList.Alarm;
// MAGIC  this.tmp = this.DataList.MX41.P;
// MAGIC  delete this.DataList.MX41;
// MAGIC  this.DataList.MX41 = {};
// MAGIC  this.DataList.MX41.P = this.tmp;
// MAGIC  delete this.tmp;
// MAGIC  delete this.DataList.WXT530.DM;
// MAGIC  delete this.DataList.WXT530.DX;
// MAGIC  delete this.DataList.WXT530.SM;
// MAGIC  delete this.DataList.WXT530.SX;
// MAGIC  delete this.DataList.WXT530.GT41;
// MAGIC  delete this.DataList.WXT530.HC;
// MAGIC  delete this.DataList.WXT530.HD;
// MAGIC  delete this.DataList.WXT530.HI;
// MAGIC  delete this.DataList.WXT530.RP;
// MAGIC  delete this.DataList.WXT530.HP;
// MAGIC  delete this.ID' -0
// MAGIC  ```
// MAGIC 
// MAGIC ### Cleaning multiple files is different folders
// MAGIC 
// MAGIC Assuming we have the following filestructure:
// MAGIC 
// MAGIC ```
// MAGIC datafiles   
// MAGIC │
// MAGIC └─── folder1
// MAGIC │   │   file011.txt
// MAGIC │   │   file012.txt
// MAGIC │   │   ...
// MAGIC │   
// MAGIC └─── folder2
// MAGIC |   │   file021.txt
// MAGIC |   │   file022.txt
// MAGIC ...
// MAGIC ```
// MAGIC 
// MAGIC Use the following command while in the `datafiles` directory to clean generate one clean output file with all the data:
// MAGIC  ```bash
// MAGIC  cat */*.json | json -ga -e 'delete this.IoTHub;
// MAGIC  delete this.PartitionId;
// MAGIC  delete this.EventEnqueuedUtcTime;
// MAGIC  delete this.EventProcessedUtcTime;
// MAGIC  delete this.DataList.ClusterControl;
// MAGIC  delete this.DataList.IO;
// MAGIC  delete this.DataList.Alarm;
// MAGIC  this.tmp = this.DataList.MX41.P;
// MAGIC  delete this.DataList.MX41;
// MAGIC  this.DataList.MX41 = {};
// MAGIC  this.DataList.MX41.P = this.tmp;
// MAGIC  delete this.tmp;
// MAGIC  delete this.DataList.WXT530.DM;
// MAGIC  delete this.DataList.WXT530.DX;
// MAGIC  delete this.DataList.WXT530.SM;
// MAGIC  delete this.DataList.WXT530.SX;
// MAGIC  delete this.DataList.WXT530.GT41;
// MAGIC  delete this.DataList.WXT530.HC;
// MAGIC  delete this.DataList.WXT530.HD;
// MAGIC  delete this.DataList.WXT530.HI;
// MAGIC  delete this.DataList.WXT530.RP;
// MAGIC  delete this.DataList.WXT530.HP;
// MAGIC  delete this.ID' -0 > clean_data.json
// MAGIC  ```

// COMMAND ----------

