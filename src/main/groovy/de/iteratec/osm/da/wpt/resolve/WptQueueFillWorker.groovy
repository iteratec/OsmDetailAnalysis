package de.iteratec.osm.da.wpt.resolve

import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.fetch.Priority
import de.iteratec.osm.da.wpt.WptDetailResultDownloadService
import org.apache.commons.logging.LogFactory

/**
 * Worker for WptDetailResultDownloadService.
 * Fills the normalPriorityQueue if it reaches a given point
 */
class WptQueueFillWorker extends WptWorker {

    WptDetailResultDownloadService service
    private static final log = LogFactory.getLog(this)

    /**
     * Amount of time in ms to wait, after the queues were filled.
     */
    int threshold = 5000


    WptQueueFillWorker(WptDetailResultDownloadService service) {
        this.type = WptWorkerType.WptQueueFillWorker
        this.service = service
        log.info("${this.class.simpleName} started")
    }

    @Override
    void run() {
        while (service.workerShouldRun) {
            setWait()
            sleep(threshold)
            fillQueues()
            setEndOfAction()
        }
    }

    /**
     * Tries to find FetchJobs in Database and adds them to the normalPriorityQueue.
     * FetchJobs which are already in the normalPriorityQueue won't be added.
     * This will only fill the normalPriorityQueue if it is atleast half empty, to prevent non-stopping queries against the database.
     */
    void fillQueues(){
        service.getAvailablePriorities().each {
            if (service.getJobCountInQueueByPriority(it) < (service.getQueueMaximumInMemory() / 2 as int)) {
                Set<FetchJob> jobsInMemory = collectJobsInMemory(it)
                log.debug("Jobs in cached $it queue and in progress: ${jobsInMemory.size()}")
                int maxToAdd = service.getQueueMaximumInMemory() - service.getJobCountInQueueByPriority(it) - 1 // -1 because there could be a running job added
                loadJobsFromDatabase(maxToAdd, jobsInMemory*.id as List<Integer>, service.getMaxTryCount(), it)
            }
        }
    }

    /**
     * Loads FetchJobs from Database which doesn't have a id in the given list.
     * The returned list will has a maximum size of maximumAmount.
     * @param maximumAmount
     * @param idsToIgnore
     * @param maximumTries amount which should be succeed from FetchJobs
     * @param priority the priority which should be loaded
     */
    void loadJobsFromDatabase(int maximumAmount, List<Integer> idsToIgnore, int maximumTries, Priority priority){
        FetchJob.withNewSession {
            def c = FetchJob.createCriteria()
            List<FetchJob> jobsToAdd = c.list(max: maximumAmount) {
                and {
                    eq('priority',priority.value)
                    order('created','asc')
                    not {
                        'in'("id", idsToIgnore)
                    }
                    lt('tryCount', maximumTries)
                }
            }
            if(jobsToAdd?.size()>0){
                jobsToAdd.each { FetchJob fetchJob ->
                    List<FetchJob> jobs = FetchJob.findAllByWptBaseURLAndWptTestId(fetchJob.wptBaseURL,fetchJob.wptTestId)
                    if(jobs.size() > 1) service.deleteJob(fetchJob) // It's not necessary to download the assets twice
                }
                service.addExistingFetchJobToQueue(jobsToAdd, priority)
            }
        }
    }
    /**
     * Checks which jobs are either in normalPriorityQueue or in progress and combines them in one list.
     * @return a List of all FetchJobs in normalPriorityQueue or in progress
     */
    Set<FetchJob> collectJobsInMemory(Priority priority){
        Set<FetchJob> alreadyLoaded = []
        synchronized (service.inProgress) {
            alreadyLoaded.addAll(service.inProgress)
            alreadyLoaded.addAll(service.queueHashMap[priority])
        }
        return alreadyLoaded
    }
}
