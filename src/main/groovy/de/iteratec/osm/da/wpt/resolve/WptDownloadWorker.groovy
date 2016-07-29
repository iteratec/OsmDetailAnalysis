package de.iteratec.osm.da.wpt.resolve

import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.wpt.WptDetailResultDownloadService
import de.iteratec.osm.da.wpt.data.WptDetailResult

class WptDownloadWorker implements Runnable{

    WptDetailResultDownloadService service

    WptDownloadWorker(WptDetailResultDownloadService service) {
        this.service = service
        println "started worker"
    }

    @Override
    void run() {
        while (service.workerShouldRun){
            while (!service.queue.isEmpty()){
                FetchJob currentJob = service.getNextJob()
                if(currentJob){
                    println Thread.currentThread().toString()+"found job and start: "+currentJob.id
                    while(currentJob.next()){
                        WptDetailResult result = service.downloadWptDetailResultFromWPTInstance(currentJob)
                        service.assetRequestPersistenceService.saveDetailDataForJobResult(result,currentJob)
                    }
                    println Thread.currentThread().toString()+"FetchJob $currentJob.id finished, start deleting "
                    service.deleteJob(currentJob)
                }
            }
            sleep(1000)//To reduce overhead we just wait 2 second and recheck, if the queue is still empty
        }
    }
}
