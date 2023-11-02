#!/bin/bash
#!/bin/sh

port=$1
profile=$2

################### Software installation #####################

if [ ! -f /usr/bin/jq ];
then
    sudo apt-get update -y
    sudo apt-get install jq -y
else
    echo "jq already installed"
fi

if [ ! -f /usr/bin/docker ];
then
    sudo apt-get update -y
    sudo apt-get install apt-transport-https ca-certificates curl gnupg lsb-release -y
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
    echo "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
    sudo apt-get update
    sudo apt-get install docker-ce docker-ce-cli containerd.io -y
else
    echo "docker already installed"
fi

if [ ! -f /usr/bin/az ];
then
    sudo apt-get update -y
    sudo curl -sL https://aka.ms/InstallAzureCLIDeb | sudo bash
else
    echo "az cli already installed"
fi

if [ ! -f /usr/bin/zip ];
then
    sudo apt-get update -y
    sudo apt-get install zip -y
else
    echo "zip already installed"
fi

if [ ! -f /usr/local/bin/kubectl ];
then
    sudo apt-get update -y
    sudo az aks install-cli
else
    echo "kubectl already installed"
fi

########################### Cleanup ########################

sudo kill -9 $(ps aux | grep "java" | grep $port | awk '{print $2}') > /dev/null 2>&1
sudo kill -9 $(ps aux | grep "java" | grep $port | awk '{print $2}') > /dev/null 2>&1

sudo rm -rf /srv/backend
sudo mkdir -p /srv/backend
sudo mkdir -p /srv/backend/appdeploy
sudo mkdir -p /srv/backend/logs
sudo chmod -R 777 /srv/backend

sudo cp -r /opt/updated_jar/AzureAccelerator-Java-0.0.1-SNAPSHOT.jar /srv/backend
sudo cp -r /opt/updated_jar/*.sh /srv/backend
sudo cp -r /opt/updated_jar/scripts /srv/backend
sudo chmod -R 777 /srv/backend

############# Unseal Vault #################################

export VAULT_ADDR='http://127.0.0.1:8200'

keys=$(cat /etc/vault/init.file | grep Unseal | head -n 3 | cut -d " " -f4)
status=$(vault status | grep Sealed | awk '{print $2}')

if [ "$status" = "true" ];
then
      echo "$keys" | while read utoken; do
      export VAULT_ADDR='http://127.0.0.1:8200' && vault operator unseal $utoken > /dev/null 2>&1
done
else
        echo "vault Unsealed already !!"
fi

################# Deployment ##################################################

token=$(cat /etc/vault/init.file | grep Token | cut -d ' ' -f 4)
#ip=$(ifconfig | grep -Eo 'inet (addr:)?([0-9]*\.){3}[0-9]*' | grep -Eo '([0-9]*\.){3}[0-9]*' | grep -v '127.0.0.1')
#ip=$(ifconfig eth0 | grep "inet " | grep -Eo 'inet (addr:)?([0-9]*\.){3}[0-9]*' | grep -Eo '([0-9]*\.){3}[0-9]*')
ip=$(ip addr | grep 'state UP' -A2 | tail -n1 | awk '{print $2}' | cut -f1  -d'/')
app=/srv/backend
jar=$app/AzureAccelerator-Java-0.0.1-SNAPSHOT.jar

sudo kill -9 $(ps aux | grep "java" | grep $port | awk '{print $2}') > /dev/null 2>&1
sleep 10

if [ -f "$jar" ]; then
  sudo -u root java -jar -Dserver.port=$port -Dvault.token=$token -Dvault.endpoint=http://$ip:8200 -Dspring.profiles.active=$profile $jar >> /$app/logs/nohup.txt &
fi