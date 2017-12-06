package de.iteratec.osm.da.persistence

import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import com.mongodb.client.MongoDatabase

class DbCleanupService {

    MongoClient mongo
    def grailsApplication

    def deleteOldAssetData(Date maxDate){
        def databaseName = grailsApplication.config.grails?.mongodb?.databaseName
        databaseName = databaseName ? databaseName : "OsmDetailAnalysis"
        MongoDatabase db = mongo.getDatabase(databaseName)
        def assetRequestGroup = db.getCollection("assetRequestGroup")
        def aggregatedAssetGroup = db.getCollection("aggregatedAssetGroup")

        log.debug("Delete all AssetRequestGroups and AggregatedAssetGroups fetched before $maxDate")
        BasicDBObject updateFields = new BasicDBObject("dateOfPersistence", new BasicDBObject("\$lt",maxDate))
        def aggregatedAssetGroupRemoveResponse = aggregatedAssetGroup.remove(updateFields)
        log.debug("Removed ${aggregatedAssetGroupRemoveResponse.deletedCount} AggregatedAssetGroups")
        def assetRequestGroupRemoveResponse = assetRequestGroup.remove(updateFields)
        log.debug("Removed ${assetRequestGroupRemoveResponse.deletedCount} AssetRequestGroups")
    }
}
