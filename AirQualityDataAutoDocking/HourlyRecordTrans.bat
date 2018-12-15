@echo off
set classpath=../lib/platform-0.2.8-SNAPSHOT.jar;../lib/ojdbc6.jar;../lib/log4j-1.2.17.jar;../lib/json-lib-2.4-jdk15.jar;../lib/ezmorph-1.0.6.jar;../lib/commons-logging-1.0.3.jar;../lib/commons-lang-2.5.jar;../lib/commons-collections-3.2.jar;../lib/commons-beanutils-1.8.3.2.jar;
cd /d   E:\publish\AirQualityDataAutoDocking\bin
java -Xms256m -Xmx1024m -XX:MaxNewSize=256m -XX:MaxPermSize=256m com.szboanda.sjdj.main.HourlyRecordExecMain
pause