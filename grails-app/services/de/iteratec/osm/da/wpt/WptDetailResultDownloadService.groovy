package de.iteratec.osm.da.wpt

import de.iteratec.osm.da.fetch.FetchBatch
import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.fetch.Priority
import de.iteratec.osm.da.persistence.AssetRequestPersistenceService
import de.iteratec.osm.da.wpt.data.WPTVersion
import de.iteratec.osm.da.wpt.data.WptDetailResult
import de.iteratec.osm.da.wpt.resolve.*
import de.iteratec.osm.da.wpt.resolve.exceptions.WptVersionNotSupportedException
import org.springframework.beans.factory.InitializingBean

import java.util.concurrent.*

/**
 * This wptDetailResultDownloadService handles the downloading of the detail data from WPT.
 * The downloading will be queued and therefore eventually persisted. The queue will be persisted.
 * All downloaded data will be passed to the persistence wptDetailResultDownloadService and therefore will be persisted.
 */
class WptDetailResultDownloadService implements InitializingBean {

    /**
     * Maximum FetchJob which should be cached in each queue.
     */
    final static int QUEUE_MAXIMUM_IN_MEMORY = 1000
    /**
     * Number of workers which should download data from wpt.
     */
    final static int CORE_NUMBER_OF_DOWNLOAD_WORKER_THREADS = 20
    final static int MAXIMUM_NUMBER_OF_DOWNLOAD_WORKER_THREADS = 40
    final static int keepAliveTimeInSeconds = 5
    final static int FILL_QUEUE_INTERVAL_IN_SEC = 10
    final BlockingQueue<WptDownloadTask> queue = new ArrayBlockingQueue<>(QUEUE_MAXIMUM_IN_MEMORY);

    ThreadPoolExecutor downloadTaskExecutorService
    private ScheduledExecutorService scheduler

    /**
     * Maximum tries a FetchJob should get, until it will be ignored
     */
    static int MAX_TRY_COUNT = 3

    AssetRequestPersistenceService assetRequestPersistenceService
    WptDetailDataStrategyService wptDetailDataStrategyService

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
    int createNewFetchJob(long osmInstance, long jobId, long jobGroupId, String wptBaseUrl, List<String> wptTestIds, String wptVersion, Priority priority, FetchBatch fetchBatch = null) {
        int numberOfNewFetchJobs = 0

        log.debug("Persist ${wptTestIds.size()} wptTestIds as FetchJobs.")

        wptTestIds.each { String wptTestId ->
            new FetchJob(priority: priority, osmInstance: osmInstance, jobId: jobId, jobGroupId: jobGroupId, wptBaseURL: wptBaseUrl,
                    wptTestId: wptTestId, wptVersion: wptVersion, fetchBatch: fetchBatch).save(flush: true, failOnError: true)
            numberOfNewFetchJobs++
            log.debug("Created a FetchJob for WptId=$wptTestId")
        }

        return numberOfNewFetchJobs
    }

    /**
     * Marks a job as failed and removes it from progress
     * @param job
     */
    void markJobAsFailed(FetchJob job, Exception e) {
        if (job) {
            try {
                job.withNewSession {
                    job.tryCount++
                    log.debug("Try ${job.tryCount} for Job ${job.id}")
                    if (job.tryCount >= MAX_TRY_COUNT && job.fetchBatch) {
                            synchronized (job.fetchBatch) {
                                job.fetchBatch.addFailure()
                                job.fetchBatch = null
                            }
                    }
                    if (job.tryCount >= MAX_TRY_COUNT) {
                        log.debug("Maximum trycount for Job ${job.id} reached, it will be deleted")
                        job.delete(failOnError: true, flush: true)
                    } else {
                        job.save(failOnError: true, flush: true)
                    }
                }
            } catch (Exception exception) {
                log.debug("caught exception while marking job as failed: ${exception.getMessage()}")
            }
        }
    }

    /**
     * Fetches the WptDetailData from the given detail page.
     * If testDetailsWaterfallURL is null, this method will return null
     * @param fetchJob
     * @return WptDetailResult
     */
    public WptDetailResult downloadWptDetailResultFromWPTInstance(FetchJob fetchJob) {
        WptDetailDataStrategyI strategy = wptDetailDataStrategyService.getStrategyForVersion(WPTVersion.get(fetchJob.wptVersion))
        if (!strategy) {
            throw new WptVersionNotSupportedException(fetchJob.wptBaseURL, fetchJob.wptTestId, fetchJob.wptVersion)
        }
        return strategy.getResult(fetchJob)
    }

    int getActiveThreadCount() {
        return downloadTaskExecutorService.getActiveCount()
    }

    int getQueuedJobCount() {
        return QUEUE_MAXIMUM_IN_MEMORY - downloadTaskExecutorService.getQueue().remainingCapacity()
    }

    @Override
    void afterPropertiesSet() throws Exception {
        downloadTaskExecutorService = new ThreadPoolExecutor(
                CORE_NUMBER_OF_DOWNLOAD_WORKER_THREADS, MAXIMUM_NUMBER_OF_DOWNLOAD_WORKER_THREADS,
                keepAliveTimeInSeconds, TimeUnit.SECONDS, queue)

        scheduler = Executors.newScheduledThreadPool(1)
        scheduler.scheduleAtFixedRate(
            new WptDownloadTaskCreator(downloadTaskExecutorService, this),
            FILL_QUEUE_INTERVAL_IN_SEC,
            FILL_QUEUE_INTERVAL_IN_SEC,
            TimeUnit.SECONDS
        )
    }
}
