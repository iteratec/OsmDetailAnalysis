package de.iteratec.osm.da.wpt.resolve

import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.wpt.WptDetailResultDownloadService
import org.apache.commons.logging.LogFactory

/**
 * Worker for WptDetailResultDownloadService.
 * Fills the queue if it reaches a given point
 */
class WptQueueFillWorker implements Runnable {

    WptDetailResultDownloadService service
    private static final log = LogFactory.getLog(this)

    /**
     * Amount of time in ms to wait, after the queue was filled.
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
     * Tries to find FetchJobs in Database and adds them to the queue.
     * FetchJobs which are already in the queue won't be added.
     */
    void fillQueue(){
        if (service.queue.size() < (service.queueMaximumInMemory / 2 as int)) {
            Set<FetchJob> jobsInMemory = collectJobsInMemory()
            log.debug("Jobs in cached queue and in progress: ${jobsInMemory.size()}")
            int maxToAdd = service.queueMaximumInMemory - service.queue.size()
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
                    not {
                        'in'("id", idsToIgnore)
                    }
                    lt('tryCount', maximumTries)
                }
            }
            if(jobsToAdd?.size()>0){
                service.queue.addAll(jobsToAdd)
                log.info("Added ${jobsToAdd.size()} jobs to queue")

            }
        }
    }
    /**
     * Checks whichs jobs are either in queue or in progress and combines them in one list.
     * @return a List of all FetchJobs in queue or in progress
     */
    Set<FetchJob> collectJobsInMemory(){
        Set<FetchJob> alreadyLoaded = []
        synchronized (service.inProgress) {
            alreadyLoaded.addAll(service.inProgress)
            alreadyLoaded.addAll(service.queue)
        }
        return alreadyLoaded
    }
}
