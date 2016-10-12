package de.iteratec.osm.da.api

import de.iteratec.osm.da.fetch.Priority
import de.iteratec.osm.da.instances.OsmInstance
import de.iteratec.osm.da.wpt.WptDetailResultDownloadService
import de.iteratec.osm.da.wpt.data.WPTVersion
import de.iteratec.osm.da.wpt.resolve.WptDownloadWorker
import grails.converters.JSON
import grails.validation.Validateable

class StatusRestApiController {

    WptDetailResultDownloadService wptDetailResultDownloadService

    /**
     * Sends message with given httpStatus and message as http response and breaks action (no subsequent
     * action code is executed).
     * @param httpStatus
     * @param message
     */
    private void sendJSONResponseAsStream(Integer httpStatus, def message) {
        response.setContentType('text/json;charset=UTF-8')
        response.status = httpStatus
        render message
    }


    def getQueueStatus(){
        Map<String, Integer> sizes = [:]
        Priority.values().each {
            sizes[it.name()] = wptDetailResultDownloadService.getJobCountInQueueByPriority(it)
        }
        sendJSONResponseAsStream(200, sizes as JSON)
    }
}
