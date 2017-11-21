package de.iteratec.osm.da.migration.changeSets

import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import com.mongodb.client.MongoDatabase
import de.iteratec.osm.da.migration.ChangeSet

class DA_V119_2017_11_16 extends ChangeSet {
    MongoClient mongo
    def grailsApplication

    @Override
    Boolean execute() {
        def databaseName = grailsApplication.config.grails?.mongodb?.databaseName
        databaseName = databaseName ? databaseName : "OsmDetailAnalysis"
        MongoDatabase db = mongo.getDatabase(databaseName)
        def assetRequestGroup = db.getCollection("assetRequestGroup")
        def aggregatedAssetGroup = db.getCollection("aggregatedAssetGroup")
        Date currentDate = new Date()
        BasicDBObject updateFields = new BasicDBObject()
        updateFields.append("\$set", new BasicDBObject().append("dateOfPersistence",currentDate))
        assetRequestGroup.updateMany(new BasicDBObject(), updateFields)
        aggregatedAssetGroup.updateMany(new BasicDBObject(), updateFields)

        db.getCollection("failedFetchJob").drop()
        db.getCollection("failedFetchJob.next_id").drop()
        db.getCollection("fetchFailure").drop()
        db.getCollection("fetchFailure.next_id").drop()
        return true
    }
}
