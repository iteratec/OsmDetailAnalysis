package de.iteratec.oms.da.external.wpt.resolve

import de.iteratec.oms.da.external.wpt.data.WPTVersion


class WPTDetailDataStrategyBuilder {

    static List<WPTDetailDataStrategyI> strategies = [new WPTDetailDataDefaultStrategy(), new WPTDetailDataOldStrategy()]

    static WPTDetailDataStrategyI getStrategyForVersion(WPTVersion version){
        return strategies.find{it.compatibleWithVersion(version)}
    }
}
