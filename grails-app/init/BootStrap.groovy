import de.iteratec.osm.da.api.ApiKey
import de.iteratec.osm.da.instances.OsmInstance
import de.iteratec.osm.da.instances.OsmMapping
import de.iteratec.osm.da.mapping.OsmDomain

class BootStrap {
    def grailsApplication

    def init = { servletContext ->
        if (ApiKey.list().isEmpty()) {
            String initialApiKey = grailsApplication.config.grails.de.iteratec.osm.da.initialApiKey.isEmpty() ?
                    null : grailsApplication.config.grails.de.iteratec.osm.da.initialApiKey
            String initialOsmUrl = grailsApplication.config.grails.de.iteratec.osm.da.initialOsmUrl.isEmpty() ?
                    null : grailsApplication.config.grails.de.iteratec.osm.da.initialOsmUrl
            if (!initialApiKey || !initialOsmUrl) log.warn("initial ApiKey configuration missing")
            else {
                OsmInstance.findAll().each { it.delete(failOnError: true) }
                OsmInstance osmInstance = new OsmInstance([name                  : "Initial Osm", url: initialOsmUrl,
                                                           jobGroupMapping: new OsmMapping([domain: OsmDomain.JobGroup]),
                                                           locationMapping     : new OsmMapping([domain: OsmDomain.Location]),
                                                           measuredEventMapping: new OsmMapping([domain: OsmDomain.MeasuredEvent]),
                                                           browserMapping      : new OsmMapping([domain: OsmDomain.Browser]),
                                                           pageMapping         : new OsmMapping([domain: OsmDomain.Page]),
                                                           jobMapping          : new OsmMapping([domain: OsmDomain.Job])]).save(failOnError: true)
                new ApiKey([secretKey            : initialApiKey, description: "Initial ApiKey for communication with OpenSpeedMonitor",
                            osmInstance          : osmInstance, valid: true, allowedToTriggerFetchJobs: true, allowedToDisplayResults: true,
                            allowedToUpdateOsmUrl: true, allowedToUpdateMapping: true]).save(failOnError: true)
            }

        }
    }
    def destroy = {
    }
}
