#!/bin/bash

clientid=$1
clientsecret=$2
tenantid=$3
registryname=$4

az login --service-principal -u $clientid -p $clientsecret --tenant $tenantid > /dev/null 2>&1
az acr login --name $registryname > /dev/null 2>&1
az acr repository list -n $registryname | jq .[] | tr -d '"'