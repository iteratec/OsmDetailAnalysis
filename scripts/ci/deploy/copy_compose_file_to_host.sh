#!/usr/bin/env bash
# set -e

COMPOSE_BIN_FOLDER="~/OsmDetailAnalysis"
stderr_file="osm_${osmda_host}_stderr.txt"
echo "stderr_file=${stderr_file}"

echo "create directory for OsmDetailAnalysis on target host"
echo "####################################################"
echo "-> create directory $COMPOSE_BIN_FOLDER"
ssh -p $osmda_host_ssh_port -o "StrictHostKeyChecking no" $osmda_host_os_username@$osmda_host /bin/bash <<EOT
echo "" > /tmp/${stderr_file}
if [ ! -d "$COMPOSE_BIN_FOLDER" ]; then
	mkdir $COMPOSE_BIN_FOLDER
fi 2> /tmp/${stderr_file}
EOT

echo "get stderr from remote host"
scp -P $osmda_host_ssh_port -o "StrictHostKeyChecking no" $osmda_host_os_username@$osmda_host:"/tmp/$stderr_file" .
echo "-> stderr create folder START"
cat "./$stderr_file"
echo "-> stderr create folder END"

echo "copy compose file to target host"
echo "####################################################"
scp -P $osmda_host_ssh_port -o "StrictHostKeyChecking no" ./docker/docker-compose.yml $osmda_host_os_username@$osmda_host:"${COMPOSE_BIN_FOLDER}/"