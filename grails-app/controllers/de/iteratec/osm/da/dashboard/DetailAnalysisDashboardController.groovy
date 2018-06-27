/* 
* OpenSpeedMonitor (OSM)
* Copyright 2014 iteratec GmbH
* 
* Licensed under the Apache License, Version 2.0 (the "License"); 
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
* 	http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software 
* distributed under the License is distributed on an "AS IS" BASIS, 
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
* See the License for the specific language governing permissions and 
* limitations under the License.
*/

package de.iteratec.osm.da.dashboard

import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import com.mongodb.client.MongoDatabase
import de.iteratec.osm.da.api.ApiKey
import de.iteratec.osm.da.instances.OsmInstance
import de.iteratec.osm.da.mapping.MappingService
import de.iteratec.osm.da.persistence.AssetRequestPersistenceService
import grails.converters.JSON
import grails.web.mapping.LinkGenerator
import org.joda.time.DateTime
import org.joda.time.Interval
/**
 * DetailAnalysisDashboardController
 * A controller class handles incoming web requests and performs actions such as redirects, rendering views and so on.
 */
class DetailAnalysisDashboardController {
    AssetRequestPersistenceService assetRequestPersistenceService
    MappingService mappingService
    LinkGenerator grailsLinkGenerator

    Map<String, Object> show(DetailAnalysisDashboardCommand cmd) {
        log.debug("Got DetailAnalysisDashboardCommand ... start collecting data")
        if (cmd.hasErrors()) {
            if(cmd.errors.hasFieldErrors('apiKey')) {
                sendSimpleResponseAsStream(403, "apiKey not valid")
            }
            StringWriter sw = new StringWriter()
            cmd.errors.getFieldErrors().each { fieldError ->
                sw << "Error field ${fieldError.getField()}: ${fieldError.getCode()}\n"
            }
            log.error("DetailAnalysisDashboardCommand has errors:",new Exception(sw.toString()))
            sendSimpleResponseAsStream(400, sw.toString())
            return
        }
        Map<String, Object> modelToRender = [:]
        modelToRender.put("serverBaseUrl", grailsLinkGenerator.serverBaseURL)
        modelToRender.put("osmInstance", ApiKey.findBySecretKey(cmd.apiKey)?.osmInstanceId)

        cmd.copyRequestDataToViewModelMap(modelToRender)

        fillWithDashboardData(modelToRender, cmd);

        log.debug("... data collecting for DetailAnalysisDashboardCommand done ... sending reply")
        modelToRender
    }

    def getAssetsForDataPoint(){
        log.debug("got a getAssetsForDataPointRequest with the following parameters epochTime=${request.JSON.date} host=${request.JSON.host} browser=${request.JSON.browser} mediaType=${request.JSON.mediaType} subType=${request.JSON.subtype} jobGroup=${request.JSON.jonGroup} page=${request.JSON.page}")
        def data = request.JSON
        List<Integer> browsers = data.browser.collect{Integer.valueOf(it)}
        List<Integer> pages = data.page.collect{Integer.valueOf(it)}
        List<Integer> jobGroups = data.jobGroup.collect{Integer.valueOf(it)}
        def result = assetRequestPersistenceService.getCompleteAssets(new DateTime(data.date).toDate(), data.host,browsers ,data.mediaType,data.subtype,jobGroups,pages,data.osmInstance)

        response.setContentType('JSON')
        response.status = 200
        render result
    }

    private void fillWithDashboardData(Map<String, Object> modelToRender, DetailAnalysisDashboardCommand cmd) {
        Interval selectedTimeFrame = cmd.createTimeFrameInterval()
        List<Long> jobGroupIds = cmd.selectedFolder as List
        List<Long> pageIds = cmd.selectedPages as List
        List<Long> browserIds = cmd.selectedBrowsers as List
        List<Long> locationIds = cmd.selectedLocations as List
        Integer bandwidthUp = cmd.bandwidthUp
        Integer bandwidthDown = cmd.bandwidthDown
        Integer latency = cmd.latency
        Integer packetloss = cmd.packetloss
        List<Long> measuredEventIds = cmd.selectedMeasuredEventIds as List

        Date fromDate = selectedTimeFrame.start.toDate()
        Date toDate = selectedTimeFrame.end.toDate()

        def graphData = assetRequestPersistenceService.getRequestAssetsAsJson(
                fromDate,
                toDate,
                jobGroupIds,
                pageIds,
                browserIds,
                locationIds,
                bandwidthUp,
                bandwidthDown,
                latency,
                packetloss,
                measuredEventIds,
                cmd.domainPath)

        modelToRender.put('graphData', graphData)
        modelToRender.put('fromDateInMillis', selectedTimeFrame.startMillis)
        modelToRender.put('toDateInMillis', selectedTimeFrame.endMillis)
        modelToRender.put('fromDate', fromDate)
        modelToRender.put('toDate', toDate)

        fillWithLabelAliases(modelToRender, OsmInstance.findByDomainPath(cmd.domainPath))
        fillWithI18N(modelToRender)

    }

    private void fillWithI18N(Map<String, Object> modelToRender) {
        Map<String, String> i18n = [:]

        i18n.put("allValuesEqual", message(code: 'de.iteratec.osm.da.allValuesEqual', default: 'For all values applies: '))
        i18n.put("outOf", message(code: 'de.iteratec.osm.da.outOf', default: 'out of'))
        i18n.put("selected", message(code: 'de.iteratec.osm.da.selected', default: 'selected'))
        i18n.put("records", message(code: 'de.iteratec.osm.da.records', default: 'records'))
        i18n.put("resetAll", message(code: 'de.iteratec.osm.da.resetAll', default: 'Reset All'))
        i18n.put("all", message(code: 'de.iteratec.osm.da.all', default: 'all'))
        i18n.put("applyFilters", message(code: 'de.iteratec.osm.da.applyFilters', default: 'Please click on the graph to apply filters.'))
        i18n.put("changeSelectionButtonText", message(code: 'de.iteratec.osm.da.changeselection.buttontext', default: 'Change Selection'))
        i18n.put("dataTableLengthMenu", message(code: 'de.iteratec.osm.da.datatable.lengthMenu', default: ' Display _MENU_ records per page'))
        i18n.put("dataTableZeroRecords", message(code: 'de.iteratec.osm.da.datatable.zeroRecords', default: 'Nothing found - sorry'))
        i18n.put("dataTableInfo", message(code: 'de.iteratec.osm.da.datatable.info', default: 'Showing page _PAGE_ of _PAGES_'))
        i18n.put("dataTableInfoEmpty", message(code: 'de.iteratec.osm.da.datatable.infoEmpty', default: 'No records available'))
        i18n.put("dataTableInfoFiltered", message(code: 'de.iteratec.osm.da.datatable.infoFiltered', default: '(filtered from _MAX_ total records'))
        i18n.put("dataTablePageSearch", message(code: 'de.iteratec.osm.da.datatable.pageSearch', default: 'Search'))
        i18n.put("dataTableNext", message(code: 'de.iteratec.osm.da.datatable.next', default: 'Next'))
        i18n.put("dataTablePrevious", message(code: 'de.iteratec.osm.da.datatable.previous', default: 'Previous'))
        modelToRender.put('i18n', i18n as JSON)
    }

    private void fillWithLabelAliases(Map<String, Object> modelToRender, OsmInstance osmInstance) {
        def labelAliases = [:]

        labelAliases['browser'] = mappingService.getBrowserMappings(osmInstance)
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
