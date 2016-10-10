#!/bin/bash

# prepare osm config respective set env variables
OSM_DA_CONFIG_TARGET_LOCATION="${OSM_DA_CONFIG_HOME}/OsmDetailAnalysis-config.yml"

echo "complete osm detail analysis config file gets prepared"
dockerize -template $OSM_DA_CONFIG_TARGET_LOCATION.j2:$OSM_DA_CONFIG_TARGET_LOCATION

# start application
java -Dgrails.env=prod -Xms$OSM_JVM_XMS -Xmx$OSM_JVM_XMX -jar $OSM_DA_HOME/OsmDetailAnalysis*.war
