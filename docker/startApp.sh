#!/usr/bin/env bash
export PUBLIC_PORT=8081
export OSM_JVM_XMS=1024m
export OSM_JVM_XMX=4096m
docker-compose up -d
