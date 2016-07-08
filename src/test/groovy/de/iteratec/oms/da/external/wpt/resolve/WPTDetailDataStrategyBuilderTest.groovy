package de.iteratec.oms.da.external.wpt.resolve

import de.iteratec.oms.da.external.wpt.data.WPTVersion
import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
class WPTDetailDataStrategyBuilderTest extends Specification {

    @Unroll
    def "get only compatible strategies"() {
        expect:
        WPTDetailDataStrategyBuilder.getStrategyForVersion(getVersion).compatibleWithVersion(actualTestVersion) == truth

        where:
        getVersion              | actualTestVersion      || truth
        new WPTVersion("2.19")  | new WPTVersion("2.19") || true
        new WPTVersion("2.19")  | new WPTVersion("2.20") || true
        new WPTVersion("2.19")  | new WPTVersion("2.18") || false
        new WPTVersion("2.18")  | new WPTVersion("2.18") || true
        new WPTVersion("2.18")  | new WPTVersion("2.19") || false

    }
}
