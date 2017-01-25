#!/usr/bin/env bash

# Exit  immediately  if  a pipeline (which may consist of a single simple command), a list, or a compound command
# (see SHELL GRAMMAR above),  exits with a non-zero status.
set -e
# After expanding each simple command, for command, case command, select command, or arithmetic for command,
# display the expanded value of PS4, followed by the command and its expanded  arguments  or  associated word list.
#set -x

COMPOSE_BIN_FOLDER="~/OsmDetailAnalysis"
stderr_file="osm_${osmda_host}_stderr.txt"
echo "stderr_file=${stderr_file}"

echo "create directory for OsmDetailAnalysis on target host"
echo "####################################################"
echo "-> create directory $COMPOSE_BIN_FOLDER"
ssh -p $osmda_host_ssh_port -o "StrictHostKeyChecking no" $SEU_JENKINS_ITERATEC_AT_OSM_HOSTS_USERNAME@$osmda_host /bin/bash <<EOT
echo "" > /tmp/${stderr_file}
if [ ! -d "$COMPOSE_BIN_FOLDER" ]; then
	mkdir $COMPOSE_BIN_FOLDER
fi 2> /tmp/${stderr_file}
EOT

echo "get stderr from remote host"
scp -P $osmda_host_ssh_port -o "StrictHostKeyChecking no" $SEU_JENKINS_ITERATEC_AT_OSM_HOSTS_USERNAME@$osmda_host:"/tmp/$stderr_file" .
echo "-> stderr create folder START"
cat "./$stderr_file"
echo "-> stderr create folder END"

echo "copy compose file to target host"
echo "####################################################"
scp -P $osmda_host_ssh_port -o "StrictHostKeyChecking no" ./docker/docker-compose.yml $SEU_JENKINS_ITERATEC_AT_OSM_HOSTS_USERNAME@$osmda_host:"${COMPOSE_BIN_FOLDER}/"