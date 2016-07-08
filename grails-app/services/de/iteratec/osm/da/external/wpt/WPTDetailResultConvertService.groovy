package de.iteratec.osm.da.external.wpt

import de.iteratec.oms.da.external.wpt.data.Request
import de.iteratec.oms.da.external.wpt.data.WPTDetailResult
import de.iteratec.osm.da.asset.AssetRequestGroup
import de.iteratec.osm.da.asset.AssetRequest
import de.iteratec.osm.da.external.FetchJob
import de.iteratec.osm.da.external.mapping.MappingService
import grails.transaction.Transactional

@Transactional
class WPTDetailResultConvertService {

    MappingService mappingService

    static String undefinedMediaType = "undefined"
    static String undefinedSubtype = "undefined"

    public List<AssetRequestGroup> convertWPTDetailResultToAssetGroups(WPTDetailResult result, FetchJob fetchJob){
        List<AssetRequestGroup> assetGroups = []
        result.steps.each {step ->
            Map<String, List<Request>> mediaTypeMap = step.requests.groupBy {
                getMediaType(it.contentType)
            }
            mediaTypeMap.each {key, value ->
                AssetRequestGroup assetGroup = createAssetGroup(result, fetchJob, key, step.isFirstView, step.eventName)
                List<AssetRequest> assets = []
                value.each {req ->
                    assets << createAsset(req)
                }
                assetGroup.assets = assets
                assetGroups << assetGroup
            }
        }
        return assetGroups
    }

    private AssetRequestGroup createAssetGroup(WPTDetailResult result, FetchJob fetchJob, String mediaType, boolean isFirstView, String eventName){
        long measuredEvent = mappingService.getIdForMeasuredEventName(fetchJob.osmInstance, eventName)
        long page = -1 //TODO get page
        long location = mappingService.getIdForLocationName(fetchJob.osmInstance, result.location)
        long browser = mappingService.getIdForBrowserName(fetchJob.osmInstance,result.browser)

        return new AssetRequestGroup(osmInstance: fetchJob.osmInstance,eventName: eventName, jobGroup: result.jobGroupID,
                bandwithUp: result.bandwidthUp, bandwidhtDown: result.bandwidthDown, latency: result.latency,
                packetLoss: result.packagelossrate, page: page, measuredEvent:measuredEvent, location:location,
                browser: browser, epochTimeCompleted: result.epochTimeCompleted, mediaType: mediaType,
                wptBaseUrl: result.wptBaseUrl, wptTestId: result.wptTestID, isFirstViewInStep: isFirstView)
    }

    private static AssetRequest createAsset(Request req){
        String[] mimeType = convertMimeTypesInAssetMap(req.contentType)
        return new AssetRequest(bytesIn: req.bytesIn, bytesOut: req.bytesOut, connectTimeMs: req.connectTimeMs,
                downloadTimeMs: req.downloadMs,loadTimeMs: req.loadMs, timeToFirstByteMs: req.ttfbMs,
                sslNegotiationTimeMs: req.sslNegotiationTimeMs, indexWithinHar: req.indexWithinStep,
                mediaType: mimeType[0], subtype: mimeType[1], host: req.host, url: req.url,
                urlWithoutParams: req.host+createURLWithoutParams(req.url))
    }

    private static String getMediaType(String mimeType){
        if(!mimeType || mimeType.indexOf("/")<0) return undefinedMediaType
        return mimeType?.split("/")[0]
    }

    /**
     * Removes all parameter from a given URL
     * @param url String
     */
    private static String createURLWithoutParams(String url){
        String urlWithoutParams
        def paramIndex = url?.indexOf("?")
        if(paramIndex != null && paramIndex>0){
            urlWithoutParams = url.substring(0,paramIndex)
        } else{
            urlWithoutParams = url
        }
        return urlWithoutParams
    }

    /**
     * Takes the MimeType and splits it into media and subtype.
     * This will will always return a String[] with the size of 2.
     * If one type could'nt not be parsed, it will be "undefined".
     *
     * @param mimeType String
     * @return String[0] = mediaType, String[1]=subtype
     */
    private static String[] convertMimeTypesInAssetMap(String mimeType){
        String[] result = [undefinedMediaType,undefinedSubtype]
        if(!mimeType) return result
        String[] split = mimeType?.split("/")
        if(split.size()>=1)result[0] = split[0]
        if(split.size()>=2)result[1] = split[1]
        return result
    }
}
