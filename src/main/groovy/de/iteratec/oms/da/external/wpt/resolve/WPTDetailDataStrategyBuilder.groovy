package de.iteratec.oms.da.external.wpt.resolve

import de.iteratec.oms.da.external.wpt.data.WPTVersion


class WPTDetailDataStrategyBuilder {

    static WPTDetailDataDefaultStrategy defaultStrategy

    static WPTDetailDataStrategyI getStrategyForVersion(WPTVersion version){
        //TODO decide which version to use
        if(!defaultStrategy) defaultStrategy = new WPTDetailDataDefaultStrategy()
        return defaultStrategy
    }
}
