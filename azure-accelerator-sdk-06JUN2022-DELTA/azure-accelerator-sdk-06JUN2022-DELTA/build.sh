#!/bin/bash
#!/bin/sh

echo -----------------Building Core------------------------
mvn -f pom.xml clean install
if [ $? != 0 ]
then
  echo "Build failure while running"
  exit 1
fi

sudo rm -rf /opt/updated_jar
sudo mkdir -p /opt/updated_jar/
sudo chmod -R 777 /opt/updated_jar
sudo cp -r target/AzureAccelerator-Java-0.0.1-SNAPSHOT.jar /opt/updated_jar
sudo cp -r scripts /opt/updated_jar
sudo cp -r *.sh /opt/updated_jar
sudo chmod -R 777 /opt/updated_jar