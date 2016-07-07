package de.iteratec.oms.da.external.wpt.resolve

import de.iteratec.oms.da.external.wpt.data.WPTDetailResult
import de.iteratec.oms.da.external.wpt.data.WPTVersion
import de.iteratec.osm.da.external.FetchJob

interface WPTDetailDataStrategyI{

    abstract WPTDetailResult getResult(FetchJob fetchJob)
    abstract boolean compatibleWithVersion(WPTVersion version)
}
