import de.iteratec.osm.da.api.ApiKey

class BootStrap {
    def grailsApplication

    def init = { servletContext ->
        if(ApiKey.list().isEmpty()){
            String initialApiKey = grailsApplication.config.grails.de.iteratec.osm.da.initialApiKey.isEmpty() ?
                    null : grailsApplication.config.grails.de.iteratec.osm.da.initialApiKey
            String initialOsmUrl = grailsApplication.config.grails.de.iteratec.osm.da.initialOsmUrl.isEmpty() ?
                    null : grailsApplication.config.grails.de.iteratec.osm.da.initialOsmUrl
            if(!initialApiKey || !initialOsmUrl) log.warn("initial ApiKey configuration missing")
            else new ApiKey([secretKey:initialApiKey,description:"Initial ApiKey for communication with OpenSpeedMonitor",
                        osmUrl:initialOsmUrl, valid: true, allowedToTriggerFetchJobs:true,allowedToDisplayResults:true,
                        allowedToUpdateOsmUrl:true, allowedToUpdateMapping:true]).save(failOnError:true)

        }


    }
    def destroy = {
    }
}
