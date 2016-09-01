package de.iteratec.osm.da.wpt.resolve

import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.fetch.Priority
import de.iteratec.osm.da.wpt.WptDetailResultDownloadService
import org.apache.commons.logging.LogFactory

/**
 * Worker for WptDetailResultDownloadService.
 * Fills the normalPriorityQueue if it reaches a given point
 */
class WptQueueFillWorker implements Runnable {

    WptDetailResultDownloadService service
    private static final log = LogFactory.getLog(this)

    /**
     * Amount of time in ms to wait, after the normalPriorityQueue was filled.
     */
    int threshhold = 5000


    WptQueueFillWorker(WptDetailResultDownloadService service) {
        this.service = service
        log.info("${this.class.simpleName} started")
    }

    @Override
    void run() {
        sleep(threshhold * 2) // wait for application to start
        while (service.workerShouldRun) {
            fillQueue()
            sleep(threshhold)
        }
    }

    /**
     * Tries to find FetchJobs in Database and adds them to the normalPriorityQueue.
     * FetchJobs which are already in the normalPriorityQueue won't be added.
     * This will only fill the normalPriorityQueue if it is atleast half empty, to prevent non-stopping queries against the database.
     */
    void fillQueue(){
        if (service.queueHashMap[Priority.Normal].size() < (service.queueMaximumInMemory / 2 as int)) {
            Set<FetchJob> jobsInMemory = collectJobsInMemory()
            log.debug("Jobs in cached normalPriorityQueue and in progress: ${jobsInMemory.size()}")
            int maxToAdd = service.queueMaximumInMemory - service.queueHashMap[Priority.Normal].size() - 1 // -1 because there could be a running job added
            loadJobsFromDatabase(maxToAdd, jobsInMemory*.id as List<Integer>, service.maxTryCount)
        }
    }

    /**
     * Loads FetchJobs from Database which doesn't have a id in the given list.
     * The returned list will has a maximum size of maximumAmount.
     * @param maximumAmount
     * @param idsToIgnore
     * @param maximumTries amount which should be succeed from FetchJobs
     */
    void loadJobsFromDatabase(int maximumAmount, List<Integer> idsToIgnore, int maximumTries){
        FetchJob.withNewSession {
            def c = FetchJob.createCriteria()
            List<FetchJob> jobsToAdd = c.list(max: maximumAmount) {
                and {
                    order('priority','desc')
                    order('created','asc')
                    not {
                        'in'("id", idsToIgnore)
                    }
                    lt('tryCount', maximumTries)
                }
            }
            if(jobsToAdd?.size()>0){
                service.addExistingFetchJobToQueue(jobsToAdd)
            }
        }
    }
    /**
     * Checks whichs jobs are either in normalPriorityQueue or in progress and combines them in one list.
     * @return a List of all FetchJobs in normalPriorityQueue or in progress
     */
    Set<FetchJob> collectJobsInMemory(){
        Set<FetchJob> alreadyLoaded = []
        synchronized (service.inProgress) {
            alreadyLoaded.addAll(service.inProgress)
            alreadyLoaded.addAll(service.queueHashMap[Priority.Normal])
        }
        return alreadyLoaded
    }
}
