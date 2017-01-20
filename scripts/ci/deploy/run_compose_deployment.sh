#!/usr/bin/env bash

COMPOSE_BIN_FOLDER="~/OsmDetailAnalysis"
stderr_file="osm_${osmda_host}_stderr.txt"
echo "stderr_file=${stderr_file}"

echo "run compose deployment on target host"
echo "####################################################"
echo "PASSWORD_seu_jenkins_iteratec@osm.hh.iteratec.de=$SEU_JENKINS_ITERATEC_AT_OSM_HOSTS_PASSWORD"
echo "USERNAME_seu_jenkins_iteratec@osm.hh.iteratec.de=$SEU_JENKINS_ITERATEC_AT_OSM_HOSTS_USERNAME"
ssh -p $osmda_host_ssh_port -o "StrictHostKeyChecking no" $SEU_JENKINS_ITERATEC_AT_OSM_HOSTS_USERNAME@$osmda_host /bin/bash <<- EOT
    echo "" > /tmp/${stderr_file}
    cd $COMPOSE_BIN_FOLDER
    echo $SEU_JENKINS_ITERATEC_AT_OSM_HOSTS_PASSWORD | sudo -S docker-compose stop 2> /tmp/${stderr_file}
    echo $SEU_JENKINS_ITERATEC_AT_OSM_HOSTS_PASSWORD | sudo -S docker-compose rm -f 2> /tmp/${stderr_file}
    echo $SEU_JENKINS_ITERATEC_AT_OSM_HOSTS_PASSWORD | sudo -S docker-compose pull 2> /tmp/${stderr_file}
    echo $SEU_JENKINS_ITERATEC_AT_OSM_HOSTS_PASSWORD | sudo -S docker-compose up -d 2> /tmp/${stderr_file}
EOT

echo "get stderr from remote host"
scp -P $osmda_host_ssh_port -o "StrictHostKeyChecking no" $SEU_JENKINS_ITERATEC_AT_OSM_HOSTS_USERNAME@$osmda_host:"/tmp/$stderr_file" .
echo "-> stderr create folder START"
cat "./$stderr_file"
echo "-> stderr create folder END"