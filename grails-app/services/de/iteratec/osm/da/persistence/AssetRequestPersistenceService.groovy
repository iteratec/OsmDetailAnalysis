package de.iteratec.osm.da.persistence

import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import com.mongodb.client.AggregateIterable
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.util.JSON
import de.iteratec.osm.da.asset.AggregatedAssetGroup
import de.iteratec.osm.da.asset.AssetRequest
import de.iteratec.osm.da.instances.OsmInstance

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

class AssetRequestPersistenceService {

    WptDetailResultConvertService wptDetailResultConvertService
    Document preFilterForCompleteAssetRequest
    Document preFilterProjectionDocument
    Document unpackIdProjectionDocument
    def grailsApplication
    MongoClient mongo
    /**
     * Parses a WptDetailResult and saves all Assets.
     * This will only happen if this result is not null and if there are steps within this result.
     * Also if a mapping is not available, the whole result won't be saved.
     * @param result JobResult which belongs to this HAR
     * @param har The HAR which belongs to this JobResult
     */
    public void saveDetailDataForJobResult(WptDetailResult result, FetchJob fetchJob) {
        if(result?.steps && !result.steps.isEmpty()){
            List<AssetRequestGroup> assetGroups = wptDetailResultConvertService.convertWPTDetailResultToAssetGroups(result, fetchJob)
            if(!assetGroups.contains(null)){
                //If there where a null value, we know that a mapping wasnt available and the whole result can be ignored.
                assetGroups.each {
                    it.save(failOnError: true, flush: true)
                    saveAssetGroupAsAggregatedAssetGroups(it)
                }
            }
        }
    }
    def saveAssetGroupAsAggregatedAssetGroups( AssetRequestGroup assetRequestGroup){
        def groups = assetRequestGroup.assets.groupBy(
                {it.mediaType},
                {it.subtype},
                {it.url})
        groups.each {mediaType,subTypes->
            subTypes.each{subType, urls ->
                urls.each{url, List<AssetRequest> assets ->
                    AggregatedAssetGroup aggregatedAssetGroup = new AggregatedAssetGroup()
                    aggregatedAssetGroup.jobGroup = assetRequestGroup.jobGroup
                    aggregatedAssetGroup.mediaType = mediaType
                    aggregatedAssetGroup.browser = assetRequestGroup.browser
                    aggregatedAssetGroup.subtype = subType
                    aggregatedAssetGroup.epochTimeStarted =assetRequestGroup.epochTimeStarted
                    aggregatedAssetGroup.measuredEvent = assetRequestGroup.measuredEvent
                    aggregatedAssetGroup.host = assets[0].host
                    aggregatedAssetGroup.page = assetRequestGroup.page
                    aggregatedAssetGroup.loadTimeMs_avg = assets.loadTimeMs.sum()/assets.loadTimeMs.size()
                    aggregatedAssetGroup.loadTimeMs_min = assets.loadTimeMs.min()
                    aggregatedAssetGroup.loadTimeMs_max = assets.loadTimeMs.max()
                    aggregatedAssetGroup.ttfb_avg = assets.timeToFirstByteMs.sum()/assets.timeToFirstByteMs.size()
                    aggregatedAssetGroup.ttfb_min = assets.timeToFirstByteMs.min()
                    aggregatedAssetGroup.ttfb_max = assets.timeToFirstByteMs.max()
                    aggregatedAssetGroup.downloadTime_avg = assets.downloadTimeMs.sum()/assets.downloadTimeMs.size()
                    aggregatedAssetGroup.downloadTime_min = assets.downloadTimeMs.min()
                    aggregatedAssetGroup.downloadTime_max = assets.downloadTimeMs.max()
                    aggregatedAssetGroup.sslTime_avg = assets.sslNegotiationTimeMs.sum()/assets.sslNegotiationTimeMs.size()
                    aggregatedAssetGroup.sslTime_min = assets.sslNegotiationTimeMs.min()
                    aggregatedAssetGroup.sslTime_max = assets.sslNegotiationTimeMs.max()
                    aggregatedAssetGroup.connectTime_avg = assets.connectTimeMs.sum()/assets.connectTimeMs.size()
                    aggregatedAssetGroup.connectTime_min = assets.connectTimeMs.min()
                    aggregatedAssetGroup.connectTime_max = assets.connectTimeMs.max()
                    aggregatedAssetGroup.dnsTime_avg = assets.dnsMs.sum()/assets.dnsMs.size()
                    aggregatedAssetGroup.dnsTime_min = assets.dnsMs.min()
                    aggregatedAssetGroup.dnsTime_max = assets.dnsMs.max()
                    aggregatedAssetGroup.bytesIn_avg = assets.bytesIn.sum()/assets.bytesIn.size()
                    aggregatedAssetGroup.bytesIn_min = assets.bytesIn.min()
                    aggregatedAssetGroup.bytesIn_max = assets.bytesIn.max()
                    aggregatedAssetGroup.bytesOut_avg = assets.bytesOut.sum()/assets.bytesOut.size()
                    aggregatedAssetGroup.bytesOut_min = assets.bytesOut.min()
                    aggregatedAssetGroup.bytesOut_max = assets.bytesOut.max()
                    aggregatedAssetGroup.count = assets.size()
                    aggregatedAssetGroup.osmInstance = assetRequestGroup.osmInstance
                    aggregatedAssetGroup.save(failOnError:true, flush:true)
                }
            }
        }
    }

