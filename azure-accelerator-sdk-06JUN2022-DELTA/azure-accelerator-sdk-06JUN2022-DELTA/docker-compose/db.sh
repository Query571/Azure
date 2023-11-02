#!/bin/bash

password=xoriant321

db=$(mysql -h localhost -u root -p${password} -e "show databases;" | grep azx > /dev/null 2>&1)

if [ "$db" != "azx" ]; then
    mysql -h localhost -u root -p${password} -e "create database azx;" > /dev/null 2>&1
    mysql -h localhost -u root -p${password} -e "use azx; source azx.sql;" > /dev/null 2>&1
    mysql -h localhost -u root -p${password} -e "CREATE USER 'root'@'%' IDENTIFIED BY '${password}';" > /dev/null 2>&1
    mysql -h localhost -u root -p${password} -e "GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;" > /dev/null 2>&1
    mysql -h localhost -u root -p${password} -e "FLUSH PRIVILEGES;" > /dev/null 2>&1
else
    echo ""
fi