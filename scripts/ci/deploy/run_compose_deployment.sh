#!/usr/bin/env bash

COMPOSE_BIN_FOLDER="~/OsmDetailAnalysis"
stderr_file="osm_${osmda_host}_stderr.txt"
echo "stderr_file=${stderr_file}"

echo "run compose deployment on target host"
echo "####################################################"
ssh -p $osmda_host_ssh_port -o "StrictHostKeyChecking no" $osmda_host_os_username@$osmda_host /bin/bash <<EOT
pwd
ls -la
#cd $COMPOSE_BIN_FOLDER
#echo "$SEU_JENKINS_ITERATEC_AT_OSM_HH_ITERATEC_DE" | sudo -S docker-compose stop 2> /tmp/${stderr_file}
#echo "$SEU_JENKINS_ITERATEC_AT_OSM_HH_ITERATEC_DE" | sudo -S docker-compose rm -f 2> /tmp/${stderr_file}
#echo "$SEU_JENKINS_ITERATEC_AT_OSM_HH_ITERATEC_DE" | sudo -S docker-compose pull 2> /tmp/${stderr_file}
#echo "$SEU_JENKINS_ITERATEC_AT_OSM_HH_ITERATEC_DE" | sudo -S docker-compose up -d 2> /tmp/${stderr_file}
EOT

echo "get stderr from remote host"
scp -P $osmda_host_ssh_port -o "StrictHostKeyChecking no" $osmda_host_os_username@$osmda_host:"/tmp/$stderr_file" .
echo "-> stderr create folder START"
cat "./$stderr_file"
echo "-> stderr create folder END"