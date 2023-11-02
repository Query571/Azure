#!/bin/bash

#echo -----------------Building Core------------------------

#mvn -f pom.xml clean install

echo -----------------Building Core------------------------
mvn -f pom.xml clean install
#mvn clean install sonar:sonar -Dsonar.host.url=http://3.109.21.114 -Dsonar.login=7560a321f46e406a0fb9b9c57e9ca587e681bc8e

if [ $? != 0 ]
then
  echo "Build failure while running"
  exit 1
fi

if [ ! -f /usr/bin/docker-compose ]; then
  sudo curl -L "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/bin/docker-compose
  sudo chmod +x /usr/bin/docker-compose
fi

sudo docker build --no-cache -t sdk .

sudo mkdir -p /opt/docker-compose
sudo chmod -R 777 /opt/docker-compose
sudo cp -r docker-compose.yml /opt/docker-compose
sudo cp -r up.sh /opt/docker-compose
sudo cp -r enableSSO.sh /opt/docker-compose
sudo cp -r docker-compose/azx.sql /opt/docker-compose
sudo chmod -R 777 /opt/docker-compose