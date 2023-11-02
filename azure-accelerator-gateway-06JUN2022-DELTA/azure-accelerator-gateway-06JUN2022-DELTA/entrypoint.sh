#!/bin/bash

echo ${port}

nohup java -jar  -Dserver.port=${port} -Deureka.address=eureka /srv/backend/azx-zuul-gateway-0.0.1-SNAPSHOT.jar