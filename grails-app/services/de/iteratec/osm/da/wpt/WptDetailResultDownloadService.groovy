package de.iteratec.osm.da.wpt

import de.iteratec.osm.da.asset.AssetRequestGroup
import de.iteratec.osm.da.wpt.data.WPTVersion
import de.iteratec.osm.da.wpt.data.WptDetailResult
import de.iteratec.osm.da.wpt.resolve.WptDetailDataStrategyI
import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.persistence.AssetRequestPersistenceService
import de.iteratec.osm.da.wpt.resolve.WptDownloadWorker
import de.iteratec.osm.da.wpt.resolve.WptQueueFillWorker

import javax.persistence.criteria.Fetch
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * This service handles the downloading of the detail data from WPT.
 * The downloading will be queued and therefore eventually persisted. The queue will be persisted.
 * All downloaded data will be passed to the persistense service and therefore will be persisted.
 */
class WptDetailResultDownloadService {

    /**
     * Number of workers which should download data from wpt.
     * Not that if you change this value you have to disable and enable the worker again.
     */
    Integer NUMBER_OF_WORKERS = 8
    /**
     * This boolean should be false at start.
     * It used to determine if the worker should be cancelled
     */
    boolean workerShouldRun = false
    /**
     * Maximum tries a FetchJob should get, until it will be ignored
     */
    int maxTryCount = 3
    /**
     * Maximum FetchJob which should be cached in queue.
     */
    int queueMaximumInMemory = 100

    AssetRequestPersistenceService assetRequestPersistenceService
    WptDetailDataStrategyService wptDetailDataStrategyService

    /**
     * This List will cache FetchJobs and will be used from WptQueueDownloadWorker to get new Jobs to fetch.
     * WptQueueFillWorker will fill this queue from the database in background.
     */
    final ConcurrentLinkedQueue<FetchJob> queue = new ConcurrentLinkedQueue<>()
    /**
     * Every worker will take a FetchJob from the queue. If this happens we have to note that in this list,
     * so the WptFillQueFillWorker will know that this FetchJob is still in progress and should't be added to queue again.
     */
    final HashSet<FetchJob> inProgress = []
    /**
     * This pool is used for multiple WptDownloadWorker to download WPTResults. We add one additional Thread to fill the queue.
     */
    ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_WORKERS + 1)


    public WptDetailResultDownloadService() {
        startWorker()
    }

    /**
     * Stops all worker. All currently running will finish their current job and
     */
    void disableWorker() {
        workerShouldRun = false
    }

    /**
     * Starts the worker. This will only has an effect, if the worker weren't running
     */
    void startWorker() {
        if (!workerShouldRun) {
            workerShouldRun = true
            NUMBER_OF_WORKERS.times {
                executor.execute(new WptDownloadWorker(this))
            }
            executor.execute(new WptQueueFillWorker(this))
        }
    }

    /**
     * Add a Job to the queue, so the assets will be downloaded eventually
     * @param osmInstance OSMInstance request source
     * @param jobGroupId
     * @param wptBaseUrl
     * @param wptTestId
     * @param wptVersion
     */
    public synchronized void addToQueue(long osmInstance, long jobId, long jobGroupId, String wptBaseUrl, List<String> wptTestIds, String wptVersion) {
        wptTestIds.each {String wptTestId ->
            //if we find at least one FetchJobs we can just ignore this add. Otherwise we must also check for an AssetRequestGroup
            def existingFetchJob = FetchJob.findByWptBaseURLAndWptTestIdAndOsmInstance(wptBaseUrl,wptTestId, osmInstance)
            if(!existingFetchJob){
                //If we find at least one AssetRequestGroup with the same parameters we know that this Job was already executed
                def existingAssetRequestGroup = AssetRequestGroup.findByWptBaseUrlAndWptTestIdAndOsmInstance(wptBaseUrl,wptTestId, osmInstance)
                if(!existingAssetRequestGroup){
                    //We can safely add the job to the queue
                    FetchJob fetchJob = new FetchJob(osmInstance: osmInstance, jobId: jobId, jobGroupId: jobGroupId, wptBaseURL: wptBaseUrl,
                            wptTestId: wptTestId, wptVersion: wptVersion).save(flush: true, failOnError: true)
                    synchronized (queue) {
                        if (queue.size() < queueMaximumInMemory) {
                            queue.offer(fetchJob)
                        }
                    }
                }
            }
        }
    }

    /**
     * Marks a job as failed and removes it from progress
     * @param job
     */
    public void markJobAsFailed(FetchJob job){
        if(job){
            job.tryCount++
            job.lastTryEpochTime = new Date().getTime()/1000
            job.save(flush:true)
            inProgress.remove(job)
        }
    }

    /**
     * Deletes a job and removes it from progress
     * @param job
     */
    public void deleteJob(FetchJob job) {
        job.delete(flush: true)
        inProgress.remove(job)
    }
    /**
     * Retrievs a job from the qeue and adds it to progress
     * @return
     */
    public synchronized FetchJob getNextJob() {
        FetchJob currentJob = queue.poll()
        if (currentJob) inProgress << currentJob
        return currentJob
    }

    /**
     * Fetches the WptDetailData from the given detail page.
     * If testDetailsWaterfallURL is null, this method will return null
     * @param fetchJob
     * @return WptDetailResult
     */
    public WptDetailResult downloadWptDetailResultFromWPTInstance(FetchJob fetchJob) {
        WptDetailDataStrategyI strategy = wptDetailDataStrategyService.getStrategyForVersion(WPTVersion.get(fetchJob.wptVersion))
        if(!strategy){
            log.error("No strategy for WPT-Version $fetchJob.wptVersion is supported")
            return null
        }
        return strategy.getResult(fetchJob)
    }
}
