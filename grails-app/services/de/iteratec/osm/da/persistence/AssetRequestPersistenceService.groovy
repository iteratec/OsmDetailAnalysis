package de.iteratec.osm.da.persistence

import com.mongodb.MongoClient
import com.mongodb.client.AggregateIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import grails.converters.JSON
import groovy.json.JsonOutput
import org.bson.Document

import static com.mongodb.client.model.Aggregates.*;
import de.iteratec.osm.da.wpt.data.WptDetailResult
import de.iteratec.osm.da.asset.AssetRequestGroup
import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.wpt.WptDetailResultConvertService
import grails.transaction.Transactional

import static com.mongodb.client.model.Filters.and
import static com.mongodb.client.model.Filters.gte
import static com.mongodb.client.model.Filters.lte

@Transactional
class AssetRequestPersistenceService {

    WptDetailResultConvertService wptDetailResultConvertService
    Document projectionDocument
    MongoClient mongo
    /**
     * Parses a WptDetailResult and saves all Assets
     * @param result JobResult which belongs to this HAR
     * @param har The HAR which belongs to this JobResult
     */
    public void saveDetailDataForJobResult(WptDetailResult result, FetchJob fetchJob) {
        List<AssetRequestGroup> assetGroups = wptDetailResultConvertService.convertWPTDetailResultToAssetGroups(result, fetchJob)
        assetGroups.each {
            it.save(failOnError: true)
        }
    }

    public String getRequestAssetsAsJson(Date from, Date to, List<Long> jobGroups, List<Long> pages, List<Long> browser, List<Long> locations){
        //TODO implement connectivity check
        List aggregateList = []
        List matchList = []
        def db = mongo.getDatabase("OsmDetailAnalysis")
        matchList<< gte("epochTimeCompleted",from.getTime()/1000 as Long)
        matchList<< lte("epochTimeCompleted",to.getTime()/1000 as Long)
        //Note that we use Filters.in because in groovy "in" is already a groovy method. So please don't listen to IntelliJ
        //We add only maps which are not empty, because the check if something is in a empty map would always fail.
        if(jobGroups) matchList << Filters.in("jobGroup", jobGroups)
        if(pages)     matchList << Filters.in("page", pages)
        if(browser)   matchList << Filters.in("browser", browser)
        if(locations) matchList << Filters.in("location", locations)

        aggregateList << match(and(matchList))
        aggregateList << unwind("\$assets")
        aggregateList << project(createProjectDocument())

        return JsonOutput.toJson(db.getCollection("assetRequestGroup").aggregate(aggregateList).allowDiskUse(true))
    }

    /**
     * Creates a projection document. Since there is no builder support from the mongodb plugin,
     * we should only parse a string once to get a matching document.
     * @return
     */
    private Document createProjectDocument(){
        if(projectionDocument) return projectionDocument
        projectionDocument = Document.parse("""
                            {browser:'\$browser',
                             epochTimeCompleted:'\$epochTimeCompleted',
                             jobId: '\$jobId',
                             mediaType:'\$mediaType',
                             subtype:'\$assets.subtype',
                             loadTimeMs:'\$assets.loadTimeMs',
                             timeToFirstByteMs:'\$assets.timeToFirstByteMs',
                            }""")
        return projectionDocument
    }
}
