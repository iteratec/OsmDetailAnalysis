package de.iteratec.osm.da.wpt.resolve

import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.wpt.WptDetailResultDownloadService
import de.iteratec.osm.da.wpt.data.WptDetailResult
import groovy.transform.EqualsAndHashCode
import org.apache.commons.logging.LogFactory

@EqualsAndHashCode(includes = ['fetchJob'])
class WptDownloadTask implements Runnable {

    private static final log = LogFactory.getLog(this)
    WptDetailResultDownloadService wptDetailResultDownloadService

    FetchJob fetchJob

    WptDownloadTask(FetchJob fetchJob, WptDetailResultDownloadService wptDetailResultDownloadService) {
        this.fetchJob = fetchJob
        this.wptDetailResultDownloadService = wptDetailResultDownloadService
    }

    @Override
    void run() {
        fetchJob()
    }

    private void fetchJob() {
        try {
            log.debug(this.toString() + " start handling Job(${fetchJob.id}).")
            WptDetailResult result = wptDetailResultDownloadService.downloadWptDetailResultFromWPTInstance(fetchJob)
            log.debug(this.toString() + " got WptDetailResult with ${result.steps.size()} steps for Job(${fetchJob.id}). Starting to persist it now ...")
            handleResult(result, fetchJob)
            log.debug(this.toString() + " finished persisting for Job(${fetchJob.id}).")
        } catch (Exception e) {
            log.debug(this.toString() + " caught exception during handling of Job(${fetchJob.id}): ${e}")
            wptDetailResultDownloadService.markJobAsFailed(fetchJob, e)
        }
    }

    /**
     * Takes care of a WptResult. If there is result, the wptDetailResultDownloadService will be noted to delete the job.
     * Otherwise the job will be marked as failed.
     * @param result
     * @param fetchJob FetchJob that was used to get this result
     */
    void handleResult(WptDetailResult result, FetchJob fetchJob) {
        try {
            log.debug(this.toString() + " FetchJob $fetchJob.id: saveDetailDataForJobResult")
            wptDetailResultDownloadService.assetRequestPersistenceService.saveDetailDataForJobResult(result, fetchJob)

            log.debug(this.toString() + " FetchJob $fetchJob.id: deleteJob")
            fetchJob.delete(failOnError: true)
        } catch (Exception e) {
            log.debug(this.toString() + " caught exception during handling of Job(${fetchJob.id}): ${e}")
            wptDetailResultDownloadService.markJobAsFailed(fetchJob, e)
        }
    }
}
