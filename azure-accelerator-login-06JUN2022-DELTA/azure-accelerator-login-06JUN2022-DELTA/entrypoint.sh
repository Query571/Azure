#!/bin/bash

echo ${port}
echo ${token}
echo ${vault}

nohup java -jar -Dserver.port=${port} -Deureka.address=eureka -Dvault.token=${token} -Dvault.endpoint=http://${vault}:8200 -Dspring.profiles.active=dev /srv/backend/azureAcceleratorLogin-Java-0.0.1-SNAPSHOT.jar