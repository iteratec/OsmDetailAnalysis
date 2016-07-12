FROM java:openjdk-8
MAINTAINER nils.kuhn@iteratec.de, birger.kamp@iteratec.de

ENV OSM_DA_VERSION 0.1
ENV OSM_DA_HOME /osm_da
ENV OSM_CONFIG_HOME /home/osm_da/.grails
ENV JAVA_OPTS "-server -Dgrails.env=prod -Dfile.encoding=UTF-8"
ENV DOCKERIZE_VERSION v0.2.0

# add osm_da-user
RUN useradd -ms /bin/bash osm_da

# install dockerize
RUN wget https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz && \
    tar -C /usr/local/bin -xzvf dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz && \
    rm dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz

# get osm_da-sources and build war-file
RUN mkdir -p $OSM_DA_HOME $OSM_DA_HOME/logs $OSM_CONFIG_HOME
WORKDIR $OSM_DA_HOME
RUN wget https://github.com/iteratec/OsmDetailAnalysis/archive/master.zip && \
    unzip OsmDetailAnalysis-master.zip -d $OSM_DA_HOME && \
    rm -rf OsmDetailAnalysis-master.zip && \
    cd $OSM_DA_HOME/OsmDetailAnalysis-master && \
    ./gradlew assemble && \
    mv ./build/libs/*.war $OSM_DA_HOME/OsmDetailAnalysis.war && \
    cd $OSM_DA_HOME/ && \
    rm -r $OSM_DA_HOME/OsmDetailAnalysis-master /root/.gradle && \
    chown osm_da:osm_da -R $OSM_DA_HOME $OSM_CONFIG_HOME

# add osm_da config file
ADD templates/osm_da-config.yml.j2 $OSM_DA_CONFIG_HOME/OsmDetailAnalysis-config.yml.j2

# add entrypoint script
ADD entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

USER osm_da
VOLUME ["$OSM_DA_CONFIG_HOME","$OSM_DA_HOME/logs"]
EXPOSE 8080
ENTRYPOINT /entrypoint.sh
