package de.iteratec.osm.da.wpt.resolve

import de.iteratec.osm.da.wpt.WptDetailDataStrategyService
import de.iteratec.osm.da.wpt.data.WPTVersion
import grails.test.mixin.TestFor
import grails.util.Holders
import spock.lang.Specification
import spock.lang.Unroll

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(WptDetailDataStrategyService)
class WptDetailDataStrategyServiceTest extends Specification {

    @Unroll
    def "get only compatible strategies"() {
        expect:
        service.getStrategyForVersion(getVersion).compatibleWithVersion(actualTestVersion) == truth

        where:
        getVersion              | actualTestVersion      || truth
        WPTVersion.get("2.19")  | WPTVersion.get("2.19") || true
        WPTVersion.get("2.19")  | WPTVersion.get("2.20") || true
        WPTVersion.get("2.19")  | WPTVersion.get("2.18") || false
        WPTVersion.get("2.18")  | WPTVersion.get("2.18") || true
        WPTVersion.get("2.18")  | WPTVersion.get("2.19") || false
    }

}
