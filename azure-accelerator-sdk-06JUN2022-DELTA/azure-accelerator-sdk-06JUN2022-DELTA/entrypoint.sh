#!/bin/bash

echo ${port}
echo ${token}
echo ${vault}
 
nohup java -jar -Dserver.port=${port} -Deureka.address=eureka -Dvault.token=${token} -Dvault.endpoint=http://${vault}:8200 -Dlog4j.configuration=file:/opt/logs/log4j2.properties -Dspring.profiles.active=dev /srv/backend/AzureAccelerator-Java-0.0.1-SNAPSHOT.jar
