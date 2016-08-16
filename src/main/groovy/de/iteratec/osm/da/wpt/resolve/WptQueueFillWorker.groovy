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

    WptQueueFillWorker(WptDetailResultDownloadService service) {
        this.service = service
        log.info("${this.class.simpleName} started")
    }

    @Override
    void run() {
        sleep(10000) // wait for application to start
        while (service.workerShouldRun) {
            if (service.queue.size() < service.queueMaximumInMemory / 2) {
                Set<FetchJob> alreadyLoaded = []
                synchronized (service.inProgress) {
                    alreadyLoaded.addAll(service.inProgress)
                    alreadyLoaded.addAll(service.queue)
                }
                FetchJob.withNewSession {

                    int maxToAdd = service.queueMaximumInMemory - service.queue.size()
                    def c = FetchJob.createCriteria()
                    def jobsToAdd = c.list(max: maxToAdd) {
                        not {
                            'in'("id", alreadyLoaded*.id)
                        }
                    }

                    if(jobsToAdd.size()>0){
                        service.queue.addAll(jobsToAdd)
                        log.info("Added ${jobsToAdd.size()} jobs to queue")

                    }
                }
            }
            sleep(5000)
        }
    }
}
