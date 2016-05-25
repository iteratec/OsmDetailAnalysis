package de.iteratec.osm.da.persistence

import de.iteratec.osm.da.asset.AssetGroup
import de.iteratec.osm.da.har.FetchJob
import de.iteratec.osm.da.har.HarConvertService
import grails.transaction.Transactional

@Transactional
class AssetPersistenceService {

    HarConvertService harConvertService
    /**
     * Parses a HAR and saves all Assets
     * @param result JobResult which belongs to this HAR
     * @param har The HAR which belongs to this JobResult
     * @return HARStatus PERSISTED or NOT_AVAILABLE
     */
    public void saveHARDataForJobResult(Map har, FetchJob fetchJob) {
        if(har){
            List<AssetGroup> assetGroups = harConvertService.convertHarToAssetGroups(har, fetchJob)
            assetGroups.each {
                it.save(failOnError: true)
            }
        }
    }

    public def getAssets(Date from, Date to, List<Long> jobGroups, List<Long> pages, List<Long> browser, List<Long> locations, List<String> connectivity){
        //TODO gmongo isn't used anymore, make sure this will work again
        Map matchList = [:]
//        GMongo mongo = new GMongo()
//        DB db = mongo.getDB("OpenSpeedMonitor")
//        matchList<<[date:[$gte:from.getTime(), $lte:to.getTime()]]
//        if(jobGroups) matchList["jobGroup"] = [$in:jobGroups]
//        if(pages) matchList << [pages:[$in:pages]]
//        if(browser) matchList << [browser:[$in:browser]]
//        if(locations) matchList << [location:[$in:locations]]
//        if(connectivity) matchList << [connectivity: [$in:connectivity]]
//
//        def options = AggregationOptions.builder().allowDiskUse(true).outputMode(AggregationOptions.OutputMode.CURSOR).build()
//        db.assetGroup.aggregate([[$match:matchList],
//                                 [$unwind:"\$assets"],
//                                 [$project:[
//                                         _id:0,
//                                         bytesIn:'\$assets.bytesIn',
//                                         bytesOut:'\$assets.bytesOut',
//                                         connectTime:'\$assets.connectTimeMs',
//                                         downloadTimeMs:'\$assets.downloadTimeMs',
//                                         loadTimeMs:'\$assets.loadTimeMs',
//                                         timeToFirstByteMs:'\$assets.timeToFirstByteMs',
//                                         indexWithinHar:'\$assets.indexWithinHar',
//                                         sslNegotiationTimeMs:'\$assets.sslNegotiationTimeMs',
//                                         mediaType:'\$assets.mediaType',
//                                         subtype:'\$assets.subtype',
//                                         url:1,
//                                         page:1,
//                                         //we rename this variable, because otherwise it may look like this specific asset was cached
//                                         //but the cached attribute belongs to the whole page
//                                         pageFromCache:'\$cached',
//                                         jobGroup:1,
//                                         connectivity:1,
//                                         location:1,
//                                         browser:1,
//                                         date:1
//                                 ]]
//        ],options).collect()
        return []
    }
}
