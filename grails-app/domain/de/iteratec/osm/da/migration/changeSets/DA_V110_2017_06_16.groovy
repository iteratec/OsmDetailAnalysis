package de.iteratec.osm.da.migration.changeSets

import com.mongodb.MongoClient
import com.mongodb.client.MongoDatabase
import de.iteratec.osm.da.migration.ChangeSet
import de.iteratec.osm.da.wpt.WptDetailResultDownloadService

class DA_V110_2017_06_16 extends ChangeSet {
    MongoClient mongo
    def grailsApplication

    @Override
    Boolean execute() {
        def databaseName = grailsApplication.config.grails?.mongodb?.databaseName
        databaseName = databaseName ? databaseName : "OsmDetailAnalysis"
        MongoDatabase db = mongo.getDatabase(databaseName)
        db.getCollection("failedFetchJob").remove([:])
        db.getCollection("fetchJob").remove(['tryCount': ['$gte': WptDetailResultDownloadService.MAX_TRY_COUNT]])
        return true
    }
}
