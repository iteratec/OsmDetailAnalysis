package de.iteratec.oms.da.external.wpt.resolve

import de.iteratec.oms.da.external.wpt.data.WPTVersion
import de.iteratec.oms.da.external.wpt.data.WptDetailResult
import de.iteratec.osm.da.external.FetchJob

interface WptDetailDataStrategyI {

    abstract WptDetailResult getResult(FetchJob fetchJob)
    abstract boolean compatibleWithVersion(WPTVersion version)
}
