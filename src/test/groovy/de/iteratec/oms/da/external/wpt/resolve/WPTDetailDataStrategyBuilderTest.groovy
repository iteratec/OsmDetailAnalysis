package de.iteratec.oms.da.external.wpt.resolve

import de.iteratec.oms.da.external.wpt.data.WPTVersion
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
        WPTVersion.get("2.19")  | WPTVersion.get("2.19") || true
        WPTVersion.get("2.19")  | WPTVersion.get("2.20") || true
        WPTVersion.get("2.19")  | WPTVersion.get("2.18") || false
        WPTVersion.get("2.18")  | WPTVersion.get("2.18") || true
        WPTVersion.get("2.18")  | WPTVersion.get("2.19") || false
    }

}
