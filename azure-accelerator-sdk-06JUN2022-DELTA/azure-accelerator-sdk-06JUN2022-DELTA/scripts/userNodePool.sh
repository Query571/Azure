#!/bin/bash

clientid=$1
clientsecret=$2
teanetid=$3
aksname=$4
nodepoolname=$5
resourcegroupname=$6
nodecount=$7
ostype=$8
nodesize=$9
maxpods=${10}
nodedisksize=${11}

if [[ "$nodedisksize" -lt "30" ]]; then
	nodedisksize=30
	az login --service-principal -u $clientid -p $clientsecret --tenant $teanetid > /dev/null 2>&1
	output=$(az aks nodepool add --cluster-name $aksname --name $nodepoolname --resource-group $resourcegroupname --node-count $nodecount --os-type $ostype --node-vm-size $nodesize --max-pods $maxpods --node-osdisk-size $nodedisksize --mode User --no-wait)
	if [[ $? == 0 ]]; then
	  echo "The process of creating node $nodepoolname is initiated successfully. Please click on refresh after sometime."
	else
    echo "$output"
    exit 1
	fi
else
	az login --service-principal -u $clientid -p $clientsecret --tenant $teanetid > /dev/null 2>&1
  output=$(az aks nodepool add --cluster-name $aksname --name $nodepoolname --resource-group $resourcegroupname --node-count $nodecount --os-type $ostype --node-vm-size $nodesize --max-pods $maxpods --node-osdisk-size $nodedisksize --mode User --no-wait)
	if [[ $? == 0 ]]; then
	  echo "The process of creating node $nodepoolname is initiated successfully. Please click on refresh after sometime."
	else
    echo "$output"
    exit 1
	fi
fi