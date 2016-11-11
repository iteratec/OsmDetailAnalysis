#!/usr/bin/env bash
set -e

COMPOSE_BIN_FOLDER="/opt/OsmDetailAnalysis"
stderr_file="osm_${bamboo_OsmDetailAnalysis_Host}_stderr.txt"

echo "create directory for OsmDetailAnalysis on target host"
echo "####################################################"
echo "create directory"
ssh -p $bamboo_OsmDetailAnalysis_Port -o "StrictHostKeyChecking no" $bamboo_OsmDetailAnalysis_OsUsername@$bamboo_OsmDetailAnalysis_Host /bin/bash <<EOT
if [ ! -d "$COMPOSE_BIN_FOLDER" ]; then mkdir $COMPOSE_BIN_FOLDER fi 2> /tmp/${stderr_file}
EOT
echo "get stderr from remote host"
scp -P $bamboo_OsmDetailAnalysis_Port -o "StrictHostKeyChecking no" $bamboo_OsmDetailAnalysis_OsUsername@$bamboo_OsmDetailAnalysis_Host:"/tmp/$stderr_file" .
echo "stderr create folder START"
cat "./$stderr_file"
echo "stderr create folder END"

echo "copy compose file to target host"
echo "####################################################"
scp -P $bamboo_OsmDetailAnalysis_Port -o "StrictHostKeyChecking no" ./OsmDetailAnalysis/docker/docker-compose.yml $bamboo_OsmDetailAnalysis_OsUsername@$bamboo_OsmDetailAnalysis_Host:"${COMPOSE_BIN_FOLDER}/docker-compose.yml"