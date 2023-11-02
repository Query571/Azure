#!/bin/bash

sudo docker-compose -f /opt/docker-compose/docker-compose.yml down > /dev/null 2>&1
sleep 5

######################### vault Congfigration ##############################

export VAULT_ADDR='http://127.0.0.1:8200'
keys=$(cat /etc/vault/init.file | grep Unseal | head -n 3 | cut -d " " -f4)
status=$(vault status | grep Sealed | awk '{print $2}')

if [ "$status" = "true" ];
then
      echo "$keys" | while read utoken; do
      export VAULT_ADDR='http://127.0.0.1:8200' && vault operator unseal $utoken > /dev/null 2>&1
done
else
        echo ""
fi

################################## Deployment ##################################
password=xoriant123
token=$(cat /etc/vault/init.file | grep Token | cut -d ' ' -f 4)
#ip=$(ip addr | grep 'state UP' -A2 | tail -n1 | awk '{print $2}' | cut -f1  -d'/')
ip=$(ifconfig | grep -Eo 'inet (addr:)?([0-9]*\.){3}[0-9]*' | grep -Eo '([0-9]*\.){3}[0-9]*' | grep -v '127.0.0.1' | tail -n1)

echo "BIND_IP=$ip" > /opt/docker-compose/.env
echo "vault=$ip" >> /opt/docker-compose/.env
echo "token=$token" >> /opt/docker-compose/.env

export VAULT_ADDR='http://127.0.0.1:8200' && vault login $token > /dev/null 2>&1
vault secrets enable -path=secret kv > /dev/null 2>&1
vault kv put secret/mysql url=jdbc:mysql://${ip}:3306/azx?useSSL=false password=${password} username=azx > /dev/null 2>&1

sleep 5
sudo docker-compose -f /opt/docker-compose/docker-compose.yml up -d

sleep 30
sudo chmod 777 /opt/docker-compose/enableSSO.sh && sudo /opt/docker-compose/enableSSO.sh