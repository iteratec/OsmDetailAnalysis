package de.iteratec.osm.da.wpt

import de.iteratec.osm.da.asset.AssetRequestGroup
import de.iteratec.osm.da.fetch.FailedFetchJob
import de.iteratec.osm.da.fetch.FetchBatch
import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.fetch.FetchFailReason
import de.iteratec.osm.da.fetch.Priority
import de.iteratec.osm.da.persistence.AssetRequestPersistenceService
import de.iteratec.osm.da.wpt.data.WPTVersion
import de.iteratec.osm.da.wpt.data.WptDetailResult
import de.iteratec.osm.da.wpt.resolve.WptDetailDataStrategyI
import de.iteratec.osm.da.wpt.resolve.WptDownloadWorker
import de.iteratec.osm.da.wpt.resolve.WptQueueFillWorker
import de.iteratec.osm.da.wpt.resolve.WptWorker
import org.junit.internal.runners.statements.Fail

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
/**
 * This service handles the downloading of the detail data from WPT.
 * The downloading will be queued and therefore eventually persisted. The queue will be persisted.
 * All downloaded data will be passed to the persistence service and therefore will be persisted.
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
     * Maximum FetchJob which should be cached in each queue.
     */
    int queueMaximumInMemory = 50

    AssetRequestPersistenceService assetRequestPersistenceService
    WptDetailDataStrategyService wptDetailDataStrategyService
    FailedFetchJobService failedFetchJobService

    /**
     * This List will cache FetchJobs and will be used from WptQueueDownloadWorker to get new Jobs to fetch.
     * WptQueueFillWorker will fill the queues from the database in background.
     */
    final HashMap<Priority, PriorityBlockingQueue<FetchJob>> queueHashMap = [(Priority.Low):createQueue(),
                                                                             (Priority.Normal):createQueue(),
                                                                             (Priority.High):createQueue()]
    /**
     * Every worker will take a FetchJob from the normalPriorityQueue. If this happens we have to note that in this list,
     * so the WptFillQueFillWorker will know that this FetchJob is still in progress and should't be added to the queues again.
     */
    final HashSet<FetchJob> inProgress = []
    /**
     * This pool is used for multiple WptDownloadWorker to download WPTResults. We add one additional Thread to fill the queues.
     */
    ThreadPoolExecutor executor = Executors.newFixedThreadPool(NUMBER_OF_WORKERS + 1) as ThreadPoolExecutor
    List<WptWorker> workerList = []

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
                WptDownloadWorker worker = new WptDownloadWorker(this)
                executor.execute(worker)
                workerList << worker
            }
            WptQueueFillWorker fillWorker = new WptQueueFillWorker(this)
            executor.execute(fillWorker)
            workerList << fillWorker
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
     * @param priority
     * @param fetchBatch
     */
    public int addNewFetchJobToQueue(long osmInstance, long jobId, long jobGroupId, String wptBaseUrl, List<String> wptTestIds, String wptVersion, Priority priority, FetchBatch fetchBatch = null) {
        int numberOfNewFetchJobs = 0

        log.debug("Persist ${wptTestIds.size()} wptTestIds as FetchJobs.")

        wptTestIds.each {String wptTestId ->
            FetchJob fetchJob = new FetchJob(priority: priority, osmInstance: osmInstance, jobId: jobId, jobGroupId: jobGroupId, wptBaseURL: wptBaseUrl,
                    wptTestId: wptTestId, wptVersion: wptVersion, fetchBatch:fetchBatch).save(flush: true, failOnError: true)
//            addToQueue(fetchJob, priority)
            numberOfNewFetchJobs++
            log.debug("Created a FetchJob for WptId=$wptTestId")
        }

        return numberOfNewFetchJobs
    }

    private void addToQueue(FetchJob fetchJob, Priority priority){
        synchronized (queueHashMap[priority]) {
            if (queueHashMap[priority].size() < queueMaximumInMemory) {
                log.debug("Add the following FetchJob to queueHashMap with priority ${priority}:\n${fetchJob}")
                queueHashMap[priority].offer(fetchJob)
            }
        }
    }

    public void addExistingFetchJobToQueue(List<FetchJob> jobsToAdd, Priority priority){
        jobsToAdd.each {queueHashMap[priority].put(it)}
        log.info("Added ${jobsToAdd.size()} jobs to $priority queue")
    }

    /**
     * Marks a job as failed and removes it from progress
     * @param job
     */
    public void markJobAsFailed(FetchJob job){
        if(job){
            job.withNewSession {
                job.tryCount++
                log.debug("Try ${job.tryCount} to Job")
                if (job.fetchBatch && job.tryCount >= maxTryCount) {
                    job.fetchBatch.addFailure(job)
                    job.fetchBatch = null
                }
                if(job.tryCount >= maxTryCount){
                    failedFetchJobService.markJobAsFailedIfNeeded(null,job, FetchFailReason.WPT_NOT_AVAILABLE)
                    job.delete(flush:true)
                }else{
                    job.lastTryEpochTime = new Date().getTime() / 1000
                    job.save(flush: true)
                }
                inProgress.remove(job)
            }
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
        FetchJob currentJob = null
        while(!currentJob){
            currentJob = queueHashMap[Priority.High].poll(2, TimeUnit.SECONDS)
            if(!currentJob) currentJob = queueHashMap[Priority.Normal].poll(500, TimeUnit.MILLISECONDS)
            if(!currentJob) currentJob = queueHashMap[Priority.Low].poll(500, TimeUnit.MILLISECONDS)
        }
        inProgress << currentJob
        return currentJob
    }

    public List<Priority> getAvailablePriorities(){
        return queueHashMap.keySet().collect()
    }

    public int getJobCountInQueueByPriority(Priority priority){
        return queueHashMap[priority].size()
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
