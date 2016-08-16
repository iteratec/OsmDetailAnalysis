package de.iteratec.osm.da.wpt

import de.iteratec.osm.da.HttpRequestService
import de.iteratec.osm.da.wpt.data.WPTVersion
import de.iteratec.osm.da.wpt.resolve.WptDetailDataDefaultStrategy
import de.iteratec.osm.da.wpt.resolve.WptDetailDataOldStrategy
import de.iteratec.osm.da.wpt.resolve.WptDetailDataStrategyI

/**
 * Decides which strategy should be used to download the detail data from wpt
 */
class WptDetailDataStrategyService {

    HttpRequestService httpRequestService

    /**
     * Maps WptVersions to the correct Strategy. If a Version wasn't found in the map, we will look in our strategy List for the correct strategy.
     */
    Map<WPTVersion, WptDetailDataStrategyI> cache = [:].withDefault {findStrategy(it)}
    List<WptDetailDataStrategyI> strategies = [new WptDetailDataDefaultStrategy(httpRequestService: httpRequestService), new WptDetailDataOldStrategy()]

    WptDetailDataStrategyI getStrategyForVersion(WPTVersion version){
        return cache[version]
    }

    private WptDetailDataStrategyI findStrategy(WPTVersion version){
        return strategies.find{it.compatibleWithVersion(version)}
    }
}
