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

import de.iteratec.osm.da.instances.OsmInstance
import de.iteratec.osm.da.mapping.MappingService
import de.iteratec.osm.da.persistence.AssetRequestPersistenceService
import grails.converters.JSON
import grails.web.mapping.LinkGenerator
import groovyx.net.http.ContentType
import org.joda.time.DateTime

import java.text.SimpleDateFormat
import java.util.zip.GZIPOutputStream

/**
 * DetailAnalysisDashboardController
 * A controller class handles incoming web requests and performs actions such as redirects, rendering views and so on.
 */
class DetailAnalysisDashboardController {

    public static final String DATE_TIME_FORMAT_STRING = 'dd.MM.yyyy'

    AssetRequestPersistenceService assetRequestPersistenceService
    MappingService mappingService
    LinkGenerator grailsLinkGenerator

    Map<String, Object> show(DetailAnalysisDashboardCommand cmd) {
        log.debug("Got DetailAnalysisDashboardCommand ... start collecting data")
        if (cmd.hasErrors()) {
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

        cmd.copyRequestDataToViewModelMap(modelToRender)

        fillWithDashboardData(modelToRender, cmd);

        log.debug("... data collecting for DetailAnalysisDashboardCommand done ... sending reply")
        modelToRender
    }

    def getAssetsForDataPoint(){
        log.debug("got a getAssetsForDataPointRequest with the following parameters epochTime=${request.JSON.date} host=${request.JSON.host} browser=${request.JSON.browser} mediaType=${request.JSON.mediaType} subType=${request.JSON.subtype} jobGroup=${request.JSON.jonGroup} page=${request.JSON.page}")
        List<Integer> browsers = []
        request.JSON.browser.each{
            browsers.add(Integer.valueOf(it))
        }
        List<Integer> pages = []
        request.JSON.page.each{
            pages.add(Integer.valueOf(it))
        }
        List<Integer> jobGroups = []
        request.JSON.jobGroup.each{
            jobGroups.add(Integer.valueOf(it))
        }
        def result = assetRequestPersistenceService.getCompleteAssets(new DateTime(request.JSON.date).toDate(), request.JSON.host,browsers ,request.JSON.mediaType,request.JSON.subtype,jobGroups,pages)

        response.setContentType(ContentType.JSON.toString())
        response.status = 200
        render result
    }

    private void fillWithDashboardData(Map<String, Object> modelToRender, DetailAnalysisDashboardCommand cmd) {
        Date from
        Date to
        if(!cmd.toDate || !cmd.fromDate) {
            println(cmd.toDate)
            println(cmd.fromDate)
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm")
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
            def toHour = 0
            if (cmd.toHour) toHour = simpleDateFormat.parse(cmd.toHour).time
            def fromHour = 0
            if (cmd.fromHour) fromHour = simpleDateFormat.parse(cmd.fromHour).time
            from = new Date(cmd.from.time + fromHour)
            to = new Date(cmd.to.time + toHour)
        }else{
            from = cmd.fromDate
            to = cmd.toDate
        }
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
                selectedAllMeasuredEvents,
                cmd.osmUrl)

        modelToRender.put('graphData', graphData)
        modelToRender.put('fromDateInMillis', from.time)
        modelToRender.put('toDateInMillis', to.time)
        modelToRender.put('fromDate', from)
        modelToRender.put('toDate', to)

        fillWithLabelAliases(modelToRender, OsmInstance.findByUrl(cmd.osmUrl))
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
