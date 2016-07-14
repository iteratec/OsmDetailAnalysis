package de.iteratec.osm.da.wpt.resolve

import de.iteratec.osm.da.wpt.data.WPTVersion
import de.iteratec.osm.da.wpt.data.WptDetailResult
import de.iteratec.osm.da.fetch.FetchJob

interface WptDetailDataStrategyI {

    abstract WptDetailResult getResult(FetchJob fetchJob)
    abstract boolean compatibleWithVersion(WPTVersion version)
}
