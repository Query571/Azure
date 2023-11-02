#!/bin/bash

clientid=$1
clientsecret=$2
tenantid=$3
acrresourcename=$4
imagename=$5
version=$6
username=$7
password=$8


  az login --service-principal -u $clientid -p $clientsecret --tenant $tenantid > /dev/null 2>&1
  docker login -u $username -p $password > /dev/null 2>&1
  az acr login --name $acrresourcename > /dev/null 2>&1

  docker pull $username/$imagename:$version > /dev/null 2>&1
  docker tag $username/$imagename:$version $acrresourcename.azurecr.io/$imagename:$version > /dev/null 2>&1

output=$(  docker push $acrresourcename.azurecr.io/$imagename:$version )
 if [ $? -eq 0 ]
 then
          echo "ACR $acrresourcename   Docker image is imported Successfully , Image is $imagename and Tag is $version."
 else
    echo "$output"
    exit 1
 fi
 docker image rm $acrresourcename.azurecr.io/$imagename:$version
 docker image rm $username/$imagename:$version
