package de.iteratec.osm.da.api

import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.wpt.WptDetailResultDownloadService
import grails.converters.JSON
import grails.core.GrailsApplication

class StatusRestApiController {

    GrailsApplication grailsApplication
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

    def getFetchJobStatus() {
        StatusDTO statusDTO = new StatusDTO()
        statusDTO.appVersion = grailsApplication.config.info.app.version
        statusDTO.activeThreads = wptDetailResultDownloadService.getActiveThreadCount()
        statusDTO.jobsInDB = FetchJob.countByTryCountLessThan(wptDetailResultDownloadService.MAX_TRY_COUNT)

        statusDTO.queuedJobs = wptDetailResultDownloadService.getQueuedJobCount()
        sendJSONResponseAsStream(200, statusDTO as JSON)
    }
}
