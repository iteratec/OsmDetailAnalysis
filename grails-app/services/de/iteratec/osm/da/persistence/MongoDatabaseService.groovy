package de.iteratec.osm.da.persistence

import com.mongodb.MongoBulkWriteException
import com.mongodb.client.MongoCollection
import de.iteratec.osm.da.ConfigService

class MongoDatabaseService {

    ConfigService configService
    def mongo
    MongoCollection aggregatedAssetGroupCollection
    MongoCollection fetchJobCollection
    MongoCollection assetRequestGroupCollection

    MongoCollection getAggregatedAssetGroupCollection(){
        if(!this.@aggregatedAssetGroupCollection) aggregatedAssetGroupCollection = getCollection("aggregatedAssetGroup")
        return this.@aggregatedAssetGroupCollection
    }

    MongoCollection getFetchJobCollection(){
        if(!this.@fetchJobCollection) fetchJobCollection = getCollection("fetchJob")
        return this.@fetchJobCollection
    }

    MongoCollection getAssetRequestGroupCollection(){
        if(!this.@assetRequestGroupCollection) assetRequestGroupCollection = getCollection('assetRequestGroup')
        return this.@assetRequestGroupCollection
    }


    MongoCollection getCollection(String name){
        return mongo.getDatabase(configService.getMongoDbDatabaseName()).getCollection(name)
    }

    void saveAndSkipIfDuplicate(MongoCollection collection, Map objectToSave) {
        try {
            collection.insert(objectToSave)

        } catch (MongoBulkWriteException e) {
            if (e.writeErrors*.code.contains(11000)) {//11000=duplicate found
//                that 's fine, we don' t want duplicates
                log.debug("Found ${collection.name} duplicate, won't save")
            } else {
                e.printStackTrace()
            }
        }
    }

}
