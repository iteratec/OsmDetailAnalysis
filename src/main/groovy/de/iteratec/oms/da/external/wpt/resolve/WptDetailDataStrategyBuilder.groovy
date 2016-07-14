package de.iteratec.oms.da.external.wpt.resolve

import de.iteratec.oms.da.external.wpt.data.WPTVersion

/**
 * Decides which strategy should be used to download the detail data from wpt
 */
class WptDetailDataStrategyBuilder {

    /**
     * Maps WptVersions to the correct Strategy. If a Version wasn't found in the map, we will look in our strategy List for the correct strategy.
     */
    static HashMap<WPTVersion, WptDetailDataStrategyI> cache = [:].withDefault {findStrategy(it)}
    static List<WptDetailDataStrategyI> strategies = [new WptDetailDataDefaultStrategy(), new WptDetailDataOldStrategy()]

    static WptDetailDataStrategyI getStrategyForVersion(WPTVersion version){
        return cache[version]
    }

    private static WptDetailDataStrategyI findStrategy(WPTVersion version){
        strategies.find{it.compatibleWithVersion(version)}
    }
}
