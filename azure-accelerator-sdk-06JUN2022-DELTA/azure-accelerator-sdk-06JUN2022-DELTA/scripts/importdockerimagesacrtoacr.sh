#!/bin/bash

clientid=$1
clientsecret=$2
tenantid=$3
acrresourcename=$4
acrdestinationname=$5
imagename=$6
version=$7



  az login --service-principal -u $clientid -p $clientsecret --tenant $tenantid > /dev/null 2>&1
   az acr login --name $acrresourcename > /dev/null 2>&1

  output=$( az acr import  --name $acrdestinationname --source $acrresourcename.azurecr.io/$imagename:$version --image $imagename:$version )
 if [ $? -eq 0 ]
 then
          echo "ACR $acrresourcename   Docker image is imported Sucessfully , Image is $imagename and Tag is $version."
 else
    echo "$output"
    exit 1
 fi