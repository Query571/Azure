#!/bin/bash
#!/bin/sh

echo -----------------Building Core------------------------
cd AzureUI

sudo npm install
#sudo sed -i -e "s|'baseUrl': .*|'baseUrl': \"https://sandbox.azx.xoriant.com/azx/\",|g" src/environments/environment.prod.ts
sudo npm run ng -- build --prod

if [ $? != 0 ]
then
  echo "Build failure while running"
  exit 1
fi


sudo rm -rf /opt/updated_front
sudo mkdir -p /opt/updated_front
sudo chmod -R 777 /opt/updated_front
sudo cp -r dist/* /opt/updated_front