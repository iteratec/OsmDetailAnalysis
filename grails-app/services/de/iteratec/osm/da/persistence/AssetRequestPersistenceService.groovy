package de.iteratec.osm.da.persistence

import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import com.mongodb.client.model.Filters

import static com.mongodb.client.model.Accumulators.*
import de.iteratec.osm.da.asset.AssetRequestGroup
import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.wpt.WptDetailResultConvertService
import de.iteratec.osm.da.wpt.data.WptDetailResult
import grails.transaction.Transactional
import groovy.json.JsonOutput
import org.bson.Document

import static com.mongodb.client.model.Aggregates.*
import static com.mongodb.client.model.Filters.*

@Transactional
class AssetRequestPersistenceService {

    WptDetailResultConvertService wptDetailResultConvertService
    Document preFilterProjectionDocument
    Document unpackIdProjectionDocument
    MongoClient mongo
    /**
     * Parses a WptDetailResult and saves all Assets.
     * This will only happen if this result is not null and if there are steps within this result.
     * @param result JobResult which belongs to this HAR
     * @param har The HAR which belongs to this JobResult
     */
    public void saveDetailDataForJobResult(WptDetailResult result, FetchJob fetchJob) {
        if(result?.steps && !result.steps.isEmpty()){
            List<AssetRequestGroup> assetGroups = wptDetailResultConvertService.convertWPTDetailResultToAssetGroups(result, fetchJob)
            assetGroups.each {
                it.save(failOnError: true)
            }
        }
    }

    public String getRequestAssetsAsJson(
            Date from,
            Date to,
            List<Long> jobGroups,
            List<Long> pages,
            List<Long> browsers,
            boolean selectedAllBrowsers,
            List<Long> locations,
            boolean selectedAllLocations,
            boolean selectedAllConnectivityProfiles,
            Integer bandwidthUp,
            Integer bandwidthDown,
            Integer latency,
            Integer packetloss,
            List<Long> measuredEvents,
            boolean selectedAllMeasuredEvents
    ){
        List aggregateList = []
        List matchList = []
        def db = mongo.getDatabase("OsmDetailAnalysis")
        matchList << gte("epochTimeStarted", from.getTime() / 1000 as Long)
        matchList << lte("epochTimeStarted", to.getTime() / 1000 as Long)
        //Note that we use Filters.in because in groovy "in" is already a groovy method. So please don't listen to IntelliJ
        //We add only maps which are not empty, because the check if something is in a empty map would always fail.
        if (jobGroups) matchList << Filters.in("jobGroup", jobGroups)
        if (pages) matchList << Filters.in("page", pages)
        if (!selectedAllBrowsers && browsers) matchList << Filters.in("browser", browsers)
        if (!selectedAllLocations && locations) matchList << Filters.in("location", locations)
        if (!selectedAllMeasuredEvents && measuredEvents) matchList << Filters.in("measuredEvent", measuredEvents)
        if (!selectedAllConnectivityProfiles) {
            if (bandwidthUp) matchList << eq("bandwidthUp", bandwidthUp)
            if (bandwidthDown) matchList << eq("bandwidthDown", bandwidthDown)
            if (packetloss) matchList << eq("packetLoss", packetloss)
            if (latency) matchList << eq("Latency", latency)
        }


        aggregateList << match(and(matchList)) //filter out unwanted assets
        aggregateList << unwind("\$assets") // return one document for each asset in the asset group
        aggregateList << project(createPreFilterProjectDocument()) //filter out unwanted fields and flatten hierarchy
        aggregateList << group(['jobId'           : '\$jobId',
                                'jobGroup'        : '\$jobGroup',
                                'mediaType'       : '\$mediaType',
                                'browser'         : '\$browser',
                                'subtype'         : '\$subtype',
                                'epochTimeStarted': '\$epochTimeStarted',
                                measuredEvent     : '\$measuredEvent',
                                host              : '\$host', page: '\$page',] as BasicDBObject, //aggregate the assets by dimension
                                avg('loadTimeMs_avg', '\$loadTimeMs'), //add average load time
                                min('loadTimeMs_min', '\$loadTimeMs'), //add min load time
                                max('loadTimeMs_max', '\$loadTimeMs'), //add max load time
                                avg('ttfb_avg', '\$timeToFirstByteMs'), //add average ttfb
                                min('ttfb_min', '\$timeToFirstByteMs'), //add min ttfb
                                max('ttfb_max', '\$timeToFirstByteMs'), //add max ttfb
                                sum('count', 1)) //add sum of elements per aggregation
        aggregateList << project(createUnpackIdProjectDocument()) //flatten hierarchy
        return JsonOutput.toJson(db.getCollection("assetRequestGroup").aggregate(aggregateList).allowDiskUse(true))
    }

    /**
     * Creates a projection document. Since there is no builder support from the mongodb plugin,
     * we should only parse a string once to get a matching document.
     * @return
     */
    private Document createPreFilterProjectDocument() {
        if (preFilterProjectionDocument) return preFilterProjectionDocument
        preFilterProjectionDocument = Document.parse("""
                            {browser:'\$browser',
                             epochTimeStarted:'\$epochTimeStarted',
                             jobId: '\$jobId',
                             jobGroup: '\$jobGroup'
                             mediaType:'\$mediaType',
                             subtype:'\$assets.subtype',
                             loadTimeMs:'\$assets.loadTimeMs',
                             timeToFirstByteMs:'\$assets.timeToFirstByteMs',
                             measuredEvent:'\$measuredEvent',
                             host:'\$assets.host',
                             page:'\$page'
                            }""")
        return preFilterProjectionDocument
    }

    private Document createUnpackIdProjectDocument() {
        if (unpackIdProjectionDocument) return unpackIdProjectionDocument
        unpackIdProjectionDocument = Document.parse("""
                            {
                            _id:0
                            jobId:'\$_id.jobId',
                            jobGroup:'\$_id.jobGroup',
                            mediaType:'\$_id.mediaType',
                            browser:'\$_id.browser',
                            subtype:'\$_id.subtype',
                            epochTimeStarted:'\$_id.epochTimeStarted',
                            measuredEvent:'\$_id.measuredEvent',
                            host:'\$_id.host',
                            page:'\$_id.page',
                            loadTimeMs_avg:'\$loadTimeMs_avg',
                            loadTimeMs_min:'\$loadTimeMs_min',
                            loadTimeMs_max:'\$loadTimeMs_max',
                            ttfb_avg:'\$ttfb_avg',
                            ttfb_min:'\$ttfb_min',
                            ttfb_max:'\$ttfb_max',
                            count:'\$count'
                            }""")
        return unpackIdProjectionDocument
    }
}
