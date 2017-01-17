#!/usr/bin/env bash

COMPOSE_BIN_FOLDER="~/OsmDetailAnalysis"

echo "prepare environment-vars file for compose"
echo "####################################################"

echo "OSMDA_MONGODB_HOST=osm_mongodb" > ./docker/.env-osmda

echo "OSMDA_INITIAL_API_KEY=$OSMDA_API_KEY_PROD_ITERATEC" >> ./docker/.env-osmda
echo "OSMDA_INITIAL_OSM_URL=http://openspeedmonitor.wpt.iteratec.de" >> ./docker/.env-osmda

echo "OSMDA_INITIAL_API_KEY1=$OSMDA_API_KEY_DEV_ITERATEC" >> ./docker/.env-osmda
echo "OSMDA_INITIAL_OSM_URL1=http://dev.openspeedmonitor.wpt.iteratec.de" >> ./docker/.env-osmda

echo "OSMDA_INITIAL_API_KEY2=$OSMDA_API_KEY_OTTO" >> ./docker/.env-osmda
echo "OSMDA_INITIAL_OSM_URL2=https://iteraspeedmonitor.otto.wpt.iteratec.de" >> ./docker/.env-osmda

echo "OSMDA_JVM_XMS=1024m" >> ./docker/.env-osmda
echo "OSMDA_JVM_XMX=8192m" >> ./docker/.env-osmda

echo "copy environment-vars file to target host"
echo "####################################################"
scp -P $osmda_host_ssh_port -o "StrictHostKeyChecking no" ./docker/.env-osmda $osmda_host_os_username@$osmda_host:"${COMPOSE_BIN_FOLDER}/"