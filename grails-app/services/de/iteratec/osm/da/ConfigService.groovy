package de.iteratec.osm.da

import grails.core.GrailsApplication
import grails.util.Holders

class ConfigService {

    GrailsApplication grailsApplication = Holders.getGrailsApplication()
    def config = grailsApplication.config.grails.de.iteratec.osm
    def graphiteConfig = config.da.report.external.graphiteServer.serverAddress
    def mongodbConfig = config.mongodb

    final String DEFAULT_MONGODB_DATABASE_NAME = "OsmDetailAnalysis"
    final int DEFAULT_GRAPHITE_PORT = 203

    String getGraphiteServerAddress(){
        return graphiteConfig.serverAddress
    }

    int getGraphiteCarbonPort(){
        return graphiteConfig.carbonPort?:DEFAULT_GRAPHITE_PORT
    }

    def getApiKeys(){
        return config?.da?.apiKeys?:[]
    }

    String getMongoDbDatabaseName(){
        return mongodbConfig?.databaseName?:DEFAULT_MONGODB_DATABASE_NAME
    }
}
