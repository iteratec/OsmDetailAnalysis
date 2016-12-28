package de.iteratec.osm.da.migration.changeSets

import com.mongodb.MongoClient
import com.mongodb.client.MongoDatabase
import de.iteratec.osm.da.asset.AggregatedAssetGroup
import de.iteratec.osm.da.asset.AssetRequest
import de.iteratec.osm.da.asset.AssetRequestGroup
import de.iteratec.osm.da.migration.ChangeSet

class DA_V_1_0_0_MarkoSchnecke_2016_12_23 extends ChangeSet{
    MongoClient mongo
    def grailsApplication

    @Override
    Boolean execute() {
        def databaseName = grailsApplication.config.grails?.mongodb?.databaseName
        databaseName = databaseName?databaseName:"OsmDetailAnalysis"
        MongoDatabase db = mongo.getDatabase(databaseName)
        db.getCollection("aggregatedAssetGroup").remove([:])
        def assetGroupCount = AssetRequestGroup.count
        def offset = 0
        def limit = 50
        AggregatedAssetGroup aggregatedAssetGroup
        while (offset< assetGroupCount) {
            log.debug("percent ${100*offset/assetGroupCount}% offset ${offset} assetGroupCount ${assetGroupCount}")
            log.debug(Runtime.getRuntime().freeMemory()*100/Runtime.getRuntime().maxMemory())
            createAggregationsForBatch(limit, offset, aggregatedAssetGroup)
            offset+=limit

        }
        return true

    }

    private void createAggregationsForBatch(int limit, int offset, AggregatedAssetGroup aggregatedAssetGroup) {
        AssetRequestGroup.withNewSession {
            def assetGroups = AssetRequestGroup.list(max: limit, offset: offset, sort: "_id")

            assetGroups.each { AssetRequestGroup assetRequestGroup ->
                if (AggregatedAssetGroup.countByJobGroupAndMediaTypeAndBrowserAndPageAndMeasuredEventAndEpochTimeStartedAndOsmInstance(
                        assetRequestGroup.jobGroup,
                        assetRequestGroup.mediaType,
                        assetRequestGroup.browser,
                        assetRequestGroup.page,
                        assetRequestGroup.measuredEvent,
                        assetRequestGroup.epochTimeStarted,
                        assetRequestGroup.osmInstance) == 0) {

                    def groups = assetRequestGroup.assets.groupBy(
                            { it.mediaType },
                            { it.subtype },
                            { it.url })
                    groups.each { mediaType, subTypes ->
                        subTypes.each { subType, urls ->
                            urls.each { url, List<AssetRequest> assets ->
                                aggregatedAssetGroup = new AggregatedAssetGroup()
                                aggregatedAssetGroup.jobGroup = assetRequestGroup.jobGroup
                                aggregatedAssetGroup.mediaType = mediaType
                                aggregatedAssetGroup.browser = assetRequestGroup.browser
                                aggregatedAssetGroup.subtype = subType
                                aggregatedAssetGroup.epochTimeStarted = assetRequestGroup.epochTimeStarted
                                aggregatedAssetGroup.measuredEvent = assetRequestGroup.measuredEvent
                                aggregatedAssetGroup.host = assets[0].host
                                aggregatedAssetGroup.page = assetRequestGroup.page
                                aggregatedAssetGroup.loadTimeMs_avg = assets.loadTimeMs.sum() / assets.loadTimeMs.size()
                                aggregatedAssetGroup.loadTimeMs_min = assets.loadTimeMs.min()
                                aggregatedAssetGroup.loadTimeMs_max = assets.loadTimeMs.max()
                                aggregatedAssetGroup.ttfb_avg = assets.timeToFirstByteMs.sum() / assets.timeToFirstByteMs.size()
                                aggregatedAssetGroup.ttfb_min = assets.timeToFirstByteMs.min()
                                aggregatedAssetGroup.ttfb_max = assets.timeToFirstByteMs.max()
                                aggregatedAssetGroup.downloadTime_avg = assets.downloadTimeMs.sum() / assets.downloadTimeMs.size()
                                aggregatedAssetGroup.downloadTime_min = assets.downloadTimeMs.min()
                                aggregatedAssetGroup.downloadTime_max = assets.downloadTimeMs.max()
                                aggregatedAssetGroup.sslTime_avg = assets.sslNegotiationTimeMs.sum() / assets.sslNegotiationTimeMs.size()
                                aggregatedAssetGroup.sslTime_min = assets.sslNegotiationTimeMs.min()
                                aggregatedAssetGroup.sslTime_max = assets.sslNegotiationTimeMs.max()
                                aggregatedAssetGroup.connectTime_avg = assets.connectTimeMs.sum() / assets.connectTimeMs.size()
                                aggregatedAssetGroup.connectTime_min = assets.connectTimeMs.min()
                                aggregatedAssetGroup.connectTime_max = assets.connectTimeMs.max()
                                aggregatedAssetGroup.dnsTime_avg = assets.dnsMs.sum() / assets.dnsMs.size()
                                aggregatedAssetGroup.dnsTime_min = assets.dnsMs.min()
                                aggregatedAssetGroup.dnsTime_max = assets.dnsMs.max()
                                aggregatedAssetGroup.bytesIn_avg = assets.bytesIn.sum() / assets.bytesIn.size()
                                aggregatedAssetGroup.bytesIn_min = assets.bytesIn.min()
                                aggregatedAssetGroup.bytesIn_max = assets.bytesIn.max()
                                aggregatedAssetGroup.bytesOut_avg = assets.bytesOut.sum() / assets.bytesOut.size()
                                aggregatedAssetGroup.bytesOut_min = assets.bytesOut.min()
                                aggregatedAssetGroup.bytesOut_max = assets.bytesOut.max()
                                aggregatedAssetGroup.count = assets.size()
                                aggregatedAssetGroup.osmInstance = assetRequestGroup.osmInstance
                                aggregatedAssetGroup.save(failOnError: true, flush: true)
                            }
                        }
                    }
                }
            }
        }
    }
}
