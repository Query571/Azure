#!/bin/bash

echo ${port}

nohup java -jar -Dserver.port=${port} /srv/backend/azx-eureka-server-0.0.1-SNAPSHOT.jar