package de.iteratec.osm.da.external.wpt

import de.iteratec.oms.da.external.wpt.data.Request
import de.iteratec.oms.da.external.wpt.data.WPTDetailResult
import de.iteratec.osm.da.asset.Asset
import de.iteratec.osm.da.asset.AssetGroup
import de.iteratec.osm.da.external.FetchJob
import grails.transaction.Transactional

@Transactional
class WPTDetailResultConvertService {

    static String undefinedMediaType = "undefined"
    static String undefinedSubtype = "undefined"

    public List<AssetGroup> convertWPTDetailResultToAssetGroups(WPTDetailResult result, FetchJob fetchJob){
        List<AssetGroup> assetGroups = []
        result.steps.each {step ->
            Map<String, List<Request>> by = step.requests.groupBy { getMediaType(it.contentType) }
            by.each {key, value ->
                AssetGroup assetGroup = new AssetGroup()
                List<Asset> assets = []
                value.each {req ->
                    String[] mimeType = convertMimeTypesInAssetMap(req.contentType)
                    assets << new Asset(bytesIn: req.bytesIn, bytesOut: req.bytesOut, connectTimeMs: req.connectTimeMs,
                            downloadTimeMs: req.downloadMs,loadTimeMs: req.loadMs, timeToFirstByteMs: req.ttfbMs,
                            sslNegotiationTimeMs: req.sslNegotiationTimeMs, indexWithinHar: req.indexWithinStep,
                            mediaType: mimeType[0], subtype: mimeType[1], host: req.host, url: req.url,
                            urlWithoutParams: req.host+createURLWithoutParams(req.url))
                }
                assetGroup.assets = assets
                assetGroups << assetGroup
            }
        }
        return assetGroups
    }

    private static String getMediaType(String mimeType){
        if(!mimeType || mimeType.indexOf("/")<0) return undefinedMediaType
        return mimeType?.split("/")
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
