#!/bin/bash

if [ -z "$KEYSTORE" ] || [ -z "$KEYSTORE_PASSWORD" ]; then
    # start without keystore
    java -server -Dgrails.env=prod \
        -Dfile.encoding=UTF-8 \
        -Xms${OSMDA_JVM_XMS} -Xmx${OSMDA_JVM_XMX} \
        -jar ${OSM_DA_HOME}/OsmDetailAnalysis.war
else
    # start with keystore
    java -server -Dgrails.env=prod \
        -Dfile.encoding=UTF-8 \
        -Djavax.net.ssl.trustStore=${KEYSTORE} \
        -Djavax.net.ssl.trustStorePassword=${KEYSTORE_PASSWORD} \
        -Xms${OSMDA_JVM_XMS} -Xmx${OSMDA_JVM_XMX} \
        -jar ${OSM_DA_HOME}/OsmDetailAnalysis.war
fi