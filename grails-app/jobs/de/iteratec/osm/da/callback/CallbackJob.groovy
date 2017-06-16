package de.iteratec.osm.da.callback

import de.iteratec.osm.da.HttpRequestService
import de.iteratec.osm.da.fetch.FetchBatch
import org.joda.time.DateTime

class CallbackJob {
    static triggers = {
      simple repeatInterval: 30000l // execute job once in 30 seconds
    }

    HttpRequestService httpRequestService

    def execute(){
        FetchBatch.findAllByQueuingDone(false).each { FetchBatch fetchBatch ->
            fetchBatch.withNewSession {
                log.debug("Start logging of FetchBatch ${fetchBatch.id} to OSM with url ${fetchBatch.osmUrl}.")
                int countAssets = fetchBatch.countFetchJobs
                int loadedAssets = countAssets - fetchBatch.fetchJobs.size()
                def callbackUrl = fetchBatch.callbackUrl
                def failureCount = fetchBatch.failureList.size()
                if(fetchBatch.lastValue == loadedAssets){
                    if((new DateTime().minusMinutes(15).millis - fetchBatch.lastUpdate.time) > 0){ // after 15 min without any update, the FetchBatch has failed and can be closed
                        fetchBatch.queuingDone = false
                        synchronized (fetchBatch) {
                            fetchBatch.save(flush: true)
                        }
                    }

                }else {
                    fetchBatch.lastUpdate = new Date()
                    fetchBatch.lastValue = loadedAssets
                    synchronized (fetchBatch) {
                        fetchBatch.save(flush: true)
                    }
                    try {
                        log.debug("Trying to report actual message of FetchBatch ${fetchBatch.id} to OSM with url ${fetchBatch.osmUrl} using callbackurl ${callbackUrl}.")
                        httpRequestService.postCallback(callbackUrl, countAssets, loadedAssets, fetchBatch.callBackId, fetchBatch.osmUrl, failureCount)
                        log.debug("... report sent")
                    } catch (Exception e) {
                        //TODO: Do some clever exceptionhandling
                        log.warn("Can not report state of FetchBatch ${fetchBatch.id} to OSM with url ${fetchBatch.osmUrl}.")
                        throw e
                    }
                    if (loadedAssets == countAssets) {
                        log.debug("Delete finished FetchBatch ${fetchBatch.id}")
                        fetchBatch.delete(flush: true)
                    }
                }
            }
        }
    }
}
