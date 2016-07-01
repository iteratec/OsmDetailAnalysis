package de.iteratec.osm.da.har

import de.iteratec.osm.da.asset.Connectivity
import de.iteratec.osm.da.persistence.AssetPersistenceService
import grails.transaction.Transactional

import javax.persistence.criteria.Fetch

@Transactional
class WptDownloadService {


    AssetPersistenceService assetPersistenceService
    int queueMaximumInMemory = 100
    Queue<FetchJob> queue = [] as Queue

    /**
     * Add a Job to the queue, so the assets will be downloaded eventually
     * @param  osmInstance OSMInstance request source
     * @param jobGroupId
     * @param wptBaseUrl
     * @param wptTestId
     * @param up BandwitdhUp
     * @param down BandwitdhDown
     * @param latency
     * @param loss PacketLoss
     */
    public void addToQeue(long osmInstance, long jobGroupId, String wptBaseUrl, String wptTestId, int up, int down, int latency, int loss){
        FetchJob fetchJob =  new FetchJob(osmInstance: osmInstance,jobGroupId: jobGroupId, wptBaseURL: wptBaseUrl,
                wptTestId: wptTestId, bandWithDown: down, bandWidthUp: up, latency: latency,packetLoss: loss).save(flush:true, failOnError:true)
        if(queue.size()< queueMaximumInMemory){
            queue << fetchJob
        }
    }

    public void getNextFromQueue(){
        //TODO a thread pool should handle this, see IT-1180
        fillQueueFromDatabase()
        if(!queue.isEmpty()){
            FetchJob currentJob = queue.poll()
            def harData = downloadHarFromWPTInstance(currentJob)
            assetPersistenceService.saveHARDataForJobResult(harData,currentJob)
            currentJob.delete(flush:true)
        }
    }

    private void fillQueueFromDatabase(){
        if(queue.isEmpty()){
            queue.addAll(FetchJob.list(max: queueMaximumInMemory, sort: "created"))
        }
    }


    /**
     * Fetches the HAR from the given detail page.
     * If testDetailsWaterfallURL is null, this method will return null
     * @param fetchJob
     * @return HAR from local Database given server, if it's not already fetched
     */
    private Map downloadHarFromWPTInstance(FetchJob fetchJob) {
        //TODO implement HTTP Stuff
        return [:]
    }

}
