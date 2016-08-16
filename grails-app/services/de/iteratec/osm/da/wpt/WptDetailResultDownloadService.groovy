package de.iteratec.osm.da.wpt

import de.iteratec.osm.da.asset.AssetRequestGroup
import de.iteratec.osm.da.wpt.data.WPTVersion
import de.iteratec.osm.da.wpt.data.WptDetailResult
import de.iteratec.osm.da.wpt.resolve.WptDetailDataStrategyI
import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.persistence.AssetRequestPersistenceService
import de.iteratec.osm.da.wpt.resolve.WptDownloadWorker
import de.iteratec.osm.da.wpt.resolve.WptQueueFillWorker

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

    AssetRequestPersistenceService assetRequestPersistenceService
    WptDetailDataStrategyService wptDetailDataStrategyService
    int queueMaximumInMemory = 100
    final ConcurrentLinkedQueue<FetchJob> queue = new ConcurrentLinkedQueue<>()
    final HashSet<FetchJob> inProgress = []
    /**
     * We ned one additional worker to refill the queue. Otherwise every thread has to check if the queue should be refilled.
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
            println "starting worker"
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
    public void addToQueue(long osmInstance, long jobId, long jobGroupId, String wptBaseUrl, List<String> wptTestIds, String wptVersion) {
        wptTestIds.each {String wptTestId ->
            if(!AssetRequestGroup.findAllByWptBaseUrlAndWptTestIdAndOsmInstance(wptBaseUrl,wptTestId, osmInstance)) {
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

    public void deleteJob(FetchJob job) {
        job.delete(flush: true)
        inProgress.remove(job)
    }

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
        return strategy.getResult(fetchJob)
    }
}
