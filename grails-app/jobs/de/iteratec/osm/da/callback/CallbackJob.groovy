package de.iteratec.osm.da.callback

import de.iteratec.osm.da.HttpRequestService
import de.iteratec.osm.da.fetch.FetchBatch
import org.joda.time.DateTime

import javax.persistence.criteria.Fetch

class CallbackJob {
    static triggers = {
      simple repeatInterval: 30000l // execute job once in 30 seconds
    }

    HttpRequestService httpRequestService

    def execute(){
        FetchBatch.list().each { FetchBatch fetchBatch ->
            fetchBatch.withNewSession {

                if(fetchBatch.queuingDone) {
                    int countAssets = fetchBatch.countFetchJobs
                    int loadedAssets = countAssets - fetchBatch.fetchJobs.size()
                    def callbackUrl = fetchBatch.callbackUrl
                    def failureCount = fetchBatch.failureList.size()
                    if(fetchBatch.lastValue == loadedAssets){
                        if((new DateTime().minusMinutes(15).millis - fetchBatch.lastUpdate.time) > 0){ // after 15 min without any update, the FetchBatch has failed and can be closed
                            println(new DateTime().minusMinutes(1).millis - fetchBatch.lastUpdate.time)
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
                            httpRequestService.postCallback(callbackUrl, countAssets, loadedAssets, fetchBatch.callBackId, fetchBatch.osmUrl, failureCount)
                        } catch (Exception e) {
                            //TODO: Do some clever exceptionhandling
                            log.warn("Can not report state of FetchBatch ${fetchBatch.id} to OSM with url ${fetchBatch.osmUrl}.")
                            throw e
                        }
                        if (loadedAssets == countAssets) {
                            fetchBatch.delete(flush: true)
                        }
                    }
                }
            }
        }
    }
}
