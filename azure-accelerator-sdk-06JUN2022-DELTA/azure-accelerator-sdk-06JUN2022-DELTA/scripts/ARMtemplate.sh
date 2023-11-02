#!/bin/bash

clientid=$1
clientsecret=$2
tenantid=$3
resourcegroup=$4
resourceid=$5

az login --service-principal -u $clientid -p $clientsecret --tenant $tenantid > /dev/null 2>&1
az group export --resource-group $resourcegroup --resource-id $resourceid
