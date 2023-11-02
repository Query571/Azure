#!/bin/bash
#required az version 2.34.1
clientid=$1
clientsecret=$2
tenantid=$3
resourcegroup=$4
aksname=$5


  az upgrade --yes > /dev/null 2>&1
  az login --service-principal -u $clientid -p $clientsecret --tenant $tenantid > /dev/null 2>&1

  az aks disable-addons --addons azure-keyvault-secrets-provider --name $aksname --resource-group $resourcegroup > /dev/null 2>&1

