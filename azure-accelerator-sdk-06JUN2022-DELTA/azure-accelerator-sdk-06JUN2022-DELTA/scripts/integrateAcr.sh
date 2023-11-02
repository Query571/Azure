#!/bin/bash

clientid=$1
clientsecret=$2
tenantid=$3
resourcegroup=$4
aksCluster=$5
acrresourcename=$6


  az login --service-principal -u $clientid -p $clientsecret --tenant $tenantid > /dev/null 2>&1
  CLIENT_ID=$(az aks show --resource-group $resourcegroup --name $aksCluster --query "servicePrincipalProfile.clientId" --output tsv)

  ACR_ID=$(az acr show --name $acrresourcename --resource-group $resourcegroup --query "id" --output tsv)

  az role assignment create --assignee $CLIENT_ID --role acrpull --scope $ACR_ID
  output=$(az aks update -n $aksCluster -g $resourcegroup --attach-acr $acrresourcename --no-wait)
  if [[ $? == 0 ]]; then
  	  echo "AKS Cluster $aksCluster is integrated to $acrresourcename."
  	else
      echo "$output"
      exit 1
  	fi

