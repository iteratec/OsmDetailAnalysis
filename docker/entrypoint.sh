#!/bin/bash

# prepare osm config respective set env variables
OSM_DA_CONFIG_TARGET_LOCATION="${OSM_DA_CONFIG_HOME}/OsmDetailAnalysis-config.yml"

echo "complete osm detail analysis config file gets prepared"
dockerize -template $OSM_DA_CONFIG_TARGET_LOCATION.j2:$OSM_DA_CONFIG_TARGET_LOCATION

echo "define stdout and stderr"
dockerize -stdout ${OSM_DA_HOME}/logs/OsmDetailAnalysisDetails.log -stderr ${OSM_DA_HOME}/logs/OsmDetailAnalysis.log

echo "wait for mongodb container to run"
dockerize -wait tcp://osm_mongodb:27017 -timeout 240s

echo "start application"
java -Dgrails.env=prod -Xms$OSM_JVM_XMS -Xmx$OSM_JVM_XMX -jar $OSM_DA_HOME/OsmDetailAnalysis*.war
