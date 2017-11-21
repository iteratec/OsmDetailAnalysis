package de.iteratec.osm.da.wpt

import de.iteratec.osm.da.asset.AssetRequest
import de.iteratec.osm.da.asset.AssetRequestGroup
import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.mapping.MappingService
import de.iteratec.osm.da.mapping.OsmDomain
import de.iteratec.osm.da.wpt.data.Request
import de.iteratec.osm.da.wpt.data.WptDetailResult

class WptDetailResultConvertService {

    MappingService mappingService

    static String UNDEFINED_MEDIA_TYPE = "undefined"
    static String UNDEFINED_SUBTYPE = "undefined"
    static String UNDEFINED_PAGE = "undefined"

    /** EventName can contain information of tested pages and teststep-number. Both informations are delimited through this. **/
    public static final String STEPNAME_DELIMITTER = ':::'

    /**
     * Will convert a result to a AssetGroup. Note that there can be null values in this list, if a mapping wasn't available
     * @param result
     * @param fetchJob
     * @param date
     * @return
     */
    public List<AssetRequestGroup> convertWPTDetailResultToAssetGroups(WptDetailResult result, FetchJob fetchJob){
        List<AssetRequestGroup> assetGroups = []
        Date dateOfPersistence = new Date() // Make sure that every Group has the same date, so later they will all deleted together
        result.steps.each {step ->
            Map<String, List<Request>> mediaTypeMap = step.requests.groupBy {
                getMediaType(it.contentType)
            }
            mediaTypeMap.each {key, value ->
                AssetRequestGroup assetGroup = createAssetGroup(result, fetchJob, key, step.isFirstView, getEventName(step.eventName), getPageName(step.eventName), step.epochTimeStarted, dateOfPersistence)
                //If there was no group created, we just skip this group
                if(!assetGroup) return
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

    private AssetRequestGroup createAssetGroup(WptDetailResult result, FetchJob fetchJob, String mediaType, boolean isFirstView, String eventName, String pageName, long epochTimeStarted, Date dateOfPersistence){
        boolean allUpdatesDone = updateMappings(fetchJob.osmInstance,result,eventName, pageName, fetchJob.jobGroupId, fetchJob.jobId)
        if(!allUpdatesDone){
            fetchJob.delete(failOnError: true, flush: true)
            return null
        }
        long measuredEvent = mappingService.getIdForMeasuredEventName(fetchJob.osmInstance, eventName)
        long page  = mappingService.getIdForPageName(fetchJob.osmInstance, pageName)
        long location = mappingService.getIdForLocationName(fetchJob.osmInstance, result.location)
        long browser = mappingService.getIdForBrowserName(fetchJob.osmInstance,result.browser)
        return new AssetRequestGroup(osmInstance: fetchJob.osmInstance,eventName: eventName, jobId: result.jobId, jobGroup: result.jobGroupID,
                bandwithUp: result.bandwidthUp, bandwidhtDown: result.bandwidthDown, latency: result.latency,
                packetLoss: result.packagelossrate, page: page, measuredEvent:measuredEvent, location:location,
                browser: browser, epochTimeStarted: epochTimeStarted, mediaType: mediaType,
                wptBaseUrl: result.wptBaseUrl, wptTestId: result.wptTestID, isFirstViewInStep: isFirstView, dateOfPersistence: dateOfPersistence)
    }

    /**
     * Updates all Mapping, which are not present. If even after an update attempt mappings are still missing, this method will return false.
     * @param osmInstance
     * @param wptDetailResult
     * @param eventName
     * @param jobGroup
     * @param jobId
     * @return true if all mappings where present after update
     */
    private boolean updateMappings(long osmInstance, WptDetailResult wptDetailResult, String eventName, String pageName, long jobGroup, long jobId){
        Map updateMap = [(OsmDomain.Page):[pageName],
                         (OsmDomain.Browser):[wptDetailResult.browser],
                         (OsmDomain.MeasuredEvent):[eventName],
                         (OsmDomain.Location):[wptDetailResult.location]]
        boolean missing = mappingService.updateIfNameMappingsDoesntExist(osmInstance,updateMap)
        missing &= mappingService.updateIfIdMappingsDoesntExist(osmInstance,[(OsmDomain.JobGroup):[jobGroup], (OsmDomain.Job):[jobId]])
        return missing
    }

    private String getEventName(String name){
        List<String> tokenized = name.split(STEPNAME_DELIMITTER)
        return tokenized.size() == 2 ? tokenized[1] : name
    }

    private String getPageName(String eventName){
        List<String> tokenized = eventName.split(STEPNAME_DELIMITTER)
        return tokenized.size() == 2 ? tokenized[0] : UNDEFINED_PAGE
    }

    private static AssetRequest createAsset(Request req){
        String[] mimeType = convertMimeTypesInAssetMap(req.contentType)
        return new AssetRequest(bytesIn: req.bytesIn, bytesOut: req.bytesOut, connectTimeMs: req.connectTimeMs,
                downloadTimeMs: req.downloadMs,loadTimeMs: req.loadMs, timeToFirstByteMs: req.ttfbMs,
                sslNegotiationTimeMs: req.sslNegotiationTimeMs, indexWithinHar: req.indexWithinStep,
                mediaType: mimeType[0], subtype: mimeType[1], host: req.host, url: req.url,
                urlWithoutParams: req!=null && req.host!= null ?"${req.host}${createURLWithoutParams(req.url)}":req.host,
                startPhase: req.startPhase,
                endPhase: req.endPhase, dnsMs: req.dnsTimeMs)
    }

    static String getMediaType(String mimeType){
        if(!mimeType || mimeType.indexOf("/")<0) return UNDEFINED_MEDIA_TYPE
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
        String[] result = [UNDEFINED_MEDIA_TYPE, UNDEFINED_SUBTYPE]
        if(!mimeType) return result
        String[] split = mimeType?.split("/")
        if(split.size()>=1)result[0] = split[0]
        if(split.size()>=2)result[1] = split[1]
        return result
    }
}