    public String getCompleteAssets(
            Date timestamp,
            List hosts,
            List browsers,
            List mediaTypes,
            List subtypes,
            List jobGroups,
            List pages
    ) {
        log.debug("${timestamp.time / 1000 as Long}${hosts}${browsers}${mediaTypes}${subtypes}${jobGroups}${pages}")
        List aggregateList = []
        List initialMatchList = []
        List matchListAfterUnwind = []
        def databaseName = grailsApplication.config.grails?.mongodb?.databaseName
        databaseName = databaseName?databaseName:"OsmDetailAnalysis"
        MongoDatabase db = mongo.getDatabase(databaseName)
        initialMatchList << eq("epochTimeStarted", timestamp.time / 1000 as Long)
        //Note that we use Filters.in because in groovy "in" is already a groovy method. So please don't listen to IntelliJ
        //We add only maps which are not empty, because the check if something is in a empty map would always fail.
        if (jobGroups) initialMatchList << Filters.in("jobGroup", jobGroups)
        if (pages) initialMatchList << Filters.in("page", pages)
        if (browsers) initialMatchList << Filters.in("browser", browsers)
        if (mediaTypes) initialMatchList << Filters.in("mediaType", mediaTypes)
        if (hosts) matchListAfterUnwind << Filters.in("host", hosts)
        if (subtypes) matchListAfterUnwind << Filters.in("subtype", subtypes)

        aggregateList << match(and(initialMatchList)) //filter out unwanted assets
        aggregateList << unwind("\$assets") // return one document for each asset in the asset group
        aggregateList << project(createPreFilterForCompleteAssetRequest()) //filter out unwanted fields and flatten hierarchy
        if (matchListAfterUnwind) aggregateList << match(and(matchListAfterUnwind)) //filter out unwanted assets
        def results = db.getCollection("assetRequestGroup").aggregate(aggregateList).allowDiskUse(true)
        log.debug("Found ${results.size()} assets.")
        return JsonOutput.toJson(results)
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
            boolean selectedAllMeasuredEvents,
            String osmUrl

    ){
        log.debug("Querying for from = ${from} to = ${to} jobGroups = ${jobGroups} pages = ${pages} browsers = ${browsers} selectedAllBrowsers = ${selectedAllBrowsers} locations = ${locations} selectedAllLocations = ${selectedAllLocations} selectedAllConnectivityProfiles = ${selectedAllConnectivityProfiles} bandwidthUp = ${bandwidthUp} bandwidthDown = ${bandwidthDown} latency = ${latency} packetloss = ${packetloss} measuredEvents = ${measuredEvents} selectedAllMeasuredEvents = ${selectedAllMeasuredEvents}")
        List aggregateList = []
        List matchList = []
        def databaseName = grailsApplication.config.grails?.mongodb?.databaseName
        databaseName = databaseName?databaseName:"OsmDetailAnalysis"
        MongoDatabase db = mongo.getDatabase(databaseName)
        def osmUrlParam = osmUrl.endsWith("/")?osmUrl:osmUrl+"/"
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
            if (latency) matchList << eq("latency", latency)
        }
        matchList << eq ("osmInstance", OsmInstance.findByUrl(osmUrlParam).id)
        aggregateList << match(and(matchList)) //filter out unwanted assets
        AggregateIterable<Document> resultList = db.getCollection("aggregatedAssetGroup").aggregate(aggregateList).allowDiskUse(true)
        def numberOfResults = resultList.size()
        log.debug("Found ${numberOfResults} results.")
        return JsonOutput.toJson(resultList)
    }
    public String getRequestAssetsAsJsonOld(
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
            boolean selectedAllMeasuredEvents,
            String osmUrl
    ){
        log.debug("Querying for from = ${from} to = ${to} jobGroups = ${jobGroups} pages = ${pages} browsers = ${browsers} selectedAllBrowsers = ${selectedAllBrowsers} locations = ${locations} selectedAllLocations = ${selectedAllLocations} selectedAllConnectivityProfiles = ${selectedAllConnectivityProfiles} bandwidthUp = ${bandwidthUp} bandwidthDown = ${bandwidthDown} latency = ${latency} packetloss = ${packetloss} measuredEvents = ${measuredEvents} selectedAllMeasuredEvents = ${selectedAllMeasuredEvents}")
        List aggregateList = []
        List matchList = []
        def databaseName = grailsApplication.config.grails?.mongodb?.databaseName
        databaseName = databaseName?databaseName:"OsmDetailAnalysis"
        MongoDatabase db = mongo.getDatabase(databaseName)
        def osmUrlParam = osmUrl.endsWith("/")?osmUrl:osmUrl+"/"
        matchList << gte("epochTimeStarted", from.getTime() / 1000 as Long)
        matchList << lte("epochTimeStarted", to.getTime() / 1000 as Long)
        //Note that we use Filters.in because in groovy "in" is already a groovy method. So please don't listen to IntelliJ
        //We add only maps which are not empty, because the check if something is in a empty map would always fail.
        if (jobGroups) matchList << Filters.in("jobGroup", jobGroups)
        if (pages) matchList << Filters.in("page", pages)
        if (!selectedAllBrowsers && browsers) matchList << Filters.in("browser", browsers)
        if (!selectedAllLocations && locations) matchList << Filters.in("location", locations)
        if (!selectedAllMeasuredEvents && measuredEvents) matchList << Filters.in("measuredEvent", measuredEvents)
        matchList << eq ("osmInstance", OsmInstance.findByUrl(osmUrlParam).id)
        if (!selectedAllConnectivityProfiles) {
            if (bandwidthUp) matchList << eq("bandwidthUp", bandwidthUp)
            if (bandwidthDown) matchList << eq("bandwidthDown", bandwidthDown)
            if (packetloss) matchList << eq("packetLoss", packetloss)
            if (latency) matchList << eq("latency", latency)
        }



        aggregateList << match(and(matchList)) //filter out unwanted assets
        aggregateList << unwind("\$assets") // return one document for each asset in the asset group
        aggregateList << project(createPreFilterProjectDocument()) //filter out unwanted fields and flatten hierarchy
        aggregateList << group(['jobGroup'        : '\$jobGroup',
                                'mediaType'       : '\$mediaType',
                                'browser'         : '\$browser',
                                'subtype'         : '\$subtype',
                                'epochTimeStarted': '\$epochTimeStarted',
                                'measuredEvent'   : '\$measuredEvent',
                                'host'            : '\$host',
                                'page': '\$page',] as BasicDBObject, //aggregate the assets by dimension
                                avg('loadTimeMs_avg',   '\$loadTimeMs'), //add average load time
                                min('loadTimeMs_min',   '\$loadTimeMs'), //add min load time
                                max('loadTimeMs_max',   '\$loadTimeMs'), //add max load time
                                avg('ttfb_avg',         '\$timeToFirstByteMs'), //add average ttfb
                                min('ttfb_min',         '\$timeToFirstByteMs'), //add min ttfb
                                max('ttfb_max',         '\$timeToFirstByteMs'), //add max ttfb
                                avg('downloadTime_avg', '\$downloadTimeMs'), //add average downloadTime
                                min('downloadTime_min', '\$downloadTimeMs'), //add min downloadTime
                                max('downloadTime_max', '\$downloadTimeMs'), //add max downloadTime
                                avg('sslTime_avg', '\$sslTime'), //add average sslNegotiationTime
                                min('sslTime_min', '\$sslTime'), //add min sslNegotiationTime
                                max('sslTime_max', '\$sslTime'), //add max sslNegotiationTime
                                avg('connectTime_avg', '\$connectTime'), //add average sslNegotiationTime
                                min('connectTime_min', '\$connectTime'), //add min sslNegotiationTime
                                max('connectTime_max', '\$connectTime'), //add max sslNegotiationTime
                                avg('dnsTime_avg', '\$dnsTime'), //add average sslNegotiationTime
                                min('dnsTime_min', '\$dnsTime'), //add min sslNegotiationTime
                                max('dnsTime_max', '\$dnsTime'), //add max sslNegotiationTime
                                avg('bytesIn_avg', '\$bytesIn'), //add average sslNegotiationTime
                                min('bytesIn_min', '\$bytesIn'), //add min sslNegotiationTime
                                max('bytesIn_max', '\$bytesIn'), //add max sslNegotiationTime                               
                                avg('bytesOut_avg', '\$bytesOut'), //add average sslNegotiationTime
                                min('bytesOut_min', '\$bytesOut'), //add min sslNegotiationTime
                                max('bytesOut_max', '\$bytesOut'), //add max sslNegotiationTime
                                sum('count', 1)) //add sum of elements per aggregation
        aggregateList << project(createUnpackIdProjectDocument()) //flatten hierarchy
        AggregateIterable<Document> resultList = db.getCollection("assetRequestGroup").aggregate(aggregateList).allowDiskUse(true)
        def numberOfResults = resultList.size()
        log.debug("Found ${numberOfResults} results.")
        return JsonOutput.toJson(resultList)
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
                             jobGroup: '\$jobGroup'
                             mediaType:'\$mediaType',
                             subtype:'\$assets.subtype',
                             loadTimeMs:'\$assets.loadTimeMs',
                             dnsTime:'\$assets.dnsMs',
                             sslTime: '\$assets.sslNegotiationTimeMs'
                             connectTime: '\$assets.connectTimeMs'
                             timeToFirstByteMs:'\$assets.timeToFirstByteMs',
                             downloadTimeMs:'\$assets.downloadTimeMs',
                             measuredEvent:'\$measuredEvent',
                             host:'\$assets.host',
                             bytesIn:'\$assets.bytesIn',
                             bytesOut:'\$assets.bytesOut',
                             page:'\$page'
                            }""")
        return preFilterProjectionDocument
    }

    private Document createPreFilterForCompleteAssetRequest() {
        if (preFilterForCompleteAssetRequest) return preFilterForCompleteAssetRequest
        preFilterForCompleteAssetRequest = Document.parse("""
                            {bandwidthDown:'\$connectivity.bandwidthDown',
                             bandwidthUp: '\$connectivity.bandwidthUp',
                             latency: '\$connectivity.latency',
                             packetLoss: '\$connectivity.packetLoss',
                             browser:'\$browser',
                             epochTimeStarted:'\$epochTimeStarted',
                             eventName:'\$eventName',
                             isFirstViewInStep:'\$isFirstViewInStep',
                             jobGroup: '\$jobGroup'
                             location:'\$location',
                             measuredEvent:'\$measuredEvent',
                             mediaType:'\$mediaType',
                             page:'\$page',
                             wptBaseUrl:'\$wptBaseUrl',
                             wptTestId:'\$wptTestId',
                             bytesIn:'\$assets.bytesIn',
                             bytesOut:'\$assets.bytesOut',
                             connectTime: '\$assets.connectTimeMs'
                             dnsTime:'\$assets.dnsMs',
                             downloadTimeMs:'\$assets.downloadTimeMs',
                             host:'\$assets.host',
                             loadTimeMs:'\$assets.loadTimeMs',
                             sslTime: '\$assets.sslNegotiationTimeMs'
                             subtype:'\$assets.subtype',
                             timeToFirstByteMs:'\$assets.timeToFirstByteMs',
                             urlWithoutParams: '\$assets.urlWithoutParams'
                            }""")
        return preFilterForCompleteAssetRequest
    }

    private Document createUnpackIdProjectDocument() {
        if (unpackIdProjectionDocument) return unpackIdProjectionDocument
        unpackIdProjectionDocument = Document.parse("""
                            {
                            _id:0
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
                            downloadTime_avg:'\$downloadTime_avg',
                            downloadTime_min:'\$downloadTime_min',
                            downloadTime_max:'\$downloadTime_max',
                            sslTime_avg:'\$sslTime_avg',
                            sslTime_min:'\$sslTime_min',
                            sslTime_max:'\$sslTime_max',
                            connectTime_avg:'\$connectTime_avg',
                            connectTime_min:'\$connectTime_min',
                            connectTime_max:'\$connectTime_max',
                            dnsTime_avg:'\$dnsTime_avg',
                            dnsTime_min:'\$dnsTime_min',
                            dnsTime_max:'\$dnsTime_max',
                            bytesIn_avg:'\$bytesIn_avg',
                            bytesIn_min:'\$bytesIn_min',
                            bytesIn_max:'\$bytesIn_max',
                            bytesOut_avg:'\$bytesOut_avg',
                            bytesOut_min:'\$bytesOut_min',
                            bytesOut_max:'\$bytesOut_max',
                            count:'\$count'
                            }""")
        return unpackIdProjectionDocument
    }

    def List<AssetRequestGroup> getAssetRequestGroups(String wptBaseUrl, String wptTestId, String measuredEvent){
        wptDetailResultConvertService.getEventName(measuredEvent)
        return AssetRequestGroup.findAllByWptBaseUrlAndWptTestId(wptBaseUrl,wptTestId,measuredEvent)
    }
}
