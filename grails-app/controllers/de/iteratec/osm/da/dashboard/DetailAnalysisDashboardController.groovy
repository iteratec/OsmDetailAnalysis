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

import de.iteratec.osm.da.mapping.MappingService
import de.iteratec.osm.da.persistence.AssetRequestPersistenceService
import grails.converters.JSON
import org.joda.time.DateTime

import java.util.zip.GZIPOutputStream

/**
 * DetailAnalysisDashboardController
 * A controller class handles incoming web requests and performs actions such as redirects, rendering views and so on.
 */
class DetailAnalysisDashboardController {

    public static final String DATE_TIME_FORMAT_STRING = 'dd.MM.yyyy'
    public final static int MONDAY_WEEKSTART = 1

    AssetRequestPersistenceService assetRequestPersistenceService
    MappingService mappingService

    Map<String, Object> show(DetailAnalysisDashboardCommand cmd) {
        Map<String, Object> modelToRender = [:]

        cmd.copyRequestDataToViewModelMap(modelToRender)

//        if (!ControllerUtils.isEmptyRequest(params)) {
//            if (!cmd.validate()) {
//                modelToRender.put('command', cmd)
//            } else {
        fillWithDashboardData(modelToRender, cmd);
//            }
//        }

        modelToRender
    }

    private void fillWithDashboardData(Map<String, Object> modelToRender, DetailAnalysisDashboardCommand cmd) {
        Date from = cmd.from
        Date to = cmd.to

        List<Long> jobGroupIds = cmd.selectedFolder as List
        List<Long> pageIds = cmd.selectedPages as List
        List<Long> browserIds = cmd.selectedBrowsers as List
        List<Long> locationIds = cmd.selectedLocations as List
        List<Long> connectivityProfileIds = cmd.selectedConnectivityProfiles as List
        List<Long> measuredEventIds = cmd.selectedMeasuredEventIds as List
        String customConnectivityName = cmd.customConnectivityName==null ? "" : cmd.customConnectivityName

        boolean selectedAllBrowsers = cmd.selectedAllBrowsers
        boolean selectedAllLocations = cmd.selectedAllLocations
        boolean selectedAllConnectivityProfiles = cmd.selectedAllConnectivityProfiles
        boolean selectedAllMeasuredEvents = cmd.selectedAllMeasuredEvents
        boolean includeNativeConnectivity = cmd.includeNativeConnectivity
        boolean includeCustomConnectivity = cmd.includeCustomConnectivity



        def graphData = assetRequestPersistenceService.getRequestAssetsAsJson(
                from,
                to,
                jobGroupIds,
                pageIds,
                browserIds,
                selectedAllBrowsers,
                locationIds,
                selectedAllLocations,
                connectivityProfileIds,
                selectedAllConnectivityProfiles,
                measuredEventIds,
                selectedAllMeasuredEvents,
                customConnectivityName,
                includeCustomConnectivity,
                includeNativeConnectivity)

        def fromDate = new DateTime(cmd.from)
        def toDate = new DateTime(cmd.to).plusDays(1)

        modelToRender.put('graphData', graphData)
        modelToRender.put('fromDateInMillis', fromDate.millis)
        modelToRender.put('toDateInMillis', toDate.millis)

        // TODO osmInstanceId einbauen
        fillWithLabelAliases(modelToRender, 1)
    }

    private void fillWithLabelAliases(Map<String, Object> modelToRender, Long osmInstanceId) {
        def labelAliases = [:]

        labelAliases['browser'] = mappingService.getBrowserMappings(osmInstanceId)
        labelAliases['job'] = mappingService.getJobMappings(osmInstanceId)

        labelAliases = labelAliases as JSON

        modelToRender.put('labelAliases', labelAliases)
    }
}
