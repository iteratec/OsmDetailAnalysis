package de.iteratec.osm.da.persistence

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import de.iteratec.osm.da.asset.AssetRequest
import de.iteratec.osm.da.asset.AssetRequestGroup
import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.instances.OsmInstance
import de.iteratec.osm.da.wpt.WptDetailResultConvertService
import de.iteratec.osm.da.wpt.data.WptDetailResult
import groovy.json.JsonOutput
import org.bson.Document

import static com.mongodb.client.model.Aggregates.*
import static com.mongodb.client.model.Filters.*

class AssetRequestPersistenceService {

    MongoDatabaseService mongoDatabaseService
    WptDetailResultConvertService wptDetailResultConvertService
    Document preFilterForCompleteAssetRequest
    Document aggregationProjection



    /**
     * Parses a WptDetailResult and saves all Assets.
     * This will only happen if this result is not null and if there are steps within this result.
     * Also if a mapping is not available, the whole result won't be saved.
     * @param result JobResult which belongs to this HAR
     * @param har The HAR which belongs to this JobResult
     */
    public void saveDetailDataForJobResult(WptDetailResult result, FetchJob fetchJob) {
        MongoCollection<Document> assetRequestGroupCollection = mongoDatabaseService.assetRequestGroupCollection
        if (result?.steps && !result.steps.isEmpty()) {
            List<Map> assetGroups = wptDetailResultConvertService.convertWPTDetailResultToAssetGroups(result, fetchJob)
            if (!assetGroups.contains(null)) {
                //If there where a null value, we know that a mapping wasnt available and the whole result can be ignored.
                assetGroups.each {
                    mongoDatabaseService.saveAndSkipIfDuplicate(assetRequestGroupCollection, it)
                    saveAssetGroupAsAggregatedAssetGroup(it)
                }
            }
        }
    }
    /**
     * Saves an aggregation of the assets in a AssetRequestGroup used by dc.js.
     * @param assetRequestGroup
     */
    def saveAssetGroupAsAggregatedAssetGroup(def assetRequestGroup) {
        MongoCollection<Document> aggregatedAssetGroupCollection = mongoDatabaseService.getAggregatedAssetGroupCollection()

        def groups = assetRequestGroup.assets.groupBy(
                { it.mediaType },
                { it.subtype },
                { it.urlWithoutParams })
        def aggregatedAssetGroup = [:]
        aggregatedAssetGroup.wptBaseUrl = assetRequestGroup.wptBaseUrl
        aggregatedAssetGroup.wptTestId = assetRequestGroup.wptTestId
        aggregatedAssetGroup.mediaType = assetRequestGroup.mediaType
        aggregatedAssetGroup.dateOfPersistence = assetRequestGroup.dateOfPersistence
        aggregatedAssetGroup.jobGroup = assetRequestGroup.jobGroup
        aggregatedAssetGroup.mediaType = aggregatedAssetGroup.mediaType
        aggregatedAssetGroup.browser = assetRequestGroup.browser
        aggregatedAssetGroup.epochTimeStarted = assetRequestGroup.epochTimeStarted
        aggregatedAssetGroup.measuredEvent = assetRequestGroup.measuredEvent
        aggregatedAssetGroup.page = assetRequestGroup.page
        aggregatedAssetGroup.osmInstance = assetRequestGroup.osmInstance
        List aggregatedAssets = []
        aggregatedAssetGroup.aggregatedAssets = aggregatedAssets
        groups.each { mediaType, subTypes ->
            subTypes.each { subType, urls ->
                urls.each { url, List<AssetRequest> assets ->
                    def aggregatedAsset = [:]
                    aggregatedAsset.url = url
                    aggregatedAsset.subtype = subType
                    aggregatedAsset.host = assets[0].host
                    aggregatedAsset.loadTimeMs_avg = (assets.loadTimeMs.sum() / assets.loadTimeMs.size()) as int
                    aggregatedAsset.loadTimeMs_min = assets.loadTimeMs.min()
                    aggregatedAsset.loadTimeMs_max = assets.loadTimeMs.max()
                    aggregatedAsset.ttfb_avg = (assets.timeToFirstByteMs.sum() / assets.timeToFirstByteMs.size()) as int
                    aggregatedAsset.ttfb_min = assets.timeToFirstByteMs.min()
                    aggregatedAsset.ttfb_max = assets.timeToFirstByteMs.max()
                    aggregatedAsset.downloadTime_avg = (assets.downloadTimeMs.sum() / assets.downloadTimeMs.size()) as int
                    aggregatedAsset.downloadTime_min = assets.downloadTimeMs.min()
                    aggregatedAsset.downloadTime_max = assets.downloadTimeMs.max()
                    aggregatedAsset.sslTime_avg = (assets.sslNegotiationTimeMs.sum() / assets.sslNegotiationTimeMs.size()) as int
                    aggregatedAsset.sslTime_min = assets.sslNegotiationTimeMs.min()
                    aggregatedAsset.sslTime_max = assets.sslNegotiationTimeMs.max()
                    aggregatedAsset.connectTime_avg = (assets.connectTimeMs.sum() / assets.connectTimeMs.size()) as int
                    aggregatedAsset.connectTime_min = assets.connectTimeMs.min()
                    aggregatedAsset.connectTime_max = assets.connectTimeMs.max()
                    aggregatedAsset.dnsTime_avg = (assets.dnsMs.sum() / assets.dnsMs.size()) as int
                    aggregatedAsset.dnsTime_min = assets.dnsMs.min()
                    aggregatedAsset.dnsTime_max = assets.dnsMs.max()
                    aggregatedAsset.bytesIn_avg = (assets.bytesIn.sum() / assets.bytesIn.size()) as int
                    aggregatedAsset.bytesIn_min = assets.bytesIn.min()
                    aggregatedAsset.bytesIn_max = assets.bytesIn.max()
                    aggregatedAsset.bytesOut_avg = (assets.bytesOut.sum() / assets.bytesOut.size()) as int
                    aggregatedAsset.bytesOut_min = assets.bytesOut.min()
                    aggregatedAsset.bytesOut_max = assets.bytesOut.max()
                    aggregatedAsset.count = assets.size()
                    aggregatedAssets << aggregatedAsset
                }
            }
        }
        mongoDatabaseService.saveAndSkipIfDuplicate(aggregatedAssetGroupCollection, aggregatedAssetGroup)
        return aggregatedAssetGroup
    }



