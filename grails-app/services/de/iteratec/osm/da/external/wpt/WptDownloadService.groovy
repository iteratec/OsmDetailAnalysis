package de.iteratec.osm.da.external.wpt

import de.iteratec.oms.da.external.wpt.data.WPTDetailResult
import de.iteratec.oms.da.external.wpt.data.WPTVersion
import de.iteratec.oms.da.external.wpt.resolve.WPTDetailDataStrategyBuilder
import de.iteratec.oms.da.external.wpt.resolve.WPTDetailDataStrategyI
import de.iteratec.osm.da.external.FetchJob
import de.iteratec.osm.da.persistence.AssetRequestPersistenceService
import grails.transaction.Transactional

@Transactional
class WptDownloadService {


    AssetRequestPersistenceService assetPersistenceService
    int queueMaximumInMemory = 100
    Queue<FetchJob> queue = [] as Queue

    /**
     * Add a Job to the queue, so the assets will be downloaded eventually
     * @param  osmInstance OSMInstance request source
     * @param jobGroupId
     * @param wptBaseUrl
     * @param wptTestId
     * @param wptVersion
     */
    public void addToQeue(long osmInstance, long jobGroupId, String wptBaseUrl, List<String> wptTestId, String wptVersion){
        FetchJob fetchJob =  new FetchJob(osmInstance: osmInstance,jobGroupId: jobGroupId, wptBaseURL: wptBaseUrl,
                wptTestId: wptTestId, wptVersion: wptVersion).save(flush:true, failOnError:true)
        if(queue.size()< queueMaximumInMemory){
            queue << fetchJob
        }
    }

    public void getNextFromQueue(){
        //TODO a thread pool should handle this, see IT-1180
        fillQueueFromDatabase()
        if(!queue.isEmpty()){
            FetchJob currentJob = queue.poll()
            while(currentJob.next()){
                WPTDetailResult result = downloadHarFromWPTInstance(currentJob)
                assetPersistenceService.saveHARDataForJobResult(result,currentJob)
            }
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
