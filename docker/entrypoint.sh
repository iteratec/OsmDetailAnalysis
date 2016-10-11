#!/bin/bash

echo "start application"
java -server -Dgrails.env=prod -Dfile.encoding=UTF-8 -Xms${OSMDA_JVM_XMS} -Xmx${OSMDA_JVM_XMX} -jar ${OSM_DA_HOME}/OsmDetailAnalysis.war