    public String getCompleteAssets(
            Date timestamp,
            List hosts,
            List browsers,
            List mediaTypes,
            List subtypes,
            List jobGroups,
            List pages,
            Long osmInstance
    ) {
        log.debug("${timestamp.time / 1000 as Long}${hosts}${browsers}${mediaTypes}${subtypes}${jobGroups}${pages}")
        List aggregateList = []
        List initialMatchList = []
        List matchListAfterUnwind = []
        initialMatchList << eq("epochTimeStarted", timestamp.time / 1000 as Long)
        initialMatchList << eq("osmInstance", osmInstance)
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
        aggregateList << project(createPreFilterForCompleteAssetRequest())
        //filter out unwanted fields and flatten hierarchy
        if (matchListAfterUnwind) aggregateList << match(and(matchListAfterUnwind)) //filter out unwanted assets
        def results = mongoDatabaseService.assetRequestGroupCollection.aggregate(aggregateList).allowDiskUse(true)
        log.debug("Found ${results.size()} assets.")
        return JsonOutput.toJson(results)
    }

    public String getRequestAssetsAsJson(
            Date from,
            Date to,
            List<Long> jobGroups,
            List<Long> pages,
            List<Long> browsers,
            List<Long> locations,
            Integer bandwidthUp,
            Integer bandwidthDown,
            Integer latency,
            Integer packetloss,
            List<Long> measuredEvents,
            String osmDomainPath

    ) {
        log.debug("Querying for from = ${from} to = ${to} jobGroups = ${jobGroups} pages = ${pages} browsers = ${browsers} locations = ${locations} bandwidthUp = ${bandwidthUp} bandwidthDown = ${bandwidthDown} latency = ${latency} packetloss = ${packetloss} measuredEvents = ${measuredEvents}")
        List aggregateList = []
        List matchList = []
        matchList << gte("epochTimeStarted", from.getTime() / 1000 as Long)
        matchList << lte("epochTimeStarted", to.getTime() / 1000 as Long)
        //Note that we use Filters.in because in groovy "in" is already a groovy method. So please don't listen to IntelliJ
        //We add only maps which are not empty, because the check if something is in a empty map would always fail.
        if (jobGroups) matchList << Filters.in("jobGroup", jobGroups)
        if (pages) matchList << Filters.in("page", pages)
        if (browsers) matchList << Filters.in("browser", browsers)
        if (locations) matchList << Filters.in("location", locations)
        if (measuredEvents) matchList << Filters.in("measuredEvent", measuredEvents)
        if (bandwidthUp) matchList << eq("bandwidthUp", bandwidthUp)
        if (bandwidthDown) matchList << eq("bandwidthDown", bandwidthDown)
        if (packetloss) matchList << eq("packetLoss", packetloss)
        if (latency) matchList << eq("latency", latency)
        matchList << eq("osmInstance", OsmInstance.findByDomainPath(osmDomainPath).id)
        aggregateList << match(and(matchList)) //filter out unwanted assets
        aggregateList << unwind("\$aggregatedAssets")
        aggregateList << project(getAggregationProjection())
        def resultList = mongoDatabaseService.aggregatedAssetGroupCollection.aggregate(aggregateList).allowDiskUse(true)
        def numberOfResults = resultList.size()
        log.debug("Found ${numberOfResults} results.")
        return JsonOutput.toJson(resultList)
    }

