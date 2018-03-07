[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

Power Forecasting
=================

Student Project
---------------

by [Gustav Björdal](https://www.linkedin.com/in/gustav-bj%C3%B6rdal-180461155/), [Mahmoud Shepero](https://www.linkedin.com/in/mahmoudshepero/) and [Dennis van der Meer](https://www.linkedin.com/in/dennis-van-der-meer-79463b94/)

### Directory structure on databricks

To setup notebook with consistent file paths, create a
folder in the `scalable-data-science` directory called
`streaming-forecast` and place all notebooks under
`scalable-data-science/streaming-forecast/`.

To run a notebook called `NotebookA` placed in our folder from another notebook, use the following command:
`%run scalable-data-science/streaming-forecast/NotebookA`

Data
====

The data used in this project comes from JSON files with the following structure:
`json {   "ID": "A1157_AS3",   "timeStamp": "2017-05-18T09:59:58.3940000",   "DataList": {     "WXT530": {       "DN": "182.0",       "DM": "182.0",       "DX": "182.0",       "SN": "4.7",       "SM": "4.7",       "SX": "4.7",       "GT3U": "15.7",       "GT41": "2.0",       "GM41": "80.5",       "GP41": "1010.3",       "RC": "52.27",       "RD": "0.0",       "RI": "0.0",       "HC": "0.0",       "HD": "0.0",       "HI": "0.0",       "RP": "0.0",       "HP": "0.0"     },     "IO": {       "AI1": "1904",       "AI2": "2491",       "AI3": "15000",       "AI4": "15000",       "AI5": "15000",       "AI6": "15000",       "AI7": "15000",       "AI8": "15000",       "GT41_MV": "1904",       "GT42_MV": "2490",       "GT43_MV": "15000",       "GT44_MV": "15000",       "GT45_MV": "15000",       "GT46_MV": "15000",       "GT47_MV": "15000",       "GT48_MV": "15000"     },     "ClusterControl": {       "V1_IT": "TRUE",       "V1_P": "7.639001",       "V1_P1": "2537.0",       "V1_P2": "2547.0",       "V1_P3": "2555.0",       "V1_U1_L1_N": "23289.0",       "V1_U2_L2_N": "23304.0",       "V1_U3_L3_N": "23341.0",       "V1_U1_L1_L2": "131070.0",       "V1_U2_L2_L3": "131070.0",       "V1_U3_L3_L1": "131070.0",       "V1_I1": "10898.0",       "V1_I2": "10935.0",       "V1_I3": "10946.0",       "V1_Yield_Wh": "39266.0",       "V1_Yield_kWh": "33658.0",       "V1_Yield_MWh": "33.0",       "V1_YieldDay_Wh": "15810.0",       "V1_YieldDay_kWh": "15.0",       "V1_PV": "131070.0",       "V1_HZ": "4994.0",       "V2_IT": "TRUE",       "V2_P": "7.665001",       "V2_P1": "2543.0",       "V2_P2": "2551.0",       "V2_P3": "2571.0",       "V2_U1_L1_N": "23304.0",       "V2_U2_L2_N": "23346.0",       "V2_U3_L3_N": "23366.0",       "V2_U1_L1_L2": "131070.0",       "V2_U2_L2_L3": "131070.0",       "V2_U3_L3_L1": "131070.0",       "V2_I1": "10917.0",       "V2_I2": "10931.0",       "V2_I3": "11001.0",       "V2_Yield_Wh": "18065.0",       "V2_Yield_kWh": "34096.0",       "V2_Yield_MWh": "34.0",       "V2_YieldDay_Wh": "15770.0",       "V2_YieldDay_kWh": "15.0",       "V2_PV": "131070.0",       "V2_HZ": "4994.0",       "V3_IT": "TRUE",       "V3_P": "1.756",       "V3_P1": "587.0",       "V3_P2": "585.0",       "V3_P3": "584.0",       "V3_U1_L1_N": "23268.0",       "V3_U2_L2_N": "23265.0",       "V3_U3_L3_N": "23311.0",       "V3_U1_L1_L2": "131070.0",       "V3_U2_L2_L3": "131070.0",       "V3_U3_L3_L1": "131070.0",       "V3_I1": "2525.0",       "V3_I2": "2518.0",       "V3_I3": "2509.0",       "V3_Yield_Wh": "57140.0",       "V3_Yield_kWh": "7659.0",       "V3_Yield_MWh": "7.0",       "V3_YieldDay_Wh": "3680.0",       "V3_YieldDay_kWh": "3.0",       "V3_PV": "131070.0",       "V3_HZ": "4994.0"     },     "MX41": {       "I1": "24.75",       "I2": "24.73637",       "I3": "24.94774",       "U1_U2": "405.5996",       "U2_U3": "403.0921",       "U3_U1": "402.1868",       "U1_N": "#NaN",       "U2_N": "#NaN",       "U3_N": "#NaN",       "P1": "#NaN",       "P2": "#NaN",       "P3": "#NaN",       "P": "17.25",       "PVAR": "0.4296875",       "PVA": "17.25",       "PFA": "0.9960938",       "HZ": "49.75",       "WHAI": "9.289301",       "WHAE": "381.856",       "VARHI": "224.013",       "VARHE": "10462.336",       "WHPAI": "381.2576",       "WHPAE": "163.013",       "WHAEI1": "163.013",       "WHAEI2": "164.009",       "WHAEI3": "164.044",       "WHRA": "0.0",       "WHRB": "0.0",       "WHRC": "0.0",       "WHRD": "0.0"     },     "Alarm": {       "BATT_L": "FALSE",       "PLC_WD": "FALSE",       "MX41_LC": "FALSE",       "GW3U_LC": "FALSE",       "CCtrl_LC": "FALSE",       "CCtrl_V1_L": "FALSE",       "CCtrl_V1_LM": "FALSE",       "CCtrl_V2_L": "FALSE",       "CCtrl_V2_LM": "FALSE",       "CCtrl_V3_L": "FALSE",       "CCtrl_V3_LM": "FALSE",       "BATT_L_ACK": "FALSE",       "MX41_LC_FL": "0",       "GW3U_LC_FL": "0",       "CCtrl_LC_FL": "0",       "CCtrl_V1_L_FL": "0",       "CCtrl_V2_L_FL": "0",       "CCtrl_V3_L_FL": "0"     }   },   "EventProcessedUtcTime": "2017-05-18T08:00:01.4401635Z",   "PartitionId": 1,   "EventEnqueuedUtcTime": "2017-05-18T08:00:02.2980000Z",   "IoTHub": {     "MessageId": null,     "CorrelationId": null,     "ConnectionDeviceId": "MqttBridge",     "ConnectionDeviceGenerationId": "636287305803981633",     "EnqueuedTime": "2017-05-18T08:00:02.6060000Z"`

From this we only need a few fields, so heres how to clean up the data.

How to clean up the data
------------------------

The cleanup is performed by the JSON tool (http://trentm.com/json/\#INSTALL-PROJECT-BUGS) for node.js (https://nodejs.org/en/)

### Clean a single file

After installing node.js and JSON use the following command to clean up
a datafile:

`bash  cat <filename> | json -ga -e 'delete this.IoTHub;  delete this.PartitionId;  delete this.EventEnqueuedUtcTime;  delete this.EventProcessedUtcTime;  delete this.DataList.ClusterControl;  delete this.DataList.IO;  delete this.DataList.Alarm;  this.tmp = this.DataList.MX41.P;  delete this.DataList.MX41;  this.DataList.MX41 = {};  this.DataList.MX41.P = this.tmp;  delete this.tmp;  delete this.DataList.WXT530.DM;  delete this.DataList.WXT530.DX;  delete this.DataList.WXT530.SM;  delete this.DataList.WXT530.SX;  delete this.DataList.WXT530.GT41;  delete this.DataList.WXT530.HC;  delete this.DataList.WXT530.HD;  delete this.DataList.WXT530.HI;  delete this.DataList.WXT530.RP;  delete this.DataList.WXT530.HP;  delete this.ID' -0`

### Cleaning multiple files is different folders

Assuming we have the following filestructure:

`datafiles    │ └─── folder1 │   │   file011.txt │   │   file012.txt │   │   ... │    └─── folder2 |   │   file021.txt |   │   file022.txt ...`

Use the following command while in the `datafiles` directory to clean generate one clean output file with all the data:
`bash  cat */*.json | json -ga -e 'delete this.IoTHub;  delete this.PartitionId;  delete this.EventEnqueuedUtcTime;  delete this.EventProcessedUtcTime;  delete this.DataList.ClusterControl;  delete this.DataList.IO;  delete this.DataList.Alarm;  this.tmp = this.DataList.MX41.P;  delete this.DataList.MX41;  this.DataList.MX41 = {};  this.DataList.MX41.P = this.tmp;  delete this.tmp;  delete this.DataList.WXT530.DM;  delete this.DataList.WXT530.DX;  delete this.DataList.WXT530.SM;  delete this.DataList.WXT530.SX;  delete this.DataList.WXT530.GT41;  delete this.DataList.WXT530.HC;  delete this.DataList.WXT530.HD;  delete this.DataList.WXT530.HI;  delete this.DataList.WXT530.RP;  delete this.DataList.WXT530.HP;  delete this.ID' -0 > clean_data.json`