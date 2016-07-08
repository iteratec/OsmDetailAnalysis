package de.iteratec.oms.da.external.wpt.data

import de.iteratec.oms.da.external.wpt.resolve.WPTDetailDataStrategyBuilder
import spock.lang.Specification
import spock.lang.Unroll


class WPTVersionTest extends Specification {

    @Unroll("Compare #leftVersion #name #rightVersion")
    def "CompareTo"() {
        expect:
        compare(leftVersion, rightVersion) == truth

        where:
        leftVersion             | rightVersion              |compare           |name || truth
        new WPTVersion("1.19")  | new WPTVersion("1.19")    |{v1,v2->v1 == v2} |"eq" || true
        new WPTVersion("1.18")  | new WPTVersion("1.20")    |{v1,v2->v1 == v2} |"eq" || false
        new WPTVersion("1.20")  | new WPTVersion("1.19")    |{v1,v2->v1 == v2} |"eq" || false

        new WPTVersion("1.19")  | new WPTVersion("1.19")    |{v1,v2->v1 > v2}  |"gt" || false
        new WPTVersion("1.19")  | new WPTVersion("1.18")    |{v1,v2->v1 > v2}  |"gt" || true
        new WPTVersion("1.18")  | new WPTVersion("1.19")    |{v1,v2->v1 > v2}  |"gt" || false

        new WPTVersion("1.19")  | new WPTVersion("1.19")    |{v1,v2->v1 >= v2} |"gte" || true
        new WPTVersion("1.19")  | new WPTVersion("1.18")    |{v1,v2->v1 >= v2} |"gte" || true
        new WPTVersion("1.18")  | new WPTVersion("1.19")    |{v1,v2->v1 >= v2} |"gte" || false

        new WPTVersion("1.19")  | new WPTVersion("1.19")    |{v1,v2->v1 < v2}  |"lt" || false
        new WPTVersion("1.19")  | new WPTVersion("1.18")    |{v1,v2->v1 < v2}  |"lt" || false
        new WPTVersion("1.18")  | new WPTVersion("1.19")    |{v1,v2->v1 < v2}  |"lt" || true

        new WPTVersion("1.19")  | new WPTVersion("1.19")    |{v1,v2->v1 <= v2} |"lte" || true
        new WPTVersion("1.19")  | new WPTVersion("1.18")    |{v1,v2->v1 <= v2} |"lte" || false
        new WPTVersion("1.18")  | new WPTVersion("1.19")    |{v1,v2->v1 <= v2} |"lte" || true
    }
}
