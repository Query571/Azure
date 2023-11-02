#!/bin/bash

clientid=$1
clientsecret=$2
tenantid=$3
resourcegroup=$4
aksname=$5


az login --service-principal -u $clientid -p $clientsecret --tenant $tenantid > /dev/null 2>&1
az aks get-credentials --resource-group $resourcegroup --name $aksname --overwrite-existing > /dev/null 2>&1
if [ ! -f /usr/local/bin/kubectl ]; then
     apt-get update -y
     az aks install-cli
else
    ip=$(/usr/local/bin/kubectl get svc -n kubernetes-dashboard | grep kubernetes-dashboard |  awk '{print $4}')
    service=$(/usr/local/bin/kubectl describe sa dashboard-admin -n kubernetes-dashboard | grep "Mountable secrets" | cut -d " " -f5)
fi

echo "https://$ip"
/usr/local/bin/kubectl describe secret sa1-token -n kubernetes-dashboard | grep "token:" | cut -d " " -f7
