#!/bin/bash

echo "start application"
java -Dgrails.env=prod -Xms$OSM_JVM_XMS -Xmx$OSM_JVM_XMX -jar $OSM_DA_HOME/OsmDetailAnalysis*.war
