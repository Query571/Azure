#!/bin/bash

clientid=$1
clientsecret=$2
tenantid=$3
acrresourcename=$4
imagename=$5
version=$6


  az login --service-principal -u $clientid -p $clientsecret --tenant $tenantid > /dev/null 2>&1
  az acr login --name $acrresourcename > /dev/null 2>&1


output=$( az acr import --name $acrresourcename --source docker.io/library/$imagename:$version -t $imagename:$version )
 if [ $? -eq 0 ]
 then
          echo "ACR $acrresourcename   Docker image is imported Sucessfully , Image is $imagename and Tag is $version."
 else
    echo "$output"
    exit 1
 fi