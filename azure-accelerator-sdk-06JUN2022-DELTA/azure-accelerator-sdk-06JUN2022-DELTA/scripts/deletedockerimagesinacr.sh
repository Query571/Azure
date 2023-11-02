#!/bin/bash

clientid=$1
clientsecret=$2
tenantid=$3
acrname=$4
imagename=$5




  az login --service-principal -u $clientid -p $clientsecret --tenant $tenantid > /dev/null 2>&1
   az acr login --name $acrname > /dev/null 2>&1


  az acr repository delete --name $acrname  --repository $imagename --yes


