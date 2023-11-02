#!/bin/bash

cd AzureUI

sudo npm install
sudo npm run ng -- build --prod

sudo docker build --no-cache -t angular .