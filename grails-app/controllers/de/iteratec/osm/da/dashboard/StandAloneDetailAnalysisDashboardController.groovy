package de.iteratec.osm.da.dashboard

import de.iteratec.osm.da.instances.OsmInstance
import de.iteratec.osm.da.mapping.MappingService
import de.iteratec.osm.da.persistence.AssetRequestPersistenceService
import grails.converters.JSON
import grails.web.mapping.LinkGenerator
import groovyx.net.http.ContentType
import org.joda.time.DateTime

class StandAloneDetailAnalysisDashboardController {
    AssetRequestPersistenceService assetRequestPersistenceService
    MappingService mappingService
    LinkGenerator grailsLinkGenerator

    Map<String, Object> show(DetailAnalysisDashboardCommand cmd) {
        if (cmd.hasErrors()) {
            StringWriter sw = new StringWriter()
            cmd.errors.getFieldErrors().each { fieldError ->
                sw << "Error field ${fieldError.getField()}: ${fieldError.getCode()}\n"
            }
            sendSimpleResponseAsStream(400, sw.toString())
            return
        }
        Map<String, Object> modelToRender = [:]
        modelToRender.put("serverBaseUrl",grailsLinkGenerator.serverBaseURL)

        cmd.copyRequestDataToViewModelMap(modelToRender)

        fillWithDashboardData(modelToRender, cmd);

        modelToRender
    }

    private void fillWithDashboardData(Map<String, Object> modelToRender, DetailAnalysisDashboardCommand cmd) {
        Date from = cmd.from
        Date to = cmd.to

        List<Long> jobGroupIds = cmd.selectedFolder as List
        List<Long> pageIds = cmd.selectedPages as List
        List<Long> browserIds = cmd.selectedBrowsers as List
        List<Long> locationIds = cmd.selectedLocations as List
        Integer bandwidthUp = cmd.bandwidthUp
        Integer bandwidthDown = cmd.bandwidthDown
        Integer latency = cmd.latency
        Integer packetloss = cmd.packetloss
        List<Long> measuredEventIds = cmd.selectedMeasuredEventIds as List

        boolean selectedAllBrowsers = cmd.selectedAllBrowsers
        boolean selectedAllLocations = cmd.selectedAllLocations
        boolean selectedAllConnectivityProfiles = cmd.selectedAllConnectivityProfiles
        boolean selectedAllMeasuredEvents = cmd.selectedAllMeasuredEvents



        def graphData = assetRequestPersistenceService.getRequestAssetsAsJson(
                from,
                to,
                jobGroupIds,
                pageIds,
                browserIds,
                selectedAllBrowsers,
                locationIds,
                selectedAllLocations,
                selectedAllConnectivityProfiles,
                bandwidthUp,
                bandwidthDown,
                latency,
                packetloss,
                measuredEventIds,
                selectedAllMeasuredEvents)

        def fromDate = new DateTime(cmd.from)
        def toDate = new DateTime(cmd.to).plusDays(1)

        modelToRender.put('graphData', graphData)
        modelToRender.put('fromDateInMillis', fromDate.millis)
        modelToRender.put('toDateInMillis', toDate.millis)

        fillWithLabelAliases(modelToRender, OsmInstance.findByUrl(cmd.osmUrl))
    }
    def getAssetsForDataPoint(){
        def result = assetRequestPersistenceService.getCompleteAssets(new DateTime(request.JSON.date).toDate(),request.JSON.jobId ,request.JSON.host,request.JSON.browser,request.JSON.mediaType,request.JSON.subtype,request.JSON.jobGroup,request.JSON.page)
        response.setContentType(ContentType.JSON.toString())
        response.status = 200
        render result
    }

    private void fillWithLabelAliases(Map<String, Object> modelToRender, OsmInstance osmInstance) {
        def labelAliases = [:]

        labelAliases['browser'] = mappingService.getBrowserMappings(osmInstance)
        labelAliases['job'] = mappingService.getJobMappings(osmInstance)
        labelAliases['page'] = mappingService.getPageMappings(osmInstance)
        labelAliases['measuredEvent'] = mappingService.getMeasuredEventMappings(osmInstance)
        labelAliases['jobGroup'] = mappingService.getJobGroupMappings(osmInstance)

        labelAliases = labelAliases as JSON

        modelToRender.put('labelAliases', labelAliases)
    }
    private void sendSimpleResponseAsStream(Integer httpStatus, String message) {
        response.setContentType('text/plain;charset=UTF-8')
        response.status = httpStatus
        render message
    }
}
