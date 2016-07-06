package de.iteratec.osm.da.har

import de.iteratec.oms.da.wpt.data.WPTDetailResult
import de.iteratec.oms.da.wpt.data.WPTVersion
import de.iteratec.oms.da.wpt.resolve.WPTDetailDataStrategyBuilder
import de.iteratec.oms.da.wpt.resolve.WPTDetailDataStrategyI
import de.iteratec.osm.da.persistence.AssetPersistenceService
import grails.transaction.Transactional

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
    public void addToQeue(long osmInstance, long jobGroupId, String wptBaseUrl, String wptTestId, int up, int down, int latency, int loss, String wptVersion){
        FetchJob fetchJob =  new FetchJob(osmInstance: osmInstance,jobGroupId: jobGroupId, wptBaseURL: wptBaseUrl,
                wptTestId: wptTestId, bandWithDown: down, bandWidthUp: up, latency: latency,packetLoss: loss, wptVersion: wptVersion).save(flush:true, failOnError:true)
        if(queue.size()< queueMaximumInMemory){
            queue << fetchJob
        }
    }

    public void getNextFromQueue(){
        //TODO a thread pool should handle this, see IT-1180
        fillQueueFromDatabase()
        if(!queue.isEmpty()){
            FetchJob currentJob = queue.poll()
            WPTDetailResult result = downloadHarFromWPTInstance(currentJob)
            assetPersistenceService.saveHARDataForJobResult(result,currentJob)
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
     * @return WPTDetailResult
     */
    private WPTDetailResult downloadHarFromWPTInstance(FetchJob fetchJob) {
        WPTDetailDataStrategyI strategy = WPTDetailDataStrategyBuilder.getStrategyForVersion(new WPTVersion(fetchJob.wptVersion))
        return strategy.getResult(fetchJob)
    }

}
