#!/bin/bash

clientid=$1
clientsecret=$2
tenantid=$3
resourcegroup=$4
aksname=$5
filename=$6

file=/srv/backend/appdeploy/$filename

if [[ -f "$file" ]];then
  az login --service-principal -u $clientid -p $clientsecret --tenant $tenantid > /dev/null 2>&1
  az aks get-credentials --resource-group $resourcegroup --name $aksname > /dev/null 2>&1
  if [ ! -f /usr/local/bin/kubectl ]; then
     apt-get update -y
     az aks install-cli
  else
    /usr/local/bin/kubectl apply -f $file > /dev/null 2>&1
    echo "your application deployed on $aksname"
     rm -rf $file > /dev/null 2>&1
  fi
else
  echo "$file not found !!"
  exit 0
fi