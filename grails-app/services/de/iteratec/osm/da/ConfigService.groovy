package de.iteratec.osm.da

import grails.core.GrailsApplication
import grails.util.Holders

class ConfigService {

    GrailsApplication grailsApplication = Holders.getGrailsApplication()
    def config = grailsApplication.config.grails.de.iteratec.osm
    def graphiteConfig = config.da.report.external.graphiteServer.serverAddress
    def mongodbConfig = config.mongodb

    final String DEFAULT_MONGODB_DATABASE_NAME = "OsmDetailAnalysis"
    final int DEFAULT_GRAPHITE_PORT = 2003
    final int DEFAULT_DOWNLOAD_THREADS = 40
    final int DEFAULT_QUEUE_MAXIMUM = 1000

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

    int getDownloadThreadCount(){
        def amount = config?.da?.downloadThreads
        if (amount == 0) return amount
        return amount?:DEFAULT_DOWNLOAD_THREADS
    }

    int getDownloadQueueMaximum(){
        return config?.da?.downloadQueueMaximumSize?:DEFAULT_QUEUE_MAXIMUM
    }
}
