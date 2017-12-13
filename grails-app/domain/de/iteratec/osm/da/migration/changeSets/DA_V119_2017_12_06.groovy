package de.iteratec.osm.da.migration.changeSets

import com.mongodb.MongoClient
import com.mongodb.client.AggregateIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import de.iteratec.osm.da.ConfigService
import de.iteratec.osm.da.migration.ChangeSet
import de.iteratec.osm.da.persistence.AssetRequestPersistenceService
import org.bson.Document

import java.time.Duration
import java.time.Instant

class DA_V119_2017_12_06 extends ChangeSet {
    MongoClient mongo
    ConfigService configService
    AssetRequestPersistenceService assetRequestPersistenceService

    String databaseName = configService.getMongoDbDatabaseName()
    MongoDatabase db = mongo.getDatabase(databaseName)
    MongoCollection<Document> assetRequestGroup = db.getCollection("assetRequestGroup")
    MongoCollection<Document> fetchJob = db.getCollection("fetchJob")
    MongoCollection<Document> aggregatedAssetGroup = db.getCollection("aggregatedAssetGroup")
    String tmpCollAssetRG = 'tmpARQ1191206'
    String tmpCollFetchJob = 'tmpFJ1191206'


    @Override
    Boolean execute() {
        db.getCollection(tmpCollAssetRG).drop()
        db.getCollection(tmpCollFetchJob).drop()
        def duplicatesOfAssetRequestGroup = getDuplicateList(assetRequestGroup, tmpCollAssetRG)
        removeGivenDuplicates(assetRequestGroup, duplicatesOfAssetRequestGroup)
        ensureUniqueIndex(assetRequestGroup, ["osmInstance": 1, "wptBaseUrl": 1, "wptTestId": 1, "measuredEvent": 1, "mediaType": 1])

        def duplicatesOfFetchJob = getDuplicateList(fetchJob, tmpCollFetchJob)
        removeGivenDuplicates(fetchJob, duplicatesOfFetchJob)
        ensureUniqueIndex(fetchJob, ["osmInstance": 1, "wptBaseUrl": 1, "wptTestId": 1])

        recreateAggregatedAssetGroups()

        db.getCollection(tmpCollFetchJob).drop()
        db.getCollection(tmpCollAssetRG).drop()
        return true
    }

    void recreateAggregatedAssetGroups() {
        aggregatedAssetGroup.drop()
        ensureUniqueIndex(aggregatedAssetGroup, ["osmInstance": 1, "wptBaseUrl": 1, "wptTestId": 1, "measuredEvent": 1, "mediaType": 1])
        aggregatedAssetGroup.createIndex(["epochTimeStarted": -1, "jobGroup": 1, "page": 1, "browser": 1, "location": 1, "measuredEvent": 1])
        int counter = 0
        Instant before = Instant.now()
        log.debug "Start recreating aggregatedAssetGroup"
        long amount = assetRequestGroup.count()
        def batch = assetRequestGroup.find()
        batch.each {
            assetRequestPersistenceService.saveAssetGroupAsAggregatedAssetGroup(it)
            if (++counter % 5000 == 0) log.debug "processed $counter/$amount assetRequestGroups so far"

        }
        log.debug "Recreated and removed duplicates of aggregatedAssetGroup in ${Duration.between(before, Instant.now()).toMillis()} ms"
    }

    def ensureUniqueIndex(MongoCollection<Document> collection, Map fields) {
        log.debug "Create index of ${collection.name}"
        Instant before = Instant.now()
        collection.createIndex(fields, [unique: true])
        log.debug "Created index of ${collection.name} in ${Duration.between(before, Instant.now()).toMillis()} ms"
    }

    /**
     *
     * @param collection either fetchJob or assetRequestGroup
     * @param tmpCollection name of a collection to temporarily store the output
     * @return
     */
    def getDuplicateList(MongoCollection<Document> collection, String tmpCollection) {
        log.debug "Create duplicate list for ${collection.name}"
        Instant start = Instant.now()
        def groupDocument= new Document('$group',
                new Document('_id',
                        new Document('wptBaseUrl', '$wptBaseUrl')
                                .append('wptTestId', '$wptTestId')
                                .append('measuredEvent', '$measuredEvent')
                                .append('mediaType', '$mediaType')
                                .append('osmInstance', '$osmInstance')
                ).append('dups', new Document('$push', '$_id'))
                        .append('count', new Document('$sum', 1)))
        def matchDocument = new Document('$match', new Document('count', new Document('$gt', 1)))
        def outDocument = new Document('$out', tmpCollection)
        AggregateIterable<Document> duplicates = collection.aggregate(
                [
                        groupDocument,
                        matchDocument,
                        outDocument
                ]
        ).allowDiskUse(true)
        duplicates.size() //to actually execute the previous query
        def afterDuplicates = Instant.now()
        log.debug "Created duplicate list of ${collection.name}in ${Duration.between(start, afterDuplicates).toMillis()} ms"
        return duplicates
    }

    /**
     *
     * @param collection either fetchJob or assetRequestGroup
     * @param duplicateList
     */
    void removeGivenDuplicates(MongoCollection<Document> collection, def duplicateList) {
        log.debug "Remove duplicates of ${collection.name}"
        long amountOfDuplicates = 0
        Instant start = Instant.now()
        duplicateList.each {
            List duplicates = it.get('dups')
            duplicates = duplicates.subList(1, duplicates.size())
//remove the first element so we remove all except of one
            amountOfDuplicates += collection.deleteMany(com.mongodb.client.model.Filters.in("_id", duplicates)).getDeletedCount()
        }
        log.debug "Removed $amountOfDuplicates of ${collection.name} in ${Duration.between(start, Instant.now()).toMillis()} ms"
    }

}
