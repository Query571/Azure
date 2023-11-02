#!/bin/bash

#echo -----------------Building Core------------------------

#mvn -f pom.xml clean install

echo -----------------Building Core------------------------
mvn -f pom.xml clean install

if [ $? != 0 ]
then
  echo "Build failure while running"
  exit 1
fi

if [ ! -f /usr/bin/docker-compose ]; then
  sudo curl -L "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/bin/docker-compose
  sudo chmod +x /usr/bin/docker-compose
fi

sudo docker build --no-cache -t eureka .