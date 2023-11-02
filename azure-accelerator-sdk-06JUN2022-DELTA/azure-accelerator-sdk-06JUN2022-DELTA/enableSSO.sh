#!/bin/bash

tokens=$(cat /etc/vault/init.file | grep Token | cut -d ' ' -f 4)

if [ ! -f /usr/bin/curl ];
then
    sudo apt-get update -y
    sudo apt-get install curl -y
else
    echo "curl already installed"
fi

ips=$(curl -s http://ipecho.net/plain)

ips=$(ifconfig | grep -Eo 'inet (addr:)?([0-9]*\.){3}[0-9]*' | grep -Eo '([0-9]*\.){3}[0-9]*' | grep -v '127.0.0.1' | tail -n1)

result=$(curl --location --request GET "http://$ips:8200/v1/secret/sso" --header "X-Vault-token: $tokens" --header 'Content-Type: application/json')


if [[ $result != 200  ]] ; then
  tenantId=$(echo $result | grep -o '"tenantId":"[^"]*' | grep -o '[^"]*$')
  clientId=$(echo $result | grep -o '"clientId":"[^"]*' | grep -o '[^"]*$')
  url=$(echo $result | grep -o '"redirectUrl":"[^"]*' | grep -o '[^"]*$')
else
  exit 1
fi

#echo "Client Id -" $clientId
#echo "Tenant Id -" $tenantId

file=/var/lib/docker/volumes/docker-compose_java-data/_data/configuration.json

if [ -d $file ]; then
  sudo rm -rf $file/
fi

if [ ! -f $file ]; then
  jsonobj='{"msal":{"auth":{"clientId":"CLIENTID","authority":"https://login.microsoftonline.com/TENANTID/","redirectUri":"PUBLICIP","postLogoutRedirectUri":"PUBLICIP","navigateToLoginRequestUrl":true},"cache":{"cacheLocation":"localStorage","storeAuthStateInCookie":true}},"guard":{"interactionType":"redirect","authRequest":{"scopes":["user.read"]},"loginFailedRoute":"/login-failed"},"interceptor":{"interactionType":"redirect","protectedResourceMap":[["https://graph.microsoft.com/v1.0/me",["user.read"]]]}}'
  echo $jsonobj > $file
else
  sudo rm -rf $file
  jsonobj='{"msal":{"auth":{"clientId":"CLIENTID","authority":"https://login.microsoftonline.com/TENANTID/","redirectUri":"PUBLICIP","postLogoutRedirectUri":"PUBLICIP","navigateToLoginRequestUrl":true},"cache":{"cacheLocation":"localStorage","storeAuthStateInCookie":true}},"guard":{"interactionType":"redirect","authRequest":{"scopes":["user.read"]},"loginFailedRoute":"/login-failed"},"interceptor":{"interactionType":"redirect","protectedResourceMap":[["https://graph.microsoft.com/v1.0/me",["user.read"]]]}}'
  echo $jsonobj > $file
fi

if [[ $clientId != "" && $tenantId != "" && $url != "" ]]; then
  sed -i -r "s/CLIENTID/$clientId/g" $file
  sed -i -r "s/TENANTID/$tenantId/g" $file
  sed -i -r "s/PUBLICIP/$url/g" $file
fi

sudo docker restart angular