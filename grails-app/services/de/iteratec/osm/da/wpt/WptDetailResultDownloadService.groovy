package de.iteratec.osm.da.wpt

import de.iteratec.osm.da.asset.AssetRequestGroup
import de.iteratec.osm.da.fetch.Priority
import de.iteratec.osm.da.wpt.data.WPTVersion
import de.iteratec.osm.da.wpt.data.WptDetailResult
import de.iteratec.osm.da.wpt.resolve.WptDetailDataStrategyI
import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.persistence.AssetRequestPersistenceService
import de.iteratec.osm.da.wpt.resolve.WptDownloadWorker
import de.iteratec.osm.da.wpt.resolve.WptQueueFillWorker

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.PriorityBlockingQueue

/**
 * This service handles the downloading of the detail data from WPT.
 * The downloading will be queued and therefore eventually persisted. The normalPriorityQueue will be persisted.
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
     * Maximum FetchJob which should be cached in normalPriorityQueue.
     */
    int queueMaximumInMemory = 100

    AssetRequestPersistenceService assetRequestPersistenceService
    WptDetailDataStrategyService wptDetailDataStrategyService

    /**
     * This List will cache FetchJobs and will be used from WptQueueDownloadWorker to get new Jobs to fetch.
     * WptQueueFillWorker will fill the queues from the database in background.
     */
    final HashMap<Priority, PriorityBlockingQueue<FetchJob>> queueHashMap = [(Priority.Low):createQueue(),
                                                                             (Priority.Normal):createQueue(),
                                                                             (Priority.High):createQueue()]
    final PriorityBlockingQueue<FetchJob> normalPriorityQueue = createQueue()


    /**
     * Every worker will take a FetchJob from the normalPriorityQueue. If this happens we have to note that in this list,
     * so the WptFillQueFillWorker will know that this FetchJob is still in progress and should't be added to normalPriorityQueue again.
     */
    final HashSet<FetchJob> inProgress = []
    /**
     * This pool is used for multiple WptDownloadWorker to download WPTResults. We add one additional Thread to fill the normalPriorityQueue.
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

    private PriorityBlockingQueue<FetchJob> createQueue(){
        return new PriorityBlockingQueue<>(queueMaximumInMemory, Collections.reverseOrder())
    }

    /**
     * Add a Job to the normalPriorityQueue, so the assets will be downloaded eventually
     * @param osmInstance OSMInstance request source
     * @param jobGroupId
     * @param wptBaseUrl
     * @param wptTestId
     * @param wptVersion
     */
    public void addNewFetchJobToQueue(long osmInstance, long jobId, long jobGroupId, String wptBaseUrl, List<String> wptTestIds, String wptVersion, Priority priority) {

        //Filter all ids which are already present as FetchJob
        List<String> existing = FetchJob.findAllByWptBaseURLAndOsmInstanceAndWptTestIdInList(wptBaseUrl, osmInstance, wptTestIds)*.wptTestId
        List<String> remaining = wptTestIds - existing
        if(existing) {
            log.info("The following WPTResults from $wptBaseUrl are already in normalPriorityQueue: \n $existing")
        }
        //Filter all ids which are already present as Result
        existing  = AssetRequestGroup.findAllByWptBaseUrlAndOsmInstanceAndWptTestIdInList(wptBaseUrl, osmInstance, remaining)*.wptTestId
        remaining = remaining - existing
        if(existing) {
            log.info("The following WPTResults from $wptBaseUrl are already persisted: \n $existing")
        }

        remaining.each {String wptTestId ->
            FetchJob fetchJob = new FetchJob(priority: priority, osmInstance: osmInstance, jobId: jobId, jobGroupId: jobGroupId, wptBaseURL: wptBaseUrl,
                    wptTestId: wptTestId, wptVersion: wptVersion).save(flush: true, failOnError: true)
            synchronized (queueHashMap[Priority.Normal]) {
                if (queueHashMap[Priority.Normal].size() < queueMaximumInMemory) {
                    queueHashMap[Priority.Normal].offer(fetchJob)
                }
            }
        }
    }

    public void addExistingFetchJobToQueue(List<FetchJob> jobsToAdd){
        jobsToAdd.each {queueHashMap[Priority.Normal].put(it)}
        log.info("Added ${jobsToAdd.size()} jobs to normalPriorityQueue")
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
     * Retrievs a job from the qeue and adds it to progress.
     * This method will block until there is a job in normalPriorityQueue to return.
     * @return
     */
    public synchronized FetchJob getNextJob() {
        FetchJob currentJob = queueHashMap[Priority.Normal].take()
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
