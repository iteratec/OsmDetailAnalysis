package de.iteratec.oms.da.external.wpt.resolve

import de.iteratec.oms.da.external.wpt.data.WPTDetailResult
import de.iteratec.oms.da.external.wpt.data.WPTVersion
import de.iteratec.osm.da.external.FetchJob

/**
 * Created by benni on 08.07.16.
 */
class WPTDetailDataOldStrategy implements WPTDetailDataStrategyI{

    static WPTVersion maximumVersion = new WPTVersion("2.18")

    @Override
    WPTDetailResult getResult(FetchJob fetchJob) {
        //TODO implement me
        return null
    }

    @Override
    boolean compatibleWithVersion(WPTVersion version) {
        //TODO implement me
        return version <= maximumVersion
    }
}
