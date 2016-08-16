package de.iteratec.osm.da.wpt.resolve

import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.wpt.WptDetailResultDownloadService
import de.iteratec.osm.da.wpt.data.WptDetailResult
import groovy.util.logging.Log
import org.apache.commons.logging.LogFactory
class WptDownloadWorker implements Runnable{

    private static idCount = 0
    private final id = nextId();
    WptDetailResultDownloadService service
    private static final log = LogFactory.getLog(this)
    WptDownloadWorker(WptDetailResultDownloadService service) {
        this.service = service
        log.info("$this started")
    }

    @Override
    String toString() {
        return "${this.class.simpleName} $id"
    }

    private static int nextId(){
        return idCount++
    }

    @Override
    void run() {
        sleep(10000) //wait for the application to fully start
        while (service.workerShouldRun){
            while (!service.queue.isEmpty()) {
                FetchJob currentJob
                try {
                    currentJob = service.getNextJob()
                    if (currentJob) {
                        log.debug(this.toString() + "found job and start: " + currentJob.id)
                        WptDetailResult result = service.downloadWptDetailResultFromWPTInstance(currentJob)
                        if(result){
                            service.assetRequestPersistenceService.saveDetailDataForJobResult(result, currentJob)
                            log.debug(this.toString() + "FetchJob $currentJob.id finished, start deleting ")
                            service.deleteJob(currentJob)
                        } else {
                            service.inProgress.remove(currentJob)
                            log.error(this.toString() + " couldn't download job with id ${currentJob.id} will be skipped.")
                        }
                    }

                } catch (Exception e) {
                    if(currentJob != null) service.inProgress.remove(currentJob)
                    log.error(this.toString() +" encountered an error, job with id ${currentJob.id} will be skipped. \n"+e)
                    e.printStackTrace()
                }
            }
            sleep(10000)//To reduce overhead we just wait 2 second and recheck, if the queue is still empty
        }
    }
}
