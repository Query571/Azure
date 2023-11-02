#!/bin/bash

clientid=$1
clientsecret=$2
tenantid=$3
resourcegroup=$4
aksname=$5
nodepool=$6
count=$7

az login --service-principal -u $clientid -p $clientsecret --tenant $tenantid > /dev/null 2>&1
output=$(az aks nodepool scale --cluster-name $aksname --name $nodepool --resource-group $resourcegroup --node-count $count --no-wait)
if [[ $? == 0 ]]; then
	  echo "nodepool $nodepool scale initiated to $count in $aksname cluster"
	else
    echo "$output"
    exit 1
fi