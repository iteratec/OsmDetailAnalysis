package de.iteratec.osm.da.wpt.resolve

import de.iteratec.osm.da.asset.AssetRequestGroup
import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.wpt.WptDetailResultDownloadService
import org.apache.commons.logging.LogFactory

import java.util.concurrent.BlockingQueue
import java.util.concurrent.ThreadPoolExecutor


class WptDownloadTaskCreator implements Runnable {

    ThreadPoolExecutor executorService
    WptDetailResultDownloadService wptDetailResultDownloadService
    private static final log = LogFactory.getLog(this)

    WptDownloadTaskCreator(ThreadPoolExecutor executorService, WptDetailResultDownloadService wptDetailResultDownloadService) {
        this.executorService = executorService
        this.wptDetailResultDownloadService = wptDetailResultDownloadService
    }

    @Override
    void run() {
        queueDownloadTasks()
    }

    /**
     * Tries to find FetchJobs in Database and adds them to the normalPriorityQueue.
     * FetchJobs which are already in the normalPriorityQueue won't be added.
     * This will only fill the normalPriorityQueue if it is atleast half empty, to prevent non-stopping queries against the database.
     */
    void queueDownloadTasks() {
        BlockingQueue<WptDownloadTask> currentQueue = executorService.getQueue()
        List<Integer> idsToIgnore = []
        currentQueue.each {
            idsToIgnore << it.fetchJob.id
        }

        Set<FetchJob> fetchJobsToQueue = loadJobsFromDatabase(currentQueue.remainingCapacity(), idsToIgnore, WptDetailResultDownloadService.MAX_TRY_COUNT)
        fetchJobsToQueue.each {
            try {
                executorService.execute(new WptDownloadTask(it, wptDetailResultDownloadService))
            } catch (Exception e) {
                log.debug("an exception occured while trying to queue fetchJob ${it}: ${e.getMessage()}")
            }
        }
        log.debug("${fetchJobsToQueue.size()} fetchJobs queued")
    }

    /**
     * Loads FetchJobs from Database which doesn't have a id in the given list.
     * The returned list will has a maximum size of maximumAmount.
     * @param maximumAmount
     * @param idsToIgnore
     * @param maximumTries amount which should be succeed from FetchJobs
     * @param priority the priority which should be loaded
     */
    Set<FetchJob> loadJobsFromDatabase(int maximumAmount, List<Integer> idsToIgnore, int maximumTries) {
        Set<FetchJob> fetchJobs = new HashSet<>()

        def notAddedJobsButStillJobsInDB = true
        while (notAddedJobsButStillJobsInDB) {  //needed in case we have a lot of duplicates in the mongobd
            FetchJob.withNewSession {
                List<FetchJob> jobsToAdd = FetchJob.createCriteria().list(max: maximumAmount) {
                    and {
                        not {
                            'in'("id", idsToIgnore)
                        }
                        lt('tryCount', maximumTries)
                        order('priority', 'desc')
                        order('created', 'asc')
                    }
                }

                if (jobsToAdd?.size() > 0) {
                    jobsToAdd.each { FetchJob fetchJob ->
                        int identicalFetchJobs = FetchJob.countByWptBaseURLAndWptTestIdAndOsmInstance(fetchJob.wptBaseURL, fetchJob.wptTestId, fetchJob.osmInstance)
                        int alreadyLoadedAssetGroups = AssetRequestGroup.countByWptBaseUrlAndWptTestIdAndOsmInstance(fetchJob.wptBaseURL, fetchJob.wptTestId, fetchJob.osmInstance)

                        if (identicalFetchJobs > 1 || alreadyLoadedAssetGroups > 0) {
                            fetchJob.delete(failOnError: true)// It's not necessary to download the assets twice
                        } else {
                            notAddedJobsButStillJobsInDB = false
                            fetchJobs.add(fetchJob)
                        }
                    }
                } else {
                    notAddedJobsButStillJobsInDB = false
                }
            }
        }

        return fetchJobs
    }
}