    def getAggregationProjection() {
        if (!aggregationProjection) {
            aggregationProjection = Document.parse("""
                {
                osmInstance      : '\$osmInstance',
                wptBaseUrl       : '\$wptBaseUrl',
                wptTestId        : '\$wptTestId',
                jobGroup         : '\$jobGroup',
                mediaType        : '\$mediaType',
                browser          : '\$browser',
                epochTimeStarted : '\$epochTimeStarted',
                location         : '\$location',
                measuredEvent    : '\$measuredEvent',
                page             : '\$page',
                dateOfPersistence: '\$dateOfPersistence',
                host             : '\$aggregatedAssets.host',
                url              : '\$aggregatedAssets.url',
                subtype          : '\$aggregatedAssets.subtype',
                loadTimeMs_avg   : '\$aggregatedAssets.loadTimeMs_avg',
                loadTimeMs_min   : '\$aggregatedAssets.loadTimeMs_min',
                loadTimeMs_max   : '\$aggregatedAssets.loadTimeMs_max',
                ttfb_avg         : '\$aggregatedAssets.ttfb_avg',
                ttfb_min         : '\$aggregatedAssets.ttfb_min',
                ttfb_max         : '\$aggregatedAssets.ttfb_max',
                downloadTime_avg : '\$aggregatedAssets.downloadTime_avg',
                downloadTime_min : '\$aggregatedAssets.downloadTime_min',
                downloadTime_max : '\$aggregatedAssets.downloadTime_max',
                sslTime_avg      : '\$aggregatedAssets.sslTime_avg',
                sslTime_min      : '\$aggregatedAssets.sslTime_min',
                sslTime_max      : '\$aggregatedAssets.sslTime_max',
                connectTime_avg  : '\$aggregatedAssets.connectTime_avg',
                connectTime_min  : '\$aggregatedAssets.connectTime_min',
                connectTime_max  : '\$aggregatedAssets.connectTime_max',
                dnsTime_avg      : '\$aggregatedAssets.dnsTime_avg',
                dnsTime_min      : '\$aggregatedAssets.dnsTime_min',
                dnsTime_max      : '\$aggregatedAssets.dnsTime_max',
                bytesIn_avg      : '\$aggregatedAssets.bytesIn_avg',
                bytesIn_min      : '\$aggregatedAssets.bytesIn_min',
                bytesIn_max      : '\$aggregatedAssets.bytesIn_max',
                bytesOut_avg     : '\$aggregatedAssets.bytesOut_avg',
                bytesOut_min     : '\$aggregatedAssets.bytesOut_min',
                bytesOut_max     : '\$aggregatedAssets.bytesOut_max',
                count            : '\$aggregatedAssets.count'
                }
        """)
        }
        return aggregationProjection

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

    def List<AssetRequestGroup> getAssetRequestGroups(String wptBaseUrl, String wptTestId, String measuredEvent) {
        measuredEvent = wptDetailResultConvertService.getEventName(measuredEvent)
        return AssetRequestGroup.findAllByWptBaseUrlAndWptTestIdAndMeasuredEvent(wptBaseUrl, wptTestId, measuredEvent)
    }
}
