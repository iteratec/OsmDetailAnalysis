package de.iteratec.osm.da.external.wpt.resolve

import de.iteratec.osm.da.external.wpt.data.WptDetailResult
import de.iteratec.osm.da.external.wpt.data.WPTVersion
import de.iteratec.osm.da.external.FetchJob

/**
 * Created by benni on 08.07.16.
 */
class WptDetailDataOldStrategy implements WptDetailDataStrategyI{

    static WPTVersion maximumVersion = WPTVersion.get("2.18")

    @Override
    WptDetailResult getResult(FetchJob fetchJob) {
        //TODO implement me
        return null
    }

    @Override
    boolean compatibleWithVersion(WPTVersion version) {
        //TODO implement me
        return version <= maximumVersion
    }
}
