import de.iteratec.osm.da.ConfigService
import de.iteratec.osm.da.api.ApiKey
import de.iteratec.osm.da.instances.OsmInstance
import de.iteratec.osm.da.instances.OsmMapping
import de.iteratec.osm.da.mapping.OsmDomain
import de.iteratec.osm.da.migration.MigrationUtil
import de.iteratec.osm.da.report.external.GraphiteServer
import de.iteratec.osm.da.report.external.HealthReportService
import de.iteratec.osm.da.util.UrlUtil
import de.iteratec.osm.da.wpt.WptDetailResultDownloadService

class BootStrap {

    HealthReportService healthReportService
    ConfigService configService
    WptDetailResultDownloadService wptDetailResultDownloadService

    def init = { servletContext ->
        MigrationUtil.executeChanges()
        initOsmInstances()
        initHealthReporting()
        wptDetailResultDownloadService.startQueueExecution()
    }
    def destroy = {
    }

    private void initOsmInstances() {
        log.debug("remove old api keys")
        ApiKey.list()*.delete(flush: true)
        def amount = 0
        HashMap<String, String> apiKeyMap = [:]
        configService.getApiKeys().each { unnecessaryParameterForcedUpponUsByGrails, apiKeyOsmTup ->
            apiKeyMap[apiKeyOsmTup.osmUrl] = apiKeyOsmTup.key
        }
        apiKeyMap.each { String osmUrl, String key ->
            OsmInstance osmInstance = ensureOsmInstanceExists(osmUrl)
            new ApiKey([secretKey            : key, description: "Key generated from initial properties",
                        osmInstance          : osmInstance, valid: true, allowedToTriggerFetchJobs: true, allowedToDisplayResults: true,
                        allowedToUpdateOsmUrl: true, allowedToUpdateMapping: true, allowedToCreateApiKeys: true]).save(failOnError: true, flush: true)
            ++amount
        }
        log.debug("created $amount API keys from config")
    }

    OsmInstance ensureOsmInstanceExists(String configOsmUrl) {
        String path = UrlUtil.removeHypertextProtocols(UrlUtil.appendTrailingSlash(configOsmUrl))
        OsmInstance osmInstance = OsmInstance.findByDomainPath(path)
        if (!osmInstance) {
            osmInstance = new OsmInstance([
                                           jobGroupMapping     : new OsmMapping([domain: OsmDomain.JobGroup]),
                                           locationMapping     : new OsmMapping([domain: OsmDomain.Location]),
                                           measuredEventMapping: new OsmMapping([domain: OsmDomain.MeasuredEvent]),
                                           browserMapping      : new OsmMapping([domain: OsmDomain.Browser]),
                                           pageMapping         : new OsmMapping([domain: OsmDomain.Page]),
                                           jobMapping          : new OsmMapping([domain: OsmDomain.Job])])
            osmInstance.setUrl(configOsmUrl)
            osmInstance.save(failOnError: true, flush: true)
        }
        return osmInstance
    }

    def initHealthReporting() {
        String serverAddress = configService.getGraphiteServerAddress()
        int carbonPort = configService.getGraphiteCarbonPort()
        if (serverAddress && carbonPort) {
            GraphiteServer graphiteServer = GraphiteServer.findByServerAddressAndPort(serverAddress, carbonPort) ?: new GraphiteServer(
                    serverAddress: serverAddress,
                    port: carbonPort
            ).save(failOnError: true)
            if (graphiteServer) {
                log.info("Starting health metric reporting for osmda to Graphite instance:\n${graphiteServer}")
                healthReportService.handleGraphiteServer(graphiteServer)
            } else {
                log.error("Graphite server couldn't be created: ${graphiteServer}")
            }
        }
    }
}
