package de.iteratec.osm.da.wpt.resolve

import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.wpt.WptDetailResultDownloadService
/**
 * Worker for WptDetailResultDownloadService.
 * Fills the queue if it reaches a given point
 */
class WptQueueFillWorker implements Runnable {

    WptDetailResultDownloadService service

    WptQueueFillWorker(WptDetailResultDownloadService service) {
        this.service = service
    }

    @Override
    void run() {
        while (service.workerShouldRun) {
            if (service.queue.size() < service.queueMaximumInMemory / 2) {
                Set<FetchJob> alreadyLoaded = []
                synchronized (service.inProgress) {
                    alreadyLoaded.addAll(service.inProgress)
                    alreadyLoaded.addAll(service.queue)
                }
                FetchJob.withNewSession {
                    def c = FetchJob.createCriteria()
                    def jobs = c.list(max: service.queueMaximumInMemory - service.queue.size()) {
                        not {
                            'in'("id", alreadyLoaded*.id)
                        }
                    }
                    service.queue.addAll(jobs)
                    if(jobs.size()>0) println "Added $jobs to queue"
                }
            }
            sleep(5000)
        }
    }
}
