package de.iteratec.oms.da.wpt.resolve

import de.iteratec.oms.da.wpt.data.WPTDetailResult
import de.iteratec.oms.da.wpt.data.WPTVersion
import de.iteratec.osm.da.har.FetchJob

interface WPTDetailDataStrategyI{

    abstract WPTDetailResult getResult(FetchJob fetchJob)
    abstract boolean compatibleWithVersion(WPTVersion version)
}
