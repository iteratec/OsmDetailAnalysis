package de.iteratec.osm.da.wpt

import de.iteratec.osm.da.mapping.MappingService
import de.iteratec.osm.da.wpt.data.WPTVersion
import de.iteratec.osm.da.wpt.data.WptDetailResult
import de.iteratec.osm.da.wpt.resolve.WptDetailDataStrategyBuilder
import de.iteratec.osm.da.wpt.resolve.WptDetailDataStrategyI
import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.persistence.AssetRequestPersistenceService
import grails.transaction.Transactional

import javax.persistence.criteria.Fetch
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * This service handles the downloading of the detail data from WPT.
 * The downloading will be queued and therefore eventually persisted. The queue will be persisted.
 * All downloaded data will be passed to the persistense service and therefore will be persisted.
 */
@Transactional
class WptDetailResultDownloadService {


    AssetRequestPersistenceService assetPersistenceService
    MappingService mappingService
    int queueMaximumInMemory = 100
    final BlockingQueue<FetchJob> queue = new ArrayBlockingQueue(queueMaximumInMemory)
    ExecutorService executor = Executors.newFixedThreadPool(8)


    public WptDetailResultDownloadService(){
        8.times {
            executor.execute{
                while (true){
                    while (!queue.isEmpty()){
                        getNextFromQueue()
                    }
                    sleep(2000)//To reduce overhead we just wait 2 second and recheck, if the queue is still empty
                }
            }
        }
    }

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
        synchronized (queue){
            if(queue.size()< queueMaximumInMemory){
                queue << fetchJob
            }
        }
    }

    /**
     * Taktes the next job from the qeue and persist it to the database. The FetchJob will be deleted afterwards.
     */
    public void getNextFromQueue(){
        fillQueueFromDatabase()
        FetchJob currentJob
        currentJob = queue.poll()
        if(currentJob){
            while(currentJob.next()){
                WptDetailResult result = downloadWptDetailResultFromWPTInstance(currentJob)
                assetPersistenceService.saveDetailDataForJobResult(result,currentJob)
            }
        }
        currentJob.delete(flush:true)
    }

    /**
     * Fills the queue to queueMaximumInMemory, if the qeue is half empty
     */
    private void fillQueueFromDatabase(){
        synchronized (queue){
            if(queue.size() < queueMaximumInMemory/2){
                println "fill queue"
                FetchJob.withNewSession{
                    println queue
                    def c = FetchJob.createCriteria()
                    def jobs = c.list (max:queueMaximumInMemory-queue.size()) {
                        not {
                            'in' ("id", queue*.id)
                        }
                    }
                    println "Ids ${jobs*.id} jobs to qeue"
                    println "Added ${jobs.size()} jobs to qeue"
                    queue.addAll(jobs)
                }
            } else{
                println "skipping fill queue"
            }
        }
    }


    /**
     * Fetches the WptDetailData from the given detail page.
     * If testDetailsWaterfallURL is null, this method will return null
     * @param fetchJob
     * @return WptDetailResult
     */
    private WptDetailResult downloadWptDetailResultFromWPTInstance(FetchJob fetchJob) {
        WptDetailDataStrategyI strategy = WptDetailDataStrategyBuilder.getStrategyForVersion(new WPTVersion(fetchJob.wptVersion))
        return strategy.getResult(fetchJob)
    }
}
