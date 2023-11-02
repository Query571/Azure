#!/bin/bash
#!/bin/sh


sudo rm -rf /var/www/html
sudo mkdir -p /var/www/html
sudo chmod -R 777 /var/www/html

sudo cp -r /opt/updated_front/AzureUI/* /var/www/html