#!/bin/bash

nohup java -jar -server -Xms12g -Xmx12g -Xss4096k -XX:+UseG1GC  -XX:MetaspaceSize=512m -XX:MaxMetaspaceSize=512m -XX:PretenureSizeThreshold=83886080  -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:NativeMemoryTracking=detail  -Xloggc:/usr/local/yunji/titanx/logs/gc.log  titanrtx-agent-0.0.1-SNAPSHOT.jar > ./nohup.out &




