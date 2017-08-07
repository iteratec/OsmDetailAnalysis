package de.iteratec.osm.da.migration.changeSets

import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import com.mongodb.client.MongoDatabase
import de.iteratec.osm.da.migration.ChangeSet

class DA_V110_2017_08_07 extends ChangeSet {
    MongoClient mongo
    def grailsApplication

    @Override
    Boolean execute() {
        def databaseName = grailsApplication.config.grails?.mongodb?.databaseName
        databaseName = databaseName ? databaseName : "OsmDetailAnalysis"
        MongoDatabase db = mongo.getDatabase(databaseName)
        def collection = db.getCollection("osmInstance")
        collection.find().each{
            String url = it.url
            String protocol
            String path
            switch (url){
                case ~/http:\/\/.+/:
                    path = url.replace("http://","")
                    protocol = "http"
                    break
                case ~/https:\/\/.+/:
                    path = url.replace("https://","")
                    protocol = "https"
                    break
            }
            it.path = path
            it.protocol = protocol
            BasicDBObject updateFields = new BasicDBObject()
            updateFields.append("\$set", new BasicDBObject().append("domainPath",path).append("protocol",protocol))
            updateFields.append("\$unset", new BasicDBObject("url",""))
            collection.updateOne(new BasicDBObject("_id", it._id), updateFields)
        }
        return true
    }
}
