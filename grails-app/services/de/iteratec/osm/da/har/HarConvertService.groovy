package de.iteratec.osm.da.har

import de.iteratec.osm.da.asset.Asset
import de.iteratec.osm.da.asset.AssetGroup
import grails.transaction.Transactional

@Transactional
class HarConvertService {

    public List<AssetGroup> convertHarToAssetGroups(Map har, FetchJob fetchJob){
        List<AssetGroup> assetGroups = []
        if (har && !har.isEmpty()) {
            har.log.pages.each { Map page ->
                String harPageId = page.id
                List<Asset> assets = extractAssetListForPage(har, harPageId)
                assetGroups.add(createAssetGroupForPage(page,assets, fetchJob))
            }
        }
        return assetGroups
    }

    /**
     * Takes a page map and the matching asset list from har and merges them
     * @param page Page Map from HAR
     * @param assets Asset List from HAR
     */
    private static AssetGroup createAssetGroupForPage(Map page, List<Asset> assets, FetchJob fetchJob){
        AssetGroup assetGroup = new AssetGroup(
                osmInstance:fetchJob.osmInstance,
                page:fetchJob.pageId,
                jobGroup: fetchJob.jobGroupId,
//                connectivity: getConnectivity(jobResult), //TODO how should connectivities be persisted?
                jobResult: fetchJob.jobResultId,
                location: fetchJob.locationId,
                browser: fetchJob.browserID,
                assets:assets,
                date: fetchJob.jobResultDate.getTime(),
                cached: page._cached,
                eventName: page._eventName,
                title: page._title)
        return assetGroup
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
        String[] result = ["undefined","undefined"]
        if(!mimeType) return result

        String[] split = mimeType?.split("/")
        if(split.size()>=1)result[0] = split[0]
        if(split.size()>=2)result[1] = split[1]
        return result
    }

    /**
     * Searches fo all assets which belongs to a given page id
     * @param har
     * @param pageID PageID do search
     * @return List of all matching Assets
     */
    private static List<Asset> extractAssetListForPage(def har, String pageID) {
        List assetMaps = []
        har.log.entries.each {
            if (it.pageref == pageID) assetMaps << it
        }
        List<Asset> assets = []
        assetMaps.each {
            String[] mimeType = convertMimeTypesInAssetMap(it._contentType as String)
            assets << new Asset(bytesIn: it._bytesIn,
                    bytesOut: it._bytesOut,
                    contentType: it._contentType,
                    connectTimeMs: it._connect_ms,
                    downloadTimeMs: it._download_ms,
                    fullURL: it._full_url,
                    host: it._host,
                    indexWithinHar: it._index,
                    loadTimeMs: it._load_ms,
                    timeToFirstByteMs: it._ttfb_ms,
                    mediaType: mimeType[0],
                    subtype: mimeType[1],
                    sslNegotiationTimeMs: it._ssl_ms,
                    urlWithoutParams: createURLWithoutParams(it._full_url)
            )
        }
        return assets
    }
}
