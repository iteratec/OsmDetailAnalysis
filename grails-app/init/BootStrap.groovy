import de.iteratec.osm.da.api.ApiKey
import de.iteratec.osm.da.instances.OsmInstance
import de.iteratec.osm.da.instances.OsmMapping
import de.iteratec.osm.da.mapping.OsmDomain
import de.iteratec.osm.da.migration.MigrationUtil
import de.iteratec.osm.da.util.UrlUtil

class BootStrap {
    def grailsApplication

    def init = { servletContext ->
        MigrationUtil.executeChanges()
        initOsmInstances()
    }

    private void initOsmInstances() {
        def apiKeyOsmMap = grailsApplication.config.grails.de.iteratec.osm.da.apiKeys
        apiKeyOsmMap.each { unnecessaryParameterForcedUpponUsByGrails, apiKeyOsmTupel ->
            boolean apiKeyIsKnown = false
            List apiKeys = ApiKey.findAllBySecretKey(apiKeyOsmTupel.key)
            String configOsmUrl = UrlUtil.appendTrailingSlash(apiKeyOsmTupel.osmUrl)
            String path = ""
            String protocol = ""
            switch (configOsmUrl){
                case ~/http:\/\/.+/:
                    path = configOsmUrl.replace("http://","")
                    protocol = "http"
                    break
                case ~/https:\/\/.+/:
                    path = configOsmUrl.replace("https://","")
                    protocol = "https"
                    break
            }
            apiKeys.each { ApiKey apiKey ->
                if (apiKey.osmInstance.domainPath == path) {
                    apiKeyIsKnown = true
                }
            }
            if (!apiKeyIsKnown) {
                OsmInstance osmInstance = OsmInstance.findByDomainPath(configOsmUrl)
                if (!osmInstance) {
                    osmInstance = new OsmInstance([name                : path,
                                                   domainPath          : path,
                                                   protocol            : protocol,
                                                   jobGroupMapping     : new OsmMapping([domain: OsmDomain.JobGroup]),
                                                   locationMapping     : new OsmMapping([domain: OsmDomain.Location]),
                                                   measuredEventMapping: new OsmMapping([domain: OsmDomain.MeasuredEvent]),
                                                   browserMapping      : new OsmMapping([domain: OsmDomain.Browser]),
                                                   pageMapping         : new OsmMapping([domain: OsmDomain.Page]),
                                                   jobMapping          : new OsmMapping([domain: OsmDomain.Job])]).save(failOnError: true, flush: true)

                }
                new ApiKey([secretKey            : apiKeyOsmTupel.key, description: "Key generated from initial properties",
                            osmInstance          : osmInstance, valid: true, allowedToTriggerFetchJobs: true, allowedToDisplayResults: true,
                            allowedToUpdateOsmUrl: true, allowedToUpdateMapping: true, allowedToCreateApiKeys: true]).save(failOnError: true, flush: true)

            }

        }
    }
    def destroy = {
    }
}
