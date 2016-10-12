package de.iteratec.osm.da.wpt.resolve

import de.iteratec.osm.da.fetch.FailedFetchJob
import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.wpt.WptDetailResultDownloadService
import de.iteratec.osm.da.wpt.data.WptDetailResult
import org.apache.commons.logging.LogFactory
/**
 * A runnable which can be used to resolve FetchJobs in background.
 */
class WptDownloadWorker extends WptWorker{

    private static final log = LogFactory.getLog(this)

    WptDetailResultDownloadService service

    WptDownloadWorker(WptDetailResultDownloadService service) {
        this.type = WptWorkerType.WptDownloadWorker
        this.service = service
        log.info("$this started")
    }

    @Override
    String toString() {
        return "${this.class.simpleName} $id"
    }

    @Override
    void run() {
        sleep(10000) //wait for the application to fully start.
        while (service.workerShouldRun){
            log.debug(this.toString() + " is ready for new work")
            setWait()
            fetch()
            setEndOfAction()
        }
    }

    /**
     * Tries to fetch the WptDetailResult of a FetchJob. If this succeeds the Job will be removed from the normalPriorityQueue and will be deleted.
     * Otherwise it will be just removed from normalPriorityQueue and will be marked as failed.
     */
    void fetch(){
        FetchJob currentJob
        try {
            currentJob = service.getNextJob()
            if (currentJob) {
                log.debug(this.toString() + " got job(${currentJob.id})")
                WptDetailResult result = service.downloadWptDetailResultFromWPTInstance(currentJob)
                handleResult(result, currentJob)
            }
        } catch (Exception e) {
            log.error("Job with id ${currentJob?.id} encountert an error while trying to get the following result:\n" +
                    "'${currentJob.getWptBaseURL()}/jsonResult.php?test=${currentJob.wptTestId}&requests=1&multiStepFormat=1'." +
                    "\n\tNew try count: ${currentJob?.tryCount}\n Message: $e")
            service.markJobAsFailed(currentJob)
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
            FailedFetchJob failedFetchJob = service.failedFetchJobService.markJobAsFailedIfNeeded(result, currentJob)
            if(failedFetchJob){
                log.info("FetchJob from ${result.wptBaseUrl+result.wptTestID} will be ignored, reason: ${failedFetchJob.reason}")
            } else{
                log.debug(this.toString() + " FetchJob $currentJob.id: saveDetailDataForJobResult")
                service.assetRequestPersistenceService.saveDetailDataForJobResult(result, currentJob)
                log.debug(this.toString() + " FetchJob $currentJob.id: saveDetailDataForJobResult... DONE")
            }
            log.debug(this.toString() + " FetchJob $currentJob.id: deleteJob")
            service.deleteJob(currentJob)
            log.debug(this.toString() + " FetchJob $currentJob.id: deleteJob... DONE")
        } else {
            log.error(this.toString() + " couldn't download job with id ${currentJob.id} will be skipped. New try count: ${currentJob?.tryCount}")
            service.markJobAsFailed(currentJob)
        }
    }
}
