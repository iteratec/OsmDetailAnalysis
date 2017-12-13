package de.iteratec.osm.da.migration.changeSets

import com.mongodb.MongoClient
import com.mongodb.client.AggregateIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import de.iteratec.osm.da.ConfigService
import de.iteratec.osm.da.migration.ChangeSet
import de.iteratec.osm.da.persistence.AssetRequestPersistenceService
import de.iteratec.osm.da.persistence.MongoDatabaseService
import org.bson.Document

import java.time.Duration
import java.time.Instant

class DA_V119_2017_12_13 extends ChangeSet {
    MongoClient mongo
    MongoDatabaseService mongoDatabaseService



    @Override
    Boolean execute() {
        log.debug("Remove unused 'name' field of osmInstance")
        mongoDatabaseService.getOsmInstanceCollection().updateMany([:],['$unset':["name":""]])
        log.debug('Create unique index on osmInstance domainPath')
        mongoDatabaseService.getOsmInstanceCollection().createIndex(['domainPath':1], [unique:true])
        return true
    }
}
