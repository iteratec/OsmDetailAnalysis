package de.iteratec.osm.da.har

import de.iteratec.osm.da.persistence.AssetPersistenceService
import grails.transaction.Transactional

@Transactional
class HarFetchService {


    AssetPersistenceService assetPersistenceService

    /**
     * Fetches a HAR from the given URL and creates matching assets with {@link AssetGroup}
     */
    public void fetch(long osmInstance, long jobGroupId, long jobResultId, long locationId, long browserID,  long pageId, Date jobResultDate, String url){
        FetchJob fetchJob = new FetchJob(osmInstance: osmInstance, jobGroupId: jobGroupId, jobResultId: jobResultId,
                locationId:locationId, browserID: browserID, pageId:pageId, jobResultDate:jobResultDate, url:url)
        assetPersistenceService.saveHARDataForJobResult(fetchHarFromWPTInstance(fetchJob.url), fetchJob)
    }


    /**
     * Fetches the HAR from the given detail page.
     * If testDetailsWaterfallURL is null, this method will return null
     * @param testDetailsWaterfallURL
     * @return HAR from local Database given server, if it's not already fetched
     */
    private Map fetchHarFromWPTInstance(String url) {
        //TODO implement HTTP Stuff
        return [:]
    }

}
