#!/bin/bash

resourceid=$*

#date=`date +"%m%d%Y%H"`
date=`date +"%s"`

clientid=$(echo "$resourceid" | cut -d " " -f 1)

clientsecret=$(echo "$resourceid" | cut -d " " -f 2)

tenantid=$(echo "$resourceid" | cut -d " " -f 3)

resourcegroup=$(echo "$resourceid" | cut -d " " -f 4)

resourcename=$(echo "$resourceid" | cut -d " " -f 5)

resourceid1=$(echo "$resourceid" | cut -d " " -f6-)

if [[ -f "export_*" ]]; then
	 rm -rf export_*
	az login --service-principal -u $clientid -p $clientsecret --tenant $tenantid > /dev/null 2>&1
	az group export --resource-group $resourcegroup --resource-ids $resourceid1 --include-parameter-default-value >> "export_"$resourcename"_"$date".json"
	if [[ -f "export_"$resourcename"_"$date".json" ]]; then
	  zip -r "export_"$resourcename"_"$date".zip" "export_"$resourcename"_"$date".json" > /dev/null 2>&1
	else
	  echo "export_"$resourcename"_"$date".json not found !!!."
	  exit 0
	fi
	if [[ -f "export_"$resourcename"_"$date".zip" ]]; then
	  mkdir -p /var/www/html/download
	  cp -r "export_"$resourcename"_"$date".zip" /var/www/html/download
	  if [[ -f /var/www/html/download/"export_"$resourcename"_"$date".zip" ]]; then
	    echo "export_"$resourcename"_"$date".zip"
	  else
	    echo "Some issue with export template. Please try again later."
	    exit 0
	  fi
	else
	  echo "issue in exporting template !!!"
	  exit 0
	fi
else
	az login --service-principal -u $clientid -p $clientsecret --tenant $tenantid > /dev/null 2>&1
	az group export --resource-group $resourcegroup --resource-ids $resourceid1 --include-parameter-default-value >> "export_"$resourcename"_"$date".json"
	if [[ -f "export_"$resourcename"_"$date".json" ]]; then
	  sudo zip -r "export_"$resourcename"_"$date".zip" "export_"$resourcename"_"$date".json" > /dev/null 2>&1
	else
	  echo "export_"$resourcename"_"$date".json not found !!!."
	  exit 0
	fi
	if [[ -f "export_"$resourcename"_"$date".zip" ]]; then
	  mkdir -p /var/www/html/download
	  cp -r "export_"$resourcename"_"$date".zip" /var/www/html/download
	  if [[ -f /var/www/html/download/"export_"$resourcename"_"$date".zip" ]]; then
	    echo "export_"$resourcename"_"$date".zip"
	  else
	    echo "Some issue with export template. Please try again later."
	    exit 0
	  fi
	else
	  echo "issue in exporting template !!!"
	  exit 0
	fi
fi