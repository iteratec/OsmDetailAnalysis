package de.iteratec.osm.da.wpt.resolve

import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.wpt.WptDetailResultDownloadService
import de.iteratec.osm.da.wpt.data.WptDetailResult
import org.apache.commons.logging.LogFactory

import javax.persistence.criteria.Fetch

/**
 * A runnable which can be used to resolve FetchJobs in background.
 */
class WptDownloadWorker implements Runnable{

    /**
     * This is used as a counter, so every worker can get a unique id
     */
    private static idCount = 0
    /**
     * The actual id of this worker
     */
    private final id = nextId();
    private static final log = LogFactory.getLog(this)

    WptDetailResultDownloadService service
    /**
     * If the queue is empty we will this thresold ms until we recheck the queue.
     * Note that if the queue is never empty, we will never use this threshold
     */
    int threshold = 2000


    WptDownloadWorker(WptDetailResultDownloadService service) {
        this.service = service
        log.info("$this started")
    }

    @Override
    String toString() {
        return "${this.class.simpleName} $id"
    }

    /**
     * Get a id for a new worker
     * @return
     */
    private static int nextId(){
        return idCount++
    }

    @Override
    void run() {
        sleep(10000) //wait for the application to fully start.
        while (service.workerShouldRun){
            log.debug(this.toString() + " is ready for new work")
            fetch()
        }
    }

    /**
     * Tries to fetch the WptDetailResult of a FetchJob. If this succeed the Job will be removed from the queue and will be deleted.
     * Otherwise it will be just removed from queue and will be marked as failed.
     */
    void fetch(){
        FetchJob currentJob
        try {
            currentJob = service.getNextJob()
            if (currentJob) {
                log.debug(this.toString() + "found job and start: " + currentJob.id)
                WptDetailResult result = service.downloadWptDetailResultFromWPTInstance(currentJob)
                handleResult(result, currentJob)
            }
        } catch (Exception e) {
            service.markJobAsFailed(currentJob)
            log.error(this.toString() +" encountered an error, job with id ${currentJob?.id} will be skipped. New try count: ${currentJob?.tryCount}\n"+e)
            e.printStackTrace()
        }
    }
    /**
     * Takes care of a WptResult. If there is result, the service will be noted to delete the job.
     * Otherwise the job will be marked as failed.
     * @param result
     * @param currentJob FetchJob that was used to get this result
     */
    void handleResult(WptDetailResult result, FetchJob currentJob){
        if(result){
            service.assetRequestPersistenceService.saveDetailDataForJobResult(result, currentJob)
            log.debug(this.toString() + " FetchJob $currentJob.id finished, start deleting ")
            service.deleteJob(currentJob)
        } else {
            service.markJobAsFailed(currentJob)
            log.error(this.toString() + " couldn't download job with id ${currentJob.id} will be skipped. New try count: ${currentJob?.tryCount}")
        }
    }
}
