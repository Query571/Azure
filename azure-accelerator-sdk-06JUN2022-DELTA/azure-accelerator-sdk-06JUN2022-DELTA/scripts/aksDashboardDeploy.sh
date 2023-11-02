#!/bin/bash

clientid=$1
clientsecret=$2
tenantid=$3
resourcegroup=$4
aksname=$5

path=/srv/backend/scripts
file1=$path/kube-dashboard.yaml
file2=$path/Dashboard-Admin-for-Kubernetes-Dashboard.yaml

if [[ -f "$file1" ]] && [[ -f "$file2" ]];then
  az login --service-principal -u $clientid -p $clientsecret --tenant $tenantid > /dev/null 2>&1
  az aks get-credentials --resource-group $resourcegroup --name $aksname --overwrite-existing > /dev/null 2>&1
  if [ ! -f /usr/local/bin/kubectl ]; then
     apt-get update -y
     az aks install-cli
  else
    /usr/local/bin/kubectl apply -f $file1 > /dev/null 2>&1
    /usr/local/bin/kubectl apply -f $file2 > /dev/null 2>&1

    echo "Dashboard deployed on $aksname"
  fi
else
  echo "$file1 or $file2 not found!!!"
  exit 0
fi
