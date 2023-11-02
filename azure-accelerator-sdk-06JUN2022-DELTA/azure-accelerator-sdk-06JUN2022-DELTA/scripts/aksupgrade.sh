#!/bin/bash

clientid=$1
clientsecret=$2
tenantid=$3
action=$4
resourcegroup=$5
aksname=$6
version=$7

if [ $action = version ];then
  az login --service-principal -u $clientid -p $clientsecret --tenant $tenantid > /dev/null 2>&1
  az aks get-upgrades --name $aksname --resource-group $resourcegroup | grep kubernetesVersion | cut -d ":" -f2 | cut -d "," -f1 | tr -d '"' | cut -d " " -f2
elif [ $action = upgrade ];then
  az login --service-principal -u $clientid -p $clientsecret --tenant $tenantid > /dev/null 2>&1
  olderversion=$(az aks show --name $aksname --resource-group $resourcegroup | grep kubernetesVersion | cut -d ":" -f2 | cut -d "," -f1 | tr -d '"' | cut -d " " -f2)
  output=$(az aks upgrade --kubernetes-version $version --name $aksname --resource-group $resourcegroup --no-wait --yes)
  if [[ $? == 0 ]]; then
	  echo "AKS Cluster $aksname upgraded from $olderversion to $version."
	else
    echo "$output"
    exit 1
	fi
else
  echo "Please select action (version|upgrade)"
  exit 1
fi