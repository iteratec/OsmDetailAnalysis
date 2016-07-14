package de.iteratec.osm.da.external.wpt.resolve

import de.iteratec.osm.da.external.wpt.data.WPTVersion
import de.iteratec.osm.da.external.wpt.data.WptDetailResult
import de.iteratec.osm.da.external.FetchJob

interface WptDetailDataStrategyI {

    abstract WptDetailResult getResult(FetchJob fetchJob)
    abstract boolean compatibleWithVersion(WPTVersion version)
}
