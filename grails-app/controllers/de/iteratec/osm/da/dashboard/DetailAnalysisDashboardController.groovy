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
        Map<String, Object> modelToRender = [:]//constructStaticViewData()

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

    /**
     * <p>
     * Constructs the static view data of the {@link #show(de.iteratec.osm.result.DetailAnalysisDashboardCommand)}
     * view as {@link Map}.
     * </p>
     *
     * <p>
     * This map does always contain all available data for selections, previous
     * selections are not considered.
     * </p>
     *
     * @return A Map containing the static view data which are accessible
     *         through corresponding keys. The Map is modifiable to add
     *         further data. Subsequent calls will never return the same
     *         instance.
     */
    private Map<String, Object> constructStaticViewData() {
        Map<String, Object> result = [:]

//
//        // JobGroups
//        List<JobGroup> jobGroups = eventResultDashboardService.getAllJobGroups()
//        result.put('folders', jobGroups)
//
//        // Pages
//        List<Page> pages = eventResultDashboardService.getAllPages()
//        result.put('pages', pages)
//
//        // MeasuredEvents
//        List<MeasuredEvent> measuredEvents = eventResultDashboardService.getAllMeasuredEvents()
//        result.put('measuredEvents', measuredEvents)
//
//        // Browsers
//        List<Browser> browsers = eventResultDashboardService.getAllBrowser()
//        result.put('browsers', browsers)
//
//        // Locations
//        List<Location> locations = eventResultDashboardService.getAllLocations()
//        result.put('locations', locations)
//
//        // ConnectivityProfiles
//        result['connectivityProfiles'] = eventResultDashboardService.getAllConnectivityProfiles()
//
//        // JavaScript-Utility-Stuff:
//        result.put("dateFormat", DATE_TIME_FORMAT_STRING)
//        result.put("weekStart", MONDAY_WEEKSTART)
//
//        // --- Map<PageID, Set<MeasuredEventID>> for fast view filtering:
//        Map<Long, Set<Long>> eventsOfPages = new HashMap<Long, Set<Long>>()
//        for (Page eachPage : pages) {
//            Set<Long> eventIds = new HashSet<Long>();
//
//            Collection<Long> ids = measuredEvents.findResults {
//                it.testedPage.getId() == eachPage.getId() ? it.getId() : null
//            }
//            if (!ids.isEmpty()) {
//                eventIds.addAll(ids);
//            }
//
//            eventsOfPages.put(eachPage.getId(), eventIds);
//        }
//        result.put('eventsOfPages', eventsOfPages);
//
//        // --- Map<BrowserID, Set<LocationID>> for fast view filtering:
//        Map<Long, Set<Long>> locationsOfBrowsers = new HashMap<Long, Set<Long>>()
//        for (Browser eachBrowser : browsers) {
//            Set<Long> locationIds = new HashSet<Long>();
//
//            Collection<Long> ids = locations.findResults {
//                it.browser.getId() == eachBrowser.getId() ? it.getId() : null
//            }
//            if (!ids.isEmpty()) {
//                locationIds.addAll(ids);
//            }
//
//            locationsOfBrowsers.put(eachBrowser.getId(), locationIds);
//        }
//        result.put('locationsOfBrowsers', locationsOfBrowsers);
//
//        result.put("selectedChartType", 0);
//        result.put("warnAboutExceededPointsPerGraphLimit", false);
//
//        result.put("tagToJobGroupNameMap", jobGroupDaoService.getTagToJobGroupNameMap())
//
//        // Done! :)
        return result;
    }

    private void fillWithDashboardData(Map<String, Object> modelToRender, DetailAnalysisDashboardCommand cmd) {
        Date from = cmd.from
        Date to = cmd.to

        List<Long> jobGroupIds = cmd.selectedFolder as List
        List<Long> pageIds = cmd.selectedPages as List

        List<Long> browserIds = cmd.selectedBrowsers as List
        List<Long> locationIds = cmd.selectedLocations as List

//        def browserIds = (cmd.selectedAllBrowsers ? Browser.list()*.id : cmd.selectedBrowsers) as List
//        def locationIds = (cmd.selectedAllLocations ? Location.list()*.id : cmd.selectedLocations) as List
//        def connectivityList = (cmd.selectedAllConnectivityProfiles ? ConnectivityProfile.list()*.name : cmd.selectedConnectivityProfiles) as List

        def graphData = assetRequestPersistenceService.getRequestAssetsAsJson(from, to, jobGroupIds, pageIds, browserIds, locationIds)

        def fromDate = new DateTime(cmd.from)
        def toDate = new DateTime(cmd.to).plusDays(1)

        modelToRender.put('graphData', graphData)
        modelToRender.put('fromDateInMillis', fromDate.millis)
        modelToRender.put('toDateInMillis', toDate.millis)

        fillWithLabelAliases(modelToRender)
    }

    private void fillWithLabelAliases(Map<String, Object> modelToRender) {
        def labelAliases = [:]

        labelAliases['browser'] = [:]
        labelAliases['browser'].put(4, mappingService.getNameForBrowserId(1l, 4l))
//
//        Browser.list().each {
//            labelAliases['browser'].put(it.id.toString(), it.name)
//        }
//
        labelAliases = labelAliases as JSON

        println "LABEL ALIASES " + labelAliases

        modelToRender.put('labelAliases', labelAliases)
    }
}
