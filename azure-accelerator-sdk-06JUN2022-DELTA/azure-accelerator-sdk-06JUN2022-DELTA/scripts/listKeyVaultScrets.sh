#!/bin/bash

clientid=$1
clientsecret=$2
tenantid=$3
keyvaultname=$4


  az login --service-principal -u $clientid -p $clientsecret --tenant $tenantid > /dev/null 2>&1

  az keyvault secret  list --vault-name $keyvaultname
